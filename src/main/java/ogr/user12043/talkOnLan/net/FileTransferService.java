package ogr.user12043.talkOnLan.net;

import ogr.user12043.talkOnLan.User;
import ogr.user12043.talkOnLan.ui.FileTransferDialog;
import ogr.user12043.talkOnLan.ui.MainUI;
import ogr.user12043.talkOnLan.util.Constants;
import ogr.user12043.talkOnLan.util.Properties;
import ogr.user12043.talkOnLan.util.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;

/**
 * Created by ME99735 on 10.08.2018 - 09:03
 * part of project: talk-onLan
 * <p>
 * Does file transferring
 */
public class FileTransferService {
    private static final Logger LOGGER = LogManager.getLogger(FileTransferService.class);

    public static void sendFile(User user, File file) throws IOException, SecurityException {
        FileTransferDialog dialog = new FileTransferDialog(MainUI.getUI(), user, true, file.getName());

        Socket socket = null;
        DataOutputStream outputStream;
        DataInputStream inputStream;
        DataInputStream fileInputStream = null;
        try {
            socket = new Socket(user.getAddress(), Constants.FILE_RECEIVE_PORT);
            socket.setSoTimeout(Constants.RECEIVE_TIMEOUT);
            outputStream = new DataOutputStream(socket.getOutputStream());
            inputStream = new DataInputStream(socket.getInputStream());
            fileInputStream = new DataInputStream(new FileInputStream(file));
            // Send send request
            outputStream.writeUTF(Constants.COMMAND_FILE_TRANSFER_REQUEST + Constants.COMMAND_SEPARATOR + file.length() + Constants.COMMAND_SEPARATOR + file.getName());
            LOGGER.info("file send request sent to " + user.getAddress());

            // Receive ok response
            String response = inputStream.readUTF();
            switch (response) {
                case Constants.COMMAND_FILE_TRANSFER_RESPONSE_ACCEPT:
                    LOGGER.info("sending file \"" + file.getName() + "\" to " + user.getAddress());

                    dialog.startSend();
                    byte[] buffer = new byte[Constants.FILE_BUFFER_LENGTH];
                    int readBytes;
                    long ration = file.length() / 100;
                    long amountProcessed = 0;
                    while ((readBytes = fileInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, readBytes);
                        amountProcessed += readBytes;
                        dialog.setProgress((int) (amountProcessed / ration));
                    }

                    LOGGER.info("\"" + file.getName() + "\"file sent to " + user.getAddress());
                    break;
                case Constants.COMMAND_FILE_TRANSFER_RESPONSE_REJECT:
                    dialog.dispose();
                    throw new SecurityException();
                default:
                    LOGGER.info("invalid send file response received from " + user.getAddress());
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
        final User user = new User();
        final boolean buddyExists = Utils.buddies.stream().anyMatch(u -> {
            if (u.getAddress().equals(incomingSocket.getInetAddress())) {
                user.setUserName(u.getUserName());
                user.setAddress(u.getAddress());
                return true;
            }
            return false;
        });
        if (!buddyExists) {
            DiscoveryService.sendDiscoveryRequest(incomingSocket.getInetAddress());
            user.setUserName("<Unknown user>");
            user.setAddress(incomingSocket.getInetAddress());
        }
        DataOutputStream outputStream;
        DataInputStream inputStream;
        DataOutputStream fileOutputStream = null;
        try {
            outputStream = new DataOutputStream(incomingSocket.getOutputStream());
            inputStream = new DataInputStream(incomingSocket.getInputStream());
            if (MainUI.getUI().confirmFileReceive(user, fileName, fileSize)) {
                File outputFile = new File(Properties.fileReceiveFolder + fileName);
                outputFile.getParentFile().mkdirs(); // Create parent folder of file if does not exists
                fileOutputStream = new DataOutputStream(new FileOutputStream(outputFile, false));

                // send allow response
                outputStream.writeUTF(Constants.COMMAND_FILE_TRANSFER_RESPONSE_ACCEPT);
                LOGGER.info("file send allow response sent to " + incomingSocket.getInetAddress());

                FileTransferDialog dialog = null;
                dialog = new FileTransferDialog(MainUI.getUI(), user, false, fileName);
                byte[] buffer = new byte[Constants.FILE_BUFFER_LENGTH];
                int readBytes;
                long ration = fileSize / 100;
                long amountProcessed = 0;
                while ((readBytes = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, readBytes);
                    amountProcessed += readBytes;
                    dialog.setProgress((int) (amountProcessed / ration));
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
