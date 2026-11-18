package gui;

import dao.ProductDAO;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import model.Product;
import util.LogoIcon;
import util.Permissions;
import util.Permissions.BusinessFunction;
import util.Session;
import util.Theme;

/** RestockScreen - the "Restock" function: add stock to an existing product. */
public class RestockScreen extends JFrame {

    private final ProductTablePanel productPanel = new ProductTablePanel();
    private final JTextField txtQty = new JTextField(6);

    public RestockScreen() {
        setTitle("Bantay Paninda System");
        setIconImage(LogoIcon.createIconImage(64));   // logo in the window title bar
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        if (!Permissions.canDo(BusinessFunction.RESTOCK)) {
            JOptionPane.showMessageDialog(this, "You are not allowed to restock.");
            dispose();
            new DashboardScreen().setVisible(true);
            return;
        }

        Container pane = getContentPane();
        pane.setLayout(new BorderLayout(0, 10));
        pane.setBackground(Theme.NAVY);
        pane.add(new HeaderPanel("Restock", "Select a product and enter the quantity"), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        center.add(productPanel, BorderLayout.CENTER);
        pane.add(center, BorderLayout.CENTER);

        // ---------- BOTTOM: quantity + buttons ----------
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        bottom.setOpaque(false);
        JLabel lbl = new JLabel("Quantity to add");
        lbl.setForeground(Theme.WHITE);
        lbl.setFont(Theme.FONT_SUBTITLE);
        JButton btnRestock = new JButton("Restock");
        JButton btnBack = new JButton("Back");
        btnRestock.addActionListener(e -> restock());
        btnBack.addActionListener(e -> goBack());
        bottom.add(lbl);
        bottom.add(txtQty);
        bottom.add(btnRestock);
        bottom.add(btnBack);
        pane.add(bottom, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private void restock() {
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

        boolean ok = new ProductDAO().restock(p.getProductId(), qty, Session.getUserId());
        if (ok) {
            JOptionPane.showMessageDialog(this,
                    qty + " added to \"" + p.getName() + "\".");
            txtQty.setText("");
            productPanel.reload();
        } else {
            JOptionPane.showMessageDialog(this, "Restock failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void goBack() {
        dispose();
        new DashboardScreen().setVisible(true);
    }
}