package org.example.auth;

import org.example.config.Config;

import java.sql.*;

public class DbAuthService implements AuthService {

    private Connection connection;

    public DbAuthService() {
        run();
    }

    @Override
    public void run() {
        try {
            this.connection = DriverManager.getConnection(Config.DB_URL);
            System.out.println("Сервис аутентификации запущен");
        } catch (SQLException e) {
            throw new RuntimeException("Error with connection with DB ", e);
        }
    }

    @Override
    public void close() {
        try {
            if (connection != null) {
                connection.close();
                System.out.println("Сервис аутентификации успешно остановлен");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error close connection with DB ", e);
        }
    }

    @Override
    public int addClient(String login, String password) throws SQLException {
        String tableName = Config.CLIENTS_TABLE_NAME;
        String sql = "INSERT INTO "+ tableName +" (login, password) VALUES (?, ?);";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, login);
            preparedStatement.setString(2, password);
            preparedStatement.execute();
        }

        return getIdByLoginAndPassword(login, password);
    }


    @Override
    public int getIdByLoginAndPassword(String login, String password) throws SQLException {
        String tableName = Config.CLIENTS_TABLE_NAME;
        String sql = "SELECT id FROM "+ tableName +" WHERE login = ? AND password = ?;";
        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, login);
            preparedStatement.setString(2, password);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()){
                return rs.getInt("id");
            }
        }
        return -1;
    }
}
