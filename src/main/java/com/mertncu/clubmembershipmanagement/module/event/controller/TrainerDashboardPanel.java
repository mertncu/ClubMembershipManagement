package com.mertncu.clubmembershipmanagement.module.event.controller;

import com.mertncu.clubmembershipmanagement.common.session.SessionManager;
import com.mertncu.clubmembershipmanagement.module.auth.dao.UserDAO;
import com.mertncu.clubmembershipmanagement.module.auth.model.User;
import com.mertncu.clubmembershipmanagement.module.body.dao.TrainerProfileDAO;
import com.mertncu.clubmembershipmanagement.module.body.model.TrainerProfile;
import com.mertncu.clubmembershipmanagement.module.event.dao.AppointmentDAO;
import com.mertncu.clubmembershipmanagement.module.event.dao.EventDAO;
import com.mertncu.clubmembershipmanagement.module.event.dao.TrainerAvailabilityDAO;
import com.mertncu.clubmembershipmanagement.module.event.model.TrainerAvailability;
import com.mertncu.clubmembershipmanagement.module.event.model.Appointment;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Home panel for TRAINER role — shows a summary + quick links. */
public class TrainerDashboardPanel extends JPanel {

    private static final Color BG_DARK      = new Color(0x18181B);
    private static final Color BG_CARD      = new Color(0x1F1F23);
    private static final Color ACCENT       = new Color(0xFF4500);
    private static final Color ACCENT_H     = new Color(0xE03D00);
    private static final Color BORDER_CLR   = new Color(0x27272A);
    private static final Color TEXT_PRIMARY  = new Color(0xF4F4F5);
    private static final Color TEXT_MUTED   = new Color(0x71717A);
    private static final Color FIELD_BG     = new Color(0x27272A);
    private static final Color SUCCESS      = new Color(0x22C55E);

    private final AppointmentDAO        apptDAO    = new AppointmentDAO();
    private final EventDAO              eventDAO   = new EventDAO();
    private final TrainerAvailabilityDAO availDAO  = new TrainerAvailabilityDAO();
    private final TrainerProfileDAO     profileDAO = new TrainerProfileDAO();
    private final UserDAO               userDAO    = new UserDAO();

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd MMM HH:mm");

    public TrainerDashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(BG_DARK);
        build();
    }

    private void build() {
        User me = SessionManager.getInstance().getCurrentUser();
        if (me == null) return;

        // Header
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(32, 36, 0, 36));
        JPanel hdr = new JPanel(); hdr.setLayout(new BoxLayout(hdr, BoxLayout.Y_AXIS)); hdr.setOpaque(false);
        JLabel title = new JLabel("Trainer Dashboard");
        title.setFont(new Font("SansSerif", Font.BOLD, 26)); title.setForeground(TEXT_PRIMARY);
        JLabel sub = new JLabel("Welcome back, " + me.getName());
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13)); sub.setForeground(TEXT_MUTED);
        hdr.add(title); hdr.add(Box.createVerticalStrut(4)); hdr.add(sub);
        JLabel dateLbl = new JLabel(DateTimeFormatter.ofPattern("MMMM d, yyyy").format(java.time.LocalDate.now()));
        dateLbl.setFont(new Font("SansSerif", Font.PLAIN, 13)); dateLbl.setForeground(TEXT_MUTED);
        JPanel dw = new JPanel(new GridBagLayout()); dw.setOpaque(false); dw.add(dateLbl);
        topBar.add(hdr, BorderLayout.WEST); topBar.add(dw, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Body
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(24, 36, 36, 36));

        // KPI cards
        int pendingAppts = (int) apptDAO.findByTrainerId(me.getId()).stream()
                .filter(a -> "PENDING".equals(a.getStatus())).count();
        int totalClients = apptDAO.findByTrainerId(me.getId()).stream()
                .mapToInt(a -> a.getUserId()).distinct().toArray().length;
        int myEvents     = eventDAO.findAll().stream()
                .filter(e -> e.getTrainerId() == me.getId()).toArray().length;
        int freeSlots    = availDAO.findAvailableByTrainer(me.getId()).size();

        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 16, 0));
        kpiRow.setOpaque(false); kpiRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        kpiRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        kpiRow.add(kpiCard("Pending Requests", String.valueOf(pendingAppts), ACCENT, "⏳"));
        kpiRow.add(kpiCard("Total Clients",    String.valueOf(totalClients), new Color(0x8B5CF6), "◈"));
        kpiRow.add(kpiCard("My Events",        String.valueOf(myEvents),     new Color(0x0EA5E9), "◉"));
        kpiRow.add(kpiCard("Free Slots",       String.valueOf(freeSlots),    SUCCESS, "◷"));
        body.add(sectionLbl("KEY STATS"));
        body.add(Box.createVerticalStrut(12));
        body.add(kpiRow);
        body.add(Box.createVerticalStrut(28));

        // Upcoming appointments (next 5)
        body.add(sectionLbl("UPCOMING APPOINTMENTS"));
        body.add(Box.createVerticalStrut(10));
        List<TrainerAvailability> slots = availDAO.findByTrainerId(me.getId()).stream()
                .filter(TrainerAvailability::isBooked).limit(5).toList();
        if (slots.isEmpty()) {
            body.add(emptyMsg("No booked appointments yet."));
        } else {
            for (TrainerAvailability slot : slots) {
                body.add(slotRow(slot));
                body.add(Box.createVerticalStrut(8));
            }
        }
        body.add(Box.createVerticalStrut(28));

        // --- Add Recent Clients ---
        body.add(sectionLbl("RECENT INTERACTED CLIENTS"));
        body.add(Box.createVerticalStrut(10));
        
        java.util.List<Appointment> trainerAppts = apptDAO.findByTrainerId(me.getId());
        java.util.List<Integer> clientIds = trainerAppts.stream()
            .map(Appointment::getUserId)
            .distinct().limit(4).toList();
            
        if (clientIds.isEmpty()) {
            body.add(emptyMsg("No clients booked yet."));
        } else {
            JPanel clientsRow = new JPanel(new GridLayout(1, 4, 12, 0));
            clientsRow.setOpaque(false);
            clientsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            clientsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
            
            for (Integer cid : clientIds) {
                userDAO.findById(cid).ifPresent(client -> {
                    JPanel clientChip = new JPanel(new BorderLayout(8, 0));
                    clientChip.setBackground(BG_CARD);
                    clientChip.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_CLR, 1, true),
                        new EmptyBorder(8, 12, 8, 12)));
                    
                    JLabel nameLbl = new JLabel(client.getName());
                    nameLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
                    nameLbl.setForeground(TEXT_PRIMARY);
                    
                    JLabel mailLbl = new JLabel(client.getEmail());
                    mailLbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
                    mailLbl.setForeground(TEXT_MUTED);
                    
                    JPanel infoWrap = new JPanel();
                    infoWrap.setLayout(new BoxLayout(infoWrap, BoxLayout.Y_AXIS));
                    infoWrap.setOpaque(false);
                    infoWrap.add(nameLbl);
                    infoWrap.add(mailLbl);
                    
                    JLabel av = new JLabel(" \uD83D\uDC64 ");
                    av.setForeground(ACCENT);
                    
                    clientChip.add(av, BorderLayout.WEST);
                    clientChip.add(infoWrap, BorderLayout.CENTER);
                    clientsRow.add(clientChip);
                });
            }
            body.add(clientsRow);
        }
        body.add(Box.createVerticalStrut(28));

        // --- Quick Tools Area ---
        body.add(sectionLbl("QUICK TOOLS"));
        body.add(Box.createVerticalStrut(10));
        
        JPanel actionsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        actionsRow.setOpaque(false);
        actionsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JButton addSlotBtn = accentBtn("+ Add Slot");
        addSlotBtn.setPreferredSize(new Dimension(160, 42));
        addSlotBtn.addActionListener(e -> showAddSlotDialog(me.getId()));
        
        TrainerProfile profile = profileDAO.findByUserId(me.getId()).orElse(new TrainerProfile(me.getId(), "", "", 0, ""));
        JButton editProfileBtn = new JButton("Edit Profile");
        editProfileBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        editProfileBtn.setForeground(ACCENT); 
        editProfileBtn.setBackground(new Color(0x2D1200));
        editProfileBtn.setBorder(BorderFactory.createLineBorder(ACCENT, 1, true));
        editProfileBtn.setFocusPainted(false); 
        editProfileBtn.setOpaque(false);
        editProfileBtn.setPreferredSize(new Dimension(160, 42));
        editProfileBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        editProfileBtn.addActionListener(e -> showEditProfileDialog(profile, me.getId()));
        
        actionsRow.add(addSlotBtn);
        actionsRow.add(editProfileBtn);
        body.add(actionsRow);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null); scroll.setOpaque(false); scroll.getViewport().setOpaque(false);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel kpiCard(String label, String value, Color accent, String icon) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
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
        iconLbl.setFont(new Font("SansSerif", Font.BOLD, 11)); iconLbl.setForeground(TEXT_MUTED);
        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font("SansSerif", Font.BOLD, 28)); valueLbl.setForeground(TEXT_PRIMARY);
        card.add(iconLbl); card.add(Box.createVerticalStrut(12)); card.add(valueLbl);
        return card;
    }

    private JPanel slotRow(TrainerAvailability slot) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR, 1, true),
                new EmptyBorder(10, 16, 10, 16)));

        JLabel time = new JLabel(slot.getSlotStart().format(DT_FMT)
                + "  →  " + slot.getSlotEnd().format(DT_FMT));
        time.setFont(new Font("SansSerif", Font.PLAIN, 13)); time.setForeground(TEXT_PRIMARY);
        JLabel badge = new JLabel("BOOKED");
        badge.setFont(new Font("SansSerif", Font.BOLD, 10)); badge.setForeground(ACCENT);
        row.add(time, BorderLayout.WEST); row.add(badge, BorderLayout.EAST);
        return row;
    }

    private void showAddSlotDialog(int trainerId) {
        JPanel form = new JPanel(new GridLayout(0, 1, 0, 8));
        form.setBackground(BG_CARD);
        JTextField startField = field("yyyy-MM-dd HH:mm");
        JTextField endField   = field("yyyy-MM-dd HH:mm");
        form.add(lb("Slot Start (yyyy-MM-dd HH:mm):")); form.add(startField);
        form.add(lb("Slot End:")); form.add(endField);

        int res = JOptionPane.showConfirmDialog(this, form, "Add Availability Slot",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            try {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                LocalDateTime start = LocalDateTime.parse(startField.getText().trim(), fmt);
                LocalDateTime end   = LocalDateTime.parse(endField.getText().trim(), fmt);
                availDAO.save(new TrainerAvailability(trainerId, start, end));
                JOptionPane.showMessageDialog(this, "Slot added successfully!");
                // Rebuild panel
                removeAll(); build(); revalidate(); repaint();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid date format. Use: yyyy-MM-dd HH:mm");
            }
        }
    }

    private void showEditProfileDialog(TrainerProfile profile, int userId) {
        JTextField specField = field(profile.getSpecialization() != null ? profile.getSpecialization() : "");
        JTextField certField = field(profile.getCertifications() != null ? profile.getCertifications() : "");
        JTextField yrsField  = field(String.valueOf(profile.getYearsExperience()));
        JTextArea  bioArea   = new JTextArea(profile.getBio() != null ? profile.getBio() : "", 4, 30);
        bioArea.setBackground(FIELD_BG); bioArea.setForeground(TEXT_PRIMARY);
        bioArea.setCaretColor(ACCENT); bioArea.setLineWrap(true); bioArea.setWrapStyleWord(true);

        JPanel form = new JPanel(new GridLayout(0, 1, 0, 8));
        form.setBackground(BG_CARD);
        form.add(lb("Specialization:")); form.add(specField);
        form.add(lb("Years Experience:")); form.add(yrsField);
        form.add(lb("Certifications:")); form.add(certField);
        form.add(lb("Bio:")); form.add(new JScrollPane(bioArea));

        int res = JOptionPane.showConfirmDialog(this, form, "Edit Trainer Profile",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            try {
                profile.setUserId(userId);
                profile.setSpecialization(specField.getText().trim());
                profile.setCertifications(certField.getText().trim());
                profile.setYearsExperience(Integer.parseInt(yrsField.getText().trim()));
                profile.setBio(bioArea.getText().trim());
                profileDAO.save(profile);
                JOptionPane.showMessageDialog(this, "Profile updated!", "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input.");
            }
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private JLabel sectionLbl(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("SansSerif", Font.BOLD, 11)); l.setForeground(new Color(0x52525B));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }
    private JLabel emptyMsg(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("SansSerif", Font.ITALIC, 13)); l.setForeground(TEXT_MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }
    private JTextField field(String ph) {
        JTextField tf = new JTextField();
        tf.setBackground(FIELD_BG); tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(ACCENT); tf.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR, 1, true),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        if (!ph.isEmpty()) tf.putClientProperty("JTextField.placeholderText", ph);
        return tf;
    }
    private JLabel lb(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(TEXT_MUTED); l.setFont(new Font("SansSerif", Font.BOLD, 11));
        return l;
    }
    private JButton accentBtn(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? ACCENT_H : ACCENT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setFocusPainted(false); btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(200, 42));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
