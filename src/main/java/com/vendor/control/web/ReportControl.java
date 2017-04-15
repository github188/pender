/**
 * 
 */
package com.vendor.control.web;

import java.sql.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.ecarry.core.domain.SysType;
import com.ecarry.core.web.control.BaseControl;
import com.ecarry.core.web.core.ContextUtil;
import com.ecarry.core.web.core.Page;
import com.ecarry.core.web.view.ExcelView;
import com.vendor.po.AppException;
import com.vendor.po.Device;
import com.vendor.po.DeviceAisle;
import com.vendor.po.Order;
import com.vendor.po.Refund;
import com.vendor.po.User;
import com.vendor.service.IDictionaryService;
import com.vendor.service.IReportService;

/**
 * @author dranson on 2015年12月7日
 */
@Controller
@RequestMapping(value = "/report")
public class ReportControl extends BaseControl {
	
	@Autowired
	private IDictionaryService dictionaryService;
	
	@Autowired
	private IReportService reportService;
	
	@RequestMapping(value = "order/find.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<Order> findOrders(Page page, Order order, Date startDate, Date endDate) {
		return reportService.findOrders(page, order, startDate, endDate);
	}
	
	@RequestMapping(value = "refund/forward.do", method = RequestMethod.GET)
	public ModelAndView forwardRefund() {
		ModelAndView view = new ModelAndView("/report/refund.jsp");
		return view;
	}
	
	@RequestMapping(value = "refund/find.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<Refund> findRefunds(Page page, Refund refund, Date startDate, Date endDate) {
		return reportService.findRefunds(page, refund, startDate, endDate);
	}
	
	@RequestMapping(value = "order/saveRefundState.json", method = RequestMethod.POST)
	public void saveRefundState(Refund refund) {
		reportService.saveUncompleteRefundOrder(refund);
	}	
	
	@RequestMapping(value = "device/forward.do", method = RequestMethod.GET)
	public ModelAndView forwardDevice() {
		ModelAndView view = new ModelAndView("/report/device.jsp");
		return view;
	}
	
	/**
	 * 跳转到异常信息页面
	 */
	@RequestMapping(value = "exception/forward.do", method = RequestMethod.GET)
	public ModelAndView forwardException() {
		ModelAndView view = new ModelAndView("/report/exception.jsp");
		User user = ContextUtil.getUser(User.class);
		view.addObject("_orgName", user.getOrgName());
		view.addObject("_orgId", user.getOrgId());
		return view;
	}

	/**
	 * 跳转到APP异常信息页面
	 */
	@RequestMapping(value = "appException/forward.do", method = RequestMethod.GET)
	public ModelAndView forwardAppException() {
		ModelAndView view = new ModelAndView("/report/appException.jsp");
		return view;
	}
	
	/**
	 * 查询APP异常信息
	 */
	@RequestMapping(value = "appException/find.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<AppException> findAppEceptions(Page page, AppException appException, Date startDate, Date endDate) {
		return reportService.findAppEceptions(page, appException, startDate, endDate);
	}
	
	/**
	 * 查询自己的设备
	 */
	@RequestMapping(value="exception/findOwnDevices.json")
	@ResponseBody
	public List<Device> findOwnDevices() {
		return reportService.findOwnDevices();
	}
	
	/**
	 * 统计设备销量信息
	 * @param page
	 * @param device
	 * @param startDate
	 * @param endDate
	 * @param timeSlot  时间下拉选项值
	 * @return
	 */
	@RequestMapping(value = "device/find.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<Device> findDevices(Page page, Device device, Date startDate, Date endDate) {
		return reportService.findDevices(page, device, startDate, endDate);
	}
	
	/**
	 * 统计设备产品销量信息
	 * @param page
	 * @param vender
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	@RequestMapping(value = "device/findSellerDevice.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<DeviceAisle> findSellerDevice(Page page, DeviceAisle deviceAisle, Date startDate, Date endDate) {
		return reportService.findSellerDevice(page, deviceAisle, startDate, endDate);
	}

	@RequestMapping(value = "device/export.xls", method = RequestMethod.GET)
	public ModelAndView exportDevice(Device device, Date startDate, Date endDate) {
		Page page = new Page();
		page.setCurPage(1);
		page.setPageSize(20000);	//	最大查询2万条
		List<Device> orders = reportService.findDevices(page, device, startDate, endDate);
		List<SysType> sysTypes = dictionaryService.findSysTypesByType("DEVICE_TYPE");
		Map<String, String> map = new HashMap<String, String>();
		for (SysType sysType : sysTypes)
			map.put(sysType.getCode(), sysType.getName());
		Map<String, Object> mapValues = new HashMap<String, Object>();
		mapValues.put("natrue", map);
		Map<String, String> titleMap = new LinkedHashMap<String, String>();
		titleMap.put("设备编号", "devNo");
		titleMap.put("设备地址", "address");
		titleMap.put("所属人", "orgName");
		titleMap.put("设备性质", "natrue");
		titleMap.put("销售总额", "salePrice");
		ExcelView view = new ExcelView(orders, titleMap, "设备销量统计列表", mapValues);
		return new ModelAndView(view);
	}

	@RequestMapping(value = "product/forward.do", method = RequestMethod.GET)
	public ModelAndView forwardProduct() {
		ModelAndView view = new ModelAndView("/report/product.jsp");
		return view;
	}
	
	/**
	 * 统计产品销量信息
	 * @param page
	 * @param device
	 * @param startDate
	 * @param endDate
	 * @param timeSlot  时间下拉选项值
	 * @return
	 */
	@RequestMapping(value = "product/find.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<DeviceAisle> findProducts(Page page, DeviceAisle deviceAisle, Date startDate, Date endDate) {
		return reportService.findProducts(page, deviceAisle, startDate, endDate);
	}

	@RequestMapping(value = "product/export.xls", method = RequestMethod.GET)
	public ModelAndView exportProduct(DeviceAisle deviceAisle, Date startDate, Date endDate) {
		Page page = new Page();
		page.setCurPage(1);
		page.setPageSize(20000);	//	最大查询2万条
		List<DeviceAisle> orders = reportService.findProducts(page, deviceAisle, startDate, endDate);
		Map<String, String> titleMap = new LinkedHashMap<String, String>();
		titleMap.put("商品名称", "productName");
		titleMap.put("零售价", "price");
		titleMap.put("销量", "sales");
		titleMap.put("销售额", "salesVolume");
		ExcelView view = new ExcelView(orders, titleMap, "商品销量统计列表");
		return new ModelAndView(view);
	}
	
}
