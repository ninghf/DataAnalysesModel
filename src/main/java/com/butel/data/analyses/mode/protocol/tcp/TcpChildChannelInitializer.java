/*===================================================================
 * 北京红云融通技术有限公司
 * 日期：2016年10月26日 上午9:50:30
 * 作者：ninghf
 * 版本：1.0.0
 * 版权：All rights reserved.
 *===================================================================
 * 修订日期           修订人               描述
 * 2016年10月26日     ninghf      创建
 */
package com.butel.data.analyses.mode.protocol.tcp;

import java.nio.ByteOrder;
import java.util.Objects;

import com.butel.data.analyses.mode.protocol.DataPackage;
import com.butel.data.analyses.mode.protocol.flowcontrol.Control;
import com.butel.data.analyses.mode.protocol.flowcontrol.WhiteList;
import com.butel.data.analyses.mode.queue.AbstractQueue;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class TcpChildChannelInitializer extends ChannelInitializer<SocketChannel> {

	private AbstractQueue<DataPackage> dataQueue;
    private Control control;
    private WhiteList whiteList;

	public TcpChildChannelInitializer(AbstractQueue<DataPackage> dataQueue) {
		this.dataQueue = dataQueue;
	}

    public Control getControl() {
        return control;
    }

    public void setControl(Control control) {
        this.control = control;
    }

    public WhiteList getWhiteList() {
        return whiteList;
    }

    public void setWhiteList(WhiteList whiteList) {
        this.whiteList = whiteList;
    }

    @Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline p = ch.pipeline();
//		p.addLast("logger", new LoggingHandler(LogLevel.DEBUG));
		p.addLast("frame-decoder", new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, 5*1024*1024, 6, 4, 0, 0, true));
		if (Objects.isNull(dataQueue))
		    throw new NullPointerException("日志收集TCP协议服务【dataQueue】队列为空!!!");
		if (Objects.nonNull(control) && Objects.nonNull(whiteList))
            p.addLast("tcp-server-decoder", new TcpChannelInboundFlowControlHandler(dataQueue, control, whiteList));
		else
		    p.addLast("tcp-server-decoder", new TcpChannelInboundHandler(dataQueue));
	}

}
