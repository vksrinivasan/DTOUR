from scipy import spatial
import numpy as np
import sqlite3
import collections

class Quantizer():
    def __init__(self, connector):
        self.__load_nodes(connector)
        self.__build_tree()


    def __load_nodes(self, connector):
        with connector.open() as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT id, name, latitude, longitude from node ORDER by id ASC")
            self.nodes = [Node(*row, index=i) for i, row in enumerate(cursor.fetchall())]
            cursor.close()

            conn.commit()


    def __build_tree(self):
        coords = np.array([(node.lat, node.long) for node in self.nodes])
        # self.coord_map = collections.defaultdict(list)
        # for i, coord in enumerate(coords):
        #     self.coord_map[coord[0], coord[1]] = self.nodes[i]

        self._kdtree = spatial.KDTree(coords)
        pass


    def query(self, lat, long, k=1):
        result = self.query((lat, long), k)
        return result[0]


    def query(self, points, k=1):
        """
        Returns the nearest node
        :param points: tuple(lat,long) or list of tuples
        :returns tuples of (Node, dist) in same dimensionality as points was specified
        """
        dists, indexes = self._kdtree.query(points, k=k)
        return self.__to_nodes(indexes, dists)


    def __to_nodes(self, indexes, dists):
        try:
            return [self.__to_nodes(index, dist) for index, dist in zip(indexes, dists)]
        except (TypeError, ValueError): # not iterable
            return self.nodes[indexes], dists


    def num_nodes(self):
        return len(self.nodes)


class Node():
    def __init__(self, db_id, name, lat, long, index):
        '''

        :param db_id: database id
        :param name:
        :param lat:
        :param long:
        :param index: used to number nodes from 0 to nodes.length-1
        '''
        self.db_id = db_id
        self.name = name
        self.lat = np.float(lat)
        self.long = np.float(long)
        self.index = index