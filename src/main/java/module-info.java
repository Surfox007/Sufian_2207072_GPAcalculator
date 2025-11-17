module com.gpa.app {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires javafx.base;
    requires javafx.graphics;

    opens com.gpa.app to javafx.fxml;
    opens com.gpa.app.model to javafx.base;
    exports com.gpa.app;
    exports com.gpa.app.controller;
    opens com.gpa.app.controller to javafx.fxml;
}