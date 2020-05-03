package ogr.user12043.talkOnLan.net;

import ogr.user12043.talkOnLan.User;
import ogr.user12043.talkOnLan.ui.MainUI;
import ogr.user12043.talkOnLan.util.Constants;
import ogr.user12043.talkOnLan.util.Properties;
import ogr.user12043.talkOnLan.util.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;

/**
 * Created by user12043 on 26.07.2018 - 11:57
 * part of project: talk-onLan
 * <p>
 * Does discovery between two devices running the program
 */
public class DiscoveryService {
    private static final Logger LOGGER = LogManager.getLogger(DiscoveryService.class);

    private static void sendRequest(InetAddress address, byte[] message) throws IOException {
        DatagramPacket sendPacket = new DatagramPacket(message, message.length, address, Constants.RECEIVE_PORT);
        NetworkService.sendSocket.send(sendPacket);
        LOGGER.debug("Discovery package sent to " + sendPacket.getAddress() + ":" + sendPacket.getPort());
    }

    public static void sendDiscoveryRequest(InetAddress address) throws IOException {
        sendRequest(address, Constants.DISCOVERY_COMMAND_REQUEST.getBytes());
    }

    public static void sendRoomDiscoveryRequest(InetAddress address) throws IOException {
        sendRequest(address, Constants.DISCOVERY_COMMAND_REQUEST_ROOM.getBytes());
    }

    static void sendDiscoveryResponse(DatagramPacket receivedRequestPacket) throws IOException {
        LOGGER.debug("Discovery request received from " + receivedRequestPacket.getAddress() + ":" + receivedRequestPacket.getPort());
        byte[] discoveryResponse = (Constants.DISCOVERY_COMMAND_RESPONSE + Constants.COMMAND_SEPARATOR + Properties.username).getBytes();
        DatagramPacket discoveryResponsePacket = new DatagramPacket(discoveryResponse, discoveryResponse.length, receivedRequestPacket.getAddress(), Constants.RECEIVE_PORT);
        NetworkService.sendSocket.send(discoveryResponsePacket);
        LOGGER.debug("Discovery response sent to: " + discoveryResponsePacket.getAddress() + ":" + discoveryResponsePacket.getPort());
        // Send a new discovery request to source address if request received from unknown address
        if (!Utils.buddyAddresses.contains(receivedRequestPacket.getAddress())) {
            sendDiscoveryRequest(receivedRequestPacket.getAddress());
        }
    }

    static void receiveDiscoveryResponse(DatagramPacket receivedResponsePacket, String receivedData) {
        LOGGER.debug("Discovery response received from " + receivedResponsePacket.getAddress() + ":" + receivedResponsePacket.getPort());
        // Ignore already discovered
        if (Utils.buddyAddresses.contains(receivedResponsePacket.getAddress())) {
            return;
        }
        Utils.buddyAddresses.add(receivedResponsePacket.getAddress());
        User user = new User();
        user.setAddress(receivedResponsePacket.getAddress());
        // Get username with parsing response
        int index = receivedData.indexOf(Constants.COMMAND_SEPARATOR);
        if (index != -1) {
            user.setUserName(receivedData.substring(index + 1));
        }
        Utils.buddies.add(user);
        SwingUtilities.invokeLater(() -> MainUI.getUI().buddiesPanel.addBuddy(user));
    }

    /**
     * If broadcasting is not working in the network, normal discovery will not work. This method sends discovery request to each ips specific
     *
     * @throws IOException IOException on connections
     */
    public static void hardDiscovery() throws IOException {
        for (InterfaceAddress hostAddress : Utils.hostAddresses) {
            final InetAddress address = hostAddress.getAddress();
            if (address instanceof Inet4Address) {
                final String hostAddressString = address.toString().replace(address.getHostName(), "").replace("/", "");
                for (int i = 0; i < 255; i++) {
                    final String targetAddressString = hostAddressString.substring(0, hostAddressString.lastIndexOf('.') + 1) + i;
                    InetAddress targetAddress = Inet4Address.getByName(targetAddressString);
                    sendDiscoveryRequest(targetAddress);
                }
            }
        }
    }
}
