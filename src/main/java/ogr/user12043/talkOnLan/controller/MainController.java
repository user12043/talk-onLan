package ogr.user12043.talkOnLan.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import ogr.user12043.talkOnLan.dao.UserDao;
import ogr.user12043.talkOnLan.model.Message;
import ogr.user12043.talkOnLan.model.User;
import ogr.user12043.talkOnLan.net.NetworkService;
import ogr.user12043.talkOnLan.util.Utils;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InterfaceAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    private static MainController instance;
    private final Map<User, BuddyPanelController> buddies = new HashMap<>();
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
    private Button btn_hostAddresses;
    @FXML
    private VBox buddiesPane;
    @FXML
    private VBox roomsPane;

    public static MainController getInstance() {
        return instance;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        instance = this;
        addUsers();
        buddies.forEach((u, c) -> c.setDisabled(true));
    }

    private void addUsers() {
        List<User> users = UserDao.get().find();
        users.forEach(user -> {
            if (!user.equals(Utils.self()) && !user.equals(Utils.selfRoom())) {
                addUser(user);
            }
        });
    }

    public void addUser(User user) {
        try {
            User existing = UserDao.get().findByFields(user);
            if (existing == null) {
                UserDao.get().save(user);
            }

            user = UserDao.get().findByFields(user);
            Utils.addUser(user);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("buddyPanel.fxml"));
            BuddyPanelController buddyPanelController = new BuddyPanelController(user);
            loader.setController(buddyPanelController);
            AnchorPane buddyPanel = loader.load();
            // Keep the references
            buddies.put(user, buddyPanelController);
            if (!user.isRoom()) {
                buddiesPane.getChildren().add(buddyPanel);
            } else {
                roomsPane.getChildren().add(buddyPanel);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void startStopDiscoveryAction(ActionEvent actionEvent) {
        btn_startStopDiscovery.setDisable(true);
        try {
            if (NetworkService.isServiceUp()) {
                btn_startStopHardDiscovery.setDisable(true);
                btn_hostRoom.setDisable(true);
                btn_hostPrivateRoom.setDisable(true);
                btn_addManually.setDisable(true);
                NetworkService.end();
            } else {
                btn_startStopHardDiscovery.setDisable(false);
                btn_hostRoom.setDisable(false);
                btn_hostPrivateRoom.setDisable(false);
                btn_addManually.setDisable(false);
                NetworkService.start();
            }
        } catch (IOException e) {
            btn_startStopHardDiscovery.setDisable(true);
            btn_hostRoom.setDisable(true);
            btn_hostPrivateRoom.setDisable(true);
            btn_addManually.setDisable(true);
            throw new RuntimeException(e);
        }
        btn_startStopDiscovery.setText((NetworkService.isServiceUp() ? "Stop" : "Start") + " Discovery");
        btn_startStopDiscovery.setDisable(false);
    }

    @FXML
    private void hostAddressesAction(ActionEvent actionEvent) {
        Utils.initInterfaces();
        StringBuilder builder = new StringBuilder();
        for (InterfaceAddress hostAddress : Utils.hostAddresses) {
            if (hostAddress.getAddress() instanceof Inet4Address) {
                final String address = hostAddress.getAddress().toString();
                builder.append(address.substring(1)).append("\n");
            }
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Local IP addresses");
        alert.setHeaderText(builder.toString());
        alert.showAndWait();
    }

    public void receiveMessage(Message message) {
        buddies.get(message.getSender()).receiveMessage(message);
    }

    private MessagePanelController getMessagePanelOfUser(User sender) {
        return null;
    }
}
