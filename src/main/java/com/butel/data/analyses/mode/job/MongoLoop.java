package com.butel.data.analyses.mode.job;

import com.butel.data.analyses.mode.queue.AbstractQueue;
import com.butel.data.analyses.mode.data.MongoAPI;
import com.butel.data.analyses.mode.data.MongoData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;

/**
 * Created by ninghf on 2017/12/14.
 */
public class MongoLoop implements Runnable, ILoop<MongoData> {

    public static final Logger LOGGER = LoggerFactory.getLogger(MongoLoop.class);

    private MongoAPI mongoAPI;
    private AbstractQueue<MongoData> queue;

    public MongoLoop(MongoAPI mongoAPI, AbstractQueue<MongoData> queue) {
        this.mongoAPI = mongoAPI;
        this.queue = queue;
        bind();
    }

    private Queue<MongoData> mongoDataQueue;

    @Override
    public void bind() {
        queue.bind(this);
    }

    @Override
    public void doBind(Queue<MongoData> queue) {
        this.mongoDataQueue = queue;
    }

    @Override
    public void run() {
        while (true) {
            MongoData data;
            try {
                data = mongoDataQueue.poll();
                if (data == null) {
//                    synchronized (this) {
//                        wait(1, 1000);
//                    }
                    Thread.sleep(1, 1000);
                    continue;
                }
                queue.stat().delCnt();
                data.insertMany(mongoAPI);
            } catch (Exception e) {
                LOGGER.error("入库任务异常...", e);
            }
        }
    }
}
