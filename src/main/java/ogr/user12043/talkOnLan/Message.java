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
    private int messageType;
    private User forwardedFrom;

    public Message() {
        this.forwardedFrom = new User();
    }

    public Message(User sender, String content, Date sentDate, int messageType) {
        this.sender = sender;
        this.content = content;
        this.sentDate = sentDate;
        this.messageType = messageType;
        this.forwardedFrom = new User();
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

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public User getForwardedFrom() {
        return forwardedFrom;
    }

    public void setForwardedFrom(User forwardedFrom) {
        this.forwardedFrom = forwardedFrom;
    }
}
