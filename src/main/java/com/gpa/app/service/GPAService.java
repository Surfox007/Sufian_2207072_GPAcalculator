package com.gpa.app.service;

import com.gpa.app.db.GPARepository;
import com.gpa.app.model.Course;
import com.gpa.app.model.GPAEntry;
import com.gpa.app.model.Student;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;


public class GPAService {


    public static double calculateGPA(List<Course> courses) {
        if (courses == null || courses.isEmpty()) {
            return 0.0;
        }

        double totalWeightedPoints = 0.0;
        double totalCredits = 0.0;

        for (Course course : courses) {
            totalWeightedPoints += course.getGradePoint() * course.getCredit();
            totalCredits += course.getCredit();
        }

        return totalCredits > 0 ? totalWeightedPoints / totalCredits : 0.0;
    }

    public static boolean calculateAndSaveGPA(Student student, List<Course> courses) {
        try {

            double gpaValue = calculateGPA(courses);
            double totalCredits = courses.stream().mapToDouble(Course::getCredit).sum();

            Student savedStudent = GPARepository.saveOrGetStudent(student.getFirstName(), student.getLastName());


            GPAEntry newEntry = new GPAEntry(
                    0,
                    savedStudent.getStudentId(),
                    savedStudent.getFirstName(),
                    savedStudent.getLastName(),
                    gpaValue,
                    totalCredits,
                    LocalDateTime.now()
            );

            int entryId = GPARepository.saveFullGpaRecord(newEntry, courses);

            return entryId != -1;

        } catch (SQLException e) {
            System.err.println("GPAService: Failed during calculation and save. Error: " + e.getMessage());
            return false;
        }
    }
}