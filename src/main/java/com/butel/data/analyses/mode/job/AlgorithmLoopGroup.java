package com.butel.data.analyses.mode.job;

import com.butel.data.analyses.mode.protocol.IServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by ninghf on 2017/12/13.
 */

public class AlgorithmLoopGroup implements IServer {

    public static final Logger LOGGER = LoggerFactory.getLogger(AlgorithmLoopGroup.class);

    private int algorithmLoopCount;

    private ExecutorService group;
    private List<AlgorithmLoop> algorithmLoops;

    public AlgorithmLoopGroup(int algorithmLoopCount, List<AlgorithmLoop> algorithmLoops) {
        this.algorithmLoopCount = algorithmLoopCount;
        this.algorithmLoops = algorithmLoops;
    }

    @Override
    public void start() throws Exception {
        if (group == null) {
            group = Executors.newFixedThreadPool(algorithmLoopCount);
        }
        for (int i = 0; i < algorithmLoops.size(); i++) {
            group.execute(algorithmLoops.get(i));
        }
        LOGGER.info("开始循环计算...");
    }

    @Override
    public void shutdown() {
        if (group == null) {
            throw new NullPointerException("AlgorithmLoopGroup.group is Null");
        }
        LOGGER.info("循环计算开始关闭...");
        group.shutdown();
        LOGGER.info("循环计算成功关闭...");
    }
}
