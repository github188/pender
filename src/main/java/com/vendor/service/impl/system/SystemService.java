/**
 * 
 */
package com.vendor.service.impl.system;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.validator.GenericValidator;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ecarry.core.dao.IGenericDao;
import com.ecarry.core.dao.SQLUtils;
import com.ecarry.core.domain.Authority;
import com.ecarry.core.domain.BoxValue;
import com.ecarry.core.domain.Menu;
import com.ecarry.core.domain.Role;
import com.ecarry.core.domain.SysType;
import com.ecarry.core.domain.UserRole;
import com.ecarry.core.domain.WebUploader;
import com.ecarry.core.exception.BusinessException;
import com.ecarry.core.util.IdWorker;
import com.ecarry.core.util.ZHConverter;
import com.ecarry.core.web.core.ContextUtil;
import com.ecarry.core.web.core.Page;
import com.vendor.po.Device;
import com.vendor.po.Orgnization;
import com.vendor.po.User;
import com.vendor.service.IDictionaryService;
import com.vendor.service.ISystemService;
import com.vendor.util.Commons;
import com.vendor.util.QiniuUtil;
import com.vendor.util.SecurityUtils;

/**
 * @author dranson on 2015年3月17日
 */
@Service("systemService")
public class SystemService implements ISystemService {
	
	private Logger logger = Logger.getLogger(SystemService.class);
	
	@Value("${file.path}")
	private String rootPath;

	@Autowired
	private IdWorker idWorker;
	
	@Autowired
	private IDictionaryService dictionaryService;
	
	@Autowired
	private IGenericDao genericDao;

	/* (non-Javadoc)
	 * @see com.ecarry.service.ISystemService#findForgetEmail(java.lang.String, java.lang.String)
	 */
	@Override
	public void findForgetEmail(String code, String username) {
	}

	/* (non-Javadoc)
	 * @see com.ecarry.service.ISystemService#saveForgetPassword(java.lang.String)
	 */
	@Override
	public Map<String, String> saveForgetPassword(String code) {
		Map<String, String> map = new HashMap<String, String>();
		return map;
	}

	/* (non-Javadoc)
	 * @see com.ecarry.service.ISystemService#savePassword(java.lang.String, java.lang.String)
	 */
	@Override
	public void savePassword(String oldPassword, String password) {
		User curUser = ContextUtil.getUser(User.class);
		Timestamp curTime = new Timestamp(System.currentTimeMillis());
		if (oldPassword.equals(password))
			throw new BusinessException("新密码不能与旧密码相同！");
		String encodePassword = genericDao.findSingle(String.class, "SELECT PASSWORD FROM SYS_USER WHERE ID=?", curUser.getId());
		if (!ContextUtil.matches(oldPassword, encodePassword))
			throw new BusinessException("原密码错误！");
		
		// 密码校验：6-16位字符；不能包含空格；纯数字不能是9位以下
		if (password.length() < 6 || password.length() > 16)
			throw new BusinessException("密码长度为6~16位");
		
		if (!password.matches("^[\\S]*$"))
			throw new BusinessException("密码不能包含空格");

		if (password.matches("^[\\d]*$") && password.length() < 9)
			throw new BusinessException("密码是纯数字的话不能是9位以下");
		
		encodePassword = ContextUtil.getPassword(password);
		genericDao.execute("UPDATE SYS_USER SET PASSWORD=?,PWD_UPDATE_TIME=? WHERE ID=?", encodePassword, curTime, curUser.getId());
		curUser.setPassword(encodePassword);
	}

	/* (non-Javadoc)
	 * @see com.ecarry.service.ISystemService#saveResetPassword(java.lang.Long)
	 */
	@Override
	public void saveResetPassword(Long userId) {
		User user;
		if (userId != null && userId.longValue() < 0) {
			user = genericDao.findT(User.class, SQLUtils.getSelectSQL(User.class) + " WHERE CREATE_USER=? AND EDITABLE=? AND COMPANY_ID=?", 1, false, userId * -1);
		} else {
			user = genericDao.findTById(User.class, userId);
		}
		if (user == null)
			throw new BusinessException("非法请求！");
		SecurityUtils.checkSystemAccess(user.getCompanyId());
		Timestamp curTime = new Timestamp(System.currentTimeMillis());
		String password = ContextUtil.getPassword(dictionaryService.findDefaultPassword());
		String sql = "UPDATE SYS_USER SET PASSWORD=?,PWD_UPDATE_TIME=?,RETRY=?,ENABLE=? WHERE ID=?";
		genericDao.execute(sql, password, curTime, 0, true, user.getId());
	}

	/* (non-Javadoc)
	 * @see com.ecarry.service.ISystemService#findMenusByParentId(java.lang.Long, java.lang.Long)
	 */
	@Override
	public List<Menu> findMenusByParentId(Long parentId, Long roleId) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer("SELECT ID,PARENT_ID,NAME,URL,ENABLE,RIGHTS,SORT");
		if (roleId != null) {
			buf.append(",(SELECT RIGHTS FROM SYS_RIGHTS WHERE MENU_ID=T.ID AND ROLE_ID=?) as grants");
			args.add(roleId);
		}
		buf.append(" FROM SYS_MENU T WHERE PARENT_ID");
		if (parentId == null) {
			buf.append(" IS NULL");
		} else {
			buf.append("=?");
			args.add(parentId);
		}
		buf.append(" AND SYS_TYPE=? ORDER BY SORT");
		args.add(ContextUtil.getUser(User.class).getSysType());
		return genericDao.findTs(Menu.class, buf.toString(), args.toArray());
	}

	/* (non-Javadoc)
	 * @see com.ecarry.service.ISystemService#findMenusByCycle(com.ecarry.core.domain.Menu)
	 */
	@Override
	public List<Menu> findMenusByCycle(Menu menu) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("name", SQLUtils.LIKE);
		map.put("url", SQLUtils.LIKE);
		if (menu == null)
			menu = new Menu();
		menu.setSysType(ContextUtil.getUser(User.class).getSysType());
		BoxValue<String, List<Object>> box = SQLUtils.getCondition(menu, map, "A");
		StringBuffer buf = new StringBuffer();
		buf.append("SELECT ").append(SQLUtils.getColumnsSQL(Menu.class, "A")).append(" FROM SYS_MENU A");
		buf.append(box.getKey()).append(" ORDER BY A.PARENT_ID,A.SORT");
		List<Menu> menus = genericDao.findTs(Menu.class, buf.toString(), box.getValue().toArray());
		List<Long> parentIds = new ArrayList<Long>();
		for (Menu menuItem : menus) {
			if (menuItem.getParentId() == null) {
				parentIds.add(menuItem.getId());
			} else {
				boolean exist = false;
				for (Menu parentItem : menus) {
					if (parentItem.getId().longValue() == menuItem.getParentId().longValue()) {
						exist = true;
						break;
					}
				}
				if (!exist)
					parentIds.add(menuItem.getId());
			}
		}
		for (int i = menus.size() - 1; i >=0; i--)
			if (!parentIds.contains(menus.get(i).getId()))
				menus.remove(i);			
		searchMenuChildrenForCycle(menus, parentIds, box);
		for (int i = 0; i < menus.size(); i++)
			if (menus.get(i).getMenus() == null || menus.get(i).getMenus().isEmpty())
				menus.remove(i--);
		return menus;
	}

	private void searchMenuChildrenForCycle(List<Menu> parentMenus, List<Long> parentIds, BoxValue<String, List<Object>> box) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer();
		buf.append("SELECT ").append(SQLUtils.getColumnsSQL(Menu.class, "A")).append(" FROM SYS_MENU A");
		buf.append(box.getKey());
		buf.append(box.getValue().size() == 0 ? " WHERE " : " AND ");
		args.addAll(box.getValue());
		args.addAll(parentIds);
		if (parentIds != null && parentIds.size() != 0) {
			for (int i = 0; i < parentIds.size(); i++)	 {
				if (i % 1000 == 0) {
					if (i != 0) {
						buf.setLength(buf.length() - 1);
						buf.append(") OR ");
					}
					buf.append("A.PARENT_ID IN(");
				}
				buf.append("?,");
			}
			buf.setLength(buf.length() - 1);
			buf.append(")");
			if (!Commons.isSystemUser()) {
				buf.append(" AND A.LIMITED=?");
				args.add(false);
			}
			buf.append(" AND A.SYS_TYPE=? ORDER BY A.PARENT_ID,A.SORT");
			args.add(ContextUtil.getUser(User.class).getSysType());
			List<Menu> menus = genericDao.findTs(Menu.class, buf.toString(), args.toArray());
			int start = 0;
			parentIds.clear();
			for (Menu menu : menus) {
				parentIds.add(menu.getId());
				if (parentMenus.get(start).getId().longValue() == menu.getParentId().longValue()) {
					parentMenus.get(start).addMenu(menu);
				} else {
					for (int i = 0; i < parentMenus.size(); i++) {
						if (parentMenus.get(i).getId().longValue() == menu.getParentId().longValue()) {
							parentMenus.get(i).addMenu(menu);
							start = i;
							break;
						}
					}
				}
			}
			if (parentIds.size() != 0)
				searchMenuChildrenForCycle(menus, parentIds, box);
		}
	}

	/* (non-Javadoc)
	 * @see com.ecarry.service.ISystemService#findMenusByParentId(java.lang.Long)
	 */
	@Override
	public List<Menu> findMenusByParentId(Long parentId) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer(SQLUtils.getSelectSQL(Menu.class));
		buf.append(" WHERE ENABLE=? AND PARENT_ID");
		args.add(true);
		if (parentId == null) {
			buf.append(" IS NULL");
		} else {
			buf.append("=?");
			args.add(parentId);
		}
		buf.append(" AND SYS_TYPE=? ORDER BY SORT");
		args.add(ContextUtil.getUser(User.class).getSysType());
		return genericDao.findTs(Menu.class, buf.toString(), args.toArray());
	}

	/* (non-Javadoc)
	 * @see com.ecarry.service.ISystemService#findMenus(com.ecarry.core.web.core.Page, com.ecarry.core.domain.Menu)
	 */
	@Override
	public List<Menu> findMenus(Page page, Menu menu) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("name", SQLUtils.LIKE);
		map.put("url", SQLUtils.LIKE);
		if (menu == null)
			menu = new Menu();
		menu.setSysType(ContextUtil.getUser(User.class).getSysType());
		return genericDao.findTs(menu, map, page);
	}

	/* (non-Javadoc)
	 * @see com.ecarry.service.ISystemService#saveMenu(com.ecarry.core.domain.Menu)
	 */
	@Override
	public void saveMenu(Menu menu) {
		SecurityUtils.checkSystemAccess();
		User curUser = ContextUtil.getUser(User.class);
		menu.setSysType(curUser.getSysType());
		if (menu.getId() == null) {
			if (menu.getParentId() != null)
				genericDao.execute("UPDATE SYS_MENU SET URL=?,RIGHTS=? WHERE ID=?", null, 0, menu.getParentId());
			if (menu.getLimited() == null)
				menu.setLimited(false);
			if (menu.getEnable() == null)
				menu.setEnable(true);
			genericDao.save(menu);
		} else {			
			List<Menu> menus = genericDao.findTs(Menu.class, SQLUtils.getSelectSQL(Menu.class) + " WHERE PARENT_ID=?", menu.getId());
			if (menus.size() == 0) {
				genericDao.update(menu);
			} else {	//父级菜单设置使用等级、URL、权限值无效
				BoxValue<String, Object[]> box = SQLUtils.getUpdateSQLByExclude(menu, "url,rights");
				genericDao.execute(box.getKey(), box.getValue());
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.ecarry.service.ISystemService#deleteMenus(java.lang.Long[])
	 */
	@Override
	public void deleteMenus(Long... ids) {
		SecurityUtils.checkSystemAccess();
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer("SELECT ID,PARENT_ID FROM SYS_MENU WHERE ID IN(");
		for (Long id : ids) {
			buf.append("?,");
			args.add(id);
		}
		buf.setLength(buf.length() - 1);
		buf.append(") AND SYS_TYPE=?");
		args.add(ContextUtil.getUser(User.class).getSysType());
		List<Menu> menus = genericDao.findTs(Menu.class, buf.toString(), args.toArray());
		if (menus.size() != ids.length)
			throw new BusinessException("非法请求！");
		buf.setLength(0);
		args.remove(args.size() - 1);
		buf.append(" IN(");
		for (int i = 0; i < ids.length; i++)
			buf.append("?,");
		menus = findMenusForDelete(ids);
		for (Menu menu : menus) {
			buf.append("?,");
			args.add(menu.getId());
		}
		buf.setLength(buf.length() - 1);
		buf.append(")");
		String inIds = buf.toString();
		buf.setLength(0);
		buf.append("DELETE FROM SYS_RIGHTS WHERE MENU_ID IN(SELECT ID FROM SYS_MENU WHERE ID").append(inIds).append(" AND SYS_TYPE=?)");
		args.add(ContextUtil.getUser(User.class).getSysType());
		genericDao.execute(buf.toString(), args.toArray());
		buf.setLength(0);
		buf.append("DELETE FROM SYS_MENU WHERE ID").append(inIds).append(" AND SYS_TYPE=?");
		genericDao.execute(buf.toString(), args.toArray());
	}
	
	private List<Menu> findMenusForDelete(Long[] parentIds) {
		StringBuffer buf = new StringBuffer();
		buf.append(SQLUtils.getSelectSQL(Menu.class)).append(" WHERE ");
		if (parentIds == null) {
			buf.append("PARENT_ID IS NULL");
		} else {
			buf.append("PARENT_ID IN(");
			for (int i = 0; i < parentIds.length; i++)
				buf.append("?,");
			buf.setLength(buf.length() - 1);
			buf.append(")");
		}
		buf.append(" AND SYS_TYPE=?");
		List<Long> args = new ArrayList<Long>(Arrays.asList(parentIds));
		args.add(ContextUtil.getUser(User.class).getSysType().longValue());
		List<Menu> list = genericDao.findTs(Menu.class, buf.toString(), args.toArray());
		if (list.size() != 0) {
			parentIds = new Long[list.size()];
			for (int i = 0; i < list.size(); i++)
				parentIds[i] = list.get(i).getId();
			list.addAll(findMenusForDelete(parentIds));
		}
		return list;
	}

	/* (non-Javadoc)
	 * @see com.ecarry.service.ISystemService#findUserRoles(java.lang.Long)
	 */
	@Override
	public List<UserRole> findUserRoles(Long userId) {
		String sql = SQLUtils.getSelectSQL(UserRole.class) + " WHERE USER_ID=?";
		return genericDao.findTs(UserRole.class, sql, userId);
	}

	/* (non-Javadoc)
	 * @see com.ecarry.service.ISystemService#findRoles()
	 */
	@Override
	public List<Role> findRoles() {
		User curUser = ContextUtil.getUser(User.class);
		String sql = SQLUtils.getSelectSQL(Role.class) + " WHERE SYS_TYPE=? AND ORG_ID=?";
		return genericDao.findTs(Role.class, sql, curUser.getSysType(), curUser.getCompanyId());
	}

	/* (non-Javadoc)
	 * @see com.ecarry.service.ISystemService#saveRole(com.ecarry.core.domain.Role)
	 */
	@Override
	public void saveRole(Role role) {
		User curUser = ContextUtil.getUser(User.class);
		Timestamp curTime = new Timestamp(System.currentTimeMillis());
		SecurityUtils.checkCompanyData(role.getOrgId());
		role.setSysType(curUser.getSysType());
		if (role.getId() == null) {
			if (role.getEditable() == null)
				role.setEditable(true);
			role.setOrgId(curUser.getOrgId());
			role.setCreateUser(curUser.getUsername());
			role.setCreateTime(curTime);
			genericDao.save(role);
		} else {
			boolean editable = genericDao.findSingle(Boolean.class, "SELECT EDITABLE FROM SYS_ROLE WHERE ID=?", role.getId());
			if (!editable)
				throw new BusinessException("禁止编辑内置系统角色！");
			role.setUpdateUser(curUser.getUsername());
			role.setUpdateTime(curTime);
			genericDao.update(role);
		}
	}

	/* (non-Javadoc)
	 * @see com.ecarry.service.ISystemService#deleteRoles(java.lang.Long[])
	 */
	@Override
	public void deleteRoles(Long... ids) {
		if (ids != null && ids.length != 0) {
			StringBuilder buf = new StringBuilder(" IN(");
			for (Long ignored : ids) {
				buf.append("?,");
			}
			buf.setLength(buf.length() - 1);
			buf.append(")");
			String inIds = buf.toString();
			buf.setLength(0);
			List<Object> args = new ArrayList<Object>();
			args.add(false);
			args.add(ContextUtil.getUser(User.class).getSysType());

			//modify by zxf 三月 13, 2017 4:45 下午
//			args.add(ContextUtil.getUser(User.class).getCompanyId());
            args.add(ContextUtil.getUser(User.class).getOrgId());

			args.addAll(Arrays.asList(ids));
			buf.append("SELECT COUNT(ID) FROM SYS_ROLE WHERE EDITABLE=? AND SYS_TYPE=? AND ORG_ID=? AND ID").append(inIds);
			int count = genericDao.findSingle(int.class, buf.toString(), args.toArray());
			if (count != 0)
				throw new BusinessException("禁止删除系统内置角色！");

            //检查角色是否为预定义角色，预定义角色不能删除
            buf.setLength(0);
            buf.append("SELECT id,name FROM sys_role WHERE id IN (");
            for (Long id  : ids) {
                buf.append(id).append(",");
            }
            buf.setLength(buf.length() - 1);
            buf.append(") AND type IN (1,2,3)");
            List<Role> roles = genericDao.findTs(Role.class, buf.toString());
            if (roles.size()>0) {
                buf.setLength(0);
                buf.append("角色");
                for (Role role:roles) {
                    buf.append(role.getName()).append("、");
                }
                buf.setLength(buf.length() - 1);
                buf.append("为预定义角色，不能删除！");
                throw new BusinessException(buf.toString());
            }

            //检查角色是否分配给用户，如果分配了则不能删除
            buf.setLength(0);
            buf.append("SELECT sr.id,sr.name FROM sys_user_role su LEFT JOIN sys_role sr ON su.role_id=sr.id WHERE role_id IN (");
            for (Long id  : ids) {
                buf.append(id).append(",");
            }
            buf.setLength(buf.length() - 1);
            buf.append(") GROUP BY sr.id");
            roles = genericDao.findTs(Role.class, buf.toString());
            if (roles.size()>0) {
                buf.setLength(0);
                buf.append("角色");
                for (Role role:roles) {
                    buf.append(role.getName()).append("、");
                }
                buf.setLength(buf.length() - 1);
                buf.append("已经分配给了用户，如需删除请先取消分配！");
                throw new BusinessException(buf.toString());
            }

            buf.setLength(0);
			buf.append("DELETE FROM SYS_RIGHTS WHERE ROLE_ID IN(SELECT ID FROM SYS_ROLE WHERE SYS_TYPE=? AND ORG_ID=? AND ID").append(inIds).append(")");
			args.remove(0);
			genericDao.execute(buf.toString(), args.toArray());
			buf.setLength(0);
			buf.append("DELETE FROM SYS_ROLE WHERE SYS_TYPE=? AND ORG_ID=? AND ID").append(inIds);
			genericDao.execute(buf.toString(), args.toArray());
		}
	}

	/* (non-Javadoc)
	 * @see com.ecarry.service.ISystemService#saveUserRoles(java.lang.Long[], java.lang.Long[])
	 */
	@Override
	public void saveUserRoles(Long[] userIds, Long[] roleIds) {
		User curUser = ContextUtil.getUser(User.class);
		List<Object> args = new ArrayList<Object>();
//		args.add(curUser.getCompanyId());
		StringBuffer buf = new StringBuffer(" IN(");
		for (Long userId : userIds) {
			buf.append("?,");
			args.add(userId);
		}
		buf.setLength(buf.length() - 1);
		buf.append(")");
		String inUserIds = buf.toString();
		buf.setLength(0);
		buf.append("SELECT COUNT(ID) FROM SYS_USER WHERE ID").append(inUserIds);
		int count = genericDao.findSingle(int.class, buf.toString(), args.toArray());
		if (count != userIds.length)
			throw new BusinessException("非法请求！");
		buf.setLength(0);
		if (roleIds == null || roleIds.length == 0) {			
//			args.remove(0);
			buf.append("DELETE FROM SYS_USER_ROLE WHERE USER_ID").append(inUserIds);
			genericDao.execute(buf.toString(), args.toArray());
		} else {
			args.clear();
//			args.add(curUser.getCompanyId());
			buf.append(" IN(");
			for (Long roleId : roleIds) {
				buf.append("?,");
				args.add(roleId);
			}
			buf.setLength(buf.length() - 1);
			buf.append(")");
			String inRoleIds = buf.toString();
			buf.setLength(0);
			buf.append("SELECT COUNT(ID) FROM SYS_ROLE WHERE ID").append(inRoleIds);
			count = genericDao.findSingle(int.class, buf.toString(), args.toArray());
			if (count != roleIds.length)
				throw new BusinessException("非法请求！");
			buf.setLength(0);
//			args.remove(0);
			Timestamp curTime = new Timestamp(System.currentTimeMillis());
			buf.append("DELETE FROM SYS_USER_ROLE WHERE ROLE_ID NOT ").append(inRoleIds);
			buf.append(" AND USER_ID=?");
			for (Long userId : userIds) {
				args.add(userId);
				genericDao.execute(buf.toString(), args.toArray());
				List<UserRole> userRoles = genericDao.findTs(UserRole.class, SQLUtils.getSelectSQL(UserRole.class) + " WHERE USER_ID=?", userId);
				for (Long roleId : roleIds) {
					boolean exist = false;
					for (UserRole userRole : userRoles) {
						if (roleId == userRole.getRoleId()) {
							exist = true;
							break;
						}
					}
					if (!exist) {
						UserRole userRole = new UserRole();
						userRole.setUserId(userId);
						userRole.setRoleId(roleId);
						userRole.setCreateUser(curUser.getUsername());
						userRole.setCreateTime(curTime);
						genericDao.save(userRole);
					}
				}
				args.remove(args.size() - 1);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.ecarry.service.ISystemService#findRights(java.lang.Long)
	 */
	@Override
	public List<Authority> findRights(Long roleId) {
		StringBuffer buf = new StringBuffer();
		buf.append(SQLUtils.getSelectSQL(Authority.class));
		buf.append(" WHERE ROLE_ID=? ORDER BY MENU_ID");
		return genericDao.findTs(Authority.class, buf.toString(), roleId);
	}

	/* (non-Javadoc)
	 * @see com.ecarry.service.ISystemService#saveRights(java.util.List)
	 */
	@Override
	public void saveRights(List<Authority> authorities) {
		User curUser = ContextUtil.getUser(User.class);
		List<Object> args = new ArrayList<Object>();
		Map<Long, Long> map = new HashMap<Long, Long>();
		List<Object> roles = new ArrayList<Object>();
		roles.add(curUser.getSysType());
		List<Object> menus = new ArrayList<Object>();
		menus.add(curUser.getSysType());
		StringBuffer bufRole = new StringBuffer("SELECT COUNT(ID) FROM SYS_ROLE WHERE SYS_TYPE=? AND ORG_ID=? AND ID IN(");
		StringBuffer bufMenu = new StringBuffer("SELECT COUNT(ID) FROM SYS_MENU WHERE SYS_TYPE=? AND ID IN(");
		for (Authority authority : authorities) {
			if (!roles.contains(authority.getRoleId())) {
				bufRole.append("?,");
				roles.add(authority.getRoleId());
			}
			if (authority.getMenuId() != null && !menus.contains(authority.getMenuId())) {
				bufMenu.append("?,");
				menus.add(authority.getMenuId());
			}
		}
		if (roles.size() >= 2) {
			bufRole.setLength(bufRole.length() - 1);
            bufRole.append(")");
            //modify by zxf 三月 13, 2017 11:45 上午
//            roles.add(1, curUser.getCompanyId());
            roles.add(1, curUser.getOrgId());
            
            int count = genericDao.findSingle(int.class, bufRole.toString(),  roles.toArray());
			if (count != roles.size() - 2)
				throw new BusinessException("非法请求！");
		}
		if (menus.size() >= 2) {
			bufMenu.setLength(bufMenu.length() - 1);
			bufMenu.append(")");
			int count = genericDao.findSingle(int.class, bufMenu.toString(),  menus.toArray());
			if (count != menus.size() - 1)
				throw new BusinessException("非法请求！");
			bufMenu.setLength(0);
			bufMenu.append("DELETE FROM SYS_RIGHTS WHERE ROLE_ID IN(");
			roles.remove(0);
			roles.remove(0);
			for (int i = 0; i < roles.size(); i++)
				bufMenu.append("?,");
			bufMenu.setLength(bufMenu.length() - 1);
			bufMenu.append(") AND MENU_ID NOT IN(");
			menus.remove(0);
			for (int i = 0; i < menus.size(); i++)
				bufMenu.append("?,");
			bufMenu.setLength(bufMenu.length() - 1);
			bufMenu.append(")");
			roles.addAll(menus);
			genericDao.execute(bufMenu.toString(), roles.toArray());
		}
		for (Authority authority : authorities) {
			Long companyId = map.get(authority.getRoleId());
			if (companyId == null) {
				companyId = genericDao.findSingle(Long.class, "SELECT ORG_ID FROM SYS_ROLE WHERE ID=?", authority.getRoleId());
				if (companyId != null)
					map.put(authority.getRoleId(), companyId);
			}
			SecurityUtils.checkSystemAccess(companyId);
			if (authority.getValue() == null) {
				genericDao.execute("DELETE FROM SYS_RIGHTS WHERE ROLE_ID=?", authority.getRoleId());
				break;
			} else if (authority.getValue() == 0) {
				genericDao.execute("DELETE FROM SYS_RIGHTS WHERE ROLE_ID=? AND MENU_ID=?", authority.getRoleId(), authority.getMenuId());
			} else {
				Timestamp curTime = new Timestamp(System.currentTimeMillis());
				args.clear();
				args.add(authority.getValue());
				args.add(curUser.getUsername());
				args.add(curTime);
				args.add(authority.getRoleId());
				args.add(authority.getMenuId());
				int count = genericDao.execute("UPDATE SYS_RIGHTS SET RIGHTS=?,CREATE_USER=?,CREATE_TIME=? WHERE ROLE_ID=? AND MENU_ID=?", args.toArray());
				if (count == 0)
					genericDao.save(authority); 
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.ecarry.service.ISystemService#saveRights(java.lang.Long, java.lang.Long[])
	 */
	@Override
	public void saveRights(Long roleId, Long[] roleIds) {
		if (roleId == null || roleIds.length == 0)
			throw new BusinessException("请选择权限复制源用户及目标用户！");
		StringBuffer buf = new StringBuffer();
		buf.append(SQLUtils.getSelectSQL(Role.class)).append(" WHERE ID IN(");
		for (int i = 0; i < roleIds.length; i++)
			buf.append("?,");
		buf.setLength(buf.length() - 1);
		buf.append(")");
		List<Object> args = new ArrayList<Object>();
		buf.setLength(0);
		buf.append("DELETE FROM SYS_RIGHTS WHERE ");
		for (int i = 0; i < roleIds.length; i++) {
			if (roleId != roleIds[i]) {
				buf.append("ROLE_ID=? OR ");
				args.add(roleIds[i]);
			}
		}
		if (args.size() != 0)
			buf.setLength(buf.length() - 4);
		genericDao.execute(buf.toString(), args.toArray());
		Timestamp curTime = new Timestamp(System.currentTimeMillis());
		User curUser = ContextUtil.getUser(User.class);
		String sql = "INSERT INTO SYS_RIGHTS(MENU_ID,RIGHTS,ROLE_ID,CREATE_USER,CREATE_TIME) SELECT MENU_ID,RIGHTS,{0},{1},{2} FROM SYS_RIGHTS WHERE ROLE_ID=?";
		for (Long destId : roleIds)
			if (destId != roleId)
				genericDao.execute(MessageFormat.format(sql, destId, curUser.getUsername(), curTime), roleId);
	}

	/* (non-Javadoc)
	 * @see com.ecarry.service.ISystemService#findOrgnizationsByParentId(java.lang.Long)
	 */
	@Override
	public List<Orgnization> findOrgnizationsByParentId(Long parentId) {
		User curUser = ContextUtil.getUser(User.class);
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer();
		buf.append(SQLUtils.getSelectSQL(Orgnization.class)).append(" WHERE ");
		if (parentId == null) {
			if (Commons.isSystemUser()) {
				buf.append("PARENT_ID IS NULL");
			} else {
				buf.append("ID=?");
				args.add(curUser.getCompanyId());
			}
		} else {
			buf.append("PARENT_ID=?");
			args.add(parentId);
		}
		buf.append(" ORDER BY CODE");
		return genericDao.findTs(Orgnization.class, buf.toString(), args.toArray());
	}

	/* (non-Javadoc)
	 * @see com.ecarry.service.ISystemService#findOrgnizations(com.ecarry.core.web.core.Page, com.ecarry.core.domain.Orgnization, java.sql.Date, java.sql.Date)
	 */
	@Override
	public List<Orgnization> findOrgnizations(Page page, Orgnization orgnization, Date startDate, Date endDate) {
		if (page != null)
			page.setOrder("code");
		StringBuffer buf = new StringBuffer();
		buf.append(" SELECT SOR.NAME AS parentName,SO.ID,SO.CODE,SO.COMPANY_ID,SO.CREATE_TIME,SO.CREATE_USER,SO.MANAGER,SO.NAME,SO.OFFICE,SO.PARENT_ID,SO.PHONE,SO.SORT,SO.STATE,SO.GENUS_AREA,SO.SETTLED_TIME,SO.UPDATE_TIME,SO.UPDATE_USER");
		buf.append(" , SO.PROV, SO.CITY, SO.DIST");
		buf.append(" FROM SYS_ORG SO LEFT JOIN SYS_ORG SOR ON SO.PARENT_ID=SOR.ID WHERE 1=1");
		if (orgnization == null)
			orgnization = new Orgnization();
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("name", SQLUtils.IGNORE);
		List<Object> args = new ArrayList<Object>();
		if (orgnization.getName() != null) {
			buf.append(" AND").append(" (SO.NAME LIKE ? OR SO.NAME LIKE ?)");
			args.add("%" + ZHConverter.convert(orgnization.getName(), ZHConverter.TRADITIONAL) + "%");
			args.add("%" + ZHConverter.convert(orgnization.getName(), ZHConverter.SIMPLIFIED) + "%");
		}
		if (null != orgnization.getParentId()) {
			buf.append(" AND SO.PARENT_ID=? ");
			args.add(orgnization.getParentId());
		}
		if (startDate != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(startDate);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			Timestamp date = new Timestamp(cal.getTimeInMillis());
			buf.append(" AND SO.CREATE_TIME>=?");
			args.add(date);
		}
		if (endDate != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(endDate);
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
			cal.set(Calendar.SECOND, 59);
			cal.set(Calendar.MILLISECOND, 999);
			Timestamp date = new Timestamp(cal.getTimeInMillis());
			buf.append(" AND SO.CREATE_TIME<=?");
			args.add(date);
		}
		return genericDao.findTs(Orgnization.class, page, buf.toString(), args.toArray());
	}

	/* (non-Javadoc)
	 * @see com.ecarry.service.ISystemService#saveOrgnization(com.ecarry.core.domain.Orgnization)
	 */
	@Override
	public void saveOrgnization(Orgnization orgnization, String[] devNos) {
		if (orgnization.getCode() == null) {
			orgnization.setCode(this.findCodeSeq().toString());
		}
		if (!GenericValidator.isInt(orgnization.getCode()))	// 防止黑客攻击，此处不正确的编码会导致内置的管理员用户名出现问题
			throw new BusinessException("机构编码格式错误！");
		User curUser = ContextUtil.getUser(User.class);
		Timestamp curTime = new Timestamp(System.currentTimeMillis());
		boolean isUpdate = false;
		if (orgnization.getId() == null) {
//			if (!Commons.isSystemUser()) {
//				if (orgnization.getParentId() == null)	// 防止黑客攻击
//					throw new BusinessException("非法请求！");
//				orgnization.setRoleType(curUser.getRoleType());
//			}
			Long companyId = findCompanyIdByOrgId(orgnization.getParentId());
			Long id = genericDao.findSingle(Long.class, "SELECT ID FROM SYS_ORG WHERE CODE=?", orgnization.getCode());
			if (id != null)
				throw new BusinessException("机构编码已经存在！");
			orgnization.setCompanyId(companyId == null ? 0L : companyId);
			orgnization.setCreateUser(curUser.getId());
			orgnization.setCreateTime(curTime);
			orgnization.setSettledTime(curTime);
			genericDao.save(orgnization);
			if (orgnization.getParentId() == null) {
				genericDao.execute("UPDATE SYS_ORG SET COMPANY_ID=? WHERE ID=?", orgnization.getId(), orgnization.getId());
			} else if (companyId == null) {
				throw new BusinessException("获取公司信息异常！");
			}
		} else {
			isUpdate = true;
			Long id = genericDao.findSingle(Long.class, "SELECT ID FROM SYS_ORG WHERE CODE=? AND ID!=?", orgnization.getCode(), orgnization.getId());
			if (id != null)
				throw new BusinessException("机构编码已经存在！");
			orgnization.setUpdateUser(curUser.getId());
			orgnization.setUpdateTime(curTime);
			genericDao.update(orgnization);
		}
		// 更改指定设备为当前机构（合伙人/网点）
		saveBindDevices(isUpdate, orgnization.getId(), devNos);
		dictionaryService.findDataToCache();
	}
	
	/**
	 * 更改指定设备为当前机构（合伙人/网点）
	 * @param devNos
	 */
	public void saveBindDevices(boolean isUpdate, Long orgId, String... devNos) {
		User curUser = ContextUtil.getUser(User.class);
		if (null == curUser) throw new BusinessException("当前用户未登录，请登录后操作。");
		if (null != devNos && devNos.length > 0) {
			StringBuffer buf = new StringBuffer(" IN(");
			List<String> args = new ArrayList<String>();
			for (String devNo : devNos) {
				buf.append("?,");
				args.add(devNo);
			}
			buf.setLength(buf.length() - 1);
			buf.append(")");
			String devNosSQL = buf.toString();
			buf.setLength(0);
			if (!isUpdate) {// insert操作
				// 将当前登录用户的指定设备，转移到形参orgId下
				List<Object> args2 = new ArrayList<Object>();
				buf.append("UPDATE T_DEVICE SET ORG_ID = ? WHERE DEV_NO").append(devNosSQL).append(" AND ORG_ID = ? ");
				args2.add(orgId);
				args2.addAll(args);
				args2.add(curUser.getOrgId());
				genericDao.execute(buf.toString(), args2.toArray());
				
				// 将t_we_user表中的该设备（如果存在）所属orgId更改为形参orgId
				args2.clear();
				args2.add(orgId);
				args2.addAll(args);
				genericDao.execute("UPDATE T_WE_USER SET ORG_ID=? WHERE DEV_NO" + devNosSQL, args2.toArray());
			} else {// update操作
				saveUpdateDevices(curUser, orgId, devNos);
			}
		} else {
			// 将当前机构（合伙人/网点）下的设备转移到上一级机构下
			genericDao.execute("UPDATE T_DEVICE SET ORG_ID = ? WHERE ORG_ID = ?", curUser.getOrgId(), orgId);
			// 将t_we_user表中的该设备（如果存在）所属orgId更改为上一级机构下
			genericDao.execute("UPDATE T_WE_USER SET ORG_ID=? WHERE ORG_ID = ?", curUser.getOrgId(), orgId);
		}
	}
	
	/**
	 * 更新绑定设备
	 */
	public void saveUpdateDevices(User user, Long orgId, String... devNos) {
        // 表中原始数据
        List<Device> originDevices = findDevices(orgId);
        // 获取表中原始数据的Map
        Map<String, Device> originDevicesMap = new HashMap<String, Device>();
        for (Device originDevice : originDevices)
        	originDevicesMap.put(originDevice.getDevNo(), originDevice);
        // 获取页面传递过来数据的Map
        Map<String, String> finalDevicesMap = new HashMap<String, String>();
        for (String devNo : devNos)
        	finalDevicesMap.put(devNo, devNo);
        
        // 1. 以【页面传递过来数据的Map】为基准：如果包含DB中的原始数据，则做更新操作；否则做删除操作
        for (Device device : originDevices) {
            String key = device.getDevNo();
            if (finalDevicesMap.keySet().contains(key)) {
                // 更新
            } else {
                // 删除
            	// 将当前机构（合伙人/网点）下的设备转移到上一级机构下
            	device.setOrgId(user.getOrgId());
            	device.setUpdateUser(user.getId());
            	device.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            	genericDao.update(device);
            	
            	// 将t_we_user表中的该设备（如果存在）所属orgId更改为上一级机构下
    			genericDao.execute("UPDATE T_WE_USER SET ORG_ID=? WHERE DEV_NO = ?", user.getOrgId(), key);
            }
        }
        
        // 2. 以【表中原始数据的Map】为基准：如果不包含页面传递过来数据，则做新增操作
        for (String devNo : devNos) {
            if (!originDevicesMap.keySet().contains(devNo)) {
                // 新增
				genericDao.execute("UPDATE T_DEVICE SET ORG_ID = ? WHERE DEV_NO =? AND ORG_ID = ? ", orgId, devNo, user.getOrgId());
				
				// 将t_we_user表中的该设备（如果存在）所属orgId更改为上一级机构下
    			genericDao.execute("UPDATE T_WE_USER SET ORG_ID=? WHERE DEV_NO = ?", orgId, devNo);
            }
        }
    }
	
	/**
	 * 根据org_id查询设备
	 * @param orgId
	 * @return
	 */
	public List<Device> findDevices(Long orgId) {
		StringBuffer buf = new StringBuffer("SELECT ");
		String cols = SQLUtils.getColumnsSQL(Device.class, "C");
		buf.append(cols);
		buf.append(" FROM T_DEVICE C WHERE C.ORG_ID=? ");
		List<Object> args = new ArrayList<Object>();
		args.add(orgId);
		return genericDao.findTs(Device.class, buf.toString(), args.toArray());
	}
	
	/**
	 * 根据机构ID查询机构所属公司ID
	 * @param id	机构ID
	 * @return	所属公司ID
	 */
	private Long findCompanyIdByOrgId(Long id) {
		return genericDao.findSingle(Long.class, "SELECT COMPANY_ID FROM SYS_ORG WHERE ID=?", id);
	}

	/* (non-Javadoc)
	 * @see com.ecarry.service.ISystemService#deleteOrgnizations(java.lang.Long[])
	 */
	@Override
	public void deleteOrgnizations(Long... ids) {
		if (ids != null && ids.length != 0) {
			StringBuffer buf = new StringBuffer(" IN(");
			List<Orgnization> orgnizations = findOrgnizations(ids);
			Set<Long> args = new HashSet<Long>();
			for (Long id : ids) {
				buf.append("?,");
				args.add(id);
			}
			for (Orgnization orgnization : orgnizations) {
				buf.append("?,");
				args.add(orgnization.getId());
			}
			buf.setLength(buf.length() - 1);
			buf.append(")");
			String inIds = buf.toString();
			buf.setLength(0);
			if (!Commons.isSystemUser()) {	// 防止黑客攻击
				buf.append("SELECT COUNT(ID) FROM SYS_ORG WHERE PARENT_ID IS NULL AND ID").append(inIds);
				int count = genericDao.findSingle(int.class, buf.toString(), args.toArray());
				if (count != 0)
					throw new BusinessException("非平台用户禁止操作！");
				buf.setLength(0);
			}
			buf.append("SELECT COUNT(ID) FROM SYS_USER WHERE ORG_ID").append(inIds);
			int count = genericDao.findSingle(int.class, buf.toString(), args.toArray());
			if (count != 0)
				throw new BusinessException("所选机构或其子机构存在用户！");
			buf.setLength(0);
			buf.append("SELECT COUNT(ID) FROM SYS_ROLE WHERE ORG_ID").append(inIds);
			count = genericDao.findSingle(Integer.class, buf.toString(), args.toArray());
			if (count != 0)
				throw new BusinessException("所选机构或其子机构存在角色！");
			
			buf.setLength(0);
			buf.append("SELECT COUNT(ID) FROM T_POINT_PLACE WHERE ORG_ID").append(inIds);
			buf.append(" AND STATE != 9 ");
			count = genericDao.findSingle(Integer.class, buf.toString(), args.toArray());
			if (count != 0)
				throw new BusinessException("所选机构或其子机构存在店铺！");
			
			buf.setLength(0);
			buf.append("SELECT COUNT(ID) FROM T_DEVICE WHERE ORG_ID").append(inIds);
			count = genericDao.findSingle(Integer.class, buf.toString(), args.toArray());
			if (count != 0)
				throw new BusinessException("所选机构或其子机构存在设备！");
			
			buf.setLength(0);
			List<Object> args2 = new ArrayList<Object>();
			buf.append("DELETE FROM SYS_ORG WHERE ID").append(inIds);
			args2.addAll(args);
			genericDao.execute(buf.toString(), args2.toArray());
			

			// 将当前机构（合伙人/网点）下的设备转移到上一级机构下
			User user = ContextUtil.getUser(User.class);
			if (null == user) throw new BusinessException("当前用户还未登录！");
			args2.clear();
			args2.add(user.getOrgId());
			args2.addAll(args);
			genericDao.execute("UPDATE T_DEVICE SET ORG_ID = ? WHERE ORG_ID" + inIds, args2.toArray());
			// 将t_we_user表中的该设备（如果存在）所属orgId更改为上一级机构下
			genericDao.execute("UPDATE T_WE_USER SET ORG_ID=? WHERE ORG_ID" + inIds, args2.toArray());
						
			dictionaryService.findDataToCache();
			dictionaryService.saveSyncSysLog(SysType.class);
		}
	}
	
	private List<Orgnization> findOrgnizations(Long[] parentIds) {
		StringBuffer buf = new StringBuffer();
		buf.append(SQLUtils.getSelectSQL(Orgnization.class)).append(" WHERE ");
		if (parentIds == null) {
			buf.append("PARENT_ID IS NULL AND ");
		} else {
			buf.append("PARENT_ID IN(");
			for (int i = 0; i < parentIds.length; i++)
				buf.append("?,");
			buf.setLength(buf.length() - 1);
			buf.append(")");
		}
		List<Long> args = new ArrayList<Long>(Arrays.asList(parentIds));
		List<Orgnization> list = genericDao.findTs(Orgnization.class, buf.toString(), args.toArray());
		if (list.size() != 0) {
			parentIds = new Long[list.size()];
			for (int i = 0; i < list.size(); i++)
				parentIds[i] = list.get(i).getId();
			list.addAll(findOrgnizations(parentIds));
		}
		return list;
	}

	/* (non-Javadoc)
	 * @see com.ecarry.service.ISystemService#findUsers(com.ecarry.core.web.core.Page, com.ecarry.core.domain.User, java.sql.Date, java.sql.Date)
	 */
	@Override
	public List<User> findUsers(Page page, User user, Date startDate, Date endDate) {
		user.setEditable(true);
		StringBuffer buf = new StringBuffer();
		buf.append("SELECT ").append(SQLUtils.getColumnsSQL(User.class, "A", "password"));
		buf.append(",B.NAME orgName FROM SYS_USER A LEFT JOIN SYS_ORG B ON A.ORG_ID=B.ID");
		List<Object> args = new ArrayList<Object>();
		if (user.getOrgId() == null) {
			user.setOrgId(ContextUtil.getUser(User.class).getOrgId());
			
			buf.append(args.size() == 0 ? " WHERE" : " AND").append(" (A.ORG_ID=?");
			args.add(user.getOrgId());
			if (Commons.isSystemUser()) {
				buf.append(" OR B.COMPANY_ID=?");
				args.add(user.getOrgId());
			}
			buf.append(" OR A.ORG_ID IN (SELECT ID FROM SYS_ORG WHERE PARENT_ID = ?)");
			args.add(user.getOrgId());
		} else {
			buf.append(args.size() == 0 ? " WHERE" : " AND").append(" (A.ORG_ID=?");
			args.add(user.getOrgId());
			if (Commons.isSystemUser()) {
				buf.append(" OR B.COMPANY_ID=?");
				args.add(user.getOrgId());
			}
		}
		buf.append(")");
		
		buf.append(args.size() == 0 ? " WHERE" : " AND").append(" A.editable = true ");
		if (user.getCompanyId() != null) {
			buf.append(args.size() == 0 ? " WHERE" : " AND").append(" B.COMPANY_ID=?");
			args.add(user.getCompanyId());
		}
		if (user.getOrgName() != null) {
			buf.append(args.size() == 0 ? " WHERE" : " AND").append(" (B.NAME LIKE ? OR B.NAME LIKE ?)");
			args.add("%" + ZHConverter.convert(user.getOrgName(), ZHConverter.TRADITIONAL) + "%");
			args.add("%" + ZHConverter.convert(user.getOrgName(), ZHConverter.SIMPLIFIED) + "%");
		}
		
		if (user.getUsername() != null) {
			buf.append(args.size() == 0 ? " WHERE" : " AND").append(" ( A.USERNAME LIKE ? OR A.USERNAME LIKE ? )");
			args.add("%" + ZHConverter.convert(user.getUsername(), ZHConverter.TRADITIONAL) + "%");
			args.add("%" + ZHConverter.convert(user.getUsername(), ZHConverter.SIMPLIFIED) + "%");
		}
		if (user.getRealName() != null) {
			buf.append(args.size() == 0 ? " WHERE" : " AND").append(" ( A.REAL_NAME LIKE ? OR A.REAL_NAME LIKE ? )");
			args.add("%" + ZHConverter.convert(user.getRealName(), ZHConverter.TRADITIONAL) + "%");
			args.add("%" + ZHConverter.convert(user.getRealName(), ZHConverter.SIMPLIFIED) + "%");
		}
		
		if (startDate != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(startDate);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			Timestamp date = new Timestamp(cal.getTimeInMillis());
			buf.append(args.size() == 0 ? " WHERE" : " AND").append(" A.CREATE_TIME>=?");
			args.add(date);
		}
		if (endDate != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(endDate);
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
			cal.set(Calendar.SECOND, 59);
			cal.set(Calendar.MILLISECOND, 999);
			Timestamp date = new Timestamp(cal.getTimeInMillis());
			buf.append(args.size() == 0 ? " WHERE" : " AND").append(" A.CREATE_TIME<=?");
			args.add(date);
		}
		logger.info("******【用户管理】SQL********" + buf.toString());
		for (Object object : args) {
			logger.info("******【用户管理】参数********" + object.toString());
		}
		return genericDao.findTs(User.class, page, buf.toString(), args.toArray());
	}

	/* (non-Javadoc)
	 * @see com.ecarry.service.ISystemService#saveUser(com.ecarry.core.domain.User)
	 */
	@Override
	public void saveUser(User user) {
		if (Commons.isAdminName(user.getUsername()))
			throw new BusinessException("禁止使用系统内置用户名！");
		
		if (user.getUsername().contains("_"))
			throw new BusinessException("用户名称不可以包含下划线");
		
		User curUser = ContextUtil.getUser(User.class);
		Timestamp curTime = new Timestamp(System.currentTimeMillis());
//		Long companyId = Long.valueOf(Commons.ORG_HQ);
//		SecurityUtils.checkSystemAccess(companyId);
		String sql = "SELECT ID,USERNAME,NICKNAME,MOBILE,EMAIL FROM SYS_USER WHERE USERNAME=? OR MOBILE=? OR EMAIL=?";
		User oldUser = genericDao.findT(User.class, sql, user.getUsername(), user.getMobile(), user.getEmail());
		user.initDefaultValue();
		if (user.getId() == null) {
//			if (curUser.getCompanyId().longValue() != Commons.ORG_HQ) {
//				int count = genericDao.findSingle(int.class, "SELECT COUNT(*) FROM SYS_USER WHERE EDITABLE=? AND COMPANY_ID=?", true, curUser.getCompanyId());
//				if (count >= curUser.getUsers().intValue())
//					throw new BusinessException("用户数超限，请联系购买！");
//			}
			if (oldUser != null) {
				if (oldUser.getUsername().equals(user.getUsername()))
					throw new BusinessException("用户名已经存在！");
				if (oldUser.getMobile() != null && oldUser.getMobile().equals(user.getMobile()))
					throw new BusinessException("手机号码已存在！");
				if (oldUser.getEmail().equals(user.getEmail()))
					throw new BusinessException("电子邮箱已存在！");
			}
			if (user.getPassword() == null)
				user.setPassword(ContextUtil.getPassword(dictionaryService.findDefaultPassword()));
			Long companyId = genericDao.findSingle(Long.class, "SELECT COMPANY_ID FROM SYS_ORG WHERE ID=?", user.getOrgId());
			user.setCompanyId(companyId);
			user.setCreateUser(curUser.getId());
			user.setCreateTime(curTime);
			user.setEnable(Commons.USER_ENABLE_TRUE);
			genericDao.save(user);
		} else {
			if (!genericDao.findSingle(boolean.class, "SELECT EDITABLE FROM SYS_USER WHERE ID=?", user.getId()))
				throw new BusinessException("禁止修改系统内置用户！");			
			if (oldUser != null && oldUser.getId().longValue() != user.getId().longValue()) {
				if (oldUser.getUsername().equals(user.getUsername()))
					throw new BusinessException("用户名已经存在！");
				if (oldUser.getMobile().equals(user.getMobile()))
					throw new BusinessException("手机号码已存在！");
				if (oldUser.getEmail().equals(user.getEmail()))
					throw new BusinessException("电子邮箱已存在！");
			}
			Long id = genericDao.findSingle(Long.class, "SELECT COALESCE(ID, 0) FROM SYS_USER WHERE USERNAME=? AND ID!=?", user.getUsername(), user.getId());
			if (id != null && id != 0)
				throw new BusinessException("用户名已经存在！");
			id = genericDao.findSingle(Long.class, "SELECT COALESCE(ID,0) FROM SYS_USER WHERE MOBILE=? AND ID!=?", user.getMobile(), user.getId());
			if (id != null && id != 0)
				throw new BusinessException("手机号码已存在！");
			id = genericDao.findSingle(Long.class, "SELECT COALESCE(ID,0) FROM SYS_USER WHERE EMAIL=? AND ID!=?", user.getMobile(), user.getId());
			if (id != null && id != 0)
				throw new BusinessException("电子邮箱已存在！");
			user.setUpdateUser(curUser.getId());
			user.setUpdateTime(curTime);
			BoxValue<String, Object[]> box = SQLUtils.getUpdateSQLByExclude(user, "password,companyId,editable,createUser,createTime,lastLoginMachine,lastLoginTime,pwdUpdateTime");
			genericDao.execute(box.getKey(), box.getValue());
		}
	}
	
	/* (non-Javadoc)
	 * @see com.meifan.service.ISystemService#saveUserInfo(com.ecarry.core.domain.User)
	 */
	@Override
	public void saveUserInfo(User user) {
//		genericDao.execute("UPDATE T_FILE SET CREATE_USER=? WHERE CREATOR_ID=?", user.getNickname(), user.getId());
//		genericDao.execute("UPDATE T_QUESTION SET CREATOR_LOGO=?,CREATE_USER=? WHERE CREATOR_ID=?", user.getPortrait(), user.getNickname(), user.getId());
//		genericDao.execute("UPDATE T_QUESTION SET UPDATOR_LOGO=?,UPDATE_USER=? WHERE UPDATOR_ID=?", user.getPortrait(), user.getNickname(), user.getId());
//		genericDao.execute("UPDATE T_ANSWER SET QUESTIONER_LOGO=?,QUESTIONER=? WHERE QUESTIONER_ID=?", user.getPortrait(), user.getNickname(), user.getId());
//		genericDao.execute("UPDATE T_ANSWER SET CREATOR_LOGO=?,CREATE_USER=? WHERE CREATOR_ID=?", user.getPortrait(), user.getNickname(), user.getId());
//		genericDao.execute("UPDATE T_EXPERIENCE SET CREATOR_LOGO=?,CREATE_USER=? WHERE CREATOR_ID=?", user.getPortrait(), user.getNickname(), user.getId());
//		genericDao.execute("UPDATE T_EXPERIENCE SET UPDATOR_LOGO=?,UPDATE_USER=? WHERE UPDATOR_ID=?", user.getPortrait(), user.getNickname(), user.getId());
	}

	/* (non-Javadoc)
	 * @see com.meifan.service.ISystemService#saveUserInfo(com.ecarry.core.domain.User, com.ecarry.core.domain.WebUploader)
	 */
	@Override
	public void saveUserInfo(User user, WebUploader uploader) {
		if (user.getPortrait() != null) {
			try {
				QiniuUtil.delete(user.getPortrait());
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage());
			}
		}
		if (uploader.getFile() != null) {
			String suffix = uploader.getFile().getOriginalFilename().substring(uploader.getFile().getOriginalFilename().lastIndexOf("."));
			String filePath = "logo/" + idWorker.nextId() + suffix;
			try {
				QiniuUtil.upload(uploader.getFile().getBytes(), filePath);
				user.setPortrait(filePath);
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage());
			}
		}
		BoxValue<String, Object[]> box = SQLUtils.getUpdateSQLBySearch(user, "portrait");
		genericDao.execute(box.getKey(), box.getValue());
	}

	/* (non-Javadoc)
	 * @see com.ecarry.service.ISystemService#deleteUsers(java.lang.Long[])
	 */
	@Override
	public void deleteUsers(Long... ids) {
		if (ids != null && ids.length != 0) {
			StringBuffer buf = new StringBuffer(" IN(");
			for (Long id : ids) {
				if (id.equals(ContextUtil.getUser(User.class).getId()))
					throw new BusinessException("不能删除当前登录用户！");
				buf.append("?,");
			}
			buf.setLength(buf.length() - 1);
			buf.append(")");
			String inIds = buf.toString();
			buf.setLength(0);
			buf.append("SELECT COUNT(ID) FROM SYS_USER WHERE EDITABLE=false AND ID").append(inIds);
			int count = genericDao.findSingle(int.class, buf.toString(), (Object[]) ids);
			if (count != 0)
				throw new BusinessException("禁止删除系统内置用户！");
			
			buf.setLength(0);
			buf.append(" SELECT COUNT(ID) FROM T_POINT_PLACE WHERE CREATE_USER ").append(inIds);
			buf.append(" AND STATE != 9 ");
			count = genericDao.findSingle(Integer.class, buf.toString(), (Object[]) ids);
			if (count != 0)
				throw new BusinessException("当前用户下存在店铺！");
			
			buf.setLength(0);
			buf.append("DELETE FROM SYS_USER_ROLE WHERE USER_ID").append(inIds);
			genericDao.execute(buf.toString(), (Object[]) ids);
			buf.setLength(0);
			buf.append("DELETE FROM SYS_USER WHERE ID").append(inIds);
			genericDao.execute(buf.toString(), (Object[]) ids);
		}
	}

	/* (non-Javadoc)
	 * @see com.ecarry.service.ISystemService#findSysTypes(com.ecarry.core.web.core.Page, com.ecarry.core.domain.SysType)
	 */
	@Override
	public List<SysType> findSysTypes(Page page, SysType sysType) {
		if (page != null)
			page.setOrder("refId,type,code");
		if (sysType == null)
			sysType = new SysType();
		sysType.setSysType(ContextUtil.getUser(User.class).getSysType());
		if (!Commons.isSystemUser())
			sysType.setDisplayable(true);
		StringBuffer buf = new StringBuffer("SELECT ");
		buf.append(SQLUtils.getColumnsSQL(SysType.class, "A"));
		buf.append(",B.NAME orgName FROM SYS_TYPE A LEFT JOIN SYS_ORG B ON A.ORG_ID=B.ID");
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("orgId", SQLUtils.IGNORE);
		BoxValue<String, List<Object>> box = SQLUtils.getCondition(sysType, map, "A");
		buf.append(box.getKey());
		if (sysType.getOrgId() == null) {
			if (!Commons.isSystemUser()) {
				buf.append(" AND (A.ORG_ID=? OR A.ORG_ID=?)");
				box.getValue().add(ContextUtil.getUser(User.class).getCompanyId());
				box.getValue().add(Commons.ORG_HQ);
			}
		} else {
			if (!Commons.isSystemUser() && sysType.getOrgId() != ContextUtil.getUser(User.class).getCompanyId() && sysType.getOrgId() != Commons.ORG_HQ)
				throw new BusinessException("非法请求！");
			buf.append(" AND A.ORG_ID=?");
			box.getValue().add(sysType.getOrgId());
		}
		return genericDao.findTs(SysType.class, page, buf.toString(), box.getValue().toArray());
	}

	/* (non-Javadoc)
	 * @see com.ecarry.service.ISystemService#saveSysType(com.ecarry.core.domain.SysType)
	 */
	@Override
	public void saveSysType(SysType sysType) {
		User curUser = ContextUtil.getUser(User.class);
		Timestamp curTime = new Timestamp(System.currentTimeMillis());
		if (sysType.getEditable() == null)
			sysType.setEditable(true);
		if (sysType.getDisplayable() == null)
			sysType.setDisplayable(true);
		if (sysType.getId() == null) {
			Long id = genericDao.findSingle(Long.class, "SELECT ID FROM SYS_TYPE WHERE CODE=? AND TYPE=? AND ORG_ID IN(?,?)", sysType.getCode(), sysType.getType(), curUser.getCompanyId(), Commons.ORG_HQ);
			if (id != null)
				throw new BusinessException("类型编码已经存在！");
			sysType.setOrgId(curUser.getCompanyId());
			sysType.setSysType(curUser.getSysType());
			sysType.setCreateUser(curUser.getUsername());
			sysType.setCreateTime(curTime);
			genericDao.save(sysType);
		} else {
			SecurityUtils.checkCompanyData(sysType.getOrgId(), "禁止编辑受保护的类型参数！");
			boolean editable = genericDao.findSingle(Boolean.class, "SELECT EDITABLE FROM SYS_TYPE WHERE ID=?", sysType.getId());
			if (!editable)
				throw new BusinessException("禁止编辑受保护的类型参数！");
			Long id = genericDao.findSingle(Long.class, "SELECT ID FROM SYS_TYPE WHERE CODE=? AND TYPE=? AND ORG_ID IN(?,?) AND ID!=?", sysType.getCode(), sysType.getType(), curUser.getCompanyId(), Commons.ORG_HQ, sysType.getId());
			if (id != null)
				throw new BusinessException("类型编码已经存在！");
			sysType.setSysType(curUser.getSysType());
			sysType.setUpdateUser(curUser.getUsername());
			sysType.setUpdateTime(curTime);
			genericDao.update(sysType);
		}
		dictionaryService.findDataToCache();
	}

	/* (non-Javadoc)
	 * @see com.ecarry.service.ISystemService#deleteSysTypes(java.lang.Long[])
	 */
	@Override
	public void deleteSysTypes(Long... ids) {
		if (ids != null && ids.length != 0) {
			User curUser = ContextUtil.getUser(User.class);
			List<Object> args = new ArrayList<Object>();
			args.add(false);
			args.add(curUser.getCompanyId());
			args.add(ContextUtil.getUser(User.class).getSysType());
			StringBuffer buf = new StringBuffer(" IN(");
			for (Long id : ids) {
				buf.append("?,");
				args.add(id);
			}				
			buf.setLength(buf.length() - 1);
			buf.append(")");
			String inIds = buf.toString();
			buf.setLength(0);
			buf.append("SELECT COUNT(*) FROM SYS_TYPE WHERE (EDITABLE=? OR ORG_ID!=?) AND SYS_TYPE=? AND ID").append(inIds);
			int count = genericDao.findSingle(Integer.class, buf.toString(), args.toArray());
			if (count != 0)
				throw new BusinessException("存在受保护的类型参数，请重新选择！");
			buf.setLength(0);
			buf.append("DELETE FROM SYS_TYPE WHERE ORG_ID=? AND SYS_TYPE=? AND ID").append(inIds);
			args.remove(0);
			genericDao.execute(buf.toString(), args.toArray());
			dictionaryService.findDataToCache();
			dictionaryService.saveSyncSysLog(SysType.class);
		}
	}

	/**
	 * 查询机构Code生产Code序列号
	 * @return	code
	 */
	private Long findCodeSeq() {
		return genericDao.findSingle(Long.class, "SELECT MAX(TO_NUMBER(CODE,'9999999999')) FROM SYS_ORG")+1l;
	}

	/**
	 * 分页条件查询平台机构用户信息
	 * @param page	分页信息
	 * @param user	查询条件
	 * @return	用户集
	 */
	@Override
	public List<User> findPlatformUser(Page page, User user) {
		user.setEditable(true);
		StringBuffer buf = new StringBuffer();
		buf.append("SELECT ").append(SQLUtils.getColumnsSQL(User.class, "A", "password"));
		buf.append(",B.NAME orgName FROM SYS_USER A LEFT JOIN SYS_ORG B ON A.ORG_ID=B.ID");
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("orgId", SQLUtils.IGNORE);
		map.put("retry", SQLUtils.IGNORE);
		BoxValue<String, List<Object>> box = SQLUtils.getCondition(user, map, "A");
		buf.append(box.getKey());
		List<Object> args = box.getValue();
		if (user.getOrgId() == null) {
			user.setOrgId(ContextUtil.getUser(User.class).getOrgId());
			
			buf.append(args.size() == 0 ? " WHERE" : " AND").append(" (A.ORG_ID=?");
			args.add(user.getOrgId());
			if (Commons.isSystemUser()) {
				buf.append(" OR B.COMPANY_ID=?");
				args.add(user.getOrgId());
			}
			buf.append(" OR A.ORG_ID IN (SELECT ID FROM SYS_ORG WHERE PARENT_ID = ?)");
			args.add(user.getOrgId());
		} else {
			buf.append(args.size() == 0 ? " WHERE" : " AND").append(" (A.ORG_ID=?");
			args.add(user.getOrgId());
			if (Commons.isSystemUser()) {
				buf.append(" OR B.COMPANY_ID=?");
				args.add(user.getOrgId());
			}
		}
		buf.append(")");
		
		if (user.getCompanyId() != null) {
			buf.append(args.size() == 0 ? " WHERE" : " AND").append(" B.COMPANY_ID=?");
			args.add(user.getCompanyId());
		}
		if (user.getOrgName() != null) {
			buf.append(args.size() == 0 ? " WHERE" : " AND").append(" (B.NAME LIKE ? OR B.NAME LIKE ?)");
			args.add("%" + ZHConverter.convert(user.getOrgName(), ZHConverter.TRADITIONAL) + "%");
			args.add("%" + ZHConverter.convert(user.getOrgName(), ZHConverter.SIMPLIFIED) + "%");
		}
		if (user.getUsername() != null) {
			buf.append(args.size() == 0 ? " WHERE" : " AND").append(" ( A.USERNAME LIKE ? OR A.USERNAME LIKE ? )");
			args.add("%" + ZHConverter.convert(user.getUsername(), ZHConverter.TRADITIONAL) + "%");
			args.add("%" + ZHConverter.convert(user.getUsername(), ZHConverter.SIMPLIFIED) + "%");
		}
		if (user.getRealName() != null) {
			buf.append(args.size() == 0 ? " WHERE" : " AND").append(" ( A.REAL_NAME LIKE ? OR A.REAL_NAME LIKE ? )");
			args.add("%" + ZHConverter.convert(user.getRealName(), ZHConverter.TRADITIONAL) + "%");
			args.add("%" + ZHConverter.convert(user.getRealName(), ZHConverter.SIMPLIFIED) + "%");
		}
		
		logger.info("******【用户管理】SQL********" + buf.toString());
		for (Object object : args) {
			logger.info("******【用户管理】参数********" + object.toString());
		}
		return genericDao.findTs(User.class, page, buf.toString(), args.toArray());
	}
	
	/**
	 * 保存平台机构信息
	 * @param orgnization	需要保存的机构对象
	 */
	@Override
	public void savePlatformOrg(Orgnization orgnization) {
		if (orgnization.getCode() == null) {
			orgnization.setCode(this.findCodeSeq().toString());
		}
		if (!GenericValidator.isInt(orgnization.getCode()))	// 防止黑客攻击，此处不正确的编码会导致内置的管理员用户名出现问题
			throw new BusinessException("机构编码格式错误！");
		User curUser = ContextUtil.getUser(User.class);
		Timestamp curTime = new Timestamp(System.currentTimeMillis());
		if (orgnization.getId() == null) {// 新增
			Long companyId = findCompanyIdByOrgId(orgnization.getParentId());
			Long id = genericDao.findSingle(Long.class, "SELECT ID FROM SYS_ORG WHERE ( CODE=? OR NAME=? )", orgnization.getCode(), orgnization.getName());
			if (id != null)
				throw new BusinessException("机构名称或编码已经存在！");
			orgnization.setCompanyId(companyId == null ? 0L : companyId);
			orgnization.setCreateUser(curUser.getId());
			orgnization.setCreateTime(curTime);
			orgnization.setSettledTime(curTime);
			genericDao.save(orgnization);
			if (orgnization.getParentId() == null) {
				genericDao.execute("UPDATE SYS_ORG SET COMPANY_ID=? WHERE ID=?", orgnization.getId(), orgnization.getId());
			} else if (companyId == null) {
				throw new BusinessException("获取公司信息异常！");
			}
		} else {//修改
			Long id = genericDao.findSingle(Long.class, "SELECT ID FROM SYS_ORG WHERE ( CODE=? OR NAME=? ) AND ID!=?", orgnization.getCode(), orgnization.getName(), orgnization.getId());
			if (id != null)
				throw new BusinessException("机构名称或编码已经存在！");
			orgnization.setUpdateUser(curUser.getId());
			orgnization.setUpdateTime(curTime);
			genericDao.update(orgnization);
		}
		dictionaryService.findDataToCache();
	}
	
	/**
	 * 删除平台机构信息
	 * @param ids	需要删除的机构ID集
	 */
	@Override
	public void deletePlatformOrg(Long... ids) {
		if (ids != null && ids.length != 0) {
			StringBuffer buf = new StringBuffer(" IN(");
			List<Orgnization> orgnizations = findOrgnizations(ids);// 查出机构信息（含子机构信息）
			Set<Long> args = new HashSet<Long>();
			for (Long id : ids) {
				buf.append("?,");
				args.add(id);
			}
			for (Orgnization orgnization : orgnizations) {
				buf.append("?,");
				args.add(orgnization.getId());
			}
			buf.setLength(buf.length() - 1);
			buf.append(")");
			String inIds = buf.toString();
			buf.setLength(0);
			if (!Commons.isSystemUser()) {	// 防止黑客攻击
				buf.append("SELECT COUNT(ID) FROM SYS_ORG WHERE PARENT_ID IS NULL AND ID").append(inIds);
				int count = genericDao.findSingle(int.class, buf.toString(), args.toArray());
				if (count != 0)
					throw new BusinessException("非平台用户禁止操作！");
				buf.setLength(0);
			}
			buf.append("SELECT COUNT(ID) FROM SYS_USER WHERE ORG_ID").append(inIds);
			int count = genericDao.findSingle(int.class, buf.toString(), args.toArray());
			if (count > 1)	// 要忽略新建公司时自动新增的默认管理员用户
				throw new BusinessException("所选机构或其子机构存在用户！");
			buf.setLength(0);
			buf.append("SELECT COUNT(ID) FROM SYS_ROLE WHERE ORG_ID").append(inIds);
			count = genericDao.findSingle(Integer.class, buf.toString(), args.toArray());
			if (count != 0)
				throw new BusinessException("所选机构或其子机构存在角色！");
			buf.setLength(0);
			List<Object> args2 = new ArrayList<Object>();
			buf.append("DELETE FROM SYS_ORG WHERE ID").append(inIds);
			args2.addAll(args);
			genericDao.execute(buf.toString(), args2.toArray());
			
			dictionaryService.findDataToCache();
			dictionaryService.saveSyncSysLog(SysType.class);
		}
	}
	
	/**
	 * 保存平台机构用户信息
	 * @param user	用户对象
	 */
	@Override
	public void savePlatformUser(User user) {
		if (Commons.isAdminName(user.getUsername()))
			throw new BusinessException("禁止使用系统内置用户名！");
		User curUser = ContextUtil.getUser(User.class);
		Timestamp curTime = new Timestamp(System.currentTimeMillis());
		
		String sql = "SELECT ID,USERNAME,NICKNAME,MOBILE,EMAIL FROM SYS_USER WHERE USERNAME=? OR MOBILE=? OR EMAIL=?";
		User oldUser = genericDao.findT(User.class, sql, user.getUsername(), user.getMobile(), user.getEmail());
		user.initDefaultValue();
		if (user.getId() == null) {
			if (oldUser != null) {
				if (oldUser.getUsername().equals(user.getUsername()))
					throw new BusinessException("登录账号已经存在！");
				if (oldUser.getMobile() != null && oldUser.getMobile().equals(user.getMobile()))
					throw new BusinessException("手机号码已存在！");
				if (oldUser.getEmail().equals(user.getEmail()))
					throw new BusinessException("电子邮箱已存在！");
			}
			if (user.getPassword() == null)
				user.setPassword(ContextUtil.getPassword(dictionaryService.findDefaultPassword()));
			Long companyId = genericDao.findSingle(Long.class, "SELECT COMPANY_ID FROM SYS_ORG WHERE ID=?", user.getOrgId());
			user.setCompanyId(companyId);
			user.setCreateUser(curUser.getId());
			user.setCreateTime(curTime);
			user.setEnable(Commons.USER_ENABLE_TRUE);
			genericDao.save(user);
		} else {
			if (!genericDao.findSingle(boolean.class, "SELECT EDITABLE FROM SYS_USER WHERE ID=?", user.getId()))
				throw new BusinessException("禁止修改系统内置用户！");
			if (oldUser != null && oldUser.getId().longValue() != user.getId().longValue()) {
				if (oldUser.getUsername().equals(user.getUsername()))
					throw new BusinessException("登录账号已经存在！");
				if (oldUser.getMobile().equals(user.getMobile()))
					throw new BusinessException("手机号码已存在！");
				if (oldUser.getEmail().equals(user.getEmail()))
					throw new BusinessException("电子邮箱已存在！");
			}
			Long id = genericDao.findSingle(Long.class, "SELECT ID FROM SYS_USER WHERE USERNAME=? AND ID!=?", user.getUsername(), user.getId());
			if (id != null)
				throw new BusinessException("登录账号已经存在！");
			id = genericDao.findSingle(Long.class, "SELECT ID FROM SYS_USER WHERE MOBILE=? AND ID!=?", user.getMobile(), user.getId());
			if (id != null)
				throw new BusinessException("手机号码已存在！");
			id = genericDao.findSingle(Long.class, "SELECT ID FROM SYS_USER WHERE EMAIL=? AND ID!=?", user.getEmail(), user.getId());
			if (id != null)
				throw new BusinessException("电子邮箱已存在！");
			user.setUpdateUser(curUser.getId());
			user.setUpdateTime(curTime);
			BoxValue<String, Object[]> box = SQLUtils.getUpdateSQLByExclude(user, "password,companyId,editable,createUser,createTime,lastLoginMachine,lastLoginTime,pwdUpdateTime");
			genericDao.execute(box.getKey(), box.getValue());
			if (!oldUser.getNickname().equals(user.getNickname()))
				saveUserInfo(user);
		}
	}
	
	/**
	 * 更改机构状态  0：启用  1:禁用; （城市合伙人/网点）
	 */
	public void saveAccountOrgState(Long orgId, Integer state) {
		if (null == orgId)
			throw new BusinessException("机构ID为空！");
		if (null == state || (Commons.ORG_STATE_DISABLE != state && Commons.ORG_STATE_ENABLE != state))
			throw new BusinessException("机构状态异常！");
		int count = genericDao.execute(" UPDATE SYS_ORG SET STATE = ? WHERE ID = ? ", state, orgId);
		if (count <= 0)
			throw new BusinessException("机构ID不存在！");
	}
	
	/* (non-Javadoc)
	 * @see com.ecarry.service.ISystemService#saveOrgnization(com.ecarry.core.domain.Orgnization)
	 */
	@Override
	public void saveVenderPartnerOrgnization(Orgnization orgnization) {
		if (orgnization.getCode() == null) {
			orgnization.setCode(this.findCodeSeq().toString());
		}
		if (!GenericValidator.isInt(orgnization.getCode()))	// 防止黑客攻击，此处不正确的编码会导致内置的管理员用户名出现问题
			throw new BusinessException("机构编码格式错误！");
		User curUser = ContextUtil.getUser(User.class);
		Timestamp curTime = new Timestamp(System.currentTimeMillis());
		if (orgnization.getId() == null) {
			Long companyId = findCompanyIdByOrgId(orgnization.getParentId());
			Long id = genericDao.findSingle(Long.class, "SELECT ID FROM SYS_ORG WHERE CODE=?", orgnization.getCode());
			if (id != null)
				throw new BusinessException("机构编码已经存在！");
			Long orgNameId = genericDao.findSingle(Long.class, "SELECT ID FROM SYS_ORG WHERE NAME=? ", orgnization.getName());
			if (orgNameId != null)
				throw new BusinessException("机构名称已经存在！");
			
			orgnization.setCompanyId(companyId == null ? 0L : companyId);
			orgnization.setCreateUser(curUser.getId());
			orgnization.setCreateTime(curTime);
			orgnization.setSettledTime(curTime);
			genericDao.save(orgnization);
			if (orgnization.getParentId() == null) {
				genericDao.execute("UPDATE SYS_ORG SET COMPANY_ID=? WHERE ID=?", orgnization.getId(), orgnization.getId());
			} else if (companyId == null) {
				throw new BusinessException("获取公司信息异常！");
			}
		} else {
			Long id = genericDao.findSingle(Long.class, "SELECT ID FROM SYS_ORG WHERE CODE=? AND ID!=?", orgnization.getCode(), orgnization.getId());
			if (id != null)
				throw new BusinessException("机构编码已经存在！");
			Long orgNameId = genericDao.findSingle(Long.class, "SELECT ID FROM SYS_ORG WHERE NAME=? AND ID!=? ", orgnization.getName(), orgnization.getId());
			if (orgNameId != null)
				throw new BusinessException("机构名称已经存在！");
			
			orgnization.setUpdateUser(curUser.getId());
			orgnization.setUpdateTime(curTime);
			genericDao.update(orgnization);
		}
		dictionaryService.findDataToCache();
	}
	
	/**
	 * 更改指定设备为当前机构（合伙人/网点）
	 * @param devNos
	 */
	public void saveBindDevices(Integer bindingFlag, Long orgId, String... devNos) {
		if (null == bindingFlag || (Commons.DEVICE_BIND_TRUE != bindingFlag && Commons.DEVICE_BIND_FALSE != bindingFlag))
			throw new BusinessException("非法操作");
		
		User curUser = ContextUtil.getUser(User.class);
		if (null == curUser)
			throw new BusinessException("当前用户未登录，请登录后操作。");
		
		if (null != devNos && devNos.length > 0) {
			StringBuffer buf = new StringBuffer(" IN(");
			List<String> args = new ArrayList<String>();
			for (String devNo : devNos) {
				buf.append("?,");
				args.add(devNo);
			}
			buf.setLength(buf.length() - 1);
			buf.append(")");
			String devIdsSQL = buf.toString();
			buf.setLength(0);
			if (Commons.DEVICE_BIND_TRUE == bindingFlag) {// 绑定设备
				// 将当前登录用户的指定设备，转移到形参orgId下
				List<Object> args2 = new ArrayList<Object>();
				buf.append("UPDATE T_DEVICE SET ORG_ID = ? WHERE DEV_NO").append(devIdsSQL).append(" AND ORG_ID = ? ");
				args2.add(orgId);
				args2.addAll(args);
				args2.add(curUser.getOrgId());
				genericDao.execute(buf.toString(), args2.toArray());
				
				// 将t_we_user表中的该设备（如果存在）所属orgId更改为形参orgId
				args2.clear();
				args2.add(orgId);
				args2.addAll(args);
				genericDao.execute("UPDATE T_WE_USER SET ORG_ID=? WHERE DEV_NO" + devIdsSQL, args2.toArray());
			} else {// 解绑设备
				// 将形参orgId下的指定设备转移到上一级机构
				List<Object> args2 = new ArrayList<Object>();
				buf.append("UPDATE T_DEVICE SET ORG_ID = ?, POINT_ID = 0, BIND_STATE = 0 WHERE DEV_NO").append(devIdsSQL).append(" AND ORG_ID = ? ");
				args2.add(curUser.getOrgId());
				args2.addAll(args);
				args2.add(orgId);
				genericDao.execute(buf.toString(), args2.toArray());
				
				// 将t_we_user表中的该设备（如果存在）所属orgId更改为上一级机构下
				args2.clear();
				args2.add(curUser.getOrgId());
				args2.addAll(args);
				genericDao.execute("UPDATE T_WE_USER SET ORG_ID=? WHERE DEV_NO" + devIdsSQL, args2.toArray());
			}
		}
	}
	
}
