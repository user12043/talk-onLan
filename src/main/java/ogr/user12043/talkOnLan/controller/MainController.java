package ogr.user12043.talkOnLan.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import ogr.user12043.talkOnLan.dao.MessageDao;
import ogr.user12043.talkOnLan.dao.UserDao;
import ogr.user12043.talkOnLan.model.Message;
import ogr.user12043.talkOnLan.model.User;
import ogr.user12043.talkOnLan.net.DiscoveryService;
import ogr.user12043.talkOnLan.net.NetworkService;
import ogr.user12043.talkOnLan.util.Constants;
import ogr.user12043.talkOnLan.util.Properties;
import ogr.user12043.talkOnLan.util.Utils;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

public class MainController implements Initializable {
    private static MainController instance;
    private final Map<User, BuddyPanelController> buddies = new HashMap<>();
    private final Map<User, AnchorPane> buddyPanels = new HashMap<>();
    private final Map<User, MessagePanelController> privateBuddyPanels = new HashMap<>();
    private MessagePanelController roomMessagePanel;
    private MessagePanelController privateRoomMessagePanel;
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

    @FXML
    private Hyperlink link_manageBlacklist;

    public static MainController getInstance() {
        return instance;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        instance = this;
        addUsers();
        // Initialize room and private room windows
        roomMessagePanel = new MessagePanelController(Utils.selfRoom(), false);
        privateRoomMessagePanel = new MessagePanelController(Utils.selfRoom(), true);
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
            // Ignore blocked buddies
            if (user.isBlocked()) {
                return;
            }
            Utils.addUser(user);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("buddyPanel.fxml"));
            BuddyPanelController buddyPanelController = new BuddyPanelController(user);
            loader.setController(buddyPanelController);
            AnchorPane buddyPanel = loader.load();
            // Keep the references
            buddies.put(user, buddyPanelController);
            buddyPanels.put(user, buddyPanel);
            if (!user.isRoom()) {
                buddiesPane.getChildren().add(buddyPanel);
            } else {
                roomsPane.getChildren().add(buddyPanel);
            }

            // Add remove option
            MenuItem removeItem = new MenuItem("Remove buddy and add to the blacklist");
            final ContextMenu contextMenu = new ContextMenu(removeItem);
            removeItem.setOnAction(event -> removeUser(buddyPanelController.getUser()));
            buddyPanel.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.SECONDARY) {
                    contextMenu.show(buddyPanel, event.getScreenX(), event.getScreenY());
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Remove and add to blacklist
    protected void removeUser(User user) {
        user.setBlocked(true);
        UserDao.get().save(user);
        MessageDao.get().clearAll(user);
        Utils.removeUser(user);
        buddies.remove(user);
        if (!user.isRoom()) {
            buddiesPane.getChildren().remove(buddyPanels.get(user));
        } else {
            roomsPane.getChildren().remove(buddyPanels.get(user));
        }
        buddyPanels.remove(user);
    }

    @FXML
    private void startStopDiscoveryAction(ActionEvent actionEvent) {
        btn_startStopDiscovery.setDisable(true);
        try {
            if (NetworkService.isServiceUp()) {
                btn_startStopHardDiscovery.setDisable(true);
                btn_addManually.setDisable(true);
                NetworkService.end();
            } else {
                btn_startStopHardDiscovery.setDisable(false);
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
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Local IP addresses");
        alert.setHeaderText(builder.toString());
        alert.showAndWait();
    }

    public void receiveMessage(Message message) {
        switch (message.getMessageType()) {
            case Constants.MSG_TYPE_ROOM:
                // Received the message to hosted room
                roomMessagePanel.receiveMessage(message);
                break;
            case Constants.MSG_TYPE_PRIVATE_ROOM:
                // Received the message to hosted private room
                privateRoomMessagePanel.receiveMessage(message);
                break;
            case Constants.MSG_TYPE_FWD_PRIVATE:
                // Received the message to remote private room
                MessagePanelController privateBuddyMessagePanelController;
                // Check for panel
                if ((privateBuddyMessagePanelController = privateBuddyPanels.get(message.getSender())) == null) {
                    privateBuddyMessagePanelController = new MessagePanelController(message.getSender(), true);
                    privateBuddyPanels.put(message.getSender(), privateBuddyMessagePanelController);
                }
                // pass the message
                privateBuddyMessagePanelController.receiveMessage(message);
                break;
            default:
                // Received one-to-one message
                buddies.get(message.getSender()).receiveMessage(message);
                break;
        }
        if (message.getMessageType() != Constants.MSG_TYPE_PRIVATE_ROOM &&
                message.getMessageType() != Constants.MSG_TYPE_FWD_PRIVATE) {
            MessageDao.get().save(message);
        }
    }

    @FXML
    private void hostRoomAction(ActionEvent actionEvent) {
        if (!Properties.roomMode) {
            Properties.roomMode = true;
            Utils.rooms.add(Utils.selfRoom());
            roomMessagePanel.receiveMessage(new Message(Utils.self(), Utils.self(), "You just started a room!", new Date(), Constants.MSG_TYPE_ROOM));
            btn_hostRoom.setText("Stop the Room");
        } else {
            Properties.roomMode = false;
            Utils.rooms.remove(Utils.selfRoom());
            btn_hostRoom.setText("Host a Room");
        }
    }

    @FXML
    private void hostPrivateRoomAction(ActionEvent actionEvent) {
        Alert alert = new Alert(AlertType.WARNING, "This function is not implemented yet!");
        alert.showAndWait();
        /*if (Utils.buddies.stream().noneMatch(User::isOnline)) {
            // show warning like "There is no buddy online!" then return
        }*/
        /* TODO:
         * Make selection from online buddies
         * create new message panel
         * add selected buddies as participants
         * */
    }

    @FXML
    private void hardDiscoveryAction(ActionEvent actionEvent) {
        try {
            DiscoveryService.hardDiscovery();
        } catch (IOException e) {
            new Alert(AlertType.ERROR, "Error on discovery!").showAndWait();
            e.printStackTrace();
        }
    }

    @FXML
    private void addManuallyAction(ActionEvent actionEvent) {
        final TextInputDialog dialog = new TextInputDialog("Enter target IP address...");
        dialog.setContentText("Enter target IP address:");
        dialog.setHeaderText("Add Manually");
        final Optional<String> s = dialog.showAndWait();
        if (s.isPresent()) {
            InetAddress address;
            try {
                address = InetAddress.getByName(s.get());
            } catch (UnknownHostException e) {
                new Alert(AlertType.ERROR, "Invalid address!").show();
                return;
            }
            try {
                DiscoveryService.sendDiscoveryRequest(address);
            } catch (Exception e) {
                new Alert(AlertType.ERROR, "Error during discovery!: " + e.getMessage());
            }
        }
    }

    @FXML
    private void manageBlackListAction(ActionEvent actionEvent) {
        final Dialog<ButtonType> dialog = new Dialog<>();
        dialog.getDialogPane().getButtonTypes().addAll(
                new ButtonType("OK", ButtonData.APPLY),
                new ButtonType("Cancel", ButtonData.CANCEL_CLOSE)
        );
        dialog.setHeaderText("Manage the Blacklist");
        dialog.setContentText("Select the buddies that you want to remove from blacklist");

        VBox vBox = new VBox();
        final List<User> users = UserDao.get().findBlocked();
        List<User> unBlockedUsers = new ArrayList<>();

        final List<CheckBox> checkBoxes = users.stream().map(user -> {
            final CheckBox checkBox = new CheckBox(
                    String.format("%s %s", user.getUsername(), user.isRoom() ? "(Room)" : ""));
            checkBox.setOnAction(event -> {
                if (checkBox.isSelected()) {
                    unBlockedUsers.add(user);
                } else {
                    unBlockedUsers.remove(user);
                }
            });
            return checkBox;
        }).collect(Collectors.toUnmodifiableList());

        vBox.getChildren().addAll(checkBoxes);
        dialog.setGraphic(vBox);
        dialog.showAndWait().filter(response -> response.getButtonData().equals(ButtonData.APPLY)).ifPresent(r -> {
            unBlockedUsers.forEach(user -> user.setBlocked(false));
            unBlockedUsers.forEach(UserDao.get()::save);
            unBlockedUsers.forEach(this::addUser);
        });
    }
}
