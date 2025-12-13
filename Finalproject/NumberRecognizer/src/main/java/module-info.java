module com.ni.numberrecognizer {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires javafx.swing;
    requires java.desktop;

    opens com.ni.numberrecognizer to javafx.fxml;
    exports com.ni.numberrecognizer;
}
