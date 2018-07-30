package ogr.user12043.talkOnLan.net;

import ogr.user12043.talkOnLan.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by user12043 on 26.07.2018 - 11:53
 * part of project: talk-onLan
 */
public class NetworkService {
    private static ExecutorService service = Executors.newFixedThreadPool(3);
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

        Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();
            if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                continue; // skip loopback or disconnected interface
            }

            for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                final InetAddress receivePacketAddress = receivePacket.getAddress();
                if (receivePacketAddress == null || receivePacketAddress.equals(interfaceAddress.getAddress())) {
                    return;
                }
            }
        }
        String receivedData = new String(receivePacket.getData()).trim();
        // Process data
        if (Constants.DISCOVERY_COMMAND_REQUEST.equals(receivedData)) {
            DiscoveryService.sendDiscoveryResponse(receivePacket);
        } else if (receivedData.startsWith(Constants.DISCOVERY_COMMAND_RESPONSE)) {
            DiscoveryService.receiveDiscoveryResponse(receivePacket, receivedData);
        }
    }

    private static void send() throws IOException {
        DiscoveryService.sendDiscoveryRequest(InetAddress.getByName("255.255.255.255"));

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
        Runnable sendThread = () -> {
            try {
                // Do task until end = true
                while (!end) {
                    send();
                    Thread.sleep(Constants.DISCOVERY_INTERVAL);
                }
            } catch (Exception e) {
                LOGGER.error("Error on send() " + Arrays.toString(e.getStackTrace()));
            }/* finally {
                sendSocket.close();
                LOGGER.info("Send socket closed");
            }*/
        };

        Runnable receiveThread = (() -> {
            try {
                // Do task until end = true
                while (!end) {
                    receive();
                }
            } catch (Exception e) {
                LOGGER.error("Error on receive() " + Arrays.toString(e.getStackTrace()));
            }/* finally {
                receiveSocket.close();
                LOGGER.info("Receive socket closed");
            }*/
        });

        service.execute(sendThread);
        service.execute(receiveThread);
    }

    public static void end() {
        end = true;
    }
}
