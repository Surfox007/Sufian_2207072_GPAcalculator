package com.gpa.app.model;


public class Course {
    private String courseName;
    private String courseCode;
    private double courseCredit;
    private String teacher1Name;
    private String teacher2Name;
    private String gradeLetter;
    private double gradePoint;

    public Course(String courseName, String courseCode, double courseCredit, String teacher1Name, String teacher2Name, String gradeLetter, double gradePoint) {
        this.courseName = courseName;
        this.courseCode = courseCode;
        this.courseCredit = courseCredit;
        this.teacher1Name = teacher1Name;
        this.teacher2Name = teacher2Name;
        this.gradeLetter = gradeLetter;
        this.gradePoint = gradePoint;
    }


    public String getCourseName() { return courseName; }
    public String getCourseCode() { return courseCode; }

    public double getCredit() { return courseCredit; }

    public String getGradeLetter() { return gradeLetter; }

    public String getTeacher1() { return teacher1Name; }

    public String getTeacher2() { return teacher2Name; }

    public double getGradePoint() {
        return gradePoint;
    }


    public double getGradePointDisplay() {
        return gradePoint;
    }


    public double getCourseCredit() {
        return courseCredit;
    }


    public String getGrade() {
        return gradeLetter;
    }

    public String getTeacher1Name() {
        return teacher1Name;
    }

    public String getTeacher2Name() {
        return teacher2Name;
    }
}