/**
 * 
 */
package com.vendor.service;

import java.sql.Date;
import java.util.List;

import com.ecarry.core.web.core.Page;
import com.vendor.po.AppException;
import com.vendor.po.Device;
import com.vendor.po.DeviceAisle;
import com.vendor.po.Order;
import com.vendor.po.Refund;

/**
 * @author dranson on 2015年12月24日
 */
public interface IReportService {
	/**
	 * 	分页查询订单信息
	 * @param page	分页信息
	 * @param order	查询条件
	 * @param startDate	起始日期
	 * @param endDate	截止日期
	 * @return	订单信息
	 */
	List<Order> findOrders(Page page, Order order, Date startDate, Date endDate);
	/**
	 * 刷新订单付款状态
	 * @param id	需要刷新的订单ID
	 */
	void saveOrderPaidState(Long id);
	/**
	 * 	分页查询订单退款信息
	 * @param page	分页信息
	 * @param refund	查询条件
	 * @param startDate	起始日期
	 * @param endDate	截止日期
	 * @return	订单信息
	 */
	List<Refund> findRefunds(Page page, Refund refund, Date startDate, Date endDate);
	/**
	 * 更新订单退款状态
	 * @param refund	需要更新退款状态的退款信息，可以为空查询所有
	 */
	void saveUncompleteRefundOrder(Refund refund);
	
	/**
	 * 查询APP异常信息
	 */
	List<AppException> findAppEceptions(Page page, AppException appException, Date startDate, Date endDate);
	
	/**
	 * 查询自己的设备
	 * @return
	 */
	List<Device> findOwnDevices();
	
	/**
	 * 分页查询设备销量统计
	 * @param page
	 * @param device
	 * @param startDate
	 * @param endDate
	 * @param timeSlot
	 * @return
	 */
	List<Device> findDevices(Page page, Device device, Date startDate, Date endDate);
	/**
	 * 分页查询设备产品销量
	 * @param page
	 * @param deviceAisle
	 * @return
	 */
	List<DeviceAisle> findSellerDevice(Page page, DeviceAisle deviceAisle, Date startDate, Date endDate);
	/**
	 * 分页查询产品销量统计
	 * @param page
	 * @param deviceAisle
	 * @param startDate
	 * @param endDate
	 * @param timeSlot
	 * @return
	 */
	List<DeviceAisle> findProducts(Page page, DeviceAisle deviceAisle, Date startDate, Date endDate);
}
