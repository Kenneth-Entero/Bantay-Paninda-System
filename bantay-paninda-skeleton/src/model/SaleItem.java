package model;

/**
 * SaleItem.java
 * ------------
 * This class represents ONE row of the "sale_items" table.
 * One SaleItem = one line of a receipt, for example:
 *     2 x Sardines @ 28.50 = 57.00
 *
 * product_name and unit_price are COPIED into this row when the sale
 * is saved. That is why an old receipt still shows the right name and
 * price even after the product is renamed, repriced or deleted.
 *
 * Table columns  ->  Java fields
 * sale_item_id        saleItemId
 * sale_id             saleId
 * product_id          productId   (can be null - product may be deleted)
 * product_name        productName
 * quantity            quantity
 * unit_price          unitPrice
 * subtotal            subtotal
 */
public class SaleItem {

    // ---------- FIELDS ----------
    private int saleItemId;             // sale_item_id : 0 means "not saved yet"
    private int saleId;                 // sale_id : which receipt this line belongs to
    private Integer productId;          // product_id : Integer so it can be null
    private String productName;         // product_name (copied from products)
    private int quantity;               // quantity : how many pieces
    private double unitPrice;           // unit_price (copied from products.price)
    private double subtotal;            // subtotal = quantity * unit_price

    // ---------- CONSTRUCTOR 1: no arguments ----------
    // Used by the DAO before it fills the object from a database row.
    public SaleItem() {
    }

    // ---------- CONSTRUCTOR 2: for a NEW sale line ----------
    // When the cashier adds an item to the cart we only know WHICH
    // product and HOW MANY. The name and the price are filled in by
    // SaleDAO.recordSale() while it locks the product row, because only
    // then do we know the real current price.
    public SaleItem(int productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    // ---------- CONSTRUCTOR 3: full constructor ----------
    public SaleItem(int saleItemId, int saleId, Integer productId, String productName,
                    int quantity, double unitPrice, double subtotal) {
        this.saleItemId = saleItemId;
        this.saleId = saleId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
    }

    // ---------- GETTERS and SETTERS ----------

    public int getSaleItemId() {
        return saleItemId;
    }

    public void setSaleItemId(int saleItemId) {
        this.saleItemId = saleItemId;
    }

    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    // ---------- HELPER METHOD ----------
    // Recalculates subtotal from quantity and unit_price.
    // SaleDAO calls this after it copies the price from the product,
    // so we never trust a subtotal that was computed somewhere else.
    public void computeSubtotal() {
        this.subtotal = roundMoney(quantity * unitPrice);
    }

    // ---------- toString() ----------
    @Override
    public String toString() {
        return productName + " x" + quantity + " @ " + unitPrice
             + " = " + subtotal;
    }

    // ---------- SMALL HELPER ----------
    // Money is kept in a double, but a receipt must not show
    // 57.000000000000004, so we round to 2 decimal places.
    private static double roundMoney(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
