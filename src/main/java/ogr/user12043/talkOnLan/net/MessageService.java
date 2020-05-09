package ogr.user12043.talkOnLan.net;

import ogr.user12043.talkOnLan.model.Message;
import ogr.user12043.talkOnLan.model.User;
import ogr.user12043.talkOnLan.ui.MainUI;
import ogr.user12043.talkOnLan.util.Constants;
import ogr.user12043.talkOnLan.util.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by user12043 on 31.07.2018 - 09:36
 * part of project: talk-onLan
 * <p>
 * Does message communication
 */
public class MessageService {
    private static final Logger LOGGER = LogManager.getLogger(MessageService.class);

    public static void sendMessage(User user, Message message) throws IOException {
        LOGGER.debug((message.getMessageType() != 0) ? "Room message" : "Message" + " sending to " + user.getAddress());
        Socket socket = new Socket(user.getAddress(), Constants.RECEIVE_PORT);
        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
        outputStream.writeUTF(Utils.generateMessage(message));
        socket.close();
    }

    static void receiveMessage(InetAddress senderAddress, String receivedData) {
        Message message = Utils.parseMessage(receivedData);
        LOGGER.debug((message.getMessageType() != 0 ? "Room message" : "Message") + " received from " + senderAddress);
        User user = Utils.findBuddy(senderAddress);
        message.setSender(user);
        if (user != null) {
            SwingUtilities.invokeLater(() -> MainUI.getUI().receiveMessage(message));
        }
    }
}
