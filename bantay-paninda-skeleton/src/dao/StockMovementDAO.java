package dao;

import db.DBConnection;
import model.StockMovement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * StockMovementDAO.java
 * --------------------
 * This class reads the "stock_movements" table.
 * The table is a history log, so this DAO only needs READ methods.
 *
 * The rows are written by ProductDAO (and later by SaleDAO) inside
 * their transactions, so the history always matches the products table.
 */
public class StockMovementDAO {

    /**
     * Returns ALL stock movements, newest first.
     * "newest first" means the biggest movement_date at the top, and
     * if two rows have the same date we use the biggest movement_id.
     */
    public List<StockMovement> getAll() {
        List<StockMovement> movements = new ArrayList<>();

        String sql = "SELECT movement_id, product_id, product_name, user_id, "
                   + "change_qty, reason, movement_date "
                   + "FROM stock_movements "
                   + "ORDER BY movement_date DESC, movement_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                movements.add(readMovementRow(rs));
            }

        } catch (SQLException e) {
            System.out.println("StockMovementDAO.getAll error: " + e.getMessage());
        }
        return movements;
    }

    /**
     * Returns the movements of ONE product, found by its name.
     * Example: getByProduct("Test Item") returns only Test Item rows.
     * Newest first, same as getAll().
     */
    public List<StockMovement> getByProduct(String name) {
        List<StockMovement> movements = new ArrayList<>();

        String sql = "SELECT movement_id, product_id, product_name, user_id, "
                   + "change_qty, reason, movement_date "
                   + "FROM stock_movements "
                   + "WHERE product_name = ? "
                   + "ORDER BY movement_date DESC, movement_id DESC";

        // If the caller passes null we compare against an empty string
        // instead of crashing.
        String productName = (name == null) ? "" : name.trim();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, productName);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    movements.add(readMovementRow(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("StockMovementDAO.getByProduct error: " + e.getMessage());
        }
        return movements;
    }

    /**
     * Helper: builds one StockMovement object from the current ResultSet row.
     * product_id and user_id can be NULL in the table, so they are read with
     * getObject and then checked before converting to Integer.
     */
    private StockMovement readMovementRow(ResultSet rs) throws SQLException {
        StockMovement m = new StockMovement();

        m.setMovementId(rs.getInt("movement_id"));
        m.setProductName(rs.getString("product_name"));
        m.setChangeQty(rs.getInt("change_qty"));
        m.setReason(rs.getString("reason"));

        // ---- nullable product_id ----
        Integer productId = null;
        Object rawProduct = rs.getObject("product_id");
        if (rawProduct != null) {
            productId = rs.getInt("product_id");
        }
        m.setProductId(productId);

        // ---- nullable user_id ----
        Integer userId = null;
        Object rawUser = rs.getObject("user_id");
        if (rawUser != null) {
            userId = rs.getInt("user_id");
        }
        m.setUserId(userId);

        // ---- DATETIME -> LocalDateTime ----
        Timestamp stamp = rs.getTimestamp("movement_date");
        if (stamp != null) {
            LocalDateTime dateTime = stamp.toLocalDateTime();
            m.setMovementDate(dateTime);
        }

        return m;
    }
}
