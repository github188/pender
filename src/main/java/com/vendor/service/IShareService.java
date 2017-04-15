/**
 * 
 */
package com.vendor.service;

/**
 * @author zhaoss on 2016年4月15日
 */
public interface IShareService {


	/**
	 * 保存wxUser 数据
	 * @param openId
	 * @param devId 设备Id
	 * @param orgId
	 * @param devNo 设备编号
	 */
    boolean saveWxUser(String openId,Long devId,Long orgId, String devNo);

    /**
     * 保存wxUser 数据
     * @param openId
     * @param devId 设备Id
     * @param orgId
     * @param devNo 设备编号
     */
    boolean saveWeChatUser(String openId,Long devId,Long orgId, String devNo);
}

