package com.vendor.po.observer;

import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.vendor.po.Device;
import com.vendor.service.IObserverService;
import com.vendor.util.Commons;

/**
 * 设备异常信息观察者
 * 
 * 用于监测设备的心跳，及时更新设备异常信息
 * @author liujia on 2016年12月23日
 */
public class DeviceLogObserver implements Observer {
	
	private static final Logger LOGGER = Logger.getLogger(DeviceLogObserver.class);
	
	public DeviceLogObserver() {
		
	}

	public DeviceLogObserver(Observable o) {
		o.addObserver(this);
	}
	
	@Override
	public void update(Observable o, Object arg) {
		LOGGER.info("************设备异常信息观察者***【更新】***开始*******");
		Device device = (Device) o;
		if (null != device) {
			Integer deviceState = device.getState();
			boolean isOnline = Commons.FAULT == deviceState ? false : true;
			LOGGER.info("***设备内部号：" + device.getDevNo() + "**isOnline:****" + isOnline);
			
			// 更新设备日志信息
			LOGGER.info("*****更新设备日志信息*******开始*******");
			
			WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
			IObserverService observerService = (IObserverService) context.getBean("observerService");
			
			observerService.saveDeviceLog(device.getDevNo(), isOnline);
			LOGGER.info("*****更新设备日志信息*******结束*******");
			
			// 删除观察者
			o.deleteObserver(this);
		}
		LOGGER.info("************设备异常信息观察者***【更新】***结束*******");
	}

}
