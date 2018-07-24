package ogr.user12043.talkOnLan.net;

import ogr.user12043.talkOnLan.util.Constants;
import ogr.user12043.talkOnLan.util.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.*;

/**
 * Created by user12043 on 23.07.2018 - 16:32
 * part of project: talk-onLan
 */
public class Discovery {
    private static final Logger LOGGER = LogManager.getLogger(Discovery.class);

    private DatagramSocket receiveSocket;
    private DatagramSocket sendSocket;
    private static boolean end = false;

    public Discovery() throws UnknownHostException, SocketException {
        sendSocket = new DatagramSocket(Constants.SEND_PORT, InetAddress.getLocalHost());
        sendSocket.setBroadcast(true);
        sendSocket.setSoTimeout(Constants.RECEIVE_TIMEOUT);
        receiveSocket = new DatagramSocket(Constants.RECEIVE_PORT, InetAddress.getByName("0.0.0.0"));
        receiveSocket.setBroadcast(true);
        receiveSocket.setSoTimeout(Constants.RECEIVE_TIMEOUT);
    }

    public static void startDiscovery() {
        Discovery discovery;
        try {
            discovery = new Discovery();
            Thread sendThread = new Thread(() -> {
                try {
                    while (!end) {
                        discovery.send();
                        Thread.sleep(Constants.DISCOVERY_INTERVAL);
                    }
                } catch (IOException | InterruptedException e) {
                    LOGGER.error("cannot send discovery request");
                }
            });

            Thread receiveThread = new Thread(() -> {
                try {
                    while (!end) {
                        discovery.receive();
                        Thread.sleep(Constants.DISCOVERY_INTERVAL);
                    }
                } catch (IOException | InterruptedException e) {
                    LOGGER.error("cannot send discovery request");
                }
            });

            sendThread.start();
            receiveThread.start();
        } catch (UnknownHostException | SocketException e) {
            LOGGER.error("Unable to create discovery connection!\n" + e);
        }
    }

    public static void endDiscovery() {
        end = true;
    }

    public void receive() throws IOException {
        byte[] buffer = Constants.DISCOVERY_PACKET_REQUEST.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        receiveSocket.receive(packet);
        LOGGER.info("Discovery package received! -> " + packet.getAddress() + ":" + packet.getPort());
        String data = new String(packet.getData()).trim();
        if (data.equals(Constants.DISCOVERY_PACKET_REQUEST)) {
            byte[] response = Constants.DISCOVERY_PACKET_RESPONSE.getBytes();
            DatagramPacket responsePacket = new DatagramPacket(response, response.length, packet.getAddress(), packet.getPort());
            receiveSocket.send(responsePacket);
            LOGGER.info("Response sent to: " + packet.getAddress() + ":" + packet.getPort());
        } else {
            LOGGER.error("Error, not valid package!" + packet.getAddress() + ":" + packet.getPort());
        }
    }

    public void send() throws IOException {
        byte[] request = Constants.DISCOVERY_PACKET_REQUEST.getBytes();
        DatagramPacket packet = new DatagramPacket(request, request.length, InetAddress.getByName("255.255.255.255"), Constants.RECEIVE_PORT);
        sendSocket.send(packet);
        LOGGER.info("Discovery package sent!" + packet.getAddress() + ":" + packet.getPort());

        //<editor-fold desc="Broadcast the message over all the network interfaces" defaultstate=collapsed>
        /*Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();
            if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                continue; // skip loopback or disconnected interface
            }
            for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                InetAddress broadcastAddress = interfaceAddress.getBroadcast();
                if (broadcastAddress == null) {
                    continue;
                }
                // Send the broadcast package!
                DatagramPacket sendPacket = new DatagramPacket(request, request.length, broadcastAddress, Constants.RECEIVE_PORT);
                sendSocket.send(sendPacket);
                LOGGER.info("Discovery package sent!" + packet.getAddress() + ":" + packet.getPort() + " over " + networkInterface.getDisplayName());
            }
        }*/
        //</editor-fold>

        byte[] response = Constants.DISCOVERY_PACKET_RESPONSE.getBytes();
        DatagramPacket responsePacket = new DatagramPacket(response, response.length);
        sendSocket.receive(responsePacket);
        // Ignore if already found
        if (Utils.buddyAddresses.contains(responsePacket.getAddress())) {
            return;
        }
        LOGGER.info("Discovery response received!" + responsePacket.getAddress() + ":" + responsePacket.getPort());

        String responseData = new String(responsePacket.getData()).trim();
        if (responseData.equals(Constants.DISCOVERY_PACKET_RESPONSE)) {
            LOGGER.info("Found buddy!" + responsePacket.getAddress() + ":" + responsePacket.getPort());
            Utils.buddyAddresses.add(responsePacket.getAddress());
            // TODO create user
        }
    }
}
