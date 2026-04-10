package com.mertncu.clubmembershipmanagement.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton database connection manager using SQLite.
 * SQLite is chosen for a desktop student project because it creates a local file,
 * avoiding complex server installations for each group member.
 */
public class DatabaseConnection {
    private static final String URL = "jdbc:sqlite:club_management.db";
    private static Connection connection = null;

    private DatabaseConnection() {
        // Private constructor for Singleton
    }

    public static Connection getConnection() {
        if (connection == null) {
            try {
                // Ensure correct driver is loaded
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(URL);
                enableForeignKeys(connection);
                System.out.println("Database connection established.");
                
                // You can call your schema initialization here if needed
                
            } catch (SQLException | ClassNotFoundException e) {
                System.err.println("Database Connection Failed: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return connection;
    }

    private static void enableForeignKeys(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.err.println("Failed to close connection: " + e.getMessage());
            }
        }
    }
}
