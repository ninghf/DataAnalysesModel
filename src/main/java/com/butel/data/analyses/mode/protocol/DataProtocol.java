package com.butel.data.analyses.mode.protocol;

import com.butel.data.analyses.mode.protocol.asynmodel.AsyncModelDataProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Created by ninghf on 2017/12/20.
 */
public abstract class DataProtocol {

    public static final Logger LOGGER = LoggerFactory.getLogger(DataProtocol.class);

    protected Integer version;
    protected Integer length;

    protected ProtocolType type;
    protected PrivateProtocolType privateProtocolType;

    public DataProtocol(Integer version, Integer length, ProtocolType type, PrivateProtocolType privateProtocolType) {
        this.version = version;
        this.length = length;
        this.type = type;
        this.privateProtocolType = privateProtocolType;
    }

    /**
     * 解析出相应的协议头
     * @param version
     * @param length
     * @param type
     * @return
     */
    public static DataProtocol parseDataProtocol(int version, long length, ProtocolType type) {
        DataProtocol dataProtocol = null;
        switch (version) {
            case 1:
                dataProtocol = new AsyncModelDataProtocol(version, (int)length, type, PrivateProtocolType.ASYNC_MODEL);
                break;
            case 3:
                dataProtocol = new DefaultDataProtocol(version, (int)length, type, PrivateProtocolType.DEFAULT);
                break;
        }
        return dataProtocol;
    }

    public abstract UserData parseUserData(ByteBuf buf, Channel resp, InetSocketAddress sender, long recvTime, int ack_flag, int reliable_flag);
}
