package ogr.user12043.talkOnLan.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ogr.user12043.talkOnLan.TalkOnLanApp;
import ogr.user12043.talkOnLan.dao.MessageDao;
import ogr.user12043.talkOnLan.model.Message;
import ogr.user12043.talkOnLan.model.User;
import ogr.user12043.talkOnLan.net.DiscoveryService;
import ogr.user12043.talkOnLan.net.MessageService;
import ogr.user12043.talkOnLan.util.Constants;

import java.io.IOException;
import java.net.URL;
import java.util.*;

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

    private Stage chatStage;
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
            this.setDisabled(false);
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
            btn_file.setDisable(true);
            btn_file.setVisible(false);
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
        // Inıtialize the chat window
        try {
            chatStage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("messagePanel.fxml"));
            messagePanelController = new MessagePanelController(this.user, false);
            loader.setController(messagePanelController);
            VBox messagePanel = loader.load();
            Scene scene = new Scene(messagePanel);
            scene.getStylesheets().add(Objects.requireNonNull(TalkOnLanApp.class.getResource("style.css")).toExternalForm());
            chatStage.setScene(scene);
            chatStage.setTitle("Chat with " + user.getUsername());
        } catch (IOException e) {
            throw new RuntimeException("Error while initializing the chat window.", e);
        }
    }

    public void setDisabled(boolean isDisable) {
        btn_file.setDisable(isDisable);
        btn_message.setDisable(isDisable);
    }

    @FXML
    private void messageAction(ActionEvent actionEvent) {
        chatStage.show();
    }

    public void receiveMessage(Message message) {
        this.messagePanelController.receiveMessage(message);
        chatStage.show();
        chatStage.requestFocus();
    }
}
