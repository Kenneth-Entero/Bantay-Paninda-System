import java.util.ArrayList;
import java.util.Scanner;

/**
 * Represents an individual product in the store inventory.
 */
class Product {
    // Instance variables
    String name;
    double price;
    int stock;

    // Static variable tracking total registered product types
    static int totalProductTypes = 0;

    /**
     * Constructor to initialize product fields
     */
    Product(String pName, double pPrice, int pStock) {
        name = pName;
        price = pPrice;
        stock = pStock;
        Product.totalProductTypes++;
    }

    // Method to display product details
    void displayDetails() {
        System.out.println("Product: " + name + " | Price: Php " + price + " | Stock: " + stock);
    }
}

/**
 * Main application class for the Store Inventory System.
 */
public class BantayPanindaSystem {

    // Threshold limit for low-stock warning
    private static final int LOW_STOCK_THRESHOLD = 5;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Dynamic ArrayList for managing product collections
        ArrayList<Product> inventory = new ArrayList<>();

        // Initial sample data
        inventory.add(new Product("Sardines", 28.50, 10));
        inventory.add(new Product("Noodles", 15.00, 3));

        boolean isRunning = true;

        System.out.println("==============================================");
        System.out.println("===== WELCOME TO BANTAY PANINDA SYSTEM! =======");
        System.out.println("==============================================");

        while (isRunning) {
            System.out.println("\n============= MENU ==================");
            System.out.println("1. Add New Product");
            System.out.println("2. Deduct Stock");
            System.out.println("3. Restock Product");
            System.out.println("4. Remove Product");
            System.out.println("5. View Inventory Status & Alerts");
            System.out.println("6. Exit");
            System.out.print("Select an option (1-6): ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Clear input buffer

            // Switch statement for menu selection
            switch (choice) {

                case 1:
                    // Add new product entry to the store
                    System.out.println("\n--- ADD NEW PRODUCT ---");
                    System.out.print("Enter Product Name: ");
                    String newProductName = scanner.nextLine();

                    boolean existingProductFound = false;

                    // Check if product already exists to prevent duplicate entries
                    for (Product p : inventory) {
                        if (p.name.equalsIgnoreCase(newProductName)) {
                            existingProductFound = true;
                            System.out.println("ERROR: Product '" + p.name + "' already exists! Duplicate products are not allowed.");
                            break;
                        }
                    }

                    // Proceed to enter price and stock only if product is unique
                    if (!existingProductFound) {
                        System.out.print("Enter Price: ");
                        double productPrice = scanner.nextDouble();
                        System.out.print("Enter Initial Stock Quantity: ");
                        int initialStock = scanner.nextInt();

                        inventory.add(new Product(newProductName, productPrice, initialStock));

                        System.out.println("SUCCESS: '" + newProductName + "' added to inventory.");
                        System.out.println("Total Registered Product Types: " + Product.totalProductTypes);
                    }
                    break;

                case 2:
                    // Deduct stock from the inventory
                    System.out.println("\n--- DEDUCT STOCK ---");
                    System.out.print("Enter Product Name to Deduct: ");
                    String productToDeduct = scanner.nextLine();
                    boolean itemFoundToDeduct = false;

                    for (Product p : inventory) {
                        if (p.name.equalsIgnoreCase(productToDeduct)) {
                            itemFoundToDeduct = true;
                            System.out.print("Enter Quantity to Deduct: ");
                            int quantityToDeduct = scanner.nextInt();

                            // Validate stock availability
                            if (quantityToDeduct <= p.stock) {
                                p.stock -= quantityToDeduct;
                                System.out.println("SUCCESS: Stock deducted. Remaining stock for " + p.name + ": " + p.stock);

                                // Low stock checking
                                if (p.stock <= LOW_STOCK_THRESHOLD) {
                                    System.out.println("WARNING: Low stock detected for '" + p.name + "' (" + p.stock + " remaining). Restock immediately.");
                                }
                            } else {
                                System.out.println("ERROR: Insufficient stock / inventory discrepancy. Current stock: " + p.stock);
                            }
                            break;
                        }
                    }

                    if (!itemFoundToDeduct) {
                        System.out.println("ERROR: Product '" + productToDeduct + "' not found in inventory.");
                    }
                    break;

                case 3:
                    // Restock existing product in the inventory
                    System.out.println("\n--- RESTOCK PRODUCT ---");
                    System.out.print("Enter Product Name to Restock: ");
                    String productToRestock = scanner.nextLine();
                    boolean itemFoundToRestock = false;

                    for (Product p : inventory) {
                        if (p.name.equalsIgnoreCase(productToRestock)) {
                            itemFoundToRestock = true;
                            System.out.print("Enter Quantity to Add (Restock): ");
                            int quantityToRestock = scanner.nextInt();

                            if (quantityToRestock > 0) {
                                p.stock += quantityToRestock;
                                System.out.println("SUCCESS: Stock added. Updated stock for " + p.name + ": " + p.stock);
                            } else {
                                System.out.println("ERROR: Restock quantity must be greater than zero.");
                            }
                            break;
                        }
                    }

                    if (!itemFoundToRestock) {
                        System.out.println("ERROR: Product '" + productToRestock + "' not found in inventory.");
                    }
                    break;

                case 4:
                    // Remove existing product record from the store
                    System.out.println("\n--- REMOVE PRODUCT ---");
                    System.out.print("Enter Product Name to Remove: ");
                    String productToRemove = scanner.nextLine();
                    boolean itemFoundToRemove = false;

                    for (Product p : inventory) {
                        if (p.name.equalsIgnoreCase(productToRemove)) {
                            inventory.remove(p);
                            itemFoundToRemove = true;
                            
                            Product.totalProductTypes--; 
                            
                            System.out.println("SUCCESS: Product '" + productToRemove + "' removed from inventory.");
                            break;
                        }
                    }

                    if (!itemFoundToRemove) {
                        System.out.println("ERROR: Product '" + productToRemove + "' not found in inventory.");
                    }
                    break;

                case 5:
                    // Display inventory records and active warnings
                    System.out.println("\n--- CURRENT INVENTORY STATUS ---");

                    if (inventory.isEmpty()) {
                        System.out.println("Inventory is currently empty.");
                    } else {
                        int itemNum = 1;
                        for (Product p : inventory) {
                            System.out.print("[" + itemNum + "] ");
                            p.displayDetails();

                            if (p.stock <= LOW_STOCK_THRESHOLD) {
                                System.out.println("    └─ [LOW STOCK ALERT] Quantity is at or below threshold (" + LOW_STOCK_THRESHOLD + ")");
                            }
                            itemNum++;
                        }
                    }
                    System.out.println("\nTotal Registered Product Types: " + Product.totalProductTypes);
                    break;

                case 6:
                    // Exit Bantay Paninda System
                    System.out.println("Exiting Bantay Paninda System.");
                    isRunning = false;
                    break;

                default:
                    System.out.println("ERROR: Invalid input option. Select a number from 1 to 6.");
                    break;
            }
        }

        scanner.close();
    }
}
