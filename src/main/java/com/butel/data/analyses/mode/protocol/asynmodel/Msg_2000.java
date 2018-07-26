package com.butel.data.analyses.mode.protocol.asynmodel;

import com.butel.data.analyses.mode.stat.IStat;
import com.butel.data.analyses.mode.util.ZlibUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.Recycler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/4/12
 * @description TODO
 */
public class Msg_2000 {

    private static final Logger logger = LoggerFactory.getLogger(Msg_2000.class);

    private int version;
    private long sendTime;
    private List<ByteBuf> bufs;

    private final Recycler.Handle<Msg_2000> recyclerHandle;
    private static final Recycler<Msg_2000> RECYCLER = new Recycler<Msg_2000>() {
        @Override
        protected Msg_2000 newObject(Handle<Msg_2000> handle) {
            return new Msg_2000(handle);
        }
    };

    public Msg_2000(Recycler.Handle <Msg_2000> handle) {
        this.recyclerHandle = handle;
    }

    public static Msg_2000 newInstance() {
        return RECYCLER.get();
    }

    public void release() {
        if (version > 0)
            version = 0;
        if (sendTime > 0)
            sendTime = 0;
        if (bufs != null)
            bufs = null;
        recyclerHandle.recycle(this);
    }

    public void byteBufToMessage(ByteBuf buf, IStat zlib_before_decompress_stat, IStat zlib_last_decompress_stat, IStat user_data_stat) {
        version = buf.readByte();
        sendTime = buf.readLongLE();
        zlib_before_decompress_stat.add(buf.readableBytes());
        ByteBuf unpack = null;
        try {
            unpack = ZlibUtils.decompress(buf);
        } catch (Exception e) {
            if (unpack != null) {
                unpack.clear();
                unpack.release();
            }
            logger.error("[Msg_2000]解压缩异常:", e);
        }
//            long decompress_end_time = System.currentTimeMillis();
        if (unpack == null) {
            return;
        }
        int unpack_last = unpack.readableBytes();
        zlib_last_decompress_stat.add(unpack_last);
        try {
            boolean isContinue = true;
            bufs = new LinkedList <>();
            while (unpack.readableBytes() > 0 && isContinue) {
                int len = unpack.readUnsignedShortLE();
                if (unpack.readableBytes() >= len) {
                    ByteBuf dst = Unpooled.buffer(len);
                    unpack.readBytes(dst, len);
                    bufs.add(dst);
                } else {
                    isContinue = false;
                }
            }
            user_data_stat.add(bufs.size());

//            long unpack_end_time = System.currentTimeMillis();
//            if (LOGGER.isDebugEnabled() && unpack_end_time - clone_start_time > 5) {
//                LOGGER.debug("Msg_1118解析总耗时【{}】ms，第一次clone耗时【{}】ms，zlib解压缩算法耗时【{}】ms，反序列化耗时【{}】ms",
//                        unpack_end_time - clone_start_time,
//                        clone_end_time - clone_start_time,
//                        decompress_end_time - clone_end_time,
//                        unpack_end_time - decompress_end_time);
//                LOGGER.debug(toString());
//            }
        } finally {
            unpack.release();
        }
    }

    public long getSendTime() {
        return sendTime;
    }

    public List <ByteBuf> getBufs() {
        return bufs;
    }
}
