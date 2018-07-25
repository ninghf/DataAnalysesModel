package com.butel.data.analyses.mode.protocol.udp;

import com.butel.data.analyses.mode.protocol.flowcontrol.ControlReq;
import com.butel.data.analyses.mode.protocol.tcp.TcpChildChannelInitializer;
import com.butel.data.analyses.mode.protocol.tcp.TcpServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by ninghf on 2017/11/22.
 */
public class UdpChannelInboundControlHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(UdpChannelInboundControlHandler.class);
    private TcpServer server;

    public UdpChannelInboundControlHandler(TcpServer server) {
        this.server = server;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().eventLoop().scheduleAtFixedRate(() -> {
            ControlReq req = new ControlReq();
//            ctx.channel().writeAndFlush()
        }, 0, 1, TimeUnit.MINUTES);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.error("日志收集服务与控制服务通信中断...");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg == null) {
            return;
        }
        if (msg instanceof DatagramPacket) {
            // 日志收集TCP监听端口变更后TCP重新绑定端口
            TcpChildChannelInitializer tcpChildChannelInitializer = server.getTcpChildChannelInitializer();
//            tcpChildChannelInitializer.setControl();
//            tcpChildChannelInitializer.setWhiteList();
//            server.bind();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
