package com.butel.data.analyses.mode.job;

import com.butel.data.analyses.mode.protocol.DataPackage;
import com.butel.data.analyses.mode.protocol.ProtocolType;
import com.butel.data.analyses.mode.queue.AbstractQueue;
import com.butel.data.analyses.mode.stat.DefaultStat;
import com.butel.data.analyses.mode.stat.IStat;
import com.butel.data.analyses.mode.protocol.asynmodel.ACK;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import static com.butel.data.analyses.mode.protocol.asynmodel.AsynModelConstant.*;
import static com.butel.data.analyses.mode.protocol.asynmodel.AsynModelConstant.udp_ack_flag;
import static com.butel.data.analyses.mode.protocol.asynmodel.AsynModelConstant.udp_data_or_padding_flag;

/**
 * Created by ninghf on 2018/3/7.
 */
public class IOProcessLoop implements IOProcess, Runnable, ILoop<DataPackage> {

    public static final Logger LOGGER = LoggerFactory.getLogger(IOProcessLoop.class);

    private final AbstractQueue<DataPackage> ioProcessQueue;
    private final AbstractQueue<DataPackage> algorithmQueue;

    public IOProcessLoop(AbstractQueue<DataPackage> ioProcessQueue, AbstractQueue<DataPackage> algorithmQueue) {
        this.ioProcessQueue = ioProcessQueue;
        this.algorithmQueue = algorithmQueue;
        bind();
    }

    private Queue<DataPackage> dataQueue;

    @Override
    public void bind() {
        ioProcessQueue.bind(this);
    }

    @Override
    public void doBind(Queue<DataPackage> queue) {
        dataQueue = queue;
    }

    @Override
    public void run() {
        while (true) {
            try {
                DataPackage data = dataQueue.poll();
                if (data == null) {
                    Thread.sleep(1, 1000);
                    continue;
                }
                /**
                 * 自定义IO处理
                 */
                ioProcessQueue.stat().delCnt();
                ioProcess(data);
                algorithmQueue.offer(data);
            } catch (Exception e) {
                LOGGER.error("IO处理任务异常...", e);
            }
        }
    }

    private static IStat udp_ack_send_stat = new DefaultStat("UDP_ACK数据包发送", 1000);

    @Override
    public void ioProcess(DataPackage dataPackage) {
        if (ProtocolType.HTTP == dataPackage.getType()) return;
        if (ProtocolType.TCP == dataPackage.getType()) {
            Object msg = dataPackage.getMsg();
            assert msg instanceof ByteBuf;
            ByteBuf buf = (ByteBuf)msg;
            int version = buf.readUnsignedShortLE();
            if (version != 3) return;
            dataPackage.setVersion(version);
            int dataId = buf.readUnsignedShortLE();
            if (dataId != 1) return;
            dataPackage.setBuf(buf);
//            long length = buf.readUnsignedIntLE();
//            /**
//             * 依据TCP长度如何界定
//             */
//            if (length != buf.readableBytes()) return;
//            dataPackage.setLength(length);
        }
        if (ProtocolType.UDP == dataPackage.getType()) {
            Object msg = dataPackage.getMsg();
            assert msg instanceof DatagramPacket;
            DatagramPacket packet = (DatagramPacket)msg;
            InetSocketAddress sender = packet.sender();
            ByteBuf buf = packet.content();
            int readableBytes = buf.readableBytes();
            /** 校验数据包开始 **/
            int version = buf.readUnsignedShortLE();
            if(version != udp_version) {
                return;
            }
            dataPackage.setVersion(version);
            int length = buf.readUnsignedShortLE();
            if(length != readableBytes) {
                return;
            }
            dataPackage.setLength(length);
            int checksum = buf.readUnsignedShortLE();
            if (udp_checksum != checksum) {
                return;
            }
            int reliable_flag = buf.readByte();
            if (reliable_flag != udp_reliable_flag && reliable_flag != udp_unreliable_flag) {
                return;
            }
            dataPackage.setReliable_flag(reliable_flag);
            int ack_flag = buf.readByte();
            if (ack_flag != udp_ack_flag && ack_flag != udp_data_or_padding_flag) {
                return;
            }
            dataPackage.setAck_flag(ack_flag);
            /** 校验数据包结束 **/
            dataPackage.setSender(sender);
            dataPackage.setBuf(buf);
            if(reliable_flag == udp_reliable_flag && ack_flag != udp_ack_flag) {
                int sn = buf.getIntLE(8);
//                ack(dataPackage.getResp(), sender, sn, dataPackage.getRecvTime());
                ack(dataPackage.getResp(), sender, sn);
            }
        }
    }

    public void ack(Channel channel_resp, InetSocketAddress sender, int sn) {
        udp_ack_send_stat.addCnt();
        ACK ack = ACK.newInstance();
        ack.build(sn);
        channel_resp.writeAndFlush(new DatagramPacket(ack.buildACK(), sender));
        ack.release();
    }

    public void ack(Channel channel_resp, InetSocketAddress sender, int sn, long recvTime) {
        udp_ack_send_stat.addCnt();
        long start_send_ack_time = System.currentTimeMillis();
        ACK ack = ACK.newInstance();
        ack.build(sn);
        channel_resp.writeAndFlush(new DatagramPacket(ack.buildACK(), sender)).addListener(future -> {
            if (future.isSuccess()) {
                long success_send_ack_time = System.currentTimeMillis();
                if (start_send_ack_time - recvTime > 100 || success_send_ack_time - recvTime > 100 || success_send_ack_time - start_send_ack_time > 100) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("数据包sn：【{}】，接收时间：【{}】，接收到数据包到开始返回ACK耗时：【{}】ms", Integer.toHexString(sn), recvTime, start_send_ack_time - recvTime);
                        LOGGER.debug("数据包sn：【{}】，接收时间：【{}】，接收到数据包到成功返回ACK耗时：【{}】ms", Integer.toHexString(sn), recvTime, success_send_ack_time - recvTime);
                        LOGGER.debug("数据包sn：【{}】，接收时间：【{}】，开始返回ACK到成功返回ACK耗时：【{}】ms", Integer.toHexString(sn), recvTime, success_send_ack_time - start_send_ack_time);
                    }
                }
            }
        });
        ack.release();
    }
}
