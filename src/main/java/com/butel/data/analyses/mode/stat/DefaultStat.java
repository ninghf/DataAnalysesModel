package com.butel.data.analyses.mode.stat;

/**
 * Created by ninghf on 2017/11/23.
 */
public class DefaultStat extends AbstractStat {

    public DefaultStat() {
        this(null);
    }

    public DefaultStat(String name) {
        this(name, 1000);
    }

    public DefaultStat(String name, long period) {
        super(name, 1000, false);
        start();
    }

    public DefaultStat(String name, long period, boolean is_accumulation) {
        super(name, 1000, is_accumulation);
        start();
    }

    public void start() {
        service.execute(this);
    }
}
