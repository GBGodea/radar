module com.godea.radiolocator {
    requires javafx.controls;
    requires javafx.fxml;
    exports com.godea.radiolocator to javafx.graphics;
    opens com.godea.radiolocator to javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires com.fazecast.jSerialComm;
}