package dao;

import db.DBConnection;
import model.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * CategoryDAO.java
 * ----------------
 * This class does all the database work for the "categories" table.
 * DAO means "Data Access Object" - it is the only class that talks
 * to the categories table with SQL.
 *
 * Rules followed here:
 * - Only PreparedStatement (never build SQL by joining text).
 * - Every connection comes from DBConnection.getConnection().
 * - try-with-resources closes everything automatically.
 * - When something goes wrong, it prints the error and returns an
 *   empty list (or null) instead of crashing, so the GUI stays safe.
 */
public class CategoryDAO {

    /**
     * Returns ALL categories, sorted by category_name (A to Z).
     * The result is an empty list (not null) when the table is empty
     * or when something goes wrong, so the GUI can loop over it safely
     * (for example, to fill the category dropdown in Add New Product).
     */
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT category_id, category_name "
                   + "FROM categories ORDER BY category_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Category c = new Category();
                c.setCategoryId(rs.getInt("category_id"));
                c.setCategoryName(rs.getString("category_name"));
                categories.add(c);
            }

        } catch (SQLException e) {
            System.out.println("getAllCategories error: " + e.getMessage());
        }
        return categories;
    }

    /**
     * Returns the category with the given id.
     * Returns null when the id does not exist, or when the database fails.
     */
    public Category getCategoryById(int id) {
        String sql = "SELECT category_id, category_name "
                   + "FROM categories WHERE category_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Category c = new Category();
                    c.setCategoryId(rs.getInt("category_id"));
                    c.setCategoryName(rs.getString("category_name"));
                    return c;
                }
            }

        } catch (SQLException e) {
            System.out.println("getCategoryById error: " + e.getMessage());
        }
        return null;
    }
}
