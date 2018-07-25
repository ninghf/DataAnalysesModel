package com.butel.data.analyses.mode.job;

import com.butel.data.analyses.mode.protocol.DataProtocol;
import com.butel.data.analyses.mode.protocol.UserData;
import com.butel.data.analyses.mode.queue.AbstractQueue;
import com.butel.data.analyses.mode.data.MongoData;
import com.butel.data.analyses.mode.data.MongoWriter;
import com.butel.data.analyses.mode.protocol.DataPackage;
import com.butel.data.analyses.mode.stat.DefaultStat;
import com.butel.data.analyses.mode.stat.IStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;

/**
 * Created by ninghf on 2017/12/13.
 */
public abstract class AlgorithmLoop implements IAlgorithm, Runnable, ILoop<DataPackage> {

    public static final Logger LOGGER = LoggerFactory.getLogger(AlgorithmLoop.class);

    private final AbstractQueue<DataPackage> queue;
    private final MongoWriter mongoWriter;

    private static IStat stat = new DefaultStat("数据包接收耗时大于100", 1000);

    public AlgorithmLoop(AbstractQueue<DataPackage> queue, MongoWriter mongoWriter) {
        this.queue = queue;
        this.mongoWriter = mongoWriter;
        bind();
    }

    private Queue<DataPackage> dataQueue;

    @Override
    public void bind() {
        queue.bind(this);
    }

    @Override
    public void doBind(Queue<DataPackage> queue) {
        this.dataQueue = queue;
    }

    @Override
    public void run() {
        while (true) {
            DataPackage data = null;
            try {
                data = dataQueue.poll();
                if (data == null) {
//                    long wait_start = System.currentTimeMillis();
//                    synchronized (this) {
//                        wait(1);
//                    }
                    Thread.sleep(1, 1000);
//                    long wait_end = System.currentTimeMillis();
//                    if (LOGGER.isDebugEnabled() && wait_end - wait_start > 5)
//                        LOGGER.debug("解析算法【TIMED_WAITING】耗时【{}】ms", wait_end > wait_start ? wait_end - wait_start : -1);
                    continue;
                }
                queue.stat().delCnt();
                long analysis_start_time = System.currentTimeMillis();
                DataProtocol protocol = data.parseDataProtocol();
                long analysis_protocol_end_time = System.currentTimeMillis();
                if (protocol != null) {
                    UserData userData = data.parseUserData();
                    long analysis_userData_end_time = System.currentTimeMillis();
                    if (userData != null) {
                        if (data.getRecvTime() > userData.getSendTime()) {
                            if (data.getRecvTime() - userData.getSendTime() > 100) {
                                stat.addCnt();
                            }
                        }

                        MongoData mongoData = byteBufToMessage(data);
                        long analysis_message_end_time = System.currentTimeMillis();
                        if (mongoData != null) {
                            mongoWriter.write(mongoData);
                            long mongo_writer_end_time = System.currentTimeMillis();
                            if (LOGGER.isDebugEnabled() && mongo_writer_end_time - analysis_protocol_end_time > 200)
                                LOGGER.debug("解析算法总耗时【{}】ms，解析协议类型耗时【{}】ms，解析用户数据耗时【{}】ms，解析具体日志耗时【{}】ms，解析完毕放入待入库队列耗时【{}】ms",
                                        mongo_writer_end_time - analysis_protocol_end_time,
                                        analysis_protocol_end_time - analysis_start_time,
                                        analysis_userData_end_time - analysis_protocol_end_time,
                                        analysis_message_end_time - analysis_userData_end_time,
                                        mongo_writer_end_time - analysis_message_end_time);
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error("计算任务异常...", e);
            } finally {
                if (data != null) {
                    data.release();
                }
            }
        }
    }
}
