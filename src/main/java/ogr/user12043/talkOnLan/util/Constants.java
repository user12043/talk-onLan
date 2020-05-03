package ogr.user12043.talkOnLan.util;

/**
 * Created by user12043 on 23.07.2018 - 16:36
 * part of project: talk-onLan
 */
public class Constants {
    public static final String DISCOVERY_COMMAND_REQUEST = "talk-onLan_APPLICATION_DISCOVERY_REQUEST";
    public static final String DISCOVERY_COMMAND_REQUEST_ROOM = "talk-onLan_APPLICATION_DISCOVERY_REQUEST_ROOM";
    public static final String DISCOVERY_COMMAND_RESPONSE = "talk-onLan_APPLICATION_DISCOVERY_RESPONSE";
    public static final String DISCOVERY_COMMAND_RESPONSE_ROOM = "talk-onLan_APPLICATION_DISCOVERY_RESPONSE_ROOM";
    public static final String COMMAND_MESSAGE = "talk-onLan_APPLICATION_MESSAGE";
    public static final String COMMAND_FILE_TRANSFER_REQUEST = "talk-onLan_FILE_TRANSFER_REQUEST";
    public static final String COMMAND_FILE_TRANSFER_RESPONSE_ACCEPT = "talk-onLan_FILE_TRANSFER_RESPONSE_ACCEPT";
    public static final String COMMAND_FILE_TRANSFER_RESPONSE_REJECT = "talk-onLan_FILE_TRANSFER_RESPONSE_REJECT";
    public static final char COMMAND_SEPARATOR = '|';
    public static final int RECEIVE_PORT = 8888;
    public static final int SEND_PORT = 8889;
    public static final int FILE_RECEIVE_PORT = 8890;
    public static final int RECEIVE_TIMEOUT = 5000;
    public static final int DISCOVERY_INTERVAL = 3000;
    public static final int DISCOVERY_BUFFER_LENGTH = 64;
    public static final int FILE_BUFFER_LENGTH = 16384;
    public static final int NETWORK_THREADS = 4;
}
