package ogr.user12043.talkOnLan.net;

import ogr.user12043.talkOnLan.Main;
import ogr.user12043.talkOnLan.User;
import ogr.user12043.talkOnLan.util.Constants;
import ogr.user12043.talkOnLan.util.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Optional;

/**
 * Created by user12043 on 31.07.2018 - 09:36
 * part of project: talk-onLan
 */
public class MessageService {
    private static final Logger LOGGER = LogManager.getLogger(MessageService.class);

    public static void sendMessage(InetAddress address, String message) throws IOException {
        int index = 0;
        int sendTimes = (message.length() / Constants.BUFFER_LENGTH) + 1;
        for (int i = 0; i < sendTimes; i++) {
            int endIndex = (index + Constants.BUFFER_LENGTH < message.length()) ? (index + Constants.BUFFER_LENGTH) : message.length();
            String piece = message.substring(index, endIndex);
            byte[] sendBytes = (Constants.COMMAND_MESSAGE + Constants.COMMAND_SEPARATOR + piece).getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendBytes, sendBytes.length, address, Constants.RECEIVE_PORT);
            NetworkService.sendSocket.send(sendPacket);
            LOGGER.debug("Message sent to " + sendPacket.getAddress() + ":" + sendPacket.getPort());
            index += Constants.BUFFER_LENGTH;
        }
    }

    static void receiveMessage(DatagramPacket receivedPacket, String receivedData) {
        LOGGER.debug("Message received from " + receivedPacket.getAddress() + ":" + receivedPacket.getPort());
        int index = receivedData.indexOf(Constants.COMMAND_SEPARATOR);
        String message = "";
        if (index != -1) {
            message = receivedData.substring(index + 1);
        }
        final boolean founded = Utils.buddyAddresses.contains(receivedPacket.getAddress());
        if (founded) {
            final Optional<User> first = Utils.buddies.stream().filter(u -> u.getAddress().equals(receivedPacket.getAddress())).findFirst();
            if (first.isPresent()) {
                Main.mainUI.receiveMessage(first.get(), message);
            }
        }
    }
}
