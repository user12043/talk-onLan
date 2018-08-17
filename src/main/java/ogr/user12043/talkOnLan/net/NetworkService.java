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
    private static final ExecutorService service = Executors.newFixedThreadPool(4);
    static DatagramSocket sendSocket; // UDP socket for send discovery
    private static DatagramSocket receiveSocket; // UDP socket for receive discovery
    private static ServerSocket messageReceiveSocket; // TCP socket for receiving messages
    private static ServerSocket fileReceiveSocket; // TCP socket for receiving files
    private static boolean end; // Control field for threads. (To safely terminate)

    /**
     * UDP receiving (for discovery)
     *
     * @throws IOException IOException on connections
     */
    private static void receive() throws IOException {
        // Receive data
        byte[] buffer = new byte[Constants.DISCOVERY_BUFFER_LENGTH];
        DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
        try {
            receiveSocket.receive(receivePacket);
        } catch (SocketTimeoutException e) {
            return;
        }

        // Return if source is localhost
        if (filterLocalhost(receivePacket.getAddress())) {
            return;
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
     * Receives a connection and does some process after process received data
     *
     * @param listenSocket listening socket
     * @return incoming socket
     * @throws IOException IOException on connections
     */
    private static Socket receiveTcp(ServerSocket listenSocket) throws IOException {
        // Accept a connection
        final Socket incomingSocket;
        try {
            incomingSocket = listenSocket.accept();
        } catch (SocketTimeoutException e) {
            return null;
        }
        incomingSocket.setSoTimeout(Constants.RECEIVE_TIMEOUT);
        // Return if source is localhost
        if (filterLocalhost(incomingSocket.getInetAddress())) {
            return null;
        }

        // Discover user if not discovered yet
        final boolean exists = Utils.buddyAddresses.stream().anyMatch(address -> (address.equals(incomingSocket.getInetAddress())));
        if (!exists) {
            DiscoveryService.sendDiscoveryRequest(incomingSocket.getInetAddress());
            // Wait for discovery
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LOGGER.error("Error when discovering remote user: ", e);
            }
        }

        return incomingSocket;
    }

    /**
     * TCP receiving for messaging
     *
     * @throws IOException IOException on connections
     */
    private static void receiveMessage() throws IOException {
        Socket incomingSocket = receiveTcp(messageReceiveSocket);
        if (incomingSocket == null) {
            return;
        }

        // Process data
        DataInputStream inputStream = new DataInputStream(incomingSocket.getInputStream());
        String message = inputStream.readUTF();
        if (message.startsWith(Constants.COMMAND_MESSAGE + Constants.COMMAND_SEPARATOR)) { // Get messages
            message = message.replace((Constants.COMMAND_MESSAGE + Constants.COMMAND_SEPARATOR), "");
            MessageService.receiveMessage(incomingSocket.getInetAddress(), incomingSocket.getPort(), message);
        }
    }

    /**
     * TCP receiving for file transfer
     *
     * @throws IOException IOException on connections
     */
    private static void receiveFile() throws IOException {
        Socket incomingSocket = receiveTcp(fileReceiveSocket);
        if (incomingSocket == null) {
            return;
        }
        // Process data
        DataInputStream inputStream = new DataInputStream(incomingSocket.getInputStream());
        String message = inputStream.readUTF();
        if (message.startsWith(Constants.COMMAND_FILE_TRANSFER_REQUEST + Constants.COMMAND_SEPARATOR)) { // Get file
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

    private static boolean filterLocalhost(InetAddress address) throws UnknownHostException {
        for (InterfaceAddress hostAddress : Utils.hostAddresses) {
            final InetAddress localhost = hostAddress.getAddress();
            if (address == null
                    || address.getHostAddress().equals(localhost.getHostAddress())
                    || address.equals(InetAddress.getByName("127.0.0.1"))) {
                return true;
            }
        }
        return false;
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
        if (messageReceiveSocket == null) {
            messageReceiveSocket = new ServerSocket(Constants.RECEIVE_PORT);
            messageReceiveSocket.setSoTimeout(Constants.RECEIVE_TIMEOUT);
        }
        if (fileReceiveSocket == null) {
            fileReceiveSocket = new ServerSocket(Constants.FILE_RECEIVE_PORT);
            fileReceiveSocket.setSoTimeout(Constants.RECEIVE_TIMEOUT);
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
                LOGGER.error("Error on receive() ", e);
            }
        });

        Runnable receiveMessageThread = (() -> {
            try {
                while (!end) {
                    receiveMessage();
                }
                LOGGER.debug("receiveMessage end");
            } catch (Exception e) {
                LOGGER.error("Error on receiveMessage() ", e);
            }
        });

        Runnable receiveFileThread = (() -> {
            try {
                while (!end) {
                    receiveFile();
                }
                LOGGER.debug("receiveFile end");
            } catch (Exception e) {
                LOGGER.error("Error on receiveFile() ", e);
            }
        });

        // Execute threads
        service.execute(sendThread);
        service.execute(receiveThread);
        service.execute(receiveMessageThread);
        service.execute(receiveFileThread);
    }

    public static void end() {
        end = true;
    }

}
