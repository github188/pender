package com.vendor.po;

import javax.persistence.*;
import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 设备监控-网咯状态
 */
@Entity
@Table(name = "T_DEVICE_MONITORING_NETWORK")
public class DeviceMonitoringNetwork {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    /** 当前网络类型(0:wifi,1:4g,2:3g,3:2g,4:网线) */
    @Column(name = "NETWORK_TYPE")
    private Integer netWorkType;

    /** 当前下载速度 */
    @Column(name = "DOWNLOAD_SPEED")
    private Integer downloadSpeed;

    /** 当前上传速度 */
    @Column(name = "UPLOAD_SPEED")
    private Integer uploadSpeed;

    /** 延迟 */
    @Column(name = "DELAY")
    private Double delay;

    /** 丢包率 */
    @Column(name = "PACKET_LOSS_RATE")
    private Double packetLossRate;

    /** 当前负载率  */
    @Column(name = "LOAD_FACTOR")
    private Double loadFactor;

    /** 当前温度 */
    @Column(name = "TEMPERATURE")
    private Integer temperature;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "CREATE_TIME")
    private Timestamp createTime;

    @Column(name = "FACTORY_DEV_NO")
    public String factoryDevNo;             //设备组号

    @Column(name = "MOBILE_TX_TRAFFIC")
    public Long mobileTxTraffic;         //GPRS发送流量(MB)

    @Column(name = "MOBILE_RX_TRAFFIC")
    public Long mobileRxTraffic;         //GPRS接收流量

    @Column(name = "TOTAL_RX_TRAFFIC")
    public Long totalRxTraffic;          //总的接收流量

    @Column(name = "TOTAL_TX_TRAFFIC")
    public Long totalTxTraffic;          //总的发送流量

    @Column(name = "NET_PING_AVG_DELAY")
    public Double netPingAvgDelay;         //网络延迟avg

    @Column(name = "NET_PING_MDEV_DELAY")
    public Double netPingMdevDelay;        //网络延迟mdev

    @Column(name = "NET_PING_MIN_DELAY")
    public Double netPingMinDelay;          //网络延迟min

    @Column(name = "NET_PING_MAX_DELAY")
    public Double netPingMaxDelay;          //网络延迟max


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNetWorkType() {
        return netWorkType;
    }

    public void setNetWorkType(Integer netWorkType) {
        this.netWorkType = netWorkType;
    }

    public Integer getDownloadSpeed() {
        return downloadSpeed;
    }

    public void setDownloadSpeed(Integer downloadSpeed) {
        this.downloadSpeed = downloadSpeed;
    }

    public Integer getUploadSpeed() {
        return uploadSpeed;
    }

    public void setUploadSpeed(Integer uploadSpeed) {
        this.uploadSpeed = uploadSpeed;
    }

    public Double getDelay() {
        return delay;
    }

    public void setDelay(Double delay) {
        this.delay = delay;
    }

    public Double getPacketLossRate() {
        return packetLossRate;
    }

    public void setPacketLossRate(Double packetLossRate) {
        this.packetLossRate = packetLossRate;
    }

    public Double getLoadFactor() {
        return loadFactor;
    }

    public void setLoadFactor(Double loadFactor) {
        this.loadFactor = loadFactor;
    }

    public Integer getTemperature() {
        return temperature;
    }

    public void setTemperature(Integer temperature) {
        this.temperature = temperature;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public String getFactoryDevNo() {
        return factoryDevNo;
    }

    public void setFactoryDevNo(String factoryDevNo) {
        this.factoryDevNo = factoryDevNo;
    }

    public Long getMobileTxTraffic() {
        return mobileTxTraffic;
    }

    public void setMobileTxTraffic(Long mobileTxTraffic) {
        this.mobileTxTraffic = mobileTxTraffic;
    }

    public Long getMobileRxTraffic() {
        return mobileRxTraffic;
    }

    public void setMobileRxTraffic(Long mobileRxTraffic) {
        this.mobileRxTraffic = mobileRxTraffic;
    }

    public Long getTotalRxTraffic() {
        return totalRxTraffic;
    }

    public void setTotalRxTraffic(Long totalRxTraffic) {
        this.totalRxTraffic = totalRxTraffic;
    }

    public Long getTotalTxTraffic() {
        return totalTxTraffic;
    }

    public void setTotalTxTraffic(Long totalTxTraffic) {
        this.totalTxTraffic = totalTxTraffic;
    }

    public Double getNetPingAvgDelay() {
        return netPingAvgDelay;
    }

    public void setNetPingAvgDelay(Double netPingAvgDelay) {
        this.netPingAvgDelay = netPingAvgDelay;
    }

    public Double getNetPingMdevDelay() {
        return netPingMdevDelay;
    }

    public void setNetPingMdevDelay(Double netPingMdevDelay) {
        this.netPingMdevDelay = netPingMdevDelay;
    }

    public Double getNetPingMinDelay() {
        return netPingMinDelay;
    }

    public void setNetPingMinDelay(Double netPingMinDelay) {
        this.netPingMinDelay = netPingMinDelay;
    }

    public Double getNetPingMaxDelay() {
        return netPingMaxDelay;
    }

    public void setNetPingMaxDelay(Double netPingMaxDelay) {
        this.netPingMaxDelay = netPingMaxDelay;
    }

}
