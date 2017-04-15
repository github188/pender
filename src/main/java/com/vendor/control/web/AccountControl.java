package com.vendor.control.web;

import com.ecarry.core.web.control.BaseControl;
import com.ecarry.core.web.core.Page;
import com.vendor.po.User;
import com.vendor.service.IAccountService;
import com.vendor.service.ISystemService;
import com.vendor.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * Created by Chris on 2017/2/24.
 * 账号Control
 */
@Controller
@RequestMapping(value = {"/orgnization", "/account"})
public class AccountControl extends BaseControl {

    private final IAccountService m_accountService;

    private final ISystemService m_systemService;

    @Autowired
    public AccountControl(IAccountService accountService, ISystemService systemService) {
        this.m_accountService = accountService;
        m_systemService = systemService;
    }

    /**
     * 新增账号并设置账号角色.
     *
     * @param user    the user
     * @param roleIds 角色ID数组
     */
    @RequestMapping(value = "account/addAccountAndSetRoles.json", method = RequestMethod.POST)
    public void addAccountAndSetRoles(User user, Long[] roleIds) {
        m_accountService.addAccount(user);
        this.updateAccountRole(new Long[]{user.getId()}, roleIds);
    }

    private void updateAccountRole(Long[] userIds, Long[] roleIds) {
        m_systemService.saveUserRoles(userIds, roleIds);
    }

    /**
     * 修改账号和账号的角色.
     *
     * @param user    the user
     * @param roleIds 角色ID数组
     */
    @RequestMapping(value = "account/updateAccountAndRoles.json", method = RequestMethod.POST)
    public void updateAccountAndRoles(User user, Long[] roleIds) {
        if (!StringUtil.isEmpty(user.getUsername()) ||
                !StringUtil.isEmpty(user.getRealName()) ||
                !StringUtil.isEmpty(user.getMobile())) {
            m_accountService.updateAccount(user);
        }
        if (roleIds!=null && roleIds.length != 0) {
            this.updateAccountRole(new Long[]{user.getId()}, roleIds);
        }
    }

    /**
     * 启用一个或者多个账号
     *
     * @param ids 账号ID数组
     */
    @RequestMapping(value = "account/saveAccountsEnable.json", method = RequestMethod.POST)
    public void saveAccountsEnable(Long[] ids) {
        m_accountService.saveAccountsEnable(ids);
    }

    /**
     * 禁用一个或者多个账号
     *
     * @param ids 账号ID数组
     */
    @RequestMapping(value = "account/saveAccountsDisable.json", method = RequestMethod.POST)
    public void saveAccountsDisable(Long[] ids) {
           m_accountService.saveAccountsDisable(ids);
    }

    /**
     * 删除一个或者多个账号.
     *
     * @param ids 账号ID数组
     */
    @RequestMapping(value = "account/deleteAccounts.json", method = RequestMethod.POST)
    public void deleteAccounts(Long[] ids) {
        m_accountService.deleteAccounts(ids);
    }

    /**
     * 根据角色和姓名查找账号
     * @param roleId 角色id   
     * @param realName   姓名
     * @param page     分页对象
     * @return   用户集合
     */
    @RequestMapping(value = "account/findAccountsByRoleAndRealName.json", method = RequestMethod.POST)
    public List<User> findAccountsByRoleAndRealName(Long roleId, String realName, Page page) {
        return m_accountService.findAccountsByRoleAndRealName(roleId, realName, page);
    }

    /**
     * 重置账号密码
     * @param userId 账号id
     */
    @RequestMapping(value = "account/saveResetAccountPassword.json", method = RequestMethod.POST)
    public void resetAccountPassword(Long userId) {
        m_systemService.saveResetPassword(userId);
    }
}
