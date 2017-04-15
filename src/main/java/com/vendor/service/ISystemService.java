package com.vendor.service;

import java.sql.Date;
import java.util.List;
import java.util.Map;

import com.ecarry.core.domain.Authority;
import com.ecarry.core.domain.Menu;
import com.ecarry.core.domain.Role;
import com.ecarry.core.domain.SysType;
import com.ecarry.core.domain.UserRole;
import com.ecarry.core.domain.WebUploader;
import com.ecarry.core.web.core.Page;
import com.vendor.po.Orgnization;
import com.vendor.po.User;

public interface ISystemService {
	/**
	 * 发送充值密码邮件
	 * @param code	公司代码
	 * @param username	用户名
	 */
	void findForgetEmail(String code, String username);
	/**
	 * 重置密码
	 * @param code	重置密码加密串
	 * @return	重置信息
	 */
	Map<String, String> saveForgetPassword(String code);
	/**
	 * 修改密码
	 * @param oldPassword	旧密码
	 * @param password	新密码
	 */
	void savePassword(String oldPassword, String password);
	/**
	 * 重置用户密码
	 * @param userId	需要重置密码的用户ID
	 */
	void saveResetPassword(Long userId);
	/**
	 * 根据父菜单及角色查询拥有权限的菜单集
	 * @param parentId	父菜单ID
	 * @param roleId	角色ID
	 * @return	角色拥有的权限菜单集
	 */
	List<Menu> findMenusByParentId(Long parentId, Long roleId);
	/**
	 * 根据查询条件递归查询其下所有的子菜单，保留层级结构
	 * @param menu	查询条件
	 * @return	子菜单集
	 */
	List<Menu> findMenusByCycle(Menu menu);
	/**
	 * 根据父菜单查询其下的子菜单（不递归，单级）
	 * @param parentId	父菜单ID
	 * @return	子菜单集
	 */
	List<Menu> findMenusByParentId(Long parentId);
	/**
	 * 根据条件查询菜单信息
	 * @param page	分页信息
	 * @param menu	查询条件
	 * @return	菜单信息
	 */
	List<Menu> findMenus(Page page, Menu menu);
	/**
	 * 保存菜单
	 * @param menu	需要保存的菜单
	 */
	void saveMenu(Menu menu);
	/**
	 * 删除菜单
	 * @param ids	需要删除菜单的ID集
	 */
	void deleteMenus(Long... ids);
	/**
	 * 查询角色
	 */
	List<Role> findRoles();
	/**
	 * 保存角色
	 * @param role	需要保存的角色对象
	 */
	void saveRole(Role role);
	/**
	 * 删除角色
	 * @param ids	需要删除的角色ID集
	 */
	void deleteRoles(Long... ids);
	/**
	 * 根据用户查询其拥有的角色权限
	 * @param userId	用户ID
	 * @return	角色权限
	 */
	List<UserRole> findUserRoles(Long userId);
	/**
	 * 保存用户角色权限
	 * @param userIds	用户ID集
	 * @param roleIds	角色ID集
	 */
	void saveUserRoles(Long[] userIds, Long[] roleIds);
	/**
	 * 查询角色拥有的权限资源
	 * @param roleId	角色ID
	 * @return	权限资源集
	 */
	List<Authority> findRights(Long roleId);
	/**
	 * 保存角色权限
	 * @param authorities	角色权限集
	 */
	void saveRights(List<Authority> authorities);
	/**
	 * 复制角色权限
	 * @param roleId	提供权限复制的角色ID
	 * @param roleIds	目标复制的角色ID集
	 */
	void saveRights(Long roleId, Long[] roleIds);
	/**
	 * 查询机构的子机构（不递归，单级）
	 * @param parentId	父机构ID
	 * @return	子机构集
	 */
	List<Orgnization> findOrgnizationsByParentId(Long parentId);
	/**
	 * 分页条件查询机构信息
	 * @param page	分页信息
	 * @param orgnization	查询条件
	 * @param startDate	起始日期
	 * @param endDate	截止日期
	 * @return	机构集
	 */
	List<Orgnization> findOrgnizations(Page page, Orgnization orgnization, Date startDate, Date endDate);
	/**
	 * 保存机构
	 * @param orgnization	需要保存的机构对象
	 */
	void saveOrgnization(Orgnization orgnization, String[] devNos);
	/**
	 * 删除机构
	 * @param ids	需要删除的机构ID集
	 */
	void deleteOrgnizations(Long... ids);
	/**
	 * 分页条件查询用户信息
	 * @param page	分页信息
	 * @param user	查询条件
	 * @param startDate	起始日期
	 * @param endDate	截止日期
	 * @return	用户集
	 */
	List<User> findUsers(Page page, User user, Date startDate, Date endDate);
	/**
	 * 保存用户
	 * @param user	用户对象
	 */
	void saveUser(User user);
	/**
	 * 修改相关业务表的用户记录数据
	 * @param user	所需修改的用户信息
	 */
	void saveUserInfo(User user);
	/**
	 * 客户端使用Web Uploader组件上传头像
	 * @param user	当前登录用户
	 * @param uploader	上传头像
	 */
	void saveUserInfo(User user, WebUploader uploader);
	/**
	 * 删除用户
	 * @param ids	需要删除的用户ID集
	 */
	void deleteUsers(Long... ids);
	/**
	 * 分页条件查询类型参数信息
	 * @param page	分页信息
	 * @param sysType	查询条件
	 * @return	类型参数集
	 */
	List<SysType> findSysTypes(Page page, SysType sysType);
	/**
	 * 保存类型参数
	 * @param sysType	需要保存的类型参数对象
	 */
	void saveSysType(SysType sysType);
	/**
	 * 删除类型参数
	 * @param ids	需要删除的类型参数ID集
	 */
	void deleteSysTypes(Long... ids);
	
	/**
	 * 分页条件查询平台机构用户信息
	 * @param page	分页信息
	 * @param user	查询条件
	 * @return	用户集
	 */
	List<User> findPlatformUser(Page page, User user);
	
	/**
	 * 保存平台机构信息
	 * @param orgnization	需要保存的机构对象
	 */
	void savePlatformOrg(Orgnization orgnization);
	
	/**
	 * 删除平台机构信息
	 * @param ids	需要删除的机构ID集
	 */
	void deletePlatformOrg(Long... ids);
	
	/**
	 * 保存平台机构用户信息
	 * @param user	用户对象
	 */
	void savePlatformUser(User user);
	
	/**
	 * 更改机构状态  0：启用  1:禁用; （城市合伙人/网点）
	 */
	void saveAccountOrgState(Long orgId, Integer state);
	
	/**
	 * 保存 城市合伙人/网点 机构
	 * @param orgnization	需要保存的机构对象
	 */
	void saveVenderPartnerOrgnization(Orgnization orgnization);
	
	/**
	 * 绑定/解绑 设备信息 （城市合伙人/网点）
	 * @param ids
	 */
	void saveBindDevices(Integer bindingFlag, Long orgId, String... devNos);
	
}
