package org.example.logic;

import org.example.config.Config;
import org.example.model.Command;
import org.example.model.Message;
import org.example.network.Client;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class ClientLogic {

    public static List<String> getFileList() {

        List<String> fileList = new ArrayList<>();
        Path userDir = Paths.get(Config.USER_DIRECTORY);

        try {
            Files.walkFileTree(userDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (!Files.isDirectory(file)) {
                        fileList.add(file.getParent().toString() + File.separator + file.getFileName().toString());
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            System.out.println("Error search files in client directory");
        }
        return fileList;
    }

    public static void deleteFile(String pathForDelete) {
        try {
            Files.delete(Paths.get(pathForDelete));
        } catch (IOException e) {
            System.out.println("Exception delete file: " + e);
        }
    }

    public static List<String> sendFileToServer(String path) {
        Path send = Paths.get(path);
        String pathInServer = Paths.get(Config.USER_DIRECTORY).relativize(send).toString();
        List<String> filesInServer = new ArrayList<>();
        try {
            if (Files.size(send) < Config.MAX_OBJECT_SIZE) {

                try {
                    Message message = Message.builder()
                            .command(Command.PUT)
                            .file(pathInServer)
                            .length(Files.size(send))
                            .data(Files.readAllBytes(send))
                            .build();
                    new Client().send(message, response -> {
                        filesInServer.addAll(response.getFiles());
                        System.out.println("i am get files list from server " + filesInServer);
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            } else {
                System.out.println("Too long file size");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return filesInServer;
    }

    public static List<String> getServerFileList() {
        List<String> serverFileList = new ArrayList<>();

        Message message = Message.builder()
                .command(Command.FILES)
                .build();
        new Client().send(message, response -> {
            serverFileList.addAll(response.getFiles());
            System.out.println("i am get files list from server " + serverFileList);
        });

        return serverFileList;
    }

    public static List<String> deleteFileInServer(String path) {
        List<String> serverFileList = new ArrayList<>();

        Message message = Message.builder().command(Command.DELETE).file(path).build();
        new Client().send(message, response -> {
            serverFileList.addAll(response.getFiles());
        });

        return serverFileList;
    }

    public static void sendFromServerToClient(String path) {

        Message message = Message.builder()
                .command(Command.GET)
                .file(path)
                .build();
        new Client().send(message, response -> {
            Path file = Paths.get(Config.USER_DIRECTORY, response.getFile());
            try {
                Files.createDirectories(file.getParent());
                Files.createFile(file);
            } catch (FileAlreadyExistsException ignore) {
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try (FileOutputStream output = new FileOutputStream(file.toFile())) {
                output.write(response.getData());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
