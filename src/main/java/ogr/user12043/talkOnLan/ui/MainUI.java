/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ogr.user12043.talkOnLan.ui;

import ogr.user12043.talkOnLan.net.NetworkService;

/**
 * Created by user12043 on 31.07.2018 - 12:05
 * part of project: talk-onLan
 */
public class MainUI extends javax.swing.JFrame {

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public ogr.user12043.talkOnLan.ui.BuddiesPanel buddiesPanel;
    private javax.swing.JButton jButton_endDiscovery;
    private javax.swing.JButton jButton_startDiscovery;
    // End of variables declaration//GEN-END:variables

    /**
     * Creates new form MainUI
     */
    public MainUI() {
        initComponents();
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

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jButton_startDiscovery.setText("Start Discovery");
        jButton_startDiscovery.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_startDiscoveryActionPerformed(evt);
            }
        });

        jButton_endDiscovery.setText("End Discovery");
        jButton_endDiscovery.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_endDiscoveryActionPerformed(evt);
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
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 181, Short.MAX_VALUE)
                                                .addComponent(jButton_endDiscovery)))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jButton_endDiscovery)
                                        .addComponent(jButton_startDiscovery))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(buddiesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton_startDiscoveryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_startDiscoveryActionPerformed
        try {
            NetworkService.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_jButton_startDiscoveryActionPerformed

    private void jButton_endDiscoveryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_endDiscoveryActionPerformed
        NetworkService.end();
    }//GEN-LAST:event_jButton_endDiscoveryActionPerformed
}
