package com.vendor.test;

import java.util.ArrayList;
import java.util.List;

import com.aliyuncs.push.model.v20150827.GetDeviceInfosResponse;
import com.vendor.util.MessagePusher;

public class Test {

	public static void main(String[] args) throws Exception {
//		checkIsOnline("864a706d8e4c419eafe7226a162a611c");// 93002393
//		checkIsOnline("3bc9eff7c937488abf5a312fe852792a");// 93003639
		checkIsOnline("59912bda292d4aecbebbe8b1ce06ab6f");// 93004630
	}
	
	/**
	 * 通过阿里检查设备是否在线
	 * 
	 * @param devicePushId 设备推送id
	 * @throws Exception
	 */
	public static void checkIsOnline(String devicePushId) throws Exception {
		List<String> deviceIds = new ArrayList<String>();
		deviceIds.add(devicePushId);
		
		// 查询设备状态
		MessagePusher pusher = new MessagePusher();
		for (GetDeviceInfosResponse.DeviceInfo deviceInfo : pusher.getDeviceInfos(deviceIds))
			System.out.println(deviceInfo.getDeviceId() + "---是否在线：---" + deviceInfo.getIsOnline());
	}

}