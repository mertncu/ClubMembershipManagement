package com.mertncu.clubmembershipmanagement.module.workout.controller;

import com.mertncu.clubmembershipmanagement.common.session.SessionManager;
import com.mertncu.clubmembershipmanagement.module.auth.model.User;
import com.mertncu.clubmembershipmanagement.module.workout.dao.WorkoutExerciseDAO;
import com.mertncu.clubmembershipmanagement.module.workout.dao.WorkoutProgramDAO;
import com.mertncu.clubmembershipmanagement.module.workout.model.WorkoutExercise;
import com.mertncu.clubmembershipmanagement.module.workout.model.WorkoutProgram;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Dual-role panel:
 *  - MEMBER  → view assigned workout program grouped by day
 *  - TRAINER → create programs & add exercises
 */
public class WorkoutProgramPanel extends JPanel {

    private static final Color BG_DARK     = new Color(0x18181B);
    private static final Color BG_CARD     = new Color(0x1F1F23);
    private static final Color ACCENT      = new Color(0xFF4500);
    private static final Color ACCENT_H    = new Color(0xE03D00);
    private static final Color BORDER_CLR  = new Color(0x27272A);
    private static final Color TEXT_PRIMARY = new Color(0xF4F4F5);
    private static final Color TEXT_MUTED  = new Color(0x71717A);
    private static final Color FIELD_BG    = new Color(0x27272A);
    private static final Color SUCCESS     = new Color(0x22C55E);
    private static final Color C_PURPLE    = new Color(0x8B5CF6);
    private static final Color C_BLUE      = new Color(0x0EA5E9);
    private static final Color C_AMBER     = new Color(0xF59E0B);

    private static final String[] DAYS = {"ALL","MON","TUE","WED","THU","FRI","SAT","SUN"};
    private static final Color[] DAY_COLORS = {ACCENT, SUCCESS, C_BLUE, C_PURPLE, C_AMBER,
            new Color(0xEC4899), new Color(0x14B8A6), new Color(0xF97316)};

    private final WorkoutProgramDAO progDAO  = new WorkoutProgramDAO();
    private final WorkoutExerciseDAO exDAO   = new WorkoutExerciseDAO();
    private final boolean isTrainer;

    private JPanel contentArea;

    public WorkoutProgramPanel() {
        User u = SessionManager.getInstance().getCurrentUser();
        isTrainer = u != null && "TRAINER".equals(u.getRole().name());
        setLayout(new BorderLayout());
        setBackground(BG_DARK);
        buildUI();
    }

    private void buildUI() {
        // Header
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(32, 36, 0, 36));
        JPanel hdr = new JPanel(); hdr.setLayout(new BoxLayout(hdr, BoxLayout.Y_AXIS)); hdr.setOpaque(false);
        JLabel title = new JLabel(isTrainer ? "Manage Workout Programs" : "My Workout Program");
        title.setFont(new Font("SansSerif", Font.BOLD, 26)); title.setForeground(TEXT_PRIMARY);
        JLabel sub = new JLabel(isTrainer ? "Design personalised training routines for your members"
                                          : "Follow your personalised workout plan");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13)); sub.setForeground(TEXT_MUTED);
        hdr.add(title); hdr.add(Box.createVerticalStrut(4)); hdr.add(sub);
        topBar.add(hdr, BorderLayout.WEST);

        if (isTrainer) {
            JButton newBtn = accentBtn("+ New Program");
            newBtn.addActionListener(e -> showCreateDialog());
            JPanel bw = new JPanel(new GridBagLayout()); bw.setOpaque(false); bw.add(newBtn);
            topBar.add(bw, BorderLayout.EAST);
        }
        add(topBar, BorderLayout.NORTH);

        contentArea = new JPanel();
        contentArea.setLayout(new BoxLayout(contentArea, BoxLayout.Y_AXIS));
        contentArea.setOpaque(false);
        contentArea.setBorder(new EmptyBorder(24, 36, 36, 36));

        JScrollPane scroll = new JScrollPane(contentArea);
        scroll.setBorder(null); scroll.setOpaque(false); scroll.getViewport().setOpaque(false);
        add(scroll, BorderLayout.CENTER);

        loadContent();
    }

    private void loadContent() {
        contentArea.removeAll();
        User me = SessionManager.getInstance().getCurrentUser();
        if (me == null) return;

        List<WorkoutProgram> progs = isTrainer
                ? progDAO.findByTrainerId(me.getId())
                : progDAO.findByUserId(me.getId());

        if (progs.isEmpty()) {
            JLabel empty = new JLabel(isTrainer
                    ? "No workout programs yet. Click '+ New Program' to create one."
                    : "No workout program has been assigned to you yet.");
            empty.setFont(new Font("SansSerif", Font.ITALIC, 14));
            empty.setForeground(TEXT_MUTED);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentArea.add(Box.createVerticalStrut(40));
            contentArea.add(empty);
        } else {
            for (WorkoutProgram prog : progs) {
                contentArea.add(buildProgramCard(prog));
                contentArea.add(Box.createVerticalStrut(24));
            }
        }
        contentArea.revalidate(); contentArea.repaint();
    }

    private JPanel buildProgramCard(WorkoutProgram prog) {
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
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR, 1, true),
                new EmptyBorder(28, 36, 28, 36)));

        // Program header row
        JPanel progHdr = new JPanel(new BorderLayout());
        progHdr.setOpaque(false); progHdr.setAlignmentX(Component.LEFT_ALIGNMENT);
        progHdr.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel nameCol = new JPanel(); nameCol.setLayout(new BoxLayout(nameCol, BoxLayout.Y_AXIS)); nameCol.setOpaque(false);
        JLabel nameLbl = new JLabel(prog.getName());
        nameLbl.setFont(new Font("SansSerif", Font.BOLD, 18)); nameLbl.setForeground(TEXT_PRIMARY);

        Color levelColor = switch (prog.getLevel() != null ? prog.getLevel() : "") {
            case "ADVANCED"     -> DANGER();
            case "INTERMEDIATE" -> C_AMBER;
            default             -> SUCCESS;
        };
        
        JPanel metaRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        metaRow.setOpaque(false);
        metaRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel metaLbl = new JLabel(prog.getGoal() + "  ·  " + prog.getDurationWeeks() + " weeks  ");
        metaLbl.setFont(new Font("SansSerif", Font.PLAIN, 13)); 
        metaLbl.setForeground(TEXT_MUTED);
        
        metaRow.add(metaLbl);
        metaRow.add(badge(prog.getLevel() != null ? prog.getLevel() : "BEGINNER", levelColor));
        
        nameCol.add(nameLbl); nameCol.add(Box.createVerticalStrut(6)); nameCol.add(metaRow);
        progHdr.add(nameCol, BorderLayout.WEST);

        if (isTrainer) {
            JButton addExBtn = new JButton("+ Add Exercise");
            addExBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
            addExBtn.setForeground(TEXT_PRIMARY); 
            addExBtn.setBackground(new Color(0x3F3F46));
            addExBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            addExBtn.setFocusPainted(false);
            addExBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            addExBtn.addActionListener(e -> showAddExerciseDialog(prog));
            JPanel bw = new JPanel(new GridBagLayout()); bw.setOpaque(false); bw.add(addExBtn);
            progHdr.add(bw, BorderLayout.EAST);
        }
        card.add(progHdr);
        card.add(Box.createVerticalStrut(20));

        // Exercises grouped by day
        List<WorkoutExercise> exercises = exDAO.findByProgramId(prog.getId());
        boolean anyAdded = false;
        for (int di = 0; di < DAYS.length; di++) {
            final int idx = di;
            List<WorkoutExercise> group = exercises.stream()
                    .filter(ex -> DAYS[idx].equals(ex.getDayOfWeek())).toList();
            if (!group.isEmpty()) {
                card.add(buildDayGroup(DAYS[idx], group, DAY_COLORS[idx]));
                card.add(Box.createVerticalStrut(14));
                anyAdded = true;
            }
        }
        if (!anyAdded) {
            JLabel noEx = new JLabel("No exercises added yet.");
            noEx.setFont(new Font("SansSerif", Font.ITALIC, 12));
            noEx.setForeground(TEXT_MUTED); noEx.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(noEx);
        }
        return card;
    }

    private JPanel buildDayGroup(String day, List<WorkoutExercise> exercises, Color color) {
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setOpaque(false); group.setAlignmentX(Component.LEFT_ALIGNMENT);
        group.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel dayLbl = new JLabel(day);
        dayLbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        dayLbl.setForeground(color); dayLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        group.add(dayLbl);
        group.add(Box.createVerticalStrut(6));

        String[] cols = {"Exercise", "Muscle", "Sets", "Reps", "Weight(kg)", "Rest(s)"};
        Object[][] data = new Object[exercises.size()][cols.length];
        for (int i = 0; i < exercises.size(); i++) {
            WorkoutExercise ex = exercises.get(i);
            data[i] = new Object[]{ex.getName(), ex.getMuscleGroup(),
                    ex.getSets(), ex.getReps(),
                    ex.getWeightKg() == 0 ? "BW" : ex.getWeightKg(),
                    ex.getRestSeconds()};
        }
        JTable t = new JTable(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        t.setBackground(BG_CARD); t.setForeground(TEXT_PRIMARY);
        t.setFont(new Font("SansSerif", Font.PLAIN, 13)); t.setRowHeight(48);
        t.setGridColor(BORDER_CLR);
        t.getTableHeader().setBackground(new Color(0x18181B));
        t.getTableHeader().setForeground(TEXT_MUTED);
        t.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));
        t.getTableHeader().setPreferredSize(new Dimension(0, 42));
        t.setShowHorizontalLines(true); t.setShowVerticalLines(false); t.setBorder(null);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(new Color(0x27272A));
        t.setSelectionForeground(TEXT_PRIMARY);

        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(null);
        sp.setBackground(BG_CARD); sp.getViewport().setBackground(BG_CARD);
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, t.getRowHeight() * exercises.size() + 42));
        group.add(sp);
        return group;
    }

    // ── Dialogs ──────────────────────────────────────────────────────────

    private void showCreateDialog() {
        User me = SessionManager.getInstance().getCurrentUser();
        if (me == null) return;

        JTextField nameField  = field(); JTextField goalField  = field();
        JTextField weeksField = field("4");
        JComboBox<String> levelCombo = new JComboBox<>(new String[]{"BEGINNER","INTERMEDIATE","ADVANCED"});
        levelCombo.setBackground(FIELD_BG); levelCombo.setForeground(TEXT_PRIMARY);
        com.mertncu.clubmembershipmanagement.module.auth.dao.UserDAO userDAO = new com.mertncu.clubmembershipmanagement.module.auth.dao.UserDAO();
        java.util.List<User> members = userDAO.findAll().stream()
                .filter(u -> "MEMBER".equalsIgnoreCase(u.getRole().name())).toList();
        
        JComboBox<UserComboItem> memberCombo = new JComboBox<>();
        for (User u : members) {
            memberCombo.addItem(new UserComboItem(u));
        }
        memberCombo.setBackground(FIELD_BG);
        memberCombo.setForeground(TEXT_PRIMARY);
        JTextArea  noteArea = new JTextArea(3, 20);
        noteArea.setBackground(FIELD_BG); noteArea.setForeground(TEXT_PRIMARY); noteArea.setCaretColor(ACCENT);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(BG_CARD);
        form.setBorder(new EmptyBorder(16, 24, 16, 24));

        JLabel title = new JLabel("Create New Workout Plan");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(title);
        form.add(Box.createVerticalStrut(24));

        form.add(createFormRow("Program Name:", nameField));
        
        JPanel row2 = new JPanel(new GridLayout(1, 2, 16, 0));
        row2.setOpaque(false); row2.setAlignmentX(Component.LEFT_ALIGNMENT);
        row2.add(createFormRow("Goal:", goalField));
        row2.add(createFormRow("Level:", levelCombo));
        form.add(row2);
        
        JPanel row3 = new JPanel(new GridLayout(1, 2, 16, 0));
        row3.setOpaque(false); row3.setAlignmentX(Component.LEFT_ALIGNMENT);
        row3.add(createFormRow("Duration (weeks):", weeksField));
        row3.add(createFormRow("Assign to Member:", memberCombo));
        form.add(row3);
        
        form.add(createFormRow("Notes:", new JScrollPane(noteArea)));

        int res = JOptionPane.showConfirmDialog(this, form, "New Workout Program",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            try {
                UserComboItem selectedMember = (UserComboItem) memberCombo.getSelectedItem();
                int uid = selectedMember != null ? selectedMember.getId() : -1;
                int weeks = Integer.parseInt(weeksField.getText().trim());
                WorkoutProgram wp = new WorkoutProgram(uid, me.getId(),
                        nameField.getText().trim(), goalField.getText().trim(),
                        (String) levelCombo.getSelectedItem(), weeks, null, noteArea.getText().trim());
                progDAO.save(wp);
                loadContent();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid values.");
            }
        }
    }

    private void showAddExerciseDialog(WorkoutProgram prog) {
        JTextField exName = field(); JTextField muscleField = field();
        JTextField setsField = field("3"); JTextField repsField = field("12");
        JTextField weightField = field("0"); JTextField restField = field("60");
        JComboBox<String> dayCombo = new JComboBox<>(DAYS);
        dayCombo.setBackground(FIELD_BG); dayCombo.setForeground(TEXT_PRIMARY);
        JTextField notesField = field();

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(BG_CARD);
        form.setBorder(new EmptyBorder(16, 24, 16, 24));

        JLabel title = new JLabel("Add Exercise");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(title);
        form.add(Box.createVerticalStrut(24));

        form.add(createFormRow("Exercise Name:", exName));
        
        JPanel row2 = new JPanel(new GridLayout(1, 2, 16, 0));
        row2.setOpaque(false); row2.setAlignmentX(Component.LEFT_ALIGNMENT);
        row2.add(createFormRow("Muscle Group:", muscleField));
        row2.add(createFormRow("Day:", dayCombo));
        form.add(row2);
        
        JPanel row3 = new JPanel(new GridLayout(1, 4, 12, 0));
        row3.setOpaque(false); row3.setAlignmentX(Component.LEFT_ALIGNMENT);
        row3.add(createFormRow("Sets:", setsField));
        row3.add(createFormRow("Reps:", repsField));
        row3.add(createFormRow("Weight (kg):", weightField));
        row3.add(createFormRow("Rest (s):", restField));
        form.add(row3);
        
        form.add(createFormRow("Notes:", notesField));

        int res = JOptionPane.showConfirmDialog(this, form, "Add Exercise",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            try {
                WorkoutExercise ex = new WorkoutExercise(prog.getId(),
                        exName.getText().trim(), muscleField.getText().trim(),
                        Integer.parseInt(setsField.getText().trim()),
                        Integer.parseInt(repsField.getText().trim()),
                        Double.parseDouble(weightField.getText().trim()),
                        Integer.parseInt(restField.getText().trim()),
                        (String) dayCombo.getSelectedItem(),
                        notesField.getText().trim());
                exDAO.save(ex);
                loadContent();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter valid numeric values.");
            }
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────
    private Color DANGER() { return new Color(0xEF4444); }
    private JTextField field() { return field(""); }
    private JTextField field(String ph) {
        JTextField tf = new JTextField();
        tf.setBackground(FIELD_BG); tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(ACCENT); tf.setFont(new Font("SansSerif", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR, 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        if (!ph.isEmpty()) tf.putClientProperty("JTextField.placeholderText", ph);
        return tf;
    }

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
        fieldComp.setMaximumSize(new Dimension(800, fieldComp instanceof JScrollPane ? 120 : 42));
        
        row.add(lbl);
        row.add(Box.createVerticalStrut(6));
        row.add(fieldComp);
        row.add(Box.createVerticalStrut(14));
        return row;
    }

    private JPanel badge(String text, Color bg) {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
            }
        };
        p.setLayout(new GridBagLayout());
        p.setOpaque(false);
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        l.setForeground(Color.WHITE);
        p.add(l);
        p.setBorder(new EmptyBorder(4, 8, 4, 8));
        p.setMaximumSize(p.getPreferredSize());
        p.setAlignmentY(Component.CENTER_ALIGNMENT);
        return p;
    }

    private static class UserComboItem {
        private final User user;
        public UserComboItem(User user) { this.user = user; }
        public int getId() { return user.getId(); }
        @Override public String toString() { return user.getName() + " (" + user.getEmail() + ")"; }
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
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setFocusPainted(false); btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(140, 38));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
