package com.vendor.po;

import javax.persistence.*;
import java.sql.Timestamp;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;
/**
 * 设备监控-硬件详情
 */
@Entity
@Table(name = "T_DEVICE_MONITORING_DETAILS")
@JsonFilter("com.vendor.po.DeviceMonitoringDetails")
public class DeviceMonitoringDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    /**
     * 设备组号
     */
    @Column(name = "FACTORY_DEV_NO")
    private Long factoryDevNo;

    /**
     * 压缩机状态
     */
    @Column(name = "COMPRESSOR_STATE")
    private Integer compressorState;

    /**
     * 节能时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "COMPRESSOR_TIME")
    private Timestamp compressorTime;

    /**
     * 灯光状态
     */
    @Column(name = "LIGHT_STATE")
    private Integer lightState;

    /**
     * 灯光时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "LIGHT_TIME")
    private Timestamp lightTime;

    /**
     * 所属机构ID
     */
    @Column(name = "ORG_ID")
    private Long orgId;

    /**
     * 货柜ID
     */
    @Column(name = "CABINET_ID")
    private Long cabinetId;

    @Transient
    private String typeStr;

    @Transient
    private String cabinetNo;

    @Transient
    private String model;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFactoryDevNo() {
        return factoryDevNo;
    }

    public void setFactoryDevNo(Long factoryDevNo) {
        this.factoryDevNo = factoryDevNo;
    }

    public Integer getCompressorState() {
        return compressorState;
    }

    public void setCompressorState(Integer compressorState) {
        this.compressorState = compressorState;
    }

    public Timestamp getCompressorTime() {
        return compressorTime;
    }

    public void setCompressorTime(Timestamp compressorTime) {
        this.compressorTime = compressorTime;
    }

    public Integer getLightState() {
        return lightState;
    }

    public void setLightState(Integer lightState) {
        this.lightState = lightState;
    }

    public Timestamp getLightTime() {
        return lightTime;
    }

    public void setLightTime(Timestamp lightTime) {
        this.lightTime = lightTime;
    }

    public Long getOrgId() {
        return orgId;
    }

    public String getTypeStr() {
        return typeStr;
    }

    public void setTypeStr(String typeStr) {
        this.typeStr = typeStr;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getModel() {
        return model;
    }

    public Long getCabinetId() {
        return cabinetId;
    }

    public void setCabinetId(Long cabinetId) {
        this.cabinetId = cabinetId;
    }

    public String getCabinetNo() {
        return cabinetNo;
    }

    public void setCabinetNo(String cabinetNo) {
        this.cabinetNo = cabinetNo;
    }
}
