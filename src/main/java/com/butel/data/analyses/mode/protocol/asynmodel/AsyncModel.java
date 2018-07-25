package com.butel.data.analyses.mode.protocol.asynmodel;

import com.google.common.base.MoreObjects;
import io.netty.buffer.ByteBuf;
import io.netty.util.Recycler;

/**
 * Created by ninghf on 2018/3/7.
 * 异步模型
 */
public class AsyncModel {

    private final Recycler.Handle<AsyncModel> recyclerHandle;

    private static final Recycler<AsyncModel> RECYCLER = new Recycler<AsyncModel>() {
        @Override
        protected AsyncModel newObject(Handle<AsyncModel> handle) {
            return new AsyncModel(handle);
        }
    };

    private int version;
    private int msgid;
    private int srcid;
    private int dstid;

    public AsyncModel(Recycler.Handle<AsyncModel> handle) {
        this.recyclerHandle = handle;
    }

    public static AsyncModel newInstance() {
        return RECYCLER.get();
    }

    public void release() {
        recyclerHandle.recycle(this);
    }

    public void byteBufToMessage(ByteBuf buf) {
//            long async_model_start_time = System.currentTimeMillis();
        version = buf.readIntLE();
        msgid = buf.readIntLE();
        srcid = buf.readIntLE();
        dstid = buf.readIntLE();
//            long async_model_end_time = System.currentTimeMillis();
        /** 这里打印日志耗时严重。。。 **/
//            if (LOGGER.isDebugEnabled())
//                LOGGER.debug(toString());
//            long async_model_print_end_time = System.currentTimeMillis();
//            if (LOGGER.isDebugEnabled() && async_model_print_end_time - async_model_start_time > 10)
//                LOGGER.debug("解析异步框架总耗时【{}】ms，解析耗时【{}】ms， 打印耗时【{}】ms",
//                        async_model_print_end_time - async_model_start_time,
//                        async_model_end_time - async_model_start_time,
//                        async_model_print_end_time - async_model_end_time);
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getMsgid() {
        return msgid;
    }

    public void setMsgid(int msgid) {
        this.msgid = msgid;
    }

    public int getSrcid() {
        return srcid;
    }

    public void setSrcid(int srcid) {
        this.srcid = srcid;
    }

    public int getDstid() {
        return dstid;
    }

    public void setDstid(int dstid) {
        this.dstid = dstid;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("version", version)
                .add("msgid", msgid)
                .add("srcid", srcid)
                .add("dstid", dstid)
                .toString();
    }
}
