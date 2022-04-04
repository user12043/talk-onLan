module talkOnLan {
    requires java.desktop;
    requires javafx.fxml;
    requires javafx.controls;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core; // "Error: automatic module cannot be used with jlink" for "mvn javafx:jlink"
    // will not create application image because of the error above. Continue with the jar packaging bt "mvn package".
    // Will generate custom jre with jlink manually
    requires java.sql;
    requires com.h2database; // "Error: automatic module cannot be used with jlink" for "mvn javafx:jlink"
    opens ogr.user12043.talkOnLan;
}