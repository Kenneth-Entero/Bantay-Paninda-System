package model;

/**
 * Product.java
 * ------------
 * This class represents ONE row of the "products" table in the database.
 * It only HOLDS data - it does not talk to the database at all.
 * The ProductDAO class is the one that saves/loads this object.
 *
 * Table columns  ->  Java fields
 * product_id          productId
 * product_name        name
 * category_id         categoryId   (can be null)
 * price               price
 * stock               stock
 * low_stock_threshold lowStockThreshold
 */
public class Product {

    // ---------- FIELDS (one for each column) ----------
    private int productId;              // product_id : 0 means "not saved yet"
    private String name;                // product_name
    private Integer categoryId;         // category_id : Integer (not int) so it can be null
    private double price;               // price
    private int stock;                  // stock
    private int lowStockThreshold;      // low_stock_threshold : default is 5

    // ---------- CONSTRUCTOR 1: no arguments ----------
    // Used by the DAO when it builds an empty object before filling it.
    public Product() {
        this.lowStockThreshold = 5;     // same default as the database
    }

    // ---------- CONSTRUCTOR 2: for adding a NEW product ----------
    // When we add a product, we do not have an id yet (the database gives it).
    // The threshold is set to the default 5 here.
    public Product(String name, double price, int stock) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.lowStockThreshold = 5;     // default value
    }

    // ---------- CONSTRUCTOR 3: full constructor ----------
    // Used by the DAO when reading a complete row from the database.
    public Product(int productId, String name, Integer categoryId,
                   double price, int stock, int lowStockThreshold) {
        this.productId = productId;
        this.name = name;
        this.categoryId = categoryId;
        this.price = price;
        this.stock = stock;
        this.lowStockThreshold = lowStockThreshold;
    }

    // ---------- GETTERS and SETTERS ----------

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(int lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    // ---------- HELPER METHOD ----------
    // Returns true if the stock is already at or below the limit,
    // so the screen can warn the owner to restock.
    public boolean isLowStock() {
        return stock <= lowStockThreshold;
    }

    // ---------- toString() ----------
    // Simple text so we can print a product while testing.
    @Override
    public String toString() {
        return name + " - P" + price + " (stock: " + stock + ")";
    }
}
