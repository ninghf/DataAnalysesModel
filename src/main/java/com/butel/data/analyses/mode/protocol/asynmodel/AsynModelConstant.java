package com.butel.data.analyses.mode.protocol.asynmodel;

/**
 * Created by ninghf on 2018/3/7.
 */
public interface AsynModelConstant {

    int udp_version = 1;
    int udp_checksum = 0;
    int udp_reliable_flag = 1;
    int udp_unreliable_flag = 0;
    int udp_ack_flag = 1;
    int udp_data_or_padding_flag = 0;

    int msgid_1118 = 1118;
    int msgid_1118_resp_1112 = 1112;

    int msgid_2000 = 2000;
    int msgid_2000_resp_2001 = 2001;
}
