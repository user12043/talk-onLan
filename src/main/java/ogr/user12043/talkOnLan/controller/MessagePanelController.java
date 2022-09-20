package ogr.user12043.talkOnLan.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import ogr.user12043.talkOnLan.TalkOnLanApp;
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

    private final User user; // remote user
    private final Set<User> participants = new HashSet<>(); // involved users of this panel
    private final boolean isPrivateChat;
    private final Set<MessageBoxController> messageBoxes = new HashSet<>();
    private final Stage stage = new Stage();
    private Tooltip newMessageTooltip;
    @FXML
    private Label label_title;
    @FXML
    private GridPane gridPane_messages;
    @FXML
    private TextArea textArea_input;
    @FXML
    private Button btn_send;
    @FXML
    private VBox vbox_participants;
    @FXML
    private ScrollPane scrollPane_messages;

    public MessagePanelController(User user, boolean isPrivateChat) {
        this.user = user;
        this.isPrivateChat = isPrivateChat;
        // Inıtialize the chat window
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("messagePanel.fxml"));
            loader.setController(this);
            SplitPane messagePanel = loader.load();
            if (user.isRoom()) {
                messagePanel.setDividerPositions(0.2);
                vbox_participants.getChildren().add(new Label(user.getUsername()));
            }
            Scene scene = new Scene(messagePanel);
            scene.getStylesheets().add(Objects.requireNonNull(TalkOnLanApp.class.getResource("style.css")).toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Chat with " + user.getUsername());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        label_title.setText((user.isRoom() ? "Room of " + user.getUsername() : user.getUsername()) + " on " + user.getAddress() +
                (isPrivateChat ? "(Private)" : ""));
        if (!isPrivateChat) {
            fetchMessages();
        }

        newMessageTooltip = new Tooltip("new message(s)");
        newMessageTooltip.setTextAlignment(TextAlignment.CENTER);
        // scroll automatically when new message added (it means the gridPane_messages height has changed)
        gridPane_messages.heightProperty().addListener(observable -> {
            final double vValue = scrollPane_messages.getVvalue();
            if (vValue > 0.7 || gridPane_messages.getChildren().size() == 0) {
                scrollPane_messages.setVvalue(1.0);
            } else {
                final Bounds bounds = scrollPane_messages.localToScreen(scrollPane_messages.getBoundsInLocal());
                newMessageTooltip.show(scrollPane_messages, bounds.getCenterX() - newMessageTooltip.getWidth() / 2,
                        bounds.getMinY() + scrollPane_messages.getHeight() - newMessageTooltip.getHeight());
            }
        });

        scrollPane_messages.vvalueProperty().addListener(observable -> {
            if (scrollPane_messages.getVvalue() > 0.95 && newMessageTooltip.isShowing()) {
                newMessageTooltip.hide();
            }
        });

        MenuItem item = new MenuItem("Clear messages");
        item.setOnAction(event -> {
            MessageDao.get().clearAll(user);
            gridPane_messages.getChildren().clear();
        });
        scrollPane_messages.setContextMenu(new ContextMenu(item));
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
        if (message.getSender() != null) {
            addParticipant(message.getSender());
            if (message.getMessageType() == Constants.MSG_TYPE_ROOM ||
                    message.getMessageType() == Constants.MSG_TYPE_PRIVATE_ROOM) {
                // forward message to all clients
                message.setMessageType(message.getMessageType() == Constants.MSG_TYPE_PRIVATE_ROOM
                        ? Constants.MSG_TYPE_FWD_PRIVATE : Constants.MSG_TYPE_FWD);
                message.setForwardedFrom(message.getSender());
                for (User participant : participants) {
                    // except sender
                    if (!participant.equals(message.getSender())) {
                        Message clone = message.cloneMessage();
                        clone.setReceiver(participant);
                        MessageService.sendMessage(clone);
                    }
                }
            }
        }
        stage.show();
        stage.requestFocus();
    }

    private void addParticipant(User user) {
        participants.add(user);
        // Refresh
        vbox_participants.getChildren().clear();
        participants.forEach(p -> {
            Label label = new Label(p.getUsername());
            label.setMaxWidth(Double.MAX_VALUE);
            vbox_participants.getChildren().add(label);
        });
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
                // hosting private room, send to participants
                sendingMessage.setMessageType(Constants.MSG_TYPE_FWD_PRIVATE);
                for (User participant : participants) {
                    Message forwardedMessage = sendingMessage.cloneMessage();
                    forwardedMessage.setReceiver(participant);
                    MessageService.sendMessage(forwardedMessage);
                }
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
            // hosting a room
            sendingMessage.setMessageType(Constants.MSG_TYPE_FWD);
            // send a clone for self
            Message selfMessage = sendingMessage.cloneMessage();
            selfMessage.setReceiver(Utils.selfRoom());
            selfMessage.setMessageType(Constants.MSG_TYPE_ROOM);
            MessageService.sendMessage(selfMessage);
            // send to participants
            for (User participant : participants) {
                Message forwardedMessage = sendingMessage.cloneMessage();
                forwardedMessage.setReceiver(participant);
                MessageService.sendMessage(forwardedMessage);
            }
        }

        Platform.runLater(() -> {
            addMessage(sendingMessage, true);
            textArea_input.setText("");
            textArea_input.setDisable(false);
            textArea_input.requestFocus();
        });
    }

    public void show() {
        stage.show();
    }
}
