package ogr.user12043.talkOnLan.net;

import ogr.user12043.talkOnLan.Main;
import ogr.user12043.talkOnLan.User;
import ogr.user12043.talkOnLan.util.Constants;
import ogr.user12043.talkOnLan.util.Properties;
import ogr.user12043.talkOnLan.util.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * Created by user12043 on 26.07.2018 - 11:57
 * part of project: talk-onLan
 */
class DiscoveryService {
    private static final Logger LOGGER = LogManager.getLogger(DiscoveryService.class);

    static void sendDiscoveryRequest(InetAddress address) throws IOException {
        byte[] request = Constants.DISCOVERY_COMMAND_REQUEST.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(request, request.length, address, Constants.RECEIVE_PORT);
        NetworkService.sendSocket.send(sendPacket);
        LOGGER.debug("Discovery package sent to " + sendPacket.getAddress() + ":" + sendPacket.getPort());
    }

    static void sendDiscoveryResponse(DatagramPacket receivedRequestPacket) throws IOException {
        /*if (Utils.buddyAddresses.contains(receivedRequestPacket.getAddress())) {
            return;
        }*/
        LOGGER.debug("Discovery request received from " + receivedRequestPacket.getAddress() + ":" + receivedRequestPacket.getPort());
        byte[] discoveryResponse = (Constants.DISCOVERY_COMMAND_RESPONSE + Constants.COMMAND_SEPARATOR + Properties.username).getBytes();
        DatagramPacket discoveryResponsePacket = new DatagramPacket(discoveryResponse, discoveryResponse.length, receivedRequestPacket.getAddress(), Constants.RECEIVE_PORT);
        NetworkService.sendSocket.send(discoveryResponsePacket);
        LOGGER.debug("Discovery response sent to: " + discoveryResponsePacket.getAddress() + ":" + discoveryResponsePacket.getPort());
    }

    static void receiveDiscoveryResponse(DatagramPacket receivedResponsePacket, String receivedData) {
        LOGGER.debug("Discovery response received from " + receivedResponsePacket.getAddress() + ":" + receivedResponsePacket.getPort());
        if (Utils.buddyAddresses.contains(receivedResponsePacket.getAddress())) {
            return;
        }
        Utils.buddyAddresses.add(receivedResponsePacket.getAddress());
        User user = new User();
        user.setAddress(receivedResponsePacket.getAddress());
        int index = receivedData.indexOf(Constants.COMMAND_SEPARATOR);
        if (index != -1) {
            user.setUserName(receivedData.substring(index + 1));
        }
        Utils.buddies.add(user);
        Main.mainUI.buddiesPanel.addBuddy(user);
    }
}
