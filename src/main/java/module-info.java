module com.example.parallelsearchonstrings {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.opencsv;

    opens com.example.parallelsearchonstrings to javafx.fxml;
    exports com.example.parallelsearchonstrings;
}