package com.vendor.po;

import java.sql.Timestamp;
import java.util.List;

/**
 * 虚拟商品推送上架对象
 *
 * @author
 * @create 2017-03-02 17:40
 **/
public class ChangeVirtualPointStateData {

    private String notifyFlag;//类型标示
    private Timestamp time;//时间戳
    private Long messageId;//消息ID
    private Integer state;//0 :下线  1：上线

    private String version = "16";// 版本号
    private List<ChangeVirtualPointData> list;


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

    public List<ChangeVirtualPointData> getList() {
        return list;
    }

    public void setList(List<ChangeVirtualPointData> list) {
        this.list = list;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }
}
