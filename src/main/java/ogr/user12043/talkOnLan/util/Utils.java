package ogr.user12043.talkOnLan.util;

import ogr.user12043.talkOnLan.dao.DBConnection;
import ogr.user12043.talkOnLan.dao.UserDao;
import ogr.user12043.talkOnLan.model.Message;
import ogr.user12043.talkOnLan.model.User;
import ogr.user12043.talkOnLan.ui.MainUI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.File;
import java.net.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
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
    private static final Logger LOGGER = LogManager.getLogger(Utils.class);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm - dd/MM/yyyy"); // Date display format date on ui
    private static User self;
    private static User selfRoom;

    /**
     * Detects network hardware and sets into {@link Utils#networkInterfaces}
     */
    public static void initInterfaces() {
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
            LOGGER.error("Error on getting network devices info");
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

    /**
     * Changes look and feel and updates the {@link MainUI}
     *
     * @param themeName Theme name to set
     * @return true if theme found and applied
     */
    public static boolean changeTheme(String themeName) {
        try {
            LookAndFeel lookAndFeel = Themes.get(themeName);
            if (lookAndFeel != null) {
                UIManager.setLookAndFeel(lookAndFeel);
                SwingUtilities.updateComponentTreeUI(MainUI.getUI());
                return true;
            } else {
                for (UIManager.LookAndFeelInfo lookAndFeelInfo : UIManager.getInstalledLookAndFeels()) {
                    if (lookAndFeelInfo.getName().equals(themeName)) {
                        UIManager.setLookAndFeel(lookAndFeelInfo.getClassName());
                        SwingUtilities.updateComponentTreeUI(MainUI.getUI());
                        return true;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return false;
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

    public static User self() {
        try {
            if (self == null) {
                self = new User(Properties.username, InetAddress.getLocalHost(), false);
            }
            return self;
        } catch (UnknownHostException e) {
            LOGGER.error(e);
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
                    "-- ID Sequences\n" +
                            "CREATE SEQUENCE PUBLIC.SEQ_USER;\n" +
                            "CREATE SEQUENCE PUBLIC.SEQ_MESSAGE;\n" +
                            "\n" +
                            "-- PUBLIC.USERS definition\n" +
                            "\n" +
                            "-- Drop table\n" +
                            "\n" +
                            "-- DROP TABLE PUBLIC.USERS;\n" +
                            "\n" +
                            "CREATE TABLE PUBLIC.USERS (\n" +
                            "\tID INTEGER DEFAULT NEXT VALUE FOR \"PUBLIC\".\"SEQ_USER\" NOT NULL,\n" +
                            "\tUSERNAME VARCHAR(100) NOT NULL,\n" +
                            "\tADDRESS VARCHAR(15) NOT NULL,\n" +
                            "\tIS_ROOM BOOLEAN,\n" +
                            "\tCONSTRAINT USERS_PK PRIMARY KEY (ID)\n" +
                            ");\n" +
                            "CREATE UNIQUE INDEX PRIMARY_KEY_61 ON PUBLIC.USERS (ID);\n" +
                            "\n" +
                            "-- PUBLIC.MESSAGES definition\n" +
                            "\n" +
                            "-- Drop table\n" +
                            "\n" +
                            "-- DROP TABLE PUBLIC.MESSAGES;\n" +
                            "\n" +
                            "CREATE TABLE PUBLIC.MESSAGES (\n" +
                            "\tID INTEGER DEFAULT NEXT VALUE FOR \"PUBLIC\".\"SEQ_MESSAGE\" NOT NULL,\n" +
                            "\tCONTENT VARCHAR(500),\n" +
                            "\tSENT_DATE BIGINT NOT NULL,\n" +
                            "\t\"TYPE\" SMALLINT NOT NULL,\n" +
                            "\tSENDER_ID INTEGER NOT NULL,\n" +
                            "\tRECEIVER_ID INTEGER NOT NULL,\n" +
                            "\tFWD_USER_ID INTEGER,\n" +
                            "\tSENT BOOLEAN,\n" +
                            "\tCONSTRAINT MESSAGES_PK PRIMARY KEY (ID)\n" +
                            ");\n" +
                            "CREATE INDEX MESSAGES_FK_FWD_INDEX_8 ON PUBLIC.MESSAGES (FWD_USER_ID);\n" +
                            "CREATE INDEX MESSAGES_FK_RECEIVER_INDEX_8 ON PUBLIC.MESSAGES (RECEIVER_ID);\n" +
                            "CREATE INDEX MESSAGES_FK_SENDER_INDEX_8 ON PUBLIC.MESSAGES (SENDER_ID);\n" +
                            "CREATE UNIQUE INDEX PRIMARY_KEY_8 ON PUBLIC.MESSAGES (ID);\n" +
                            "\n" +
                            "\n" +
                            "-- PUBLIC.MESSAGES foreign keys\n" +
                            "\n" +
                            "ALTER TABLE PUBLIC.MESSAGES ADD CONSTRAINT MESSAGES_FK_FWD FOREIGN KEY (FWD_USER_ID) REFERENCES PUBLIC.USERS(ID) ON DELETE RESTRICT ON UPDATE RESTRICT;\n" +
                            "ALTER TABLE PUBLIC.MESSAGES ADD CONSTRAINT MESSAGES_FK_RECEIVER FOREIGN KEY (RECEIVER_ID) REFERENCES PUBLIC.USERS(ID) ON DELETE RESTRICT ON UPDATE RESTRICT;\n" +
                            "ALTER TABLE PUBLIC.MESSAGES ADD CONSTRAINT MESSAGES_FK_SENDER FOREIGN KEY (SENDER_ID) REFERENCES PUBLIC.USERS(ID) ON DELETE RESTRICT ON UPDATE RESTRICT;");
            connection.closeStatement();
        }
    }
}
