package model;

/**
 * User.java
 * --------
 * This class represents ONE row of the "users" table.
 * It only HOLDS data - UserDAO is the one that saves/loads it.
 *
 * The PIN is never kept here. Only the HASH of the PIN is stored in
 * the database, and UserDAO checks it. That is why there is no pin
 * or pinHash field in this class.
 *
 * Table columns  ->  Java fields
 * user_id             userId
 * full_name           fullName
 * username            username
 * role                role   (MANAGER, INVENTORY, CASHIER)
 */
public class User {

    // ---------- FIELDS (one for each column we need) ----------
    private int userId;         // user_id : 0 means "not saved yet"
    private String fullName;    // full_name
    private String username;    // username
    private String role;        // role : MANAGER / INVENTORY / CASHIER

    // ---------- CONSTRUCTOR 1: no arguments ----------
    // Used by the DAO before it fills the object from a database row.
    public User() {
    }

    // ---------- CONSTRUCTOR 2: full constructor ----------
    // Used by the DAO when it reads a complete row.
    public User(int userId, String fullName, String username, String role) {
        this.userId = userId;
        this.fullName = fullName;
        this.username = username;
        this.role = role;
    }

    // ---------- GETTERS and SETTERS ----------

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // ---------- HELPER METHOD ----------
    // Returns true when this account is the manager.
    // The GUI can use this to show or hide the manager buttons.
    public boolean isManager() {
        return "MANAGER".equals(role);
    }

    // ---------- toString() ----------
    // Simple text so we can print a user while testing.
    @Override
    public String toString() {
        return fullName + " (" + username + ") - " + role;
    }
}
