package com.pandav.pdvserver.pdvaccess;

public class UserData {
    private long lastActiveMs; // 用于清理过期 session
    private String p2pkAddress;
    private boolean gateTxReady = false; // 门禁交易是否准备好
    private String lastTxId; // ✅ 存储最近交易 ID

    public UserData() {
        setActiveNow();
    }

    public long getLastActiveMs() {
        return lastActiveMs;
    }

    public void setActiveNow() {
        this.lastActiveMs = System.currentTimeMillis();
    }

    public String getP2pkAddress() {
        return p2pkAddress;
    }

    public void setP2pkAddress(String p2pkAddress) {
        this.p2pkAddress = p2pkAddress;
    }

    public boolean isGateTxReady() {
        return gateTxReady;
    }

    public void setGateTxReady(boolean gateTxReady) {
        this.gateTxReady = gateTxReady;
    }

    public String getLastTxId() {
        return lastTxId;
    }

    public void setLastTxId(String lastTxId) {
        this.lastTxId = lastTxId;
    }

    @Override
    public String toString() {
        return "UserData{" +
                "lastActiveMs=" + lastActiveMs +
                ", p2pkAddress='" + p2pkAddress + '\'' +
                ", gateTxReady=" + gateTxReady +
                ", lastTxId='" + lastTxId + '\'' +
                '}';
    }
}
