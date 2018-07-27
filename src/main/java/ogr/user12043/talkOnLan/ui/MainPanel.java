package ogr.user12043.talkOnLan.ui;

import ogr.user12043.talkOnLan.net.NetworkService;
import ogr.user12043.talkOnLan.util.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.InetAddress;

/**
 * Created by user12043 on 25.07.2018 - 11:04
 * part of project: talk-onLan
 */
public class MainPanel extends JFrame {
//    private static MainPanel mainPanel = new MainPanel();

    private JButton button_startDiscovery;
    private JButton button_endDiscovery;

    public MainPanel() {
        initComponents();
    }

    public void refresh() {
//        mainPanel.removeAll();
        removeAll();
        /*for (User buddy : Utils.buddies) {
            mainPanel.add(new JLabel(buddy.getUserName() + " | " + buddy.getAddress()));
        }*/
        for (InetAddress address : Utils.buddyAddresses) {
            add(new JLabel(address.toString()));
        }

        pack();
    }

    public void addBuddy(InetAddress address) {
        add(new JLabel(address.toString()));
        pack();
    }

    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(300, 300);
        setPreferredSize(new Dimension(300, 300));
        setLayout(new FlowLayout());
        setAlwaysOnTop(true);

        button_startDiscovery = new JButton("Start discovery");
        button_startDiscovery.addActionListener(this::startDiscovery);
        add(button_startDiscovery);

        button_endDiscovery = new JButton("End discovery");
        button_endDiscovery.addActionListener(this::endDiscovery);
        add(button_endDiscovery);

        pack();
        revalidate();
    }

    private void startDiscovery(ActionEvent event) {
        try {
            NetworkService.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void endDiscovery(ActionEvent event) {
        NetworkService.end();
    }
}
