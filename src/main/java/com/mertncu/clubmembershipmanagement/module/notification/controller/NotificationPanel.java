package com.mertncu.clubmembershipmanagement.module.notification.controller;

import com.mertncu.clubmembershipmanagement.common.session.SessionManager;
import com.mertncu.clubmembershipmanagement.module.auth.model.User;
import com.mertncu.clubmembershipmanagement.module.notification.dao.NotificationDAO;
import com.mertncu.clubmembershipmanagement.module.notification.model.Notification;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class NotificationPanel extends JPanel {

    private static final Color BG_DARK      = new Color(0x18181B);
    private static final Color BG_CARD      = new Color(0x1F1F23);
    private static final Color ACCENT       = new Color(0xFF4500);
    private static final Color ACCENT_H     = new Color(0xE03D00);
    private static final Color BORDER_CLR   = new Color(0x27272A);
    private static final Color TEXT_PRIMARY  = new Color(0xF4F4F5);
    private static final Color TEXT_MUTED   = new Color(0x71717A);
    private static final Color FIELD_BG     = new Color(0x27272A);
    private static final Color SUCCESS      = new Color(0x22C55E);
    private static final Color WARNING      = new Color(0xF59E0B);
    private static final Color DANGER       = new Color(0xEF4444);
    private static final Color INFO         = new Color(0x38BDF8);

    private JPanel listPanel;
    private final NotificationDAO dao = new NotificationDAO();
    private boolean isAdmin = false;

    // Admin compose fields
    private JTextField titleField;
    private JTextArea  msgArea;
    private JComboBox<String> typeCombo;

    public NotificationPanel() {
        User u = SessionManager.getInstance().getCurrentUser();
        isAdmin = u != null && "ADMIN".equals(u.getRole().name());
        setLayout(new BorderLayout());
        setBackground(BG_DARK);
        buildUI();
        loadNotifications();
    }

    private void buildUI() {
        // Header
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(32, 36, 0, 36));
        JPanel hdr = new JPanel(); hdr.setLayout(new BoxLayout(hdr, BoxLayout.Y_AXIS)); hdr.setOpaque(false);
        JLabel title = new JLabel("Notifications");
        title.setFont(new Font("SansSerif", Font.BOLD, 26)); title.setForeground(TEXT_PRIMARY);
        JLabel sub = new JLabel(isAdmin ? "Send and manage system notifications" : "Your latest alerts and messages");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13)); sub.setForeground(TEXT_MUTED);
        hdr.add(title); hdr.add(Box.createVerticalStrut(4)); hdr.add(sub);

        JButton refreshBtn = new JButton("↻ Refresh");
        refreshBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        refreshBtn.setForeground(TEXT_MUTED);
        refreshBtn.setContentAreaFilled(false);
        refreshBtn.setBorderPainted(false);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> loadNotifications());
        JPanel rw = new JPanel(new GridBagLayout()); rw.setOpaque(false); rw.add(refreshBtn);

        topBar.add(hdr, BorderLayout.WEST);
        topBar.add(rw, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Body
        JPanel body = new JPanel(isAdmin ? new GridBagLayout() : new BorderLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(24, 36, 36, 36));

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        JScrollPane listScroll = new JScrollPane(listPanel);
        listScroll.setBorder(null);
        listScroll.setOpaque(false);
        listScroll.getViewport().setOpaque(false);

        if (isAdmin) {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1;
            gbc.weightx = 0.6; gbc.gridx = 0; gbc.insets = new Insets(0, 0, 0, 14);
            body.add(listScroll, gbc);
            gbc.weightx = 0.4; gbc.gridx = 1; gbc.insets = new Insets(0, 0, 0, 0);
            body.add(buildComposePanel(), gbc);
        } else {
            body.add(listScroll, BorderLayout.CENTER);
        }

        add(body, BorderLayout.CENTER);
    }

    private JPanel buildComposePanel() {
        JPanel card = baseCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel heading = new JLabel("Send Notification");
        heading.setFont(new Font("SansSerif", Font.BOLD, 16));
        heading.setForeground(TEXT_PRIMARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(heading);
        card.add(Box.createVerticalStrut(20));

        card.add(fieldLabel("Title"));
        card.add(Box.createVerticalStrut(6));
        titleField = new JTextField();
        titleField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        titleField.setBackground(FIELD_BG); titleField.setForeground(TEXT_PRIMARY);
        titleField.setCaretColor(ACCENT);
        titleField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        titleField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR, 1, true),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        titleField.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(titleField);
        card.add(Box.createVerticalStrut(14));

        card.add(fieldLabel("Message"));
        card.add(Box.createVerticalStrut(6));
        msgArea = new JTextArea(5, 20);
        msgArea.setLineWrap(true); msgArea.setWrapStyleWord(true);
        msgArea.setBackground(FIELD_BG); msgArea.setForeground(TEXT_PRIMARY);
        msgArea.setCaretColor(ACCENT); msgArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        msgArea.setBorder(new EmptyBorder(8, 10, 8, 10));
        JScrollPane msgScroll = new JScrollPane(msgArea);
        msgScroll.setBorder(BorderFactory.createLineBorder(BORDER_CLR, 1, true));
        msgScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        msgScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(msgScroll);
        card.add(Box.createVerticalStrut(14));

        card.add(fieldLabel("Type"));
        card.add(Box.createVerticalStrut(6));
        typeCombo = new JComboBox<>(new String[]{"INFO", "SUCCESS", "WARNING"});
        typeCombo.setBackground(FIELD_BG); typeCombo.setForeground(TEXT_PRIMARY);
        typeCombo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        typeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        typeCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(typeCombo);
        card.add(Box.createVerticalStrut(20));

        JButton sendBtn = accentBtn("Send to All Members");
        sendBtn.addActionListener(e -> sendNotification());
        card.add(sendBtn);

        return card;
    }

    private void sendNotification() {
        String t  = titleField.getText().trim();
        String m  = msgArea.getText().trim();
        String ty = (String) typeCombo.getSelectedItem();
        if (t.isEmpty() || m.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title and message are required.");
            return;
        }
        // userId=0 → broadcast
        dao.save(new Notification(0, t, m, ty));
        JOptionPane.showMessageDialog(this, "Notification sent to all members!", "Sent", JOptionPane.INFORMATION_MESSAGE);
        titleField.setText(""); msgArea.setText("");
        loadNotifications();
    }

    private void loadNotifications() {
        listPanel.removeAll();
        User user = SessionManager.getInstance().getCurrentUser();
        List<Notification> notes = isAdmin
                ? dao.findAll()
                : (user != null ? dao.findByUserId(user.getId()) : List.of());

        if (notes.isEmpty()) {
            JLabel empty = new JLabel("No notifications yet.");
            empty.setFont(new Font("SansSerif", Font.ITALIC, 14));
            empty.setForeground(TEXT_MUTED);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            listPanel.add(Box.createVerticalStrut(40));
            listPanel.add(empty);
        } else {
            for (Notification n : notes) listPanel.add(notifCard(n));
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel notifCard(Notification n) {
        Color typeColor = switch (n.getType()) {
            case "SUCCESS" -> SUCCESS;
            case "WARNING" -> WARNING;
            default        -> INFO;
        };

        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(n.isRead() ? BG_CARD : new Color(0x22222A));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(typeColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, 4, getHeight(), 4, 4));
                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout(12, 0));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(n.isRead() ? BORDER_CLR : typeColor, 1, true),
                new EmptyBorder(14, 20, 14, 16)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        JLabel titleL = new JLabel(n.getTitle());
        titleL.setFont(new Font("SansSerif", n.isRead() ? Font.PLAIN : Font.BOLD, 14));
        titleL.setForeground(TEXT_PRIMARY);

        JLabel msgL = new JLabel(n.getMessage().length() > 80 ? n.getMessage().substring(0, 80) + "…" : n.getMessage());
        msgL.setFont(new Font("SansSerif", Font.PLAIN, 12));
        msgL.setForeground(TEXT_MUTED);

        content.add(titleL);
        content.add(Box.createVerticalStrut(4));
        content.add(msgL);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM HH:mm");
        JLabel timeLbl = new JLabel(n.getCreatedAt() != null ? n.getCreatedAt().format(fmt) : "");
        timeLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        timeLbl.setForeground(TEXT_MUTED);

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setOpaque(false);
        right.add(timeLbl);
        if (!n.isRead() && !isAdmin) {
            JButton markBtn = new JButton("Mark Read");
            markBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
            markBtn.setForeground(typeColor);
            markBtn.setContentAreaFilled(false);
            markBtn.setBorderPainted(false);
            markBtn.setFocusPainted(false);
            markBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            markBtn.addActionListener(e -> { dao.markAsRead(n.getId()); loadNotifications(); });
            right.add(markBtn);
        }

        card.add(content, BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(0, 0, 8, 0));
        wrapper.add(card);
        return wrapper;
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private JPanel baseCard() {
        JPanel card = new JPanel() {
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
        return card;
    }

    private JLabel fieldLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        l.setForeground(TEXT_MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
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
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
