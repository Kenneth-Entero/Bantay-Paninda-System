package main;

import gui.SplashScreen;
import javax.swing.SwingUtilities;

/**
 * Main - entry point of the Bantay Paninda System.
 * Runs the splash screen on the Swing event thread.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SplashScreen().setVisible(true));
    }
}

