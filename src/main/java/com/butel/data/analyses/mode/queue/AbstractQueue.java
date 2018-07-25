/*===================================================================
 * 北京红云融通技术有限公司
 * 日期：2017年7月30日 上午10:46:14
 * 作者：ninghf
 * 版本：1.0.0
 * 版权：All rights reserved.
 *===================================================================
 * 修订日期           修订人               描述
 * 2017年7月30日     ninghf      创建
 */
package com.butel.data.analyses.mode.queue;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import com.butel.data.analyses.mode.job.ILoop;
import com.butel.data.analyses.mode.stat.DefaultStat;
import com.butel.data.analyses.mode.stat.IStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractQueue<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractQueue.class);
	
	protected int queueCount;
	protected Map<Integer, Queue<T>> map = new HashMap<Integer, Queue<T>>();
    protected IStat queue_stat;

	public AbstractQueue(int queueCount, String name) {
		LOGGER.info("[{}] 初始化 [{}] 个队列.", this.getClass().getSimpleName(), queueCount);
		this.queueCount = queueCount;
        this.queue_stat = new DefaultStat(name + "队列淤积", 1000, true);
		for (int i = 0; i < queueCount; i++) {
			Queue<T> queue = new ConcurrentLinkedQueue<T>();
			map.put(i, queue);
		}
	}

    private AtomicInteger offer_order = new AtomicInteger(1);
    public void offer(T t) {
        Queue<T> queue = map.get(next(offer_order, queueCount));
        queue_stat.addCnt();
        queue.offer(t);
    }

    private AtomicInteger bind_order = new AtomicInteger(1);
    public synchronized void bind(ILoop loop) {
        Queue<T> queue = map.get(next(bind_order, queueCount));
        loop.doBind(queue);
    }

    public static int next(AtomicInteger atomic, int limit) {
        int sn = atomic.getAndIncrement();
        if (sn >= limit) {
            sn = 0;
            atomic.set(1);
        }
        return sn;
    }

    public IStat stat() {
        return queue_stat;
    }
}
