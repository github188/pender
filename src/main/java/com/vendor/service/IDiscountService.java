package com.vendor.service;

import java.util.List;

import com.ecarry.core.web.core.Page;
import com.vendor.po.Discount;
import com.vendor.po.Order;
import com.vendor.po.PointPlace;
import com.vendor.po.Product;

/**
* @ClassName: 限时打折Service
* @Description: TODO
* @author: duanyx
* @date: 2016年11月17日 下午3:55:07
*/
public interface IDiscountService {


	/**
	 * @Title: *1-分页查询限时打折列表
	 * @Description: TODO
	 * @return
	 * @return: List<MarketingTool>
	 */
	public List<Discount> findDisCount(Discount discount, Page page);


	/**
	 * @Title: *2-删除/下线打折活动
	 * @Description: TODO
	 * @param id
	 * @return: void
	 */
	public void delDiscount(Long ids,  Integer index);

	/**
	 * @Title: *3-1.第一步保存新增/编辑对象(保存打折基本信息)
	 * @param discount
	 * @return void
	 * @return: Discount
	 */
	public Discount saveDiscount(Discount discount);

	/**
	 * @Title: *4.-选择店铺时(查询店铺)
	 * @param discount
	 * @param productId 查商品已选店铺时需要传字段
	 * @return
	 * @return: List<PointPlace>
	 */
	public List<PointPlace> findPointPlacePage(Discount discount, Long productId, Page page);
	
	/**
	 * @Title: *5.-选择商品时(查询商品)
	 * @param discount
	 * @param page
	 * @param type 区分是查(1：已选的)还是(2：未选的)
	 * @return
	 * @return: List<Product>
	 */
	public List<Product> findProductPage(Discount discount, Page page, Integer type);
	
	/**
	 * @Title: *6-1保存第二步(保存商铺/商品)
	 * @param discount
	 * @return
	 * @return: void
	 */
	public void savePointPlaceProduct(Long id, String[] pointPlaceList, String type, String[] productList);
	
	/**
	 * @Title: *7.-删除选中商品(以及商品对应的商铺)
	 * @param @param discountId  删除打折活动的商品需要带
	 * @param @param productId 删除打折活动的商品需要带  
	 * @return void
	 * @throws
	 */
	public void deleteProductPointPlace(Long discountId, Long productId);
	
	/**
	 * @Title: *7-1.更新商品的(店铺)
	 * @param @param discount
	 * @return void
	 * @throws
	 */
	public void updateProduct(Discount discount);
	
	/**
	 * @Title: *8.-删除店铺
	 * @param @param discountId
	 * @param @param pointPlaceId
	 * @return void
	 * @throws
	 */
	public void delectPointPlace(Long discountId, Long pointPlaceId);
	
	/**
	 * @Title: *9.-这里是根据活动ID查询出订单数据
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param @param discountId
	 * @param @return
	 * @return List<Order>
	 * @throws
	 */
	public Order findOrderPage(Long discountId);

	
	/**
	 * 定时更新打折活动
	 */
	public void updateDiscountStateJob();
	
	
	/**
	 * 定时执行离线信息 
	 */
	public void updateOfflineStateJob();


	/**
	 * 根据离线消息Id删除打折活动的离线消息
	 * @param id
	 */
	public void deleteOffLineMessage(Long id) throws Exception;

	
}
