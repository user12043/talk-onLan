package ogr.user12043.talkOnLan.ui;

import ogr.user12043.talkOnLan.model.User;

import java.awt.event.WindowEvent;

/**
 * Created by user12043 on 14.08.2018 - 10:47
 * part of project: talk-onLan
 */
public class FileTransferDialog extends javax.swing.JDialog {

    private final boolean sending;
    private final User user;
    private final String fileName;
    private boolean cancel;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton_cancel;
    private javax.swing.JLabel jLabel_fileName;
    private javax.swing.JLabel jLabel_info;
    private javax.swing.JProgressBar jProgressBar_transferProgress;
    // End of variables declaration//GEN-END:variables

    /**
     * Creates new form FileTransferDialog
     */
    public FileTransferDialog(java.awt.Frame parent, User user, boolean sending, String fileName) {
        super(parent);
        this.sending = sending;
        this.user = user;
        this.fileName = fileName;
        cancel = false;
        setModal(false);
        setTitle(fileName);
        initComponents();
        setLocationRelativeTo(MainUI.getUI());
        setVisible(true);
    }

    public void startTransfer() {
        jProgressBar_transferProgress.setIndeterminate(false);
        jProgressBar_transferProgress.setValue(0);
        jLabel_info.setText(((sending) ? "Sending to " : "Receiving from ") + user.getUsername() + " on " + user.getAddress());
    }

    public void setProgress(int progress) {
        jProgressBar_transferProgress.setValue(progress);
        setTitle("%" + jProgressBar_transferProgress.getValue() + " - " + fileName);
        if (jProgressBar_transferProgress.getValue() == 100) {
            requestFocus();
            setTitle(fileName);
            jLabel_info.setText("Transfer Completed");
            jButton_cancel.setText("Close");
        }
    }

    public boolean cancelled() {
        return cancel;
    }

    /**
     * Make dialog non-closable to only dispose the dialog on clicking cancel button
     *
     * @param e window event
     */
    @Override
    protected void processWindowEvent(WindowEvent e) {
        if (e.getID() != WindowEvent.WINDOW_CLOSING) {
            super.processWindowEvent(e);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jProgressBar_transferProgress = new javax.swing.JProgressBar();
        jLabel_info = new javax.swing.JLabel();
        jLabel_fileName = new javax.swing.JLabel();
        jButton_cancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jProgressBar_transferProgress.setToolTipText("");
        jProgressBar_transferProgress.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        jProgressBar_transferProgress.setStringPainted(true);

        jLabel_info.setText(((sending) ? "Waiting for confirmation " : "Receiving from ") + user.getUsername() + " on " + user.getAddress());

        jLabel_fileName.setText(fileName);

        jButton_cancel.setText("Cancel");
        jButton_cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_cancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jProgressBar_transferProgress, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(jLabel_info)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jLabel_fileName))
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(jButton_cancel)
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel_info)
                                        .addComponent(jLabel_fileName))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jProgressBar_transferProgress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jButton_cancel)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton_cancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_cancelActionPerformed
        cancel = true;
        dispose();
    }//GEN-LAST:event_jButton_cancelActionPerformed
}
