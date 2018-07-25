package com.butel.data.analyses.mode.protocol.flowcontrol.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.List;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/6/7
 * @description TODO
 */
public class WhiteList {

    @XmlElement(name = "DataAnalysesClient")
    private List<DataAnalysesClient> dataAnalysesClients;

    @XmlTransient
    public List <DataAnalysesClient> getDataAnalysesClients() {
        return dataAnalysesClients;
    }

    public void setDataAnalysesClients(List <DataAnalysesClient> dataAnalysesClients) {
        this.dataAnalysesClients = dataAnalysesClients;
    }
}
