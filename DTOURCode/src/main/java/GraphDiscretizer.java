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


    public List<Node> queryKDTree(double latitude, double longitude, int numNeighbors) {
        List<Node> ret;
        try {
            ret = kdTree.nearest(new double[]{latitude, longitude}, numNeighbors);
        } catch (KeySizeException e) {
            throw new RuntimeException(e);
        }
        return ret;
    }


    public List<Node> pathToNodes(List<double[]> latLngs) {
        double currLat = latLngs.get(0)[0];
        double currLong = latLngs.get(0)[1];
        Node currNode = queryKDTree(currLat, currLong, 1).get(0);
        List<Node> nodesPath = new ArrayList<>();
        nodesPath.add(currNode);

        for (int i = 1; i < latLngs.size(); i++) {
            double[] nextLatLng = latLngs.get(i);
            double nextLat = nextLatLng[0];
            double nextLong = nextLatLng[1];

            while (true) {
                Map<Integer, Edge> destMap = adjEdgeMap.get(currNode.id);
                if (destMap == null || destMap.isEmpty()) {
                    throw new RuntimeException(String.format("[%d, %d] is out of bounds"));
                }

                Node nextNode = null;
                double minDist = Math.pow(nextLat - currNode.latitude, 2) + Math.pow(nextLong - currNode.longitude, 2);
                for (Edge edge : destMap.values()) {
                    Node destNode = idToNodeMap.get(edge.destNodeId);
                    double dist = Math.pow(nextLat - destNode.latitude, 2) + Math.pow(nextLong - destNode.longitude, 2);
                    if (dist < minDist) {
                        minDist = dist;
                        nextNode = destNode;
                    }
                }

                if (nextNode == null) {
                    break; //No adjacent node is closer to next point in LatLng path than current node
                } else {
                    currNode = nextNode;
                    nodesPath.add(currNode);
                }
            }
        }
        return nodesPath;
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

        List<double[]> path = Arrays.asList(
                new double[]{40.75856, -73.98501},
                new double[]{40.75538, -73.97744},
                new double[]{40.754720000000006, -73.97591000000001},
                new double[]{40.754650000000005, -73.97568000000001},
                new double[]{40.75526, -73.97524},
                new double[]{40.75652, -73.97435},
                new double[]{40.757760000000005, -73.97342},
                new double[]{40.7584, -73.97298},
                new double[]{40.759, -73.97251},
                new double[]{40.760270000000006, -73.97160000000001},
                new double[]{40.760760000000005, -73.97276000000001},
                new double[]{40.76099000000001, -73.97327}
        );
        List<Node> nodesPath = discretizer.pathToNodes(path);
        for (Node node : nodesPath) {
            System.out.println(node.name);
        }
        int i = 0;
    }
}
