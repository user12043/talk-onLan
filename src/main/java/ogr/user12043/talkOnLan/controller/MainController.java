package ogr.user12043.talkOnLan.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import ogr.user12043.talkOnLan.net.NetworkService;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    private TextField textField_manualIP;

    @FXML
    private Button btn_addManually;

    @FXML
    private Button btn_startStopDiscovery;

    @FXML
    private Button btn_startStopHardDiscovery;

    @FXML
    private Button btn_hostRoom;

    @FXML
    private Button btn_hostPrivateRoom;

    @FXML
    private Button btn_myIP;

    @FXML
    private VBox buddiesPane;

    @FXML
    private VBox roomsPane;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    @FXML
    private void startStopDiscovery(ActionEvent actionEvent) {
        try {
            if (NetworkService.isServiceUp()) {
                NetworkService.end();
                btn_startStopHardDiscovery.setDisable(true);
                btn_hostRoom.setDisable(true);
                btn_hostPrivateRoom.setDisable(true);
                btn_addManually.setDisable(true);
            } else {
                NetworkService.start();
                btn_startStopHardDiscovery.setDisable(false);
                btn_hostRoom.setDisable(false);
                btn_hostPrivateRoom.setDisable(false);
                btn_addManually.setDisable(false);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        btn_startStopDiscovery.setText((NetworkService.isServiceUp() ? "Stop" : "Start") + " Discovery");
    }
}
