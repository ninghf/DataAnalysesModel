package com.butel.data.analyses.mode.protocol.flowcontrol;

import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/5/15
 * @description TODO
 */
public class Control {

    private static final Logger logger = LoggerFactory.getLogger(Control.class);

    // 定时任务
    private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    private final HashMap<Integer, List<User>> waitForAlloc = new HashMap <>();// 排队等候分配带宽的客户端队列（优先级为key）
    public final HashMap<String, User> workers = new HashMap <>();// 已经分配带宽的客户端表（索引：客户端ID）
    private final List<User> waitForFree = new ArrayList <>();// 客户端日志已经上传完毕日志等待带宽回收

    private Flow flow;

    public Control() {
        doTask();
    }

    public void doTask() {
        service.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (Objects.nonNull(flow)) {
                    if (!waitForFree.isEmpty())
                        flow.freeBandWidth(waitForFree);
                    for (int i = 9; i >= 0; i--) {
                        List <User> users = waitForAlloc.get(i);
                        if (Objects.nonNull(users) && !users.isEmpty())
                            flow.allocBandWidth(users, i, workers);
                    }
                }
            }
        }, 1, 30, TimeUnit.SECONDS);
    }

    public void createFlow(long maxBandWidth, int ratio, long max, long min, int rate) {
        Flow flow = new Flow(maxBandWidth, ratio, max, min, rate);
        if (Objects.isNull(this.flow)) {
            this.flow = flow;
            return;
        }
        if (Objects.nonNull(this.flow) && !flow.equals(this.flow))
            this.flow = flow;
    }

    public void waitForAlloc(User user) {
        if (workers.containsKey(user.getUserID())) return;
        if (!waitForAlloc.containsKey(user.getPriority())) waitForAlloc.put(user.getPriority(), new ArrayList <>());
        if (!waitForAlloc.get(user.getPriority()).contains(user))
            waitForAlloc.get(user.getPriority()).add(user);
    }

    public void free(User user) {
        if (logger.isInfoEnabled())
            logger.info("客户端：{}上传完毕, 准备释放使用带宽", user);
        waitForFree.add(workers.remove(user.getUserID()));
    }

    public void free(String connID) {
        if (StringUtil.isNullOrEmpty(connID)) return;
        Iterator <Map.Entry <String, User>> it = workers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry <String, User> entry = it.next();
            User user = entry.getValue();
            if (connID.equals(user.getConnID())) {
                if (logger.isInfoEnabled())
                    logger.info("客户端：{}异常断开, 准备释放使用带宽", user);
                waitForFree.add(user);
                it.remove();
                break;
            }
        }
    }

    public User alloc(String userID) {
        return workers.get(userID);
    }

    public static void main(String[] args) throws InterruptedException {
        Control control = new Control();
        long maxBandWidth = 10240;
        int ratio = 50;
        long max = 2048;
        long min = 1024;
        int rate = 1024;
        control.createFlow(maxBandWidth, ratio, max, min, rate);
        List<User> users = new ArrayList <>();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            users.add(new User(Integer.toString(i), random.nextInt(10),
                    "127.0.0.1", 50025, "", 1000));
        }
        for (int i = 0; i < users.size(); i++) {
            control.waitForAlloc(users.get(i));
        }
        Thread.sleep(5 * 1000);
        for (int i = 0; i < users.size(); i++) {
            control.free(users.get(i));
        }
        System.out.println("==================================");
    }
}
