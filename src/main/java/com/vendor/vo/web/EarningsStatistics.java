package com.vendor.vo.web;

import java.io.Serializable;

import com.ecarry.core.util.MathUtil;

/**
 * 收益统计
 * 
 ***/
public class EarningsStatistics implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 微商城今日收益
	 */
	private double ecarryTodayEarnings;

	/**
	 * 微商城昨日收益
	 */
	private double ecarryYstdEarnings;

	/**
	 * 微商城累计收益
	 */
	private double ecarryAllEarnings;

	/**
	 * 微商城今日新增用户数
	 */
	private int ecarryTodayUsers;

	/**
	 * 微商城昨日新增用户数
	 */
	private int ecarryYstdUsers;

	/**
	 * 微商城累计用户数
	 */
	private int ecarryAllUsers;

	/**
	 * @return the ecarryTodayEarnings
	 */
	public double getEcarryTodayEarnings() {
		return MathUtil.round(ecarryTodayEarnings, 2);
	}

	/**
	 * @param ecarryTodayEarnings the ecarryTodayEarnings to set
	 */
	public void setEcarryTodayEarnings(double ecarryTodayEarnings) {
		this.ecarryTodayEarnings = ecarryTodayEarnings;
	}

	/**
	 * @return the ecarryYstdEarnings
	 */
	public double getEcarryYstdEarnings() {
		return MathUtil.round(ecarryYstdEarnings, 2);
	}

	/**
	 * @param ecarryYstdEarnings the ecarryYstdEarnings to set
	 */
	public void setEcarryYstdEarnings(double ecarryYstdEarnings) {
		this.ecarryYstdEarnings = ecarryYstdEarnings;
	}

	/**
	 * @return the ecarryAllEarnings
	 */
	public double getEcarryAllEarnings() {
		return MathUtil.round(ecarryAllEarnings, 2);
	}

	/**
	 * @param ecarryAllEarnings the ecarryAllEarnings to set
	 */
	public void setEcarryAllEarnings(double ecarryAllEarnings) {
		this.ecarryAllEarnings = ecarryAllEarnings;
	}

	/**
	 * @return the ecarryTodayUsers
	 */
	public int getEcarryTodayUsers() {
		return ecarryTodayUsers;
	}

	/**
	 * @param ecarryTodayUsers the ecarryTodayUsers to set
	 */
	public void setEcarryTodayUsers(int ecarryTodayUsers) {
		this.ecarryTodayUsers = ecarryTodayUsers;
	}

	/**
	 * @return the ecarryYstdUsers
	 */
	public int getEcarryYstdUsers() {
		return ecarryYstdUsers;
	}

	/**
	 * @param ecarryYstdUsers the ecarryYstdUsers to set
	 */
	public void setEcarryYstdUsers(int ecarryYstdUsers) {
		this.ecarryYstdUsers = ecarryYstdUsers;
	}

	/**
	 * @return the ecarryAllUsers
	 */
	public int getEcarryAllUsers() {
		return ecarryAllUsers;
	}

	/**
	 * @param ecarryAllUsers the ecarryAllUsers to set
	 */
	public void setEcarryAllUsers(int ecarryAllUsers) {
		this.ecarryAllUsers = ecarryAllUsers;
	}

}
