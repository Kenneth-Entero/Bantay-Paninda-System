package model;

/**
 * Category.java
 * -------------
 * This class represents ONE row of the "categories" table in the database.
 * It only HOLDS data - it does not talk to the database at all.
 * The CategoryDAO class is the one that saves/loads this object.
 *
 * Table columns  ->  Java fields
 * category_id      categoryId
 * category_name    categoryName
 *
 * toString() returns categoryName so a JComboBox shows the name,
 * instead of something like "model.Category@1a2b3c".
 */
public class Category {

    // ---------- FIELDS (one for each column) ----------
    private int categoryId;      // category_id : primary key
    private String categoryName; // category_name

    // ---------- CONSTRUCTOR 1: no arguments ----------
    // Used by the DAO when it builds an empty object before filling it.
    public Category() {
    }

    // ---------- CONSTRUCTOR 2: full constructor ----------
    // Used by the DAO when reading a complete row from the database.
    public Category(int categoryId, String categoryName) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    // ---------- GETTERS and SETTERS ----------

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    // ---------- toString() ----------
    // Returns only the name so a JComboBox (or any list) shows
    // the category name instead of the object's hash code.
    @Override
    public String toString() {
        return categoryName;
    }
}
