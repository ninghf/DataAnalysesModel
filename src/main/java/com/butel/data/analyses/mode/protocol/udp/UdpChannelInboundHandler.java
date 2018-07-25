package com.butel.data.analyses.mode.protocol.udp;

import com.butel.data.analyses.mode.protocol.ProtocolType;
import com.butel.data.analyses.mode.queue.AbstractQueue;
import com.butel.data.analyses.mode.protocol.DataPackage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;

/**
 * Created by ninghf on 2017/11/22.
 */
public class UdpChannelInboundHandler extends ChannelInboundHandlerAdapter {

    private AbstractQueue<DataPackage> dataQueue;

    public UdpChannelInboundHandler(AbstractQueue<DataPackage> dataQueue) {
        this.dataQueue = dataQueue;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg == null) {
            return;
        }
        if (msg instanceof DatagramPacket) {
//            DataPackage dataPackage = new DataPackage(msg, ctx.channel(), ProtocolType.UDP);
            DataPackage dataPackage = DataPackage.newInstance();
            dataPackage.build(msg, ctx.channel(), ProtocolType.UDP);
            dataQueue.offer(dataPackage);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
