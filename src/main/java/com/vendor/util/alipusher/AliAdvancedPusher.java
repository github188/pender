package com.vendor.util.alipusher;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.aliyuncs.push.model.v20150827.GetDeviceInfosRequest;
import com.aliyuncs.push.model.v20150827.GetDeviceInfosResponse;
import com.aliyuncs.push.model.v20150827.PushRequest;
import com.aliyuncs.push.model.v20150827.PushResponse;

public abstract class AliAdvancedPusher extends Config {

	private static final Logger logger = Logger.getLogger(AliAdvancedPusher.class);

	private String makeDevStr(List<String> devIDs) {
		String devStr = "";
		if (null == devIDs || 0 == devIDs.size()) {
			return devStr;
		}

		for (String dev : devIDs) {
			devStr = devStr + dev + ",";
		}

		int endIndex = devStr.lastIndexOf(",");
		devStr = devStr.substring(0, endIndex);
		return devStr;
	}

	public void pushMessageToAndroidDevices(List<String> devIDs, String jsonMessage, boolean storeOffline) throws Exception {
		final SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");
		final String date = dateFormat.format(new Date());
		PushRequest pushRequest = new PushRequest();

		// 推送目标
		pushRequest.setAppKey(appKey);
		pushRequest.setTarget("account"); // 推送目标: device:推送给设备; account:推送给指定帐号,tag:推送给自定义标签; all: 推送给全部
		pushRequest.setTargetValue(makeDevStr(devIDs)); // 根据Target来设定，如Target=device, 则对应的值为 设备id1,设备id2. 多个值使用逗号分隔.(帐号与设备有一次最多100个的限制)
		logger.info("*****makeDevStr(devIDs):" + makeDevStr(devIDs));
		pushRequest.setDeviceType(1); // 设备类型deviceType 取值范围为:0~3. iOS设备: 0; Android设备: 1; 全部: 3, 这是默认值.

		// 推送配置
		pushRequest.setType(0); // 0:表示消息(默认为0), 1:表示通知
		pushRequest.setTitle(date); // 消息的标题
		pushRequest.setBody(jsonMessage); // 消息的内容
		pushRequest.setSummary("PushRequest summary"); // 通知的摘要

		pushRequest.setStoreOffline(storeOffline); // 离线消息是否保存,若保存, 在推送时候，用户即使不在线，下一次上线则会收到
		pushRequest.setBatchNumber("100010"); // 批次编号,用于活动效果统计. 设置成业务可以记录的字符串

		setPlatformParameter(pushRequest);

		PushResponse pushResponse = client.getAcsResponse(pushRequest);

		System.out.printf("RequestId: %s, ResponseId: %s, message: %s\n", pushResponse.getRequestId(), pushResponse.getResponseId(), pushResponse.getMessage());
	}

	// 推送配置: Android
	// pushRequest.setAndroidOpenType("3"); // 点击通知后动作,1:打开应用 2: 打开应用Activity 3:打开 url
	// pushRequest.setAndroidOpenUrl("http://www.baidu.com"); // Android收到推送后打开对应的url,仅仅当androidOpenType=3有效
	// pushRequest.setAndroidExtParameters("{\"k1\":\"android\",\"k2\":\"v2\"}"); // 设定android类型设备通知的扩展属性
	abstract public void setPlatformParameter(PushRequest pushReq);

	/**
	 * 查询设备状态
	 * 
	 * @param devIDs 推送设备ID
	 * @return
	 * @throws Exception
	 */
	public List<GetDeviceInfosResponse.DeviceInfo> getDeviceInfos(List<String> devIDs) throws Exception {
		GetDeviceInfosRequest getDeviceInfosRequest = new GetDeviceInfosRequest();
		getDeviceInfosRequest.setAppKey(appKey);
		getDeviceInfosRequest.setDevices(makeDevStr(devIDs));

		GetDeviceInfosResponse getDeviceInfosResponse = client.getAcsResponse(getDeviceInfosRequest);
		return getDeviceInfosResponse.getDeviceInfos();
		// for (GetDeviceInfosResponse.DeviceInfo deviceInfo : getDeviceInfosResponse.getDeviceInfos()) {
		// System.out.printf("deviceId: %s, isOnline: %s\n", deviceInfo.getDeviceId(), deviceInfo.getIsOnline());
		// }
		// return null;
	}
}
