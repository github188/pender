package com.vendor.service;

import com.ecarry.core.web.core.Page;
import com.vendor.po.PointPlace;
import com.vendor.po.User;

import java.util.List;

/**
 * Created by Chris on 2017/2/24.
 * 账号Service
 */
public interface IAccountService {
    /**
     * 新增一个账号
     *
     * @param user user对象
     */
    void addAccount(User user);

    /**
     * 更新一个账号
     *
     * @param user user对象
     */
    void updateAccount(User user);

    /**
     * 删除一个或者多个账号
     *
     * @param ids 账号id数组
     */
    void deleteAccounts(Long[] ids);

    /**
     * 启用一个或者多个账号
     *
     * @param ids 账号ID数组
     */
    void saveAccountsEnable(Long[] ids);

    /**
     * 禁用一个或者多个账号
     *
     * @param ids 账号ID数组
     */
    void saveAccountsDisable(Long[] ids);

    /**
     * 根据角色和姓名查找账号
     *
     * @param roleId   角色id
     * @param realName 姓名
     * @param page     分页对象
     * @return 用户集合
     */
    List<User> findAccountsByRoleAndRealName(Long roleId, String realName, Page page);

    /**
     * 查询本组织内已经分配了店铺的用户
     *
     * @param page     分页对象
     * @param realName 姓名
     * @return 用户集合
     */

    List<User> findAccountsAssignedStores(Page page, String realName);

    /**
     * 查询某个用户已经分配的店铺
     *
     * @param userId 用户id
     * @param page   分院对象
     * @return 店铺集合
     */
    List<PointPlace> findStoresAssignedToAccount(Long userId, Page page);

    /**
     * 以角色为分组查询本组织未分配店铺的用户
     *
     * @return 集合
     */
    List<com.vendor.po.Role> findAccountsNotAssignedStore();

    /**
     * 为用户分配店铺
     *
     * @param userId   用户id
     * @param storeIds 店铺id集合
     */
    void assignStoresToAccount(Long userId, Long[] storeIds);

    /**
     * 解除店铺分配
     *
     * @param storeIds 店铺id集合
     */
    void releaseStoresAssign(Long[] storeIds);
}
