import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class StudentFrame extends JFrame {
    static final String[] DOCS = {"Form 137", "Form 138 / Report Card", "Good Moral Certificate",
                                  "Certificate of Enrollment", "Diploma", "Certification of Grades"};
    static final int[] FEES = {150, 100, 50, 50, 200, 75};

    String lrn;
    JLabel lblInfo = new JLabel();
    JTextField tfContact = new JTextField(12);
    JTextField tfAddress = new JTextField(25);
    JComboBox<String> cbDoc = new JComboBox<>(DOCS);
    JLabel lblFee = new JLabel("Fee: PHP 150");
    JTextField tfPurpose = new JTextField(25);
    JTable table = Ui.table();

    public StudentFrame(String lrn) {
        super("Student Portal");
        this.lrn = lrn;

        JTabbedPane tabs = new JTabbedPane();
        tabs.add("My Profile", profileTab());
        tabs.add("New Request", requestTab());
        tabs.add("My Requests", Ui.page(Ui.row(Ui.button("Refresh", this::loadRequests),
                Ui.button("Print Request Slip", this::printSlip),
                Ui.button("View Receipt", this::viewReceipt)), table, null));
        add(tabs, BorderLayout.CENTER);
        add(Ui.row(Ui.changePasswordButton(lrn), Ui.backButton(this)), BorderLayout.SOUTH);

        Ui.addMenu(this, lrn);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1050, 560);
        setLocationRelativeTo(null);
        loadProfile();
        loadRequests();
    }

    void notifyReady() {
        try {
            DefaultTableModel m = Db.query("SELECT r.request_no, r.doc_type, p.status, r.claim_date "
                    + "FROM Document_Request r JOIN Payment p ON p.request_no = r.request_no "
                    + "WHERE r.lrn = ? AND r.status = 'Ready for Release' AND r.archived = 'N' "
                    + "ORDER BY r.request_no", lrn);
            if (m.getRowCount() == 0) return;

            StringBuilder sb = new StringBuilder("Good news! These documents are ready for release:\n\n");
            boolean unpaid = false;
            for (int i = 0; i < m.getRowCount(); i++) {
                String pay = String.valueOf(m.getValueAt(i, 2));
                sb.append("Request #").append(m.getValueAt(i, 0)).append(" - ")
                  .append(m.getValueAt(i, 1)).append(" (Payment: ").append(pay).append(")\n")
                  .append("   Claim date: ")
                  .append(m.getValueAt(i, 3) == null ? "to be announced" : m.getValueAt(i, 3)).append("\n");
                if (!pay.equals("Paid")) unpaid = true;
            }
            sb.append(unpaid
                    ? "\nPlease pay the fee at the registrar's office."
                    : "\nBring your payment receipt to the registrar's office on the claim date.");
            Ui.info("Document Ready", sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    JPanel profileTab() {
        JPanel p = new JPanel(new GridLayout(0, 1));
        p.setBackground(Ui.PINK_BG);
        lblInfo.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));
        p.add(lblInfo);
        p.add(Ui.row(new JLabel("Contact (09XXXXXXXXX):"), tfContact));
        p.add(Ui.row(new JLabel("Address:"), tfAddress));
        p.add(Ui.row(Ui.button("Update Profile", this::saveContact)));
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.add(p, BorderLayout.NORTH);
        return wrap;
    }

    JPanel requestTab() {
        cbDoc.addActionListener(e -> lblFee.setText("Fee: PHP " + FEES[cbDoc.getSelectedIndex()]));
        JPanel p = new JPanel(new GridLayout(0, 1));
        p.setBackground(Ui.PINK_BG);
        p.add(Ui.row(new JLabel("Document:"), cbDoc, lblFee));
        p.add(Ui.row(new JLabel("Purpose:"), tfPurpose));
        p.add(Ui.row(Ui.button("Submit Request", this::submit),
                     Ui.button("Clear", () -> tfPurpose.setText(""))));
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.add(p, BorderLayout.NORTH);
        return wrap;
    }

    void loadProfile() {
        try {
            DefaultTableModel m = Db.query("SELECT s.full_name, s.grade_level, st.strand_name, s.contact, "
                    + "s.gender, s.birthdate, s.address "
                    + "FROM Student s JOIN Strand st ON s.strand_id = st.strand_id WHERE s.lrn = ?", lrn);
            lblInfo.setText("<html>LRN: " + lrn + "<br>Name: " + m.getValueAt(0, 0)
                    + "<br>Grade " + m.getValueAt(0, 1) + " - " + m.getValueAt(0, 2)
                    + "<br>Gender: " + m.getValueAt(0, 4) + " | Birthdate: " + m.getValueAt(0, 5) + "</html>");
            tfContact.setText(String.valueOf(m.getValueAt(0, 3)));
            tfAddress.setText(m.getValueAt(0, 6) == null ? "" : m.getValueAt(0, 6).toString());
        } catch (Exception e) {
            Ui.error("Could not load profile: " + e.getMessage());
        }
    }

    void loadRequests() {
        Ui.show(table, "SELECT r.request_no, r.doc_type, r.purpose, r.request_date, r.status, "
                + "p.amount, p.status AS payment, r.claim_date, d.release_date AS released_on, d.receiver AS received_by FROM Document_Request r "
                + "JOIN Payment p ON p.request_no = r.request_no LEFT JOIN Document_Release d ON d.request_no = r.request_no "
                + "WHERE r.lrn = ? ORDER BY r.request_no DESC", lrn);
    }

    /** Request slip to show the registrar (not needed once the request is paid). */
    void printSlip() throws Exception {
        String no = Ui.cell(table, 0);
        if (no == null) throw new Exception("Select a request first.");
        if ("Paid".equals(Ui.cell(table, 6)))
            throw new Exception("This request is already paid, so no request slip is needed.\nUse your receipt instead.");
        new SlipFrame(Integer.parseInt(no)).setVisible(true);
    }

    void viewReceipt() throws Exception {
        String no = Ui.cell(table, 0);
        if (no == null) throw new Exception("Select a request first.");
        if (!"Paid".equals(Ui.cell(table, 6))) throw new Exception("The receipt is available after the payment is marked Paid.");
        new ReceiptFrame(Integer.parseInt(no), lrn).setVisible(true);
    }

    void saveContact() throws Exception {
        if (!tfContact.getText().matches("09\\d{9}"))
            throw new Exception("Contact must be 11 digits starting with 09.");
        if (tfAddress.getText().trim().isEmpty()) throw new Exception("Address is required.");
        Db.update("UPDATE Student SET contact = ?, address = ? WHERE lrn = ?",
                tfContact.getText(), tfAddress.getText().trim(), lrn);
        Ui.info("Profile updated.");
        loadProfile();
    }

    void submit() throws Exception {
        String purpose = tfPurpose.getText().trim();
        if (purpose.isEmpty()) throw new Exception("Purpose is required.");

        Db.update("INSERT INTO Document_Request (lrn, doc_type, purpose, request_date) VALUES (?, ?, ?, ?)",
                lrn, cbDoc.getSelectedItem(), purpose, Db.today());
        Object no = Db.value("SELECT MAX(request_no) FROM Document_Request WHERE lrn = ?", lrn);
        Db.update("INSERT INTO Payment (request_no, amount) VALUES (?, ?)", no, FEES[cbDoc.getSelectedIndex()]);

        tfPurpose.setText("");
        loadRequests();
        Ui.info("Request submitted!\nYour request number is " + no + ".\n\n"
                + "Print your request slip and show it to the registrar. The registrar processes the request, "
                + "then you pay the fee and get your receipt.");
        new SlipFrame(((Number) no).intValue()).setVisible(true);
    }
}
