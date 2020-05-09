package ogr.user12043.talkOnLan.ui;

import ogr.user12043.talkOnLan.dao.DBConnection;
import ogr.user12043.talkOnLan.dao.MessageDao;
import ogr.user12043.talkOnLan.dao.UserDao;
import ogr.user12043.talkOnLan.model.Message;
import ogr.user12043.talkOnLan.model.User;
import ogr.user12043.talkOnLan.net.DiscoveryService;
import ogr.user12043.talkOnLan.net.MessageService;
import ogr.user12043.talkOnLan.net.NetworkService;
import ogr.user12043.talkOnLan.util.Constants;
import ogr.user12043.talkOnLan.util.Properties;
import ogr.user12043.talkOnLan.util.Utils;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by user12043 on 31.07.2018 - 12:05
 * part of project: talk-onLan
 */
public class MainUI extends javax.swing.JFrame {

    private static MainUI mainUI; // To create one instance of MainUI
    private final Set<MessagePanel> messagePanels;
    private final MessagePanel roomMessagePanel;
    private final JDialog loadingDialog;
    private final int discoveryStartEndWait = 1000;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private ogr.user12043.talkOnLan.ui.BuddiesPanel buddiesPanel;
    private javax.swing.JButton jButton_addManually;
    private javax.swing.JButton jButton_endDiscovery;
    private javax.swing.JButton jButton_hardDiscovery;
    private javax.swing.JButton jButton_hostAddresses;
    private javax.swing.JButton jButton_hostRoom;
    private javax.swing.JButton jButton_startDiscovery;
    private javax.swing.JButton jButton_stopRoom;
    private javax.swing.JComboBox<String> jComboBox_themes;
    private ogr.user12043.talkOnLan.ui.BuddiesPanel roomsPanel;
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
        roomMessagePanel = new MessagePanel(this, null);
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
     * @param message received message
     */
    public void receiveMessage(Message message) {
        MessagePanel messagePanel;
        if (message.getMessageType() == 0) {
            messagePanel = getMessagePanelOfUser(message.getSender());
        } else if (message.getMessageType() == 1) {
            messagePanel = roomMessagePanel;
        } else if (message.getMessageType() == 2) {
            User room = Utils.findRoom(message.getSender().getAddress());
            messagePanel = getMessagePanelOfUser(room);
        } else return;
        messagePanel.setVisible(true);
        messagePanel.receiveMessage(message);
        MessageDao.get().save(message);
    }

    /**
     * Asks user to confirm receiving a file from another user
     *
     * @param user     sender user
     * @param fileName sending file's name
     * @param fileSize sending file's size
     * @return user confirmation result
     */
    public boolean confirmFileReceive(User user, String fileName, long fileSize) {
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
        String message = user.getUsername() + " on " + user.getAddress() + " wants to send you this file:\n" + fileName + " (" + fileSizeString + ")\nAccept the file?";
        final int option = JOptionPane.showConfirmDialog(this, message, "Confirm file receive", JOptionPane.YES_NO_OPTION);
        return option == 0; // 0 = OK option
    }

    private void toggleLoading() {
        // Set dialog location to center
        int width = (getLocation().x + (getSize().width / 2) - (loadingDialog.getSize().width / 2));
        loadingDialog.setLocation(width, (getLocation().y + 75));
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

    public void addUser(User user) {
        // check database and add if not exists
        User existing = UserDao.get().findByFields(user);
        if (existing == null) {
            UserDao.get().save(user);
        }

        user = UserDao.get().findByFields(user);
        Utils.addUser(user);

        if (user.isRoom()) {
            roomsPanel.addBuddy(user);
        } else {
            buddiesPanel.addBuddy(user);
        }
        pack();

        // retrieve this user's messages and send unsent ones
        List<Message> messages = MessageDao.get().findUnsentByReceiver(user);
        for (Message message : messages) {
            MessageService.sendMessage(message);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents

        jButton_startDiscovery = new javax.swing.JButton();
        jButton_endDiscovery = new javax.swing.JButton();
        jButton_addManually = new javax.swing.JButton();
        jButton_hostAddresses = new javax.swing.JButton();
        jButton_hardDiscovery = new javax.swing.JButton();
        javax.swing.JScrollPane jScrollPane_buddiesPanel = new javax.swing.JScrollPane();
        buddiesPanel = new ogr.user12043.talkOnLan.ui.BuddiesPanel();
        javax.swing.JLabel jLabel_header = new javax.swing.JLabel();
        jComboBox_themes = new javax.swing.JComboBox<>();
        javax.swing.JLabel jLabel_theme = new javax.swing.JLabel();
        jButton_hostRoom = new javax.swing.JButton();
        javax.swing.JScrollPane jScrollPane_roomsPanel = new javax.swing.JScrollPane();
        roomsPanel = new ogr.user12043.talkOnLan.ui.BuddiesPanel();
        jButton_stopRoom = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("talk-onLan");
        setMinimumSize(new java.awt.Dimension(675, 250));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

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

        jScrollPane_buddiesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Buddies"));
        jScrollPane_buddiesPanel.setAutoscrolls(true);

        buddiesPanel.setEnabled(false);
        buddiesPanel.setLayout(new java.awt.GridBagLayout());
        jScrollPane_buddiesPanel.setViewportView(buddiesPanel);

        jLabel_header.setFont(new java.awt.Font("Dialog", 1, 24)); // NOI18N
        jLabel_header.setText("talk-onLan");

        jComboBox_themes.setModel(new DefaultComboBoxModel<String>
                (Utils.getLookAndFeels()));
        jComboBox_themes.setSelectedItem(Utils.getCurrentTheme());
        jComboBox_themes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox_themesActionPerformed(evt);
            }
        });

        jLabel_theme.setText("Theme: ");

        jButton_hostRoom.setText("Host a room");
        jButton_hostRoom.setEnabled(false);
        jButton_hostRoom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_hostRoomActionPerformed(evt);
            }
        });

        jScrollPane_roomsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Rooms"));
        jScrollPane_roomsPanel.setAutoscrolls(true);

        roomsPanel.setLayout(new java.awt.GridBagLayout());
        jScrollPane_roomsPanel.setViewportView(roomsPanel);

        jButton_stopRoom.setText("Stop room");
        jButton_stopRoom.setEnabled(false);
        jButton_stopRoom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_stopRoomActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(jLabel_header)
                                                .addGap(18, 18, Short.MAX_VALUE)
                                                .addComponent(jLabel_theme)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jComboBox_themes, javax.swing.GroupLayout.PREFERRED_SIZE, 342, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                                                .addContainerGap()
                                                                .addComponent(jScrollPane_buddiesPanel))
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addGap(12, 12, 12)
                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                                        .addComponent(jButton_startDiscovery, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                        .addComponent(jButton_hostRoom, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addGroup(layout.createSequentialGroup()
                                                                                .addComponent(jButton_hardDiscovery)
                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                .addComponent(jButton_addManually))
                                                                        .addComponent(jButton_stopRoom, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addGap(0, 8, Short.MAX_VALUE)
                                                                .addComponent(jButton_hostAddresses)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(jButton_endDiscovery, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addComponent(jScrollPane_roomsPanel))))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel_header)
                                        .addComponent(jComboBox_themes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel_theme))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jButton_startDiscovery)
                                        .addComponent(jButton_addManually)
                                        .addComponent(jButton_hostAddresses)
                                        .addComponent(jButton_hardDiscovery)
                                        .addComponent(jButton_endDiscovery))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jButton_hostRoom)
                                        .addComponent(jButton_stopRoom))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jScrollPane_roomsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE)
                                        .addComponent(jScrollPane_buddiesPanel))
                                .addContainerGap())
        );

        pack();
    }//GEN-END:initComponents

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
                    Thread.sleep(discoveryStartEndWait);
                } catch (InterruptedException ignored) {
                }
                jButton_hardDiscovery.setEnabled(true);
                jButton_addManually.setEnabled(true);
                jButton_hostAddresses.setEnabled(true);
                jButton_endDiscovery.setEnabled(true);
                jButton_hostRoom.setEnabled(true);
                buddiesPanel.setEnabled(true);
                toggleLoading();
            }).start();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Unable to start discovery. Check network connectivity", "ERROR", JOptionPane.ERROR_MESSAGE);
            jButton_endDiscovery.doClick();
            toggleLoading();
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
        jButton_hostRoom.setEnabled(false);
        messagePanels.forEach(Window::dispose);
        new Thread(() -> {
            try {
                Thread.sleep(discoveryStartEndWait);
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
     * calls {@link DiscoveryService#hardDiscovery()}
     *
     * @param evt action event
     */
    private void jButton_hardDiscoveryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_hardDiscoveryActionPerformed
        try {
            DiscoveryService.hardDiscovery();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error on discovery!", "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButton_hardDiscoveryActionPerformed

    private void jComboBox_themesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox_themesActionPerformed
        Utils.changeTheme((String) jComboBox_themes.getSelectedItem());
        pack();
    }//GEN-LAST:event_jComboBox_themesActionPerformed

    private void jButton_hostRoomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_hostRoomActionPerformed
        try {
            Properties.roomMode = true;
            User room = new User();
            room.setUsername(Properties.username);
            room.setAddress(InetAddress.getLocalHost());
            room.setRoom(true);
            Utils.rooms.add(room);
            roomMessagePanel.receiveMessage(new Message(null, null, "You just started a room!", new Date(), Constants.MSG_TYPE_ROOM));
            roomMessagePanel.setVisible(true);
            jButton_hostRoom.setEnabled(false);
            jButton_stopRoom.setEnabled(true);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error occurred while making room!", "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButton_hostRoomActionPerformed

    private void jButton_stopRoomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_stopRoomActionPerformed
        Properties.roomMode = false;
        jButton_hostRoom.setEnabled(true);
    }//GEN-LAST:event_jButton_stopRoomActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        try {
            DBConnection.get().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_formWindowClosing
}
