/**
 * 
 */
package com.vendor.control.web;

import java.sql.Date;
import java.util.List;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.ecarry.core.domain.Authority;
import com.ecarry.core.domain.Menu;
import com.ecarry.core.domain.Role;
import com.ecarry.core.domain.SysType;
import com.ecarry.core.domain.UserRole;
import com.ecarry.core.exception.BusinessException;
import com.ecarry.core.web.control.BaseControl;
import com.ecarry.core.web.core.ContextUtil;
import com.ecarry.core.web.core.Page;
import com.vendor.po.Orgnization;
import com.vendor.po.User;
import com.vendor.service.IDictionaryService;
import com.vendor.service.ISystemService;
import com.vendor.util.Commons;

/**
 * @author dranson on 2011-1-17
 */
@Controller
@RequestMapping(value = "/system")
public class SystemControl extends BaseControl {
	
	@Autowired
	private IDictionaryService dictionaryService;
	
	@Autowired
	private ISystemService systemService;
	
	@RequestMapping(value = "level/forward.do", method = RequestMethod.GET)
	public ModelAndView forwardLevel() {
		ModelAndView view = new ModelAndView("/system/level.jsp");
		return view;
	}
	
	@RequestMapping(value = "level/findMenusForLevel.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<Menu> findMenusForLevel(Menu menu) {
		if (menu == null)
			menu = new Menu();
		menu.setLimited(false);
		return systemService.findMenusByCycle(menu);
	}
	
	@RequestMapping(value = "access/forward.do", method = RequestMethod.GET)
	public ModelAndView forwardAccess() {
		ModelAndView view = new ModelAndView("/system/access.jsp");
		return view;
	}
	
	@RequestMapping(value = "access/findMenusForAccess.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<Menu> findMenusForAccess(Menu menu) {
		return systemService.findMenusByCycle(menu);
	}
	
	@RequestMapping(value = "access/findMenusByParentId.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<Menu> findMenusByParentId(Long id, Long roleId) {
		return systemService.findMenusByParentId(id, roleId);
	}
	
	@RequestMapping(value = "access/findUserRoles.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<UserRole> findUserRoles(Long userId) {
		return systemService.findUserRoles(userId);
	}
	
	@RequestMapping(value = "access/findRights.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<Authority> findRights(Long roleId) {
		return systemService.findRights(roleId);
	}
	
	@RequestMapping(value = "access/save.json", method = RequestMethod.POST)
	public void saveRights(List<Authority> authorities) {
		systemService.saveRights(authorities);
	}
	
	@RequestMapping(value = "access/copy.json", method = RequestMethod.POST)
	public void saveRights(Long roleId, Long[] roleIds) {
		systemService.saveRights(roleId, roleIds);
	}
	
	@RequestMapping(value = "access/findUsers.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<User> findAccessUsers(Page page, User user, Date startDate, Date endDate) {
		return systemService.findUsers(page, user, startDate, endDate);
	}
	
	@RequestMapping(value = "access/findRoles.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<Role> findRoles() {
		return systemService.findRoles();
	}
	
	@RequestMapping(value = "access/saveRole.json", method = RequestMethod.POST)
	public Role saveRole(Role role) {
		systemService.saveRole(role);
		return role;
	}
	
	@RequestMapping(value = "access/deleteRoles.json", method = RequestMethod.POST)
	public void deleteRoles(Long[] ids) {
		systemService.deleteRoles(ids);
	}
	
	@RequestMapping(value = "access/saveUserRoles.json", method = RequestMethod.POST)
	public void saveUserRoles(Long[] userIds, Long[] roleIds) {
		systemService.saveUserRoles(userIds, roleIds);
	}
	
	@RequestMapping(value = "org/forward.do", method = RequestMethod.GET)
	public ModelAndView forwardOrg() {
		ModelAndView view = new ModelAndView("/system/org.jsp");
		return view;
	}
	
	@RequestMapping(value = "org/find.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<Orgnization> findOrgnizations(Page page, Orgnization orgnization, Date startDate, Date endDate) {
		return systemService.findOrgnizations(page, orgnization, startDate, endDate);
	}
	
	@RequestMapping(value = "org/save.json", method = RequestMethod.POST)
	public Orgnization saveOrgnization(Orgnization orgnization, String[] devNos) {
		if (Commons.isSystemUser() && orgnization.getId() == null && orgnization.getParentId() == null)
			throw new BusinessException("请通过平台账户功能新增公司信息");
		systemService.saveOrgnization(orgnization, devNos);
		return orgnization;
	}
	
	@RequestMapping(value = "org/delete.json", method = RequestMethod.POST)
	public void deleteOrgnizations(Long[] ids) {
		systemService.deleteOrgnizations(ids);
	}
	
	@RequestMapping(value = "user/savePassword.json", method = RequestMethod.POST)
	public void savePassword(String oldPassword, String password) {
		systemService.savePassword(oldPassword, password);
	}
	
	@RequestMapping(value = "user/findOrgnizationsByParentId.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<Orgnization> findOrgnizationsByParentId(Long id) {
		return systemService.findOrgnizationsByParentId(id);
	}
	
	@RequestMapping(value = "user/forward.do", method = RequestMethod.GET)
	public ModelAndView forwardUser() {
		ModelAndView view = new ModelAndView("/system/user.jsp");
		return view;
	}
	
	@RequestMapping(value = "user/find.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<User> findUsers(Page page, User user, Date startDate, Date endDate) {
		return systemService.findUsers(page, user, startDate, endDate);
	}
	
	@RequestMapping(value = "user/save.json", method = RequestMethod.POST)
	public void saveUser(User user) {
		systemService.saveUser(user);
	}
	
	@RequestMapping(value = "user/saveResetPassword.json", method = RequestMethod.POST)
	public void saveResetPassword(Long userId) {
		systemService.saveResetPassword(userId);
	}
	
	@RequestMapping(value = "user/delete.json", method = RequestMethod.POST)
	public void deleteUsers(Long[] ids) {
		systemService.deleteUsers(ids);
	}
	
	@RequestMapping(value = "menu/forward.do", method = RequestMethod.GET)
	public ModelAndView forwardMenu() {
		ModelAndView view = new ModelAndView("/system/menu.jsp");
		return view;
	}
	
	@RequestMapping(value = "menu/findByCycle.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<Menu> findByCycle(Menu menu) {
		return systemService.findMenusByCycle(menu);
	}
	
	@RequestMapping(value = "menu/find.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<Menu> findMenus(Page page, Menu menu) {
		return systemService.findMenus(page, menu);
	}
	
	@RequestMapping(value = "menu/save.json", method = RequestMethod.POST)
	public Menu saveMenu(Menu menu) {
		systemService.saveMenu(menu);
		return menu;
	}
	
	@RequestMapping(value = "menu/delete.json", method = RequestMethod.POST)
	public void deleteMenus(Long[] ids) {
		systemService.deleteMenus(ids);
	}
	
	@RequestMapping(value = "type/forward.do", method = RequestMethod.GET)
	public ModelAndView forwardSysType() {
		ModelAndView view = new ModelAndView("/system/systype.jsp");
		return view;
	}
	
	@RequestMapping(value = "type/find.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<SysType> findSysTypes(Page page, SysType sysType) {
		return systemService.findSysTypes(page, sysType);
	}
	
	@RequestMapping(value = "type/save.json", method = RequestMethod.POST)
	public void saveSysType(SysType sysType) {
		systemService.saveSysType(sysType);
	}
	
	@RequestMapping(value = "type/delete.json", method = RequestMethod.POST)
	public void deleteSysTypes(Long[] ids) {
		systemService.deleteSysTypes(ids);
	}
	
	@RequestMapping(value = "job/executeSyncData.json", method = RequestMethod.POST)
	public void executeSyncData() {
		dictionaryService.findDataToCache();
	}
	
	/**
	 * 删除服务器缓存文件
	 */
	@RequestMapping(value = "job/executeDeleteCache.json", method = RequestMethod.POST)
	public void executeDeleteCache() {
		try {
			ContextUtil.getBeanByName(DisposableBean.class, "cacheManager").destroy();
		} catch (Exception e) {
		}
	}
	
	/**
	 * 清除无效上传图片
	 */
	@RequestMapping(value = "job/executeRemoveUpload.json", method = RequestMethod.POST)
	public void executeRemoveUpload() {
		dictionaryService.deleteExpireFiles();
	}
	
}
