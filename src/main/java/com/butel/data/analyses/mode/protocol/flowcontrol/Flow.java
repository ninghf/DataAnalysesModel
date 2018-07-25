package com.butel.data.analyses.mode.protocol.flowcontrol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/5/15
 * @description
 * 1) 从控制端获取当前日志收集（logAnalyses）的带宽限制（maxBandWidth）
 *    和共用区域与预留区域的比例（ratio）的配置信息
 * 2) 分配可用带宽
 */
public class Flow {

    private static final Logger logger = LoggerFactory.getLogger(Flow.class);

    private long maxBandWidth;// 每秒最大传输字节
    private int ratio;// 共用区域和预留区域比列；默认值：60
    private long max;// 传输包大小默认设置：max和min两个值；
    private long min;// 传输包大小默认设置：max和min两个值；
    private int rate;// 每秒传输包个数
    private long sharedSize;
    private long reservedSize;
    private long usedShared;// 共用区域已使用
    private long usedReserved;// 预留区域已使用
    private boolean allocationShared;// 是否还有可分配流量
    private boolean allocationReserved;// 是否还有可分配流量

    public Flow(long maxBandWidth, int ratio, long max, long min, int rate) {
        this.maxBandWidth = maxBandWidth;
        this.ratio = ratio;
        this.max = max;
        this.min = min;
        this.rate = rate;
        divided();
    }

    private void divided() {
        sharedSize = maxBandWidth * 1024 * ratio / 100;
        reservedSize = maxBandWidth * 1024 * (100 - ratio) / 100;
        allocationShared = true;
        allocationReserved = true;
    }

    public boolean isAllocationShared() {
        return allocationShared;
    }

    public boolean isAllocationReserved() {
        return allocationReserved;
    }

    /**
     * 传输包大小 * 传输速率 * 用户数 = 空闲流量
     * 流量上限 >= 空闲流量 + 使用流量
     * @param waitForAlloc
     * @return
     */
    public void allocBandWidth(List<User> waitForAlloc, int priority, HashMap<String, User> workers) {
        if (priority > 0) {
            if (isAllocationReserved()) {
                allocShared(waitForAlloc, workers);
                if (!waitForAlloc.isEmpty()) {
                    long freeSize = reservedSize - usedReserved;
                    if (freeSize >= min) {
                        long used = alloc(waitForAlloc, freeSize, workers, false);
                        usedReserved += used;
                        if (freeSize - used < min) allocationReserved = false;
                    }
                }
            }
        } else {
            allocShared(waitForAlloc, workers);
        }
    }

    /**
     * 公共区域分配算法
     * @param waitForAlloc
     * @param workers
     */
    private void allocShared(List<User> waitForAlloc, HashMap<String, User> workers) {
        if (isAllocationShared()) {
            long freeSize = sharedSize - usedShared;
            if (freeSize >= min) {
                long used = alloc(waitForAlloc, freeSize, workers, true);
                usedShared += used;
                if (freeSize - used < min) allocationShared = false;
            }
        }
    }

    /**
     * 计算本次分配带宽大小，并从等待队列中去除已分配的客户端到已分配表中
     * @param waitForAlloc
     * @param freeSize
     * @param workers
     * @param isShared
     * @return
     */
    private long alloc(List<User> waitForAlloc, long freeSize, HashMap<String, User> workers, boolean isShared) {
        long used = 0L;
        int size = waitForAlloc.size();
        int expectCount = expectCount(freeSize, size);
        if (expectCount > 0) {
            long packetSize = freeSize/rate/expectCount;
            if (packetSize > max) {
                packetSize = max;
                expectCount = (int)(freeSize/rate/packetSize);
            } else if (packetSize < min) {
                packetSize = 0;
                expectCount = 0;
            }
            Iterator <User> it = waitForAlloc.iterator();
            while (it.hasNext() && expectCount > 0 && packetSize > 0) {
                User user = it.next();
                user.setShared(isShared);
                user.setPacketSize(packetSize);
                user.setRate(rate);
                workers.put(user.getUserID(), user);
                if (logger.isInfoEnabled())
                    logger.info("用户【{}】本次分配包大小【{}】bytes具体信息：{}", user.getUserID(), user.getPacketSize(), user);
                it.remove();
                expectCount--;
                used += packetSize * rate;
            }
        }
        return used;
    }

    /**
     * 迭代算出当前周期最大的分配客户端数
     * @param freeSize
     * @param size
     * @return
     */
    private int expectCount(long freeSize, int size) {
        int expectCount = 0;
        long packetSize = freeSize/rate/size;
        if (packetSize > max) {
            expectCount = (int)(freeSize/rate/max);
        } else if (packetSize < min) {
            if (freeSize >= min && size > 1)
                expectCount = expectCount(freeSize, size - 1);
        } else {
            expectCount = size;
        }
        return expectCount;
    }

    /**
     * 释放已分配带宽：
     * 1) 共用区域与预留区域分别还原到各自的区域；
     * @param waitForFree
     */
    public void freeBandWidth(List<User> waitForFree) {
        Iterator <User> it = waitForFree.iterator();
        while (it.hasNext()) {
            User user = it.next();
            if (Objects.nonNull(user)) {
                if (user.getPriority() > 0 && !user.isShared()) {
                    usedReserved -= user.getPacketSize() * rate;
                } else {
                    usedShared -= user.getPacketSize() * rate;
                }
            }
            it.remove();
        }
        if (sharedSize - usedShared >= min) allocationShared = true;
        if (reservedSize - usedReserved >= min) allocationReserved = true;
        logger.info("释放内存后：{}", this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Flow) {
            Flow flow = (Flow) obj;
            if (flow.maxBandWidth == this.maxBandWidth && flow.ratio == this.ratio
                    && flow.max == this.max && flow.min == this.min
                    && flow.rate == this.rate)
                return true;
        }
        return false;
    }

    public long getMaxBandWidth() {
        return maxBandWidth;
    }

    public int getRatio() {
        return ratio;
    }

    public long getMax() {
        return max;
    }

    public long getMin() {
        return min;
    }

    public int getRate() {
        return rate;
    }

    public long getSharedSize() {
        return sharedSize;
    }

    public long getReservedSize() {
        return reservedSize;
    }

    public long getUsedShared() {
        return usedShared;
    }

    public long getUsedReserved() {
        return usedReserved;
    }

    @Override
    public String toString() {
        return "Flow{" +
                "maxBandWidth=" + maxBandWidth +
                ", ratio=" + ratio +
                ", max=" + max +
                ", min=" + min +
                ", rate=" + rate +
                ", sharedSize=" + sharedSize +
                ", reservedSize=" + reservedSize +
                ", usedShared=" + usedShared +
                ", usedReserved=" + usedReserved +
                ", allocationShared=" + allocationShared +
                ", allocationReserved=" + allocationReserved +
                '}';
    }
}
