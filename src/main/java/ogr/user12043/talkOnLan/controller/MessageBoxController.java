package ogr.user12043.talkOnLan.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import ogr.user12043.talkOnLan.model.Message;
import ogr.user12043.talkOnLan.util.Utils;

import java.net.URL;
import java.util.ResourceBundle;

public class MessageBoxController implements Initializable {
    private final Message message;
    private final boolean owned;

    @FXML
    private Label label_info;

    @FXML
    private Label label_message;

    public MessageBoxController(Message message, boolean owned) {
        this.message = message;
        this.owned = owned;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        label_info.setText(label_info.getText().replace("<buddy>", message.getSender().getUsername())
                .replace("<date>", Utils.formatDate(message.getSentDate())));
        label_message.setText(message.getContent());
        if (owned) {
            label_info.setAlignment(Pos.CENTER_RIGHT);
            label_message.setAlignment(Pos.CENTER_RIGHT);
        }
    }
}
