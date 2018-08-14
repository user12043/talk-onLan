package ogr.user12043.talkOnLan.ui;

import ogr.user12043.talkOnLan.User;
import ogr.user12043.talkOnLan.net.DiscoveryService;
import ogr.user12043.talkOnLan.net.NetworkService;
import ogr.user12043.talkOnLan.util.Constants;
import ogr.user12043.talkOnLan.util.Utils;

import javax.swing.*;
import java.awt.*;
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

    private static MainUI mainUI; // To create one instance of MainUI
    private final Set<MessagePanel> messagePanels;
    private final JDialog loadingDialog;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public ogr.user12043.talkOnLan.ui.BuddiesPanel buddiesPanel;
    private javax.swing.JDialog fileTransferDialog;
    private javax.swing.JButton jButton_addManually;
    private javax.swing.JButton jButton_endDiscovery;
    private javax.swing.JButton jButton_hardDiscovery;
    private javax.swing.JButton jButton_hostAddresses;
    private javax.swing.JButton jButton_startDiscovery;
    private javax.swing.JLabel jLabel_info;
    private javax.swing.JProgressBar jProgressBar_transferProgress;
    private javax.swing.JScrollPane jScrollPane_buddiesPanel;
    // End of variables declaration//GEN-END:variables

    /**
     * Creates new form MainUI
     */
    private MainUI() {
        initComponents();
        setLocationRelativeTo(null);
        messagePanels = new HashSet<>();
        loadingDialog = createLoadingDialog();
        initializeGlassPane();
    }

    /**
     * Gives instance of class. This class can be accessed on this method only to avoid creating new instances in same time.
     * Applies "Singleton Pattern"
     *
     * @return instance
     * @see <a href="https://en.wikipedia.org/wiki/Singleton_pattern">More information about Singleton Pattern</a>
     */
    public static MainUI getUI() {
        if (mainUI == null) {
            mainUI = new MainUI();
        }
        return mainUI;
    }

    /**
     * Creates a dialog for display loading.
     *
     * @return crated dialog
     */
    private JDialog createLoadingDialog() {
        JDialog dialog = new JDialog(this, true);
        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        dialog.add(bar);
        dialog.setUndecorated(true); // hides title bar, must be called before "pack()"
        dialog.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
        dialog.pack();
        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));// should be called after "pack()"
        return dialog;
    }

    /**
     * Set frame's glass pane darken to darken the window on loading dialog shows
     */
    private void initializeGlassPane() {
        getRootPane().setGlassPane(new JComponent() {
            @Override
            public void paintComponent(Graphics g) {
                g.setColor(new Color(0, 0, 0, 100));
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        });
    }

    /**
     * Gives message panel for a user. Returns existing if created before, creates new one if not.
     *
     * @param user owner of panel
     * @return message panel of user
     */
    MessagePanel getMessagePanelOfUser(User user) {
        // Search for existing panel
        for (MessagePanel panel : messagePanels) {
            if (panel.getUser().equals(user)) {
                return panel;
            }
        }

        // Create new one if not exists
        MessagePanel messagePanel = new MessagePanel(this, user);
        messagePanels.add(messagePanel);
        return messagePanel;
    }

    /**
     * Receives message from remote user
     *
     * @param user    remote user
     * @param message received message content
     */
    public void receiveMessage(User user, String message) {
        final MessagePanel messagePanel = getMessagePanelOfUser(user);
        messagePanel.setVisible(true);
        messagePanel.receiveMessage(message);
    }

    /**
     * Asks user to confirm receiving a file from another user
     *
     * @param senderAddress address of sender
     * @param fileName      sending file's name
     * @param fileSize      sending file's size
     * @return user confirmation result
     */
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
        if (buddyExists) { // Process request if sender has discovered

            // Turn file size to readable string
            String fileSizeString = "";
            if (fileSize < 1024) {
                fileSizeString = (fileSize + " Bytes");
            } else if (fileSize < 1024 * 1024) {
                fileSizeString = (fileSize / 1024) + " KB";
            } else if (fileSize < 1024 * 1024 * 1024) {
                fileSizeString = (fileSize / 1024 / 1024) + " MB";
            }

            // Display dialog
            String message = user.getUserName() + " on " + user.getAddress() + " wants to send you this file:\n" + fileName + " (" + fileSizeString + ")\nAccept the file?";
            final int option = JOptionPane.showConfirmDialog(this, message, "Confirm file receive", JOptionPane.YES_NO_OPTION);
            return option == 0; // 0 = OK option
        }
        return false;
    }

    private void toggleLoading() {
        // Set dialog location to center
        int width = (getLocation().x + (getSize().width / 2) - (loadingDialog.getSize().width / 2));
        loadingDialog.setLocation(width, (getLocation().y + 65));
        SwingUtilities.invokeLater(() -> {
            if (loadingDialog.isShowing()) {
                loadingDialog.dispose(); // Hide loading dialog
                getRootPane().getGlassPane().setVisible(false); // hide dark glass pane
            } else {
                getRootPane().getGlassPane().setVisible(true); // show dark glass pane
                loadingDialog.setVisible(true); // Show loading dialog
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fileTransferDialog = new javax.swing.JDialog();
        jLabel_info = new javax.swing.JLabel();
        jProgressBar_transferProgress = new javax.swing.JProgressBar();
        jButton_startDiscovery = new javax.swing.JButton();
        jButton_endDiscovery = new javax.swing.JButton();
        jButton_addManually = new javax.swing.JButton();
        jButton_hostAddresses = new javax.swing.JButton();
        jButton_hardDiscovery = new javax.swing.JButton();
        jScrollPane_buddiesPanel = new javax.swing.JScrollPane();
        buddiesPanel = new ogr.user12043.talkOnLan.ui.BuddiesPanel();

        jLabel_info.setText("jLabel_info");

        javax.swing.GroupLayout fileTransferDialogLayout = new javax.swing.GroupLayout(fileTransferDialog.getContentPane());
        fileTransferDialog.getContentPane().setLayout(fileTransferDialogLayout);
        fileTransferDialogLayout.setHorizontalGroup(
                fileTransferDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(fileTransferDialogLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(fileTransferDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jProgressBar_transferProgress, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                                        .addGroup(fileTransferDialogLayout.createSequentialGroup()
                                                .addComponent(jLabel_info)
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap())
        );
        fileTransferDialogLayout.setVerticalGroup(
                fileTransferDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(fileTransferDialogLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel_info)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jProgressBar_transferProgress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(0, 250));
        setResizable(false);

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

        jScrollPane_buddiesPanel.setAutoscrolls(true);

        buddiesPanel.setEnabled(false);
        jScrollPane_buddiesPanel.setViewportView(buddiesPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jScrollPane_buddiesPanel)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(jButton_startDiscovery)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jButton_hardDiscovery)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jButton_addManually)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jButton_hostAddresses)
                                                .addGap(12, 12, 12)
                                                .addComponent(jButton_endDiscovery)
                                                .addGap(0, 0, Short.MAX_VALUE)))
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
                                .addGap(18, 18, 18)
                                .addComponent(jScrollPane_buddiesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 182, Short.MAX_VALUE)
                                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Starts network services for discovery
     *
     * @param evt action event
     */
    private void jButton_startDiscoveryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_startDiscoveryActionPerformed
        jButton_startDiscovery.setEnabled(false);
        toggleLoading();
        try {
            NetworkService.start();
            new Thread(() -> {
                try {
                    Thread.sleep(Constants.RECEIVE_TIMEOUT);
                } catch (InterruptedException ignored) {
                }
                jButton_hardDiscovery.setEnabled(true);
                jButton_addManually.setEnabled(true);
                jButton_hostAddresses.setEnabled(true);
                jButton_endDiscovery.setEnabled(true);
                buddiesPanel.setEnabled(true);
                toggleLoading();
            }).start();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Unable to start discovery. Check network connectivity", "ERROR", JOptionPane.ERROR_MESSAGE);
            jButton_endDiscovery.doClick();
        }
    }//GEN-LAST:event_jButton_startDiscoveryActionPerformed

    /**
     * Terminates network services
     *
     * @param evt action event
     */
    private void jButton_endDiscoveryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_endDiscoveryActionPerformed
        toggleLoading();
        NetworkService.end();
        jButton_hardDiscovery.setEnabled(false);
        jButton_addManually.setEnabled(false);
        jButton_hostAddresses.setEnabled(false);
        jButton_endDiscovery.setEnabled(false);
        new Thread(() -> {
            try {
                Thread.sleep(Constants.RECEIVE_TIMEOUT);
            } catch (InterruptedException ignored) {
            }
            jButton_startDiscovery.setEnabled(true);
            buddiesPanel.setEnabled(false);
            toggleLoading();
        }).start();
    }//GEN-LAST:event_jButton_endDiscoveryActionPerformed

    /**
     * Adds a user with ip address if can not be discovered
     *
     * @param evt action event
     */
    private void jButton_addManuallyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_addManuallyActionPerformed
        String input = JOptionPane.showInputDialog(this, "Enter ip address", "Direct IP", JOptionPane.QUESTION_MESSAGE);
        if (input != null && !input.isEmpty()) { // Check for empty input
            InetAddress address;
            try {
                address = InetAddress.getByName(input);
            } catch (UnknownHostException e) { // Display error message if input is not valid for ip address
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

    /**
     * Detects and shows the current users ip addresses on each connected network hardware
     *
     * @param evt action event
     */
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

    /**
     * calls {@link NetworkService#hardDiscovery()}
     *
     * @param evt action event
     */
    private void jButton_hardDiscoveryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_hardDiscoveryActionPerformed
        try {
            NetworkService.hardDiscovery();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error on discovery!", "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButton_hardDiscoveryActionPerformed
}
