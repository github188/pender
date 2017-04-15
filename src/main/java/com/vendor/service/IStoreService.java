package com.vendor.service;

import com.ecarry.core.web.core.Page;
import com.vendor.po.PointPlace;

import java.util.List;

/**
 * Created by Chris on 2017/3/13.
 * 店铺服务类
 */
public interface IStoreService {
    /**
     * 查询本组织尚未分配的店铺
     * @param page  分页对象
     * @return 店铺集合
     */
    List<PointPlace> findNotAssignedStore(Page page);
}
