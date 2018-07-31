package ogr.user12043.talkOnLan.util;

import ogr.user12043.talkOnLan.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

/**
 * Created by user12043 on 24.07.2018 - 11:56
 * part of project: talk-onLan
 */
public class Utils {
    private static final Logger LOGGER = LogManager.getLogger(Constants.class);

    public static Set<InetAddress> buddyAddresses = new HashSet<>();
    public static Set<User> buddies = new HashSet<>();

    public static List<NetworkInterface> networkInterfaces = new ArrayList<>();
    public static Set<InterfaceAddress> hostAddresses = new HashSet<>();

    public static void initInterfaces() {
        try {
            final Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
            while (enumeration.hasMoreElements()) {
                NetworkInterface networkInterface = enumeration.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue; // skip loopback or disconnected interface
                }
                networkInterfaces.add(networkInterface);
                hostAddresses.addAll(networkInterface.getInterfaceAddresses());
            }
        } catch (SocketException e) {
            LOGGER.error("Error on getting network devices info");
        }
    }
}
