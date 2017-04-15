package com.vendor.vo.web;

import java.util.List;

import com.ecarry.core.util.MathUtil;

/**
 * 消费者数据
 * @author liujia 2016年11月7日
 */
public class ConsumerData {
	/**
	 * 复购率
	 */
	private Double rebuyRate = 0.0;
	
	/**
	 * 新用户数
	 */
	private Integer newUserCount = 0;
	
	/**
	 * 用户总户数
	 */
	private Integer allUserCount = 0;
	
	/**
	 * 用户消费明细
	 */
	private List<Consumption> consumptions;

	public Double getRebuyRate() {
		return MathUtil.round(null == this.rebuyRate ? 0 : this.rebuyRate, 2);
	}

	public void setRebuyRate(Double rebuyRate) {
		this.rebuyRate = rebuyRate;
	}

	public Integer getNewUserCount() {
		return newUserCount;
	}

	public void setNewUserCount(Integer newUserCount) {
		this.newUserCount = newUserCount;
	}

	public Integer getAllUserCount() {
		return allUserCount;
	}

	public void setAllUserCount(Integer allUserCount) {
		this.allUserCount = allUserCount;
	}

	public List<Consumption> getConsumptions() {
		return consumptions;
	}

	public void setConsumptions(List<Consumption> consumptions) {
		this.consumptions = consumptions;
	}
	
}
