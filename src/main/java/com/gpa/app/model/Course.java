package com.gpa.app.model;

public class Course {
    private String courseName;
    private String courseCode;
    private double courseCredit;
    private String teacher1Name;
    private String teacher2Name;
    private String grade;

    public Course(String courseName, String courseCode, double courseCredit, String teacher1Name, String teacher2Name, String grade) {
        this.courseName = courseName;
        this.courseCode = courseCode;
        this.courseCredit = courseCredit;
        this.teacher1Name = teacher1Name;
        this.teacher2Name = teacher2Name;
        this.grade = grade;
    }

//Grade point conversion
    public double getGradePoint() {
        return switch (grade.toUpperCase()) {
            case "A+", "A" -> 4.0;
            case "A-" -> 3.7;
            case "B+" -> 3.3;
            case "B" -> 3.0;
            case "B-" -> 2.7;
            case "C+" -> 2.3;
            case "C" -> 2.0;
            case "C-" -> 1.7;
            case "D+" -> 1.3;
            case "D" -> 1.0;
            case "F" -> 0.0;
            default -> 0.0;
        };
    }

    //Getters for TableView
    public String getCourseName() { return courseName; }
    public String getCourseCode() { return courseCode; }
    public double getCourseCredit() { return courseCredit; }
    public String getTeacher1Name() { return teacher1Name; }
    public String getTeacher2Name() { return teacher2Name; }
    public String getGrade() { return grade; }

    //Special getter for the Result Table
    public double getGradePointDisplay() { return getGradePoint(); }
}