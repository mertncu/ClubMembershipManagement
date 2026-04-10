package com.mertncu.clubmembershipmanagement.module.dashboard.controller;

import com.mertncu.clubmembershipmanagement.module.auth.dao.UserDAO;
import com.mertncu.clubmembershipmanagement.module.auth.model.User;
import com.mertncu.clubmembershipmanagement.module.branch.dao.GymBranchDAO;
import com.mertncu.clubmembershipmanagement.module.membership.dao.SubscriptionDAO;
import com.mertncu.clubmembershipmanagement.module.payment.dao.PaymentDAO;
import com.mertncu.clubmembershipmanagement.module.payment.model.Payment;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminDashboardHomePanel extends JPanel {

    private static final Color BG_DARK      = new Color(0x18181B);
    private static final Color BG_CARD      = new Color(0x1F1F23);
    private static final Color ACCENT       = new Color(0xFF4500);
    private static final Color BORDER_CLR   = new Color(0x27272A);
    private static final Color TEXT_PRIMARY  = new Color(0xF4F4F5);
    private static final Color TEXT_MUTED   = new Color(0x71717A);

    private final PaymentDAO      paymentDAO      = new PaymentDAO();
    private final SubscriptionDAO subscriptionDAO = new SubscriptionDAO() ;
    private final GymBranchDAO    branchDAO       = new GymBranchDAO();
    private final UserDAO         userDAO         = new UserDAO();

    public AdminDashboardHomePanel() {
        setLayout(new BorderLayout());
        setBackground(BG_DARK);
        build();
    }

    private void build() {
        // ── Top bar ──────────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(32, 36, 0, 36));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        JLabel title = new JLabel("Admin Overview");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(TEXT_PRIMARY);
        JLabel sub = new JLabel("System health at a glance");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sub.setForeground(TEXT_MUTED);
        titleBlock.add(title); titleBlock.add(Box.createVerticalStrut(4)); titleBlock.add(sub);
        topBar.add(titleBlock, BorderLayout.WEST);

        JLabel dateLbl = new JLabel(DateTimeFormatter.ofPattern("MMMM d, yyyy")
                .format(java.time.LocalDate.now()));
        dateLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        dateLbl.setForeground(TEXT_MUTED);
        JPanel dw = new JPanel(new GridBagLayout()); dw.setOpaque(false); dw.add(dateLbl);
        topBar.add(dw, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // ── Body ─────────────────────────────────────────────────────────
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(24, 36, 36, 36));

        // --- KPI Stats ---
        body.add(sectionLabel("KEY METRICS"));
        body.add(Box.createVerticalStrut(12));

        int totalUsers      = userDAO.findAll().size();
        int activeSubs      = (int) subscriptionDAO.findAll().stream()
                                    .filter(s -> s.isActive()).count();
        double totalRevenue = paymentDAO.getTotalRevenue();
        int totalBranches   = branchDAO.findAll().size();

        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 16, 0));
        kpiRow.setOpaque(false);
        kpiRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        kpiRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        kpiRow.add(kpiCard("Total Members",     String.valueOf(totalUsers),     new Color(0x8B5CF6), "◈"));
        kpiRow.add(kpiCard("Active Subs",        String.valueOf(activeSubs),     new Color(0x22C55E), "◉"));
        kpiRow.add(kpiCard("Total Revenue",      String.format("₺%.0f", totalRevenue), ACCENT, "◆"));
        kpiRow.add(kpiCard("Branches",           String.valueOf(totalBranches),  new Color(0x0EA5E9), "⊞"));
        body.add(kpiRow);
        body.add(Box.createVerticalStrut(32));

        // --- Activity Grids ---
        JPanel gridsRow = new JPanel(new GridLayout(1, 2, 24, 0));
        gridsRow.setOpaque(false);
        gridsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel txBox = new JPanel();
        txBox.setLayout(new BoxLayout(txBox, BoxLayout.Y_AXIS));
        txBox.setOpaque(false);
        txBox.add(sectionLabel("RECENT TRANSACTIONS"));
        txBox.add(Box.createVerticalStrut(12));
        txBox.add(buildTransactionsTable());
        
        JPanel userBox = new JPanel();
        userBox.setLayout(new BoxLayout(userBox, BoxLayout.Y_AXIS));
        userBox.setOpaque(false);
        userBox.add(sectionLabel("LATEST SIGNUPS"));
        userBox.add(Box.createVerticalStrut(12));
        userBox.add(buildNewUsersTable());
        
        gridsRow.add(txBox);
        gridsRow.add(userBox);
        body.add(gridsRow);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel kpiCard(String label, String value, Color accent, String icon) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(accent);
                g2.fill(new RoundRectangle2D.Float(0, 0, 4, getHeight(), 4, 4));
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR, 1, true),
                new EmptyBorder(20, 24, 20, 16)));

        JLabel iconLbl = new JLabel(icon + "  " + label);
        iconLbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        iconLbl.setForeground(TEXT_MUTED);

        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font("SansSerif", Font.BOLD, 28));
        valueLbl.setForeground(TEXT_PRIMARY);

        card.add(iconLbl);
        card.add(Box.createVerticalStrut(12));
        card.add(valueLbl);
        return card;
    }

    private JPanel buildTransactionsTable() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_CARD);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR, 1, true),
                new EmptyBorder(0, 0, 0, 0)));

        // Table header
        String[] cols = {"#", "User ID", "Amount", "Discount", "Method", "Status", "Date"};
        List<Payment> payments = paymentDAO.findAll();

        Object[][] data = new Object[payments.size()][cols.length];
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
        for (int i = 0; i < payments.size(); i++) {
            Payment p = payments.get(i);
            data[i][0] = p.getId();
            data[i][1] = p.getUserId();
            data[i][2] = String.format("₺%.2f", p.getAmount());
            data[i][3] = String.format("₺%.2f", p.getDiscountAmount());
            data[i][4] = p.getPaymentMethod();
            data[i][5] = p.getStatus();
            data[i][6] = p.getPaidAt() != null ? p.getPaidAt().format(fmt) : "—";
        }

        JTable table = new JTable(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_PRIMARY);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(40);
        table.setGridColor(BORDER_CLR);
        table.getTableHeader().setBackground(new Color(0x111113));
        table.getTableHeader().setForeground(TEXT_MUTED);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));
        table.setSelectionBackground(new Color(0x27272A));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setBorder(null);
        table.setIntercellSpacing(new Dimension(0, 0));

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(null);
        sp.setBackground(BG_CARD);
        sp.getViewport().setBackground(BG_CARD);
        sp.setPreferredSize(new Dimension(0, 280));

        wrapper.add(sp, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildNewUsersTable() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_CARD);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR, 1, true),
                new EmptyBorder(0, 0, 0, 0)));

        String[] cols = {"ID", "Name", "Role", "Email"};
        List<User> users = userDAO.findAll();
        // Take last 10 users max
        int size = Math.min(users.size(), 10);
        Object[][] data = new Object[size][cols.length];
        
        for (int i = 0; i < size; i++) {
            User u = users.get(users.size() - 1 - i); // Reverse order for newest first
            data[i][0] = u.getId();
            data[i][1] = u.getName();
            data[i][2] = u.getRole();
            data[i][3] = u.getEmail();
        }

        JTable table = new JTable(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_PRIMARY);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(40);
        table.setGridColor(BORDER_CLR);
        table.getTableHeader().setBackground(new Color(0x111113));
        table.getTableHeader().setForeground(TEXT_MUTED);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));
        table.setSelectionBackground(new Color(0x27272A));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setBorder(null);
        table.setIntercellSpacing(new Dimension(0, 0));

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(null);
        sp.setBackground(BG_CARD);
        sp.getViewport().setBackground(BG_CARD);
        sp.setPreferredSize(new Dimension(0, 280));

        wrapper.add(sp, BorderLayout.CENTER);
        return wrapper;
    }

    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        lbl.setForeground(new Color(0x52525B));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }
}
