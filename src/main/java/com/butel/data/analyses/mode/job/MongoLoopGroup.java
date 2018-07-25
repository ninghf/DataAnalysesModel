package com.butel.data.analyses.mode.job;

import com.butel.data.analyses.mode.data.MongoAPI;
import com.butel.data.analyses.mode.data.MongoData;
import com.butel.data.analyses.mode.protocol.IServer;
import com.butel.data.analyses.mode.queue.AbstractQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by ninghf on 2017/12/14.
 */
public class MongoLoopGroup implements IServer {

    public static final Logger LOGGER = LoggerFactory.getLogger(MongoLoopGroup.class);

    private int mongoLoopCount;
    private MongoAPI mongoAPI;
    private AbstractQueue<MongoData> queue;

    private ExecutorService group;

    public MongoLoopGroup(int mongoLoopCount, MongoAPI mongoAPI, AbstractQueue<MongoData> queue) {
        this.mongoLoopCount = mongoLoopCount;
        this.mongoAPI = mongoAPI;
        this.queue = queue;
    }

    @Override
    public void start() throws Exception {
        if (group == null) {
            group = Executors.newFixedThreadPool(mongoLoopCount);
        }
        for (int i = 0; i < mongoLoopCount; i++) {
            group.execute(new MongoLoop(mongoAPI, queue));
        }
        LOGGER.info("开始循环计算...");
    }

    @Override
    public void shutdown() {
        if (group == null) {
            throw new NullPointerException("MongoLoopGroup.group is Null");
        }
        LOGGER.info("循环入库开始关闭...");
        group.shutdown();
        LOGGER.info("循环入库成功关闭...");
    }
}
