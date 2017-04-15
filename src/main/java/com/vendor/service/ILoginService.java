/**
 * 
 */
package com.vendor.service;

import java.sql.Date;
import java.util.List;
import java.util.Map;

import com.ecarry.core.domain.Menu;
import com.ecarry.core.web.core.Page;
import com.vendor.po.Device;
import com.vendor.po.DeviceLog;
import com.vendor.po.Order;
import com.vendor.po.ProductLog;
import com.vendor.vo.web.BestSellerLists;

/**
 * @author dranson 2011-3-22
 */
public interface ILoginService {
	/**
	 * 查询权限菜单
	 * 
	 * @return 权限菜单
	 */
	List<Menu> findAuthorities();

	/**
	 * 记录登录日志
	 */
	String saveLoginLog();
	
	/**
	 * 首页用系统各数据
	 */
	Map<String, Object> saveSysData();
	
	/**
	 * 查询销售数据
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	List<Order> findSalesData(Date startTime, Date endTime);
	
	/**
	 * 查询设备异常信息
	 * @param page
	 * @return
	 */
	List<DeviceLog> findDeviceLogs(Page page);
	
	/**
	 * 查询商品异常信息
	 * @param page
	 * @return
	 */
	List<ProductLog> findProductLogs(Page page);

	/**
	 * 销售排行榜
	 * @param date
	 * @return
	 */
	BestSellerLists findBestSellerlists(Date date);
	
	/**
	 * 查询货道售空设备信息
	 * @param page
	 * @return
	 */
	List<Device> findSaleEmptyDevices(Page page);
	
	/**
	 * 店铺销售排行榜（所有）
	 * @param page
	 * @return
	 */
	List<Order> findStoreSalesList(Page page, Date date);
	
	/**
	 * 商品销售额排行榜（所有）
	 * @param page
	 * @return
	 */
	List<Order> findStoreSalesAmountList(Page page, Date date);
	
	/**
	 * 商品销售量排行榜（所有）
	 * @param page
	 * @return
	 */
	List<Order> findStoreSalesVolumeList(Page page, Date date);
}
