package com.butel.data.analyses.mode.data;

import com.butel.data.analyses.mode.queue.AbstractQueue;

/**
 * Created by ninghf on 2017/12/13.
 */
public class MongoWriter implements IMongoWriter {

    private AbstractQueue<MongoData> queue;

    public MongoWriter(AbstractQueue<MongoData> queue) {
        this.queue = queue;
    }

    @Override
    public void write(MongoData data) {
        queue.offer(data);
    }
}
