package ogr.user12043.talkOnLan.ui;

import ogr.user12043.talkOnLan.User;
import ogr.user12043.talkOnLan.net.FileTransferService;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

/**
 * Created by user12043 on 31.07.2018 - 12:11
 * part of project: talk-onLan
 */
class BuddyPanel extends javax.swing.JPanel {

    private final User user;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton_file;
    private javax.swing.JButton jButton_message;
    private javax.swing.JLabel jLabel_address;
    private javax.swing.JLabel jLabel_name;
    // End of variables declaration//GEN-END:variables

    /**
     * Creates new form Buddy
     */
    BuddyPanel(User user) {
        initComponents();
        this.user = user;
        jLabel_name.setText(user.getUserName());
        jLabel_address.setText(" on " + user.getAddress().toString().replace("/", ""));
        if (user.isRoom()) {
            jButton_message.setEnabled(false);
            jButton_message.setVisible(false);
        }
    }

    /**
     * Overrided setEnabled() method to apply enabled state to action buttons in this panel
     *
     * @param enabled enabled state
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        jButton_file.setEnabled(enabled);
        jButton_message.setEnabled(enabled);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel_name = new javax.swing.JLabel();
        jLabel_address = new javax.swing.JLabel();
        jButton_message = new javax.swing.JButton();
        jButton_file = new javax.swing.JButton();

        jLabel_name.setFont(jLabel_name.getFont().deriveFont(jLabel_name.getFont().getSize() + 1f));
        jLabel_name.setText("name");

        jLabel_address.setFont(jLabel_address.getFont().deriveFont(jLabel_address.getFont().getSize() + 1f));
        jLabel_address.setText("address");

        jButton_message.setText("Message");
        jButton_message.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_messageActionPerformed(evt);
            }
        });

        jButton_file.setText("File");
        jButton_file.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_fileActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addComponent(jLabel_name)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel_address)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton_message)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton_file)
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel_name)
                                        .addComponent(jLabel_address)
                                        .addComponent(jButton_message)
                                        .addComponent(jButton_file))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Opens messaging dialog
     *
     * @param evt action event
     */
    private void jButton_messageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_messageActionPerformed
        MainUI.getUI().getMessagePanelOfUser(user).setVisible(true);
    }//GEN-LAST:event_jButton_messageActionPerformed

    /**
     * Opens file select dialog to send
     *
     * @param evt action event
     */
    private void jButton_fileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_fileActionPerformed
        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
        final int state = chooser.showOpenDialog(this);
        if (state != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        new Thread(() -> {
            try {
                FileTransferService.sendFile(user, file);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, ("Unable to send file:\n" + e), "ERROR", JOptionPane.ERROR_MESSAGE);
            } catch (SecurityException e) {
                JOptionPane.showMessageDialog(this, ("File did not send. Target user rejected send request"), "WARNING", JOptionPane.WARNING_MESSAGE);
            }
        }).start();
    }//GEN-LAST:event_jButton_fileActionPerformed
}
