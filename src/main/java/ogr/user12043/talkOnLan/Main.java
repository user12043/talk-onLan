package ogr.user12043.talkOnLan;

import ogr.user12043.talkOnLan.dao.DBConnection;
import ogr.user12043.talkOnLan.ui.MainUI;
import ogr.user12043.talkOnLan.util.Utils;

import javax.swing.*;
import java.util.logging.Logger;

/**
 * @deprecated Created by user12043 on 23.07.2018 - 13:44
 * part of project: talk-onLan
 */
public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        try {
            System.setProperty("java.net.preferIPv4Stack", "true"); // Set prefer to ipv4 addresses in java
            // some property for smooth text
            System.setProperty("swing.aatext", "true");
            System.setProperty("awt.useSystemAAFontSettings", "on");

            Utils.initDatabase();

            DBConnection.get(); // init database connection
            Utils.saveSelf();
            Utils.saveSelfRoom();

            if (!Utils.changeTheme("GTK+")) {
                Utils.changeTheme("Windows");
            }

            SwingUtilities.invokeLater(() -> MainUI.getUI().setVisible(true)); // display main ui
        } catch (Exception e) {
            LOGGER.severe("Unexpected error happened!: " + e);
        }
    }
}
