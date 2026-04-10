package com.mertncu.clubmembershipmanagement.module.dashboard.controller;

import com.mertncu.clubmembershipmanagement.common.session.SessionManager;
import com.mertncu.clubmembershipmanagement.common.ui.ViewManager;
import com.mertncu.clubmembershipmanagement.module.auth.controller.LoginPanel;
import com.mertncu.clubmembershipmanagement.module.auth.controller.ManageUsersPanel;
import com.mertncu.clubmembershipmanagement.module.auth.model.User;
import com.mertncu.clubmembershipmanagement.module.body.controller.BMICalculatorPanel;
import com.mertncu.clubmembershipmanagement.module.branch.controller.GymBranchPanel;
import com.mertncu.clubmembershipmanagement.module.diet.controller.DietProgramPanel;
import com.mertncu.clubmembershipmanagement.module.event.controller.AppointmentsPanel;
import com.mertncu.clubmembershipmanagement.module.event.controller.EventsPanel;
import com.mertncu.clubmembershipmanagement.module.event.controller.TrainerDashboardPanel;
import com.mertncu.clubmembershipmanagement.module.event.controller.TrainersPanel;
import com.mertncu.clubmembershipmanagement.module.membership.controller.ManageMembershipsPanel;
import com.mertncu.clubmembershipmanagement.module.membership.controller.MySubscriptionPanel;
import com.mertncu.clubmembershipmanagement.module.message.controller.ChatPanel;
import com.mertncu.clubmembershipmanagement.module.notification.controller.NotificationPanel;
import com.mertncu.clubmembershipmanagement.module.payment.controller.PaymentPanel;
import com.mertncu.clubmembershipmanagement.module.workout.controller.WorkoutProgramPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class DashboardPanel extends JPanel {

    // ── Palette ──────────────────────────────────────────────────────────
    private static final Color BG_DARK      = new Color(0x18181B);
    private static final Color BG_SIDEBAR   = new Color(0x111113);
    private static final Color BG_HEADER    = new Color(0x111113);
    private static final Color ACCENT       = new Color(0xFF4500);
    private static final Color BORDER_CLR   = new Color(0x27272A);
    private static final Color TEXT_PRIMARY  = new Color(0xF4F4F5);
    private static final Color TEXT_MUTED   = new Color(0x71717A);
    private static final Color NAV_HOVER    = new Color(0x27272A);
    private static final Color NAV_ACTIVE   = new Color(0x2D1200);
    // ─────────────────────────────────────────────────────────────────────

    private JLabel  welcomeLabel;
    private JLabel  roleLabel;
    private JPanel  mainContentPane;
    private JPanel  menuContainer;
    private JButton activeBtn;

    // Common
    private JButton btnHome;
    // Member
    private JButton btnSubscription;
    private JButton btnEvents;
    private JButton btnTrainers;
    private JButton btnBMI;
    private JButton btnNotifications;
    private JButton btnChat;
    // Admin
    private JButton btnManageUsers;
    private JButton btnManageBranches;
    private JButton btnManageMemberships;
    private JButton btnBranches;
    private JButton btnAdminNotif;
    private JButton btnAdminChat;
    // Trainer
    private JButton btnTrainerAppts;
    private JButton btnManageEvents;
    private JButton btnDiet;
    private JButton btnWorkout;
    private JButton btnTrainerBMI;
    private JButton btnTrainerNotif;
    private JButton btnTrainerChat;
    // Member programs
    private JButton btnMemberDiet;
    private JButton btnMemberWorkout;
    private JButton btnMemberAppts;

    public DashboardPanel() {
        initComponents();
        initializeData();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(BG_DARK);

        // ── Header ──────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_HEADER);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_CLR),
                new EmptyBorder(0, 24, 0, 24)));
        header.setPreferredSize(new Dimension(0, 60));

        welcomeLabel = new JLabel("Welcome");
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        welcomeLabel.setForeground(TEXT_PRIMARY);

        roleLabel = new JLabel("MEMBER");
        roleLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
        roleLabel.setForeground(ACCENT);
        roleLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT, 1, true),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)));

        JPanel userInfo = new JPanel();
        userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.Y_AXIS));
        userInfo.setOpaque(false);
        userInfo.setBorder(new EmptyBorder(10, 0, 10, 0));
        userInfo.add(welcomeLabel);
        userInfo.add(Box.createVerticalStrut(4));
        userInfo.add(roleLabel);
        header.add(userInfo, BorderLayout.WEST);

        JButton logoutBtn = new JButton("Sign Out") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? ACCENT : new Color(0x27272A));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logoutBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        logoutBtn.setForeground(TEXT_PRIMARY);
        logoutBtn.setContentAreaFilled(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setOpaque(false);
        logoutBtn.setPreferredSize(new Dimension(100, 36));
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> handleLogout());

        JPanel logoutWrapper = new JPanel(new GridBagLayout());
        logoutWrapper.setOpaque(false);
        logoutWrapper.add(logoutBtn);
        header.add(logoutWrapper, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── Sidebar ──────────────────────────────────────────────────────
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_CLR));

        JPanel sidebarTop = new JPanel(new GridBagLayout());
        sidebarTop.setOpaque(false);
        sidebarTop.setPreferredSize(new Dimension(220, 80));
        sidebarTop.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_CLR));
        try {
            java.net.URL logoUrl = getClass().getResource(
                    "/com/mertncu/clubmembershipmanagement/images/logo.png");
            if (logoUrl != null) {
                Image img = new ImageIcon(logoUrl).getImage()
                        .getScaledInstance(38, 38, Image.SCALE_SMOOTH);
                JLabel logoLbl = new JLabel(new ImageIcon(img));
                JLabel appName = new JLabel("ClubManager");
                appName.setFont(new Font("SansSerif", Font.BOLD, 14));
                appName.setForeground(TEXT_PRIMARY);
                JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
                row.setOpaque(false);
                row.add(logoLbl); row.add(appName);
                sidebarTop.add(row);
            }
        } catch (Exception ignored) {}
        sidebar.add(sidebarTop, BorderLayout.NORTH);

        menuContainer = new JPanel();
        menuContainer.setLayout(new BoxLayout(menuContainer, BoxLayout.Y_AXIS));
        menuContainer.setOpaque(false);
        menuContainer.setBorder(new EmptyBorder(12, 8, 12, 8));

        JScrollPane menuScroll = new JScrollPane(menuContainer);
        menuScroll.setBorder(null);
        menuScroll.setOpaque(false);
        menuScroll.getViewport().setOpaque(false);
        sidebar.add(menuScroll, BorderLayout.CENTER);
        add(sidebar, BorderLayout.WEST);

        // ── Content ──────────────────────────────────────────────────────
        mainContentPane = new JPanel(new BorderLayout());
        mainContentPane.setBackground(BG_DARK);
        add(mainContentPane, BorderLayout.CENTER);

        // ── Create all nav buttons ────────────────────────────────────────
        btnHome              = navBtn("⌂  Home");
        btnSubscription      = navBtn("◈  My Subscription");
        btnEvents            = navBtn("◉  Events");
        btnTrainers          = navBtn("◐  Trainers");
        btnBMI               = navBtn("◎  BMI Calculator");
        btnNotifications     = navBtn("🔔  Notifications");
        btnChat              = navBtn("💬  Messages");
        btnManageUsers       = navBtn("⊕  Manage Users");
        btnManageBranches    = navBtn("⊞  Manage Branches");
        btnManageMemberships = navBtn("☰  Memberships");
        btnBranches          = navBtn("⊞  Branches");
        btnAdminNotif        = navBtn("🔔  Notifications");
        btnAdminChat         = navBtn("💬  Messages");
        btnTrainerAppts      = navBtn("◷  Appointments");
        btnManageEvents      = navBtn("◉  Manage Events");
        btnDiet              = navBtn("🥗  Diet Programs");
        btnWorkout           = navBtn("💪  Workout Programs");
        btnTrainerBMI        = navBtn("◎  BMI Calculator");
        btnTrainerNotif      = navBtn("🔔  Notifications");
        btnTrainerChat       = navBtn("💬  Messages");

        // Wire up actions
        btnHome.addActionListener(e            -> { setActive(btnHome); showHome(); });
        btnSubscription.addActionListener(e    -> { setActive(btnSubscription); showView(new MySubscriptionPanel()); });
        btnEvents.addActionListener(e          -> { setActive(btnEvents); showView(new EventsPanel()); });
        btnTrainers.addActionListener(e        -> { setActive(btnTrainers); showView(new TrainersPanel()); });
        btnBMI.addActionListener(e             -> { setActive(btnBMI); showView(new BMICalculatorPanel()); });
        btnNotifications.addActionListener(e   -> { setActive(btnNotifications); showView(new NotificationPanel()); });
        btnChat.addActionListener(e            -> { setActive(btnChat); showView(new ChatPanel()); });
        btnManageUsers.addActionListener(e     -> { setActive(btnManageUsers); showView(new ManageUsersPanel()); });
        btnManageBranches.addActionListener(e  -> { setActive(btnManageBranches); showView(new GymBranchPanel()); });
        btnManageMemberships.addActionListener(e -> { setActive(btnManageMemberships); showView(new ManageMembershipsPanel()); });
        btnBranches.addActionListener(e        -> { setActive(btnBranches); showView(new GymBranchPanel()); });
        btnAdminNotif.addActionListener(e      -> { setActive(btnAdminNotif); showView(new NotificationPanel()); });
        btnAdminChat.addActionListener(e       -> { setActive(btnAdminChat); showView(new ChatPanel()); });
        // Trainer
        btnTrainerAppts.addActionListener(e    -> { setActive(btnTrainerAppts); showView(new AppointmentsPanel()); });
        btnManageEvents.addActionListener(e    -> { setActive(btnManageEvents); showView(new EventsPanel()); });
        btnDiet.addActionListener(e            -> { setActive(btnDiet); showView(new DietProgramPanel()); });
        btnWorkout.addActionListener(e         -> { setActive(btnWorkout); showView(new WorkoutProgramPanel()); });
        btnTrainerBMI.addActionListener(e      -> { setActive(btnTrainerBMI); showView(new BMICalculatorPanel()); });
        btnTrainerNotif.addActionListener(e    -> { setActive(btnTrainerNotif); showView(new NotificationPanel()); });
        btnTrainerChat.addActionListener(e     -> { setActive(btnTrainerChat); showView(new ChatPanel()); });
        // Member programs
        btnMemberDiet    = navBtn("🥗  Diet Programs");
        btnMemberWorkout = navBtn("💪  Workout Programs");
        btnMemberAppts   = navBtn("◷  My Appointments");
        btnMemberDiet.addActionListener(e    -> { setActive(btnMemberDiet); showView(new DietProgramPanel()); });
        btnMemberWorkout.addActionListener(e -> { setActive(btnMemberWorkout); showView(new WorkoutProgramPanel()); });
        btnMemberAppts.addActionListener(e   -> { setActive(btnMemberAppts); showView(new AppointmentsPanel()); });
    }

    private JButton navBtn(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (this == activeBtn) {
                    g2.setColor(NAV_ACTIVE);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                } else if (getModel().isRollover()) {
                    g2.setColor(NAV_HOVER);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btn.setForeground(TEXT_MUTED);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setBorder(new EmptyBorder(0, 14, 0, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (btn != activeBtn) btn.setForeground(TEXT_PRIMARY);
                btn.repaint();
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (btn != activeBtn) btn.setForeground(TEXT_MUTED);
                btn.repaint();
            }
        });
        return btn;
    }

    private void setActive(JButton btn) {
        if (activeBtn != null) {
            activeBtn.setForeground(TEXT_MUTED);
            activeBtn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        }
        activeBtn = btn;
        btn.setForeground(ACCENT);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.repaint();
    }

    private void initializeData() {
        User u = SessionManager.getInstance().getCurrentUser();
        if (u != null) {
            welcomeLabel.setText(u.getName());
            roleLabel.setText(u.getRole().name());
            setupMenuForRole(u.getRole().name());
        }
        setActive(btnHome);
        showHome();
    }

    private void setupMenuForRole(String role) {
        menuContainer.removeAll();

        addSectionLabel("MAIN");
        menuContainer.add(btnHome);
        menuContainer.add(Box.createVerticalStrut(2));

        if ("ADMIN".equals(role)) {
            addSectionLabel("MANAGEMENT");
            menuContainer.add(btnManageUsers);
            menuContainer.add(Box.createVerticalStrut(2));
            menuContainer.add(btnManageBranches);
            menuContainer.add(Box.createVerticalStrut(2));
            menuContainer.add(btnManageMemberships);
            addSectionLabel("TOOLS");
            menuContainer.add(btnBMI);
            menuContainer.add(Box.createVerticalStrut(2));
            addSectionLabel("COMMUNICATION");
            menuContainer.add(btnAdminNotif);
            menuContainer.add(Box.createVerticalStrut(2));
            menuContainer.add(btnAdminChat);
        } else if ("TRAINER".equals(role)) {
            addSectionLabel("TRAINING");
            menuContainer.add(btnTrainerAppts);
            menuContainer.add(Box.createVerticalStrut(2));
            menuContainer.add(btnManageEvents);
            addSectionLabel("PROGRAMS");
            menuContainer.add(btnDiet);
            menuContainer.add(Box.createVerticalStrut(2));
            menuContainer.add(btnWorkout);
            addSectionLabel("TOOLS");
            menuContainer.add(btnTrainerBMI);
            addSectionLabel("COMMUNICATION");
            menuContainer.add(btnTrainerNotif);
            menuContainer.add(Box.createVerticalStrut(2));
            menuContainer.add(btnTrainerChat);
        } else {
            // MEMBER
            addSectionLabel("MEMBERSHIP");
            menuContainer.add(btnSubscription);
            menuContainer.add(Box.createVerticalStrut(2));
            menuContainer.add(btnEvents);
            menuContainer.add(Box.createVerticalStrut(2));
            menuContainer.add(btnTrainers);
            menuContainer.add(Box.createVerticalStrut(2));
            menuContainer.add(btnMemberAppts);
            addSectionLabel("MY PROGRAMS");
            menuContainer.add(btnMemberDiet);
            menuContainer.add(Box.createVerticalStrut(2));
            menuContainer.add(btnMemberWorkout);
            addSectionLabel("TOOLS");
            menuContainer.add(btnBMI);
            addSectionLabel("COMMUNICATION");
            menuContainer.add(btnNotifications);
            menuContainer.add(Box.createVerticalStrut(2));
            menuContainer.add(btnChat);
        }

        menuContainer.revalidate();
        menuContainer.repaint();
    }

    private void addSectionLabel(String text) {
        menuContainer.add(Box.createVerticalStrut(16));
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        lbl.setForeground(new Color(0x52525B));
        lbl.setBorder(new EmptyBorder(0, 14, 4, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        menuContainer.add(lbl);
        menuContainer.add(Box.createVerticalStrut(2));
    }

    private void showView(JPanel panel) {
        mainContentPane.removeAll();
        mainContentPane.add(panel, BorderLayout.CENTER);
        mainContentPane.revalidate();
        mainContentPane.repaint();
    }

    private void showHome() {
        User u = SessionManager.getInstance().getCurrentUser();
        if (u != null && "ADMIN".equals(u.getRole().name())) {
            showView(new AdminDashboardHomePanel());
        } else if (u != null && "TRAINER".equals(u.getRole().name())) {
            showView(new TrainerDashboardPanel());
        } else {
            showView(new DashboardHomePanel());
        }
    }

    private void showPlaceholder(String title) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BG_DARK);
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(new Color(0x1F1F23));
        box.setBorder(new EmptyBorder(40, 60, 40, 60));
        JLabel icon = new JLabel("🚧", SwingConstants.CENTER);
        icon.setFont(new Font("SansSerif", Font.PLAIN, 48));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 20));
        lbl.setForeground(TEXT_PRIMARY);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel sub = new JLabel("This section is coming soon.");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sub.setForeground(TEXT_MUTED);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        box.add(icon); box.add(Box.createVerticalStrut(16));
        box.add(lbl); box.add(Box.createVerticalStrut(8)); box.add(sub);
        p.add(box);
        showView(p);
    }

    private void handleLogout() {
        SessionManager.getInstance().logout();
        ViewManager.switchView(new LoginPanel(), "Login - Club Management");
    }
}
