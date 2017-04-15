package com.vendor.service.impl.account;

import com.ecarry.core.dao.IGenericDao;
import com.ecarry.core.dao.SQLUtils;
import com.ecarry.core.domain.BoxValue;
import com.ecarry.core.exception.BusinessException;
import com.ecarry.core.util.ZHConverter;
import com.ecarry.core.web.core.ContextUtil;
import com.ecarry.core.web.core.Page;
import com.vendor.po.PointPlace;
import com.vendor.po.Role;
import com.vendor.po.User;
import com.vendor.service.IAccountService;
import com.vendor.service.IDictionaryService;
import com.vendor.service.ISystemService;
import com.vendor.util.Commons;
import com.vendor.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 账号服务实现类
 *
 * @author Chris
 * @since 2017/2/24
 */
@Service("accountService")
public class AccountService implements IAccountService {
    private final IGenericDao m_genericDao;
    private final IDictionaryService m_dictionaryService;
    private final ISystemService m_systemService;

    /*
     * (non-Javadoc)
     * @see IAccountService#addAccount(com.vendor.po.User)
     */
    @Override
    public List<com.vendor.po.Role> findAccountsNotAssignedStore() {
        User curUser = ContextUtil.getUser(User.class);
        //查询本组织还未分配店铺的用户集合
        List<User> users = m_genericDao.findTs(User.class, "SELECT  su.id,su.real_name,su.username,su.enable from sys_user su LEFT JOIN t_point_place tp ON tp.user_id=su.id WHERE tp.user_id is NULL AND su.org_id=" + curUser.getOrgId());
        if (users.size() != 0) {
            //查询未分配店铺用户所属的角色集合
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM sys_role WHERE id IN (SELECT role_id FROM sys_user_role WHERE user_id")
                .append(" IN (");
            for (User user : users) {
                sb.append(user.getId()).append(",");
            }
            sb.setLength(sb.length() - 1);
            sb.append(")) AND sys_role.org_id=").append(curUser.getOrgId());
            List<Role> roles = m_genericDao.findTs(Role.class, sb.toString());
            sb.setLength(0);
            sb.append("SELECT su.id,su.username,su.real_name,su.enable,su.mobile,sr.role_id AS roleId FROM sys_user su LEFT JOIN sys_user_role sr ON sr.user_id=su.id WHERE su.id IN (");
            for (User user : users) {
                sb.append(user.getId()).append(",");
            }
            sb.setLength(sb.length() - 1);
            sb.append(")");
            List<User> userList = m_genericDao.findTs(User.class, sb.toString());
            List<User> list;
            for (Role role:roles) {
                list = new ArrayList<>();
                for (User user: userList) {
                    if (role.getId().equals(user.getRoleId())) {
                        list.add(user);
                    }
                }
                role.setUsers(list);
            }
            return roles;
        }
        return null;
    }

    @Autowired
    public AccountService(IGenericDao genericDao, IDictionaryService dictionaryService, ISystemService systemService) {
        this.m_genericDao = genericDao;
        this.m_dictionaryService = dictionaryService;
        this.m_systemService = systemService;
    }

    /*
     * (non-Javadoc)
     * @see IAccountService#addAccount(com.vendor.po.User)
     */
    @Override
    public void addAccount(User user) {
        user.initDefaultValue();
        this.validCommonAccount(user);
        this.validAddAccount(user);
        User curUser = ContextUtil.getUser(User.class);
        user.setCompanyId(curUser.getCompanyId());
        user.setOrgId(curUser.getOrgId());
        Timestamp curTime = new Timestamp(System.currentTimeMillis());
        user.setCreateTime(curTime);
        user.setPassword(ContextUtil.getPassword(m_dictionaryService.findDefaultPassword()));
        user.setCreateUser(curUser.getId());
        user.setEnable(Commons.USER_ENABLE_TRUE);
        m_genericDao.save(user);
    }

    /**
     * 新增账户验证账户有效性
     *
     * @param user user对象
     */
    private void validAddAccount(User user) {
        String sql = "SELECT ID,USERNAME,MOBILE FROM SYS_USER WHERE USERNAME=? OR MOBILE=?";
        User dbUser = m_genericDao.findT(User.class, sql, user.getUsername(), user.getMobile());
        if (dbUser != null) {
            if (user.getUsername().equals(dbUser.getUsername())) {
                throw new BusinessException("用户名已经存在！");
            }
            if (user.getMobile().equals(dbUser.getMobile())) {
                throw new BusinessException("手机号码已存在！");
            }
        }

    }

    /**
     * 账号通用验证
     *
     * @param user user对象
     */
    private void validCommonAccount(User user) {
        if (StringUtil.isEmpty(user.getUsername())) {
            throw new BusinessException("用户名必需填写！");
        }
        if (StringUtil.isEmpty(user.getMobile())) {
            throw new BusinessException("手机号必需填写！");
        }
        if (StringUtil.isEmpty(user.getRealName())) {
            throw new BusinessException("姓名必需填写！");
        }
        if (Commons.isAdminName(user.getUsername()))
            throw new BusinessException("禁止使用系统内置用户名！");
        if (user.getUsername().contains("_"))
            throw new BusinessException("用户名称不可以包含下划线");
    }

    private void validUpdateAccount(User user, User dbUser) {
        if (dbUser == null) {
            throw new BusinessException("找不到该用户！");
        }
        Long id;
        if (!StringUtil.isEmpty(user.getUsername())) {
            id = m_genericDao.findSingle(Long.class, "SELECT COALESCE(ID, 0) FROM SYS_USER WHERE USERNAME=? AND ID!=?", user.getUsername(), dbUser.getId());
            if (id != null && id != 0)
                throw new BusinessException("用户名已经存在！");
        }
        if (!StringUtil.isEmpty(user.getMobile())) {
            id = m_genericDao.findSingle(Long.class, "SELECT COALESCE(ID,0) FROM SYS_USER WHERE MOBILE=? AND ID!=?", user.getMobile(), dbUser.getId());
            if (id != null && id != 0)
                throw new BusinessException("手机号码已存在！");
        }
        if (!dbUser.getEditable())
            throw new BusinessException("禁止修改系统内置用户！");
    }


    /**
     * @see IAccountService#updateAccount(com.vendor.po.User)
     */
    @Override
    public void updateAccount(User user) {
        String sql = "SELECT * FROM SYS_USER WHERE ID=?";
        User dbUser = m_genericDao.findT(User.class, sql, user.getId());
        this.validUpdateAccount(user, dbUser);
        this.validCommonAccount(dbUser);
        if (!StringUtil.isEmpty(user.getUsername())) {
            dbUser.setUsername(user.getUsername());
        }
        if (!StringUtil.isEmpty(user.getMobile())) {
            dbUser.setMobile(user.getMobile());
        }
        if (!StringUtil.isEmpty(user.getRealName())) {
            dbUser.setRealName(user.getRealName());
        }
        User curUser = ContextUtil.getUser(User.class);
        dbUser.setUpdateUser(curUser.getId());
        Timestamp curTime = new Timestamp(System.currentTimeMillis());
        dbUser.setUpdateTime(curTime);
        BoxValue<String, Object[]> box = SQLUtils.getUpdateSQLByExclude(dbUser, "password,companyId,editable,createUser,createTime,lastLoginMachine,lastLoginTime,pwdUpdateTime");
        m_genericDao.execute(box.getKey(), box.getValue());
    }

    /**
     * @see IAccountService#deleteAccounts(Long[])
     */
    @Override
    public void deleteAccounts(Long[] ids) {
        this.m_systemService.deleteUsers(ids);
    }

    /**
     * @see IAccountService#saveAccountsEnable(Long[])
     */
    @Override
    public void saveAccountsEnable(Long[] ids) {
        StringBuilder buf = this.assembleEnableOrDisableSql(ids, true);
        m_genericDao.execute(buf.toString(), (Object[]) ids);
    }

    private StringBuilder assembleEnableOrDisableSql(Long[] ids, boolean enable) {
        StringBuilder buf = new StringBuilder(" IN(");
        for (Long ignored : ids) {
            buf.append("?,");
        }
        buf.setLength(buf.length() - 1);
        buf.append(")");
        String inIds = buf.toString();
        buf.setLength(0);
        buf.append("UPDATE  SYS_USER SET ENABLE=").append(enable ? "TRUE" : "FALSE").append(" WHERE ID").append(inIds);
        return buf;
    }

    /**
     * @see IAccountService#saveAccountsDisable(Long[])
     */
    @Override
    public void saveAccountsDisable(Long[] ids) {
        this.validAccountDisable(ids);
        StringBuilder buf = this.assembleEnableOrDisableSql(ids, false);
        m_genericDao.execute(buf.toString(), (Object[]) ids);

    }

    /**
     * @see IAccountService#findAccountsAssignedStores(Page, String)
     */
    @Override
    public List<User> findAccountsAssignedStores(Page page, String realName) {
        StringBuilder bd = new StringBuilder();
        bd.append("SELECT su.mobile,su.id,su.real_name,su.username,su.enable ")
            .append("FROM sys_user su LEFT JOIN t_point_place tp ON tp.user_id = su.id ");
        String condition = " tp.user_id IS NOT NULL AND su.org_id = ? GROUP BY su.id";
        List<Object> args = new ArrayList<>();
        if (StringUtil.isEmpty(realName)) {
            bd.append(" WHERE ").append(condition);
        } else {
            bd.append("WHERE (su.real_name LIKE ? OR su.real_name LIKE ?) AND ").append(condition);
            args.add("%" + ZHConverter.convert(realName, ZHConverter.TRADITIONAL) + "%");
            args.add("%" + ZHConverter.convert(realName, ZHConverter.SIMPLIFIED) + "%");
        }
        User curUser = ContextUtil.getUser(User.class);
        args.add(curUser.getOrgId());
        return m_genericDao.findTs(User.class, page, bd.toString(), args.toArray());
    }

    /**
     * @see IAccountService#findStoresAssignedToAccount(Long, Page)
     */
    @Override
    public List<PointPlace> findStoresAssignedToAccount(Long userId, Page page) {
        User curUser = ContextUtil.getUser(User.class);
        String sql = "SELECT org_id FROM sys_user WHERE id=" + userId;
        Long orgId = m_genericDao.findSingle(Long.class, sql);
        if (orgId == null) {
            throw new BusinessException("对不起，没有该用户！");
        } else if (!curUser.getOrgId().equals(orgId)) {
            throw new BusinessException("该用户不属于您所在组织！");
        }
        sql = "SELECT id,point_no,point_address, point_type,point_name FROM  t_point_place WHERE user_id=" + userId;
        return m_genericDao.findTs(PointPlace.class, page, sql);
    }

    /**
     * @see IAccountService#findAccountsByRoleAndRealName(Long, String, Page)
     */
    @Override
    public List<User> findAccountsByRoleAndRealName(Long roleId, String realName, Page page) {
        StringBuilder buf = new StringBuilder();
        buf.append("SELECT ").append(SQLUtils.getColumnsSQL(User.class, "su", "password"))
            .append(" FROM sys_user su LEFT JOIN sys_user_role sr ON su.id=sr.user_id WHERE role_id=?  AND su.org_id=? ");
        List<Object> args = new ArrayList<>();
        args.add(roleId);
        User curUser = ContextUtil.getUser(User.class);
        args.add(curUser.getOrgId());
        if (!StringUtil.isEmpty(StringUtil.trim(realName))) {
            realName = StringUtil.trim(realName);
            buf.append(" AND (su.real_name like ? OR su.real_name like ?)");
            args.add("%" + ZHConverter.convert(realName, ZHConverter.TRADITIONAL) + "%");
            args.add("%" + ZHConverter.convert(realName, ZHConverter.SIMPLIFIED) + "%");
        }
        List<User> users = m_genericDao.findTs(User.class, page, buf.toString(), args.toArray());
        //把给账号分配的角色的角色名设置到User的bound属性(bound属性的临时用法)
        if (users.size() != 0) {
            buf.setLength(0);
            args.clear();
            buf.append("SELECT su.user_id,sr.id from sys_user_role su JOIN sys_role sr on su.role_id=sr.id WHERE  user_id in (");
            for (User user : users) {
                buf.append(user.getId()).append(",");
            }
            buf.setLength(buf.length() - 1);
            buf.append(")");
            @SuppressWarnings("deprecation") List<Map<String, Object>> mapList = m_genericDao.findListMap(buf.toString());
            List<String> list;
            for (User user : users) {
                list = new ArrayList<>();
                for (Map<String, Object> map : mapList) {
                    if (map.get("user_id").equals(user.getId())) {
                        list.add(String.valueOf(map.get("id")));
                    }
                }
                if (list.size() != 0) {
                    user.setBound(list);
                }
            }
        }
        return users;
    }

    /**
     * @see IAccountService#releaseStoresAssign(Long[])
     */
    @Override
    public void releaseStoresAssign(Long[] storeIds) {
        StringBuffer sb = new StringBuffer("UPDATE t_point_place SET user_id=NULL WHERE id IN (");
        for (Long storeId : storeIds) {
            sb.append(storeId).append(",");
        }
        sb.setLength(sb.length() - 1);
        Long orgId = ContextUtil.getUser(User.class).getOrgId();
        sb.append(") AND org_id=").append(orgId);
        m_genericDao.execute(sb.toString());
    }

    /**
     * @see IAccountService#assignStoresToAccount(Long, Long[])
     */
    @Override
    public void assignStoresToAccount(Long userId, Long[] storeIds) {
        //检查当前登录的用户和被分配店铺的用户是否在同一个组织且存在被分配店铺的用户
        User curUser = ContextUtil.getUser(User.class);
        String sql = "SELECT org_id FROM sys_user WHERE id=" + userId;
        Long orgId = m_genericDao.findSingle(Long.class, sql);
        if (orgId == null) {
            throw new BusinessException("用户不存在！");
        } else if (!orgId.equals(curUser.getOrgId())) {
            throw new BusinessException("您不能为非本组织的用户分配店铺！");
        }

        //检查分配给用户的店铺是否还未分配
        StringBuilder sb = new StringBuilder("SELECT id,user_id FROM t_point_place WHERE id IN (");
        for (Long storeId : storeIds) {
            sb.append(storeId).append(",");
        }
        sb.setLength(sb.length() - 1);
        sb.append(") AND org_id=").append(orgId);
        List<PointPlace> stores = m_genericDao.findTs(PointPlace.class, sb.toString());
        if (stores.size() != 0) {
            for (PointPlace store : stores) {
                if (store.getUserId() != null) {
                    throw new BusinessException("您不能分配已经被分配的店铺！");
                }
            }

            //分配店铺
            sb.setLength(0);
            sb.append("UPDATE t_point_place set user_id=").append(userId).append(" WHERE id IN (");
            for (PointPlace store : stores) {
                sb.append(store.getId()).append(",");
            }
            sb.setLength(sb.length() - 1);
            sb.append(")");
            m_genericDao.execute(sb.toString());
        }
    }

    private void validAccountDisable(Long[] ids) {
        if (ids != null && ids.length != 0) {
            StringBuilder buf = new StringBuilder(" IN(");
            Long curUserId = ContextUtil.getUser(User.class).getId();
            for (Long id : ids) {
                if (curUserId.equals(id))
                    throw new BusinessException("不能禁用当前登录用户！");
                buf.append("?,");
            }
            buf.setLength(buf.length() - 1);
            buf.append(")");
            String inIds = buf.toString();
            buf.setLength(0);
            buf.append("SELECT COUNT(ID) FROM SYS_USER WHERE EDITABLE=false AND ID").append(inIds);
            int count = m_genericDao.findSingle(int.class, buf.toString(), (Object[]) ids);
            if (count != 0)
                throw new BusinessException("禁止禁用系统内置用户！");
        } else {
            throw new BusinessException("请选择需要禁用的用户！");
        }
    }

}
