package com.butel.data.analyses.mode.protocol.asynmodel;

import com.butel.data.analyses.mode.stat.DefaultStat;
import com.butel.data.analyses.mode.stat.IStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by ninghf on 2018/3/4.
 */
public class RespCache implements Runnable {

    public static final Logger LOGGER = LoggerFactory.getLogger(RespCache.class);

    private IStat stat = new DefaultStat("Resp缓存淤积", 1000, true);

    private PingPongList<Integer> snPingPongList = new PingPongList<>();
    private PingPongList<Resp> respPingPongList = new PingPongList<>();
    private SortedMap<Integer, Resp> bucket = new TreeMap<>();
    private long last_check_time = System.currentTimeMillis();

    public RespCache() {
        new Thread(this).start();
    }

    public void put(int sn, Resp resp_obj) {
        respPingPongList.add(resp_obj);
        stat.addCnt();
    }

    public void remove(int sn) {
        snPingPongList.add(sn);
        //stat.delCnt();
    }

    public void traversal() {
        List<Integer> ack_list = new ArrayList<>();
        List<Resp> resp_list = new ArrayList<>();

        respPingPongList.switch_list();
        respPingPongList.dump_list(resp_list);

        snPingPongList.switch_list();
        snPingPongList.dump_list(ack_list);

        resp_list.forEach(resp -> bucket.put(resp.getSn(), resp));
        ack_list.forEach(sn -> {bucket.remove(sn);stat.delCnt();});

        if(System.currentTimeMillis() - last_check_time > 200)
        {
            last_check_time = System.currentTimeMillis();
            Iterator<Map.Entry<Integer, Resp>> it= bucket.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Integer, Resp> entry = it.next();
                Resp resp = entry.getValue();
                if (resp.getCount() > 4 || resp.isTimeout()) {
                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug("RESP_SN【{}】发送【{}】次后, 还是失败！", resp.getSn(), resp.getCount());
                    it.remove();
                    resp.release();
                    stat.delCnt();
                } else if (resp.isRepeatSend()) resp.sendResp();
            }
        }
    }

    @Override
    public void run() {
        long period = 50;
        while (true) {
            try {
//                synchronized (this) {
//                    wait(period);
//                }
                Thread.sleep(period);
                long start_time = System.currentTimeMillis();
                traversal();
                long end_time = System.currentTimeMillis();
                if (end_time - start_time > 100)
                    LOGGER.info("遍历RESP缓存一次耗时：【{}】ms", end_time - start_time);
            } catch (Exception e) {
                LOGGER.error("Resp缓存机制异常：", e);
            }
        }
    }

    public static void main(String[] args) {
        SortedSet<Integer> test = new TreeSet<>();
        for (int i = 0; i < 1000000; i++) {
            test.add(i*2);
        }
        int count = 0;
        Iterator<Integer> it = test.iterator();
        while (it.hasNext() && count < 10) {
            int val = it.next();
            System.out.println(val);
            it.remove();
            count++;
        }

        System.out.println();
        long start_time = System.nanoTime();
        SortedSet<Integer> test_sub = test.tailSet(900000);
        long end_time = System.nanoTime();
        System.out.println("==========" + (end_time - start_time));
//        test_sub.forEach(integer -> System.out.println(integer));
//        System.out.println(test.size());
//        Iterator<Integer> it = test.iterator();
//        while (it.hasNext()) {
//            if (it.next() == 2) it.remove();
//        }
//        System.out.println(test.size());
    }
}
