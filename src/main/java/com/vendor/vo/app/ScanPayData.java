package com.vendor.vo.app;

/**
 * 扫码后通知对象
 * @author liujia 2016年9月14日
 *
 */
public class ScanPayData {
	
	private String notifyFlag;
	
	private Integer type;

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getNotifyFlag() {
		return notifyFlag;
	}

	public void setNotifyFlag(String notifyFlag) {
		this.notifyFlag = notifyFlag;
	}

}
