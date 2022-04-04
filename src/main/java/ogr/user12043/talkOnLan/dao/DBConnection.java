package ogr.user12043.talkOnLan.dao;

import ogr.user12043.talkOnLan.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.sql.*;

/**
 * Created by user12043 on 9.05.2020 - 00:10
 * part of project: talk-onLan
 */
public class DBConnection {
    private static final Logger LOGGER = LogManager.getLogger(DBConnection.class);
    private static DBConnection instance;
    private final Connection connection;
    private Statement statement;

    private DBConnection() throws SQLException {
        connection = DriverManager.getConnection(Properties.databaseUrl, Properties.databaseUsername, Properties.databasePassword);
    }

    public static DBConnection get() {
        if (instance == null) {
            try {
                instance = new DBConnection();
            } catch (Exception e) {
                LOGGER.error("Could not connect to database!", e);
                JOptionPane.showMessageDialog(null, "Could not connect to database!", "talk-onLan", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
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

    public Statement createStatement() throws SQLException {
        return connection.createStatement();
    }
}
