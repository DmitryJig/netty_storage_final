package org.example.auth;

import org.example.config.Config;

import java.sql.*;

/**
 * Запускаем 1 раз для создания структуры базы данных
 */
public class InitDb {
    Connection connection;

    public static void main(String[] args) {
        InitDb initDb = new InitDb();
        initDb.run();
        initDb.createTable();
        try {
            System.out.println(initDb.addClient("login1", "password1"));
        } catch (SQLException e) {
            System.out.println("Error add client");
        }
        initDb.close();
    }

    public void run() {
        try {
            this.connection = DriverManager.getConnection(Config.DB_URL);

            System.out.println("Сервис аутентификации запущен, соединение с базой данных установлено");
        } catch (SQLException e) {

            throw new RuntimeException("Ошибка подключения к базе данных", e);
        }
    }

    private void createTable() {
        try (final PreparedStatement statement = connection.prepareStatement("" +
                " CREATE TABLE IF NOT EXISTS " + Config.CLIENTS_TABLE_NAME + " (" +
                " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " login TEXT UNIQUE," +
                " password TEXT);")) {
            statement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

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
}
