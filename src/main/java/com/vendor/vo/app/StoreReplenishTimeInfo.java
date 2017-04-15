/**
 * 
 */
package com.vendor.vo.app;

import java.io.Serializable;

/**
 * 【补货APP】店铺信息
 * 
 * @author liujia on 2016年6月27日
 */
public class StoreReplenishTimeInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 店铺编号
	 */
	private String startTime;
	/**
	 * 结束时间
	 */
	private String endTime;

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

}
