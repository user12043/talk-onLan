package ogr.user12043.talkOnLan;

import java.net.InetAddress;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(getUserName(), user.getUserName()) &&
                Objects.equals(getAddress(), user.getAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, address);
    }
}
