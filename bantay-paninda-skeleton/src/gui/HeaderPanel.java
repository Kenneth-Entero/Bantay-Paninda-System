package gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import util.LogoIcon;
import util.Theme;

/**
 * HeaderPanel - the top bar of every screen: the system logo together with
 * the screen title and subtitle. An optional text on the right side can show
 * the logged in user (for example "Maria - Cashier").
 *
 * Usage:
 *   add(new HeaderPanel("Dashboard", "Choose a task"), BorderLayout.NORTH);
 *   header.setRightText("Maria - Cashier");
 */
public class HeaderPanel extends JPanel {

    private final JLabel titleLabel = new JLabel();
    private final JLabel subtitleLabel = new JLabel();
    private final JLabel rightLabel = new JLabel();
    private Image customLogo = null;

    /** Header with the default system name and no subtitle. */
    public HeaderPanel() {
        this("Bantay Paninda System", "");
    }

    /** Header with the screen's own title and subtitle. */
    public HeaderPanel(String title, String subtitle) {
        this(title, subtitle, null);
    }

    /** Header with a custom logo image; pass {@code null} to use the default BP shield. */
    public HeaderPanel(String title, String subtitle, Image logoImage) {
        setLayout(new BorderLayout());
        setOpaque(false);
        customLogo = logoImage;

        // Center: logo + title/subtitle
        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        center.setOpaque(false);
        center.add(new LogoPanel(customLogo));

        titleLabel.setFont(Theme.FONT_TITLE);
        titleLabel.setForeground(Theme.WHITE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        subtitleLabel.setFont(Theme.FONT_SUBTITLE);
        subtitleLabel.setForeground(Theme.MUTED_TEXT);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.add(Box.createVerticalGlue());
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(8));
        titlePanel.add(subtitleLabel);
        titlePanel.add(Box.createVerticalGlue());
        center.add(titlePanel);

        add(center, BorderLayout.CENTER);

        // East: small text for the logged in user (hidden when empty)
        rightLabel.setFont(Theme.FONT_SUBTITLE);
        rightLabel.setForeground(Theme.MUTED_TEXT);
        rightLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 10));
        rightLabel.setVisible(false);
        add(rightLabel, BorderLayout.EAST);

        setTitle(title);
        setSubtitle(subtitle);
    }

    /** Changes the big title text. */
    public void setTitle(String title) {
        titleLabel.setText(title == null ? "" : title);
    }

    /** Changes the smaller line under the title. */
    public void setSubtitle(String subtitle) {
        subtitleLabel.setText(subtitle == null ? "" : subtitle);
    }

    /** Replaces the header logo with a custom image; pass {@code null} to restore the default BP shield. */
    public void setLogoImage(Image image) {
        customLogo = image;
        repaint();
    }

    /** Shows a right-aligned text (null or empty hides it). */
    public void setRightText(String text) {
        boolean has = text != null && !text.isEmpty();
        rightLabel.setText(has ? text : "");
        rightLabel.setVisible(has);
        revalidate();
        repaint();
    }

    /** Custom logo drawn using Graphics2D (same shield as the splash screen). */
    private static class LogoPanel extends JPanel {

        private final Image customLogo;

        LogoPanel(Image logoImage) {
            setOpaque(false);
            Dimension size = new Dimension(110, 110);
            setPreferredSize(size);
            setMinimumSize(size);
            setMaximumSize(size);
            customLogo = logoImage;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();

            if (customLogo != null) {
                int w = getWidth();
                int h = getHeight();
                int iw = customLogo.getWidth(this);
                int ih = customLogo.getHeight(this);
                if (iw > 0 && ih > 0) {
                    double scale = Math.min(w / (double) iw, h / (double) ih);
                    int dw = (int) (iw * scale);
                    int dh = (int) (ih * scale);
                    g2.drawImage(customLogo, (w - dw) / 2, (h - dh) / 2, dw, dh, this);
                }
            } else {
                LogoIcon.drawLogo(g2, getWidth(), getHeight());
            }

            g2.dispose();
        }
    }
}