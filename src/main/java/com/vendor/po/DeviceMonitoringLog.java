package com.vendor.po;

import com.fasterxml.jackson.annotation.JsonFilter;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * 设备监控日志与文件关联
 *
 * @auther duanyx
 * @create 2017/3/31 17:40
 */
@Entity
@Table(name = "T_DEVICE_MONITORING_LOG")
@JsonFilter("com.vendor.po.DeviceMonitoringLog")
public class DeviceMonitoringLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Column(name = "CREATE_TIME")
    private Timestamp createTime;

    @Column(name = "MONITORING_ID")
    private Long monitoringId;

    @Column(name = "LOG_TYPE")
    private Integer logType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Long getMonitoringId() {
        return monitoringId;
    }

    public void setMonitoringId(Long monitoringId) {
        this.monitoringId = monitoringId;
    }

    public Integer getLogType() {
        return logType;
    }

    public void setLogType(Integer logType) {
        this.logType = logType;
    }
}
