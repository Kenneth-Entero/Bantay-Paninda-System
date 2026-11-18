package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import util.LogoIcon;
import util.Theme;

/**
 * ModeScreen - choose between Tindahan or Mall business type.
 * <p>
 * Same look as the splash screen: centered, not resizable, navy background.
 */
public class ModeScreen extends JFrame {

    public ModeScreen() {

        // Set up the main window
        setTitle("Bantay Paninda System");
        setIconImage(LogoIcon.createIconImage(64));
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Create the main background panel
        JPanel root = new JPanel(new BorderLayout(0, 20));
        root.setBackground(Theme.NAVY);
        root.setBorder(
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        );

        // Header: logo + title and subtitle
        root.add(new HeaderPanel("Welcome!", "Choose your business type"), BorderLayout.NORTH);

        // Buttons: one for each business type
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 0));
        buttonPanel.setOpaque(false);

        JButton btnMall = modeButton("MALL", "Employee access");
        JButton btnTindahan = modeButton("TINDAHAN", "Store access");

        btnMall.addActionListener(e -> onMall());
        btnTindahan.addActionListener(e -> onTindahan());

        buttonPanel.add(btnMall);
        buttonPanel.add(btnTindahan);

        root.add(buttonPanel, BorderLayout.CENTER);

        setContentPane(root);

        pack();
        setLocationRelativeTo(null); // center on screen
    }

    /**
     * Builds a big gold button whose label sits on top of a smaller hint.
     */
    private JButton modeButton(String label, String hint) {

        JButton btn = new JButton(
                "<html><div style='text-align:center;'>"
                + "<b style='font-size:18px;'>" + label + "</b><br>"
                + "<font size='2'>" + hint + "</font>"
                + "</div></html>"
        );

        btn.setBackground(Theme.GOLD);
        btn.setForeground(Theme.DARK_NAVY);
        btn.setFont(Theme.FONT_BUTTON);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setMargin(new Insets(12, 16, 12, 16));
        btn.setPreferredSize(new Dimension(220, 120));

        return btn;
    }

    /** Tindahan mode: open login screen (PIN only). Session is started inside LoginScreen after a correct PIN. */
    private void onTindahan() {
        dispose();
        new LoginScreen(true).setVisible(true);
    }

    /** Mall mode: open login screen (username + PIN). Session is started inside LoginScreen after a successful login. */
    private void onMall() {
        dispose();
        new LoginScreen(false).setVisible(true);
    }
}