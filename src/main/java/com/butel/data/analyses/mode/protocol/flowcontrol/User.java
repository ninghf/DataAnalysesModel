package com.butel.data.analyses.mode.protocol.flowcontrol;

import java.util.Objects;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/5/15
 * @description TODO
 */
public class User implements Comparable<User> {

    private String userID;
    private String connID;
    private int priority;// 0~9（数值越大优先级越高）
    private long packetSize;// 可用带宽大小
    private int rate; // 每秒传输包个数
    private boolean shared;// 是否是共用区域带宽；
    private String ip;
    private int port;
    private String mark;
    private int heartbeatRate;// 心跳频率

    public User(String userID, int priority, String ip, int port, String mark, int heartbeatRate) {
        this.userID = userID;
        this.priority = priority;
        this.ip = ip;
        this.port = port;
        this.mark = mark;
        this.heartbeatRate = heartbeatRate;
    }

    @Override
    public int compareTo(User user) {
        if (this.priority < user.priority)
            return 1;
        else if (this.priority > user.priority)
            return -1;
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userID, user.userID);
    }

    @Override
    public int hashCode() {

        return Objects.hash(userID);
    }

    public String getUserID() {
        return userID;
    }

    public void setConnID(String connID) {
        this.connID = connID;
    }

    public String getConnID() {
        return connID;
    }

    public int getPriority() {
        return priority;
    }

    public long getPacketSize() {
        return packetSize;
    }

    public void setPacketSize(long packetSize) {
        this.packetSize = packetSize;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public boolean isShared() {
        return shared;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }

    public String getMark() {
        return mark;
    }

    public int getHeartbeatRate() {
        return heartbeatRate;
    }

    @Override
    public String toString() {
        return "User{" +
                "userID='" + userID + '\'' +
                ", connID='" + connID + '\'' +
                ", priority=" + priority +
                ", packetSize=" + packetSize +
                ", rate=" + rate +
                ", shared=" + shared +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", mark='" + mark + '\'' +
                ", heartbeatRate=" + heartbeatRate +
                '}';
    }
}
