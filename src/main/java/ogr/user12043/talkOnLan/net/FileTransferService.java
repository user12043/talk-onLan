package ogr.user12043.talkOnLan.net;

import ogr.user12043.talkOnLan.util.Constants;
import ogr.user12043.talkOnLan.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ME99735 on 10.08.2018 - 09:03
 * part of project: talk-onLan
 */
public class FileTransferService {
    private static final Logger LOGGER = LogManager.getLogger(FileTransferService.class);
    private static final AtomicInteger atomicInteger = new AtomicInteger(0);

    public static void sendFileRequest(InetAddress address, File file) throws IOException {
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

            // Receive okey response
            String response = inputStream.readUTF();
            if (!response.equals(Constants.COMMAND_FILE_TRANSFER_RESPONSE)) {
                LOGGER.info("invalid send file response from " + address);
                return;
            }
            LOGGER.info("sending file to " + address);
            byte[] buffer = new byte[Constants.BUFFER_LENGTH];
            while (fileInputStream.read(buffer) != -1) {
                outputStream.write(buffer);
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

    public static void receiveFile(Socket incomingSocket, long fileLength, String fileName) throws IOException {
        DataOutputStream outputStream;
        DataInputStream inputStream;
        DataOutputStream fileOutputStream = null;
        try {
            outputStream = new DataOutputStream(incomingSocket.getOutputStream());
            inputStream = new DataInputStream(incomingSocket.getInputStream());
            File outputFile = new File(Properties.fileReceiveFolder + "/" + fileName);
            outputFile.getParentFile().mkdirs();
            fileOutputStream = new DataOutputStream(new FileOutputStream(outputFile, false));
            // send response
            outputStream.writeUTF(Constants.COMMAND_FILE_TRANSFER_RESPONSE);
            LOGGER.info("file send response sent to " + incomingSocket.getInetAddress());

            byte[] buffer = new byte[Constants.BUFFER_LENGTH];
            while (inputStream.read(buffer) != -1) {
                fileOutputStream.write(buffer);
            }
            LOGGER.info("file received from " + incomingSocket.getInetAddress());
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            incomingSocket.close();
        }
    }
}
