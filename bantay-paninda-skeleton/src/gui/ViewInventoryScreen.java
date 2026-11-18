package gui;

import dao.ProductDAO;
import dao.SaleDAO;
import dao.StockMovementDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.Product;
import model.Sale;
import model.SaleItem;
import model.StockMovement;
import util.LogoIcon;
import util.Permissions;
import util.Permissions.BusinessFunction;
import util.Theme;

/**
 * ViewInventoryScreen - "View Inventory Status & Alerts".
 *
 * Three tabs:
 *   1. Inventory     - totals, search, low-stock alerts, product table
 *   2. Stock History - every stock change (from stock_movements)
 *   3. Sales History - every sale and its items (from sales / sale_items)
 *
 * Lesson 13 concepts used: extends JFrame, getContentPane(), GridLayout,
 * JLabel, JTextField (setEditable(false), getText/setText), addActionListener.
 */
public class ViewInventoryScreen extends JFrame {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ProductDAO productDAO = new ProductDAO();
    private final StockMovementDAO movementDAO = new StockMovementDAO();
    private final SaleDAO saleDAO = new SaleDAO();

    // ---------- Inventory tab ----------
    private final JTextField txtTotal = new JTextField();      // Lesson 13: JTextField
    private final JTextField txtLow = new JTextField();
    private final JTextField txtOut = new JTextField();
    private final JTextField txtSearch = new JTextField(18);
    private final JLabel lblAlert = new JLabel(" ");
    private final DefaultTableModel productModel = readOnlyModel(
            "Name", "Price", "Stock", "Alert at", "Status");
    private final JTable productTable = new JTable(productModel);

    // ---------- Stock History tab ----------
    private final DefaultTableModel historyModel = readOnlyModel(
            "Date", "Product", "Change", "Reason", "By");

    // ---------- Sales History tab ----------
    private final DefaultTableModel salesModel = readOnlyModel("Sale #", "Date", "Total (PHP)");
    private final JTable salesTable = new JTable(salesModel);
    private final DefaultTableModel itemsModel = readOnlyModel("Item", "Qty", "Price", "Subtotal");
    private List<Sale> shownSales = new ArrayList<>();

    public ViewInventoryScreen() {
        setTitle("Bantay Paninda System");
        setIconImage(LogoIcon.createIconImage(64));   // logo in the window title bar
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Permission guard
        if (!Permissions.canDo(BusinessFunction.VIEW_INVENTORY)) {
            JOptionPane.showMessageDialog(this, "You are not allowed to view the inventory.");
            dispose();
            new DashboardScreen().setVisible(true);
            return;
        }

        // Lesson 13: the content pane of the JFrame
        Container pane = getContentPane();
        pane.setLayout(new BorderLayout(0, 10));
        pane.setBackground(Theme.NAVY);
        pane.add(new HeaderPanel("Inventory Status & Alerts",
                "Stock levels, history and sales"), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Inventory", buildInventoryTab());
        tabs.addTab("Stock History", buildHistoryTab());
        tabs.addTab("Sales History", buildSalesTab());
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        center.add(tabs, BorderLayout.CENTER);
        pane.add(center, BorderLayout.CENTER);

        JButton btnRefresh = new JButton("Refresh");
        JButton btnBack = new JButton("Back");
        btnRefresh.addActionListener(e -> reloadAll());
        btnBack.addActionListener(e -> goBack());
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        bottom.add(btnRefresh);
        bottom.add(btnBack);
        pane.add(bottom, BorderLayout.SOUTH);

        reloadAll();
        pack();
        setLocationRelativeTo(null);
    }

    // ==========================================================
    // TAB 1: Inventory
    // ==========================================================
    private JPanel buildInventoryTab() {
        JPanel tab = new JPanel(new BorderLayout(0, 8));
        tab.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top: totals in a GridLayout (label above a read-only field)
        JPanel totals = new JPanel(new GridLayout(2, 3, 10, 2));      // Lesson 13: GridLayout
        totals.add(new JLabel("Total Products"));                      // Lesson 13: JLabel
        totals.add(new JLabel("Low Stock"));
        totals.add(new JLabel("Out of Stock"));
        for (JTextField f : new JTextField[] { txtTotal, txtLow, txtOut }) {
            f.setEditable(false);                                      // Lesson 13: setEditable(false)
            f.setHorizontalAlignment(JTextField.CENTER);
            totals.add(f);
        }

        // Search row
        JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JButton btnSearch = new JButton("Search");
        JButton btnAll = new JButton("Show all");
        txtSearch.addActionListener(e -> loadProducts());              // Enter key
        btnSearch.addActionListener(e -> loadProducts());              // Lesson 13: addActionListener
        btnAll.addActionListener(e -> {
            txtSearch.setText("");
            loadProducts();
        });
        search.add(new JLabel("Search"));
        search.add(txtSearch);
        search.add(btnSearch);
        search.add(btnAll);

        lblAlert.setFont(Theme.FONT_BUTTON);

        JPanel north = new JPanel(new BorderLayout(0, 6));
        north.add(totals, BorderLayout.NORTH);
        north.add(search, BorderLayout.CENTER);
        north.add(lblAlert, BorderLayout.SOUTH);
        tab.add(north, BorderLayout.NORTH);

        // Table with coloured rows: LOW = yellow, OUT = red
        productTable.setRowHeight(24);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productTable.setDefaultRenderer(Object.class, new StatusRenderer());
        JScrollPane scroll = new JScrollPane(productTable);
        scroll.setPreferredSize(new Dimension(640, 250));
        tab.add(scroll, BorderLayout.CENTER);
        return tab;
    }

    /** Colours a row by its Status column (OK / LOW / OUT). */
    private class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                String status = String.valueOf(productModel.getValueAt(
                        table.convertRowIndexToModel(row), 4));
                if (status.equals("OUT")) {
                    c.setBackground(new Color(255, 205, 205));
                } else if (status.equals("LOW")) {
                    c.setBackground(new Color(255, 236, 179));
                } else {
                    c.setBackground(Color.WHITE);
                }
                c.setForeground(Color.BLACK);
            }
            return c;
        }
    }

    // ==========================================================
    // TAB 2: Stock History
    // ==========================================================
    private JPanel buildHistoryTab() {
        JPanel tab = new JPanel(new BorderLayout());
        tab.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JTable table = new JTable(historyModel);
        table.setRowHeight(24);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(640, 330));
        tab.add(scroll, BorderLayout.CENTER);
        return tab;
    }

    // ==========================================================
    // TAB 3: Sales History (top: sales, bottom: items of the selected sale)
    // ==========================================================
    private JPanel buildSalesTab() {
        JPanel tab = new JPanel(new GridLayout(2, 1, 0, 8));           // Lesson 13: GridLayout
        tab.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        salesTable.setRowHeight(24);
        salesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        salesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showItemsOfSelectedSale();
            }
        });
        JScrollPane salesScroll = new JScrollPane(salesTable);
        salesScroll.setBorder(BorderFactory.createTitledBorder("Sales (select one to see its items)"));

        JTable itemsTable = new JTable(itemsModel);
        itemsTable.setRowHeight(24);
        JScrollPane itemsScroll = new JScrollPane(itemsTable);
        itemsScroll.setBorder(BorderFactory.createTitledBorder("Items of the selected sale"));

        salesScroll.setPreferredSize(new Dimension(640, 160));
        itemsScroll.setPreferredSize(new Dimension(640, 160));
        tab.add(salesScroll);
        tab.add(itemsScroll);
        return tab;
    }

    // ==========================================================
    // LOADING DATA (all database work stays in the DAOs)
    // ==========================================================
    private void reloadAll() {
        loadSummary();
        loadProducts();
        loadHistory();
        loadSales();
    }

    /** Totals and the alert line always count ALL products, not only the search result. */
    private void loadSummary() {
        List<Product> all = productDAO.getAllProducts();
        int low = 0;
        int out = 0;
        for (Product p : all) {
            if (p.getStock() == 0) {
                out++;
            } else if (p.isLowStock()) {
                low++;
            }
        }
        txtTotal.setText(String.valueOf(productDAO.countProducts()));
        txtLow.setText(String.valueOf(low));
        txtOut.setText(String.valueOf(out));

        if (low + out > 0) {
            lblAlert.setForeground(new Color(190, 60, 0));
            lblAlert.setText("ALERT: " + (low + out) + " item(s) need restocking");
        } else {
            lblAlert.setForeground(new Color(0, 120, 60));
            lblAlert.setText("All items are well stocked.");
        }
    }

    private void loadProducts() {
        String keyword = txtSearch.getText().trim();
        List<Product> list = keyword.isEmpty()
                ? productDAO.getAllProducts()
                : productDAO.searchProducts(keyword);

        productModel.setRowCount(0);
        for (Product p : list) {
            String status = "OK";
            if (p.getStock() == 0) {
                status = "OUT";
            } else if (p.isLowStock()) {
                status = "LOW";
            }
            productModel.addRow(new Object[] {
                p.getName(), String.format("%.2f", p.getPrice()),
                p.getStock(), p.getLowStockThreshold(), status });
        }
    }

    private void loadHistory() {
        historyModel.setRowCount(0);
        for (StockMovement m : movementDAO.getAll()) {
            int qty = m.getChangeQty();
            historyModel.addRow(new Object[] {
                formatDate(m.getMovementDate()),
                m.getProductName(),
                (qty > 0 ? "+" : "") + qty,
                m.getReason(),
                m.getUserId() == null ? "Tindahan" : "User #" + m.getUserId() });
        }
    }

    private void loadSales() {
        shownSales = saleDAO.getAllSales();
        salesModel.setRowCount(0);
        itemsModel.setRowCount(0);
        for (Sale s : shownSales) {
            salesModel.addRow(new Object[] {
                s.getSaleId(), formatDate(s.getSaleDate()),
                String.format("%.2f", s.getTotalAmount()) });
        }
    }

    private void showItemsOfSelectedSale() {
        itemsModel.setRowCount(0);
        int row = salesTable.getSelectedRow();
        if (row < 0 || row >= shownSales.size()) {
            return;
        }
        for (SaleItem item : saleDAO.getItemsBySale(shownSales.get(row).getSaleId())) {
            itemsModel.addRow(new Object[] {
                item.getProductName(), item.getQuantity(),
                String.format("%.2f", item.getUnitPrice()),
                String.format("%.2f", item.getSubtotal()) });
        }
    }

    // ==========================================================
    // SMALL HELPERS
    // ==========================================================
    private static DefaultTableModel readOnlyModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private static String formatDate(LocalDateTime date) {
        return date == null ? "" : date.format(DATE_FORMAT);
    }

    private void goBack() {
        dispose();
        new DashboardScreen().setVisible(true);
    }
}