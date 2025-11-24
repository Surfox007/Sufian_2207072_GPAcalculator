package com.gpa.app.controller;

import com.gpa.app.model.Course;
import com.gpa.app.service.GPAService;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

public class ResultController {

    @FXML private Label gpaResultLabel;
    @FXML private TableView<Course> resultTable;


    public void initData(ObservableList<Course> courses) {
        if (courses == null || courses.isEmpty()) {
            gpaResultLabel.setText("N/A");
            return;
        }

        double finalGPA = GPAService.calculateGPA(courses);
        gpaResultLabel.setText(String.format("%.2f", finalGPA));

        resultTable.setItems(courses);
    }
}