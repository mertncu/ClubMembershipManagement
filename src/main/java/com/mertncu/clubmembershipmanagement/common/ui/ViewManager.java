package com.mertncu.clubmembershipmanagement.common.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Utility to manage view switching globally across the application for Swing.
 */
public class ViewManager {
    private static JFrame mainFrame;

    public static void setMainFrame(JFrame frame) {
        mainFrame = frame;
    }

    public static JFrame getMainFrame() {
        return mainFrame;
    }

    /**
     * Replaces the current content of the main frame with a new JPanel.
     * @param panel The JPanel to display
     * @param title The title for the window
     */
    public static void switchView(JPanel panel, String title) {
        if (mainFrame == null) {
            throw new IllegalStateException("Main Frame is not set. Call setMainFrame first.");
        }

        if (title != null) {
            mainFrame.setTitle(title);
        }

        mainFrame.setContentPane(panel);
        mainFrame.revalidate();
        mainFrame.repaint();
    }
}
