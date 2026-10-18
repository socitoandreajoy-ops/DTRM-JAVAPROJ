import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.*;
import java.time.LocalDate;
import java.util.HexFormat;
import javax.swing.table.DefaultTableModel;

/** Database helper (embedded Apache Derby). */
public class Db {

    static final String URL = "jdbc:derby:SchoolDB;create=true";

    static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    // ---------- setup ----------
    static void setup() throws SQLException {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Derby driver not found. Make sure Maven dependencies are downloaded.");
        }

        try (Connection c = connect()) {
            System.out.println("Connected to: jdbc:derby:SchoolDB");

            ensure(c, "STRAND",
                "CREATE TABLE Strand (strand_id INT PRIMARY KEY, strand_name VARCHAR(20) NOT NULL)");

            ensure(c, "STUDENT",
                "CREATE TABLE Student (lrn VARCHAR(12) PRIMARY KEY, full_name VARCHAR(100) NOT NULL, "
                + "grade_level INT NOT NULL, strand_id INT NOT NULL REFERENCES Strand(strand_id), "
                + "contact VARCHAR(11), gender VARCHAR(20), birthdate DATE, address VARCHAR(200))");

            ensure(c, "APPUSER",
                "CREATE TABLE AppUser (username VARCHAR(30) PRIMARY KEY, password VARCHAR(200) NOT NULL, "
                + "role VARCHAR(20) NOT NULL, lrn VARCHAR(12) REFERENCES Student(lrn))");

            ensure(c, "DOCUMENT_REQUEST",
                "CREATE TABLE Document_Request (request_no INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, "
                + "lrn VARCHAR(12) NOT NULL REFERENCES Student(lrn), doc_type VARCHAR(40) NOT NULL, "
                + "purpose VARCHAR(200) NOT NULL, request_date DATE NOT NULL, "
                + "status VARCHAR(30) DEFAULT 'Pending' NOT NULL, archived CHAR(1) DEFAULT 'N' NOT NULL)");

            ensure(c, "PAYMENT",
                "CREATE TABLE Payment (payment_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, "
                + "request_no INT NOT NULL REFERENCES Document_Request(request_no), "
                + "amount DECIMAL(10,2) NOT NULL, payment_date DATE, method VARCHAR(40), "
                + "status VARCHAR(10) DEFAULT 'Unpaid' NOT NULL)");

            ensure(c, "DOCUMENT_RELEASE",
                "CREATE TABLE Document_Release (release_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, "
                + "request_no INT NOT NULL REFERENCES Document_Request(request_no), "
                + "release_date DATE NOT NULL, receiver VARCHAR(100), released_by VARCHAR(30))");

            // add claim_date column for databases created before this feature
            boolean hasClaim;
            try (ResultSet rs = c.getMetaData().getColumns(null, null, "DOCUMENT_REQUEST", "CLAIM_DATE")) {
                hasClaim = rs.next();
            }
            if (!hasClaim) {
                try (Statement s = c.createStatement()) {
                    s.executeUpdate("ALTER TABLE Document_Request ADD COLUMN claim_date DATE");
                }
            }

            // amount_tendered: money handed over by the student (amount = the fee, so change can be computed)
            boolean hasTender;
            try (ResultSet rs = c.getMetaData().getColumns(null, null, "PAYMENT", "AMOUNT_TENDERED")) {
                hasTender = rs.next();
            }
            if (!hasTender) {
                try (Statement s = c.createStatement()) {
                    s.executeUpdate("ALTER TABLE Payment ADD COLUMN amount_tendered DECIMAL(10,2)");
                    s.executeUpdate("UPDATE Payment SET amount_tendered = amount WHERE status = 'Paid'");
                }
                // restore the official fee on old rows where the fee had been overwritten by the amount paid
                try (PreparedStatement ps = c.prepareStatement("UPDATE Payment SET amount = ? WHERE request_no IN "
                        + "(SELECT request_no FROM Document_Request WHERE doc_type = ?)")) {
                    for (int i = 0; i < StudentFrame.DOCS.length; i++) {
                        ps.setInt(1, StudentFrame.FEES[i]);
                        ps.setString(2, StudentFrame.DOCS[i]);
                        ps.executeUpdate();
                    }
                }
                try (Statement s = c.createStatement()) {
                    s.executeUpdate("UPDATE Payment SET amount_tendered = amount WHERE amount_tendered < amount");
                }
            }

            // seed data
            if (count(c, "SELECT COUNT(*) FROM Strand") == 0) {
                try (Statement s = c.createStatement()) {
                    s.executeUpdate("INSERT INTO Strand VALUES (1, 'STEM')");
                    s.executeUpdate("INSERT INTO Strand VALUES (2, 'ASHH')");
                }
            }
            if (count(c, "SELECT COUNT(*) FROM AppUser WHERE username = 'registrar'") == 0) {
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO AppUser VALUES (?, ?, 'Registrar', NULL)")) {
                    ps.setString(1, "registrar");
                    ps.setString(2, hash("admin123"));
                    ps.executeUpdate();
                }
            }
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                DriverManager.getConnection("jdbc:derby:;shutdown=true");
            } catch (SQLException ignored) {
            }
        }));

        System.out.println("Database setup successful!");
    }

    static boolean exists(Connection c, String table) throws SQLException {
        try (ResultSet rs = c.getMetaData().getTables(null, null, table, new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    static void ensure(Connection c, String table, String ddl) throws SQLException {
        if (!exists(c, table)) {
            try (Statement s = c.createStatement()) {
                s.executeUpdate(ddl);
            }
        }
    }

    static int count(Connection c, String sql) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    // ---------- queries ----------
    static void bind(PreparedStatement ps, Object... p) throws SQLException {
        for (int i = 0; i < p.length; i++) {
            ps.setObject(i + 1, p[i]);
        }
    }

    static DefaultTableModel query(String sql, Object... p) throws SQLException {
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, p);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                int n = md.getColumnCount();
                DefaultTableModel m = new DefaultTableModel();
                for (int i = 1; i <= n; i++) {
                    m.addColumn(md.getColumnLabel(i).replace('_', ' '));
                }
                while (rs.next()) {
                    Object[] row = new Object[n];
                    for (int i = 1; i <= n; i++) {
                        row[i - 1] = rs.getObject(i);
                    }
                    m.addRow(row);
                }
                return m;
            }
        }
    }

    static int update(String sql, Object... p) throws SQLException {
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, p);
            return ps.executeUpdate();
        }
    }

    static Object value(String sql, Object... p) throws SQLException {
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, p);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getObject(1) : null;
            }
        }
    }

    static java.sql.Date today() {
        return java.sql.Date.valueOf(LocalDate.now());
    }

    // ---------- passwords (salted SHA-256) ----------
    static byte[] sha(byte[] salt, String pass) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            return md.digest(pass.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static String hash(String pass) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        HexFormat hf = HexFormat.of();
        return hf.formatHex(salt) + "$" + hf.formatHex(sha(salt, pass));
    }

    static boolean checkPassword(String pass, String stored) {
        if (stored == null || !stored.contains("$")) return false;
        String[] parts = stored.split("\\$", 2);
        HexFormat hf = HexFormat.of();
        try {
            byte[] salt = hf.parseHex(parts[0]);
            return MessageDigest.isEqual(sha(salt, pass), hf.parseHex(parts[1]));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
