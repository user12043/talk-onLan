package ogr.user12043.talkOnLan.dao;

import ogr.user12043.talkOnLan.model.User;
import ogr.user12043.talkOnLan.util.DBUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by user12043 on 9.05.2020 - 09:42
 * part of project: talk-onLan
 */
public class UserDao implements Dao<User, Integer> {
    private static final Logger LOGGER = LogManager.getLogger();
    private final DBConnection db;

    public UserDao() {
        DBConnection db = null;
        try {
            db = DBConnection.get();
        } catch (Exception e) {
            LOGGER.error("Could not connect to database!", e);
            JOptionPane.showMessageDialog(null, "Could not connect to database!\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        this.db = db;
    }

    @Override
    public Set<User> find() {
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
                return users;
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
