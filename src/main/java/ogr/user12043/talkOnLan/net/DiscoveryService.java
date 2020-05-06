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

    public static void sendDiscoveryRequestRoom(InetAddress address) throws IOException {
        sendRequest(address, Constants.DISCOVERY_COMMAND_REQUEST_ROOM.getBytes());
    }

    static void sendDiscoveryResponse(InetAddress receiveAddress, boolean isRoom) throws IOException {
        LOGGER.debug("Discovery request received from " + receiveAddress);
        byte[] discoveryResponse = (!isRoom ? Constants.DISCOVERY_COMMAND_RESPONSE : Constants.DISCOVERY_COMMAND_RESPONSE_ROOM + Constants.COMMAND_SEPARATOR + Properties.username).getBytes();
        DatagramPacket discoveryResponsePacket = new DatagramPacket(discoveryResponse, discoveryResponse.length, receiveAddress, Constants.RECEIVE_PORT);
        NetworkService.sendSocket.send(discoveryResponsePacket);
        LOGGER.debug("Discovery response sent to: " + discoveryResponsePacket.getAddress() + ":" + discoveryResponsePacket.getPort());
        // Send a new discovery request to source address if request received from unknown address
        if (!isRoom && !Utils.isDiscovered(receiveAddress)) {
            sendDiscoveryRequest(receiveAddress);
        } else if (isRoom && !Utils.isDiscoveredRoom(receiveAddress)) {
            sendDiscoveryRequestRoom(receiveAddress);
        }
    }

    static void receiveDiscoveryResponse(InetAddress receiveAddress, String receivedData, boolean isRoom) {
        LOGGER.debug("Discovery response received from " + receiveAddress);
        // Ignore already discovered
        if ((!isRoom && Utils.isDiscovered(receiveAddress)) || (isRoom && Utils.isDiscoveredRoom(receiveAddress))) {
            return;
        }

        User user = new User();
        user.setAddress(receiveAddress);
        user.setRoom(isRoom);
        // Get username with parsing response
        int index = receivedData.indexOf(Constants.COMMAND_SEPARATOR);
        if (index != -1) {
            user.setUserName(receivedData.substring(index + 1));
        }
        if (!isRoom) {
            Utils.buddies.add(user);
            SwingUtilities.invokeLater(() -> MainUI.getUI().buddiesPanel.addBuddy(user));
        } else {
            Utils.rooms.add(user);
            SwingUtilities.invokeLater(() -> MainUI.getUI().roomsPanel.addBuddy(user));
        }
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
