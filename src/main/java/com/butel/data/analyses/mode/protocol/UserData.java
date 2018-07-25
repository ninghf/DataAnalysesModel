package com.butel.data.analyses.mode.protocol;

import io.netty.buffer.ByteBuf;

import java.util.List;

/**
 * Created by ninghf on 2017/12/20.
 */
public class UserData {

    /**
     * 用户数据类型ByteBuf
     */
    private List<ByteBuf> bufs;
    /**
     * 用户数据类型String
     */
    private List<String> logs;
    private long sendTime;

    public UserData(List<ByteBuf> bufs, List<String> logs, long sendTime) {
        if (bufs != null) {
            this.bufs = bufs;
        }
        if (logs != null) {
            this.logs = logs;
        }
        this.sendTime = sendTime;
    }

    public List<ByteBuf> getBufs() {
        return bufs;
    }

    public List<String> getLogs() {
        return logs;
    }

    public long getSendTime() {
        return sendTime;
    }
}
