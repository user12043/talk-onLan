package ogr.user12043.talkOnLan.model;

import ogr.user12043.talkOnLan.util.Constants;

import java.net.InetAddress;
import java.util.Date;
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
    private Date lastSeen;

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

    public Date getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Date lastSeen) {
        this.lastSeen = lastSeen;
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
        return Objects.hash(username, address);
    }

    public User cloneUser() {
        return new User(getUsername(), getAddress(), isRoom());
    }

    public boolean isOnline() {
        if (getLastSeen() == null) {
            return false;
        }
        return new Date().getTime() - getLastSeen().getTime() < Constants.RECEIVE_TIMEOUT + 3000;
    }

    @Override
    public String toString() {
        return getUsername();
    }
}
