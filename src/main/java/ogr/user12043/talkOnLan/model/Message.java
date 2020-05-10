package ogr.user12043.talkOnLan.model;

import java.util.Date;

/**
 * Created by user12043 on 7.05.2020 - 20:43
 * part of project: talk-onLan
 */
public class Message {
    private Integer id;
    private User sender;
    private User receiver;
    private String content;
    private Date sentDate;
    private int messageType;
    private User forwardedFrom;
    private boolean sent;

    public Message() {
        this.forwardedFrom = new User();
    }

    public Message(User sender, User receiver, String content, Date sentDate, int messageType) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.sentDate = sentDate;
        this.messageType = messageType;
        this.forwardedFrom = new User();
    }

    public Message cloneMessage() {
        Message message = new Message(getSender(), getReceiver(), getContent(), getSentDate(), getMessageType());
        message.setSent(isSent());
        message.setForwardedFrom(getForwardedFrom());
        return message;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
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

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }
}
