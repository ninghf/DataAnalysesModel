/*===================================================================
 * 北京红云融通技术有限公司
 * 日期：2017年4月28日 下午5:02:59
 * 作者：ninghf
 * 版本：1.0.0
 * 版权：All rights reserved.
 *===================================================================
 * 修订日期           修订人               描述
 * 2017年4月28日     ninghf      创建
 */
package com.butel.data.analyses.mode.protocol.asynmodel;

import java.util.*;

import com.butel.data.analyses.mode.stat.DefaultStat;
import com.butel.data.analyses.mode.stat.IStat;
import com.google.common.primitives.UnsignedInts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Distinct implements Runnable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Distinct.class);

    private IStat stat = new DefaultStat("去重缓存淤积", 1000, true);

	private int bucket_array = 10;
	private Bucket[] buckets = new Bucket[bucket_array];

    public Distinct() {
        init();
        new Thread(this).start();
    }

    public void init() {
		for (int i = 0; i < bucket_array; i++) {
            buckets[i] = new Bucket();
		}
		LOGGER.info("当前[去重]桶的总数[{}]个!!!", bucket_array);
    }
	
	private Bucket getBucket(String ip, int port, int sn) {
        String sn_ip_port = sn + ":" + ip + ":" + port;
		return buckets[Math.abs(sn_ip_port.hashCode()%10)];
	}
	
	public void add(String ip, int port, int sn) {
		Bucket bucket = getBucket(ip, port, sn);
		bucket.add(ip, port, sn);
	}
	
	public boolean contains(String ip, int port, int sn) {
        Bucket bucket = getBucket(ip, port, sn);
		return bucket.contains(ip, port, sn);
	}
	
	public void clear() {
		int bucket_array = 10;
		for (int i = 0; i < bucket_array; i++) {
			Bucket bucket = buckets[i];
			bucket.clear();
		}
	}

	@Override
	public void run() {
        long wait_time = 1000;
		while (true) {
            try {
//                synchronized (this) {
//                    wait(wait_time);
//                }
                Thread.sleep(wait_time);
                clear();
            } catch (Exception e) {
                LOGGER.error("去重清空桶异常：", e);
            }
		}
	}

	class Bucket {

		private SortedMap<Integer, Structure> sortedMap = new TreeMap<>();

        private int sector = 100;
        private ArrayList<Long> sns = new ArrayList<>(2*sector + 2);

        public Bucket() {
            for (long i = 0; i <= sector; i++) {
                sns.add(i);
            }
            long max = UnsignedInts.toLong(-1);
            for (long i = max - sector; i <= max ; i++) {
                sns.add(i);
            }
        }

        private Structure getBucketInner(String ip, int port) {
            String ip_port = ip + ":" + port;
            int key = ip_port.hashCode();
            Structure structure = sortedMap.get(key);
            if (structure == null) {
                structure = new Structure();
                sortedMap.put(key, structure);
            }
            return structure;
		}

		public void add(String ip, int port, int sn) {
            Structure bucketInner = getBucketInner(ip, port);
            bucketInner.add(sn);
		}

        public boolean contains(String ip, int port, int sn) {
            Structure bucketInner = getBucketInner(ip, port);
            return bucketInner.contains(sn);
        }

		public void clear() {
            Iterator<Map.Entry<Integer, Structure>> it = sortedMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Integer, Structure> entry = it.next();
                Structure structure = entry.getValue();
                if (structure.isValid()) structure.clear();
                else it.remove();
            }
		}

        class Structure {
            private int T = 500;
            private TreeMap<Long, Long> treeMap = new TreeMap<>();
//            private volatile long sn;
            private volatile long lastTime;

            public void add(int sn) {
                long snL = UnsignedInts.toLong(sn);
                long cur_time = System.currentTimeMillis();
                if (cur_time > lastTime) lastTime = cur_time;
                if (treeMap.size() >= T && !sns.contains(snL)) {
                    synchronized (treeMap) {
                        Map.Entry<Long, Long> firstEntry = treeMap.pollFirstEntry();
                        if (firstEntry != null) stat.delCnt();
                        treeMap.put(snL, cur_time);
                        stat.addCnt();
                    }
                } else {
                    synchronized (treeMap) {
                        treeMap.put(snL, cur_time);
                        stat.addCnt();
                    }
                }

            }

            public boolean contains(int sn) {
                return treeMap.containsKey(UnsignedInts.toLong(sn));
            }

            public boolean isValid() {
                long cur_time = System.currentTimeMillis();
                if (cur_time - lastTime >= 30*60*1000) return false;
                return true;
            }

            public void clear() {
                Map.Entry<Long, Long> firstEntry = treeMap.firstEntry();
                Map.Entry<Long, Long> lastEntry = treeMap.lastEntry();
                long cur_time = System.currentTimeMillis();
                if ((firstEntry != null && sns.contains(firstEntry.getKey())) || (lastEntry != null && sns.contains(lastEntry.getKey()))) {
                    doClearByTraverse(cur_time);
                } else {
                    if (firstEntry != null && cur_time - firstEntry.getValue() < 1200) return;
                    doClearByTraverse(cur_time);
                }
            }

            public void doClearByTraverse(long cur_time) {
                boolean is_succ = false;
                while (!is_succ) {
                    synchronized (treeMap) {
                        int count = 0;
                        Iterator<Map.Entry<Long, Long>> it = treeMap.entrySet().iterator();
                        if (!it.hasNext())
                            is_succ = true;
                        while (it.hasNext() && count < 100) {
                            Map.Entry<Long, Long> entry = it.next();
                            Long time = entry.getValue();
                            if (cur_time - time > 1200) {
                                it.remove();
                                stat.delCnt();
                                count++;
                            } else {
                                is_succ = true;
                                break;
                            }
                        }
                    }

                }
            }
        }
	}

    public static void main(String[] args) {
        System.out.println(UnsignedInts.toLong(-1));
//        System.out.println(UnsignedInteger.MAX_VALUE);
//        System.out.println(UnsignedInts.toLong(Integer.MAX_VALUE));
//        System.out.println(UnsignedInts.toLong(Integer.MIN_VALUE));
//
//        int sector = 100;
//        ArrayList<Long> sns = new ArrayList<>(2*sector + 2);
//        for (long i = 0; i <= sector; i++) {
//            sns.add(i);
//        }
//        long max = UnsignedInts.toLong(-1);
//        for (long i = max - sector; i <= max ; i++) {
//            sns.add(i);
//        }
//        System.out.println(sns.size());
//        sns.forEach(aLong -> System.out.println(aLong));
    }

}
