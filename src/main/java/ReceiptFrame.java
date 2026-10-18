import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;

/** Payment receipt + claim slip. The student shows this at the registrar's office. */
public class ReceiptFrame extends JFrame {

    JPanel page = new JPanel(new GridBagLayout());

    /** lrn = the logged-in student's LRN (so students only see their own); null for registrar staff. */
    public ReceiptFrame(int requestNo, String lrn) throws Exception {
        super("Receipt - Request #" + requestNo);

        String sql = "SELECT p.payment_id, p.amount, p.payment_date, p.method, p.status, r.doc_type, r.purpose, "
                + "r.status, r.claim_date, s.full_name, s.lrn, s.grade_level, st.strand_name, p.amount_tendered "
                + "FROM Payment p JOIN Document_Request r ON p.request_no = r.request_no "
                + "JOIN Student s ON r.lrn = s.lrn JOIN Strand st ON s.strand_id = st.strand_id "
                + "WHERE r.request_no = ?";
        var m = (lrn == null)
                ? Db.query(sql, requestNo)
                : Db.query(sql + " AND r.lrn = ?", requestNo, lrn);
        if (m.getRowCount() == 0) throw new Exception("Request not found.");
        if (!"Paid".equals(String.valueOf(m.getValueAt(0, 4))))
            throw new Exception("A receipt is only available once the payment is marked Paid.");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMMM d, yyyy");
        String orNo = "OR-" + String.format("%05d", ((Number) m.getValueAt(0, 0)).intValue());
        double dueAmt = ((Number) m.getValueAt(0, 1)).doubleValue();
        double gotAmt = m.getValueAt(0, 13) == null ? dueAmt : ((Number) m.getValueAt(0, 13)).doubleValue();
        String amount = "PHP " + String.format("%,.2f", dueAmt);
        String received = "PHP " + String.format("%,.2f", gotAmt);
        String change = "PHP " + String.format("%,.2f", Math.max(0, gotAmt - dueAmt));
        String paidOn = m.getValueAt(0, 2) == null ? "-" : LocalDate.parse(m.getValueAt(0, 2).toString().substring(0, 10)).format(fmt);
        String method = m.getValueAt(0, 3) == null ? "Cash" : m.getValueAt(0, 3).toString();
        String doc = String.valueOf(m.getValueAt(0, 5));
        String purpose = String.valueOf(m.getValueAt(0, 6));
        String status = String.valueOf(m.getValueAt(0, 7));
        Object claimObj = m.getValueAt(0, 8);
        String claim = claimObj == null ? "To be announced by the registrar"
                : LocalDate.parse(claimObj.toString().substring(0, 10)).format(fmt);
        String name = String.valueOf(m.getValueAt(0, 9));
        String sLrn = String.valueOf(m.getValueAt(0, 10));
        String gradeStrand = "Grade " + m.getValueAt(0, 11) + " - " + m.getValueAt(0, 12);

        // ---- build the receipt ----
        page.setBackground(Color.WHITE);
        page.setBorder(new CompoundBorder(new EmptyBorder(12, 12, 12, 12),
                new LineBorder(Ui.PINK_DARK, 3)));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridwidth = 2; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;

        JLabel head = new JLabel("OFFICIAL RECEIPT", JLabel.CENTER);
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

        String[][] rows = {
            {"Receipt No.", orNo},
            {"Request No.", String.valueOf(requestNo)},
            {"Student", name},
            {"LRN", sLrn},
            {"Grade / Strand", gradeStrand},
            {"Document", doc},
            {"Purpose", purpose},
            {"Amount Due", amount},
            {"Amount Received", received},
            {"Change", change},
            {"Payment Method", method},
            {"Date Paid", paidOn},
            {"Request Status", status},
        };
        int y = 2;
        for (String[] r : rows) {
            addRow(r[0], r[1], y++, false);
        }

        // claim date box
        JLabel claimBox = new JLabel("<html><div style='text-align:center'>CLAIM YOUR DOCUMENT ON<br>"
                + "<span style='font-size:16pt'><b>" + claim + "</b></span><br>"
                + "<span style='font-size:9pt'>Present this receipt and a valid ID at the registrar's office.</span></div></html>",
                JLabel.CENTER);
        claimBox.setOpaque(true);
        claimBox.setBackground(Ui.PINK_SOFT);
        claimBox.setForeground(Ui.PINK_DARK);
        claimBox.setFont(new Font(Ui.FONT, Font.BOLD, 15));
        claimBox.setBorder(new CompoundBorder(new EmptyBorder(14, 14, 6, 14),
                new CompoundBorder(new LineBorder(Ui.PINK_BUTTON, 2, true), new EmptyBorder(10, 10, 10, 10))));
        g.gridy = y++;
        g.insets = new Insets(10, 0, 0, 0);
        page.add(claimBox, g);

        JLabel foot = new JLabel("This receipt is system-generated. Keep it until you receive your document.", JLabel.CENTER);
        foot.setFont(new Font(Ui.FONT, Font.ITALIC, 15));
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
        add(Ui.row(Ui.button("Print", this::print), Ui.button("Save as Image", () -> saveImage(orNo)),
                Ui.button("Close", this::dispose)), BorderLayout.SOUTH);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(620, 800);
        setLocationRelativeTo(null);
    }

    void addRow(String label, String value, int y, boolean last) {
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

    void print() throws Exception {
        java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
        job.setJobName("Receipt");
        job.setPrintable((Graphics gr, java.awt.print.PageFormat pf, int index) -> {
            if (index > 0) return java.awt.print.Printable.NO_SUCH_PAGE;
            Graphics2D g2 = (Graphics2D) gr;
            g2.translate(pf.getImageableX(), pf.getImageableY());
            double s = Math.min(1.0, Math.min(pf.getImageableWidth() / page.getWidth(),
                    pf.getImageableHeight() / page.getHeight()));
            g2.scale(s, s);
            page.printAll(g2);
            return java.awt.print.Printable.PAGE_EXISTS;
        });
        if (job.printDialog()) job.print();
    }

    void saveImage(String orNo) throws Exception {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(orNo + ".png"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        BufferedImage img = new BufferedImage(page.getWidth(), page.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        page.printAll(g2);
        g2.dispose();
        File f = fc.getSelectedFile();
        if (!f.getName().toLowerCase().endsWith(".png")) f = new File(f.getPath() + ".png");
        ImageIO.write(img, "png", f);
        Ui.info("Receipt saved to:\n" + f.getAbsolutePath());
    }
}
