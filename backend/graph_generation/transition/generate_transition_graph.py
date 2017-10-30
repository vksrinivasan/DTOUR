# Usage
# python generate_transition_graph.py <INERVAL_START> <INTERVAL_END> <DATA_DIR> <OUTPUT_DIR>
# Example: python generate_transition_graph.py 06:00 07:00 ../../data/nyc_gov_trip_data/ ../../graph/

from quantization import Quantizer
import sys
import datetime as dt
import dateutil.parser
import csv
import numpy as np
import json
import os
import errno
import math
import sqlite3
import re

# Outline
# main function: reads csv
# load Quantizer
# For each line, take the origin/destination and quantize them using Quantizer that uses k quantization bins
# Add that to kxk matrix
# Once we're done calculate transition matrix and write to output.json

NEAREST_NODE_TOLERANCE = 0.5 # If dest or origin is greater than this distance in km, throw trip away


def generate_trip_graph(data_file, quantizer, interval_start, interval_end):
    trip_graph = np.zeros((quantizer.num_nodes(), quantizer.num_nodes()), dtype=np.int)

    with open(data_file) as f:
        rows = (row for row in csv.reader(f) if len(row) > 0)
        header = rows.next()

        header = [column.strip() for column in header]
        pu_lat_index = header.index('pickup_latitude')
        pu_long_index = header.index('pickup_longitude')
        do_lat_index = header.index('dropoff_latitude')
        do_long_index = header.index('dropoff_longitude')
        for i, column in enumerate(header):
            if 'pickup_datetime' in column:
                pu_time_index = i
                break

        for row in rows:
            pu_date = dateutil.parser.parse(row[pu_time_index])
            if pu_date.time() >= interval_start and pu_date.time() < interval_end:
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
                except ValueError:
                    continue


    return trip_graph


if __name__ == '__main__':
    hour1, minute1 = sys.argv[1].split(':')
    interval_start = dt.time(hour=int(hour1), minute=int(minute1))

    hour2, minute2 = sys.argv[2].split(':')
    if int(hour2) > 23:
        interval_end = dt.time(hour=23, minute=59, second=59)
    else:
        interval_end = dt.time(hour=int(hour2), minute=int(minute2))

    data_path = sys.argv[3]
    if os.path.isfile(data_path):
        data_files = [data_path]
    elif os.path.isdir(data_path):
        data_files = [os.path.join(data_path, data_file_name) for data_file_name in os.listdir(data_path)]
        data_files = [data_file for data_file in data_files if os.path.isfile(data_file)]
    else:
        raise ValueError('{} is not a valid file path'.format(data_path))

    graph_db = sys.argv[4]
    quantizer = Quantizer(graph_db)

    for data_file in data_files:
        trip_graph = generate_trip_graph(data_file, quantizer, interval_start, interval_end)
        # outbound_degrees = trip_graph.sum(axis = 1)
        # transition_probabilities = trip_graph.astype(np.float) / outbound_degrees[:, np.newaxis]
        # transition_probabilities = np.nan_to_num(transition_probabilities) # Replace nans with 0's

        data_file_name = os.path.split(data_file)[1]

        try:
            match = re.search(r'-(\d\d)(?!d)', data_file_name)
            month = int(match.group(1))
        except (ValueError, AttributeError):
            month = 0
            sys.stderr.write('Could not parse month from {}\n'.format(data_file_name))

        try:
            match = re.search(r'(\d{4})-', data_file_name)
            year = int(match.group(1))
        except (ValueError, AttributeError):
            year = 0
            sys.stderr.write('Could not parse year from {}\n'.format(data_file_name))

        try:
            match = re.search(r'^(.+?)_', data_file_name)
            taxi_type = match.group(1)
        except AttributeError:
            taxi_type = None
            sys.stderr.write('Could not parse taxi type from {}\n'.format(data_file_name))

        interval_start = int(hour1)*60 + int(minute1)
        interval_end = int(hour2)*60 + int(minute2)
        graph_name = '{}_{}_{}'.format(os.path.splitext(data_file_name)[0], interval_start, interval_end)

        transition_graph_row = (graph_name, month, year, taxi_type, interval_start, interval_end)
        with sqlite3.connect(graph_db) as conn:
            conn.execute('PRAGMA foreign_keys = ON')
            conn.execute('INSERT INTO transition_graphs (name, month, year, taxi_type, '
                         'interval_start, interval_end) '
                         'values (?,?,?,?,?,?)', transition_graph_row)

            cursor = conn.execute('SELECT id from transition_graphs WHERE name=\'{}\''.format(graph_name))
            transition_graph_id = next(cursor)[0]

            conn.commit()

        # list of (transition_graph_id, source_node_id, dest_node_id, weight)
        l_edges = []

        for source_index in range(quantizer.num_nodes()):
            for dest_index in range(quantizer.num_nodes()):
                weight = trip_graph[source_index, dest_index]
                src_node_id = quantizer.nodes[source_index].db_id
                dest_node_id = quantizer.nodes[dest_index].db_id
                if weight > 0:
                    l_edges.append((transition_graph_id, src_node_id, dest_node_id, weight))

        with sqlite3.connect(graph_db) as conn:
            conn.execute('PRAGMA foreign_keys = ON')
            conn.executemany('INSERT INTO transition_edges (transition_graph_id, '
                         'source_node_id, dest_node_id, weight)'
                         'values (?,?,?,?)', l_edges)

            conn.commit()