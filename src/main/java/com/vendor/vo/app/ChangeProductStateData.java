package com.vendor.vo.app;

import java.sql.Timestamp;
import java.util.List;

/**
 * 商品状态变更通知对象（包括商品状态和价格）
 * @author liujia 2016年9月14日
 *
 */
public class ChangeProductStateData {
	
	private String notifyFlag;
	
	private Timestamp time;
	
	private Long messageId;
	
	private String version = "16";// 版本号
	
	private List<ChangeProductStateProductData> list;

	public String getNotifyFlag() {
		return notifyFlag;
	}

	public void setNotifyFlag(String notifyFlag) {
		this.notifyFlag = notifyFlag;
	}

	public Timestamp getTime() {
		return time;
	}

	public void setTime(Timestamp time) {
		this.time = time;
	}

	public Long getMessageId() {
		return messageId;
	}

	public void setMessageId(Long messageId) {
		this.messageId = messageId;
	}

	public List<ChangeProductStateProductData> getList() {
		return list;
	}

	public void setList(List<ChangeProductStateProductData> list) {
		this.list = list;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
}
