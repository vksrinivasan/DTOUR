import os
import json
import sqlite3
import sys

dirname, _ = os.path.split(os.path.abspath(__file__))
NODE_FILE_PATH = os.path.join(dirname, 'gps_dict.json')
ADJ_FILE_PATH = os.path.join(dirname, 'Manhattan_adj_graph.json')
DB_PATH = os.path.join(dirname, '../../../graph/graph_database.db')

if __name__ == '__main__':
    if len(sys.argv) > 1:
        DB_PATH = sys.argv[1]

    with open(NODE_FILE_PATH) as f:
        d_nodes = json.load(f)
    with open(ADJ_FILE_PATH) as f:
        d_adj = json.load(f)

    # tuples of (name, lat, long)
    node_rows = []
    for name, lat_long in d_nodes.items():
        try:
            node_rows.append((name, float(lat_long[0]), float(lat_long[1])))
        except ValueError:
            pass

    # e.g. 1st St & Avenue D -> 5
    node_name_to_id = {}

    with sqlite3.connect(DB_PATH) as conn:
        conn.execute('PRAGMA foreign_keys = ON')
        conn.executemany('INSERT INTO nodes (name, latitude, longitude) values (?,?,?)', node_rows)

        cursor = conn.execute("SELECT id, name from nodes")
        for row in cursor:
            node_name_to_id[row[1]] = row[0]

        conn.commit()

    # tuples of (source_node_id, dest_node_id)
    adj_rows = []
    for source, l_dest in d_adj.items():
        if source in node_name_to_id:
            source_id = node_name_to_id[source]

            for dest in l_dest:
                if dest in node_name_to_id:
                    dest_id = node_name_to_id[dest]
                    adj_rows.append((source_id, dest_id))


    with sqlite3.connect(DB_PATH) as conn:
        conn.execute('PRAGMA foreign_keys = ON')
        conn.executemany('INSERT INTO adj_edges (source_node_id, dest_node_id) values (?,?)', adj_rows)

        conn.commit()

