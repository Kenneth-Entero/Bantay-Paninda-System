package gui;

import dao.ProductDAO;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import model.Product;
import util.LogoIcon;
import util.Permissions;
import util.Permissions.BusinessFunction;
import util.Session;
import util.Theme;

/** RemoveProductScreen - the "Remove Product" function. */
public class RemoveProductScreen extends JFrame {

    private final ProductTablePanel productPanel = new ProductTablePanel();

    public RemoveProductScreen() {
        setTitle("Bantay Paninda System");
        setIconImage(LogoIcon.createIconImage(64));   // logo in the window title bar
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        if (!Permissions.canDo(BusinessFunction.REMOVE_PRODUCT)) {
            JOptionPane.showMessageDialog(this, "You are not allowed to remove products.");
            dispose();
            new DashboardScreen().setVisible(true);
            return;
        }

        Container pane = getContentPane();
        pane.setLayout(new BorderLayout(0, 10));
        pane.setBackground(Theme.NAVY);
        pane.add(new HeaderPanel("Remove Product", "Select the product to remove"), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        center.add(productPanel, BorderLayout.CENTER);
        pane.add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        bottom.setOpaque(false);
        JButton btnRemove = new JButton("Remove");
        JButton btnBack = new JButton("Back");
        btnRemove.addActionListener(e -> remove());
        btnBack.addActionListener(e -> goBack());
        bottom.add(btnRemove);
        bottom.add(btnBack);
        pane.add(bottom, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private void remove() {
        Product p = productPanel.getSelectedProduct();
        if (p == null) {
            JOptionPane.showMessageDialog(this, "Please select a product first.");
            return;
        }

        int answer = JOptionPane.showConfirmDialog(this,
                "Remove \"" + p.getName() + "\"? This cannot be undone.",
                "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        boolean ok = new ProductDAO().deleteProduct(p.getProductId(), Session.getUserId());
        if (ok) {
            JOptionPane.showMessageDialog(this, "\"" + p.getName() + "\" was removed.");
            productPanel.reload();
        } else {
            JOptionPane.showMessageDialog(this, "Remove failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void goBack() {
        dispose();
        new DashboardScreen().setVisible(true);
    }
}