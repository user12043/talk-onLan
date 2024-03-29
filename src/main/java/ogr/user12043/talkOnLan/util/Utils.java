package ogr.user12043.talkOnLan.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import ogr.user12043.talkOnLan.dao.DBConnection;
import ogr.user12043.talkOnLan.dao.UserDao;
import ogr.user12043.talkOnLan.model.Message;
import ogr.user12043.talkOnLan.model.User;

import javax.swing.*;
import java.io.File;
import java.net.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Created by user12043 on 24.07.2018 - 11:56
 * part of project: talk-onLan
 */
public class Utils {
    public static final Set<User> buddies = new HashSet<>(); // users for discovered addresses
    public static final Set<User> rooms = new HashSet<>(); // users for discovered servers
    public static final List<NetworkInterface> networkInterfaces = new ArrayList<>(); // network hardware list of device
    public static final Set<InterfaceAddress> hostAddresses = new HashSet<>(); // self ip addresses on each network hardware
    public static final Set<User> roomClients = new HashSet<>(); // users connected the room while hosting room
    private static final Logger LOGGER = Logger.getLogger(Utils.class.getName());
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm - dd/MM/yyyy"); // Date display format date on ui
    private static User self;
    private static User selfRoom;

    /**
     * Detects network hardware and sets into {@link Utils#networkInterfaces}
     */
    public static void initInterfaces() {
        networkInterfaces.clear();
        hostAddresses.clear();
        try {
            final Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
            while (enumeration.hasMoreElements()) {
                NetworkInterface networkInterface = enumeration.nextElement();
                if (networkInterface.isLoopback()) {
                    continue; // skip loop back
                }
                networkInterfaces.add(networkInterface);
                hostAddresses.addAll(networkInterface.getInterfaceAddresses());
            }
        } catch (SocketException e) {
            LOGGER.severe("Error on getting network devices info\n" + e);
        }
    }

    /**
     * Formats date for display to user
     *
     * @param date date to format
     * @return formatted date string
     */
    public static String formatDate(Date date) {
        return dateFormat.format(date);
    }

    /**
     * Creates a string array contains names of available swing look and feels
     *
     * @return Names of themes
     */
    public static String[] getLookAndFeels() {
//        List<String> installed = Arrays.stream(Themes.INSTALLED_LOOK_AND_FEELS).map(UIManager.LookAndFeelInfo::getName).collect(Collectors.toList());
//        installed.addAll(Arrays.asList(Themes.THEMES));
        return Arrays.stream(Themes.INSTALLED_LOOK_AND_FEELS).map(UIManager.LookAndFeelInfo::getName).toArray(String[]::new);
    }

    public static String getCurrentTheme() {
        for (UIManager.LookAndFeelInfo lookAndFeel : UIManager.getInstalledLookAndFeels()) {
            if (lookAndFeel.getClassName().equals(UIManager.getLookAndFeel().getClass().getName())) {
                return lookAndFeel.getName();
            }
        }
        return "";
    }

    public static boolean isDiscovered(User user) {
        if (user.isRoom()) {
            return rooms.stream().anyMatch(u -> u.equals(user));
        } else {
            return buddies.stream().anyMatch(u -> u.equals(user));
        }
    }

    public static boolean isDiscovered(InetAddress inetAddress) {
        return buddies.stream().anyMatch(user -> user.getAddress().equals(inetAddress));
    }

    public static boolean isDiscoveredRoom(InetAddress inetAddress) {
        return rooms.stream().anyMatch(user -> user.getAddress().equals(inetAddress));
    }

    public static User findBuddy(InetAddress inetAddress) {
        Optional<User> first = buddies.stream().filter(user -> user.getAddress().equals(inetAddress)).findFirst();
        return first.orElseGet(() -> UserDao.get().findByAddress(inetAddress, false));
    }

    private static User findBuddyByUsername(String username) {
        Optional<User> first = buddies.stream().filter(user -> user.getUsername().equals(username)).findFirst();
        return first.orElseGet(() -> UserDao.get().findByUsername(username));
    }

    public static User findRoom(InetAddress inetAddress) {
        Optional<User> first = rooms.stream().filter(user -> user.getAddress().equals(inetAddress)).findFirst();
        return first.orElseGet(() -> UserDao.get().findByAddress(inetAddress, true));
    }

    public static String generateMessage(Message message) {
        return String.format("%s%c%s%c%d%c%d%c%s",
                Constants.COMMAND_MESSAGE,
                Constants.COMMAND_SEPARATOR, message.getContent(),
                Constants.COMMAND_SEPARATOR, message.getSentDate().getTime(),
                Constants.COMMAND_SEPARATOR, message.getMessageType(),
                Constants.COMMAND_SEPARATOR, message.getForwardedFrom().getUsername());
    }

    public static Message parseMessage(String content, InetAddress senderAddress) {
        String[] split = Pattern.compile(String.valueOf(Constants.COMMAND_SEPARATOR), Pattern.LITERAL).split(content);
        Message message = new Message();
        message.setContent(split[0]);
        message.setSentDate(new Date(Long.parseLong(split[1])));
        message.setMessageType(Integer.parseInt(split[2]));
        message.setForwardedFrom(findBuddyByUsername(split[3]));
        User user = message.getMessageType() != Constants.MSG_TYPE_FWD ? findBuddy(senderAddress)
                : findRoom(senderAddress);
        message.setSender(user);
        message.setReceiver(message.getMessageType() != Constants.MSG_TYPE_ROOM ? Utils.self() : Utils.selfRoom());
        message.setSent(true);

        return message;
    }

    public static void addUser(User user) {
        if (user.isRoom()) {
            rooms.add(user);
        } else {
            buddies.add(user);
        }
    }

    public static void removeUser(User user) {
        if (user.isRoom()) {
            rooms.remove(user);
        } else {
            buddies.remove(user);
        }
    }

    public static User self() {
        try {
            if (self == null) {
                self = new User(Properties.username, InetAddress.getLocalHost(), false);
            }
            return self;
        } catch (UnknownHostException e) {
            LOGGER.severe("Error on Utils::self\n" + e);
            System.exit(1);
        }
        return new User();
    }

    public static User selfRoom() {
        if (selfRoom == null) {
            User self = self();
            selfRoom = self.cloneUser();
            selfRoom.setRoom(true);
        }
        return selfRoom;
    }

    public static void saveSelf() {
        // create self user if not exists
        User existing = UserDao.get().findByFields(self());
        if (existing == null) {
            UserDao.get().save(self());
            existing = UserDao.get().findByFields(self());
        }
        self = existing;
    }

    public static void saveSelfRoom() {
        // create self user if not exists
        User existing = UserDao.get().findByFields(selfRoom());
        if (existing == null) {
            UserDao.get().save(selfRoom());
            existing = UserDao.get().findByFields(selfRoom());
        }
        selfRoom = existing;
    }

    public static void initDatabase() throws SQLException {
        File dbFile = new File(Constants.DB_FILE);
        if (!dbFile.exists()) {
            final DBConnection connection = DBConnection.get();
            connection.openStatement();
            connection.executeUpdateQuery( // Contents from db.sql
                    "CREATE TABLE USERS (\n" +
                            "\tID INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                            "\tUSERNAME VARCHAR(100) NOT NULL,\n" +
                            "\tADDRESS VARCHAR(15) NOT NULL,\n" +
                            "\tIS_ROOM BOOLEAN,\n" +
                            "\tIS_BLOCKED BOOLEAN\n" +
                            ")");
            connection.executeUpdateQuery("CREATE UNIQUE INDEX PRIMARY_KEY_61 ON USERS (ID);");
            connection.executeUpdateQuery("CREATE UNIQUE INDEX USERS_USERNAME_IDX ON USERS (USERNAME,ADDRESS,IS_ROOM);");
            connection.executeUpdateQuery(
                    "CREATE TABLE MESSAGES (\n" +
                            "\tID INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                            "\tCONTENT VARCHAR(500),\n" +
                            "\tSENT_DATE BIGINT NOT NULL,\n" +
                            "\t\"TYPE\" SMALLINT NOT NULL,\n" +
                            "\tSENDER_ID INTEGER NOT NULL,\n" +
                            "\tRECEIVER_ID INTEGER NOT NULL,\n" +
                            "\tFWD_USER_ID INTEGER,\n" +
                            "\tSENT BOOLEAN\n" +
                            ")"
            );
            connection.executeUpdateQuery("CREATE INDEX MESSAGES_FK_FWD_INDEX_8 ON MESSAGES (FWD_USER_ID)");
            connection.executeUpdateQuery("CREATE INDEX MESSAGES_FK_RECEIVER_INDEX_8 ON MESSAGES (RECEIVER_ID)");
            connection.executeUpdateQuery("CREATE INDEX MESSAGES_FK_SENDER_INDEX_8 ON MESSAGES (SENDER_ID)");
            connection.executeUpdateQuery("CREATE UNIQUE INDEX PRIMARY_KEY_8 ON MESSAGES (ID)");
            connection.closeStatement();
        }
    }
    // JavaFX does not provide Platform.runAndWait(). So I need to implement my own one.

    public static void platformRunAndWait(Runnable r) {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                r.run();
            } finally {
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getUserFriendlyFileSize(long fileSize) {
        String fileSizeString = "";
        if (fileSize < 1024) {
            fileSizeString = (fileSize + " Bytes");
        } else if (fileSize < 1024 * 1024) {
            fileSizeString = (fileSize / 1024) + " KB";
        } else if (fileSize < 1024 * 1024 * 1024) {
            fileSizeString = (fileSize / 1024 / 1024) + " MB";
        }
        return fileSizeString;
    }

    public static boolean getConfirmationByAlert(String message) {
        AtomicBoolean confirmed = new AtomicBoolean(false);
        platformRunAndWait(() -> {
            final Optional<ButtonType> buttonType = new Alert(Alert.AlertType.CONFIRMATION, message).showAndWait();
            if (buttonType.isPresent() && buttonType.get() == ButtonType.OK) {
                confirmed.set(true);
            }
        });
        return confirmed.get();
    }
}
