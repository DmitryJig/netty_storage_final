package org.example.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {

    public static final String DB_URL;
    public static final int PORT;
    public static final String USER_DIRECTORY;
    public static final int MAX_OBJECT_SIZE;
    public static final String CLIENTS_TABLE_NAME;



    static {
        try (
                InputStream inputStream = Config.class.getResourceAsStream("/application.properties")
        ) {
            Properties properties = new Properties();
            properties.load(inputStream);
            PORT = Integer.parseInt(properties.getProperty("port"));
            USER_DIRECTORY = properties.getProperty("user_directory");
            MAX_OBJECT_SIZE = Integer.parseInt(properties.getProperty("max_object_size"));
            DB_URL = properties.getProperty("db_url");
            CLIENTS_TABLE_NAME = properties.getProperty("clients_table_in_db");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
