package com.vendor.vo.app;

import java.util.List;

/**
 * 订单通知对象
 * @author liujia 2016年9月14日
 *
 */
public class OrderData {
	
	private String notifyFlag;
	
	private String orderNo;
	
	private Integer state;
	
	private List<VProduct> list;

	public String getNotifyFlag() {
		return notifyFlag;
	}

	public void setNotifyFlag(String notifyFlag) {
		this.notifyFlag = notifyFlag;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public List<VProduct> getList() {
		return list;
	}

	public void setList(List<VProduct> list) {
		this.list = list;
	}

}
