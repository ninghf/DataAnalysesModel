package com.butel.data.analyses.mode.protocol.flowcontrol.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/6/7
 * @description TODO
 */
public class SDN {

    private byte version;
    @XmlElement(name = "Address")
    private Address address;
    @XmlElement(name = "FlowControl")
    private FlowControl flowControl;
    @XmlElement(name = "WhiteList")
    private WhiteList whiteList;

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    @XmlTransient
    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    @XmlTransient
    public FlowControl getFlowControl() {
        return flowControl;
    }

    public void setFlowControl(FlowControl flowControl) {
        this.flowControl = flowControl;
    }

    @XmlTransient
    public WhiteList getWhiteList() {
        return whiteList;
    }

    public void setWhiteList(WhiteList whiteList) {
        this.whiteList = whiteList;
    }
}
