package com.butel.data.analyses.mode.protocol.flowcontrol.config;

import com.butel.data.analyses.mode.util.XmlUtils;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/6/7
 * @description TODO
 */
@XmlRootElement(name = "DataAnalyses")
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType
public class DataAnalysesConfig {

    @XmlElement(name = "SDN")
    private List<SDN> sdns;

    @XmlTransient
    public List <SDN> getSdns() {
        return sdns;
    }

    public void setSdns(List <SDN> sdns) {
        this.sdns = sdns;
    }

    public static void main(String[] args) {
        StringBuilder builder = new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<DataAnalyses>\n" +
                "    <!-- 日志收集服务端地址 -->\n" +
                "    <SDN>\n" +
                "        <version>1</version>\n" +
                "        <Address>\n" +
                "            <master>ip:port</master>\n" +
                "            <slaver>ip:port</slaver>\n" +
                "        </Address>\n" +
                "        <FlowControl>\n" +
                "            <!-- 每秒最大传输字节 (单位：KB/s)-->\n" +
                "            <maxBandWidth></maxBandWidth>\n" +
                "            <!-- 共用区域和预留区域比列 (默认值：60) -->\n" +
                "            <ratio></ratio>\n" +
                "            <!-- 传输包大小默认设置：max和min两个值 -->\n" +
                "            <max></max>\n" +
                "            <min></min>\n" +
                "            <!-- 策略 -->\n" +
                "            <strategy></strategy>\n" +
                "            <!-- 时段 -->\n" +
                "            <period></period>\n" +
                "            <!-- 心跳频率 -->\n" +
                "            <heartbeatRate></heartbeatRate>\n" +
                "        </FlowControl>\n" +
                "        <!-- 白名单 -->\n" +
                "        <WhiteList>\n" +
                "            <DataAnalysesClient>\n" +
                "                <id></id>\n" +
                "                <!-- 汇报日志时本地监听端口 -->\n" +
                "                <localAddress></localAddress>\n" +
                "                <!-- 优先级 (0~9) -->\n" +
                "                <priority></priority>\n" +
                "                <!-- 有效日志前缀 -->\n" +
                "                <mark></mark>\n" +
                "            </DataAnalysesClient>\n" +
                "            <DataAnalysesClient>\n" +
                "                <id></id>\n" +
                "                <!-- 汇报日志时本地监听端口 -->\n" +
                "                <localAddress></localAddress>\n" +
                "                <!-- 优先级 (0~9) -->\n" +
                "                <priority></priority>\n" +
                "                <!-- 有效日志前缀 -->\n" +
                "                <mark></mark>\n" +
                "            </DataAnalysesClient>\n" +
                "        </WhiteList>\n" +
                "    </SDN>\n" +
                "</DataAnalyses>");

        DataAnalysesConfig dataAnalysesConfig = new XmlUtils <DataAnalysesConfig>().xmlToBean(builder.toString(), DataAnalysesConfig.class);
        System.out.println(dataAnalysesConfig);
    }
}
