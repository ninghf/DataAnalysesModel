package com.butel.data.analyses.mode.util;

import io.netty.util.Recycler;

import java.util.zip.Inflater;

/**
 * Created by ninghf on 2018/3/13.
 */
public class PooledInflater {

    private Inflater inflater;

    private final Recycler.Handle<PooledInflater> recyclerHandle;
    private static final Recycler<PooledInflater> RECYCLER = new Recycler<PooledInflater>() {
        @Override
        protected PooledInflater newObject(Handle<PooledInflater> handle) {
            return new PooledInflater(handle);
        }
    };

    private PooledInflater(Recycler.Handle<PooledInflater> handle) {
        this.recyclerHandle = handle;
        if (inflater == null)
            this.inflater = new Inflater();
    }

    public static PooledInflater newInstance() {
        return RECYCLER.get();
    }

    public void release() {
        inflater.reset();
        recyclerHandle.recycle(this);
    }

    public Inflater getInflater() {
        return inflater;
    }
}
