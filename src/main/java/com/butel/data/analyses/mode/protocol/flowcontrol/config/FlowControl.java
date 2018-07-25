package com.butel.data.analyses.mode.protocol.flowcontrol.config;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/6/7
 * @description TODO
 */
public class FlowControl {

    private int maxBandWidth;
    private int ratio;
    private int max;
    private int min;
    private int rate;
    private byte strategy;
    private byte period;
    private int heartbeatRate;

    public int getMaxBandWidth() {
        return maxBandWidth;
    }

    public void setMaxBandWidth(int maxBandWidth) {
        this.maxBandWidth = maxBandWidth;
    }

    public int getRatio() {
        return ratio;
    }

    public void setRatio(int ratio) {
        this.ratio = ratio;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public byte getStrategy() {
        return strategy;
    }

    public void setStrategy(byte strategy) {
        this.strategy = strategy;
    }

    public byte getPeriod() {
        return period;
    }

    public void setPeriod(byte period) {
        this.period = period;
    }

    public int getHeartbeatRate() {
        return heartbeatRate;
    }

    public void setHeartbeatRate(int heartbeatRate) {
        this.heartbeatRate = heartbeatRate;
    }
}
