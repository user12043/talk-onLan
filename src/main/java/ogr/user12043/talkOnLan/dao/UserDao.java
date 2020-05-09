package ogr.user12043.talkOnLan.dao;

import ogr.user12043.talkOnLan.model.User;
import ogr.user12043.talkOnLan.util.DBUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by user12043 on 9.05.2020 - 09:42
 * part of project: talk-onLan
 */
public class UserDao implements Dao<User, Integer> {
    private final DBConnection db;

    public UserDao() throws SQLException {
        db = DBConnection.get();
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
                e.printStackTrace();
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
            user = DBUtils.resultSetToUser(resultSet);
            db.closeStatement();
            return user;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void save(User user) {
        String query;
        if (user.getId() != null) {
            query = "UPDATE users SET username=':username:', address=':address:', is_room=':isRoom:' WHERE id=" + user.getId();
        } else {
            query = "INSERT INTO users VALUES(DEFAULT, ':username:', ':address:', ':isRoom:')";
        }

        query = query.replace(":username:", user.getUsername());
        query = query.replace(":address:", user.getAddress().getHostAddress());
        query = query.replace(":isRoom:", String.valueOf(user.isRoom()));

        try {
            db.openStatement();
            db.executeUpdateQuery(query);
            db.closeStatement();
        } catch (SQLException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }
}
