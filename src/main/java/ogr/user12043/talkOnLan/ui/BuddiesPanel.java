package ogr.user12043.talkOnLan.ui;

import ogr.user12043.talkOnLan.User;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by user12043 on 31.07.2018 - 12:10
 * part of project: talk-onLan
 */
public class BuddiesPanel extends javax.swing.JPanel {

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    private final Set<BuddyPanel> buddyPanels;
    private String title = "Buddies";

    /**
     * Creates new form BuddiesPanel
     */
    public BuddiesPanel() {
        initComponents();
        buddyPanels = new HashSet<>();
    }

    public BuddiesPanel(String title) {
        this.title = title;
        initComponents();
        buddyPanels = new HashSet<>();
    }

    /**
     * Add new panel for new user
     *
     * @param user new user to add
     */
    public void addBuddy(User user) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = buddyPanels.size();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0d;
        BuddyPanel buddyPanel = new BuddyPanel(user);
        buddyPanel.setBackground((buddyPanels.size() % 2 == 0) ? Color.WHITE : Color.LIGHT_GRAY);
        add(buddyPanel, constraints);
        buddyPanels.add(buddyPanel);
        revalidate();
    }

    /**
     * Overrided setEnabled() method to apply enabled state to included buddy panels
     *
     * @param enabled enabled state
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        // setEnabled child components also
        for (Component component : getComponents()) {
            component.setEnabled(enabled);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents

        setLayout(new java.awt.GridBagLayout());
    }//GEN-END:initComponents
}
