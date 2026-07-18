package com.minimarket.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    public static void initializeDatabase(Connection conn) throws SQLException {
        if (!tablesExist(conn)) {
            logger.info("Database tables not found. Initializing schema...");
            initializeSchema(conn);
        } else {
            logger.info("Database tables already exist. Skipping schema initialization.");
        }
    }

    public static boolean tablesExist(Connection conn) throws SQLException {
        if (conn == null || conn.isClosed() || !conn.isValid(2)) {
            throw new SQLException("Database connection is closed or invalid.");
        }
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1 FROM usuario LIMIT 1")) {
            return true;
        } catch (SQLException e) {
            String sqlState = e.getSQLState();
            // If it is a connection/authentication error, throw it.
            if (sqlState != null && (sqlState.startsWith("08") || sqlState.startsWith("28") || e.getErrorCode() == 1045)) {
                throw e;
            }
            return false;
        }
    }

    public static boolean isDatabaseEmpty(Connection conn) throws SQLException {
        if (conn == null || conn.isClosed() || !conn.isValid(2)) {
            throw new SQLException("Database connection is closed or invalid.");
        }
        String sql = "SELECT COUNT(*) FROM usuario";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        }
        return true;
    }

    private static void initializeSchema(Connection conn) throws SQLException {
        try (InputStream is = DatabaseInitializer.class.getResourceAsStream("/db_schema.sql")) {
            if (is == null) {
                throw new SQLException("db_schema.sql resource not found in classpath!");
            }
            
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.startsWith("--") || trimmed.startsWith("//") || trimmed.isEmpty()) {
                        continue;
                    }
                    sb.append(line).append("\n");
                }
            } catch (IOException e) {
                throw new SQLException("Error reading db_schema.sql", e);
            }

            String[] statements = sb.toString().split(";");
            try (Statement stmt = conn.createStatement()) {
                for (String sql : statements) {
                    String trimmedSql = sql.trim();
                    if (!trimmedSql.isEmpty()) {
                        stmt.execute(trimmedSql);
                    }
                }
                logger.info("Database schema initialized successfully.");
            }
        } catch (IOException e) {
            throw new SQLException("Error closing schema stream", e);
        }
    }
}
