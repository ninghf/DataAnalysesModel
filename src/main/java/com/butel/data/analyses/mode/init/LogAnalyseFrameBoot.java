package com.butel.data.analyses.mode.init;

import com.butel.data.analyses.mode.data.MongoAPI;
import com.butel.data.analyses.mode.job.AlgorithmLoop;
import com.butel.data.analyses.mode.job.IOProcessLoopGroup;
import com.butel.data.analyses.mode.protocol.flowcontrol.Control;
import com.butel.data.analyses.mode.protocol.flowcontrol.WhiteList;
import com.butel.data.analyses.mode.protocol.flowcontrol.config.DataAnalysesConfig;
import com.butel.data.analyses.mode.protocol.flowcontrol.config.FlowControl;
import com.butel.data.analyses.mode.protocol.flowcontrol.config.SDN;
import com.butel.data.analyses.mode.protocol.http.HttpChildChannelInitializer;
import com.butel.data.analyses.mode.protocol.tcp.TcpChildChannelInitializer;
import com.butel.data.analyses.mode.protocol.tcp.TcpServer;
import com.butel.data.analyses.mode.protocol.udp.UdpChannelInitializer;
import com.butel.data.analyses.mode.protocol.udp.UdpServer;
import com.butel.data.analyses.mode.queue.AbstractQueue;
import com.butel.data.analyses.mode.queue.DefaultAlgorithmQueue;
import com.butel.data.analyses.mode.queue.DefaultDataPackQueue;
import com.butel.data.analyses.mode.queue.DefaultMongoQueue;
import com.butel.data.analyses.mode.data.MongoData;
import com.butel.data.analyses.mode.data.MongoWriter;
import com.butel.data.analyses.mode.job.AlgorithmLoopGroup;
import com.butel.data.analyses.mode.job.MongoLoopGroup;
import com.butel.data.analyses.mode.protocol.DataPackage;
import com.butel.data.analyses.mode.protocol.IServer;
import com.butel.data.analyses.mode.protocol.http.HttpServer;
import com.butel.data.analyses.mode.util.XmlUtils;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.SystemPropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ninghf on 2017/12/14.
 * 日志收集分析框架
 */
public final class LogAnalyseFrameBoot {

    public static final Logger LOGGER = LoggerFactory.getLogger(LogAnalyseFrameBoot.class);

    private List<IServer> servers;
    private AbstractQueue<DataPackage> ioProcessQueue;
    private AbstractQueue<DataPackage> algorithmQueue;
    private AbstractQueue<MongoData> mongoQueue;
    private MongoAPI mongoAPI;
    private MongoWriter mongoWriter;

    /**
     *
     * @param ioProcessQueueCount IO处理队列个数 3
     * @param algorithmQueueCount 计算队列个数 5
     * @param mongoQueueCount MongoDB 队列个数 5
     * @param replicaSet MongoDB url
     */
    public LogAnalyseFrameBoot(int ioProcessQueueCount, int algorithmQueueCount, int mongoQueueCount, String replicaSet) {
        this.showOSAndJVMInfo();
        this.servers = new ArrayList<IServer>();
        this.ioProcessQueue = new DefaultDataPackQueue(ioProcessQueueCount);
        this.algorithmQueue = new DefaultAlgorithmQueue(algorithmQueueCount);
        this.mongoQueue = new DefaultMongoQueue(mongoQueueCount);
        this.mongoWriter = new MongoWriter(mongoQueue);
        this.mongoAPI = new MongoAPI(replicaSet);
    }

    public void shutdown() {
        for (int i = 0; i < servers.size(); i++) {
            IServer server = servers.get(i);
            server.shutdown();
        }
        LOGGER.info("开始关闭统计...");
        mongoAPI.stop();
    }

    public void check(int ioProcessQueueCount, int ioProcessLoopCount, int algorithmQueueCount, int algorithmLoopCount, int mongoQueueCount, int mongoLoopCount) {
        if (ioProcessQueueCount > ioProcessLoopCount)
            throw new IndexOutOfBoundsException("ioProcessQueueCount is out of ioProcessLoopCount!");
        if (algorithmQueueCount > algorithmLoopCount)
            throw new IndexOutOfBoundsException("algorithmQueueCount is out of algorithmLoopCount!");
        if (mongoQueueCount > mongoLoopCount)
            throw new IndexOutOfBoundsException("mongoQueueCount is out of mongoLoopCount!");
    }

    public void initTcpServer(String host, int port) throws Exception {
        TcpChildChannelInitializer channelInitializer = new TcpChildChannelInitializer(ioProcessQueue);
        TcpServer server = new TcpServer(host, port, channelInitializer);
        server.start();
        servers.add(server);
    }

    /**
     *
     * @param host
     * @param cPort 与控制服务通信的端口
     * @throws Exception
     */
    public void initTcpServerOfFlowControl(String host, int cPort) throws Exception {
        TcpChildChannelInitializer channelInitializer = new TcpChildChannelInitializer(ioProcessQueue);
        TcpServer server = new TcpServer(channelInitializer);
        try {
            //TODO 这里如何排除控制服务不在线
//            initUdpServerOfFlowControl(host, cPort, true, server);
            throw new NullPointerException("测试从本地读取配置文件");
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("控制服务不在线..., 准备读取本地配置文件!!!");
            // 控制服务不存在时，读取本地配置文件
            DataAnalysesConfig dataAnalysesConfig = loadDataAnalysesConfig();
            SDN sdn = dataAnalysesConfig.getSdns().get(0);
            FlowControl flowControl = sdn.getFlowControl();
            Control control = new Control();
            control.createFlow(flowControl.getMaxBandWidth(), flowControl.getRatio(), flowControl.getMax(), flowControl.getMin(), flowControl.getRate());
            channelInitializer.setControl(control);
            WhiteList whiteList = new WhiteList();
            whiteList.init(sdn.getWhiteList().getDataAnalysesClients(), flowControl);
            channelInitializer.setWhiteList(whiteList);
            String[] address = sdn.getAddress().getMaster().trim().split(":");
            server.bind(address[0], Integer.parseInt(address[1]));
        }
        servers.add(server);
    }

    public DataAnalysesConfig loadDataAnalysesConfig() throws IOException {
        String conf = SystemPropertyUtil.get("conf");
        if (StringUtil.isNullOrEmpty(conf))
            throw new NullPointerException("请配置本地文件路径【conf】");
        StringBuilder path = new StringBuilder();
        path.append(conf).append(SystemPropertyUtil.get("file.separator")).append("DataAnalysesConfig.xml");
        Path file = Paths.get(path.toString());
        if (!Files.exists(file))
            throw new NullPointerException("请配置本地文件【DataAnalysesConfig.xml】");
        byte[] bytes = Files.readAllBytes(file);
        DataAnalysesConfig dataAnalysesConfig = new XmlUtils<DataAnalysesConfig>().xmlToBean(new String(bytes, CharsetUtil.UTF_8), DataAnalysesConfig.class);
        return dataAnalysesConfig;
    }

    public void initUdpServer(String host, int port) throws Exception {
        initUdpServerOfFlowControl(host, port, false, null);
    }

    public void initUdpServerOfFlowControl(String host, int port, boolean flowControl, TcpServer tcpServer) throws Exception {
        UdpChannelInitializer channelInitializer = null;
        if (flowControl) {
            channelInitializer = new UdpChannelInitializer(null);
            channelInitializer.setServer(tcpServer);
        } else {
            channelInitializer = new UdpChannelInitializer(ioProcessQueue);
        }
        channelInitializer.setFlowControl(flowControl);
        UdpServer server = new UdpServer(host, port, channelInitializer);
        server.start();
        servers.add(server);
    }

    public void initHttpServer(String host, int port) throws Exception {
        HttpChildChannelInitializer channelInitializer = new HttpChildChannelInitializer();
        HttpServer server = new HttpServer(host, port, channelInitializer);
        server.start();
        servers.add(server);
    }

    /**
     *
     * @param ioProcessLoopCount IO_ACK线程数
     * @throws Exception
     */
    public void initIOProcessLoopGroup(int ioProcessLoopCount) throws Exception {
        IOProcessLoopGroup group = new IOProcessLoopGroup(ioProcessLoopCount, ioProcessQueue, algorithmQueue);
        group.start();
        servers.add(group);
    }

    /**
     *
     * @param algorithmLoopCount 计算线程数
     * @throws Exception
     */
    public void initAlgorithmLoopGroup(int algorithmLoopCount, Class<? extends AlgorithmLoop> clazz, String dbName) throws Exception {
        List<AlgorithmLoop> algorithmLoops = new ArrayList<AlgorithmLoop>();
        for (int i = 0; i < algorithmLoopCount; i++) {
            AlgorithmLoop algorithmLoop = clazz.getConstructor(AbstractQueue.class, MongoWriter.class, String.class)
                    .newInstance(algorithmQueue, mongoWriter, dbName);
            algorithmLoops.add(algorithmLoop);
        }
        AlgorithmLoopGroup group = new AlgorithmLoopGroup(algorithmLoopCount, algorithmLoops);
        group.start();
        servers.add(group);
    }

    /**
     *
     * @param mongoLoopCount 入库线程数
     * @throws Exception
     */
    public void initMongoLoopGroup(int mongoLoopCount) throws Exception {
        MongoLoopGroup group = new MongoLoopGroup(mongoLoopCount, mongoAPI, mongoQueue);
        group.start();
        servers.add(group);
    }

    public void showOSAndJVMInfo() {
        Runtime r = Runtime.getRuntime();
        LOGGER.info("当前系统用户名:【{}】", SystemPropertyUtil.get("user.name"));
        LOGGER.info("操作系统的名称:【{}】", SystemPropertyUtil.get("os.name"));
        LOGGER.info("操作系统的构架:【{}】", SystemPropertyUtil.get("os.arch"));
        LOGGER.info("操作系统的版本:【{}】", SystemPropertyUtil.get("os.version"));
        LOGGER.info("用户的当前工作目录:【{}】", SystemPropertyUtil.get("user.dir"));
        LOGGER.info("文件分隔符:【{}】", SystemPropertyUtil.get("file.separator"));
        LOGGER.info("Java的运行环境版本:【{}】", SystemPropertyUtil.get("java.version"));
        LOGGER.info("JVM可以使用的总内存:【{}】M", r.totalMemory() / 1024 / 1024);
        LOGGER.info("JVM可以使用的剩余内存:【{}】M", r.freeMemory() / 1024 / 1024);
        LOGGER.info("JVM可以使用的处理器个数:【{}】个", r.availableProcessors());
    }
}
