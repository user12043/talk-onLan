package ogr.user12043.talkOnLan.util;

import ogr.user12043.talkOnLan.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by user12043 on 24.07.2018 - 11:56
 * part of project: talk-onLan
 */
public class Utils {
    public static final Set<InetAddress> buddyAddresses = new HashSet<>(); // discovered addresses
    public static final Set<User> buddies = new HashSet<>(); // users for discovered addresses
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
}
