package ogr.user12043.talkOnLan.dao;

import ogr.user12043.talkOnLan.util.Properties;

import java.sql.*;

/**
 * Created by user12043 on 9.05.2020 - 00:10
 * part of project: talk-onLan
 */
public class DBConnection {
    private static DBConnection instance;
    private final Connection connection;
    private Statement statement;

    private DBConnection() throws SQLException {
        connection = DriverManager.getConnection(Properties.databaseUrl, Properties.databaseUsername, Properties.databasePassword);
    }

    public static DBConnection get() throws SQLException {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    public ResultSet executeSelectQuery(String query) throws SQLException {
        return statement.executeQuery(query);
    }

    public void executeUpdateQuery(String query) throws SQLException {
        statement.executeUpdate(query);
    }

    public void openStatement() throws SQLException {
        statement = connection.createStatement();
    }

    public void closeStatement() throws SQLException {
        statement.close();
    }

    public void close() throws SQLException {
        connection.close();
    }
}
