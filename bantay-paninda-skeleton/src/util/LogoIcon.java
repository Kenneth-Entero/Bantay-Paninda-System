package util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

/**
 * LogoIcon - the single shared source for drawing the Bantay Paninda shield
 * logo.
 * <p>
 * The logo is drawn with Graphics2D onto a 220x220 logical canvas and scaled
 * to fit the target size, so the window title-bar icon, the header logo and
 * the splash-screen logo are always identical. All methods are static.
 */
public final class LogoIcon {

    /** Logical canvas size (in pixels) used by the original drawing code. */
    private static final int CANVAS = 220;

    private LogoIcon() {
        // Utility class: nobody should create a LogoIcon object.
    }

    /**
     * Draws the BP shield logo into a new {@code size x size} BufferedImage
     * and returns it. Use this for window title-bar icons, for example
     * {@code setIconImage(LogoIcon.createIconImage(64))}.
     */
    public static BufferedImage createIconImage(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        try {
            drawLogo(g2, size, size);
        } finally {
            g2.dispose();
        }
        return image;
    }

    /**
     * Draws the BP shield logo, scaled and centered to fit the given width
     * and height, onto the supplied {@link Graphics2D}.
     */
    public static void drawLogo(Graphics2D g2, int width, int height) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        // Composite is intentionally left to the caller (for example
        // SplashScreen's fade-in alpha). The default is SRC_OVER fully opaque,
        // which is what HeaderPanel and window icons need.

        double scale = Math.min(width / (double) CANVAS, height / (double) CANVAS);
        g2.translate((width - CANVAS * scale) / 2, (height - CANVAS * scale) / 2);
        g2.scale(scale, scale);

        // Outer shield
        Path2D shield = new Path2D.Double();
        shield.moveTo(110, 8);
        shield.lineTo(190, 38);
        shield.lineTo(184, 125);
        shield.curveTo(180, 166, 140, 195, 110, 210);
        shield.curveTo(80, 195, 40, 166, 36, 125);
        shield.lineTo(30, 38);
        shield.closePath();
        g2.setColor(Theme.GOLD);
        g2.fill(shield);

        // Inner shield
        Path2D innerShield = new Path2D.Double();
        innerShield.moveTo(110, 19);
        innerShield.lineTo(179, 44);
        innerShield.lineTo(173, 122);
        innerShield.curveTo(169, 158, 135, 184, 110, 198);
        innerShield.curveTo(85, 184, 51, 158, 47, 122);
        innerShield.lineTo(41, 44);
        innerShield.closePath();
        g2.setColor(Theme.NAVY);
        g2.fill(innerShield);

        // Store canopy
        int canopyX = 57;
        int canopyY = 45;
        g2.setColor(Color.WHITE);
        g2.fillRect(canopyX, canopyY, 106, 13);

        // Alternating stripes
        for (int i = 0; i < 6; i++) {
            int x = canopyX + (i * 18);
            g2.setColor(i % 2 == 0 ? Theme.GOLD : Color.WHITE);
            int[] xs = { x, x + 18, x + 18, x + 9, x };
            int[] ys = { canopyY, canopyY, canopyY + 23, canopyY + 28, canopyY + 23 };
            g2.fillPolygon(new Polygon(xs, ys, 5));
        }

        // BP letters
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 66));
        FontMetrics fm = g2.getFontMetrics();
        String text = "BP";
        g2.drawString(text, (CANVAS - fm.stringWidth(text)) / 2, 133);

        // Shopping cart
        g2.setColor(Theme.GOLD);
        g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(75, 153, 84, 153);
        g2.drawLine(84, 153, 91, 176);
        g2.drawLine(91, 176, 145, 176);
        g2.drawLine(145, 176, 153, 157);
        g2.drawLine(87, 158, 150, 158);
        g2.fillOval(95, 180, 9, 9);
        g2.fillOval(136, 180, 9, 9);
    }
}
