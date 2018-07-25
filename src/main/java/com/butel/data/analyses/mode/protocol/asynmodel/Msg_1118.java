package com.butel.data.analyses.mode.protocol.asynmodel;

import com.butel.data.analyses.mode.util.ZlibUtils;
import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.Recycler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ninghf on 2018/3/7.
 *
 * 异步模型-Msg-1118
 * <table border="1" cellspacing="0" cellpadding="6">
 * <tr>
 * <th>Name</th><th>Bytes</th><th>description</th>
 * </tr><tr>
 * <td>version</td><td>1</td><td>Msg-1118版本号（默认：3）</td>
 * </tr><tr>
 * <td>sendTime</td><td>8</td><td>发送时间</td>
 * </tr><tr>
 * <td>logs</td><td>未知</td><td>压缩后的日志（log记录长度（4Bytes）+= log记录）</td>
 * </tr>
 * </table>
 */
public class Msg_1118 {

    private static final Logger logger = LoggerFactory.getLogger(Msg_1118.class);

    private int version;
    private long sendTime;
    private List<String> logs;

    private final Recycler.Handle<Msg_1118> recyclerHandle;
    private static final Recycler<Msg_1118> RECYCLER = new Recycler<Msg_1118>() {
        @Override
        protected Msg_1118 newObject(Handle<Msg_1118> handle) {
            return new Msg_1118(handle);
        }
    };

    public Msg_1118(Recycler.Handle<Msg_1118> handle) {
        this.recyclerHandle = handle;
    }

    public static Msg_1118 newInstance() {
        return RECYCLER.get();
    }

    public void release() {
        if (version > 0)
            version = 0;
        if (sendTime > 0)
            sendTime = 0;
        if (logs != null)
            logs = null;
        recyclerHandle.recycle(this);
    }

    public long getSendTime() {
        return sendTime;
    }

    public List<String> getLogs() {
        return logs;
    }

    public void byteBufToMessage(ByteBuf buf) {
        version = buf.readByte();
        sendTime = buf.readLongLE();
//        long clone_start_time = System.currentTimeMillis();
//        ByteBuf clone = Unpooled.copiedBuffer(buf);
//        long clone_end_time = System.currentTimeMillis();
        ByteBuf unpack = null;
        try {
            unpack = ZlibUtils.decompress(buf);
        } catch (Exception e) {
            if (unpack != null) {
                unpack.clear();
                unpack.release();
            }
            logger.error("[Msg_1118]解压缩异常:", e);
        }
//            long decompress_end_time = System.currentTimeMillis();
        if (unpack == null) {
            return;
        }
        try {
            logs = new ArrayList<String>();
            while (unpack.readableBytes() > 0) {
                long length = unpack.readUnsignedIntLE();
                String log = unpack.readCharSequence((int) length, Charsets.UTF_8).toString();
                logs.add(log);
            }
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
            unpack.clear();
            unpack.release();
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("version", version)
                .add("sendTime", sendTime)
                .add("logs", logs)
                .toString();
    }
}
