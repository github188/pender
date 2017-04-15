/**
 * 
 */
package com.vendor.service.impl.login;

import java.io.File;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ecarry.core.dao.IGenericDao;
import com.ecarry.core.dao.SQLUtils;
import com.ecarry.core.domain.Menu;
import com.ecarry.core.exception.BusinessException;
import com.ecarry.core.util.MathUtil;
import com.ecarry.core.web.core.ContextUtil;
import com.ecarry.core.web.core.Page;
import com.vendor.po.Card;
import com.vendor.po.Device;
import com.vendor.po.DeviceLog;
import com.vendor.po.Order;
import com.vendor.po.Orgnization;
import com.vendor.po.ProductLog;
import com.vendor.po.User;
import com.vendor.service.ILoginService;
import com.vendor.service.IOrgnizationService;
import com.vendor.service.impl.TranxServiceImpl;
import com.vendor.thirdparty.tl.TranxCon;
import com.vendor.util.CommonUtil;
import com.vendor.util.Commons;
import com.vendor.util.DateUtil;
import com.vendor.util.XmlTools;
import com.vendor.vo.web.BestSellerLists;

/**
 * @author dranson 2011-3-22
 */
@Service("loginService")
public class LoginService implements ILoginService {
	
	@Value("${system.user}")
	private String admin;

	/**
	 * 通联单笔提现url
	 */
	@Value("${tl.tran.url}")
	private String tranURL;
	
	/**
	 * 通联商户号
	 */
	@Value("${tl.merchantid}")
	private String merchantId;

	/**
	 * 通联用户登录密码
	 */
	@Value("${tl.password}")
	private String tlPassword;
	
	@Autowired
	private IGenericDao genericDao;
	
	@Autowired
	private IOrgnizationService orgnizationService;

	@Override
	public List<Menu> findAuthorities() {
		List<Object> args = new ArrayList<Object>();
		args.add(true);
		StringBuffer buf = new StringBuffer();
		buf.append("SELECT ID,PARENT_ID,NAME,URL FROM SYS_MENU WHERE ENABLE=?");
		User user = ContextUtil.getUser(User.class);
		if (!user.getUsername().equals(admin)) {// 超级管理员用户无需不受权限控制
			buf.append(" AND (URL IS NULL OR URL = '' OR ID IN(SELECT MENU_ID FROM SYS_RIGHTS WHERE ROLE_ID IN(");
			buf.append("SELECT ROLE_ID FROM SYS_USER_ROLE WHERE USER_ID=?)))");
			args.add(user.getId());
		}
		buf.append(" ORDER BY SORT");
		return genericDao.findTs(Menu.class, buf.toString(), args.toArray());
	}

	/*
	 * (non-Javadoc)
	 * @see com.uc56.core.report.service.ILoginService#saveLoginLog()
	 */
	@Override
	public String saveLoginLog() {
		Timestamp curTime = new Timestamp(System.currentTimeMillis());
		String sql = "UPDATE SYS_USER SET LAST_LOGIN_TIME=? WHERE ID=?";
		genericDao.execute(sql, curTime, ContextUtil.getUser(User.class).getId());
		return genericDao.findSingle(String.class, "SELECT PWD_UPDATE_TIME FROM SYS_USER WHERE ID=?", ContextUtil.getUser(User.class).getId());
	}

	/**
	 * 首页用系统各数据
	 * 销售额，销售量，平均单价，退款金额，网络异常设备
	 */
	@Override
	public Map<String, Object> saveSysData() {
		Map<String, Object> map = new HashMap<String, Object>();
		// 当前登录用户
		User user = ContextUtil.getUser(User.class);
		if(null == user)
			throw new BusinessException("用户未登录");

		// 今日销售额
		double salesTodayAmount = MathUtil.round(getSalesAmount(user, new Date(System.currentTimeMillis())), 2);
		map.put("salesTodayAmount", salesTodayAmount);
		// 昨天销售额
		double salesYesterdayAmount = MathUtil.round(getSalesAmount(user, Date.valueOf(DateUtil.getYestdayDate())), 2);
		map.put("salesYesterdayAmount", salesYesterdayAmount);
		
		// 今日销售量
		int salesTodayQty = getSalesQty(user, new Date(System.currentTimeMillis()));
		map.put("salesTodayQty", salesTodayQty);
		// 昨天销售量
		int salesYesterdayQty = getSalesQty(user, Date.valueOf(DateUtil.getYestdayDate()));
		map.put("salesYesterdayQty", salesYesterdayQty);
		
		// 今日平均单价
		double todayAveragePrice = salesTodayQty == 0 ? 0.0 : MathUtil.round(MathUtil.div(salesTodayAmount, salesTodayQty), 2);
		map.put("todayAveragePrice", todayAveragePrice);
		// 昨日平均单价
		double ystdAveragePrice = salesYesterdayQty == 0 ? 0.0 : MathUtil.round(MathUtil.div(salesYesterdayAmount, salesYesterdayQty), 2);
		map.put("ystdAveragePrice", ystdAveragePrice);
		
		// 今日退款金额
		double todayRefundAmount = MathUtil.round(getRefundAmount(user, new Date(System.currentTimeMillis())), 2);
		map.put("todayRefundAmount", todayRefundAmount);
		// 昨天退款金额
		double ystdRefundAmount = MathUtil.round(getRefundAmount(user, Date.valueOf(DateUtil.getYestdayDate())), 2);
		map.put("ystdRefundAmount", ystdRefundAmount);
		
		// 货道售空设备数
		int saleEmptyQty = getSaleEmptyQty(user);
		map.put("saleEmptyQty", saleEmptyQty);
		
		// 今日网络异常(离线)设备数量
		int offlineDevQty = getDeviceLogQty(user, Commons.DEVICE_STATUS_OFFLINE, new Date(System.currentTimeMillis())); // 离线
		map.put("offlineDevQty", offlineDevQty);
		
		return map;
	}
	
	/**
	 * 设备异常数
	 * 
	 * @param user
	 * @return
	 */
	private int getDeviceLogQty(User user, Integer state, Date date) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer("");
		buf.append("SELECT COUNT(*) FROM T_DEVICE_LOG L LEFT JOIN T_DEVICE D ON D.DEV_NO = L.DEVICE_NO WHERE 1=1 AND L.DEVICE_STATUS = ? ");
		args.add(state);
		
		if (Commons.ORG_HQ != user.getOrgId()) {// 邦马特平台
			// 循环递归查询出树状结构的组织ID集合
			List<Long> orgIds = CommonUtil.getOrgIdsByCyclicOrgnization(getOrgnizationList(user.getOrgId()));
			if (orgIds != null && !orgIds.isEmpty()) {
				StringBuffer buffer = new StringBuffer(" IN(");
				for (Long orgId : orgIds) {
					buffer.append("?,");
					args.add(orgId);
				}
				buffer.setLength(buffer.length() - 1);
				buffer.append(")");
				String orgIdsSQL = buffer.toString();
				
				buf.append(" AND D.ORG_ID ").append(orgIdsSQL);
			}
		}
		
		// 指定日期
		if (null != date) {
			// 开始日期
			buf.append(" AND L.CREATE_TIME>=? ");
			args.add(DateUtil.getStartDate(date));
			// 结束日期
			buf.append(" AND L.CREATE_TIME<=? ");
			args.add(DateUtil.getEndDate(date));
		}
		return genericDao.findSingle(int.class, buf.toString(), args.toArray());
	}
	
	/**
	 * 取得总销售额
	 * @param user 当前登录用户
	 * @param date 指定日期
	 * @return 总销售额
	 */
	private double getSalesAmount(User user, Date date) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer(" SELECT COALESCE(SUM(OD.qty*OD.price), 0) ");
		buf.append(" FROM T_ORDER O ");
		buf.append(" LEFT JOIN T_ORDER_DETAIL OD ON OD.ORDER_NO = O.CODE ");
		buf.append(" WHERE 1=1  ");
		
		if (Commons.ORG_HQ != user.getOrgId()) {// 邦马特平台
			// 循环递归查询出树状结构的组织ID集合
			List<Long> orgIds = CommonUtil.getOrgIdsByCyclicOrgnization(getOrgnizationList(user.getOrgId()));
			if (orgIds != null && !orgIds.isEmpty()) {
				StringBuffer buffer = new StringBuffer(" IN(");
				for (Long orgId : orgIds) {
					buffer.append("?,");
					args.add(orgId);
				}
				buffer.setLength(buffer.length() - 1);
				buffer.append(")");
				String orgIdsSQL = buffer.toString();
				
				buf.append(" AND O.ORG_ID ").append(orgIdsSQL);
			}
		}
		
		buf.append(" AND O.STATE=? ");
		args.add(Commons.ORDER_STATE_FINISH);
		
		//开始时间
		buf.append(" AND O.PAY_TIME >= ? ");
		args.add(DateUtil.getStartDate(date));
		//结束时间
		buf.append(" AND O.PAY_TIME<= ? ");
		args.add(DateUtil.getEndDate(date));
		
		Double amount = genericDao.findSingle(Double.class, buf.toString(), args.toArray());
		return amount == null ? 0 : amount;
	}
	
	/**
	 * 新增店铺数量
	 * @param user 当前登录用户
	 * @param date 时间
	 * @return 新增店铺数量
	 */
	private int getStoreCount(User user, Date date) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer("");
		buf.append("SELECT COUNT(*) FROM T_POINT_PLACE WHERE 1=1 ");

		if (Commons.ORG_HQ != user.getOrgId()) {// 邦马特平台
			// 循环递归查询出树状结构的组织ID集合
			List<Long> orgIds = CommonUtil.getOrgIdsByCyclicOrgnization(getOrgnizationList(user.getOrgId()));
			if (orgIds != null && !orgIds.isEmpty()) {
				StringBuffer buffer = new StringBuffer(" IN(");
				for (Long orgId : orgIds) {
					buffer.append("?,");
					args.add(orgId);
				}
				buffer.setLength(buffer.length() - 1);
				buffer.append(")");
				String orgIdsSQL = buffer.toString();
				
				buf.append(" AND ORG_ID ").append(orgIdsSQL);
			}
		}

		// 指定日期
		if (null != date) {
			// 开始日期
			buf.append(" AND CREATE_TIME>=? ");
			args.add(DateUtil.getStartDate(date));
			// 结束日期
			buf.append(" AND CREATE_TIME<=? ");
			args.add(DateUtil.getEndDate(date));
		}
		
		buf.append(" AND STATE != ? ");
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
		return genericDao.findSingle(int.class, buf.toString(), args.toArray());
	}
	
	/**
	 * 新增设备数量
	 * @param user 当前登录用户
	 * @param date 时间
	 * @return 新增设备数量
	 */
	private int getDeviceCount(User user, Date date) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer("");
		buf.append("SELECT COUNT(*) FROM T_DEVICE WHERE 1=1 AND BIND_STATE = ? ");
		args.add(1);
		
		if (Commons.ORG_HQ != user.getOrgId()) {// 邦马特平台
			// 循环递归查询出树状结构的组织ID集合
			List<Long> orgIds = CommonUtil.getOrgIdsByCyclicOrgnization(getOrgnizationList(user.getOrgId()));
			if (orgIds != null && !orgIds.isEmpty()) {
				StringBuffer buffer = new StringBuffer(" IN(");
				for (Long orgId : orgIds) {
					buffer.append("?,");
					args.add(orgId);
				}
				buffer.setLength(buffer.length() - 1);
				buffer.append(")");
				String orgIdsSQL = buffer.toString();
				
				buf.append(" AND ORG_ID ").append(orgIdsSQL);
			}
		}
		
		// 指定日期
		if (null != date) {
			// 开始日期
			buf.append(" AND CREATE_TIME>=? ");
			args.add(DateUtil.getStartDate(date));
			// 结束日期
			buf.append(" AND CREATE_TIME<=? ");
			args.add(DateUtil.getEndDate(date));
		}
		return genericDao.findSingle(int.class, buf.toString(), args.toArray());
	}

	/**
	 * 取得当前用户的销售量
	 * @param user 当前登录用户
	 * @param date 指定日期
	 * @return 当前登录用户的销售量
	 */
	private int getSalesQty(User user, Date date) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer(" SELECT COALESCE(SUM(OD.QTY), 0) FROM T_ORDER_DETAIL OD LEFT JOIN T_ORDER O ON O.CODE = OD.ORDER_NO WHERE 1 = 1 ");
		
		if (Commons.ORG_HQ != user.getOrgId()) {// 邦马特平台
			// 循环递归查询出树状结构的组织ID集合
			List<Long> orgIds = CommonUtil.getOrgIdsByCyclicOrgnization(getOrgnizationList(user.getOrgId()));
			if (orgIds != null && !orgIds.isEmpty()) {
				StringBuffer buffer = new StringBuffer(" IN(");
				for (Long orgId : orgIds) {
					buffer.append("?,");
					args.add(orgId);
				}
				buffer.setLength(buffer.length() - 1);
				buffer.append(")");
				String orgIdsSQL = buffer.toString();
				
				buf.append(" AND O.ORG_ID ").append(orgIdsSQL);
			}
		}
		
		// 指定日期
		if (null != date) {
			// 开始日期
			buf.append(" AND O.PAY_TIME>=? ");
			args.add(DateUtil.getStartDate(date));
			// 结束日期
			buf.append(" AND O.PAY_TIME<=? ");
			args.add(DateUtil.getEndDate(date));
		}
		
		buf.append(" AND O.STATE = ? ");
		args.add(Commons.ORDER_STATE_FINISH);
		
		return genericDao.findSingle(int.class, buf.toString(), args.toArray());
	}
	
	/**
	 * 取得货道售空的设备数
	 * @param user 当前登录用户
	 * @return 当前货道售空的设备数
	 */
	private int getSaleEmptyQty(User user) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer(" SELECT COALESCE(COUNT(*), 0) FROM T_DEVICE WHERE 1 = 1 ");
		
		buf.append(" AND ID IN ( SELECT DEVICE_ID FROM T_DEVICE_AISLE WHERE 1=1 AND STOCK = 0 AND PRODUCT_ID != 0 AND PRODUCT_ID IS NOT NULL GROUP BY DEVICE_ID ) ");
		
		if (Commons.ORG_HQ != user.getOrgId()) {// 邦马特平台
			// 循环递归查询出树状结构的组织ID集合
			List<Long> orgIds = CommonUtil.getOrgIdsByCyclicOrgnization(getOrgnizationList(user.getOrgId()));
			if (orgIds != null && !orgIds.isEmpty()) {
				StringBuffer buffer = new StringBuffer(" IN(");
				for (Long orgId : orgIds) {
					buffer.append("?,");
					args.add(orgId);
				}
				buffer.setLength(buffer.length() - 1);
				buffer.append(")");
				String orgIdsSQL = buffer.toString();
				
				buf.append(" AND ORG_ID ").append(orgIdsSQL);
			}
		}
		
		buf.append(" AND BIND_STATE = ? ");
		args.add(Commons.BIND_STATE_SUCCESS);
		
		return genericDao.findSingle(int.class, buf.toString(), args.toArray());
	}
	
	/**
	 * 取得当前登录用户的退款金额
	 * @param user 当前登录用户
	 * @param date 指定日期
	 * @return 当前登录用户的退款金额
	 */
	private double getRefundAmount(User user, Date date) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer(" SELECT COALESCE(SUM(FEE_REFUND), 0) FROM T_REFUND WHERE 1 = 1 ");
		
		if (Commons.ORG_HQ != user.getOrgId()) {// 邦马特平台
			// 循环递归查询出树状结构的组织ID集合
			List<Long> orgIds = CommonUtil.getOrgIdsByCyclicOrgnization(getOrgnizationList(user.getOrgId()));
			if (orgIds != null && !orgIds.isEmpty()) {
				StringBuffer buffer = new StringBuffer(" IN(");
				for (Long orgId : orgIds) {
					buffer.append("?,");
					args.add(orgId);
				}
				buffer.setLength(buffer.length() - 1);
				buffer.append(")");
				String orgIdsSQL = buffer.toString();
				
				buf.append(" AND ORG_ID ").append(orgIdsSQL);
			}
		}
		
		// 指定日期
		if (null != date) {
			// 开始日期
			buf.append(" AND UPDATE_TIME>=? ");
			args.add(DateUtil.getStartDate(date));
			// 结束日期
			buf.append(" AND UPDATE_TIME<=? ");
			args.add(DateUtil.getEndDate(date));
		}
		
		buf.append(" AND STATE = ? ");
		args.add(Commons.REFUND_STATE_SUCCESS);
		
		Double amount = genericDao.findSingle(Double.class, buf.toString(), args.toArray());
		return amount == null ? 0 : amount;
	}
	
	/**
	 * 店铺销售排行榜
	 * @return
	 */
	private List<Order> getTop10Stores(User user, Date date) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer(" SELECT PP.POINT_NAME AS name, COALESCE(SUM(OD.qty*OD.price), 0) AS amount ");
		buf.append(" FROM T_ORDER O ");
		buf.append(" LEFT JOIN T_ORDER_DETAIL OD ON OD.ORDER_NO = O.CODE ");
		buf.append(" LEFT JOIN SYS_ORG ORG ON O.ORG_ID = ORG.ID ");
		buf.append(" LEFT JOIN T_POINT_PLACE PP ON PP.POINT_NO = O.POINT_NO ");
		buf.append(" WHERE 1=1  ");
		
		if (Commons.ORG_HQ != user.getOrgId()) {// 邦马特平台
			// 循环递归查询出树状结构的组织ID集合
			List<Long> orgIds = CommonUtil.getOrgIdsByCyclicOrgnization(getOrgnizationList(user.getOrgId()));
			if (orgIds != null && !orgIds.isEmpty()) {
				StringBuffer buffer = new StringBuffer(" IN(");
				for (Long orgId : orgIds) {
					buffer.append("?,");
					args.add(orgId);
				}
				buffer.setLength(buffer.length() - 1);
				buffer.append(")");
				String orgIdsSQL = buffer.toString();
				
				buf.append(" AND O.ORG_ID ").append(orgIdsSQL);
			}
		}
		
		buf.append(" AND PP.POINT_NAME IS NOT NULL ");
		
		buf.append(" AND PP.STATE != ? ");
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
		
		buf.append(" AND O.STATE=? ");
		args.add(Commons.ORDER_STATE_FINISH);
		
		//开始时间
		buf.append(" AND O.PAY_TIME >= ? ");
		args.add(DateUtil.getStartDate(date));
		//结束时间
		buf.append(" AND O.PAY_TIME<= ? ");
		args.add(DateUtil.getEndDate(date));
		
		buf.append(" GROUP BY PP.POINT_NAME ");
		buf.append(" ORDER BY AMOUNT DESC ");
		buf.append(" LIMIT ? ");
		args.add(10);//前10名
		
		return genericDao.findTs(Order.class , buf.toString(), args.toArray());
	}
	
	/**
	 * 商品销售排行榜
	 * @return
	 */
	private List<Order> getTop10Products(User user, Date date) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer(" SELECT P.SKU_NAME AS name, COALESCE(SUM(OD.qty*OD.price), 0) AS amount ");
		buf.append(" FROM T_ORDER O ");
		buf.append(" LEFT JOIN T_ORDER_DETAIL OD ON OD.ORDER_NO = O.CODE ");
		buf.append(" LEFT JOIN SYS_ORG ORG ON O.ORG_ID = ORG.ID ");
		buf.append(" LEFT JOIN T_PRODUCT P ON P.ID = OD.SKU_ID ");
		buf.append(" WHERE 1=1  ");
		
		if (Commons.ORG_HQ != user.getOrgId()) {// 邦马特平台
			// 循环递归查询出树状结构的组织ID集合
			List<Long> orgIds = CommonUtil.getOrgIdsByCyclicOrgnization(getOrgnizationList(user.getOrgId()));
			if (orgIds != null && !orgIds.isEmpty()) {
				StringBuffer buffer = new StringBuffer(" IN(");
				for (Long orgId : orgIds) {
					buffer.append("?,");
					args.add(orgId);
				}
				buffer.setLength(buffer.length() - 1);
				buffer.append(")");
				String orgIdsSQL = buffer.toString();
				
				buf.append(" AND O.ORG_ID ").append(orgIdsSQL);
			}
		}
		
		buf.append(" AND P.SKU_NAME IS NOT NULL ");
		buf.append(" AND O.STATE=? ");
		args.add(Commons.ORDER_STATE_FINISH);
		
		//开始时间
		buf.append(" AND O.PAY_TIME >= ? ");
		args.add(DateUtil.getStartDate(date));
		//结束时间
		buf.append(" AND O.PAY_TIME<= ? ");
		args.add(DateUtil.getEndDate(date));
		
		buf.append(" GROUP BY P.SKU_NAME ");
		buf.append(" ORDER BY AMOUNT DESC ");
		buf.append(" LIMIT ? ");
		args.add(10);//前10名
		return genericDao.findTs(Order.class , buf.toString(), args.toArray());
	}

	/**
	 * 商品销售量排行榜
	 * @return
	 */
	private List<Order> getTop10ProductSalesQtyOrders(User user, Date date) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer(" SELECT P.SKU_NAME AS name, COALESCE(SUM(OD.qty), 0) AS salesVolume ");
		buf.append(" FROM T_ORDER O ");
		buf.append(" LEFT JOIN T_ORDER_DETAIL OD ON OD.ORDER_NO = O.CODE ");
		buf.append(" LEFT JOIN SYS_ORG ORG ON O.ORG_ID = ORG.ID ");
		buf.append(" LEFT JOIN T_PRODUCT P ON P.ID = OD.SKU_ID ");
		buf.append(" WHERE 1=1  ");
		
		if (Commons.ORG_HQ != user.getOrgId()) {// 邦马特平台
			// 循环递归查询出树状结构的组织ID集合
			List<Long> orgIds = CommonUtil.getOrgIdsByCyclicOrgnization(getOrgnizationList(user.getOrgId()));
			if (orgIds != null && !orgIds.isEmpty()) {
				StringBuffer buffer = new StringBuffer(" IN(");
				for (Long orgId : orgIds) {
					buffer.append("?,");
					args.add(orgId);
				}
				buffer.setLength(buffer.length() - 1);
				buffer.append(")");
				String orgIdsSQL = buffer.toString();
				
				buf.append(" AND O.ORG_ID ").append(orgIdsSQL);
			}
		}
		
		buf.append(" AND P.SKU_NAME IS NOT NULL ");
		buf.append(" AND O.STATE=? ");
		args.add(Commons.ORDER_STATE_FINISH);
		
		//开始时间
		buf.append(" AND O.PAY_TIME >= ? ");
		args.add(DateUtil.getStartDate(date));
		//结束时间
		buf.append(" AND O.PAY_TIME<= ? ");
		args.add(DateUtil.getEndDate(date));
		
		buf.append(" GROUP BY P.SKU_NAME ");
		buf.append(" ORDER BY salesVolume DESC ");
		buf.append(" LIMIT ? ");
		args.add(10);//前10名
		return genericDao.findTs(Order.class , buf.toString(), args.toArray());
	}
	
	public BestSellerLists findBestSellerlists(Date date) {
		// 取得当前登录用户
		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("用户未登录");
		
		BestSellerLists lists = new BestSellerLists();
		
		lists.setStoreSales(getTop10Stores(user, date));// 店铺销售排行榜
		lists.setProductSales(getTop10Products(user, date));// 商品销售额排行榜
		lists.setProductSalesQtyOrders(getTop10ProductSalesQtyOrders(user, date));// 商品销售量排行榜
		
		return lists;
	}
	
	/**
	 * 通联单笔付款接口
	 * @return
	 * @throws Exception 
	 */
	public String singleTranx(Card card, double tradeAmount) throws Exception {
		// String testTranURL="https://113.108.182.3/aipg/ProcessServlet"; //通联测试环境，外网（商户测试使用）
		// String tranURL = "http://tlt.allinpay.com/aipg/ProcessServlet";// 通联生产环境（商户上线时使用）
		boolean isfront = false;// 是否发送至前置机（由前置机进行签名）如不特别说明，商户技术不要设置为true
		String trx_code, busicode;// 100001批量代收 100002批量代付 100011单笔实时代收 100014单笔实时代付
		TranxServiceImpl tranxService = new TranxServiceImpl();

		/**
		 * 测试的时候不用修改以下业务代码，但上生产环境的时候，必须使用业务人员提供的业务代码，否则返回“未开通业务类型”
		 * 另外，特别说明：如果生产环境对接的时候返回”未开通产品“那么说明该商户开通的接口与目前测试的接口不一样，需要找业务确认
		 * 代收是批量代收接口的简称，代付 是批量代付接口的简称，
		 * 对接报文中，info下面的用户名一般是：商户号+04，比如商户号为：200604000000445，那么对接用户一般为：20060400000044504
		 */
		trx_code = Commons.TRX_CODE;// 单笔提现
		if ("100011".equals(trx_code) || "100001".equals(trx_code))// 收款的时候，填写收款的业务代码
			busicode = "19900";
		else
			busicode = "09900";
		//设置安全提供者,注意，这一步尤为重要
		BouncyCastleProvider provider = new BouncyCastleProvider();
		XmlTools.initProvider(provider);
		
		TranxCon tranxCon = new TranxCon();
		tranxCon.setAcctName(card.getCardOwner());
		tranxCon.setAcctNo(card.getCardNo());
		tranxCon.setAmount((int)MathUtil.mul(tradeAmount, 100) + "");//单位：分
		tranxCon.setSum((int)MathUtil.mul(tradeAmount, 100) + "");
		tranxCon.setMerchantId(merchantId);
		tranxCon.setPassword(tlPassword);
		tranxCon.setPfxPath(getSrcFilePath("config/tl/bmt", "20058400001320804.p12"));
		tranxCon.setPfxPassword("111111");
		tranxCon.setTel(card.getMobileNo());
		tranxCon.setTltcerPath(getSrcFilePath("config/tl", "allinpay-pds.cer"));
		tranxCon.setUserName(merchantId + "04");
		String bankCode = CommonUtil.getBankCodeByType(card.getCardType());
		return tranxService.singleTranx(tranxCon, tranURL, trx_code, busicode,isfront, card.getMode(), bankCode);
	}
	
	public String getSrcFilePath(String dir, String fileName) {
		return this.getClass().getResource("/").getPath() + dir + File.separator + fileName;
	}
	
	/**
	 * 是否为同一天
	 * 
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	private boolean compareSomeDay(Date startTime, Date endTime) {
		if (null == startTime || null == endTime) {
			throw new BusinessException("参数非法");
		}
		return (DateUtil.getDay(startTime) == DateUtil.getDay(endTime)) ? true : false;
	}
	
	/**
	 * 统计销售数据
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	@Override
	public List<Order> findSalesData(Date startTime, Date endTime) {
		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("当前用户未登录");

		startTime = null == startTime ? new Date(System.currentTimeMillis()) : startTime;
		endTime = null == endTime ? new Date(System.currentTimeMillis()) : endTime;

		StringBuffer buf = new StringBuffer(" SELECT ");
		List<Object> args = new ArrayList<Object>();
		buf.append(" TO_CHAR(O.pay_time, ");
		if (compareSomeDay(startTime, endTime)) {
			buf.append(" 'HH24' ");
		} else {
			buf.append(" 'YYYY-MM-DD' ");
		}
		buf.append(" ) ");
		buf.append(" AS date , COALESCE(SUM(OD.qty * OD.price), 0) AS salesAmount, COALESCE (SUM(OD.qty), 0) as salesVolume ");
		buf.append(" FROM T_ORDER O ");
		buf.append(" LEFT JOIN T_ORDER_DETAIL OD ON O.CODE = OD.ORDER_NO ");
		buf.append(" WHERE 1 = 1 ");
		buf.append(" AND O.PAY_TIME>=? ");
		args.add(DateUtil.getStartDate(startTime));
		buf.append(" AND O.PAY_TIME<=? ");
		args.add(DateUtil.getEndDate(endTime));
		
		if (Commons.ORG_HQ != user.getOrgId()) {// 邦马特平台
			// 循环递归查询出树状结构的组织ID集合
			List<Long> orgIds = CommonUtil.getOrgIdsByCyclicOrgnization(getOrgnizationList(user.getOrgId()));
			if (orgIds != null && !orgIds.isEmpty()) {
				StringBuffer buffer = new StringBuffer(" IN(");
				for (Long orgId : orgIds) {
					buffer.append("?,");
					args.add(orgId);
				}
				buffer.setLength(buffer.length() - 1);
				buffer.append(")");
				String orgIdsSQL = buffer.toString();
				
				buf.append(" AND O.ORG_ID ").append(orgIdsSQL);
			}
		}
		
		buf.append(" AND O.STATE = ? ");
		args.add(Commons.ORDER_STATE_FINISH);
		buf.append(" GROUP BY date ORDER BY date ");

		List<Order> orders = genericDao.findTs(Order.class, buf.toString(), args.toArray());
		for (Order order : orders)
			order.setAveragePrice(order.getSalesVolume() == 0 ? 0.0 : MathUtil.div(order.getSalesAmount(), order.getSalesVolume()));

		return orders;
	}
	
	/**
	 * 查询设备异常信息
	 * @param page
	 * @return
	 */
	public List<DeviceLog> findDeviceLogs(Page page) {
		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("当前用户未登录");
		
		StringBuffer buffer = new StringBuffer("SELECT ");
		List<Object> args = new ArrayList<Object>();
		buffer.append(SQLUtils.getColumnsSQL(DeviceLog.class, "DL"));
		buffer.append(" ,DR.FACTORY_DEV_NO AS factoryDevNo, PP.POINT_ADDRESS AS pointAddress ");
		buffer.append(" FROM T_DEVICE_LOG DL ");
		buffer.append(" LEFT JOIN T_DEVICE D ON DL.DEVICE_NO = D.DEV_NO ");
		buffer.append(" LEFT JOIN T_DEVICE_RELATION DR ON D.DEV_NO = DR.DEV_NO ");
		buffer.append(" LEFT JOIN T_POINT_PLACE PP ON PP.ID = D.POINT_ID ");
		buffer.append(" WHERE 1 = 1 ");
		buffer.append(" and D.DEV_NO is not null ");
		
		if (Commons.ORG_HQ != user.getOrgId()) {// 邦马特平台
			// 循环递归查询出树状结构的组织ID集合
			List<Long> orgIds = CommonUtil.getOrgIdsByCyclicOrgnization(getOrgnizationList(user.getOrgId()));
			if (orgIds != null && !orgIds.isEmpty()) {
				StringBuffer buf = new StringBuffer(" IN(");
				for (Long orgId : orgIds) {
					buf.append("?,");
					args.add(orgId);
				}
				buf.setLength(buf.length() - 1);
				buf.append(")");
				String orgIdsSQL = buf.toString();
				
				buffer.append(" AND D.ORG_ID ").append(orgIdsSQL);
			}
		}
		
		buffer.append(" AND PP.STATE != ? ");
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
		
		return genericDao.findTs(DeviceLog.class, page, buffer.toString(), args.toArray());
	}

	/**
	 * 查询商品异常信息
	 * @param page
	 * @return
	 */
	public List<ProductLog> findProductLogs(Page page) {
		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("当前用户未登录");
		
		StringBuffer buffer = new StringBuffer("SELECT ");
		List<Object> args = new ArrayList<Object>();
		buffer.append(SQLUtils.getColumnsSQL(ProductLog.class, "PL"));
		buffer.append(" ,P.SKU_NAME AS productName ");
		buffer.append(" FROM T_PRODUCT_LOG PL ");
		buffer.append(" LEFT JOIN T_PRODUCT P ON P.ID = PL.PRODUCT_ID ");
		buffer.append(" WHERE 1 = 1 ");
		buffer.append(" AND P. ID IS NOT NULL ");
		
		if (Commons.ORG_HQ != user.getOrgId()) {// 邦马特平台
			// 循环递归查询出树状结构的组织ID集合
			List<Long> orgIds = CommonUtil.getOrgIdsByCyclicOrgnization(getOrgnizationList(user.getOrgId()));
			if (orgIds != null && !orgIds.isEmpty()) {
				StringBuffer buf = new StringBuffer(" IN(");
				for (Long orgId : orgIds) {
					buf.append("?,");
					args.add(orgId);
				}
				buf.setLength(buf.length() - 1);
				buf.append(")");
				String orgIdsSQL = buf.toString();
				
				buffer.append(" AND P.ORG_ID ").append(orgIdsSQL);
			}
		}
		
		return genericDao.findTs(ProductLog.class, page, buffer.toString(), args.toArray());
	}
	
	public List<Orgnization> getOrgnizationList(Long orgId) {
		return orgnizationService.findCyclicOrgnizations(orgId);
	}
	
	/**
	 * 查询货道售空设备信息
	 * @param page
	 * @return
	 */
	@Override
	public List<Device> findSaleEmptyDevices(Page page) {
		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("当前用户未登录");
		
		StringBuffer buffer = new StringBuffer("SELECT ");
		List<Object> args = new ArrayList<Object>();
		buffer.append(" DR.FACTORY_DEV_NO AS FACTORYDEVNO, PP.POINT_NAME AS POINTNAME, PP.POINT_ADDRESS AS POINTADDRESS, COALESCE(SUM(C.AISLE_COUNT), 0) AS AISLECOUNTS, D.ID ");
		buffer.append(" FROM T_DEVICE D ");
		buffer.append(" LEFT JOIN T_POINT_PLACE PP ON PP.ID = D.POINT_ID ");
		buffer.append(" LEFT JOIN T_DEVICE_RELATION DR ON DR.DEV_NO = D.DEV_NO ");
		buffer.append(" LEFT JOIN T_CABINET C ON C.DEVICE_ID = D.ID ");
		buffer.append(" WHERE 1 = 1 ");
		buffer.append(" AND d.ID IN ( SELECT DEVICE_ID FROM T_DEVICE_AISLE WHERE 1=1 AND STOCK = 0 AND PRODUCT_ID != 0 AND PRODUCT_ID IS NOT NULL GROUP BY DEVICE_ID ) ");
		
		if (Commons.ORG_HQ != user.getOrgId()) {// 邦马特平台
			// 循环递归查询出树状结构的组织ID集合
			List<Long> orgIds = CommonUtil.getOrgIdsByCyclicOrgnization(getOrgnizationList(user.getOrgId()));
			if (orgIds != null && !orgIds.isEmpty()) {
				StringBuffer buf = new StringBuffer(" IN(");
				for (Long orgId : orgIds) {
					buf.append("?,");
					args.add(orgId);
				}
				buf.setLength(buf.length() - 1);
				buf.append(")");
				String orgIdsSQL = buf.toString();
				
				buffer.append(" AND D.ORG_ID ").append(orgIdsSQL);
			}
		}
		
		buffer.append(" AND D.BIND_STATE = ? ");
		args.add(Commons.BIND_STATE_SUCCESS);

		buffer.append(" GROUP BY DR.FACTORY_DEV_NO, PP.POINT_NAME, PP.POINT_ADDRESS, D.ID ");
		
		List<Device> devices = genericDao.findTs(Device.class, page, buffer.toString(), args.toArray()); 
		
		for (Device device : devices) {
			int saleEmptyQty = genericDao.findSingle(int.class, "SELECT COALESCE(COUNT(*), 0) FROM T_DEVICE_AISLE WHERE DEVICE_ID  = ? AND STOCK = 0 AND PRODUCT_ID != 0 AND PRODUCT_ID IS NOT NULL", new Object[] {device.getId()});
			device.setSaleEmptyQty(saleEmptyQty);
		}
		
		return devices;
	}
	
	/**
	 * 店铺销售排行榜（所有）
	 * @param page
	 * @return
	 */
	@Override
	public List<Order> findStoreSalesList(Page page, Date date) {
		// 取得当前登录用户
		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("用户未登录");
		
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer(" SELECT PP.POINT_NAME AS pointName, PP.POINT_ADDRESS AS pointAddress, ORG.NAME as orgName, COALESCE(SUM(OD.qty*OD.price), 0) AS salesAmount, COALESCE(SUM(OD.qty), 0) AS salesVolume ");
		buf.append(" FROM T_ORDER O ");
		buf.append(" LEFT JOIN T_ORDER_DETAIL OD ON OD.ORDER_NO = O.CODE ");
		buf.append(" LEFT JOIN SYS_ORG ORG ON O.ORG_ID = ORG.ID ");
		buf.append(" LEFT JOIN T_POINT_PLACE PP ON PP.POINT_NO = O.POINT_NO ");
		buf.append(" WHERE 1=1  ");
		
		if (Commons.ORG_HQ != user.getOrgId()) {// 邦马特平台
			// 循环递归查询出树状结构的组织ID集合
			List<Long> orgIds = CommonUtil.getOrgIdsByCyclicOrgnization(getOrgnizationList(user.getOrgId()));
			if (orgIds != null && !orgIds.isEmpty()) {
				StringBuffer buffer = new StringBuffer(" IN(");
				for (Long orgId : orgIds) {
					buffer.append("?,");
					args.add(orgId);
				}
				buffer.setLength(buffer.length() - 1);
				buffer.append(")");
				String orgIdsSQL = buffer.toString();
				
				buf.append(" AND O.ORG_ID ").append(orgIdsSQL);
			}
		}
		
		buf.append(" AND PP.POINT_NAME IS NOT NULL ");
		
		buf.append(" AND PP.STATE != ? ");
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
		
		buf.append(" AND O.STATE=? ");
		args.add(Commons.ORDER_STATE_FINISH);
		
		//开始时间
		buf.append(" AND O.PAY_TIME >= ? ");
		args.add(DateUtil.getStartDate(date));
		//结束时间
		buf.append(" AND O.PAY_TIME<= ? ");
		args.add(DateUtil.getEndDate(date));
		
		buf.append(" GROUP BY PP.POINT_NAME, PP.POINT_ADDRESS, ORG.NAME ");
		buf.append(" ORDER BY salesAmount DESC ");
		
		return genericDao.findTs(Order.class, page, buf.toString(), args.toArray());
	}
	
	/**
	 * 商品销售额排行榜（所有）
	 * @param page
	 * @return
	 */
	@Override
	public List<Order> findStoreSalesAmountList(Page page, Date date) {
		// 取得当前登录用户
		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("用户未登录");
		
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer(" SELECT P.SKU_NAME AS productName, COALESCE(SUM(OD.qty*OD.price), 0) AS salesAmount ");
		buf.append(" FROM T_ORDER O ");
		buf.append(" LEFT JOIN T_ORDER_DETAIL OD ON OD.ORDER_NO = O.CODE ");
		buf.append(" LEFT JOIN SYS_ORG ORG ON O.ORG_ID = ORG.ID ");
		buf.append(" LEFT JOIN T_PRODUCT P ON P.ID = OD.SKU_ID ");
		buf.append(" WHERE 1=1  ");
		
		if (Commons.ORG_HQ != user.getOrgId()) {// 邦马特平台
			// 循环递归查询出树状结构的组织ID集合
			List<Long> orgIds = CommonUtil.getOrgIdsByCyclicOrgnization(getOrgnizationList(user.getOrgId()));
			if (orgIds != null && !orgIds.isEmpty()) {
				StringBuffer buffer = new StringBuffer(" IN(");
				for (Long orgId : orgIds) {
					buffer.append("?,");
					args.add(orgId);
				}
				buffer.setLength(buffer.length() - 1);
				buffer.append(")");
				String orgIdsSQL = buffer.toString();
				
				buf.append(" AND O.ORG_ID ").append(orgIdsSQL);
			}
		}
		
		buf.append(" AND P.SKU_NAME IS NOT NULL ");
		buf.append(" AND O.STATE=? ");
		args.add(Commons.ORDER_STATE_FINISH);
		
		//开始时间
		buf.append(" AND O.PAY_TIME >= ? ");
		args.add(DateUtil.getStartDate(date));
		//结束时间
		buf.append(" AND O.PAY_TIME<= ? ");
		args.add(DateUtil.getEndDate(date));
		
		buf.append(" GROUP BY P.SKU_NAME ");
		buf.append(" ORDER BY salesAmount DESC ");
		
		List<Order> orders = genericDao.findTs(Order.class, page, buf.toString(), args.toArray());
		
		// 销售额
		double totalSalesAmount = MathUtil.round(getSalesAmount(user, date), 2);
		
		for (Order order : orders) {
			double salesRate = totalSalesAmount == 0 ? 0.0 : MathUtil.div(MathUtil.mul(order.getSalesAmount(), 100), totalSalesAmount);
			order.setSalesRate(salesRate);// 销售额占比
		}
		
		return orders;
	}
	
	/**
	 * 商品销售量排行榜（所有）
	 * @param page
	 * @return
	 */
	@Override
	public List<Order> findStoreSalesVolumeList(Page page, Date date) {
		// 取得当前登录用户
		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("用户未登录");
		
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer(" SELECT P.SKU_NAME AS productName, COALESCE(SUM(OD.qty), 0) AS salesVolume ");
		buf.append(" FROM T_ORDER O ");
		buf.append(" LEFT JOIN T_ORDER_DETAIL OD ON OD.ORDER_NO = O.CODE ");
		buf.append(" LEFT JOIN SYS_ORG ORG ON O.ORG_ID = ORG.ID ");
		buf.append(" LEFT JOIN T_PRODUCT P ON P.ID = OD.SKU_ID ");
		buf.append(" WHERE 1=1  ");
		
		if (Commons.ORG_HQ != user.getOrgId()) {// 邦马特平台
			// 循环递归查询出树状结构的组织ID集合
			List<Long> orgIds = CommonUtil.getOrgIdsByCyclicOrgnization(getOrgnizationList(user.getOrgId()));
			if (orgIds != null && !orgIds.isEmpty()) {
				StringBuffer buffer = new StringBuffer(" IN(");
				for (Long orgId : orgIds) {
					buffer.append("?,");
					args.add(orgId);
				}
				buffer.setLength(buffer.length() - 1);
				buffer.append(")");
				String orgIdsSQL = buffer.toString();
				
				buf.append(" AND O.ORG_ID ").append(orgIdsSQL);
			}
		}
		
		buf.append(" AND P.SKU_NAME IS NOT NULL ");
		buf.append(" AND O.STATE=? ");
		args.add(Commons.ORDER_STATE_FINISH);
		
		//开始时间
		buf.append(" AND O.PAY_TIME >= ? ");
		args.add(DateUtil.getStartDate(date));
		//结束时间
		buf.append(" AND O.PAY_TIME<= ? ");
		args.add(DateUtil.getEndDate(date));
		
		buf.append(" GROUP BY P.SKU_NAME ");
		buf.append(" ORDER BY salesVolume DESC ");
		List<Order> orders = genericDao.findTs(Order.class, page, buf.toString(), args.toArray());
		
		// 总销售量
		int totalSalesVolume = getSalesQty(user, new Date(System.currentTimeMillis()));
		
		for (Order order : orders) {
			double salesVolumeRate = totalSalesVolume == 0 ? 0.0 : MathUtil.div(MathUtil.mul(Double.valueOf(order.getSalesVolume()), 100), Double.valueOf(totalSalesVolume));
			order.setSalesVolumeRate(salesVolumeRate);// 销售量占比
		}
		
		return orders;
	}
	
}

