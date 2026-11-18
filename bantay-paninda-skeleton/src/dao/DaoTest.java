package dao;

import model.Category;
import model.Product;
import model.SaleItem;
import model.StockMovement;
import model.User;
import util.Permissions;
import util.Session;

import java.util.ArrayList;
import java.util.List;

/**
 * DaoTest.java
 * -----------
 * A simple console test for ProductDAO and StockMovementDAO.
 * Run it with the "main" method - it uses the real database
 * (bantay_paninda_db), so Laragon / MySQL must be running first.
 *
 * Every step prints PASS or FAIL so we can see right away
 * which part of the DAO is broken.
 */
public class DaoTest {

    // The product name used by the write steps.
    private static final String TEST_NAME = "Test Item";

    // Counters for the final summary.
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        ProductDAO productDAO = new ProductDAO();
        StockMovementDAO movementDAO = new StockMovementDAO();

        System.out.println("=== BANTAY PANINDA - DAO TEST ===");

        // If an older run stopped in the middle, remove the leftover row
        // so this test can be run again and again.
        removeLeftoverTestItem(productDAO);

        // ---- STEP 1: read all products ----
        step1GetAll(productDAO);

        // ---- STEP 2: search products ----
        step2Search(productDAO);

        // ---- STEP 3: add a product (and try a duplicate) ----
        int testId = step3Add(productDAO);

        // ---- STEP 4: restock it by 20 ----
        step4Restock(productDAO, testId);

        // ---- STEP 5: delete it ----
        step5Delete(productDAO, testId);

        // ---- STEP 6: check the history rows ----
        step6Movements(movementDAO);

        // ---- STEP 7: UserDAO.login checks ----
        step7Logins();

        // ---- STEP 8: SaleDAO.recordSale checks ----
        step8RecordSale(productDAO, movementDAO);

        // ---- STEP 9: Permissions checks ----
        step9Permissions();

        // ---- STEP 10: CategoryDAO checks ----
        step10Categories();

        // ---- Put back any stock the test changed (so the test is repeatable) ----
        restoreSardinesStock(productDAO);

        // ---- SUMMARY ----
        System.out.println();
        System.out.println("=== RESULT: " + passed + " passed, " + failed + " failed ===");
    }

    // ==========================================================
    // STEP 1 - getAllProducts() and countProducts()
    // ==========================================================
    private static void step1GetAll(ProductDAO productDAO) {
        System.out.println();
        System.out.println("--- STEP 1: getAllProducts / countProducts ---");

        List<Product> all = productDAO.getAllProducts();
        int count = productDAO.countProducts();

        // Show every product we found.
        for (Product p : all) {
            System.out.println("    " + p.getName()
                    + " | price: " + p.getPrice()
                    + " | stock: " + p.getStock()
                    + " | categoryId: " + p.getCategoryId());
        }

        boolean hasSardines = findByName(all, "Sardines") != null;
        boolean hasNoodles  = findByName(all, "Noodles") != null;
        boolean countOk     = (count == all.size());

        check("getAllProducts returns a list", all != null);
        check("Sardines is in the list", hasSardines);
        check("Noodles is in the list", hasNoodles);
        check("countProducts (" + count + ") matches the list size (" + all.size() + ")", countOk);
    }

    // ==========================================================
    // STEP 2 - searchProducts("sar")
    // ==========================================================
    private static void step2Search(ProductDAO productDAO) {
        System.out.println();
        System.out.println("--- STEP 2: searchProducts(\"sar\") ---");

        List<Product> found = productDAO.searchProducts("sar");

        for (Product p : found) {
            System.out.println("    found: " + p.getName());
        }

        boolean sardinesFound = findByName(found, "Sardines") != null;

        check("searchProducts(\"sar\") is not empty", !found.isEmpty());
        check("searchProducts(\"sar\") contains Sardines", sardinesFound);
    }

    // ==========================================================
    // STEP 3 - addProduct(): new name works, duplicate returns false
    // Returns the new product id for the next steps.
    // ==========================================================
    private static int step3Add(ProductDAO productDAO) {
        System.out.println();
        System.out.println("--- STEP 3: addProduct / duplicate name ---");

        Product newProduct = new Product(TEST_NAME, 10.00, 5);

        // userId is null because we are testing Tindahan mode (no login).
        boolean firstTry = productDAO.addProduct(newProduct, null);
        check("addProduct(\"" + TEST_NAME + "\", 10, 5) returns true", firstTry);

        int newId = newProduct.getProductId();
        check("new product got a generated id (" + newId + ")", newId > 0);

        // The same name again must NOT crash - it must simply return false.
        Product duplicate = new Product(TEST_NAME, 99.00, 1);
        boolean secondTry = productDAO.addProduct(duplicate, null);
        check("addProduct with the SAME name returns false (no crash)", !secondTry);

        // Only one row may exist with that name.
        int matches = 0;
        for (Product p : productDAO.searchProducts(TEST_NAME)) {
            if (p.getName().equalsIgnoreCase(TEST_NAME)) {
                matches++;
            }
        }
        check("only ONE \"" + TEST_NAME + "\" row exists", matches == 1);

        return newId;
    }

    // ==========================================================
    // STEP 4 - restock by 20, stock must become 25
    // ==========================================================
    private static void step4Restock(ProductDAO productDAO, int productId) {
        System.out.println();
        System.out.println("--- STEP 4: restock(" + productId + ", 20) ---");

        boolean ok = productDAO.restock(productId, 20, null);
        check("restock returns true", ok);

        // Read the product again to see the new stock.
        Product after = findById(productDAO, productId);
        if (after == null) {
            check("product still exists after restock", false);
            return;
        }

        System.out.println("    stock now: " + after.getStock() + " (expected 25)");
        check("stock is 5 + 20 = 25", after.getStock() == 25);

        // A restock of 0 or less must be refused.
        check("restock with qty 0 returns false", !productDAO.restock(productId, 0, null));
    }

    // ==========================================================
    // STEP 5 - deleteProduct()
    // ==========================================================
    private static void step5Delete(ProductDAO productDAO, int productId) {
        System.out.println();
        System.out.println("--- STEP 5: deleteProduct(" + productId + ") ---");

        boolean ok = productDAO.deleteProduct(productId, null);
        check("deleteProduct returns true", ok);

        Product gone = findById(productDAO, productId);
        check("\"" + TEST_NAME + "\" is no longer in products", gone == null);
    }

    // ==========================================================
    // STEP 6 - the history must show the three movements
    // ==========================================================
    private static void step6Movements(StockMovementDAO movementDAO) {
        System.out.println();
        System.out.println("--- STEP 6: StockMovementDAO ---");

        List<StockMovement> all = movementDAO.getAll();
        System.out.println("    total movements in the table: " + all.size());

        // Collect only the rows that belong to our test product.
        List<StockMovement> mine = new ArrayList<>();
        for (StockMovement m : all) {
            if (TEST_NAME.equalsIgnoreCase(m.getProductName())) {
                mine.add(m);
            }
        }

        System.out.println("    movements for \"" + TEST_NAME + "\":");
        for (StockMovement m : mine) {
            System.out.println("        " + m.getMovementDate()
                    + " | " + m.getReason()
                    + " | qty: " + m.getChangeQty()
                    + " | productId: " + m.getProductId()
                    + " | userId: " + m.getUserId());
        }

        check("NEW_PRODUCT row exists", hasReason(mine, "NEW_PRODUCT"));
        check("RESTOCK row exists",     hasReason(mine, "RESTOCK"));
        check("REMOVED row exists",     hasReason(mine, "REMOVED"));

        check("NEW_PRODUCT qty is +5",  hasChangeQty(mine, "NEW_PRODUCT", 5));
        check("RESTOCK qty is +20",     hasChangeQty(mine, "RESTOCK", 20));
        check("REMOVED qty is -25",     hasChangeQty(mine, "REMOVED", -25));

        // getByProduct() must find the same rows (history survives deletion).
        List<StockMovement> byName = movementDAO.getByProduct(TEST_NAME);
        check("getByProduct(\"" + TEST_NAME + "\") finds " + mine.size() + " rows",
                byName.size() == mine.size());

        // The product_id became NULL after the delete (ON DELETE SET NULL),
        // but product_name was copied, so the history is still readable.
        boolean namesKept = true;
        for (StockMovement m : byName) {
            if (!TEST_NAME.equals(m.getProductName())) {
                namesKept = false;
            }
        }
        check("product_name is still saved after the product was deleted", namesKept);
    }

    // ==========================================================
    // STEP 7 - UserDAO.login checks
    // ==========================================================
    private static void step7Logins() {
        System.out.println();
        System.out.println("--- STEP 7: UserDAO.login ---");

        UserDAO userDAO = new UserDAO();

        check("manager/1111 returns MANAGER", "MANAGER".equals(roleOf(userDAO, "manager", "1111")));
        check("inventory/2222 returns INVENTORY", "INVENTORY".equals(roleOf(userDAO, "inventory", "2222")));
        check("cashier/3333 returns CASHIER", "CASHIER".equals(roleOf(userDAO, "cashier", "3333")));
        check("manager/wrong PIN returns null", roleOf(userDAO, "manager", "0000") == null);
        check("unknown username returns null", roleOf(userDAO, "nobody", "1111") == null);
    }

    private static String roleOf(UserDAO userDAO, String username, String pin) {
        User user = userDAO.login(username, pin);
        if (user == null) {
            return null;
        }
        return user.getRole();
    }

    // ==========================================================
    // STEP 8 - SaleDAO.recordSale checks (uses Sardines)
    // ==========================================================
    private static void step8RecordSale(ProductDAO productDAO, StockMovementDAO movementDAO) {
        System.out.println();
        System.out.println("--- STEP 8: SaleDAO.recordSale (Sardines) ---");

        Product sardines = findByName(productDAO.getAllProducts(), "Sardines");
        if (sardines == null) {
            check("Sardines exists for the sale test", false);
            return;
        }
        int sardineId = sardines.getProductId();

        int stockBefore = sardines.getStock();
        System.out.println("    Sardines stock before: " + stockBefore);

        List<SaleItem> cart = new ArrayList<>();
        cart.add(new SaleItem(sardineId, 2));

        boolean sold = new SaleDAO().recordSale(cart, null);
        check("recordSale(2 Sardines) returns true", sold);

        Product afterSale = findById(productDAO, sardineId);
        boolean stockDroppedByTwo = afterSale != null
                && afterSale.getStock() == stockBefore - 2;
        check("stock drops by exactly 2 (" + stockBefore + " -> " + (stockBefore - 2) + ")",
                stockDroppedByTwo);

        int stockAfterSale = afterSale == null ? stockBefore : afterSale.getStock();

        List<SaleItem> bigCart = new ArrayList<>();
        bigCart.add(new SaleItem(sardineId, stockAfterSale + 1000));

        boolean refused = new SaleDAO().recordSale(bigCart, null);
        check("recordSale(more than stock) returns false", !refused);

        Product stillThere = findById(productDAO, sardineId);
        boolean stockUnchanged = stillThere != null
                && stillThere.getStock() == stockAfterSale;
        check("stock is unchanged after the refused sale (" + stockAfterSale + ")",
                stockUnchanged);

        List<StockMovement> all = movementDAO.getAll();
        boolean hasSaleMinus2 = false;
        for (StockMovement m : all) {
            if ("SALE".equals(m.getReason())
                    && m.getChangeQty() == -2
                    && "Sardines".equalsIgnoreCase(m.getProductName())) {
                hasSaleMinus2 = true;
            }
        }
        check("stock_movements has SALE row with change_qty -2 for Sardines", hasSaleMinus2);
    }

    // ==========================================================
    // STEP 9 - Permissions checks (no Session needed)
    // ==========================================================
    private static void step9Permissions() {
        System.out.println();
        System.out.println("--- STEP 9: Permissions ---");

        Session.BusinessType mall = Session.BusinessType.MALL;
        Session.BusinessType tindahan = Session.BusinessType.TINDAHAN;

        check("Cashier cannot REMOVE_PRODUCT",
                !Permissions.canDo(mall, "CASHIER", Permissions.BusinessFunction.REMOVE_PRODUCT));
        check("Manager can REMOVE_PRODUCT",
                Permissions.canDo(mall, "MANAGER", Permissions.BusinessFunction.REMOVE_PRODUCT));
        check("Inventory cannot DEDUCT_STOCK",
                !Permissions.canDo(mall, "INVENTORY", Permissions.BusinessFunction.DEDUCT_STOCK));
        check("Tindahan can do all 5 functions",
                Permissions.allowedFor(tindahan, null).size() == 5);
    }

    // ==========================================================
    // Restore Sardines stock so the test can be run again
    // ==========================================================
    private static void restoreSardinesStock(ProductDAO productDAO) {
        System.out.println();
        System.out.println("--- Restoring Sardines stock (+2) ---");

        Product sardines = findByName(productDAO.getAllProducts(), "Sardines");
        if (sardines == null) {
            System.out.println("    (Sardines not found, nothing to restore)");
            return;
        }

        boolean ok = productDAO.restock(sardines.getProductId(), 2, null);
        System.out.println("    restock +2 returned: " + ok);
    }

    // ==========================================================
    // STEP 10 - CategoryDAO checks
    // ==========================================================
    private static void step10Categories() {
        System.out.println();
        System.out.println("--- STEP 10: CategoryDAO ---");

        CategoryDAO categoryDAO = new CategoryDAO();
        List<Category> all = categoryDAO.getAllCategories();

        // Show every category we found.
        for (Category c : all) {
            System.out.println("    " + c.getCategoryId() + " | " + c.getCategoryName());
        }

        boolean hasCannedGoods = false;
        if (all != null) {
            for (Category c : all) {
                if ("Canned Goods".equals(c.getCategoryName())) {
                    hasCannedGoods = true;
                }
            }
        }

        int realId = -1;
        if (all != null && !all.isEmpty()) {
            realId = all.get(0).getCategoryId();
        }

        Category found = categoryDAO.getCategoryById(realId);

        check("getAllCategories returns a list", all != null);
        check("getAllCategories returns 6 categories", all != null && all.size() == 6);
        check("\"Canned Goods\" is in the list", hasCannedGoods);
        check("getCategoryById(real id) returns a category", found != null);
        check("getCategoryById(real id) returns the same name",
                found != null && found.getCategoryName().equals(all.get(0).getCategoryName()));
        check("getCategoryById(99999) returns null",
                categoryDAO.getCategoryById(99999) == null);
    }


    /** Prints PASS or FAIL and updates the counters. */
    private static void check(String label, boolean result) {
        if (result) {
            passed++;
            System.out.println("    PASS - " + label);
        } else {
            failed++;
            System.out.println("    FAIL - " + label);
        }
    }

    /** Finds a product in a list by its exact name (ignoring upper/lower case). */
    private static Product findByName(List<Product> list, String name) {
        for (Product p : list) {
            if (name.equalsIgnoreCase(p.getName())) {
                return p;
            }
        }
        return null;
    }

    /** Finds a product in the database by its id. */
    private static Product findById(ProductDAO productDAO, int productId) {
        for (Product p : productDAO.getAllProducts()) {
            if (p.getProductId() == productId) {
                return p;
            }
        }
        return null;
    }

    /** True if the list has a row with this reason. */
    private static boolean hasReason(List<StockMovement> list, String reason) {
        for (StockMovement m : list) {
            if (reason.equals(m.getReason())) {
                return true;
            }
        }
        return false;
    }

    /** True if the list has a row with this reason AND this change_qty. */
    private static boolean hasChangeQty(List<StockMovement> list, String reason, int qty) {
        for (StockMovement m : list) {
            if (reason.equals(m.getReason()) && m.getChangeQty() == qty) {
                return true;
            }
        }
        return false;
    }

    /**
     * Deletes a "Test Item" left over from an interrupted run,
     * so the test always starts from a clean state.
     */
    private static void removeLeftoverTestItem(ProductDAO productDAO) {
        List<Product> leftovers = productDAO.searchProducts(TEST_NAME);
        for (Product p : leftovers) {
            if (p.getName().equalsIgnoreCase(TEST_NAME)) {
                System.out.println("(cleaning up an old \"" + TEST_NAME + "\" row, id "
                        + p.getProductId() + ")");
                productDAO.deleteProduct(p.getProductId(), null);
            }
        }
    }
}
