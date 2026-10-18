import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;

/** Request slip: printed right after a request is submitted so the student has something in hand. */
public class SlipFrame extends JFrame {

    JPanel page = new JPanel(new GridBagLayout());

    public SlipFrame(int requestNo) throws Exception {
        super("Request Slip - Request #" + requestNo);

        var m = Db.query("SELECT r.request_no, r.request_date, r.doc_type, r.purpose, r.status, "
                + "p.amount, p.status, s.full_name, s.lrn, s.grade_level, st.strand_name "
                + "FROM Document_Request r JOIN Payment p ON p.request_no = r.request_no "
                + "JOIN Student s ON r.lrn = s.lrn JOIN Strand st ON s.strand_id = st.strand_id "
                + "WHERE r.request_no = ?", requestNo);
        if (m.getRowCount() == 0) throw new Exception("Request not found.");
        if ("Paid".equals(String.valueOf(m.getValueAt(0, 6))))
            throw new Exception("This request is already paid, so no request slip is needed.");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMMM d, yyyy");
        String date = LocalDate.parse(m.getValueAt(0, 1).toString().substring(0, 10)).format(fmt);
        String fee = "PHP " + String.format("%,.2f", ((Number) m.getValueAt(0, 5)).doubleValue());
        String payment = String.valueOf(m.getValueAt(0, 6));

        page.setBackground(Color.WHITE);
        page.setBorder(new CompoundBorder(new EmptyBorder(12, 12, 12, 12), new LineBorder(Ui.PINK_DARK, 3)));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridwidth = 2; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;

        JLabel head = new JLabel("DOCUMENT REQUEST SLIP", JLabel.CENTER);
        head.setOpaque(true);
        head.setBackground(Ui.PINK_DARK);
        head.setForeground(Color.WHITE);
        head.setFont(new Font(Ui.FONT, Font.BOLD, 22));
        head.setBorder(new EmptyBorder(14, 10, 4, 10));
        g.gridy = 0;
        page.add(head, g);

        JLabel sub = new JLabel(CertificateFrame.SCHOOL + " - " + CertificateFrame.DEPARTMENT, JLabel.CENTER);
        sub.setOpaque(true);
        sub.setBackground(Ui.PINK_DARK);
        sub.setForeground(Color.WHITE);
        sub.setFont(new Font(Ui.FONT, Font.PLAIN, 15));
        sub.setBorder(new EmptyBorder(0, 10, 12, 10));
        g.gridy = 1;
        page.add(sub, g);

        // big request number so it is easy to read out at the counter
        JLabel no = new JLabel("<html><div style='text-align:center'>REQUEST NO.<br><span style='font-size:26pt'><b>"
                + requestNo + "</b></span></div></html>", JLabel.CENTER);
        no.setFont(new Font(Ui.FONT, Font.BOLD, 14));
        no.setForeground(Ui.PINK_DARK);
        no.setBorder(new EmptyBorder(12, 10, 4, 10));
        g.gridy = 2;
        page.add(no, g);

        String[][] rows = {
            {"Date Requested", date},
            {"Student", String.valueOf(m.getValueAt(0, 7))},
            {"LRN", String.valueOf(m.getValueAt(0, 8))},
            {"Grade / Strand", "Grade " + m.getValueAt(0, 9) + " - " + m.getValueAt(0, 10)},
            {"Document", String.valueOf(m.getValueAt(0, 2))},
            {"Purpose", String.valueOf(m.getValueAt(0, 3))},
            {"Fee", fee},
            {"Payment", payment},
            {"Request Status", String.valueOf(m.getValueAt(0, 4))},
        };
        int y = 3;
        for (String[] r : rows) addRow(r[0], r[1], y++);

        String note = "Show this slip to the registrar. After your request is processed, pay the fee of <b>"
                + fee + "</b> at the registrar's office.";
        JLabel box = new JLabel("<html><div style='text-align:center'>" + note + "<br>"
                + "<span style='font-size:9pt'>Keep your payment receipt - it shows your claim date.</span></div></html>",
                JLabel.CENTER);
        box.setOpaque(true);
        box.setBackground(Ui.PINK_SOFT);
        box.setForeground(Ui.PINK_DARK);
        box.setFont(new Font(Ui.FONT, Font.BOLD, 14));
        box.setBorder(new CompoundBorder(new EmptyBorder(14, 14, 6, 14),
                new CompoundBorder(new LineBorder(Ui.PINK_BUTTON, 2, true), new EmptyBorder(10, 10, 10, 10))));
        g.gridy = y++;
        g.insets = new Insets(10, 0, 0, 0);
        page.add(box, g);

        JLabel foot = new JLabel("This slip is system-generated. It is not an official receipt.", JLabel.CENTER);
        foot.setFont(new Font(Ui.FONT, Font.ITALIC, 13));
        foot.setForeground(Color.GRAY);
        foot.setBorder(new EmptyBorder(6, 10, 10, 10));
        g.gridy = y;
        g.insets = new Insets(0, 0, 0, 0);
        page.add(foot, g);

        JPanel holder = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 15));
        holder.setBackground(Ui.PINK_BG);
        holder.add(page);
        page.setPreferredSize(new Dimension(540, page.getPreferredSize().height));

        JScrollPane sp = new JScrollPane(holder);
        sp.getVerticalScrollBar().setUnitIncrement(16);

        getContentPane().setBackground(Ui.PINK_BG);
        setLayout(new BorderLayout());
        add(sp, BorderLayout.CENTER);
        add(Ui.row(Ui.button("Print", () -> Ui.printPanel(page, "Request Slip " + requestNo)),
                Ui.button("Save as Image", () -> saveImage(requestNo)),
                Ui.button("Close", this::dispose)), BorderLayout.SOUTH);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(620, 700);
        setLocationRelativeTo(null);
    }

    void addRow(String label, String value, int y) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = y;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(5, 18, 5, 8);

        JLabel l = new JLabel(label + ":");
        l.setFont(new Font(Ui.FONT, Font.BOLD, 15));
        l.setForeground(Ui.PINK_DARK);
        g.gridx = 0;
        page.add(l, g);

        JLabel v = new JLabel(value);
        v.setFont(new Font(Ui.FONT, Font.PLAIN, 15));
        v.setForeground(Color.BLACK);
        g.gridx = 1;
        g.weightx = 1;
        g.insets = new Insets(5, 0, 5, 18);
        page.add(v, g);
    }

    void saveImage(int requestNo) throws Exception {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("Request-" + requestNo + ".png"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        BufferedImage img = new BufferedImage(page.getWidth(), page.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        page.printAll(g2);
        g2.dispose();
        File f = fc.getSelectedFile();
        if (!f.getName().toLowerCase().endsWith(".png")) f = new File(f.getPath() + ".png");
        ImageIO.write(img, "png", f);
        Ui.info("Request slip saved to:\n" + f.getAbsolutePath());
    }
}
