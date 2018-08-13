package ogr.user12043.talkOnLan.net;

import ogr.user12043.talkOnLan.util.Constants;
import ogr.user12043.talkOnLan.util.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by user12043 on 26.07.2018 - 11:53
 * part of project: talk-onLan
 * <p>
 * The main class for network operations
 */
public class NetworkService {
    private static final Logger LOGGER = LogManager.getLogger(NetworkService.class);
    private static final ExecutorService service = Executors.newFixedThreadPool(3);
    static DatagramSocket sendSocket; // UDP socket for send discovery
    private static DatagramSocket receiveSocket; // UDP socket for receive discovery
    private static ServerSocket tcpReceiveSocket; // TCP socket for receiving messages and files
    private static boolean end; // Control field for threads. (To safely terminate)

    /**
     * UDP receiving (for discovery)
     *
     * @throws IOException IOException on connections
     */
    private static void receive() throws IOException {
        // Receive data
        byte[] buffer = new byte[Constants.BUFFER_LENGTH];
        DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
        try {
            receiveSocket.receive(receivePacket);
        } catch (SocketTimeoutException e) {
            return;
        }

        // Return if source is localhost
        for (InterfaceAddress hostAddress : Utils.hostAddresses) {
            final InetAddress receivePacketAddress = receivePacket.getAddress();
            final InetAddress localhost = hostAddress.getAddress();
            if (receivePacketAddress == null
                    || receivePacketAddress.getHostAddress().equals(localhost.getHostAddress())
                    || receivePacketAddress.equals(InetAddress.getByName("127.0.0.1"))) {
                return;
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

    /**
     * TCP receiving for messaging and file transfer
     *
     * @throws IOException IOException on connections
     */
    private static void receiveTcp() throws IOException {
        // Accept a connection
        final Socket incomingSocket;
        try {
            incomingSocket = tcpReceiveSocket.accept();
        } catch (SocketTimeoutException e) {
            return;
        }

        // Return if source is localhost
        for (InterfaceAddress hostAddress : Utils.hostAddresses) {
            final InetAddress receiveAddress = incomingSocket.getInetAddress();
            final InetAddress localhost = hostAddress.getAddress();
            if (receiveAddress == null
                    || receiveAddress.getHostAddress().equals(localhost.getHostAddress())
                    || receiveAddress.equals(InetAddress.getByName("127.0.0.1"))) {
                return;
            }
        }

        // Discover user if not discovered yet
        final boolean exists = Utils.buddyAddresses.stream().anyMatch(address -> (address == incomingSocket.getInetAddress()));
        if (!exists) {
            DiscoveryService.sendDiscoveryRequest(incomingSocket.getInetAddress());
        }

        // Process data
        DataInputStream inputStream = new DataInputStream(incomingSocket.getInputStream());
        String message = inputStream.readUTF();
        if (message.startsWith(Constants.COMMAND_MESSAGE + Constants.COMMAND_SEPARATOR)) { // Get messages
            message = message.replace((Constants.COMMAND_MESSAGE + Constants.COMMAND_SEPARATOR), "");
            MessageService.receiveMessage(incomingSocket.getInetAddress(), incomingSocket.getPort(), message);
        } else if (message.startsWith(Constants.COMMAND_FILE_TRANSFER_REQUEST + Constants.COMMAND_SEPARATOR)) { // Get file
            final String[] arguments = message.split("\\" + Constants.COMMAND_SEPARATOR);
            try {
                if (arguments.length > 3) {
                    throw new Exception();
                }
                final long size = Long.parseLong(arguments[1]);
                FileTransferService.receiveFile(incomingSocket, arguments[2], size);
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                LOGGER.error("invalid file send request from " + incomingSocket.getInetAddress());
            }
        }
    }

    /**
     * UDP sending (for discovery)
     *
     * @throws IOException IOException on connections
     */
    private static void send() throws IOException {
        // First send to generic broadcast address
        DiscoveryService.sendDiscoveryRequest(InetAddress.getByName("255.255.255.255"));

        //<editor-fold desc="Broadcast the message over all the network interfaces" defaultstate=collapsed>
        for (NetworkInterface networkInterface : Utils.networkInterfaces) {
            if (networkInterface.isUp()) { // Check for connectivity
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcastAddress = interfaceAddress.getBroadcast();
                    if (broadcastAddress == null) {
                        continue;
                    }
                    // Send the broadcast package!
                    DiscoveryService.sendDiscoveryRequest(broadcastAddress);
                }

                // If interface is got up after program start, its host address will be added.
                Utils.hostAddresses.addAll(networkInterface.getInterfaceAddresses());
            } else {
                // If interface is got down after program start, its host address will be removed.
                Utils.hostAddresses.removeAll(networkInterface.getInterfaceAddresses());
            }
        }
        //</editor-fold>
    }

    private static void initConnections() throws IOException {
        if (sendSocket == null) {
            sendSocket = new DatagramSocket(Constants.SEND_PORT);
            sendSocket.setBroadcast(true); // This is important to broadcasting
        }
        if (receiveSocket == null) {
            receiveSocket = new DatagramSocket(Constants.RECEIVE_PORT, InetAddress.getByName("0.0.0.0"));
//            receiveSocket.setBroadcast(true); // Not necessary here
            receiveSocket.setSoTimeout(Constants.RECEIVE_TIMEOUT);
        }
        if (tcpReceiveSocket == null) {
            tcpReceiveSocket = new ServerSocket(Constants.RECEIVE_PORT);
            tcpReceiveSocket.setSoTimeout(Constants.RECEIVE_TIMEOUT);
        }
    }

    /**
     * Create and start network threads
     *
     * @throws IOException IOException on connections
     */
    public static void start() throws IOException {
        initConnections();
        end = false;

        // Create threads
        Runnable sendThread = () -> {
            try {
                // Do task until end = true
                while (!end) {
                    send();
                    Thread.sleep(Constants.DISCOVERY_INTERVAL);
                }
                LOGGER.debug("send end");
            } catch (Exception e) {
                LOGGER.error("Error on send() " + Arrays.toString(e.getStackTrace()));
            }
        };

        Runnable receiveThread = (() -> {
            try {
                // Do task until end = true
                while (!end) {
                    receive();
                }
                LOGGER.debug("receive end");
            } catch (Exception e) {
                LOGGER.error("Error on receive() " + Arrays.toString(e.getStackTrace()));
            }
        });

        Runnable receiveTcpThread = (() -> {
            try {
                while (!end) {
                    receiveTcp();
                }
                LOGGER.debug("receiveTcp end");
            } catch (Exception e) {
                LOGGER.error("Error on receiveTcp() " + Arrays.toString(e.getStackTrace()));
            }
        });

        // Execute threads
        service.execute(sendThread);
        service.execute(receiveThread);
        service.execute(receiveTcpThread);
    }

    public static void end() {
        end = true;
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
                    System.out.println(targetAddressString);
                    InetAddress targetAddress = Inet4Address.getByName(targetAddressString);
                    DiscoveryService.sendDiscoveryRequest(targetAddress);
                }
            }
        }
    }
}
