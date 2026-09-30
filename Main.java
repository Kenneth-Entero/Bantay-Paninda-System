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

    /*
     * Constructor to initialize product fields
     * and increment the static totalProductTypes counter
     */
    Product(String name, double price, int stock) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        Product.totalProductTypes++;
    }

    // Method to display product details
    void displayDetails() {
        System.out.println("Product: " + name + " | Price: $" + price + " | Stock: " + stock);
    }
}

/**
 * Main application class for the Store Inventory System.
 */
public class StoreInventorySystem {

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

        while (isRunning) {
            System.out.println("\n==============================================");
            System.out.println("BANTAY-PANINDA");
            System.out.println("==============================================");
            System.out.println("1. Add Product");
            System.out.println("2. Purchase Item / Deduct Stock");
            System.out.println("3. Remove Product");
            System.out.println("4. View Inventory Status & Alerts");
            System.out.println("5. Check Product Availability");
            System.out.println("6. Exit");
            System.out.print("Select an option (1-6): ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Clear input buffer

            switch (choice) {

                case 1:
                    // Add new product entry
                    System.out.println("\n--- ADD NEW PRODUCT ---");
                    System.out.print("Enter Product Name: ");
                    String name = scanner.nextLine();
                    System.out.print("Enter Price: ");
                    double price = scanner.nextDouble();
                    System.out.print("Enter Stock Quantity: ");
                    int stock = scanner.nextInt();

                    inventory.add(new Product(name, price, stock));
                    
                    System.out.println("SUCCESS: '" + name + "' added to inventory.");
                    System.out.println("Total Registered Product Types: " + Product.totalProductTypes);
                    break;

                case 2:
                    // Purchase item and deduct stock
                    System.out.println("\n--- PURCHASE / DEDUCT STOCK ---");
                    System.out.print("Enter Product Name to Purchase: ");
                    String buyName = scanner.nextLine();
                    boolean itemFoundToBuy = false;

                    for (int i = 0; i < inventory.size(); i++) {
                        Product p = inventory.get(i);

                        if (p.name.equalsIgnoreCase(buyName)) {
                            itemFoundToBuy = true;
                            System.out.print("Enter Quantity to Purchase: ");
                            int qty = scanner.nextInt();

                            if (qty <= p.stock) {
                                p.stock -= qty;
                                System.out.println("SUCCESS: Transaction complete. Remaining stock for " + p.name + ": " + p.stock);

                                if (p.stock <= LOW_STOCK_THRESHOLD) {
                                    System.out.println("WARNING: Low stock detected for '" + p.name + "' (" + p.stock + " remaining). Restock immediately.");
                                }
                            } else {
                                System.out.println("ERROR: Insufficient stock / inventory discrepancy. Current stock: " + p.stock);
                            }
                            break;
                        }
                    }

                    if (!itemFoundToBuy) {
                        System.out.println("ERROR: Product '" + buyName + "' not found.");
                    }
                    break;

                case 3:
                    // Remove existing product record
                    System.out.println("\n--- REMOVE PRODUCT ---");
                    System.out.print("Enter Product Name to Remove: ");
                    String removeName = scanner.nextLine();
                    boolean itemFoundToRemove = false;

                    for (int i = 0; i < inventory.size(); i++) {
                        if (inventory.get(i).name.equalsIgnoreCase(removeName)) {
                            inventory.remove(i);
                            itemFoundToRemove = true;
                            
                            Product.totalProductTypes--; 
                            
                            System.out.println("SUCCESS: Product '" + removeName + "' removed from inventory.");
                            break;
                        }
                    }

                    if (!itemFoundToRemove) {
                        System.out.println("ERROR: Product '" + removeName + "' not found in inventory.");
                    }
                    break;

                case 4:
                    // Display inventory records and active warnings
                    System.out.println("\n--- CURRENT INVENTORY STATUS ---");

                    if (inventory.size() == 0) {
                        System.out.println("Inventory is currently empty.");
                    } else {
                        for (int i = 0; i < inventory.size(); i++) {
                            Product p = inventory.get(i);
                            System.out.print("[" + (i + 1) + "] ");
                            p.displayDetails();

                            if (p.stock <= LOW_STOCK_THRESHOLD) {
                                System.out.println("    └─ [LOW STOCK ALERT] Quantity is at or below threshold (" + LOW_STOCK_THRESHOLD + ")");
                            }
                        }
                    }
                    System.out.println("\nTotal Registered Product Types: " + Product.totalProductTypes);
                    break;

                case 5:
                    // Check product availability
                    System.out.println("\n--- CHECK PRODUCT AVAILABILITY ---");
                    System.out.print("Enter Product Name to Check: ");
                    String checkName = scanner.nextLine();
                    
                    boolean exists = false;
                    for (int i = 0; i < inventory.size(); i++) {
                        if (inventory.get(i).name.equalsIgnoreCase(checkName)) {
                            exists = true;
                            break;
                        }
                    }

                    if (exists) {
                        System.out.println("STATUS: Product '" + checkName + "' is available in stock.");
                    } else {
                        System.out.println("STATUS: Product '" + checkName + "' is NOT available in inventory.");
                    }
                    break;

                case 6:
                    // Exit application
                    System.out.println("Exiting Store Inventory System.");
                    isRunning = false;
                    break;

                default:
                    System.out.println("ERROR: Invalid input option. Select a number between 1 and 6.");
                    break;
            }
        }

        scanner.close();
    }
}
