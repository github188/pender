package com.vendor.service;

import com.ecarry.core.domain.Authority;
import com.vendor.po.Role;

import java.util.List;

/**
 * Created by Chris on 2017/2/28.
 * 角色服务接口
 */
public interface IRoleService {
    /**
     * 查询当前登录用户所属组织的所有角色
     * @return 角色列表
     */
    List<Role> findRoles();

    /**
     * 保存角色
     * @param role	需要保存的角色对象
     */
    void saveRole(Role role);

    /**
     * 修改角色权限
     *
     * @param authorities 角色权限集
     */
    void saveRights(List<Authority> authorities);
}
