/**
 * 
 */
package com.vendor.control.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ecarry.core.web.control.BaseControl;
import com.ecarry.core.web.core.Page;
import com.vendor.po.ReplenishmentAppVersion;
import com.vendor.service.IVendingService;
import com.vendor.util.Commons;
import com.vendor.vo.app.ProductReplenishInfo;
import com.vendor.vo.app.StoreInfo;
import com.vendor.vo.common.ResultAppBase;

/**
 * 提供在安全权限管控下的API
 */
@Controller
@RequestMapping(value = "/m/auth")
public class MobileAuthControl extends BaseControl {

	@Autowired
	private IVendingService vendingService;

	/****************************补货APP接口**************开始***********************/
	
	/**
	 * 【补货APP接口】4.修改密码
	 */
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "savePassword.json", method = RequestMethod.POST)
	@ResponseBody
	public ResultAppBase savePassword(String oldPassword, String password) {
		logger.info("********【补货APP接口】4.修改密码*****开始*****");
		ResultAppBase rab= new ResultAppBase();
		if (StringUtils.isEmpty(oldPassword) || StringUtils.isEmpty(password)) {
			rab.setResultMessage("参数不允许为空");
			return rab;
		}
		try {
			rab = vendingService.savePassword(rab, oldPassword, password);
		} catch (Exception e) {
			rab.setResultMessage("修改密码失败");
			logger.error(e.getMessage(), e);
		}
		logger.info("********【补货APP接口】4.修改密码*****结束*****");
		return rab;
	}
	
	/**
	 * 【补货APP接口】5.查询店铺信息(补货/全部)
	 * 
	 * @param page 分页参数
	 * @param time 时间参数  今天0，明天1 
	 * @param type 查询类型   1：待补货的店铺  3：全部店铺
	 * @param prov 省
	 * @param city 市
	 * @param dist 区
	 * @return
	 */
	@RequestMapping(value = "findStores.json", method = RequestMethod.POST)
	@ResponseBody
	public ResultAppBase<List<StoreInfo>> findStores(Page page, Integer time, Integer type, String prov, String city, String dist) {
		logger.info("********【补货APP接口】5.查询店铺信息(补货/全部)*****开始*****");
		ResultAppBase<List<StoreInfo>> rab= new ResultAppBase<List<StoreInfo>>();
		if (null == time || (Commons.TIME_TODAY != time && Commons.TIME_TOMORROW != time) || null == type || (Commons.FIND_STORE_TYPE_REPLENISH != type && Commons.FIND_STORE_TYPE_ALL != type)) {
			rab.setResultMessage("非法的参数信息");
			return rab;
		}
		try {
			rab = vendingService.findStores(rab, page, time, type, prov, city, dist);
		} catch (Exception e) {
			rab.setResultMessage("查询店铺信息失败");
			logger.error(e.getMessage(), e);
		}
		logger.info("********【补货APP接口】5.查询店铺信息(补货/全部)*****结束*****");
		return rab;
	}
	
	/**
	 * 【补货APP接口】6.查询店铺设备商品补货信息
	 * 
	 * @param page 分页参数
	 * @param storeNo 区
	 * @return
	 */
	@RequestMapping(value = "findReplenishProducts.json", method = RequestMethod.POST)
	@ResponseBody
	public ResultAppBase<List<ProductReplenishInfo>> findReplenishProducts(Page page, String factoryDevNo) {
		logger.info("********【补货APP接口】6.查询店铺商品补货信息*****开始*****");
		ResultAppBase<List<ProductReplenishInfo>> rab= new ResultAppBase<List<ProductReplenishInfo>>();
		try {
			rab = vendingService.findReplenishProducts(rab, page, factoryDevNo);
		} catch (Exception e) {
			rab.setResultMessage("查询店铺信息失败");
			logger.error(e.getMessage(), e);
		}
		logger.info("********【补货APP接口】6.查询店铺商品补货信息*****结束*****");
		return rab;
	}

	/**
	 * 【补货APP接口】7.获取APP最新版本
	 * 
	 * @return
	 */
	@RequestMapping(value = "findReplenishAppVersion.json", method = RequestMethod.POST)
	@ResponseBody
	public ResultAppBase<ReplenishmentAppVersion> findReplenishAppVersion() {
		logger.info("********【补货APP接口】7.获取APP最新版本*****开始*****");
		ResultAppBase<ReplenishmentAppVersion> rab= new ResultAppBase<ReplenishmentAppVersion>();
		try {
			rab = vendingService.findReplenishAppVersion(rab);
		} catch (Exception e) {
			rab.setResultMessage("获取APP最新版本失败");
			logger.error(e.getMessage(), e);
		}
		logger.info("********【补货APP接口】7.获取APP最新版本*****结束*****");
		return rab;
	}
	
	
	
	/****************************补货APP接口**************结束***********************/
	
}
