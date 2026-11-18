package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection - the one place that knows how to connect to MySQL (Laragon).
 * Every DAO gets its connection from here.
 */
public class DBConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/bantay_paninda_db";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Laragon uses a blank password by default

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
