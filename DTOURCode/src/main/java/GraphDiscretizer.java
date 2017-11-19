//import weka.core.*;

import de.biomedical_imaging.edu.wlu.cs.levy.CG.KDTree;
import de.biomedical_imaging.edu.wlu.cs.levy.CG.KeyDuplicateException;
import de.biomedical_imaging.edu.wlu.cs.levy.CG.KeySizeException;

import java.sql.*;
import java.util.*;

public class GraphDiscretizer {
    private Map<Integer, Node> idToNodeMap;
    private Map<Integer, Map<Integer, Edge>> adjEdgeMap;
    private KDTree<Node> kdTree;
//    private ArrayList<Attribute> attributes;

    public GraphDiscretizer() {
        initData();
        initKDTree();
    }

    private void initData() {
        Connection conn = null;
        try {
            conn = getConnection();
            this.idToNodeMap = getIdToLatLngMap(conn);
            this.adjEdgeMap = getAdjEdgeMap(conn);
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private void initKDTree() {
        kdTree = new KDTree<>(2);

        for (Node node : idToNodeMap.values()) {
            try {
                kdTree.insert(new double[]{node.latitude, node.longitude}, node);
            } catch (KeySizeException|KeyDuplicateException e) {}
        }
    }


    private List<Node> queryKDTree(double latitude, double longitude, int numNeighbors) {
        List<Node> ret;
        try {
            ret = kdTree.nearest(new double[]{latitude, longitude}, numNeighbors);
        } catch (KeySizeException e) {
            throw new RuntimeException(e);
        }
        return ret;
    }


    private Map<Integer, Node> getIdToLatLngMap(Connection connection) throws SQLException {
        Map<Integer, Node> idToLatLngMap = new TreeMap<>();

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT id, name, latitude, longitude from node");

        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String name = resultSet.getString("name");
            double latitude = resultSet.getDouble("latitude");
            double longitude = resultSet.getDouble("longitude");

            idToLatLngMap.put(id, new Node(id, name, latitude, longitude));
        }
        return idToLatLngMap;
    }

    private Map<Integer, Map<Integer, Edge>> getAdjEdgeMap(Connection connection) throws SQLException {
        Map<Integer, Map<Integer, Edge>> adjEdgeMap = new TreeMap<>();

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT id, src_node_id, dest_node_id from adj_edge");

        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            int srcNodeId = resultSet.getInt("src_node_id");
            int destNodeId = resultSet.getInt("dest_node_id");

            Map<Integer, Edge> destEdgeMap = adjEdgeMap.computeIfAbsent(srcNodeId, k -> new HashMap<Integer, Edge>());
            destEdgeMap.put(destNodeId, new Edge(id, srcNodeId, destNodeId));
        };
        return adjEdgeMap;
    }

    private Connection getConnection() throws SQLException {
        ConnectionParams connectionParams = ConnectionParams.loadConnectionParams();
//        Connection conn = connectionDataSource.getConnection();
        String connString = String.format("jdbc:mysql://%s/%s?user=%s&password=%s",
                connectionParams.host, connectionParams.db, connectionParams.user, connectionParams.password);
        Connection conn = DriverManager.getConnection(connString);
        return conn;
    }

    class Node {
        public final int id;
        public final String name;
        public final double latitude;
        public final double longitude;

        public Node(int id, String name, double latitude, double longitude) {
            this.id = id;
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    class Edge {
        public final int id;
        public final int srcNodeId;
        public final int destNodeId;

        public Edge(int id, int srcNodeId, int destNodeId) {
            this.id = id;
            this.srcNodeId = srcNodeId;
            this.destNodeId = destNodeId;
        }
    }

    public static void main(String[] args) {
        GraphDiscretizer discretizer = new GraphDiscretizer();
        List<Node> neighbors = discretizer.queryKDTree(40.760971, -73.973285, 2);
        int i = 0;
    }
}
