package util;

import java.awt.Color;
import java.awt.Font;

/**
 * Theme - shared colors and fonts for all screens.
 * <p>
 * Constants only. No logic here.
 *
 * <h3>Colors</h3>
 * <ul>
 *   <li>NAVY      - background color (#1F3A5F)</li>
 *   <li>DARK_NAVY - darker background variant (#16294A)</li>
 *   <li>GOLD      - accent / button color (#F4B942)</li>
 *   <li>WHITE     - primary text color</li>
 *   <li>MUTED_TEXT- secondary text color</li>
 * </ul>
 *
 * <h3>Fonts</h3>
 * <ul>
 *   <li>FONT_TITLE   - SansSerif bold for screen titles</li>
 *   <li>FONT_SUBTITLE- SansSerif plain for subtitles</li>
 *   <li>FONT_BUTTON  - SansSerif bold for buttons</li>
 * </ul>
 */
public class Theme {

    // ---------- Colors ----------
    public static final Color NAVY = new Color(0x1F3A5F);
    public static final Color DARK_NAVY = new Color(0x16294A);
    public static final Color GOLD = new Color(0xF4B942);
    public static final Color WHITE = Color.WHITE;
    public static final Color MUTED_TEXT = new Color(0xB9C8DA);

    // ---------- Fonts ----------
    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 24);
    public static final Font FONT_SUBTITLE = new Font("SansSerif", Font.PLAIN, 16);
    public static final Font FONT_BUTTON = new Font("SansSerif", Font.BOLD, 16);

    private Theme() {
        // Constants-only class: nobody should create a Theme object.
    }
}

