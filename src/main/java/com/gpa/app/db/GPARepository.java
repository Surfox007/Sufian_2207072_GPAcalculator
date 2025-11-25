package com.gpa.app.db;

import com.gpa.app.model.Course;
import com.gpa.app.model.GPAEntry;
import com.gpa.app.model.Student;

import java.sql.SQLException;
import java.util.List;


public class GPARepository {

    private static final DatabaseService db = DatabaseService.getInstance();


    public static Student saveOrGetStudent(String firstName, String lastName) throws SQLException {
        return db.saveOrGetStudent(firstName, lastName);
    }


    public static int saveFullGpaRecord(GPAEntry entry, List<Course> courses) {
        try {
            return db.saveGpaEntry(entry, courses);
        } catch (SQLException e) {
            System.err.println("GPARepository: Failed to save full GPA record. Error: " + e.getMessage());
            return -1;
        }
    }


    public static List<Student> getAllStudents() throws SQLException {
        return db.getAllStudents();
    }


    public static void deleteLatestGpaEntry(int studentId) throws SQLException {
        db.deleteLatestGpaEntryByStudentId(studentId);
    }


    public static boolean deleteStudent(int studentId) {
        try {
            return db.deleteStudent(studentId);
        } catch (Exception e) {
            System.err.println("GPARepository: Failed to delete student record. Error: " + e.getMessage());
            return false;
        }
    }


    public static List<Course> getLatestGpaEntryWithCourses(int studentId) throws SQLException {

        List<GPAEntry> entries = db.getEntriesForStudent(studentId);

        if (entries.isEmpty()) {
            return List.of();
        }


        int latestEntryId = entries.get(0).getEntryId();


        return db.getCoursesForEntry(latestEntryId);
    }


    public static double getLatestGpaValue(int studentId) throws SQLException {
        List<GPAEntry> entries = db.getEntriesForStudent(studentId);
        if (entries.isEmpty()) {
            return 0.0;
        }
        // Return the GPA value of the latest entry
        return entries.get(0).getGpaValue();
    }
}