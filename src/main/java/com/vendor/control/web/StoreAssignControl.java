package com.vendor.control.web;

import com.ecarry.core.web.control.BaseControl;
import com.ecarry.core.web.core.ContextUtil;
import com.ecarry.core.web.core.Page;
import com.vendor.po.PointPlace;
import com.vendor.po.User;
import com.vendor.service.IAccountService;
import com.vendor.service.IStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * Created by Chris on 2017/3/4.
 * 店铺分配控制器
 */
@Controller
@RequestMapping(value = "/store")
public class StoreAssignControl extends BaseControl {
    private final IAccountService m_accountService;
    private final IStoreService m_storeService;

    @Autowired
    public StoreAssignControl(IAccountService m_accountService, IStoreService m_storeService) {
        this.m_accountService = m_accountService;
        this.m_storeService = m_storeService;
    }

    /**
     * Forward point model and view.
     *
     * @return the model and view
     */
    @RequestMapping(value = "storeAssign/forward.do", method = RequestMethod.GET)
    public ModelAndView forwardPoint() {
        ModelAndView view = new ModelAndView("/store/storeAssign.jsp");
        User user = ContextUtil.getUser(User.class);
        view.addObject("_orgName", user.getOrgName());
        view.addObject("_orgId", user.getOrgId());
        return view;
    }

    /**
     * 查询本组织内已经分配了店铺的用户
     *
     * @param page the page
     * @return the list
     */
    @RequestMapping(value = "storeAssign/findAccountsAssignedStores.json", method = RequestMethod.POST)
    public List<User> findAccountsAssignedStores(Page page, String realName) {
        return m_accountService.findAccountsAssignedStores(page, realName);
    }

    /**
     * 查询某个用户已经分配的店铺
     *
     * @param userId the user id
     * @param page   the page
     * @return the list
     */
    @RequestMapping(value = "storeAssign/findStoresAssignedToAccount.json", method = RequestMethod.POST)
    public List<PointPlace> findStoresAssignedToAccount(Long userId, Page page) {
        return m_accountService.findStoresAssignedToAccount(userId, page);
    }

    /**
     * 为用户分配店铺
     *
     * @param userId 用户Id
     */
    @RequestMapping(value = "storeAssign/saveAssignStoresToAccount.json", method = RequestMethod.POST)
    public void assignStoresToAccount(Long userId, Long[] storeIds) {
        m_accountService.assignStoresToAccount(userId, storeIds);
    }

    /**
     * 解除店铺分配
     *
     * @param storeIds 店铺id集合
     */
    @RequestMapping(value = "storeAssign/saveReleaseStoresAssign.json", method = RequestMethod.POST)
    public void releaseStoresAssign(Long[] storeIds) {
        m_accountService.releaseStoresAssign(storeIds);
    }

    /**
     * 以角色为分组查询本组织未分配店铺的用户
     * @return 集合
     */
    @RequestMapping(value = "storeAssign/findAccountsNotAssignedStore.json", method = RequestMethod.POST)
    public List<com.vendor.po.Role> findAccountsNotAssignedStore() {
        return m_accountService.findAccountsNotAssignedStore();
    }

    /**
     * 查询本组织尚未分配的店铺
     *
     * @param page the page
     * @return the list
     */
    @RequestMapping(value = "storeAssign/findNotAssignedStore.json", method = RequestMethod.POST)
    public List<PointPlace> findNotAssignedStore(Page page) {
        return m_storeService.findNotAssignedStore(page);
    }
}
