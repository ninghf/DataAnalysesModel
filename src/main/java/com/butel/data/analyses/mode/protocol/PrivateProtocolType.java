package com.butel.data.analyses.mode.protocol;

/**
 * Created by ninghf on 2018/1/24.
 */
public enum PrivateProtocolType {

    DEFAULT("默认", 3),ASYNC_MODEL("异步模型", 1);

    private String name;
    private int version;

    PrivateProtocolType(String name, int version) {
        this.name = name;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public int getVersion() {
        return version;
    }
}
