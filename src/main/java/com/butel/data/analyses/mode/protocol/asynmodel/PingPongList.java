package com.butel.data.analyses.mode.protocol.asynmodel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by ninghf on 2018/3/15.
 */
public class PingPongList<T> {

    private Integer current_list_idx = 0;
    private List[] lists = new List[2];
    private ReentrantReadWriteLock list_idx_lock = new ReentrantReadWriteLock();

    public PingPongList() {
        lists[0] = new ArrayList<>();
        lists[1] = new ArrayList<>();
    }

    public void add(T t) {
        list_idx_lock.readLock().lock();
        synchronized (lists[current_list_idx]) {
            lists[current_list_idx].add(t);
        }
        list_idx_lock.readLock().unlock();
    }

    public void switch_list() {
        list_idx_lock.writeLock().lock();
        current_list_idx = (current_list_idx + 1) % 2;
        list_idx_lock.writeLock().unlock();
    }

    public void dump_list(List<T> out_list) {
        Integer idx = (current_list_idx + 1) % 2;
        out_list.addAll(lists[idx]);
        lists[idx].clear();
    }
}
