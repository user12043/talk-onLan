package ogr.user12043.talkOnLan.ui;

import ogr.user12043.talkOnLan.dao.MessageDao;
import ogr.user12043.talkOnLan.model.Message;
import ogr.user12043.talkOnLan.model.User;
import ogr.user12043.talkOnLan.net.MessageService;
import ogr.user12043.talkOnLan.util.Constants;
import ogr.user12043.talkOnLan.util.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by user12043 on 31.07.2018 - 16:22
 * part of project: talk-onLan
 */
class MessagePanel extends javax.swing.JDialog {
    private static final Logger LOGGER = LogManager.getLogger();
    private final User user; // Remote user
    private final Set<User> participants;
    private int lineNumber;
    private boolean shiftPressed;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton_send;
    private javax.swing.JPanel jPanel_dialogue;
    private javax.swing.JProgressBar jProgressBar_sending;
    private javax.swing.JScrollPane jScrollPane_dialogue;
    private javax.swing.JTextArea jTextArea_content;
    // End of variables declaration//GEN-END:variables

    /**
     * Creates new form MessagePanel
     *
     * @param parent parent frame
     * @param user   remote user
     */
    MessagePanel(Frame parent, User user) {
        super(parent, false);
        initComponents();
        lineNumber = 0;
        this.user = user;
        participants = new HashSet<>();
        setTitle((user.isRoom() ? "Room of " + user.getUsername() : user.getUsername()) + " on " + user.getAddress());
        jProgressBar_sending.setVisible(false);
        jTextArea_content.requestFocusInWindow();
        fetchMessages();
    }

    private void fetchMessages() {
        List<Message> messages;
        if (user.equals(Utils.selfRoom())) {
            messages = MessageDao.get().findSelfRoomConversation();
        } else if (user.isRoom()) {
            messages = MessageDao.get().findRoomConversation(user);
        } else {
            messages = MessageDao.get().findConversation(Utils.self(), user);
        }
        for (Message message : messages) {
            addMessage(message, message.getSender().equals(Utils.self()));
        }
    }

    User getUser() {
        return user;
    }

    /**
     * Sends message to buddy
     */
    private void sendMessage(final String content) {
        Message sendingMessage = new Message();
        sendingMessage.setContent(content);
        sendingMessage.setSentDate(new Date());
        sendingMessage.setSender(Utils.self());
        try {
            if (!user.equals(Utils.selfRoom())) {
                // Direct or connected room
                sendingMessage.setMessageType(user.isRoom() ? Constants.MSG_TYPE_ROOM : Constants.MSG_TYPE_DIRECT);
                sendingMessage.setReceiver(user);
                MessageService.sendMessage(sendingMessage);
            } else {
                // hosting a room
                sendingMessage.setMessageType(Constants.MSG_TYPE_FWD);
                // send a clone for self
                Message selfMessage = sendingMessage.cloneMessage();
                selfMessage.setReceiver(Utils.selfRoom());
                selfMessage.setMessageType(Constants.MSG_TYPE_ROOM);
                MessageService.sendMessage(selfMessage);
                // send to participants
                for (User participant : participants) {
                    Message forwardedMessage = sendingMessage.cloneMessage();
                    forwardedMessage.setReceiver(participant);
                    MessageService.sendMessage(forwardedMessage);
                }
            }
            SwingUtilities.invokeLater(() -> {
                addMessage(sendingMessage, true); // Add message box to panel
                jTextArea_content.setText("");
                jTextArea_content.requestFocusInWindow();
                setInputEnabled(true);
            });
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Cannot send the message!", "ERROR", JOptionPane.ERROR_MESSAGE);
            LOGGER.error("Cannot send the message!", e);
            SwingUtilities.invokeLater(() -> {
                jTextArea_content.setText(sendingMessage.getContent());
                setInputEnabled(true);
            });
        }
    }

    void receiveMessage(Message message) {
        addMessage(message, false);
        if (message.getSender() != null) {
            participants.add(message.getSender());
            if (message.getMessageType() == Constants.MSG_TYPE_ROOM) {
                // forward message to all clients
                message.setMessageType(Constants.MSG_TYPE_FWD);
                message.setForwardedFrom(message.getSender());
                for (User participant : participants) {
                    // except sender
                    if (!participant.equals(message.getSender())) {
                        Message clone = message.cloneMessage();
                        clone.setReceiver(participant);
                        MessageService.sendMessage(clone);
                    }
                }
            }
        }
    }

    /**
     * Adds a {@link MessageBox} to panel for a message
     *
     * @param message message content
     * @param own     owning state
     */
    private void addMessage(Message message, boolean own) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = lineNumber;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0d;
        constraints.insets = new Insets(0, 0, 20, 0);
        MessageBox messageBox = new MessageBox(message, own);
        jPanel_dialogue.add(messageBox, constraints);
        lineNumber++;
        revalidate();
    }

    private void setInputEnabled(boolean enabled) {
        jButton_send.setEnabled(enabled);
        jTextArea_content.setEnabled(enabled);
        jProgressBar_sending.setVisible(!enabled);
    }

    public void addParticipant(User user) {
        participants.add(user);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents

        javax.swing.JScrollPane jScrollPane_content = new javax.swing.JScrollPane();
        jTextArea_content = new javax.swing.JTextArea();
        jButton_send = new javax.swing.JButton();
        jScrollPane_dialogue = new javax.swing.JScrollPane();
        jPanel_dialogue = new javax.swing.JPanel();
        jProgressBar_sending = new javax.swing.JProgressBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setLocationByPlatform(true);
        setMinimumSize(new java.awt.Dimension(300, 450));

        jTextArea_content.setLineWrap(true);
        jTextArea_content.setRows(5);
        jTextArea_content.setWrapStyleWord(true);
        jTextArea_content.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextArea_contentKeyPressed(evt);
            }

            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextArea_contentKeyReleased(evt);
            }
        });
        jScrollPane_content.setViewportView(jTextArea_content);

        jButton_send.setText("Send");
        jButton_send.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_sendActionPerformed(evt);
            }
        });

        jScrollPane_dialogue.setBorder(null);
        jScrollPane_dialogue.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane_dialogue.setAutoscrolls(true);

        jPanel_dialogue.setAutoscrolls(true);
        jPanel_dialogue.setLayout(new java.awt.GridBagLayout());
        jScrollPane_dialogue.setViewportView(jPanel_dialogue);

        jProgressBar_sending.setIndeterminate(true);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(jButton_send)
                                                .addGap(185, 185, 185)
                                                .addComponent(jProgressBar_sending, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(0, 0, Short.MAX_VALUE))
                                        .addComponent(jScrollPane_content)
                                        .addComponent(jScrollPane_dialogue))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jScrollPane_dialogue, javax.swing.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jScrollPane_content, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jButton_send)
                                        .addComponent(jProgressBar_sending, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
        );

        pack();
    }//GEN-END:initComponents

    private void jButton_sendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_sendActionPerformed
        final String sendingMessage = jTextArea_content.getText().trim();
        if (sendingMessage.isEmpty()) { // Ignore if empty
            return;
        }
        setInputEnabled(false);
        new Thread(() -> sendMessage(sendingMessage)).start();
    }//GEN-LAST:event_jButton_sendActionPerformed

    private void jTextArea_contentKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextArea_contentKeyPressed
        final int keyCode = evt.getKeyCode();
        if (keyCode == KeyEvent.VK_SHIFT) {
            shiftPressed = true;
        } else if (keyCode == KeyEvent.VK_ENTER) {
            if (!shiftPressed) {
                jButton_sendActionPerformed(null);
            } else {
                jTextArea_content.append("\n");
            }
        }
    }//GEN-LAST:event_jTextArea_contentKeyPressed

    private void jTextArea_contentKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextArea_contentKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (!shiftPressed) {
                jTextArea_content.setText("");
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_SHIFT) {
            shiftPressed = false;
        }
    }//GEN-LAST:event_jTextArea_contentKeyReleased
}
