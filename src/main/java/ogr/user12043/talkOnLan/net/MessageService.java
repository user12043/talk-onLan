package ogr.user12043.talkOnLan.net;

import ogr.user12043.talkOnLan.User;
import ogr.user12043.talkOnLan.ui.MainUI;
import ogr.user12043.talkOnLan.util.Constants;
import ogr.user12043.talkOnLan.util.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Optional;

/**
 * Created by user12043 on 31.07.2018 - 09:36
 * part of project: talk-onLan
 * <p>
 * Does message communication
 */
public class MessageService {
    private static final Logger LOGGER = LogManager.getLogger(MessageService.class);

    public static void sendMessage(InetAddress address, String message) throws IOException {
        Socket socket = new Socket(address, Constants.RECEIVE_PORT, InetAddress.getLocalHost(), Constants.SEND_PORT);
        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
        message = (Constants.COMMAND_MESSAGE + Constants.COMMAND_SEPARATOR + message);
        outputStream.writeUTF(message);
        socket.close();
    }

    static void receiveMessage(InetAddress senderAddress, int senderPort, String receivedData) {
        LOGGER.debug("Message received from " + senderAddress + ":" + senderPort);
        final boolean founded = Utils.buddyAddresses.contains(senderAddress);
        if (founded) {
            final Optional<User> first = Utils.buddies.stream().filter(u -> u.getAddress().equals(senderAddress)).findFirst();
            first.ifPresent(user -> MainUI.getUI().receiveMessage(user, receivedData));
        }
    }
}
