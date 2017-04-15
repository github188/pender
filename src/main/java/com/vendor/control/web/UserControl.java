package com.vendor.control.web;

import java.sql.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.ecarry.core.exception.BusinessException;
import com.ecarry.core.web.control.BaseControl;
import com.ecarry.core.web.core.ContextUtil;
import com.ecarry.core.web.core.Page;
import com.vendor.po.Device;
import com.vendor.po.Orgnization;
import com.vendor.po.User;
import com.vendor.service.ISystemService;
import com.vendor.service.IUserInfoService;
import com.vendor.util.Commons;

@Controller
@RequestMapping(value = "/user")
public class UserControl extends BaseControl {
	
	@Autowired
	private ISystemService systemService;

	@Autowired
	private IUserInfoService userInfoService;
	
	@RequestMapping(value = "vender/forward.do", method = RequestMethod.GET)
	public ModelAndView forwardVender() {
		ModelAndView view = new ModelAndView("/user/vender.jsp");
		return view;
	}
	
	@RequestMapping(value = "partner/forward.do", method = RequestMethod.GET)
	public ModelAndView forwardPartner() {
		ModelAndView view = new ModelAndView("/user/partner.jsp");
		User user = ContextUtil.getUser(User.class);
		if (user != null){
			view.addObject("_orgId", user.getOrgId());
			view.addObject("_orgName", user.getOrgName());
		}
		return view;
	}
	
	/**
	 * 分页查询网点信息
	 * @param page
	 * @param orgnization
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	@RequestMapping(value = "partner/find.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<Orgnization> findDotNet(Page page, Orgnization orgnization, Date startDate, Date endDate) {
		User user = ContextUtil.getUser(User.class);
		orgnization.setParentId(user.getOrgId());
		return systemService.findOrgnizations(page, orgnization, startDate, endDate);
	}
	
	/**
	 * 保存网点信息
	 * @param org
	 */
	@RequestMapping(value = "partner/save.json", method = RequestMethod.POST)
	public void saveDotNet(Orgnization org) {
		org.setSort("1");
		org.setState(Commons.ORG_STATE_ENABLE);//启用
		User user = ContextUtil.getUser(User.class);
		org.setParentId(user.getOrgId());
		
		systemService.saveVenderPartnerOrgnization(org);
	}
	
	/**
	 * 删除城市合伙人信息
	 * @param ids
	 */
	@RequestMapping(value = "partner/delete.json", method = RequestMethod.POST)
	public void deleteDotNets(Long[] ids) {
		systemService.deleteOrgnizations(ids);
	}
	
	@RequestMapping(value = "partner/findBindingDevices.json", method = RequestMethod.POST)
	public List<Device> findPartnerBindingDevices(Long orgId) {
		return userInfoService.findBindingDevices(orgId);
	}
	
	@RequestMapping(value = "partner/findVenderPartnerDevices.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<Device> findPartnerVenderPartnerDevices(Page page, Device device, String startFactoryDevNo, String endFactoryDevNo) {
		User user = ContextUtil.getUser(User.class);
		device.setOrgId(user.getOrgId());
		return userInfoService.findVenderPartnerDevices(page, device, startFactoryDevNo, endFactoryDevNo);
	}
	
	/**
	 * 校验所选设备号是否已上架了商品
	 */
	@RequestMapping(value = "partner/findIsShelving.json", method = RequestMethod.POST)
	public void findIsShelvingPartner(String finalDevNos) {
		userInfoService.findIsShelving(finalDevNos);
	}
	
	@RequestMapping(value = "partner/findUnBindingVenderPartnerDevices.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<Device> findPartnerUnBindingVenderPartnerDevices(Page page, Device device, String startFactoryDevNo, String endFactoryDevNo) {
		return userInfoService.findVenderPartnerDevices(page, device, startFactoryDevNo, endFactoryDevNo);
	}
	
	@RequestMapping(value = "partner/saveUnBindDevices.json", method = RequestMethod.POST)
	public void savePartnerUnBindDevices(String[] devNos) {
		userInfoService.saveVenderpartnerUnBindDevices(devNos);
	}
	
	/**
	 * 分页查询城市合伙人信息
	 * @param page
	 * @param orgnization
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	@RequestMapping(value = "vender/find.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<Orgnization> findCityPartner(Page page, Orgnization orgnization, Date startDate, Date endDate) {
		User user = ContextUtil.getUser(User.class);
		orgnization.setParentId(user.getOrgId());
		return systemService.findOrgnizations(page, orgnization, startDate, endDate);
	}
	
	/**
	 * 保存城市合伙人信息
	 * @param org
	 */
	@RequestMapping(value = "vender/save.json", method = RequestMethod.POST)
	public void saveCityPartner(Orgnization org) {
		org.setSort("1");
		org.setState(Commons.ORG_STATE_ENABLE);//启用
		User user = ContextUtil.getUser(User.class);
		org.setParentId(user.getOrgId());
		
		systemService.saveVenderPartnerOrgnization(org);
	}
	
	/**
	 * 删除城市合伙人信息
	 * @param ids
	 */
	@RequestMapping(value = "vender/delete.json", method = RequestMethod.POST)
	public void deleteCityPartners(Long[] ids) {
		systemService.deleteOrgnizations(ids);
	}
	
	/**
	 * 查询设备信息
	 */
	@RequestMapping(value = "vender/findBindingDevices.json", method = RequestMethod.POST)
	public List<Device> findBindingDevices(Long orgId) {
		return userInfoService.findBindingDevices(orgId);
	}
	
	/**
	 * 分页查询设备信息
	 * @param page
	 * @param device
	 * @param startDevNo
	 * @param endDevNo
	 * @return
	 */
	@RequestMapping(value = "vender/findVenderPartnerDevices.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<Device> findVenderPartnerDevices(Page page, Device device, String startFactoryDevNo, String endFactoryDevNo) {
		User user = ContextUtil.getUser(User.class);
		device.setOrgId(user.getOrgId());
		return userInfoService.findVenderPartnerDevices(page, device, startFactoryDevNo, endFactoryDevNo);
	}
	
	/**
	 * 校验所选设备号是否已上架了商品
	 */
	@RequestMapping(value = "vender/findIsShelving.json", method = RequestMethod.POST)
	public void findIsShelving(String finalDevNos) {
		userInfoService.findIsShelving(finalDevNos);
	}
	
	/**
	 * 分页查询需解绑的设备信息
	 * @param page
	 * @param device
	 * @param startDevNo
	 * @param endDevNo
	 * @return
	 */
	@RequestMapping(value = "vender/findUnBindingVenderPartnerDevices.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<Device> findUnBindingVenderPartnerDevices(Page page, Device device, String startFactoryDevNo, String endFactoryDevNo) {
		return userInfoService.findVenderPartnerDevices(page, device, startFactoryDevNo, endFactoryDevNo);
	}
	
	/**
	 * 解绑城市合伙人/网点设备
	 * @param devNos
	 */
	@RequestMapping(value = "vender/saveUnBindDevices.json", method = RequestMethod.POST)
	public void saveVenderUnBindDevices(String[] devNos) {
		userInfoService.saveVenderpartnerUnBindDevices(devNos);
	}
	
	@RequestMapping(value = "platformUser/forward.do", method = RequestMethod.GET)
	public ModelAndView forwardPlatformUser() {
		ModelAndView view = new ModelAndView("/user/platformUser.jsp");
		return view;
	}
	
	@RequestMapping(value = "account/forward.do", method = RequestMethod.GET)
	public ModelAndView forwardAccount() {
		ModelAndView view = new ModelAndView("/user/account.jsp");
		return view;
	}
	
/**************************************开始**************************************************************/
	
	/**
	 * 保存平台机构信息
	 * @param org
	 */
	@RequestMapping(value = "platform/savePlatformOrg.json", method = RequestMethod.POST)
	public Orgnization savePlatformOrg(Orgnization org, String[] devNos) {
		if (Commons.isSystemUser() && org.getId() == null && org.getParentId() == null)
			throw new BusinessException("请通过平台账户功能新增公司信息");
		systemService.saveOrgnization(org, devNos);
		
		return org;
	}
	
	/**
	 * 删除平台机构信息
	 * @param ids
	 */
	@RequestMapping(value = "platform/deletePlatformOrg.json", method = RequestMethod.POST)
	public void deletePlatformOrg(Long[] ids) {
		systemService.deleteOrgnizations(ids);
	}
	
	/**
	 * 更改机构状态  0：启用  1:禁用; （城市合伙人/网点）
	 */
	@RequestMapping(value = "partner/saveAccountOrgState.json", method = RequestMethod.POST)
	public void savePartnerAccountOrgState(Long orgId, Integer state) {
		systemService.saveAccountOrgState(orgId, state);
	}

	/**
	 * 更改机构状态  0：启用  1:禁用; （城市合伙人/网点）
	 */
	@RequestMapping(value = "vender/saveAccountOrgState.json", method = RequestMethod.POST)
	public void saveVenderAccountOrgState(Long orgId, Integer state) {
		systemService.saveAccountOrgState(orgId, state);
	}
	
	
	/**
	 * 绑定/解绑 设备信息 （网点）
	 * @param devNos
	 */
	@RequestMapping(value = "partner/saveBindDevices.json", method = RequestMethod.POST)
	public void savePartnerBindDevices(Integer bindingFlag, Long orgId, String[] devNos) {
		systemService.saveBindDevices(bindingFlag, orgId, devNos);
	}

	/**
	 * 绑定/解绑 设备信息 （城市合伙人）
	 * @param devNos
	 */
	@RequestMapping(value = "vender/saveBindDevices.json", method = RequestMethod.POST)
	public void saveVenderBindDevices(Integer bindingFlag, Long orgId, String[] devNos) {
		systemService.saveBindDevices(bindingFlag, orgId, devNos);
	}
	
	
	/**************************************结束**************************************************************/
	
}
