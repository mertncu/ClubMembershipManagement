package com.mertncu.clubmembershipmanagement.module.membership.controller;

import com.mertncu.clubmembershipmanagement.module.auth.dao.UserDAO;
import com.mertncu.clubmembershipmanagement.module.auth.model.User;
import com.mertncu.clubmembershipmanagement.module.membership.dao.MembershipTypeDAO;
import com.mertncu.clubmembershipmanagement.module.membership.dao.SubscriptionDAO;
import com.mertncu.clubmembershipmanagement.module.membership.model.MembershipType;
import com.mertncu.clubmembershipmanagement.module.membership.model.Subscription;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.util.List;

/**
 * Admin panel for managing membership plan types and active subscriptions.
 */
public class ManageMembershipsPanel extends JPanel {

    private static final Color BG_DARK     = new Color(0x18181B);
    private static final Color BG_CARD     = new Color(0x1F1F23);
    private static final Color ACCENT      = new Color(0xFF4500);
    private static final Color ACCENT_H    = new Color(0xE03D00);
    private static final Color BORDER_CLR  = new Color(0x27272A);
    private static final Color TEXT_PRIMARY = new Color(0xF4F4F5);
    private static final Color TEXT_MUTED  = new Color(0x71717A);
    private static final Color FIELD_BG    = new Color(0x27272A);
    private static final Color SUCCESS     = new Color(0x22C55E);
    private static final Color DANGER      = new Color(0xEF4444);

    private final MembershipTypeDAO typeDAO = new MembershipTypeDAO();
    private final SubscriptionDAO   subDAO  = new SubscriptionDAO();
    private final UserDAO           userDAO = new UserDAO();

    private DefaultTableModel plansModel;
    private DefaultTableModel subsModel;
    private JTable plansTable;
    private JTable subsTable;
    private List<MembershipType> types;
    private List<Subscription>   subs;

    public ManageMembershipsPanel() {
        setLayout(new BorderLayout());
        setBackground(BG_DARK);
        buildUI();
    }

    private void buildUI() {
        // ── Top Bar ──────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(32, 36, 24, 36));

        JPanel hdr = new JPanel();
        hdr.setLayout(new BoxLayout(hdr, BoxLayout.Y_AXIS));
        hdr.setOpaque(false);
        JLabel title = new JLabel("Membership Management");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(TEXT_PRIMARY);
        JLabel sub = new JLabel("Manage subscription plans and active member subscriptions");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sub.setForeground(TEXT_MUTED);
        hdr.add(title);
        hdr.add(Box.createVerticalStrut(4));
        hdr.add(sub);
        topBar.add(hdr, BorderLayout.WEST);

        add(topBar, BorderLayout.NORTH);

        // ── Body (two side-by-side tables) ───────────────────────────
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(0, 36, 36, 36));

        // --- Plans Card ---
        JPanel plansCard = buildSectionCard("MEMBERSHIP PLANS",
                new String[]{"ID", "Name", "Duration (months)", "Price (₺)"},
                table -> {
                    plansTable = table;
                    plansModel = (DefaultTableModel) table.getModel();
                    plansTable.getColumnModel().getColumn(0).setMaxWidth(50);
                    plansTable.getColumnModel().getColumn(2).setMaxWidth(160);
                    plansTable.getColumnModel().getColumn(3).setMaxWidth(140);
                },
                actionRow -> {
                    JButton addPlan = accentBtn("+ New Plan");
                    addPlan.addActionListener(e -> showAddPlanDialog());
                    JButton editPlan = solidBtn("Edit Plan", new Color(0x3F3F46));
                    editPlan.addActionListener(e -> showEditPlanDialog());
                    JButton deletePlan = solidBtn("Delete Plan", new Color(0x3F0000));
                    deletePlan.setForeground(DANGER);
                    deletePlan.addActionListener(e -> deleteSelectedPlan());
                    actionRow.add(addPlan);
                    actionRow.add(Box.createHorizontalStrut(8));
                    actionRow.add(editPlan);
                    actionRow.add(Box.createHorizontalStrut(8));
                    actionRow.add(deletePlan);
                });

        // --- Active Subs Card ---
        JPanel subsCard = buildSectionCard("ACTIVE SUBSCRIPTIONS",
                new String[]{"ID", "Member", "Plan", "Start", "End", "Status"},
                table -> {
                    subsTable = table;
                    subsModel = (DefaultTableModel) table.getModel();
                    subsTable.getColumnModel().getColumn(0).setMaxWidth(50);
                    subsTable.getColumnModel().getColumn(5).setMaxWidth(100);
                },
                actionRow -> {
                    JButton deactivate = solidBtn("Deactivate", new Color(0x3F0000));
                    deactivate.setForeground(DANGER);
                    deactivate.addActionListener(e -> deactivateSelectedSub());
                    actionRow.add(deactivate);
                });

        body.add(plansCard);
        body.add(Box.createVerticalStrut(24));
        body.add(subsCard);

        JScrollPane bodyScroll = new JScrollPane(body);
        bodyScroll.setBorder(null);
        bodyScroll.setOpaque(false);
        bodyScroll.getViewport().setOpaque(false);
        add(bodyScroll, BorderLayout.CENTER);

        loadPlans();
        loadSubs();
    }

    private JPanel buildSectionCard(String sectionTitle, String[] cols,
                                     java.util.function.Consumer<JTable> tableConfig,
                                     java.util.function.Consumer<JPanel> actionConfig) {
        // Section label
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel sLabel = new JLabel(sectionTitle);
        sLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        sLabel.setForeground(new Color(0x52525B));
        sLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sLabel.setBorder(new EmptyBorder(0, 0, 8, 0));
        wrapper.add(sLabel);

        // Card
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createLineBorder(BORDER_CLR, 1, true));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_PRIMARY);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(48);
        table.setGridColor(BORDER_CLR);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(0x27272A));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setBorder(null);

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(0x18181B));
        header.setForeground(TEXT_MUTED);
        header.setFont(new Font("SansSerif", Font.BOLD, 11));
        header.setPreferredSize(new Dimension(0, 42));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_CLR));

        tableConfig.accept(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.setBackground(BG_CARD);
        scroll.getViewport().setBackground(BG_CARD);
        card.add(scroll, BorderLayout.CENTER);

        // Action row
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 12));
        actionRow.setBackground(new Color(0x18181B));
        actionRow.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_CLR),
                new EmptyBorder(0, 16, 0, 16)));
        actionConfig.accept(actionRow);
        card.add(actionRow, BorderLayout.SOUTH);

        wrapper.add(card);
        return wrapper;
    }

    private void loadPlans() {
        plansModel.setRowCount(0);
        types = typeDAO.findAll();
        for (MembershipType t : types) {
            plansModel.addRow(new Object[]{
                t.getId(), t.getName(), t.getDurationMonths() + " months",
                String.format("₺%.2f", t.getPrice())
            });
        }
    }

    private void loadSubs() {
        subsModel.setRowCount(0);
        subs = subDAO.findAll();
        List<User>          allUsers = userDAO.findAll();
        List<MembershipType> allTypes = typeDAO.findAll();

        for (Subscription s : subs) {
            String memberName = allUsers.stream()
                    .filter(u -> u.getId() == s.getUserId())
                    .map(User::getName).findFirst().orElse("ID:" + s.getUserId());
            String planName = allTypes.stream()
                    .filter(t -> t.getId() == s.getMembershipTypeId())
                    .map(MembershipType::getName).findFirst().orElse("Plan:" + s.getMembershipTypeId());
            String status = s.isActive() ? "ACTIVE" : "INACTIVE";
            subsModel.addRow(new Object[]{
                s.getId(), memberName, planName,
                s.getStartDate(), s.getEndDate(), status
            });
        }
    }

    private void showAddPlanDialog() {
        JTextField nameField  = field("e.g. 3 Months Plan");
        JTextField monthField = field("3");
        JTextField priceField = field("1350.00");

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(BG_CARD);
        form.setBorder(new EmptyBorder(16, 24, 16, 24));
        JLabel title = new JLabel("New Membership Plan");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(title);
        form.add(Box.createVerticalStrut(20));
        form.add(createFormRow("Plan Name:", nameField));

        JPanel row2 = new JPanel(new GridLayout(1, 2, 16, 0));
        row2.setOpaque(false); row2.setAlignmentX(Component.LEFT_ALIGNMENT);
        row2.add(createFormRow("Duration (months):", monthField));
        row2.add(createFormRow("Price (₺):", priceField));
        form.add(row2);

        int res = JOptionPane.showConfirmDialog(this, form, "New Plan", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            try {
                int months = Integer.parseInt(monthField.getText().trim());
                double price = Double.parseDouble(priceField.getText().trim());
                typeDAO.save(new MembershipType(nameField.getText().trim(), months, price));
                loadPlans();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numeric values.");
            }
        }
    }

    private void showEditPlanDialog() {
        int row = plansTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a plan first."); return; }
        MembershipType selected = types.get(row);

        JTextField nameField  = field(selected.getName());
        nameField.setText(selected.getName());
        JTextField monthField = field(String.valueOf(selected.getDurationMonths()));
        monthField.setText(String.valueOf(selected.getDurationMonths()));
        JTextField priceField = field(String.valueOf(selected.getPrice()));
        priceField.setText(String.valueOf(selected.getPrice()));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(BG_CARD);
        form.setBorder(new EmptyBorder(16, 24, 16, 24));
        JLabel title = new JLabel("Edit Plan — " + selected.getName());
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(title);
        form.add(Box.createVerticalStrut(20));
        form.add(createFormRow("Plan Name:", nameField));

        JPanel row2 = new JPanel(new GridLayout(1, 2, 16, 0));
        row2.setOpaque(false); row2.setAlignmentX(Component.LEFT_ALIGNMENT);
        row2.add(createFormRow("Duration (months):", monthField));
        row2.add(createFormRow("Price (₺):", priceField));
        form.add(row2);

        int res = JOptionPane.showConfirmDialog(this, form, "Edit Plan", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            try {
                selected.setName(nameField.getText().trim());
                selected.setDurationMonths(Integer.parseInt(monthField.getText().trim()));
                selected.setPrice(Double.parseDouble(priceField.getText().trim()));
                typeDAO.update(selected);
                loadPlans();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numeric values.");
            }
        }
    }

    private void deleteSelectedPlan() {
        int row = plansTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a plan first."); return; }
        MembershipType selected = types.get(row);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete plan \"" + selected.getName() + "\"?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            typeDAO.delete(selected.getId());
            loadPlans();
        }
    }

    private void deactivateSelectedSub() {
        int row = subsTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a subscription first."); return; }
        Subscription selected = subs.get(row);
        if (!selected.isActive()) { JOptionPane.showMessageDialog(this, "This subscription is already inactive."); return; }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Deactivate this subscription?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            selected.setActive(false);
            subDAO.update(selected);
            loadSubs();
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────
    private JPanel createFormRow(String labelText, JComponent fieldComp) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lbl = new JLabel(labelText);
        lbl.setForeground(TEXT_MUTED);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        fieldComp.setAlignmentX(Component.LEFT_ALIGNMENT);
        fieldComp.setMaximumSize(new Dimension(800, 42));
        row.add(lbl);
        row.add(Box.createVerticalStrut(6));
        row.add(fieldComp);
        row.add(Box.createVerticalStrut(14));
        return row;
    }

    private JTextField field(String placeholder) {
        JTextField tf = new JTextField();
        tf.setBackground(FIELD_BG);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(ACCENT);
        tf.setFont(new Font("SansSerif", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR, 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        if (!placeholder.isEmpty()) tf.putClientProperty("JTextField.placeholderText", placeholder);
        return tf;
    }

    private JButton accentBtn(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? ACCENT_H : ACCENT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setFocusPainted(false); btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(140, 38));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton solidBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(TEXT_PRIMARY);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
