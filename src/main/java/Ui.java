import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/** Shared UI helpers + pink theme. */
public class Ui {

    static final String FONT = "Segoe UI";   // falls back automatically if not installed

    static final Color PINK_BG     = new Color(255, 238, 244);
    static final Color PINK_PANEL  = new Color(255, 240, 245);
    static final Color PINK_ROW    = new Color(255, 246, 249);
    static final Color PINK_SOFT   = new Color(248, 187, 208);
    static final Color PINK_DARK   = new Color(194, 24, 91);
    static final Color PINK_BUTTON = new Color(240, 98, 146);
    static final Color PINK_HOT    = new Color(236, 64, 122);
    static final Color PINK_PALE   = new Color(255, 247, 250);
    static final Color INK         = new Color(45, 30, 38);

    static final String SCHOOL     = "Tres Marias Montessori";
    static final String DEPARTMENT = "Senior High School (SHS)";
    static final String APP_TITLE  = "Document Tracking and Records Management";

    @FunctionalInterface
    interface Action {
        void run() throws Exception;
    }

    // ---------- theme ----------
    static void install() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        Font bold = new Font(FONT, Font.BOLD, 14);
        Font plain = new Font(FONT, Font.PLAIN, 14);

        UIManager.put("Panel.background", PINK_BG);
        UIManager.put("OptionPane.background", PINK_BG);
        UIManager.put("OptionPane.messageForeground", PINK_DARK);
        UIManager.put("CheckBox.background", PINK_BG);
        UIManager.put("CheckBox.foreground", PINK_DARK);
        UIManager.put("CheckBox.font", bold);
        UIManager.put("Label.foreground", PINK_DARK);
        UIManager.put("Label.font", bold);
        UIManager.put("TextField.font", plain);
        UIManager.put("PasswordField.font", plain);
        UIManager.put("ComboBox.font", plain);
        UIManager.put("ComboBox.background", Color.WHITE);
        UIManager.put("ComboBox.selectionBackground", PINK_BUTTON);
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);
        UIManager.put("TabbedPane.background", PINK_BG);
        UIManager.put("TabbedPane.selected", PINK_SOFT);
        UIManager.put("TabbedPane.contentAreaColor", PINK_BG);
        UIManager.put("TabbedPane.foreground", PINK_DARK);
        UIManager.put("TabbedPane.font", bold);
        UIManager.put("TabbedPane.focus", PINK_BG);
        UIManager.put("MenuBar.background", PINK_DARK);
        UIManager.put("Menu.foreground", Color.WHITE);
        UIManager.put("Menu.background", PINK_DARK);
        UIManager.put("ScrollPane.background", PINK_BG);
        UIManager.put("Viewport.background", Color.WHITE);
        for (String k : new String[]{"Button.font", "Menu.font", "MenuItem.font", "Spinner.font",
                "FormattedTextField.font", "Table.font", "List.font", "OptionPane.messageFont",
                "OptionPane.buttonFont", "ToolTip.font", "TitledBorder.font", "RadioButton.font"}) {
            UIManager.put(k, plain);
        }
        UIManager.put("Menu.font", bold);
    }

    // ---------- buttons / rows ----------
    static void styleButton(JButton b) {
        b.setBackground(PINK_BUTTON);
        b.setForeground(Color.WHITE);
        b.setFont(new Font(FONT, Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorder(new EmptyBorder(9, 20, 9, 20));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    static JButton button(String text, Action action) {
        return run(new RoundButton(text, PRIMARY), action);
    }

    static JPanel row(Component... items) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 3));
        p.setBackground(PINK_BG);
        for (Component c : items) p.add(c);
        return p;
    }

    static JPanel page(Component top, JTable table, Component bottom) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBackground(PINK_BG);
        if (top != null) p.add(top, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(Color.WHITE);
        sp.setBorder(BorderFactory.createLineBorder(PINK_BUTTON, 2));
        p.add(sp, BorderLayout.CENTER);
        if (bottom != null) p.add(bottom, BorderLayout.SOUTH);
        return p;
    }


    // ================= modern look: buttons, cards, fields =================
    static final int PRIMARY = 0, OUTLINE = 1, GHOST = 2, WHITE = 3, NAV = 4;

    /** Rounded button with several looks (primary, outline, ghost-on-pink, white, nav tab). */
    static class RoundButton extends JButton {
        final int mode;
        boolean selected;

        RoundButton(String text, int mode) {
            super(text);
            this.mode = mode;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setFont(new Font(FONT, Font.BOLD, 14));
            setBorder(new EmptyBorder(10, 22, 10, 22));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        void setSelectedTab(boolean on) {
            selected = on;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean hover = getModel().isRollover() || getModel().isArmed();
            int w = getWidth() - 1, h = getHeight() - 1, arc = 14;
            Color text;
            switch (mode) {
                case PRIMARY -> {
                    g.setColor(!isEnabled() ? new Color(200, 170, 185) : hover ? PINK_HOT : PINK_DARK);
                    g.fillRoundRect(0, 0, w, h, arc, arc);
                    text = Color.WHITE;
                }
                case OUTLINE -> {
                    g.setColor(hover ? PINK_PALE : Color.WHITE);
                    g.fillRoundRect(0, 0, w, h, arc, arc);
                    g.setColor(hover ? PINK_BUTTON : PINK_SOFT);
                    g.setStroke(new BasicStroke(1.6f));
                    g.drawRoundRect(0, 0, w, h, arc, arc);
                    text = PINK_DARK;
                }
                case GHOST -> {
                    g.setColor(new Color(255, 255, 255, hover ? 70 : 25));
                    g.fillRoundRect(0, 0, w, h, arc, arc);
                    g.setColor(Color.WHITE);
                    g.setStroke(new BasicStroke(1.6f));
                    g.drawRoundRect(0, 0, w, h, arc, arc);
                    text = Color.WHITE;
                }
                case WHITE -> {
                    g.setColor(hover ? PINK_PALE : Color.WHITE);
                    g.fillRoundRect(0, 0, w, h, h, h);
                    text = PINK_DARK;
                }
                default -> { // NAV
                    if (selected) {
                        g.setColor(PINK_DARK);
                        g.fillRoundRect(0, 0, w, h, arc, arc);
                        text = Color.WHITE;
                    } else {
                        if (hover) {
                            g.setColor(PINK_BG);
                            g.fillRoundRect(0, 0, w, h, arc, arc);
                        }
                        text = INK;
                    }
                }
            }
            g.dispose();
            setForeground(text);
            super.paintComponent(g0);
        }
    }

    static JButton run(JButton b, Action action) {
        b.addActionListener(e -> {
            try {
                action.run();
            } catch (Exception ex) {
                error(ex.getMessage() == null ? ex.toString() : ex.getMessage());
            }
        });
        return b;
    }

    static JButton outline(String text, Action a) { return run(new RoundButton(text, OUTLINE), a); }
    static JButton ghost(String text, Action a)   { return run(new RoundButton(text, GHOST), a); }
    static JButton white(String text, Action a)   { return run(new RoundButton(text, WHITE), a); }

    static RoundButton nav(String text, Action a) {
        RoundButton b = new RoundButton(text, NAV);
        b.setBorder(new EmptyBorder(10, 18, 10, 18));
        run(b, a);
        return b;
    }

    /** Text that looks like a link. */
    static JButton link(String text, Action a) {
        JButton b = new JButton("<html><u>" + text + "</u></html>");
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setForeground(PINK_DARK);
        b.setFont(new Font(FONT, Font.BOLD, 13));
        b.setBorder(new EmptyBorder(2, 2, 2, 2));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return run(b, a);
    }

    /** White rounded panel with a pink border (the "card" look). */
    static class RoundPanel extends JPanel {
        final Color fill, line;
        final int arc;

        RoundPanel(LayoutManager lm, Color fill, Color line, int arc) {
            super(lm);
            this.fill = fill;
            this.line = line;
            this.arc = arc;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(fill);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            if (line != null) {
                g.setColor(line);
                g.setStroke(new BasicStroke(1.5f));
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            }
            g.dispose();
            super.paintComponent(g0);
        }
    }

    static JPanel card(String title, Component content) {
        RoundPanel p = new RoundPanel(new BorderLayout(0, 4), Color.WHITE, PINK_SOFT, 20);
        p.setBorder(new EmptyBorder(8, 16, 8, 16));
        if (title != null) {
            JLabel t = new JLabel(title);
            t.setFont(new Font(FONT, Font.BOLD, 18));
            t.setForeground(PINK_DARK);
            p.add(t, BorderLayout.NORTH);
        }
        p.add(content, BorderLayout.CENTER);
        return p;
    }

    /** Light-pink page background with padding, used for each tab. */
    static JPanel pageBg() {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBackground(PINK_PALE);
        p.setBorder(new EmptyBorder(8, 14, 10, 14));
        return p;
    }

    /** Dark bold label, used for field captions. */
    static JLabel lbl(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font(FONT, Font.BOLD, 13));
        l.setForeground(INK);
        return l;
    }

    /** A caption above a field. */
    static JPanel field(String caption, JComponent c) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setOpaque(false);
        p.add(lbl(caption), BorderLayout.NORTH);
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    /** Left-aligned, transparent row of components. */
    static JPanel rowLeft(Component... items) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, -10, 0, 0));   // cancels the first 10px gap so rows line up with the fields
        for (Component c : items) p.add(c);
        return p;
    }

    /** Grid of fields; each spec is {component, gridwidth}. All rows are 'cols' columns wide. */
    static JPanel grid(int cols, Object[][] cells) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.insets = new Insets(0, 0, 4, 12);
        int x = 0, y = 0;
        for (Object[] cell : cells) {
            int w = (Integer) cell[1];
            if (x + w > cols) { x = 0; y++; }
            c.gridx = x;
            c.gridy = y;
            c.gridwidth = w;
            p.add((Component) cell[0], c);
            x += w;
        }
        return p;
    }

    /** Card holding an optional filter area on top and a table below, with optional buttons at the bottom. */
    static JPanel tableCard(Component top, JTable t, Component bottom) {
        JPanel inner = new JPanel(new BorderLayout(0, 4));
        inner.setOpaque(false);
        if (top != null) inner.add(top, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(t);
        sp.getViewport().setBackground(Color.WHITE);
        sp.setBorder(BorderFactory.createLineBorder(PINK_SOFT, 1));
        inner.add(sp, BorderLayout.CENTER);
        if (bottom != null) inner.add(bottom, BorderLayout.SOUTH);
        return card(null, inner);
    }

    static void styleField(JComponent c) {
        Border b = new CompoundBorder(new LineBorder(PINK_SOFT, 1, true), new EmptyBorder(6, 10, 6, 10));
        if (c instanceof JTextField tf) {
            tf.setBorder(b);
            tf.setBackground(tf.isEditable() ? Color.WHITE : new Color(252, 241, 246));
            tf.setCaretColor(PINK_DARK);
        } else if (c instanceof JComboBox<?> cb) {
            cb.setBackground(Color.WHITE);
            cb.setBorder(new LineBorder(PINK_SOFT, 1, true));
        } else if (c instanceof JSpinner sp) {
            sp.setBorder(new LineBorder(PINK_SOFT, 1, true));
            if (sp.getEditor() instanceof JSpinner.DefaultEditor de)
                de.getTextField().setBorder(new EmptyBorder(6, 8, 6, 8));
        }
        Dimension d = c.getPreferredSize();
        c.setPreferredSize(new Dimension(d.width, Math.max(d.height, 34)));
    }

    /** Applies the field look to every text field / combo / spinner inside a container (not inside tables). */
    static void styleTree(Container root) {
        for (Component c : root.getComponents()) {
            if (c instanceof JCheckBox cb) {
                cb.setOpaque(false);
            } else if (c instanceof JTextField || c instanceof JComboBox || c instanceof JSpinner) {
                styleField((JComponent) c);
            } else if (c instanceof Container k && !(c instanceof JTable) && !(c instanceof JScrollPane)) {
                styleTree(k);
            }
        }
    }

    /** Pink diagonal gradient with soft circles. */
    static class Gradient extends JPanel {
        final boolean circles;

        Gradient(LayoutManager lm, boolean circles) {
            super(lm);
            this.circles = circles;
            setOpaque(false);   // we paint the whole background ourselves
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g.setPaint(new GradientPaint(0, 0, PINK_DARK, w, h, new Color(240, 80, 140)));
            g.fillRect(0, 0, w, h);
            if (circles) {
                g.setColor(new Color(255, 255, 255, 28));
                g.fillOval((int) (w * 0.62), -(int) (h * 0.22), (int) (w * 0.55), (int) (w * 0.55));
                g.fillOval(-(int) (w * 0.22), (int) (h * 0.62), (int) (w * 0.5), (int) (w * 0.5));
            }
            g.dispose();
            super.paintComponent(g0);
        }
    }

    // ---------- school logo ----------
    private static BufferedImage logoImg;
    private static boolean logoLoaded;

    /**
     * The real school logo, or null if none was added (the drawn icon is used instead).
     * Put the file in src/main/resources/ (logo.png), or next to the SchoolDB folder (logo.png / logo.jpg).
     */
    static synchronized BufferedImage logoImage() {
        if (logoLoaded) return logoImg;
        logoLoaded = true;
        try {
            java.net.URL u = Ui.class.getResource("/logo.png");
            if (u == null) u = Ui.class.getResource("/logo.jpg");
            if (u != null) {
                logoImg = ImageIO.read(u);
            } else {
                for (String n : new String[]{"logo.png", "logo.jpg", "logo.jpeg"}) {
                    File f = new File(n);
                    if (f.isFile()) { logoImg = ImageIO.read(f); break; }
                }
            }
        } catch (Exception ignored) {
            logoImg = null;
        }
        return logoImg;
    }

    /** A label showing the real school logo scaled to 'size' pixels high, or null if there is no logo file. */
    static JLabel logoLabel(int size) {
        BufferedImage img = logoImage();
        if (img == null) return null;
        int h = size, w = Math.max(1, img.getWidth() * size / img.getHeight());
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(img, 0, 0, w, h, null);
        g.dispose();
        JLabel l = new JLabel(new ImageIcon(out));
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    /** Draws the school logo (or, without a logo file, the document + check icon) centred on (cx, cy); s = overall size. */
    static void paintLogo(Graphics2D g, int cx, int cy, int s) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        BufferedImage img = logoImage();
        if (img != null) {
            double k = Math.min((double) s / img.getWidth(), (double) s / img.getHeight());
            int w = Math.max(1, (int) (img.getWidth() * k)), h = Math.max(1, (int) (img.getHeight() * k));
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(img, cx - w / 2, cy - h / 2, w, h, null);
            return;
        }
        int w = (int) (s * 0.62), h = (int) (s * 0.8);
        int x = cx - w / 2, y = cy - h / 2;
        g.setColor(Color.WHITE);
        g.fillRoundRect(x, y, w, h, s / 12, s / 12);
        int fold = (int) (s * 0.2);
        g.setColor(new Color(252, 200, 222));
        g.fillPolygon(new int[]{x + w - fold, x + w, x + w}, new int[]{y, y, y + fold}, 3);
        g.setColor(new Color(236, 100, 150));
        g.setStroke(new BasicStroke(Math.max(3f, s * 0.045f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int lx = x + (int) (w * 0.2), lw = (int) (w * 0.6);
        g.drawLine(lx, y + (int) (h * 0.38), lx + lw, y + (int) (h * 0.38));
        g.drawLine(lx, y + (int) (h * 0.52), lx + lw, y + (int) (h * 0.52));
        g.drawLine(lx, y + (int) (h * 0.66), lx + (int) (lw * 0.6), y + (int) (h * 0.66));
        int d = (int) (s * 0.3);
        int bx = x + w - d + (int) (s * 0.06), by = y + h - d + (int) (s * 0.06);
        g.setColor(new Color(48, 168, 80));
        g.fillOval(bx, by, d, d);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(Math.max(2.5f, s * 0.035f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawPolyline(new int[]{bx + (int) (d * 0.26), bx + (int) (d * 0.44), bx + (int) (d * 0.76)},
                       new int[]{by + (int) (d * 0.52), by + (int) (d * 0.70), by + (int) (d * 0.32)}, 3);
    }

    /** Component that just draws the logo. */
    static JComponent logo(int size) {
        JComponent c = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                paintLogo((Graphics2D) g, getWidth() / 2, getHeight() / 2, size);
            }
        };
        Dimension d = logoImage() != null ? new Dimension(size, size)
                : new Dimension((int) (size * 0.9), (int) (size * 1.05));
        c.setPreferredSize(d);
        c.setMinimumSize(d);
        c.setMaximumSize(d);
        c.setAlignmentX(Component.CENTER_ALIGNMENT);
        return c;
    }

    /** Transparent centred label for use on the pink panels. */
    static JLabel centered(String html, int size, int style, Color color) {
        JLabel l = new JLabel("<html><div style='text-align:center'>" + html + "</div></html>", JLabel.CENTER);
        l.setFont(new Font(FONT, style, size));
        l.setForeground(color);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    // ---------- dialogs ----------
    static void error(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    static void info(String msg) {
        info("Information", msg);
    }

    static void info(String title, String msg) {
        JOptionPane.showMessageDialog(null, msg, title, JOptionPane.INFORMATION_MESSAGE);
    }

    static boolean confirm(String msg) {
        return JOptionPane.showConfirmDialog(null, msg, "Confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    // ---------- tables ----------
    static JTable table() {
        JTable t = new JTable() {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }

            /** Fill the window when the columns fit; otherwise keep their width and show a horizontal scroll bar. */
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return getParent() instanceof JViewport vp
                        && (Boolean.TRUE.equals(getClientProperty("alwaysFit")) || vp.getWidth() >= getPreferredSize().width);
            }
        };
        t.setRowHeight(32);
        t.setFont(new Font(FONT, Font.PLAIN, 14));
        t.setGridColor(PINK_SOFT);
        t.setSelectionBackground(PINK_BUTTON);
        t.setSelectionForeground(Color.WHITE);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        t.setFillsViewportHeight(true);

        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tb, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(tb, v, s, f, r, c);
                setBackground(PINK_DARK);
                setForeground(Color.WHITE);
                setFont(new Font(FONT, Font.BOLD, 14));
                setHorizontalAlignment(LEFT);
                setBorder(new EmptyBorder(9, 8, 9, 8));
                return this;
            }
        });

        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tb, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(tb, v, s, false, r, c);
                if (s) {
                    setBackground(PINK_BUTTON);
                    setForeground(Color.WHITE);
                } else {
                    setBackground(r % 2 == 0 ? Color.WHITE : PINK_ROW);
                    setForeground(Color.BLACK);
                    String col = tb.getColumnName(c);
                    if (v != null && (col.equalsIgnoreCase("STATUS") || col.equalsIgnoreCase("PAYMENT"))) {
                        switch (v.toString()) {
                            case "Released", "Paid" -> { setBackground(new Color(214, 244, 222)); setForeground(new Color(22, 110, 55)); setFont(getFont().deriveFont(Font.BOLD)); }
                            case "Ready for Release" -> { setBackground(new Color(236, 226, 250)); setForeground(new Color(95, 45, 160)); setFont(getFont().deriveFont(Font.BOLD)); }
                            case "Processing" -> { setBackground(new Color(222, 236, 252)); setForeground(new Color(25, 85, 160)); setFont(getFont().deriveFont(Font.BOLD)); }
                            case "Pending" -> { setBackground(new Color(255, 242, 210)); setForeground(new Color(150, 90, 0)); setFont(getFont().deriveFont(Font.BOLD)); }
                            case "Unpaid" -> { setBackground(new Color(255, 224, 224)); setForeground(new Color(185, 30, 30)); setFont(getFont().deriveFont(Font.BOLD)); }
                            default -> { }
                        }
                    }
                }
                setBorder(new EmptyBorder(0, 6, 0, 6));
                return this;
            }
        });
        return t;
    }

    static void show(JTable t, String sql, Object... params) {
        try {
            t.setModel(Db.query(sql, params));
            fit(t);
        } catch (Exception e) {
            error("Could not load data: " + e.getMessage());
        }
    }

    /** Sizes every column to its header and contents (so headers are never cut off). */
    static void fit(JTable t) {
        TableColumnModel cm = t.getColumnModel();
        int rows = Math.min(t.getRowCount(), 200);
        for (int c = 0; c < cm.getColumnCount(); c++) {
            TableColumn col = cm.getColumn(c);
            Component h = t.getTableHeader().getDefaultRenderer()
                    .getTableCellRendererComponent(t, col.getHeaderValue(), false, false, -1, c);
            int w = h.getPreferredSize().width;
            for (int r = 0; r < rows; r++) {
                Component k = t.prepareRenderer(t.getCellRenderer(r, c), r, c);
                w = Math.max(w, k.getPreferredSize().width);
            }
            col.setPreferredWidth(Math.min(w + 10, 260));
        }
    }

    /** Removes columns (by header text) from the view; the data stays in the model. */
    static void hideColumns(JTable t, String... names) {
        TableColumnModel cm = t.getColumnModel();
        for (String n : names) {
            for (int c = cm.getColumnCount() - 1; c >= 0; c--) {
                if (n.equalsIgnoreCase(String.valueOf(cm.getColumn(c).getHeaderValue()))) cm.removeColumn(cm.getColumn(c));
            }
        }
    }

    /** Value of a cell in the selected row by MODEL column, so it still works when columns are hidden. */
    static String mcell(JTable t, int modelCol) {
        int row = t.getSelectedRow();
        if (row < 0) return null;
        Object v = t.getModel().getValueAt(t.convertRowIndexToModel(row), modelCol);
        return v == null ? "" : v.toString();
    }

    /** Prints a panel scaled to fit one page. */
    static void printPanel(JComponent page, String jobName) throws Exception {
        java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
        job.setJobName(jobName);
        job.setPrintable((Graphics gr, java.awt.print.PageFormat pf, int index) -> {
            if (index > 0) return java.awt.print.Printable.NO_SUCH_PAGE;
            Graphics2D g2 = (Graphics2D) gr;
            g2.translate(pf.getImageableX(), pf.getImageableY());
            double sc = Math.min(1.0, Math.min(pf.getImageableWidth() / page.getWidth(),
                    pf.getImageableHeight() / page.getHeight()));
            g2.scale(sc, sc);
            page.printAll(g2);
            return java.awt.print.Printable.PAGE_EXISTS;
        });
        if (job.printDialog()) job.print();
    }

    /** Value of a cell in the selected row; null if no row is selected, "" if the cell is empty. */
    static String cell(JTable t, int col) {
        int row = t.getSelectedRow();
        if (row < 0) return null;
        Object v = t.getValueAt(row, col);
        return v == null ? "" : v.toString();
    }

    // ---------- dates ----------
    static JSpinner dateSpinner(LocalDate d) {
        JSpinner sp = new JSpinner(new SpinnerDateModel(
                Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant()), null, null, java.util.Calendar.DAY_OF_MONTH));
        sp.setEditor(new JSpinner.DateEditor(sp, "yyyy-MM-dd"));
        return sp;
    }

    static java.sql.Date dateOf(JSpinner sp) throws Exception {
        try {
            sp.commitEdit();
        } catch (java.text.ParseException e) {
            throw new Exception("Invalid date. Use yyyy-mm-dd.");
        }
        LocalDate ld = ((Date) sp.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return java.sql.Date.valueOf(ld);
    }

    static void setDate(JSpinner sp, String yyyyMmDd) {
        try {
            LocalDate ld = LocalDate.parse(yyyyMmDd.length() > 10 ? yyyyMmDd.substring(0, 10) : yyyyMmDd);
            sp.setValue(Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        } catch (Exception ignored) {
        }
    }

    /** Asks for a date (not in the past). Returns null if cancelled. */
    static java.sql.Date askDate(String title, String message, LocalDate def) throws Exception {
        JSpinner sp = dateSpinner(def);
        JPanel p = new JPanel(new GridLayout(0, 1, 6, 6));
        p.add(new JLabel(message));
        p.add(sp);
        int r = JOptionPane.showConfirmDialog(null, p, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return null;
        java.sql.Date d = dateOf(sp);
        if (d.before(java.sql.Date.valueOf(LocalDate.now()))) throw new Exception("The date cannot be in the past.");
        return d;
    }

    // ---------- account helpers ----------
    static void changePassword(String username) throws Exception {
        JPasswordField cur = new JPasswordField(15), nw = new JPasswordField(15), cf = new JPasswordField(15);
        JPanel p = new JPanel(new GridLayout(0, 2, 6, 6));
        p.add(new JLabel("Current password:"));
        p.add(cur);
        p.add(new JLabel("New password (4-30 characters):"));
        p.add(nw);
        p.add(new JLabel("Confirm new password:"));
        p.add(cf);

        int r = JOptionPane.showConfirmDialog(null, p, "Change Password",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return;

        String old = new String(cur.getPassword());
        String np = new String(nw.getPassword());
        String cp = new String(cf.getPassword());

        var m = Db.query("SELECT password FROM AppUser WHERE username = ?", username);
        if (m.getRowCount() == 0 || !Db.checkPassword(old, (String) m.getValueAt(0, 0)))
            throw new Exception("Current password is incorrect.");
        if (np.length() < 4 || np.length() > 30) throw new Exception("Password must be 4 to 30 characters.");
        if (!np.equals(cp)) throw new Exception("Passwords do not match.");

        Db.update("UPDATE AppUser SET password = ? WHERE username = ?", Db.hash(np), username);
        info("Password changed.");
    }

    static JButton changePasswordButton(String username) {
        return button("Change Password", () -> changePassword(username));
    }

    static void backToLogin(JFrame f) {
        f.dispose();
        new Login().setVisible(true);
    }

    static JButton backButton(JFrame f) {
        return button("Back to Login", () -> backToLogin(f));
    }

    static void addMenu(JFrame f, String username) {
        JMenuBar bar = new JMenuBar();
        bar.setBackground(PINK_DARK);
        JMenu file = new JMenu("File");
        file.setForeground(Color.WHITE);

        JMenuItem pw = new JMenuItem("Change Password");
        pw.addActionListener(e -> {
            try {
                changePassword(username);
            } catch (Exception ex) {
                error(ex.getMessage());
            }
        });
        JMenuItem out = new JMenuItem("Logout");
        out.addActionListener(e -> backToLogin(f));
        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> System.exit(0));

        file.add(pw);
        file.add(out);
        file.addSeparator();
        file.add(exit);
        bar.add(file);
        f.setJMenuBar(bar);
    }
}
