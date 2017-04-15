package com.vendor.po;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * 打折对象推送对象
 * 
 * @author duanyx
 *
 */
public class ChangeDiscountStateData {

	private String notifyFlag;// 通知标识，用于区分表示本次推送是商品状态变更后的推送。
	private Timestamp time;// 随机时间戳
	private Long messageId;// 消息ID
	private String version = "16";// 版本号

	private List<ChangeDiscountData> list = new ArrayList<ChangeDiscountData>();

	public String getNotifyFlag() {
		return notifyFlag;
	}

	public void setNotifyFlag(String notifyFlag) {
		this.notifyFlag = notifyFlag;
	}

	public List<ChangeDiscountData> getList() {
		return list;
	}

	public void setList(List<ChangeDiscountData> list) {
		this.list = list;
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

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

}
