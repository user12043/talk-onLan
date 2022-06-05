package ogr.user12043.talkOnLan.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import ogr.user12043.talkOnLan.dao.MessageDao;
import ogr.user12043.talkOnLan.model.Message;
import ogr.user12043.talkOnLan.model.User;
import ogr.user12043.talkOnLan.net.MessageService;
import ogr.user12043.talkOnLan.util.Constants;
import ogr.user12043.talkOnLan.util.Utils;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class MessagePanelController implements Initializable {

    private final User user;
    @FXML
    private Label label_title;
    @FXML
    private GridPane gridPane_messages;
    @FXML
    private TextArea textArea_input;
    @FXML
    private Button btn_send;

    private final boolean isPrivateChat;

    private final Set<MessageBoxController> messageBoxes = new HashSet<>();

    public MessagePanelController(User user, boolean isPrivateChat) {
        this.user = user;
        this.isPrivateChat = isPrivateChat;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        label_title.setText((user.isRoom() ? "Room of " + user.getUsername() : user.getUsername()) + " on " + user.getAddress() +
                (isPrivateChat ? "(Private)" : ""));
        if (!isPrivateChat) {
            fetchMessages();
        }
    }

    private void fetchMessages() {
        List<Message> messages;
        if (user.equals(Utils.selfRoom())) {
            messages = MessageDao.get().findSelfRoomConversation();
        } else if (user.isRoom()) {
            messages = MessageDao.get().findRoomConversation(user);
        } else {
            messages = MessageDao.get().findConversation(Utils.self(), user);
        }
        for (Message message : messages) {
            addMessage(message, message.getSender().equals(Utils.self()));
        }
    }

    private void addMessage(Message message, boolean owned) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("messageBox.fxml"));
            MessageBoxController controller = new MessageBoxController(message, owned);
            loader.setController(controller);
            VBox messageBox = loader.load();
            messageBoxes.add(controller);
            gridPane_messages.add(messageBox, owned ? 1 : 0, messageBoxes.size(), 2, 1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void receiveMessage(Message message) {
        addMessage(message, false);
    }

    @FXML
    private void messageAction(ActionEvent event) {
        final String sendingMessage = textArea_input.getText();
        if (sendingMessage.isEmpty()) {
            return;
        }
        textArea_input.setDisable(true);
        new Thread(() -> this.sendMessage(sendingMessage)).start();
    }

    @FXML
    private void inputAction(KeyEvent event) {
        if (event.getEventType() == KeyEvent.KEY_PRESSED && event.getCode() == KeyCode.ENTER) {
            if (event.isControlDown()) {
                textArea_input.appendText(System.lineSeparator());
            } else {
                textArea_input.setText(textArea_input.getText().trim());
                btn_send.fireEvent(new ActionEvent());
            }
        }
    }

    private void sendMessage(String content) {
        Message sendingMessage = new Message();
        sendingMessage.setContent(content);
        sendingMessage.setSentDate(new Date());
        sendingMessage.setSender(Utils.self());

        if (isPrivateChat) {
            // private chat
            if (user.equals(Utils.self())) {
                // TODO: implement the case
                // hosting private room, send to participants
                sendingMessage.setMessageType(Constants.MSG_TYPE_FWD_PRIVATE);
                /*for (User participant : participants) {
                    Message forwardedMessage = sendingMessage.cloneMessage();
                    forwardedMessage.setReceiver(participant);
                    MessageService.sendMessage(forwardedMessage);
                }*/
            } else {
                // receiving messages from private room
                sendingMessage.setMessageType(Constants.MSG_TYPE_PRIVATE_ROOM);
                sendingMessage.setReceiver(user);
                MessageService.sendMessage(sendingMessage);
            }
        } else if (!user.equals(Utils.selfRoom())) {
            // Direct or connected room
            sendingMessage.setMessageType(user.isRoom() ? Constants.MSG_TYPE_ROOM : Constants.MSG_TYPE_DIRECT);
            sendingMessage.setReceiver(user);
            MessageService.sendMessage(sendingMessage);
        } else {
            // TODO: implement the case
            // hosting a room
            sendingMessage.setMessageType(Constants.MSG_TYPE_FWD);
            // send a clone for self
            Message selfMessage = sendingMessage.cloneMessage();
            selfMessage.setReceiver(Utils.selfRoom());
            selfMessage.setMessageType(Constants.MSG_TYPE_ROOM);
            MessageService.sendMessage(selfMessage);
            // send to participants
            /*for (User participant : participants) {
                Message forwardedMessage = sendingMessage.cloneMessage();
                forwardedMessage.setReceiver(participant);
                MessageService.sendMessage(forwardedMessage);
            }*/
        }

        Platform.runLater(() -> {
            addMessage(sendingMessage, true);
            textArea_input.setText("");
            textArea_input.requestFocus();
            textArea_input.setDisable(false);
        });
    }
}
