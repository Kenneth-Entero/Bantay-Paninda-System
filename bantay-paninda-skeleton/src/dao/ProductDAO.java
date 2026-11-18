package dao;

import db.DBConnection;
import model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * ProductDAO.java
 * --------------
 * This class does all the database work for the "products" table.
 * DAO means "Data Access Object" - it is the only class that talks
 * to the products table with SQL.
 *
 * Rules followed here:
 * - Only PreparedStatement (no string concatenation of user input).
 * - Every connection comes from DBConnection.getConnection().
 * - try-with-resources closes the Statement/ResultSet automatically.
 * - Methods that change two tables use ONE transaction.
 */
public class ProductDAO {

    // ==========================================================
    // PART 1 : READ METHODS
    // ==========================================================

    /**
     * Returns ALL products, sorted by product_name (A to Z).
     * The result is an empty list (not null) when the table is empty,
     * so the GUI can loop over it safely.
     */
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT product_id, product_name, category_id, price, stock, low_stock_threshold "
                   + "FROM products ORDER BY product_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                products.add(readProductRow(rs));
            }

        } catch (SQLException e) {
            System.out.println("getAllProducts error: " + e.getMessage());
        }
        return products;
    }

    /**
     * Searches products whose name contains the keyword.
     * Example: keyword "sar" finds "Sardines".
     * The % signs are put in the SQL string, but the keyword itself is
     * still passed safely with setString (no SQL injection).
     */
    public List<Product> searchProducts(String keyword) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT product_id, product_name, category_id, price, stock, low_stock_threshold "
                   + "FROM products WHERE product_name LIKE ? ORDER BY product_name";

        // If the caller passes null, we search for an empty keyword instead.
        String searchText = (keyword == null) ? "" : keyword.trim();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + searchText + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(readProductRow(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("searchProducts error: " + e.getMessage());
        }
        return products;
    }

    /**
     * Returns how many products are in the table.
     * Returns 0 when something goes wrong.
     */
    public int countProducts() {
        String sql = "SELECT COUNT(*) FROM products";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.out.println("countProducts error: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Helper: builds one Product object from the current row of the ResultSet.
     * category_id can be NULL in the database, so we read it with getObject
     * and then ask wasNull() to know if it was NULL.
     */
    private Product readProductRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("product_id"));
        p.setName(rs.getString("product_name"));

        // ---- the nullable column ----
        Integer categoryId = null;
        Object rawCategory = rs.getObject("category_id");
        if (rawCategory != null) {
            categoryId = rs.getInt("category_id");
        }
        p.setCategoryId(categoryId);

        p.setPrice(rs.getDouble("price"));
        p.setStock(rs.getInt("stock"));
        p.setLowStockThreshold(rs.getInt("low_stock_threshold"));
        return p;
    }

    // ==========================================================
    // PART 2 : WRITE METHODS
    // Every method here takes an Integer userId.
    // It is null when we are in Tindahan mode (no login),
    // and it holds the real user id in Mall mode.
    // ==========================================================

    /**
     * Adds a new product AND writes a NEW_PRODUCT stock movement.
     * Both inserts happen in ONE transaction: either both are saved
     * or none of them are saved.
     *
     * Returns true  = product was saved.
     * Returns false = bad input, or the name already exists.
     */
    public boolean addProduct(Product p, Integer userId) {
        // ---------- STEP 1: validate the input ----------
        if (p == null || p.getName() == null || p.getName().trim().isEmpty()) {
            return false;                       // name is required
        }
        if (p.getPrice() < 0 || p.getStock() < 0) {
            return false;                       // price and stock cannot be negative
        }

        String insertProductSql =
                "INSERT INTO products (product_name, category_id, price, stock, low_stock_threshold) "
              + "VALUES (?, ?, ?, ?, ?)";

        String cleanName = p.getName().trim();

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);          // START the transaction

            try {
                // ---------- STEP 2: insert the product row ----------
                int newProductId;
                try (PreparedStatement ps = conn.prepareStatement(
                        insertProductSql, Statement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1, cleanName);
                    setNullableInt(ps, 2, p.getCategoryId());   // category may be null
                    ps.setDouble(3, p.getPrice());
                    ps.setInt(4, p.getStock());
                    ps.setInt(5, p.getLowStockThreshold());

                    if (ps.executeUpdate() == 0) {
                        conn.rollback();
                        return false;
                    }

                    // read the id that MySQL generated for us
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            conn.rollback();
                            return false;
                        }
                        newProductId = keys.getInt(1);
                    }
                }

                // ---------- STEP 3: insert the movement row ----------
                // change_qty cannot be 0 (the table has a CHECK rule),
                // so we skip the movement when the starting stock is 0.
                if (p.getStock() > 0) {
                    insertMovement(conn, newProductId, cleanName, userId,
                                   p.getStock(), "NEW_PRODUCT");
                }

                // ---------- STEP 4: save both changes ----------
                conn.commit();
                p.setProductId(newProductId);   // give the object its new id
                return true;

            } catch (SQLIntegrityConstraintViolationException duplicate) {
                // The product_name column is UNIQUE, so a repeated name
                // arrives here. It is NOT an error for the user: return false.
                conn.rollback();
                return false;

            } catch (SQLException e) {
                conn.rollback();                // undo everything on any other error
                System.out.println("addProduct error: " + e.getMessage());
                return false;

            } finally {
                conn.setAutoCommit(true);       // restore the connection state
            }

        } catch (SQLException e) {
            System.out.println("addProduct connection error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Adds qty pieces to an existing product AND writes a RESTOCK movement.
     * qty must be greater than 0.
     */
    public boolean restock(int productId, int qty, Integer userId) {
        if (qty <= 0) {
            return false;                       // nothing to add
        }

        String selectSql = "SELECT product_name FROM products WHERE product_id = ?";
        String updateSql = "UPDATE products SET stock = stock + ? WHERE product_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);          // START the transaction

            try {
                // ---------- STEP 1: find the product and copy its name ----------
                String productName;
                try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                    ps.setInt(1, productId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return false;       // product does not exist
                        }
                        productName = rs.getString("product_name");
                    }
                }

                // ---------- STEP 2: increase the stock ----------
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setInt(1, qty);
                    ps.setInt(2, productId);
                    if (ps.executeUpdate() == 0) {
                        conn.rollback();
                        return false;
                    }
                }

                // ---------- STEP 3: write the RESTOCK movement (+qty) ----------
                insertMovement(conn, productId, productName, userId, qty, "RESTOCK");

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                System.out.println("restock error: " + e.getMessage());
                return false;

            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.out.println("restock connection error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes a product AND writes a REMOVED movement with the old stock.
     * The movement is written FIRST, while the product still exists,
     * because stock_movements.product_id points to the products table.
     */
    public boolean deleteProduct(int productId, Integer userId) {
        String selectSql = "SELECT product_name, stock FROM products WHERE product_id = ?";
        String deleteSql = "DELETE FROM products WHERE product_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);          // START the transaction

            try {
                // ---------- STEP 1: remember the name and stock ----------
                String productName;
                int oldStock;
                try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                    ps.setInt(1, productId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return false;       // product does not exist
                        }
                        productName = rs.getString("product_name");
                        oldStock = rs.getInt("stock");
                    }
                }

                // ---------- STEP 2: write the REMOVED movement (-stock) ----------
                // Skipped when stock is 0 because change_qty cannot be 0.
                if (oldStock > 0) {
                    insertMovement(conn, productId, productName, userId,
                                   -oldStock, "REMOVED");
                }

                // ---------- STEP 3: delete the product row ----------
                try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                    ps.setInt(1, productId);
                    if (ps.executeUpdate() == 0) {
                        conn.rollback();
                        return false;
                    }
                }

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                System.out.println("deleteProduct error: " + e.getMessage());
                return false;

            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.out.println("deleteProduct connection error: " + e.getMessage());
            return false;
        }
    }

    // ==========================================================
    // SMALL HELPERS used by the write methods
    // ==========================================================

    /**
     * Inserts one row into stock_movements using the SAME connection
     * and the SAME transaction as the caller.
     * product_name is copied so the history stays readable even after
     * the product is deleted.
     */
    private void insertMovement(Connection conn, int productId, String productName,
                                Integer userId, int changeQty, String reason)
            throws SQLException {

        String sql = "INSERT INTO stock_movements "
                   + "(product_id, product_name, user_id, change_qty, reason) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setString(2, productName);
            setNullableInt(ps, 3, userId);      // null in Tindahan mode
            ps.setInt(4, changeQty);
            ps.setString(5, reason);
            ps.executeUpdate();
        }
    }

    /**
     * Helper for nullable INT columns.
     * setInt() cannot store null, so we use setNull with Types.INTEGER
     * when the value is null.
     */
    private void setNullableInt(PreparedStatement ps, int index, Integer value)
            throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }
}
