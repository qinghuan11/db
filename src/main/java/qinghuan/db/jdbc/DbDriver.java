package qinghuan.db.jdbc;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

public class DbDriver implements Driver {
    static {
        try {
            DriverManager.registerDriver(new DbDriver());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to register DbDriver", e);
        }
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        if (!acceptsURL(url)) {
            return null;
        }
        String[] parts = url.split(":");
        String host = parts[2];
        int port = Integer.parseInt(parts[3]);
        return new DbConnection(host, port);
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return url.startsWith("jdbc:db:");
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return new DriverPropertyInfo[0];
    }

    @Override
    public int getMajorVersion() {
        return 1;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public boolean jdbcCompliant() {
        return false;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }
}
