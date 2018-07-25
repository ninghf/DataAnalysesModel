package com.butel.data.analyses.mode.queue;

import com.butel.data.analyses.mode.data.MongoData;

/**
 * Created by ninghf on 2017/12/14.
 */
public class DefaultMongoQueue extends AbstractQueue<MongoData> {

    public DefaultMongoQueue(int queueCount) {
        super(queueCount, "入库");
    }
}
