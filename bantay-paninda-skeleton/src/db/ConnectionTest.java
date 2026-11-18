package db;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * ConnectionTest - run this file to check that Java can reach the database.
 * Before running: start MySQL in Laragon and import database/bantay_paninda_db.sql.
 */
public class ConnectionTest {

    public static void main(String[] args) {
        try (Connection connection = DBConnection.getConnection()) {
            System.out.println("Connected to the database!");
            System.out.println("Database: " + connection.getCatalog());
        } catch (SQLException e) {
            System.err.println("Could not connect: " + e.getMessage());
        }
    }
}
