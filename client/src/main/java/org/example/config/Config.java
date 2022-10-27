package org.example.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {

    public static final String HOST;
    public static final int PORT;
    public static final String USER_DIRECTORY;
    public static final int MAX_OBJECT_SIZE;



    static {
        try (
                InputStream inputStream = Config.class.getResourceAsStream("/application.properties")
        ) {
            Properties properties = new Properties();
            properties.load(inputStream);
            HOST = properties.getProperty("host");
            PORT = Integer.parseInt(properties.getProperty("port"));
            USER_DIRECTORY = properties.getProperty("user_directory");
            MAX_OBJECT_SIZE = Integer.parseInt(properties.getProperty("max_object_size"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
