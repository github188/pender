/**
 * 
 */
package com.vendor.service;

import java.util.List;

import com.ecarry.core.web.core.Page;
import com.vendor.po.Device;

public interface IUserInfoService {
	/**
	 * 根据org_id查询设备
	 * @param orgId
	 * @return
	 */
	List<Device> findBindingDevices(Long orgId);
	
	/**
	 * 分页条件查询设备信息
	 */
	List<Device> findVenderPartnerDevices(Page page, Device device, String startDevNo, String endDevNo);
	
	/**
	 * 校验所选设备号是否已上架了商品
	 * @param finalDevNos
	 */
	void findIsShelving(String finalDevNos);
	
	/**
	 * 解绑城市合伙人/网点设备
	 * @param devNos
	 */
	void saveVenderpartnerUnBindDevices(String... devNos);
	
}

