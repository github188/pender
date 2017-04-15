package com.vendor.control.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.ecarry.core.web.control.BaseControl;
import com.ecarry.core.web.core.ContextUtil;
import com.ecarry.core.web.core.Page;
import com.vendor.po.DevCombination;
import com.vendor.po.Device;
import com.vendor.po.Orgnization;
import com.vendor.po.PointPlace;
import com.vendor.po.User;
import com.vendor.service.IOrgnizationService;
import com.vendor.service.IPlatformService;

/**
 * 店铺总览控制器
 * @author liujia on 2017年3月2日
 */
@Controller
@RequestMapping(value = "/store")
public class StoreControl extends BaseControl {
	
	@Autowired
	private IPlatformService platformService;
	
	@Autowired
	private IOrgnizationService orgnizationService;
	
	@RequestMapping(value = "overview/forward.do", method = RequestMethod.GET)
	public ModelAndView forwardPoint() {
		ModelAndView view = new ModelAndView("/store/store.jsp");
		User user = ContextUtil.getUser(User.class);
		view.addObject("_orgName", user.getOrgName());
		view.addObject("_orgId", user.getOrgId());
		return view;
	}
	
	/**
	 * 查询点位信息
	 * @return
	 */
	@RequestMapping(value = "overview/find.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<PointPlace> findPoint(Page page, PointPlace pointPlace) {
		return platformService.findPointPlaces(page, pointPlace);
	}
	
	@RequestMapping(value = "overview/findBindDevCombination.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<DevCombination> findBindDevCombination(Page page, Long pointplaceId) {
		return platformService.findBindDevCombination(page, pointplaceId);
	}
	
	@RequestMapping(value = "overview/findDevCombination.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<Device> findDevCombination(Page page, Long orgId) {
		return platformService.findDevCombination(page, orgId);
	}
	
	/**
	 * 保存点位信息
	 * @param org
	 */
	@RequestMapping(value = "overview/save.json", method = RequestMethod.POST)
	public void savePoint(@RequestBody PointPlace pointPlace) {
		platformService.savePoint(pointPlace);
	}
	
	/**
	 * 删除点位信息
	 * @param ids
	 */
	@RequestMapping(value = "overview/delete.json", method = RequestMethod.POST)
	public void deletePoints(Long[] ids) {
		platformService.deletePoints(ids);
	}
	
	@RequestMapping(value = "overview/addDevCombination.json", method = RequestMethod.POST)	
	public void addDevCombination(Integer combinationNo, Long pointplaceID) {	
		platformService.addDevCombination(combinationNo, pointplaceID);
	}
	
	@RequestMapping(value = "overview/deleteDevCombination.json", method = RequestMethod.POST)
	public void deleteDevCombination(String identity) {
		platformService.deleteDevCombination(identity);
	}
	
	/**
	 * 循环递归查询组织信息
	 * @return
	 */
	@RequestMapping(value = "overview/findCyclicOrgnizations.json", method = RequestMethod.POST)
	public List<Orgnization> findCyclicOrgnizations() {
		User curUser = ContextUtil.getUser(User.class);
		return orgnizationService.findCyclicPeersOrgnization(curUser.getOrgId());
	}
	
	/**
	 * 我的店铺
	 */
	@RequestMapping(value = "myStore/forward.do", method = RequestMethod.GET)
	public ModelAndView forwardMyStore() {
		ModelAndView view = new ModelAndView("/store/myStore.jsp");
		User user = ContextUtil.getUser(User.class);
		view.addObject("_orgName", user.getOrgName());
		view.addObject("_orgId", user.getOrgId());
		return view;
	}
	
	/**
	 * 我的店铺---查询点位信息
	 * @return
	 */
	@RequestMapping(value = "myStore/find.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<PointPlace> findMyStores(Page page, PointPlace pointPlace) {
		return platformService.findMyStores(page, pointPlace);
	}
	
	/**
	 * 我的店铺---查询店铺绑定设备
	 * @return
	 */
	@RequestMapping(value = "myStore/findBindDevCombination.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<DevCombination> findMyStoreBindDevCombination(Page page, Long pointplaceId) {
		return platformService.findBindDevCombination(page, pointplaceId);
	}
	
	/**
	 * 我的店铺---查询未绑定店铺的设备
	 * @return
	 */
	@RequestMapping(value = "myStore/findDevCombination.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<Device> findMyStoreDevCombination(Page page, Long orgId) {
		return platformService.findDevCombination(page, orgId);
	}
	
	/**
	 * 我的店铺---保存点位信息
	 * @param org
	 */
	@RequestMapping(value = "myStore/save.json", method = RequestMethod.POST)
	public void saveMyStorePoint(@RequestBody PointPlace pointPlace) {
		platformService.savePoint(pointPlace);
	}
	
	/**
	 * 我的店铺---删除点位信息
	 * @param ids
	 */
	@RequestMapping(value = "myStore/delete.json", method = RequestMethod.POST)
	public void deleteMyStorePoints(Long[] ids) {
		platformService.deletePoints(ids);
	}
	
	/**
	 * 我的店铺---绑定店铺
	 * @param ids
	 */
	@RequestMapping(value = "myStore/addDevCombination.json", method = RequestMethod.POST)	
	public void addMyStoreDevCombination(Integer combinationNo, Long pointplaceID) {	
		platformService.addDevCombination(combinationNo, pointplaceID);
	}
	
	/**
	 * 我的店铺---解绑店铺
	 * @param ids
	 */
	@RequestMapping(value = "myStore/deleteDevCombination.json", method = RequestMethod.POST)
	public void deleteMyStoreDevCombination(String identity) {
		platformService.deleteDevCombination(identity);
	}
	
	/**
	 * 我的店铺---循环递归查询组织信息
	 * @return
	 */
	@RequestMapping(value = "myStore/findCyclicOrgnizations.json", method = RequestMethod.POST)
	public List<Orgnization> findMyStoreCyclicOrgnizations() {
		User curUser = ContextUtil.getUser(User.class);
		return orgnizationService.findCyclicPeersOrgnization(curUser.getOrgId());
	}
	
}
