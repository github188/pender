/**
 * 
 */
package com.vendor.service;
import java.sql.Date;
import java.util.List;
import java.util.Map;

import com.ecarry.core.web.core.Page;
import com.vendor.po.PointPlace;

/**
 * @author liujia on 2016年4月20日
 */
public interface IWechatService {
	
	final String ACCESS_TOKEN = "ACCESS_TOKEN";
	final String TOKEN_EXPIRED = "TOKEN_EXPIRED";
	final String TOKEN_TIME = "TOKEN_TIME";
	
	/**
	 * 获取用户信息
	 * 
	 * @param token 用户accessToken
	 * @param url
	 * @param openId
	 ***/
	Map<String, Object> getWxUser(String token, String url, String openId);

	/**
	 * 获取用户信息
	 * 
	 * @param token 用户accessToken
	 * @param url
	 * @param openId
	 ***/
	Map<String, Object> getWeChatUser(String token, String url, String openId);
	
	/***微信看店**********开始******/
	/**
	 * 【店铺经营】页面相关数据
	 */
	Map<String, Object> findStoreOperData(Page page, Date startTime, Date endTime, String pointNo, String factorDevNo);

	/**
	 * 【店铺经营】查询店铺设备数据
	 */
	List<PointPlace> findStoreDevices(Page page);
	
	/**
	 * 【设备管理】页面相关数据
	 */
	Map<String, Object> findDeviceOperData(Page page, Date startTime, Date endTime);

	/**
	 * 【补货计划】页面相关数据
	 */
	Map<String, Object> findReplenishmentData(Page page);

	/**
	 * 【补货计划】店铺商品补货信息
	 */
	Map<String, Object> findReplenishProds(Page page, Long storeId);
	/***微信看店**********结束******/
}
