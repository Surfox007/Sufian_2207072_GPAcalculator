package com.gpa.app.controller;

import com.gpa.app.MainApp;
import com.gpa.app.model.Course;
import com.gpa.app.model.Student;
import com.gpa.app.service.GPAService;
import com.gpa.app.db.DatabaseService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.DecimalFormat;

public class EntryController {

    private final DatabaseService dbService = DatabaseService.getInstance();
    private final GPAService gpaService = new GPAService();

    private final String[] GRADES = {"A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "F"};

    private ObservableList<Course> courseList = FXCollections.observableArrayList();
    private double currentTotalCredits = 0.0;
    private final DecimalFormat df = new DecimalFormat("0.0");
    private double requiredCredits;

    private Student studentToUpdate = null;


    @FXML private TextField studentNameField;
    @FXML private TextField studentRollField;
    @FXML private TextField courseNameField;
    @FXML private TextField courseCodeField;
    @FXML private TextField courseCreditField;
    @FXML private TextField teacher1Field;
    @FXML private TextField teacher2Field;
    @FXML private ComboBox<String> gradeComboBox;
    @FXML private TableView<Course> courseTable;
    @FXML private Label validationLabel;
    @FXML private Label creditSummaryLabel;
    @FXML private Button calculateGpaButton;


    public void initData(String creditValue) {
        if (creditValue != null && !creditValue.isEmpty()) {
            try {
                this.requiredCredits = Double.parseDouble(creditValue);
            } catch (NumberFormatException e) {
                this.requiredCredits = 0.0;
            }
        } else {
            this.requiredCredits = 0.0;
        }
        updateCreditSummary();
        checkGpaCalculationEligibility();
    }

    public void initUpdateData(Student student) {
        this.studentToUpdate = student;

        studentNameField.setText(student.getFirstName());
        studentRollField.setText(student.getLastName());

        studentNameField.setDisable(true);
        studentRollField.setDisable(true);

        calculateGpaButton.setText("Recalculate and Replace GPA");

        validationLabel.setText("Update Mode: Recalculating GPA will REPLACE the student's latest saved record.");
    }


    @FXML
    public void initialize() {
        gradeComboBox.getItems().addAll(GRADES);
        courseTable.setItems(courseList);
        updateCreditSummary();
        checkGpaCalculationEligibility();
    }



    @FXML
    private void addCourse() {

        if (courseNameField == null || courseCreditField == null || gradeComboBox == null) {
            System.err.println("FXML INJECTION FAILURE in addCourse. Check Entry.fxml fx:id attributes.");
            validationLabel.setText("Error: Form initialization failed. Check FXML setup.");
            return;
        }

        String validationMessage = validateInput();
        if (!validationMessage.isEmpty()) {
            validationLabel.setText(validationMessage);
            return;
        }
        validationLabel.setText("");

        try {
            String name = courseNameField.getText().trim();
            String code = courseCodeField.getText().trim();
            double credit = Double.parseDouble(courseCreditField.getText().trim());
            String t1 = teacher1Field.getText().trim();
            String t2 = teacher2Field.getText().trim();
            String grade = gradeComboBox.getValue();

            double gradePoint = convertGradeToPoint(grade);

            Course newCourse = new Course(name, code, credit, t1, t2, grade, gradePoint);
            courseList.add(newCourse);

            currentTotalCredits += credit;
            updateCreditSummary();
            checkGpaCalculationEligibility();
            clearInputFields();

            courseTable.refresh();

        } catch (NumberFormatException e) {
            validationLabel.setText("Course Credit must be a valid number (e.g., 3.0).");
        }
    }


    @FXML
    private void calculateGPA(ActionEvent event) throws IOException {

        if (studentNameField == null || studentRollField == null || validationLabel == null) {
            System.err.println("FATAL: FXML components are null. Ensure fx:id attributes in Entry.fxml match the controller fields.");
            new Alert(Alert.AlertType.ERROR, "Internal error: Form initialization failed. Please check the Entry.fxml file.").showAndWait();
            return;
        }

        if (studentToUpdate == null && (studentNameField.getText().trim().isEmpty() || studentRollField.getText().trim().isEmpty())) {
            validationLabel.setText("Student Name and Roll are required to save the record.");
            return;
        }

        if (courseList.isEmpty()) {
            validationLabel.setText("Please add at least one course before calculating GPA.");
            return;
        }

        String firstName = studentNameField.getText().trim();
        String lastName = studentRollField.getText().trim();

        Student studentRecord;
        if (studentToUpdate != null) {
            studentRecord = studentToUpdate;

            try {

                dbService.deleteLatestGpaEntryByStudentId(studentRecord.getStudentId());
                System.out.println("Previous GPA record for student ID " + studentRecord.getStudentId() + " successfully deleted.");
            } catch (Exception e) {

                System.err.println("WARNING: Failed to delete previous GPA record. Proceeding with new save. Error: " + e.getMessage());
            }


        } else {

            studentRecord = new Student(firstName, lastName);
        }


        boolean saveSuccessful = GPAService.calculateAndSaveGPA(studentRecord, courseList);

        if (!saveSuccessful) {
            new Alert(Alert.AlertType.ERROR, "Failed to save the complete GPA record (GPA entry or course history).").showAndWait();
            return;
        }



        java.net.URL location = MainApp.class.getResource("/com/gpa/app/Dashboard.fxml");
        if (location == null) {
            String errorMessage = "FATAL: Could not find resource /resources/Dashboard.fxml. Check the path and file existence on the classpath.";
            System.err.println(errorMessage);
            new Alert(Alert.AlertType.ERROR, errorMessage).showAndWait();
            return;
        }

        FXMLLoader fxmlLoader = new FXMLLoader(location);

        try {
            Scene dashboardScene = new Scene(fxmlLoader.load());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(dashboardScene);
            stage.setTitle("GPA Calculator - Student Dashboard");
            stage.show();

        } catch (IOException e) {
            validationLabel.setText("Error loading Dashboard.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void updateCreditSummary() {
        if (creditSummaryLabel != null) {
            creditSummaryLabel.setText(String.format("Credits: %s / %s Required", df.format(currentTotalCredits), df.format(requiredCredits)));
        }
    }


    private void checkGpaCalculationEligibility() {
        if (calculateGpaButton != null) {
            boolean isEligible = currentTotalCredits >= requiredCredits;
            calculateGpaButton.setDisable(!isEligible);
        }
    }


    private void clearInputFields() {
        if (courseNameField != null) courseNameField.clear();
        if (courseCodeField != null) courseCodeField.clear();
        if (courseCreditField != null) courseCreditField.clear();
        if (teacher1Field != null) teacher1Field.clear();
        if (teacher2Field != null) teacher2Field.clear();
        if (gradeComboBox != null) gradeComboBox.setValue(null);
    }

    private String validateInput() {
        if (courseNameField.getText().trim().isEmpty() ||
                courseCreditField.getText().trim().isEmpty() ||
                gradeComboBox.getValue() == null) {
            return "Course Name, Credit, and Grade are required fields.";
        }
        return "";
    }

    private double convertGradeToPoint(String grade) {
        return switch (grade.toUpperCase()) {
            case "A+" -> 4.00;
            case "A" -> 3.75;
            case "A-" -> 3.50;
            case "B+" -> 3.25;
            case "B" -> 3.00;
            case "B-" -> 2.75;
            case "C+" -> 2.50;
            case "C" -> 2.25;
            case "C-" -> 2.00;
            case "D+" -> 1.75;
            case "D" -> 1.50;
            case "F" -> 0.00;
            default -> 0.00;
        };
    }
}