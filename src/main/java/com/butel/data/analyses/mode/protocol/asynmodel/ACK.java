package com.butel.data.analyses.mode.protocol.asynmodel;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.util.Recycler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.butel.data.analyses.mode.protocol.asynmodel.AsynModelConstant.*;

/**
 * Created by ninghf on 2018/3/7.
 */
public class ACK {

    public static final Logger LOGGER = LoggerFactory.getLogger(ACK.class);
//    private static PooledByteBufAllocator ALLOC_Pooled = PooledByteBufAllocator.DEFAULT;

    private int version = udp_version;
    private int length = 12;
    private int checksum = udp_checksum;
    private int reliable_flag = udp_reliable_flag;
    private int ack_flag = udp_ack_flag;
    private int sn;

    private final Recycler.Handle<ACK> recyclerHandle;
    private static final Recycler<ACK> RECYCLER = new Recycler<ACK>() {
        @Override
        protected ACK newObject(Handle<ACK> handle) {
            return new ACK(handle);
        }
    };

    public ACK(Recycler.Handle<ACK> handle) {
        this.recyclerHandle = handle;
    }

    public static ACK newInstance() {
        return RECYCLER.get();
    }

    public void build(int sn) {
        this.sn = sn;
    }

    public void release() {
        recyclerHandle.recycle(this);
    }

//    public ACK(int sn) {
//        this.sn = sn;
//    }

    public ByteBuf buildACK() {
        ByteBuf buf = Unpooled.directBuffer(length);
        buf.writeShortLE(version);
        buf.writeShortLE(length);// 响应包整个的长度
        buf.writeShortLE(checksum);
        buf.writeByte(reliable_flag);
        buf.writeByte(ack_flag);
        buf.writeIntLE(sn);
        return buf;
    }
}
