package com.vendor.vo.app;

import java.sql.Timestamp;
import java.util.List;

/**
 * 关联推荐商品推送消息通知对象
 *
 */
public class RecommendPushData {
	
	private String notifyFlag = "recommendProduct_SD";
	
	private Timestamp time;
	
	private Long messageId;
	
	private List<RecommendGoodsInfo> list;

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

	public List<RecommendGoodsInfo> getList() {
		return list;
	}

	public void setList(List<RecommendGoodsInfo> list) {
		this.list = list;
	}

}
