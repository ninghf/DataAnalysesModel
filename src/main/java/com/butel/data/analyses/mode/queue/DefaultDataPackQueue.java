package com.butel.data.analyses.mode.queue;

import com.butel.data.analyses.mode.protocol.DataPackage;

/**
 * Created by ninghf on 2017/12/14.
 */
public class DefaultDataPackQueue extends AbstractQueue<DataPackage> {

    public DefaultDataPackQueue(int queueCount) {
        super(queueCount, "IO_ACK处理");
    }
}
