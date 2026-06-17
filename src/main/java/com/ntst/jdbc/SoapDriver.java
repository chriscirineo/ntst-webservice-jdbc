package com.ntst.jdbc;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

public class SoapDriver implements java.sql.Driver {

    static {
        try {
            DriverManager.registerDriver(new SoapDriver());
        } catch (SQLException e) {
            throw new RuntimeException("Cannot register SoapDriver", e);
        }
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        if (!acceptsURL(url)) return null;

        try {
            // 1. Temporarily replace the prefix to trick Java into parsing it as a valid URI
            String parsableUri = url.replace("jdbc:soap-ws://", "https://");
            java.net.URI uri = new java.net.URI(parsableUri);

            // 2. Default environment fallback
            String envCode = "BLD";

            // 3. Parse the query string to find the "env" parameter
            String query = uri.getQuery();
            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    String[] pair = param.split("=");
                    // If we find exactly "env=something", extract the value
                    if (pair.length == 2 && pair[0].equalsIgnoreCase("env")) {
                        envCode = pair[1].toUpperCase(); // Force uppercase to match your system (UAT, SBOX, etc.)
                        break;
                    }
                }
            }

            // 4. Reconstruct the actual endpoint URL WITHOUT the query parameters
            // We do not want to send "?env=UAT" to the actual SOAP server in the URL path
            String host = uri.getHost();
            int port = uri.getPort();
            String path = uri.getPath();

            StringBuilder targetEndpoint = new StringBuilder("https://");
            targetEndpoint.append(host);

            // Only append the port if a custom one was explicitly provided (e.g., :8443)
            if (port != -1) {
                targetEndpoint.append(":").append(port);
            }
            if (path != null) {
                targetEndpoint.append(path);
            }

            // 5. Pass the clean target URL and explicitly parsed envCode to the Connection
            return new SoapConnection(targetEndpoint.toString(), info, envCode);

        } catch (Exception e) {
            throw new SQLException("Failed to parse JDBC URL. Format should be: jdbc:soap-ws://host/path?env=UAT", e);
        }
    }

    @Override
    public boolean acceptsURL(String url) {
        return url != null && url.startsWith("jdbc:soap-ws://");
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {
        return new DriverPropertyInfo[0];
    }

    @Override
    public int getMajorVersion() { return 1; }

    @Override
    public int getMinorVersion() { return 0; }

    @Override
    public boolean jdbcCompliant() { return false; }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }
}