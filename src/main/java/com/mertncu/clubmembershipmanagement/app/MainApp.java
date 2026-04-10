package com.mertncu.clubmembershipmanagement.app;

import com.formdev.flatlaf.FlatLightLaf;
import com.mertncu.clubmembershipmanagement.common.ui.ViewManager;
import com.mertncu.clubmembershipmanagement.config.DatabaseConnection;
import com.mertncu.clubmembershipmanagement.module.auth.dao.UserDAO;
import com.mertncu.clubmembershipmanagement.module.auth.controller.LoginPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainApp {

    public static void main(String[] args) {
        try {
            
            UIManager.put("Button.arc", 15);
            UIManager.put("Component.arc", 15);
            UIManager.put("TextComponent.arc", 15);
            UIManager.put("Component.focusWidth", 2);
            UIManager.put("ScrollBar.showButtons", true);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new java.awt.Insets(2, 2, 2, 2));

            UIManager.put("defaultFont", new Font("SansSerif", Font.PLAIN, 13));
            UIManager.put("Panel.background", new Color(0x18181B));
            UIManager.put("Label.foreground", new Color(0xB0BEC5));
            UIManager.put("TitlePane.background", new Color(0x18181B));
            UIManager.put("TitlePane.foreground", new Color(0xB0BEC5));
            UIManager.put("TabbedPane.selectedBackground", new Color(0xFF4500));
            UIManager.put("Button.focusedBackground", new Color(0x2C2C2C));
            
            com.formdev.flatlaf.FlatDarkLaf.setup();
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }

        SwingUtilities.invokeLater(() -> {
            DatabaseConnection.getConnection();
            
            new UserDAO();

            JFrame mainFrame = new JFrame("Club Membership Management");
            mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            mainFrame.setSize(2560, 1600    );
            mainFrame.setLocationRelativeTo(null);
            
            mainFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    DatabaseConnection.closeConnection();
                    System.exit(0);
                }
            });

            ViewManager.setMainFrame(mainFrame);

            ViewManager.switchView(new LoginPanel(), "Login - Club Management");
            
            mainFrame.setVisible(true);
        });
    }
}
