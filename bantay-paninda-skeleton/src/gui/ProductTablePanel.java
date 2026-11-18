package gui;

import dao.ProductDAO;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import model.Product;
import util.Theme;

/**
 * ProductTablePanel - a search box and a product table.
 * Shared by the Restock, Remove Product (and later Deduct Stock) screens
 * so the same code is not written again and again.
 *
 * Columns: Name | Price | Stock | Status (OK / LOW / OUT)
 */
public class ProductTablePanel extends JPanel {

    private final ProductDAO productDAO = new ProductDAO();

    private final JTextField txtSearch = new JTextField(18);
    private final DefaultTableModel model;
    private final JTable table;

    // The products currently shown, in the same order as the table rows.
    private List<Product> shown = new ArrayList<>();

    public ProductTablePanel() {
        setLayout(new BorderLayout(0, 8));
        setOpaque(false);

        // ---------- TOP: search ----------
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        top.setOpaque(false);
        JLabel lbl = new JLabel("Search");
        lbl.setForeground(Theme.WHITE);
        lbl.setFont(Theme.FONT_SUBTITLE);
        JButton btnSearch = new JButton("Search");
        JButton btnAll = new JButton("Show all");
        top.add(lbl);
        top.add(txtSearch);
        top.add(btnSearch);
        top.add(btnAll);
        add(top, BorderLayout.NORTH);

        txtSearch.addActionListener(e -> search());     // Enter key
        btnSearch.addActionListener(e -> search());
        btnAll.addActionListener(e -> {
            txtSearch.setText("");
            reload();
        });

        // ---------- CENTER: table (cells cannot be edited) ----------
        model = new DefaultTableModel(new Object[] { "Name", "Price", "Stock", "Status" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(560, 260));
        add(scroll, BorderLayout.CENTER);

        reload();
    }

    /** Loads ALL products from the database. */
    public void reload() {
        fill(productDAO.getAllProducts());
    }

    /** Loads the products that match the search box (all, when it is empty). */
    private void search() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            reload();
        } else {
            fill(productDAO.searchProducts(keyword));
        }
    }

    private void fill(List<Product> products) {
        shown = products;
        model.setRowCount(0);
        for (Product p : products) {
            String status = "OK";
            if (p.getStock() == 0) {
                status = "OUT";
            } else if (p.isLowStock()) {
                status = "LOW";
            }
            model.addRow(new Object[] {
                p.getName(), String.format("%.2f", p.getPrice()), p.getStock(), status });
        }
    }

    /** Returns the selected product, or null when no row is selected. */
    public Product getSelectedProduct() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= shown.size()) {
            return null;
        }
        return shown.get(row);
    }
}