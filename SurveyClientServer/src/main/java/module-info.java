module com.example.surveyclientserver {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens com.example.surveyclientserver to javafx.fxml;
    exports com.example.surveyclientserver;
}