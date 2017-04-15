/**
 * 
 */
package com.vendor.service;

import java.util.List;

import com.ecarry.core.web.core.Page;
import com.vendor.po.Category;
import com.vendor.po.DevCombination;
import com.vendor.po.Device;
import com.vendor.po.DeviceAisle;
import com.vendor.po.PointPlace;

/**
 * @author zhaoss on 2016年3月25日
 */
public interface IPlatformService {

	/**
	 * 循环递归查询商品类目
	 * @param parentId	商品类目父ID
	 * @return	树状结构的商品类目集
	 */
	List<Category> findCyclicCategories(Long parentId);
	
	/**
	 * 校验所选设备号是否已上架了商品
	 * @param finalDevNos
	 */
	void findIsShelving(String finalDevNos);
	
	/**
	 * 分页条件查询设备信息
	 */
	List<Device> findVenderPartnerDevices(Page page, Device device, String startDevNo, String endDevNo, String devNos);

	/**
	 * 解绑城市合伙人/网点设备
	 * @param devNos
	 */
	void saveVenderpartnerUnBindDevices(String... devNos);

	/**
	 * 导入设备信息
	 * @param device
	 * @param key
	 * @param fileIds
	 */
	void saveDevices(Device device, long key, String[] fileIds) throws Exception;
	
	/**
	 * 根据org_id查询设备
	 * @param orgId
	 * @return
	 */
	List<Device> findBindingDevices(Long orgId);

	/**
	 * 查询设备货道信息
	 * @param object
	 * @return
	 */
	List<DeviceAisle> findDeviceAisles(DeviceAisle deviceAisles);
	/**
	 * 查询设备货道
	 * @param page
	 * @param deviceAisle
	 * @return
	 */
	List<DeviceAisle> findSellerDevice(Page page, DeviceAisle deviceAisle);

	/**
	 * 查询设备
	 * @param page
	 * @param device
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	List<Device> findDevices(Page page, Device device);

	/**
	 * 查询点位
	 * @param page
	 * @param pointPlace
	 * @return
	 */
	List<PointPlace> findPointPlaces(Page page, PointPlace pointPlace);
	/**
	 * 保存设备信息
	 * @param device
	 */
	void saveDevice(Device device);

	/**
	 * 保存点位信息
	 * @param device
	 */
	void savePoint(PointPlace pointPlace);
	/**
	 * 删除设备信息
	 * @param ids
	 */
	void deleteDevices(Long[] ids);
	/**
	 * 删除点位信息
	 * @param ids
	 */
	void deletePoints(Long[] ids);
	
	/**
	 * 获取设备编号
	 * 
	 * ****/
	List<Device> findDevices();
	
	/**
	 * 删除文件
	 * @param infoId
	 * @param type
	 */
	void deleteFile(Long infoId, Integer type);
	
	void addDevCombination(Integer combinationNo, Long pointplaceID);
	void deleteDevCombination(String identity);
		
	List<Device> findDevCombination(Page page, Long orgId);
	List<DevCombination> findBindDevCombination(Page page, Long pointplaceId);
	
	/**
	 * 我的店铺---查询点位信息
	 * @param page
	 * @param pointPlace
	 * @return
	 */
	List<PointPlace> findMyStores(Page page, PointPlace pointPlace);
}

