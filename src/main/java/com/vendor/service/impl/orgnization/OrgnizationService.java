package com.vendor.service.impl.orgnization;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.ecarry.core.dao.IGenericDao;
import com.ecarry.core.dao.SQLUtils;
import com.ecarry.core.domain.Authority;
import com.ecarry.core.domain.SysType;
import com.ecarry.core.domain.UserRole;
import com.ecarry.core.exception.BusinessException;
import com.ecarry.core.util.ZHConverter;
import com.ecarry.core.web.core.ContextUtil;
import com.ecarry.core.web.core.Page;
import com.vendor.po.Orgnization;
import com.vendor.po.Role;
import com.vendor.po.User;
import com.vendor.service.IDictionaryService;
import com.vendor.service.IOrgnizationService;
import com.vendor.util.CommonUtil;
import com.vendor.util.Commons;
import com.vendor.util.DateUtil;

@Service("orgnizationService")
public class OrgnizationService implements IOrgnizationService {

	private static final Logger logger = Logger.getLogger(OrgnizationService.class);
	
	@Autowired
	private IGenericDao genericDao;
	
	@Autowired
	private IDictionaryService dictionaryService;

	/**
	 * 循环递归查询组织信息
	 * @param parentId	父组织ID
	 * @return	树状结构的组织集
	 */
	@Override
	public List<Orgnization> findCyclicOrgnizations(Long parentId) {
		StringBuffer buf = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		buf.append(SQLUtils.getSelectSQL(Orgnization.class)).append(" WHERE ");
		if (parentId == null) {
			buf.append("PARENT_ID IS NULL");
		} else {
			buf.append("ID=?");
			args.add(parentId);
		}
		buf.append(" AND STATE = ? ");
		args.add(Commons.ORG_STATE_ENABLE); //启用状态 
		buf.append(" AND IS_RELATE = ? ");
		args.add(Commons.ORG_IS_RELATE_TRUE); //已关联(存在管理关系)
		
		buf.append(" ORDER BY CODE");
		List<Orgnization> orgnizations = genericDao.findTs(Orgnization.class, buf.toString(), args.toArray());
		searchOrgnizationChildren(orgnizations);
		return orgnizations;
	}

	private void searchOrgnizationChildren(List<Orgnization> parentOrgnizations) {
		if (parentOrgnizations != null && parentOrgnizations.size() != 0) {
			StringBuffer buf = new StringBuffer();
			List<Object> args = new ArrayList<Object>();
			buf.append(SQLUtils.getSelectSQL(Orgnization.class)).append(" WHERE PARENT_ID IN(");
			for (Orgnization orgnization : parentOrgnizations) {// 2
				buf.append("?,");
				args.add(orgnization.getId());
			}
			buf.setLength(buf.length() - 1);
			buf.append(")");
			
			buf.append(" AND STATE = ? ");
			args.add(Commons.ORG_STATE_ENABLE); //启用状态 
			buf.append(" AND IS_RELATE = ? ");
			args.add(Commons.ORG_IS_RELATE_TRUE); //已关联(存在管理关系)
			
			buf.append(" ORDER BY PARENT_ID,CODE");
			List<Orgnization> orgnizations = genericDao.findTs(Orgnization.class, buf.toString(), args.toArray());
			int start = 0;
			for (Orgnization orgnization : orgnizations) {
				if (parentOrgnizations.get(start).getId().longValue() == orgnization.getParentId().longValue()) {
					parentOrgnizations.get(start).addOrgnization(orgnization);
				} else {
					for (int i = 0; i < parentOrgnizations.size(); i++) {
						if (parentOrgnizations.get(i).getId().longValue() == orgnization.getParentId().longValue()) {
							parentOrgnizations.get(i).addOrgnization(orgnization);
							start = i;
							break;
						}
					}
				}
			}
			if (orgnizations.size() != 0) {
				searchOrgnizationChildren(orgnizations);
			}
		}
	}
	
	/**
	 * 获取当前登录用户信息
	 * @return	当前登录用户信息
	 */
	@Override
	public User findCurrentUser() {
		User curUser = ContextUtil.getUser(User.class);
		StringBuffer buf = new StringBuffer("SELECT ");
		String columns = SQLUtils.getColumnsSQL(User.class, "U");
		buf.append(columns);
		
		if (!"admin".equals(curUser.getUsername()))
			buf.append(" ,STRING_AGG(R.NAME, ',' ORDER BY R.ID) AS ROLENAME ");
		
		buf.append(" FROM SYS_USER U ");
		if (!"admin".equals(curUser.getUsername())) {
			buf.append(" LEFT JOIN SYS_USER_ROLE UR ON U.ID = UR.USER_ID ");
			buf.append(" LEFT JOIN SYS_ROLE R ON R.ID = UR.ROLE_ID ");
		}
		buf.append(" WHERE 1=1 ");
		List<Object> args = new ArrayList<>();
		buf.append(" AND U.ID = ? ");
		args.add(curUser.getId());
		buf.append(" GROUP BY ").append(columns);
		return genericDao.findT(User.class, buf.toString(), args.toArray());
	}
	
	/**
	 * 修改当前登录用户信息
	 */
	@Override
	public void saveCurrentUser(User user) {
		User curUser = ContextUtil.getUser(User.class);
		if (null == curUser)
			throw new BusinessException("非法请求");
		
		User userDB = findUserById(curUser.getId());
		if (!StringUtils.isEmpty(user.getRealName())) // 真实姓名
			userDB.setRealName(user.getRealName());
		
		if (!StringUtils.isEmpty(user.getMobile())) // 联系电话
			userDB.setMobile(user.getMobile());
		
		userDB.setUpdateUser(curUser.getId());
		userDB.setUpdateTime(new Timestamp(System.currentTimeMillis()));
		genericDao.update(userDB);
	}
	
	public User findUserById(Long userId) {
		StringBuffer buf = new StringBuffer("SELECT ");
		String cols = SQLUtils.getColumnsSQL(User.class, "U");
		buf.append(cols);
		buf.append(" FROM SYS_USER U WHERE U.ID = ? ");
		return genericDao.findT(User.class, buf.toString(), new Object[] { userId });
	}

	/**
	 * 获取当前登录用户所属组织信息
	 * @return	当前登录用户所属组织信息
	 */
	@Override
	public Orgnization findCurrentOrgnization() {
		User curUser = ContextUtil.getUser(User.class);
		if (null == curUser)
			throw new BusinessException("非法请求");
		
		StringBuffer buf = new StringBuffer("SELECT ");
		String cols = SQLUtils.getColumnsSQL(Orgnization.class, "O");
		buf.append(cols);
		buf.append(" , COALESCE(U.USERNAME, '') AS ALIAS ");
		buf.append(" FROM SYS_ORG O LEFT JOIN SYS_USER U ON U.ID = O.USER_ID WHERE 1=1 ");
		buf.append(" AND O.ID = ? ");
		
		Orgnization orgnization = genericDao.findT(Orgnization.class, buf.toString(), new Object[] { curUser.getOrgId() });
		if (null == orgnization)
			throw new BusinessException("当前用户所属组织不存在");
		
		if (null != orgnization.getParentId()) {// 存在父组织
			Orgnization parentOrg = findOrgnizationById(orgnization.getParentId());
			if (null != parentOrg)
				orgnization.setParentName(parentOrg.getName());// 父级组织名称
		}
		
		return orgnization;
	}
	
	public Orgnization findOrgnizationById(Long orgId) {
		StringBuffer buf = new StringBuffer("SELECT ");
		String cols = SQLUtils.getColumnsSQL(Orgnization.class, "O");
		buf.append(cols);
		buf.append(" FROM SYS_ORG O WHERE O.ID = ? ");
		return genericDao.findT(Orgnization.class, buf.toString(), new Object[] { orgId });
	}

	/**
	 * 修改当前登录用户所属组织信息
	 */
	@Override
	public void saveCurrentOrgnization(Orgnization orgnization) {
		User curUser = ContextUtil.getUser(User.class);
		if (null == curUser)
			throw new BusinessException("非法请求");
		
		Orgnization orgnizationDB = findOrgnizationById(curUser.getOrgId());
		if (!StringUtils.isEmpty(orgnization.getManager())) // 联系人姓名
			orgnizationDB.setManager(orgnization.getManager());
		
		if (!StringUtils.isEmpty(orgnization.getPhone())) // 联系电话
			orgnizationDB.setPhone(orgnization.getPhone());

		if (!StringUtils.isEmpty(orgnization.getAlias())) // 组织别名
			genericDao.execute("UPDATE SYS_USER SET USERNAME = ? WHERE ID = ?", orgnization.getAlias(), orgnizationDB.getUserId());
		
		orgnizationDB.setUpdateUser(curUser.getId());
		orgnizationDB.setUpdateTime(new Timestamp(System.currentTimeMillis()));
		genericDao.update(orgnizationDB);
	}
	
	/**
	 * 保存关联申请信息
	 * @param applyRelate 2：拒绝申请、3：同意申请
	 */
	@Override
	public void saveOrgRelation(Integer applyRelate) {
		User curUser = ContextUtil.getUser(User.class);
		if (null == curUser)
			throw new BusinessException("非法请求");
		
		if (null == applyRelate || (Commons.ORG_APPLY_RELATE_REFUSE != applyRelate && Commons.ORG_APPLY_RELATE_AGREE != applyRelate))
			throw new BusinessException("申请关联参数错误");
		
		Orgnization orgnizationDB = findOrgnizationById(curUser.getOrgId());// 当前登录用户所属组织
		
		orgnizationDB.setIsRelate(Commons.ORG_APPLY_RELATE_REFUSE == applyRelate ? Commons.ORG_IS_RELATE_FALSE : Commons.ORG_IS_RELATE_TRUE);// 父节点是否已关联数据
		orgnizationDB.setApplyRelate(applyRelate);// 父节点关联申请
		orgnizationDB.setUpdateUser(curUser.getId());
		orgnizationDB.setUpdateTime(new Timestamp(System.currentTimeMillis()));
		genericDao.update(orgnizationDB);
	}
	
	/**
	 * 分页条件查询机构信息
	 * @param page	分页信息
	 * @param orgnization	查询条件
	 * @param startDate 合作时间-起始时间
	 * @param endDate 合作时间-截止时间
	 * @return	机构集
	 */
	@Override
	public List<Orgnization> findOrgnizationList(Page page, Orgnization orgnization, Date startDate, Date endDate) {
		User curUser = ContextUtil.getUser(User.class);
		if (null == curUser)
			throw new BusinessException("非法请求");
		
		StringBuffer buf = new StringBuffer(" SELECT ");
		String cols = SQLUtils.getColumnsSQL(Orgnization.class, "SO");
		buf.append(cols);
		buf.append(" , U.USERNAME AS ALIAS ");
		buf.append(" FROM SYS_ORG SO ");
		buf.append(" LEFT JOIN SYS_USER U ON U.ID = SO.USER_ID ");
		buf.append(" WHERE 1=1 ");
		if (orgnization == null)
			orgnization = new Orgnization();
		List<Object> args = new ArrayList<Object>();
		
		buf.append(" AND SO.PARENT_ID = ? ");
		args.add(null == orgnization.getId() ? curUser.getOrgId() : orgnization.getId());
		
		if (orgnization.getName() != null) {
			buf.append(" AND").append(" (SO.NAME LIKE ? OR SO.NAME LIKE ?)");
			args.add("%" + ZHConverter.convert(orgnization.getName(), ZHConverter.TRADITIONAL) + "%");
			args.add("%" + ZHConverter.convert(orgnization.getName(), ZHConverter.SIMPLIFIED) + "%");
		}
		if (null != orgnization.getOrgType()) {// 组织类型
			buf.append(" AND SO.ORG_TYPE=? ");
			args.add(orgnization.getOrgType());
		}
		if (null != orgnization.getIsRelate()) {// 关联关系 
			buf.append(" AND SO.IS_RELATE=? ");
			args.add(orgnization.getIsRelate());
		}
		if (null != orgnization.getMode()) {// 合作方式
			buf.append(" AND SO.MODE=? ");
			args.add(orgnization.getMode());
		}
		if (startDate != null) {
			buf.append(" AND SO.SETTLED_TIME>=?");
			args.add(DateUtil.getStartDate(startDate));
		}
		if (endDate != null) {
			buf.append(" AND SO.SETTLED_TIME<=?");
			args.add(DateUtil.getEndDate(endDate));
		}

		buf.append(" AND SO.STATE = ? ");
		args.add(Commons.ORG_STATE_ENABLE);// 启用状态
		
		buf.append(" ORDER BY SO.SETTLED_TIME DESC ");
		return genericDao.findTs(Orgnization.class, page, buf.toString(), args.toArray());
	}
	
	/**
	 * 保存机构
	 * 
	 * 组织编码规则： 3~4位区号+6位年月日+5位序列号
	 * @param orgnization	需要保存的机构对象
	 */
	@Override
	public void saveOrgnization(Orgnization orgnization) {
		// 组织信息基础校验
		validOrgnization(orgnization);
		
		orgnization.setCode(makeOrgCode(orgnization.getAreaCode()));// 组织编码
		
		User curUser = ContextUtil.getUser(User.class);
		Timestamp curTime = new Timestamp(System.currentTimeMillis());
		
		orgnization.setParentId(orgnization.getParentId() == null ? curUser.getOrgId() : orgnization.getParentId());
		if (orgnization.getId() == null) {// 新增
			// 1. 新增组织信息
			Long companyId = findCompanyIdByOrgId(orgnization.getParentId());
			Long id = genericDao.findSingle(Long.class, "SELECT ID FROM SYS_ORG WHERE CODE=?", orgnization.getCode());
			if (id != null)
				throw new BusinessException("机构编码已经存在！");
			orgnization.setCompanyId(companyId == null ? 0L : companyId);
			orgnization.setCreateUser(curUser.getId());
			orgnization.setCreateTime(curTime);
			orgnization.setSettledTime(curTime);
			
			genericDao.save(orgnization);

			// 2. 以组织别名作为用户名创建该组织（管理者/经营者）的超级管理员账号(角色：超级管理员)
			User user = buildUser(orgnization);
			genericDao.save(user);
			
			// 3. 更新该组织的userId信息
			orgnization.setUserId(user.getId());
			genericDao.update(orgnization);
			
			// 4. 自动给该组织创建6种包含指定权限的角色及权限（超级管理员、管理员、运营、财务、客服、补货员）
			List<Role> roles = buildRoles(orgnization);
			for (Role role : roles) {
				genericDao.save(role);
				
				if ("超级管理员".equals(role.getName())) { // 绑定第2步创建的超级管理员用户的角色
					UserRole userRole = buildUserRole(user, role);
					genericDao.save(userRole);
				}
				
				String roleName = role.getName();
				if ("超级管理员".equals(roleName)) // 根据组织的类型来确定超级管理员角色继承【管理者】或【经营者】的所有权限
					roleName = Commons.ORG_TYPE_MANAGER == orgnization.getOrgType() ? Commons.ORG_TYPE_NAME_MANAGER : Commons.ORG_TYPE_NAME_OPERATOR;
				
				List<Authority> rightsList = findRightsList(1L, roleName);// 1L: 邦马特超级管理员组织ID（所有的角色权限统一继承自邦马特超级管理员的）
				for (Authority rights : rightsList) {
					rights.setId(null);
					rights.setRoleId(role.getId());
					genericDao.save(rights);
				}
			}
		} else {// 修改
			Orgnization orgnizationDB = findOrgnizationById(orgnization.getId());
			convertOrg(orgnizationDB, orgnization);
			orgnizationDB.setUpdateUser(curUser.getId());
			orgnizationDB.setUpdateTime(curTime);
			
			// 目前暂不允许更改组织类型
//			orgnizationDB.setOrgType(orgnization.getOrgType());// 组织类型
			
			// 从不关联变为关联，需要下级同意方可关联
			Integer isRelateDB = orgnizationDB.getIsRelate(); // 未修改之前的关联关系
			Integer isRelateInput = orgnization.getIsRelate(); // 前台输入的（想要修改的）关联关系
			if (Commons.ORG_IS_RELATE_FALSE == isRelateDB && Commons.ORG_IS_RELATE_TRUE == isRelateInput) {// 从不关联变为关联，需要下级同意方可关联
				orgnizationDB.setApplyRelate(Commons.ORG_APPLY_RELATE_ING);// 提出申请
				orgnizationDB.setApplyTime(curTime);
			} else {// 其他情况，直接修改关联关系
				orgnizationDB.setIsRelate(orgnization.getIsRelate());// 关联关系
			}
			
			// 组织别名若修改，则将改组织关联的管理员账户的username级联更改
			User userDB = findUserById(orgnizationDB.getUserId());
			String usernameDB = userDB.getUsername(); // 原组织别名（用户名）
			String alias = orgnizationDB.getAlias(); // 修改后的用户别名
			if (!usernameDB.equals(alias)) {// 不一致，更新用户信息
				userDB.setUsername(alias);
				userDB.setUpdateUser(curUser.getId());
				userDB.setUpdateTime(curTime);
				genericDao.update(userDB);
			}
			
			genericDao.update(orgnizationDB);
		}
	}
	
	/**
	 * 组织信息基础校验
	 * @param orgnization
	 */
	public void validOrgnization(Orgnization orgnization) {
		if (null == orgnization)
			throw new BusinessException("非法请求！");
		if (StringUtils.isEmpty(orgnization.getName()))
			throw new BusinessException("组织名称不允许为空！");
		if (StringUtils.isEmpty(orgnization.getAlias()))
			throw new BusinessException("组织别名不允许为空！");
		if (orgnization.getName().equals(orgnization.getAlias()))
			throw new BusinessException("组织名称和组织别名不可相同！");
		if (orgnization.getAlias().contains("_"))
			throw new BusinessException("组织别名不可以包含下划线");
		if (null == orgnization.getOrgType() || (Commons.ORG_TYPE_MANAGER != orgnization.getOrgType() && Commons.ORG_TYPE_OPERATOR != orgnization.getOrgType()))
			throw new BusinessException("组织类型有误！");
		if (null == orgnization.getMode() || (Commons.ORG_MODE_COOPERATION != orgnization.getMode() && Commons.ORG_MODE_JOINT_OPERATION != orgnization.getMode()
				&& Commons.ORG_MODE_JOINT != orgnization.getMode() && Commons.ORG_MODE_SELF_SUPPORT != orgnization.getMode()))
			throw new BusinessException("合作方式有误！");
		if (null == orgnization.getIsRelate() || (Commons.ORG_IS_RELATE_TRUE != orgnization.getIsRelate() && Commons.ORG_IS_RELATE_FALSE != orgnization.getIsRelate()))
			throw new BusinessException("管理关系有误！");
		if (null == orgnization.getSettledTime())
			throw new BusinessException("合作时间不允许为空！");
		if (StringUtils.isEmpty(orgnization.getProv()) || StringUtils.isEmpty(orgnization.getCity()) || StringUtils.isEmpty(orgnization.getDist()))
			throw new BusinessException("省市区均不允许为空！");
		if (StringUtils.isEmpty(orgnization.getManager()))
			throw new BusinessException("联系人不允许为空！");
		if (StringUtils.isEmpty(orgnization.getPhone()))
			throw new BusinessException("联系电话不允许为空！");
		if (StringUtils.isEmpty(orgnization.getAreaCode()))
			throw new BusinessException("区号不允许为空！");
	}
	
	/**
	 * 将前台要修改的组织信息赋值给数据库对应的当前指定组织
	 * @param orgnizationDB
	 * @param orgnization
	 */
	private void convertOrg(Orgnization orgnizationDB, Orgnization orgnization) {
		orgnizationDB.setName(orgnization.getName());
		orgnizationDB.setMode(orgnization.getMode());// 合作方式
		orgnizationDB.setSettledTime(orgnization.getSettledTime());// 合作时间
		orgnizationDB.setProv(orgnization.getProv());
		orgnizationDB.setCity(orgnization.getCity());
		orgnizationDB.setDist(orgnization.getDist());
		orgnizationDB.setManager(orgnization.getManager());
		orgnizationDB.setPhone(orgnization.getPhone());
		orgnizationDB.setAlias(orgnization.getAlias());
	}
	
	/**
	 * 创建组织时，自动创建该组织的超级管理员账号
	 * @param orgnization
	 * @return
	 */
	private User buildUser(Orgnization orgnization) {
		User user = new User();
		user.setUsername(orgnization.getAlias());// 用户名（组织别名）
		user.setPassword(ContextUtil.getPassword(dictionaryService.findDefaultPassword()));
		user.setNickname(orgnization.getAlias());
		user.setRealName(orgnization.getManager());
		user.setMobile(orgnization.getPhone());
		user.setOrgId(orgnization.getId());
		user.setCompanyId(orgnization.getCompanyId());
		user.setCreateUser(orgnization.getCreateUser());
		user.setCreateTime(orgnization.getCreateTime());
		user.initDefaultValue();
		
		if (Commons.isAdminName(user.getUsername()))
			throw new BusinessException("禁止使用系统内置用户名！");
		
		if (user.getUsername().contains("_"))
			throw new BusinessException("用户名称不可以包含下划线");
		
		String sql = "SELECT ID,USERNAME,NICKNAME,MOBILE,EMAIL FROM SYS_USER WHERE USERNAME=? OR MOBILE=?";
		User oldUser = genericDao.findT(User.class, sql, user.getUsername(), user.getMobile());
		if (oldUser != null) {
			if (oldUser.getUsername().equals(user.getUsername()))
				throw new BusinessException("用户名已经存在！");
			if (oldUser.getMobile() != null && oldUser.getMobile().equals(user.getMobile()))
				throw new BusinessException("手机号码已存在！");
		}
		
		return user;
	}
	
	/**
	 * 创建组织时，自动给该组织创建6种包含指定权限的角色（超级管理员、管理员、运营、财务、客服、补货员）
	 * @param orgnization
	 * @return
	 */
	private List<Role> buildRoles(Orgnization orgnization) {
		String[] roleNames = {"超级管理员", "管理员", "运营", "财务", "客服", "补货员"};
		Map<String, String> remarkMap = new HashMap<String, String>();
		remarkMap.put("超级管理员", "超级管理员具有平台全部功能使用权限。请谨慎配置。");
		remarkMap.put("管理员", "管理员具有平台全部功能使用权限。请谨慎配置。");
		remarkMap.put("运营", "运营能够进行日常经营活动，规划商品，查看经营情况。");
		remarkMap.put("财务", "财务能够进行平台的各项资金操作，以及下载查看平台的各项资金单据。");
		remarkMap.put("客服", "客服能够进行订单的查询，处理退款等日常经营中遇到的问题。");
		remarkMap.put("补货员", "补货员能够查看补货信息，设备库存信息等。");

		Map<String, Integer> typeMap = new HashMap<String, Integer>();
		typeMap.put("超级管理员", 3);
		typeMap.put("管理员", 4);
		typeMap.put("运营", 5);
		typeMap.put("财务", 6);
		typeMap.put("客服", 7);
		typeMap.put("补货员", 8);
		
		List<Role> roles = new ArrayList<Role>();
		
		User curUser = ContextUtil.getUser(User.class);
		Timestamp curTime = new Timestamp(System.currentTimeMillis());
		
		for (String roleName : roleNames) {
			Role role = new Role();
			role.setName(roleName);
			role.setEditable(true);
			role.setOrgId(orgnization.getId());
			role.setSysType(curUser.getSysType());
			role.setRemark(remarkMap.get(roleName));
			role.setType(typeMap.get(roleName));
			role.setCreateUser(curUser.getUsername());
			role.setCreateTime(curTime);
			roles.add(role);
		}
		return roles;
	}
	
	/**
	 * 创建用户角色信息
	 * @param user
	 * @param role
	 * @return
	 */
	private UserRole buildUserRole(User user, Role role) {
		UserRole userRole = new UserRole();
		userRole.setUserId(user.getId());
		userRole.setRoleId(role.getId());
		userRole.setCreateUser(role.getCreateUser());
		userRole.setCreateTime(role.getCreateTime());
		return userRole;
	}
	
	/**
	 * 获取指定组织、指定角色的所有权限
	 * @return
	 */
	private List<Authority> findRightsList(Long orgId, String roleName) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer(" SELECT ");
		String cols = SQLUtils.getColumnsSQL(Authority.class, "RI");
		buf.append(cols);
		buf.append(" FROM SYS_RIGHTS RI ");
		buf.append(" LEFT JOIN SYS_ROLE RO ON RO.ID = RI.ROLE_ID ");
		buf.append(" WHERE 1=1  ");
		buf.append(" AND RO.ORG_ID = ? ");
		args.add(orgId);
		buf.append(" AND RO.NAME = ? ");
		args.add(roleName);
		
		return genericDao.findTs(Authority.class, buf.toString(), args.toArray());
	}
	
	/**
	 * 根据机构ID查询机构所属公司ID
	 * @param id	机构ID
	 * @return	所属公司ID
	 */
	private Long findCompanyIdByOrgId(Long id) {
		return genericDao.findSingle(Long.class, "SELECT COMPANY_ID FROM SYS_ORG WHERE ID=?", id);
	}

	/**
	 * 查询当前机构数量
	 * @param id	机构ID
	 * @return	所属公司ID
	 */
	private Integer findOrgCount() {
		return genericDao.findSingle(Integer.class, "SELECT COUNT(1) FROM SYS_ORG");
	}
	
	/**
	 * 根据区号生成组织编码:  3~4位区号 + 6位的当前日期字符串 + 5位（或更多）的序列号
	 * 
	 * @param areaCode 3~4位区号
	 * @return
	 */
	public String makeOrgCode(String areaCode) {
		String dateStr = DateUtil.dateToDateString(new java.util.Date(), DateUtil.YYMMDD_EN);// 6位的当前日期字符串
		String orgSeq = makeOrgSeq(findOrgCount());// 5位（或更多）的序列号
		
		return areaCode + dateStr + orgSeq;
	}
	
	/**
	 * 生成组织编码后5位的序列号
	 * 
	 * 5位以内的数字，加1后，不够5位则前面补0；5位以上的加1后直接返回
	 * @param orgTotalCount 目前组织数量
	 * @return
	 */
	public String makeOrgSeq(int orgTotalCount) {
		String orgCode = "";
		
		orgTotalCount++;
		int length = ("" + orgTotalCount).length();
		
		if (length > 5)
			return "" + orgTotalCount;
		
		for (int i = 0; i < 5 - length; i++) // 补0
			orgCode += "0";
		
		return orgCode + orgTotalCount;
	}
	
	/**
	 * 删除机构
	 * @param ids	需要删除的机构ID集
	 */
	@Override
	public void deleteOrgnizations(Long... ids) {
		if (ids != null && ids.length != 0) {
			StringBuffer buf = new StringBuffer(" IN(");
			Set<Long> args = new HashSet<Long>();
			for (Long id : ids) {
				buf.append("?,");
				args.add(id);
			}
			buf.setLength(buf.length() - 1);
			buf.append(")");
			String inIds = buf.toString();
			
			buf.setLength(0);
			buf.append("SELECT COUNT(ID) FROM SYS_ORG WHERE STATE = 0 AND PARENT_ID").append(inIds);
			int count = genericDao.findSingle(int.class, buf.toString(), args.toArray());
			if (count != 0)
				throw new BusinessException("所选机构或其子机构存在附属机构！");
			
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
			
			// 删除权限信息
			buf.setLength(0);
			buf.append("DELETE FROM SYS_RIGHTS WHERE ROLE_ID IN (SELECT ID FROM SYS_ROLE WHERE ORG_ID").append(inIds).append(" )");
			genericDao.execute(buf.toString(), args.toArray());
			// 删除用户角色信息
			buf.setLength(0);
			buf.append("DELETE FROM SYS_USER_ROLE WHERE ROLE_ID IN (SELECT ID FROM SYS_ROLE WHERE ORG_ID").append(inIds).append(" )");
			genericDao.execute(buf.toString(), args.toArray());
			// 删除角色信息
			buf.setLength(0);
			buf.append("DELETE FROM SYS_ROLE WHERE ORG_ID").append(inIds);
			genericDao.execute(buf.toString(), args.toArray());
			// 删除用户信息
			buf.setLength(0);
			buf.append("DELETE FROM SYS_USER WHERE ORG_ID").append(inIds);
			genericDao.execute(buf.toString(), args.toArray());
			
			buf.setLength(0);
			List<Object> args2 = new ArrayList<Object>();
			buf.append("UPDATE SYS_ORG SET STATE = 1 WHERE ID").append(inIds);
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
	
	/**
	 * 建立/取消关联（管理）信息
	 * @param orgId 要建立关联的下级组织信息
	 * @param applyRelate 1：建立管理   2：取消管理
	 */
	@Override
	public void saveRelation(Long orgId, Integer applyRelate) {
		Orgnization orgnization = findOrgnizationById(orgId);
		if (null == orgnization)
			throw new BusinessException("非法请求！");
		
		User curUser = ContextUtil.getUser(User.class);
		
		Timestamp curTime = new Timestamp(System.currentTimeMillis());
		if (Commons.ORG_APPLY_RELATE_APPLY == applyRelate) { // 建立管理
			orgnization.setApplyRelate(Commons.ORG_APPLY_RELATE_ING);// 提出了申请
			orgnization.setApplyTime(curTime);
		} else if (Commons.ORG_APPLY_RELATE_CANCEL == applyRelate) { // 取消管理
			orgnization.setIsRelate(Commons.ORG_IS_RELATE_FALSE);
		}
		
		orgnization.setUpdateUser(curUser.getId());
		orgnization.setUpdateTime(curTime);
		genericDao.update(orgnization);
	}
	
	/**
	 * 查询机构的子机构（不递归，单级）
	 * @param parentId	父机构ID
	 * @return	子机构集
	 */
	@Override
	public List<Orgnization> findOrgnizationsByParentId(Long parentId) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer();
		buf.append(SQLUtils.getSelectSQL(Orgnization.class)).append(" WHERE 1=1 ");
		buf.append(" AND PARENT_ID = ? ");
		
		User curUser = ContextUtil.getUser(User.class);
		parentId = parentId == null ? curUser.getOrgId() : parentId;
		args.add(parentId);
		
		buf.append(" AND STATE = ? ");
		args.add(Commons.ORG_STATE_ENABLE);
		buf.append(" AND IS_RELATE = ? ");
		args.add(Commons.ORG_IS_RELATE_TRUE);
		buf.append(" ORDER BY SETTLED_TIME DESC ");
		return genericDao.findTs(Orgnization.class, buf.toString(), args.toArray());
	}
	
	/**
	 * 循环递归查询平级的组织信息
	 * @return	平级的组织集
	 */
	@Override
	public List<Orgnization> findCyclicPeersOrgnization(Long orgId) {
		List<Orgnization> treeOrgnizations = findCyclicOrgnizations(orgId);
		
		return CommonUtil.getOrgnizationsByCyclicOrgnization(treeOrgnizations);
	}
	
}
