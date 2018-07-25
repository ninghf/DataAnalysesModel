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
import com.butel.data.analyses.mode.queue.AbstractQueue;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class TcpChannelInboundHandler extends ChannelInboundHandlerAdapter {

	private AbstractQueue<DataPackage> dataQueue;

	public TcpChannelInboundHandler(AbstractQueue<DataPackage> dataQueue) {
		this.dataQueue = dataQueue;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {

	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg == null) {
			return;
		}
		if (msg instanceof ByteBuf) {
//			DataPackage dataPackage = new DataPackage(msg, ctx.channel(), ProtocolType.TCP);
			DataPackage dataPackage = DataPackage.newInstance();
			dataPackage.build(msg, ctx.channel(), ProtocolType.TCP);
			dataQueue.offer(dataPackage);
        }
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
//		ctx.close();
	}

}
