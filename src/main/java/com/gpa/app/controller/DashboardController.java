package com.gpa.app.controller;

import com.gpa.app.MainApp;
import com.gpa.app.db.GPARepository;
import com.gpa.app.model.Course;
import com.gpa.app.model.Student;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
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
    @FXML private Button createNewButton;


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


        setInteractiveButtonsDisabled(true);


        loadStudents();

        studentListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                selectedStudent = newValue;
                loadStudentDetails(newValue); // Load details asynchronously
                setInteractiveButtonsDisabled(false);
            } else {
                clearDetails();
                setInteractiveButtonsDisabled(true);
            }
        });
    }

    private void setInteractiveButtonsDisabled(boolean disabled) {
        updateGradesButton.setDisable(disabled);
        deleteRecordButton.setDisable(disabled);
    }


    private void loadStudents() {

        studentListView.setDisable(true);
        createNewButton.setDisable(true);

        Task<List<Student>> loadTask = new Task<>() {
            @Override
            protected List<Student> call() throws SQLException {

                return GPARepository.getAllStudents();
            }
        };

        loadTask.setOnSucceeded(e -> {

            studentListView.setDisable(false);
            createNewButton.setDisable(false);
            ObservableList<Student> observableStudents = FXCollections.observableArrayList(loadTask.getValue());
            studentListView.setItems(observableStudents);
        });

        loadTask.setOnFailed(e -> {
            studentListView.setDisable(false);
            createNewButton.setDisable(false);
            Throwable exception = loadTask.getException();
            System.err.println("Error loading students: " + exception.getMessage());
            showAlert("Database Error", "Failed to load student list.", Alert.AlertType.ERROR);
        });

        new Thread(loadTask).start();
    }


    private void loadStudentDetails(Student student) {
        // Show loading state
        studentNameLabel.setText(student.getFirstName());
        studentRollLabel.setText(student.getLastName());
        gpaValueLabel.setText("Loading...");
        detailedResultTable.getItems().clear();
        setInteractiveButtonsDisabled(true);

        Task<LoadDetailsResult> loadDetailsTask = new Task<>() {
            @Override
            protected LoadDetailsResult call() throws SQLException {

                double latestGpa = GPARepository.getLatestGpaValue(student.getStudentId());
                List<Course> courses = GPARepository.getLatestGpaEntryWithCourses(student.getStudentId());
                return new LoadDetailsResult(latestGpa, courses);
            }
        };

        loadDetailsTask.setOnSucceeded(e -> {

            LoadDetailsResult result = loadDetailsTask.getValue();
            gpaValueLabel.setText(df.format(result.gpa));
            detailedResultTable.setItems(FXCollections.observableArrayList(result.courses));
            setInteractiveButtonsDisabled(false);
        });

        loadDetailsTask.setOnFailed(e -> {

            setInteractiveButtonsDisabled(false);
            Throwable exception = loadDetailsTask.getException();
            System.err.println("Error loading student details: " + exception.getMessage());
            showAlert("Database Error", "Failed to load GPA and course details.", Alert.AlertType.ERROR);
            clearDetails();
        });

        new Thread(loadDetailsTask).start();
    }

    private static class LoadDetailsResult {
        final double gpa;
        final List<Course> courses;

        LoadDetailsResult(double gpa, List<Course> courses) {
            this.gpa = gpa;
            this.courses = courses;
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
                    setInteractiveButtonsDisabled(true);

                    Task<Boolean> deleteTask = new Task<>() {
                        @Override
                        protected Boolean call() throws Exception {

                            return GPARepository.deleteStudent(selectedStudent.getStudentId());
                        }
                    };

                    deleteTask.setOnSucceeded(e -> {
                        if (deleteTask.getValue()) {
                            showAlert("Success", "Student record deleted successfully.", Alert.AlertType.INFORMATION);
                            loadStudents(); // Refresh the list asynchronously
                            clearDetails();
                        } else {
                            showAlert("Failure", "Failed to delete student record.", Alert.AlertType.ERROR);
                            setInteractiveButtonsDisabled(false);
                        }
                    });

                    deleteTask.setOnFailed(e -> {

                        setInteractiveButtonsDisabled(false);
                        Throwable exception = deleteTask.getException();
                        System.err.println("Deletion error: " + exception.getMessage());
                        showAlert("Database Error", "An error occurred during deletion: " + exception.getMessage(), Alert.AlertType.ERROR);
                    });

                    new Thread(deleteTask).start();
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