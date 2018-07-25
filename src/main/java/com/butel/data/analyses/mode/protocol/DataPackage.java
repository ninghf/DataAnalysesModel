package com.butel.data.analyses.mode.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.util.Recycler;
import io.netty.util.ReferenceCountUtil;

import java.net.InetSocketAddress;

public class DataPackage {

	private Object msg;
    private ByteBuf buf;
    private Channel resp;
    private InetSocketAddress sender;
    private long recvTime;
    private ProtocolType type;
    private int version;
    private long length;
    private int ack_flag;
    private int reliable_flag;
    private DataProtocol dataProtocol;
    private UserData userData;

//	public DataPackage(Object msg, Channel resp, ProtocolType type) {
//		this.msg = msg;
//        this.resp = resp;
//        this.type = type;
//        this.recvTime = System.currentTimeMillis();
//	}

    private final Recycler.Handle<DataPackage> recyclerHandle;
    private static final Recycler<DataPackage> RECYCLER = new Recycler<DataPackage>() {
        @Override
        protected DataPackage newObject(Handle<DataPackage> handle) {
            return new DataPackage(handle);
        }
    };

    public DataPackage(Recycler.Handle<DataPackage> handle) {
        this.recyclerHandle = handle;
    }

    public static DataPackage newInstance() {
        return RECYCLER.get();
    }

    public void build(Object msg, Channel resp, ProtocolType type) {
        this.msg = msg;
        this.resp = resp;
        this.type = type;
        this.recvTime = System.currentTimeMillis();
    }

    public void setBuf(ByteBuf buf) {
        this.buf = buf;
    }

    public void setSender(InetSocketAddress sender) {
        this.sender = sender;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public void setAck_flag(int ack_flag) {
        this.ack_flag = ack_flag;
    }

    public void setReliable_flag(int reliable_flag) {
        this.reliable_flag = reliable_flag;
    }

    public Object getMsg() {
        return msg;
    }

    public Channel getResp() {
        return resp;
    }

    public long getRecvTime() {
        return recvTime;
    }

    public ProtocolType getType() {
        return type;
    }

    public UserData getUserData() {
        return userData;
    }

    public DataProtocol parseDataProtocol() {
        return dataProtocol = DataProtocol.parseDataProtocol(version, length, type);
    }

    public UserData parseUserData() {
        return userData = dataProtocol.parseUserData(buf, resp, sender, recvTime, ack_flag, reliable_flag);
    }

    public void release() {
		ReferenceCountUtil.release(msg);
        if (resp != null) {
            resp = null;
        }
        if (sender != null) {
            sender = null;
        }
        if (recvTime > 0) {
            recvTime = 0;
        }
        if (type != null) {
            type = null;
        }
        if (version > 0) {
            version = 0;
        }
        if (length > 0) {
            length = 0;
        }
        if (ack_flag >= 0) {
            ack_flag = -1;
        }
        if (reliable_flag >= 0) {
            reliable_flag = -1;
        }
        if (dataProtocol != null) {
            dataProtocol = null;
        }
        if (userData != null) {
            userData = null;
        }
        recyclerHandle.recycle(this);
    }
}
