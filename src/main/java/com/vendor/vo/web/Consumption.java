package com.vendor.vo.web;

/**
 * 用户消费明细
 * @author liujia 2016年11月7日
 */
public class Consumption {
	/**
	 * 描述信息，eg:消费1次
	 */
	private String name;
	
	/**
	 * 用户数
	 */
	private Integer userCount;
	
	public Consumption(String name, Integer userCount) {
		this.name = name;
		this.userCount = userCount;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getUserCount() {
		return userCount;
	}

	public void setUserCount(Integer userCount) {
		this.userCount = userCount;
	}
	
}
