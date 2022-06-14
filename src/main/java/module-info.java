module talkOnLan {
    requires java.desktop;
    requires javafx.fxml;
    requires javafx.controls;
    requires java.sql;
//    requires org.xerial.sqlitejdbc; // "Error: automatic module cannot be used with jlink" for "mvn javafx:jlink"
    // either
    //      continue with the jar packaging bt "mvn package"
    //      or add the sqlite to classpath after jlink generation. By adding -cp sqlite-jdbc.jar to the launcher
    opens ogr.user12043.talkOnLan;
    opens ogr.user12043.talkOnLan.controller to javafx.fxml;
}