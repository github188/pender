package com.vendor.service;

import com.ecarry.core.web.core.Page;
import com.ecarry.core.web.view.DownloadView;
import com.vendor.po.Device;
import com.vendor.po.DeviceMonitoring;
import com.vendor.po.DeviceMonitoringNetwork;
import com.vendor.po.InfoBean;
import com.vendor.vo.common.ResultBase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Date;
import java.util.List;

/**
 * 设备监控
 */
public interface IMonitoringService {


    /**
     * 上传监控文件
     *
     * @param file         the file
     * @param factoryDevNo the factory dev no
     * @param rb           the rb
     * @return the result base
     */
    ResultBase uploadMonitoringFile(MultipartFile file, String factoryDevNo, String paramType, ResultBase rb);

    /**
     * 获取设备监控列表
     *
     * @param device         the device
     * @param page           the page
     * @param monitoringType 设备监控类型(0:丢包率异常,1:CPU负载率异常)
     * @return list
     */
    List<Device> findDeviceMonitoringPage(Device device, Page page, Integer monitoringType);

    /**
     * 查询设备详情
     *
     * @param factoryDevNo the factory dev no
     * @return device monitoring
     */
    DeviceMonitoring findDeviceMonitoringDateils(String factoryDevNo);

    /**
     * 查询设备监控异常日志列表
     *
     * @param factoryDevNo the factory dev no
     * @param logType      the log type
     * @return list
     */
    List<DeviceMonitoring> findDeviceMonitoringAndDeviceLog(String factoryDevNo, Integer logType);

    /**
     * 更新设备监控信息
     *
     * @param infoBean the info bean
     * @return
     */
    void updateDeviceMonitoringDeteils(InfoBean infoBean);

    /**
     * (5分钟)定时获取网络数据
     *
     * @param factoryDevNo the factory dev no
     * @param startTime    the start time
     * @param endTime      the end time
     * @return list
     */
    List<DeviceMonitoringNetwork> findDeviceMoitoringNetworkList(String factoryDevNo, Date startTime, Date endTime);

    /**
     * 获取丢包率异常设备 CPU负载率异常设备的数量
     *
     * @return device monitoring
     */
    DeviceMonitoring findDetailsAndNetworkCount();

    /**
     * 实时上传日志
     *
     * @param factoryDevNo the factory dev no
     * @param rb
     */
    ResultBase updateMonitoring(String factoryDevNo, ResultBase rb);

    /**
     * 导出异常信息
     * @param exceptionId
     */
    DownloadView findExportExceptionLog(Long exceptionId);

    /**
     * 定时清理掉30前天的日志
     */
    void updateMonitoringStateJob();
}
