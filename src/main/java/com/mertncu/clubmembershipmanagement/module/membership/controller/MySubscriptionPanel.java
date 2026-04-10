package com.mertncu.clubmembershipmanagement.module.membership.controller;

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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Unified "Membership & Payment" panel.
 *
 * Flow:
 *   1. User sees current active subscription (or a "no plan" notice).
 *   2. Available plans are shown as cards below.
 *   3. Clicking "Choose Plan" populates the payment checkout panel (right side).
 *   4. User can optionally enter a coupon and pick a payment method.
 *   5. Only after clicking "Pay Now" does the subscription change and payment record created.
 */
public class MySubscriptionPanel extends JPanel {

    // ── Palette ──────────────────────────────────────────────────────────
    private static final Color BG_DARK      = new Color(0x18181B);
    private static final Color BG_CARD      = new Color(0x1F1F23);
    private static final Color ACCENT       = new Color(0xFF4500);
    private static final Color ACCENT_H     = new Color(0xE03D00);
    private static final Color BORDER_CLR   = new Color(0x27272A);
    private static final Color TEXT_PRIMARY  = new Color(0xF4F4F5);
    private static final Color TEXT_MUTED   = new Color(0x71717A);
    private static final Color FIELD_BG     = new Color(0x27272A);
    private static final Color SUCCESS      = new Color(0x22C55E);
    private static final Color DANGER       = new Color(0xEF4444);
    private static final Color WARNING      = new Color(0xF59E0B);
    // ─────────────────────────────────────────────────────────────────────

    // ── DAOs ─────────────────────────────────────────────────────────────
    private final SubscriptionDAO   subDAO  = new SubscriptionDAO();
    private final MembershipTypeDAO typeDAO = new MembershipTypeDAO();
    private final PaymentDAO        payDAO  = new PaymentDAO();
    private final CouponDAO         cpnDAO  = new CouponDAO();
    // ─────────────────────────────────────────────────────────────────────

    // ── State ─────────────────────────────────────────────────────────────
    private Subscription     activeSub      = null;
    private MembershipType   selectedPlan   = null;   // chosen but NOT yet paid
    private Coupon           appliedCoupon  = null;
    private String           payMethod      = "CREDIT_CARD";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    // ─────────────────────────────────────────────────────────────────────

    // ── Left-side live labels ─────────────────────────────────────────────
    private JLabel planNameLabel;
    private JLabel statusLabel;
    private JLabel startDateLabel;
    private JLabel endDateLabel;
    private JLabel daysLabel;
    private JPanel activePlanBox;
    private JPanel noActivePlanBox;
    private JPanel plansContainer;
    // ─────────────────────────────────────────────────────────────────────

    // ── Right checkout panel components ──────────────────────────────────
    private JPanel  checkoutPanel;
    private JLabel  chkPlanName;
    private JLabel  chkDuration;
    private JLabel  chkOrigPrice;
    private JLabel  chkDiscount;
    private JLabel  chkFinalPrice;
    private JTextField couponField;
    private JLabel     couponStatus;
    private JPanel     methodGroup;
    private JButton    payBtn;
    // ─────────────────────────────────────────────────────────────────────

    public MySubscriptionPanel() {
        setLayout(new BorderLayout());
        setBackground(BG_DARK);
        build();
        loadCurrentSubscription();
        loadAvailablePlans();
        showCheckout(false); // hide checkout until a plan is selected
    }

    // ═════════════════════════════════════════════════════════════════════
    //  BUILD
    // ═════════════════════════════════════════════════════════════════════

    private void build() {
        // Header
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(32, 36, 0, 36));

        JPanel hdr = new JPanel(); hdr.setLayout(new BoxLayout(hdr, BoxLayout.Y_AXIS)); hdr.setOpaque(false);
        JLabel title = new JLabel("Membership & Payment");
        title.setFont(new Font("SansSerif", Font.BOLD, 26)); title.setForeground(TEXT_PRIMARY);
        JLabel sub = new JLabel("Select a plan and complete payment to activate your membership");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13)); sub.setForeground(TEXT_MUTED);
        hdr.add(title); hdr.add(Box.createVerticalStrut(4)); hdr.add(sub);
        topBar.add(hdr, BorderLayout.WEST);
        add(topBar, BorderLayout.NORTH);

        // Two-column body: LEFT = subscription info + plan list | RIGHT = checkout
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(24, 36, 36, 36));

        // ── LEFT ─────────────────────────────────────────────────────────
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        // Current plan card
        activePlanBox = buildCurrentPlanCard();
        activePlanBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(activePlanBox);

        // No-plan notice
        noActivePlanBox = new JPanel(new GridBagLayout());
        noActivePlanBox.setOpaque(false);
        noActivePlanBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        noActivePlanBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        JPanel noSubRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        noSubRow.setOpaque(false);
        JLabel noIcon = new JLabel("⚠  "); noIcon.setFont(new Font("SansSerif", Font.PLAIN, 13)); noIcon.setForeground(ACCENT);
        JLabel noMsg  = new JLabel("No active subscription. Pick a plan and complete payment.");
        noMsg.setFont(new Font("SansSerif", Font.PLAIN, 13)); noMsg.setForeground(TEXT_MUTED);
        noSubRow.add(noIcon); noSubRow.add(noMsg);
        noActivePlanBox.add(noSubRow);
        left.add(noActivePlanBox);

        // Plans section
        left.add(Box.createVerticalStrut(28));
        JLabel plansSec = capLabel("AVAILABLE PLANS");
        plansSec.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(plansSec);
        left.add(Box.createVerticalStrut(12));

        plansContainer = new JPanel();
        plansContainer.setLayout(new BoxLayout(plansContainer, BoxLayout.Y_AXIS));
        plansContainer.setOpaque(false);
        plansContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(plansContainer);

        JScrollPane leftScroll = new JScrollPane(left);
        leftScroll.setBorder(null); leftScroll.setOpaque(false);
        leftScroll.getViewport().setOpaque(false);

        // ── RIGHT ─────────────────────────────────────────────────────────
        checkoutPanel = buildCheckoutPanel();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1;
        gbc.weightx = 0.60; gbc.gridx = 0; gbc.insets = new Insets(0, 0, 0, 16);
        body.add(leftScroll, gbc);
        gbc.weightx = 0.40; gbc.gridx = 1; gbc.insets = new Insets(0, 0, 0, 0);
        body.add(checkoutPanel, gbc);

        add(body, BorderLayout.CENTER);
    }

    // ── Current plan card ─────────────────────────────────────────────────
    private JPanel buildCurrentPlanCard() {
        JPanel card = new JPanel(new GridLayout(1, 2, 0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                // accent top stripe
                g2.setColor(ACCENT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), 4, 4, 4));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT, 1, true),
                new EmptyBorder(26, 28, 26, 28)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        JPanel left = new JPanel(); left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS)); left.setOpaque(false);
        JLabel cur = new JLabel("CURRENT PLAN");
        cur.setFont(new Font("SansSerif", Font.BOLD, 10)); cur.setForeground(ACCENT);
        planNameLabel = new JLabel("—");
        planNameLabel.setFont(new Font("SansSerif", Font.BOLD, 28)); planNameLabel.setForeground(TEXT_PRIMARY);
        statusLabel = infoLbl("Status", "...");
        left.add(cur); left.add(Box.createVerticalStrut(8));
        left.add(planNameLabel); left.add(Box.createVerticalStrut(8)); left.add(statusLabel);

        JPanel right = new JPanel(); right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS)); right.setOpaque(false);
        startDateLabel = infoLbl("Start",     "...");
        endDateLabel   = infoLbl("Expires",   "...");
        daysLabel      = infoLbl("Remaining", "...");
        right.add(startDateLabel); right.add(Box.createVerticalStrut(8));
        right.add(endDateLabel);   right.add(Box.createVerticalStrut(8));
        right.add(daysLabel);

        card.add(left); card.add(right);
        return card;
    }

    // ── Checkout panel ────────────────────────────────────────────────────
    private JPanel buildCheckoutPanel() {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR, 1, true),
                new EmptyBorder(28, 28, 28, 28)));

        // Plan summary at top
        card.add(capLabel("CHECKOUT"));
        card.add(Box.createVerticalStrut(16));

        chkPlanName  = bigLabel("—"); chkPlanName.setForeground(ACCENT);
        chkDuration  = muteLabel("Select a plan to continue");
        card.add(chkPlanName); card.add(Box.createVerticalStrut(4)); card.add(chkDuration);
        card.add(Box.createVerticalStrut(20));

        JSeparator sep1 = new JSeparator();
        sep1.setForeground(BORDER_CLR); sep1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        card.add(sep1); card.add(Box.createVerticalStrut(20));

        // Order rows
        chkOrigPrice  = rowLabel("Plan Price", "₺—");
        chkDiscount   = rowLabel("Coupon Discount", "—");
        chkDiscount.setForeground(SUCCESS);
        card.add(chkOrigPrice); card.add(Box.createVerticalStrut(8)); card.add(chkDiscount);

        JSeparator sep2 = new JSeparator();
        sep2.setForeground(BORDER_CLR); sep2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        card.add(Box.createVerticalStrut(14)); card.add(sep2); card.add(Box.createVerticalStrut(14));

        JLabel totalCap = capLabel("TOTAL");
        totalCap.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(totalCap); card.add(Box.createVerticalStrut(6));
        chkFinalPrice = new JLabel("₺—");
        chkFinalPrice.setFont(new Font("SansSerif", Font.BOLD, 36)); chkFinalPrice.setForeground(TEXT_PRIMARY);
        chkFinalPrice.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(chkFinalPrice); card.add(Box.createVerticalStrut(22));

        // Coupon field
        card.add(capLabel("COUPON CODE (OPTIONAL)")); card.add(Box.createVerticalStrut(8));
        JPanel couponRow = new JPanel(new BorderLayout(8, 0));
        couponRow.setOpaque(false); couponRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        couponField = new JTextField();
        couponField.setBackground(FIELD_BG); couponField.setForeground(TEXT_PRIMARY);
        couponField.setCaretColor(ACCENT); couponField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        couponField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR, 1, true),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        couponField.putClientProperty("JTextField.placeholderText", "e.g. SUMMER20");
        JButton applyBtn = smallBtn("Apply");
        applyBtn.addActionListener(e -> applyCoupon());
        couponRow.add(couponField, BorderLayout.CENTER); couponRow.add(applyBtn, BorderLayout.EAST);
        card.add(couponRow); card.add(Box.createVerticalStrut(6));
        couponStatus = new JLabel(" ");
        couponStatus.setFont(new Font("SansSerif", Font.PLAIN, 11)); couponStatus.setForeground(TEXT_MUTED);
        card.add(couponStatus); card.add(Box.createVerticalStrut(20));

        // Payment method
        card.add(capLabel("PAYMENT METHOD")); card.add(Box.createVerticalStrut(10));
        methodGroup = new JPanel(new GridLayout(1, 3, 8, 0));
        methodGroup.setOpaque(false); methodGroup.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        String[] codes  = {"CREDIT_CARD", "CASH", "BANK_TRANSFER"};
        String[] labels = {"Credit Card",  "Cash",  "Bank"};
        ButtonGroup bg  = new ButtonGroup();
        for (int i = 0; i < codes.length; i++) {
            final String code = codes[i];
            JToggleButton tb  = new JToggleButton(labels[i]);
            if (i == 0) { tb.setSelected(true); payMethod = code; }
            tb.setFont(new Font("SansSerif", Font.BOLD, 11));
            tb.setForeground(i == 0 ? ACCENT : TEXT_PRIMARY);
            tb.setBackground(i == 0 ? new Color(0x2D1200) : FIELD_BG);
            tb.setBorderPainted(false); tb.setFocusPainted(false);
            tb.addActionListener(e -> {
                payMethod = code;
                for (Component c : methodGroup.getComponents()) {
                    if (c instanceof JToggleButton b) {
                        b.setBackground(b.isSelected() ? new Color(0x2D1200) : FIELD_BG);
                        b.setForeground(b.isSelected() ? ACCENT : TEXT_PRIMARY);
                    }
                }
            });
            bg.add(tb); methodGroup.add(tb);
        }
        card.add(methodGroup); card.add(Box.createVerticalGlue()); card.add(Box.createVerticalStrut(24));

        // Pay Now
        payBtn = new JButton("Pay Now  →") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? ACCENT_H : ACCENT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose(); super.paintComponent(g);
            }
        };
        payBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
        payBtn.setForeground(Color.WHITE);
        payBtn.setContentAreaFilled(false); payBtn.setBorderPainted(false);
        payBtn.setFocusPainted(false); payBtn.setOpaque(false);
        payBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        payBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        payBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        payBtn.addActionListener(e -> handlePayment());
        card.add(payBtn);

        return card;
    }

    // ═════════════════════════════════════════════════════════════════════
    //  DATA LOADING
    // ═════════════════════════════════════════════════════════════════════

    private void loadCurrentSubscription() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        Optional<Subscription> opt = subDAO.findActiveSubscriptionByUserId(user.getId());
        if (opt.isPresent()) {
            activeSub = opt.get();
            activePlanBox.setVisible(true);
            noActivePlanBox.setVisible(false);

            typeDAO.findById(activeSub.getMembershipTypeId()).ifPresent(t ->
                    planNameLabel.setText(t.getName()));
            setInfo(statusLabel,    "Status",    "Active",  SUCCESS);
            setInfo(startDateLabel, "Start",     activeSub.getStartDate().format(DATE_FMT), TEXT_PRIMARY);
            setInfo(endDateLabel,   "Expires",   activeSub.getEndDate().format(DATE_FMT),   TEXT_PRIMARY);
            long rem = activeSub.getRemainingDays();
            setInfo(daysLabel, "Remaining", rem + " days", rem <= 7 ? DANGER : rem <= 30 ? WARNING : SUCCESS);
        } else {
            activeSub = null;
            activePlanBox.setVisible(false);
            noActivePlanBox.setVisible(true);
        }
    }

    private void loadAvailablePlans() {
        plansContainer.removeAll();
        List<MembershipType> types = typeDAO.findAll();
        for (MembershipType t : types) {
            plansContainer.add(buildPlanCard(t));
            plansContainer.add(Box.createVerticalStrut(10));
        }
        plansContainer.revalidate(); plansContainer.repaint();
    }

    // ═════════════════════════════════════════════════════════════════════
    //  PLAN CARDS
    // ═════════════════════════════════════════════════════════════════════

    private JPanel buildPlanCard(MembershipType type) {
        boolean isCurrent  = activeSub != null && activeSub.getMembershipTypeId() == type.getId();
        boolean isSelected = selectedPlan != null && selectedPlan.getId() == type.getId();

        Color borderColor = (isCurrent || isSelected) ? ACCENT : BORDER_CLR;

        JPanel card = new JPanel(new BorderLayout(16, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                if (isCurrent || isSelected) {
                    g2.setColor(ACCENT);
                    g2.fill(new RoundRectangle2D.Float(0, 0, 4, getHeight(), 4, 4));
                }
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1, true),
                new EmptyBorder(14, 20, 14, 20)));

        // Left Content (Name, Badge, Duration)
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JPanel nameRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        nameRow.setOpaque(false);
        nameRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nameLbl = new JLabel(type.getName());
        nameLbl.setFont(new Font("SansSerif", Font.BOLD, 15));
        nameLbl.setForeground(TEXT_PRIMARY);
        nameRow.add(nameLbl);

        if (isCurrent) {
            JLabel badge = new JLabel("ACTIVE");
            badge.setFont(new Font("SansSerif", Font.BOLD, 10)); badge.setForeground(SUCCESS);
            nameRow.add(badge);
        } else if (isSelected) {
            JLabel badge = new JLabel("SELECTED");
            badge.setFont(new Font("SansSerif", Font.BOLD, 10)); badge.setForeground(ACCENT);
            nameRow.add(badge);
        }

        left.add(nameRow);
        left.add(Box.createVerticalStrut(4));

        JLabel durLbl = new JLabel(type.getDurationMonths() + (type.getDurationMonths() > 1 ? " months" : " month"));
        durLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        durLbl.setForeground(TEXT_MUTED); 
        durLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(durLbl);

        // Right Content (Price, Button)
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        right.setOpaque(false);

        JLabel priceLbl = new JLabel("₺" + String.format("%.0f", type.getPrice()));
        priceLbl.setFont(new Font("SansSerif", Font.BOLD, 22));
        priceLbl.setForeground((isCurrent || isSelected) ? ACCENT : TEXT_PRIMARY);
        priceLbl.setBorder(new EmptyBorder(2, 0, 0, 0));
        right.add(priceLbl);

        JButton btn;
        if (isCurrent && !isSelected) {
            btn = buildBtn("Renew", new Color(0x052E16), new Color(0x052E16));
            btn.setForeground(SUCCESS);
            btn.addActionListener(e -> planSelected(type));
        } else if (isSelected) {
            btn = buildBtn("Selected  ✓", new Color(0x2D1200), new Color(0x2D1200));
            btn.setForeground(ACCENT);
            btn.setEnabled(false);
        } else {
            btn = buildBtn("Choose Plan", ACCENT, ACCENT_H);
            btn.setForeground(Color.WHITE);
            btn.addActionListener(e -> planSelected(type));
        }
        btn.setPreferredSize(new Dimension(110, 36));
        right.add(btn);

        card.add(left, BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);
        return card;
    }

    // ═════════════════════════════════════════════════════════════════════
    //  EVENT HANDLERS
    // ═════════════════════════════════════════════════════════════════════

    /** Called when user clicks "Choose Plan" or "Renew" on a card. */
    private void planSelected(MembershipType type) {
        selectedPlan  = type;
        appliedCoupon = null;
        couponField.setText("");
        couponStatus.setText(" ");

        // Update checkout header
        chkPlanName.setText(type.getName());
        chkDuration.setText(type.getDurationMonths()
                + (type.getDurationMonths() > 1 ? " months plan" : " month plan"));

        refreshCheckoutSummary();
        showCheckout(true);

        // Rebuild plan cards to reflect selection state
        loadAvailablePlans();
    }

    private void applyCoupon() {
        String code = couponField.getText().trim();
        if (code.isEmpty()) { couponStatus.setText(" "); return; }
        Optional<Coupon> opt = cpnDAO.findByCode(code);
        if (opt.isPresent() && opt.get().isValid()) {
            appliedCoupon = opt.get();
            couponStatus.setForeground(SUCCESS);
            couponStatus.setText("✔ " + appliedCoupon.getDiscountPercent() + "% discount applied!");
        } else {
            appliedCoupon = null;
            couponStatus.setForeground(new Color(0xF87171));
            couponStatus.setText("✘ Invalid or expired coupon code.");
        }
        refreshCheckoutSummary();
    }

    private void handlePayment() {
        if (selectedPlan == null) {
            JOptionPane.showMessageDialog(this, "Please select a plan first.", "No Plan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        double original = selectedPlan.getPrice();
        double discount = appliedCoupon != null ? original * (appliedCoupon.getDiscountPercent() / 100.0) : 0;
        double finalAmt = original - discount;

        // Confirmation dialog
        String msg = "<html><b>Confirm Purchase</b><br><br>"
                + "Plan: <b>" + selectedPlan.getName() + "</b><br>"
                + "Duration: " + selectedPlan.getDurationMonths() + " month(s)<br>"
                + "Amount: <b>₺" + String.format("%.0f", finalAmt) + "</b><br>"
                + "Method: " + payMethod.replace("_", " ") + "<br><br>"
                + "Your membership will be activated immediately.</html>";

        int confirm = JOptionPane.showConfirmDialog(this, msg,
                "Confirm Payment", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (confirm != JOptionPane.OK_OPTION) return;

        // Deactivate existing subscription
        if (activeSub != null) {
            activeSub.setActive(false);
            subDAO.update(activeSub);
        }

        // Create new subscription
        LocalDate start = LocalDate.now();
        LocalDate end   = start.plusMonths(selectedPlan.getDurationMonths());
        Subscription newSub = new Subscription(user.getId(), selectedPlan.getId(), start, end, true);
        subDAO.save(newSub);

        // Record payment
        Integer couponId = appliedCoupon != null ? appliedCoupon.getId() : null;
        Payment payment  = new Payment(user.getId(), newSub.getId(), finalAmt, discount, couponId, payMethod, "COMPLETED");
        payDAO.save(payment);

        JOptionPane.showMessageDialog(this,
                "Payment successful! \"" + selectedPlan.getName() + "\" is now active.",
                "Success ✓", JOptionPane.INFORMATION_MESSAGE);

        // Reset & refresh
        selectedPlan  = null;
        appliedCoupon = null;
        couponField.setText("");
        couponStatus.setText(" ");
        showCheckout(false);
        loadCurrentSubscription();
        loadAvailablePlans();
    }

    // ═════════════════════════════════════════════════════════════════════
    //  UI HELPERS
    // ═════════════════════════════════════════════════════════════════════

    private void showCheckout(boolean visible) {
        checkoutPanel.setVisible(visible);
        if (!visible) {
            // Show a gentle call-to-action placeholder inside the right column
            chkPlanName.setText("—");
            chkDuration.setText("Select a plan to continue");
            chkOrigPrice.setText("Plan Price:  ₺—");
            chkDiscount.setText("Coupon Discount:  —");
            chkFinalPrice.setText("₺—");
        }
        checkoutPanel.setVisible(true); // always visible — content dims until selection
    }

    private void refreshCheckoutSummary() {
        if (selectedPlan == null) return;
        double original = selectedPlan.getPrice();
        double discount = appliedCoupon != null ? original * (appliedCoupon.getDiscountPercent() / 100.0) : 0;
        double finalAmt = original - discount;

        chkOrigPrice.setText("Plan Price:  ₺" + String.format("%.0f", original));
        chkDiscount.setText("Discount:  -₺" + String.format("%.0f", discount));
        chkFinalPrice.setText("₺" + String.format("%.0f", finalAmt));
    }

    private void setInfo(JLabel lbl, String key, String value, Color valueColor) {
        String hex = String.format("#%02X%02X%02X",
                valueColor.getRed(), valueColor.getGreen(), valueColor.getBlue());
        lbl.setText("<html><span style='color:#71717A'>" + key + ":&nbsp;</span>"
                + "<span style='color:" + hex + "'>" + value + "</span></html>");
    }

    private JLabel infoLbl(String key, String value) {
        JLabel lbl = new JLabel();
        setInfo(lbl, key, value, TEXT_MUTED);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        return lbl;
    }

    private JLabel capLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        l.setForeground(new Color(0x52525B));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JLabel bigLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 22));
        l.setForeground(TEXT_PRIMARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JLabel muteLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        l.setForeground(TEXT_MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JLabel rowLabel(String key, String value) {
        JLabel l = new JLabel(key + ":  " + value);
        l.setFont(new Font("SansSerif", Font.PLAIN, 13));
        l.setForeground(TEXT_MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JButton buildBtn(String text, Color bg, Color hover) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? hover : bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setFocusPainted(false); btn.setOpaque(false);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton smallBtn(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? ACCENT_H : ACCENT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setFocusPainted(false); btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(72, 46));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── WrapLayout (responsive card grid) ────────────────────────────────
    static class WrapLayout extends FlowLayout {
        WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }
        @Override public Dimension preferredLayoutSize(Container t) { return layoutSz(t, true); }
        @Override public Dimension minimumLayoutSize(Container t)   { Dimension d = layoutSz(t, false); d.width -= (getHgap()+1); return d; }
        private Dimension layoutSz(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int tw = target.getSize().width; if (tw == 0) tw = Integer.MAX_VALUE;
                int hg = getHgap(), vg = getVgap();
                Insets ins = target.getInsets();
                int maxW = tw - ins.left - ins.right - hg * 2;
                Dimension dim = new Dimension(0, 0);
                int rw = 0, rh = 0;
                for (int i = 0; i < target.getComponentCount(); i++) {
                    Component m = target.getComponent(i);
                    if (m.isVisible()) {
                        Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                        if (rw + d.width > maxW) { dim.width = Math.max(dim.width, rw); dim.height += rh + vg; rw = 0; rh = 0; }
                        rw += d.width + hg; rh = Math.max(rh, d.height);
                    }
                }
                dim.width = Math.max(dim.width, rw); dim.height += rh + vg * 2;
                dim.width += ins.left + ins.right + hg * 2; dim.height += ins.top + ins.bottom;
                return dim;
            }
        }
    }
}
