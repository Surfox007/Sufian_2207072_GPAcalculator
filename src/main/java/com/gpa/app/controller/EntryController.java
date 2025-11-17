package com.gpa.app.controller;

import com.gpa.app.MainApp;
import com.gpa.app.model.Course;
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

    private final String[] GRADES = {"A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "F"};

    private ObservableList<Course> courseList = FXCollections.observableArrayList();
    private double currentTotalCredits = 0.0;
    private final DecimalFormat df = new DecimalFormat("0.0");
    private double requiredCredits;

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
        this.requiredCredits = Double.parseDouble(creditValue);
        updateCreditSummary();
        checkGpaCalculationEligibility();
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


            Course newCourse = new Course(name, code, credit, t1, t2, grade);
            courseList.add(newCourse);


            currentTotalCredits += credit;
            updateCreditSummary();
            checkGpaCalculationEligibility();

            clearInputFields();

            courseTable.refresh();

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Course '" + name + "' added successfully!", ButtonType.OK);
            alert.setHeaderText(null);
            alert.showAndWait();

        } catch (NumberFormatException e) {
            validationLabel.setText("Course Credit must be a valid number (e.g., 3.0).");
        }
    }


    @FXML
    private void calculateGPA(ActionEvent event) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("Result.fxml"));
        Scene resultScene = new Scene(fxmlLoader.load());



        //Pass data to the ResultController
        ResultController resultController = fxmlLoader.getController();
        resultController.initData(courseList);


        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(resultScene);



        stage.setTitle("GPA Calculator - Results");
        stage.show();
    }


    private void updateCreditSummary() {
        creditSummaryLabel.setText(String.format("Credits: %s / %s Required", df.format(currentTotalCredits), df.format(requiredCredits)));
    }


    private void checkGpaCalculationEligibility() {
        boolean isEligible = currentTotalCredits >= requiredCredits;
        calculateGpaButton.setDisable(!isEligible);
    }


    private void clearInputFields() {
        courseNameField.clear();
        courseCodeField.clear();
        courseCreditField.clear();
        teacher1Field.clear();
        teacher2Field.clear();
        gradeComboBox.setValue(null);
    }

    private String validateInput() {
        if (courseNameField.getText().trim().isEmpty() ||
                courseCreditField.getText().trim().isEmpty() ||
                gradeComboBox.getValue() == null) {
            return "Course Name, Credit, and Grade are required fields.";
        }
        return "";
    }
}