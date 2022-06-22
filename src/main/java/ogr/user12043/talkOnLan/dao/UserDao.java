package ogr.user12043.talkOnLan.dao;

import ogr.user12043.talkOnLan.model.User;
import ogr.user12043.talkOnLan.util.DBUtils;

import java.net.InetAddress;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by user12043 on 9.05.2020 - 09:42
 * part of project: talk-onLan
 */
public class UserDao implements Dao<User, Integer> {
    private static final Logger LOGGER = Logger.getLogger(UserDao.class.getName());
    private static UserDao instance;
    private final DBConnection db;

    private UserDao() {
        db = DBConnection.get();
    }

    public static UserDao get() {
        if (instance == null) {
            instance = new UserDao();
        }

        return instance;
    }

    @Override
    public List<User> find() {
        String query = "SELECT * FROM users";
        Set<User> users = new HashSet<>();
        try {
            db.openStatement();
            ResultSet resultSet = db.executeSelectQuery(query);
            while (!resultSet.isClosed() && resultSet.next()) {
                User user = DBUtils.resultSetToUser(resultSet);
                users.add(user);
            }
            db.closeStatement();
            return new ArrayList<>(users);
        } catch (SQLException e) {
            LOGGER.severe("Error on UserDao::find\n" + e);
            return null;
        }
    }

    @Override
    public User findById(Integer id) {
        String query = "SELECT * FROM users WHERE id=?";
        User user;
        try {
            try (PreparedStatement preparedStatement = db.createPreparedStatement(query)) {
                preparedStatement.setInt(1, id);
                final ResultSet resultSet = preparedStatement.executeQuery();
                return getUser(resultSet);
            }
        } catch (SQLException e) {
            LOGGER.severe("Error on UserDao::findById\n" + e);
        }
        return null;
    }

    @Override
    public void save(User user) {
        String query;
        if (user.getId() != null) {
            query = "UPDATE users SET username=?, address=?, is_room=? WHERE id=?";
        } else {
            query = "INSERT INTO users (USERNAME, ADDRESS, IS_ROOM) VALUES(?, ?, ?)";
        }

        try {
            try (PreparedStatement preparedStatement = db.createPreparedStatement(query)) {
                preparedStatement.setString(1, user.getUsername());
                preparedStatement.setString(2, user.getAddress().getHostAddress());
                preparedStatement.setBoolean(3, user.isRoom());
                if (user.getId() != null) {
                    preparedStatement.setInt(4, user.getId());
                }
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            LOGGER.severe("Error on UserDao::save\n" + e);
        }
    }

    @Override
    public void delete(User user) {
        deleteById(user.getId());
    }

    @Override
    public void deleteById(Integer id) {
        String query = "DELETE FROM users WHERE id=?";
        try {
            final PreparedStatement statement = db.createPreparedStatement(query);
            statement.setInt(1, id);
            statement.execute();
            statement.close();
        } catch (SQLException e) {
            LOGGER.severe("Error on UserDao::deleteById\n" + e);
            throw new RuntimeException(e);
        }
    }

    public User findByFields(User user) {
        String query = "SELECT * FROM users WHERE username=? AND address=? AND is_room=?";
        try {
            try (PreparedStatement preparedStatement = db.createPreparedStatement(query)) {
                preparedStatement.setString(1, user.getUsername());
                preparedStatement.setString(2, user.getAddress().getHostAddress());
                preparedStatement.setBoolean(3, user.isRoom());
                final ResultSet resultSet = preparedStatement.executeQuery();
                return getUser(resultSet);
            }
        } catch (SQLException e) {
            LOGGER.severe("Error on UserDao::findByFields\n" + e);
        }
        return null;
    }

    public User findByAddress(InetAddress address, boolean isRoom) {
        String query = "SELECT * FROM users WHERE address=? AND is_room=?";
        try {
            try (PreparedStatement preparedStatement = db.createPreparedStatement(query)) {
                preparedStatement.setString(1, address.getHostAddress());
                preparedStatement.setBoolean(2, isRoom);
                final ResultSet resultSet = preparedStatement.executeQuery();
                return getUser(resultSet);
            }
        } catch (SQLException e) {
            LOGGER.severe("Error on UserDao::findByAddress\n" + e);
        }
        return null;
    }

    public User findByUsername(String username) {
        String query = "SELECT * FROM users WHERE username=? AND is_room=0";
        try {
            try (PreparedStatement preparedStatement = db.createPreparedStatement(query)) {
                preparedStatement.setString(1, username);
                final ResultSet resultSet = preparedStatement.executeQuery();
                return getUser(resultSet);
            }
        } catch (SQLException e) {
            LOGGER.severe("Error on UserDao::findByUsername\n" + e);
        }
        return null;
    }

    private User getUser(ResultSet resultSet) {
        try {
            if (resultSet.next()) {
                return DBUtils.resultSetToUser(resultSet);
            }
        } catch (SQLException e) {
            LOGGER.severe("Error on UserDao::getUser\n" + e);
        }
        return null;
    }
}
