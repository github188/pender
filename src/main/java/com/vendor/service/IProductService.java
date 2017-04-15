package com.vendor.service;

import java.util.List;

import com.ecarry.core.web.core.Page;
import com.vendor.po.Cabinet;
import com.vendor.po.Category;
import com.vendor.po.Device;
import com.vendor.po.DeviceAisle;
import com.vendor.po.Orgnization;
import com.vendor.po.PointPlace;
import com.vendor.po.Product;

public interface IProductService {

	/**
	 * 查询当前设备下的货柜信息
	 */
	public List<Cabinet> findCabinets(Long deviceId);

	/**
	 * 查询当前设备下的设备商品信息
	 */
	public List<DeviceAisle> findDeviceProds(Long deviceId, Long cabinetId);
	
	/**
	 * 保存单个货柜商品上架信息
	 * @param cabinet
	 */
	public void saveShelf(Cabinet cabinet);
	
	/**
	 * 查询【经营分析】店铺信息（包括点击按钮查询）
	 * @param page
	 * @return
	 */
	public List<PointPlace> findSalesPlanStores(Page page, PointPlace pointPlace);

	/**
	 * 查找城市合伙人商品
	 * @param product 查询条件
	 * @param page 页面参数
	 ***/
	public List<Product> findInventory(Product product, Page page);
	/**
	 * 补货
	 ****/
	public void resotck();
	
	/**
	 * 查找设备
	 * @param device 查询条件
	 * @param page 分页
	 ***/
	public List<Device> findDeVice(Device device, Page page);

	/**
	 * 查找设备商品
	 * 
	 ***/
	public List<DeviceAisle> findDeviceProduct(DeviceAisle aisle, Page page);
	
	/**
	 * 分页查询商品库存管理列表
	 ***/
	public List<Product> findProductStock(Product product, Page page);
	
	/**
	 * 保存商品
	 ***/
	public void saveProductStock(Product product, long key, long[] fileIds);
	
	/**
	 * 删除商品
	 ***/
	public void deleteProductStock(Long[] ids);
	
	/**
	 * 分页查询平台供货商品列表
	 */
	public List<Product> findPlatformSupply(Product product, Page page);

	/**
	 * 查询供货对象
	 */
	public List<Orgnization> findSupplyObject();
	
	/**
	 * 进行平台供货
	 */
	public void savePlatformSupply(Long supplyOrgId, List<Product> products);
	
	/**
	 * 	分页查询类目维护数据
	 * @param page	分页信息
	 * @return	商品类目信息
	 */
	List<Category> findCategorieMaintains(Page page);
	
	/**
	 * 保存类目维护数据
	 * @param category	需要保存的商品类目信息
	 */
	void saveCategorieMaintains(Category category);
	
	/**
	 * 删除类目维护数据
	 * @param ids	需要删除商品类目信息的ID集
	 */
	void deleteCategorieMaintains(Long... ids);
	
	/**
	 * 定时任务更新广告的状态
	 */
	void updateAdvState();
	
	/**
	 * 查询类目是否已有相关的商品
	 * @param categories 类目结构
	 * @return 相关的商品记录数
	 */
	int findAboutCategory(List<Category> categories);

	List<DeviceAisle> findDeviceProds(Long deviceId, Integer cabinetNo);

	/**
	 * @Title: 虚拟商品【01】查看机构的虚拟商品(选中未选中的)
	 * @param factoryDevNo
	 *            设备编号
	 * @return
	 * @return: List<Product>
	 */
	List<Product> findDeviceVirtualProduct(String factoryDevNo, Page page);
	
    /**
     * @Title: 虚拟商品【02】保存上架信息(且上架)
     * @param factoryDevNo
     *            设备号
     * @param productIds
     *            商品集合数组
     * @return: void
     */
	void saveDeviceVirtualProduct(String factoryDevNo, Long... productIds);
	/**
	 * 商品下线，查询商品
	 * @param product
	 * @param sku
	 * @param page
	 * @return
	 */
	List<Product> findProductOffline(Product product, Page page);
	
	/**
	 * 商品正式下线
	 * @param product
	 */
	void saveProductOffline(Product product);
	
	/**
	 * 查询所有商品编码为负数的商品信息
	 ***/
	List<Product> findMinusProductNoGoods();

    /**
     * 虚拟商品推送上下架状态接口
     * @param machineNum 设备组号
     * @param messageId 消息id
     * @param exeState 执行状态
     * @return
     */
    void virtualGoodsExeState(String machineNum, String messageId, String exeState) throws Exception;
}
