module com.example.viennaubahnroutefinder {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.viennaubahnroutefinder to javafx.fxml;
    exports com.example.viennaubahnroutefinder;
}