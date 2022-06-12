package ogr.user12043.talkOnLan;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ogr.user12043.talkOnLan.dao.DBConnection;
import ogr.user12043.talkOnLan.net.NetworkService;
import ogr.user12043.talkOnLan.util.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

public class TalkOnLanApp extends Application {
    private static final Logger LOGGER = LogManager.getLogger(TalkOnLanApp.class);

    private static Stage primaryStage;

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

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        TalkOnLanApp.primaryStage = primaryStage;
        Parent p = new FXMLLoader(getClass().getResource("controller/main.fxml")).load();
        Scene scene = new Scene(p);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("talk-onLan");
        primaryStage.show();
        primaryStage.setOnCloseRequest(windowEvent -> {
            try {
                if (NetworkService.isServiceUp()) {
                    NetworkService.end();
                }
            } catch (IOException ignored) {
            }
            try {
                DBConnection.get().close();
            } catch (SQLException e) {
                LOGGER.error("Error while closing!", e);
            }
            Platform.exit();
            System.exit(0);
        });
    }
}
