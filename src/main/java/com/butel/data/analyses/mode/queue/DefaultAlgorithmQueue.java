package com.butel.data.analyses.mode.queue;

import com.butel.data.analyses.mode.protocol.DataPackage;

/**
 * Created by ninghf on 2018/3/7.
 */
public class DefaultAlgorithmQueue extends AbstractQueue<DataPackage> {

    public DefaultAlgorithmQueue(int queueCount) {
        super(queueCount, "计算");
    }
}
