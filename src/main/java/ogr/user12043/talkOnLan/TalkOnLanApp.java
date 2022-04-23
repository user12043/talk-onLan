package ogr.user12043.talkOnLan;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ogr.user12043.talkOnLan.util.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class TalkOnLanApp extends Application {
    private static final Logger LOGGER = LogManager.getLogger(TalkOnLanApp.class);

    public static void main(String[] args) {
        try {
            System.setProperty("java.net.preferIPv4Stack", "true"); // Set prefer to ipv4 addresses in java
            Utils.initDatabase();
            Utils.saveSelf();
            Utils.saveSelfRoom();
        } catch (Exception e) {
            LOGGER.error("Unexpected error!", e);
        }
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent p = new FXMLLoader(getClass().getResource("/fxml/main.fxml")).load();
        primaryStage.setScene(new Scene(p));
        primaryStage.show();
    }
}
