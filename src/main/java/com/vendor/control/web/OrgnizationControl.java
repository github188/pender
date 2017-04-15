package com.vendor.control.web;

import java.sql.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.ecarry.core.web.control.BaseControl;
import com.ecarry.core.web.core.Page;
import com.vendor.po.Orgnization;
import com.vendor.service.IOrgnizationService;
import com.vendor.util.Commons;

/**
 * 组织管理控制层
 */
@Controller
@RequestMapping(value = "/orgnization")
public class OrgnizationControl extends BaseControl {
	
	@Autowired
	private IOrgnizationService orgnizationService;
	
	@RequestMapping(value = "orgnizationInfo/forward.do", method = RequestMethod.GET)
	public ModelAndView forwardOrgnizationInfo() {
		ModelAndView view = new ModelAndView("/orgnization/orgnizationInfo.jsp");
		return view;
	}
	
	@RequestMapping(value = "orgnizationList/forward.do", method = RequestMethod.GET)
	public ModelAndView forwardOrgnizationList() {
		ModelAndView view = new ModelAndView("/orgnization/orgnizationList.jsp");
		return view;
	}
	
	@RequestMapping(value = "account/forward.do", method = RequestMethod.GET)
	public ModelAndView forwardStoreStock() {
		ModelAndView view = new ModelAndView("/orgnization/account.jsp");
		return view;
	}
	
	@RequestMapping(value = "crossOrgnizationList/forward.do", method = RequestMethod.GET)
	public ModelAndView forwardCrossOrgnizationList() {
		ModelAndView view = new ModelAndView("/orgnization/crossOrgnizationList.jsp");
		return view;
	}
	
	/**
	 * 获取当前登录用户所属组织信息
	 */
	@RequestMapping(value = "/orgnizationInfo/findCurrentOrgnization.json", method = RequestMethod.POST)
	public Orgnization findCurrentOrgnization() {
		return orgnizationService.findCurrentOrgnization();
	}

	/**
	 * 修改当前登录用户所属组织信息
	 */
	@RequestMapping(value = "/orgnizationInfo/saveCurrentOrgnization.json", method = RequestMethod.POST)
	public void saveCurrentOrgnization(Orgnization orgnization) {
		orgnizationService.saveCurrentOrgnization(orgnization);
	}

	/**
	 * 保存关联申请信息
	 * @param applyRelate 2：拒绝申请、3：同意申请
	 */
	@RequestMapping(value = "/orgnizationInfo/saveOrgRelation.json", method = RequestMethod.POST)
	public void saveOrgRelation(Integer applyRelate) {
		orgnizationService.saveOrgRelation(applyRelate);
	}
	
	/**
	 * 分页条件查询机构信息
	 * @param page	分页信息
	 * @param orgnization	查询条件
	 * @param startDate 合作时间-起始时间
	 * @param endDate 合作时间-截止时间
	 * @return	机构集
	 */
	@RequestMapping(value = "orgnizationList/find.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<Orgnization> findOrgnizationList(Page page, Orgnization orgnization, Date startDate, Date endDate) {
		return orgnizationService.findOrgnizationList(page, orgnization, startDate, endDate);
	}
	
	/**
	 * 保存组织信息
	 * @param org
	 */
	@RequestMapping(value = "orgnizationList/save.json", method = RequestMethod.POST)
	public void saveOrgnization(Orgnization orgnization) {
		orgnization.setSort("1");
		orgnization.setState(Commons.ORG_STATE_ENABLE);// 启用
		orgnizationService.saveOrgnization(orgnization);
	}
	
	/**
	 * 删除组织信息
	 * @param ids
	 */
	@RequestMapping(value = "orgnizationList/delete.json", method = RequestMethod.POST)
	public void deleteOrgnizations(Long[] ids) {
		orgnizationService.deleteOrgnizations(ids);
	}

	/**
	 * 建立/取消关联（管理）信息
	 * @param orgId 要建立关联的下级组织信息
	 * @param applyRelate 1：建立管理   2：取消管理
	 */
	@RequestMapping(value = "orgnizationList/saveRelation.json", method = RequestMethod.POST)
	public void saveRelation(Long orgId, Integer applyRelate) {
		orgnizationService.saveRelation(orgId, applyRelate);
	}
	
	/**
	 * 跨组织管理-根据父组织ID查询子组织列表
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "orgnizationList/findOrgnizationsByParentId.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<Orgnization> findOrgnizationsByParentId(Long id) {
		return orgnizationService.findOrgnizationsByParentId(id);
	}
	
}
