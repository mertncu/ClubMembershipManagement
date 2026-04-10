package com.mertncu.clubmembershipmanagement.module.branch.controller;

import com.mertncu.clubmembershipmanagement.module.branch.dao.GymBranchDAO;
import com.mertncu.clubmembershipmanagement.module.branch.model.GymBranch;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class GymBranchPanel extends JPanel {

    private static final Color BG_DARK     = new Color(0x18181B);
    private static final Color BG_CARD     = new Color(0x1F1F23);
    private static final Color ACCENT      = new Color(0xFF4500);
    private static final Color ACCENT_H    = new Color(0xE03D00);
    private static final Color BORDER_CLR  = new Color(0x27272A);
    private static final Color TEXT_PRIMARY = new Color(0xF4F4F5);
    private static final Color TEXT_MUTED  = new Color(0x71717A);
    private static final Color FIELD_BG    = new Color(0x27272A);
    private static final Color DANGER      = new Color(0xEF4444);

    private final GymBranchDAO branchDAO = new GymBranchDAO();
    private DefaultTableModel tableModel;
    private JTable table;

    // Form fields
    private JTextField fName, fAddress, fCity, fPhone, fManager, fCapacity;
    private JButton saveBtn;
    private GymBranch editingBranch = null;

    public GymBranchPanel() {
        setLayout(new BorderLayout());
        setBackground(BG_DARK);
        buildUI();
        loadData();
    }

    private void buildUI() {
        // Header
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(32, 36, 0, 36));
        JPanel hdr = new JPanel(); hdr.setLayout(new BoxLayout(hdr, BoxLayout.Y_AXIS)); hdr.setOpaque(false);
        JLabel title = new JLabel("Gym Branches");
        title.setFont(new Font("SansSerif", Font.BOLD, 26)); title.setForeground(TEXT_PRIMARY);
        JLabel sub = new JLabel("Add, edit and manage your gym locations");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13)); sub.setForeground(TEXT_MUTED);
        hdr.add(title); hdr.add(Box.createVerticalStrut(4)); hdr.add(sub);
        topBar.add(hdr, BorderLayout.WEST);
        add(topBar, BorderLayout.NORTH);

        // Split: left = table, right = form
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(24, 36, 36, 36));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1;
        gbc.weightx = 0.6; gbc.gridx = 0; gbc.insets = new Insets(0, 0, 0, 14);
        body.add(buildTablePanel(), gbc);

        gbc.weightx = 0.4; gbc.gridx = 1; gbc.insets = new Insets(0, 0, 0, 0);
        body.add(buildFormPanel(), gbc);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildTablePanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        String[] cols = {"ID", "Branch Name", "City", "Manager", "Capacity"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setBackground(BG_CARD); table.setForeground(TEXT_PRIMARY);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13)); table.setRowHeight(40);
        table.setGridColor(BORDER_CLR);
        table.getTableHeader().setBackground(new Color(0x111113));
        table.getTableHeader().setForeground(TEXT_MUTED);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));
        table.setSelectionBackground(new Color(0x27272A));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setShowHorizontalLines(true); table.setShowVerticalLines(false);
        table.setBorder(null); table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelectedIntoForm();
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_CLR, 1, true));
        sp.setBackground(BG_CARD); sp.getViewport().setBackground(BG_CARD);

        // Action row under table
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionRow.setOpaque(false);
        actionRow.setBorder(new EmptyBorder(10, 0, 0, 0));

        JButton editBtn = outlineBtn("Edit Selected", new Color(0x0EA5E9));
        editBtn.addActionListener(e -> loadSelectedIntoForm());

        JButton delBtn = outlineBtn("Delete", DANGER);
        delBtn.addActionListener(e -> deleteSelected());

        actionRow.add(editBtn);
        actionRow.add(delBtn);

        wrapper.add(sp, BorderLayout.CENTER);
        wrapper.add(actionRow, BorderLayout.SOUTH);
        return wrapper;
    }

    private JPanel buildFormPanel() {
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
                new EmptyBorder(24, 24, 24, 24)));

        JLabel formTitle = new JLabel("Add / Edit Branch");
        formTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        formTitle.setForeground(TEXT_PRIMARY);
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(formTitle);
        card.add(Box.createVerticalStrut(20));

        fName     = addField(card, "Branch Name");
        fAddress  = addField(card, "Address");
        fCity     = addField(card, "City");
        fPhone    = addField(card, "Phone");
        fManager  = addField(card, "Manager Name");
        fCapacity = addField(card, "Capacity");

        card.add(Box.createVerticalStrut(20));

        saveBtn = new JButton("Add Branch") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? ACCENT_H : ACCENT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        saveBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setContentAreaFilled(false);
        saveBtn.setBorderPainted(false);
        saveBtn.setFocusPainted(false);
        saveBtn.setOpaque(false);
        saveBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.addActionListener(e -> saveBranch());
        card.add(saveBtn);

        card.add(Box.createVerticalStrut(8));
        JButton clearBtn = new JButton("Clear Form");
        clearBtn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        clearBtn.setForeground(TEXT_MUTED);
        clearBtn.setContentAreaFilled(false);
        clearBtn.setBorderPainted(false);
        clearBtn.setFocusPainted(false);
        clearBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        clearBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearBtn.addActionListener(e -> clearForm());
        card.add(clearBtn);

        return card;
    }

    private JTextField addField(JPanel parent, String label) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        lbl.setForeground(TEXT_MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(lbl);
        parent.add(Box.createVerticalStrut(5));

        JTextField tf = new JTextField();
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        tf.setBackground(FIELD_BG); tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(ACCENT);
        tf.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR, 1, true),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(tf);
        parent.add(Box.createVerticalStrut(10));
        return tf;
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<GymBranch> branches = branchDAO.findAll();
        for (GymBranch b : branches) {
            tableModel.addRow(new Object[]{b.getId(), b.getName(), b.getCity(), b.getManagerName(), b.getCapacity()});
        }
    }

    private void loadSelectedIntoForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int id = (int) tableModel.getValueAt(row, 0);
        branchDAO.findById(id).ifPresent(b -> {
            editingBranch = b;
            fName.setText(b.getName());
            fAddress.setText(b.getAddress());
            fCity.setText(b.getCity());
            fPhone.setText(b.getPhone());
            fManager.setText(b.getManagerName());
            fCapacity.setText(String.valueOf(b.getCapacity()));
            saveBtn.setText("Save Changes");
        });
    }

    private void saveBranch() {
        try {
            String name = fName.getText().trim();
            if (name.isEmpty()) { JOptionPane.showMessageDialog(this, "Branch name is required."); return; }

            if (editingBranch == null) {
                GymBranch b = new GymBranch(name, fAddress.getText().trim(), fCity.getText().trim(),
                        fPhone.getText().trim(), fManager.getText().trim(),
                        fCapacity.getText().isEmpty() ? 0 : Integer.parseInt(fCapacity.getText().trim()));
                branchDAO.save(b);
            } else {
                editingBranch.setName(name);
                editingBranch.setAddress(fAddress.getText().trim());
                editingBranch.setCity(fCity.getText().trim());
                editingBranch.setPhone(fPhone.getText().trim());
                editingBranch.setManagerName(fManager.getText().trim());
                editingBranch.setCapacity(fCapacity.getText().isEmpty() ? 0 : Integer.parseInt(fCapacity.getText().trim()));
                branchDAO.update(editingBranch);
            }
            clearForm();
            loadData();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Capacity must be a number.");
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int id = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this branch?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            branchDAO.delete(id);
            loadData();
            clearForm();
        }
    }

    private void clearForm() {
        editingBranch = null;
        fName.setText(""); fAddress.setText(""); fCity.setText("");
        fPhone.setText(""); fManager.setText(""); fCapacity.setText("");
        saveBtn.setText("Add Branch");
        table.clearSelection();
    }

    private JButton outlineBtn(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setForeground(color);
        btn.setBackground(new Color(0x1F1F23));
        btn.setBorder(BorderFactory.createLineBorder(color, 1, true));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
