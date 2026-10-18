import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

public class StaffFrame extends JFrame {
    static final String[] STATUS = {"Pending", "Processing", "Ready for Release", "Released"};
    static final String REQ_SQL = "SELECT r.request_no, r.lrn, s.full_name, st.strand_name, s.grade_level, r.doc_type, "
            + "r.purpose, r.request_date, r.status, p.status AS payment, r.archived, r.claim_date, d.release_date AS released_on FROM Document_Request r "
            + "JOIN Student s ON r.lrn = s.lrn JOIN Strand st ON s.strand_id = st.strand_id "
            + "JOIN Payment p ON p.request_no = r.request_no "
            + "LEFT JOIN Document_Release d ON d.request_no = r.request_no";
    static final String PAY_SQL = "SELECT p.payment_id, p.request_no, s.full_name, r.doc_type, p.amount, "
            + "p.payment_date, p.method, p.status, p.amount_tendered AS amount_received, "
            + "(p.amount_tendered - p.amount) AS change_given FROM Payment p JOIN Document_Request r ON p.request_no = r.request_no "
            + "JOIN Student s ON r.lrn = s.lrn";
    static final LocalDate DEFAULT_BIRTH = LocalDate.of(2008, 1, 1);

    String user;

    JTable tbStudent = Ui.table();
    JTextField sSearch = new JTextField(12), fLrn = new JTextField(12), fName = new JTextField(20),
               fContact = new JTextField(11), fAddress = new JTextField(25);
    JSpinner fBirth = Ui.dateSpinner(DEFAULT_BIRTH);
    JComboBox<String> sGrade = new JComboBox<>(new String[]{"All", "11", "12"});
    JComboBox<String> sStrand = new JComboBox<>(new String[]{"All", "STEM", "ASHH"});
    JComboBox<String> fGrade = new JComboBox<>(new String[]{"11", "12"});
    JComboBox<String> fStrand = new JComboBox<>(new String[]{"STEM", "ASHH"});
    JComboBox<String> fGender = new JComboBox<>(new String[]{"Male", "Female", "Not specified"});

    JTable tbRequest = Ui.table();
    JTextField rSearch = new JTextField(10);
    JCheckBox rUseDate = new JCheckBox("Date:");
    JSpinner rDate = Ui.dateSpinner(LocalDate.now());
    JComboBox<String> rStatus = new JComboBox<>(new String[]{"All", "Pending", "Processing", "Ready for Release", "Released"});
    JComboBox<String> rDoc = new JComboBox<>(new String[]{"All", "Form 137", "Form 138 / Report Card",
            "Good Moral Certificate", "Certificate of Enrollment", "Diploma", "Certification of Grades"});
    JComboBox<String> rStrand = new JComboBox<>(new String[]{"All", "STEM", "ASHH"});
    JCheckBox rArchived = new JCheckBox("Show archived");
    JCheckBox rAllCols = new JCheckBox("Show all columns");

    JTable tbPayment = Ui.table();
    JTextField pSearch = new JTextField(10), pAmount = new JTextField(8), pReceived = new JTextField(8);
    JLabel lblChange = Ui.lbl("Change: PHP 0.00");
    JComboBox<String> pFilter = new JComboBox<>(new String[]{"All", "Unpaid", "Paid"});
    JComboBox<String> pMethod = new JComboBox<>(new String[]{"Cash", "Online Payment"});
    JComboBox<String> pOnline = new JComboBox<>(new String[]{"GCash", "Maya", "Bank Transfer"});
    JComboBox<String> pStatus = new JComboBox<>(new String[]{"Unpaid", "Paid"});

    JTable tbReport = Ui.table();
    JComboBox<String> cbReport = new JComboBox<>(new String[]{"Document requests", "Payment transactions",
            "Released documents", "Pending requests", "Student transaction history"});
    JComboBox<String> repDoc = new JComboBox<>(new String[]{"All", "Form 137", "Form 138 / Report Card",
            "Good Moral Certificate", "Certificate of Enrollment", "Diploma", "Certification of Grades"});
    JComboBox<String> repStatus = new JComboBox<>(new String[]{"All", "Pending", "Processing", "Ready for Release", "Released"});
    JCheckBox repUseDate = new JCheckBox("Date:");
    JSpinner repDate = Ui.dateSpinner(LocalDate.now());
    JTextField rLrn = new JTextField(12);
    JLabel lblTotal = new JLabel();

    // new document request (entered by the registrar on behalf of a student)
    StudentPicker nPick = new StudentPicker();
    JTextField nStudent = new JTextField(12), nFee = new JTextField(8), nPurpose = new JTextField(30);
    JComboBox<String> nDoc = new JComboBox<>(StudentFrame.DOCS);

    static final String[] NAV = {"Dashboard", "Users", "Records", "Payments", "Reports"};
    java.awt.CardLayout cardLayout = new java.awt.CardLayout();
    JPanel content = new JPanel(cardLayout);
    Ui.RoundButton[] navButtons = new Ui.RoundButton[NAV.length];
    JLabel[] dashVals = new JLabel[6];
    JTable tbDash = Ui.table();

    public StaffFrame(String user, String role) {
        super(Ui.APP_TITLE);
        this.user = user;

        JPanel[] pages = {dashTab(), studentTab(), requestTab(), paymentTab(), reportTab()};
        content.setBackground(Ui.PINK_PALE);
        for (int i = 0; i < NAV.length; i++) content.add(pages[i], NAV[i]);

        // ---- pink header ----
        Ui.Gradient header = new Ui.Gradient(new java.awt.BorderLayout(), true);
        header.setBorder(new EmptyBorder(10, 24, 10, 24));
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel t1 = new JLabel(Ui.APP_TITLE);
        t1.setFont(new java.awt.Font(Ui.FONT, java.awt.Font.BOLD, 30));
        t1.setForeground(java.awt.Color.WHITE);
        JLabel t2 = new JLabel(Ui.SCHOOL + "  |  " + Ui.DEPARTMENT);
        t2.setFont(new java.awt.Font(Ui.FONT, java.awt.Font.BOLD, 14));
        t2.setForeground(new java.awt.Color(255, 232, 242));
        JLabel t3 = new JLabel("Student document requests, payments, and releases");
        t3.setFont(new java.awt.Font(Ui.FONT, java.awt.Font.PLAIN, 13));
        t3.setForeground(new java.awt.Color(255, 232, 242));
        titles.add(t1);
        titles.add(Box.createVerticalStrut(2));
        titles.add(t2);
        titles.add(t3);

        JPanel who = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 16, 0));
        who.setOpaque(false);
        JLabel lblWho = new JLabel(role + ": " + user);
        lblWho.setFont(new java.awt.Font(Ui.FONT, java.awt.Font.BOLD, 15));
        lblWho.setForeground(java.awt.Color.WHITE);
        who.add(lblWho);
        who.add(Ui.ghost("Logout", () -> Ui.backToLogin(this)));
        header.add(titles, java.awt.BorderLayout.WEST);
        header.add(who, java.awt.BorderLayout.EAST);

        // ---- navigation bar ----
        JPanel tabsRow = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        tabsRow.setOpaque(false);
        for (int i = 0; i < NAV.length; i++) {
            final int idx = i;
            navButtons[i] = Ui.nav(NAV[i], () -> select(idx));
            tabsRow.add(navButtons[i]);
        }
        tabsRow.add(Box.createHorizontalStrut(6));
        tabsRow.add(Ui.outline("Exit", () -> System.exit(0)));

        JPanel tools = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        tools.setOpaque(false);
        tools.add(Ui.outline("Refresh", this::refreshAll));
        tools.add(Ui.outline("Change Password", () -> Ui.changePassword(user)));

        JPanel navBar = new JPanel(new java.awt.BorderLayout());
        navBar.setBackground(java.awt.Color.WHITE);
        navBar.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, Ui.PINK_SOFT), new EmptyBorder(5, 16, 5, 16)));
        navBar.add(tabsRow, java.awt.BorderLayout.WEST);
        navBar.add(tools, java.awt.BorderLayout.EAST);

        JPanel top = new JPanel(new java.awt.BorderLayout());
        top.add(header, java.awt.BorderLayout.NORTH);
        top.add(navBar, java.awt.BorderLayout.SOUTH);

        JPanel root = new JPanel(new java.awt.BorderLayout());
        root.setBorder(new LineBorder(Ui.PINK_DARK, 3));
        root.add(top, java.awt.BorderLayout.NORTH);
        root.add(content, java.awt.BorderLayout.CENTER);
        setContentPane(root);

        Ui.styleTree(root);
        select(0);

        Ui.addMenu(this, user);
        addShortcuts();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1320, 860);
        setMinimumSize(new java.awt.Dimension(1100, 720));
        setLocationRelativeTo(null);
    }

    // ================= KEYBOARD SHORTCUTS =================
    /** Go menu: Ctrl+1..5 switch tabs, Ctrl+N new request, F5 refresh. Enter searches in the search boxes. */
    void addShortcuts() {
        int ctrl = java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        JMenu go = new JMenu("Go");
        go.setForeground(java.awt.Color.WHITE);
        go.add(menuItem("New Document Request", KeyEvent.VK_N, ctrl, this::newRequestShortcut));
        go.addSeparator();
        int[] keys = {KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3, KeyEvent.VK_4, KeyEvent.VK_5};
        for (int i = 0; i < NAV.length; i++) {
            final int idx = i;
            go.add(menuItem(NAV[i], keys[i], ctrl, () -> select(idx)));
        }
        go.addSeparator();
        go.add(menuItem("Refresh", KeyEvent.VK_F5, 0, this::refreshAll));
        getJMenuBar().add(go);

        // Enter inside a search box runs the search
        sSearch.addActionListener(e -> loadStudents());
        rSearch.addActionListener(e -> loadRequests());
        pSearch.addActionListener(e -> loadPayments());
        rLrn.addActionListener(e -> showReport());
        nPurpose.addActionListener(e -> guard(this::submitRequest));
    }

    static JMenuItem menuItem(String text, int key, int mask, Ui.Action a) {
        JMenuItem mi = new JMenuItem(text);
        mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke(key, mask));
        mi.addActionListener(e -> guard(a));
        return mi;
    }

    static void guard(Ui.Action a) {
        try {
            a.run();
        } catch (Exception ex) {
            Ui.error(ex.getMessage() == null ? ex.toString() : ex.getMessage());
        }
    }

    void newRequestShortcut() {
        select(2);
        nPick.focus();
    }

    void select(int i) {
        cardLayout.show(content, NAV[i]);
        for (int k = 0; k < navButtons.length; k++) navButtons[k].setSelectedTab(k == i);
        if (i == 0) loadDashboard();
    }

    void refreshAll() {
        loadDashboard();
        loadStudents();
        loadRequests();
        loadPayments();
        showReport();
    }

    // ================= DASHBOARD =================
    JPanel dashTab() {
        String[] names = {"Total Students", "Total Requests", "Today's Requests",
                          "Pending Requests", "Unpaid Payments", "Released Documents"};
        Runnable[] go = {
            () -> { sSearch.setText(""); sGrade.setSelectedIndex(0); sStrand.setSelectedIndex(0); loadStudents(); select(1); },
            () -> openRecords("All", false),
            () -> openRecords("All", true),
            () -> openRecords("Pending", false),
            () -> { pSearch.setText(""); pFilter.setSelectedItem("Unpaid"); loadPayments(); select(3); },
            () -> openRecords("Released", false),
        };
        JPanel cards = new JPanel(new java.awt.GridLayout(2, 3, 10, 10));
        cards.setOpaque(false);
        for (int i = 0; i < names.length; i++) {
            JPanel c = new Ui.RoundPanel(new java.awt.BorderLayout(), java.awt.Color.WHITE, Ui.PINK_SOFT, 20);
            c.setBorder(new EmptyBorder(8, 18, 8, 18));
            JLabel t = new JLabel(names[i]);
            t.setFont(new java.awt.Font(Ui.FONT, java.awt.Font.BOLD, 14));
            t.setForeground(java.awt.Color.GRAY);
            dashVals[i] = new JLabel("0");
            dashVals[i].setFont(new java.awt.Font(Ui.FONT, java.awt.Font.BOLD, 34));
            dashVals[i].setForeground(Ui.PINK_DARK);
            JLabel hint = new JLabel("View list  \u203A");
            hint.setFont(new java.awt.Font(Ui.FONT, java.awt.Font.BOLD, 12));
            hint.setForeground(Ui.PINK_BUTTON);
            c.add(t, java.awt.BorderLayout.NORTH);
            c.add(dashVals[i], java.awt.BorderLayout.CENTER);
            c.add(hint, java.awt.BorderLayout.SOUTH);
            makeClickable(c, go[i], names[i]);
            cards.add(c);
        }
        cards.setPreferredSize(new java.awt.Dimension(0, 170));

        tbDash.putClientProperty("alwaysFit", true);   // shrink columns instead of scrolling
        JScrollPane recent = new JScrollPane(tbDash);
        recent.getViewport().setBackground(java.awt.Color.WHITE);
        recent.setBorder(BorderFactory.createLineBorder(Ui.PINK_SOFT, 1));
        JPanel lower = Ui.card("RECENT REQUESTS", recent);

        JPanel p = Ui.pageBg();
        p.add(cards, java.awt.BorderLayout.NORTH);
        p.add(lower, java.awt.BorderLayout.CENTER);
        loadDashboard();
        return p;
    }

    /** Makes a dashboard card (and the labels inside it) clickable. */
    static void makeClickable(JComponent card, Runnable action, String tip) {
        java.awt.event.MouseAdapter ma = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) { action.run(); }
        };
        card.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        card.setToolTipText("Click to see: " + tip);
        card.addMouseListener(ma);
        for (java.awt.Component k : card.getComponents()) {
            k.addMouseListener(ma);
            k.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        }
    }

    /** Dashboard card -> Records tab with the filters set (archived are included so the count matches the card). */
    void openRecords(String status, boolean today) {
        rSearch.setText("");
        rStrand.setSelectedIndex(0);
        rDoc.setSelectedIndex(0);
        rStatus.setSelectedItem(status);
        rUseDate.setSelected(today);
        if (today) Ui.setDate(rDate, LocalDate.now().toString());
        rArchived.setSelected(true);
        loadRequests();
        select(2);
    }

    void loadDashboard() {
        String[] sql = {
            "SELECT COUNT(*) FROM Student",
            "SELECT COUNT(*) FROM Document_Request",
            "SELECT COUNT(*) FROM Document_Request WHERE request_date = ?",
            "SELECT COUNT(*) FROM Document_Request WHERE status = 'Pending'",
            "SELECT COUNT(*) FROM Payment WHERE status = 'Unpaid'",
            "SELECT COUNT(*) FROM Document_Request WHERE status = 'Released'"
        };
        try {
            for (int i = 0; i < sql.length; i++) {
                Object v = (i == 2) ? Db.value(sql[i], Db.today()) : Db.value(sql[i]);
                dashVals[i].setText(String.valueOf(v));
            }
        } catch (Exception e) {
            Ui.error("Could not load the dashboard: " + e.getMessage());
        }
        Ui.show(tbDash, "SELECT r.request_no, s.full_name, r.doc_type, r.request_date, r.status "
                + "FROM Document_Request r JOIN Student s ON r.lrn = s.lrn "
                + "ORDER BY r.request_no DESC FETCH FIRST 15 ROWS ONLY");
    }

    // ================= STUDENTS =================
    JPanel studentTab() {
        tbStudent.getSelectionModel().addListSelectionListener(e -> pickStudent());

        JPanel fields = Ui.grid(4, new Object[][]{
            {Ui.field("LRN (12 digits)", fLrn), 1},
            {Ui.field("Full Name", fName), 2},
            {Ui.field("Contact (09XXXXXXXXX)", fContact), 1},
            {Ui.field("Grade", fGrade), 1},
            {Ui.field("Strand", fStrand), 1},
            {Ui.field("Gender", fGender), 1},
            {Ui.field("Birthdate (yyyy-mm-dd)", fBirth), 1},
            {Ui.field("Address", fAddress), 4},
        });
        JPanel buttons = Ui.rowLeft(Ui.button("Save", this::addStudent), Ui.outline("Update", this::updateStudent),
                Ui.outline("Delete", this::deleteStudent), Ui.outline("Clear", this::clearStudent),
                Ui.outline("Reset Password", this::resetPassword),
                Ui.outline("Create Request for Selected", this::requestForSelected));
        JPanel formBody = new JPanel(new java.awt.BorderLayout(0, 0));
        formBody.setOpaque(false);
        formBody.add(fields, java.awt.BorderLayout.CENTER);
        formBody.add(buttons, java.awt.BorderLayout.SOUTH);

        JPanel search = Ui.rowLeft(Ui.lbl("Search:"), sSearch, Ui.lbl("Grade:"), sGrade,
                Ui.lbl("Strand:"), sStrand, Ui.button("Search", this::loadStudents),
                Ui.outline("Refresh", () -> { sSearch.setText(""); sGrade.setSelectedIndex(0);
                                              sStrand.setSelectedIndex(0); loadStudents(); }));

        JPanel p = Ui.pageBg();
        p.add(Ui.card("USERS", formBody), java.awt.BorderLayout.NORTH);
        p.add(Ui.tableCard(search, tbStudent, null), java.awt.BorderLayout.CENTER);
        loadStudents();
        return p;
    }

    /** Users tab -> jump to Records with the selected student's LRN filled in. */
    void requestForSelected() throws Exception {
        String lrn = Ui.cell(tbStudent, 0);
        if (lrn == null) throw new Exception("Select a student in the table first.");
        nPick.setLrn(lrn);
        select(2);
        nPurpose.requestFocus();
    }

    void loadStudents() {
        String sql = "SELECT s.lrn, s.full_name, s.grade_level, st.strand_name, s.contact, "
                + "s.gender, s.birthdate, s.address "
                + "FROM Student s JOIN Strand st ON s.strand_id = st.strand_id "
                + "WHERE (s.lrn LIKE ? OR UPPER(s.full_name) LIKE ?)";
        String key = "%" + sSearch.getText().trim().toUpperCase() + "%";
        List<Object> p = new ArrayList<>(List.of(key, key));
        if (sGrade.getSelectedIndex() > 0) { sql += " AND s.grade_level = ?"; p.add(Integer.parseInt((String) sGrade.getSelectedItem())); }
        if (sStrand.getSelectedIndex() > 0) { sql += " AND st.strand_name = ?"; p.add(sStrand.getSelectedItem()); }
        Ui.show(tbStudent, sql + " ORDER BY s.full_name", p.toArray());
    }

    void pickStudent() {
        if (tbStudent.getSelectedRow() < 0) return;
        fLrn.setText(Ui.cell(tbStudent, 0));
        fName.setText(Ui.cell(tbStudent, 1));
        fGrade.setSelectedItem(Ui.cell(tbStudent, 2));
        fStrand.setSelectedItem(Ui.cell(tbStudent, 3));
        fContact.setText(Ui.cell(tbStudent, 4));
        fGender.setSelectedItem(Ui.cell(tbStudent, 5));
        Ui.setDate(fBirth, Ui.cell(tbStudent, 6));
        fAddress.setText(Ui.cell(tbStudent, 7));
    }

    void checkStudent() throws Exception {
        if (!fLrn.getText().matches("\\d{12}")) throw new Exception("LRN must be exactly 12 digits.");
        if (fName.getText().trim().isEmpty()) throw new Exception("Name is required.");
        if (Ui.dateOf(fBirth).after(Db.today())) throw new Exception("Birthdate cannot be in the future.");
        if (!fContact.getText().matches("09\\d{9}")) throw new Exception("Contact must be 11 digits starting with 09.");
        if (fAddress.getText().trim().isEmpty()) throw new Exception("Address is required.");
    }

    void addStudent() throws Exception {
        checkStudent();

        int found = ((Number) Db.value("SELECT COUNT(*) FROM Student WHERE lrn = ?", fLrn.getText())).intValue();
        if (found > 0) throw new Exception("This LRN is already registered.");

        Db.update("INSERT INTO Student (lrn, full_name, grade_level, strand_id, contact, "
                + "gender, birthdate, address) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                fLrn.getText(), fName.getText().trim(),
                Integer.parseInt((String) fGrade.getSelectedItem()), fStrand.getSelectedIndex() + 1,
                fContact.getText(),
                fGender.getSelectedItem(), Ui.dateOf(fBirth),
                fAddress.getText().trim());
        Db.update("INSERT INTO AppUser VALUES (?, ?, 'Student', ?)", fLrn.getText(), Db.hash("1234"), fLrn.getText());
        Ui.info("Student saved. Login: LRN / password 1234");
        clearStudent();
        loadStudents();
    }

    void updateStudent() throws Exception {
        checkStudent();
        int n = Db.update("UPDATE Student SET full_name = ?, grade_level = ?, strand_id = ?, contact = ?, "
                + "gender = ?, birthdate = ?, address = ? WHERE lrn = ?",
                fName.getText().trim(), Integer.parseInt((String) fGrade.getSelectedItem()), fStrand.getSelectedIndex() + 1,
                fContact.getText(),
                fGender.getSelectedItem(), Ui.dateOf(fBirth),
                fAddress.getText().trim(), fLrn.getText());
        if (n == 0) throw new Exception("Select an existing student first.");
        Ui.info("Student updated.");
        loadStudents();
    }

    void deleteStudent() throws Exception {
        String lrn = fLrn.getText();
        if (Ui.cell(tbStudent, 0) == null) throw new Exception("Select a student first.");
        int requests = ((Number) Db.value("SELECT COUNT(*) FROM Document_Request WHERE lrn = ?", lrn)).intValue();
        if (requests > 0) throw new Exception("This student has request records, so it cannot be deleted.");
        if (!Ui.confirm("Are you sure you want to delete this record?")) return;
        Db.update("DELETE FROM AppUser WHERE lrn = ?", lrn);
        Db.update("DELETE FROM Student WHERE lrn = ?", lrn);
        Ui.info("Student deleted.");
        clearStudent();
        loadStudents();
    }

    void resetPassword() throws Exception {
        String lrn = Ui.cell(tbStudent, 0);
        if (lrn == null) throw new Exception("Select a student first.");
        if (!Ui.confirm("Reset the password of " + lrn + " to 1234?")) return;
        int n = Db.update("UPDATE AppUser SET password = ? WHERE username = ?", Db.hash("1234"), lrn);
        if (n == 0) throw new Exception("This student has no login account.");
        Login.unlock(lrn);
        Ui.info("Password reset. The student can now log in with password 1234.");
    }

    void clearStudent() {
        fLrn.setText(""); fName.setText(""); fContact.setText(""); fAddress.setText("");
        Ui.setDate(fBirth, DEFAULT_BIRTH.toString());
        fGender.setSelectedIndex(0);
        tbStudent.clearSelection();
    }

    // ================= REQUESTS =================
    JPanel requestTab() {
        // ---- NEW DOCUMENT REQUEST (registrar enters it for the student) ----
        nStudent.setEditable(false);
        nFee.setEditable(false);
        nFee.setText("PHP " + StudentFrame.FEES[0]);
        nDoc.addActionListener(e -> nFee.setText("PHP " + StudentFrame.FEES[nDoc.getSelectedIndex()]));
        nPick.setOnChange(this::showPickedStudent);

        JPanel newFields = Ui.grid(4, new Object[][]{
            {Ui.field("Student (type a name or LRN)", nPick), 2},
            {Ui.field("Grade / Strand", nStudent), 1},
            {Ui.field("Document", nDoc), 1},
            {Ui.field("Fee", nFee), 1},
            {Ui.field("Purpose", nPurpose), 2},
            {Ui.field(" ", Ui.rowLeft(Ui.button("Submit Request", this::submitRequest),
                    Ui.outline("Clear", this::clearNewRequest))), 1},
        });
        JPanel newCard = Ui.card("NEW DOCUMENT REQUEST", newFields);

        // ---- filters + table ----
        JPanel filters = new JPanel(new java.awt.GridLayout(0, 1));
        filters.setOpaque(false);
        filters.add(Ui.rowLeft(Ui.lbl("Search Req#/LRN/Name:"), rSearch, rUseDate, rDate,
                Ui.lbl("Strand:"), rStrand, rArchived));
        filters.add(Ui.rowLeft(Ui.lbl("Status:"), rStatus, Ui.lbl("Document:"), rDoc, rAllCols,
                Ui.button("Search", this::loadRequests)));
        rAllCols.addActionListener(e -> loadRequests());
        JPanel actions = Ui.rowLeft(Ui.button("Move to Next Status", this::advance),
                Ui.outline("Set Claim Date", this::setClaimDate),
                Ui.outline("Delete / Archive", this::removeRequest),
                Ui.outline("Preview Certificate", this::previewCertificate),
                Ui.outline("Print Request Slip", this::printSlip));

        JPanel p = Ui.pageBg();
        p.add(newCard, java.awt.BorderLayout.NORTH);
        p.add(Ui.tableCard(filters, tbRequest, actions), java.awt.BorderLayout.CENTER);
        loadRequests();
        return p;
    }

    void showPickedStudent() {
        var it = nPick.getSelected();
        nStudent.setText(it == null ? "" : "Gr. " + it.grade() + " " + it.strand());
    }

    void submitRequest() throws Exception {
        String lrn = nPick.getLrn();
        if (lrn.isEmpty()) throw new Exception("Pick a student first: type a name or LRN and choose from the list.");
        Object name = Db.value("SELECT full_name FROM Student WHERE lrn = ?", lrn);
        if (name == null) throw new Exception("No student has this LRN.\nAdd the student in the Users tab first.");
        String purpose = nPurpose.getText().trim();
        if (purpose.isEmpty()) throw new Exception("Purpose is required.");
        if (purpose.length() > 200) throw new Exception("Purpose must be 200 characters or less.");

        int idx = nDoc.getSelectedIndex();

        // warn if this student already has an open (not yet released) request for the same document
        var open = Db.query("SELECT request_no, status FROM Document_Request "
                + "WHERE lrn = ? AND doc_type = ? AND status <> 'Released' AND archived = 'N' ORDER BY request_no",
                lrn, nDoc.getSelectedItem());
        if (open.getRowCount() > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < open.getRowCount(); i++)
                sb.append(i > 0 ? ", " : "").append("#").append(open.getValueAt(i, 0))
                  .append(" (").append(open.getValueAt(i, 1)).append(")");
            if (!Ui.confirm(name + " already has an open request for " + nDoc.getSelectedItem() + ":\n"
                    + sb + "\n\nSubmit another one anyway?")) return;
        }

        Db.update("INSERT INTO Document_Request (lrn, doc_type, purpose, request_date) VALUES (?, ?, ?, ?)",
                lrn, nDoc.getSelectedItem(), purpose, Db.today());
        Object no = Db.value("SELECT MAX(request_no) FROM Document_Request WHERE lrn = ?", lrn);
        Db.update("INSERT INTO Payment (request_no, amount) VALUES (?, ?)", no, StudentFrame.FEES[idx]);

        boolean slip = Ui.confirm("Request #" + no + " was added for " + name + ".\n"
                + nDoc.getSelectedItem() + " - fee PHP " + StudentFrame.FEES[idx]
                + " (see the Payments tab).\n\nPrint the request slip for the student now?");
        clearNewRequest();
        loadRequests();
        loadPayments();
        loadDashboard();
        if (slip) new SlipFrame(((Number) no).intValue()).setVisible(true);
    }

    void printSlip() throws Exception {
        String no = Ui.mcell(tbRequest, 0);
        if (no == null) throw new Exception("Select a request first.");
        if ("Paid".equals(Ui.mcell(tbRequest, 9)))
            throw new Exception("This request is already paid, so no request slip is needed.\nUse the receipt (Payments tab) instead.");
        new SlipFrame(Integer.parseInt(no)).setVisible(true);
    }

    void clearNewRequest() {
        nPick.clear();
        nStudent.setText("");
        nPurpose.setText("");
        nDoc.setSelectedIndex(0);
    }

    void loadRequests() {
        String search = rSearch.getText().trim();
        String key = "%" + search.toUpperCase() + "%";

        String sql = REQ_SQL + " WHERE (r.lrn LIKE ? OR UPPER(s.full_name) LIKE ?";

        List<Object> params = new ArrayList<>();
        params.add(key);
        params.add(key);

        if (search.matches("\\d+")) {
            try {
                params.add(Integer.valueOf(search));
                sql += " OR r.request_no = ?";
            } catch (NumberFormatException ex) {
            }
        }

        sql += ")";

        if (!rArchived.isSelected()) {
            sql += " AND r.archived = 'N'";
        }

        if (rStatus.getSelectedIndex() > 0) {
            sql += " AND r.status = ?";
            params.add(rStatus.getSelectedItem());
        }

        if (rDoc.getSelectedIndex() > 0) {
            sql += " AND r.doc_type = ?";
            params.add(rDoc.getSelectedItem());
        }

        if (rStrand.getSelectedIndex() > 0) {
            sql += " AND st.strand_name = ?";
            params.add(rStrand.getSelectedItem());
        }

        if (rUseDate.isSelected()) {
            try {
                params.add(Ui.dateOf(rDate));
                sql += " AND r.request_date = ?";
            } catch (Exception ex) {
                Ui.error(ex.getMessage());
                return;
            }
        }

        Ui.show(tbRequest, sql + " ORDER BY r.request_no DESC", params.toArray());
        if (!rAllCols.isSelected()) Ui.hideColumns(tbRequest, "ARCHIVED", "RELEASED ON");
    }

    void advance() throws Exception {
        String no = Ui.mcell(tbRequest, 0);
        if (no == null) throw new Exception("Select a request first.");
        int i = Arrays.asList(STATUS).indexOf(Ui.mcell(tbRequest, 8));
        if (i == STATUS.length - 1) throw new Exception("This request is already Released.");
        String next = STATUS[i + 1];

        if (next.equals("Ready for Release") && !"Paid".equals(Ui.mcell(tbRequest, 9)))
            throw new Exception("The student must pay first (Payments tab) before the claim date is set.");

        if (next.equals("Ready for Release") && Ui.mcell(tbRequest, 11).isEmpty()) {
            java.sql.Date d = Ui.askDate("Claim Date",
                    "Date the student can claim the document (yyyy-mm-dd):", LocalDate.now().plusDays(3));
            if (d == null) throw new Exception("A claim date is required before marking Ready for Release.");
            Db.update("UPDATE Document_Request SET claim_date = ? WHERE request_no = ?", d, Integer.parseInt(no));
        }

        if (next.equals("Released")) {
            if (!"Paid".equals(Ui.mcell(tbRequest, 9))) throw new Exception("The payment must be Paid before release.");
            if (!Ui.confirm("Did the student show the payment receipt (OR-" + String.format("%05d", paymentId(no)) + ")?"))
                return;
            String who = JOptionPane.showInputDialog(this, "Name of the person receiving the document:");
            if (who == null || who.trim().isEmpty()) throw new Exception("Receiver name is required.");
            Db.update("INSERT INTO Document_Release (request_no, release_date, receiver, released_by) VALUES (?, ?, ?, ?)",
                    Integer.parseInt(no), Db.today(), who.trim(), user);
        }
        Db.update("UPDATE Document_Request SET status = ? WHERE request_no = ?", next, Integer.parseInt(no));
        Ui.info("Request #" + no + " is now: " + next);
        loadRequests();
    }

    /** Payment id of a request (the receipt number is OR-<payment id>). */
    static int paymentId(String requestNo) {
        try {
            return ((Number) Db.value("SELECT payment_id FROM Payment WHERE request_no = ?", Integer.parseInt(requestNo))).intValue();
        } catch (Exception e) {
            return 0;
        }
    }

    void removeRequest() throws Exception {
        String no = Ui.mcell(tbRequest, 0);
        if (no == null) throw new Exception("Select a request first.");
        if (Ui.mcell(tbRequest, 8).equals("Released")) {
            if (Ui.confirm("Released requests are archived, not deleted. Archive this one?")) {
                Db.update("UPDATE Document_Request SET archived = 'Y' WHERE request_no = ?", Integer.parseInt(no));
                Ui.info("Request archived.");
                loadRequests();
            }
            return;
        }
        if (!Ui.confirm("Are you sure you want to delete this record?")) return;
        Db.update("DELETE FROM Payment WHERE request_no = ?", Integer.parseInt(no));
        Db.update("DELETE FROM Document_Request WHERE request_no = ?", Integer.parseInt(no));
        Ui.info("Request deleted.");
        loadRequests();
    }

    void setClaimDate() throws Exception {
        String no = Ui.mcell(tbRequest, 0);
        if (no == null) throw new Exception("Select a request first.");
        if (Ui.mcell(tbRequest, 8).equals("Released")) throw new Exception("This request is already Released.");
        String current = Ui.mcell(tbRequest, 11);
        LocalDate def = LocalDate.now().plusDays(3);
        if (!current.isEmpty()) {
            LocalDate old = LocalDate.parse(current.substring(0, 10));
            if (!old.isBefore(LocalDate.now())) def = old;
        }
        java.sql.Date d = Ui.askDate("Claim Date", "Date the student can claim the document (yyyy-mm-dd):", def);
        if (d == null) return;
        Db.update("UPDATE Document_Request SET claim_date = ? WHERE request_no = ?", d, Integer.parseInt(no));
        Ui.info("Claim date for request #" + no + " set to " + d + ".");
        loadRequests();
    }

    void previewCertificate() throws Exception {
        String no = Ui.mcell(tbRequest, 0);
        if (no == null) throw new Exception("Select a request first.");
        new CertificateFrame(Integer.parseInt(no), user).setVisible(true);
    }

    // ================= PAYMENTS =================
    JPanel paymentTab() {
        pOnline.setEnabled(false);
        pAmount.setEditable(false);
        pReceived.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateChange(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateChange(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateChange(); }
        });
        pMethod.addActionListener(e -> pOnline.setEnabled(pMethod.getSelectedIndex() == 1));

        tbPayment.getSelectionModel().addListSelectionListener(e -> {
            if (tbPayment.getSelectedRow() < 0) return;
            pAmount.setText(Ui.cell(tbPayment, 4));
            pReceived.setText(Ui.cell(tbPayment, 8));
            pStatus.setSelectedItem(Ui.cell(tbPayment, 7));

            String method = Ui.cell(tbPayment, 6);
            if (method.startsWith("Online - ")) {
                pMethod.setSelectedItem("Online Payment");
                pOnline.setSelectedItem(method.substring("Online - ".length()));
            } else {
                pMethod.setSelectedItem("Cash");
            }
        });
        JPanel form = Ui.grid(4, new Object[][]{
            {Ui.field("Amount Due", pAmount), 1},
            {Ui.field("Amount Received", pReceived), 1},
            {Ui.field(" ", lblChange), 1},
            {Ui.field("Status", pStatus), 1},
            {Ui.field("Method", pMethod), 1},
            {Ui.field("Online via", pOnline), 1},
        });
        JPanel buttons = Ui.rowLeft(Ui.button("Update Payment", this::updatePayment),
                Ui.outline("Print Receipt", this::showReceipt),
                Ui.outline("Clear", () -> { pAmount.setText(""); pReceived.setText(""); tbPayment.clearSelection(); }));
        JPanel formBody = new JPanel(new java.awt.BorderLayout(0, 0));
        formBody.setOpaque(false);
        formBody.add(form, java.awt.BorderLayout.CENTER);
        formBody.add(buttons, java.awt.BorderLayout.SOUTH);

        JPanel search = Ui.rowLeft(Ui.lbl("Search Req#/LRN/Name:"), pSearch, Ui.lbl("Status:"), pFilter,
                Ui.button("Search", this::loadPayments));

        JPanel p = Ui.pageBg();
        p.add(Ui.card("PAYMENTS", formBody), java.awt.BorderLayout.NORTH);
        p.add(Ui.tableCard(search, tbPayment, null), java.awt.BorderLayout.CENTER);
        loadPayments();
        return p;
    }

    void loadPayments() {
        String search = pSearch.getText().trim();
        String key = "%" + search.toUpperCase() + "%";

        String sql = PAY_SQL + " WHERE (r.lrn LIKE ? OR UPPER(s.full_name) LIKE ?";

        List<Object> params = new ArrayList<>();
        params.add(key);
        params.add(key);

        if (search.matches("\\d+")) {
            try {
                params.add(Integer.valueOf(search));
                sql += " OR p.request_no = ?";
            } catch (NumberFormatException ex) {
            }
        }

        sql += ")";

        if (pFilter.getSelectedIndex() > 0) {
            sql += " AND p.status = ?";
            params.add(pFilter.getSelectedItem());
        }

        Ui.show(tbPayment, sql + " ORDER BY p.payment_id DESC", params.toArray());
    }

    void updateChange() {
        try {
            double due = Double.parseDouble(pAmount.getText().trim());
            double got = Double.parseDouble(pReceived.getText().trim());
            lblChange.setText(got >= due
                    ? "Change: PHP " + String.format("%,.2f", got - due)
                    : "Short by: PHP " + String.format("%,.2f", due - got));
        } catch (NumberFormatException ex) {
            lblChange.setText("Change: PHP 0.00");
        }
    }

    void updatePayment() throws Exception {
        String id = Ui.cell(tbPayment, 0);
        if (id == null) throw new Exception("Select a payment first.");
        double due = Double.parseDouble(Ui.cell(tbPayment, 4));
        String reqNo = Ui.cell(tbPayment, 1);
        boolean paid = pStatus.getSelectedItem().equals("Paid");

        if (paid) {
            Object st = Db.value("SELECT status FROM Document_Request WHERE request_no = ?", Integer.parseInt(reqNo));
            if ("Pending".equals(st))
                throw new Exception("Process the request first (Records tab > Move to Next Status), then take the payment.");
            double received;
            try { received = Double.parseDouble(pReceived.getText().trim()); }
            catch (NumberFormatException e) { throw new Exception("Enter the amount received (a number)."); }
            if (received < due)
                throw new Exception(String.format(
                        "Amount received (PHP %,.2f) is less than the amount due (PHP %,.2f).", received, due));

            String method = pMethod.getSelectedIndex() == 0 ? "Cash" : "Online - " + pOnline.getSelectedItem();
            Db.update("UPDATE Payment SET amount_tendered = ?, method = ?, status = 'Paid', payment_date = ? "
                    + "WHERE payment_id = ?", received, method, Db.today(), Integer.parseInt(id));
            double change = received - due;
            Ui.info(change > 0
                    ? String.format("Payment recorded.\nGive change: PHP %,.2f", change)
                    : "Payment recorded. Exact amount received.");
        } else {
            Db.update("UPDATE Payment SET amount_tendered = NULL, method = NULL, status = 'Unpaid', "
                    + "payment_date = NULL WHERE payment_id = ?", Integer.parseInt(id));
            Ui.info("Payment marked Unpaid.");
        }
        loadPayments();
        if (paid && Ui.confirm("Show the receipt now?"))
            new ReceiptFrame(Integer.parseInt(reqNo), null).setVisible(true);
    }

    void showReceipt() throws Exception {
        String reqNo = Ui.cell(tbPayment, 1);
        if (reqNo == null) throw new Exception("Select a payment first.");
        new ReceiptFrame(Integer.parseInt(reqNo), null).setVisible(true);
    }

    // ================= REPORTS =================
    JPanel reportTab() {
        JPanel top = new JPanel(new java.awt.GridLayout(0, 1));
        top.setOpaque(false);
        top.add(Ui.rowLeft(Ui.lbl("Report:"), cbReport, Ui.lbl("LRN (for history):"), rLrn));
        top.add(Ui.rowLeft(Ui.lbl("Filter (Document requests)  Document:"), repDoc,
                Ui.lbl("Status:"), repStatus, repUseDate, repDate));
        top.add(Ui.rowLeft(
                Ui.button("Show Report", this::showReport),
                Ui.outline("Clear Filters", () -> {
                    repDoc.setSelectedIndex(0);
                    repStatus.setSelectedIndex(0);
                    repUseDate.setSelected(false);
                    showReport();
                }),
                lblTotal));
        JPanel p = Ui.pageBg();
        p.add(Ui.tableCard(top, tbReport, null), java.awt.BorderLayout.CENTER);
        showReport();
        return p;
    }

    void showReport() {
        lblTotal.setText("");

        switch (cbReport.getSelectedIndex()) {
            case 0 -> {
                String sql = REQ_SQL + " WHERE 1 = 1";
                List<Object> params = new ArrayList<>();
                if (repDoc.getSelectedIndex() > 0) {
                    sql += " AND r.doc_type = ?";
                    params.add(repDoc.getSelectedItem());
                }
                if (repStatus.getSelectedIndex() > 0) {
                    sql += " AND r.status = ?";
                    params.add(repStatus.getSelectedItem());
                }
                if (repUseDate.isSelected()) {
                    try {
                        params.add(Ui.dateOf(repDate));
                        sql += " AND r.request_date = ?";
                    } catch (Exception ex) {
                        Ui.error(ex.getMessage());
                        return;
                    }
                }
                Ui.show(tbReport, sql + " ORDER BY r.doc_type, r.request_date", params.toArray());
            }
            case 1 -> {
                Ui.show(tbReport, PAY_SQL + " ORDER BY p.payment_date");
                try {
                    Object total = Db.value("SELECT SUM(amount) FROM Payment WHERE status = 'Paid'");
                    double sum = total == null ? 0 : ((Number) total).doubleValue();
                    lblTotal.setText("   Total collected (Paid): PHP " + String.format("%,.2f", sum));
                } catch (Exception ex) {
                    Ui.error("Could not compute the total: " + ex.getMessage());
                }
            }
            case 2 -> Ui.show(tbReport, "SELECT d.release_id, d.request_no, s.full_name, r.doc_type, d.release_date, "
                    + "d.receiver, d.released_by FROM Document_Release d JOIN Document_Request r ON d.request_no = r.request_no "
                    + "JOIN Student s ON r.lrn = s.lrn ORDER BY d.release_date");
            case 3 -> Ui.show(tbReport, REQ_SQL + " WHERE r.status = 'Pending' ORDER BY r.request_date");
            default -> {
                if (!rLrn.getText().matches("\\d{12}")) { Ui.error("Enter a 12-digit LRN for the history."); return; }
                Ui.show(tbReport, REQ_SQL + " WHERE r.lrn = ? ORDER BY r.request_date", rLrn.getText());
            }
        }
    }
}
