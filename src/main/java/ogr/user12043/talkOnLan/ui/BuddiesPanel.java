package ogr.user12043.talkOnLan.ui;

import ogr.user12043.talkOnLan.User;

import java.awt.*;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by user12043 on 31.07.2018 - 12:10
 * part of project: talk-onLan
 */
public class BuddiesPanel extends javax.swing.JPanel {

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    private Set<BuddyPanel> buddyPanels;

    /**
     * Creates new form BuddiesPanel
     */
    public BuddiesPanel() {
        initComponents();
        buddyPanels = new HashSet<>();
    }

    public void addBuddy(User user) {
        InetAddress address = user.getAddress();
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = buddyPanels.size();
        c.weightx = 0.5;
        BuddyPanel buddyPanel = new BuddyPanel(user);
        add(buddyPanel, c);
        buddyPanels.add(buddyPanel);
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

        setLayout(new java.awt.GridBagLayout());
    }// </editor-fold>//GEN-END:initComponents
}
