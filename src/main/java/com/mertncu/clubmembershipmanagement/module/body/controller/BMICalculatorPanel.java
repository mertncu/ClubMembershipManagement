package com.mertncu.clubmembershipmanagement.module.body.controller;

import com.mertncu.clubmembershipmanagement.common.session.SessionManager;
import com.mertncu.clubmembershipmanagement.module.auth.model.User;
import com.mertncu.clubmembershipmanagement.module.body.dao.BodyMeasurementDAO;
import com.mertncu.clubmembershipmanagement.module.body.model.BodyMeasurement;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class BMICalculatorPanel extends JPanel {

    private static final Color BG_DARK      = new Color(0x18181B);
    private static final Color BG_CARD      = new Color(0x1F1F23);
    private static final Color ACCENT       = new Color(0xFF4500);
    private static final Color ACCENT_H     = new Color(0xE03D00);
    private static final Color BORDER_CLR   = new Color(0x27272A);
    private static final Color TEXT_PRIMARY  = new Color(0xF4F4F5);
    private static final Color TEXT_MUTED   = new Color(0x71717A);
    private static final Color FIELD_BG     = new Color(0x27272A);

    // BMI range colours
    private static final Color C_UNDER  = new Color(0x38BDF8); // blue
    private static final Color C_NORMAL = new Color(0x22C55E); // green
    private static final Color C_OVER   = new Color(0xF59E0B); // amber
    private static final Color C_OBESE  = new Color(0xEF4444); // red

    private JTextField weightField, heightField, ageField;
    private JComboBox<String> genderCombo;

    // Result display
    private JLabel bmiValueLbl, bmiCategoryLbl, bodyFatLbl;
    private GaugePanel gaugePanel;

    private final BodyMeasurementDAO dao = new BodyMeasurementDAO();

    public BMICalculatorPanel() {
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
        JLabel title = new JLabel("BMI & Body Analysis");
        title.setFont(new Font("SansSerif", Font.BOLD, 26)); title.setForeground(TEXT_PRIMARY);
        JLabel sub = new JLabel("Calculate your Body Mass Index and body fat percentage");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13)); sub.setForeground(TEXT_MUTED);
        hdr.add(title); hdr.add(Box.createVerticalStrut(4)); hdr.add(sub);
        topBar.add(hdr, BorderLayout.WEST);
        add(topBar, BorderLayout.NORTH);

        // Body: 2-column
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(24, 36, 36, 36));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1;

        gbc.weightx = 0.45; gbc.gridx = 0; gbc.insets = new Insets(0, 0, 0, 14);
        body.add(buildInputCard(), gbc);

        gbc.weightx = 0.55; gbc.gridx = 1; gbc.insets = new Insets(0, 0, 0, 0);
        body.add(buildResultCard(), gbc);

        add(body, BorderLayout.CENTER);
    }

    private JPanel buildInputCard() {
        JPanel card = baseCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(28, 28, 28, 28));

        addFormLabel(card, "Weight (kg)");
        weightField = addFormField(card, "e.g. 75");

        addFormLabel(card, "Height (cm)");
        heightField = addFormField(card, "e.g. 175");

        addFormLabel(card, "Age");
        ageField = addFormField(card, "e.g. 25");

        addFormLabel(card, "Gender");
        genderCombo = new JComboBox<>(new String[]{"MALE", "FEMALE"});
        genderCombo.setBackground(FIELD_BG); genderCombo.setForeground(TEXT_PRIMARY);
        genderCombo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        genderCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        genderCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(genderCombo);
        card.add(Box.createVerticalStrut(24));

        JButton calcBtn = accentBtn("Calculate", ACCENT, ACCENT_H);
        calcBtn.addActionListener(e -> calculate());
        card.add(calcBtn);
        card.add(Box.createVerticalStrut(10));

        JButton saveBtn = accentBtn("Save Result", new Color(0x27272A), new Color(0x3F3F46));
        saveBtn.setForeground(TEXT_MUTED);
        saveBtn.addActionListener(e -> saveResult());
        card.add(saveBtn);

        return card;
    }

    private JPanel buildResultCard() {
        JPanel card = baseCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(28, 28, 28, 28));

        JLabel rlbl = new JLabel("Your Results");
        rlbl.setFont(new Font("SansSerif", Font.BOLD, 16));
        rlbl.setForeground(TEXT_PRIMARY);
        rlbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(rlbl);
        card.add(Box.createVerticalStrut(20));

        // Gauge
        gaugePanel = new GaugePanel();
        gaugePanel.setPreferredSize(new Dimension(200, 110));
        gaugePanel.setMaximumSize(new Dimension(300, 130));
        gaugePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(gaugePanel);
        card.add(Box.createVerticalStrut(12));

        bmiValueLbl = new JLabel("—");
        bmiValueLbl.setFont(new Font("SansSerif", Font.BOLD, 48));
        bmiValueLbl.setForeground(TEXT_PRIMARY);
        bmiValueLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(bmiValueLbl);

        bmiCategoryLbl = new JLabel("Enter your data and press Calculate");
        bmiCategoryLbl.setFont(new Font("SansSerif", Font.BOLD, 15));
        bmiCategoryLbl.setForeground(TEXT_MUTED);
        bmiCategoryLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(bmiCategoryLbl);

        card.add(Box.createVerticalStrut(24));
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_CLR);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(sep);
        card.add(Box.createVerticalStrut(20));

        bodyFatLbl = resultRow(card, "Body Fat %", "—");
        card.add(Box.createVerticalStrut(12));

        // BMI legend
        JPanel legend = new JPanel(new GridLayout(2, 2, 8, 8));
        legend.setOpaque(false);
        legend.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        legend.setAlignmentX(Component.CENTER_ALIGNMENT);
        legend.add(legendChip("Underweight", "< 18.5", C_UNDER));
        legend.add(legendChip("Normal",      "18.5–24.9", C_NORMAL));
        legend.add(legendChip("Overweight",  "25–29.9", C_OVER));
        legend.add(legendChip("Obese",       "≥ 30", C_OBESE));
        card.add(legend);

        return card;
    }

    private void calculate() {
        try {
            double w = Double.parseDouble(weightField.getText().trim());
            double h = Double.parseDouble(heightField.getText().trim());
            int    a = Integer.parseInt(ageField.getText().trim());
            String g = (String) genderCombo.getSelectedItem();

            BodyMeasurement m = new BodyMeasurement(0, w, h, a, g);
            double bmi = m.calcBMI();
            double bf  = m.calcBodyFat();

            bmiValueLbl.setText(String.format("%.1f", bmi));
            String cat = BodyMeasurement.bmiCategory(bmi);
            bmiCategoryLbl.setText(cat);
            bmiCategoryLbl.setForeground(bmiColor(bmi));
            bmiValueLbl.setForeground(bmiColor(bmi));
            bodyFatLbl.setText(String.format("%.1f%%", bf));
            gaugePanel.setBmi(bmi);
            gaugePanel.repaint();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric values.", "Input Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void saveResult() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) { JOptionPane.showMessageDialog(this, "You must be logged in to save."); return; }
        try {
            double w = Double.parseDouble(weightField.getText().trim());
            double h = Double.parseDouble(heightField.getText().trim());
            int    a = Integer.parseInt(ageField.getText().trim());
            String g = (String) genderCombo.getSelectedItem();
            BodyMeasurement m = new BodyMeasurement(user.getId(), w, h, a, g);
            dao.save(m);
            JOptionPane.showMessageDialog(this, "Measurement saved successfully!", "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please calculate first before saving.");
        }
    }

    // ── Inner gauge ───────────────────────────────────────────────────────
    private static class GaugePanel extends JPanel {
        private double bmi = 0;

        void setBmi(double v) { this.bmi = v; }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            setOpaque(false);

            int w = getWidth(), h = getHeight();
            int cx = w / 2, cy = h - 10;
            int r = Math.min(cx, cy) - 10;

            // Background arc
            g2.setStroke(new BasicStroke(14, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(0x27272A));
            g2.draw(new Arc2D.Double(cx - r, cy - r, r * 2, r * 2, 0, 180, Arc2D.OPEN));

            // Coloured segments
            Color[] colors = {C_UNDER, C_NORMAL, C_OVER, C_OBESE};
            double[] starts = {0, 45, 90, 135};
            double[] spans  = {45, 45, 45, 45};
            for (int i = 0; i < 4; i++) {
                g2.setColor(colors[i]);
                g2.draw(new Arc2D.Double(cx - r, cy - r, r * 2, r * 2, starts[i], spans[i], Arc2D.OPEN));
            }

            // Needle
            if (bmi > 0) {
                double clamped = Math.min(Math.max(bmi, 10), 40);
                double angle   = ((clamped - 10) / 30.0) * 180.0; // 10–40 -> 0–180 degrees
                double rad     = Math.toRadians(180 - angle);
                int nx = (int) (cx + (r - 18) * Math.cos(rad));
                int ny = (int) (cy - (r - 18) * Math.sin(rad));
                g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(new Color(0xF4F4F5));
                g2.drawLine(cx, cy, nx, ny);
                g2.setColor(new Color(0xFF4500));
                g2.fillOval(cx - 5, cy - 5, 10, 10);
            }
            g2.dispose();
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private Color bmiColor(double bmi) {
        if (bmi < 18.5) return C_UNDER;
        if (bmi < 25)   return C_NORMAL;
        if (bmi < 30)   return C_OVER;
        return C_OBESE;
    }

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

    private void addFormLabel(JPanel parent, String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        lbl.setForeground(TEXT_MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(lbl);
        parent.add(Box.createVerticalStrut(4));
    }

    private JTextField addFormField(JPanel parent, String placeholder) {
        JTextField tf = new JTextField();
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        tf.setBackground(FIELD_BG); tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(ACCENT);
        tf.setFont(new Font("SansSerif", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR, 1, true),
                BorderFactory.createEmptyBorder(0, 12, 0, 12)));
        tf.putClientProperty("JTextField.placeholderText", placeholder);
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(tf);
        parent.add(Box.createVerticalStrut(12));
        return tf;
    }

    private JLabel resultRow(JPanel parent, String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        row.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel k = new JLabel(label);
        k.setFont(new Font("SansSerif", Font.PLAIN, 13)); k.setForeground(TEXT_MUTED);
        JLabel v = new JLabel(value);
        v.setFont(new Font("SansSerif", Font.BOLD, 14)); v.setForeground(TEXT_PRIMARY);
        row.add(k, BorderLayout.WEST); row.add(v, BorderLayout.EAST);
        parent.add(row);
        return v;
    }

    private JPanel legendChip(String label, String range, Color color) {
        JPanel chip = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 30));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(color);
                g2.fill(new RoundRectangle2D.Float(0, 0, 4, getHeight(), 4, 4));
                g2.dispose();
            }
        };
        chip.setOpaque(false);
        chip.setLayout(new BoxLayout(chip, BoxLayout.Y_AXIS));
        chip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(color.getRed(), color.getGreen(), color.getBlue(), 60), 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        JLabel nameLbl = new JLabel(label);
        nameLbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        nameLbl.setForeground(color);
        JLabel rangeLbl = new JLabel("BMI " + range);
        rangeLbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
        rangeLbl.setForeground(TEXT_MUTED);
        chip.add(nameLbl); chip.add(rangeLbl);
        return chip;
    }

    private JButton accentBtn(String text, Color color, Color hover) {
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
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setForeground(TEXT_PRIMARY);
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
