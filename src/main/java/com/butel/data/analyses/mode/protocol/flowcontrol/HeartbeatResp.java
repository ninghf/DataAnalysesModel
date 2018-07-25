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
public class HeartbeatResp {

    private long packetSize;
    private int rate;
    private int heartbeatRate;
    private String mark;

    public long getPacketSize() {
        return packetSize;
    }

    public void setPacketSize(long packetSize) {
        this.packetSize = packetSize;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public int getHeartbeatRate() {
        return heartbeatRate;
    }

    public void setHeartbeatRate(int heartbeatRate) {
        this.heartbeatRate = heartbeatRate;
    }

    public static HeartbeatResp decode(ByteBuf buf) {
        HeartbeatResp resp = new HeartbeatResp();
        if (buf.readShortLE() != 3)
            return null;
        if (buf.readShortLE() != 2)
            return null;
        resp.setPacketSize(buf.readLongLE());
        resp.setRate(buf.readIntLE());
        resp.setHeartbeatRate(buf.readIntLE());
        int len = buf.readShortLE();
        if (len != buf.readableBytes())
            return null;
        resp.setMark(buf.readCharSequence(len, CharsetUtil.UTF_8).toString());
        return resp;
    }

    public ByteBuf encode() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeLongLE(packetSize);
        buf.writeIntLE(rate);
        buf.writeIntLE(heartbeatRate);
        byte[] marks = mark.getBytes(CharsetUtil.UTF_8);
        buf.writeShortLE(marks.length);
        buf.writeBytes(marks);
        return buf;
    }

    @Override
    public String toString() {
        return "HeartbeatResp{" +
                "packetSize=" + packetSize +
                ", rate=" + rate +
                ", mark='" + mark + '\'' +
                '}';
    }
}
