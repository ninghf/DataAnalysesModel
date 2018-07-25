package com.butel.data.analyses.mode.protocol.http;

import com.butel.data.analyses.mode.protocol.IServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.internal.PlatformDependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ninghf on 2017/11/21.
 * 未完成
 */
public class HttpServer implements IServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServer.class);

    private String host;
    private int port;
    private HttpChildChannelInitializer httpChildChannelInitializer;

    public HttpServer(String host, int port, HttpChildChannelInitializer httpChildChannelInitializer) {
        this.host = host;
        this.port = port;
        this.httpChildChannelInitializer = httpChildChannelInitializer;
    }

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelFuture f;

    /**
     * **********************************ChannelConfig***********************************
     * ChannelOption.CONNECT_TIMEOUT_MILLIS setConnectTimeoutMillis(int)
     * ChannelOption.WRITE_SPIN_COUNT setWriteSpinCount(int)
     * ChannelOption.WRITE_BUFFER_WATER_MARK setWriteBufferWaterMark(WriteBufferWaterMark)
     * ChannelOption.ALLOCATOR setAllocator(ByteBufAllocator)
     * ChannelOption.AUTO_READ setAutoRead(boolean)
     * *****************************ServerSocketChannelConfig*****************************
     * ChannelOption.SO_BACKLOG setBacklog(int)
     * ChannelOption.SO_REUSEADDR setReuseAddress(boolean)
     * ChannelOption.SO_RCVBUF setReceiveBufferSize(int)
     * ********************************SocketChannelConfig********************************
     * ChannelOption.SO_KEEPALIVE setKeepAlive(boolean)
     * ChannelOption.SO_REUSEADDR setReuseAddress(boolean)
     * ChannelOption.SO_LINGER setSoLinger(int)
     * ChannelOption.TCP_NODELAY setTcpNoDelay(boolean)
     * ChannelOption.SO_RCVBUF setReceiveBufferSize(int)
     * ChannelOption.SO_SNDBUF setSendBufferSize(int)
     * ChannelOption.IP_TOS setTrafficClass(int)
     * ChannelOption.ALLOW_HALF_CLOSURE setAllowHalfClosure(boolean)
     * <p>Title: start</p>
     * <p>Author: ninghf</p>
     * <p>Date: 2017年6月7日</p>
     * <p>Description: TODO</p>
     */
    @Override
    public void start() throws Exception {
        ServerBootstrap b = new ServerBootstrap();

        if (PlatformDependent.isWindows()) {
            bossGroup = new NioEventLoopGroup(1,  new DefaultThreadFactory("Netty-HTTP-Boss-Pool"));
            workerGroup = new NioEventLoopGroup(2, new DefaultThreadFactory("Netty-HTTP-Worker-Pool"));
            b.channel(NioServerSocketChannel.class);
            b.option(ChannelOption.SO_BACKLOG, 128);
        } else {
            bossGroup = new EpollEventLoopGroup(1,  new DefaultThreadFactory("Netty-HTTP-Boss-Pool"));
            workerGroup = new EpollEventLoopGroup(2, new DefaultThreadFactory("Netty-HTTP-Worker-Pool"));
            b.channel(EpollServerSocketChannel.class);
            b.option(EpollChannelOption.SO_BACKLOG, 128);
        }
        b.group(bossGroup, workerGroup);
        b.handler(new LoggingHandler(LogLevel.DEBUG));
        b.childHandler(httpChildChannelInitializer);
        try {
            f = b.bind(host, port).sync();
        } finally {
            if (f.isSuccess()) {
                LOGGER.info("HTTP 服务端【{}:{}】启动成功!", host, port);
            }
//			f.link().closeFuture().sync();
        }
    }

    @Override
    public void shutdown() {
        LOGGER.info("HTTP 服务端【{}:{}】即将关闭...", host, port);
        if (f != null) {
            f.channel().close();
        }
        if (bossGroup != null && !bossGroup.isShutdown()) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null && !workerGroup.isShutdown()) {
            workerGroup.shutdownGracefully();
        }
        LOGGER.info("HTTP 服务端【{}:{}】即将关闭...", host, port);
    }
}
