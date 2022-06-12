package ogr.user12043.talkOnLan.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import ogr.user12043.talkOnLan.TalkOnLanApp;
import ogr.user12043.talkOnLan.dao.MessageDao;
import ogr.user12043.talkOnLan.model.Message;
import ogr.user12043.talkOnLan.model.User;
import ogr.user12043.talkOnLan.net.DiscoveryService;
import ogr.user12043.talkOnLan.net.FileTransferService;
import ogr.user12043.talkOnLan.net.MessageService;
import ogr.user12043.talkOnLan.util.Constants;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class BuddyPanelController implements Initializable {
    private final SimpleStringProperty statusProperty = new SimpleStringProperty("OFFLINE");
    private final User user;
    @FXML
    private Label label_buddy;
    @FXML
    private Label label_status;
    @FXML
    private Button btn_message;
    @FXML
    private Button btn_file;
    @FXML
    private HBox hBox_info;
    private MessagePanelController messagePanelController;

    public BuddyPanelController(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    private void refresh() {
        if (DiscoveryService.updateOnlineStatus(user)) {
            online();
            // retrieve this user's messages and send unsent ones
            List<Message> messages = MessageDao.get().findUnsentByReceiver(user);
            for (Message message : messages) {
                MessageService.sendMessage(message);
            }
        } else {
            offline();
        }
    }

    public void online() {
        Platform.runLater(() -> {
            statusProperty.set(Constants.STATUS_ONLINE);
            label_status.getStyleClass().removeAll("offline");
            label_status.getStyleClass().add("online");
        });
    }

    public void offline() {
        Platform.runLater(() -> {
            statusProperty.set(Constants.STATUS_OFFLINE);
            label_status.getStyleClass().removeAll("online");
            label_status.getStyleClass().add("offline");
        });
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set label and buttons
        label_buddy.setText(user.getUsername() + " on " + user.getAddress().getHostAddress());
        if (user.isRoom()) {
            hBox_info.getChildren().remove(btn_file);
        }
        // Initialize and start the refresh job
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                refresh();
            }
        }, 0, Constants.DISCOVERY_INTERVAL);
        label_status.textProperty().bindBidirectional(statusProperty);
        messagePanelController = new MessagePanelController(this.user, false);
    }

    @FXML
    private void messageAction(ActionEvent actionEvent) {
        messagePanelController.show();
    }

    @FXML
    private void fileAction(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        final File file = chooser.showOpenDialog(TalkOnLanApp.getPrimaryStage());
        if (file != null) {
            new Thread(() -> {
                try {
                    FileTransferService.sendFile(user, file);
                } catch (IOException e) {
                    Platform.runLater(() -> new Alert(AlertType.ERROR, ("Unable to send file:\n" + e)).showAndWait());
                }
            }).start();
        }
    }

    public void receiveMessage(Message message) {
        this.messagePanelController.receiveMessage(message);
    }
}
