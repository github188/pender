package com.vendor.service.impl.interaction;

import com.ecarry.core.dao.IGenericDao;
import com.ecarry.core.domain.WebUploader;
import com.ecarry.core.exception.BusinessException;
import com.ecarry.core.web.core.ContextUtil;
import com.ecarry.core.web.core.Page;
import com.ecarry.core.web.view.DownloadView;
import com.vendor.po.*;
import com.vendor.service.IDictionaryService;
import com.vendor.service.IMonitoringService;
import com.vendor.util.CommonUtil;
import com.vendor.util.Commons;
import com.vendor.util.DateUtil;
import com.vendor.util.FileUtil;
import com.vendor.vo.common.ResultBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yunxi on 2017/3/22.
 */
@Service("monitoringService")
public class MonitoringService implements IMonitoringService {

    private static final Logger logger = LoggerFactory.getLogger(InteractionService.class);

    @Autowired
    private IGenericDao genericDao;

    @Autowired
    private IDictionaryService dictionaryService;

    /**
     * 上传监控文件
     *
     * @param file
     * @param factoryDevNo
     * @param rb
     */
    @Override
    public ResultBase uploadMonitoringFile(MultipartFile file, String factoryDevNo, String paramType, ResultBase rb) {
        if (null == file || null == paramType) {
            rb.setResultCode(-1);
            rb.setResultMessage("参数不能为空");
            return rb;
        }
        //查询出设备所在机构号,设备是否离线
        StringBuffer buf = new StringBuffer();
        List<Object> args = new ArrayList<>();
        buf.append(" SELECT D.ORG_ID, COALESCE(DL.DEVICE_STATUS, 1) deviceStatus");
        buf.append(" FROM T_DEVICE D");
        buf.append(" LEFT JOIN T_DEVICE_LOG DL ON DL.DEVICE_NO = D.DEV_NO AND DL.DEVICE_STATUS=?");
        args.add(Commons.DEVICE_STATUS_OFFLINE);
        buf.append(" LEFT JOIN T_DEVICE_RELATION DR ON DR.DEV_NO = D.DEV_NO");
        buf.append(" WHERE DR.FACTORY_DEV_NO =? AND D.BIND_STATE =?");
        args.add(factoryDevNo);
        args.add(Commons.BIND_STATE_SUCCESS);
        Device device = genericDao.findT(Device.class, buf.toString(), args.toArray());
        if (null == device) {
            rb.setResultCode(-1);
            rb.setResultMessage("设备不存在");
            return rb;
        }
        //新增设备监控数据
        try {
            DeviceMonitoring deviceMonitoring = genericDao.findT(DeviceMonitoring.class, "SELECT * FROM T_DEVICE_MONITORING WHERE FACTORY_DEV_NO=?", factoryDevNo);
            User user = new User();
            user.setId(1L);
            if (null == deviceMonitoring) {
                deviceMonitoring.setFactoryDevNo(factoryDevNo);
                deviceMonitoring.setCreateTime(DateUtil.stringToTimestamp(file.getOriginalFilename().split("_")[2].split("\\.")[0] + " 00:00:00"));
                deviceMonitoring.setOrgId(device.getOrgId());
                genericDao.save(deviceMonitoring);

            } else {
                deviceMonitoring.setUpdateTime(DateUtil.stringToTimestamp(file.getOriginalFilename().split("_")[2].split("\\.")[0] + " 00:00:00"));
                genericDao.update(deviceMonitoring);
            }
            DeviceMonitoringLog deviceMonitoringLog = new DeviceMonitoringLog();
            deviceMonitoringLog.setCreateTime(new Timestamp(System.currentTimeMillis()));
            deviceMonitoringLog.setMonitoringId(deviceMonitoring.getId());
            deviceMonitoringLog.setLogType(Integer.valueOf(paramType));
            genericDao.save(deviceMonitoringLog);

            WebUploader uploader = new WebUploader();
            uploader.setFile(file);
            uploader.setKey(deviceMonitoring.getId() * -1);
            uploader.setModule(Commons.FILE_MONITORING_TYPE);
            dictionaryService.saveUploader(user, uploader);
            rb.setResultMessage("上传文件成功..");
            rb.setResultCode(0);
        } catch (Exception e) {
            rb.setResultMessage("上传保存失败..");
            rb.setResultCode(-1);
        }
        return rb;
    }

    /**
     * 获取设备监控列表
     *
     * @param device
     * @param page
     * @param monitoringType
     * @return
     */
    @Override
    public List<Device> findDeviceMonitoringPage(Device device, Page page, Integer monitoringType) {
        User user = ContextUtil.getUser(User.class);
        if (null == user)
            throw new BusinessException("当前用户未登录");
        return findDeviceMonitoringList(device, user.getOrgId(), page, monitoringType);
    }

    /**
     * 获取设备监控列表
     *
     * @param device
     * @param monitoringType 设备监控类型(0:丢包率异常,1:CPU负载率异常)
     * @return
     */
    private List<Device> findDeviceMonitoringList(Device device, Long orgId, Page page, Integer monitoringType) {
        User user = ContextUtil.getUser(User.class);
        if (null == user)
            throw new BusinessException("当前用户未登录");
        StringBuffer buf = new StringBuffer();
        List<Object> args = new ArrayList<>();
        //先查询店铺信息
        buf.append(" SELECT D.DEV_NO,D.MANUFACTURER,POINT_ID,COALESCE(DL.DEVICE_STATUS, 1) deviceStatus,");
        buf.append(" pp.POINT_NAME pointName,pp.POINT_ADDRESS pointAddress FROM T_DEVICE D");
        buf.append(" LEFT JOIN T_POINT_PLACE PP ON PP.ID=D.POINT_ID");
        buf.append(" LEFT JOIN T_DEVICE_LOG DL ON DL.DEVICE_NO=DEV_NO AND DL.DEVICE_STATUS=?");
        args.add(Commons.DEVICE_STATUS_OFFLINE);
        buf.append(" WHERE D.ORG_ID=? AND BIND_STATE=?");
        args.add(orgId);
        args.add(Commons.BIND_STATE_SUCCESS);

        if (null != device.getFactoryDevNo()) {
            buf.append(" AND D.DEV_NO IN (SELECT DEV_NO FROM T_DEVICE_RELATION WHERE FACTORY_DEV_NO LIKE ?)");
            args.add("%" + device.getFactoryDevNo() + "%");
        }

        if (null != device.getPointName()) {
            buf.append(" AND D.POINT_ID IN (SELECT ID FROM T_POINT_PLACE WHERE POINT_NAME LIKE ?)");
            args.add("%" + device.getPointName() + "%");
        }

        if (null != device.getDeviceStatus()) {
            if (2 == device.getDeviceStatus()) {
                buf.append(" AND DL.DEVICE_STATUS=?");
                args.add(device.getDeviceStatus());
            } else {
                buf.append(" AND DL.DEVICE_STATUS IS NULL");
            }
        }

        if (null != device.getProv() || null != device.getCity() || null != device.getDist()) {
            buf.append(" AND (PP.PROV LIKE ? OR PP.CITY LIKE ? OR PP.DIST LIKE ?)");
            args.add(null != device.getProv() ? device.getProv() : "");
            args.add(null != device.getCity() ? device.getCity() : "");
            args.add(null != device.getDist() ? device.getDist() : "");
        }

        if (null != monitoringType) {
            String[] strings = getfactoryDevNos(monitoringType, user.getOrgId());
            buf.append(" AND D.DEV_NO IN (SELECT DEV_NO FROM T_DEVICE_RELATION WHERE FACTORY_DEV_NO IN(");
            if(null != strings && strings.length > 0){
                for (String string : strings) {
                    buf.append("?,");
                    args.add(string);
                }
                buf.setLength(buf.length() - 1);
            }else{
                buf.append("null");
            }
            buf.append("))");
        }

        List<Device> deviceList = genericDao.findTs(Device.class, page, buf.toString(), args.toArray());
        if (null != deviceList && !deviceList.isEmpty()) {
            for (Device devices : deviceList) {
                String factoryDevNo = genericDao.findSingle(String.class, "select factory_dev_no from t_device_relation where dev_no=?", devices.getDevNo());
                devices.setFactoryDevNo(factoryDevNo);
                DeviceMonitoring deviceMonitoring = genericDao.findT(DeviceMonitoring.class,
                        "SELECT NETWORK_TYPE,SOFTWARE_VERSION,FIRMWARE_VERSION,MEMORY FROM T_DEVICE_MONITORING WHERE FACTORY_DEV_NO=? AND ORG_ID=?", devices.getFactoryDevNo(), orgId);
                if (null != deviceMonitoring) {
                    devices.setMemory(deviceMonitoring.getMemory());
                    devices.setNetWorkType(deviceMonitoring.getNetWorkType());
                    devices.setSoftwareVersion(deviceMonitoring.getSoftwareVersion());
                    devices.setFirmwareVersion(deviceMonitoring.getFirmwareVersion());
                }
            }
        }
        return deviceList;
    }

    /**
     * 查询出异常的设备
     * @param monitoringType
     * @param orgId
     * @return
     */
    private String[] getfactoryDevNos(Integer monitoringType, Long orgId) {
        StringBuffer buf = new StringBuffer();
        List<Object> args = new ArrayList<>();
        String[] strings = null;
        buf.append(" SELECT FACTORY_DEV_NO FROM T_DEVICE_MONITORING_NETWORK");
        buf.append(" WHERE FACTORY_DEV_NO IN");
        buf.append(" (SELECT FACTORY_DEV_NO FROM T_DEVICE_RELATION where DEV_NO IN(SELECT DEV_NO FROM T_DEVICE WHERE ORG_ID=? AND BIND_STATE=?)");
        args.add(orgId);
        args.add(Commons.BIND_STATE_SUCCESS);
        buf.append(" )");
        if (0 == monitoringType) {
            buf.append(" AND PACKET_LOSS_RATE > ?");
            args.add(1.0);
        }
        if (1 == monitoringType) {
            buf.append(" AND LOAD_FACTOR > ?");
            args.add(80.0);
        }
        List<Object> listSingle = genericDao.findListSingle(buf.toString(), args.toArray());
        if (null != listSingle && !listSingle.isEmpty()) {
            strings = CommonUtil.convertToStringArr(listSingle);
        }
        return strings;
    }

    /**
     * 查询设备详情
     *
     * @param factoryDevNo
     * @return
     */
    @Override
    public DeviceMonitoring findDeviceMonitoringDateils(String factoryDevNo) {
        User user = ContextUtil.getUser(User.class);
        if (null == user)
            throw new BusinessException("当前用户未登录");
        Device device = new Device();
        device.setFactoryDevNo(factoryDevNo);
        StringBuffer buf = new StringBuffer();
        List<Object> args = new ArrayList<>();
        buf.append(" SELECT D.DEV_NO devNo,D.MANUFACTURER manufacturer,POINT_ID pointId,COALESCE(DL.DEVICE_STATUS, 1) deviceStatus,");
        buf.append(" pp.POINT_NAME pointName,pp.POINT_ADDRESS pointAddress FROM T_DEVICE D");
        buf.append(" LEFT JOIN T_POINT_PLACE PP ON PP.ID=D.POINT_ID");
        buf.append(" LEFT JOIN T_DEVICE_LOG DL ON DL.DEVICE_NO=DEV_NO AND DL.DEVICE_STATUS=?");
        args.add(Commons.DEVICE_STATUS_OFFLINE);
        buf.append(" WHERE D.ORG_ID=? AND BIND_STATE=?");
        args.add(user.getOrgId());
        args.add(Commons.BIND_STATE_SUCCESS);
        buf.append(" AND DL.DEVICE_NO=(SELECT DEV_NO FROM T_DEVICE_RELATION WHERE FACTORY_DEV_NO=?)");
        args.add(factoryDevNo);
        DeviceMonitoring deviceMonitoring = genericDao.findT(DeviceMonitoring.class, buf.toString(), args.toArray());
        if (null != deviceMonitoring) {
            args.clear();
            buf.setLength(0);
            buf.append(" SELECT FACTORY_DEV_NO,SOFTWARE_VERSION,FIRMWARE_VERSION,MEMORY,CREATE_TIME,NETWORK_TYPE,CPU_RATE,ROM_RATE,INTERNAL_SD_TOTAL,INTERNAL_SD_LEFT");
            buf.append(" FROM T_DEVICE_MONITORING WHERE FACTORY_DEV_NO=? AND ORG_ID=?");
            args.add(factoryDevNo);
            args.add(user.getOrgId());
            DeviceMonitoring deviceMonitoring1 = genericDao.findT(DeviceMonitoring.class, buf.toString(), args.toArray());
            if (null != deviceMonitoring1) {
                deviceMonitoring.setFactoryDevNo(deviceMonitoring1.getFactoryDevNo());
                deviceMonitoring.setSoftwareVersion(deviceMonitoring1.getSoftwareVersion());
                deviceMonitoring.setFirmwareVersion(deviceMonitoring1.getFirmwareVersion());
                deviceMonitoring.setMemory(deviceMonitoring1.getInternalSDTotal() - deviceMonitoring1.getInternalSDLeft());
                deviceMonitoring.setCreateTime(deviceMonitoring1.getCreateTime());
                deviceMonitoring.setNetWorkType(deviceMonitoring1.getNetWorkType());
                deviceMonitoring.setCpuRate(deviceMonitoring1.getCpuRate());
                deviceMonitoring.setRomRate(deviceMonitoring1.getRomRate());
                deviceMonitoring.setInternalSDTotal(deviceMonitoring1.getInternalSDTotal());
            }
            args.clear();
            buf.setLength(0);
            buf.append(" SELECT ID cabinetId,CABINET_NO cabinetNo,MODEL model FROM T_CABINET WHERE DEVICE_ID = (");
            buf.append("         SELECT ID FROM T_DEVICE WHERE ORG_ID=? AND STATE!=? AND DEV_NO=(");
            args.add(user.getOrgId());
            args.add(Commons.PRODUCT_STATE_TRASH);
            buf.append("         SELECT DEV_NO FROM T_DEVICE_RELATION WHERE FACTORY_DEV_NO=?");
            args.add(factoryDevNo);
            buf.append("     )");
            buf.append(" )");
            List<DeviceMonitoringDetails> deviceMonitoringDateilsList = genericDao.findTs(DeviceMonitoringDetails.class, buf.toString(), args.toArray());
            deviceMonitoringDateilsList = deviceList(deviceMonitoringDateilsList);
            if (null != deviceMonitoringDateilsList && !deviceMonitoringDateilsList.isEmpty()) {
                for (DeviceMonitoringDetails deviceMonitoringDetails : deviceMonitoringDateilsList) {
                    args.clear();
                    buf.setLength(0);
                    buf.append(" SELECT COMPRESSOR_STATE, COMPRESSOR_TIME,LIGHT_STATE,LIGHT_TIME FROM T_DEVICE_MONITORING_DETAILS WHERE CABINET_ID=? AND ORG_ID=?");
                    DeviceMonitoringDetails deviceMonDetails = genericDao.findT(DeviceMonitoringDetails.class, buf.toString(), deviceMonitoringDetails.getCabinetId(), user.getOrgId());
                    if (null != deviceMonDetails) {
                        deviceMonitoringDetails.setCompressorState(deviceMonDetails.getCompressorState());
                        deviceMonitoringDetails.setCompressorTime(deviceMonDetails.getCompressorTime());
                        deviceMonitoringDetails.setLightState(deviceMonDetails.getLightState());
                        deviceMonitoringDetails.setLightTime(deviceMonDetails.getLightTime());
                    }
                }
            }
            deviceMonitoring.setDeviceMonitoringDetails(deviceMonitoringDateilsList);
        }
        return deviceMonitoring;
    }


    /**
     * Device list list.
     *
     * @param deviceMonitoringDetails the device monitoring details
     * @return the list
     * @Title: 拼接设备类型
     * @return: List<Device>
     */
    public List<DeviceMonitoringDetails> deviceList(List<DeviceMonitoringDetails> deviceMonitoringDetails) {
        StringBuffer buf = new StringBuffer();
        if (null != deviceMonitoringDetails && !deviceMonitoringDetails.isEmpty()) {
            for (DeviceMonitoringDetails deviceMonitoringDetail : deviceMonitoringDetails) {//拼接此设备下所有附属设备类型
                String typeStr = CommonUtil.getDeviceTypeStrByModel(deviceMonitoringDetail.getModel());
                if (StringUtils.isEmpty(typeStr))
                    continue;
                buf.append(typeStr + "+");
            }
        }
        return deviceMonitoringDetails;
    }

    /**
     * 查询设备监控日志列表
     *
     * @param factoryDevNo
     * @param logType      0：日常日志，1：异常日志
     * @return
     */
    @Override
    public List<DeviceMonitoring> findDeviceMonitoringAndDeviceLog(String factoryDevNo, Integer logType) {
        User user = ContextUtil.getUser(User.class);
        if (null == user)
            throw new BusinessException("当前用户未登录");
        if (null == factoryDevNo || null == logType)
            throw new BusinessException("请求错误");
        StringBuffer buf = new StringBuffer();
        List<Object> args = new ArrayList<>();
        List<DeviceMonitoring> deviceMonitoringList;
        if (0 == logType) {
            buf.append(" SELECT STRING_AGG(B.ID||','||B.NAME||','||B.TYPE||','||B.REAL_PATH, ';' ORDER BY B.TYPE DESC) AS IMAGES,");
            buf.append(" DM.FACTORY_DEV_NO,DL.ID,DL.CREATE_TIME");
            buf.append(" FROM T_DEVICE_MONITORING DM");
            buf.append(" LEFT JOIN T_DEVICE_MONITORING_LOG DL ON DL.MONITORING_ID=DM.ID");
            buf.append(" LEFT JOIN T_FILE B ON B.INFO_ID=DM.ID AND B.TYPE=?");
            args.add(Commons.FILE_MONITORING_TYPE);
            buf.append(" WHERE DM.FACTORY_DEV_NO=? AND ORG_ID=? AND DL.LOG_TYPE=?");
            args.add(factoryDevNo);
            args.add(user.getOrgId());
            args.add(logType);
            buf.append(" GROUP BY DM.FACTORY_DEV_NO,DL.ID,DL.CREATE_TIME");
            deviceMonitoringList = genericDao.findTs(DeviceMonitoring.class, buf.toString(), args.toArray());
            if (null != deviceMonitoringList && !deviceMonitoringList.isEmpty()) {
                for (DeviceMonitoring deviceMonitoring : deviceMonitoringList) {
                    splitImages(deviceMonitoring);
                }
            }
        } else {
            buf.append(" SELECT AE.CREATE_TIME,AE.ID exceptionId FROM T_APP_EXCEPTION AE");
            buf.append(" LEFT JOIN T_DEVICE_RELATION DR ON DR.DEV_NO=AE.DEVICE_NO");
            buf.append(" LEFT JOIN T_DEVICE D ON D.DEV_NO=DR.DEV_NO");
            buf.append(" WHERE DR.FACTORY_DEV_NO=? AND D.ORG_ID=?");
            args.add(factoryDevNo);
            args.add(user.getOrgId());
            buf.append(" GROUP BY AE.CREATE_TIME,AE.ID");
            buf.append(" ORDER BY AE.ID DESC");
            deviceMonitoringList = genericDao.findTs(DeviceMonitoring.class, buf.toString(), args.toArray());
        }
        return deviceMonitoringList;
    }

    /**
     * 更新设备监控信息
     *
     * @param infoBean
     * @return
     */
    @Override
    public void updateDeviceMonitoringDeteils(InfoBean infoBean) {
        //更新设备监控-网咯状态
        DeviceMonitoringNetwork deviceMonitoringNetwork = new DeviceMonitoringNetwork();
        deviceMonitoringNetwork.setNetWorkType(infoBean.getNetType());
        deviceMonitoringNetwork.setDownloadSpeed(null);//当前下载速度
        deviceMonitoringNetwork.setUploadSpeed(null);//当前上传速度
        deviceMonitoringNetwork.setDelay(CommonUtil.getFormatTwoNum(infoBean.getNetPingAvgDelay()));
        deviceMonitoringNetwork.setPacketLossRate(CommonUtil.getFormatTwoNum(infoBean.getNetPingLost()));
        deviceMonitoringNetwork.setLoadFactor((double) infoBean.getCpuRate());//当前负载率
        deviceMonitoringNetwork.setTemperature(0);//当前温度
        deviceMonitoringNetwork.setCreateTime(new Timestamp(System.currentTimeMillis()));
        deviceMonitoringNetwork.setFactoryDevNo(infoBean.getMachineNo());
        deviceMonitoringNetwork.setMobileTxTraffic(infoBean.getMobileTxTraffic());
        deviceMonitoringNetwork.setMobileRxTraffic(infoBean.getMobileRxTraffic());
        deviceMonitoringNetwork.setTotalRxTraffic(infoBean.getTotalRxTraffic());
        deviceMonitoringNetwork.setTotalTxTraffic(infoBean.getTotalTxTraffic());
        deviceMonitoringNetwork.setNetPingAvgDelay(CommonUtil.getFormatTwoNum((double) infoBean.getNetPingAvgDelay()));
        deviceMonitoringNetwork.setNetPingMdevDelay(CommonUtil.getFormatTwoNum((double) infoBean.getNetPingMdevDelay()));
        deviceMonitoringNetwork.setNetPingMinDelay(CommonUtil.getFormatTwoNum((double) infoBean.getNetPingMinDelay()));
        deviceMonitoringNetwork.setNetPingMaxDelay(CommonUtil.getFormatTwoNum((double) infoBean.getNetPingMaxDelay()));
        genericDao.save(deviceMonitoringNetwork);

        DeviceMonitoring deviceMonitoring = genericDao.findT(DeviceMonitoring.class, "select * from t_device_monitoring where factory_dev_no=?", infoBean.getMachineNo());
        if (null != deviceMonitoring) {
            deviceMonitoring.setCpuRate((double) infoBean.getCpuRate());
            deviceMonitoring.setRomRate((double) infoBean.getRomRate());
            deviceMonitoring.setInternalSDTotal(infoBean.getInternalSDTotal());
            deviceMonitoring.setInternalSDLeft(infoBean.getInternalSDLeft());
            deviceMonitoring.setExternalSDTotal(infoBean.getExternalSDTotal());
            deviceMonitoring.setExternalSDLeft(infoBean.getExternalSDLeft());
            genericDao.update(deviceMonitoring);
        } else {
            Long orgId = genericDao.findSingle(Long.class, "select org_id from t_device where dev_no=(select dev_no from t_device_relation where factory_dev_no=?)", infoBean.getMachineNo());
            DeviceMonitoring deviceMonitoring2 = new DeviceMonitoring();
            deviceMonitoring2.setOrgId(orgId);
            deviceMonitoring2.setFactoryDevNo(infoBean.getMachineNo());
            deviceMonitoring2.setCreateTime(new Timestamp(System.currentTimeMillis()));
            deviceMonitoring2.setMemory(infoBean.getInternalSDTotal() - infoBean.getInternalSDLeft());
            deviceMonitoring2.setNetWorkType(infoBean.getNetType());
            deviceMonitoring2.setCpuRate((double) infoBean.getCpuRate());
            deviceMonitoring2.setRomRate((double) infoBean.getRomRate());
            deviceMonitoring2.setInternalSDTotal(infoBean.getInternalSDTotal());
            deviceMonitoring2.setInternalSDLeft(infoBean.getInternalSDLeft());
            deviceMonitoring2.setExternalSDTotal(infoBean.getExternalSDTotal());
            deviceMonitoring2.setExternalSDLeft(infoBean.getExternalSDLeft());
            genericDao.save(deviceMonitoring2);
        }

        //TODO 设备详情中还有没有保存的数据(设备端没传过来)

    }


    /**
     * (5分钟)定时获取网络数据
     *
     * @param factoryDevNo
     * @param startTime
     * @return
     */
    @Override
    public List<DeviceMonitoringNetwork> findDeviceMoitoringNetworkList(String factoryDevNo, Date startTime, Date endTime) {
        StringBuffer buf = new StringBuffer();
        List<Object> args = new ArrayList<>();
        buf.append(" SELECT * FROM T_DEVICE_MONITORING_NETWORK WHERE FACTORY_DEV_NO=?");
        args.add(factoryDevNo);
        if (null != endTime && null != startTime) {
            buf.append(" AND CREATE_TIME BETWEEN ? AND ?");
            args.add(new Timestamp(startTime.getTime()));
            args.add(new Timestamp(endTime.getTime()));
        }
        return genericDao.findTs(DeviceMonitoringNetwork.class, buf.toString(), args.toArray());
    }

    /**
     * 获取丢包率异常设备 CPU负载率异常设备的数量
     *
     * @return
     */
    @Override
    public DeviceMonitoring findDetailsAndNetworkCount() {
        User user = ContextUtil.getUser(User.class);
        if (null == user)
            throw new BusinessException("当前用户未登录");

        StringBuffer buf = new StringBuffer();
        List<Object> args = new ArrayList<>();
        buf.append("SELECT");
        buf.append(" (SELECT COUNT(ID) FROM T_DEVICE_MONITORING_NETWORK");
        buf.append(" WHERE PACKET_LOSS_RATE > ? AND FACTORY_DEV_NO IN(");
        args.add(1.0);
        buf.append(" SELECT FACTORY_DEV_NO FROM T_DEVICE_RELATION WHERE DEV_NO IN (");
        buf.append(" SELECT DEV_NO FROM T_DEVICE WHERE ORG_ID=? AND BIND_STATE=?");
        args.add(user.getOrgId());
        args.add(Commons.BIND_STATE_SUCCESS);
        buf.append(" ))) packetLossRateNumber,");//丢包率异常设备数量

        buf.append(" (SELECT COUNT(ID) FROM T_DEVICE_MONITORING_NETWORK");
        buf.append(" WHERE LOAD_FACTOR > ? AND FACTORY_DEV_NO IN(");
        args.add(80.0);
        buf.append(" SELECT FACTORY_DEV_NO FROM T_DEVICE_RELATION WHERE DEV_NO IN (");
        buf.append(" SELECT DEV_NO FROM T_DEVICE WHERE ORG_ID=? AND BIND_STATE=?");
        args.add(user.getOrgId());
        args.add(Commons.BIND_STATE_SUCCESS);
        buf.append(" ))) cpuRateNumber");//CPU负载率异常设备数量
        return genericDao.findT(DeviceMonitoring.class, buf.toString(), args.toArray());
    }

    /**
     * 实时更新日志
     *
     * @param factoryDevNo the factory dev no
     * @param rb
     */
    @Override
    public ResultBase updateMonitoring(String factoryDevNo, ResultBase rb) {
        User user = ContextUtil.getUser(User.class);
        if (null == user)
            throw new BusinessException("当前用户未登录");

        if (null == factoryDevNo) {
            rb.setResultMessage("参数不能为空");
            rb.setResultCode(-1);
        }


        return null;
    }

    /**
     * 导出异常信息
     *
     * @param exceptionId
     */
    @Override
    public DownloadView findExportExceptionLog(Long exceptionId) {
        User user = ContextUtil.getUser(User.class);
        if (null == user)
            throw new BusinessException("当前用户未登录");

        if (null == exceptionId)
            throw new BusinessException("请求错误");

        AppException appexception = genericDao.findT(AppException.class, "Select ID,EXCEPTIONS,CREATE_TIME FROM T_APP_EXCEPTION WHERE ID=?", exceptionId);
        if (null == appexception)
            throw new BusinessException("数据不存在或已删除");

        String fileName = DateUtil.getTimestampStr(appexception.getCreateTime(), "yyyyMMddHHmmss")+".txt";
        String downloadpath = getExcelFilePath(fileName);
        String[] split = downloadpath.split(":\\/");
        String path = split[1]+ ":/"+split[2];
        if(!FileUtil.isExsit(path)){
            try {
                FileUtil.createIfNotNewFile(path);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        FileUtil.saveToFile(appexception.getExceptions(), path, "UTF-8");

        return new DownloadView(path);
    }

    /**
     * 定时清理掉30天前的日志
     */
    @Override
    public void updateMonitoringStateJob() {
        String format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis()));
        Timestamp startTime = DateUtil.getWeekBeforeDays(format, -30);
        Timestamp endTime = Timestamp.valueOf(format);
        StringBuffer buf = new StringBuffer();
        List<Object> args = new ArrayList<>();
        buf.append("DELETE FROM T_APP_EXCEPTION WHERE ID NOT IN (SELECT ID FROM T_APP_EXCEPTION WHERE CREATE_TIME BETWEEN ? AND ?)");
        args.add(startTime);
        args.add(endTime);
        genericDao.execute(buf.toString(), args.toArray());

        args.clear();
        buf.setLength(0);
        buf.append("DELETE FROM T_DEVICE_MONITORING_LOG WHERE ID NOT IN (SELECT ID FROM T_DEVICE_MONITORING_LOG WHERE CREATE_TIME BETWEEN ? AND ?)");
        args.add(startTime);
        args.add(endTime);
        genericDao.execute(buf.toString(), args.toArray());
    }

    private String getExcelFilePath(String fileName) {
        return this.getClass().getClassLoader().getResource("/") + "downloadFile/" + fileName;
    }

    /**
     * @param deviceMonitoring 活动内容对象
     * @Title: 截取推送的图片数据
     * @return: LotteryProduct
     */
    private void splitImages(DeviceMonitoring deviceMonitoring) {
        if (null != deviceMonitoring) {
            String[] strImage = deviceMonitoring.getImages().split(";");//准备号活动内容的图片
            boolean isDownload = false;
            for (String image : strImage) {
                if (isDownload)
                    break;
                if (!isDownload && Commons.FILE_MONITORING_TYPE == Integer.valueOf(image.split(",")[2]) && null != image.split(",")[3]) {//主图
                    isDownload = true;
                    deviceMonitoring.setDownloadUrl(dictionaryService.getFileServer() + image.split(",")[3]);
                }
            }
        }
    }
}
