package com.mertncu.clubmembershipmanagement.module.diet.controller;

import com.mertncu.clubmembershipmanagement.common.session.SessionManager;
import com.mertncu.clubmembershipmanagement.module.auth.model.User;
import com.mertncu.clubmembershipmanagement.module.diet.dao.DietItemDAO;
import com.mertncu.clubmembershipmanagement.module.diet.dao.DietProgramDAO;
import com.mertncu.clubmembershipmanagement.module.diet.model.DietItem;
import com.mertncu.clubmembershipmanagement.module.diet.model.DietProgram;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Dual-role panel:
 *  - MEMBER → sees assigned diet program with meals grouped by type
 *  - TRAINER → creates/assigns diet programs and adds meal items
 */
public class DietProgramPanel extends JPanel {

    private static final Color BG_DARK      = new Color(0x18181B);
    private static final Color BG_CARD      = new Color(0x1F1F23);
    private static final Color ACCENT       = new Color(0xFF4500);
    private static final Color ACCENT_H     = new Color(0xE03D00);
    private static final Color BORDER_CLR   = new Color(0x27272A);
    private static final Color TEXT_PRIMARY  = new Color(0xF4F4F5);
    private static final Color TEXT_MUTED   = new Color(0x71717A);
    private static final Color FIELD_BG     = new Color(0x27272A);
    private static final Color SUCCESS      = new Color(0x22C55E);
    private static final Color C_BLUE       = new Color(0x0EA5E9);

    private final Color[] MEAL_COLORS = {ACCENT, C_BLUE, SUCCESS, new Color(0x8B5CF6)};
    private final String[] MEAL_TYPES = {"BREAKFAST", "LUNCH", "DINNER", "SNACK"};

    private final DietProgramDAO dietDAO = new DietProgramDAO();
    private final DietItemDAO    itemDAO = new DietItemDAO();
    private final boolean isTrainer;

    private JPanel contentArea;
    private JComboBox<String> programSelector;
    private List<DietProgram> programs;

    public DietProgramPanel() {
        User u = SessionManager.getInstance().getCurrentUser();
        isTrainer = u != null && "TRAINER".equals(u.getRole().name());
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
        JLabel title = new JLabel(isTrainer ? "Manage Diet Programs" : "My Diet Program");
        title.setFont(new Font("SansSerif", Font.BOLD, 26)); title.setForeground(TEXT_PRIMARY);
        JLabel sub = new JLabel(isTrainer ? "Create and manage personalised nutrition plans for members"
                                          : "View your personalised nutrition plan assigned by your trainer");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13)); sub.setForeground(TEXT_MUTED);
        hdr.add(title); hdr.add(Box.createVerticalStrut(4)); hdr.add(sub);
        topBar.add(hdr, BorderLayout.WEST);

        if (isTrainer) {
            JButton newBtn = accentBtn("+ New Program");
            newBtn.addActionListener(e -> showCreateProgramDialog());
            JPanel btnWrap = new JPanel(new GridBagLayout()); btnWrap.setOpaque(false); btnWrap.add(newBtn);
            topBar.add(btnWrap, BorderLayout.EAST);
        }
        add(topBar, BorderLayout.NORTH);

        // Body
        contentArea = new JPanel();
        contentArea.setLayout(new BoxLayout(contentArea, BoxLayout.Y_AXIS));
        contentArea.setOpaque(false);
        contentArea.setBorder(new EmptyBorder(24, 36, 36, 36));

        JScrollPane scroll = new JScrollPane(contentArea);
        scroll.setBorder(null); scroll.setOpaque(false); scroll.getViewport().setOpaque(false);
        add(scroll, BorderLayout.CENTER);

        loadContent();
    }

    private void loadContent() {
        contentArea.removeAll();
        User me = SessionManager.getInstance().getCurrentUser();
        if (me == null) return;

        programs = isTrainer ? dietDAO.findByTrainerId(me.getId()) : dietDAO.findByUserId(me.getId());

        if (programs.isEmpty()) {
            JLabel empty = new JLabel(isTrainer
                    ? "No diet programs created yet. Click '+ New Program' to start."
                    : "No diet program has been assigned to you yet.");
            empty.setFont(new Font("SansSerif", Font.ITALIC, 14));
            empty.setForeground(TEXT_MUTED);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentArea.add(Box.createVerticalStrut(40));
            contentArea.add(empty);
        } else {
            for (DietProgram prog : programs) {
                contentArea.add(buildProgramSection(prog));
                contentArea.add(Box.createVerticalStrut(24));
            }
        }
        contentArea.revalidate(); contentArea.repaint();
    }

    private JPanel buildProgramSection(DietProgram prog) {
        JPanel section = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.dispose();
            }
        };
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        section.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR, 1, true),
                new EmptyBorder(28, 36, 28, 36)));

        // Program header
        JPanel progHdr = new JPanel(new BorderLayout());
        progHdr.setOpaque(false);
        progHdr.setAlignmentX(Component.LEFT_ALIGNMENT);
        progHdr.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel nameCol = new JPanel(); nameCol.setLayout(new BoxLayout(nameCol, BoxLayout.Y_AXIS)); nameCol.setOpaque(false);
        JLabel nameLbl = new JLabel(prog.getName());
        nameLbl.setFont(new Font("SansSerif", Font.BOLD, 18)); nameLbl.setForeground(TEXT_PRIMARY);
        JPanel metaRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        metaRow.setOpaque(false);
        metaRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel goalLbl = new JLabel("Goal: " + prog.getGoal() + "  ");
        goalLbl.setFont(new Font("SansSerif", Font.PLAIN, 13)); goalLbl.setForeground(TEXT_MUTED);
        
        metaRow.add(goalLbl);
        metaRow.add(badge(prog.getDailyCalories() + " kcal/day", new Color(0xF59E0B)));
        
        nameCol.add(nameLbl); nameCol.add(Box.createVerticalStrut(6)); nameCol.add(metaRow);
        progHdr.add(nameCol, BorderLayout.WEST);

        if (isTrainer) {
            JButton addItemBtn = new JButton("+ Add Meal");
            addItemBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
            addItemBtn.setForeground(TEXT_PRIMARY);
            addItemBtn.setBackground(new Color(0x3F3F46));
            addItemBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            addItemBtn.setFocusPainted(false);
            addItemBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            addItemBtn.addActionListener(e -> showAddItemDialog(prog));
            JPanel btnWrap = new JPanel(new GridBagLayout()); btnWrap.setOpaque(false); btnWrap.add(addItemBtn);
            progHdr.add(btnWrap, BorderLayout.EAST);
        }
        section.add(progHdr);
        section.add(Box.createVerticalStrut(18));

        // Meals grouped by type
        List<DietItem> items = itemDAO.findByProgramId(prog.getId());
        for (int mi = 0; mi < MEAL_TYPES.length; mi++) {
            final int idx = mi;
            List<DietItem> group = items.stream()
                    .filter(i -> MEAL_TYPES[idx].equals(i.getMealType())).toList();
            if (!group.isEmpty()) {
                section.add(mealGroupWidget(MEAL_TYPES[idx], group, MEAL_COLORS[idx]));
                section.add(Box.createVerticalStrut(14));
            }
        }
        if (items.isEmpty()) {
            JLabel noItems = new JLabel("No meals added yet.");
            noItems.setFont(new Font("SansSerif", Font.ITALIC, 12));
            noItems.setForeground(TEXT_MUTED);
            noItems.setAlignmentX(Component.LEFT_ALIGNMENT);
            section.add(noItems);
        }
        return section;
    }

    private JPanel mealGroupWidget(String mealType, List<DietItem> items, Color color) {
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setOpaque(false);
        group.setAlignmentX(Component.LEFT_ALIGNMENT);
        group.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel mealLbl = new JLabel(mealType);
        mealLbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        mealLbl.setForeground(color);
        mealLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        group.add(mealLbl);
        group.add(Box.createVerticalStrut(6));

        String[] cols = {"Food", "Qty(g)", "kcal", "P(g)", "C(g)", "F(g)"};
        Object[][] data = new Object[items.size()][cols.length];
        for (int i = 0; i < items.size(); i++) {
            DietItem it = items.get(i);
            data[i] = new Object[]{it.getFoodName(), it.getQuantity(),
                    it.getCalories(), it.getProtein(), it.getCarbs(), it.getFat()};
        }
        JTable t = new JTable(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        t.setBackground(BG_CARD); t.setForeground(TEXT_PRIMARY);
        t.setFont(new Font("SansSerif", Font.PLAIN, 13)); t.setRowHeight(48);
        t.setGridColor(BORDER_CLR);
        t.getTableHeader().setBackground(new Color(0x18181B));
        t.getTableHeader().setForeground(TEXT_MUTED);
        t.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));
        t.getTableHeader().setPreferredSize(new Dimension(0, 42));
        t.setShowHorizontalLines(true); t.setShowVerticalLines(false);
        t.setBorder(null);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(new Color(0x27272A));
        t.setSelectionForeground(TEXT_PRIMARY);

        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(null);
        sp.setBackground(BG_CARD); sp.getViewport().setBackground(BG_CARD);
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, t.getRowHeight() * items.size() + 42));
        group.add(sp);
        return group;
    }

    // ── Dialogs ──────────────────────────────────────────────────────────

    private void showCreateProgramDialog() {
        User me = SessionManager.getInstance().getCurrentUser();
        if (me == null) return;

        JTextField nameField = field(); JTextField goalField = field();
        JTextField calField  = field("2000"); JTextArea noteArea = new JTextArea(3, 20);
        noteArea.setBackground(FIELD_BG); noteArea.setForeground(TEXT_PRIMARY);
        noteArea.setCaretColor(ACCENT); noteArea.setFont(new Font("SansSerif", Font.PLAIN, 13));

        com.mertncu.clubmembershipmanagement.module.auth.dao.UserDAO userDAO = new com.mertncu.clubmembershipmanagement.module.auth.dao.UserDAO();
        java.util.List<User> members = userDAO.findAll().stream()
                .filter(u -> "MEMBER".equalsIgnoreCase(u.getRole().name())).toList();
        
        JComboBox<UserComboItem> memberCombo = new JComboBox<>();
        for (User u : members) {
            memberCombo.addItem(new UserComboItem(u));
        }
        memberCombo.setBackground(FIELD_BG);
        memberCombo.setForeground(TEXT_PRIMARY);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(BG_CARD);
        form.setBorder(new EmptyBorder(16, 24, 16, 24));

        JLabel title = new JLabel("Create New Diet Plan");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(title);
        form.add(Box.createVerticalStrut(24));

        form.add(createFormRow("Program Name:", nameField));
        form.add(createFormRow("Goal:", goalField));
        form.add(createFormRow("Daily Calories:", calField));
        form.add(createFormRow("Assign to Member:", memberCombo));
        form.add(createFormRow("Notes:", new JScrollPane(noteArea)));

        int res = JOptionPane.showConfirmDialog(this, form, "New Diet Program",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            try {
                UserComboItem selectedMember = (UserComboItem) memberCombo.getSelectedItem();
                int memberId = selectedMember != null ? selectedMember.getId() : -1;
                int cal      = Integer.parseInt(calField.getText().trim());
                DietProgram dp = new DietProgram(memberId, me.getId(),
                        nameField.getText().trim(), goalField.getText().trim(),
                        null, null, cal, noteArea.getText().trim());
                dietDAO.save(dp);
                loadContent();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numeric values.");
            }
        }
    }

    private void showAddItemDialog(DietProgram prog) {
        JComboBox<String> mealCombo = new JComboBox<>(MEAL_TYPES);
        mealCombo.setBackground(FIELD_BG); mealCombo.setForeground(TEXT_PRIMARY);
        JTextField foodField = field(); JTextField qtyField = field("100");
        JTextField calField  = field("0"); JTextField proteinField = field("0");
        JTextField carbsField = field("0"); JTextField fatField = field("0");
        JComboBox<String> dayCombo = new JComboBox<>(new String[]{"ALL","MON","TUE","WED","THU","FRI","SAT","SUN"});
        dayCombo.setBackground(FIELD_BG); dayCombo.setForeground(TEXT_PRIMARY);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(BG_CARD);
        form.setBorder(new EmptyBorder(16, 24, 16, 24));

        JLabel title = new JLabel("Add Meal Item");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(title);
        form.add(Box.createVerticalStrut(24));

        form.add(createFormRow("Meal Type:", mealCombo));
        form.add(createFormRow("Food Name:", foodField));
        
        JPanel row2 = new JPanel(new GridLayout(1, 2, 16, 0));
        row2.setOpaque(false); row2.setAlignmentX(Component.LEFT_ALIGNMENT);
        row2.add(createFormRow("Qty (g):", qtyField));
        row2.add(createFormRow("Calories:", calField));
        form.add(row2);
        
        JPanel row3 = new JPanel(new GridLayout(1, 3, 16, 0));
        row3.setOpaque(false); row3.setAlignmentX(Component.LEFT_ALIGNMENT);
        row3.add(createFormRow("Protein (g):", proteinField));
        row3.add(createFormRow("Carbs (g):", carbsField));
        row3.add(createFormRow("Fat (g):", fatField));
        form.add(row3);
        
        form.add(createFormRow("Day:", dayCombo));

        int res = JOptionPane.showConfirmDialog(this, form, "Add Meal Item",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            try {
                DietItem item = new DietItem(prog.getId(),
                        (String) mealCombo.getSelectedItem(),
                        foodField.getText().trim(),
                        Double.parseDouble(qtyField.getText().trim()),
                        Integer.parseInt(calField.getText().trim()),
                        Double.parseDouble(proteinField.getText().trim()),
                        Double.parseDouble(carbsField.getText().trim()),
                        Double.parseDouble(fatField.getText().trim()),
                        (String) dayCombo.getSelectedItem());
                itemDAO.save(item);
                loadContent();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numeric values.");
            }
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private JTextField field() {
        return field("");
    }
    private JTextField field(String placeholder) {
        JTextField tf = new JTextField();
        tf.setBackground(FIELD_BG); tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(ACCENT); tf.setFont(new Font("SansSerif", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR, 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        if (!placeholder.isEmpty()) tf.putClientProperty("JTextField.placeholderText", placeholder);
        return tf;
    }

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
        fieldComp.setMaximumSize(new Dimension(800, fieldComp instanceof JScrollPane ? 120 : 42));
        
        row.add(lbl);
        row.add(Box.createVerticalStrut(6));
        row.add(fieldComp);
        row.add(Box.createVerticalStrut(14));
        return row;
    }

    private JPanel badge(String text, Color bg) {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
            }
        };
        p.setLayout(new GridBagLayout());
        p.setOpaque(false);
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        l.setForeground(Color.WHITE);
        p.add(l);
        p.setBorder(new EmptyBorder(4, 8, 4, 8));
        p.setMaximumSize(p.getPreferredSize());
        p.setAlignmentY(Component.CENTER_ALIGNMENT);
        return p;
    }

    private static class UserComboItem {
        private final User user;
        public UserComboItem(User user) { this.user = user; }
        public int getId() { return user.getId(); }
        @Override public String toString() { return user.getName() + " (" + user.getEmail() + ")"; }
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
}
