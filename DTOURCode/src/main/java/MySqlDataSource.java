import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import org.apache.commons.dbcp2.*;
import org.apache.commons.pool2.impl.GenericObjectPool;

public class MySqlDataSource implements DataSource {
    private static final String CREDENTIALS_FILE = "credentials.txt";
    // Change the following value to get different transition graphs;
    private static final String TRANSITION_GROUP_NAME = "yellow_tripdata_aggregated";
//    private static final String TRANSITION_GROUP_NAME = "yellow_tripdata_2014-01";
    private static final int GRAPH_INTERVAL = 60; //interval between graphs

    private ConnectionParams connectionParams;
    private TreeMap<Integer, Integer> intervalToIdMap;
    private PoolingDataSource connectionDataSource;

    @Override
    public void initSource() {
        connectionParams = ConnectionParams.loadConnectionParams();

        Properties props = new Properties();
        props.setProperty("user", connectionParams.user);
        props.setProperty("password", connectionParams.password);

        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(String.format("jdbc:mysql://%s/%s",
                connectionParams.host, connectionParams.db), props);
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, null);
        GenericObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<>(poolableConnectionFactory);
        poolableConnectionFactory.setPool(connectionPool);
        connectionPool.setMaxTotal(2);
        connectionPool.setMaxWaitMillis(400);

        connectionDataSource = new PoolingDataSource(connectionPool);

        intervalToIdMap = getIntervalToIdMap();
    }

    @Override
    public List<EdgeData> getNeighbors(int source) {
        Connection connection = null;

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/New_York"));
        int interval = now.getHour()*60 + now.getMinute();
        int transitionGraphId = intervalToIdMap.get(intervalToIdMap.floorKey(interval));
        Map<Integer, EdgeData> edgeDataMap = new TreeMap<>();
        int weightSum = 0;

        try {
            connection = getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT src_node_id, dest_node_id, weight from transition_edge " +
                    String.format("where transition_graph_id=%s AND src_node_id=%s", transitionGraphId, source));
            while (resultSet.next()) {
                int dest = resultSet.getInt("dest_node_id");
                double weight = resultSet.getDouble("weight");
                weightSum += weight;
                EdgeData edgeData = new EdgeData(source, dest, weight);
                edgeDataMap.put(dest, edgeData);
            }

            resultSet = statement.executeQuery("SELECT src_node_id, dest_node_id from adj_edge " +
                    String.format("where src_node_id=%s", source));
            while (resultSet.next()) {
                int dest = resultSet.getInt("dest_node_id");
                double weight = 1.0;
                weightSum += weight;

                EdgeData edgeData = edgeDataMap.get(dest);
                if (edgeData == null) {
                    edgeData = new EdgeData(source, dest, weight);
                    edgeDataMap.put(dest, edgeData);
                } else {
                    edgeData.weight += weight;
                }
            }

            for (EdgeData edgeData : edgeDataMap.values()) {
                edgeData.weight /= (double) weightSum;

                edgeData.weight = inputAdjustment.probabilityAdjustment(edgeData.weight);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not connect to the database", e);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>(edgeDataMap.values());
    }

    @Override
    public double[] getGeoList() {
        Connection connection = null;
        int nodeCount = 0;
        double[] geoList;

        try {
            connection = getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT count(*) id  FROM node");
            while (resultSet.next()) {
                nodeCount = resultSet.getInt("id");
            }

            geoList = new double[(nodeCount+1)*2];

            resultSet = statement.executeQuery("SELECT id, longitude, latitude from node");
            while (resultSet.next()) {
                int tempID = resultSet.getInt("id");
                double tempLong = resultSet.getDouble("longitude");
                double tempLat = resultSet.getDouble("latitude");
                geoList[2*tempID] = tempLong;
                geoList[2*tempID+1] = tempLat;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Could not connect to the database", e);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return geoList;
    }

    @Override
    public void closeSource() {

    }

    private TreeMap<Integer, Integer> getIntervalToIdMap() {
        TreeMap<Integer, Integer> intervalToIdMap = new TreeMap<>();
        Connection connection = null;

        try {
            connection = getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT id from transition_group " +
                    String.format("where name=\'%s\'", TRANSITION_GROUP_NAME));

            resultSet.next();
            int transitionGroupId = resultSet.getInt("id");

            for (int intervalStart = 0; intervalStart + GRAPH_INTERVAL < 24 * 20; intervalStart += GRAPH_INTERVAL) {
                resultSet = statement.executeQuery("SELECT id from transition_graph " +
                        String.format("where transition_group_id=%s AND interval_start=%d AND interval_end=%d",
                                transitionGroupId, intervalStart, intervalStart + GRAPH_INTERVAL));
                resultSet.next();
                intervalToIdMap.put(intervalStart, resultSet.getInt("id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not connect to the database", e);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return intervalToIdMap;
    }

    private Connection getConnection() throws SQLException {
        Connection conn = connectionDataSource.getConnection();
//        String connString = String.format("jdbc:mysql://%s/%s?user=%s&password=%s",
//                connectionParams.host, connectionParams.db, connectionParams.user, connectionParams.password);
//        Connection conn = DriverManager.getConnection(connString);
        return conn;
    }


    public static void main(String[] args) {
        MySqlDataSource dataSource = new MySqlDataSource();
        dataSource.initSource();
        List<EdgeData> neighbors = dataSource.getNeighbors(2);
        for(EdgeData temp : neighbors) {
            System.out.println(temp.dest);
        }
        int i = 0;
    }
}
