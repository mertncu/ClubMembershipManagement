package com.mertncu.clubmembershipmanagement.module.event.controller;

import com.mertncu.clubmembershipmanagement.common.session.SessionManager;
import com.mertncu.clubmembershipmanagement.module.auth.model.User;
import com.mertncu.clubmembershipmanagement.module.event.dao.EventCategoryDAO;
import com.mertncu.clubmembershipmanagement.module.event.dao.EventDAO;
import com.mertncu.clubmembershipmanagement.module.event.dao.EventRegistrationDAO;
import com.mertncu.clubmembershipmanagement.module.event.model.Event;
import com.mertncu.clubmembershipmanagement.module.event.model.EventCategory;
import com.mertncu.clubmembershipmanagement.module.event.model.EventRegistration;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EventsPanel extends JPanel {

    // ── Palette ──────────────────────────────────────────────────────────
    private static final Color BG_DARK      = new Color(0x18181B);
    private static final Color BG_CARD      = new Color(0x1F1F23);
    private static final Color ACCENT       = new Color(0xFF4500);
    private static final Color ACCENT_H     = new Color(0xE03D00);
    private static final Color BORDER_CLR   = new Color(0x27272A);
    private static final Color TEXT_PRIMARY  = new Color(0xF4F4F5);
    private static final Color TEXT_MUTED   = new Color(0x71717A);
    private static final Color SUCCESS      = new Color(0x22C55E);
    private static final Color DANGER       = new Color(0xEF4444);
    private static final Color C_PURPLE     = new Color(0x8B5CF6);
    private static final Color C_BLUE       = new Color(0x0EA5E9);
    private static final Color C_AMBER      = new Color(0xF59E0B);
    // ─────────────────────────────────────────────────────────────────────

    private final EventDAO              eventDAO       = new EventDAO();
    private final EventCategoryDAO      catDAO         = new EventCategoryDAO();
    private final EventRegistrationDAO  regDAO         = new EventRegistrationDAO();

    private JPanel eventsGrid;
    private JComboBox<String> categoryFilter;
    private List<EventCategory> categories;
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy  HH:mm");

    public EventsPanel() {
        setLayout(new BorderLayout());
        setBackground(BG_DARK);
        buildUI();
        loadEvents(-1);
    }

    private void buildUI() {
        // ── Header bar ───────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(32, 36, 0, 36));

        JPanel hdr = new JPanel(); hdr.setLayout(new BoxLayout(hdr, BoxLayout.Y_AXIS)); hdr.setOpaque(false);
        JLabel title = new JLabel("Events");
        title.setFont(new Font("SansSerif", Font.BOLD, 26)); title.setForeground(TEXT_PRIMARY);
        JLabel sub = new JLabel("Browse and register for upcoming events");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13)); sub.setForeground(TEXT_MUTED);
        hdr.add(title); hdr.add(Box.createVerticalStrut(4)); hdr.add(sub);
        topBar.add(hdr, BorderLayout.WEST);

        // Category filter
        categories = catDAO.findAll();
        String[] catNames = new String[categories.size() + 1];
        catNames[0] = "All Categories";
        for (int i = 0; i < categories.size(); i++) catNames[i + 1] = categories.get(i).getName();
        categoryFilter = new JComboBox<>(catNames);
        categoryFilter.setBackground(new Color(0x27272A));
        categoryFilter.setForeground(TEXT_PRIMARY);
        categoryFilter.setFont(new Font("SansSerif", Font.PLAIN, 13));
        categoryFilter.setPreferredSize(new Dimension(200, 38));
        categoryFilter.addActionListener(e -> {
            int idx = categoryFilter.getSelectedIndex();
            loadEvents(idx == 0 ? -1 : categories.get(idx - 1).getId());
        });
        JPanel filterWrap = new JPanel(new GridBagLayout()); filterWrap.setOpaque(false);
        filterWrap.add(categoryFilter);
        topBar.add(filterWrap, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // ── Events grid ──────────────────────────────────────────────────
        eventsGrid = new JPanel(new WrapLayout(FlowLayout.LEFT, 16, 16));
        eventsGrid.setOpaque(false);
        eventsGrid.setBorder(new EmptyBorder(24, 28, 36, 28));

        JScrollPane scroll = new JScrollPane(eventsGrid);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        add(scroll, BorderLayout.CENTER);
    }

    private void loadEvents(int categoryId) {
        eventsGrid.removeAll();
        User user = SessionManager.getInstance().getCurrentUser();

        List<Event> events = categoryId < 0
                ? eventDAO.findUpcomingEvents()
                : eventDAO.findAll().stream()
                          .filter(e -> e.getCategoryId() == categoryId)
                          .toList();

        if (events.isEmpty()) {
            JLabel empty = new JLabel("No events found.", SwingConstants.CENTER);
            empty.setFont(new Font("SansSerif", Font.ITALIC, 14));
            empty.setForeground(TEXT_MUTED);
            eventsGrid.add(empty);
        } else {
            Color[] palette = {ACCENT, C_PURPLE, C_BLUE, C_AMBER, SUCCESS};
            for (int i = 0; i < events.size(); i++) {
                eventsGrid.add(buildEventCard(events.get(i), palette[i % palette.length], user));
            }
        }
        eventsGrid.revalidate();
        eventsGrid.repaint();
    }

    private JPanel buildEventCard(Event evt, Color accent, User user) {
        int registered = regDAO.getRegistrationCountForEvent(evt.getId());
        int remaining  = evt.getQuota() - registered;
        boolean isFull = remaining <= 0;
        boolean isRegistered = user != null && regDAO.isUserRegisteredForEvent(user.getId(), evt.getId());

        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                // top accent stripe
                g2.setColor(accent);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), 4, 4, 4));
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(280, 230));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(isRegistered ? accent : BORDER_CLR, 1, true),
                new EmptyBorder(20, 20, 20, 20)));

        // Category chip
        categories.stream().filter(c -> c.getId() == evt.getCategoryId()).findFirst().ifPresent(c -> {
            JLabel chip = new JLabel(c.getName());
            chip.setFont(new Font("SansSerif", Font.BOLD, 9));
            chip.setForeground(accent);
            chip.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 80), 1, true),
                    BorderFactory.createEmptyBorder(2, 8, 2, 8)));
            card.add(chip);
            card.add(Box.createVerticalStrut(10));
        });

        JLabel nameLbl = new JLabel("<html><div style='width:230px'>" + evt.getTitle() + "</div></html>");
        nameLbl.setFont(new Font("SansSerif", Font.BOLD, 15));
        nameLbl.setForeground(TEXT_PRIMARY);
        card.add(nameLbl);
        card.add(Box.createVerticalStrut(8));

        JLabel dateLbl = new JLabel("📅  " + evt.getEventDate().format(DT_FMT));
        dateLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        dateLbl.setForeground(TEXT_MUTED);
        card.add(dateLbl);
        card.add(Box.createVerticalStrut(4));

        // Quota bar
        JLabel quotaLbl = new JLabel(registered + " / " + evt.getQuota() + " registered");
        quotaLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        quotaLbl.setForeground(isFull ? DANGER : TEXT_MUTED);
        card.add(quotaLbl);

        JProgressBar bar = new JProgressBar(0, evt.getQuota());
        bar.setValue(registered);
        bar.setBackground(new Color(0x27272A));
        bar.setForeground(isFull ? DANGER : accent);
        bar.setBorder(null);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 4));
        card.add(Box.createVerticalStrut(6));
        card.add(bar);
        card.add(Box.createVerticalGlue());
        card.add(Box.createVerticalStrut(14));

        // Action button
        JButton btn;
        if (isRegistered) {
            btn = buildBtn("✓  Registered", new Color(0x052E16), new Color(0x052E16));
            btn.setForeground(SUCCESS);
            btn.addActionListener(e -> {
                int c = JOptionPane.showConfirmDialog(this, "Cancel your registration for this event?",
                        "Confirm", JOptionPane.YES_NO_OPTION);
                if (c == JOptionPane.YES_OPTION && user != null) {
                    regDAO.deleteByUserAndEvent(user.getId(), evt.getId());
                    loadEvents(categoryFilter.getSelectedIndex() == 0 ? -1
                            : categories.get(categoryFilter.getSelectedIndex() - 1).getId());
                }
            });
        } else if (isFull) {
            btn = buildBtn("Event Full", new Color(0x27272A), new Color(0x27272A));
            btn.setForeground(TEXT_MUTED);
            btn.setEnabled(false);
        } else {
            btn = buildBtn("Register Now", ACCENT, ACCENT_H);
            btn.setForeground(Color.WHITE);
            btn.addActionListener(e -> {
                if (user == null) return;
                int c = JOptionPane.showConfirmDialog(this,
                        "Register for \"" + evt.getTitle() + "\"?",
                        "Confirm Registration", JOptionPane.OK_CANCEL_OPTION);
                if (c == JOptionPane.OK_OPTION) {
                    regDAO.save(new EventRegistration(evt.getId(), user.getId()));
                    JOptionPane.showMessageDialog(this, "Registered successfully!", "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadEvents(categoryFilter.getSelectedIndex() == 0 ? -1
                            : categories.get(categoryFilter.getSelectedIndex() - 1).getId());
                }
            });
        }
        card.add(btn);
        return card;
    }

    private JButton buildBtn(String text, Color color, Color hover) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? hover : color);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── WrapLayout (flow-wrap) ────────────────────────────────────────────
    /** A FlowLayout variant that wraps to the next line when the width is exceeded. */
    static class WrapLayout extends FlowLayout {
        WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }
        @Override
        public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }
        @Override
        public Dimension minimumLayoutSize(Container target) {
            Dimension minimum = layoutSize(target, false);
            minimum.width -= (getHgap() + 1);
            return minimum;
        }
        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getSize().width;
                if (targetWidth == 0) targetWidth = Integer.MAX_VALUE;
                int hgap = getHgap(), vgap = getVgap();
                Insets insets = target.getInsets();
                int horizontalInsetsAndGap = insets.left + insets.right + hgap * 2;
                int maxWidth = targetWidth - horizontalInsetsAndGap;

                Dimension dim = new Dimension(0, 0);
                int rowWidth = 0, rowHeight = 0;
                int nmembers = target.getComponentCount();
                for (int i = 0; i < nmembers; i++) {
                    Component m = target.getComponent(i);
                    if (m.isVisible()) {
                        Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                        if (rowWidth + d.width > maxWidth) {
                            dim.width = Math.max(dim.width, rowWidth);
                            dim.height += rowHeight + vgap;
                            rowWidth = 0; rowHeight = 0;
                        }
                        rowWidth += d.width + hgap;
                        rowHeight = Math.max(rowHeight, d.height);
                    }
                }
                dim.width = Math.max(dim.width, rowWidth);
                dim.height += rowHeight + vgap * 2;
                dim.width += horizontalInsetsAndGap;
                dim.height += insets.top + insets.bottom;
                return dim;
            }
        }
    }
}
