import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.border.*;

/** Preview + print of Certificate of Enrollment / Good Moral Certificate. */
public class CertificateFrame extends JFrame {

    // ---- EDIT THESE to match your school ----
    static final String SCHOOL = "TRES MARIAS MONTESSORI";
    static final String DEPARTMENT = "Senior High School (SHS)";
    static final String SCHOOL_ADDRESS = "Taguig City, Philippines";
    // -----------------------------------------

    JPanel page = new JPanel();

    public CertificateFrame(int requestNo, String issuedBy) throws Exception {
        super("Certificate Preview - Request #" + requestNo);

        var m = Db.query("SELECT r.doc_type, r.purpose, s.full_name, s.lrn, s.grade_level, st.strand_name "
                + "FROM Document_Request r JOIN Student s ON r.lrn = s.lrn "
                + "JOIN Strand st ON s.strand_id = st.strand_id WHERE r.request_no = ?", requestNo);
        if (m.getRowCount() == 0) throw new Exception("Request not found.");

        String doc = String.valueOf(m.getValueAt(0, 0));
        String purpose = String.valueOf(m.getValueAt(0, 1));
        String name = String.valueOf(m.getValueAt(0, 2)).toUpperCase();
        String lrn = String.valueOf(m.getValueAt(0, 3));
        String grade = String.valueOf(m.getValueAt(0, 4));
        String strand = strandFull(String.valueOf(m.getValueAt(0, 5)));

        boolean enrollment = doc.equals("Certificate of Enrollment");
        boolean goodMoral = doc.equals("Good Moral Certificate");
        if (!enrollment && !goodMoral)
            throw new Exception("Certificate preview is only available for\n"
                    + "Certificate of Enrollment and Good Moral Certificate.");

        LocalDate now = LocalDate.now();
        int y = now.getMonthValue() >= 6 ? now.getYear() : now.getYear() - 1;
        String schoolYear = y + "-" + (y + 1);
        String date = now.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));

        String title = enrollment ? "CERTIFICATE OF ENROLLMENT" : "CERTIFICATE OF GOOD MORAL CHARACTER";
        String body = enrollment
                ? "This is to certify that <b>" + name + "</b> (LRN: " + lrn + ") is a bona fide student of this "
                  + "school and is officially enrolled in <b>Grade " + grade + "</b> under the <b>" + strand
                  + "</b> strand for School Year <b>" + schoolYear + "</b>."
                : "This is to certify that <b>" + name + "</b> (LRN: " + lrn + "), a Grade " + grade + " "
                  + strand + " student of this school, is a student of <b>good moral character</b> and has no "
                  + "derogatory record on file as of this date.";

        // ---- build the certificate page ----
        final int W = 620;   // width of the text area
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBackground(Color.WHITE);
        page.setBorder(new CompoundBorder(
                new EmptyBorder(14, 14, 14, 14),
                new CompoundBorder(
                        new LineBorder(Ui.PINK_DARK, 5),
                        new CompoundBorder(
                                new EmptyBorder(4, 4, 4, 4),
                                new CompoundBorder(
                                        new LineBorder(Ui.PINK_BUTTON, 1),
                                        new EmptyBorder(28, 44, 24, 44))))));

        JLabel crest = Ui.logoLabel(80);          // real school logo, if logo.png was added
        if (crest != null) {
            page.add(row(crest, FlowLayout.CENTER));
            page.add(Box.createVerticalStrut(6));
        }
        page.add(row(label("Republic of the Philippines", new Font("Serif", Font.PLAIN, 14), Color.DARK_GRAY), FlowLayout.CENTER));
        page.add(row(label("Department of Education", new Font("Serif", Font.PLAIN, 14), Color.DARK_GRAY), FlowLayout.CENTER));
        page.add(Box.createVerticalStrut(8));
        page.add(row(label(SCHOOL, new Font("Serif", Font.BOLD, 24), Ui.PINK_DARK), FlowLayout.CENTER));
        page.add(row(label(DEPARTMENT, new Font("Serif", Font.BOLD, 16), Ui.PINK_DARK), FlowLayout.CENTER));
        page.add(row(label(SCHOOL_ADDRESS, new Font("Serif", Font.ITALIC, 13), Color.DARK_GRAY), FlowLayout.CENTER));
        page.add(Box.createVerticalStrut(12));

        JPanel rule = new JPanel();
        rule.setBackground(Ui.PINK_BUTTON);
        rule.setPreferredSize(new Dimension(180, 2));
        page.add(row(rule, FlowLayout.CENTER));
        page.add(Box.createVerticalStrut(16));

        page.add(row(label(title, new Font("Serif", Font.BOLD, 22), Ui.PINK_DARK), FlowLayout.CENTER));
        page.add(Box.createVerticalStrut(26));

        page.add(row(label("TO WHOM IT MAY CONCERN:", new Font("Serif", Font.BOLD, 15), Color.BLACK), FlowLayout.LEFT));
        page.add(Box.createVerticalStrut(12));

        JLabel text = new JLabel("<html><div style='width:" + W + "px;text-align:justify;line-height:1.6'>"
                + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + body
                + "<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;This certification is issued on <b>" + date
                + "</b> upon the request of the above-named student for the purpose of: <i>" + purpose + "</i>.</div></html>");
        text.setFont(new Font("Serif", Font.PLAIN, 16));
        text.setForeground(Color.BLACK);
        page.add(row(text, FlowLayout.LEFT));

        page.add(Box.createVerticalStrut(60));
        JLabel sig = new JLabel(issuedBy.toUpperCase(), JLabel.CENTER);
        sig.setFont(new Font("Serif", Font.BOLD, 15));
        sig.setForeground(Color.BLACK);
        sig.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, Color.BLACK), new EmptyBorder(4, 0, 0, 0)));
        sig.setPreferredSize(new Dimension(240, 28));
        page.add(row(sig, FlowLayout.RIGHT));

        page.add(Box.createVerticalStrut(28));
        page.add(row(label("Not valid without the school's dry seal.", new Font("Serif", Font.ITALIC, 11), Color.GRAY), FlowLayout.LEFT));

        JPanel holder = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 15));
        holder.setBackground(Ui.PINK_BG);
        holder.add(page);

        JScrollPane sp = new JScrollPane(holder);
        sp.getVerticalScrollBar().setUnitIncrement(16);

        getContentPane().setBackground(Ui.PINK_BG);
        setLayout(new BorderLayout());
        add(sp, BorderLayout.CENTER);
        add(Ui.row(Ui.button("Print", this::print), Ui.button("Close", this::dispose)), BorderLayout.SOUTH);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(830, 740);
        setLocationRelativeTo(null);
    }

    static String strandFull(String s) {
        return switch (s) {
            case "STEM" -> "Science, Technology, Engineering and Mathematics (STEM)";
            case "ASHH" -> "Arts, Social Sciences and Humanities (ASHH)";
            default -> s;
        };
    }

    /** Full-width row that holds one component with the given FlowLayout alignment (left / center / right). */
    static JPanel row(Component c, int align) {
        JPanel p = new JPanel(new FlowLayout(align, 0, 0));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(c);
        return p;
    }

    static JLabel label(String text, Font f, Color c) {
        JLabel l = new JLabel(text);
        l.setFont(f);
        l.setForeground(c);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    void print() throws Exception {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Certificate");
        job.setPrintable((Graphics g, PageFormat pf, int index) -> {
            if (index > 0) return Printable.NO_SUCH_PAGE;
            Graphics2D g2 = (Graphics2D) g;
            g2.translate(pf.getImageableX(), pf.getImageableY());
            double sx = pf.getImageableWidth() / page.getWidth();
            double sy = pf.getImageableHeight() / page.getHeight();
            double s = Math.min(1.0, Math.min(sx, sy));
            g2.scale(s, s);
            page.printAll(g2);
            return Printable.PAGE_EXISTS;
        });
        if (job.printDialog()) job.print();
    }
}