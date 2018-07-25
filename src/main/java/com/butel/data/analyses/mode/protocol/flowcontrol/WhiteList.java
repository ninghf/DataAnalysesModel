package com.butel.data.analyses.mode.protocol.flowcontrol;

import com.butel.data.analyses.mode.protocol.flowcontrol.config.DataAnalysesClient;
import com.butel.data.analyses.mode.protocol.flowcontrol.config.FlowControl;

import java.util.HashMap;
import java.util.List;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/5/21
 * @description TODO
 */
public class WhiteList {

    // key:/ip:port | 从控制服务拉取User列表
    private static final HashMap<String, User> whiteList = new HashMap <>();

    public void init(List <DataAnalysesClient> dataAnalysesClients, FlowControl flowControl) {
        dataAnalysesClients.forEach(dataAnalysesClient -> {
            String[] localAddress = dataAnalysesClient.getLocalAddress().trim().split(":");
            User user = new User(dataAnalysesClient.getId(), dataAnalysesClient.getPriority(),
                    localAddress[0], Integer.parseInt(localAddress[1]), dataAnalysesClient.getMark(), flowControl.getHeartbeatRate());
            whiteList.put("/" + dataAnalysesClient.getLocalAddress(), user);
        });
    }

    public boolean isWhite(String key) {
        return whiteList.containsKey(key);
    }

    public User getUser(String key) {
        return whiteList.get(key);
    }
}
