package ogr.user12043.talkOnLan.ui;

import ogr.user12043.talkOnLan.User;
import ogr.user12043.talkOnLan.net.MessageService;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Created by user12043 on 31.07.2018 - 16:22 part of project: talk-onLan
 */
class MessagePanel extends javax.swing.JDialog {

    private User user;
    private StringBuilder receivingMessage;
    private int lineNumber;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton_send;
    private javax.swing.JPanel jPanel_dialog;
    private javax.swing.JScrollPane jScrollPane_content;
    private javax.swing.JScrollPane jScrollPane_dialog;
    private javax.swing.JTextArea jTextArea_content;
    // End of variables declaration//GEN-END:variables

    /**
     * Creates new form MessagePanel
     */
    MessagePanel(java.awt.Frame parent, boolean modal, User user) {
        super(parent, modal);
        initComponents();
        receivingMessage = new StringBuilder();
        lineNumber = 0;
        this.user = user;
        setTitle(user.getUserName() + " on " + user.getAddress());
    }

    User getUser() {
        return user;
    }

    private void sendMessage() {
        String sendingMessage = jTextArea_content.getText();
        try {
            MessageService.sendMessage(user.getAddress(), sendingMessage);
            addMessage(jTextArea_content.getText(), true);
            jTextArea_content.setText("");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Cannot send the message!", "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    void receiveMessage(String message) {
        addMessage(message, false);
    }

    private void addMessage(String message, boolean own) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = ((own) ? GridBagConstraints.LAST_LINE_END : GridBagConstraints.LAST_LINE_START);
        constraints.gridy = lineNumber;
        constraints.weightx = 0.5;
        constraints.gridx = ((own) ? 1 : 0);
        constraints.fill = GridBagConstraints.HORIZONTAL;
//        JTextArea textArea = new JTextArea(message);
//        textArea.setLineWrap(true);
//        textArea.setWrapStyleWord(true);
//        textArea.setRows(0);
//        textArea.setColumns(80);
        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/html");
        textPane.setText("<p style=\"text-align: " + ((own) ? ("right") : ("left")) + "\">" + message + "</p>");
        textPane.setEditable(false);
        lineNumber++;
//        jPanel_dialog.add(textArea, constraints);
        jPanel_dialog.add(textPane, constraints);
        pack();
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
        jScrollPane_content.setViewportView(jTextArea_content);

        jButton_send.setText("Send");
        jButton_send.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_sendActionPerformed(evt);
            }
        });

        jScrollPane_dialog.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane_dialog.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane_dialog.setAutoscrolls(true);

        jPanel_dialog.setLayout(new java.awt.GridBagLayout());
        jScrollPane_dialog.setViewportView(jPanel_dialog);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jScrollPane_content, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
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
                                .addComponent(jScrollPane_dialog, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
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
}
