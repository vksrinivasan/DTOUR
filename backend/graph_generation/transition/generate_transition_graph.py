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

# Outline
# main function: reads csv
# load Quantizer
# For each line, take the origin/destination and quantize them using Quantizer that uses k quantization bins
# Add that to kxk matrix
# Once we're done calculate transition matrix and write to output.json


NUM_NODES = 265


def generate_trip_graph(interval_start, interval_end, data_path):
    trip_graph = np.zeros((NUM_NODES, NUM_NODES), dtype=np.int)

    with open(data_path) as f:
        rows = (row for row in csv.reader(f) if len(row) > 0)
        header = rows.next()
        pu_id_index = header.index('PULocationID')
        do_id_index = header.index('DOLocationID')
        pu_time_index = header.index('tpep_pickup_datetime')

        for row in rows:
            pu_date = dateutil.parser.parse(row[pu_time_index])
            if pu_date.time() >= interval_start and pu_date.time() < interval_end:
                pu_id = int(row[pu_id_index])
                do_id = int(row[do_id_index])
                trip_graph[pu_id-1, do_id-1] += 1

    return trip_graph


if __name__ == '__main__':
    hour1, minute1 = sys.argv[1].split(':')
    interval_start = dt.time(hour=int(hour1), minute=int(minute1))

    hour2, minute2 = sys.argv[2].split(':')
    if int(hour2) > 23:
        interval_end = dt.time(hour=23, minute=59, second=59)
    else:
        interval_end = dt.time(hour=int(hour2), minute=int(minute2))

    data_dir = sys.argv[3]
    data_paths = [os.path.join(data_dir, data_file) for data_file in os.listdir(data_dir)]
    data_paths = [data_path for data_path in data_paths if os.path.isfile(data_path)]

    output_dir = sys.argv[4]

    for data_path in data_paths:
        trip_graph = generate_trip_graph(interval_start, interval_end, data_path)
        outbound_degrees = trip_graph.sum(axis = 1)
        transition_probabilities = trip_graph.astype(np.float) / outbound_degrees[:, np.newaxis]
        transition_probabilities = np.nan_to_num(transition_probabilities) # Replace nans with 0's

        data_file = os.path.split(data_path)[1]
        data_file_output_dir = os.path.join(output_dir, os.path.splitext(data_file)[0])
        try:
            os.mkdir(data_file_output_dir)
        except OSError as e:
            if e.errno != errno.EEXIST:
                raise e

        output_path = os.path.join(data_file_output_dir, '{:02d}{:02d}-{:02d}{:02d}.json'
                                   .format(int(hour1), int(minute1), int(hour2), int(minute2)))
        with open(output_path, 'w+') as f:
            results = {}
            results['transition_graph'] = transition_probabilities.tolist()
            results['num_pickups'] = outbound_degrees.tolist()
            results['data_files'] = [os.path.split(data_path)[1] for data_path in data_paths]
            results['interval'] = [interval_start.isoformat(), interval_end.isoformat()]
            results['num_zones'] = NUM_NODES

            json.dump(results, f, sort_keys=True)