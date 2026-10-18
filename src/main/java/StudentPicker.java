import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Searchable student picker: type part of a name or LRN, pick from the list.
 * Up/Down move through the matches, Enter picks, Esc closes the list.
 */
public class StudentPicker extends JPanel {

    record Item(String lrn, String name, int grade, String strand) {
        @Override
        public String toString() {
            if (lrn.isEmpty()) return name;          // the "No student found" row
            return name + "   -   " + lrn + "   (Gr. " + grade + " " + strand + ")";
        }
    }

    static final int MAX_MATCHES = 8;

    final JTextField tf = new JTextField(24);
    final JPopupMenu popup = new JPopupMenu();
    final DefaultListModel<Item> model = new DefaultListModel<>();
    final JList<Item> list = new JList<>(model);

    Item selected;
    boolean adjusting;
    Runnable onChange = () -> { };

    public StudentPicker() {
        super(new BorderLayout());
        setOpaque(false);
        add(tf, BorderLayout.CENTER);
        tf.setToolTipText("Type a name or LRN, then pick the student from the list");

        list.setFocusable(false);
        list.setFont(new Font(Ui.FONT, Font.PLAIN, 14));
        list.setSelectionBackground(Ui.PINK_BUTTON);
        list.setSelectionForeground(Color.WHITE);
        list.setVisibleRowCount(MAX_MATCHES);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                int i = list.locationToIndex(e.getPoint());
                if (i >= 0) pick(model.get(i));
            }
        });
        list.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int i = list.locationToIndex(e.getPoint());
                if (i >= 0) list.setSelectedIndex(i);
            }
        });
        popup.setFocusable(false);
        popup.setBorder(new LineBorder(Ui.PINK_SOFT, 1));
        popup.add(list);

        tf.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { typed(); }
            public void removeUpdate(DocumentEvent e) { typed(); }
            public void changedUpdate(DocumentEvent e) { typed(); }
        });
        key("DOWN", () -> move(1));
        key("UP", () -> move(-1));
        key("ESCAPE", () -> popup.setVisible(false));
        tf.addActionListener(e -> {                 // Enter
            if (popup.isVisible() && !model.isEmpty()) {
                Item it = list.getSelectedValue();
                pick(it != null ? it : model.get(0));
            }
        });
        tf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) { popup.setVisible(false); }
        });
    }

    // ---------- public API ----------
    /** LRN of the chosen student ("" if nobody is chosen yet). */
    String getLrn() { return selected == null ? "" : selected.lrn(); }

    Item getSelected() { return selected; }

    void setOnChange(Runnable r) { onChange = r; }

    void focus() { tf.requestFocusInWindow(); }

    void clear() {
        adjusting = true;
        tf.setText("");
        adjusting = false;
        selected = null;
        popup.setVisible(false);
        onChange.run();
    }

    /** Selects a student by LRN (used by the Users tab shortcut). */
    void setLrn(String lrn) {
        try {
            Item it = find(lrn);
            if (it != null) pick(it);
        } catch (Exception ignored) {
        }
    }

    // ---------- internals ----------
    void key(String name, Runnable r) {
        tf.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(name), "sp-" + name);
        tf.getActionMap().put("sp-" + name, new AbstractAction() {
            public void actionPerformed(ActionEvent e) { r.run(); }
        });
    }

    void move(int d) {
        if (!popup.isVisible()) {
            search();
            return;
        }
        if (model.isEmpty()) return;
        int i = Math.floorMod(list.getSelectedIndex() + d, model.size());
        list.setSelectedIndex(i);
        list.ensureIndexIsVisible(i);
    }

    void typed() {
        if (adjusting) return;
        if (selected != null) {          // text was edited after picking, so the pick no longer applies
            selected = null;
            onChange.run();
        }
        search();
    }

    void pick(Item it) {
        if (it == null || it.lrn().isEmpty()) return;
        adjusting = true;
        tf.setText(it.name() + "  (" + it.lrn() + ")");
        adjusting = false;
        selected = it;
        popup.setVisible(false);
        onChange.run();
    }

    Item find(String lrn) throws Exception {
        var m = Db.query("SELECT s.lrn, s.full_name, s.grade_level, st.strand_name FROM Student s "
                + "JOIN Strand st ON s.strand_id = st.strand_id WHERE s.lrn = ?", lrn);
        return m.getRowCount() == 0 ? null : row(m, 0);
    }

    static Item row(javax.swing.table.TableModel m, int r) {
        return new Item(String.valueOf(m.getValueAt(r, 0)), String.valueOf(m.getValueAt(r, 1)),
                ((Number) m.getValueAt(r, 2)).intValue(), String.valueOf(m.getValueAt(r, 3)));
    }

    void search() {
        String text = tf.getText().trim();
        if (text.isEmpty()) {
            popup.setVisible(false);
            return;
        }
        // every word typed must match the name or the LRN, so "cruz juan" finds "Juan D. Cruz"
        StringBuilder sql = new StringBuilder("SELECT s.lrn, s.full_name, s.grade_level, st.strand_name FROM Student s "
                + "JOIN Strand st ON s.strand_id = st.strand_id WHERE 1 = 1");
        List<Object> p = new ArrayList<>();
        for (String w : text.toUpperCase().split("\\s+")) {
            sql.append(" AND (UPPER(s.full_name) LIKE ? OR s.lrn LIKE ?)");
            p.add("%" + w + "%");
            p.add("%" + w + "%");
        }
        sql.append(" ORDER BY s.full_name FETCH FIRST ").append(MAX_MATCHES).append(" ROWS ONLY");
        try {
            var m = Db.query(sql.toString(), p.toArray());
            model.clear();
            for (int i = 0; i < m.getRowCount(); i++) model.addElement(row(m, i));
        } catch (Exception e) {
            model.clear();
        }
        if (model.isEmpty()) {
            model.addElement(new Item("", "No student found", 0, ""));   // a "no result" row cannot be picked
        }
        boolean none = model.size() == 1 && model.get(0).lrn().isEmpty();
        list.setSelectedIndex(none ? -1 : 0);
        list.setEnabled(!none);
        list.setPreferredSize(null);
        Dimension d = list.getPreferredSize();
        list.setPreferredSize(new Dimension(Math.max(tf.getWidth(), d.width), d.height));
        if (!tf.isShowing()) return;
        if (popup.isVisible()) popup.pack();
        else popup.show(tf, 0, tf.getHeight());
    }
}
