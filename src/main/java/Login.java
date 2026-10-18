import java.awt.*;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.*;

public class Login extends JFrame {

    static final int MAX_TRIES = 3;
    static final long LOCK_MS = 15_000;

    static final Map<String, Integer> fails = new HashMap<>();
    static final Map<String, Long> lockedUntil = new HashMap<>();

    JTextField tfUser = new JTextField(15);
    JPasswordField pfPass = new JPasswordField(15);
    JCheckBox cbShow = new JCheckBox("Show password");

    public Login() {
        super(Ui.APP_TITLE + " - Login");

        // ---------- left: pink brand panel ----------
        Ui.Gradient left = new Ui.Gradient(null, true);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setPreferredSize(new Dimension(470, 560));
        left.add(Box.createVerticalGlue());
        left.add(Ui.centered("TRES MARIAS MONTESSORI", 15, Font.BOLD, new Color(255, 225, 238)));
        left.add(Ui.centered(Ui.DEPARTMENT, 13, Font.PLAIN, new Color(255, 225, 238)));
        left.add(Box.createVerticalStrut(18));
        left.add(Ui.logo(130));
        left.add(Box.createVerticalStrut(18));
        left.add(Ui.centered("Document Tracking<br>and Records Management", 26, Font.BOLD, Color.WHITE));
        left.add(Box.createVerticalStrut(14));
        left.add(Ui.centered("Request, track, and release<br>student documents in one place.",
                15, Font.PLAIN, new Color(255, 236, 244)));
        left.add(Box.createVerticalGlue());

        // ---------- right: form ----------
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(Color.WHITE);
        right.setBorder(new EmptyBorder(30, 52, 30, 52));

        JLabel head = new JLabel("SYSTEM LOGIN");
        head.setFont(new Font(Ui.FONT, Font.BOLD, 32));
        head.setForeground(Ui.PINK_DARK);
        JLabel sub = new JLabel("Sign in to access the system");
        sub.setFont(new Font(Ui.FONT, Font.PLAIN, 14));
        sub.setForeground(Color.GRAY);

        pfPass.setEchoChar(pfPass.getEchoChar());
        final char echo = pfPass.getEchoChar();
        cbShow.setOpaque(false);
        cbShow.setFont(new Font(Ui.FONT, Font.PLAIN, 13));
        cbShow.setForeground(Color.GRAY);
        cbShow.setFocusPainted(false);
        cbShow.addActionListener(e -> pfPass.setEchoChar(cbShow.isSelected() ? (char) 0 : echo));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.setOpaque(false);
        buttons.setBorder(new EmptyBorder(0, -10, 0, 0));
        JButton login = Ui.button("Login", this::doLogin);
        buttons.add(login);
        buttons.add(Ui.outline("Clear", () -> {
            tfUser.setText("");
            pfPass.setText("");
            tfUser.requestFocus();
        }));
        buttons.add(Ui.outline("Exit", () -> System.exit(0)));

        JPanel links = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        links.setOpaque(false);
        links.setBorder(new EmptyBorder(0, -4, 0, 0));
        links.add(Ui.link("Student sign up", () -> new SignUpFrame(this).setVisible(true)));
        links.add(new JLabel("|"));
        links.add(Ui.link("Back to home", () -> {
            new WelcomeFrame().setVisible(true);
            dispose();
        }));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        int y = 0;
        c.gridy = y++; c.insets = new Insets(0, 0, 2, 0);  right.add(head, c);
        c.gridy = y++; c.insets = new Insets(0, 0, 26, 0); right.add(sub, c);
        c.gridy = y++; c.insets = new Insets(0, 0, 6, 0);  right.add(Ui.lbl("Username / LRN"), c);
        c.gridy = y++; c.insets = new Insets(0, 0, 14, 0); right.add(tfUser, c);
        c.gridy = y++; c.insets = new Insets(0, 0, 6, 0);  right.add(Ui.lbl("Password"), c);
        c.gridy = y++; c.insets = new Insets(0, 0, 6, 0);  right.add(pfPass, c);
        c.gridy = y++; c.insets = new Insets(0, 0, 18, 0); right.add(cbShow, c);
        c.gridy = y++; c.insets = new Insets(0, 0, 16, 0); right.add(buttons, c);
        c.gridy = y++; c.insets = new Insets(0, 0, 0, 0);  right.add(links, c);

        styleField(tfUser);
        styleField(pfPass);

        // ---------- frame with a pink border ----------
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new LineBorder(Ui.PINK_DARK, 3));
        root.add(left, BorderLayout.WEST);
        root.add(right, BorderLayout.CENTER);
        setContentPane(root);

        getRootPane().setDefaultButton(login);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(980, 580);
        setResizable(false);
        setLocationRelativeTo(null);
    }

    static void styleField(JTextField f) {
        Ui.styleField(f);
    }

    static void unlock(String username) {
        fails.remove(username.toLowerCase());
        lockedUntil.remove(username.toLowerCase());
    }

    void doLogin() throws SQLException {

        String user = tfUser.getText().trim();
        String pass = new String(pfPass.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            Ui.error("Please enter your username and password.");
            return;
        }

        String key = user.toLowerCase();

        Long until = lockedUntil.get(key);
        if (until != null) {
            long left = until - System.currentTimeMillis();
            if (left > 0) {
                Ui.error("Too many wrong attempts. Try again in " + ((left + 999) / 1000) + " second(s).");
                return;
            }
            unlock(key);
        }

        var model = Db.query(
            "SELECT role, lrn, password FROM AppUser WHERE username = ?",
            user
        );

        if (model.getRowCount() == 0
                || !Db.checkPassword(pass, (String) model.getValueAt(0, 2))) {

            int tries = fails.merge(key, 1, Integer::sum);

            if (tries >= MAX_TRIES) {
                lockedUntil.put(key, System.currentTimeMillis() + LOCK_MS);
                fails.remove(key);
                Ui.error("Too many wrong attempts. This account is locked for "
                        + (LOCK_MS / 1000) + " seconds.");
            } else {
                Ui.error("Incorrect username or password.\nAttempts left: " + (MAX_TRIES - tries));
            }
            return;
        }

        unlock(key);

        String role = (String) model.getValueAt(0, 0);

        if (role.equals("Student")) {
            StudentFrame sf = new StudentFrame((String) model.getValueAt(0, 1));
            sf.setVisible(true);
            dispose();
            sf.notifyReady();
        } else {
            new StaffFrame(user, role).setVisible(true);
            dispose();
        }
    }

    public static void main(String[] args) {

        Ui.install(); // pink theme + cross-platform look and feel

        try {
            Db.setup();
        } catch (SQLException e) {
            e.printStackTrace();

            StringBuilder details = new StringBuilder();
            details.append("Database setup failed:\n");

            SQLException current = e;
            while (current != null) {
                details.append("\nMessage: ").append(current.getMessage());
                details.append("\nSQLState: ").append(current.getSQLState());
                details.append("\nError Code: ").append(current.getErrorCode());
                current = current.getNextException();
            }

            JOptionPane.showMessageDialog(null, details.toString(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(() -> new WelcomeFrame().setVisible(true));
    }
}
