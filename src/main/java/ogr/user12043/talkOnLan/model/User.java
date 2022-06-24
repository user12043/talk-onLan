package ogr.user12043.talkOnLan.model;

import java.net.InetAddress;
import java.util.Objects;

/**
 * Created by user12043 on 24.07.2018 - 11:55
 * part of project: talk-onLan
 * <p>
 * The model class of User
 */
public class User {
    private Integer id;
    private String username;
    private InetAddress address;
    private boolean isRoom;
    private boolean isOnline;
    private boolean isBlocked;

    public User() {
    }

    public User(String username, InetAddress address, boolean isRoom) {
        this.username = username;
        this.address = address;
        this.isRoom = isRoom;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
        return Objects.equals(getUsername(), user.getUsername()) &&
                Objects.equals(getAddress(), user.getAddress()) &&
                Objects.equals(isRoom(), user.isRoom());
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, address, isRoom);
    }

    public User cloneUser() {
        return new User(getUsername(), getAddress(), isRoom());
    }

    @Override
    public String toString() {
        return getUsername();
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }
}
