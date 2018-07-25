package com.butel.data.analyses.mode.stat;

/**
 * Created by ninghf on 2018/2/4.
 */
public interface IStat {

    String name();
    void reset();
    void status();
    void addCnt();
    void add(long delta);
    void delCnt();
    void stat();
}
