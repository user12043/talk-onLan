package ogr.user12043.talkOnLan;

import java.util.Date;

/**
 * Created by user12043 on 7.05.2020 - 20:43
 * part of project: talk-onLan
 */
public class Message {
    private User sender;
    private String content;
    private Date sentDate;
    private boolean roomMessage;

    public Message() {
    }

    public Message(User sender, String content, Date sentDate, boolean roomMessage) {
        this.sender = sender;
        this.content = content;
        this.sentDate = sentDate;
        this.roomMessage = roomMessage;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getSentDate() {
        return sentDate;
    }

    public void setSentDate(Date sentDate) {
        this.sentDate = sentDate;
    }

    public boolean isRoomMessage() {
        return roomMessage;
    }

    public void setRoomMessage(boolean roomMessage) {
        this.roomMessage = roomMessage;
    }
}
