package com.butel.data.analyses.mode.protocol.flowcontrol;

import java.util.List;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/6/5
 * @description 从控制服务拉取配置信息；cmdid: 30002
 */
public class ControlReq {

    private short m_version;// 协议版本
    private int cfg_num;//	请求的配置文件总个数
    private List<Conf> confs;
    private int req_node_info_num;// 请求携带的用户信息条数
    private List<Req> reqs;

    class Conf {
        private int len;// cfg name长度（不包含“\0”）
        private String cfg_name;// cfg name内容
    }

    class Req {
        private int name_len;// 用户信息的类别字符串的长度信息
        private int content_len;// 用户信息的内容长度信息
        private String name;// 用户信息的类别
        private String content;// 用户信息的内容
    }
}
