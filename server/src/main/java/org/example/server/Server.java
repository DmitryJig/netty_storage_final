package org.example.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;
import org.example.auth.AuthService;
import org.example.auth.DbAuthService;
import org.example.config.Config;

public class Server {

    private AuthService authService;
    private final int port;
    public Server(){
        this.port = Config.PORT;
    }

    public static void main(String[] args) {
        new Server().run();
    }

    private void run() {
        authService = new DbAuthService();

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try{
            ServerBootstrap server = new ServerBootstrap();
            server.group(bossGroup, workerGroup);
            server.channel(NioServerSocketChannel.class);
            server.option(ChannelOption.SO_BACKLOG, 128);
            server.childOption(ChannelOption.SO_KEEPALIVE, true);
            server.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(8 * 1024, 32 * 1024));
            server.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(
                            new JsonObjectDecoder(),
                            new JacksonDecoder(),
                            new JacksonEncoder(),
                            new ServerHandler(authService)
                    );
                }
            });
            ChannelFuture future = server.bind(port);
            System.out.println("Server running on port " + port);
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            authService.close();
        }
    }
}
