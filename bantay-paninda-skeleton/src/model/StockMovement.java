package model;

import java.time.LocalDateTime;

/**
 * StockMovement.java
 * -----------------
 * This class represents ONE row of the "stock_movements" table.
 * It is a history log: every time the stock of a product changes,
 * one row is added here so the owner can see what happened.
 *
 * change_qty is positive when stock was ADDED (restock, new product)
 * and negative when stock was REMOVED (sale, deleted product).
 *
 * Table columns  ->  Java fields
 * movement_id         movementId
 * product_id          productId    (can be null - product may be deleted)
 * product_name        productName  (copied name, so history stays readable)
 * user_id             userId       (can be null - Tindahan mode has no login)
 * change_qty          changeQty
 * reason              reason       (NEW_PRODUCT, SALE, RESTOCK, REMOVED)
 * movement_date       movementDate
 */
public class StockMovement {

    // ---------- FIELDS (one for each column) ----------
    private int movementId;                 // movement_id
    private Integer productId;              // product_id : Integer so it can be null
    private String productName;             // product_name
    private Integer userId;                 // user_id : Integer so it can be null
    private int changeQty;                  // change_qty : + added, - removed
    private String reason;                  // reason : NEW_PRODUCT / SALE / RESTOCK / REMOVED
    private LocalDateTime movementDate;     // movement_date

    // ---------- CONSTRUCTOR 1: no arguments ----------
    // Used by the DAO before it fills the object from a database row.
    public StockMovement() {
    }

    // ---------- CONSTRUCTOR 2: full constructor ----------
    public StockMovement(int movementId, Integer productId, String productName,
                         Integer userId, int changeQty, String reason,
                         LocalDateTime movementDate) {
        this.movementId = movementId;
        this.productId = productId;
        this.productName = productName;
        this.userId = userId;
        this.changeQty = changeQty;
        this.reason = reason;
        this.movementDate = movementDate;
    }

    // ---------- GETTERS and SETTERS ----------

    public int getMovementId() {
        return movementId;
    }

    public void setMovementId(int movementId) {
        this.movementId = movementId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public int getChangeQty() {
        return changeQty;
    }

    public void setChangeQty(int changeQty) {
        this.changeQty = changeQty;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getMovementDate() {
        return movementDate;
    }

    public void setMovementDate(LocalDateTime movementDate) {
        this.movementDate = movementDate;
    }

    // ---------- HELPER METHOD ----------
    // Returns true if this movement ADDED stock (for example RESTOCK).
    public boolean isStockAdded() {
        return changeQty > 0;
    }

    // ---------- toString() ----------
    // Simple text so we can print a movement while testing.
    @Override
    public String toString() {
        return movementDate + " | " + productName + " | " + reason
             + " | qty: " + changeQty;
    }
}
