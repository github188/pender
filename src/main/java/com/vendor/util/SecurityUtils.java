/**
 * 
 */
package com.vendor.util;

import com.ecarry.core.exception.BusinessException;
import com.ecarry.core.web.core.ContextUtil;
import com.vendor.po.User;

/**
 * @author dranson on 2014年9月24日
 */
public abstract class SecurityUtils {
	/**
	 * 检查是否需要系统数据权限
	 */
	public static void checkSystemAccess() {
		if (!Commons.isSystemUser())
			throw new BusinessException("非法请求！");
	}
	/**
	 * 检查是否需要系统数据权限
	 * @param companyId	公司ID
	 */
	public static void checkSystemAccess(Long companyId) {
		User curUser = ContextUtil.getUser(User.class);
		if ((companyId == null || curUser.getCompanyId().longValue() != companyId.longValue()) && !Commons.isSystemUser())
			throw new BusinessException("非法请求！");
	}
	/**
	 * 检查公司数据权限
	 * @param companyId	公司ID
	 */
	public static void checkCompanyData(Long companyId) {
		checkCompanyData(companyId, "非法请求！");
	}
	/**
	 * 检查公司数据权限
	 * @param companyId	公司ID
	 * @param message	返回的消息
	 */
	public static void checkCompanyData(Long companyId, String message) {
		if ((companyId ==null || ContextUtil.getUser(User.class).getCompanyId().longValue() != companyId.longValue()))
			throw new BusinessException(message);
	}
	/**
	 * 日期查询区间非空校验
	 * @author yuyuanyuan on 2014年10月17日
	 */
	public static void checkDateRange(String startDate, String endDate) {
		if (startDate == null || endDate == null)
			throw new BusinessException("开始日期与结束日期不允许为空！");
	}
}
