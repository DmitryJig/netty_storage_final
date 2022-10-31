package org.example.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.example.auth.AuthService;
import org.example.logic.StorageLogic;
import org.example.model.Message;

public class ServerHandler extends SimpleChannelInboundHandler<Message> {
    AuthService authService;

    public ServerHandler(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        System.out.println("input msg = " + msg);
        StorageLogic.process(msg, ctx.channel(), authService);
    }
}
