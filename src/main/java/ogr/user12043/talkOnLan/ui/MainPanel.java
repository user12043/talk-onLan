package ogr.user12043.talkOnLan.ui;

import ogr.user12043.talkOnLan.net.NetworkService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Created by user12043 on 25.07.2018 - 11:04
 * part of project: talk-onLan
 */
public class MainPanel extends JFrame {
    private JButton button_startDiscovery;
    private JButton button_endDiscovery;
    public BuddiesPanel buddiesPanel;

    public MainPanel() {
        initComponents();
    }

    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(600, 600);
        setPreferredSize(new Dimension(600, 600));
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.weightx = 0.5;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        button_startDiscovery = new JButton("Start discovery");
        button_startDiscovery.addActionListener(this::startDiscovery);
        add(button_startDiscovery, constraints);

        constraints.gridx = 1;
        constraints.gridy = 0;
        button_endDiscovery = new JButton("End discovery");
        button_endDiscovery.addActionListener(this::endDiscovery);
        add(button_endDiscovery, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        buddiesPanel = new BuddiesPanel();
        add(buddiesPanel);

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
