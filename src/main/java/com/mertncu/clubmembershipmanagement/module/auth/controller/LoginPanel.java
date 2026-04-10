package com.mertncu.clubmembershipmanagement.module.auth.controller;

import com.mertncu.clubmembershipmanagement.common.ui.ViewManager;
import com.mertncu.clubmembershipmanagement.module.auth.model.User;
import com.mertncu.clubmembershipmanagement.module.auth.service.AuthService;
import com.mertncu.clubmembershipmanagement.module.dashboard.controller.DashboardPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class LoginPanel extends JPanel {

    // ── Palette ──────────────────────────────────────────────────────────
    private static final Color BG_DARK      = new Color(0x18181B);
    private static final Color BG_CARD      = new Color(0x1F1F23);
    private static final Color ACCENT       = new Color(0xFF4500);
    private static final Color ACCENT_HOVER = new Color(0xE03D00);
    private static final Color BORDER_CLR   = new Color(0x2E2E33);
    private static final Color TEXT_PRIMARY  = new Color(0xF4F4F5);
    private static final Color TEXT_MUTED   = new Color(0x71717A);
    private static final Color FIELD_BG     = new Color(0x27272A);
    // ─────────────────────────────────────────────────────────────────────

    private JTextField    emailField;
    private JPasswordField passwordField;
    private JLabel        errorLabel;
    private final AuthService authService;

    public LoginPanel() {
        this.authService = new AuthService();
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridLayout(1, 2));

        // ── Left: Branding Panel ─────────────────────────────────────────
        JPanel leftPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient background
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x1A0A00),
                        getWidth(), getHeight(), new Color(0x2C0F00));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Decorative circles
                g2.setColor(new Color(0xFF4500, false));
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.12f));
                g2.fillOval(-80, -80, 350, 350);
                g2.fillOval(getWidth() - 200, getHeight() - 200, 350, 350);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.06f));
                g2.setColor(Color.WHITE);
                g2.fillOval(50, getHeight() / 2, 200, 200);
                g2.dispose();
            }
        };
        leftPanel.setOpaque(false);

        JPanel brand = new JPanel();
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setOpaque(false);

        // Logo
        try {
            java.net.URL logoUrl = getClass().getResource(
                    "/com/mertncu/clubmembershipmanagement/images/logo.png");
            if (logoUrl != null) {
                Image img = new ImageIcon(logoUrl).getImage()
                        .getScaledInstance(350, 220, Image.SCALE_SMOOTH);
                JLabel logo = new JLabel(new ImageIcon(img));
                logo.setAlignmentX(Component.CENTER_ALIGNMENT);
                brand.add(logo);
                brand.add(Box.createVerticalStrut(28));
            }
        } catch (Exception ignored) {}

        JLabel headline = new JLabel("<html><div style='text-align:center;'>"
                + "<span style='font-size:26px;font-weight:bold;color:#FF4500;'>CLUB</span>"
                + "<span style='font-size:26px;font-weight:bold;color:#FFFFFF;'> MEMBERSHIP</span>"
                + "</div></html>", SwingConstants.CENTER);
        headline.setAlignmentX(Component.CENTER_ALIGNMENT);
        brand.add(headline);

        brand.add(Box.createVerticalStrut(16));

        JLabel sub = new JLabel("<html><div style='text-align:center;color:#A1A1AA;font-size:13px;'>"
                + "Manage your fitness journey<br>with precision and ease.</div></html>",
                SwingConstants.CENTER);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        brand.add(sub);

        brand.add(Box.createVerticalStrut(50));

        // Feature bullets
        String[] features = { "✦  Track your membership", "✦  Manage subscriptions", "✦  View training sessions" };
        for (String f : features) {
            JLabel feat = new JLabel(f);
            feat.setFont(new Font("SansSerif", Font.PLAIN, 13));
            feat.setForeground(new Color(0xA1A1AA));
            feat.setAlignmentX(Component.CENTER_ALIGNMENT);
            brand.add(feat);
            brand.add(Box.createVerticalStrut(10));
        }

        leftPanel.add(brand);
        add(leftPanel);

        // ── Right: Form Panel ────────────────────────────────────────────
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(BG_DARK);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(BG_CARD);
        form.setBorder(new EmptyBorder(48, 48, 48, 48));
        form.putClientProperty("FlatLaf.style", "arc: 20");

        // "Sign In" heading
        JLabel title = new JLabel("Sign In");
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(title);
        form.add(Box.createVerticalStrut(6));

        JLabel subtitle = new JLabel("Enter your credentials to continue");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(subtitle);
        form.add(Box.createVerticalStrut(36));

        // Email
        form.add(fieldLabel("Email Address"));
        form.add(Box.createVerticalStrut(6));
        emailField = styledTextField("you@example.com", false);
        form.add(emailField);
        form.add(Box.createVerticalStrut(20));

        // Password
        form.add(fieldLabel("Password"));
        form.add(Box.createVerticalStrut(6));
        passwordField = (JPasswordField) styledTextField("••••••••", true);
        form.add(passwordField);
        form.add(Box.createVerticalStrut(10));

        // Error
        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        errorLabel.setForeground(new Color(0xF87171));
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(errorLabel);
        form.add(Box.createVerticalStrut(24));

        // Login button
        JButton loginBtn = accentButton("Sign In", true);
        loginBtn.addActionListener(e -> handleLogin());
        form.add(loginBtn);
        form.add(Box.createVerticalStrut(16));

        // Divider
        form.add(divider());
        form.add(Box.createVerticalStrut(16));

        // Register link
        JPanel linkRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        linkRow.setOpaque(false);
        linkRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel noAccLbl = new JLabel("Don't have an account?");
        noAccLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        noAccLbl.setForeground(TEXT_MUTED);
        JButton regBtn = linkButton("Create one");
        regBtn.addActionListener(e -> goToRegister());
        linkRow.add(noAccLbl);
        linkRow.add(regBtn);
        form.add(linkRow);

        // Constrain form width
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        rightPanel.add(form, gbc);
        add(rightPanel);
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(new Color(0xA1A1AA));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JTextField styledTextField(String placeholder, boolean isPassword) {
        JTextField field = isPassword ? new JPasswordField(28) : new JTextField(28);
        field.setPreferredSize(new Dimension(360, 44));
        field.setMaximumSize(new Dimension(360, 44));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setBackground(FIELD_BG);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR, 1, true),
                BorderFactory.createEmptyBorder(0, 14, 0, 14)));
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.putClientProperty("JTextField.placeholderText", placeholder);
        field.putClientProperty("JComponent.roundRect", true);
        return field;
    }

    private JButton accentButton(String text, boolean filled) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover() ? ACCENT_HOVER : ACCENT;
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(360, 46));
        btn.setMaximumSize(new Dimension(360, 46));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton linkButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setForeground(ACCENT);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder());
        return btn;
    }

    private JPanel divider() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(360, 20));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
        JSeparator sep1 = new JSeparator(); sep1.setForeground(BORDER_CLR);
        JSeparator sep2 = new JSeparator(); sep2.setForeground(BORDER_CLR);
        JLabel or = new JLabel("or"); or.setFont(new Font("SansSerif", Font.PLAIN, 11));
        or.setForeground(TEXT_MUTED); or.setBorder(new EmptyBorder(0, 8, 0, 8));
        p.add(sep1, g); g.weightx = 0; p.add(or, g); g.weightx = 1; p.add(sep2, g);
        return p;
    }

    // ── Logic ─────────────────────────────────────────────────────────────

    private void handleLogin() {
        String email    = emailField.getText();
        String password = new String(passwordField.getPassword());
        try {
            boolean success = authService.login(email, password);
            if (success) {
                User user = com.mertncu.clubmembershipmanagement.common.session.SessionManager
                        .getInstance().getCurrentUser();
                System.out.println("Login successful. Role: " + (user != null ? user.getRole() : "Unknown"));
                ViewManager.switchView(new DashboardPanel(), "Dashboard - Club Management");
            } else {
                errorLabel.setText("Invalid email or password.");
            }
        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
        }
    }

    private void goToRegister() {
        ViewManager.switchView(new RegisterPanel(), "Register - Club Management");
    }
}
