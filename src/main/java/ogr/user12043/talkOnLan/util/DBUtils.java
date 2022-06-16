package ogr.user12043.talkOnLan.util;

import ogr.user12043.talkOnLan.model.Message;
import ogr.user12043.talkOnLan.model.User;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by user12043 on 9.05.2020 - 10:48
 * part of project: talk-onLan
 */
public class DBUtils {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static User resultSetToUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt("id"));
        user.setUsername(resultSet.getString("username"));
        try {
            user.setAddress(InetAddress.getByName(resultSet.getString("address")));
        } catch (UnknownHostException e) {
            throw new SQLException("IP address format invalid");
        }
        user.setRoom(resultSet.getBoolean("is_room"));
        return user;
    }

    public static Message resultSetToMessage(ResultSet resultSet) throws SQLException, ParseException {
        Message message = new Message();
        message.setId(resultSet.getInt("id"));
        message.setContent(resultSet.getString("content"));
        message.setSentDate(new Date(resultSet.getLong("sent_date")));
        message.setMessageType(resultSet.getInt("type"));
        message.setSent(resultSet.getBoolean("sent"));
        return message;
    }

    public static String getTimeStamp(Date date) {
        return dateFormat.format(date);
    }
}
