package com.butel.data.analyses.mode.protocol.udp;

import com.butel.data.analyses.mode.protocol.tcp.TcpServer;
import com.butel.data.analyses.mode.queue.AbstractQueue;
import com.butel.data.analyses.mode.protocol.DataPackage;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.DatagramChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.Objects;

/**
 * Created by ninghf on 2017/11/22.
 */
public class UdpChannelInitializer extends ChannelInitializer<DatagramChannel> {

    private AbstractQueue<DataPackage> dataQueue;
    private boolean flowControl;
    private TcpServer server;

    public UdpChannelInitializer(AbstractQueue<DataPackage> dataQueue) {
        this.dataQueue = dataQueue;
    }

    public boolean isFlowControl() {
        return flowControl;
    }

    public void setFlowControl(boolean flowControl) {
        this.flowControl = flowControl;
    }

    public TcpServer getServer() {
        return server;
    }

    public void setServer(TcpServer server) {
        this.server = server;
    }

    @Override
    protected void initChannel(DatagramChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
//        p.addLast("logger", new LoggingHandler(LogLevel.DEBUG));
        if (isFlowControl()) {
            if (Objects.isNull(server))
                throw new NullPointerException("日志收集与控制服务通信【TcpServer】为空!!!");
            p.addLast("codec", new UdpChannelInboundControlHandler(server));
        } else {
            if (Objects.isNull(dataQueue))
                throw new NullPointerException("日志收集UDP协议服务【dataQueue】队列为空!!!");
            p.addLast("codec", new UdpChannelInboundHandler(dataQueue));
        }
    }
}
