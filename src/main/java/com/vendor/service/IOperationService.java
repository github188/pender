/**
 * 
 */
package com.vendor.service;

import java.sql.Date;
import java.util.List;
import java.util.Map;

import com.ecarry.core.web.core.Page;
import com.vendor.po.*;

public interface IOperationService {
	
	/**
	 * 查询【经营分析】店铺信息（包括点击按钮查询）
	 * @param page
	 * @return
	 */
	List<PointPlace> findAnalysiStores(Page page, PointPlace pointPlace);
	
	/**
	 * 取得【经营分析】各项累计数据
	 */
	Map<String, Object> findSysData(PointPlace pointPlace);
	
	/**
	 * 取得【经营分析】销售数据分析
	 */
	Map<String, Object> findSalesData(PointPlace pointPlace, Date startDate, Date endDate);

	/**
	 * 取得导出用【经营分析】销售数据分析
	 */
	List<Order> findExportSalesData(Page page, PointPlace pointPlace, Date startDate, Date endDate);

	/**
	 * 取得【经营分析】商品销售饼状图
	 */
	Map<String, Object> findProductSalesPieChartData(PointPlace pointPlace, Date startDate, Date endDate);


	/**
	 * 取得【经营分析】商品销售数据
	 */
	List<Order> findProductSalesData(Page page, PointPlace pointPlace, Date startDate, Date endDate);

	/**
	 * 取得导出用【经营分析】商品销售数据
	 */
	List<Order> findExportProductSalesData(Page page, PointPlace pointPlace, Date startDate, Date endDate);

	/**
	 * 查询【店铺库存】店铺信息
	 * @param page
	 * @return
	 */
	List<PointPlace> findStockStores(Page page, PointPlace pointPlace);

	/**
	 * 查询【店铺库存】店铺商品补货信息
	 * @param page
	 * @return
	 */
	List<DeviceAisle> findReplenishProds(Page page, Long[] ids);

	/**
	 * 查询【店铺库存】店铺补货清单信息
	 * @param page
	 * @return
	 */
	List<PointPlace> findStoreProds(Long[] ids);
	
	/**
	 * 	分页查询【订单信息】
	 * @param page	分页信息
	 * @param order	查询条件
	 * @param startDate	起始日期
	 * @param endDate	截止日期
	 * @return	订单信息
	 */
	List<Order> findOrders(Page page, Order order, Date startDate, Date endDate);
	
	/**
	 * 查询【订单退款明细信息】
	 * @param refund
	 * @return
	 */
	List<Refund> findRefundDetail(Refund refund);

	/**
	 * 	分页查询导出用【订单信息】
	 * @param page	分页信息
	 * @param order	查询条件
	 * @param startDate	起始日期
	 * @param endDate	截止日期
	 * @return	订单信息
	 */
	List<OrderDetail> findExportOrders(Page page, Order order, Date startDate, Date endDate);
	
	/**
	 * 保存退款信息
	 * @param refund	需要保存的退款信息
	 */
	void saveRefund(Refund refund, User user) throws Exception;

	/**
	 * 查询订单退款信息
	 * @param refund 退款查询参数信息
	 */
	List<Refund> findOrderRefund(Refund refund);
	
	/**
	 * 更新微信退款状态
	 */
	void saveRefundStateJob() throws Exception;
	
	/**
	 * 【店铺库存】店铺一键改价
	 * @return
	 */
	void saveStoresPrice(PointPlace pointPlace);
	
	/**
	 * 查询【店铺库存】店铺商品补货信息
	 * @param page
	 * @return
	 */
	List<DeviceAisle> findAllProds(Page page, Product product);
	
	/**
	 * 【商品一键改价】查询商品所在的店铺信息
	 * @return
	 */
	List<PointPlace> findStoresByProdId(Page page, Long[] finalProductIds);

	
	/**
	 * 【店铺库存】商品一键改价
	 * @return
	 */
	void saveProdsPrice(DeviceAisle deviceAisle);
	
	/**
	 * 【店铺库存】根据店铺查询店铺下的货柜信息
	 */
	List<Cabinet> findCabinetsByStoreId(Long storeId);
	
	/**
	 * 【店铺库存】查询货柜商品补货信息
	 * @return
	 */
	List<DeviceAisle> findReplenishProdsByCabId(Page page, Long cabId);

    /**
     * @Title: 更新商品的排序序号
     * @param cabId
     *            货柜ID
     * @param productId
     *            商品ID
     * @param serialNumber
     *            排序序号
     * @param stickType
     *            (置顶状态:1置顶,2取消置顶)
     * @return: void
     */
    void updateDeviceAisleSerialNumber(Long cabId, Long productId, Integer serialNumber, Integer stickType);

	/**
	 * 【店铺库存】更新商品序号
	 * @param sortingRecommendeds
	 *
	 */
	void updateSortingRecommended(List<SortingRecommended> sortingRecommendeds);

	/**
	 * 根据组号查询本设备组上的商品(包括实物、虚拟商品)
	 * @param factoryDevNo
	 * @return
	 */
    List<DeviceAisle> findProductByFactoryDevNo(String factoryDevNo);

}

