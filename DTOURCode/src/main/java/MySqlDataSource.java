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
    private static final String TRANSITION_GROUP_NAME = "yellow_tripdata_2014-01";
    private static final int GRAPH_INTERVAL = 60; //interval between graphs

    private ConnectionParams connectionParams;
    private TreeMap<Integer, Integer> intervalToIdMap;
    private PoolingDataSource connectionDataSource;

    @Override
    public void initSource() {
        connectionParams = loadConnectionParams();

        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(String.format("jdbc:mysql://%s/%s",
                connectionParams.host, connectionParams.db), connectionParams.user, connectionParams.password);
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
        int transitionGraphId = intervalToIdMap.floorKey(interval);
        Map<Integer, EdgeData> edgeDataMap = new TreeMap<>();
        int weightSum = 0;

        try {
            connection = getConnection();
//            connection = DriverManager.getConnection(
//                    String.format("jdbc:mysql://%s/%s?user=%s&password=%s",
//                            connectionParams.host, connectionParams.db, connectionParams.user, connectionParams.password));
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT source, dest, weight from transition_edge " +
                    String.format("where transition_graph_id=%s AND source=%s", transitionGraphId, source));
            while (resultSet.next()) {
                int dest = resultSet.getInt("dest");
                int weight = resultSet.getInt("weight");
                weightSum += weight;
                EdgeData edgeData = new EdgeData(source, dest, weight);
                edgeDataMap.put(dest, edgeData);
            }

            resultSet = statement.executeQuery("SELECT source, dest from adj_edge " +
                    String.format("where source=%s", source));
            while (resultSet.next()) {
                int dest = resultSet.getInt("dest");
                int weight = resultSet.getInt("weight");
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
    public void closeSource() {

    }

    private ConnectionParams loadConnectionParams() {
        InputStream is = MySqlDataSource.class.getResourceAsStream(CREDENTIALS_FILE);
        byte[] buffer;
        ConnectionParams connectionParams;
        try {
            buffer = new byte[is.available()];
            is.read(buffer);
            connectionParams = new ConnectionParams(buffer);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Could not load %s", CREDENTIALS_FILE), e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return connectionParams;
    }

    private TreeMap<Integer, Integer> getIntervalToIdMap() {
        TreeMap<Integer, Integer> intervalToIdMap = new TreeMap<>();
        Connection connection = null;

        try {
            connection = getConnection();
//            connection = DriverManager.getConnection(
//                    String.format("jdbc:mysql://%s/%s?user=%s&password=%s",
//                            connectionParams.host, connectionParams.db, connectionParams.user, connectionParams.password));
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT id from transition_group " +
                    String.format("where name=\'%s\'", TRANSITION_GROUP_NAME));
            resultSet.next();
            int transitionGroupId = resultSet.getInt("id");

            for (int intervalStart = 0; intervalStart + GRAPH_INTERVAL < 24 *60; intervalStart += GRAPH_INTERVAL) {
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
        return conn;
    }

    private class ConnectionParams {
        private final String host;
        private final String db;
        private final String user;
        private final String password;

        private ConnectionParams(byte[] buffer) {
            String temp = new String(buffer);
            String[] lines = temp.split("\n");
            host = lines[0];
            db = lines[1];
            user = lines[2];
            password = lines[3];
        }
    }


    public static void main(String[] args) {
        MySqlDataSource dataSource = new MySqlDataSource();
        dataSource.initSource();
    }
}
