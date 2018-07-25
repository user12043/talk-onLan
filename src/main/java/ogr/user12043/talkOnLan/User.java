package ogr.user12043.talkOnLan;

import java.net.InetAddress;

/**
 * Created by user12043 on 24.07.2018 - 11:55
 * part of project: talk-onLan
 */
public class User {
    private String userName;
    private InetAddress address;

    public User() {
    }

    public User(String userName, InetAddress address) {
        this.userName = userName;
        this.address = address;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }
}
