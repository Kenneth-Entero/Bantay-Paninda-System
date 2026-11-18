package dao;

import db.DBConnection;
import model.Product;
import model.Sale;
import model.SaleItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * SaleDAO.java
 * -----------
 * This class does the database work for a sale ("Deduct Stock").
 * One sale touches FOUR tables, so everything is done in ONE
 * transaction: if anything fails, nothing is saved.
 *
 *   1. products       - stock goes down
 *   2. sales          - one new receipt row
 *   3. sale_items     - one row per item of that receipt
 *   4. stock_movements- one SALE row per item (negative quantity)
 *
 * Rules followed here:
 * - Only PreparedStatement.
 * - Every connection comes from DBConnection.getConnection().
 * - try-with-resources closes every Statement/ResultSet.
 * - The product row is locked with SELECT ... FOR UPDATE so two
 *   cashiers cannot sell the last piece at the same time.
 */
public class SaleDAO {

    // ==========================================================
    // PART 1 : RECORDING A SALE
    // ==========================================================

    /**
     * Saves one sale and deducts the stock of every item.
     *
     * The items that come in only need a productId and a quantity.
     * The real product_name and unit_price are read from the products
     * table (while the row is locked) and copied into the item, so the
     * receipt always shows the real name and the real price.
     *
     * @param items  the lines of the cart
     * @param userId the cashier id, or null in Tindahan mode
     * @return true when the whole sale was saved.
     *         false when the input is bad, when a product does not
     *         exist, or when the stock is not enough. In that case
     *         NOTHING was changed, because the transaction is rolled back.
     */
    public boolean recordSale(List<SaleItem> items, Integer userId) {
        // ---------- STEP 1: check the cart before touching the database ----------
        if (!isCartValid(items)) {
            return false;
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);          // START the transaction

            try {
                // ---------- STEP 2: check and deduct every item ----------
                // If one item fails, we roll back the WHOLE sale, so the
                // earlier items get their stock back.
                double total = 0.0;
                for (SaleItem item : items) {
                    Product product = lockProduct(conn, item.getProductId());
                    if (product == null) {
                        conn.rollback();        // product does not exist anymore
                        return false;
                    }
                    if (item.getQuantity() > product.getStock()) {
                        conn.rollback();        // not enough stock: whole sale refused
                        return false;
                    }

                    // Copy the real name and price from the database.
                    item.setProductName(product.getName());
                    item.setUnitPrice(product.getPrice());
                    item.computeSubtotal();
                    total = total + item.getSubtotal();

                    // Reduce the stock. The column cannot go below 0
                    // because we already checked the quantity above.
                    if (!takeStock(conn, item.getProductId(), item.getQuantity())) {
                        conn.rollback();
                        return false;
                    }
                }
                total = roundMoney(total);

                // ---------- STEP 3: insert the receipt row ----------
                int saleId = insertSale(conn, total);
                if (saleId <= 0) {
                    conn.rollback();
                    return false;
                }

                // ---------- STEP 4: insert the items + the history rows ----------
                for (SaleItem item : items) {
                    item.setSaleId(saleId);
                    insertSaleItem(conn, item);
                    insertSaleMovement(conn, item, userId);
                }

                // ---------- STEP 5: save everything ----------
                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();                // undo the whole sale
                System.out.println("recordSale error: " + e.getMessage());
                return false;

            } finally {
                conn.setAutoCommit(true);       // restore the connection state
            }

        } catch (SQLException e) {
            System.out.println("recordSale connection error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Returns true when every line of the cart has a product id and a
     * quantity greater than 0, and the cart is not empty.
     */
    private boolean isCartValid(List<SaleItem> items) {
        if (items == null || items.isEmpty()) {
            return false;
        }
        for (SaleItem item : items) {
            if (item == null || item.getProductId() == null) {
                return false;
            }
            if (item.getQuantity() <= 0) {
                return false;               // cannot sell 0 or a negative number
            }
        }
        return true;
    }

    // ==========================================================
    // PART 2 : SMALL HELPERS used inside the transaction
    // Each one receives the SAME "conn", so it stays inside the
    // same transaction. They all throw SQLException upward, and
    // recordSale() catches it and rolls back.
    // ==========================================================

    /**
     * Reads one product AND locks its row until the transaction ends.
     *
     * The "FOR UPDATE" is the important part. While we hold the lock,
     * another cashier cannot read-then-change the same row, so two
     * people cannot sell the last Sardines at the same time.
     *
     * Returns null when the product id does not exist.
     */
    private Product lockProduct(Connection conn, int productId) throws SQLException {
        String sql = "SELECT product_id, product_name, price, stock "
                   + "FROM products WHERE product_id = ? FOR UPDATE";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;            // no such product
                }

                Product p = new Product();
                p.setProductId(rs.getInt("product_id"));
                p.setName(rs.getString("product_name"));
                p.setPrice(rs.getDouble("price"));
                p.setStock(rs.getInt("stock"));
                return p;
            }
        }
    }

    /**
     * Subtracts qty from products.stock of one product.
     * Returns false when no row was changed (product gone).
     */
    private boolean takeStock(Connection conn, int productId, int qty) throws SQLException {
        String sql = "UPDATE products SET stock = stock - ? WHERE product_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, qty);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Inserts the one row of this receipt into "sales".
     * Returns the new sale_id (or 0 when it failed).
     */
    private int insertSale(Connection conn, double totalAmount) throws SQLException {
        String sql = "INSERT INTO sales (total_amount) VALUES (?)";

        try (PreparedStatement ps = conn.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setDouble(1, totalAmount);
            if (ps.executeUpdate() == 0) {
                return 0;
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    return 0;
                }
                return keys.getInt(1);
            }
        }
    }

    /**
     * Inserts one line of the receipt into "sale_items".
     * product_name, unit_price and subtotal are already filled in
     * by recordSale(), so the old receipt keeps the old price.
     */
    private void insertSaleItem(Connection conn, SaleItem item) throws SQLException {
        String sql = "INSERT INTO sale_items "
                   + "(sale_id, product_id, product_name, quantity, unit_price, subtotal) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, item.getSaleId());
            setNullableInt(ps, 2, item.getProductId());
            ps.setString(3, item.getProductName());
            ps.setInt(4, item.getQuantity());
            ps.setDouble(5, item.getUnitPrice());
            ps.setDouble(6, item.getSubtotal());
            ps.executeUpdate();
        }
    }

    /**
     * Writes the SALE row in "stock_movements" for one item.
     * change_qty is NEGATIVE because stock went down.
     * product_name is copied so the history stays readable.
     */
    private void insertSaleMovement(Connection conn, SaleItem item,
                                    Integer userId) throws SQLException {
        String sql = "INSERT INTO stock_movements "
                   + "(product_id, product_name, user_id, change_qty, reason) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setNullableInt(ps, 1, item.getProductId());
            ps.setString(2, item.getProductName());
            setNullableInt(ps, 3, userId);      // null in Tindahan mode
            ps.setInt(4, -item.getQuantity());  // negative = removed by a sale
            ps.setString(5, "SALE");
            ps.executeUpdate();
        }
    }

    /**
     * Helper for nullable INT columns (product_id, user_id, category_id).
     * setInt() cannot store null, so setNull is used instead.
     */
    private void setNullableInt(PreparedStatement ps, int index, Integer value)
            throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }

    /**
     * Rounds money to 2 decimal places, so a total of
     * 57.000000000000004 becomes 57.00.
     */
    private double roundMoney(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    // ==========================================================
    // PART 3 : READ METHODS (for the Sales History screen)
    // ==========================================================

    /**
     * Returns ALL sales, newest first.
     * The list is empty (not null) when there are no sales yet.
     * The items of each sale are NOT loaded here - use
     * getItemsBySale() to see the receipt of one sale.
     */
    public List<Sale> getAllSales() {
        List<Sale> sales = new ArrayList<>();

        String sql = "SELECT sale_id, sale_date, total_amount "
                   + "FROM sales ORDER BY sale_date DESC, sale_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int saleId = rs.getInt("sale_id");
                double total = rs.getDouble("total_amount");

                // DATETIME -> LocalDateTime
                LocalDateTime date = null;
                Timestamp stamp = rs.getTimestamp("sale_date");
                if (stamp != null) {
                    date = stamp.toLocalDateTime();
                }

                sales.add(new Sale(saleId, date, total));
            }

        } catch (SQLException e) {
            System.out.println("getAllSales error: " + e.getMessage());
        }
        return sales;
    }

    /**
     * Returns the items of ONE receipt, in the order they were saved.
     * This is what the history screen shows when the owner clicks a sale.
     */
    public List<SaleItem> getItemsBySale(int saleId) {
        List<SaleItem> items = new ArrayList<>();

        String sql = "SELECT sale_item_id, sale_id, product_id, product_name, "
                   + "quantity, unit_price, subtotal "
                   + "FROM sale_items WHERE sale_id = ? ORDER BY sale_item_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, saleId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SaleItem item = new SaleItem();
                    item.setSaleItemId(rs.getInt("sale_item_id"));
                    item.setSaleId(rs.getInt("sale_id"));

                    // ---- nullable product_id (the product may be deleted) ----
                    Integer productId = null;
                    Object rawProduct = rs.getObject("product_id");
                    if (rawProduct != null) {
                        productId = rs.getInt("product_id");
                    }
                    item.setProductId(productId);

                    item.setProductName(rs.getString("product_name"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getDouble("unit_price"));
                    item.setSubtotal(rs.getDouble("subtotal"));
                    items.add(item);
                }
            }

        } catch (SQLException e) {
            System.out.println("getItemsBySale error: " + e.getMessage());
        }
        return items;
    }

    /**
     * Returns how many receipts are in the sales table.
     * The test uses this to check that one sale row was added.
     */
    public int countSales() {
        String sql = "SELECT COUNT(*) FROM sales";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.out.println("countSales error: " + e.getMessage());
        }
        return 0;
    }
}
