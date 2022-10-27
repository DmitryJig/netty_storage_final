package org.example.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;
import org.example.config.Config;
import org.example.model.Message;
import java.util.function.Consumer;

public class Client {

    public void send(Message message, Consumer<Message> callback) {
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap client = new Bootstrap();
            client.group(workerGroup);
            client.channel(NioSocketChannel.class);
            client.option(ChannelOption.SO_KEEPALIVE, true);
            client.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel sh) {
                    sh.pipeline().addLast(
                            new JsonObjectDecoder(), // проверяет все входящие данные и ищет последнюю закрытую скобку,
                            // только потом передает следующему обработчику данные как объект
                            new JacksonDecoder(), // этот превратит json в объект
                            new JacksonEncoder(),
                            new ClientHandler(message, callback)
                    );
                }
            });
            ChannelFuture future = client.connect(Config.HOST, Config.PORT).sync();
            future.addListener(f -> {
                if (!f.isSuccess()) {
                    callback.accept(Message.builder().command(message.getCommand()).status("CONNECTION FAILED").build());
                }
            });
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
