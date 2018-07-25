package com.butel.data.analyses.mode.protocol.http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
/**
 * Created by ninghf on 2017/11/21.
 * 未完成
 */
public class HttpChildChannelInitializer extends ChannelInitializer<SocketChannel> {

    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        p.addLast("logger", new LoggingHandler(LogLevel.DEBUG));
    }
}
