import java.awt.*;
import java.time.LocalDate;
import javax.swing.*;

public class SignUpFrame extends JFrame {

    static final LocalDate DEFAULT_BIRTH = LocalDate.of(2008, 1, 1);

    Login login;

    JTextField tfLrn = new JTextField(15), tfName = new JTextField(20),
               tfContact = new JTextField(11), tfAddress = new JTextField(25);
    JSpinner spBirth = Ui.dateSpinner(DEFAULT_BIRTH);
    JComboBox<String> cbGender = new JComboBox<>(new String[]{"Male", "Female"});
    JComboBox<String> cbGrade = new JComboBox<>(new String[]{"11", "12"});
    JComboBox<String> cbStrand = new JComboBox<>(new String[]{"STEM", "ASHH"});
    JPasswordField pfPass = new JPasswordField(15), pfConfirm = new JPasswordField(15);

    public SignUpFrame(Login login) {
        super("Student Sign Up");
        this.login = login;

        JPanel form = new JPanel(new GridLayout(0, 2, 5, 8));
        form.setBackground(Ui.PINK_PANEL);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 15, 5, 15),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Ui.PINK_BUTTON, 2, true),
                        BorderFactory.createEmptyBorder(12, 12, 12, 12))));
        addRow(form, "LRN (12 digits):", tfLrn);
        addRow(form, "Full name:", tfName);
        addRow(form, "Gender:", cbGender);
        addRow(form, "Birthdate (yyyy-mm-dd):", spBirth);
        addRow(form, "Grade:", cbGrade);
        addRow(form, "Strand:", cbStrand);
        addRow(form, "Contact (09XXXXXXXXX):", tfContact);
        addRow(form, "Address:", tfAddress);
        addRow(form, "Password (4-30 characters):", pfPass);
        addRow(form, "Confirm password:", pfConfirm);

        JPanel buttons = Ui.row(
            Ui.button("Create Account", this::register),
            Ui.button("Clear", this::clearForm),
            Ui.button("Cancel", this::dispose)
        );

        JLabel title = new JLabel("Student Sign Up", JLabel.CENTER);
        title.setFont(new Font(Ui.FONT, Font.BOLD, 24));
        title.setForeground(Ui.PINK_DARK);
        title.setBorder(BorderFactory.createEmptyBorder(12, 10, 0, 10));

        getContentPane().setBackground(Ui.PINK_BG);
        setLayout(new BorderLayout(5, 5));
        add(title, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        getRootPane().setDefaultButton((JButton) buttons.getComponent(0));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(login);
    }

    private void addRow(JPanel form, String label, Component field) {
        form.add(new JLabel(label));
        form.add(field);
    }

    void clearForm() {
        tfLrn.setText(""); tfName.setText("");
        tfContact.setText(""); tfAddress.setText("");
        pfPass.setText(""); pfConfirm.setText("");
        Ui.setDate(spBirth, DEFAULT_BIRTH.toString());
        cbGender.setSelectedIndex(0);
        cbGrade.setSelectedIndex(0);
        cbStrand.setSelectedIndex(0);
    }

    void register() throws Exception {
        String lrn = tfLrn.getText().trim();
        String pass = new String(pfPass.getPassword());
        String confirm = new String(pfConfirm.getPassword());

        if (!lrn.matches("\\d{12}")) throw new Exception("LRN must be exactly 12 digits.");
        if (tfName.getText().trim().isEmpty()) throw new Exception("Full name is required.");

        java.sql.Date birth = Ui.dateOf(spBirth);
        if (birth.after(Db.today())) throw new Exception("Birthdate cannot be in the future.");

        if (!tfContact.getText().matches("09\\d{9}")) throw new Exception("Contact must be 11 digits starting with 09.");
        if (tfAddress.getText().trim().isEmpty()) throw new Exception("Address is required.");
        if (pass.length() < 4 || pass.length() > 30) throw new Exception("Password must be 4 to 30 characters.");
        if (!pass.equals(confirm)) throw new Exception("Passwords do not match.");

        int found = ((Number) Db.value("SELECT COUNT(*) FROM Student WHERE lrn = ?", lrn)).intValue();
        if (found > 0) throw new Exception("This LRN is already registered. Please go back and log in.");

        Db.update("INSERT INTO Student (lrn, full_name, grade_level, strand_id, contact, "
                + "gender, birthdate, address) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                lrn, tfName.getText().trim(),
                Integer.parseInt((String) cbGrade.getSelectedItem()), cbStrand.getSelectedIndex() + 1,
                tfContact.getText(),
                cbGender.getSelectedItem(), birth, tfAddress.getText().trim());

        try {
            Db.update("INSERT INTO AppUser VALUES (?, ?, 'Student', ?)", lrn, Db.hash(pass), lrn);
        } catch (Exception e) {
            Db.update("DELETE FROM Student WHERE lrn = ?", lrn);
            throw e;
        }

        Ui.info("Account created! You can now log in with your LRN and your password.");
        login.tfUser.setText(lrn);
        login.pfPass.setText("");
        login.pfPass.requestFocus();
        dispose();
    }
}
