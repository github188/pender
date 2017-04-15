package com.vendor.service;

import java.sql.Date;
import java.util.List;

import com.ecarry.core.web.core.Page;
import com.vendor.po.Orgnization;
import com.vendor.po.User;

public interface IOrgnizationService {
	
	/**
	 * 循环递归查询组织信息
	 * @param parentId	父组织ID
	 * @return	树状结构的组织集
	 */
	List<Orgnization> findCyclicOrgnizations(Long parentId);

	/**
	 * 获取当前登录用户信息
	 * @return	当前登录用户信息
	 */
	User findCurrentUser();
	
	/**
	 * 修改当前登录用户信息
	 */
	void saveCurrentUser(User user);

	/**
	 * 获取当前登录用户所属组织信息
	 * @return	当前登录用户所属组织信息
	 */
	Orgnization findCurrentOrgnization();
	
	/**
	 * 修改当前登录用户所属组织信息
	 */
	void saveCurrentOrgnization(Orgnization orgnization);

	/**
	 * 保存关联申请信息
	 * @param applyRelate 2：拒绝申请、3：同意申请
	 */
	void saveOrgRelation(Integer applyRelate);
	
	/**
	 * 分页条件查询机构信息
	 * @param page	分页信息
	 * @param orgnization	查询条件
	 * @param startDate 合作时间-起始时间
	 * @param endDate 合作时间-截止时间
	 * @return	机构集
	 */
	List<Orgnization> findOrgnizationList(Page page, Orgnization orgnization, Date startDate, Date endDate);
	
	/**
	 * 保存机构
	 * @param orgnization	需要保存的机构对象
	 */
	void saveOrgnization(Orgnization orgnization);
	
	/**
	 * 删除机构
	 * @param ids	需要删除的机构ID集
	 */
	void deleteOrgnizations(Long... ids);
	
	/**
	 * 建立/取消关联（管理）信息
	 * @param orgId 要建立关联的下级组织信息
	 * @param applyRelate 1：建立管理   2：取消管理
	 */
	void saveRelation(Long orgId, Integer applyRelate);
	
	/**
	 * 查询机构的子机构（不递归，单级）
	 * @param parentId	父机构ID
	 * @return	子机构集
	 */
	List<Orgnization> findOrgnizationsByParentId(Long parentId);
	
	/**
	 * 循环递归查询平级的组织信息
	 * @return	平级的组织集
	 */
	List<Orgnization> findCyclicPeersOrgnization(Long orgId);
}

