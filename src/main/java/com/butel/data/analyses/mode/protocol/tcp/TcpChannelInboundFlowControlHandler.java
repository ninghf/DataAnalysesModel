/*===================================================================
 * 北京红云融通技术有限公司
 * 日期：2016年10月26日 上午10:00:05
 * 作者：ninghf
 * 版本：1.0.0
 * 版权：All rights reserved.
 *===================================================================
 * 修订日期           修订人               描述
 * 2016年10月26日     ninghf      创建
 */
package com.butel.data.analyses.mode.protocol.tcp;

import com.butel.data.analyses.mode.protocol.DataPackage;
import com.butel.data.analyses.mode.protocol.ProtocolType;
import com.butel.data.analyses.mode.protocol.flowcontrol.*;
import com.butel.data.analyses.mode.queue.AbstractQueue;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.Objects;

public class TcpChannelInboundFlowControlHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(TcpChannelInboundFlowControlHandler.class);

    private AbstractQueue <DataPackage> dataQueue;
    private Control control;
    private WhiteList whiteList;

    public TcpChannelInboundFlowControlHandler(AbstractQueue <DataPackage> dataQueue, Control control, WhiteList whiteList) {
        this.dataQueue = dataQueue;
        this.control = control;
        this.whiteList = whiteList;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (Objects.nonNull(whiteList)) {
            SocketAddress remoteAddress = ctx.channel().remoteAddress();
            if (whiteList.isWhite(remoteAddress.toString())) {
                return;
            }
            ctx.close();
            throw new NullPointerException("地址【" + remoteAddress.toString() + "】不在白名单中，请联系管理员...");
        }

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        control.free(ctx.channel().id().asLongText());
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg == null) {
            return;
        }
        if (msg instanceof ByteBuf) {
            // 心跳检测
            if (isHeartbeat(msg))
                heartbeat(msg, ctx);
            else {
                DataPackage dataPackage = DataPackage.newInstance();
                dataPackage.build(msg, ctx.channel(), ProtocolType.TCP);
                dataQueue.offer(dataPackage);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
//		ctx.close();
    }

    public boolean isHeartbeat(Object msg) {
        ByteBuf buf = (ByteBuf) msg;
        int version = buf.getUnsignedShortLE(0);
        int dataId = buf.getUnsignedShortLE(2);
        if (version == 3 && dataId == 2) return true;
        else return false;
    }

    public void heartbeat(Object msg, ChannelHandlerContext ctx) {
        ByteBuf buf = (ByteBuf) msg;
        int version = buf.getUnsignedShortLE(0);
        if (version != 3) return;
        int dataId = buf.getUnsignedShortLE(2);
        if (dataId == 2 && Objects.nonNull(control) && Objects.nonNull(whiteList)) {
            HeartbeatReq req = HeartbeatReq.decode(buf);
            if (Objects.nonNull(req)) {
                logger.info("心跳请求：{}", req);
                User user = control.alloc(req.getUserID());
                if (Objects.nonNull(user)) {
                    HeartbeatResp resp = new HeartbeatResp();
                    if (req.getLogSize() == 0 || (req.getLogSize() == -1 && user.getPacketSize() == 0)) {
                        control.free(user);
                        resp.setPacketSize(0);
                    } else {
                        resp.setPacketSize(user.getPacketSize());
                    }
                    resp.setRate(user.getRate());
                    resp.setMark(user.getMark());
                    resp.setHeartbeatRate(user.getHeartbeatRate());
                    ctx.writeAndFlush(resp.encode());
                    logger.info("心跳响应：{}", resp);
                } else if (req.getLogSize() > 0 || req.getLogSize() == -1){
                    User user_ = whiteList.getUser(ctx.channel().remoteAddress().toString());
                    user_.setConnID(ctx.channel().id().asLongText());
                    if (Objects.nonNull(user_))
                        control.waitForAlloc(user_);
                }
            }
        }
        ReferenceCountUtil.release(msg);
    }

}
