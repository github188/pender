package com.vendor.service.impl.observer;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecarry.core.dao.IGenericDao;
import com.ecarry.core.dao.SQLUtils;
import com.vendor.po.DeviceLog;
import com.vendor.service.IObserverService;
import com.vendor.util.Commons;

/**
 * 观察者更新业务类
 * @author liujia on 2016年12月28日
 */
@Service("observerService")
public class ObserverService implements IObserverService {
	
	@Autowired
	private IGenericDao genericDao;
	
	/**
	 * 更新设备日志信息
	 * 
	 * @param devNo 设备编号
	 * @param isOnline 设备是否在线 true:在线 false:离线
	 */
	@Override
	public void saveDeviceLog(String devNo, boolean isOnline) {
		DeviceLog deviceLog = findDeviceLog(devNo, Commons.DEVICE_STATUS_OFFLINE);//离线设备
		if (!isOnline) {// 离线
			saveDeviceLog(devNo, deviceLog, Commons.DEVICE_STATUS_OFFLINE);
		} else {
			deleteDeviceLog(deviceLog);
		}
	}
	
	public DeviceLog findDeviceLog(String deviceNo, Integer deviceStatus) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer("SELECT ");
		String cols = SQLUtils.getColumnsSQL(DeviceLog.class, "C");
		buf.append(cols);
		buf.append(" FROM T_DEVICE_LOG C WHERE C.DEVICE_NO = ? AND C.DEVICE_STATUS = ? ");
		args.add(deviceNo);
		args.add(deviceStatus);
		return genericDao.findT(DeviceLog.class, buf.toString(), args.toArray());
	}
	
	/**
	 * 保存设备日志
	 * @param device
	 * @param deviceLog
	 */
	private void saveDeviceLog(String devNo, DeviceLog deviceLog, Integer deviceStatus) {
		if (null == deviceLog) {
			deviceLog = new DeviceLog();
			deviceLog.setDeviceNo(devNo);
			deviceLog.setCreateTime(new Timestamp(System.currentTimeMillis()));
			deviceLog.setDeviceStatus(deviceStatus);
			genericDao.save(deviceLog);
		}
	}

	/**
	 * 删除设备日志
	 * @param device
	 * @param deviceLog
	 */
	private void deleteDeviceLog(DeviceLog deviceLog) {
		if (null != deviceLog)
			genericDao.delete(deviceLog);
	}
	
}
