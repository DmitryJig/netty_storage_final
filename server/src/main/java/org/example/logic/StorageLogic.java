package org.example.logic;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.example.config.Config;
import org.example.model.Command;
import org.example.model.Message;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class StorageLogic {

    public static void process(Message message, Channel channel) {
        if (message.getCommand().equals(Command.PUT)) {
            System.out.println(message.getFile()); // todo delete
            Path file = Paths.get(Config.USER_DIRECTORY, message.getFile());
            try {
                Files.createDirectories(file.getParent());
                Files.createFile(file);

            } catch (FileAlreadyExistsException ignored) {
            } catch (IOException e) {
                ChannelFuture future = channel.writeAndFlush(
                        Message.builder().command(message.getCommand()).status("FILE ERROR").build()
                );
                future.addListener(ChannelFutureListener.CLOSE);
                return;
            }
            try (FileOutputStream output = new FileOutputStream(file.toFile())) {

                output.write(message.getData());

                channel.writeAndFlush(Message.builder()
                        .command(message.getCommand())
                        .status("OK")
                        .files(getFileList()).build());
            } catch (IOException e) {
                ChannelFuture future = channel.writeAndFlush(
                        Message.builder().command(message.getCommand()).status("FILE ERROR").build()
                );
                future.addListener(ChannelFutureListener.CLOSE);
                return;
            } finally {
                channel.close();
            }
        }

        if (message.getCommand().equals(Command.GET)) {
            Path file = Paths.get(message.getFile());
            String cleanPath = Paths.get(Config.USER_DIRECTORY).relativize(file).toString();
            try {
                if (Files.exists(file) && Files.size(file) < Config.MAX_OBJECT_SIZE) {
                    Message message1 = Message.builder()
                            .command(message.getCommand())
                            .file(cleanPath)
                            .status("OK")
                            .length(Files.size(file))
                            .data(Files.readAllBytes(file))
                            .build();
                    channel.writeAndFlush(message1);
                }
            } catch (IOException e) {
                ChannelFuture future = channel.writeAndFlush(
                        Message.builder().command(message.getCommand()).status("GET FILE ERROR").build()
                );
                future.addListener(ChannelFutureListener.CLOSE);
            } finally {
                channel.close();
            }
        }

        if (message.getCommand().equals(Command.FILES)) {

            try {
                List<String> fileList = getFileList();
                fileList.forEach(System.out::println); //todo delete

                Message message1 = Message.builder()
                        .command(message.getCommand())
                        .files(fileList)
                        .status("OK")
                        .length(fileList.size()).build();
                channel.writeAndFlush(message1);

            } catch (IOException e) {
                ChannelFuture future = channel.writeAndFlush(
                        Message.builder().command(message.getCommand()).status("GET FILE LIST ERROR").build()
                );
                future.addListener(ChannelFutureListener.CLOSE);
            } finally {
                channel.close();
            }
        }

        if (message.getCommand().equals(Command.DELETE)){
            String pathForDelete = message.getFile();
            try {
                deleteFile(pathForDelete);
                List<String> serverFileList = getFileList();
                Message message1 = Message.builder()
                        .command(message.getCommand())
                        .files(serverFileList)
                        .status("OK")
                        .length(serverFileList.size()).build();
                channel.writeAndFlush(message1);
            } catch (IOException e) {
                ChannelFuture future = channel.writeAndFlush(
                        Message.builder().command(message.getCommand()).status("FILE DELETING ERROR").build()
                );
                future.addListener(ChannelFutureListener.CLOSE);
            } finally {
                channel.close();
            }
        }
    }

    public static List<String> getFileList() throws IOException {

        List<String> fileList = new ArrayList();
        Files.walkFileTree(Paths.get(Config.USER_DIRECTORY), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!Files.isDirectory(file)) {
                    fileList.add(file.getParent().toString() + File.separator + file.getFileName().toString());
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return fileList;
    }

    public static void deleteFile(String pathForDelete) {
        try {
            Files.delete(Paths.get(pathForDelete));
        } catch (IOException e) {
            System.out.println("Exception delete file: " + e);
        }
    }
}
