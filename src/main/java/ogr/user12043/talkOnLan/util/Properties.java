package ogr.user12043.talkOnLan.util;

/**
 * Created by user12043 on 25.07.2018 - 10:28
 * part of project: talk-onLan
 */
public class Properties {
    // TODO Properties here will be read from a configuration file
    public static final String username = System.getProperty("user.name");
    public static final String fileReceiveFolder = "receivedFiles/";
    public static boolean roomMode = false;
    public static String databaseUrl = "jdbc:h2:./data";
    public static String databaseUsername = "talk-onLan";
    public static String databasePassword = "myAwesomePassword";
}
