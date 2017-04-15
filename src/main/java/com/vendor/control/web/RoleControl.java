package com.vendor.control.web;

import com.ecarry.core.domain.Authority;
import com.ecarry.core.web.control.BaseControl;
import com.ecarry.core.web.core.ContextUtil;
import com.ecarry.core.web.view.JasperWordView;
import com.vendor.po.Role;
import com.vendor.po.User;
import com.vendor.service.IRoleService;
import com.vendor.service.ISystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * Created by Chris on 2017/2/23.
 * 角色Control类
 */
@Controller
@RequestMapping(value = {"/orgnization", "/role"})
public class RoleControl extends BaseControl {
    private final ISystemService m_systemService;
    private final IRoleService m_roleService;

    @Autowired
    public RoleControl(ISystemService systemService, IRoleService roleService) {
        this.m_systemService = systemService;
        this.m_roleService = roleService;
    }

    /**
     * 新增或编辑并保存角色权限
     * 如果角色id为空则为编辑，否则为新增
     * @param role        the role
     * @param authorities the 角色权限集
     * @return the role
     */
    @RequestMapping(value = "account/saveRoleAndRights.json", method = RequestMethod.POST)
    public Role saveRoleAndRights(Role role, List<Authority> authorities) {
        User curUser = ContextUtil.getUser(User.class);
        role.setOrgId(curUser.getOrgId());
        m_roleService.saveRole(role);
        if (authorities.size()!=0) {//如果角色权限集不为空则保存角色权限
            for (Authority authority : authorities) {
                authority.setRoleId(role.getId());
            }
            m_systemService.saveRights(authorities);
        }
        return role;
    }

    /**
     * 删除一个或者多个角色.
     *
     * @param ids 角色ID数组
     */
    @RequestMapping(value = "account/deleteRoles.json", method = RequestMethod.POST)
    public void deleteRoles(Long[] ids) {
        m_systemService.deleteRoles(ids);
    }

    /**
     * 查询当前登录用户所属组织的所有角色
     *
     * @return the list
     */
    @RequestMapping(value = "account/findRoles.json", method = RequestMethod.POST)
    @ModelAttribute("rows")
    public List<Role> findRoles() {
        return m_roleService.findRoles();
    }

    /**
     * 查询角色权限
     *
     * @param roleId   角色id
     * @return 角色权限集合
     */
    @RequestMapping(value = "account/findRights.json", method = RequestMethod.POST)
    @ModelAttribute("rows")
    public List<Authority> findRights(Long roleId) {
        return m_systemService.findRights(roleId);
    }
}
