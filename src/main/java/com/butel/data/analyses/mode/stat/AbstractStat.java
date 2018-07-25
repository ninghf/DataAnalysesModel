package com.butel.data.analyses.mode.stat;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by ninghf on 2017/11/23.
 */
public abstract class AbstractStat implements IStat, Runnable {

    public static final Logger LOGGER = LoggerFactory.getLogger(AbstractStat.class);

    private static final String stat_prefix = "stat_";
    private static AtomicLong stat_id = new AtomicLong();
    private final String name;
    private AtomicLong count = new AtomicLong();
    private AtomicLong atomic = new AtomicLong();
    protected static  final ExecutorService service = Executors.newFixedThreadPool(24);
    /**
     * 统计周期（单位：ms）
     */
    private long period;
    /**
     * 本次统计的开始时间
     */
    private volatile long startTime;
    /**
     * 本次统计的结束时间
     */
    private volatile long endTime;

    protected AbstractStat(String name, long period, boolean is_accumulation) {
        if (Strings.isNullOrEmpty(name)) {
            name = stat_prefix + stat_id.getAndIncrement();
        }
        this.name = name;
        this.period = period;
        this.is_accumulation = is_accumulation;
    }

    @Override
    public String name() {
        return name;
    }

    private boolean is_accumulation = false;

    @Override
    public void reset() {
        if (count.get() == Long.MAX_VALUE) count.set(0L);
        count.addAndGet(atomic.get());
        if (!is_accumulation)
            atomic.set(0L);
    }

    @Override
    public synchronized void status() {
        long currentTime = System.currentTimeMillis();
        if (currentTime > endTime) {
            startTime = currentTime;
            endTime = startTime + period;
            stat();
            reset();
        }
    }

    @Override
    public void addCnt() {
        status();
        if (atomic.get() == Long.MAX_VALUE) reset();
        atomic.getAndIncrement();
    }

    @Override
    public void add(long delta) {
        status();
        if (atomic.get() == Long.MAX_VALUE) reset();
        atomic.getAndAdd(delta);
    }

    @Override
    public void delCnt() {
        if (atomic.get() == 0) return;
        atomic.getAndDecrement();
        count.getAndIncrement();
    }

    @Override
    public void stat() {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("本统计周期【{}】s统计项【{}】统计结果【{}】累积值【{}】", period/1000, name, atomic.get(), count);
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(period);
                status();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
