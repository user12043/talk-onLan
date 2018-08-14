package ogr.user12043.talkOnLan.ui;

import ogr.user12043.talkOnLan.User;
import ogr.user12043.talkOnLan.net.MessageService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;

/**
 * Created by user12043 on 31.07.2018 - 16:22
 * part of project: talk-onLan
 */
class MessagePanel extends javax.swing.JDialog {

    private final User user; // Remote user
    private int lineNumber;
    private boolean shiftPressed;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton_send;
    private javax.swing.JPanel jPanel_dialog;
    private javax.swing.JScrollPane jScrollPane_content;
    private javax.swing.JScrollPane jScrollPane_dialog;
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
        setTitle(user.getUserName() + " on " + user.getAddress());
    }

    User getUser() {
        return user;
    }

    /**
     * Sends message to buddy
     */
    private void sendMessage() {
        String sendingMessage = jTextArea_content.getText();
        if (sendingMessage.isEmpty()) { // Ignore if empty
            return;
        }
        try {
            MessageService.sendMessage(user.getAddress(), sendingMessage); // Send message
            addMessage(sendingMessage, true); // Add message box to panel
            jTextArea_content.setText("");
            jTextArea_content.grabFocus();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Cannot send the message!", "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    void receiveMessage(String message) {
        addMessage(message, false);
    }

    /**
     * Adds a {@link MessageBox} to panel for a message
     *
     * @param message message content
     * @param own     owning state
     */
    private void addMessage(String message, boolean own) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = lineNumber;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0d;
        constraints.insets = new Insets(0, 0, 20, 0);
        MessageBox messageBox = new MessageBox(user, message, own);
        jPanel_dialog.add(messageBox, constraints);
        lineNumber++;
        revalidate();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane_content = new javax.swing.JScrollPane();
        jTextArea_content = new javax.swing.JTextArea();
        jButton_send = new javax.swing.JButton();
        jScrollPane_dialog = new javax.swing.JScrollPane();
        jPanel_dialog = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jTextArea_content.setColumns(20);
        jTextArea_content.setRows(5);
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

        jScrollPane_dialog.setAutoscrolls(true);

        jPanel_dialog.setAutoscrolls(true);
        jPanel_dialog.setLayout(new java.awt.GridBagLayout());
        jScrollPane_dialog.setViewportView(jPanel_dialog);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jScrollPane_content, javax.swing.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                                .addComponent(jButton_send)
                                                .addGap(0, 0, Short.MAX_VALUE))
                                        .addComponent(jScrollPane_dialog, javax.swing.GroupLayout.Alignment.LEADING))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jScrollPane_dialog, javax.swing.GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE)
                                .addGap(18, 18, 18)
                                .addComponent(jScrollPane_content, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton_send)
                                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton_sendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_sendActionPerformed
        sendMessage();
    }//GEN-LAST:event_jButton_sendActionPerformed

    private void jTextArea_contentKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextArea_contentKeyPressed
        final int keyCode = evt.getKeyCode();
        if (keyCode == KeyEvent.VK_SHIFT) {
            shiftPressed = true;
        } else if (keyCode == KeyEvent.VK_ENTER) {
            if (!shiftPressed) {
                jButton_send.doClick();
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
