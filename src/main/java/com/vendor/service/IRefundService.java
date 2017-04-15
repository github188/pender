package com.vendor.service;

import com.vendor.po.Order;
import com.vendor.po.Refund;
import com.vendor.po.User;

import java.util.List;

/**
 * Created by Chris Zhu on 2017/3/27.
 *
 * @author Chris Zhu
 */
public interface IRefundService
{
	/**
	 * 退款
	 *
	 * @param order 订单
	 */
	void refund(Order order);

	/**
	 * 保存退款信息
	 *
	 * @param refund 退款单
	 * @param user   用户
	 */
	void saveRefundOrder(Refund refund, User user);

	/**
	 * 保存退款单
	 *
	 * @param order 订单
	 * @return 退款单集合
	 */
	List<Refund> saveRefundOrder(Order order);

	/**
	 * 根据订单号查找退款单
	 *
	 * @param orderNo 内部订单号
	 * @return 退款单集合
	 */
	List<Refund> findRefundByOrderNo(String orderNo);

	/**
	 * 定时退款
	 */
	void timingRefund();

}
