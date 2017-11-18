import json
import os
import sys
from mysql_util import Connector

dirname, _ = os.path.split(os.path.abspath(__file__))
NODE_FILE_PATH = os.path.join(dirname, 'gps_data_complete.json')
ADJ_FILE_PATH = os.path.join(dirname, 'Manhattan_adj_graph_complete.json')
CREDENTIALS_PATH = os.path.join(dirname, '../credentials.txt')

if __name__ == '__main__':
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

    connector = Connector(CREDENTIALS_PATH)
    with connector.open() as conn:
        cursor = conn.cursor()
        cursor.executemany('INSERT INTO node (name, latitude, longitude) values (%s,%s,%s)', node_rows)

        cursor.execute("SELECT id, name from node")
        for row in cursor:
            node_name_to_id[row[1]] = row[0]

        # tuples of (source_node_id, dest_node_id)
        adj_rows = []
        for source, l_dest in d_adj.items():
            if source in node_name_to_id:
                source_id = node_name_to_id[source]

                for dest in l_dest:
                    if dest in node_name_to_id:
                        dest_id = node_name_to_id[dest]
                        adj_rows.append((source_id, dest_id))

        cursor.executemany('INSERT INTO adj_edge (src_node_id, dest_node_id) values (%s,%s)', adj_rows)

        conn.commit()

