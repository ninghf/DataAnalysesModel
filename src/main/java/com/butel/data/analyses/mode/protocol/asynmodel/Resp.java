package com.butel.data.analyses.mode.protocol.asynmodel;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.Recycler;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import static com.butel.data.analyses.mode.protocol.asynmodel.AsynModelConstant.*;

/**
 * Created by ninghf on 2018/3/7.
 *
 * ack.version = req.version
 * ack.success_flag = 实际解析结果
 * ack.index = 0
 * ack.ctx= 0
 */
public class Resp {

//    private static PooledByteBufAllocator ALLOC_Pooled = PooledByteBufAllocator.DEFAULT;

    private Channel channel_resp;
//    private DatagramPacket resp_packet;

    private InetSocketAddress sender;
    private AsyncModel asyncModel;

//    public Resp(Channel channel_resp, InetSocketAddress sender, AsyncModel asyncModel) {
//        this.channel_resp = channel_resp;
//        this.sender = sender;
//        this.asyncModel = asyncModel;
//    }

    private final Recycler.Handle<Resp> recyclerHandle;

    private static final Recycler<Resp> RECYCLER = new Recycler<Resp>() {
        @Override
        protected Resp newObject(Handle<Resp> handle) {
            return new Resp(handle);
        }
    };

    public Resp(Recycler.Handle<Resp> handle) {
        this.recyclerHandle = handle;
    }

    public static Resp newInstance() {
        return RECYCLER.get();
    }

    public void build(Channel channel_resp, InetSocketAddress sender, AsyncModel asyncModel) {
        this.channel_resp = channel_resp;
        this.sender = sender;
        this.asyncModel = asyncModel;
        this.sn = generateRespSN();
        this.createTime = System.currentTimeMillis();
    }

    /**
     * 1 字节
     */
    private byte version = 3;
    /**
     * 1 字节
     */
    private boolean success_flag = true;
    /**
     * 4 字节
     */
    private int index = 0;
    /**
     * 当且仅当 version = 3 才有此值, 长度是8字节;
     */
    private long ctx = 0L;

    private int sn;

    public int getSn() {
        return sn;
    }

    public ByteBuf buildResp() {
        ByteBuf buf = Unpooled.directBuffer(udp_resp_length);

        /** 可靠UDP头 **/
        buf.writeShortLE(udp_version);
        buf.writeShortLE(udp_resp_length);// 响应包整个的长度
        buf.writeShortLE(udp_checksum);
        buf.writeByte(udp_reliable_flag);
        buf.writeByte(udp_data_or_padding_flag);
        buf.writeIntLE(sn);

        /** 异步模型 **/
        buf.writeIntLE(asyncModel.getVersion());
        // MsgId = 2000 -> 2001 | MsgId = 1118 -> 1112 | MsgID = 1113 -> 1114 | MsgId = 1110 -> 1112 | MsgId = 1117 -> 1112
        if (msgid_1118 == asyncModel.getMsgid()) {
            buf.writeIntLE(msgid_1118_resp_1112);
        }
        if (msgid_2000 == asyncModel.getMsgid()) {
            buf.writeIntLE(msgid_2000_resp_2001);
        }
        buf.writeIntLE(asyncModel.getDstid());// 与destId互换
        buf.writeIntLE(asyncModel.getSrcid());// 与srcId互换

        /** 应用响应体 **/
        buf.writeByte(version);
        buf.writeBoolean(success_flag);
        buf.writeIntLE(index);
        buf.writeLongLE(ctx);
        return buf;
    }

    public DatagramPacket pack() {
        return new DatagramPacket(buildResp(), sender);
    }

    public void sendResp() {
        DatagramPacket resp_packet = pack();
        assert channel_resp != null && resp_packet != null;
        channel_resp.writeAndFlush(resp_packet);
    }

    private int count = 1;

    public int getCount() {
        return count;
    }

    private long createTime;

    public boolean isTimeout() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - createTime > 1000)
            return true;
        return false;
    }

    public boolean isRepeatSend() {
        boolean repeat = false;
        long currentTime = System.currentTimeMillis();
        if (count <= 4 && (currentTime - createTime) > 200*count) {
            repeat = true;
            count++;
        }
        return repeat;
    }

    private final int udp_resp_length = 12 + 16 + 14;
    private static final AtomicInteger generate_resp_sn = new AtomicInteger(0);

    public int generateRespSN() {
        if (generate_resp_sn.equals(Integer.MAX_VALUE)) {
            generate_resp_sn.set(0);
        }
        return generate_resp_sn.incrementAndGet();
    }

    public void release() {
        if (channel_resp != null) {
            channel_resp = null;
        }
        if (sender != null) {
            sender = null;
        }
        if (asyncModel != null) {
            asyncModel.release();
            asyncModel = null;
        }
        if (count >= 1) {
            count = 1;
        }
        if (createTime > 0) {
            createTime = 0;
        }
        recyclerHandle.recycle(this);
    }
}
