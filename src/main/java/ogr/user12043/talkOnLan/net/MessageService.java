package ogr.user12043.talkOnLan.net;

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

/**
 * Created by user12043 on 31.07.2018 - 09:36
 * part of project: talk-onLan
 * <p>
 * Does message communication
 */
public class MessageService {
    private static final Logger LOGGER = LogManager.getLogger(MessageService.class);

    public static void sendMessage(InetAddress address, String message, boolean isRoom) throws IOException {
        Socket socket = new Socket(address, Constants.RECEIVE_PORT);
        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
        message = (!isRoom ? Constants.COMMAND_MESSAGE : Constants.COMMAND_MESSAGE_ROOM + Constants.COMMAND_SEPARATOR + message);
        outputStream.writeUTF(message);
        socket.close();
    }

    static void receiveMessage(InetAddress senderAddress, String receivedData, boolean isRoom) {
        LOGGER.debug((isRoom ? "Room message" : "Message") + "received from " + senderAddress);
        User user = !isRoom ? Utils.findBuddy(senderAddress) : Utils.findRoom(senderAddress);
        if (user != null) {
            SwingUtilities.invokeLater(() -> MainUI.getUI().receiveMessage(user, receivedData));
        }
    }
}
