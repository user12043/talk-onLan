package ogr.user12043.talkOnLan;

import java.net.InetAddress;
import java.util.Objects;

/**
 * Created by user12043 on 24.07.2018 - 11:55
 * part of project: talk-onLan
 * <p>
 * The model class of User
 */
public class User {
    private String userName;
    private InetAddress address;
    private boolean isRoom;

    public User() {
    }

    public User(String userName, InetAddress address, boolean isRoom) {
        this.userName = userName;
        this.address = address;
        this.isRoom = isRoom;
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

    public boolean isRoom() {
        return isRoom;
    }

    public void setRoom(boolean room) {
        isRoom = room;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(getUserName(), user.getUserName()) &&
                Objects.equals(getAddress(), user.getAddress()) &&
                Objects.equals(isRoom(), user.isRoom());
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, address);
    }
}
