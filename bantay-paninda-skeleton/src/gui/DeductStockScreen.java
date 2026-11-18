package gui;

import dao.SaleDAO;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import model.Product;
import model.SaleItem;
import util.LogoIcon;
import util.Permissions;
import util.Permissions.BusinessFunction;
import util.Session;
import util.Theme;

/**
 * DeductStockScreen - the "Deduct Stock" function = a SALE.
 *
 * The cashier picks products, puts them in a cart, and presses Checkout.
 * SaleDAO.recordSale() then saves the receipt (sales + sale_items),
 * lowers the stock and writes the SALE history, all in ONE transaction.
 */
public class DeductStockScreen extends JFrame {

    private final ProductTablePanel productPanel = new ProductTablePanel();
    private final JTextField txtQty = new JTextField("1", 5);

    // The cart: one SaleItem per product (the name and price are kept
    // here only to show them in the cart table; SaleDAO reads the real
    // name and price again from the database when the sale is saved).
    private final List<SaleItem> cart = new ArrayList<>();
    // The cart table (cells cannot be edited)
    private final DefaultTableModel cartModel =
            new DefaultTableModel(new Object[] { "Item", "Qty", "Price", "Subtotal" }, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
    private final JTable cartTable = new JTable(cartModel);
    private final JLabel lblTotal = new JLabel("Total: PHP 0.00");

    public DeductStockScreen() {
        setTitle("Bantay Paninda System");
        setIconImage(LogoIcon.createIconImage(64));   // logo in the window title bar
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Permission guard
        if (!Permissions.canDo(BusinessFunction.DEDUCT_STOCK)) {
            JOptionPane.showMessageDialog(this, "You are not allowed to deduct stock.");
            dispose();
            new DashboardScreen().setVisible(true);
            return;
        }

        Container pane = getContentPane();
        pane.setLayout(new BorderLayout(0, 10));
        pane.setBackground(Theme.NAVY);
        pane.add(new HeaderPanel("Deduct Stock", "Pick the items sold, then Checkout"), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(16, 0));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        center.add(buildLeft(), BorderLayout.CENTER);
        center.add(buildRight(), BorderLayout.EAST);
        pane.add(center, BorderLayout.CENTER);

        JButton btnBack = new JButton("Back");
        btnBack.addActionListener(e -> goBack());
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        bottom.add(btnBack);
        pane.add(bottom, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    // ---------- LEFT: products + quantity + "Add to Cart" ----------
    private JPanel buildLeft() {
        JPanel left = new JPanel(new BorderLayout(0, 8));
        left.setOpaque(false);
        left.add(productPanel, BorderLayout.CENTER);

        JPanel add = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        add.setOpaque(false);
        JLabel lbl = new JLabel("Quantity");
        lbl.setForeground(Theme.WHITE);
        lbl.setFont(Theme.FONT_SUBTITLE);
        JButton btnAdd = new JButton("Add to Cart");
        btnAdd.addActionListener(e -> addToCart());
        add.add(lbl);
        add.add(txtQty);
        add.add(btnAdd);
        left.add(add, BorderLayout.SOUTH);
        return left;
    }

    // ---------- RIGHT: cart + total + buttons ----------
    private JPanel buildRight() {
        JPanel right = new JPanel(new BorderLayout(0, 8));
        right.setOpaque(false);

        JLabel title = new JLabel("Cart");
        title.setForeground(Theme.WHITE);
        title.setFont(Theme.FONT_SUBTITLE);
        right.add(title, BorderLayout.NORTH);

        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cartTable.setRowHeight(24);
        JScrollPane scroll = new JScrollPane(cartTable);
        scroll.setPreferredSize(new Dimension(400, 230));
        right.add(scroll, BorderLayout.CENTER);

        lblTotal.setForeground(Theme.GOLD);
        lblTotal.setFont(Theme.FONT_TITLE);

        JButton btnRemove = new JButton("Remove item");
        JButton btnClear = new JButton("Clear cart");
        JButton btnCheckout = new JButton("Checkout");
        btnRemove.addActionListener(e -> removeFromCart());
        btnClear.addActionListener(e -> {
            cart.clear();
            refreshCart();
        });
        btnCheckout.addActionListener(e -> checkout());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(btnRemove);
        buttons.add(btnClear);
        buttons.add(btnCheckout);

        JPanel south = new JPanel(new BorderLayout(0, 6));
        south.setOpaque(false);
        south.add(lblTotal, BorderLayout.NORTH);
        south.add(buttons, BorderLayout.SOUTH);
        right.add(south, BorderLayout.SOUTH);
        return right;
    }

    // ==========================================================
    // CART ACTIONS
    // ==========================================================

    private void addToCart() {
        Product p = productPanel.getSelectedProduct();
        if (p == null) {
            JOptionPane.showMessageDialog(this, "Please select a product first.");
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(txtQty.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Quantity must be a whole number.");
            return;
        }
        if (qty <= 0) {
            JOptionPane.showMessageDialog(this, "Quantity must be greater than 0.");
            return;
        }

        // The cart may already hold some of this product.
        SaleItem existing = findInCart(p.getProductId());
        int inCart = (existing == null) ? 0 : existing.getQuantity();

        if (inCart + qty > p.getStock()) {
            JOptionPane.showMessageDialog(this,
                    "Not enough stock for \"" + p.getName() + "\".\n"
                    + "In stock: " + p.getStock() + ", already in cart: " + inCart);
            return;
        }

        if (existing != null) {
            existing.setQuantity(inCart + qty);
            existing.computeSubtotal();
        } else {
            SaleItem item = new SaleItem(p.getProductId(), qty);
            item.setProductName(p.getName());
            item.setUnitPrice(p.getPrice());
            item.computeSubtotal();
            cart.add(item);
        }
        txtQty.setText("1");
        refreshCart();
    }

    private void removeFromCart() {
        int row = cartTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item in the cart.");
            return;
        }
        cart.remove(row);
        refreshCart();
    }

    private SaleItem findInCart(int productId) {
        for (SaleItem item : cart) {
            if (item.getProductId() != null && item.getProductId() == productId) {
                return item;
            }
        }
        return null;
    }

    private double cartTotal() {
        double total = 0.0;
        for (SaleItem item : cart) {
            total += item.getSubtotal();
        }
        return Math.round(total * 100.0) / 100.0;
    }

    private void refreshCart() {
        cartModel.setRowCount(0);
        for (SaleItem item : cart) {
            cartModel.addRow(new Object[] {
                item.getProductName(),
                item.getQuantity(),
                String.format("%.2f", item.getUnitPrice()),
                String.format("%.2f", item.getSubtotal()) });
        }
        lblTotal.setText(String.format("Total: PHP %.2f", cartTotal()));
    }

    // ==========================================================
    // CHECKOUT = the sale
    // ==========================================================
    private void checkout() {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "The cart is empty.");
            return;
        }

        int answer = JOptionPane.showConfirmDialog(this,
                String.format("Confirm this sale?\nTotal: PHP %.2f", cartTotal()),
                "Checkout", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        // A fresh list: only the product id and quantity are sent.
        // SaleDAO reads the real name and price from the database.
        List<SaleItem> toSell = new ArrayList<>();
        for (SaleItem item : cart) {
            toSell.add(new SaleItem(item.getProductId(), item.getQuantity()));
        }

        boolean ok = new SaleDAO().recordSale(toSell, Session.getUserId());

        if (ok) {
            // recordSale filled the real name, price and subtotal.
            StringBuilder receipt = new StringBuilder("Sale saved.\n\n");
            double total = 0.0;
            for (SaleItem item : toSell) {
                receipt.append(item).append("\n");
                total += item.getSubtotal();
            }
            total = Math.round(total * 100.0) / 100.0;
            receipt.append(String.format("%nTotal: PHP %.2f", total));
            JOptionPane.showMessageDialog(this, receipt.toString(), "Receipt",
                    JOptionPane.INFORMATION_MESSAGE);
            cart.clear();
            refreshCart();
        } else {
            JOptionPane.showMessageDialog(this,
                    "The sale was NOT saved (nothing was changed).\n"
                    + "The stock may have changed. Please check the list and try again.",
                    "Sale failed", JOptionPane.ERROR_MESSAGE);
        }
        productPanel.reload();      // show the new stock
    }

    private void goBack() {
        dispose();
        new DashboardScreen().setVisible(true);
    }
}