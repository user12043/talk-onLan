package ogr.user12043.talkOnLan.net;

import ogr.user12043.talkOnLan.Main;
import ogr.user12043.talkOnLan.util.Constants;
import ogr.user12043.talkOnLan.util.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;

/**
 * Created by user12043 on 26.07.2018 - 11:53
 * part of project: talk-onLan
 */
public class NetworkService {
    private static final Logger LOGGER = LogManager.getLogger(NetworkService.class);
    static DatagramSocket sendSocket;
    private static DatagramSocket receiveSocket;
    private static boolean end; // Control field for threads. (To safely terminate)

    private static void receive() throws IOException {
        // Receive data
        byte[] buffer = new byte[Constants.BUFFER_LENGTH];
        DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
        try {
            receiveSocket.receive(receivePacket);
        } catch (SocketTimeoutException ignored) {
        }
        String receiveData = new String(receivePacket.getData()).trim();

        // Process data
        switch (receiveData) {
            case Constants.DISCOVERY_COMMAND_REQUEST: {
                DiscoveryService.sendDiscoveryResponse(receivePacket);
                break;
            }
            case Constants.DISCOVERY_COMMAND_RESPONSE: {
                DiscoveryService.receiveDiscoveryResponse(receivePacket);
                break;
            }
        }
    }

    private static void send() throws IOException {
        //<editor-fold desc="Broadcast the message over all the network interfaces" defaultstate=collapsed>
        Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
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
                DiscoveryService.sendDiscoveryRequest(broadcastAddress);
            }
        }
        //</editor-fold>
    }

    private static void refresh() throws IOException {
        for (InetAddress address : Utils.buddyAddresses) {
            DiscoveryService.sendDiscoveryRequest(address);

            byte[] buffer = new byte[Constants.BUFFER_LENGTH];
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
            try {
                receiveSocket.receive(receivePacket);
                return;
            } catch (SocketTimeoutException ignored) {
            }

            //Remove buddy if no response or invalid response received
            Utils.buddyAddresses.remove(address);
            Main.mainPanel.removeBuddy(address);
        }
    }

    private static void initConnections() throws UnknownHostException, SocketException {
        if (sendSocket == null) {
            sendSocket = new DatagramSocket(Constants.SEND_PORT);
            sendSocket.setBroadcast(true);
        }
        if (receiveSocket == null) {
            receiveSocket = new DatagramSocket(Constants.RECEIVE_PORT, InetAddress.getByName("0.0.0.0"));
            receiveSocket.setBroadcast(true);
            receiveSocket.setSoTimeout(Constants.RECEIVE_TIMEOUT);
        }
    }

    public static void start() throws UnknownHostException, SocketException {
        initConnections();
        end = false;
        Thread sendThread = new Thread(() -> {
            try {
                // Do task until end = true
                while (!end) {
                    send();
                    Thread.sleep(Constants.DISCOVERY_INTERVAL);
                }
            } catch (Exception e) {
                LOGGER.error("Error on send()" + e);
            } finally {
                sendSocket.close();
                LOGGER.info("Send socket closed");
            }
        });

        Thread receiveThread = new Thread(() -> {
            try {
                // Do task until end = true
                while (!end) {
                    receive();
                }
            } catch (Exception e) {
                LOGGER.error("Error on receive()" + e);
            } finally {
                receiveSocket.close();
                LOGGER.info("Receive socket closed");
            }
        });

        Thread refreshThread = new Thread(() -> {
            try {
                while (!end) {
                    refresh();
                    Thread.sleep(Constants.DISCOVERY_INTERVAL);
                }
            } catch (Exception e) {
                LOGGER.error("Error on refresh()" + e);
            }
        });

        sendThread.start();
        receiveThread.start();
        refreshThread.start();
    }

    public static void end() {
        end = true;
    }
}
