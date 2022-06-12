package ogr.user12043.talkOnLan.net;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressIndicator;
import ogr.user12043.talkOnLan.model.User;
import ogr.user12043.talkOnLan.ui.FileTransferDialog;
import ogr.user12043.talkOnLan.ui.MainUI;
import ogr.user12043.talkOnLan.util.Constants;
import ogr.user12043.talkOnLan.util.Properties;
import ogr.user12043.talkOnLan.util.Utils;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/**
 * Created by ME99735 on 10.08.2018 - 09:03
 * part of project: talk-onLan
 * <p>
 * Does file transferring
 */
public class FileTransferService {
    private static final Logger LOGGER = Logger.getLogger(FileTransferService.class.getName());

    public static void sendFile(User user, File file) throws IOException {
        // Create send dialog by Alert
        AtomicReference<Alert> a = new AtomicReference<>(); // Alert will be used by multiple threads
        AtomicBoolean dialogClosed = new AtomicBoolean(false); // To be set true when sender cancels
        Utils.platformRunAndWait(() -> {
            a.set(new Alert(AlertType.NONE));
            a.get().setTitle(file.getName());
            a.get().getButtonTypes().add(ButtonType.CANCEL);
            final ProgressIndicator indicator = new ProgressIndicator(0.0);
            a.get().setGraphic(indicator);
            a.get().setHeaderText("Send file to " + user.getUsername() + " at " + user.getAddress());
            a.get().setOnCloseRequest(event -> dialogClosed.set(true));
            a.get().show();
        });

        Socket socket = null;
        DataOutputStream outputStream;
        DataInputStream inputStream;
        DataInputStream fileInputStream = null;
        try {
            socket = new Socket(user.getAddress(), Constants.FILE_RECEIVE_PORT);
            outputStream = new DataOutputStream(socket.getOutputStream());
            inputStream = new DataInputStream(socket.getInputStream());
            fileInputStream = new DataInputStream(new FileInputStream(file));
            // Send the file send request
            outputStream.writeUTF(Constants.COMMAND_FILE_TRANSFER_REQUEST + Constants.COMMAND_SEPARATOR + file.length() + Constants.COMMAND_SEPARATOR + file.getName());
            LOGGER.info("file send request sent to " + user.getAddress() + "for file \"" + file.getName() + "\"");
            Utils.platformRunAndWait(() ->
                    a.get().setContentText("Waiting for approval on " + user.getUsername() + " side..."));

            String response = inputStream.readUTF();
            switch (response) {
                case Constants.COMMAND_FILE_TRANSFER_RESPONSE_ACCEPT:
                    // Receive ok response
                    LOGGER.info("sending file \"" + file.getName() + "\" to " + user.getAddress());

                    Utils.platformRunAndWait(() -> a.get().setContentText("Sending the file..."));
                    byte[] buffer = new byte[Constants.FILE_BUFFER_LENGTH];
                    int readBytes;
                    double amountProcessed = 0;
                    while ((readBytes = fileInputStream.read(buffer)) != -1) {
                        if (dialogClosed.get()) {
                            socket.shutdownOutput();
                            socket.close();
                            fileInputStream.close();
                            LOGGER.info("sending interrupted, user closed the dialog");
                            return;
                        }
                        outputStream.write(buffer, 0, readBytes);
                        amountProcessed += readBytes;
                        double progress = amountProcessed / file.length();
                        Utils.platformRunAndWait(() -> {
                            ((ProgressIndicator) a.get().getGraphic()).setProgress(progress);
                            a.get().setTitle("%" + Math.ceil(progress) + " " + file.getName());
                        });
                    }

                    LOGGER.info("\"" + file.getName() + "\" file sent to " + user.getAddress());
                    Utils.platformRunAndWait(() -> {
                        a.get().setAlertType(AlertType.INFORMATION);
                        a.get().setContentText("Successfully sent the file");
                        a.get().getButtonTypes().clear();
                        a.get().getButtonTypes().add(ButtonType.FINISH);
                    });
                    break;
                case Constants.COMMAND_FILE_TRANSFER_RESPONSE_REJECT:
                    // Receive negative response
                    Utils.platformRunAndWait(() -> {
                        a.get().setAlertType(AlertType.WARNING);
                        a.get().setContentText("File did not send. Target user rejected the request");
                        a.get().getButtonTypes().clear();
                        a.get().getButtonTypes().add(ButtonType.CLOSE);
                    });
                default:
                    LOGGER.info("invalid send file response received from " + user.getAddress());
            }
        } catch (SocketException e) {
            LOGGER.severe("transfer process interrupted! " + e);
            Platform.runLater(() -> {
                a.get().setAlertType(AlertType.ERROR);
                a.get().setContentText("Transfer process interrupted!");
            });
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
                user.setUsername(u.getUsername());
                user.setAddress(u.getAddress());
                return true;
            }
            return false;
        });
        if (!buddyExists) {
            DiscoveryService.sendDiscoveryRequest(incomingSocket.getInetAddress());
            user.setUsername("<Unknown user>");
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
