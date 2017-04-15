package com.vendor.control.app;

import java.sql.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.ecarry.core.exception.BusinessException;
import com.ecarry.core.web.control.BaseControl;
import com.ecarry.core.web.core.Page;
import com.vendor.po.PointPlace;
import com.vendor.service.IWechatService;

@RestController
@RequestMapping(value = "/wechat")
public class WechatControl extends BaseControl {
	
	@Autowired
	private IWechatService wechatService;
	
	@RequestMapping(value = "index/forward.do")
	public ModelAndView forwardLogin() {
		ModelAndView view = new ModelAndView("/wechat/index.jsp");
		return view;
	}

	/**
	 * 成功登录，跳转到首页
	 ****/
	@RequestMapping(value = "/session/list.do")
	public ModelAndView forwardList() {
		ModelAndView view = new ModelAndView("/wechat/page/store.html");
		return view;
	}
	
	/***微信看店**********开始******/
	/**
	 * 【店铺经营】页面相关数据
	 */
	@RequestMapping(value = "/findStoreOperData.json", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> findStoreOperData(Page page, Date startTime, Date endTime, String storeNo, String factorDevNo) {
		return wechatService.findStoreOperData(page, startTime, endTime, storeNo, factorDevNo);
	}

	/**
	 * 【店铺经营】查询店铺设备数据
	 */
	@RequestMapping(value = "/findStoreDevices.json", method = RequestMethod.POST)
	@ResponseBody
	public List<PointPlace> findStoreDevices(Page page) {
		return wechatService.findStoreDevices(page);
	}
	
	/**
	 * 【设备管理】页面相关数据
	 */
	@RequestMapping(value = "/findDeviceOperData.json", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> findDeviceOperData(Page page, Date startTime, Date endTime) {
		return wechatService.findDeviceOperData(page, startTime, endTime);
	}
	
	/**
	 * 【补货计划】页面相关数据
	 */
	@RequestMapping(value = "/findReplenishmentData.json", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> findReplenishmentData(Page page) {
		return wechatService.findReplenishmentData(page);
	}
	
	/**
	 * 【补货计划】店铺商品补货信息
	 * @return
	 */
	@RequestMapping(value = "/findReplenishProds.json", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> findReplenishProds(Page page, Long storeId) {
		if (null == storeId)
			throw new BusinessException("非法请求");
		
		return wechatService.findReplenishProds(page, storeId);
	}
	/***微信看店**********结束******/
	

}
