package com.demoproject.db;

import com.demoproject.config.ConfigReader;
import com.demoproject.exceptions.FrameworkException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * JDBC helper with one lazily opened connection per thread. All SQL runs through
 * {@link PreparedStatement} with bound parameters and the configured query timeout;
 * statements and result sets are closed with try-with-resources.
 */
public final class DbUtils {

    private static final Logger LOG = Logger.getLogger(DbUtils.class.getName());
    private static final ThreadLocal<Connection> CONNECTION = new ThreadLocal<>();
    private static final Pattern SAFE_IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]{0,63}");

    private DbUtils() {
    }

    /**
     * @param sql    SELECT statement with {@code ?} placeholders
     * @param params values bound in order
     * @return rows as ordered column-label/value maps
     */
    public static List<Map<String, Object>> query(String sql, Object... params) {
        try (PreparedStatement statement = prepare(sql, params);
             ResultSet resultSet = statement.executeQuery()) {
            ResultSetMetaData meta = resultSet.getMetaData();
            List<Map<String, Object>> rows = new ArrayList<>();
            while (resultSet.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    row.put(meta.getColumnLabel(i), resultSet.getObject(i));
                }
                rows.add(row);
            }
            LOG.info(() -> "Query returned " + rows.size() + " row(s): " + sql);
            return rows;
        } catch (SQLException e) {
            throw new FrameworkException("Query failed [" + sql + "]: " + e.getMessage(), e);
        }
    }

    /**
     * @param sql    INSERT/UPDATE/DELETE/DDL statement with {@code ?} placeholders
     * @param params values bound in order
     * @return affected row count
     */
    public static int update(String sql, Object... params) {
        try (PreparedStatement statement = prepare(sql, params)) {
            int count = statement.executeUpdate();
            LOG.info(() -> "Statement affected " + count + " row(s): " + sql);
            return count;
        } catch (SQLException e) {
            throw new FrameworkException("Statement failed [" + sql + "]: " + e.getMessage(), e);
        }
    }

    /**
     * Table and column names cannot be bound as JDBC parameters. This validates a name taken from
     * test data against a strict pattern and back-quotes it, so it is safe to place in SQL.
     *
     * @param name table or column name
     * @return the back-quoted identifier
     */
    public static String identifier(String name) {
        if (name == null || !SAFE_IDENTIFIER.matcher(name).matches()) {
            throw new FrameworkException("Unsafe SQL identifier '" + name + "'. Use letters, digits and '_' only.");
        }
        return "`" + name + "`";
    }

    /**
     * Closes and unbinds the current thread's connection (no-op if none was opened).
     */
    public static void closeConnection() {
        Connection connection = CONNECTION.get();
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException e) {
            LOG.warning(() -> "Error closing DB connection: " + e.getMessage());
        } finally {
            CONNECTION.remove();
        }
    }

    private static PreparedStatement prepare(String sql, Object... params) throws SQLException {
        PreparedStatement statement = connection().prepareStatement(sql);
        try {
            statement.setQueryTimeout(ConfigReader.getInstance().getDbTimeoutSeconds());
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            return statement;
        } catch (SQLException e) {
            statement.close();
            throw e;
        }
    }

    private static Connection connection() throws SQLException {
        Connection connection = CONNECTION.get();
        if (connection != null && !connection.isClosed()) {
            return connection;
        }
        ConfigReader config = ConfigReader.getInstance();
        int timeoutMs = config.getDbTimeoutSeconds() * 1000;
        Properties props = new Properties();
        props.setProperty("user", config.getDbUsername());
        props.setProperty("password", config.getDbPassword());
        props.setProperty("connectTimeout", String.valueOf(timeoutMs));
        props.setProperty("socketTimeout", String.valueOf(timeoutMs));
        try {
            connection = java.sql.DriverManager.getConnection(config.getDbUrl(), props);
        } catch (SQLException e) {
            throw new SQLException("Unable to connect to " + config.getDbUrl() + " as '" + config.getDbUsername()
                    + "': " + e.getMessage(), e.getSQLState(), e);
        }
        CONNECTION.set(connection);
        LOG.info(() -> "Opened DB connection to " + config.getDbUrl());
        return connection;
    }
}
