package com.vendor.service.impl;

import com.ecarry.core.dao.IGenericDao;
import com.ecarry.core.dao.SQLUtils;
import com.ecarry.core.domain.Authority;
import com.ecarry.core.exception.BusinessException;
import com.ecarry.core.web.core.ContextUtil;
import com.vendor.po.Role;
import com.vendor.po.User;
import com.vendor.service.IRoleService;
import com.vendor.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Chris on 2017/2/28.
 * 角色服务实现类
 */
@Service("roleService")
public class RoleService implements IRoleService {
    private final IGenericDao m_genericDao;

    @Autowired
    public RoleService(IGenericDao genericDao) {
        this.m_genericDao = genericDao;
    }

    /**
     * @see IRoleService#findRoles()
     */
    @Override
    public List<Role> findRoles() {
        User curUser = ContextUtil.getUser(User.class);
        StringBuffer sb = new StringBuffer("SELECT * FROM sys_role WHERE SYS_TYPE=? AND ORG_ID=? ");
        List<Object> args = new ArrayList<>();
        args.add(curUser.getSysType());
        args.add(curUser.getOrgId());
        if (!curUser.getUsername().equals("admin")) {
            sb.append(" AND type NOT IN (?,?,?) ");
            args.add(1);
            args.add(2);
            args.add(3);
        }
        List<Role> roles = m_genericDao.findTs(Role.class, sb.toString(), args.toArray());
        //添加用户数统计
        if (roles.size() != 0) {
            sb.setLength(0);
            sb.append("SELECT role_id AS id,count(user_id) AS userCount FROM sys_user_role sr LEFT JOIN sys_user su ON su.id = sr.user_id" );
            sb.append(" WHERE org_id = ").append(curUser.getOrgId())
                .append(" AND sr.role_id IN (");
            for (Role role : roles) {
                sb.append(role.getId()).append(",");
            }
            sb.setLength(sb.length() - 1);
            sb.append(") GROUP BY role_id");
            List<Role> rolesCount = m_genericDao.findTs(Role.class, sb.toString());
            for (Role role : roles) {
                role.setUserCount(0L);
                for (Role roleCount: rolesCount) {
                    if (roleCount.getId().equals(role.getId())) {
                        role.setUserCount(roleCount.getUserCount());
                    }
                }
            }
        }
        return roles;
    }

    /**
     * @see IRoleService#saveRole(Role)
     */
    @Override
    public void saveRole(Role role) {
        User curUser = ContextUtil.getUser(User.class);
        Timestamp curTime = new Timestamp(System.currentTimeMillis());
//        SecurityUtils.checkCompanyData(role.getOrgId());
        role.setSysType(curUser.getSysType());

        if (role.getId() == null) {
            if (role.getType() == null) {
                role.setType(0);
            }
            if (role.getEditable() == null)
                role.setEditable(true);
            role.setOrgId(curUser.getOrgId());
            role.setCreateUser(curUser.getUsername());
            role.setCreateTime(curTime);
            m_genericDao.save(role);
        } else {
            Role roleFindInDb = m_genericDao.findTById(Role.class, role.getId());
            if (roleFindInDb.getType() == null) {
                roleFindInDb.setType(0);
            }
            String name = role.getName();
            String remark = role.getRemark();
            if (name != null && !"".equals(name.trim())) {
                roleFindInDb.setName(name);
            }
            if (remark != null && !"".equals(remark)) {
                roleFindInDb.setRemark(remark);
            }
            boolean editable = roleFindInDb.getEditable();
            if (!editable)
                throw new BusinessException("禁止编辑内置系统角色！");
            roleFindInDb.setUpdateUser(curUser.getUsername());
            roleFindInDb.setUpdateTime(curTime);
            m_genericDao.update(roleFindInDb);
        }
    }

    /**
     * @see IRoleService#saveRights(List)
     */
    @Override
    public void saveRights(List<Authority> authorities) {

    }
}
