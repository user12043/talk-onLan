package ogr.user12043.talkOnLan;

import ogr.user12043.talkOnLan.ui.MainPanel;
import ogr.user12043.talkOnLan.util.Properties;

import javax.swing.*;

/**
 * Created by user12043 on 23.07.2018 - 13:44
 * part of project: talk-onLan
 */
public class Main {
    public static MainPanel mainPanel;

    public static void main(String args[]) {
        try {
            Properties.username = "user1";
            /*MainPanel mainPanel = MainPanel.get();
            mainPanel.setVisible(true);*/
            SwingUtilities.invokeLater(() -> {
                mainPanel = new MainPanel();
                mainPanel.setVisible(true);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
