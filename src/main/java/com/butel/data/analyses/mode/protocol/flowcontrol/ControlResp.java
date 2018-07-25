package com.butel.data.analyses.mode.protocol.flowcontrol;

import java.util.List;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/6/5
 * @description TODO
 */
public class ControlResp {

    private short m_version;// 协议版本
    private int m_cfg_num;// 返回的配置文件个数
    private int name_len;//	配置文件名字长度
    private long config_len;// 配置文件内容长度
    private List<Conf> confs;

    class Conf {
        private String name;// 配置文件名字
        private String config;// 配置文件内容
    }

}
