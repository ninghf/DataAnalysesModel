/*===================================================================
 * 北京红云融通技术有限公司
 * 日期：2017年11月15日 上午10:08:11
 * 作者：ninghf
 * 版本：1.0.0
 * 版权：All rights reserved.
 *===================================================================
 * 修订日期           修订人               描述
 * 2017年11月15日     ninghf      创建
 */
package com.butel.data.analyses.mode.protocol.udp;

import com.butel.data.analyses.mode.protocol.IServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.internal.PlatformDependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UdpServer implements IServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UdpServer.class);

    private String host;
    private int port;
    private UdpChannelInitializer udpChannelInitializer;

    public UdpServer(String host, int port, UdpChannelInitializer udpChannelInitializer) {
        this.host = host;
        this.port = port;
        this.udpChannelInitializer = udpChannelInitializer;
    }

    private EventLoopGroup group;
    private ChannelFuture f;

    /**
     * **********************************ChannelConfig***********************************
     * ChannelOption.CONNECT_TIMEOUT_MILLIS setConnectTimeoutMillis(int)
     * ChannelOption.WRITE_SPIN_COUNT setWriteSpinCount(int)
     * ChannelOption.WRITE_BUFFER_WATER_MARK setWriteBufferWaterMark(WriteBufferWaterMark)
     * ChannelOption.ALLOCATOR setAllocator(ByteBufAllocator)
     * ChannelOption.AUTO_READ setAutoRead(boolean)
     * ******************************DatagramChannelConfig********************************
     * ChannelOption.SO_BROADCAST setBroadcast(boolean)
     * ChannelOption.IP_MULTICAST_ADDR setInterface(InetAddress)
     * ChannelOption.IP_MULTICAST_LOOP_DISABLED setLoopbackModeDisabled(boolean)
     * ChannelOption.IP_MULTICAST_IF setNetworkInterface(NetworkInterface)
     * ChannelOption.SO_REUSEADDR setReuseAddress(boolean)
     * ChannelOption.SO_RCVBUF setReceiveBufferSize(int)
     * ChannelOption.SO_SNDBUF setSendBufferSize(int)
     * ChannelOption.IP_MULTICAST_TTL setTimeToLive(int)
     * ChannelOption.IP_TOS setTrafficClass(int)
     * <p>Title: start</p>
     * <p>Author: ninghf</p>
     * <p>Date: 2017年6月7日</p>
     * <p>Description: TODO</p>
     */
    @Override
    public void start() throws Exception {
        Bootstrap b = new Bootstrap();
        // UnpooledHeapByteBuf PooledHeapByteBuf UnpooledDirectByteBuf PooledDirectByteBuf
        // 设置这四种内存分配方式需要利用
        // ch.config().setAllocator或Bootstrap.option(ChannelOption.ALLOCATOR, ByteBufAllocator.DEFAULT),
        // 结合-Dio.netty.noUnsafe=true|false,
        // (-Dio.netty.allocator.type=unpooled|pooled是否可以设置内存是否池化(Netty默认内存池化))
        // 可以灵活的在如下四种ByteBuf之间进行切换
        if (PlatformDependent.isWindows()) {
            group = new NioEventLoopGroup(1, new DefaultThreadFactory("Netty-UDP-Boss-Pool"));
            b.channel(NioDatagramChannel.class);
            b.option(ChannelOption.SO_BROADCAST, false);
//            b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            b.option(ChannelOption.SO_RCVBUF, 2097152);// 128K=131072|2M=2097152|16M=16777216|32M=33554432|128M=134217728
            b.option(ChannelOption.SO_SNDBUF, 2097152);// 256K=262144|2M=2097152|16M=16777216|32M=33554432|128M=134217728
        } else {
            group = new EpollEventLoopGroup(1, new DefaultThreadFactory("Netty-UDP-Boss-Pool"));
            b.channel(EpollDatagramChannel.class);
//            b.option(EpollChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            b.option(EpollChannelOption.EPOLL_MODE, EpollMode.EDGE_TRIGGERED);
            b.option(EpollChannelOption.SO_BROADCAST, false);
            b.option(EpollChannelOption.SO_RCVBUF, 2097152);// 128K=131072|2M=2097152|16M=16728064|32M=33554432
            b.option(EpollChannelOption.SO_SNDBUF, 2097152);// 256K=262144|2M=2097152|16M=16728064|32M=33554432
        }
        b.group(group);
        b.handler(udpChannelInitializer);
        try {
            f = b.bind(host, port).sync();
        } finally {
            if (f.isSuccess()) {
                LOGGER.info("UDP 服务端【{}:{}】启动成功!", host, port);
            }
//          f.link().closeFuture().sync();
        }
    }

    @Override
    public void shutdown() {
        LOGGER.info("UDP 服务端【{}:{}】即将关闭...", host, port);
        if (f != null) {
            f.channel().close();
        }
        if (group != null && !group.isShutdown()) {
            group.shutdownGracefully();
        }
        LOGGER.info("UDP 服务端【{}:{}】关闭成功!", host, port);
    }
}
