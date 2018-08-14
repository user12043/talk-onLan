package ogr.user12043.talkOnLan.net;

import ogr.user12043.talkOnLan.User;
import ogr.user12043.talkOnLan.ui.FileTransferDialog;
import ogr.user12043.talkOnLan.ui.MainUI;
import ogr.user12043.talkOnLan.util.Constants;
import ogr.user12043.talkOnLan.util.Properties;
import ogr.user12043.talkOnLan.util.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;

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
            outputStream = new DataOutputStream(socket.getOutputStream());
            inputStream = new DataInputStream(socket.getInputStream());
            fileInputStream = new DataInputStream(new FileInputStream(file));
            // Send send request
            outputStream.writeUTF(Constants.COMMAND_FILE_TRANSFER_REQUEST + Constants.COMMAND_SEPARATOR + file.length() + Constants.COMMAND_SEPARATOR + file.getName());
            LOGGER.info("file send request sent to " + user.getAddress() + "for file \"" + file.getName() + "\"");

            // Receive ok response
            String response = inputStream.readUTF();
            switch (response) {
                case Constants.COMMAND_FILE_TRANSFER_RESPONSE_ACCEPT:
                    LOGGER.info("sending file \"" + file.getName() + "\" to " + user.getAddress());

                    dialog.startTransfer();
                    byte[] buffer = new byte[Constants.FILE_BUFFER_LENGTH];
                    int readBytes;
                    long ration = file.length() / 100;
                    long amountProcessed = 0;
                    try {
                        while ((readBytes = fileInputStream.read(buffer)) != -1) {
                            if (dialog.cancelled()) {
                                socket.shutdownOutput();
                                socket.close();
                                fileInputStream.close();
                                return;
                            }
                            outputStream.write(buffer, 0, readBytes);
                            amountProcessed += readBytes;
                            dialog.setProgress((int) (amountProcessed / ration));
                        }
                    } catch (SocketException e) {
                        if (e.getMessage().equals("Software caused connection abort: socket write error")) {
                            JOptionPane.showMessageDialog(dialog, ("File did not send! Transfer interrupted by receiver"), "ERROR", JOptionPane.ERROR_MESSAGE);
                            dialog.dispose();
                        } else {
                            throw e;
                        }
                    }

                    LOGGER.info("\"" + file.getName() + "\" file sent to " + user.getAddress());
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
                if (outputFile.exists()) { // Ask user for overwriting
                    final int option = JOptionPane.showConfirmDialog(MainUI.getUI(), "The receiving file is already exists. Do you want to continue and overwrite the file?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (option != 0) {
                        // send deny response
                        outputStream.writeUTF(Constants.COMMAND_FILE_TRANSFER_RESPONSE_REJECT);
                        LOGGER.info("file \"" + fileName + "\" deny response sent to " + incomingSocket.getInetAddress());
                        incomingSocket.close();
                    }
                }
                outputFile.getParentFile().mkdirs(); // Create parent folder of file if does not exists
                fileOutputStream = new DataOutputStream(new FileOutputStream(outputFile, false));

                // send allow response
                outputStream.writeUTF(Constants.COMMAND_FILE_TRANSFER_RESPONSE_ACCEPT);
                LOGGER.info("file \"" + fileName + "\" allow response sent to " + incomingSocket.getInetAddress());

                FileTransferDialog dialog = new FileTransferDialog(MainUI.getUI(), user, false, fileName);
                dialog.startTransfer();
                byte[] buffer = new byte[Constants.FILE_BUFFER_LENGTH];
                int readBytes;
                long ration = fileSize / 100;
                long amountProcessed = 0;
                while ((readBytes = inputStream.read(buffer)) != -1) {
                    if (dialog.cancelled()) {
                        fileOutputStream.close();
                        outputFile.delete();
                        incomingSocket.close();
                        return;
                    }
                    fileOutputStream.write(buffer, 0, readBytes);
                    amountProcessed += readBytes;
                    dialog.setProgress((int) (amountProcessed / ration));
                }
                // Detect send interrupts
                if (amountProcessed != fileSize) {
                    JOptionPane.showMessageDialog(dialog, "Transfer interrupted! The sender cancelled transfer!", "ERROR", JOptionPane.ERROR_MESSAGE);
                    dialog.dispose();
                    fileOutputStream.close();
                    outputFile.delete();
                } else {
                    LOGGER.info("file \"" + fileName + "\" received from " + incomingSocket.getInetAddress());
                }
            } else {
                // send deny response
                outputStream.writeUTF(Constants.COMMAND_FILE_TRANSFER_RESPONSE_REJECT);
                LOGGER.info("file \"" + fileName + "\" deny response sent to " + incomingSocket.getInetAddress());
            }
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            incomingSocket.close();
        }

    }
}
