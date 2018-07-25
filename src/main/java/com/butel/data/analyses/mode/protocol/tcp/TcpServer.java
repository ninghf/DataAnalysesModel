/*===================================================================
 * 北京红云融通技术有限公司
 * 日期：2017年11月13日 下午2:34:07
 * 作者：ninghf
 * 版本：1.0.0
 * 版权：All rights reserved.
 *===================================================================
 * 修订日期           修订人               描述
 * 2017年11月13日     ninghf      创建
 */
package com.butel.data.analyses.mode.protocol.tcp;

import com.butel.data.analyses.mode.protocol.IServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class TcpServer implements IServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TcpServer.class);

    private String host;
    private int port;
    private TcpChildChannelInitializer tcpChildChannelInitializer;

    public TcpServer(String host, int port, TcpChildChannelInitializer tcpChildChannelInitializer) {
        this.host = host;
        this.port = port;
        this.tcpChildChannelInitializer = tcpChildChannelInitializer;
        init();
    }

    public TcpServer(TcpChildChannelInitializer tcpChildChannelInitializer) {
        this.tcpChildChannelInitializer = tcpChildChannelInitializer;
        init();
    }

    private ServerBootstrap b;
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

    public void init() {
        b = new ServerBootstrap();

        if (PlatformDependent.isWindows()) {
            bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("Netty-TCP-Boss-Pool"));
            workerGroup = new NioEventLoopGroup(2, new DefaultThreadFactory("Netty-TCP-Worker-Pool"));
            b.channel(NioServerSocketChannel.class);
            b.option(ChannelOption.SO_BACKLOG, 128);
        } else {
            bossGroup = new EpollEventLoopGroup(1, new DefaultThreadFactory("Netty-TCP-Boss-Pool"));
            workerGroup = new EpollEventLoopGroup(2, new DefaultThreadFactory("Netty-TCP-Worker-Pool"));
            b.channel(EpollServerSocketChannel.class);
            b.option(EpollChannelOption.SO_BACKLOG, 128);
        }
        b.group(bossGroup, workerGroup);
        b.handler(new LoggingHandler(LogLevel.DEBUG));
        b.childHandler(tcpChildChannelInitializer);
    }

    public void bind(String host, int port) throws InterruptedException {
        try {
            f = b.bind(host, port).sync();
        } finally {
            if (f.isSuccess()) {
                LOGGER.info("TCP 服务端【{}:{}】启动成功!", host, port);
            }
//			f.link().closeFuture().sync();
        }
    }

    @Override
    public void start() throws Exception {
        try {
            f = b.bind(host, port).sync();
        } finally {
            if (f.isSuccess()) {
                LOGGER.info("TCP 服务端【{}:{}】启动成功!", host, port);
            }
//			f.link().closeFuture().sync();
        }
    }

    @Override
    public void shutdown() {
        LOGGER.info("TCP 服务端【{}:{}】即将关闭...", host, port);
        if (f != null) {
            f.channel().close();
        }
        if (bossGroup != null && !bossGroup.isShutdown()) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null && !workerGroup.isShutdown()) {
            workerGroup.shutdownGracefully();
        }
        LOGGER.info("TCP 服务端【{}:{}】即将关闭...", host, port);
    }

    public TcpChildChannelInitializer getTcpChildChannelInitializer() {
        return tcpChildChannelInitializer;
    }
}
