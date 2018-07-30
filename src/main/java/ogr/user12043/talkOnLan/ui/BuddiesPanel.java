package ogr.user12043.talkOnLan.ui;

import javax.swing.*;
import java.awt.*;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by user12043 on 30.07.2018 - 15:29
 * part of project: talk-onLan
 */
public class BuddiesPanel extends JPanel {
    private List<JButton> buddyButtons;

    BuddiesPanel() {
        initComponents();
        buddyButtons = new ArrayList<>();
    }

    public void addBuddy(InetAddress address) {
        JButton button = new JButton(address.toString());
        button.setText(address.toString());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0;
        constraints.gridy = buddyButtons.size();
        constraints.weightx = 1.0;
        add(button, constraints);
        buddyButtons.add(button);
        revalidate();
    }

    public void removeBuddy(InetAddress address) {
        for (ListIterator<JButton> iterator = buddyButtons.listIterator(); iterator.hasNext(); ) {
            JButton button = iterator.next();
            if (button.getText().equals(address.toString())) {
                remove(button);
                iterator.remove();
                revalidate();
            }
        }
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createLineBorder(Color.BLUE));

        revalidate();
    }
}
