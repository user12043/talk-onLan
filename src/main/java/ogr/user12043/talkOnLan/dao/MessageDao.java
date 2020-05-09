package ogr.user12043.talkOnLan.dao;

import ogr.user12043.talkOnLan.model.Message;
import ogr.user12043.talkOnLan.model.User;
import ogr.user12043.talkOnLan.ui.MainUI;
import ogr.user12043.talkOnLan.util.DBUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by user12043 on 9.05.2020 - 09:41
 * part of project: talk-onLan
 */
public class MessageDao implements Dao<Message, Integer> {
    private static final Logger LOGGER = LogManager.getLogger();
    private final DBConnection db;

    public MessageDao() {
        DBConnection db = null;
        try {
            db = DBConnection.get();
        } catch (Exception e) {
            LOGGER.error("Could not connect to database!", e);
            JOptionPane.showMessageDialog(MainUI.getUI(), "Could not connect to database!", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        this.db = db;
    }

    @Override
    public Set<Message> find() {
        String query = "SELECT * FROM messages";
        Set<Message> messages = new HashSet<>();
        try {
            db.openStatement();
            ResultSet resultSet = db.executeSelectQuery(query);
            while (!resultSet.isClosed() && resultSet.next()) {
                Message message = DBUtils.resultSetToMessage(resultSet);
                fillUsers(message, resultSet);
                messages.add(message);
            }
            db.closeStatement();
            return messages;
        } catch (SQLException e) {
            LOGGER.error(e);
        }
        return null;
    }

    @Override
    public Message findById(Integer id) {
        String query = "SELECT * FROM messages WHERE id=" + id;
        Message message;
        try {
            db.openStatement();
            ResultSet resultSet = db.executeSelectQuery(query);
            if (resultSet.next()) {
                message = DBUtils.resultSetToMessage(resultSet);
                fillUsers(message, resultSet);
                db.closeStatement();
                return message;
            }
        } catch (SQLException e) {
            LOGGER.error(e);
        }
        return null;
    }

    @Override
    public void save(Message message) {
        String query;
        if (message.getId() != null) {
            query = "UPDATE users SET content=':content:', sent_date=':sentDate:', \"type\"=:messageType:" +
                    "sender_id=:senderId:, fwd_user_id=:fwdUserId:, sent=:sent: WHERE id=" + message.getId();
        } else {
            query = "INSERT INTO users VALUES(DEFAULT, ':content:', ':sentDate:', :type:, :senderId:, :fwdUserId:, :sent:)";
        }

        query = query.replace(":content:", message.getContent());
        query = query.replace(":sentDate:", DBUtils.getTimeStamp(message.getSentDate()));
        query = query.replace(":messageType:", String.valueOf(message.getMessageType()));
        query = query.replace(":sent:", String.valueOf(message.isSent()));
        query = query.replace(":senderId:", String.valueOf(message.getSender().getId()));
        if (message.getForwardedFrom() != null) {
            query = query.replace(":fwdUserId:", String.valueOf(message.getForwardedFrom().getId()));
        }

        try {
            db.openStatement();
            db.executeUpdateQuery(query);
            db.closeStatement();
        } catch (SQLException e) {
            LOGGER.error(e);
        }
    }

    @Override
    public void delete(Message message) {
        deleteById(message.getId());
    }

    @Override
    public void deleteById(Integer id) {
        String query = "DELETE FROM messages WHERE id=" + id;
        try {
            db.openStatement();
            db.executeUpdateQuery(query);
            db.closeStatement();
        } catch (SQLException e) {
            LOGGER.error(e);
        }
    }

    private void fillUsers(Message message, ResultSet resultSet) throws SQLException {
        String senderQuery = "SELECT * FROM users WHERE id=" + resultSet.getInt("sender_id");
        String forwardedFromQuery = "SELECT * FROM users where id=" + resultSet.getInt("fwd_user_id");
        ResultSet rs = db.executeSelectQuery(senderQuery);
        if (rs.next()) {
            User sender = DBUtils.resultSetToUser(rs);
            message.setSender(sender);
        }

        rs = db.executeSelectQuery(forwardedFromQuery);
        if (rs.next()) {
            User fwdFrom = DBUtils.resultSetToUser(rs);
            message.setForwardedFrom(fwdFrom);
        }
    }
}
