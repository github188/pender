package com.vendor.service;

import java.util.List;

import com.ecarry.core.web.core.Page;
import com.vendor.po.ProductRecommend;

/**
 * 关联推荐
 * @author liujia on 2016年12月12日
 */
public interface IRecommendService {

	/**
	 * 查看关联推荐列表信息
	 * 
	 * @param productRecommend 关联推荐信息
	 * @param page 分页参数
	 * @return 关联推荐列表信息
	 */
	List<ProductRecommend> findProductRecommends(ProductRecommend productRecommend, Page page);

	/**
	 * 更新当前关联度
	 * @param productRecommend
	 */
	void saveRecommendRelevancy(ProductRecommend productRecommend);

}
