package com.gpa.app.controller;

import com.gpa.app.MainApp;
import com.gpa.app.db.GPARepository;
import com.gpa.app.model.Course;
import com.gpa.app.model.Student;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;

public class DashboardController {

    private final DecimalFormat df = new DecimalFormat("0.00");
    private Student selectedStudent = null;

    @FXML private ListView<Student> studentListView;
    @FXML private Label studentNameLabel;
    @FXML private Label studentRollLabel;
    @FXML private Label gpaValueLabel;
    @FXML private Button updateGradesButton;
    @FXML private Button deleteRecordButton;

    @FXML private TableView<Course> detailedResultTable;
    @FXML private TableColumn<Course, String> courseCodeCol;
    @FXML private TableColumn<Course, String> courseNameCol;
    @FXML private TableColumn<Course, Double> creditsCol;
    @FXML private TableColumn<Course, String> gradeCol;
    @FXML private TableColumn<Course, Double> gpaCol;

    @FXML
    public void initialize() {

        courseCodeCol.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        courseNameCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        creditsCol.setCellValueFactory(new PropertyValueFactory<>("credit"));
        gradeCol.setCellValueFactory(new PropertyValueFactory<>("gradeLetter"));
        gpaCol.setCellValueFactory(new PropertyValueFactory<>("gradePoint"));

        updateGradesButton.setDisable(true);
        deleteRecordButton.setDisable(true);


        loadStudents();
        studentListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                selectedStudent = newValue;
                loadStudentDetails(newValue);
                updateGradesButton.setDisable(false);
                deleteRecordButton.setDisable(false);
            } else {
                clearDetails();
                updateGradesButton.setDisable(true);
                deleteRecordButton.setDisable(true);
            }
        });
    }

    private void loadStudents() {
        try {
            List<Student> students = GPARepository.getAllStudents();
            ObservableList<Student> observableStudents = FXCollections.observableArrayList(students);
            studentListView.setItems(observableStudents);
        } catch (SQLException e) {
            System.err.println("Error loading students: " + e.getMessage());
            showAlert("Database Error", "Failed to load student list.", Alert.AlertType.ERROR);
        }
    }


    private void loadStudentDetails(Student student) {
        studentNameLabel.setText(student.getFirstName());
        studentRollLabel.setText(student.getLastName());

        try {
            double latestGpa = GPARepository.getLatestGpaValue(student.getStudentId());
            gpaValueLabel.setText(df.format(latestGpa));

            List<Course> courses = GPARepository.getLatestGpaEntryWithCourses(student.getStudentId());
            detailedResultTable.setItems(FXCollections.observableArrayList(courses));

        } catch (SQLException e) {
            System.err.println("Error loading student details: " + e.getMessage());
            showAlert("Database Error", "Failed to load GPA and course details.", Alert.AlertType.ERROR);
            clearDetails();
        }
    }

    private void clearDetails() {
        studentNameLabel.setText("[Select a Student]");
        studentRollLabel.setText("[Roll Number]");
        gpaValueLabel.setText("0.00");
        detailedResultTable.getItems().clear();
    }


    @FXML
    private void handleCreateNewStudent(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("/com/gpa/app/Entry.fxml"));
            Scene entryScene = new Scene(fxmlLoader.load());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(entryScene);
            stage.setTitle("GPA Calculator - New Entry");
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading Entry.fxml: " + e.getMessage());
            showAlert("Navigation Error", "Could not load the new entry screen.", Alert.AlertType.ERROR);
        }
    }


    @FXML
    private void handleUpdateGrades(ActionEvent event) {
        if (selectedStudent == null) {
            showAlert("Selection Required", "Please select a student record to update.", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("/com/gpa/app/Entry.fxml"));
            Scene entryScene = new Scene(fxmlLoader.load());

            EntryController controller = fxmlLoader.getController();


            List<Course> existingCourses = GPARepository.getLatestGpaEntryWithCourses(selectedStudent.getStudentId());


            controller.initUpdateData(selectedStudent);


            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(entryScene);
            stage.setTitle("GPA Calculator - Update Entry for " + selectedStudent.getFirstName());
            stage.show();
        } catch (Exception e) {
            System.err.println("Error loading update screen: " + e.getMessage());
            e.printStackTrace();
            showAlert("Navigation Error", "Could not load the update screen.", Alert.AlertType.ERROR);
        }
    }


    @FXML
    private void handleDeleteRecord() {
        if (selectedStudent == null) {
            showAlert("Selection Required", "Please select a student record to delete.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to permanently delete the entire record for " + selectedStudent.getFirstName() + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Deletion");


        Platform.runLater(() -> {
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    try {
                        if (GPARepository.deleteStudent(selectedStudent.getStudentId())) {
                            showAlert("Success", "Student record deleted successfully.", Alert.AlertType.INFORMATION);
                            loadStudents();
                            clearDetails();
                        } else {
                            showAlert("Failure", "Failed to delete student record.", Alert.AlertType.ERROR);
                        }
                    } catch (Exception e) {
                        System.err.println("Deletion error: " + e.getMessage());
                        showAlert("Database Error", "An error occurred during deletion.", Alert.AlertType.ERROR);
                    }
                }
            });
        });
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}