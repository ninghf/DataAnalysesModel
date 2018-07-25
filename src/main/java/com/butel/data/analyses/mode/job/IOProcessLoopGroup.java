package com.butel.data.analyses.mode.job;

import com.butel.data.analyses.mode.protocol.DataPackage;
import com.butel.data.analyses.mode.protocol.IServer;
import com.butel.data.analyses.mode.queue.AbstractQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by ninghf on 2018/3/7.
 */
public class IOProcessLoopGroup implements IServer {

    public static final Logger LOGGER = LoggerFactory.getLogger(IOProcessLoopGroup.class);

    private int ioProcessLoopCount;
    private AbstractQueue<DataPackage> ioProcessQueue;
    private AbstractQueue<DataPackage> algorithmQueue;

    private ExecutorService group;

    public IOProcessLoopGroup(int ioProcessLoopCount, AbstractQueue<DataPackage> ioProcessQueue, AbstractQueue<DataPackage> algorithmQueue) {
        this.ioProcessLoopCount = ioProcessLoopCount;
        this.ioProcessQueue = ioProcessQueue;
        this.algorithmQueue = algorithmQueue;
    }

    @Override
    public void start() throws Exception {
        if (group == null) {
            group = Executors.newFixedThreadPool(ioProcessLoopCount);
        }
        for (int i = 0; i < ioProcessLoopCount; i++) {
            group.execute(new IOProcessLoop(ioProcessQueue, algorithmQueue));
        }
        LOGGER.info("开始循环计算...");
    }

    @Override
    public void shutdown() {
        if (group == null) {
            throw new NullPointerException("IOProcessLoopGroup.group is Null");
        }
        LOGGER.info("IO_ACK处理开始关闭...");
        group.shutdown();
        LOGGER.info("IO_ACK处理成功关闭...");
    }
}
