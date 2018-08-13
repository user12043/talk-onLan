package ogr.user12043.talkOnLan;

import ogr.user12043.talkOnLan.ui.MainUI;
import ogr.user12043.talkOnLan.util.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;

/**
 * Created by user12043 on 23.07.2018 - 13:44
 * part of project: talk-onLan
 */
public class Main {
    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    public static void main(String args[]) {
        try {
            System.setProperty("java.net.preferIPv4Stack", "true"); // Set prefer to ipv4 addresses in java
            Utils.initInterfaces();
            SwingUtilities.invokeLater(() -> MainUI.getUI().setVisible(true));
        } catch (Exception e) {
            LOGGER.error("Unexpected error happened!: ", e);
        }
    }
}
