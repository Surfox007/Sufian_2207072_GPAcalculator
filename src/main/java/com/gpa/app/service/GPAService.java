package com.gpa.app.service;

import com.gpa.app.model.Course;
import java.util.List;

public class GPAService {

    //Calculates the weighted GPA. Formula: Sum(Credit * Grade Point) / Sum(Credit)

    public static double calculateGPA(List<Course> courses) {
        if (courses == null || courses.isEmpty()) {
            return 0.0;
        }

        double totalWeightedPoints = 0.0;
        double totalCredits = 0.0;

        for (Course course : courses) {
            totalWeightedPoints += (course.getCourseCredit() * course.getGradePoint());
            totalCredits += course.getCourseCredit();
        }

        return (totalCredits == 0.0) ? 0.0 : totalWeightedPoints / totalCredits;
    }
}