package com.mertncu.clubmembershipmanagement.module.auth.controller;

import com.mertncu.clubmembershipmanagement.common.enums.UserRole;
import com.mertncu.clubmembershipmanagement.module.auth.dao.UserDAO;
import com.mertncu.clubmembershipmanagement.module.auth.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Admin panel for managing users — view, add, edit role, delete.
 */
public class ManageUsersPanel extends JPanel {

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

    private final UserDAO userDAO = new UserDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private List<User> users;

    public ManageUsersPanel() {
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
        JLabel title = new JLabel("Manage Users");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(TEXT_PRIMARY);
        JLabel sub = new JLabel("View, create, and manage all system users and their roles");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sub.setForeground(TEXT_MUTED);
        hdr.add(title);
        hdr.add(Box.createVerticalStrut(4));
        hdr.add(sub);
        topBar.add(hdr, BorderLayout.WEST);

        JButton addBtn = accentBtn("+ Add User");
        addBtn.addActionListener(e -> showAddUserDialog());
        JPanel btnWrap = new JPanel(new GridBagLayout());
        btnWrap.setOpaque(false);
        btnWrap.add(addBtn);
        topBar.add(btnWrap, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // ── Table Card ───────────────────────────────────────────────
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
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR, 1, true),
                new EmptyBorder(0, 0, 0, 0)));

        // Table model
        String[] cols = {"ID", "Name", "Email", "Role", "Created At"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
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

        // Set column widths
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.getColumnModel().getColumn(3).setMaxWidth(120);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.setBackground(BG_CARD);
        scroll.getViewport().setBackground(BG_CARD);

        card.add(scroll, BorderLayout.CENTER);

        // ── Action Row ───────────────────────────────────────────────
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        actionRow.setBackground(new Color(0x18181B));
        actionRow.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_CLR));

        JButton editRoleBtn = solidBtn("Change Role", new Color(0x3F3F46));
        editRoleBtn.addActionListener(e -> changeSelectedRole());

        JButton deleteBtn = solidBtn("Delete User", new Color(0x3F0000));
        deleteBtn.setForeground(DANGER);
        deleteBtn.addActionListener(e -> deleteSelectedUser());

        actionRow.add(editRoleBtn);
        actionRow.add(deleteBtn);
        card.add(actionRow, BorderLayout.SOUTH);

        JPanel outerWrap = new JPanel(new BorderLayout());
        outerWrap.setOpaque(false);
        outerWrap.setBorder(new EmptyBorder(0, 36, 36, 36));
        outerWrap.add(card, BorderLayout.CENTER);

        add(outerWrap, BorderLayout.CENTER);

        loadUsers();
    }

    private void loadUsers() {
        tableModel.setRowCount(0);
        users = userDAO.findAll();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
        for (User u : users) {
            String createdAt = u.getCreatedAt() != null ? u.getCreatedAt().format(fmt) : "—";
            tableModel.addRow(new Object[]{
                u.getId(), u.getName(), u.getEmail(), u.getRole().name(), createdAt
            });
        }
    }

    private void showAddUserDialog() {
        JTextField nameField   = field("Full Name");
        JTextField emailField  = field("email@example.com");
        JTextField passField   = field("Password");
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"MEMBER", "TRAINER", "ADMIN"});
        roleCombo.setBackground(FIELD_BG);
        roleCombo.setForeground(TEXT_PRIMARY);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(BG_CARD);
        form.setBorder(new EmptyBorder(16, 24, 16, 24));

        JLabel title = new JLabel("Add New User");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(title);
        form.add(Box.createVerticalStrut(20));

        form.add(createFormRow("Full Name:", nameField));
        form.add(createFormRow("Email:", emailField));
        form.add(createFormRow("Password:", passField));
        form.add(createFormRow("Role:", roleCombo));

        int res = JOptionPane.showConfirmDialog(this, form, "Add User", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String name  = nameField.getText().trim();
            String email = emailField.getText().trim();
            String pass  = passField.getText().trim();
            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.");
                return;
            }
            User u = new User(name, email, pass, UserRole.valueOf((String) roleCombo.getSelectedItem()));
            userDAO.save(u);
            loadUsers();
        }
    }

    private void changeSelectedRole() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a user first."); return; }
        User selected = users.get(row);
        String[] roles = {"MEMBER", "TRAINER", "ADMIN"};
        String newRole = (String) JOptionPane.showInputDialog(
                this, "Select new role for " + selected.getName() + ":",
                "Change Role", JOptionPane.PLAIN_MESSAGE, null, roles, selected.getRole().name());
        if (newRole != null) {
            selected.setRole(UserRole.valueOf(newRole));
            userDAO.update(selected);
            loadUsers();
        }
    }

    private void deleteSelectedUser() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a user first."); return; }
        User selected = users.get(row);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete user \"" + selected.getName() + "\"? This cannot be undone.",
                "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            userDAO.delete(selected.getId());
            loadUsers();
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
