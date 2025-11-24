package com.gpa.app.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GPAEntry {
    private int entryId;
    private int studentId;
    private String studentName;
    private String studentRoll;
    private double gpaValue;
    private double totalCredits;
    private LocalDateTime date;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public GPAEntry(int entryId, int studentId, String studentName, String studentRoll, double gpaValue, double totalCredits, LocalDateTime date) {
        this.entryId = entryId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentRoll = studentRoll;
        this.gpaValue = gpaValue;
        this.totalCredits = totalCredits;
        this.date = date;
    }


    public GPAEntry(int studentId, String studentName, String studentRoll, double gpaValue, double totalCredits, LocalDateTime date) {
        this(0, studentId, studentName, studentRoll, gpaValue, totalCredits, date);
    }


    public int getEntryId() {
        return entryId;
    }

    public int getStudentId() {
        return studentId;
    }


    public String getStudentName() {
        return studentName;
    }

    public String getStudentRoll() {
        return studentRoll;
    }

    public double getGpaValue() {
        return gpaValue;
    }

    public double getTotalCredits() {
        return totalCredits;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getDateCalculated() {
        return date.format(FORMATTER);
    }
}