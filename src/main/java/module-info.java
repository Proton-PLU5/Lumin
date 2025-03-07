module me.protonplus.lumin {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires org.jetbrains.annotations;

    opens me.protonplus.lumin to javafx.fxml;
    exports me.protonplus.lumin;
}