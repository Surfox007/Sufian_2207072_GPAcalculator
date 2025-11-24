package com.gpa.app.db;

import com.gpa.app.model.Course;
import com.gpa.app.model.GPAEntry;
import com.gpa.app.model.Student;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton service class to handle all database operations (CRUD) for Students,
 * GPA Entries, and Course Records using SQLite.
 */
public class DatabaseService {

    private static DatabaseService instance;
    private static final String URL = "jdbc:sqlite:gpa_records.db";

    private DatabaseService() {
        initializeDatabase();
    }

    public static synchronized DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    private void initializeDatabase() {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {

            stmt.execute("DROP TABLE IF EXISTS Courses;");
            stmt.execute("DROP TABLE IF EXISTS GPAEntries;");
            stmt.execute("DROP TABLE IF EXISTS Students;");

            stmt.execute("CREATE TABLE Students (" +
                    "student_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "first_name TEXT NOT NULL," +
                    "last_name TEXT NOT NULL," +
                    "UNIQUE (first_name, last_name)" +
                    ");");


            stmt.execute("CREATE TABLE GPAEntries (" +
                    "entry_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "student_id INTEGER NOT NULL," +
                    "gpa_value REAL NOT NULL," +
                    "total_credits REAL NOT NULL," +
                    "date_calculated TEXT NOT NULL," + // Stored as ISO 8601 string
                    "FOREIGN KEY (student_id) REFERENCES Students(student_id) ON DELETE CASCADE" +
                    ");");


            stmt.execute("CREATE TABLE Courses (" +
                    "course_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "entry_id INTEGER NOT NULL," +
                    "course_name TEXT NOT NULL," +
                    "course_code TEXT," +
                    "credit REAL NOT NULL," +
                    "teacher1 TEXT," +
                    "teacher2 TEXT," +
                    "grade_letter TEXT NOT NULL," +
                    "grade_point REAL NOT NULL," +
                    "FOREIGN KEY (entry_id) REFERENCES GPAEntries(entry_id) ON DELETE CASCADE" +
                    ");");

        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
        }
    }


    public Student saveOrGetStudent(String firstName, String lastName) throws SQLException {
        String selectSql = "SELECT student_id FROM Students WHERE first_name = ? AND last_name = ?";
        String insertSql = "INSERT INTO Students (first_name, last_name) VALUES (?, ?)";

        try (Connection conn = connect();
             PreparedStatement selectStmt = conn.prepareStatement(selectSql);
             PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {

            selectStmt.setString(1, firstName);
            selectStmt.setString(2, lastName);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {

                int studentId = rs.getInt("student_id");
                return new Student(studentId, firstName, lastName);
            } else {

                insertStmt.setString(1, firstName);
                insertStmt.setString(2, lastName);
                insertStmt.executeUpdate();

                ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int newId = generatedKeys.getInt(1);
                    return new Student(newId, firstName, lastName);
                } else {
                    throw new SQLException("Creating student failed, no ID obtained.");
                }
            }
        }
    }


    public List<Student> getAllStudents() throws SQLException {
        String sql = "SELECT student_id, first_name, last_name FROM Students ORDER BY first_name, last_name";
        List<Student> students = new ArrayList<>();

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int studentId = rs.getInt("student_id");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                students.add(new Student(studentId, firstName, lastName));
            }
        }
        return students;
    }


    public boolean deleteStudent(int studentId) {
        String sql = "DELETE FROM Students WHERE student_id = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting student ID " + studentId + ": " + e.getMessage());
            return false;
        }
    }



    public int saveGpaEntry(GPAEntry entry, List<Course> courses) throws SQLException {
        String entrySql = "INSERT INTO GPAEntries (student_id, gpa_value, total_credits, date_calculated) VALUES (?, ?, ?, ?)";
        String courseSql = "INSERT INTO Courses (entry_id, course_name, course_code, credit, teacher1, teacher2, grade_letter, grade_point) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        int newEntryId = -1;

        try (Connection conn = connect()) {
            conn.setAutoCommit(false);


            try (PreparedStatement entryStmt = conn.prepareStatement(entrySql, Statement.RETURN_GENERATED_KEYS)) {
                entryStmt.setInt(1, entry.getStudentId());
                entryStmt.setDouble(2, entry.getGpaValue());
                entryStmt.setDouble(3, entry.getTotalCredits());
                entryStmt.setString(4, entry.getDate().toString());
                entryStmt.executeUpdate();

                ResultSet rs = entryStmt.getGeneratedKeys();
                if (rs.next()) {
                    newEntryId = rs.getInt(1);
                } else {
                    throw new SQLException("Creating GPA entry failed, no ID obtained.");
                }
            }


            try (PreparedStatement courseStmt = conn.prepareStatement(courseSql)) {
                for (Course course : courses) {
                    courseStmt.setInt(1, newEntryId);
                    courseStmt.setString(2, course.getCourseName());
                    courseStmt.setString(3, course.getCourseCode());
                    courseStmt.setDouble(4, course.getCredit());
                    courseStmt.setString(5, course.getTeacher1());
                    courseStmt.setString(6, course.getTeacher2());
                    courseStmt.setString(7, course.getGradeLetter());
                    courseStmt.setDouble(8, course.getGradePoint());
                    courseStmt.addBatch();
                }
                courseStmt.executeBatch();
            }

            conn.commit();
            return newEntryId;

        } catch (SQLException e) {

            System.err.println("Transaction failed. Rolling back changes: " + e.getMessage());

            throw e;
        }
    }

    public List<GPAEntry> getAllGpaEntries() throws SQLException {

        String sql = "SELECT e.entry_id, e.student_id, s.first_name, s.last_name, e.gpa_value, e.total_credits, e.date_calculated " +
                "FROM GPAEntries e JOIN Students s ON e.student_id = s.student_id " +
                "ORDER BY e.date_calculated DESC";

        List<GPAEntry> entries = new ArrayList<>();

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int entryId = rs.getInt("entry_id");
                int studentId = rs.getInt("student_id");
                String studentName = rs.getString("first_name");
                String studentRoll = rs.getString("last_name");
                double gpaValue = rs.getDouble("gpa_value");
                double totalCredits = rs.getDouble("total_credits");
                LocalDateTime date = LocalDateTime.parse(rs.getString("date_calculated"));

                entries.add(new GPAEntry(entryId, studentId, studentName, studentRoll, gpaValue, totalCredits, date));
            }
        }
        return entries;
    }


    public List<GPAEntry> getEntriesForStudent(int studentId) throws SQLException {
        String sql = "SELECT e.entry_id, e.gpa_value, e.total_credits, e.date_calculated, s.first_name, s.last_name " +
                "FROM GPAEntries e JOIN Students s ON e.student_id = s.student_id " +
                "WHERE e.student_id = ? ORDER BY e.date_calculated DESC";

        List<GPAEntry> entries = new ArrayList<>();

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            String firstName = null;
            String lastName = null;

            while (rs.next()) {
                if (firstName == null) {
                    firstName = rs.getString("first_name");
                    lastName = rs.getString("last_name");
                }

                int entryId = rs.getInt("entry_id");
                double gpaValue = rs.getDouble("gpa_value");
                double totalCredits = rs.getDouble("total_credits");
                LocalDateTime date = LocalDateTime.parse(rs.getString("date_calculated"));

                entries.add(new GPAEntry(entryId, studentId, firstName, lastName, gpaValue, totalCredits, date));
            }
        }
        return entries;
    }

    public List<Course> getCoursesForEntry(int entryId) throws SQLException {
        String sql = "SELECT course_name, course_code, credit, teacher1, teacher2, grade_letter, grade_point " +
                "FROM Courses WHERE entry_id = ?";

        List<Course> courses = new ArrayList<>();

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, entryId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String name = rs.getString("course_name");
                String code = rs.getString("course_code");
                double credit = rs.getDouble("credit");
                String t1 = rs.getString("teacher1");
                String t2 = rs.getString("teacher2");
                String gradeLetter = rs.getString("grade_letter");
                double gradePoint = rs.getDouble("grade_point");

                courses.add(new Course(name, code, credit, t1, t2, gradeLetter, gradePoint));
            }
        }
        return courses;
    }

    public void deleteGpaEntry(int entryId) throws SQLException {

        String sql = "DELETE FROM GPAEntries WHERE entry_id = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, entryId);
            pstmt.executeUpdate();
        }
    }


    public void deleteLatestGpaEntryByStudentId(int studentId) throws SQLException {

        String selectSql = "SELECT entry_id FROM GPAEntries WHERE student_id = ? ORDER BY date_calculated DESC LIMIT 1";

        try (Connection conn = connect();
             PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {

            selectStmt.setInt(1, studentId);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                int latestEntryId = rs.getInt("entry_id");

                deleteGpaEntry(latestEntryId);

                System.out.println("Deleted GPA entry ID: " + latestEntryId + " for student ID: " + studentId);
            } else {
                System.out.println("No existing GPA entry found for student ID: " + studentId + " to delete.");
            }
        }
    }
}