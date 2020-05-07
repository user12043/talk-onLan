package ogr.user12043.talkOnLan.util;

import ogr.user12043.talkOnLan.Message;
import ogr.user12043.talkOnLan.User;
import ogr.user12043.talkOnLan.ui.MainUI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
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
    private static final Logger LOGGER = LogManager.getLogger(Constants.class);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm - dd/MM/yyyy"); // Date display format date on ui

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
     */
    public static void changeTheme(String themeName) {
        try {
            LookAndFeel lookAndFeel = Themes.get(themeName);
            if (lookAndFeel != null) {
                UIManager.setLookAndFeel(lookAndFeel);
            } else {
                for (UIManager.LookAndFeelInfo lookAndFeelInfo : UIManager.getInstalledLookAndFeels()) {
                    if (lookAndFeelInfo.getName().equals(themeName)) {
                        UIManager.setLookAndFeel(lookAndFeelInfo.getClassName());
                    }
                }
            }
            SwingUtilities.updateComponentTreeUI(MainUI.getUI());
        } catch (Exception ignored) {
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
        return first.orElse(null);
    }

    public static User findRoom(InetAddress inetAddress) {
        Optional<User> first = rooms.stream().filter(user -> user.getAddress().equals(inetAddress)).findFirst();
        return first.orElse(null);
    }

    public static User findRoomByUsername(String username) {
        Optional<User> first = rooms.stream().filter(user -> user.getUserName().equals(username)).findFirst();
        return first.orElse(null);
    }

    public static String generateMessage(Message message) {
        return String.format("%s%c%s%c%d",
                !message.isRoomMessage() ? Constants.COMMAND_MESSAGE : Constants.COMMAND_MESSAGE_ROOM,
                Constants.COMMAND_SEPARATOR, message.getContent(),
                Constants.COMMAND_SEPARATOR, message.getSentDate().getTime());
    }

    public static Message parseMessage(String content) {
        String[] split = Pattern.compile(String.valueOf(Constants.COMMAND_SEPARATOR), Pattern.LITERAL).split(content);
        Message message = new Message();
        message.setContent(split[0]);
        message.setSentDate(new Date(Long.parseLong(split[1])));
        return message;
    }
}
