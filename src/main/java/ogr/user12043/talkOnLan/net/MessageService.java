package ogr.user12043.talkOnLan.net;

import ogr.user12043.talkOnLan.Message;
import ogr.user12043.talkOnLan.User;
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
import java.util.Date;

/**
 * Created by user12043 on 31.07.2018 - 09:36
 * part of project: talk-onLan
 * <p>
 * Does message communication
 */
public class MessageService {
    private static final Logger LOGGER = LogManager.getLogger(MessageService.class);

    public static void sendMessage(User user, Message message) throws IOException {
        Socket socket = new Socket(user.getAddress(), Constants.RECEIVE_PORT);
        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
        outputStream.writeUTF(Utils.generateMessage(message));
        socket.close();
    }

    static void receiveMessage(InetAddress senderAddress, String receivedData, boolean isRoom) {
        LOGGER.debug((isRoom ? "Room message" : "Message") + " received from " + senderAddress);
        Message message = new Message();
        int index = receivedData.indexOf(Constants.COMMAND_SEPARATOR);
        message.setContent(receivedData.substring(0, index));
        message.setSentDate(new Date(Long.parseLong(receivedData.substring(index + 1))));
        message.setRoomMessage(isRoom);
        User user = Utils.findBuddy(senderAddress);
        message.setSender(user);
        if (user != null) {
            SwingUtilities.invokeLater(() -> MainUI.getUI().receiveMessage(message));
        }
    }
}
