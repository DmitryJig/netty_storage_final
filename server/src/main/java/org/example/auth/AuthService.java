package org.example.auth;

import java.io.Closeable;
import java.sql.SQLException;

public interface AuthService extends Closeable {
    void run();
    void close();
    int addClient(String login, String password) throws SQLException;
    int getIdByLoginAndPassword(String login, String password) throws SQLException;
}
