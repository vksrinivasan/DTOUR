import java.io.IOException;
import java.io.InputStream;

public class ConnectionParams {
    private static final String CREDENTIALS_FILE = "credentials.txt";

    public static ConnectionParams loadConnectionParams() {
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

    final String host;
    final String db;
    final String user;
    final String password;

    private ConnectionParams(byte[] buffer) {
        String temp = new String(buffer);
        String[] lines = temp.split("\n");
        host = lines[0].trim();
        db = lines[1].trim();
        user = lines[2].trim();
        password = lines[3].trim();
    }
}
