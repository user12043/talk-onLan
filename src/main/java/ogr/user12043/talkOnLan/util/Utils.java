package ogr.user12043.talkOnLan.util;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
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
import java.util.stream.Collectors;

/**
 * Created by user12043 on 24.07.2018 - 11:56
 * part of project: talk-onLan
 */
public class Utils {
    public static final Set<InetAddress> buddyAddresses = new HashSet<>(); // discovered addresses
    public static final Set<InetAddress> connectedServers = new HashSet<>(); // discovered servers
    public static final Set<User> buddies = new HashSet<>(); // users for discovered addresses
    public static final Set<User> rooms = new HashSet<>(); // users for discovered servers
    public static final List<NetworkInterface> networkInterfaces = new ArrayList<>(); // network hardware list of device
    public static final Set<InterfaceAddress> hostAddresses = new HashSet<>(); // self ip addresses on each network hardware
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
        List<String> installed = Arrays.stream(UIManager.getInstalledLookAndFeels()).map(UIManager.LookAndFeelInfo::getName).collect(Collectors.toList());
        installed.add("Flat Intellij");
        installed.add("Flat Darcula");
        installed.add("Flat Light");
        installed.add("Flat Dark");
        return installed.toArray(new String[0]);
    }

    /**
     * Changes look and feel and updates the {@link MainUI}
     *
     * @param themeName Theme name to set
     */
    public static void changeTheme(String themeName) {
        try {
            switch (themeName) {
                case "Flat Intellij":
                    UIManager.setLookAndFeel(new FlatIntelliJLaf());
                    break;
                case "Flat Darcula":
                    UIManager.setLookAndFeel(new FlatDarculaLaf());
                    break;
                case "Flat Light":
                    UIManager.setLookAndFeel(new FlatLightLaf());
                    break;
                case "Flat Dark":
                    UIManager.setLookAndFeel(new FlatDarkLaf());
                    break;
            }
            for (UIManager.LookAndFeelInfo lookAndFeelInfo : UIManager.getInstalledLookAndFeels()) {
                if (lookAndFeelInfo.getName().equals(themeName)) {
                    UIManager.setLookAndFeel(lookAndFeelInfo.getClassName());
                }
            }
            SwingUtilities.updateComponentTreeUI(MainUI.getUI());
        } catch (Exception ignored) {
        }
    }
}
