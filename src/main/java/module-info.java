module com.example.viennaubahnroutefinder {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.viennaubahnroutefinder to javafx.fxml;
    exports com.example.viennaubahnroutefinder;
    exports com.example.viennaubahnroutefinder.controllers;
    opens com.example.viennaubahnroutefinder.controllers to javafx.fxml;
    exports com.example.viennaubahnroutefinder.model;
    opens com.example.viennaubahnroutefinder.model to javafx.fxml;
    exports com.example.viennaubahnroutefinder.algorithm;
    opens com.example.viennaubahnroutefinder.algorithm to javafx.fxml;
}