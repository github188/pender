package com.vendor.po;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

/**
 * 设备监控数据
 */
@Entity
@Table(name = "T_DEVICE_MONITORING")
@JsonFilter("com.vendor.po.DeviceMonitoring")
public class DeviceMonitoring {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    /**
     * 设备组号
     */
    @Column(name = "FACTORY_DEV_NO")
    private String factoryDevNo;

    /**
     * 占用内存
     */
    @Column(name = "MEMORY")
    private Long memory;

    /**
     * 软件版本号
     */
    @Column(name = "SOFTWARE_VERSION")
    private String softwareVersion;

    /**
     * 固件版本号
     */
    @Column(name = "FIRMWARE_VERSION")
    private String firmwareVersion;

    /**
     * 所属机构ID
     */
    @Column(name = "ORG_ID")
    private Long orgId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "CREATE_TIME")
    private Timestamp createTime;

    /**
     * 修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "UPDATE_TIME")
    private Timestamp updateTime;

    /**
     * 当前网络类型(0:wifi,1:4g,2:3g,3:2g,4:网线)
     */
    @Column(name = "NETWORK_TYPE")
    private Integer netWorkType;

    @Column(name = "CPU_RATE")
    private Double cpuRate;                 //CPU使用率

    @Column(name = "ROM_RATE")
    private Double romRate;                 //内存使用率

    @Column(name = "INTERNAL_SD_TOTAL")
    private Long internalSDTotal;         //机身存储总量

    @Column(name = "INTERNAL_SD_LEFT")
    private Long internalSDLeft;          //机身-sd卡余量

    @Column(name = "EXTERNAL_SD_TOTAL")
    private Long externalSDTotal;        //外部-SD卡总存储量

    @Column(name = "EXTERNAL_SD_LEFT")
    private Long externalSDLeft;          //外部-sd卡余量

    @Transient
    private List<DeviceMonitoringDetails> deviceMonitoringDetails;

    @Transient
    private List<DeviceMonitoringNetwork> deviceMonitoringNetworks;

    @Transient
    private String images;

    /**
     * 下载地址
     */
    @Transient
    private String downloadUrl;

    /**
     * 设备号
     */
    @Transient
    private String devNo;

    /**
     * 生产厂家
     */
    @Transient
    private String manufacturer;

    /**
     * 点位ID
     */
    @Transient
    private Long pointId;

    /**
     * 异常信息ID
     */
    @Transient
    private Long exceptionId;

    /**
     * 设备状态
     */
    @Transient
    private Integer deviceStatus;

    /**
     * 点位名称
     */
    @Transient
    private String pointName;

    /**
     * 点位地址
     */
    @Transient
    private String pointAddress;

    /**
     * 丢包率异常设备数量
     */
    @Transient
    private Integer packetLossRateNumber;

    /**
     * CPU负载率异常设备数量
     */
    @Transient
    private Integer cpuRateNumber;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFactoryDevNo() {
        return factoryDevNo;
    }

    public void setFactoryDevNo(String factoryDevNo) {
        this.factoryDevNo = factoryDevNo;
    }

    public Long getMemory() {
        return memory;
    }

    public void setMemory(Long memory) {
        this.memory = memory;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getNetWorkType() {
        return netWorkType;
    }

    public void setNetWorkType(Integer netWorkType) {
        this.netWorkType = netWorkType;
    }

    public List<DeviceMonitoringDetails> getDeviceMonitoringDetails() {
        return deviceMonitoringDetails;
    }

    public void setDeviceMonitoringDetails(List<DeviceMonitoringDetails> deviceMonitoringDetails) {
        this.deviceMonitoringDetails = deviceMonitoringDetails;
    }

    public List<DeviceMonitoringNetwork> getDeviceMonitoringNetworks() {
        return deviceMonitoringNetworks;
    }

    public void setDeviceMonitoringNetworks(List<DeviceMonitoringNetwork> deviceMonitoringNetworks) {
        this.deviceMonitoringNetworks = deviceMonitoringNetworks;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public Double getCpuRate() {
        return cpuRate;
    }

    public void setCpuRate(Double cpuRate) {
        this.cpuRate = cpuRate;
    }

    public Double getRomRate() {
        return romRate;
    }

    public void setRomRate(Double romRate) {
        this.romRate = romRate;
    }

    public Long getInternalSDTotal() {
        return internalSDTotal;
    }

    public void setInternalSDTotal(Long internalSDTotal) {
        this.internalSDTotal = internalSDTotal;
    }

    public Long getInternalSDLeft() {
        return internalSDLeft;
    }

    public void setInternalSDLeft(Long internalSDLeft) {
        this.internalSDLeft = internalSDLeft;
    }

    public Long getExternalSDTotal() {
        return externalSDTotal;
    }

    public void setExternalSDTotal(Long externalSDTotal) {
        this.externalSDTotal = externalSDTotal;
    }

    public Long getExternalSDLeft() {
        return externalSDLeft;
    }

    public void setExternalSDLeft(Long externalSDLeft) {
        this.externalSDLeft = externalSDLeft;
    }

    public String getDevNo() {
        return devNo;
    }

    public void setDevNo(String devNo) {
        this.devNo = devNo;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Long getPointId() {
        return pointId;
    }

    public void setPointId(Long pointId) {
        this.pointId = pointId;
    }

    public Integer getDeviceStatus() {
        return deviceStatus;
    }

    public void setDeviceStatus(Integer deviceStatus) {
        this.deviceStatus = deviceStatus;
    }

    public String getPointName() {
        return pointName;
    }

    public void setPointName(String pointName) {
        this.pointName = pointName;
    }

    public String getPointAddress() {
        return pointAddress;
    }

    public void setPointAddress(String pointAddress) {
        this.pointAddress = pointAddress;
    }

    public Integer getPacketLossRateNumber() {
        return packetLossRateNumber;
    }

    public void setPacketLossRateNumber(Integer packetLossRateNumber) {
        this.packetLossRateNumber = packetLossRateNumber;
    }

    public Integer getCpuRateNumber() {
        return cpuRateNumber;
    }

    public void setCpuRateNumber(Integer cpuRateNumber) {
        this.cpuRateNumber = cpuRateNumber;
    }

    public Long getExceptionId() {
        return exceptionId;
    }

    public void setExceptionId(Long exceptionId) {
        this.exceptionId = exceptionId;
    }
}
