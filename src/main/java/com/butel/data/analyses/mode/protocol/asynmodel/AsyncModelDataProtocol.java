package com.butel.data.analyses.mode.protocol.asynmodel;

import com.butel.data.analyses.mode.protocol.*;
import com.butel.data.analyses.mode.stat.DefaultStat;
import com.butel.data.analyses.mode.stat.IStat;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;

import static com.butel.data.analyses.mode.protocol.asynmodel.AsynModelConstant.*;

/**
 * Created by ninghf on 2018/1/23.
 */
public class AsyncModelDataProtocol extends DataProtocol {


    public AsyncModelDataProtocol(Integer version, Integer length, ProtocolType type, PrivateProtocolType privateProtocolType) {
        super(version, length, type, privateProtocolType);
    }

    private static IStat udp_stat = new DefaultStat("UDP数据包总接收", 1000);
    private static IStat udp_reliable_stat = new DefaultStat("UDP可靠数据包接收", 1000);
    private static IStat udp_unreliable_stat = new DefaultStat("UDP不可靠数据包接收", 1000);
    private static IStat udp_ack_recv_stat = new DefaultStat("UDP_ACK数据包接收", 1000);

    private static IStat distinct_packets_stat = new DefaultStat("重复包", 1000);
    private static IStat udp_resp_stat = new DefaultStat("UDP_RESP数据包发送", 1000);

    private static Distinct distinct_packets = new Distinct();
    private static RespCache respCache = new RespCache();

    private static IStat Zlib_before_decompress_stat = new DefaultStat("Zlib解压缩前包大小", 1000);
    private static IStat Zlib_last_decompress_stat = new DefaultStat("Zlib解压缩后包大小", 1000);
    private static IStat user_data_stat = new DefaultStat("用户数据条数", 1000);

    /**
     * 异步模型
     * <table border="1" cellspacing="0" cellpadding="6">
     * <tr>
     * <th>Name</th><th>Bytes</th><th>description</th>
     * </tr><tr>
     * <td>version</td><td>4</td><td>异步模型版本号</td>
     * </tr><tr>
     * <td>msgid</td><td>4</td><td>消息ID</td>
     * </tr><tr>
     * <td>srcid</td><td>4</td><td>来源ID</td>
     * </tr><tr>
     * <td>dstid</td><td>4</td><td>目的ID</td>
     * </tr>
     * </table>
     * 异步模型-TCP头（6）
     * <table border="1" cellspacing="0" cellpadding="6">
     * <tr>
     * <th>Name</th><th>Bytes</th><th>description</th>
     * </tr><tr>
     * <td>version</td><td>2</td><td>异步模型TCP头版本号</td>
     * </tr><tr>
     * <td>length</td><td>4</td><td>消息长度（不包含TCP头自身长度（6）即数据体长度）</td>
     * </tr>
     * </table>
     * 异步模型-可靠UDP头|ACK（12）
     * <table border="1" cellspacing="0" cellpadding="6">
     * <tr>
     * <th>Name</th><th>Bytes</th><th>description</th>
     * </tr><tr>
     * <td>version</td><td>2</td><td>异步模型UDP头版本号</td>
     * </tr><tr>
     * <td>length</td><td>2</td><td>消息长度（UDP头长度+数据体长度）</td>
     * </tr><tr>
     * <td>checksum</td><td>2</td><td>检测（默认：0）</td>
     * </tr><tr>
     * <td>reliable_flag</td><td>1</td><td>可靠/不可靠UDP头标记位（可靠：1；不可靠：0）</td>
     * </tr><tr>
     * <td>ackpacket_flag</td><td>1</td><td>ACK（ACK：1；数据包：0）</td>
     * </tr><tr>
     * <td>sn</td><td>4</td><td>数据包序号</td>
     * </tr>
     * </table>
     * 异步模型-不可靠UDP头（8）
     * <table border="1" cellspacing="0" cellpadding="6">
     * <tr>
     * <th>Name</th><th>Bytes</th><th>description</th>
     * </tr><tr>
     * <td>version</td><td>2</td><td>异步模型UDP头版本号</td>
     * </tr><tr>
     * <td>length</td><td>2</td><td>消息长度（UDP头长度+数据体长度）</td>
     * </tr><tr>
     * <td>checksum</td><td>2</td><td>检测（默认：0）</td>
     * </tr><tr>
     * <td>reliable_flag</td><td>1</td><td>可靠/不可靠UDP头标记位（可靠：1；不可靠：0）</td>
     * </tr><tr>
     * <td>padding</td><td>1</td><td>填充</td>
     * </tr>
     * </table>
     * @param buf
     * @param resp
     * @param sender
     * @param recvTime
     * @param ack_flag
     * @param reliable_flag
     * @return
     */
    @Override
    public UserData parseUserData(ByteBuf buf, Channel resp, InetSocketAddress sender, long recvTime, int ack_flag, int reliable_flag) {
        UserData userData = null;
        if(ProtocolType.TCP == type) {

        }
        if (ProtocolType.UDP == type) {
            long analysis_userData_start_time = System.currentTimeMillis();
            udp_stat.addCnt();
            /** ACK 数据包丢弃 **/
            if(ack_flag == udp_ack_flag) {
                int sn = buf.readIntLE();
                respCache.remove(sn);
                udp_ack_recv_stat.addCnt();
                return userData;
            }
            /** 可靠UDP处理 **/
            if(reliable_flag == udp_reliable_flag) {
                udp_reliable_stat.addCnt();
                int sn = buf.readIntLE();
                /** 去重机制-开始 **/
//                String ip_port = sender.getAddress() + ":" + sender.getPort();
//                String sn_ip_port = sn + ":" + ip_port;
                // 线程安全
                if (distinct_packets.contains(sender.getAddress().toString(), sender.getPort(), sn)) {
                    distinct_packets_stat.addCnt();
                    return userData;
                }
                distinct_packets.add(sender.getAddress().toString(), sender.getPort(), sn);
                /** 去重机制-结束 **/
                long analysis_userData_distinct_end_time = System.currentTimeMillis();
//                AsyncModel asyncModel = new AsyncModel();
                AsyncModel asyncModel = AsyncModel.newInstance();
                asyncModel.byteBufToMessage(buf);
                long analysis_asyn_model_end_time = System.currentTimeMillis();
                if (asyncModel.getMsgid() == msgid_1118) {
                    long analysis_asyn_model_message_end_time = System.currentTimeMillis();
                    resp(resp, sender, asyncModel);
                    long resp_send_end_time = System.currentTimeMillis();
//                    Msg_1118 msg_1118 = new Msg_1118();
                    Msg_1118 msg_1118 = Msg_1118.newInstance();
                    msg_1118.byteBufToMessage(buf);
                    long analysis_userData_unpack_end_time = System.currentTimeMillis();
                    userData = new UserData(null, msg_1118.getLogs(), msg_1118.getSendTime());
                    msg_1118.release();
                    long analysis_userData_packaging_end_time = System.currentTimeMillis();
                    if (LOGGER.isDebugEnabled() && analysis_userData_packaging_end_time - analysis_userData_start_time > 150)
                        LOGGER.debug("解析用户数据总耗时【{}】ms，去重机制耗时【{}】ms，" +
                                        "解析异步框架耗时【{}】ms，解析异步框架具体消息耗时【{}】ms，用户数据响应耗时【{}】ms，" +
                                        "[Msg_2000]数据包解压缩耗时【{}】ms，封装用户数据耗时【{}】ms",
                                analysis_userData_packaging_end_time - analysis_userData_start_time,
                                analysis_userData_distinct_end_time - analysis_userData_start_time,
                                analysis_asyn_model_end_time - analysis_userData_distinct_end_time,
                                analysis_asyn_model_message_end_time - analysis_asyn_model_end_time,
                                resp_send_end_time - analysis_asyn_model_message_end_time,
                                analysis_userData_unpack_end_time - resp_send_end_time,
                                analysis_userData_packaging_end_time - analysis_userData_unpack_end_time);
                }
                if (asyncModel.getMsgid() == msgid_2000) {
                    long analysis_asyn_model_message_end_time = System.currentTimeMillis();
                    resp(resp, sender, asyncModel);
                    long resp_send_end_time = System.currentTimeMillis();
                    Msg_2000 msg_2000 = Msg_2000.newInstance();
                    msg_2000.byteBufToMessage(buf, Zlib_before_decompress_stat, Zlib_last_decompress_stat, user_data_stat);
                    long analysis_userData_unpack_end_time = System.currentTimeMillis();
                    userData = new UserData(msg_2000.getBufs(), null, msg_2000.getSendTime());
                    msg_2000.release();
                    long analysis_userData_packaging_end_time = System.currentTimeMillis();
                    if (LOGGER.isDebugEnabled() && analysis_userData_packaging_end_time - analysis_userData_start_time > 150)
                        LOGGER.debug("解析用户数据总耗时【{}】ms，去重机制耗时【{}】ms，" +
                                        "解析异步框架耗时【{}】ms，解析异步框架具体消息耗时【{}】ms，用户数据响应耗时【{}】ms，" +
                                        "[Msg_2000]数据包解压缩耗时【{}】ms，封装用户数据耗时【{}】ms",
                                analysis_userData_packaging_end_time - analysis_userData_start_time,
                                analysis_userData_distinct_end_time - analysis_userData_start_time,
                                analysis_asyn_model_end_time - analysis_userData_distinct_end_time,
                                analysis_asyn_model_message_end_time - analysis_asyn_model_end_time,
                                resp_send_end_time - analysis_asyn_model_message_end_time,
                                analysis_userData_unpack_end_time - resp_send_end_time,
                                analysis_userData_packaging_end_time - analysis_userData_unpack_end_time);
                }
            }
            /** 不可靠UDP处理 **/
            if(reliable_flag == udp_unreliable_flag) {
                udp_unreliable_stat.addCnt();
                int padding = buf.readByte();
                /**
                 * 这里暂时不做校验
                 */
//                if (padding != udp_data_or_padding_flag) {
//                    return userData;
//                }
//                AsyncModel asyncModel = new AsyncModel();
                AsyncModel asyncModel = AsyncModel.newInstance();
                asyncModel.byteBufToMessage(buf);
                if (asyncModel.getMsgid() == msgid_1118) {
//                    Msg_1118 msg_1118 = new Msg_1118();
                    Msg_1118 msg_1118 = Msg_1118.newInstance();
                    resp(resp, sender, asyncModel);
                    msg_1118.byteBufToMessage(buf);
                    userData = new UserData(null, msg_1118.getLogs(), msg_1118.getSendTime());
                    msg_1118.release();
                }
                if (asyncModel.getMsgid() == msgid_2000) {
                    Msg_2000 msg_2000 = Msg_2000.newInstance();
                    resp(resp, sender, asyncModel);
                    msg_2000.byteBufToMessage(buf, Zlib_before_decompress_stat, Zlib_last_decompress_stat, user_data_stat);
                    userData = new UserData(msg_2000.getBufs(), null, msg_2000.getSendTime());
                    msg_2000.release();
                }
            }
        }
        return userData;
    }

    public void resp(Channel channel_resp, InetSocketAddress sender, AsyncModel asyncModel) {
        udp_resp_stat.addCnt();
//        Resp resp_obj = new Resp(channel_resp, sender, asyncModel);
        Resp resp_obj = Resp.newInstance();
        resp_obj.build(channel_resp, sender, asyncModel);
        respCache.put(resp_obj.getSn(), resp_obj);
        resp_obj.sendResp();
    }

}
