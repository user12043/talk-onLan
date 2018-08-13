package ogr.user12043.talkOnLan.net;

import ogr.user12043.talkOnLan.Main;
import ogr.user12043.talkOnLan.util.Constants;
import ogr.user12043.talkOnLan.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by ME99735 on 10.08.2018 - 09:03
 * part of project: talk-onLan
 */
public class FileTransferService {
    private static final Logger LOGGER = LogManager.getLogger(FileTransferService.class);

    public static void sendFile(InetAddress address, File file) throws IOException, SecurityException {
        Socket socket = null;
        DataOutputStream outputStream;
        DataInputStream inputStream;
        DataInputStream fileInputStream = null;
        try {
            socket = new Socket(address, Constants.RECEIVE_PORT);
//        socket.setSoTimeout(Constants.RECEIVE_TIMEOUT);
            outputStream = new DataOutputStream(socket.getOutputStream());
            inputStream = new DataInputStream(socket.getInputStream());
            fileInputStream = new DataInputStream(new FileInputStream(file));
            // Send send request
            outputStream.writeUTF(Constants.COMMAND_FILE_TRANSFER_REQUEST + Constants.COMMAND_SEPARATOR + file.length() + Constants.COMMAND_SEPARATOR + file.getName());
            LOGGER.info("file send request sent to " + address);

            // Receive ok response
            String response = inputStream.readUTF();
            switch (response) {
                case Constants.COMMAND_FILE_TRANSFER_RESPONSE_ACCEPT:
                    LOGGER.info("sending file to " + address);
                    byte[] buffer = new byte[Constants.BUFFER_LENGTH];
                    while (fileInputStream.read(buffer) != -1) {
                        outputStream.write(buffer);
                    }
                    break;
                case Constants.COMMAND_FILE_TRANSFER_RESPONSE_REJECT:
                    throw new SecurityException();
                default:
                    LOGGER.info("invalid send file response received from " + address);
            }
        } finally {
            if (socket != null) {
                socket.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }
    }

    static void receiveFile(Socket incomingSocket, String fileName, long fileSize) throws IOException {
        DataOutputStream outputStream;
        DataInputStream inputStream;
        DataOutputStream fileOutputStream = null;
        try {
            outputStream = new DataOutputStream(incomingSocket.getOutputStream());
            inputStream = new DataInputStream(incomingSocket.getInputStream());
            if (Main.mainUI.confirmFileReceive(incomingSocket.getInetAddress(), fileName, fileSize)) {
                File outputFile = new File(Properties.fileReceiveFolder + "/" + fileName);
                outputFile.getParentFile().mkdirs();
                fileOutputStream = new DataOutputStream(new FileOutputStream(outputFile, false));

                // send allow response
                outputStream.writeUTF(Constants.COMMAND_FILE_TRANSFER_RESPONSE_ACCEPT);
                LOGGER.info("file send allow response sent to " + incomingSocket.getInetAddress());

                byte[] buffer = new byte[Constants.BUFFER_LENGTH];
                while (inputStream.read(buffer) != -1) {
                    fileOutputStream.write(buffer);
                }
                LOGGER.info("file received from " + incomingSocket.getInetAddress());
            } else {
                // send deny response
                outputStream.writeUTF(Constants.COMMAND_FILE_TRANSFER_RESPONSE_REJECT);
                LOGGER.info("file send deny response sent to " + incomingSocket.getInetAddress());
            }
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            incomingSocket.close();
        }
    }
}
