package com.vendor.po;

import java.sql.Timestamp;

/**
 * 主题皮肤推送
 */
public class ChangeThemeStateData {

    private String notifyFlag;// 通知标识，用于区分表示本次推送是商品状态变更后的推送。
    private Timestamp time;// 随机时间戳
    private Long messageId;// 消息ID

    private ChangeThemeData changeThemeData;

    private String version = "16";// 版本号

    public String getNotifyFlag() {
        return notifyFlag;
    }

    public void setNotifyFlag(String notifyFlag) {
        this.notifyFlag = notifyFlag;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public ChangeThemeData getChangeThemeData() {
        return changeThemeData;
    }

    public void setChangeThemeData(ChangeThemeData changeThemeData) {
        this.changeThemeData = changeThemeData;
    }
}
