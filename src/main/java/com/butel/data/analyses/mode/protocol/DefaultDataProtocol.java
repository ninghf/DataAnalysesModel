package com.butel.data.analyses.mode.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ninghf on 2017/12/24.
 */
public class DefaultDataProtocol extends DataProtocol {

    public static final Logger LOGGER = LoggerFactory.getLogger(DefaultDataProtocol.class);
    public static final int sendTime_length;

    static {
        sendTime_length = 8;
    }

    public DefaultDataProtocol(Integer version, Integer length, ProtocolType type, PrivateProtocolType privateProtocolType) {
        super(version, length, type, privateProtocolType);
    }

    @Override
    public UserData parseUserData(ByteBuf buf, Channel resp, InetSocketAddress sender, long recvTime, int ack_flag, int reliable_flag) {
        int version = buf.readUnsignedShortLE();
        int length = buf.readIntLE();
        long sendTime = buf.readLongLE();
//        LOGGER.info("version={},length={},sendTime={}", version, length, sendTime);
        ByteBuf src = Unpooled.buffer(length - sendTime_length);
        buf.readBytes(src);
        boolean isContinue = true;
        List<ByteBuf> bufs = new ArrayList<ByteBuf>();
        while (src.readableBytes() > 0 && isContinue) {
//            int len = src.readUnsignedShort();
            int len = src.readUnsignedShortLE();
            if (src.readableBytes() >= len) {
                ByteBuf dst = Unpooled.buffer(len);
                src.readBytes(dst, len);
                bufs.add(dst);
            } else {
                isContinue = false;
            }
        }
        UserData userData = new UserData(bufs, null, sendTime);
        return userData;
    }
}
