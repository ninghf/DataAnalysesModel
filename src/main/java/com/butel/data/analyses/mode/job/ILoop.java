package com.butel.data.analyses.mode.job;

import java.util.Queue;

/**
 * Created by ninghf on 2018/2/4.
 */
public interface ILoop<T> {

    void bind();
    void doBind(Queue<T> queue);
}
