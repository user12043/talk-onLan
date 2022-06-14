package ogr.user12043.talkOnLan.dao;

import ogr.user12043.talkOnLan.model.Message;
import ogr.user12043.talkOnLan.model.User;
import ogr.user12043.talkOnLan.util.Constants;
import ogr.user12043.talkOnLan.util.DBUtils;
import ogr.user12043.talkOnLan.util.Utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by user12043 on 9.05.2020 - 09:41
 * part of project: talk-onLan
 */
public class MessageDao implements Dao<Message, Integer> {
    private static final Logger LOGGER = Logger.getLogger(MessageDao.class.getName());
    private static MessageDao instance;
    private final DBConnection db;

    private MessageDao() {
        db = DBConnection.get();
    }

    public static MessageDao get() {
        if (instance == null) {
            instance = new MessageDao();
        }

        return instance;
    }

    @Override
    public List<Message> find() {
        String query = "SELECT * FROM messages";
        return getMessages(query);
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
        } catch (SQLException | ParseException e) {
            LOGGER.severe("Error on MessageDao::findById\n" + e);
        }
        return null;
    }

    @Override
    public void save(Message message) {
        String query;
        if (message.getId() != null) {
            query = "UPDATE messages SET content=':content:', sent_date=':sentDate:', type=:messageType:" +
                    ", sender_id=:senderId:, receiver_id=:receiverId:, fwd_user_id=:fwdUserId:, sent=:sent: WHERE id=" + message.getId();
        } else {
            query = "INSERT INTO messages (CONTENT, SENT_DATE, TYPE, SENDER_ID, RECEIVER_ID, FWD_USER_ID, SENT) VALUES(':content:', ':sentDate:', :messageType:, :senderId:, :receiverId:" +
                    ", :fwdUserId:, :sent:)";
        }

        query = query.replace(":content:", message.getContent());
        query = query.replace(":sentDate:", String.valueOf(message.getSentDate().getTime()));
        query = query.replace(":messageType:", String.valueOf(message.getMessageType()));
        query = query.replace(":sent:", String.valueOf(message.isSent()));
        query = query.replace(":senderId:", String.valueOf(message.getSender().getId()));
        query = query.replace(":receiverId:", String.valueOf(message.getReceiver().getId()));
        query = query.replace(":fwdUserId:",
                message.getForwardedFrom() != null ? String.valueOf(message.getForwardedFrom().getId())
                        : "null"
        );

        try {
            db.openStatement();
            db.executeUpdateQuery(query);
            db.closeStatement();
        } catch (SQLException e) {
            LOGGER.severe("Error on MessageDao::save\n" + e);
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
            LOGGER.severe("Error on MessageDao::deleteById\n" + e);
        }
    }

    public List<Message> findConversation(User sender, User receiver) {
        String query = "SELECT * FROM messages WHERE (sender_id=" + sender.getId() + " AND receiver_id="
                + receiver.getId() + ") OR (sender_id=" + receiver.getId() + " AND receiver_id=" + sender.getId()
                + ") ORDER BY sent_date";
        return getMessages(query);
    }

    public List<Message> findRoomConversation(User room) {
        String query = "SELECT * FROM messages WHERE (type=:fwdMessageType: AND sender_id=:roomId:) " +
                "OR (type=:roomMessageType: AND sender_id=:selfId: AND receiver_id=:roomId:)";
        query = query.replace(":fwdMessageType:", String.valueOf(Constants.MSG_TYPE_FWD));
        query = query.replace(":roomMessageType:", String.valueOf(Constants.MSG_TYPE_ROOM));
        query = query.replace(":roomId:", String.valueOf(room.getId()));
        query = query.replace(":selfId:", String.valueOf(Utils.self().getId()));
        return getMessages(query);
    }

    public List<Message> findSelfRoomConversation() {
        String query = "SELECT * FROM messages WHERE (type=:roomMessageType: AND receiver_id=:roomId:)" +
                "OR (type=:fwdMessageType: AND receiver_id=:roomId:)";
        query = query.replace(":roomMessageType:", String.valueOf(Constants.MSG_TYPE_ROOM));
        query = query.replace(":fwdMessageType:", String.valueOf(Constants.MSG_TYPE_FWD));
        query = query.replace(":roomId:", String.valueOf(Utils.selfRoom().getId()));
        return getMessages(query);
    }

    public List<Message> findUnsentByReceiver(User user) {
        String query = "SELECT * FROM messages WHERE sent=false AND receiver_id=" + user.getId() +
                " ORDER BY sent_date";
        return getMessages(query);
    }

    private List<Message> getMessages(String query) {
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
            List<Message> list = new ArrayList<>(messages);
            list.sort(Comparator.comparing(Message::getSentDate));
            return list;
        } catch (SQLException | ParseException e) {
            LOGGER.severe("Error on MessageDao::getMessages\n" + e);
        }
        return null;
    }

    private void fillUsers(Message message, ResultSet resultSet) throws SQLException {
        String senderQuery = "SELECT * FROM users WHERE id=" + resultSet.getInt("sender_id");
        String receiverQuery = "SELECT * FROM users WHERE id=" + resultSet.getInt("receiver_id");
        String forwardedFromQuery = "SELECT * FROM users where id=" + resultSet.getInt("fwd_user_id");
        Statement statement = db.createStatement();
        ResultSet rs = statement.executeQuery(senderQuery);
        if (rs.next()) {
            User sender = DBUtils.resultSetToUser(rs);
            message.setSender(sender);
        }

        rs = statement.executeQuery(receiverQuery);
        if (rs.next()) {
            User receiver = DBUtils.resultSetToUser(rs);
            message.setReceiver(receiver);
        }

        rs = statement.executeQuery(forwardedFromQuery);
        if (rs.next()) {
            User fwdFrom = DBUtils.resultSetToUser(rs);
            message.setForwardedFrom(fwdFrom);
        }
        statement.close();
    }
}
