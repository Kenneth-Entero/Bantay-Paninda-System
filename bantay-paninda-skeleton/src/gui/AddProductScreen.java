package gui;

import dao.CategoryDAO;
import dao.ProductDAO;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import model.Category;
import model.Product;
import util.LogoIcon;
import util.Permissions;
import util.Permissions.BusinessFunction;
import util.Session;
import util.Theme;

/** AddProductScreen - the "Add New Product" function. */
public class AddProductScreen extends JFrame {

    private final JTextField txtName = new JTextField(20);
    private final JTextField txtPrice = new JTextField(20);
    private final JTextField txtStock = new JTextField(20);
    private final JTextField txtThreshold = new JTextField("5", 20);
    private final JComboBox<Object> cboCategory = new JComboBox<>();

    private final List<Category> categories = new CategoryDAO().getAllCategories();

    public AddProductScreen() {
        setTitle("Bantay Paninda System");
        setIconImage(LogoIcon.createIconImage(64));   // logo in the window title bar
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Permission guard
        if (!Permissions.canDo(BusinessFunction.ADD_NEW_PRODUCT)) {
            JOptionPane.showMessageDialog(this, "You are not allowed to add products.");
            dispose();
            new DashboardScreen().setVisible(true);
            return;
        }

        Container pane = getContentPane();
        pane.setLayout(new BorderLayout(0, 10));
        pane.setBackground(Theme.NAVY);
        pane.add(new HeaderPanel("Add New Product", "Fill in the product details"), BorderLayout.NORTH);

        // ---------- CENTER: form (label + field per row) ----------
        cboCategory.addItem("(no category)");
        for (Category c : categories) {
            cboCategory.addItem(c.getCategoryName());
        }

        JPanel form = new JPanel(new GridLayout(5, 2, 10, 10));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));
        addRow(form, "Product name", txtName);
        addRow(form, "Category", cboCategory);
        addRow(form, "Price", txtPrice);
        addRow(form, "Starting stock", txtStock);
        addRow(form, "Low-stock alert at", txtThreshold);
        pane.add(form, BorderLayout.CENTER);

        // ---------- BOTTOM: buttons ----------
        JButton btnSave = new JButton("Save");
        JButton btnBack = new JButton("Back");
        btnSave.addActionListener(e -> save());
        btnBack.addActionListener(e -> goBack());
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        bottom.add(btnSave);
        bottom.add(btnBack);
        pane.add(bottom, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private void addRow(JPanel form, String label, java.awt.Component field) {
        JLabel lbl = new JLabel(label);
        lbl.setForeground(Theme.WHITE);
        lbl.setFont(Theme.FONT_SUBTITLE);
        form.add(lbl);
        form.add(field);
    }

    private void save() {
        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the product name.");
            return;
        }

        double price;
        int stock;
        int threshold;
        try {
            price = Double.parseDouble(txtPrice.getText().trim());
            stock = Integer.parseInt(txtStock.getText().trim());
            threshold = Integer.parseInt(txtThreshold.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Price must be a number. Stock and alert level must be whole numbers.");
            return;
        }
        if (price < 0 || stock < 0 || threshold < 0) {
            JOptionPane.showMessageDialog(this, "Price, stock and alert level cannot be negative.");
            return;
        }

        Product p = new Product(name, price, stock);
        p.setLowStockThreshold(threshold);

        // index 0 is "(no category)"
        int chosen = cboCategory.getSelectedIndex();
        if (chosen > 0) {
            p.setCategoryId(categories.get(chosen - 1).getCategoryId());
        }

        boolean ok = new ProductDAO().addProduct(p, Session.getUserId());
        if (ok) {
            JOptionPane.showMessageDialog(this, "\"" + name + "\" was added.");
            txtName.setText("");
            txtPrice.setText("");
            txtStock.setText("");
            txtThreshold.setText("5");
            cboCategory.setSelectedIndex(0);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Could not add the product. The name may already exist.",
                    "Add failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void goBack() {
        dispose();
        new DashboardScreen().setVisible(true);
    }
}
