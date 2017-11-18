# See the database schema in DTOUR/graph/README.txt for exact schema details.

# This file should connect to the mysql database and aggregate transition_groups.
# It should take in as command line arguments '<new_transition_group_name> <transition_group1> <transition_group2> ...'
# where transition_group1, transition_group2, ... are the (name or id) of existing transition_groups.

# To aggregate transition groups:
# 1. Create the new transition group
# 2. Take the union of the transition_periods that reference transition_group1, transition_group2, ... and make copies
#    of them that reference new transition group instead.
# 3. Take the intersection of transition_graphs (meaning same start and end interval) that correspond to transition_group1, transition_group2, ...
#    Create copies of them that reference the new transition_group.
# 4. For each of the new transition_graphs above,, add the corresponding transition_edges from the corresponding graphs in transition_group1, transition_group2, ...
#    Have these new transition_edges reference the new transition graph.

import math
import os
import re
import sys

import MySQLdb
import numpy as np

from mysql_util import Connector

if __name__ == '__main__':
    connector = Connector('../credentials.txt')

    transition_group_ids = range(1, 31)

    INTERVAL_LENGTH = 60
    intervals = []
    for interval_start in range(0, 24*INTERVAL_LENGTH, INTERVAL_LENGTH):
        intervals.append((interval_start, interval_start + INTERVAL_LENGTH))

    DECAY_FUNCTION = lambda x: 0.95**x

    new_transition_group_name = 'yellow_tripdata_aggregated'

    with connector.open() as conn:
        cursor = conn.cursor()
        cursor.execute('SELECT id FROM transition_group WHERE NAME = %s', (new_transition_group_name,))
        new_group_id = cursor.fetchone()
        if new_group_id is None:
            cursor.execute('INSERT INTO transition_group (name) VALUES (%s)',
                           (new_transition_group_name,))
            new_group_id = cursor.lastrowid
            cursor.execute('SELECT month, year, SUM(weight) as weight_sum, taxi_type from transition_period ' +
                            'WHERE transition_group_id in (' +
                            ', '.join(str(id) for id in transition_group_ids) + ') ' +
                            'GROUP BY month, year, taxi_type ORDER by year DESC, month DESC')
            results = cursor.fetchall()
            new_periods = []
            for i, result in enumerate(results):
                new_periods.append((new_group_id, result[0], result[1], DECAY_FUNCTION(i)*result[2], result[3]))
            cursor.executemany('INSERT into transition_period (transition_group_id, month, year, weight, taxi_type) ' +
                           'VALUES (%s, %s, %s, %s, %s)', new_periods)
        conn.commit()

    for interval_start, interval_end in intervals:
        with connector.open() as conn:
            try:
                cursor = conn.cursor()
                cursor.execute('SELECT id FROM transition_graph where transition_group_id in (' +
                               ', '.join(str(id) for id in transition_group_ids) + ')' +
                               'AND interval_start=%s AND interval_end=%s ' +
                               'ORDER by transition_group_id DESC',
                               (interval_start, interval_end))
                transition_graph_ids = cursor.fetchall()
                transition_graph_ids = [t[0] for t in transition_graph_ids]

                cursor.execute('INSERT INTO transition_graph (transition_group_id, interval_start, interval_end) ' +
                               'VALUES (%s, %s, %s)', (new_group_id, interval_start, interval_end))
                new_graph_id = cursor.lastrowid

                src_dest_weight ={}
                for i, graph_id in enumerate(transition_group_ids):
                    cursor.execute('SELECT src_node_id, dest_node_id, weight from transition_edge ' +
                                   'WHERE transition_graph_id=%s', (graph_id,))
                    decay_factor= DECAY_FUNCTION(i)
                    edges = cursor.fetchall()
                    for edge in edges:
                        src_dest_weight.setdefault(edge[0], {})
                        dest_weight = src_dest_weight[edge[0]]
                        dest_weight.setdefault(edge[1], 0)
                        dest_weight[edge[1]] += decay_factor*edge[2]

                new_edges = []
                for src, dest_weight in src_dest_weight.iteritems():
                    for dest, weight in dest_weight.iteritems():
                        new_edges.append((new_graph_id, src, dest, weight))

                SLICE_SIZE = 100000
                for part in range(0, len(new_edges), SLICE_SIZE):
                    cursor.executemany('INSERT INTO transition_edge (transition_graph_id, src_node_id, dest_node_id, weight) ' +
                                   'VALUES (%s, %s, %s, %s)', new_edges[part:part+SLICE_SIZE])

                conn.commit()
            except MySQLdb.Error as e:
                conn.rollback()
                import traceback
                sys.stderr.write('Could not write for interval between {} and {}:\n{}\n'
                                 .format(interval_start, interval_end, traceback.format_exc()))
        pass