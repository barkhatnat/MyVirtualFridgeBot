package ru.barkhatnat.db;

import java.sql.*;

public class DatabaseConnectionManager {
    static String connectionUrl = "jdbc:h2:mem:/test";
    static String user = "sa";
    static String password = "";
    public static Connection connection;

    private DatabaseConnectionManager() {
    }

    public static void openConnection() throws SQLException {
        connection = DriverManager.getConnection(connectionUrl, user, password);
    }

    public static void closeConnection() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    public static ResultSet executeQuery(String query) throws SQLException {
        Statement statement = connection.createStatement();
        return statement.executeQuery(query);
    }

    public static int executeUpdate(String query) throws SQLException {
        Statement statement = connection.createStatement();
        return statement.executeUpdate(query);
    }
}
