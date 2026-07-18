package com.minimarket.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseConnection {

    private static DatabaseConnection instance;
    private static final Object LOCK = new Object();
    private final Object connectionLock = new Object();
    private Connection rawConnection;
    private Connection proxyConnection;
    private Properties properties = new Properties();

    private DatabaseConnection() {
        loadProperties();

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

    private File getPropertiesFile() {
        String userHome = System.getProperty("user.home");
        File configDir = new File(userHome, ".minimarket");
        return new File(configDir, "database.properties");
    }

    private void loadProperties() {
        properties = new Properties();
        boolean loaded = false;
        
        // 1. Try to load from user home folder
        File userHomeFile = getPropertiesFile();
        if (userHomeFile.exists()) {
            try (InputStream input = new FileInputStream(userHomeFile)) {
                properties.load(input);
                System.out.println("Loaded database configuration from user home: " + userHomeFile.getAbsolutePath());
                Class.forName(properties.getProperty("db.driver"));
                loaded = true;
            } catch (Exception ex) {
                System.err.println("Failed to load user home properties, falling back: " + ex.getMessage());
            }
        }
        
        // 2. Try to load from external file in working directory
        if (!loaded) {
            File externalFile = new File("database.properties");
            if (externalFile.exists()) {
                try (InputStream input = new FileInputStream(externalFile)) {
                    properties.load(input);
                    System.out.println("Loaded database configuration from external file: " + externalFile.getAbsolutePath());
                    Class.forName(properties.getProperty("db.driver"));
                    loaded = true;
                } catch (Exception ex) {
                    System.err.println("Failed to load external properties, falling back to classpath: " + ex.getMessage());
                }
            }
        }
        
        // 3. Fall back to classpath resource
        if (!loaded) {
            try (InputStream input = getClass().getClassLoader().getResourceAsStream("database.properties")) {
                if (input != null) {
                    properties.load(input);
                    System.out.println("Loaded database configuration from classpath resource.");
                    Class.forName(properties.getProperty("db.driver"));
                } else {
                    // Default fallback
                    properties.setProperty("db.driver", "com.mysql.cj.jdbc.Driver");
                    properties.setProperty("db.url", "jdbc:mysql://localhost:3306/minimarket_yuly?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
                    properties.setProperty("db.username", "root");
                    properties.setProperty("db.password", "1234");
                }
            } catch (Exception ex) {
                System.err.println("Error loading properties: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        sanitizeUrl();
    }

    private void sanitizeUrl() {
        String url = properties.getProperty("db.url");
        if (url != null && url.contains("createDatabaseIfNotExist=")) {
            url = url.replaceAll("([&?])createDatabaseIfNotExist=[a-zA-Z0-9]+", "");
            if (url.endsWith("?") || url.endsWith("&")) {
                url = url.substring(0, url.length() - 1);
            }
            properties.setProperty("db.url", url);
            System.out.println("Sanitized database URL (removed createDatabaseIfNotExist): " + url);
        }
    }

    public void setConnectionSettings(String host, String port, String database, String username, String password) throws SQLException {
        synchronized (connectionLock) {
            String baseUrl = "jdbc:mysql://" + host + ":" + port + "/?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
            
            // 1. Test connection to the MySQL server and create the database if it doesn't exist
            try (Connection testConn = DriverManager.getConnection(baseUrl, username, password);
                 Statement stmt = testConn.createStatement()) {
                stmt.execute("CREATE DATABASE IF NOT EXISTS " + database);
            }
            
            // 2. Save the URL (WITHOUT createDatabaseIfNotExist) to properties
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
            properties.setProperty("db.url", url);
            properties.setProperty("db.username", username);
            properties.setProperty("db.password", password);
            properties.setProperty("db.driver", "com.mysql.cj.jdbc.Driver");
            
            // Reset active connection so it reconnects with new credentials
            if (rawConnection != null) {
                try {
                    rawConnection.close();
                } catch (SQLException e) {
                    // Ignore
                }
                rawConnection = null;
            }
            
            // Write to user home folder database.properties
            File externalFile = getPropertiesFile();
            File parent = externalFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            try (OutputStream output = new FileOutputStream(externalFile)) {
                properties.store(output, "External Database Configuration");
                System.out.println("Saved database configuration to: " + externalFile.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Error saving database.properties: " + e.getMessage());
            }
        }
    }

    public void saveProperty(String key, String value) {
        synchronized (connectionLock) {
            properties.setProperty(key, value);
            File externalFile = getPropertiesFile();
            File parent = externalFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            try (OutputStream output = new FileOutputStream(externalFile)) {
                properties.store(output, "Updated Property: " + key);
                System.out.println("Saved property " + key + "=" + value + " to: " + externalFile.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Error saving database properties: " + e.getMessage());
            }
        }
    }

    public static DatabaseConnection getInstance() {
        synchronized (LOCK) {
            if (instance == null) {
                instance = new DatabaseConnection();
            }
            return instance;
        }
    }

    // Returns a proxy connection that is completely immune to database firewall timeouts and drops
    public Connection getConnection() throws SQLException {
        return proxyConnection;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    // Checks the raw connection health and reconnects automatically if dropped
    private Connection getActiveRawConnection() throws SQLException {
        synchronized (connectionLock) {
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

    public boolean isConfigured() {
        return getPropertiesFile().exists() || new File("database.properties").exists();
    }

    public boolean checkHealth() {
        synchronized (connectionLock) {
            try {
                Connection conn = getActiveRawConnection();
                return conn != null && !conn.isClosed() && conn.isValid(1);
            } catch (SQLException e) {
                return false;
            }
        }
    }

    @Override
    protected final void finalize() {
        // Prevent finalizer attacks (SpotBugs CT_CONSTRUCTOR_THROW)
    }
}
