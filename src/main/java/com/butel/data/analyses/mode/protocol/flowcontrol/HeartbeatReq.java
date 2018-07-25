package com.butel.data.analyses.mode.protocol.flowcontrol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/5/22
 * @description TODO
 */
public class HeartbeatReq {

    private int dataVersion = 3;
    private int dataId = 2;
    private int version = 1;
    private long logSize;
    private int len;
    private String userID;

    public HeartbeatReq() {
    }

    public HeartbeatReq(long logSize) {
        this.logSize = logSize;
    }

    public ByteBuf encode() {
        ByteBuf buf = Unpooled.buffer(12);
        buf.writeShortLE(dataVersion);
        buf.writeShortLE(dataId);
        buf.writeShortLE(version);
        buf.writeIntLE(8 + 2 + len);
        buf.writeLongLE(logSize);
        buf.writeShortLE(len);
        buf.writeBytes(userID.getBytes(CharsetUtil.UTF_8));
        return buf;
    }

    public static HeartbeatReq decode(ByteBuf buf) {
        HeartbeatReq req = new HeartbeatReq();
        buf.skipBytes(4 + 6);
        req.setLogSize(buf.readLongLE());
        int len = buf.readShortLE();
        req.setLen(len);
        req.setUserID(buf.readCharSequence(len, CharsetUtil.UTF_8).toString());
        return req;
    }

    public long getLogSize() {
        return logSize;
    }

    public void setLogSize(long logSize) {
        this.logSize = logSize;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    @Override
    public String toString() {
        return "HeartbeatReq{" +
                "dataVersion=" + dataVersion +
                ", dataId=" + dataId +
                ", version=" + version +
                ", logSize=" + logSize +
                ", len=" + len +
                ", userID='" + userID + '\'' +
                '}';
    }
}
