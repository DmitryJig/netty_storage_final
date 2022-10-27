package org.example.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.example.model.Message;

import java.io.InputStream;
import java.util.List;

public class JacksonDecoder extends ByteToMessageDecoder {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
        InputStream inputStream = new ByteBufInputStream(in);
        out.add(mapper.readValue(inputStream, Message.class));
    }
}
