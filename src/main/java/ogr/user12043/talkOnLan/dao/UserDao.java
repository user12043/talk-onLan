package ogr.user12043.talkOnLan.dao;

import ogr.user12043.talkOnLan.model.User;
import ogr.user12043.talkOnLan.util.DBUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by user12043 on 9.05.2020 - 09:42
 * part of project: talk-onLan
 */
public class UserDao implements Dao<User, Integer> {
    private static final Logger LOGGER = LogManager.getLogger(UserDao.class);
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
        {
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
                LOGGER.error(e);
            }
            return null;
        }
    }

    @Override
    public User findById(Integer id) {
        String query = "SELECT * FROM users WHERE id=" + id;
        User user;
        try {
            db.openStatement();
            ResultSet resultSet = db.executeSelectQuery(query);
            if (resultSet.next()) {
                user = DBUtils.resultSetToUser(resultSet);
                db.closeStatement();
                return user;
            }
        } catch (SQLException e) {
            LOGGER.error(e);
        }
        return null;
    }

    @Override
    public void save(User user) {
        String query;
        if (user.getId() != null) {
            query = "UPDATE users SET username=':username:', address=':address:', is_room=:isRoom: WHERE id=" + user.getId();
        } else {
            query = "INSERT INTO users VALUES(DEFAULT, ':username:', ':address:', :isRoom:)";
        }

        query = query.replace(":username:", user.getUsername());
        query = query.replace(":address:", user.getAddress().getHostAddress());
        query = query.replace(":isRoom:", String.valueOf(user.isRoom()));

        try {
            db.openStatement();
            db.executeUpdateQuery(query);
            db.closeStatement();
        } catch (SQLException e) {
            LOGGER.error(e);
        }
    }

    @Override
    public void delete(User user) {
        deleteById(user.getId());
    }

    @Override
    public void deleteById(Integer id) {
        String query = "DELETE FROM users WHERE id=" + id;
        try {
            db.openStatement();
            db.executeUpdateQuery(query);
            db.closeStatement();
        } catch (SQLException e) {
            LOGGER.error(e);
        }
    }

    public User findByFields(User user) {
        String query = "SELECT * FROM users WHERE username=':username:' AND address=':address:' AND is_room=:isRoom:";
        query = query.replace(":username:", user.getUsername());
        query = query.replace(":address:", user.getAddress().getHostAddress());
        query = query.replace(":isRoom:", String.valueOf(user.isRoom()));
        return getUser(query);
    }

    public User findByAddress(InetAddress address, boolean isRoom) {
        String query = "SELECT * FROM users WHERE address='" + address.getHostAddress() + "' AND is_room=" + isRoom;
        return getUser(query);
    }

    public User findByUsername(String username) {
        String query = "SELECT * FROM users WHERE username='" + username + "' AND is_room=false";
        return getUser(query);
    }

    private User getUser(String query) {
        try {
            db.openStatement();
            ResultSet resultSet = db.executeSelectQuery(query);
            if (resultSet.next()) {
                User result = DBUtils.resultSetToUser(resultSet);
                db.closeStatement();
                return result;
            }
        } catch (SQLException e) {
            LOGGER.error(e);
        }
        return null;
    }
}
