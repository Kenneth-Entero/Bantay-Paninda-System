package gui;

import dao.UserDAO;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import model.User;
import util.LogoIcon;
import util.PinUtil;
import util.Session;
import util.Theme;

/**
 * LoginScreen - PIN keypad screen (Lesson 13: JFrame, getContentPane,
 * GridLayout, JLabel, JTextField, addActionListener).
 *
 * Two modes:
 *   - Mall mode    : choose a role (dropdown) + 4-digit PIN.
 *   - Tindahan mode: PIN only (the fixed account "tindahan" is used).
 */
public class LoginScreen extends JFrame {

    // The Tindahan has no username box, so this fixed account is used.
    private static final String TINDAHAN_USERNAME = "tindahan";

    private final boolean tindahanMode;

    // Role names shown in the dropdown, and the account username of each role.
    // Index 0 is only the placeholder.
    private static final String[] ROLE_NAMES = { "Select role", "Manager", "Inventory", "Cashier" };
    private static final String[] ROLE_USERNAMES = { null, "manager", "inventory", "cashier" };

    // Dropdown for the role (Mall mode only)
    private JComboBox<String> cboRole;

    // Lesson 13: JTextField (a read-only PIN display)
    private JTextField txtPinDisplay;

    // The real PIN digits typed so far (the display only shows asterisks).
    private final StringBuilder pin = new StringBuilder();

    public LoginScreen(boolean tindahanMode) {
        this.tindahanMode = tindahanMode;

        setTitle("Bantay Paninda System");
        setIconImage(LogoIcon.createIconImage(64));   // logo in the window title bar
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Lesson 13: get the content pane of the JFrame
        Container pane = getContentPane();
        pane.setLayout(new BorderLayout(0, 10));
        pane.setBackground(Theme.NAVY);

        // ---------- TOP: logo + title ----------
        HeaderPanel header;
        if (tindahanMode) {
            header = new HeaderPanel("Store Access", "Enter your PIN");
        } else {
            header = new HeaderPanel("Employee Login", "Choose your role and enter your PIN");
        }
        pane.add(header, BorderLayout.NORTH);

        // ---------- CENTER: form + keypad ----------
        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));
        center.add(buildForm(), BorderLayout.NORTH);
        center.add(buildKeypad(), BorderLayout.CENTER);
        pane.add(center, BorderLayout.CENTER);

        // ---------- BOTTOM: back button ----------
        JButton btnBack = new JButton("Back");
        btnBack.setFocusPainted(false);
        btnBack.addActionListener(e -> goBack());
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        bottom.add(btnBack);
        pane.add(bottom, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    // ==========================================================
    // FORM: labels + text fields in a GridLayout
    // ==========================================================
    private JPanel buildForm() {
        int rows = tindahanMode ? 2 : 4;
        JPanel form = new JPanel(new GridLayout(rows, 1, 0, 4));   // Lesson 13: GridLayout
        form.setOpaque(false);

        if (!tindahanMode) {
            JLabel lblRole = new JLabel("Role");                     // Lesson 13: JLabel
            lblRole.setForeground(Theme.WHITE);
            lblRole.setFont(Theme.FONT_SUBTITLE);
            cboRole = new JComboBox<>(ROLE_NAMES);                   // dropdown (like the Category box)
            cboRole.setFont(Theme.FONT_SUBTITLE);
            form.add(lblRole);
            form.add(cboRole);
        }

        JLabel lblPin = new JLabel("PIN");
        lblPin.setForeground(Theme.WHITE);
        lblPin.setFont(Theme.FONT_SUBTITLE);

        txtPinDisplay = new JTextField(15);
        txtPinDisplay.setEditable(false);                            // Lesson 13: setEditable(false)
        txtPinDisplay.setHorizontalAlignment(JTextField.CENTER);
        txtPinDisplay.setFont(Theme.FONT_TITLE);

        form.add(lblPin);
        form.add(txtPinDisplay);
        return form;
    }

    // ==========================================================
    // KEYPAD: 4 rows x 3 columns (1-9, Clear, 0, Enter)
    // ==========================================================
    private JPanel buildKeypad() {
        JPanel keypad = new JPanel(new GridLayout(4, 3, 8, 8));      // Lesson 13: GridLayout(4,3)
        keypad.setOpaque(false);

        String[] labels = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "Clear", "0", "Enter" };
        for (String label : labels) {
            JButton btn = new JButton(label);
            btn.setFont(Theme.FONT_BUTTON);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setOpaque(true);
            btn.setBackground(label.equals("Enter") ? Theme.GOLD : Theme.MUTED_TEXT);
            btn.setForeground(Theme.DARK_NAVY);

            // Lesson 13: addActionListener
            btn.addActionListener(e -> onKey(label));
            keypad.add(btn);
        }
        return keypad;
    }

    // ==========================================================
    // KEY PRESSES
    // ==========================================================
    private void onKey(String label) {
        if (label.equals("Clear")) {
            clearPin();
        } else if (label.equals("Enter")) {
            attemptLogin();
        } else if (pin.length() < PinUtil.PIN_LENGTH) {
            pin.append(label);                  // a digit 0-9
            refreshDisplay();
        }
    }

    private void refreshDisplay() {
        // Show one asterisk per typed digit, never the real PIN.
        txtPinDisplay.setText("*".repeat(pin.length()));
    }

    private void clearPin() {
        pin.setLength(0);
        refreshDisplay();
    }

    // ==========================================================
    // LOGIN
    // ==========================================================
    private void attemptLogin() {
        String username;
        if (tindahanMode) {
            username = TINDAHAN_USERNAME;
        } else {
            username = ROLE_USERNAMES[cboRole.getSelectedIndex()];
            if (username == null) {
                JOptionPane.showMessageDialog(this, "Please choose your role first.");
                return;
            }
        }
        if (!PinUtil.isValidPin(pin.toString())) {
            JOptionPane.showMessageDialog(this, "The PIN must be exactly 4 digits.");
            return;
        }

        // All database work stays in the DAO (no SQL in the GUI).
        User user = new UserDAO().login(username, pin.toString());

        if (user == null) {
            // Do not say which part was wrong.
            String msg = tindahanMode ? "Wrong PIN." : "Wrong PIN.";
            JOptionPane.showMessageDialog(this, msg, "Login failed", JOptionPane.ERROR_MESSAGE);
            clearPin();
            return;
        }

        if (tindahanMode) {
            Session.startTindahan();            // no user is kept in Tindahan mode
        } else {
            Session.startMall(user);
        }

        dispose();
        new DashboardScreen().setVisible(true);
    }

    private void goBack() {
        dispose();
        new ModeScreen().setVisible(true);
    }
}