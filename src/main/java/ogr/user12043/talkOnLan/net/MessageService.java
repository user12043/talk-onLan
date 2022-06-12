package ogr.user12043.talkOnLan.net;

import javafx.application.Platform;
import ogr.user12043.talkOnLan.controller.MainController;
import ogr.user12043.talkOnLan.dao.MessageDao;
import ogr.user12043.talkOnLan.model.Message;
import ogr.user12043.talkOnLan.util.Constants;
import ogr.user12043.talkOnLan.util.Utils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * Created by user12043 on 31.07.2018 - 09:36
 * part of project: talk-onLan
 * <p>
 * Does message communication
 */
public class MessageService {
    private static final Logger LOGGER = Logger.getLogger(MessageService.class.getName());

    public static void sendMessage(Message message) {
        LOGGER.fine((message.getMessageType() != Constants.MSG_TYPE_DIRECT ? "Room message"
                : "Message") + " sending to " + message.getReceiver().getAddress());
        try {
//            Socket socket = new Socket(message.getReceiver().getAddress(), Constants.RECEIVE_PORT);
            Socket socket = new Socket();
            InetSocketAddress address = new InetSocketAddress(message.getReceiver().getAddress(), Constants.RECEIVE_PORT);
            socket.connect(address, Constants.RECEIVE_TIMEOUT);
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeUTF(Utils.generateMessage(message));
            socket.close();
            message.setSent(true);
        } catch (IOException ignored) {
            message.setSent(false);
        }
        if (message.getMessageType() != Constants.MSG_TYPE_FWD_PRIVATE) {
            MessageDao.get().save(message);
        }
    }

    static void receiveMessage(InetAddress senderAddress, String receivedData) {
        Message message = Utils.parseMessage(receivedData, senderAddress);
        LOGGER.fine((message.getMessageType() != 0 ? "Room message" : "Message") + " received from " + senderAddress);
        if (message.getSender() != null) {
            Platform.runLater(() -> MainController.getInstance().receiveMessage(message));
        }
    }
}
