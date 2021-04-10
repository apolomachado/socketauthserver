package pt.apolomachado.serverside.database;

import pt.apolomachado.serverside.Instance;

import java.sql.*;

public class DatabaseProvider {

    protected final Instance instance;
    protected Connection connection;

    public DatabaseProvider(Instance instance) {
        this.instance = instance;
        startConnection();
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    protected void startConnection() {
        try {
            setConnection(DriverManager.getConnection("jdbc:mysql://localhost/accounts", "username", "password"));
            getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS accounts (id INT NOT NULL, username TEXT NOT NULL, password TEXT NOT NULL, PRIMARY KEY (id), lastlogin BIGINT NOT NULL);").execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean existsUsername(String username) {
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM accounts WHERE username = ?;");
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getPassword(String username) {
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM accounts WHERE username = ?;");
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                return resultSet.getString("password");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateLastLogin(String username, long lastLogin) {
        try {
            PreparedStatement updateStatement = getConnection().prepareStatement("UPDATE accounts SET lastlogin = ? WHERE username = ?;");
            updateStatement.setLong(1, lastLogin);
            updateStatement.setString(2, username);
            updateStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}