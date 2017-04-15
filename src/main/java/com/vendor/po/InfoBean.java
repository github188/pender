package com.vendor.po;

/**
 * Created by Administrator on 2017/3/24.
 */
public class InfoBean {

    public String     machineNo;               //机器号(8位)
    public float   cpuRate;                 //cpu使用率
    public float   romRate;                 //内存使用率

    public long    mobileTxTraffic;         //GPRS发送流量(KB)
    public long    mobileRxTraffic;         //GPRS接收流量(KB)
    public long    totalRxTraffic;          //总的接收流量(KB)
    public long    totalTxTraffic;          //总的发送流量(KB)

    public long    internalSDTotal;         //机身存储总量(Byte)
    public long    internalSDLeft;          //机身存储存余量(Byte)
    public long    externalSDTotal;         //sd卡总量(Byte)
    public long    externalSDLeft;          //sd卡余量(Byte)


    public float    netPingAvgDelay;         //网络延迟avg(ms)
    public float    netPingMdevDelay;        //网络延迟mdev(ms)
    public float    netPingMinDelay;          //网络延迟min(ms)
    public float    netPingMaxDelay;          //网络延迟max(ms)
    public float    netPingLost;             //网络丢包率(ms)
    public float    netDownloadRate;         //网络下载速度(kb/s)
    public int      netType;                 //网络类型(0:无网络;1:wap;2:2G;3:3G即以上(4G);4:wifi;5:以太网(网线))

    public String getMachineNo() {
        return machineNo;
    }

    public void setMachineNo(String machineNo) {
        this.machineNo = machineNo;
    }

    public float getCpuRate() {
        return cpuRate;
    }

    public void setCpuRate(float cpuRate) {
        this.cpuRate = cpuRate;
    }

    public float getRomRate() {
        return romRate;
    }

    public void setRomRate(float romRate) {
        this.romRate = romRate;
    }

    public long getMobileTxTraffic() {
        return mobileTxTraffic;
    }

    public void setMobileTxTraffic(long mobileTxTraffic) {
        this.mobileTxTraffic = mobileTxTraffic;
    }

    public long getMobileRxTraffic() {
        return mobileRxTraffic;
    }

    public void setMobileRxTraffic(long mobileRxTraffic) {
        this.mobileRxTraffic = mobileRxTraffic;
    }

    public long getTotalRxTraffic() {
        return totalRxTraffic;
    }

    public void setTotalRxTraffic(long totalRxTraffic) {
        this.totalRxTraffic = totalRxTraffic;
    }

    public long getTotalTxTraffic() {
        return totalTxTraffic;
    }

    public void setTotalTxTraffic(long totalTxTraffic) {
        this.totalTxTraffic = totalTxTraffic;
    }

    public long getInternalSDTotal() {
        return internalSDTotal;
    }

    public void setInternalSDTotal(long internalSDTotal) {
        this.internalSDTotal = internalSDTotal;
    }

    public long getInternalSDLeft() {
        return internalSDLeft;
    }

    public void setInternalSDLeft(long internalSDLeft) {
        this.internalSDLeft = internalSDLeft;
    }

    public long getExternalSDTotal() {
        return externalSDTotal;
    }

    public void setExternalSDTotal(long externalSDTotal) {
        this.externalSDTotal = externalSDTotal;
    }

    public long getExternalSDLeft() {
        return externalSDLeft;
    }

    public void setExternalSDLeft(long externalSDLeft) {
        this.externalSDLeft = externalSDLeft;
    }

    public float getNetPingAvgDelay() {
        return netPingAvgDelay;
    }

    public void setNetPingAvgDelay(float netPingAvgDelay) {
        this.netPingAvgDelay = netPingAvgDelay;
    }

    public float getNetPingMdevDelay() {
        return netPingMdevDelay;
    }

    public void setNetPingMdevDelay(float netPingMdevDelay) {
        this.netPingMdevDelay = netPingMdevDelay;
    }

    public float getNetPingMinDelay() {
        return netPingMinDelay;
    }

    public void setNetPingMinDelay(float netPingMinDelay) {
        this.netPingMinDelay = netPingMinDelay;
    }

    public float getNetPingMaxDelay() {
        return netPingMaxDelay;
    }

    public void setNetPingMaxDelay(float netPingMaxDelay) {
        this.netPingMaxDelay = netPingMaxDelay;
    }

    public float getNetPingLost() {
        return netPingLost;
    }

    public void setNetPingLost(float netPingLost) {
        this.netPingLost = netPingLost;
    }

    public float getNetDownloadRate() {
        return netDownloadRate;
    }

    public void setNetDownloadRate(float netDownloadRate) {
        this.netDownloadRate = netDownloadRate;
    }

    public int getNetType() {
        return netType;
    }

    public void setNetType(int netType) {
        this.netType = netType;
    }
}

