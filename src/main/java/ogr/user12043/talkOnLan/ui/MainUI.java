package ogr.user12043.talkOnLan.ui;

import ogr.user12043.talkOnLan.User;
import ogr.user12043.talkOnLan.net.DiscoveryService;
import ogr.user12043.talkOnLan.net.NetworkService;
import ogr.user12043.talkOnLan.util.Utils;

import javax.swing.*;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by user12043 on 31.07.2018 - 12:05
 * part of project: talk-onLan
 */
public class MainUI extends javax.swing.JFrame {

    private final Set<MessagePanel> messagePanels;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public ogr.user12043.talkOnLan.ui.BuddiesPanel buddiesPanel;
    private javax.swing.JButton jButton_addManually;
    private javax.swing.JButton jButton_endDiscovery;
    private javax.swing.JButton jButton_hardDiscovery;
    private javax.swing.JButton jButton_hostAddresses;
    private javax.swing.JButton jButton_startDiscovery;
    // End of variables declaration//GEN-END:variables

    /**
     * Creates new form MainUI
     */
    public MainUI() {
        initComponents();
        messagePanels = new HashSet<>();
    }

    public MessagePanel getMessagePanelOfUser(User user) {
        for (MessagePanel panel : messagePanels) {
            if (panel.getUser().equals(user)) {
                return panel;
            }
        }
        MessagePanel messagePanel = new MessagePanel(this, user);
        messagePanels.add(messagePanel);
        return messagePanel;
    }

    public void receiveMessage(User user, String message) {
        final boolean panelExists = messagePanels.stream().anyMatch(messagePanel -> {
            if (messagePanel.getUser().equals(user)) {
                messagePanel.receiveMessage(message);
                messagePanel.setVisible(true);
                return true;
            }
            return false;
        });
        if (!panelExists) {
            final MessagePanel messagePanel = getMessagePanelOfUser(user);
            messagePanel.setVisible(true);
            messagePanel.receiveMessage(message);
        }
    }

    public boolean confirmFileReceive(InetAddress senderAddress, String fileName, long fileSize) {
        final User user = new User();
        final boolean buddyExists = Utils.buddies.stream().anyMatch(u -> {
            if (u.getAddress().equals(senderAddress)) {
                user.setUserName(u.getUserName());
                user.setAddress(u.getAddress());
                return true;
            }
            return false;
        });
        if (buddyExists) {
            String fileSizeString = "";
            if (fileSize < 1024) {
                fileSizeString = (fileSize + " Bytes");
            } else if (fileSize < 1024 * 1024) {
                fileSizeString = (fileSize / 1024) + " KB";
            } else if (fileSize < 1024 * 1024 * 1024) {
                fileSizeString = (fileSize / 1024 / 1024) + " MB";
            }

            String message = user.getUserName() + " on " + user.getAddress() + " wants to send you this file:\n" + fileName + " (" + fileSizeString + ")\nAccept the file?";
            final int option = JOptionPane.showConfirmDialog(this, message, "Confirm file receive", JOptionPane.YES_NO_OPTION);
            return option == 0;
        }
        return false;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton_startDiscovery = new javax.swing.JButton();
        jButton_endDiscovery = new javax.swing.JButton();
        buddiesPanel = new ogr.user12043.talkOnLan.ui.BuddiesPanel();
        jButton_addManually = new javax.swing.JButton();
        jButton_hostAddresses = new javax.swing.JButton();
        jButton_hardDiscovery = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jButton_startDiscovery.setText("Start Discovery");
        jButton_startDiscovery.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_startDiscoveryActionPerformed(evt);
            }
        });

        jButton_endDiscovery.setText("End Discovery");
        jButton_endDiscovery.setEnabled(false);
        jButton_endDiscovery.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_endDiscoveryActionPerformed(evt);
            }
        });

        jButton_addManually.setText("Add Manually");
        jButton_addManually.setEnabled(false);
        jButton_addManually.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_addManuallyActionPerformed(evt);
            }
        });

        jButton_hostAddresses.setText("My Address(es)");
        jButton_hostAddresses.setEnabled(false);
        jButton_hostAddresses.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_hostAddressesActionPerformed(evt);
            }
        });

        jButton_hardDiscovery.setText("Hard Discovery");
        jButton_hardDiscovery.setEnabled(false);
        jButton_hardDiscovery.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_hardDiscoveryActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(buddiesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(jButton_startDiscovery)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jButton_hardDiscovery)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jButton_addManually)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jButton_hostAddresses)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jButton_endDiscovery)
                                                .addGap(0, 4, Short.MAX_VALUE)))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jButton_endDiscovery)
                                        .addComponent(jButton_startDiscovery)
                                        .addComponent(jButton_addManually)
                                        .addComponent(jButton_hostAddresses)
                                        .addComponent(jButton_hardDiscovery))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(buddiesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton_startDiscoveryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_startDiscoveryActionPerformed
        try {
            NetworkService.start();
            jButton_startDiscovery.setEnabled(false);
            jButton_hardDiscovery.setEnabled(true);
            jButton_addManually.setEnabled(true);
            jButton_hostAddresses.setEnabled(true);
            jButton_endDiscovery.setEnabled(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Unable to start discovery. Check network connectivity", "ERROR", JOptionPane.ERROR_MESSAGE);
            jButton_endDiscovery.doClick();
        }
    }//GEN-LAST:event_jButton_startDiscoveryActionPerformed

    private void jButton_endDiscoveryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_endDiscoveryActionPerformed
        NetworkService.end();
        jButton_startDiscovery.setEnabled(true);
        jButton_hardDiscovery.setEnabled(false);
        jButton_addManually.setEnabled(false);
        jButton_hostAddresses.setEnabled(false);
        jButton_endDiscovery.setEnabled(false);
    }//GEN-LAST:event_jButton_endDiscoveryActionPerformed

    private void jButton_addManuallyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_addManuallyActionPerformed
        String input = JOptionPane.showInputDialog(this, "Enter ip address", "Direct IP", JOptionPane.QUESTION_MESSAGE);
        if (input != null && !input.isEmpty()) {
            InetAddress address;
            try {
                address = InetAddress.getByName(input);
            } catch (UnknownHostException e) {
                JOptionPane.showMessageDialog(this, "Invalid input!", "ERROR", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                DiscoveryService.sendDiscoveryRequest(address);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error on discovery!", "ERROR", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_jButton_addManuallyActionPerformed

    private void jButton_hostAddressesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_hostAddressesActionPerformed
        StringBuilder builder = new StringBuilder();
        for (InterfaceAddress hostAddress : Utils.hostAddresses) {
            if (hostAddress.getAddress() instanceof Inet4Address) {
                final String address = hostAddress.getAddress().toString();
                builder.append(address.substring(1)).append("\n");
            }
        }
        JOptionPane.showMessageDialog(this, builder.toString(), "Local IP addresses", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_jButton_hostAddressesActionPerformed

    private void jButton_hardDiscoveryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_hardDiscoveryActionPerformed
        try {
            NetworkService.hardDiscovery();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error on discovery!", "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButton_hardDiscoveryActionPerformed
}
