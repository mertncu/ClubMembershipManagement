package com.mertncu.clubmembershipmanagement.module.message.controller;

import com.mertncu.clubmembershipmanagement.common.session.SessionManager;
import com.mertncu.clubmembershipmanagement.module.auth.dao.UserDAO;
import com.mertncu.clubmembershipmanagement.module.auth.model.User;
import com.mertncu.clubmembershipmanagement.module.message.dao.MessageDAO;
import com.mertncu.clubmembershipmanagement.module.message.model.Message;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ChatPanel extends JPanel {

    private static final Color BG_DARK      = new Color(0x18181B);
    private static final Color BG_CARD      = new Color(0x1F1F23);
    private static final Color BG_SIDEBAR   = new Color(0x111113);
    private static final Color ACCENT       = new Color(0xFF4500);
    private static final Color ACCENT_H     = new Color(0xE03D00);
    private static final Color BORDER_CLR   = new Color(0x27272A);
    private static final Color TEXT_PRIMARY  = new Color(0xF4F4F5);
    private static final Color TEXT_MUTED   = new Color(0x71717A);
    private static final Color FIELD_BG     = new Color(0x27272A);
    private static final Color BUBBLE_OUT   = new Color(0x2D1200); // sent
    private static final Color BUBBLE_IN    = new Color(0x27272A); // received

    private final MessageDAO msgDAO  = new MessageDAO();
    private final UserDAO    userDAO = new UserDAO();

    private final User me;
    private User selectedContact = null;

    private JPanel contactListPanel;
    private JPanel messagesPanel;
    private JScrollPane messagesScroll;
    private JTextField inputField;
    private JLabel chatHeaderLabel;

    public ChatPanel() {
        me = SessionManager.getInstance().getCurrentUser();
        setLayout(new BorderLayout());
        setBackground(BG_DARK);
        buildUI();
        loadContacts();
    }

    private void buildUI() {
        // Header
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(32, 36, 0, 36));
        JPanel hdr = new JPanel(); hdr.setLayout(new BoxLayout(hdr, BoxLayout.Y_AXIS)); hdr.setOpaque(false);
        JLabel title = new JLabel("Messages");
        title.setFont(new Font("SansSerif", Font.BOLD, 26)); title.setForeground(TEXT_PRIMARY);
        JLabel sub = new JLabel("Chat with other members and trainers");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13)); sub.setForeground(TEXT_MUTED);
        hdr.add(title); hdr.add(Box.createVerticalStrut(4)); hdr.add(sub);
        topBar.add(hdr, BorderLayout.WEST);
        add(topBar, BorderLayout.NORTH);

        // Body: left contacts + right chat
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(24, 36, 36, 36));

        // Left contacts
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(220, 0));
        leftPanel.setBackground(BG_CARD);
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR, 1, true),
                new EmptyBorder(0, 0, 0, 0)));

        JLabel contactsTitle = new JLabel("Contacts");
        contactsTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        contactsTitle.setForeground(TEXT_MUTED);
        contactsTitle.setBorder(new EmptyBorder(14, 16, 10, 16));
        leftPanel.add(contactsTitle, BorderLayout.NORTH);

        contactListPanel = new JPanel();
        contactListPanel.setLayout(new BoxLayout(contactListPanel, BoxLayout.Y_AXIS));
        contactListPanel.setBackground(BG_CARD);

        JScrollPane contactScroll = new JScrollPane(contactListPanel);
        contactScroll.setBorder(null);
        contactScroll.setBackground(BG_CARD);
        contactScroll.getViewport().setBackground(BG_CARD);
        leftPanel.add(contactScroll, BorderLayout.CENTER);

        // Start chat with all users button
        JButton allUsersBtn = new JButton("+ New Chat");
        allUsersBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        allUsersBtn.setForeground(ACCENT);
        allUsersBtn.setBackground(new Color(0x2D1200));
        allUsersBtn.setBorder(new EmptyBorder(10, 16, 10, 16));
        allUsersBtn.setFocusPainted(false);
        allUsersBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        allUsersBtn.addActionListener(e -> pickNewContact());
        leftPanel.add(allUsersBtn, BorderLayout.SOUTH);

        body.add(leftPanel, BorderLayout.WEST);

        // Right chat area
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(BG_DARK);
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR, 1, true),
                new EmptyBorder(0, 0, 0, 0)));

        // Chat header
        chatHeaderLabel = new JLabel("Select a contact to start chatting");
        chatHeaderLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        chatHeaderLabel.setForeground(TEXT_PRIMARY);
        chatHeaderLabel.setOpaque(true);
        chatHeaderLabel.setBackground(new Color(0x111113));
        chatHeaderLabel.setBorder(new EmptyBorder(14, 20, 14, 16));
        rightPanel.add(chatHeaderLabel, BorderLayout.NORTH);

        // Messages area
        messagesPanel = new JPanel();
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setBackground(BG_DARK);
        messagesPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        messagesScroll = new JScrollPane(messagesPanel);
        messagesScroll.setBorder(null);
        messagesScroll.setBackground(BG_DARK);
        messagesScroll.getViewport().setBackground(BG_DARK);
        rightPanel.add(messagesScroll, BorderLayout.CENTER);

        // Input bar
        JPanel inputBar = new JPanel(new BorderLayout(8, 0));
        inputBar.setBackground(BG_CARD);
        inputBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_CLR),
                new EmptyBorder(12, 16, 12, 16)));

        inputField = new JTextField();
        inputField.setBackground(FIELD_BG);
        inputField.setForeground(TEXT_PRIMARY);
        inputField.setCaretColor(ACCENT);
        inputField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR, 1, true),
                BorderFactory.createEmptyBorder(0, 14, 0, 14)));
        inputField.putClientProperty("JTextField.placeholderText", "Type a message…");
        inputField.addActionListener(e -> sendMessage());

        JButton sendBtn = new JButton("Send") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? ACCENT_H : ACCENT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        sendBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        sendBtn.setForeground(Color.WHITE);
        sendBtn.setContentAreaFilled(false);
        sendBtn.setBorderPainted(false);
        sendBtn.setFocusPainted(false);
        sendBtn.setOpaque(false);
        sendBtn.setPreferredSize(new Dimension(80, 42));
        sendBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sendBtn.addActionListener(e -> sendMessage());

        inputBar.add(inputField, BorderLayout.CENTER);
        inputBar.add(sendBtn, BorderLayout.EAST);
        rightPanel.add(inputBar, BorderLayout.SOUTH);

        body.add(rightPanel, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
    }

    private void loadContacts() {
        contactListPanel.removeAll();
        if (me == null) return;

        List<Integer> contactIds = msgDAO.findContactUserIds(me.getId());
        if (contactIds.isEmpty()) {
            JLabel empty = new JLabel("No conversations yet");
            empty.setFont(new Font("SansSerif", Font.ITALIC, 12));
            empty.setForeground(TEXT_MUTED);
            empty.setBorder(new EmptyBorder(20, 16, 0, 16));
            contactListPanel.add(empty);
        } else {
            for (int uid : contactIds) {
                userDAO.findById(uid).ifPresent(this::addContactRow);
            }
        }
        contactListPanel.revalidate();
        contactListPanel.repaint();
    }

    private void addContactRow(User contact) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        row.setCursor(new Cursor(Cursor.HAND_CURSOR));
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_CLR),
                new EmptyBorder(10, 16, 10, 16)));

        // Avatar circle
        JLabel avatar = new JLabel(String.valueOf(contact.getName().charAt(0)).toUpperCase()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ACCENT);
                g2.fillOval(0, 0, 34, 34);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setForeground(Color.WHITE);
        avatar.setFont(new Font("SansSerif", Font.BOLD, 14));
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setPreferredSize(new Dimension(34, 34));

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        info.setBorder(new EmptyBorder(0, 10, 0, 0));

        JLabel nameLbl = new JLabel(contact.getName());
        nameLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        nameLbl.setForeground(TEXT_PRIMARY);

        JLabel roleLbl = new JLabel(contact.getRole().name());
        roleLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        roleLbl.setForeground(TEXT_MUTED);

        info.add(nameLbl); info.add(roleLbl);
        row.add(avatar, BorderLayout.WEST);
        row.add(info, BorderLayout.CENTER);

        row.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) { openChat(contact); }
            public void mouseEntered(java.awt.event.MouseEvent e) { row.setBackground(new Color(0x27272A)); row.setOpaque(true); row.repaint(); }
            public void mouseExited(java.awt.event.MouseEvent e) { row.setOpaque(false); row.repaint(); }
        });

        contactListPanel.add(row);
    }

    private void openChat(User contact) {
        selectedContact = contact;
        chatHeaderLabel.setText(contact.getName() + "  — " + contact.getRole().name());
        loadMessages();
    }

    private void loadMessages() {
        if (me == null || selectedContact == null) return;
        messagesPanel.removeAll();

        List<Message> msgs = msgDAO.findConversation(me.getId(), selectedContact.getId());
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");

        for (Message m : msgs) {
            boolean isMe = m.getSenderId() == me.getId();
            messagesPanel.add(buildBubble(m.getContent(), isMe,
                    m.getSentAt() != null ? m.getSentAt().format(fmt) : ""));
            messagesPanel.add(Box.createVerticalStrut(6));
        }

        if (msgs.isEmpty()) {
            JLabel empty = new JLabel("Start the conversation!", SwingConstants.CENTER);
            empty.setFont(new Font("SansSerif", Font.ITALIC, 13));
            empty.setForeground(TEXT_MUTED);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            messagesPanel.add(Box.createVerticalStrut(60));
            messagesPanel.add(empty);
        }

        messagesPanel.revalidate();
        messagesPanel.repaint();
        // Scroll to bottom
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = messagesScroll.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private JPanel buildBubble(String text, boolean isMe, String time) {
        JPanel wrapper = new JPanel(new FlowLayout(isMe ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JPanel bubble = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isMe ? BUBBLE_OUT : BUBBLE_IN);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.dispose();
            }
        };
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
        bubble.setOpaque(false);
        bubble.setBorder(new EmptyBorder(10, 14, 8, 14));

        JLabel textLbl = new JLabel("<html><div style='width:200px;'>" + text + "</div></html>");
        textLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        textLbl.setForeground(TEXT_PRIMARY);

        JLabel timeLbl = new JLabel(time);
        timeLbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
        timeLbl.setForeground(TEXT_MUTED);
        timeLbl.setAlignmentX(isMe ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);

        bubble.add(textLbl);
        bubble.add(Box.createVerticalStrut(4));
        bubble.add(timeLbl);

        wrapper.add(bubble);
        return wrapper;
    }

    private void sendMessage() {
        if (selectedContact == null || me == null) return;
        String content = inputField.getText().trim();
        if (content.isEmpty()) return;

        Message msg = new Message(me.getId(), selectedContact.getId(), content);
        msgDAO.save(msg);
        inputField.setText("");
        loadMessages();
        loadContacts(); // refresh contact list
    }

    private void pickNewContact() {
        if (me == null) return;
        List<User> allUsers = userDAO.findAll();
        allUsers.removeIf(u -> u.getId() != null && u.getId().equals(me.getId()));

        if (allUsers.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No other users found.");
            return;
        }

        String[] names = allUsers.stream().map(u -> u.getName() + " (" + u.getRole() + ")").toArray(String[]::new);
        String selected = (String) JOptionPane.showInputDialog(this,
                "Select a user to message:", "New Chat",
                JOptionPane.PLAIN_MESSAGE, null, names, names[0]);
        if (selected == null) return;

        int idx = java.util.Arrays.asList(names).indexOf(selected);
        User contact = allUsers.get(idx);
        addContactRow(contact);
        contactListPanel.revalidate();
        contactListPanel.repaint();
        openChat(contact);
    }
}
