/**
 * 
 */
package com.vendor.control.web;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.ecarry.core.domain.Menu;
import com.ecarry.core.web.control.BaseControl;
import com.ecarry.core.web.core.ContextUtil;
import com.ecarry.core.web.core.Page;
import com.vendor.po.Device;
import com.vendor.po.DeviceLog;
import com.vendor.po.Order;
import com.vendor.po.ProductLog;
import com.vendor.service.IDictionaryService;
import com.vendor.service.ILoginService;
import com.vendor.vo.web.BestSellerLists;

/**
 * @author dranson
 * 2011-1-17
 */
@Controller
public class LoginControl extends BaseControl {

	@Autowired
	private IDictionaryService dictionaryService;
	
	@Autowired
	private ILoginService loginService;
	
	@Value("${msg.sendMsgContent}")
	private String msg;
	
	@RequestMapping(value = "login.do", method = RequestMethod.GET)
	public ModelAndView login() {
		String pwdUpdateTime = loginService.saveLoginLog();
		ModelAndView view = new ModelAndView("frame.jsp");
		
		Map<Long, Menu> menuMap = new HashMap<Long, Menu>();
		List<Menu> menus = new ArrayList<Menu>();
		for (Menu menu : loginService.findAuthorities()) {
			if (menu.getParentId() == null) {
				// 一级菜单
				menuMap.put(menu.getId(), menu);
			} else {
				// 二级菜单
				if (menuMap.keySet().contains(menu.getParentId()))
					menuMap.get(menu.getParentId()).addMenu(menu);
			}
		}
		for (Map.Entry<Long, Menu> entry : menuMap.entrySet())
			menus.add(entry.getValue());
		
		try {
			view.addObject("menus", menus);
			String password = dictionaryService.findDefaultPassword();
			if (password == null)
				password = "123456";
			view.addObject("reset", ContextUtil.matches(password, ContextUtil.getUser().getPassword()));
			view.addObject("loginUser", getJson(ContextUtil.getUser(), "id,username,nickname,realName,portrait,orgId,orgCode,orgName,users,sellers,companyId,companyCode,companyName,currency,currencyRate"));
			if (pwdUpdateTime == null) {
				view.addObject("newUser", true);
			} else {
				view.addObject("newUser", false);
			}
			view.addObject("fileServer", dictionaryService.getFileServer());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return view;
	}
	
	/**
	 * 首页
	 ****/
	@RequestMapping(value = "/index/forward.do")
	public ModelAndView forwardIndex() {
		ModelAndView view = new ModelAndView("/index.jsp");
		return view;
	}
	
	/**
	 * 查询设备异常信息
	 * @param page
	 * @return
	 */
	@RequestMapping(value = "/findDeviceLogs.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<DeviceLog> findDeviceLogs(Page page) {
		return loginService.findDeviceLogs(page);
	}

	/**
	 * 查询商品异常信息
	 * @param page
	 * @return
	 */
	@RequestMapping(value = "/findProductLogs.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<ProductLog> findProductLogs(Page page) {
		return loginService.findProductLogs(page);
	}
	
	/**
	 * 销售额，销售量，平均单价，退款金额，网络异常设备
	 */
	@RequestMapping(value = "findSysData.json", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> findSysData() {
		return loginService.saveSysData();
	}
	
	
	/**
	 * 销售统计图 
	 */
	@RequestMapping(value = "findSalesAndAmount.json", method = RequestMethod.POST)
	public List<Order> findSalesAndAmount(Date startTime, Date endTime) {
		return loginService.findSalesData(startTime, endTime);
	}

	/**
	 * 销售排行榜
	 */
	@RequestMapping(value = "findBestSellerLists.json", method = RequestMethod.POST)
	@ResponseBody
	public BestSellerLists findBestSellerLists(Date date) {
		if (null == date)
			date = new Date(System.currentTimeMillis());
		
		return loginService.findBestSellerlists(date);
	}
	
	/**
	 * 查询货道售空设备信息
	 * @param page
	 * @return
	 */
	@RequestMapping(value = "/findSaleEmptyDevices.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<Device> findSaleEmptyDevices(Page page) {
		return loginService.findSaleEmptyDevices(page);
	}

	/**
	 * 店铺销售排行榜（所有）
	 * @param page
	 * @return
	 */
	@RequestMapping(value = "/findStoreSalesList.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<Order> findStoreSalesList(Page page, Date date) {
		if (null == date)
			date = new Date(System.currentTimeMillis());
		
		return loginService.findStoreSalesList(page, date);
	}
	
	/**
	 * 商品销售额排行榜（所有）
	 * @param page
	 * @return
	 */
	@RequestMapping(value = "/findStoreSalesAmountList.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<Order> findStoreSalesAmountList(Page page, Date date) {
		if (null == date)
			date = new Date(System.currentTimeMillis());
		
		return loginService.findStoreSalesAmountList(page, date);
	}

	/**
	 * 商品销售量排行榜（所有）
	 * @param page
	 * @return
	 */
	@RequestMapping(value = "/findStoreSalesVolumeList.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<Order> findStoreSalesVolumeList(Page page, Date date) {
		if (null == date)
			date = new Date(System.currentTimeMillis());
		
		return loginService.findStoreSalesVolumeList(page, date);
	}
	
}
