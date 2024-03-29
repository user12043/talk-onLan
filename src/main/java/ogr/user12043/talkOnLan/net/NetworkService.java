package ogr.user12043.talkOnLan.net;

import ogr.user12043.talkOnLan.util.Constants;
import ogr.user12043.talkOnLan.util.Properties;
import ogr.user12043.talkOnLan.util.Utils;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Created by user12043 on 26.07.2018 - 11:53
 * part of project: talk-onLan
 * <p>
 * The main class for network operations
 */
public class NetworkService {
    private static final Logger LOGGER = Logger.getLogger(NetworkService.class.getName());
    private static final ScheduledExecutorService service = Executors.newScheduledThreadPool(Constants.NETWORK_THREADS);
    static DatagramSocket sendSocket; // UDP socket for send discovery
    private static DatagramSocket receiveSocket; // UDP socket for receive discovery
    private static ServerSocket messageReceiveSocket; // TCP socket for receiving messages
    private static ServerSocket fileReceiveSocket; // TCP socket for receiving files
    private static boolean serviceUp; // Control field for threads. (To safely terminate)

    // network tasks
    private static ScheduledFuture<?> discoverySendTask;
    private static ScheduledFuture<?> discoveryReceiveTask;
    private static ScheduledFuture<?> messageReceiveTask;
    private static ScheduledFuture<?> fileReceiveTask;

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
        if (receivedData.equals(Constants.DISCOVERY_COMMAND_REQUEST_ROOM) && Properties.roomMode) {
            DiscoveryService.sendDiscoveryResponse(receivePacket.getAddress(), true);
        } else if (receivedData.equals(Constants.DISCOVERY_COMMAND_REQUEST)) {
            DiscoveryService.sendDiscoveryResponse(receivePacket.getAddress(), false);
        } else if (receivedData.startsWith(Constants.DISCOVERY_COMMAND_RESPONSE_ROOM)) {
            DiscoveryService.receiveDiscoveryResponse(receivePacket.getAddress(), receivedData, true);
        } else if (receivedData.startsWith(Constants.DISCOVERY_COMMAND_RESPONSE)) {
            DiscoveryService.receiveDiscoveryResponse(receivePacket.getAddress(), receivedData, false);
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
        String message;
        try {
            message = inputStream.readUTF();
        } catch (EOFException e) {
            return;
        }
        // Get messages
        if (message.startsWith(Constants.COMMAND_MESSAGE + Constants.COMMAND_SEPARATOR)) {
            message = message.replace((Constants.COMMAND_MESSAGE + Constants.COMMAND_SEPARATOR), "");
            MessageService.receiveMessage(incomingSocket.getInetAddress(), message);
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
                LOGGER.severe("invalid file send request from " + incomingSocket.getInetAddress());
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
        InetAddress broadcastAll = InetAddress.getByName("255.255.255.255");
        try {
            DiscoveryService.sendDiscoveryRequest(broadcastAll);
            DiscoveryService.sendDiscoveryRequestRoom(broadcastAll);
        } catch (IOException e) {
            LOGGER.severe("Discovery send error to " + broadcastAll + " - \n" + e.getLocalizedMessage());
        }

        //<editor-fold desc="Broadcast the message over all the network interfaces" defaultstate=collapsed>
        for (NetworkInterface networkInterface : Utils.networkInterfaces) {
            if (networkInterface.isUp()) { // Check for connectivity
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcastAddress = interfaceAddress.getBroadcast();
                    if (broadcastAddress == null) {
                        continue;
                    }
                    // Send the broadcast package!
                    try {
                        DiscoveryService.sendDiscoveryRequest(broadcastAddress);
                        DiscoveryService.sendDiscoveryRequestRoom(broadcastAddress);
                    } catch (IOException e) {
                        LOGGER.severe("Discovery send error to " + broadcastAddress + " - \n" + e.getLocalizedMessage());
                    }
                }

                // If interface is got up after program start, its host address will be added.
                Utils.hostAddresses.addAll(networkInterface.getInterfaceAddresses());
            } else {
                // If interface is got down after program start, its host address will be removed.
                networkInterface.getInterfaceAddresses().forEach(Utils.hostAddresses::remove);
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
        Utils.initInterfaces();
        sendSocket = new DatagramSocket(Constants.SEND_PORT);
        sendSocket.setBroadcast(true); // This is important to broadcasting
        receiveSocket = new DatagramSocket(Constants.RECEIVE_PORT, InetAddress.getByName("0.0.0.0"));
//            receiveSocket.setBroadcast(true); // Not necessary here
        receiveSocket.setSoTimeout(Constants.RECEIVE_TIMEOUT);
        messageReceiveSocket = new ServerSocket(Constants.RECEIVE_PORT);
        messageReceiveSocket.setSoTimeout(Constants.RECEIVE_TIMEOUT);
        fileReceiveSocket = new ServerSocket(Constants.FILE_RECEIVE_PORT);
        fileReceiveSocket.setSoTimeout(Constants.RECEIVE_TIMEOUT);
    }

    private static void stopConnections() throws IOException {
        if (sendSocket != null) {
            sendSocket.close();
        }
        if (receiveSocket != null) {
            receiveSocket.close();
        }
        if (messageReceiveSocket != null) {
            messageReceiveSocket.close();
        }
        if (fileReceiveSocket != null) {
            fileReceiveSocket.close();
        }
    }

    /**
     * Create and start network threads
     *
     * @throws IOException IOException on connections
     */
    public static void start() throws IOException {
        initConnections();

        // Create threads
        Runnable sendThread = () -> {
            try {
                send();
            } catch (Exception e) {
                LOGGER.severe("Error on send() \n" + e);
            }
        };

        Runnable receiveThread = (() -> {
            try {
                receive();
            } catch (Exception e) {
                LOGGER.severe("Error on receive() \n" + e);
            }
        });

        Runnable receiveMessageThread = (() -> {
            try {
                receiveMessage();
            } catch (Exception e) {
                LOGGER.severe("Error on receiveMessage() \n" + e);
            }
        });

        Runnable receiveFileThread = (() -> {
            try {
                receiveFile();
            } catch (Exception e) {
                LOGGER.severe("Error on receiveFile() \n" + e);
            }
        });

        // Execute threads
        discoverySendTask = service.scheduleWithFixedDelay(sendThread, 0L, Constants.DISCOVERY_INTERVAL, TimeUnit.MILLISECONDS);
        discoveryReceiveTask = service.scheduleWithFixedDelay(receiveThread, 0L, 300L, TimeUnit.MILLISECONDS);
        messageReceiveTask = service.scheduleWithFixedDelay(receiveMessageThread, 0L, 100L, TimeUnit.MILLISECONDS);
        fileReceiveTask = service.scheduleWithFixedDelay(receiveFileThread, 0L, 300L, TimeUnit.MILLISECONDS);
        serviceUp = true;
    }

    public static void end() throws IOException {
        endTask(discoverySendTask);
        endTask(discoveryReceiveTask);
        endTask(messageReceiveTask);
        endTask(fileReceiveTask);
        try {
            service.awaitTermination(Constants.RECEIVE_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) {
        }
        stopConnections();
        serviceUp = false;
    }

    private static void endTask(ScheduledFuture<?> task) {
        if (task != null && (!task.isDone() || !task.isCancelled())) {
            task.cancel(true);
        }
    }

    public static boolean isServiceUp() {
        return serviceUp;
    }
}
