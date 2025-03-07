open module me.protonplus.lumin {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires org.jetbrains.annotations;
    requires java.desktop;
    requires com.google.gson;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    requires porcupine.java;
    requires cheetah.java;

    uses org.apache.logging.log4j.spi.Provider;

    exports me.protonplus.lumin;
}