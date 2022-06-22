package ogr.user12043.talkOnLan.dao;

import ogr.user12043.talkOnLan.model.Message;
import ogr.user12043.talkOnLan.model.User;
import ogr.user12043.talkOnLan.util.Constants;
import ogr.user12043.talkOnLan.util.DBUtils;
import ogr.user12043.talkOnLan.util.Utils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
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
        Set<Message> messages = new HashSet<>();
        try {
            db.openStatement();
            final ResultSet resultSet = db.executeSelectQuery(query);
            while (!resultSet.isClosed() && resultSet.next()) {
                Message message = DBUtils.resultSetToMessage(resultSet);
                messages.add(message);
            }
            db.closeStatement();
            return new ArrayList<>(messages);
        } catch (SQLException | ParseException e) {
            LOGGER.severe("Erron on MessageDao::find\n" + e);
            return null;
        }
    }

    @Override
    public Message findById(Integer id) {
        String query = "SELECT * FROM messages WHERE id=?";
        Message message;
        try {
            try (PreparedStatement preparedStatement = db.createPreparedStatement(query)) {
                preparedStatement.setInt(1, id);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    message = DBUtils.resultSetToMessage(resultSet);
                    fillUsers(message, resultSet);
                    return message;
                }
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
            query = "UPDATE messages SET content=?, sent_date=?, type=?" +
                    ", sender_id=?, receiver_id=?, fwd_user_id=?, sent=? WHERE id=?";
        } else {
            query = "INSERT INTO messages (CONTENT, SENT_DATE, TYPE, SENDER_ID, RECEIVER_ID, FWD_USER_ID, SENT)" +
                    "VALUES(?, ?, ?, ?, ?, ?, ?)";
        }

        try {
            try (PreparedStatement preparedStatement = db.createPreparedStatement(query)) {
                preparedStatement.setString(1, message.getContent());
                preparedStatement.setLong(2, message.getSentDate().getTime());
                preparedStatement.setInt(3, message.getMessageType());
                preparedStatement.setInt(4, message.getSender().getId());
                preparedStatement.setInt(5, message.getReceiver().getId());
                if (message.getForwardedFrom() != null && message.getForwardedFrom().getId() != null) {
                    preparedStatement.setInt(6, message.getForwardedFrom().getId());
                } else {
                    preparedStatement.setNull(6, Types.INTEGER);
                }
                preparedStatement.setBoolean(7, message.isSent());
                if (message.getId() != null) {
                    preparedStatement.setInt(8, message.getId());
                }
                preparedStatement.executeUpdate();
            }
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
        String query = "DELETE FROM messages WHERE id=?";
        try {
            try (PreparedStatement preparedStatement = db.createPreparedStatement(query)) {
                preparedStatement.setInt(1, id);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            LOGGER.severe("Error on MessageDao::deleteById\n" + e);
        }
    }

    public void clearAll(User user) {
        String query = "DELETE FROM messages where SENDER_ID=? OR RECEIVER_ID=?";
        try {
            try (PreparedStatement preparedStatement = db.createPreparedStatement(query)) {
                preparedStatement.setInt(1, user.getId());
                preparedStatement.setInt(2, user.getId());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            LOGGER.severe("Error on MessageDao::clearAll\n" + e);
            throw new RuntimeException(e);
        }
    }

    public List<Message> findConversation(User sender, User receiver) {
        String query = "SELECT * FROM messages WHERE (sender_id=? AND receiver_id=?)" +
                " OR (sender_id=? AND receiver_id=?) ORDER BY sent_date";
        try {
            try (PreparedStatement preparedStatement = db.createPreparedStatement(query)) {
                preparedStatement.setInt(1, sender.getId());
                preparedStatement.setInt(2, receiver.getId());
                preparedStatement.setInt(3, receiver.getId());
                preparedStatement.setInt(4, sender.getId());
                final ResultSet resultSet = preparedStatement.executeQuery();
                return getMessages(resultSet);
            }
        } catch (SQLException e) {
            LOGGER.severe("Error on MessageDao::findConversation\n" + e);
            return null;
        }
    }

    public List<Message> findRoomConversation(User room) {
        String query = "SELECT * FROM messages WHERE (type=? AND sender_id=?) " +
                "OR (type=? AND sender_id=? AND receiver_id=?)";
        try {
            try (PreparedStatement preparedStatement = db.createPreparedStatement(query)) {
                preparedStatement.setInt(1, Constants.MSG_TYPE_FWD);
                preparedStatement.setInt(2, room.getId());
                preparedStatement.setInt(3, Constants.MSG_TYPE_ROOM);
                preparedStatement.setInt(4, Utils.self().getId());
                preparedStatement.setInt(5, room.getId());
                final ResultSet resultSet = preparedStatement.executeQuery();
                return getMessages(resultSet);
            }
        } catch (SQLException e) {
            LOGGER.severe("Error on MessageDao::findRoomConversation\n" + e);
            return null;
        }
    }

    public List<Message> findSelfRoomConversation() {
        String query = "SELECT * FROM messages WHERE (type=? AND receiver_id=?)" +
                "OR (type=? AND receiver_id=?)";
        try {
            try (PreparedStatement preparedStatement = db.createPreparedStatement(query)) {
                preparedStatement.setInt(1, Constants.MSG_TYPE_ROOM);
                preparedStatement.setInt(2, Utils.selfRoom().getId());
                preparedStatement.setInt(3, Constants.MSG_TYPE_FWD);
                preparedStatement.setInt(4, Utils.selfRoom().getId());
                final ResultSet resultSet = preparedStatement.executeQuery();
                return getMessages(resultSet);
            }
        } catch (SQLException e) {
            LOGGER.severe("Error on MessageDao::findSelfRoomConversation\n" + e);
            return null;
        }
    }

    public List<Message> findUnsentByReceiver(User user) {
        String query = "SELECT * FROM messages WHERE sent=0 AND receiver_id=? ORDER BY sent_date";
        try {
            try (PreparedStatement preparedStatement = db.createPreparedStatement(query)) {
                preparedStatement.setInt(1, user.getId());
                final ResultSet resultSet = preparedStatement.executeQuery();
                return getMessages(resultSet);
            }
        } catch (SQLException e) {
            LOGGER.severe("Error on MessageDao::findUnsentByReceiver\n" + e);
            return null;
        }
    }

    private List<Message> getMessages(ResultSet resultSet) {
        Set<Message> messages = new HashSet<>();
        try {
            while (!resultSet.isClosed() && resultSet.next()) {
                Message message = DBUtils.resultSetToMessage(resultSet);
                fillUsers(message, resultSet);
                messages.add(message);
            }
            List<Message> list = new ArrayList<>(messages);
            list.sort(Comparator.comparing(Message::getSentDate));
            return list;
        } catch (SQLException | ParseException e) {
            LOGGER.severe("Error on MessageDao::getMessages\n" + e);
        }
        return null;
    }

    private void fillUsers(Message message, ResultSet resultSet) throws SQLException {
        String senderQuery = "SELECT * FROM users WHERE id=?";
        PreparedStatement statement = db.createPreparedStatement(senderQuery);
        statement.setInt(1, resultSet.getInt("sender_id"));
        ResultSet rs = statement.executeQuery();
        if (rs.next()) {
            User sender = DBUtils.resultSetToUser(rs);
            message.setSender(sender);
        }

        statement.setInt(1, resultSet.getInt("receiver_id"));
        rs = statement.executeQuery();
        if (rs.next()) {
            User receiver = DBUtils.resultSetToUser(rs);
            message.setReceiver(receiver);
        }

        statement.setInt(1, resultSet.getInt("fwd_user_id"));
        rs = statement.executeQuery();
        if (rs.next()) {
            User fwdFrom = DBUtils.resultSetToUser(rs);
            message.setForwardedFrom(fwdFrom);
        }
        statement.close();
    }
}
