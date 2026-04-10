package com.mertncu.clubmembershipmanagement.module.dashboard.controller;

import com.mertncu.clubmembershipmanagement.common.session.SessionManager;
import com.mertncu.clubmembershipmanagement.module.auth.model.User;
import com.mertncu.clubmembershipmanagement.module.membership.dao.MembershipTypeDAO;
import com.mertncu.clubmembershipmanagement.module.membership.dao.SubscriptionDAO;
import com.mertncu.clubmembershipmanagement.module.membership.model.MembershipType;
import com.mertncu.clubmembershipmanagement.module.membership.model.Subscription;
import com.mertncu.clubmembershipmanagement.module.body.dao.BodyMeasurementDAO;
import com.mertncu.clubmembershipmanagement.module.body.model.BodyMeasurement;
import com.mertncu.clubmembershipmanagement.module.event.dao.AppointmentDAO;
import com.mertncu.clubmembershipmanagement.module.event.model.Appointment;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class DashboardHomePanel extends JPanel {

    // ── Palette ──────────────────────────────────────────────────────────
    private static final Color BG_DARK     = new Color(0x18181B);
    private static final Color BG_CARD     = new Color(0x1F1F23);
    private static final Color ACCENT      = new Color(0xFF4500);
    private static final Color BORDER_CLR  = new Color(0x27272A);
    private static final Color TEXT_PRIMARY = new Color(0xF4F4F5);
    private static final Color TEXT_MUTED  = new Color(0x71717A);
    private static final Color SUCCESS     = new Color(0x22C55E);
    private static final Color DANGER      = new Color(0xEF4444);
    private static final Color WARNING     = new Color(0xF59E0B);
    // ─────────────────────────────────────────────────────────────────────

    private JLabel greetingLabel;
    private JLabel membershipStatusLabel;
    private JLabel daysRemainingLabel;
    private JLabel expiryDateLabel;
    private JLabel planLabel;
    private JPanel noSubscriptionBox;

    private final SubscriptionDAO    subscriptionDAO    = new SubscriptionDAO();
    private final MembershipTypeDAO  membershipTypeDAO  = new MembershipTypeDAO();
    private final BodyMeasurementDAO bodyDAO            = new BodyMeasurementDAO();
    private final AppointmentDAO     apptDAO            = new AppointmentDAO();

    private JLabel latestWeightLbl;
    private JLabel latestBmiLbl;
    private JPanel apptsContainer;

    public DashboardHomePanel() {
        initComponents();
        setGreeting();
        loadSubscriptionData();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(BG_DARK);

        // ── Page Header ──────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(32, 36, 0, 36));

        greetingLabel = new JLabel("Welcome! 👋");
        greetingLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        greetingLabel.setForeground(TEXT_PRIMARY);
        topBar.add(greetingLabel, BorderLayout.WEST);

        JLabel dateLabel = new JLabel(DateTimeFormatter.ofPattern("MMMM d, yyyy")
                .format(java.time.LocalDate.now()));
        dateLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        dateLabel.setForeground(TEXT_MUTED);
        JPanel dateWrapper = new JPanel(new GridBagLayout());
        dateWrapper.setOpaque(false);
        dateWrapper.add(dateLabel);
        topBar.add(dateWrapper, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // ── Body ─────────────────────────────────────────────────────────
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(24, 36, 36, 36));

        // Section: Your Membership
        body.add(sectionLabel("YOUR MEMBERSHIP"));
        body.add(Box.createVerticalStrut(12));

        // Stat cards row
        JPanel cardsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        cardsRow.setOpaque(false);
        cardsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        membershipStatusLabel = new JLabel("...");
        membershipStatusLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        membershipStatusLabel.setForeground(SUCCESS);

        planLabel = new JLabel("...");
        planLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        planLabel.setForeground(TEXT_PRIMARY);

        daysRemainingLabel = new JLabel("...");
        daysRemainingLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        daysRemainingLabel.setForeground(TEXT_PRIMARY);

        expiryDateLabel = new JLabel("...");
        expiryDateLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        expiryDateLabel.setForeground(TEXT_PRIMARY);

        cardsRow.add(statCard("Status",         "◉",  membershipStatusLabel, ACCENT));
        cardsRow.add(statCard("Active Plan",     "◈",  planLabel,            new Color(0x8B5CF6)));
        cardsRow.add(statCard("Days Remaining",  "◷",  daysRemainingLabel,   new Color(0x0EA5E9)));
        cardsRow.add(statCard("Expiry Date",     "◻",  expiryDateLabel,      new Color(0xF59E0B)));

        body.add(cardsRow);

        // No subscription notice
        body.add(Box.createVerticalStrut(20));
        noSubscriptionBox = new JPanel();
        noSubscriptionBox.setLayout(new BoxLayout(noSubscriptionBox, BoxLayout.Y_AXIS));
        noSubscriptionBox.setOpaque(false);
        noSubscriptionBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel noSubCard = new JPanel(new GridBagLayout());
        noSubCard.setBackground(new Color(0x1F1F23));
        noSubCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xFF4500, false), 1, true),
                new EmptyBorder(24, 28, 24, 28)));
        noSubCard.putClientProperty("FlatLaf.style", "arc: 12");

        JPanel noSubContent = new JPanel();
        noSubContent.setLayout(new BoxLayout(noSubContent, BoxLayout.Y_AXIS));
        noSubContent.setOpaque(false);

        JLabel noSubIcon = new JLabel("⚠");
        noSubIcon.setFont(new Font("SansSerif", Font.PLAIN, 28));
        noSubIcon.setForeground(ACCENT);
        noSubIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel noSubTitle = new JLabel("No Active Subscription");
        noSubTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        noSubTitle.setForeground(TEXT_PRIMARY);
        noSubTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel noSubDesc = new JLabel("Visit 'My Subscription' to choose a plan.");
        noSubDesc.setFont(new Font("SansSerif", Font.PLAIN, 13));
        noSubDesc.setForeground(TEXT_MUTED);
        noSubDesc.setAlignmentX(Component.CENTER_ALIGNMENT);

        noSubContent.add(noSubIcon);
        noSubContent.add(Box.createVerticalStrut(8));
        noSubContent.add(noSubTitle);
        noSubContent.add(Box.createVerticalStrut(4));
        noSubContent.add(noSubDesc);
        noSubCard.add(noSubContent);
        noSubscriptionBox.add(noSubCard);

        body.add(noSubscriptionBox);

        // --- Quick Stats / Health Snapshot ---
        body.add(Box.createVerticalStrut(24));
        body.add(sectionLabel("HEALTH SNAPSHOT"));
        body.add(Box.createVerticalStrut(12));

        JPanel healthRow = new JPanel(new GridLayout(1, 2, 16, 0));
        healthRow.setOpaque(false);
        healthRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        healthRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        latestWeightLbl = new JLabel("— kg");
        latestWeightLbl.setFont(new Font("SansSerif", Font.BOLD, 22));
        latestWeightLbl.setForeground(TEXT_PRIMARY);

        latestBmiLbl = new JLabel("—");
        latestBmiLbl.setFont(new Font("SansSerif", Font.BOLD, 22));
        latestBmiLbl.setForeground(TEXT_PRIMARY);

        healthRow.add(statCard("Weight", "⚖", latestWeightLbl, new Color(0x38BDF8)));
        healthRow.add(statCard("BMI", "📈", latestBmiLbl, new Color(0xF43F5E)));
        body.add(healthRow);

        // --- Upcoming Appointments ---
        body.add(Box.createVerticalStrut(24));
        body.add(sectionLabel("UPCOMING APPOINTMENTS"));
        body.add(Box.createVerticalStrut(12));

        apptsContainer = new JPanel();
        apptsContainer.setLayout(new BoxLayout(apptsContainer, BoxLayout.Y_AXIS));
        apptsContainer.setOpaque(false);
        apptsContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(apptsContainer);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        add(scroll, BorderLayout.CENTER);
    }

    /**
     * Creates a rounded stat card with an accent-coloured icon strip on the left.
     */
    private JPanel statCard(String title, String icon, JLabel valueLabel, Color iconColor) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                // left accent bar
                g2.setColor(iconColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, 4, getHeight(), 4, 4));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR, 1, true),
                new EmptyBorder(20, 24, 20, 16)));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        JLabel iconLbl = new JLabel(icon + "  " + title);
        iconLbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        iconLbl.setForeground(TEXT_MUTED);

        content.add(iconLbl);
        content.add(Box.createVerticalStrut(12));
        content.add(valueLabel);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    // ── Data ─────────────────────────────────────────────────────────────

    private void setGreeting() {
        User user  = SessionManager.getInstance().getCurrentUser();
        String name = (user != null) ? user.getName() : "there";
        int hour   = LocalTime.now().getHour();
        String tod = hour < 12 ? "Good morning" : hour < 18 ? "Good afternoon" : "Good evening";
        greetingLabel.setText(tod + ", " + name + "! 👋");
    }

    private void loadSubscriptionData() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) { showNoSubscription(); return; }

        Optional<Subscription> subOpt = subscriptionDAO.findActiveSubscriptionByUserId(user.getId());
        if (subOpt.isPresent()) {
            Subscription sub = subOpt.get();
            membershipStatusLabel.setText(sub.isActive() ? "Active" : "Inactive");
            membershipStatusLabel.setForeground(sub.isActive() ? SUCCESS : DANGER);

            long days = sub.getRemainingDays();
            daysRemainingLabel.setText(days + " days");
            if (days <= 7) daysRemainingLabel.setForeground(DANGER);
            else if (days <= 30) daysRemainingLabel.setForeground(WARNING);
            else daysRemainingLabel.setForeground(SUCCESS);

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
            expiryDateLabel.setText(sub.getEndDate().format(fmt));

            Optional<MembershipType> typeOpt = membershipTypeDAO.findById(sub.getMembershipTypeId());
            planLabel.setText(typeOpt.map(MembershipType::getName).orElse("Standard"));
            noSubscriptionBox.setVisible(false);
        } else {
            showNoSubscription();
        }

        // Load latest body measurements
        List<BodyMeasurement> measures = bodyDAO.findByUserId(user.getId());
        if (!measures.isEmpty()) {
            // grab the latest 
            BodyMeasurement latest = measures.get(measures.size() - 1);
            latestWeightLbl.setText(String.format("%.1f kg", latest.getWeightKg()));
            latestBmiLbl.setText(String.format("%.1f", latest.calcBMI()));
        }

        // Load upcoming appointments
        apptsContainer.removeAll();
        List<Appointment> appts = apptDAO.findByUserId(user.getId()).stream()
                .filter(a -> a.getStatus().equals("APPROVED") || a.getStatus().equals("PENDING"))
                .limit(3).toList();

        if (appts.isEmpty()) {
            JLabel noAppt = new JLabel("No upcoming appointments booked.");
            noAppt.setFont(new Font("SansSerif", Font.ITALIC, 13));
            noAppt.setForeground(TEXT_MUTED);
            apptsContainer.add(noAppt);
        } else {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
            for (Appointment a : appts) {
                JPanel row = new JPanel(new BorderLayout());
                row.setOpaque(false);
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
                row.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_CLR, 1, true),
                    new EmptyBorder(8, 16, 8, 16)));
                
                JLabel lbl = new JLabel("Session on " + (a.getAppointmentDate() != null ? a.getAppointmentDate().format(fmt) : "TBD"));
                lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
                lbl.setForeground(TEXT_PRIMARY);

                JLabel stat = new JLabel(a.getStatus());
                stat.setFont(new Font("SansSerif", Font.BOLD, 11));
                stat.setForeground("APPROVED".equals(a.getStatus()) ? SUCCESS : WARNING);

                row.add(lbl, BorderLayout.WEST);
                row.add(stat, BorderLayout.EAST);
                apptsContainer.add(row);
                apptsContainer.add(Box.createVerticalStrut(8));
            }
        }
        apptsContainer.revalidate(); apptsContainer.repaint();
    }

    private void showNoSubscription() {
        membershipStatusLabel.setText("None");
        membershipStatusLabel.setForeground(DANGER);
        daysRemainingLabel.setText("—");
        expiryDateLabel.setText("—");
        planLabel.setText("—");
        noSubscriptionBox.setVisible(true);
    }

    private JLabel sectionLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        l.setForeground(new Color(0x52525B));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }
}
