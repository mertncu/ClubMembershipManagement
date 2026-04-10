package com.mertncu.clubmembershipmanagement.module.auth.controller;

import com.mertncu.clubmembershipmanagement.common.enums.UserRole;
import com.mertncu.clubmembershipmanagement.common.ui.ViewManager;
import com.mertncu.clubmembershipmanagement.module.auth.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class RegisterPanel extends JPanel {

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

    private JTextField     nameField;
    private JTextField     emailField;
    private JPasswordField passwordField;
    private JComboBox<UserRole> roleComboBox;
    private JLabel         errorLabel;
    private final AuthService authService;

    public RegisterPanel() {
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
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x1A0A00),
                        getWidth(), getHeight(), new Color(0x2C0F00));
                g2.setPaint(gp); g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(0xFF, 0x45, 0x00));
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.12f));
                g2.fillOval(-80, -80, 350, 350);
                g2.fillOval(getWidth() - 200, getHeight() - 200, 350, 350);
                g2.dispose();
            }
        };
        leftPanel.setOpaque(false);

        JPanel brand = new JPanel();
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setOpaque(false);

        try {
            java.net.URL logoUrl = getClass().getResource(
                    "/com/mertncu/clubmembershipmanagement/images/logo.png");
            if (logoUrl != null) {
                Image img = new ImageIcon(logoUrl).getImage()
                        .getScaledInstance(90, 90, Image.SCALE_SMOOTH);
                JLabel logo = new JLabel(new ImageIcon(img));
                logo.setAlignmentX(Component.CENTER_ALIGNMENT);
                brand.add(logo);
                brand.add(Box.createVerticalStrut(28));
            }
        } catch (Exception ignored) {}

        JLabel headline = new JLabel("<html><div style='text-align:center;'>"
                + "<span style='font-size:26px;font-weight:bold;color:#FF4500;'>JOIN</span>"
                + "<span style='font-size:26px;font-weight:bold;color:#FFFFFF;'> THE CLUB</span>"
                + "</div></html>", SwingConstants.CENTER);
        headline.setAlignmentX(Component.CENTER_ALIGNMENT);
        brand.add(headline);
        brand.add(Box.createVerticalStrut(16));

        JLabel sub = new JLabel("<html><div style='text-align:center;color:#A1A1AA;font-size:13px;'>"
                + "Start your membership today<br>and unlock all features.</div></html>",
                SwingConstants.CENTER);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        brand.add(sub);
        brand.add(Box.createVerticalStrut(50));

        String[] perks = { "✦  Unlimited access", "✦  Flexible plans", "✦  Expert trainers" };
        for (String p : perks) {
            JLabel perk = new JLabel(p);
            perk.setFont(new Font("SansSerif", Font.PLAIN, 13));
            perk.setForeground(new Color(0xA1A1AA));
            perk.setAlignmentX(Component.CENTER_ALIGNMENT);
            brand.add(perk);
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
        form.setBorder(new EmptyBorder(40, 48, 40, 48));
        form.putClientProperty("FlatLaf.style", "arc: 20");

        JLabel title = new JLabel("Create Account");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(title);
        form.add(Box.createVerticalStrut(6));

        JLabel subtitle = new JLabel("Fill in your details to get started");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(subtitle);
        form.add(Box.createVerticalStrut(30));

        form.add(fieldLabel("Full Name"));
        form.add(Box.createVerticalStrut(6));
        nameField = styledTextField("Enter your full name", false);
        form.add(nameField);
        form.add(Box.createVerticalStrut(16));

        form.add(fieldLabel("Email Address"));
        form.add(Box.createVerticalStrut(6));
        emailField = styledTextField("you@example.com", false);
        form.add(emailField);
        form.add(Box.createVerticalStrut(16));

        form.add(fieldLabel("Password"));
        form.add(Box.createVerticalStrut(6));
        passwordField = (JPasswordField) styledTextField("Create a password", true);
        form.add(passwordField);
        form.add(Box.createVerticalStrut(16));

        form.add(fieldLabel("Role"));
        form.add(Box.createVerticalStrut(6));
        roleComboBox = new JComboBox<>(UserRole.values());
        roleComboBox.setSelectedItem(UserRole.MEMBER);
        roleComboBox.setMaximumSize(new Dimension(360, 44));
        roleComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        roleComboBox.setBackground(FIELD_BG);
        roleComboBox.setForeground(TEXT_PRIMARY);
        roleComboBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
        form.add(roleComboBox);
        form.add(Box.createVerticalStrut(10));

        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        errorLabel.setForeground(new Color(0xF87171));
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(errorLabel);
        form.add(Box.createVerticalStrut(20));

        JButton registerBtn = accentButton("Create Account");
        registerBtn.addActionListener(e -> handleRegister());
        form.add(registerBtn);
        form.add(Box.createVerticalStrut(16));

        JPanel linkRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        linkRow.setOpaque(false);
        linkRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel hasAcc = new JLabel("Already have an account?");
        hasAcc.setFont(new Font("SansSerif", Font.PLAIN, 13));
        hasAcc.setForeground(TEXT_MUTED);
        JButton loginBtn = linkButton("Sign in");
        loginBtn.addActionListener(e -> goToLogin());
        linkRow.add(hasAcc);
        linkRow.add(loginBtn);
        form.add(linkRow);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.CENTER;
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

    private JButton accentButton(String text) {
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

    // ── Logic ─────────────────────────────────────────────────────────────

    private void handleRegister() {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());
        UserRole role = (UserRole) roleComboBox.getSelectedItem();
        try {
            authService.register(name, email, password, role);
            ViewManager.switchView(new LoginPanel(), "Login - Club Management");
        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
        }
    }

    private void goToLogin() {
        ViewManager.switchView(new LoginPanel(), "Login - Club Management");
    }
}
