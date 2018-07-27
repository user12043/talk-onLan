package ogr.user12043.talkOnLan.net;

import ogr.user12043.talkOnLan.Main;
import ogr.user12043.talkOnLan.util.Constants;
import ogr.user12043.talkOnLan.util.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;

/**
 * Created by user12043 on 26.07.2018 - 11:57
 * part of project: talk-onLan
 */
class DiscoveryService {
    private static final Logger LOGGER = LogManager.getLogger(DiscoveryService.class);

    static void sendDiscoveryResponse(DatagramPacket receivedRequestPacket) throws IOException {
        // Check existence
        if (Utils.buddyAddresses.contains(receivedRequestPacket.getAddress())) {
            return;
        }
        LOGGER.info("Discovery package received from " + receivedRequestPacket.getAddress() + ":" + receivedRequestPacket.getPort());
        byte[] discoveryResponse = Constants.DISCOVERY_COMMAND_RESPONSE.getBytes();
        DatagramPacket discoveryResponsePacket = new DatagramPacket(discoveryResponse, discoveryResponse.length, receivedRequestPacket.getAddress(), Constants.RECEIVE_PORT);
        NetworkService.sendSocket.send(discoveryResponsePacket);
        LOGGER.info("Discovery response sent to: " + discoveryResponsePacket.getAddress() + ":" + discoveryResponsePacket.getPort());
    }

    static void receiveDiscoveryResponse(DatagramPacket receivedResponsePacket) {
        LOGGER.info("Discovery response received from " + receivedResponsePacket.getAddress() + ":" + receivedResponsePacket.getPort());
        if (Utils.buddyAddresses.contains(receivedResponsePacket.getAddress())) {
            return;
        }
        Utils.buddyAddresses.add(receivedResponsePacket.getAddress());
        // TODO request username
        Main.mainPanel.addBuddy(receivedResponsePacket.getAddress());
    }
}
