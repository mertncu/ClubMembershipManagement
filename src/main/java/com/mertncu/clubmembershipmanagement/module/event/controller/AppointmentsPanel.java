package com.mertncu.clubmembershipmanagement.module.event.controller;

import com.mertncu.clubmembershipmanagement.common.session.SessionManager;
import com.mertncu.clubmembershipmanagement.module.auth.dao.UserDAO;
import com.mertncu.clubmembershipmanagement.module.auth.model.User;
import com.mertncu.clubmembershipmanagement.module.event.dao.AppointmentDAO;
import com.mertncu.clubmembershipmanagement.module.event.model.Appointment;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Member view: see & cancel own appointments.
 * Trainer view: see all their appointments and approve/cancel.
 */
public class AppointmentsPanel extends JPanel {

    private static final Color BG_DARK      = new Color(0x18181B);
    private static final Color BG_CARD      = new Color(0x1F1F23);
    private static final Color ACCENT       = new Color(0xFF4500);
    private static final Color BORDER_CLR   = new Color(0x27272A);
    private static final Color TEXT_PRIMARY  = new Color(0xF4F4F5);
    private static final Color TEXT_MUTED   = new Color(0x71717A);
    private static final Color SUCCESS      = new Color(0x22C55E);
    private static final Color DANGER       = new Color(0xEF4444);
    private static final Color WARNING      = new Color(0xF59E0B);

    private final AppointmentDAO apptDAO    = new AppointmentDAO();
    private final UserDAO        userDAO    = new UserDAO();
    private DefaultTableModel    tableModel;
    private JTable               table;
    private List<Appointment>    currentList;
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy  HH:mm");

    private final boolean isTrainer;

    public AppointmentsPanel() {
        User u = SessionManager.getInstance().getCurrentUser();
        isTrainer = u != null && "TRAINER".equals(u.getRole().name());
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
        JLabel title = new JLabel(isTrainer ? "Client Appointments" : "My Appointments");
        title.setFont(new Font("SansSerif", Font.BOLD, 26)); title.setForeground(TEXT_PRIMARY);
        JLabel sub = new JLabel(isTrainer ? "Review and manage incoming booking requests" : "Track your booked training sessions");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13)); sub.setForeground(TEXT_MUTED);
        hdr.add(title); hdr.add(Box.createVerticalStrut(4)); hdr.add(sub);
        topBar.add(hdr, BorderLayout.WEST);

        JButton refresh = new JButton("↻ Refresh");
        refresh.setFont(new Font("SansSerif", Font.PLAIN, 12)); refresh.setForeground(TEXT_MUTED);
        refresh.setContentAreaFilled(false); refresh.setBorderPainted(false); refresh.setFocusPainted(false);
        refresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refresh.addActionListener(e -> loadData());
        JPanel rw = new JPanel(new GridBagLayout()); rw.setOpaque(false); rw.add(refresh);
        topBar.add(rw, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Table
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(24, 36, 36, 36));

        String[] cols = isTrainer
                ? new String[]{"#", "Member", "Date & Time", "Status"}
                : new String[]{"#", "Trainer", "Date & Time", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        styleTable(table);
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_CLR, 1, true));
        sp.setBackground(BG_CARD); sp.getViewport().setBackground(BG_CARD);
        body.add(sp, BorderLayout.CENTER);

        // Action row
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionRow.setOpaque(false);
        actionRow.setBorder(new EmptyBorder(12, 0, 0, 0));

        if (isTrainer) {
            JButton approveBtn = outlineBtn("✓  Approve", SUCCESS);
            approveBtn.addActionListener(e -> updateStatus("APPROVED"));
            JButton cancelBtn  = outlineBtn("✗  Cancel",  DANGER);
            cancelBtn.addActionListener(e -> updateStatus("CANCELLED"));
            actionRow.add(approveBtn);
            actionRow.add(cancelBtn);
        } else {
            JButton cancelBtn = outlineBtn("✗  Cancel Appointment", DANGER);
            cancelBtn.addActionListener(e -> updateStatus("CANCELLED"));
            actionRow.add(cancelBtn);
        }
        body.add(actionRow, BorderLayout.SOUTH);
        add(body, BorderLayout.CENTER);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        User me = SessionManager.getInstance().getCurrentUser();
        if (me == null) return;

        currentList = isTrainer
                ? apptDAO.findByTrainerId(me.getId())
                : apptDAO.findByUserId(me.getId());

        for (Appointment a : currentList) {
            String peerName = isTrainer
                    ? userDAO.findById(a.getUserId()).map(User::getName).orElse("Unknown")
                    : userDAO.findById(a.getTrainerId()).map(User::getName).orElse("Unknown");
            tableModel.addRow(new Object[]{
                    a.getId(),
                    peerName,
                    a.getAppointmentDate().format(DT_FMT),
                    a.getStatus()
            });
        }
    }

    private void updateStatus(String newStatus) {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a row first."); return; }
        Appointment appt = currentList.get(row);
        if ("CANCELLED".equals(appt.getStatus())) {
            JOptionPane.showMessageDialog(this, "This appointment is already cancelled.");
            return;
        }
        int c = JOptionPane.showConfirmDialog(this,
                "Mark this appointment as " + newStatus + "?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            appt.setStatus(newStatus);
            apptDAO.update(appt);
            loadData();
        }
    }

    private void styleTable(JTable t) {
        t.setBackground(BG_CARD); t.setForeground(TEXT_PRIMARY);
        t.setFont(new Font("SansSerif", Font.PLAIN, 13)); t.setRowHeight(42);
        t.setGridColor(BORDER_CLR);
        t.getTableHeader().setBackground(new Color(0x111113));
        t.getTableHeader().setForeground(TEXT_MUTED);
        t.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));
        t.setSelectionBackground(new Color(0x27272A));
        t.setSelectionForeground(TEXT_PRIMARY);
        t.setShowHorizontalLines(true); t.setShowVerticalLines(false);
        t.setBorder(null); t.setIntercellSpacing(new Dimension(0, 0));
        // Color-code status column
        t.setDefaultRenderer(Object.class, (tbl, value, isSelected, hasFocus, row2, col) -> {
            JLabel cell = new JLabel(value != null ? value.toString() : "");
            cell.setOpaque(true);
            cell.setBackground(isSelected ? new Color(0x27272A) : BG_CARD);
            cell.setBorder(new EmptyBorder(0, 12, 0, 12));
            cell.setFont(col == 3 ? new Font("SansSerif", Font.BOLD, 12) : new Font("SansSerif", Font.PLAIN, 13));
            if (col == 3) {
                cell.setForeground(switch (value != null ? value.toString() : "") {
                    case "APPROVED"  -> SUCCESS;
                    case "CANCELLED" -> DANGER;
                    default          -> WARNING;  // PENDING
                });
            } else {
                cell.setForeground(isSelected ? TEXT_PRIMARY : TEXT_PRIMARY);
            }
            return cell;
        });
    }

    private JButton outlineBtn(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setForeground(color);
        btn.setBackground(BG_CARD);
        btn.setBorder(BorderFactory.createLineBorder(color, 1, true));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
