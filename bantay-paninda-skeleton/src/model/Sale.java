package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Sale.java
 * --------
 * This class represents ONE row of the "sales" table.
 * One Sale = one receipt = one time the cashier finished a transaction.
 *
 * The items that were bought are kept in a list inside this object,
 * so a receipt can be shown without asking the database again.
 *
 * Table columns  ->  Java fields
 * sale_id             saleId
 * sale_date           saleDate
 * total_amount        totalAmount
 */
public class Sale {

    // ---------- FIELDS ----------
    private int saleId;                     // sale_id : 0 means "not saved yet"
    private LocalDateTime saleDate;         // sale_date
    private double totalAmount;             // total_amount

    // The lines of this receipt. It starts empty (never null) so the
    // GUI can always loop over it safely.
    private List<SaleItem> items = new ArrayList<>();

    // ---------- CONSTRUCTOR 1: no arguments ----------
    public Sale() {
    }

    // ---------- CONSTRUCTOR 2: used by the DAO when reading a row ----------
    public Sale(int saleId, LocalDateTime saleDate, double totalAmount) {
        this.saleId = saleId;
        this.saleDate = saleDate;
        this.totalAmount = totalAmount;
    }

    // ---------- GETTERS and SETTERS ----------

    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    public LocalDateTime getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDateTime saleDate) {
        this.saleDate = saleDate;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<SaleItem> getItems() {
        return items;
    }

    public void setItems(List<SaleItem> items) {
        // An empty list is used instead of null so nobody gets a
        // NullPointerException when looping over the items.
        this.items = (items == null) ? new ArrayList<>() : items;
    }

    /** Adds one line to this receipt. */
    public void addItem(SaleItem item) {
        if (item != null) {
            items.add(item);
        }
    }

    /** Returns how many pieces were sold in total. */
    public int getTotalQuantity() {
        int total = 0;
        for (SaleItem item : items) {
            total = total + item.getQuantity();
        }
        return total;
    }

    // ---------- toString() ----------
    @Override
    public String toString() {
        return "Sale #" + saleId + " | " + saleDate + " | total: P" + totalAmount;
    }
}
