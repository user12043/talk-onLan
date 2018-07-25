package ogr.user12043.talkOnLan.ui;

import ogr.user12043.talkOnLan.User;
import ogr.user12043.talkOnLan.util.Utils;

import javax.swing.*;
import java.awt.*;

/**
 * Created by user12043 on 25.07.2018 - 11:04
 * part of project: talk-onLan
 */
public class MainPanel extends JFrame {
    private static MainPanel mainPanel = new MainPanel();

    private MainPanel() {
        initComponents();
        setVisible(true);
    }

    public static MainPanel get() {
        return mainPanel;
    }

    public static void refresh() {
        mainPanel.removeAll();
        for (User buddy : Utils.buddies) {
            addComponent(new JLabel(buddy.getUserName() + " | " + buddy.getAddress()));
        }
    }

    public static void addComponent(Component component) {
        mainPanel.add(component);
    }

    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(300, 300);
        pack();
    }
}
