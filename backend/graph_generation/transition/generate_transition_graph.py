# Usage
# python generate_transition_graph.py <INERVAL_LENGTH (in minutes)> <DATA_DIR> <OUTPUT_DIR>
# Example: python generate_transition_graph.py 60 ../../data/nyc_gov_trip_data/ ../../graph/

from quantization import Quantizer
import sys
import numpy as np
import os
import math
import sqlite3
import re
from pyspark import SparkConf, SparkContext

# Outline
# main function: reads csv
# load Quantizer
# For each line, take the origin/destination and quantize them using Quantizer that uses k quantization bins
# Add that to kxk matrix
# Once we're done calculate transition matrix and write to output.json

NEAREST_NODE_TOLERANCE = 0.5 # If dest or origin is greater than this distance in km, throw trip away


def map_row(row, pu_time_index, interval_length):
    import dateutil.parser
    try:
        pu_date = dateutil.parser.parse(row[pu_time_index])
        pu_minutes = 60*pu_date.time().hour + pu_date.time().minute
        return int(pu_minutes/interval_length), row
    except (ValueError, IndexError):
        return None


def generate_trip_graph(entry, header, graph_db):
    quantizer = Quantizer(graph_db)
    trip_graph = np.zeros((quantizer.num_nodes(), quantizer.num_nodes()), dtype=np.int)

    pu_lat_index = header.index('pickup_latitude')
    pu_long_index = header.index('pickup_longitude')
    do_lat_index = header.index('dropoff_latitude')
    do_long_index = header.index('dropoff_longitude')

    key = entry[0]
    rows = entry[1]
    for row in rows:
        try:
            pu_lat = float(row[pu_lat_index])
            pu_long = float(row[pu_long_index])
            do_lat = float(row[do_lat_index])
            do_long = float(row[do_long_index])

            pu_node, pu_dist = quantizer.query((pu_lat, pu_long))
            do_node, do_dist = quantizer.query((do_lat, do_long))

            pu_dist = pu_dist * 6371 * math.pi /180  # Angle to km
            do_dist = do_dist * 6371 * math.pi / 180  # Angle to km

            if pu_dist <= NEAREST_NODE_TOLERANCE and do_dist <= NEAREST_NODE_TOLERANCE and pu_node != do_node:
                trip_graph[pu_node.index, do_node.index] += 1
        except (ValueError, IndexError):
            continue


    return key, trip_graph


def write_trip_graph(entry, transition_group_id, interval_length, graph_db):
    interval_index = entry[0]
    trip_graph = entry[1]

    quantizer = Quantizer(graph_db)
    # outbound_degrees = trip_graph.sum(axis = 1)
    # transition_probabilities = trip_graph.astype(np.float) / outbound_degrees[:, np.newaxis]
    # transition_probabilities = np.nan_to_num(transition_probabilities) # Replace nans with 0's

    interval_start = interval_index * interval_length
    interval_end = min((interval_index + 1) * interval_length, 24 * 60)

    transition_graph_row = (transition_group_id, interval_start, interval_end)

    # list of (transition_graph_id, source_node_id, dest_node_id, weight)
    l_edges = []

    for source_index in range(quantizer.num_nodes()):
        for dest_index in range(quantizer.num_nodes()):
            weight = trip_graph[source_index, dest_index]
            src_node_id = quantizer.nodes[source_index].db_id
            dest_node_id = quantizer.nodes[dest_index].db_id
            if weight > 0:
                l_edges.append((src_node_id, dest_node_id, weight))

    try:
        with sqlite3.connect(graph_db) as conn:
            conn.execute('PRAGMA foreign_keys = ON')

            transition_graph_id = conn.execute('INSERT INTO transition_graph (transition_group_id, interval_start, interval_end) '
                         'values (?,?,?)', transition_graph_row).lastrowid

            l_edges = [(transition_graph_id,) + edge for edge in l_edges]

            conn.execute('PRAGMA foreign_keys = ON')
            conn.executemany('INSERT INTO transition_edge (transition_graph_id, '
                             'src_node_id, dest_node_id, weight)'
                             'values (?,?,?,?)', l_edges)

            conn.commit()
    except (sqlite3.IntegrityError, ValueError):
        import traceback
        sys.stderr.write('Error when writing graph to transition group {} for interval {} to {}:\n{}\n'
                         .format(transition_group_id, interval_start, interval_end, traceback.format_exc()))


def _get_transition_group_id(transition_group_name):
    match = re.search(r'-(\d\d)(?!d)', transition_group_name)
    month = int(match.group(1))

    match = re.search(r'(\d{4})-', transition_group_name)
    year = int(match.group(1))

    match = re.search(r'^(.+?)_', transition_group_name)
    taxi_type = match.group(1)

    with sqlite3.connect(graph_db) as conn:
        conn.execute('PRAGMA foreign_keys = ON')

        cursor = conn.execute('SELECT id from transition_group where name=?', (transition_group_name,))
        result = cursor.fetchone()

        if result is None:
            transition_group_id = conn.execute('INSERT into transition_group (name) VALUES (?)', (transition_group_name,)).lastrowid
            transition_period_id = conn.execute(
                'INSERT INTO transition_period (transition_group_id, month, year, taxi_type) VALUES (?,?,?,?)',
                (transition_group_id, month, year, taxi_type)).lastrowid
        else:
            transition_group_id = result[0]
            cursor = conn.execute('SELECT id, month, year, taxi_type '
                                  'from transition_period where transition_group_id=?', (transition_group_id,))
            results = cursor.fetchall()
            if (len(results) != 1):
                raise ValueError(
                    'A transition group with the same name was found and it has incompatible transition_period(s)')
            result = results[0]
            if (result[1] != month or result[2] != year or result[3] != taxi_type):
                raise ValueError(
                    'A transition group with the same name was found and it has incompatible transition_period(s)')
            transition_period_id = result[0]

        conn.commit()
    return transition_group_id


if __name__ == '__main__':
    interval_length = int(sys.argv[1])

    data_path = sys.argv[2]
    if os.path.isfile(data_path):
        data_files = [data_path]
    elif os.path.isdir(data_path):
        data_files = [os.path.join(data_path, data_file_name) for data_file_name in os.listdir(data_path)]
        data_files = [data_file for data_file in data_files if os.path.isfile(data_file)]
    else:
        raise ValueError('{} is not a valid file path'.format(data_path))

    graph_db = sys.argv[3]

    for data_file in data_files:
        transition_group_name = os.path.split(data_file)[1]
        transition_group_id = _get_transition_group_id(transition_group_name)

        script_dir = os.path.split(os.path.realpath(__file__))[0]
        pyFiles = [os.path.join(script_dir, py_file) for py_file in ['quantization.py']]
        sc = SparkContext("local", "Transition Graph", pyFiles=pyFiles)
        taxi_data = sc.textFile(data_file) \
            .map(lambda row: row.split(',')) \
            .filter(lambda row: len(row) > 1)

        header = taxi_data.first()
        header = [column.strip() for column in header]

        for i, column in enumerate(header):
            if 'pickup_datetime' in column:
                pu_time_index = i
                break

        taxi_graphs = taxi_data \
            .map(lambda row: map_row(row, pu_time_index, interval_length)) \
            .filter(lambda row: row is not None) \
            .groupByKey() \
            .map(lambda entry: generate_trip_graph(entry, header, graph_db)) \
            .map(lambda entry: write_trip_graph(entry, transition_group_id, interval_length, graph_db)) \
            .collect()