package com.vendor.service.impl;

import com.ecarry.core.dao.IGenericDao;
import com.ecarry.core.web.core.ContextUtil;
import com.ecarry.core.web.core.Page;
import com.vendor.po.PointPlace;
import com.vendor.po.User;
import com.vendor.service.IStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Chris on 2017/3/13.
 * 店铺服务实现类
 */
@Service("storeService")
public class StoreService implements IStoreService {
    private final IGenericDao m_genericDao;

    @Autowired
    public StoreService(IGenericDao m_genericDao) {
        this.m_genericDao = m_genericDao;
    }

    /**
     * @see IStoreService#findNotAssignedStore(Page)
     */
    @Override
    public List<PointPlace> findNotAssignedStore(Page page) {
        User curUser = ContextUtil.getUser(User.class);
        String sql = "SELECT * FROM t_point_place WHERE user_id IS null AND org_id="+curUser.getOrgId();
        return m_genericDao.findTs(PointPlace.class, page, sql);
    }

}
