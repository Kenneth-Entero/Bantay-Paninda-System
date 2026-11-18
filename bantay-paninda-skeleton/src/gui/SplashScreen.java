package gui;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import util.LogoIcon;

public class SplashScreen extends JFrame {

    // Colors used in the splash screen
    private static final Color BG = new Color(0x1F3A5F);
    private static final Color ACCENT = new Color(0xF4B942);
    private static final Color DARK_BLUE = new Color(0x16294A);

    private final LogoPanel logoPanel = new LogoPanel();
    private final JProgressBar progressBar = new JProgressBar(0, 100);

    private final JLabel statusLabel =
            new JLabel("Starting...", SwingConstants.CENTER);

    private final JButton btnStart = new JButton("Start");

    public SplashScreen() {

        // Set up the main window
        setTitle("Bantay Paninda System");
        setIconImage(createWindowIcon());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(520, 580);
        setLocationRelativeTo(null);
        setResizable(false);

        // Create the main background panel
        JPanel root = new JPanel(new BorderLayout(0, 20));
        root.setBackground(BG);
        root.setBorder(
                BorderFactory.createEmptyBorder(35, 30, 35, 30)
        );

        // Create the header with the logo and system title
        JPanel header = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 15, 0)
        );

        header.setOpaque(false);
        header.setPreferredSize(new Dimension(440, 120));

        header.add(logoPanel);

        // Display the system title and subtitle
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(
                new BoxLayout(titlePanel, BoxLayout.Y_AXIS)
        );

        titlePanel.setOpaque(false);

        JLabel title = new JLabel("BANTAY PANINDA SYSTEM");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tagline = new JLabel("Inventory and Sales System");
        tagline.setFont(new Font("SansSerif", Font.PLAIN, 14));
        tagline.setForeground(ACCENT);
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);

        titlePanel.add(Box.createVerticalGlue());
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(8));
        titlePanel.add(tagline);
        titlePanel.add(Box.createVerticalGlue());

        header.add(titlePanel);
        root.add(header, BorderLayout.NORTH);

        // Set up the loading section
        JPanel loadingPanel = new JPanel();

        loadingPanel.setLayout(
                new BoxLayout(loadingPanel, BoxLayout.Y_AXIS)
        );

        loadingPanel.setOpaque(false);

        statusLabel.setFont(
                new Font("SansSerif", Font.ITALIC, 14)
        );

        statusLabel.setForeground(Color.WHITE);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Customize the progress bar
        progressBar.setForeground(ACCENT);
        progressBar.setBackground(DARK_BLUE);
        progressBar.setBorderPainted(false);
        progressBar.setPreferredSize(new Dimension(400, 28));

        progressBar.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 28)
        );

        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Configure the Start button
        btnStart.setEnabled(false);

        btnStart.setFont(
                new Font("SansSerif", Font.BOLD, 14)
        );

        btnStart.addActionListener(e -> {
            JOptionPane.showMessageDialog(
                    this,
                    "Susunod: Tindahan o Mall screen"
            );
        });

        JPanel buttonRow = new JPanel(
                new FlowLayout(FlowLayout.CENTER)
        );

        buttonRow.setOpaque(false);
        buttonRow.add(btnStart);

        // Arrange the loading components
        loadingPanel.add(Box.createVerticalGlue());
        loadingPanel.add(statusLabel);
        loadingPanel.add(Box.createVerticalStrut(22));
        loadingPanel.add(progressBar);
        loadingPanel.add(Box.createVerticalStrut(25));
        loadingPanel.add(buttonRow);
        loadingPanel.add(Box.createVerticalGlue());

        root.add(loadingPanel, BorderLayout.CENTER);

        setContentPane(root);

        // Start the logo and loading animations
        startFadeIn();
        startLoading();
    }

    // Create the custom BP icon for the window title bar using the shared logo.
    private BufferedImage createWindowIcon() {
        return LogoIcon.createIconImage(64);
    }

    // Animate the logo with a fade-in effect
    private void startFadeIn() {

        Timer fade = new Timer(30, null);

        fade.addActionListener(e -> {

            logoPanel.setAlpha(
                    logoPanel.getAlpha() + 0.04f
            );

            if (logoPanel.getAlpha() >= 1f) {
                fade.stop();
            }
        });

        fade.start();
    }

    // Update the progress bar and loading messages
    private void startLoading() {

        Timer load = new Timer(40, null);

        load.addActionListener(e -> {

            int value = progressBar.getValue() + 1;

            progressBar.setValue(value);

            if (value < 35) {

                statusLabel.setText(
                        "Connecting to database..."
                );

            } else if (value < 75) {

                statusLabel.setText(
                        "Loading products..."
                );

            } else if (value < 100) {

                statusLabel.setText(
                        "Almost ready..."
                );

            } else {

                statusLabel.setText("Ready!");
                btnStart.setEnabled(true);

                load.stop();

                // Loading complete: close the splash and open the mode screen.
                dispose();
                new ModeScreen().setVisible(true);
            }
        });

        load.start();
    }

    // Custom logo drawn using Graphics2D
    static class LogoPanel extends JPanel {

        private float alpha = 0f;

        LogoPanel() {

            setOpaque(false);

            setPreferredSize(new Dimension(110, 110));
            setMinimumSize(new Dimension(110, 110));
            setMaximumSize(new Dimension(110, 110));
        }

        float getAlpha() {
            return alpha;
        }

        void setAlpha(float alpha) {

            this.alpha = Math.min(
                    1f,
                    Math.max(0f, alpha)
            );

            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();

            // Apply the fade-in effect
            g2.setComposite(
                    AlphaComposite.getInstance(
                            AlphaComposite.SRC_OVER,
                            alpha
                    )
            );

            LogoIcon.drawLogo(g2, getWidth(), getHeight());

            g2.dispose();
        }
    }
}