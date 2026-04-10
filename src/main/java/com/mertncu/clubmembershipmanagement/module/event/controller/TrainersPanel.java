package com.mertncu.clubmembershipmanagement.module.event.controller;

import com.mertncu.clubmembershipmanagement.common.session.SessionManager;
import com.mertncu.clubmembershipmanagement.module.auth.dao.UserDAO;
import com.mertncu.clubmembershipmanagement.module.auth.model.User;
import com.mertncu.clubmembershipmanagement.module.body.dao.TrainerProfileDAO;
import com.mertncu.clubmembershipmanagement.module.body.model.TrainerProfile;
import com.mertncu.clubmembershipmanagement.module.event.dao.AppointmentDAO;
import com.mertncu.clubmembershipmanagement.module.event.dao.TrainerAvailabilityDAO;
import com.mertncu.clubmembershipmanagement.module.event.model.Appointment;
import com.mertncu.clubmembershipmanagement.module.event.model.TrainerAvailability;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TrainersPanel extends JPanel {

    private static final Color BG_DARK      = new Color(0x18181B);
    private static final Color BG_CARD      = new Color(0x1F1F23);
    private static final Color ACCENT       = new Color(0xFF4500);
    private static final Color ACCENT_H     = new Color(0xE03D00);
    private static final Color BORDER_CLR   = new Color(0x27272A);
    private static final Color TEXT_PRIMARY  = new Color(0xF4F4F5);
    private static final Color TEXT_MUTED   = new Color(0x71717A);
    private static final Color C_PURPLE     = new Color(0x8B5CF6);
    private static final Color C_BLUE       = new Color(0x0EA5E9);
    private static final Color C_AMBER      = new Color(0xF59E0B);

    private final UserDAO              userDAO        = new UserDAO();
    private final TrainerProfileDAO    profileDAO     = new TrainerProfileDAO();
    private final TrainerAvailabilityDAO availDAO     = new TrainerAvailabilityDAO();
    private final AppointmentDAO       apptDAO        = new AppointmentDAO();
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd MMM HH:mm");

    private JPanel listPanel;
    private JPanel detailPanel;

    public TrainersPanel() {
        setLayout(new BorderLayout());
        setBackground(BG_DARK);
        buildUI();
        loadTrainers();
    }

    private void buildUI() {
        // Header
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(32, 36, 0, 36));
        JPanel hdr = new JPanel(); hdr.setLayout(new BoxLayout(hdr, BoxLayout.Y_AXIS)); hdr.setOpaque(false);
        JLabel title = new JLabel("Our Trainers");
        title.setFont(new Font("SansSerif", Font.BOLD, 26)); title.setForeground(TEXT_PRIMARY);
        JLabel sub = new JLabel("Browse trainer profiles and book a private session");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13)); sub.setForeground(TEXT_MUTED);
        hdr.add(title); hdr.add(Box.createVerticalStrut(4)); hdr.add(sub);
        topBar.add(hdr, BorderLayout.WEST);
        add(topBar, BorderLayout.NORTH);

        // Body: left = list, right = detail
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(24, 36, 36, 36));

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);
        JScrollPane listScroll = new JScrollPane(listPanel);
        listScroll.setBorder(null); listScroll.setOpaque(false);
        listScroll.getViewport().setOpaque(false);

        detailPanel = new JPanel(new GridBagLayout());
        detailPanel.setOpaque(false);
        JLabel hint = new JLabel("← Select a trainer to see details");
        hint.setForeground(TEXT_MUTED); hint.setFont(new Font("SansSerif", Font.ITALIC, 14));
        detailPanel.add(hint);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1;
        gbc.weightx = 0.4; gbc.gridx = 0; gbc.insets = new Insets(0, 0, 0, 14);
        body.add(listScroll, gbc);
        gbc.weightx = 0.6; gbc.gridx = 1; gbc.insets = new Insets(0, 0, 0, 0);
        body.add(detailPanel, gbc);

        add(body, BorderLayout.CENTER);
    }

    private void loadTrainers() {
        listPanel.removeAll();
        List<User> trainers = userDAO.findAll().stream()
                .filter(u -> "TRAINER".equals(u.getRole().name()))
                .toList();

        if (trainers.isEmpty()) {
            JLabel e = new JLabel("No trainers registered yet.");
            e.setFont(new Font("SansSerif", Font.ITALIC, 13));
            e.setForeground(TEXT_MUTED);
            e.setBorder(new EmptyBorder(20, 0, 0, 0));
            listPanel.add(e);
        }

        Color[] palette = {ACCENT, C_PURPLE, C_BLUE, C_AMBER};
        int i = 0;
        for (User trainer : trainers) {
            TrainerProfile profile = profileDAO.findByUserId(trainer.getId()).orElse(null);
            listPanel.add(buildTrainerRow(trainer, profile, palette[i++ % palette.length]));
            listPanel.add(Box.createVerticalStrut(8));
        }
        listPanel.revalidate(); listPanel.repaint();
    }

    private JPanel buildTrainerRow(User trainer, TrainerProfile profile, Color accent) {
        JPanel row = new JPanel(new BorderLayout(14, 0)) {
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
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR, 1, true),
                new EmptyBorder(14, 18, 14, 14)));
        row.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Avatar
        JLabel avatar = new JLabel(String.valueOf(trainer.getName().charAt(0)).toUpperCase()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accent);
                g2.fillOval(0, 0, 40, 40);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setForeground(Color.WHITE);
        avatar.setFont(new Font("SansSerif", Font.BOLD, 17));
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setPreferredSize(new Dimension(40, 40));

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel nameLbl = new JLabel(trainer.getName());
        nameLbl.setFont(new Font("SansSerif", Font.BOLD, 14)); nameLbl.setForeground(TEXT_PRIMARY);
        JLabel specLbl = new JLabel(profile != null && profile.getSpecialization() != null
                ? profile.getSpecialization() : "General Fitness");
        specLbl.setFont(new Font("SansSerif", Font.PLAIN, 12)); specLbl.setForeground(TEXT_MUTED);
        info.add(nameLbl); info.add(specLbl);

        JLabel arrowLbl = new JLabel("›");
        arrowLbl.setFont(new Font("SansSerif", Font.BOLD, 22));
        arrowLbl.setForeground(TEXT_MUTED);

        row.add(avatar, BorderLayout.WEST);
        row.add(info, BorderLayout.CENTER);
        row.add(arrowLbl, BorderLayout.EAST);

        row.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) { showTrainerDetail(trainer, profile, accent); }
            public void mouseEntered(java.awt.event.MouseEvent e) { row.setOpaque(true); row.setBackground(new Color(0x27272A)); row.repaint(); }
            public void mouseExited(java.awt.event.MouseEvent e)  { row.setOpaque(false); row.repaint(); }
        });
        return row;
    }

    private void showTrainerDetail(User trainer, TrainerProfile profile, Color accent) {
        detailPanel.removeAll();
        detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
        detailPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR, 1, true),
                new EmptyBorder(28, 28, 28, 28)));

        // Avatar header
        JPanel avatarRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        avatarRow.setOpaque(false);
        avatarRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel bigAvatar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accent);
                g2.fillOval(0, 0, 64, 64);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 26));
                FontMetrics fm = g2.getFontMetrics();
                String letter = String.valueOf(trainer.getName().charAt(0)).toUpperCase();
                g2.drawString(letter, (64 - fm.stringWidth(letter)) / 2, (64 + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        bigAvatar.setPreferredSize(new Dimension(64, 64));
        bigAvatar.setOpaque(false);

        JPanel nameBlock = new JPanel(); nameBlock.setLayout(new BoxLayout(nameBlock, BoxLayout.Y_AXIS)); nameBlock.setOpaque(false);
        JLabel nameLbl = new JLabel(trainer.getName());
        nameLbl.setFont(new Font("SansSerif", Font.BOLD, 20)); nameLbl.setForeground(TEXT_PRIMARY);
        JLabel tagLbl = new JLabel(profile != null && profile.getSpecialization() != null ? profile.getSpecialization() : "Trainer");
        tagLbl.setFont(new Font("SansSerif", Font.PLAIN, 13)); tagLbl.setForeground(accent);
        nameBlock.add(nameLbl); nameBlock.add(Box.createVerticalStrut(4)); nameBlock.add(tagLbl);
        avatarRow.add(bigAvatar); avatarRow.add(nameBlock);
        detailPanel.add(avatarRow);
        detailPanel.add(Box.createVerticalStrut(20));

        if (profile != null) {
            if (profile.getBio() != null && !profile.getBio().isEmpty()) {
                JLabel bioLbl = new JLabel("<html><div style='width:360px;'>" + profile.getBio() + "</div></html>");
                bioLbl.setFont(new Font("SansSerif", Font.PLAIN, 13)); bioLbl.setForeground(TEXT_MUTED);
                bioLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
                detailPanel.add(bioLbl);
                detailPanel.add(Box.createVerticalStrut(16));
            }
            detailPanel.add(infoChip("Experience", profile.getYearsExperience() + " yrs"));
            detailPanel.add(Box.createVerticalStrut(6));
            if (profile.getCertifications() != null)
                detailPanel.add(infoChip("Certifications", profile.getCertifications()));
        }

        detailPanel.add(Box.createVerticalStrut(24));

        // Availability slots
        JLabel availTitle = sectionLbl("AVAILABLE SLOTS");
        detailPanel.add(availTitle);
        detailPanel.add(Box.createVerticalStrut(10));

        List<TrainerAvailability> slots = availDAO.findAvailableByTrainer(trainer.getId());
        if (slots.isEmpty()) {
            JLabel noSlot = new JLabel("No availability slots defined.");
            noSlot.setFont(new Font("SansSerif", Font.ITALIC, 12));
            noSlot.setForeground(TEXT_MUTED);
            noSlot.setAlignmentX(Component.LEFT_ALIGNMENT);
            detailPanel.add(noSlot);
        } else {
            JPanel slotsPanel = new JPanel();
            slotsPanel.setLayout(new BoxLayout(slotsPanel, BoxLayout.Y_AXIS));
            slotsPanel.setOpaque(false);
            slotsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            for (TrainerAvailability slot : slots) {
                slotsPanel.add(buildSlotRow(slot, trainer));
                slotsPanel.add(Box.createVerticalStrut(6));
            }
            JScrollPane slotScroll = new JScrollPane(slotsPanel);
            slotScroll.setBorder(null); slotScroll.setOpaque(false);
            slotScroll.getViewport().setOpaque(false);
            slotScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
            slotScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
            detailPanel.add(slotScroll);
        }

        detailPanel.revalidate(); detailPanel.repaint();
    }

    private JPanel buildSlotRow(TrainerAvailability slot, User trainer) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR, 1, true),
                new EmptyBorder(8, 12, 8, 10)));

        JLabel timeLbl = new JLabel(slot.getSlotStart().format(DT_FMT)
                + "  →  " + slot.getSlotEnd().format(DT_FMT));
        timeLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        timeLbl.setForeground(TEXT_PRIMARY);

        JButton bookBtn = new JButton("Book") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? ACCENT_H : ACCENT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 6, 6));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        bookBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        bookBtn.setForeground(Color.WHITE);
        bookBtn.setContentAreaFilled(false); bookBtn.setBorderPainted(false);
        bookBtn.setFocusPainted(false); bookBtn.setOpaque(false);
        bookBtn.setPreferredSize(new Dimension(60, 30));
        bookBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bookBtn.addActionListener(e -> bookSlot(slot, trainer));

        row.add(timeLbl, BorderLayout.CENTER);
        row.add(bookBtn, BorderLayout.EAST);
        return row;
    }

    private void bookSlot(TrainerAvailability slot, User trainer) {
        User me = SessionManager.getInstance().getCurrentUser();
        if (me == null) return;
        int c = JOptionPane.showConfirmDialog(this,
                "Book a session with " + trainer.getName() + "\non "
                        + slot.getSlotStart().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")) + "?",
                "Confirm Booking", JOptionPane.OK_CANCEL_OPTION);
        if (c == JOptionPane.OK_OPTION) {
            Appointment appt = new Appointment(me.getId(), trainer.getId(), slot.getSlotStart(), "PENDING");
            apptDAO.save(appt);
            slot.setBooked(true);
            availDAO.update(slot);
            JOptionPane.showMessageDialog(this, "Appointment booked! Awaiting trainer approval.",
                    "Booked", JOptionPane.INFORMATION_MESSAGE);
            showTrainerDetail(trainer, profileDAO.findByUserId(trainer.getId()).orElse(null),
                    ACCENT); // refresh
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────
    private JLabel sectionLbl(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        l.setForeground(new Color(0x52525B));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }
    private JPanel infoChip(String key, String val) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false); p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel k = new JLabel(key + ":"); k.setFont(new Font("SansSerif", Font.BOLD, 12)); k.setForeground(TEXT_MUTED);
        JLabel v = new JLabel(val); v.setFont(new Font("SansSerif", Font.PLAIN, 12)); v.setForeground(TEXT_PRIMARY);
        p.add(k); p.add(v);
        return p;
    }
}
