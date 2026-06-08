package com.minimarket.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection rawConnection;
    private Connection proxyConnection;
    private Properties properties = new Properties();

    private DatabaseConnection() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("database.properties")) {
            if (input == null) {
                throw new RuntimeException("Sorry, unable to find database.properties");
            }

            properties.load(input);

            Class.forName(properties.getProperty("db.driver"));
        } catch (IOException ex) {
            System.err.println("Error loading properties file: " + ex.getMessage());
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            System.err.println("MySQL Driver not found: " + ex.getMessage());
            ex.printStackTrace();
        }

        // Initialize the Dynamic Proxy Connection to intercept SQL calls and auto-reconnect if dropped by cloud server
        this.proxyConnection = (Connection) java.lang.reflect.Proxy.newProxyInstance(
            Connection.class.getClassLoader(),
            new Class<?>[] { Connection.class },
            new java.lang.reflect.InvocationHandler() {
                @Override
                public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) throws Throwable {
                    Connection activeConn = getActiveRawConnection();
                    try {
                        return method.invoke(activeConn, args);
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        throw e.getCause(); // Propagate original SQL exception
                    }
                }
            }
        );
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    // Returns a proxy connection that is completely immune to database firewall timeouts and drops
    public Connection getConnection() throws SQLException {
        return proxyConnection;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    // Checks the raw connection health and reconnects automatically if dropped
    private synchronized Connection getActiveRawConnection() throws SQLException {
        if (rawConnection == null || rawConnection.isClosed() || !rawConnection.isValid(2)) {
            if (rawConnection != null) {
                try {
                    rawConnection.close();
                } catch (SQLException e) {
                    // Silently ignore
                }
            }
            String url = properties.getProperty("db.url");
            String user = properties.getProperty("db.username");
            String password = properties.getProperty("db.password");
            rawConnection = DriverManager.getConnection(url, user, password);
        }
        return rawConnection;
    }
}
