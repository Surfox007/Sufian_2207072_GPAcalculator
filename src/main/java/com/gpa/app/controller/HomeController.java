package com.gpa.app.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeController {
    @FXML private TextField totalCreditNumber;
    //@FXML private Label welcomeText;

    @FXML
    public void startCalculator(ActionEvent event) throws IOException {

        String creditValue = totalCreditNumber.getText().trim();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/gpa/app/Entry.fxml"));
        Scene entryScene = new Scene(fxmlLoader.load());

        String cssPath = getClass().getResource("/com/gpa/app/entry_style.css").toExternalForm();

        entryScene.getStylesheets().add(cssPath);

        EntryController entryController = fxmlLoader.getController();
        entryController.initData(creditValue);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(entryScene);
        stage.setTitle("CGPA Calculator - Course Entry");
        stage.show();
    }


}
