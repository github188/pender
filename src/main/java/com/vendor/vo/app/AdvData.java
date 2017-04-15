package com.vendor.vo.app;

/**
 * 广告通知对象
 * @author liujia 2016年9月14日
 *
 */
public class AdvData {
	
	private String notifyFlag;
	
	private Integer type;

	private VAdvertisement advertisement;

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public VAdvertisement getAdvertisement() {
		return advertisement;
	}

	public void setAdvertisement(VAdvertisement advertisement) {
		this.advertisement = advertisement;
	}

	public String getNotifyFlag() {
		return notifyFlag;
	}

	public void setNotifyFlag(String notifyFlag) {
		this.notifyFlag = notifyFlag;
	}

}
