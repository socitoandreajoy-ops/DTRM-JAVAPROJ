import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Front page: school name, department and system title. */
public class WelcomeFrame extends JFrame {

    public WelcomeFrame() {
        super(Ui.APP_TITLE);

        Ui.Gradient bg = new Ui.Gradient(new GridBagLayout(), true) {
            @Override
            protected void paintComponent(Graphics g0) {
                super.paintComponent(g0);
                // soft white frame inside the window
                Graphics2D g = (Graphics2D) g0.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(new Color(255, 255, 255, 120));
                g.setStroke(new BasicStroke(2f));
                g.drawRoundRect(18, 18, getWidth() - 37, getHeight() - 37, 30, 30);
                g.dispose();
            }
        };

        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));

        JLabel school = Ui.centered("TRES MARIAS MONTESSORI", 36, Font.BOLD, Color.WHITE);
        JLabel dept = Ui.centered(Ui.DEPARTMENT, 20, Font.PLAIN, new Color(255, 225, 238));

        JPanel divider = new JPanel();
        divider.setBackground(new Color(255, 255, 255, 170));
        divider.setMaximumSize(new Dimension(90, 3));
        divider.setPreferredSize(new Dimension(90, 3));
        divider.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = Ui.centered("Document Tracking<br>and Records Management", 40, Font.BOLD, Color.WHITE);
        JLabel tag = Ui.centered("Request, track, and release student documents in one place.", 16, Font.PLAIN,
                new Color(255, 232, 242));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        buttons.setOpaque(false);
        JButton start = Ui.white("Get Started", this::openLogin);
        start.setFont(new Font(Ui.FONT, Font.BOLD, 17));
        start.setBorder(new EmptyBorder(13, 42, 13, 42));
        JButton exit = Ui.ghost("Exit", () -> System.exit(0));
        exit.setFont(new Font(Ui.FONT, Font.BOLD, 17));
        exit.setBorder(new EmptyBorder(13, 34, 13, 34));
        buttons.add(start);
        buttons.add(exit);
        buttons.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel chips = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        chips.setOpaque(false);
        for (String t : new String[]{"Document Requests", "Payments & Receipts", "Records & Reports"}) {
            chips.add(chip(t));
        }
        chips.setAlignmentX(Component.CENTER_ALIGNMENT);

        col.add(Box.createVerticalGlue());
        col.add(Ui.logo(150));
        col.add(Box.createVerticalStrut(8));
        col.add(school);
        col.add(Box.createVerticalStrut(4));
        col.add(dept);
        col.add(Box.createVerticalStrut(18));
        col.add(divider);
        col.add(Box.createVerticalStrut(20));
        col.add(title);
        col.add(Box.createVerticalStrut(12));
        col.add(tag);
        col.add(Box.createVerticalStrut(30));
        col.add(buttons);
        col.add(Box.createVerticalStrut(34));
        col.add(chips);
        col.add(Box.createVerticalGlue());

        bg.add(col);
        setContentPane(bg);

        getRootPane().setDefaultButton((JButton) buttons.getComponent(0));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1020, 700);
        setMinimumSize(new Dimension(860, 640));
        setLocationRelativeTo(null);
    }

    static JComponent chip(String text) {
        JLabel l = new JLabel(text, JLabel.CENTER);
        l.setFont(new Font(Ui.FONT, Font.BOLD, 13));
        l.setForeground(Color.WHITE);
        Ui.RoundPanel p = new Ui.RoundPanel(new BorderLayout(), new Color(255, 255, 255, 40),
                new Color(255, 255, 255, 150), 30);
        p.setBorder(new EmptyBorder(7, 18, 7, 18));
        p.add(l);
        return p;
    }

    void openLogin() {
        new Login().setVisible(true);
        dispose();
    }
}
