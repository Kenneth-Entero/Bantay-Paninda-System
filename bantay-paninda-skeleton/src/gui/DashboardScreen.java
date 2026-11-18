package gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import util.LogoIcon;
import util.Permissions;
import util.Permissions.BusinessFunction;
import util.Session;
import util.Theme;

/**
 * DashboardScreen - main menu after login.
 * Only the buttons the current user is allowed to use are shown
 * (the rules are in util/Permissions.java).
 */
public class DashboardScreen extends JFrame {

    public DashboardScreen() {

        setTitle("Bantay Paninda System");
        setIconImage(LogoIcon.createIconImage(64));   // logo in the window title bar
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Nobody is logged in: go back to the start.
        if (!Session.isLoggedIn()) {
            dispose();
            new ModeScreen().setVisible(true);
            return;
        }

        Container pane = getContentPane();
        pane.setLayout(new BorderLayout(0, 10));
        pane.setBackground(Theme.NAVY);

        // ---------- TOP: logo + title + who is logged in ----------
        HeaderPanel header = new HeaderPanel("Dashboard", "Choose a task");
        if (Session.isTindahan()) {
            header.setRightText("Tindahan");
        } else {
            header.setRightText(Session.getCurrentUser().getFullName()
                    + " - " + Session.getRole());
        }
        pane.add(header, BorderLayout.NORTH);

        // ---------- CENTER: one button per allowed function ----------
        JPanel buttons = new JPanel(new GridLayout(0, 2, 16, 16));   // Lesson 13: GridLayout
        buttons.setOpaque(false);
        buttons.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        addButton(buttons, BusinessFunction.ADD_NEW_PRODUCT, "Add New Product");
        addButton(buttons, BusinessFunction.DEDUCT_STOCK, "Deduct Stock");
        addButton(buttons, BusinessFunction.RESTOCK, "Restock");
        addButton(buttons, BusinessFunction.REMOVE_PRODUCT, "Remove Product");
        addButton(buttons, BusinessFunction.VIEW_INVENTORY, "<html><center>View Inventory<br>Status &amp; Alerts</center></html>");

        pane.add(buttons, BorderLayout.CENTER);

        // ---------- BOTTOM: logout ----------
        JButton btnLogout = new JButton("Logout");
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> logout());
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        bottom.add(btnLogout);
        pane.add(bottom, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    /** Adds a button only when the user may do this function. */
    private void addButton(JPanel panel, BusinessFunction function, String text) {
        if (!Permissions.canDo(function)) {
            return;
        }

        JButton btn = new JButton(text);
        btn.setBackground(Theme.GOLD);
        btn.setForeground(Theme.DARK_NAVY);
        btn.setFont(Theme.FONT_BUTTON);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setPreferredSize(new java.awt.Dimension(220, 80));
        btn.addActionListener(e -> openFunction(function));
        panel.add(btn);
    }

    /** Opens the screen of the chosen function. */
    private void openFunction(BusinessFunction function) {
        switch (function) {
            case ADD_NEW_PRODUCT:
                dispose();
                new AddProductScreen().setVisible(true);
                break;
            case DEDUCT_STOCK:
                dispose();
                new DeductStockScreen().setVisible(true);
                break;
            case VIEW_INVENTORY:
                dispose();
                new ViewInventoryScreen().setVisible(true);
                break;
            case RESTOCK:
                dispose();
                new RestockScreen().setVisible(true);
                break;
            case REMOVE_PRODUCT:
                dispose();
                new RemoveProductScreen().setVisible(true);
                break;
            default:
                JOptionPane.showMessageDialog(this, function + " (gagawin sa susunod na step)");
        }
    }

    private void logout() {
        Session.logout();
        dispose();
        new ModeScreen().setVisible(true);
    }
}