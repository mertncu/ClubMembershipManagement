package com.mertncu.clubmembershipmanagement.module.payment.controller;

import com.mertncu.clubmembershipmanagement.common.session.SessionManager;
import com.mertncu.clubmembershipmanagement.module.auth.model.User;
import com.mertncu.clubmembershipmanagement.module.membership.dao.MembershipTypeDAO;
import com.mertncu.clubmembershipmanagement.module.membership.dao.SubscriptionDAO;
import com.mertncu.clubmembershipmanagement.module.membership.model.MembershipType;
import com.mertncu.clubmembershipmanagement.module.membership.model.Subscription;
import com.mertncu.clubmembershipmanagement.module.payment.dao.CouponDAO;
import com.mertncu.clubmembershipmanagement.module.payment.dao.PaymentDAO;
import com.mertncu.clubmembershipmanagement.module.payment.model.Coupon;
import com.mertncu.clubmembershipmanagement.module.payment.model.Payment;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class PaymentPanel extends JPanel {

    private static final Color BG_DARK      = new Color(0x18181B);
    private static final Color BG_CARD      = new Color(0x1F1F23);
    private static final Color ACCENT       = new Color(0xFF4500);
    private static final Color ACCENT_HOVER = new Color(0xE03D00);
    private static final Color BORDER_CLR   = new Color(0x27272A);
    private static final Color TEXT_PRIMARY  = new Color(0xF4F4F5);
    private static final Color TEXT_MUTED   = new Color(0x71717A);
    private static final Color FIELD_BG     = new Color(0x27272A);
    private static final Color SUCCESS      = new Color(0x22C55E);

    private JComboBox<MembershipType> planCombo;
    private JTextField couponField;
    private JLabel couponStatusLabel;
    private JLabel originalPriceLbl;
    private JLabel discountLbl;
    private JLabel finalPriceLbl;
    private JPanel methodBtnPanel;
    private String selectedMethod = "CREDIT_CARD";
    private Coupon appliedCoupon = null;

    private final MembershipTypeDAO membershipTypeDAO = new MembershipTypeDAO();
    private final CouponDAO         couponDAO         = new CouponDAO();
    private final SubscriptionDAO   subscriptionDAO   = new SubscriptionDAO();
    private final PaymentDAO        paymentDAO        = new PaymentDAO();

    public PaymentPanel() {
        setLayout(new BorderLayout());
        setBackground(BG_DARK);
        buildUI();
    }

    private void buildUI() {
        // Header
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(32, 36, 0, 36));
        JPanel hdr = new JPanel(); hdr.setLayout(new BoxLayout(hdr, BoxLayout.Y_AXIS)); hdr.setOpaque(false);
        JLabel title = new JLabel("Make a Payment");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(TEXT_PRIMARY);
        JLabel sub = new JLabel("Select your plan, apply a coupon, then confirm");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sub.setForeground(TEXT_MUTED);
        hdr.add(title); hdr.add(Box.createVerticalStrut(4)); hdr.add(sub);
        topBar.add(hdr, BorderLayout.WEST);
        add(topBar, BorderLayout.NORTH);

        // Body
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(30, 36, 36, 36));

        JPanel form = buildForm();
        JPanel summary = buildSummary();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1;
        gbc.weightx = 0.55; gbc.gridx = 0; gbc.insets = new Insets(0, 0, 0, 12);
        body.add(form, gbc);
        gbc.weightx = 0.45; gbc.gridx = 1; gbc.insets = new Insets(0, 0, 0, 0);
        body.add(summary, gbc);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildForm() {
        JPanel card = baseCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(28, 28, 28, 28));

        card.add(sectionLabel("SELECT PLAN"));
        card.add(Box.createVerticalStrut(10));

        List<MembershipType> types = membershipTypeDAO.findAll();
        planCombo = new JComboBox<>(types.toArray(new MembershipType[0]));
        planCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof MembershipType t)
                    setText(t.getName() + "  —  " + t.getDurationMonths() + " mo  —  ₺" + String.format("%.0f", t.getPrice()));
                setBackground(isSelected ? new Color(0x3F3F46) : FIELD_BG);
                setForeground(TEXT_PRIMARY);
                setBorder(new EmptyBorder(8, 12, 8, 12));
                return this;
            }
        });
        planCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        planCombo.setBackground(FIELD_BG);
        planCombo.setForeground(TEXT_PRIMARY);
        planCombo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        planCombo.addActionListener(e -> refreshSummary());
        card.add(planCombo);
        card.add(Box.createVerticalStrut(24));

        // Coupon
        card.add(sectionLabel("COUPON CODE (OPTIONAL)"));
        card.add(Box.createVerticalStrut(10));

        JPanel couponRow = new JPanel(new BorderLayout(8, 0));
        couponRow.setOpaque(false);
        couponRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        couponField = new JTextField();
        couponField.setBackground(FIELD_BG);
        couponField.setForeground(TEXT_PRIMARY);
        couponField.setCaretColor(ACCENT);
        couponField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        couponField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR, 1, true),
                BorderFactory.createEmptyBorder(0, 12, 0, 12)));
        couponField.putClientProperty("JTextField.placeholderText", "e.g. SUMMER20");

        JButton applyBtn = smallAccentBtn("Apply");
        applyBtn.addActionListener(e -> applyCoupon());

        couponRow.add(couponField, BorderLayout.CENTER);
        couponRow.add(applyBtn, BorderLayout.EAST);
        card.add(couponRow);
        card.add(Box.createVerticalStrut(6));

        couponStatusLabel = new JLabel(" ");
        couponStatusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        couponStatusLabel.setForeground(TEXT_MUTED);
        card.add(couponStatusLabel);
        card.add(Box.createVerticalStrut(24));

        // Payment method
        card.add(sectionLabel("PAYMENT METHOD"));
        card.add(Box.createVerticalStrut(10));

        methodBtnPanel = new JPanel(new GridLayout(1, 3, 8, 0));
        methodBtnPanel.setOpaque(false);
        methodBtnPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        String[] methods = {"CREDIT_CARD", "CASH", "BANK_TRANSFER"};
        String[] labels  = {"Credit Card",  "Cash",  "Bank Transfer"};
        ButtonGroup methodGroup = new ButtonGroup();
        for (int i = 0; i < methods.length; i++) {
            final String m = methods[i];
            JToggleButton tb = new JToggleButton(labels[i]);
            if (i == 0) tb.setSelected(true);
            tb.setFont(new Font("SansSerif", Font.BOLD, 12));
            tb.setForeground(TEXT_PRIMARY);
            tb.setBackground(i == 0 ? new Color(0x2D1200) : FIELD_BG);
            tb.setBorderPainted(false);
            tb.setFocusPainted(false);
            tb.addActionListener(e -> {
                selectedMethod = m;
                for (Component c : methodBtnPanel.getComponents()) {
                    if (c instanceof JToggleButton b) {
                        b.setBackground(b.isSelected() ? new Color(0x2D1200) : FIELD_BG);
                        b.setForeground(b.isSelected() ? ACCENT : TEXT_PRIMARY);
                    }
                }
            });
            methodGroup.add(tb);
            methodBtnPanel.add(tb);
        }
        card.add(methodBtnPanel);

        return card;
    }

    private JPanel buildSummary() {
        JPanel card = baseCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(28, 28, 28, 28));

        card.add(sectionLabel("ORDER SUMMARY"));
        card.add(Box.createVerticalStrut(20));

        originalPriceLbl = summaryRow("Plan Price", "₺—");
        discountLbl      = summaryRow("Coupon Discount", "—");
        discountLbl.setForeground(SUCCESS);
        finalPriceLbl    = new JLabel("₺—");
        finalPriceLbl.setFont(new Font("SansSerif", Font.BOLD, 32));
        finalPriceLbl.setForeground(TEXT_PRIMARY);

        card.add(originalPriceLbl);
        card.add(Box.createVerticalStrut(10));
        card.add(discountLbl);
        card.add(Box.createVerticalStrut(16));

        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_CLR);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        card.add(sep);
        card.add(Box.createVerticalStrut(16));

        JLabel totalLbl = new JLabel("TOTAL");
        totalLbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        totalLbl.setForeground(TEXT_MUTED);
        card.add(totalLbl);
        card.add(Box.createVerticalStrut(6));
        card.add(finalPriceLbl);
        card.add(Box.createVerticalGlue());
        card.add(Box.createVerticalStrut(28));

        JButton payBtn = new JButton("Pay Now") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? ACCENT_HOVER : ACCENT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        payBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
        payBtn.setForeground(Color.WHITE);
        payBtn.setContentAreaFilled(false);
        payBtn.setBorderPainted(false);
        payBtn.setFocusPainted(false);
        payBtn.setOpaque(false);
        payBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        payBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        payBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        payBtn.addActionListener(e -> handlePayment());
        card.add(payBtn);

        refreshSummary();
        return card;
    }

    private void refreshSummary() {
        MembershipType selected = (MembershipType) planCombo.getSelectedItem();
        if (selected == null) return;
        double original = selected.getPrice();
        double discount = appliedCoupon != null ? original * (appliedCoupon.getDiscountPercent() / 100.0) : 0;
        double finalP   = original - discount;

        originalPriceLbl.setText("Plan Price:  ₺" + String.format("%.0f", original));
        discountLbl.setText("Discount:  -₺" + String.format("%.0f", discount));
        finalPriceLbl.setText("₺" + String.format("%.0f", finalP));
    }

    private void applyCoupon() {
        String code = couponField.getText().trim();
        if (code.isEmpty()) return;
        Optional<Coupon> opt = couponDAO.findByCode(code);
        if (opt.isPresent() && opt.get().isValid()) {
            appliedCoupon = opt.get();
            couponStatusLabel.setForeground(SUCCESS);
            couponStatusLabel.setText("✔ " + appliedCoupon.getDiscountPercent() + "% discount applied!");
        } else {
            appliedCoupon = null;
            couponStatusLabel.setForeground(new Color(0xF87171));
            couponStatusLabel.setText("✘ Invalid or expired coupon code.");
        }
        refreshSummary();
    }

    private void handlePayment() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        MembershipType type = (MembershipType) planCombo.getSelectedItem();
        if (type == null) return;

        double original = type.getPrice();
        double discount = appliedCoupon != null ? original * (appliedCoupon.getDiscountPercent() / 100.0) : 0;
        double finalAmt = original - discount;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Confirm payment of ₺" + String.format("%.0f", finalAmt)
                + " for \"" + type.getName() + "\" via " + selectedMethod + "?",
                "Confirm Payment", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.OK_OPTION) return;

        // Create subscription
        LocalDate start = LocalDate.now();
        LocalDate end   = start.plusMonths(type.getDurationMonths());
        Subscription sub = new Subscription(user.getId(), type.getId(), start, end, true);
        subscriptionDAO.save(sub);

        // Record payment
        Integer couponId = appliedCoupon != null ? appliedCoupon.getId() : null;
        Payment payment  = new Payment(user.getId(), sub.getId(), finalAmt, discount, couponId, selectedMethod, "COMPLETED");
        paymentDAO.save(payment);

        JOptionPane.showMessageDialog(this,
                "Payment successful! Your " + type.getName() + " plan is now active.",
                "Success", JOptionPane.INFORMATION_MESSAGE);

        // Reset
        appliedCoupon = null;
        couponField.setText("");
        couponStatusLabel.setText(" ");
        refreshSummary();
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private JPanel baseCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createLineBorder(BORDER_CLR, 1, true));
        return card;
    }

    private JLabel sectionLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        l.setForeground(new Color(0x52525B));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JLabel summaryRow(String label, String value) {
        JLabel l = new JLabel(label + ":  " + value);
        l.setFont(new Font("SansSerif", Font.PLAIN, 14));
        l.setForeground(TEXT_MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JButton smallAccentBtn(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? ACCENT_HOVER : ACCENT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(80, 46));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
