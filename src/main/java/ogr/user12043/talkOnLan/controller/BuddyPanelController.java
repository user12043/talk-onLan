package ogr.user12043.talkOnLan.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import ogr.user12043.talkOnLan.dao.MessageDao;
import ogr.user12043.talkOnLan.model.Message;
import ogr.user12043.talkOnLan.model.User;
import ogr.user12043.talkOnLan.net.DiscoveryService;
import ogr.user12043.talkOnLan.net.MessageService;
import ogr.user12043.talkOnLan.net.NetworkService;
import ogr.user12043.talkOnLan.util.Constants;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class BuddyPanelController implements Initializable {
    private User user;

    @FXML
    private Label label_buddy;

    @FXML
    private Label label_status;

    @FXML
    private Button btn_message;

    @FXML
    private Button btn_file;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        label_buddy.setText(user.getUsername() + " on " + user.getAddress().getHostAddress());
        if (user.isRoom()) {
            btn_file.setDisable(true);
            btn_file.setVisible(false);
        }
    }

    private void refresh() {
        if (user == null) {
            return;
        }
        if (NetworkService.isServiceUp() && DiscoveryService.isOnline(user)) {
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
            label_status.setText(Constants.STATUS_ONLINE);
            label_status.getStyleClass().add("online");
        });
    }

    public void offline() {
        Platform.runLater(() -> {
            label_status.setText(Constants.STATUS_OFFLINE);
            label_status.getStyleClass().remove("online");
        });
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (NetworkService.isServiceUp()) {
                    refresh();
                }
            }
        }, 0, Constants.DISCOVERY_INTERVAL);
    }

    public void setDisabled(boolean isDisable) {
        btn_file.setDisable(isDisable);
        btn_message.setDisable(isDisable);
    }
}
