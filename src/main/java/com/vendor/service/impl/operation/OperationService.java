package com.vendor.service.impl.operation;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.ecarry.core.dao.IGenericDao;
import com.ecarry.core.dao.SQLUtils;
import com.ecarry.core.exception.BusinessException;
import com.ecarry.core.util.IdWorker;
import com.ecarry.core.util.MathUtil;
import com.ecarry.core.util.ZHConverter;
import com.ecarry.core.web.core.ContextUtil;
import com.ecarry.core.web.core.Page;
import com.vendor.po.Cabinet;
import com.vendor.po.DeviceAisle;
import com.vendor.po.OfflineMessage;
import com.vendor.po.Order;
import com.vendor.po.OrderDetail;
import com.vendor.po.Orgnization;
import com.vendor.po.PointPlace;
import com.vendor.po.Product;
import com.vendor.po.Refund;
import com.vendor.po.SortingRecommended;
import com.vendor.po.TradeFlow;
import com.vendor.po.User;
import com.vendor.service.IOperationService;
import com.vendor.service.IOrgnizationService;
import com.vendor.util.CommonUtil;
import com.vendor.util.Commons;
import com.vendor.util.DateUtil;
import com.vendor.util.HttpAdapter;
import com.vendor.util.MessagePusher;
import com.vendor.util.wechat.ClientCustomSSL;
import com.vendor.util.wxpay.GetWxOrderno;
import com.vendor.util.wxpay.RequestHandler;
import com.vendor.util.wxpay.TenpayUtil;
import com.vendor.vo.app.ChangeProductStateData;
import com.vendor.vo.app.ChangeProductStateProductData;

@Service("operationService")
public class OperationService implements IOperationService {
	
	private static final Logger logger = Logger.getLogger(OperationService.class);
	
	@Autowired
	private IGenericDao genericDao;
	
	@Autowired
	private HttpAdapter httpAdapter;
	
	// 微信支付商户开通后 微信会提供appid和appsecret和商户号partner
	@Value("${wx.appid}")
	private String wechatAppId;// appid
	
	@Value("${wx.secret}")
	private String wechatAppKey;// appsecret
	
	@Value("${pay.wechatMchId}")
	private String wechatMchId;// 商户号partner
	
	// 这个参数partnerkey是在商户后台配置的一个32位的key,微信商户平台-账户设置-安全设置-api安全
	@Value("${pay.wechatKey}")
	private String wechatKey;// partnerkey
	
	@Autowired
	private IdWorker idWorker;
	
	@Value("${pay.refund.url}")
	private String refundUrl;//退款url

	@Value("${pay.refund.query.url}")
	private String refundQueryUrl;//退款查询url

	@Autowired
	private IOrgnizationService orgnizationService;

	/**
	 * 查询【经营分析】店铺信息（包括点击按钮查询）
	 * @param page
	 * @param store
	 * @return
	 */
	@Override
	public List<PointPlace> findAnalysiStores(Page page, PointPlace pointPlace) {
		StringBuffer buf = new StringBuffer("SELECT ");
		String cols = SQLUtils.getColumnsSQL(PointPlace.class, "C");
		buf.append(cols);
		buf.append(" ,SO.NAME AS orgName ");
		buf.append(" FROM T_POINT_PLACE C LEFT JOIN SYS_ORG SO ON C.ORG_ID=SO.ID WHERE 1=1 ");
		List<Object> args = new ArrayList<Object>();;

		User user = ContextUtil.getUser(User.class);

		if (pointPlace == null)
			pointPlace = new PointPlace();
		
		Long finalOrgId = pointPlace.getOrgId() != null ? pointPlace.getOrgId() : user.getOrgId();
		if (Commons.CONTAIN_SUB_ORG_TRUE == pointPlace.getContainSubOrg()) {// 查询关联的下级组织
			// 循环递归查询出树状结构的组织ID集合
			List<Long> orgIds = CommonUtil.getOrgIdsByCyclicOrgnization(getOrgnizationList(finalOrgId));
			if (orgIds != null && !orgIds.isEmpty()) {
				StringBuffer buffer = new StringBuffer(" IN(");
				for (Long orgId : orgIds) {
					buffer.append("?,");
					args.add(orgId);
				}
				buffer.setLength(buffer.length() - 1);
				buffer.append(")");
				String orgIdsSQL = buffer.toString();

				buf.append(" AND C.ORG_ID ").append(orgIdsSQL);
			}
		} else {
			buf.append(" AND C.ORG_ID = ? ");
			args.add(finalOrgId);
		}

		buf.append(" AND C.STATE != ? ");
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除

		if (pointPlace.getPointNo() != null) {
			buf.append(" AND").append(" (C.POINT_NO LIKE ? OR C.POINT_NO LIKE ?)");
			args.add("%" + ZHConverter.convert(pointPlace.getPointNo(), ZHConverter.TRADITIONAL) + "%");
			args.add("%" + ZHConverter.convert(pointPlace.getPointNo(), ZHConverter.SIMPLIFIED) + "%");
		}
		if (pointPlace.getPointName() != null) {
			buf.append(" AND").append(" (C.POINT_NAME LIKE ? OR C.POINT_NAME LIKE ?)");
			args.add("%" + ZHConverter.convert(pointPlace.getPointName(), ZHConverter.TRADITIONAL) + "%");
			args.add("%" + ZHConverter.convert(pointPlace.getPointName(), ZHConverter.SIMPLIFIED) + "%");
		}
		if (pointPlace.getPointAddress() != null) {
			buf.append(" AND").append(" (C.POINT_ADDRESS LIKE ? OR C.POINT_ADDRESS LIKE ?)");
			args.add("%" + ZHConverter.convert(pointPlace.getPointAddress(), ZHConverter.TRADITIONAL) + "%");
			args.add("%" + ZHConverter.convert(pointPlace.getPointAddress(), ZHConverter.SIMPLIFIED) + "%");
		}
		if (pointPlace.getOrgName() != null) {
			buf.append(" AND").append(" (SO.NAME LIKE ? OR SO.NAME LIKE ?)");
			args.add("%" + ZHConverter.convert(pointPlace.getOrgName(), ZHConverter.TRADITIONAL) + "%");
			args.add("%" + ZHConverter.convert(pointPlace.getOrgName(), ZHConverter.SIMPLIFIED) + "%");
		}
		if (!StringUtils.isEmpty(pointPlace.getProv())) {//省
			buf.append(" AND C.PROV = ? ");
			args.add(pointPlace.getProv());
		}
		if (!StringUtils.isEmpty(pointPlace.getCity())) {//市
			buf.append(" AND C.CITY = ? ");
			args.add(pointPlace.getProv());
		}
		if (!StringUtils.isEmpty(pointPlace.getDist())) {//区
			buf.append(" AND C.DIST = ? ");
			args.add(pointPlace.getProv());
		}
		if (pointPlace.getPointType() != null) {
			buf.append(" AND C.POINT_TYPE = ? ");
			args.add(pointPlace.getPointType());
		}
		return genericDao.findTs(PointPlace.class, page, buf.toString(), args.toArray());
	}
	
	/**
	 * 取得【经营分析】各项累计数据
	 */
	@Override
	public Map<String, Object> findSysData(PointPlace pointPlace) {
		Map<String, Object> map = new HashMap<String, Object>();
		// 构造店铺ID集合
		List<Long> pointIds = createPointIds(pointPlace);
		
		// 总销售额(所有商品总销售额)
		double salesAmount = MathUtil.round(getSalesAmount(null, null, pointIds), 2);
		map.put("salesAmount", salesAmount);
		
		// 总销量(所有商品总销量)
		int salesVolume = getSalesVolume(null, null, pointIds);
		map.put("salesVolume", salesVolume);

		// 平均单价
		double averagePrice = salesVolume == 0 ? 0 : MathUtil.round(MathUtil.div(salesAmount, salesVolume),  2);
		map.put("averagePrice", averagePrice);
		
		// 总退款金额
		double refundAmount = MathUtil.round(getRefundAmount(null, null, pointIds), 2);;
		map.put("refundAmount", refundAmount);
		
		// 总店铺数量
		int storeCount = pointIds.size();
		map.put("storeCount", storeCount);
		
		// 总设备数量
		int deviceCount = getDeviceCount(pointIds);
		map.put("deviceCount", deviceCount);
		
		return map;
	}
	
	/**
	 * 构造店铺ID集合
	 * 如果查询条件传过来店铺ID的情况下，直接返回改ID；否则根据其他查询条件检索符合条件的店铺信息，构造店铺ID返回。
	 * @param pointPlace
	 * @return
	 */
	public List<Long> createPointIds(PointPlace pointPlace) {
		List<Long> pointIds = new ArrayList<Long>();
		if (null != pointPlace.getId())
			pointIds.add(pointPlace.getId());
		else {
			// 取得所有符合条件的的店铺信息
			List<PointPlace> pointPlaces = findAnalysiStores(null, pointPlace);
			for (PointPlace point : pointPlaces)
				pointIds.add(point.getId());
		}
		
		return pointIds;
	}
	
	/**
	 * 取得选择店铺的销售额
	 * @param date 指定日期
	 * @return 选择店铺的销售额
	 */
	private double getSalesAmount(Date startDate, Date endDate, List<Long> pointIds) {
		if (null == pointIds || pointIds.isEmpty())
			return 0;
		
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer(" SELECT COALESCE(SUM(OD.QTY * OD.PRICE), 0) FROM T_ORDER O ");
		buf.append(" LEFT JOIN T_ORDER_DETAIL OD ON O.CODE = OD.ORDER_NO ");
		buf.append(" WHERE 1 = 1 ");
		
		// 开始日期
		if (null != startDate) {
			buf.append(" AND O.PAY_TIME>=? ");
			args.add(DateUtil.getStartDate(startDate));
		}
		// 结束日期
		if (null != endDate) {
			buf.append(" AND O.PAY_TIME<=? ");
			args.add(DateUtil.getEndDate(endDate));
		}
		
		// 构造店铺ID查询语句
		StringBuffer buffer = new StringBuffer(" IN(");
		for (Long pointId : pointIds) {
			buffer.append("?,");
			args.add(pointId);
		}
		buffer.setLength(buffer.length() - 1);
		buffer.append(")");
		String pointIdsSQL = buffer.toString();
		
		// 店铺ID
		buf.append(" AND O.POINT_NO IN (SELECT POINT_NO FROM T_POINT_PLACE WHERE ID ").append(pointIdsSQL).append(" AND STATE != ? ) ");
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
		
		buf.append(" AND O.STATE = ? ");
		args.add(Commons.ORDER_STATE_FINISH);
		
		Double amount = genericDao.findSingle(Double.class, buf.toString(), args.toArray());
		return amount == null ? 0 : amount;
	}
	
	/**
	 * 取得选择店铺的销售量
	 * @param date 指定日期
	 * @return 选择店铺的销售量
	 */
	private int getSalesVolume(Date startDate, Date endDate, List<Long> pointIds) {
		if (null == pointIds || pointIds.isEmpty())
			return 0;
		
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer(" SELECT COALESCE(SUM(OD.QTY), 0) FROM T_ORDER O ");
		buf.append(" LEFT JOIN T_ORDER_DETAIL OD ON O.CODE = OD.ORDER_NO ");
		buf.append(" WHERE 1 = 1 ");
		
		// 开始日期
		if (null != startDate) {
			buf.append(" AND O.PAY_TIME>=? ");
			args.add(DateUtil.getStartDate(startDate));
		}
		
		// 结束日期
		if (null != endDate) {
			buf.append(" AND O.PAY_TIME<=? ");
			args.add(DateUtil.getEndDate(endDate));
		}
		
		// 构造店铺ID查询语句
		StringBuffer buffer = new StringBuffer(" IN(");
		for (Long pointId : pointIds) {
			buffer.append("?,");
			args.add(pointId);
		}
		buffer.setLength(buffer.length() - 1);
		buffer.append(")");
		String pointIdsSQL = buffer.toString();
		
		// 店铺ID
		buf.append(" AND O.POINT_NO IN (SELECT POINT_NO FROM T_POINT_PLACE WHERE ID ").append(pointIdsSQL).append(" AND STATE != ? ) ");
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除

		buf.append(" AND O.STATE = ? ");
		args.add(Commons.ORDER_STATE_FINISH);
		
		Integer salesVolume = genericDao.findSingle(Integer.class, buf.toString(), args.toArray());
		return salesVolume == null ? 0 : salesVolume;
	}
	
	/**
	 * 取得选择店铺的购物车销售额
	 * 购物车：1、单个商品多件；2、多个商品
	 * @param date 指定日期
	 * @return 选择店铺的销售额
	 */
	private double getCartSalesAmount(Date startDate, Date endDate, List<Long> pointIds) {
		if (null == pointIds || pointIds.isEmpty())
			return 0;
		
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer(" SELECT COALESCE(SUM(OD.QTY * OD.PRICE), 0) FROM T_ORDER O ");
		buf.append(" LEFT JOIN T_ORDER_DETAIL OD ON O.CODE = OD.ORDER_NO ");
		buf.append(" WHERE 1 = 1 ");
		

		// 开始日期
		if (null != startDate) {
			buf.append(" AND O.PAY_TIME>=? ");
			args.add(DateUtil.getStartDate(startDate));
		}
		// 结束日期
		if (null != endDate) {
			buf.append(" AND O.PAY_TIME<=? ");
			args.add(DateUtil.getEndDate(endDate));
		}

		// 购物车订单
		buf.append(" AND O.CODE IN (SELECT O.CODE FROM T_ORDER O LEFT JOIN T_ORDER_DETAIL OD ON O.CODE = OD.ORDER_NO WHERE (OD.QTY > 1 OR O.CODE IN (SELECT ORDER_NO FROM T_ORDER_DETAIL GROUP BY ORDER_NO HAVING COUNT (ORDER_NO) >= 2)) GROUP BY O.CODE) ");
		
		// 构造店铺ID查询语句
		StringBuffer buffer = new StringBuffer(" IN(");
		for (Long pointId : pointIds) {
			buffer.append("?,");
			args.add(pointId);
		}
		buffer.setLength(buffer.length() - 1);
		buffer.append(")");
		String pointIdsSQL = buffer.toString();
		
		// 店铺ID
		buf.append(" AND O.POINT_NO IN (SELECT POINT_NO FROM T_POINT_PLACE WHERE ID ").append(pointIdsSQL).append(" AND STATE != ? ) ");
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
		
		buf.append(" AND O.STATE = ? ");
		args.add(Commons.ORDER_STATE_FINISH);
		
		Double amount = genericDao.findSingle(Double.class, buf.toString(), args.toArray());
		return amount == null ? 0 : amount;
	}
	
	/**
	 * 取得选择店铺的购物车销售量
	 * @param date 指定日期
	 * @return 选择店铺的销售量
	 */
	private int getCartSalesVolume(Date startDate, Date endDate, List<Long> pointIds) {
		if (null == pointIds || pointIds.isEmpty())
			return 0;
		
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer(" SELECT COALESCE(SUM(OD.QTY), 0) FROM T_ORDER O ");
		buf.append(" LEFT JOIN T_ORDER_DETAIL OD ON O.CODE = OD.ORDER_NO ");
		buf.append(" WHERE 1 = 1 ");
		
		// 开始日期
		if (null != startDate) {
			buf.append(" AND O.PAY_TIME>=? ");
			args.add(DateUtil.getStartDate(startDate));
		}
		// 结束日期
		if (null != endDate) {
			buf.append(" AND O.PAY_TIME<=? ");
			args.add(DateUtil.getEndDate(endDate));
		}

		// 购物车订单
		buf.append(" AND O.CODE IN (SELECT O.CODE FROM T_ORDER O LEFT JOIN T_ORDER_DETAIL OD ON O.CODE = OD.ORDER_NO WHERE (OD.QTY > 1 OR O.CODE IN (SELECT ORDER_NO FROM T_ORDER_DETAIL GROUP BY ORDER_NO HAVING COUNT (ORDER_NO) >= 2)) GROUP BY O.CODE) ");
		
		// 构造店铺ID查询语句
		StringBuffer buffer = new StringBuffer(" IN(");
		for (Long pointId : pointIds) {
			buffer.append("?,");
			args.add(pointId);
		}
		buffer.setLength(buffer.length() - 1);
		buffer.append(")");
		String pointIdsSQL = buffer.toString();
		
		// 店铺ID
		buf.append(" AND O.POINT_NO IN (SELECT POINT_NO FROM T_POINT_PLACE WHERE ID ").append(pointIdsSQL).append(" AND STATE != ? ) ");
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
		
		buf.append(" AND O.STATE = ? ");
		args.add(Commons.ORDER_STATE_FINISH);
		
		Integer salesVolume = genericDao.findSingle(Integer.class, buf.toString(), args.toArray());
		return salesVolume == null ? 0 : salesVolume;
	}
	
	/**
	 * 取得选择店铺的退款金额
	 * @param date 指定日期
	 * @return 选择店铺的退款金额
	 */
	private double getRefundAmount(Date startDate, Date endDate, List<Long> pointIds) {
		if (null == pointIds || pointIds.isEmpty())
			return 0;
		
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer(" SELECT COALESCE(SUM(R.FEE_REFUND), 0) FROM T_REFUND R ");
		buf.append(" LEFT JOIN T_ORDER O ON O.CODE = R.ORDER_NO ");
		buf.append(" WHERE 1 = 1 ");
		
		// 开始日期
		if (null != startDate) {
			buf.append(" AND O.PAY_TIME>=? ");
			args.add(DateUtil.getStartDate(startDate));
		}
		// 结束日期
		if (null != endDate) {
			buf.append(" AND O.PAY_TIME<=? ");
			args.add(DateUtil.getEndDate(endDate));
		}
		
		// 构造店铺ID查询语句
		StringBuffer buffer = new StringBuffer(" IN(");
		for (Long pointId : pointIds) {
			buffer.append("?,");
			args.add(pointId);
		}
		buffer.setLength(buffer.length() - 1);
		buffer.append(")");
		String pointIdsSQL = buffer.toString();
		
		// 店铺ID
		buf.append(" AND O.POINT_NO IN (SELECT POINT_NO FROM T_POINT_PLACE WHERE ID ").append(pointIdsSQL).append(" AND STATE != ? ) ");
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
		
		buf.append(" AND O.STATE = ? ");
		args.add(Commons.ORDER_STATE_FINISH);

		buf.append(" AND R.STATE = ? ");
		args.add(Commons.REFUND_STATE_SUCCESS);//退款成功
		
		Double amount = genericDao.findSingle(Double.class, buf.toString(), args.toArray());
		return amount == null ? 0 : amount;
	}
	
	/**
	 * 取得选择店铺的设备数量
	 * @return 选择店铺的设备数量
	 */
	private int getDeviceCount(List<Long> pointIds) {
		if (null == pointIds || pointIds.isEmpty())
			return 0;
		
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer(" SELECT COALESCE(COUNT(*), 0) FROM T_DEVICE ");
		buf.append(" WHERE 1 = 1 ");
		
		// 构造店铺ID查询语句
		StringBuffer buffer = new StringBuffer(" IN(");
		for (Long pointId : pointIds) {
			buffer.append("?,");
			args.add(pointId);
		}
		buffer.setLength(buffer.length() - 1);
		buffer.append(")");
		String pointIdsSQL = buffer.toString();
		
		// 店铺ID
		buf.append(" AND POINT_ID ").append(pointIdsSQL);
		
		Integer deviceCount = genericDao.findSingle(Integer.class, buf.toString(), args.toArray());
		return deviceCount == null ? 0 : deviceCount;
	}

	/**
	 * 取得【经营分析】销售数据分析
	 */
	@Override
	public Map<String, Object> findSalesData(PointPlace pointPlace, Date startDate, Date endDate) {
		Map<String, Object> map = new HashMap<String, Object>();
		// 构造店铺ID集合
		List<Long> pointIds = createPointIds(pointPlace);
		
		// 销售额
		double salesAmount = MathUtil.round(getSalesAmount(startDate, endDate, pointIds), 2);
		map.put("salesAmount", salesAmount);
		
		// 销售量
		int salesVolume = getSalesVolume(startDate, endDate, pointIds);
		map.put("salesVolume", salesVolume);

		// 平均单价
		double averagePrice = salesVolume == 0 ? 0 : MathUtil.round(MathUtil.div(salesAmount, salesVolume),  2);
		map.put("averagePrice", averagePrice);
		
		// 购物车销售额
		double cartSalesAmount = MathUtil.round(getCartSalesAmount(startDate, endDate, pointIds), 2);
		map.put("cartSalesAmount", cartSalesAmount);
		
		// 购物车销售量
		int cartSalesVolume = getCartSalesVolume(startDate, endDate, pointIds);
		map.put("cartSalesVolume", cartSalesVolume);
		
		// 购物车平均单价
		double cartAveragePrice = cartSalesVolume == 0 ? 0 : MathUtil.round(MathUtil.div(cartSalesAmount, cartSalesVolume),  2);
		map.put("cartAveragePrice", cartAveragePrice);
		
		// 购物车销售额占比
		double cartSalesRate = salesAmount == 0 ? 0.0 : MathUtil.div(MathUtil.mul(cartSalesAmount, 100), salesAmount);
		map.put("cartSalesRate", cartSalesRate);
		
		// 统计图表数据
		List<Order> orders = findOrderSalesData(pointIds, startDate, endDate);
		map.put("orders", orders);
		
		return map;
	}
	
	/**
	 * 【经营分析】取得‘销售统计图’中的图表数据
	 * @param pointIds
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public List<Order> findOrderSalesData(List<Long> pointIds, Date startDate, Date endDate) {
		if (null == pointIds || pointIds.isEmpty())
			return new ArrayList<Order>();
		
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer(" SELECT ");
		buf.append(" TO_CHAR(O.pay_time, ");
		if (compareSomeDay(startDate, endDate)) {
			buf.append(" 'HH24' ");
		} else {
			buf.append(" 'YYYY-MM-DD' ");
		}
		buf.append(" ) ");
		buf.append(" AS date, COALESCE (SUM(OD.qty*OD.price), 0) as salesAmount, COALESCE (SUM(OD.qty), 0) as salesVolume ");
		
		buf.append(" FROM T_ORDER O ");
		buf.append(" LEFT JOIN T_ORDER_DETAIL OD ON O.CODE = OD.ORDER_NO ");
		buf.append(" WHERE 1 = 1 ");
		
		// 构造店铺ID查询语句
		StringBuffer buffer = new StringBuffer(" IN(");
		for (Long pointId : pointIds) {
			buffer.append("?,");
			args.add(pointId);
		}
		buffer.setLength(buffer.length() - 1);
		buffer.append(")");
		String pointIdsSQL = buffer.toString();
		
		buf.append(" AND O.POINT_NO IN (SELECT POINT_NO FROM T_POINT_PLACE WHERE ID ").append(pointIdsSQL).append(" AND STATE != ? ) ");
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
		
		// 开始日期
		if (null != startDate) {
			buf.append(" AND O.PAY_TIME>=? ");
			args.add(DateUtil.getStartDate(startDate));
		}
		// 结束日期
		if (null != endDate) {
			buf.append(" AND O.PAY_TIME<=? ");
			args.add(DateUtil.getEndDate(endDate));
		}
		
		buf.append(" AND O.STATE = ? ");
		args.add(Commons.ORDER_STATE_FINISH);
		
		buf.append(" GROUP BY DATE ");
		buf.append(" ORDER BY DATE ");
		List<Order> orders = genericDao.findTs(Order.class, buf.toString(), args.toArray());
		
		for (Order order : orders)
			order.setAveragePrice(order.getSalesVolume() == 0 ? 0.0 : MathUtil.div(order.getSalesAmount(), order.getSalesVolume()));

		return orders;
	}
	
	/**
	 * 取得导出用【经营分析】销售数据分析
	 */
	@Override
	public List<Order> findExportSalesData(Page page, PointPlace pointPlace, Date startDate, Date endDate) {
		// 构造店铺ID集合
		List<Long> pointIds = createPointIds(pointPlace);
		
		if (null == pointIds || pointIds.isEmpty())
			return new ArrayList<Order>();
		
		// 构造店铺ID查询语句
		List<Object> pointIdArgs = new ArrayList<Object>();
		String pointIdsSQL = getPointIdsSql(pointIds, pointIdArgs);
		
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer(" SELECT T1.date, T1.prov,T1.city,T1.dist,T1.point_name as pointName,T1.point_address as pointAddress,T1.name as orgName, COALESCE(T1.salesAmount, 0) as salesAmount, COALESCE(t1.salesVolume, 0) as salesVolume, COALESCE(t2.cartSalesAmount, 0) as cartSalesAmount, COALESCE(t2.cartSalesVolume, 0) as cartSalesVolume ");

		// 销售额和销售量
		buf.append(" from ( ");
		buf.append(" SELECT TO_CHAR(O.pay_time, 'yyyy-MM-dd') as date , o.point_no, pp.prov,pp.city,pp.dist,pp.point_name,pp.point_address,org.name, COALESCE(sum(od.price*od.qty), 0) as salesAmount, COALESCE(sum(od.qty), 0) as salesVolume ");
		buf.append(" FROM T_ORDER O ");
		buf.append(" LEFT JOIN T_ORDER_DETAIL OD ON O.CODE = OD.ORDER_NO ");
		buf.append(" LEFT JOIN T_POINT_PLACE PP ON PP.POINT_NO = O.POINT_NO ");
		buf.append(" LEFT JOIN SYS_ORG ORG ON ORG.ID = PP.ORG_ID ");
		buf.append(" WHERE 1 = 1 ");
		buf.append(" AND O.POINT_NO IN (SELECT POINT_NO FROM T_POINT_PLACE WHERE ID ").append(pointIdsSQL).append(" AND STATE != ? ) ");
		args.addAll(pointIdArgs);
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
		if (null != startDate) {
			buf.append(" AND O.PAY_TIME>=? ");
			args.add(DateUtil.getStartDate(startDate));
		}
		if (null != endDate) {
			buf.append(" AND O.PAY_TIME<=? ");
			args.add(DateUtil.getEndDate(endDate));
		}
		buf.append(" GROUP BY date, o.point_no, pp.prov,pp.city,pp.dist,pp.point_name,pp.point_address,org.name ");
		buf.append(" order by date ");
		buf.append(" ) T1 ");

		// 购物车销售额和购物车销售量
		buf.append(" left join ( ");
		buf.append(" SELECT TO_CHAR(O.pay_time, 'yyyy-MM-dd') as date , o.point_no, pp.prov,pp.city,pp.dist,pp.point_name,pp.point_address,org.name, COALESCE(sum(od.price*od.qty), 0) as cartSalesAmount, COALESCE(sum(od.qty), 0) as cartSalesVolume ");
		buf.append(" FROM T_ORDER O ");
		buf.append(" LEFT JOIN T_ORDER_DETAIL OD ON O.CODE = OD.ORDER_NO ");
		buf.append(" LEFT JOIN T_POINT_PLACE PP ON PP.POINT_NO = O.POINT_NO ");
		buf.append(" LEFT JOIN SYS_ORG ORG ON ORG.ID = PP.ORG_ID ");
		buf.append(" WHERE 1 = 1 ");
		buf.append(" AND O.POINT_NO IN (SELECT POINT_NO FROM T_POINT_PLACE WHERE ID ").append(pointIdsSQL).append(" AND STATE != ? ) ");
		args.addAll(pointIdArgs);
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
		if (null != startDate) {
			buf.append(" AND O.PAY_TIME>=? ");
			args.add(DateUtil.getStartDate(startDate));
		}
		if (null != endDate) {
			buf.append(" AND O.PAY_TIME<=? ");
			args.add(DateUtil.getEndDate(endDate));
		}
		
		// 购物车订单
		buf.append(" AND O.CODE IN (SELECT O.CODE FROM T_ORDER O LEFT JOIN T_ORDER_DETAIL OD ON O.CODE = OD.ORDER_NO WHERE (OD.QTY > 1 OR O.CODE IN (SELECT ORDER_NO FROM T_ORDER_DETAIL GROUP BY ORDER_NO HAVING COUNT (ORDER_NO) >= 2)) GROUP BY O.CODE) ");
		
		buf.append(" AND O.STATE = ? ");
		args.add(Commons.ORDER_STATE_FINISH);
		buf.append(" GROUP BY date, o.point_no, pp.prov,pp.city,pp.dist,pp.point_name,pp.point_address,org.name ");
		buf.append(" order by date ");
		buf.append(" ) T2 on t1.date = t2.date and t1.point_no = t2.point_no ");
		
		List<Order> orders = genericDao.findTs(Order.class, page, buf.toString(), args.toArray());

		for (Order order : orders) {
			// 平均单价
			double averagePrice = order.getSalesVolume() == 0 ? 0 : MathUtil.round(MathUtil.div(order.getSalesAmount(), order.getSalesVolume()),  2);
			order.setAveragePrice(averagePrice);
			
			// 购物车平均单价
			double cartAveragePrice = order.getCartSalesVolume() == 0 ? 0 : MathUtil.round(MathUtil.div(order.getCartSalesAmount(), order.getCartSalesVolume()),  2);
			order.setCartAveragePrice(cartAveragePrice);
			
			// 购物车销售额占比
			double cartSalesRate = order.getSalesAmount() == 0 ? 0.0 : MathUtil.div(MathUtil.mul(order.getCartSalesAmount(), 100), order.getSalesAmount());
			order.setCartSalesRate(cartSalesRate);
		}
		
		return orders;
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
	 * 取得【经营分析】商品销售饼状图
	 */
	@Override
	public Map<String, Object> findProductSalesPieChartData(PointPlace pointPlace, Date startDate, Date endDate) {
		Map<String, Object> map = new HashMap<String, Object>();
		// 构造店铺ID集合
		List<Long> pointIds = createPointIds(pointPlace);
		
		// 商品销售额占比
		List<Order> salesAmountList = findProductSalesAmount(pointIds, startDate, endDate);
		map.put("salesAmountList", salesAmountList);

		// 商品销售量占比
		List<Order> salesVolumeList = findProductSalesVolume(pointIds, startDate, endDate);
		map.put("salesVolumeList", salesVolumeList);
		
		return map;
	}
	
	/**
	 * 取得【经营分析】商品销售饼状图-商品销售额占比
	 */
	public List<Order> findProductSalesAmount(List<Long> pointIds, Date startDate, Date endDate) {
		if (null == pointIds || pointIds.isEmpty())
			return new ArrayList<Order>();
		
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer(" SELECT od.sku_id, p.sku_name as productName, COALESCE(sum(OD.qty*OD.price), 0) as salesAmount, COALESCE (SUM(OD.qty), 0) as salesVolume ");
		buf.append(" FROM T_ORDER O ");
		buf.append(" LEFT JOIN T_ORDER_DETAIL OD ON O.CODE = OD.ORDER_NO ");
		buf.append(" LEFT JOIN T_PRODUCT P ON OD.SKU_ID = P.ID ");
		buf.append(" WHERE 1 = 1 ");
		
		// 构造店铺ID查询语句
		StringBuffer buffer = new StringBuffer(" IN(");
		for (Long pointId : pointIds) {
			buffer.append("?,");
			args.add(pointId);
		}
		buffer.setLength(buffer.length() - 1);
		buffer.append(")");
		String pointIdsSQL = buffer.toString();
		
		// 店铺ID
		buf.append(" AND O.POINT_NO IN (SELECT POINT_NO FROM T_POINT_PLACE WHERE ID ").append(pointIdsSQL).append(" AND STATE != ? ) ");
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
		
		// 开始日期
		if (null != startDate) {
			buf.append(" AND O.PAY_TIME>=? ");
			args.add(DateUtil.getStartDate(startDate));
		}
		// 结束日期
		if (null != endDate) {
			buf.append(" AND O.PAY_TIME<=? ");
			args.add(DateUtil.getEndDate(endDate));
		}
		
		buf.append(" AND O.STATE = ? ");
		args.add(Commons.ORDER_STATE_FINISH);

		buf.append(" AND P.ID IS NOT NULL ");
		
		buf.append(" GROUP BY OD.SKU_ID,P.SKU_NAME ");
		buf.append(" ORDER BY salesAmount DESC ");
		buf.append(" LIMIT 10 ");//前十

		List<Order> orders = genericDao.findTs(Order.class, buf.toString(), args.toArray());
		
		// 取得总销售额
		double totalAmount = getSalesAmount(startDate, endDate, pointIds);
		double tempAmount = 0.0;
		for (Order order : orders)
			tempAmount = MathUtil.add(tempAmount, order.getSalesAmount());
		
		double otherSalesAmount = MathUtil.round(MathUtil.sub(totalAmount, tempAmount), 2);
		if (totalAmount != 0 && !orders.isEmpty() && otherSalesAmount != 0) {
			Order ortherOrder = new Order();
			ortherOrder.setProductName("其他");
			ortherOrder.setSalesAmount(otherSalesAmount);
			orders.add(ortherOrder);
		}
		
		return orders;
	}
	
	/**
	 * 取得【经营分析】商品销售饼状图-商品销量占比
	 */
	public List<Order> findProductSalesVolume(List<Long> pointIds, Date startDate, Date endDate) {
		if (null == pointIds || pointIds.isEmpty())
			return new ArrayList<Order>();
		
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer(" SELECT od.sku_id, p.sku_name as productName, COALESCE(sum(OD.qty*OD.price), 0) as salesAmount, COALESCE (SUM(OD.qty), 0) as salesVolume ");
		buf.append(" FROM T_ORDER O ");
		buf.append(" LEFT JOIN T_ORDER_DETAIL OD ON O.CODE = OD.ORDER_NO ");
		buf.append(" LEFT JOIN T_PRODUCT P ON OD.SKU_ID = P.ID ");
		buf.append(" WHERE 1 = 1 ");
		
		// 构造店铺ID查询语句
		StringBuffer buffer = new StringBuffer(" IN(");
		for (Long pointId : pointIds) {
			buffer.append("?,");
			args.add(pointId);
		}
		buffer.setLength(buffer.length() - 1);
		buffer.append(")");
		String pointIdsSQL = buffer.toString();
		
		buf.append(" AND O.POINT_NO IN (SELECT POINT_NO FROM T_POINT_PLACE WHERE ID ").append(pointIdsSQL).append(" AND STATE != ? ) ");
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
		
		// 开始日期
		if (null != startDate) {
			buf.append(" AND O.PAY_TIME>=? ");
			args.add(DateUtil.getStartDate(startDate));
		}
		// 结束日期
		if (null != endDate) {
			buf.append(" AND O.PAY_TIME<=? ");
			args.add(DateUtil.getEndDate(endDate));
		}
		
		buf.append(" AND O.STATE = ? ");
		args.add(Commons.ORDER_STATE_FINISH);
		
		buf.append(" AND P.ID IS NOT NULL ");
		
		buf.append(" GROUP BY OD.SKU_ID,P.SKU_NAME ");
		buf.append(" ORDER BY salesVolume DESC ");
		buf.append(" LIMIT 10 ");//前十
		
		List<Order> orders = genericDao.findTs(Order.class, buf.toString(), args.toArray());
		
		// 取得总销售量
		int totalSalesVolume = getSalesVolume(startDate, endDate, pointIds);
		int tempSalesVolume = 0;
		for (Order order : orders)
			tempSalesVolume += order.getSalesVolume();
		 
		int otherSalesVolume = totalSalesVolume - tempSalesVolume;
		if (totalSalesVolume != 0 && !orders.isEmpty() && otherSalesVolume != 0) {
			Order ortherOrder = new Order();
			ortherOrder.setProductName("其他");
			ortherOrder.setSalesVolume(otherSalesVolume);
			orders.add(ortherOrder);
		}
		return orders;
	}
	
	/**
	 * 取得【经营分析】商品销售数据
	 */
	@Override
	public List<Order> findProductSalesData(Page page, PointPlace pointPlace, Date startDate, Date endDate) {
		if (null != page) {
			if ("salesRate".equals(page.getOrder())) //销售额占比排序
				page.setOrder("clinchDealOrder");
			else if ("salesVolumeRate".equals(page.getOrder())) //销售量占比排序
				page.setOrder("salesVolume");
		}
		
		// 构造店铺ID集合
		List<Long> pointIds = createPointIds(pointPlace);
		
		if (null == pointIds || pointIds.isEmpty())
			return genericDao.findTs(Order.class, page, "SELECT ID FROM T_ORDER WHERE ID = 0");
		
		// 构造店铺ID查询语句
		List<Object> pointIdArgs = new ArrayList<Object>();
		String pointIdsSQL = getPointIdsSql(pointIds, pointIdArgs);
		
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer(" SELECT p.sku_name as productName, COALESCE(t1.totalOrdersNumber, 0) as totalOrdersNumber, COALESCE(t1.totalOrderAmount, 0) as totalOrderAmount, COALESCE(t2.dealQuantity, 0) as dealQuantity, COALESCE(t2.clinchDealOrder, 0) as clinchDealOrder, COALESCE(t3.refundQty, 0) as refundQty, COALESCE(t3.refundAmount, 0) as refundAmount, COALESCE(t4.salesAmount, 0) as salesAmount, COALESCE(t4.salesVolume, 0) as salesVolume ");
		buf.append(" from t_product p ");
		
		// 总订单数和总订单金额
		buf.append(" right join ( ");
		buf.append(" SELECT od.sku_id, COALESCE(count(o.code), 0) as totalOrdersNumber, COALESCE(sum(od.price*od.qty), 0) as totalOrderAmount ");
		buf.append(" FROM T_ORDER O ");
		buf.append(" LEFT JOIN T_ORDER_DETAIL OD ON O.CODE = OD.ORDER_NO ");
		buf.append(" LEFT JOIN T_PRODUCT P ON OD.SKU_ID = P.ID ");
		buf.append(" WHERE 1 = 1 ");

		buf.append(" AND O.POINT_NO IN (SELECT POINT_NO FROM T_POINT_PLACE WHERE ID ").append(pointIdsSQL).append(" AND STATE != ? ) ");
		args.addAll(pointIdArgs);
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
		if (null != startDate) {
			buf.append(" AND O.PAY_TIME>=? ");
			args.add(DateUtil.getStartDate(startDate));
		}
		if (null != endDate) {
			buf.append(" AND O.PAY_TIME<=? ");
			args.add(DateUtil.getEndDate(endDate));
		}
		buf.append(" AND P.ID IS NOT NULL ");
		buf.append(" GROUP BY OD.SKU_ID ");
		buf.append(" ) T1 on p.id = t1.sku_id ");

		// 成交订单数和成交订单金额
		buf.append(" left join ( ");
		buf.append(" SELECT od.sku_id, COALESCE(count(o.code), 0) as dealQuantity, COALESCE(sum(od.price*od.qty), 0) as clinchDealOrder ");
		buf.append(" FROM T_ORDER O ");
		buf.append(" LEFT JOIN T_ORDER_DETAIL OD ON O.CODE = OD.ORDER_NO ");
		buf.append(" LEFT JOIN T_PRODUCT P ON OD.SKU_ID = P.ID ");
		buf.append(" WHERE 1 = 1 ");
		buf.append(" AND O.POINT_NO IN (SELECT POINT_NO FROM T_POINT_PLACE WHERE ID ").append(pointIdsSQL).append(" AND STATE != ? ) ");
		args.addAll(pointIdArgs);
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
		if (null != startDate) {
			buf.append(" AND O.PAY_TIME>=? ");
			args.add(DateUtil.getStartDate(startDate));
		}
		if (null != endDate) {
			buf.append(" AND O.PAY_TIME<=? ");
			args.add(DateUtil.getEndDate(endDate));
		}
		buf.append(" AND O.STATE = ? ");
		args.add(Commons.ORDER_STATE_FINISH);
		buf.append(" AND P.ID IS NOT NULL ");
		buf.append(" GROUP BY OD.SKU_ID ");
		buf.append(" ) T2 on p.id = t2.sku_id ");

		// 退款数量和退款金额
		buf.append(" left join ( ");
		buf.append(" select r.sku_id, COALESCE(sum(r.refund_qty), 0) as refundQty, COALESCE(sum(r.fee_refund), 0) as refundAmount ");
		buf.append(" from t_refund r ");
		buf.append(" left join t_order o on o.code = r.order_no ");
		buf.append(" WHERE 1 = 1 ");
		buf.append(" AND O.POINT_NO IN (SELECT POINT_NO FROM T_POINT_PLACE WHERE ID ").append(pointIdsSQL).append(" AND STATE != ? ) ");
		args.addAll(pointIdArgs);
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
		if (null != startDate) {
			buf.append(" AND O.PAY_TIME>=? ");
			args.add(DateUtil.getStartDate(startDate));
		}
		if (null != endDate) {
			buf.append(" AND O.PAY_TIME<=? ");
			args.add(DateUtil.getEndDate(endDate));
		}
		buf.append(" AND O.STATE = ? ");
		args.add(Commons.ORDER_STATE_FINISH);
		buf.append(" AND R.STATE = ? ");
		args.add(Commons.REFUND_STATE_SUCCESS);
		buf.append(" GROUP BY R.SKU_ID ");
		buf.append(" ) T3 on p.id = t3.sku_id ");

		// 销售额和销售量
		buf.append(" left join ( ");
		buf.append(" SELECT od.sku_id, COALESCE(sum(OD.qty*OD.price), 0) as salesAmount, COALESCE(SUM(OD.qty), 0) as salesVolume ");
		buf.append(" FROM T_ORDER O ");
		buf.append(" LEFT JOIN T_ORDER_DETAIL OD ON O.CODE = OD.ORDER_NO ");
		buf.append(" LEFT JOIN T_PRODUCT P ON OD.SKU_ID = P.ID ");
		buf.append(" WHERE 1 = 1 ");
		buf.append(" AND O.POINT_NO IN (SELECT POINT_NO FROM T_POINT_PLACE WHERE ID ").append(pointIdsSQL).append(" AND STATE != ? ) ");
		args.addAll(pointIdArgs);
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
		if (null != startDate) {
			buf.append(" AND O.PAY_TIME>=? ");
			args.add(DateUtil.getStartDate(startDate));
		}
		if (null != endDate) {
			buf.append(" AND O.PAY_TIME<=? ");
			args.add(DateUtil.getEndDate(endDate));
		}
		buf.append(" AND O.STATE = ? ");
		args.add(Commons.ORDER_STATE_FINISH);
		buf.append(" AND P.ID IS NOT NULL ");
		buf.append(" GROUP BY OD.SKU_ID ");
		buf.append(" ) T4 on p.id = t4.sku_id ");
		
		List<Order> orders = genericDao.findTs(Order.class, page, buf.toString(), args.toArray());
		
		// 取得总销售额
		double totalAmount = getSalesAmount(startDate, endDate, pointIds);
		// 取得总销售量
		int totalSalesVolume = getSalesVolume(startDate, endDate, pointIds);
		for (Order order : orders) {
			// 销售额占比=成交订单金额/总销售额
			double salesRate = totalAmount == 0 ? 0.0 : MathUtil.div(MathUtil.mul(order.getClinchDealOrder(), 100), totalAmount);
			order.setSalesRate(salesRate);
			// 销售量占比=销售量/总销售量
			double salesVolumeRate = totalSalesVolume == 0 ? 0.0 : MathUtil.div(MathUtil.mul(Double.valueOf(order.getSalesVolume()), 100), Double.valueOf(totalSalesVolume));
			order.setSalesVolumeRate(salesVolumeRate);
		}
		
		return orders;
	}
	
	/**
	 * 取得导出用【经营分析】商品销售数据
	 */
	@Override
	public List<Order> findExportProductSalesData(Page page, PointPlace pointPlace, Date startDate, Date endDate) {
		// 构造店铺ID集合
		List<Long> pointIds = createPointIds(pointPlace);
		
		if (null == pointIds || pointIds.isEmpty())
			return new ArrayList<Order>();
		
		// 构造店铺ID查询语句
		List<Object> pointIdArgs = new ArrayList<Object>();
		String pointIdsSQL = getPointIdsSql(pointIds, pointIdArgs);
		
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer(" SELECT t1.date, t1.point_no, t1.prov,t1.city,t1.dist,t1.point_name as pointName,t1.point_address as pointAddress,t1.name as orgName, ");
		buf.append(" p.sku_name as productName, COALESCE(t1.totalOrdersNumber, 0) as totalOrdersNumber, COALESCE(t1.totalOrderAmount, 0) as totalOrderAmount, COALESCE(t2.dealQuantity, 0) as dealQuantity, COALESCE(t2.clinchDealOrder, 0) as clinchDealOrder, COALESCE(t3.refundQty, 0) as refundQty, COALESCE(t3.refundAmount, 0) as refundAmount, COALESCE(t4.salesAmount, 0) as salesAmount, COALESCE(t4.salesVolume, 0) as salesVolume ");
		buf.append(" from t_product p ");
		
		// 总订单数和总订单金额
		buf.append(" right join ( ");
		buf.append(" SELECT TO_CHAR(O.pay_time, 'yyyy-MM-dd') as date , o.point_no, pp.prov,pp.city,pp.dist,pp.point_name,pp.point_address,org.name, ");
		buf.append(" od.sku_id, COALESCE(count(o.code), 0) as totalOrdersNumber, COALESCE(sum(od.price*od.qty), 0) as totalOrderAmount ");
		buf.append(" FROM T_ORDER O ");
		buf.append(" LEFT JOIN T_ORDER_DETAIL OD ON O.CODE = OD.ORDER_NO ");
		buf.append(" LEFT JOIN T_PRODUCT P ON OD.SKU_ID = P.ID ");
		buf.append(" LEFT JOIN T_POINT_PLACE PP ON PP.POINT_NO = O.POINT_NO ");
		buf.append(" LEFT JOIN SYS_ORG ORG ON ORG.ID = PP.ORG_ID ");
		buf.append(" WHERE 1 = 1 ");
		
		buf.append(" AND O.POINT_NO IN (SELECT POINT_NO FROM T_POINT_PLACE WHERE ID ").append(pointIdsSQL).append(" AND STATE != ? ) ");
		args.addAll(pointIdArgs);
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
		if (null != startDate) {
			buf.append(" AND O.PAY_TIME>=? ");
			args.add(DateUtil.getStartDate(startDate));
		}
		if (null != endDate) {
			buf.append(" AND O.PAY_TIME<=? ");
			args.add(DateUtil.getEndDate(endDate));
		}
		buf.append(" AND P.ID IS NOT NULL ");
		buf.append(" GROUP BY OD.SKU_ID,date, o.point_no, pp.prov,pp.city,pp.dist,pp.point_name,pp.point_address,org.name ");
		buf.append(" order by date ");
		buf.append(" ) T1 on p.id = t1.sku_id ");

		// 成交订单数和成交订单金额
		buf.append(" left join ( ");
		buf.append(" SELECT TO_CHAR(O.pay_time, 'yyyy-MM-dd') as date , o.point_no, pp.prov,pp.city,pp.dist,pp.point_name,pp.point_address,org.name, od.sku_id, COALESCE(count(o.code), 0) as dealQuantity, COALESCE(sum(od.price*od.qty), 0) as clinchDealOrder ");
		buf.append(" FROM T_ORDER O ");
		buf.append(" LEFT JOIN T_ORDER_DETAIL OD ON O.CODE = OD.ORDER_NO ");
		buf.append(" LEFT JOIN T_PRODUCT P ON OD.SKU_ID = P.ID ");
		buf.append(" LEFT JOIN T_POINT_PLACE PP ON PP.POINT_NO = O.POINT_NO ");
		buf.append(" LEFT JOIN SYS_ORG ORG ON ORG.ID = PP.ORG_ID ");
		buf.append(" WHERE 1 = 1 ");
		buf.append(" AND O.POINT_NO IN (SELECT POINT_NO FROM T_POINT_PLACE WHERE ID ").append(pointIdsSQL).append(" AND STATE != ? ) ");
		args.addAll(pointIdArgs);
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
		if (null != startDate) {
			buf.append(" AND O.PAY_TIME>=? ");
			args.add(DateUtil.getStartDate(startDate));
		}
		if (null != endDate) {
			buf.append(" AND O.PAY_TIME<=? ");
			args.add(DateUtil.getEndDate(endDate));
		}
		buf.append(" AND O.STATE = ? ");
		args.add(Commons.ORDER_STATE_FINISH);
		buf.append(" AND P.ID IS NOT NULL ");
		buf.append(" GROUP BY OD.SKU_ID,date,o.point_no, pp.prov,pp.city,pp.dist,pp.point_name,pp.point_address,org.name ");
		buf.append(" order by date ");
		buf.append(" ) T2 on p.id = t2.sku_id and t2.date = t1.date and t2.point_no = t1.point_no ");

		// 退款数量和退款金额
		buf.append(" left join ( ");
		buf.append(" select TO_CHAR(O.pay_time, 'yyyy-MM-dd') as date , o.point_no, pp.prov,pp.city,pp.dist,pp.point_name,pp.point_address,org.name, r.sku_id, COALESCE(sum(r.refund_qty), 0) as refundQty, COALESCE(sum(r.fee_refund), 0) as refundAmount ");
		buf.append(" from t_refund r ");
		buf.append(" left join t_order o on o.code = r.order_no ");
		buf.append(" LEFT JOIN T_POINT_PLACE PP ON PP.POINT_NO = O.POINT_NO ");
		buf.append(" LEFT JOIN SYS_ORG ORG ON ORG.ID = PP.ORG_ID ");
		buf.append(" WHERE 1 = 1 ");
		buf.append(" AND O.POINT_NO IN (SELECT POINT_NO FROM T_POINT_PLACE WHERE ID ").append(pointIdsSQL).append(" AND STATE != ? ) ");
		args.addAll(pointIdArgs);
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
		if (null != startDate) {
			buf.append(" AND O.PAY_TIME>=? ");
			args.add(DateUtil.getStartDate(startDate));
		}
		if (null != endDate) {
			buf.append(" AND O.PAY_TIME<=? ");
			args.add(DateUtil.getEndDate(endDate));
		}
		buf.append(" AND O.STATE = ? ");
		args.add(Commons.ORDER_STATE_FINISH);
		buf.append(" AND R.STATE = ? ");
		args.add(Commons.REFUND_STATE_SUCCESS);
		buf.append(" GROUP BY R.SKU_ID,date ,o.point_no, pp.prov,pp.city,pp.dist,pp.point_name,pp.point_address,org.name ");
		buf.append(" order by date ");
		buf.append(" ) T3 on p.id = t3.sku_id and t3.date = t1.date and t3.point_no = t1.point_no ");

		// 销售额和销售量
		buf.append(" left join ( ");
		buf.append(" SELECT TO_CHAR(O.pay_time, 'yyyy-MM-dd') as date , o.point_no, pp.prov,pp.city,pp.dist,pp.point_name,pp.point_address,org.name, od.sku_id, COALESCE(sum(OD.qty*OD.price), 0) as salesAmount, COALESCE(SUM(OD.qty), 0) as salesVolume ");
		buf.append(" FROM T_ORDER O ");
		buf.append(" LEFT JOIN T_ORDER_DETAIL OD ON O.CODE = OD.ORDER_NO ");
		buf.append(" LEFT JOIN T_PRODUCT P ON OD.SKU_ID = P.ID ");
		buf.append(" LEFT JOIN T_POINT_PLACE PP ON PP.POINT_NO = O.POINT_NO ");
		buf.append(" LEFT JOIN SYS_ORG ORG ON ORG.ID = PP.ORG_ID ");
		buf.append(" WHERE 1 = 1 ");
		buf.append(" AND O.POINT_NO IN (SELECT POINT_NO FROM T_POINT_PLACE WHERE ID ").append(pointIdsSQL).append(" AND STATE != ? ) ");
		args.addAll(pointIdArgs);
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
		if (null != startDate) {
			buf.append(" AND O.PAY_TIME>=? ");
			args.add(DateUtil.getStartDate(startDate));
		}
		if (null != endDate) {
			buf.append(" AND O.PAY_TIME<=? ");
			args.add(DateUtil.getEndDate(endDate));
		}
		buf.append(" AND O.STATE = ? ");
		args.add(Commons.ORDER_STATE_FINISH);
		buf.append(" AND P.ID IS NOT NULL ");
		buf.append(" GROUP BY OD.SKU_ID,date,o.point_no, pp.prov,pp.city,pp.dist,pp.point_name,pp.point_address,org.name ");
		buf.append(" order by date ");
		buf.append(" ) T4 on p.id = t4.sku_id and t4.date = t1.date and t4.point_no = t1.point_no ");
		
		buf.append(" order by t1.date ");
		List<Order> orders = genericDao.findTs(Order.class, page, buf.toString(), args.toArray());
		
		// 取得总销售额
		double totalAmount = getSalesAmount(startDate, endDate, pointIds);
		// 取得总销售量
		int totalSalesVolume = getSalesVolume(startDate, endDate, pointIds);
		for (Order order : orders) {
			// 销售额占比=成交订单金额/总销售额
			double salesRate = totalAmount == 0 ? 0.0 : MathUtil.div(MathUtil.mul(order.getClinchDealOrder(), 100), totalAmount);
			order.setSalesRate(salesRate);
			// 销售量占比=销售量/总销售量
			double salesVolumeRate = totalSalesVolume == 0 ? 0.0 : MathUtil.div(MathUtil.mul(Double.valueOf(order.getSalesVolume()), 100), Double.valueOf(totalSalesVolume));
			order.setSalesVolumeRate(salesVolumeRate);
		}
		
		return orders;
	}
	
	public String getPointIdsSql(List<Long> pointIds, List<Object> args) {
		StringBuffer buffer = new StringBuffer(" IN(");
		for (Long pointId : pointIds) {
			buffer.append("?,");
			args.add(pointId);
		}
		buffer.setLength(buffer.length() - 1);
		buffer.append(")");
		String pointIdsSQL = buffer.toString();
		
		return pointIdsSQL;
	}
	
	/**
	 * 查询【店铺库存】店铺信息
	 * @param page
	 * @param store
	 * @return
	 */
	@Override
	public List<PointPlace> findStockStores(Page page, PointPlace pointPlace) {
		StringBuffer buf = new StringBuffer("SELECT ");
		String cols = SQLUtils.getColumnsSQL(PointPlace.class, "C");
		buf.append(cols);
		buf.append(" ,SO.NAME AS orgName ");
		buf.append(" FROM T_POINT_PLACE C LEFT JOIN SYS_ORG SO ON C.ORG_ID=SO.ID WHERE C.ORG_ID=? ");
		List<Object> args = new ArrayList<Object>();;
		User user = ContextUtil.getUser(User.class);
		
		args.add(user.getOrgId());

		buf.append(" AND C.STATE != ? ");
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除

		if (pointPlace == null) {
			pointPlace = new PointPlace();
		}
		if (pointPlace.getPointNo() != null) {
			buf.append(" AND").append(" (C.POINT_NO LIKE ? OR C.POINT_NO LIKE ?)");
			args.add("%" + ZHConverter.convert(pointPlace.getPointNo(), ZHConverter.TRADITIONAL) + "%");
			args.add("%" + ZHConverter.convert(pointPlace.getPointNo(), ZHConverter.SIMPLIFIED) + "%");
		}
		if (pointPlace.getPointName() != null) {
			buf.append(" AND").append(" (C.POINT_NAME LIKE ? OR C.POINT_NAME LIKE ?)");
			args.add("%" + ZHConverter.convert(pointPlace.getPointName(), ZHConverter.TRADITIONAL) + "%");
			args.add("%" + ZHConverter.convert(pointPlace.getPointName(), ZHConverter.SIMPLIFIED) + "%");
		}
		if (pointPlace.getPointAddress() != null) {
			buf.append(" AND").append(" (C.POINT_ADDRESS LIKE ? OR C.POINT_ADDRESS LIKE ?)");
			args.add("%" + ZHConverter.convert(pointPlace.getPointAddress(), ZHConverter.TRADITIONAL) + "%");
			args.add("%" + ZHConverter.convert(pointPlace.getPointAddress(), ZHConverter.SIMPLIFIED) + "%");
		}
		if (pointPlace.getOrgName() != null) {
			buf.append(" AND").append(" (SO.NAME LIKE ? OR SO.NAME LIKE ?)");
			args.add("%" + ZHConverter.convert(pointPlace.getOrgName(), ZHConverter.TRADITIONAL) + "%");
			args.add("%" + ZHConverter.convert(pointPlace.getOrgName(), ZHConverter.SIMPLIFIED) + "%");
		}
		if (pointPlace.getPointType() != null) {
			buf.append(" AND C.POINT_TYPE = ? ");
			args.add(pointPlace.getPointType());
		}
		return genericDao.findTs(PointPlace.class, page, buf.toString(), args.toArray());
	}
	
	/**
	 * 查询【店铺库存】店铺商品补货信息
	 * @param page
	 * @return
	 */
	@Override
	public List<DeviceAisle> findReplenishProds(Page page, Long[] ids) {
		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("当前用户未登录");

		return findPhysicalProducts(ids, user.getOrgId(), null, page);
	}

	/**
	 * 查询上架商品列表
	 * @param page
	 * @param ids 店铺ID
	 * @param orgId 机构Id
	 * @param factoryDevNo 设备组号
	 * @return
	 */
	private List<DeviceAisle> findPhysicalProducts(Long[] ids, Long orgId, String factoryDevNo, Page page) {
		StringBuffer buffer = new StringBuffer();
		List<Object> args = new ArrayList<Object>();

        buffer.append(" SELECT STRING_AGG (B. ID || ',' || B. NAME || ',' || B. TYPE || ',' || B.REAL_PATH,';'ORDER BY B. ID DESC ) AS IMAGES,");
        buffer.append(" P.CODE AS productCode, P.SKU_NAME productName, P.ID productId, T.PRICE, T.PRICE_ON_LINE, T.SELLABLE, P.TYPE AS type, T.STOCK AS totalStock,");
        buffer.append("         T.totalSupplementNo, T.totalCapacity, COALESCE(T.SERIALNUMBER, A.SERIALNUMBER) as serialNumber, COALESCE(T.STICKTIME, A.STICKTIME) as stickTime, p.TRUE_OR_FALSE trueOrFalse");
        buffer.append(" FROM T_PRODUCT P");
        buffer.append(" LEFT JOIN(");
        buffer.append("         SELECT P.SKU_NAME,P.ID,DA.PRICE,DA.PRICE_ON_LINE,DA.SELLABLE,SUM(DA.STOCK) STOCK,SUM (DA.SUPPLEMENT_NO) totalSupplementNo,");
        buffer.append("         SUM (DA.CAPACITY) totalCapacity,SR.SERIAL_NUMBER SERIALNUMBER,SR.STICK_TIME STICKTIME");
        buffer.append(" FROM T_PRODUCT P");
        buffer.append(" LEFT JOIN T_DEVICE_AISLE DA ON DA.PRODUCT_ID = P . ID");
        buffer.append(" LEFT JOIN T_DEVICE D ON D. ID = DA.DEVICE_ID");
        buffer.append(" LEFT JOIN T_DEVICE_RELATION DR ON DR.DEV_NO = D.DEV_NO");
        buffer.append(" LEFT JOIN T_SORTING_RECOMMENDED SR ON SR.FACTORY_DEV_NO = DR.FACTORY_DEV_NO AND SR.POINT_ID = D.POINT_ID AND SR.PRODUCT_ID = P . ID");
        buffer.append(" WHERE 1=1");
		if (null != factoryDevNo){
            buffer.append(" AND DR.FACTORY_DEV_NO=?");
            args.add(factoryDevNo);
        }
        if(null != ids && ids.length > 0){
            StringBuffer buffer2 = new StringBuffer();
			List<Object> args2 = new ArrayList<Object>();
			buffer2.append(" ( ");
			for (Long id : ids) {
				buffer2.append("?,");
				args2.add(id);
			}
			buffer2.setLength(buffer2.length() - 1);
			buffer2.append(")");

			buffer.append(" AND D.POINT_ID IN ").append(buffer2.toString());
			args.addAll(args2);
        }

        buffer.append(" GROUP BY P.SKU_NAME,P.ID,DA.PRICE,DA.PRICE_ON_LINE,DA.SELLABLE,SR.SERIAL_NUMBER,SR.STICK_TIME");
	    buffer.append(" ) T ON T.ID=P.ID");
        buffer.append(" LEFT JOIN (");
        buffer.append(" SELECT P.SKU_NAME, P.ID, SR.SERIAL_NUMBER SERIALNUMBER, SR.STICK_TIME STICKTIME FROM T_PRODUCT P");
        buffer.append(" LEFT JOIN T_VIRTUAL_POINT VP ON VP.PRODUCT_ID=P.ID AND VP.ORG_ID=VP.ORG_ID");
        buffer.append(" LEFT JOIN T_SORTING_RECOMMENDED SR ON SR.FACTORY_DEV_NO=VP.FACTORY_DEV_NO AND SR.PRODUCT_ID=VP.PRODUCT_ID AND SR.ORG_ID=VP.ORG_ID");
        buffer.append(" WHERE 1=1");
        if(null != factoryDevNo){
            buffer.append(" AND VP.FACTORY_DEV_NO=?");
            args.add(factoryDevNo);
        }
        if(null != ids && ids.length > 0){
            StringBuffer buffer2 = new StringBuffer();
            List<Object> args2 = new ArrayList<Object>();
            buffer.append(" AND VP.DEVICE_ID IN(");
            buffer.append(" 	SELECT ID FROM T_DEVICE WHERE POINT_ID IN ( ");
            for (Long id : ids) {
                buffer2.append("?,");
                args2.add(id);
            }
            buffer2.setLength(buffer2.length() - 1);
            buffer2.append(")");

            buffer.append(buffer2.toString());
            args.addAll(args2);
            buffer.append(")");
        }
	    buffer.append(" GROUP BY P.SKU_NAME, P.ID, SR.SERIAL_NUMBER,SR.STICK_TIME");
	    buffer.append(" ) A ON A.ID=P.ID");
        buffer.append(" LEFT JOIN T_FILE B ON B.INFO_ID=P.ID AND B.TYPE=3");
        buffer.append(" WHERE P.ORG_ID=? AND P.STATE!=? AND P.CATEGORY!=?");
        args.add(orgId);
        args.add(Commons.PRODUCT_STATE_TRASH);
        args.add(Commons.PRODUCT_CATEGORY_TYPE);
        buffer.append(" AND (");
        buffer.append(" P.ID IN");
        buffer.append("  (");
        buffer.append("  SELECT P.ID FROM T_PRODUCT P");
        buffer.append("  LEFT JOIN T_DEVICE_AISLE DA ON DA.PRODUCT_ID = P . ID");
        buffer.append("  LEFT JOIN T_DEVICE D ON D. ID = DA.DEVICE_ID");
        buffer.append("  LEFT JOIN T_DEVICE_RELATION DR ON DR.DEV_NO = D.DEV_NO");
        buffer.append("  LEFT JOIN T_SORTING_RECOMMENDED SR ON SR.FACTORY_DEV_NO = DR.FACTORY_DEV_NO AND SR.POINT_ID = D.POINT_ID AND SR.PRODUCT_ID = P . ID");
        buffer.append("  WHERE 1=1");
        if(null != factoryDevNo){
            buffer.append(" AND DR.FACTORY_DEV_NO=?");
            args.add(factoryDevNo);
        }
        if(null != ids && ids.length > 0){
            StringBuffer buffer2 = new StringBuffer();
            List<Object> args2 = new ArrayList<Object>();
            buffer2.append(" ( ");
            for (Long id : ids) {
                buffer2.append("?,");
                args2.add(id);
            }
            buffer2.setLength(buffer2.length() - 1);
            buffer2.append(")");

            buffer.append(" AND D.POINT_ID IN ").append(buffer2.toString());
            args.addAll(args2);
        }
        buffer.append(" GROUP BY P.ID");
        buffer.append(" )");
        buffer.append(" OR P.ID IN");
        buffer.append("  (");
        buffer.append(" SELECT P.ID FROM T_PRODUCT P");
        buffer.append(" LEFT JOIN T_VIRTUAL_POINT VP ON VP.PRODUCT_ID=P.ID");
        buffer.append(" WHERE 1=1");
        if(null != factoryDevNo){
            buffer.append(" AND VP.FACTORY_DEV_NO=?");
            args.add(factoryDevNo);
        }
        if(null != ids && ids.length > 0){
            StringBuffer buffer2 = new StringBuffer();
            List<Object> args2 = new ArrayList<Object>();
            buffer.append(" AND VP.DEVICE_ID IN(");
            buffer.append(" 	SELECT ID FROM T_DEVICE WHERE POINT_ID IN ( ");
            for (Long id : ids) {
                buffer2.append("?,");
                args2.add(id);
            }
            buffer2.setLength(buffer2.length() - 1);
            buffer2.append(")");

            buffer.append(buffer2.toString());
            args.addAll(args2);
            buffer.append(" )");
        }
        buffer.append(" )");
        buffer.append(" )");
        buffer.append(" GROUP BY P.CODE,P.ID,P.SKU_NAME,T.PRICE,T.PRICE_ON_LINE,T.SELLABLE,P.TYPE,T.STOCK,T.TOTALSUPPLEMENTNO,T.TOTALCAPACITY,T.SERIALNUMBER,T.STICKTIME, p.TRUE_OR_FALSE, A.SERIALNUMBER,A.STICKTIME");
        buffer.append(" ORDER BY COALESCE(T.SERIALNUMBER, A.SERIALNUMBER)");




//		buffer.append("SELECT STRING_AGG(B.ID||','||B.NAME||','||B.TYPE||','||B.REAL_PATH, ';' ORDER BY B.ID DESC) AS images, ");
//		buffer.append(" P.CODE as productCode,P.SKU_NAME as productName,DA.PRODUCT_ID,DA.PRICE, DA.PRICE_ON_LINE, DA.SELLABLE, P.TYPE as type,SUM(DA.STOCK) as totalStock,SUM(DA.SUPPLEMENT_NO) as totalSupplementNo,SUM(DA.CAPACITY) as totalCapacity ");
//		buffer.append(" FROM T_DEVICE_AISLE DA ");
//		buffer.append(" LEFT JOIN T_DEVICE D ON D. ID = DA.DEVICE_ID ");
//		buffer.append(" LEFT JOIN T_PRODUCT P ON P.ID = DA.PRODUCT_ID AND D.ORG_ID = P.ORG_ID ");
//		buffer.append(" LEFT JOIN SYS_ORG O ON P.ORG_ID=O.ID LEFT JOIN T_FILE B ON P.ID=B.INFO_ID AND B.TYPE=? ");
//		buffer.append(" WHERE D.ORG_ID = ?  ");
//		args.add(orgId);
//		buffer.append(" AND P.STATE!=? ");
//		args.add(Commons.PRODUCT_STATE_TRASH);
//		if (null != factoryDevNo){
//            buffer.append(" AND DA.DEVICE_ID IN(SELECT ID FROM T_DEVICE WHERE DEV_NO=(SELECT DEV_NO FROM T_DEVICE_RELATION WHERE FACTORY_DEV_NO=?))");
//            args.add(factoryDevNo);
//        }
//		buffer.append(" AND D.POINT_ID != 0 ");
//		if (ids != null && ids.length > 0) {// 店铺ID
//			StringBuffer buffer2 = new StringBuffer();
//			List<Object> args2 = new ArrayList<Object>();
//			buffer2.append(" ( ");
//			for (Long id : ids) {
//				buffer2.append("?,");
//				args2.add(id);
//			}
//			buffer2.setLength(buffer2.length() - 1);
//			buffer2.append(")");
//
//			buffer.append(" AND D.POINT_ID IN ").append(buffer2.toString());
//			args.addAll(args2);
//		}
//		buffer.append(" AND DA.PRODUCT_ID IS NOT NULL ");
//		buffer.append(" GROUP BY ").append(" P.CODE,P.SKU_NAME,DA.PRODUCT_ID,DA.PRICE, DA.PRICE_ON_LINE, DA.SELLABLE, P.TYPE ");
//		args.add(0, Commons.FILE_PRODUCT);

		//设备商品的集合
		return genericDao.findTs(DeviceAisle.class, page, buffer.toString(), args.toArray());
	}

	/**
	 * 查询【店铺库存】店铺商品补货信息(列出所有货道的商品信息)
	 * @param page
	 * @return
	 */
	public List<DeviceAisle> findAisleReplenishProds(Page page, Long[] ids) {
		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("当前用户未登录");
		
		StringBuffer buffer = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		
		buffer.append(" SELECT DA.ID,DA.AISLE_NUM,DA.PRODUCT_ID,DA.PRICE, DA.SELLABLE, COALESCE(DA.PRICE_ON_LINE, 0) as PRICE_ON_LINE,DA.CAPACITY, DA.SUPPLEMENT_NO, T.productCode,COALESCE(T.productName, '无') as productName,T.TYPE,T.images, DR.FACTORY_DEV_NO as factoryDevNo ");
		buffer.append(" FROM T_DEVICE_AISLE DA ");
		buffer.append(" LEFT JOIN ( ");
		
		buffer.append(" SELECT A.ID, A.AISLE_NUM, A.PRODUCT_ID, A.PRICE, A.SELLABLE, A.PRICE_ON_LINE, A.CAPACITY, A.SUPPLEMENT_NO, P.CODE as productCode, P.SKU_NAME as productName, P.TYPE as type ");
		buffer.append(" , STRING_AGG(B.ID||','||B.NAME||','||B.TYPE||','||B.REAL_PATH, ';' ORDER BY B.ID DESC) AS images");
		buffer.append(" FROM T_DEVICE_AISLE A ");
		buffer.append(" LEFT JOIN T_DEVICE D ON D.ID = A.DEVICE_ID ");
		buffer.append(" LEFT JOIN T_PRODUCT P ON A.PRODUCT_ID = P.ID ");
		buffer.append(" LEFT JOIN SYS_ORG O ON P.ORG_ID=O.ID LEFT JOIN T_FILE B ON P.ID=B.INFO_ID AND (B.TYPE=?) ");
		buffer.append(" WHERE 1 = 1 ");
		
		buffer.append(" AND D.ORG_ID = ?  ");
		args.add(user.getOrgId());
		
		buffer.append(" AND P.STATE!=? ");
		args.add(Commons.PRODUCT_STATE_TRASH);
		
		buffer.append(" AND D.POINT_ID != 0 ");
		if (ids != null && ids.length > 0) {// 店铺ID
			StringBuffer buffer2 = new StringBuffer();
			List<Object> args2 = new ArrayList<Object>();
			buffer2.append(" ( ");
			for (Long id : ids) {
				buffer2.append("?,");
				args2.add(id);
			}
			buffer2.setLength(buffer2.length() - 1);
			buffer2.append(")");
			
			buffer.append(" AND D.POINT_ID IN ").append(buffer2.toString());
			args.addAll(args2);
		}
		
		buffer.append(" GROUP BY ").append(" A.ID , A.AISLE_NUM, A.PRODUCT_ID, A.PRICE, A.SELLABLE, A.PRICE_ON_LINE, A.CAPACITY, A.SUPPLEMENT_NO, P.CODE, P.SKU_NAME, P.TYPE ");
		args.add(0, Commons.FILE_PRODUCT);
		
		buffer.append(" ) T ON T.ID = DA.ID ");
		buffer.append(" LEFT JOIN T_DEVICE D ON D.ID = DA.DEVICE_ID ");
		buffer.append(" LEFT JOIN T_DEVICE_RELATION DR ON DR.DEV_NO = D.DEV_NO ");
		
		buffer.append(" WHERE 1 = 1 ");
		buffer.append(" AND D.ORG_ID = ?  ");
		args.add(user.getOrgId());
		
		buffer.append(" AND D.POINT_ID != 0 ");
		if (ids != null && ids.length > 0) {// 店铺ID
			StringBuffer buffer2 = new StringBuffer();
			List<Object> args2 = new ArrayList<Object>();
			buffer2.append(" ( ");
			for (Long id : ids) {
				buffer2.append("?,");
				args2.add(id);
			}
			buffer2.setLength(buffer2.length() - 1);
			buffer2.append(")");
			
			buffer.append(" AND D.POINT_ID IN ").append(buffer2.toString());
			args.addAll(args2);
		}
		
		buffer.append(" ORDER BY DR.FACTORY_DEV_NO, DA.AISLE_NUM ");
		
		return genericDao.findTs(DeviceAisle.class, buffer.toString(), args.toArray());
	}
	
	/**
	 * 查询【店铺库存】店铺补货清单信息
	 * @param page
	 * @return
	 */
	@Override
	public List<PointPlace> findStoreProds(Long[] ids) {
		if (ids == null || ids.length <= 0)
			throw new BusinessException("请指定需要导出的设备");

		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("当前用户未登录");

		List<PointPlace> pointPlaces = new ArrayList<PointPlace>();
		for (Long id : ids) {//店铺ID
			PointPlace pointPlace = findPointPlace(id, user.getOrgId());
			if (null == pointPlace)
				throw new BusinessException("店铺ID【" + id + "】不存在");
			
			// 查询该店铺下的商品补货信息
			Page page = new Page();
			page.setCurPage(1);
			page.setPageSize(20000);	//	最大查询2万条
			Long[] storeIds = new Long[] {id};
			List<DeviceAisle> deviceAisles = findAisleReplenishProds(page, storeIds);
			
			pointPlace.setDeviceAisles(deviceAisles);
			pointPlaces.add(pointPlace);
		}
		return pointPlaces;
	}
		
	public PointPlace findPointPlace(Long id, Long orgId) {
		StringBuffer buffer = new StringBuffer("SELECT ");
		buffer.append(SQLUtils.getColumnsSQL(PointPlace.class, "A"));
		buffer.append(" FROM T_POINT_PLACE A WHERE A.ID = ? AND A.ORG_ID = ? AND A.STATE != ? ");
		return genericDao.findT(PointPlace.class, buffer.toString(), id, orgId, Commons.POINT_PLACE_STATE_DELETE);
	}
	
	/**
	 * 【店铺库存】店铺一键改价
	 * @return
	 */
	public void saveStoresPrice(PointPlace pointPlace) {
		StringBuffer buffer = new StringBuffer();
		List<Object> args = new ArrayList<Object>();

		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("当前用户未登录");
		if (null == pointPlace || null == pointPlace.getId())
			throw new BusinessException("非法请求");
		
		Double discount = pointPlace.getDiscount();
		List<DeviceAisle> deviceAisles = pointPlace.getDeviceAisles();
		if (null != discount && discount <= 0)
			throw new BusinessException("请输入合法的折扣");
		if (null == deviceAisles || deviceAisles.isEmpty())
			throw new BusinessException("商品信息为空");
		
		// 更新t_point_place
		buffer.append(" UPDATE T_POINT_PLACE SET DISCOUNT = ? WHERE ORG_ID = ? AND ID = ? ");
		args.add(null == discount ? null : discount);
		args.add(user.getOrgId());
		args.add(pointPlace.getId());
		genericDao.execute(buffer.toString(), args.toArray());
		
		// 商品状态及价格信息
		List<ChangeProductStateProductData> list = new ArrayList<ChangeProductStateProductData>();
		for (DeviceAisle deviceAisle : deviceAisles) {
			if (null == deviceAisle.getProductId() || StringUtils.isEmpty(deviceAisle.getProductCode()) || null == deviceAisle.getPriceOnLine() || null == deviceAisle.getSellable())
				throw new BusinessException("商品信息不完整");
			if (Commons.SELLABLE_FALSE != deviceAisle.getSellable() && Commons.SELLABLE_TRUE != deviceAisle.getSellable())
				throw new BusinessException("可售状态有误");
			
			// 更新t_device_aisle
			buffer.setLength(0);
			args.clear();
			buffer.append(" UPDATE T_DEVICE_AISLE SET PRICE_ON_LINE = ?, SELLABLE = ? WHERE PRODUCT_ID = ? AND DEVICE_ID IN ( SELECT ID FROM T_DEVICE WHERE ORG_ID = ? AND POINT_ID = ? ) ");
			args.add(deviceAisle.getPriceOnLine());
			args.add(deviceAisle.getSellable());
			args.add(deviceAisle.getProductId());
			args.add(user.getOrgId());
			args.add(pointPlace.getId());
			genericDao.execute(buffer.toString(), args.toArray());
			
			// 构造商品状态及价格信息
			ChangeProductStateProductData productData = new ChangeProductStateProductData();
			productData.setProductNo(deviceAisle.getProductCode());
			productData.setState(deviceAisle.getSellable());
			
			Double discountValue = genericDao.findSingle(Double.class, "SELECT DISTINCT(DISCOUNT_VALUE) FROM T_DEVICE_AISLE WHERE DEVICE_ID IN (SELECT ID FROM T_DEVICE WHERE POINT_ID = ?) AND PRODUCT_ID = ?", pointPlace.getId(), deviceAisle.getProductId());
			discountValue = null == discountValue ? 1 : MathUtil.round(discountValue, 2);
			productData.setZhekou_num(discountValue);// 折扣值
			
			productData.setPrice(MathUtil.round(MathUtil.mul(deviceAisle.getPriceOnLine(), discountValue), 2));//折后价
			productData.setDeletePrice(deviceAisle.getPriceOnLine());//零售价
			
			list.add(productData);
		}

		// 将商品改价通知到指定店铺下的所有设备
		List<Object> devNos = findDeviceNosByPointId(pointPlace.getId());
		if (null != devNos && devNos.size() > 0) {
			String[] devNoArr = CommonUtil.convertToStringArr(devNos);
			// 通知各设备
			for(String devNo : devNoArr)
				pushChangeProductStateMessage(devNo, list);
		}
		
	}
	
	/**
	 * 根据店铺ID查询投放的设备编号
	 * @return
	 */
	public List<Object> findDeviceNosByPointId(Long pointId) {
		StringBuffer buffer = new StringBuffer("");
		List<Object> args = new ArrayList<Object>();
		
		buffer.append(" SELECT DISTINCT(DR.FACTORY_DEV_NO) ");
		buffer.append(" FROM T_DEVICE D ");
		buffer.append(" LEFT JOIN T_POINT_PLACE PP ON D.POINT_ID = PP.ID ");
		buffer.append(" LEFT JOIN T_DEVICE_RELATION DR ON DR.DEV_NO = D.DEV_NO ");
		buffer.append(" WHERE D.POINT_ID = ? ");
		args.add(pointId);
		buffer.append(" AND D.ORG_ID = ? ");
		args.add(ContextUtil.getUser(User.class).getOrgId());
		buffer.append(" AND D.BIND_STATE = ? ");
		args.add(Commons.BIND_STATE_SUCCESS);
		buffer.append(" AND PP.STATE != ? ");
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
		return genericDao.findListSingle(buffer.toString(), args.toArray());
	}

	/**
	 * 根据商品ID查询投放的设备编号
	 * @return
	 */
	public List<Object> findDeviceNosByProductId(Long productId) {
		StringBuffer buffer = new StringBuffer("");
		List<Object> args = new ArrayList<Object>();
		
		buffer.append(" SELECT DR.FACTORY_DEV_NO ");
		buffer.append(" FROM T_DEVICE D ");
		buffer.append(" LEFT JOIN T_DEVICE_AISLE DA ON D.ID = DA.DEVICE_ID ");
		buffer.append(" LEFT JOIN T_DEVICE_RELATION DR ON DR.DEV_NO = D.DEV_NO ");
		buffer.append(" WHERE DA.PRODUCT_ID = ? ");
		buffer.append(" AND D.ORG_ID = ? ");
		args.add(productId);
		args.add(ContextUtil.getUser(User.class).getOrgId());
		return genericDao.findListSingle(buffer.toString(), args.toArray());
	}
	
	/**
	 * 推送商品改价信息
	 */
	public void pushChangeProductStateMessage(String devNo, List<ChangeProductStateProductData> lists) {
		ChangeProductStateData data = new ChangeProductStateData();
		data.setNotifyFlag(Commons.NOTIFY_CHANGE_PRODUCT_STATE);// 商品状态变更通知flag（包括价格和商品状态）
		data.setTime(new Timestamp(System.currentTimeMillis()));
		
		List<List<ChangeProductStateProductData>> fatherlist = CommonUtil.fatherList(lists, 15);
		
		for (List<ChangeProductStateProductData> list : fatherlist) {
			
			OfflineMessage offlines = new OfflineMessage();
			data.setList(list);
			offlines.setOfflines(ContextUtil.getJson(data));
			offlines.setDevNos(devNo);//设备号
			genericDao.save(offlines);//保存离线时数据
			data.setMessageId(offlines.getId());
			offlines.setOfflines(ContextUtil.getJson(data));
			genericDao.update(offlines);//更新离线时数据字段messageId
			
			String json = ContextUtil.getJson(data);
			if(json != null && !"".equals(json) && json.length() > 1000){
				genericDao.delete(offlines);
				
				List<List<ChangeProductStateProductData>> fatherlists = CommonUtil.fatherList(list, 10);
				for (List<ChangeProductStateProductData> list2 : fatherlists) {
					OfflineMessage offline = new OfflineMessage();
					data.setList(list2);
					offline.setOfflines(ContextUtil.getJson(data));
					offline.setDevNos(devNo);//设备号
					genericDao.save(offline);//保存离线时数据
					data.setMessageId(offline.getId());
					offline.setOfflines(ContextUtil.getJson(data));
					genericDao.update(offline);//更新离线时数据字段messageId
					// 主动通知
					MessagePusher pusher = new MessagePusher();
					try {
						logger.info("【商品改价推送消息数据格式：设备编号:"+devNo+",["+ContextUtil.getJson(data)+"]】");
						pusher.pushMessageToAndroidDevices(Arrays.asList(devNo), ContextUtil.getJson(data), true);
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
						throw new BusinessException("商品改价通知失败！");
					}
				}
			}else{
				// 主动通知
				MessagePusher pusher = new MessagePusher();
				try {
					logger.info("【商品改价推送消息数据格式：设备编号:"+devNo+",["+ContextUtil.getJson(data)+"]】");
					pusher.pushMessageToAndroidDevices(Arrays.asList(devNo), ContextUtil.getJson(data), true);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					throw new BusinessException("商品改价通知失败！");
				}
			}
		}
	}
	
	/**
	 * 查询【店铺库存】店铺商品补货信息
	 * @param page
	 * @return
	 */
	public List<DeviceAisle> findAllProds(Page page, Product product) {
		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("当前用户未登录");
		
		StringBuffer buffer = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		
		buffer.append("SELECT STRING_AGG(B.ID||','||B.NAME||','||B.TYPE||','||B.REAL_PATH, ';' ORDER BY B.ID DESC) AS images, ");
		buffer.append(" P.CODE as productCode,P.SKU_NAME as productName,DA.PRODUCT_ID,DA.PRICE ");
		buffer.append(" FROM T_DEVICE_AISLE DA ");
		buffer.append(" LEFT JOIN T_DEVICE D ON D. ID = DA.DEVICE_ID ");
		buffer.append(" LEFT JOIN T_PRODUCT P ON P.ID = DA.PRODUCT_ID AND D.ORG_ID = P.ORG_ID ");
		buffer.append(" LEFT JOIN SYS_ORG O ON P.ORG_ID=O.ID LEFT JOIN T_FILE B ON P.ID=B.INFO_ID AND B.TYPE=? ");
		buffer.append(" WHERE D.ORG_ID = ?  ");
		args.add(user.getOrgId());
		
		buffer.append(" AND P.STATE!=? ");
		args.add(Commons.PRODUCT_STATE_TRASH);
		
		if (null != product) {
			if (!StringUtils.isEmpty(product.getSkuName())) {
				buffer.append(" AND (P.SKU_NAME LIKE ? OR P.SKU_NAME LIKE ?) ");
				args.add("%" + ZHConverter.convert(product.getSkuName(), ZHConverter.TRADITIONAL) + "%");
				args.add("%" + ZHConverter.convert(product.getSkuName(), ZHConverter.SIMPLIFIED) + "%");
			}
		}
		
		buffer.append(" AND D.POINT_ID != 0 ");
		buffer.append(" AND DA.PRODUCT_ID IS NOT NULL ");
		buffer.append(" GROUP BY ").append(" P.CODE,P.SKU_NAME,DA.PRODUCT_ID,DA.PRICE ");
		args.add(0, Commons.FILE_PRODUCT);
		
		return genericDao.findTs(DeviceAisle.class, page, buffer.toString(), args.toArray());
	}
	
	/**
	 * 【商品一键改价】查询商品所在的店铺信息
	 * @return
	 */
	public List<PointPlace> findStoresByProdId(Page page, Long[] finalProductIds) {
		StringBuffer buf = new StringBuffer("");
		List<Object> args = new ArrayList<Object>();
		List<Object> args2 = new ArrayList<Object>();
		
		if (finalProductIds != null && finalProductIds.length > 0) {// 商品ID
			buf.append(" ( ");
			for (Long id : finalProductIds) {
				buf.append("?,");
				args2.add(id);
			}
			buf.setLength(buf.length() - 1);
			buf.append(" ) ");
		}
		String inIds = buf.toString();
		buf.setLength(0);
		
		buf.append(" SELECT ");
		String cols = SQLUtils.getColumnsSQL(PointPlace.class, "C");
		buf.append(cols);
		buf.append(" ,SO.NAME AS orgName, DA.PRICE_ON_LINE AS priceOnLine, DA.SELLABLE AS sellable ");
		buf.append(" FROM T_POINT_PLACE C ");
		buf.append(" LEFT JOIN SYS_ORG SO ON C.ORG_ID=SO.ID ");
		buf.append(" LEFT JOIN T_DEVICE D ON D.POINT_ID = C.ID ");
		buf.append(" LEFT JOIN T_DEVICE_AISLE DA ON DA.DEVICE_ID = D.ID ");
		buf.append(" WHERE C.ORG_ID=? ");
		User user = ContextUtil.getUser(User.class);
		args.add(user.getOrgId());

		buf.append(" AND C.STATE != ? ");
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除

		if (finalProductIds != null && finalProductIds.length > 0) {
			buf.append(" AND DA.PRODUCT_ID IN ").append(inIds);
			args.addAll(args2);
		}
		
		buf.append(" GROUP BY ").append(cols).append(", SO.NAME, DA.PRICE_ON_LINE, DA.SELLABLE");
		return genericDao.findTs(PointPlace.class, page, buf.toString(), args.toArray());
	}
	
	/**
	 * 【店铺库存】商品一键改价
	 * @return
	 */
	public void saveProdsPrice(DeviceAisle deviceAisle) {
		StringBuffer buf = new StringBuffer("");

		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("当前用户未登录");
		
		if (null == deviceAisle.getProductId() || StringUtils.isEmpty(deviceAisle.getProductCode()))
			throw new BusinessException("商品信息不完整");
		
		List<PointPlace> pointPlaces = deviceAisle.getPointPlaces();
		if (null == pointPlaces || pointPlaces.isEmpty())
			throw new BusinessException("店铺信息为空");
		
		for (PointPlace pointPlace : pointPlaces) {
			if (null == pointPlace.getId() || null == pointPlace.getPriceOnLine() || pointPlace.getPriceOnLine() == 0 || null == pointPlace.getSellable())
				throw new BusinessException("店铺信息不完整");
			if (Commons.SELLABLE_FALSE != pointPlace.getSellable() && Commons.SELLABLE_TRUE != pointPlace.getSellable())
				throw new BusinessException("可售状态有误");

			// 更新t_device_aisle
			buf.setLength(0);
			buf.append(" UPDATE T_DEVICE_AISLE SET PRICE_ON_LINE = ?, SELLABLE = ? WHERE PRODUCT_ID = ? AND DEVICE_ID IN ( SELECT ID FROM T_DEVICE WHERE ORG_ID = ? AND POINT_ID = ? ) ");
			genericDao.execute(buf.toString(), pointPlace.getPriceOnLine(), pointPlace.getSellable(), deviceAisle.getProductId(), user.getOrgId(), pointPlace.getId());

			
			// 商品状态及价格信息
			List<ChangeProductStateProductData> list = new ArrayList<ChangeProductStateProductData>();
			// 构造商品状态及价格信息
			ChangeProductStateProductData productData = new ChangeProductStateProductData();
			productData.setProductNo(deviceAisle.getProductCode());
			productData.setState(pointPlace.getSellable());
			
			Double discountValue = genericDao.findSingle(Double.class, "SELECT DISTINCT(DISCOUNT_VALUE) FROM T_DEVICE_AISLE WHERE DEVICE_ID IN (SELECT ID FROM T_DEVICE WHERE POINT_ID = ?) AND PRODUCT_ID = ?", pointPlace.getId(), deviceAisle.getProductId());
			discountValue = null == discountValue ? 1 : MathUtil.round(discountValue, 2);
			productData.setZhekou_num(discountValue);// 折扣值
			
			productData.setPrice(MathUtil.round(MathUtil.mul(pointPlace.getPriceOnLine(), discountValue), 2));//折后价
			productData.setDeletePrice(pointPlace.getPriceOnLine());//零售价
			
			list.add(productData);
			
			// 将商品改价通知到指定店铺下的所有设备
			List<Object> devNos = findDeviceNosByPointId(pointPlace.getId());
			if (null != devNos && devNos.size() > 0) {
				String[] devNoArr = CommonUtil.convertToStringArr(devNos);
				// 通知各设备
				for(String devNo: devNoArr)
					pushChangeProductStateMessage(devNo, list);
			}
		}
		
	}
	
	/**
	 * 【店铺库存】根据店铺查询店铺下的货柜信息
	 */
	public List<Cabinet> findCabinetsByStoreId(Long storeId) {
		StringBuffer buf = new StringBuffer(" SELECT ");
		List<Object> args = new ArrayList<Object>();
		
		String cols = SQLUtils.getColumnsSQL(Cabinet.class, "C");
		buf.append(cols);
		buf.append(" , DR.FACTORY_DEV_NO AS FACTORYDEVNO, D.BIND_STATE AS BINDSTATE ");
		buf.append(" FROM T_CABINET C ");
		buf.append(" LEFT JOIN T_DEVICE D ON D.ID = C.DEVICE_ID ");
		buf.append(" LEFT JOIN T_DEVICE_RELATION DR ON D.DEV_NO = DR.DEV_NO ");
		buf.append(" WHERE D.ORG_ID=? ");
		User user = ContextUtil.getUser(User.class);
		args.add(user.getOrgId());
		
		buf.append(" AND D.POINT_ID != 0 ");
		buf.append(" AND D.POINT_ID IS NOT NULL ");

		buf.append(" AND D.POINT_ID = ? ");
		args.add(storeId);
		buf.append(" GROUP BY ").append(cols).append(", DR.FACTORY_DEV_NO, D.BIND_STATE");
		buf.append(" ORDER BY DR.FACTORY_DEV_NO, C.CABINET_NO ");
		return genericDao.findTs(Cabinet.class, buf.toString(), args.toArray());
	}
	
	/**
	 * 【店铺库存】查询货柜商品补货信息
	 * @return
	 */
	public List<DeviceAisle> findReplenishProdsByCabId(Page page, Long cabId) {
		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("当前用户未登录");
		
		StringBuffer buffer = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		
		buffer.append("SELECT STRING_AGG(B.ID||','||B.NAME||','||B.TYPE||','||B.REAL_PATH, ';' ORDER BY B.ID DESC) AS images, ");
		buffer.append(" P.CODE as productCode,P.SKU_NAME as productName,DA.PRODUCT_ID,DA.PRICE, DA.PRICE_ON_LINE, DA.SELLABLE, P.TYPE as type,SUM(DA.STOCK) as totalStock,SUM(DA.SUPPLEMENT_NO) as totalSupplementNo,SUM(DA.CAPACITY) as totalCapacity ");
		buffer.append(" FROM T_DEVICE_AISLE DA ");
		buffer.append(" LEFT JOIN T_DEVICE D ON D.ID = DA.DEVICE_ID ");
		buffer.append(" LEFT JOIN T_CABINET C ON C.ID = DA.CABINET_ID AND C.DEVICE_ID = D.ID ");
		buffer.append(" LEFT JOIN T_PRODUCT P ON P.ID = DA.PRODUCT_ID AND D.ORG_ID = P.ORG_ID ");
		buffer.append(" LEFT JOIN SYS_ORG O ON P.ORG_ID=O.ID LEFT JOIN T_FILE B ON P.ID=B.INFO_ID AND B.TYPE=? ");
		buffer.append(" WHERE D.ORG_ID = ?  ");
		args.add(user.getOrgId());
		
		buffer.append(" AND P.STATE!=? ");
		args.add(Commons.PRODUCT_STATE_TRASH);
		
		buffer.append(" AND D.POINT_ID != 0 ");
		buffer.append(" AND D.POINT_ID IS NOT NULL ");

		buffer.append(" AND C.ID = ? ");
		args.add(cabId);

		buffer.append(" AND DA.PRODUCT_ID IS NOT NULL ");
		buffer.append(" GROUP BY ").append(" P.CODE,P.SKU_NAME,DA.PRODUCT_ID,DA.PRICE, DA.PRICE_ON_LINE, DA.SELLABLE, P.TYPE ");
		args.add(0, Commons.FILE_PRODUCT);
		
		return genericDao.findTs(DeviceAisle.class, page, buffer.toString(), args.toArray());
	}

	/**
	 * @Title: 更新商品的排序序号
	 * @param cabId
	 *            货柜ID
	 * @param productId
	 *            商品ID
	 * @param serialNumber
	 *            排序序号
	 * @param stickType
	 *            (置顶状态:1置顶,2取消置顶)
	 * @return: void
	 */
	@Override
	public void updateDeviceAisleSerialNumber(Long cabId, Long productId, Integer serialNumber, Integer stickType) {
		logger.info("**********【更新商品的排序序号】 start**********");
		if (null == serialNumber && null == stickType)
			throw new BusinessException("非法请求");
		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("当前用户未登录");

		StringBuffer buf = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		if(null != serialNumber){//排序
			// 验证数据库中此设备上是否已存在此序号
			buf.append(" SELECT DA.ID");
			buf.append(" FROM T_DEVICE_AISLE DA");
			buf.append(" LEFT JOIN T_CABINET C ON C . ID = DA.CABINET_ID AND C .DEVICE_ID = DA.DEVICE_ID");
			buf.append(" LEFT JOIN T_PRODUCT P ON P . ID = DA.PRODUCT_ID");
			buf.append(" LEFT JOIN SYS_ORG O ON P .ORG_ID = O. ID");
			buf.append(" LEFT JOIN T_FILE B ON P . ID = B.INFO_ID AND B. TYPE =?");
			args.add(Commons.FILE_PRODUCT);
			buf.append(" WHERE P.ORG_ID=? AND P.STATE!=? AND C.ID=? AND P.ID=? AND DA.PRODUCT_ID IS NOT NULL");
			args.add(user.getOrgId());
			args.add(Commons.PRODUCT_STATE_TRASH);
			args.add(cabId);
			args.add(productId);
			buf.append(" AND DA.SERIAL_NUMBER=?");
			args.add(serialNumber);
			buf.append(" GROUP BY DA.ID");
			List<DeviceAisle> deviceAisleList = genericDao.findTs(DeviceAisle.class, buf.toString(), args.toArray());
			if (null != deviceAisleList && !deviceAisleList.isEmpty())
				throw new BusinessException("此序号已存在,请更换！");

			args.clear();
			buf.setLength(0);
			buf.append(" UPDATE T_DEVICE_AISLE SET SERIAL_NUMBER=? WHERE ID IN ( SELECT DA.ID FROM T_DEVICE_AISLE DA");
			args.add(serialNumber);
			buf.append(" LEFT JOIN T_DEVICE D ON D.ID=DA.DEVICE_ID ");
			buf.append(" LEFT JOIN T_CABINET C ON C.DEVICE_ID=D.ID AND C.ORG_ID=D.ORG_ID");
			buf.append(" LEFT JOIN T_PRODUCT P ON P.ID=DA.PRODUCT_ID AND P.ORG_ID=D.ORG_ID");
			buf.append(" WHERE P.ID=? AND D.ORG_ID=? AND C.ID=? AND P.STATE!=? AND DA.PRODUCT_ID IS NOT NULL");
			args.add(productId);
			args.add(user.getOrgId());
			args.add(cabId);
			args.add(Commons.PRODUCT_STATE_TRASH);
			buf.append(" )");
			try {
				genericDao.execute(buf.toString(), args.toArray());// 更新商品序号
				logger.info("**********【更新商品的排序序号】 成功**********");
			} catch (Exception e) {
				logger.info("**********【更新商品的排序序号】 失败**********");
				e.printStackTrace();
			}
		}
		if(null != stickType){//置顶

			buf.append(" UPDATE T_DEVICE_AISLE SET STICK_TIME=? WHERE ID =(SELECT DA.ID FROM T_DEVICE_AISLE DA");
			args.add(stickType==1?new Date(System.currentTimeMillis()):null);
			buf.append(" LEFT JOIN T_DEVICE D ON D.ID=DA.DEVICE_ID");
			buf.append(" LEFT JOIN T_CABINET C ON C.DEVICE_ID=D.ID AND C.ORG_ID=D.ORG_ID");
			buf.append(" LEFT JOIN T_PRODUCT P ON P.ID=DA.PRODUCT_ID AND P.ORG_ID=D.ORG_ID");
			buf.append(" WHERE P.ID=? AND D.ORG_ID=? AND C.ID=? AND P.STATE!=? AND DA.PRODUCT_ID IS NOT NULL");
			args.add(productId);
			args.add(user.getOrgId());
			args.add(cabId);
			args.add(Commons.PRODUCT_STATE_TRASH);
			buf.append(" )");
			try {
				genericDao.execute(buf.toString(), args.toArray());
				logger.info("**********【商品置顶】 成功**********");
			} catch (Exception e) {
				logger.info("**********【商品置顶】失败**********");
				e.printStackTrace();
			}
		}
		logger.info("**********【更新商品的排序序号】 end**********");
	}

	/**
	 * 【店铺库存】更新商品序号
	 * @param sortingRecommendeds
	 *
	 */
	@Override
	public void updateSortingRecommended(List<SortingRecommended> sortingRecommendeds) {
		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("当前用户未登录");
		if(null == sortingRecommendeds || sortingRecommendeds.isEmpty())
			throw new BusinessException("请求错误");
		Map<String, Integer> map = new HashMap<String, Integer>();
		for(SortingRecommended sortingRecommended : sortingRecommendeds){
			if(1 == sortingRecommended.getType()){
				if(map.containsKey("type")){//如果存在，增加1
					int qty=map.get("type");
					map.put("type",qty+1);
				}else{
					map.put("type",1);
				}
			}
		}
		if(null != map.get("type") &&  3 < map.get("type")){
			throw new BusinessException("推荐商品不能大于3个！");
		}

		//区分推荐和排序的
		List<SortingRecommended> sortingRecommendedList = genericDao.findTs(SortingRecommended.class, "SELECT * FROM T_SORTING_RECOMMENDED WHERE FACTORY_DEV_NO=? AND ORG_ID=?", sortingRecommendeds.get(0).getFactoryDevNo(), user.getOrgId());
		if (null != sortingRecommendedList && !sortingRecommendedList.isEmpty()){//有数据就删除在插入
			genericDao.execute("DELETE FROM T_SORTING_RECOMMENDED WHERE FACTORY_DEV_NO=? AND ORG_ID=?", sortingRecommendeds.get(0).getFactoryDevNo(), user.getOrgId());
		}
		//没有数据就插入数据
		StringBuffer buf = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		Long pointId = genericDao.findSingle(Long.class, "SELECT POINT_ID FROM T_DEVICE WHERE DEV_NO=(SELECT DEV_NO FROM T_DEVICE_RELATION WHERE FACTORY_DEV_NO=?)", sortingRecommendeds.get(0).getFactoryDevNo());

		buf.append("INSERT INTO T_SORTING_RECOMMENDED(PRODUCT_ID,POINT_ID,ORG_ID,FACTORY_DEV_NO,CREATE_TIME,SERIAL_NUMBER,STICK_TIME)VALUES");
		for(SortingRecommended sortingRecommended : sortingRecommendeds){
			buf.append("(?,?,?,?,?,?,?),");
			args.add(sortingRecommended.getProductId());
			args.add(pointId);
			args.add(user.getOrgId());
			args.add(sortingRecommended.getFactoryDevNo());
			args.add(new Date(System.currentTimeMillis()));
			args.add(sortingRecommended.getSerialNumber());
			if(1 == sortingRecommended.getType()){
				args.add(new Date(System.currentTimeMillis()));
			}else {
				args.add(null);
			}
		}
		buf.setLength(buf.length()-1);
		genericDao.execute(buf.toString(), args.toArray());


	}

	/**
	 * 根据组号查询本设备组上的商品(包括实物、虚拟商品)
	 * @param factoryDevNo
	 * @param page
	 * @return
	 */
	@Override
	public List<DeviceAisle> findProductByFactoryDevNo(String factoryDevNo) {
		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("当前用户未登录");
		if(null == factoryDevNo)
			throw new BusinessException("请求错误");
//        List<DeviceAisle> deviceAisleList = getDeviceAisles(factoryDevNo, user.getOrgId());
//        if (null == deviceAisleList) {
////			deviceAisleList = findPhysicalProducts(null, user.getOrgId(), factoryDevNo);
//			deviceAisleList = getDeviceAisles(factoryDevNo, user.getOrgId());
//			//获取本店铺上架虚拟商品集合
//			if (null != findVirtualPoints(null, user.getOrgId(), factoryDevNo) && !findVirtualPoints(null, user.getOrgId(), factoryDevNo).isEmpty()) {
//				deviceAisleList.addAll(findVirtualPoints(null, user.getOrgId(), factoryDevNo));
//			}
//		}
		return findPhysicalProducts(null, user.getOrgId(), factoryDevNo, null);
	}

	/**
	 * 	分页查询【订单信息】
	 * @param page	分页信息
	 * @param order	查询条件
	 * @param startDate	起始日期
	 * @param endDate	截止日期
	 * @return	订单信息
	 */
	@Override
	public List<Order> findOrders(Page page, Order order, Date startDate, Date endDate) {
		if (page != null) {
			if (StringUtils.isEmpty(page.getOrder())) {
				page.setOrder(" A.CREATE_TIME ");
				page.setDesc(true);
			}
		}
		
		StringBuffer buf = new StringBuffer("SELECT ");
		String columns = SQLUtils.getColumnsSQL(Order.class, "A") + ",O.NAME";
		buf.append(columns);
		buf.append(" AS orgName,PP.POINT_NAME as pointName, DR.FACTORY_DEV_NO AS factoryDevNo");
		buf.append(" FROM T_ORDER A ");
		buf.append(" LEFT JOIN SYS_ORG O ON A.ORG_ID=O.ID ");
		buf.append(" LEFT JOIN T_DEVICE D ON D.DEV_NO = A.DEVICE_NO ");
		buf.append(" LEFT JOIN T_DEVICE_RELATION DR ON A.DEVICE_NO = DR.DEV_NO ");
		buf.append(" LEFT JOIN T_POINT_PLACE PP ON PP.POINT_NO = A.POINT_NO ");
		buf.append(" WHERE 1=1 ");
		List<Object> args = new ArrayList<Object>();
		if (order.getOrgName() != null) {
			buf.append(" AND (O.NAME LIKE ? OR O.NAME LIKE ?)");
			args.add("%" + ZHConverter.convert(order.getOrgName(), ZHConverter.TRADITIONAL) + "%");
			args.add("%" + ZHConverter.convert(order.getOrgName(), ZHConverter.SIMPLIFIED) + "%");
		}
		if (order.getPointName() != null) {
			buf.append(" AND (PP.POINT_NAME LIKE ? OR PP.POINT_NAME LIKE ?)");
			args.add("%" + ZHConverter.convert(order.getPointName(), ZHConverter.TRADITIONAL) + "%");
			args.add("%" + ZHConverter.convert(order.getPointName(), ZHConverter.SIMPLIFIED) + "%");
		}
		if (order.getCode() != null) {
			buf.append(" AND (A.CODE LIKE ? OR A.CODE LIKE ?)");
			args.add("%" + ZHConverter.convert(order.getCode(), ZHConverter.TRADITIONAL) + "%");
			args.add("%" + ZHConverter.convert(order.getCode(), ZHConverter.SIMPLIFIED) + "%");
		}
		if (order.getPayCode() != null) {//交易单号
			buf.append(" AND (A.PAY_CODE LIKE ? OR A.PAY_CODE LIKE ?)");
			args.add("%" + ZHConverter.convert(order.getPayCode(), ZHConverter.TRADITIONAL) + "%");
			args.add("%" + ZHConverter.convert(order.getPayCode(), ZHConverter.SIMPLIFIED) + "%");
		}
		if (order.getState() != null) {//订单状态
			buf.append(" AND A.STATE = ? ");
			args.add(order.getState());
		}
		User user = ContextUtil.getUser(User.class);

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

				buf.append(" AND A.ORG_ID ").append(orgIdsSQL);
			}
		}

		buf.append(" AND PP.STATE != ? ");
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除

		if (startDate != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(startDate);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			Timestamp date = new Timestamp(cal.getTimeInMillis());
			buf.append(" AND A.PAY_TIME>=? ");
			args.add(date);
		}
		if (endDate != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(endDate);
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
			cal.set(Calendar.SECOND, 59);
			cal.set(Calendar.MILLISECOND, 999);
			Timestamp date = new Timestamp(cal.getTimeInMillis());
			buf.append(" AND A.PAY_TIME<=? ");
			args.add(date);
		}
		buf.append(" GROUP BY ").append(columns).append(",PP.POINT_NAME, DR.FACTORY_DEV_NO ");
		
		// 上面的sql查出来的已退款金额有误，因此将上面的结果作为子表继续查询
//		String innerSql = buf.toString();
//		buf.setLength(0);
//		String outColumns = SQLUtils.getColumnsSQL(Order.class, "T");
//		buf.append(" SELECT ").append(outColumns).append(", T.refundAmount,T.orgName,T.pointName,T.factoryDevNo ");
//		buf.append(" , STRING_AGG(B.SKU||','||B.SKU_NAME||','||B.CURRENCY||','||B.PRICE||','||B.QTY||','||COALESCE(B.SPEC, '')||','||COALESCE(B.ORDER_TYPE, 0)||','||COALESCE(ROUND(B.DISCOUNT::numeric, 2), 1), ';' ORDER BY B.ID) AS details ");
//		buf.append(" FROM ( ").append(innerSql).append(" ) T ");
//		buf.append(" LEFT JOIN (SELECT D.SKU,D.SKU_NAME,C.CURRENCY,C.PRICE,C.QTY,D.SPEC,C.ID,C.ORDER_NO,C.ORDER_TYPE,C.DISCOUNT FROM T_ORDER_DETAIL C LEFT JOIN T_PRODUCT D ON C.SKU_ID=D.ID) B ON T.CODE=B.ORDER_NO ");
//		buf.append(" GROUP BY ").append(outColumns).append(", T.refundAmount,T.orgName,T.pointName,T.factoryDevNo ");
		
		List<Order> orders = genericDao.findTs(Order.class, page, buf.toString(), args.toArray());
		
		for (Order orderDB : orders) {
			// 根据订单编号查询订单详情
			List<OrderDetail> orderDetails = findOrderDetailsByCode(orderDB.getCode());
			Double refundAmount = 0.0;
			for (OrderDetail orderDetail : orderDetails)
				refundAmount = MathUtil.add(refundAmount, orderDetail.getRefundAmount());
			
			orderDB.setOrderDetails(orderDetails);
			orderDB.setRefundAmount(MathUtil.round(refundAmount, 2));// 已退款金额
		}
		
		return orders;
	}
	
	/**
	 * 根据订单编号查询订单详情
	 * @param code
	 * @return
	 */
	public List<OrderDetail> findOrderDetailsByCode(String code) {
		StringBuffer buf = new StringBuffer("SELECT OD.SKU_ID,OD.QTY,OD.PRICE,OD.CURRENCY,OD.ORG_ID,OD.ORDER_NO,OD.DISCOUNT,OD.ORDER_TYPE, ");
		buf.append(" P.SKU AS SKU,P.SKU_NAME AS SKUNAME,P.SPEC AS SPEC, COALESCE(SUM(R.FEE_REFUND), 0) AS REFUNDAMOUNT, COALESCE(SUM(R.REFUND_QTY), 0) AS REFUNDQTY ");
		buf.append(" FROM T_ORDER_DETAIL OD ");
		buf.append(" LEFT JOIN T_ORDER O ON O.CODE = OD.ORDER_NO ");
		buf.append(" LEFT JOIN T_REFUND R ON OD.ORDER_NO = R.ORDER_NO AND OD.SKU_ID = R.SKU_ID AND R.STATE = ? ");
		buf.append(" LEFT JOIN T_PRODUCT P ON P.ID = OD.SKU_ID ");
		buf.append(" WHERE 1=1 ");
		List<Object> args = new ArrayList<Object>();
		args.add(Commons.REFUND_STATE_SUCCESS);//退款成功
		
		// 订单编号
		buf.append(" AND O.CODE = ? ");
		args.add(code);

		buf.append(" GROUP BY OD.SKU_ID,OD.QTY,OD.PRICE,OD.CURRENCY,OD.ORG_ID,OD.ORDER_NO,OD.DISCOUNT,OD.ORDER_TYPE, P.SKU,P.SKU_NAME,P.SPEC ");
		
		return genericDao.findTs(OrderDetail.class, buf.toString(), args.toArray());
	}
	
	/**
	 * 查询【订单退款明细信息】
	 * @param refund
	 * @return
	 */
	@Override
	public List<Refund> findRefundDetail(Refund refund) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buffer = new StringBuffer("SELECT ");
		buffer.append(SQLUtils.getColumnsSQL(Refund.class, "A"));
		buffer.append(" FROM T_REFUND A WHERE A.ORDER_NO=? ");
		args.add(refund.getOrderNo());
		
		buffer.append(" AND A.SKU_ID = ? ");
		args.add(refund.getSkuId());
		return genericDao.findTs(Refund.class, buffer.toString(), args.toArray());
	}

	/**
	 * 	分页查询导出用【订单信息】
	 * @param page	分页信息
	 * @param order	查询条件
	 * @param startDate	起始日期
	 * @param endDate	截止日期
	 * @return	订单信息
	 */
	@Override
	public List<OrderDetail> findExportOrders(Page page, Order order, Date startDate, Date endDate) {
		StringBuffer buf = new StringBuffer("SELECT ");
		String columns = SQLUtils.getColumnsSQL(OrderDetail.class, "OD") + ",O.NAME";
		buf.append(columns);
		buf.append(" AS ORGNAME, P.SKU_NAME AS SKUNAME, OD.QTY * OD.PRICE AS AMOUNT, A.PAY_TIME AS PAYTIME, A.PAY_CODE AS PAYCODE, A.USERNAME AS OPENID, PP.POINT_ADDRESS AS POINTADDRESS, DR.FACTORY_DEV_NO AS FACTORYDEVNO");
		buf.append(" , COALESCE(SUM(R.REFUND_QTY), 0) AS REFUNDQTY, COALESCE(SUM(R.FEE_REFUND), 0) AS REFUNDAMOUNT ");
		buf.append(" FROM T_ORDER_DETAIL OD ");
		buf.append(" LEFT JOIN T_ORDER A ON A.CODE = OD.ORDER_NO ");
		buf.append(" LEFT JOIN SYS_ORG O ON A.ORG_ID=O.ID ");
		buf.append(" LEFT JOIN T_DEVICE D ON D.DEV_NO = A.DEVICE_NO ");
		buf.append(" LEFT JOIN T_DEVICE_RELATION DR ON A.DEVICE_NO = DR.DEV_NO ");
		buf.append(" LEFT JOIN T_POINT_PLACE PP ON PP.POINT_NO = A.POINT_NO ");
		buf.append(" LEFT JOIN T_PRODUCT P ON P.ID = OD.SKU_ID ");
		buf.append(" LEFT JOIN T_REFUND R ON R.ORDER_NO = OD.ORDER_NO AND R.SKU_ID = OD.SKU_ID ");
		buf.append(" WHERE 1=1 ");
		List<Object> args = new ArrayList<Object>();
		if (order.getOrgName() != null) {
			buf.append(" AND (O.NAME LIKE ? OR O.NAME LIKE ?)");
			args.add("%" + ZHConverter.convert(order.getOrgName(), ZHConverter.TRADITIONAL) + "%");
			args.add("%" + ZHConverter.convert(order.getOrgName(), ZHConverter.SIMPLIFIED) + "%");
		}
		if (order.getPointName() != null) {
			buf.append(" AND (PP.POINT_NAME LIKE ? OR PP.POINT_NAME LIKE ?)");
			args.add("%" + ZHConverter.convert(order.getPointName(), ZHConverter.TRADITIONAL) + "%");
			args.add("%" + ZHConverter.convert(order.getPointName(), ZHConverter.SIMPLIFIED) + "%");
		}
		if (order.getCode() != null) {
			buf.append(" AND (A.CODE LIKE ? OR A.CODE LIKE ?)");
			args.add("%" + ZHConverter.convert(order.getCode(), ZHConverter.TRADITIONAL) + "%");
			args.add("%" + ZHConverter.convert(order.getCode(), ZHConverter.SIMPLIFIED) + "%");
		}
		if (order.getPayCode() != null) {//交易单号
			buf.append(" AND (A.PAY_CODE LIKE ? OR A.PAY_CODE LIKE ?)");
			args.add("%" + ZHConverter.convert(order.getPayCode(), ZHConverter.TRADITIONAL) + "%");
			args.add("%" + ZHConverter.convert(order.getPayCode(), ZHConverter.SIMPLIFIED) + "%");
		}
		
		//订单状态
		buf.append(" AND A.STATE = ? ");
		args.add(Commons.ORDER_STATE_FINISH);

		buf.append(" AND PP.STATE != ? ");
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除

		User user = ContextUtil.getUser(User.class);

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

				buf.append(" AND A.ORG_ID ").append(orgIdsSQL);
			}
		}
		
		if (startDate != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(startDate);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			Timestamp date = new Timestamp(cal.getTimeInMillis());
			buf.append(" AND A.PAY_TIME>=? ");
			args.add(date);
		}
		if (endDate != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(endDate);
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
			cal.set(Calendar.SECOND, 59);
			cal.set(Calendar.MILLISECOND, 999);
			Timestamp date = new Timestamp(cal.getTimeInMillis());
			buf.append(" AND A.PAY_TIME<=? ");
			args.add(date);
		}
		buf.append(" GROUP BY ").append(columns).append(" , P.SKU_NAME, A.PAY_TIME, A.PAY_CODE, A.USERNAME, PP.POINT_ADDRESS, DR.FACTORY_DEV_NO");
		
		return genericDao.findTs(OrderDetail.class, page, buf.toString(), args.toArray());
	}
	
	/**
	 * 保存退款信息
	 * @param refund	需要保存的退款信息
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void saveRefund(Refund refund, User user) throws Exception {
		if (null == refund) {
			logger.info("*****退款信息为空*****" );
			throw new BusinessException("退款信息为空");
		}
		
		logger.info("*****保存退款信息*********开始***" );
		saveRefundOrder(refund, user);// 保存退款信息
		logger.info("*****保存退款信息*********结束***" );
		
		if (Commons.PAY_TYPE_WX == refund.getType()) {//微信退款
			String refundStr = getWxRefundStr(refund);
			Map resultMap = GetWxOrderno.doXMLParse(refundStr);
			
			if ("FAIL".equals(resultMap.get("return_code"))) {
				String errorMsg = resultMap.keySet().contains("return_msg") ? resultMap.get("return_msg").toString() : "";
				logger.error("*****微信退款接口返回错误:*****" + errorMsg);
				throw new BusinessException(errorMsg);
			} else {
				if ("FAIL".equals(resultMap.get("result_code"))) {
					String errorMsg = resultMap.keySet().contains("err_code_des") ? resultMap.get("err_code_des").toString() : "";
					logger.error("*****微信退款接口返回错误:*****" + errorMsg);
					throw new BusinessException(errorMsg);
				}
			}

			String resultCode = null == resultMap.get("result_code") ? "" : resultMap.get("result_code").toString();
			if (!"SUCCESS".equals(resultCode))
				throw new BusinessException("*****微信退款提交业务失败*****");
				
			// 退款申请接收成功，结果通过退款查询接口查询
			refund.setState(Commons.REFUND_STATE_ING);//退款中
			genericDao.update(refund);
		} else if (Commons.PAY_TYPE_ALI == refund.getType()) {//支付宝退款
			// TODO
		}
	}
	
	/**
	 * 查询订单退款信息
	 * @param refund 退款查询参数信息
	 */
	@Override
	public List<Refund> findOrderRefund(Refund refund) {
		if (null == refund)
			throw new BusinessException("非法请求！");
		
		StringBuffer buf = new StringBuffer(" SELECT ");
		List<Object> args = new ArrayList<Object>();
		
		String cols = SQLUtils.getColumnsSQL(Refund.class, "C");
		buf.append(cols);
		buf.append(" FROM T_REFUND C ");
		buf.append(" WHERE 1=1 ");
		
		if (refund.getOrderNo() != null) {// 订单单号
			buf.append(" AND C.ORDER_NO = ? ");
			args.add(refund.getOrderNo());
		}
		if (refund.getSkuId() != null) {//商品ID
			buf.append(" AND C.SKU_ID = ? ");
			args.add(refund.getSkuId());
		}

		buf.append(" ORDER BY C.UPDATE_TIME DESC ");
		return genericDao.findTs(Refund.class, buf.toString(), args.toArray());
	}
	
	/**
	 * 更新微信退款状态
	 * @throws Exception 
	 */
	@SuppressWarnings("rawtypes")
	public void saveRefundStateJob() throws Exception {
		logger.info("*****【更新微信退款状态】定时任务***开始***");
		// 查询【退款中】的退款信息
		List<Refund> refunds = findRefunds(Commons.REFUND_STATE_ING);
		for (Refund refund : refunds) {
			String refundQueryStr = getWxRefundQueryStr(refund);//获取微信【查询】退款返回信息
			Map resultMap = GetWxOrderno.doXMLParse(refundQueryStr);
			if (refundQueryStr.indexOf("FAIL") != -1) {
				logger.error("*****微信退款接口返回错误:*****" + (resultMap.keySet().contains("return_msg") ? resultMap.get("return_msg") : ""));
				continue;
			}
			
			String result_code = resultMap.keySet().contains("result_code") ? resultMap.get("result_code").toString() : "";
			if (!"SUCCESS".equals(result_code)) {
				logger.error("*****退款申请接收失败，结果未通过退款查询接口查询*****");
				continue;
			}
			
			String refund_status_$n = resultMap.keySet().contains("refund_status_0") ? resultMap.get("refund_status_0").toString() : "";
			logger.info("***退款状态：***" + refund_status_$n);
			if ("SUCCESS".equals(refund_status_$n)) {//退款成功
				logger.info("***退款编号：【" + refund.getCode() + "】***退款成功*****");
				refund.setState(Commons.REFUND_STATE_SUCCESS);
				refund.setUpdateTime(new Timestamp(System.currentTimeMillis()));
				
				// 追加一条退款流水  TRADE_TYPE_REFUND
				saveRefundTradeFlow(refund.getOrgId(), refund.getFeeRefund());
				logger.info("***saveTradeFlow成功***");
			} else if ("FAIL".equals(refund_status_$n) || "NOTSURE".equals(refund_status_$n)) {//【退款失败】或【未确定，需要商户原退款单号重新发起】
				logger.info("***退款编号：【" + refund.getCode() + "】***【退款失败】或【未确定，需要商户原退款单号重新发起】*****");
				refund.setState(Commons.REFUND_STATE_FAILED);
			}
			genericDao.update(refund);//更新状态
		}
		logger.info("*****【更新微信退款状态】定时任务***结束***");
	}
	
	/**
	 * 保存退款交易流水信息
	 */
	public void saveRefundTradeFlow(Long orgId, Double amount) {
		// 从交易流水表中查询当前用户最近时间的流水
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer("SELECT ");
		String cols = SQLUtils.getColumnsSQL(TradeFlow.class, "C");
		buf.append(cols);
		buf.append(" FROM T_TRADE_FLOW C WHERE C.ORG_ID = ? AND C.TRADE_STATUS = ? ORDER BY C.TRADE_TIME DESC ");
		args.add(orgId);
		args.add(Commons.TRADE_STATUS_SUCCESS);
		TradeFlow tradeFlow = genericDao.findT(TradeFlow.class, buf.toString(), args.toArray());
		if (null == tradeFlow) {
			tradeFlow = createTradeFlow(orgId);
			if (null != tradeFlow && tradeFlow.getTradeAmount() > 0) {
				genericDao.save(tradeFlow);
				
				// 追加一条退款流水
				TradeFlow tFlow = new TradeFlow();
				tFlow.setOrgId(orgId);
				tFlow.setTradeType(Commons.TRADE_TYPE_REFUND);
				tFlow.setTradeAmount(amount);// 交易金额
				tFlow.setTradeTime(new Timestamp(System.currentTimeMillis()));
				tFlow.setBalance(MathUtil.round(MathUtil.sub(tradeFlow.getBalance(), amount), 2));
				tFlow.setTradeStatus(Commons.TRADE_STATUS_SUCCESS);
				genericDao.save(tFlow);
			}
		} else {// 之前有交易流水记录
			// 追加一条退款流水
			TradeFlow tFlow = new TradeFlow();
			tFlow.setOrgId(orgId);
			tFlow.setTradeType(Commons.TRADE_TYPE_REFUND);
			tFlow.setTradeAmount(amount);// 交易金额
			tFlow.setTradeTime(new Timestamp(System.currentTimeMillis()));
			tFlow.setBalance(MathUtil.round(MathUtil.sub(tradeFlow.getBalance(), amount), 2));
			tFlow.setTradeStatus(Commons.TRADE_STATUS_SUCCESS);
			genericDao.save(tFlow);
		}
	}
	
	/**
	 * 构建初始化交易流水信息
	 * 
	 * @param user 当前用户
	 * @return 初始化交易流水信息
	 */
	private TradeFlow createTradeFlow(Long orgId) {
		Timestamp curTime = new Timestamp(System.currentTimeMillis());
		// 取得总销售额(所有商品总销售额)
		User user = new User();
		user.setOrgId(orgId);
		double salesTotalAmount = getSalesAmount(user, null);

		TradeFlow tradeFlow = new TradeFlow();
		tradeFlow.setOrgId(orgId);
		tradeFlow.setTradeType(Commons.TRADE_TYPE_RECHARGE);
		tradeFlow.setTradeAmount(MathUtil.round(salesTotalAmount, 2));
		tradeFlow.setTradeTime(curTime);
		tradeFlow.setBalance(MathUtil.round(salesTotalAmount, 2));
		tradeFlow.setTradeStatus(Commons.TRADE_STATUS_SUCCESS);
		return tradeFlow;
	}
	
	/**
	 * 取得当前登录用户的销售额
	 * 
	 * @param user 当前登录用户
	 * @param date 指定日期
	 * @return 当前登录用户的销售额
	 */
	private double getSalesAmount(User user, Date date) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer(" SELECT COALESCE(SUM(O.AMOUNT), 0) FROM T_ORDER O WHERE 1 = 1 ");
		buf.append(" AND O.ORG_ID = ? ");
		args.add(user.getOrgId());

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

		Double amount = genericDao.findSingle(Double.class, buf.toString(), args.toArray());
		return amount == null ? 0 : amount;
	}
	
	/**
	 * 获取微信【查询】退款返回信息
	 */
	public String getWxRefundQueryStr(Refund refund) {
		// 1 参数
		// 商户号
		String mch_id = wechatMchId;
		// 随机字符串
		String nonce_str = getNonceStr();
		// 商户订单号
		String out_trade_no = refund.getOrderNo();

		SortedMap<String, String> packageParams = new TreeMap<String, String>();
		packageParams.put("appid", wechatAppId);
		packageParams.put("mch_id", mch_id);
		packageParams.put("nonce_str", nonce_str);
		packageParams.put("out_trade_no", out_trade_no);

		RequestHandler reqHandler = new RequestHandler(null, null);
		reqHandler.init(wechatAppId, wechatAppKey, wechatKey);

		String sign = reqHandler.createSign(packageParams);
		String xml = "<xml>" + "<appid>" + wechatAppId + "</appid>" + "<mch_id>" + mch_id + "</mch_id>" + "<nonce_str>" + nonce_str + "</nonce_str>" + "<sign>" + sign + "</sign>"
				+ "<out_trade_no>" + out_trade_no + "</out_trade_no>" + "</xml>";

		String refundQueryStr = httpAdapter.postData(refundQueryUrl, xml);
		logger.info("*******微信【查询】退款返回信息：***********" + refundQueryStr);
		return refundQueryStr;
	}
	
	/**
	 * 获取微信退款返回信息
	 * @throws Exception 
	 */
	public String getWxRefundStr(Refund refund) throws Exception {
		// 1 参数
		// 商户号
		String mch_id = wechatMchId;
		// 随机字符串
		String nonce_str = getNonceStr();
		// 商户订单号
		String out_trade_no = refund.getOrderNo();
		// 商户退款单号
		String out_refund_no = refund.getCode();
		// 订单总金额以分为单位，不带小数点
		String totalFee = getMoney(refund.getAmount() + "");
		// 退款金额以分为单位，不带小数点
		String refund_fee = getMoney(refund.getFeeRefund() + "");
		// 操作员帐号, 默认为商户号
		String op_user_id = wechatMchId;

		SortedMap<String, String> packageParams = new TreeMap<String, String>();
		packageParams.put("appid", wechatAppId);
		packageParams.put("mch_id", mch_id);
		packageParams.put("nonce_str", nonce_str);
		packageParams.put("out_trade_no", out_trade_no);
		packageParams.put("out_refund_no", out_refund_no);
		packageParams.put("total_fee", totalFee);
		packageParams.put("refund_fee", refund_fee);
		packageParams.put("op_user_id", op_user_id);

		RequestHandler reqHandler = new RequestHandler(null, null);
		reqHandler.init(wechatAppId, wechatAppKey, wechatKey);

		String sign = reqHandler.createSign(packageParams);
		String xml = "<xml>" + "<appid>" + wechatAppId + "</appid>" + "<mch_id>" + mch_id + "</mch_id>" + "<nonce_str>" + nonce_str + "</nonce_str>" + "<sign>" + sign + "</sign>"
				+ "<out_trade_no>" + out_trade_no + "</out_trade_no>" + "<out_refund_no>" + out_refund_no + "</out_refund_no>" + "<total_fee>" + totalFee
				+ "</total_fee>" + "<refund_fee>" + refund_fee + "</refund_fee>" + "<op_user_id>" + op_user_id + "</op_user_id>" + "</xml>";
		
		// 证书验证+退款
		String refundStr = ClientCustomSSL.doRefund(getSrcFilePath("config/wechat", "apiclient_cert.p12"), wechatMchId, refundUrl, xml);

//		String refundStr = httpAdapter.postData(refundUrl, xml);
		logger.info("*******微信退款返回信息：***********" + refundStr);
		return refundStr;
	}
	
	public String getSrcFilePath(String dir, String fileName) {
		return this.getClass().getResource("/").getPath() + dir + File.separator + fileName;
	}
	
	/**
	 * 获取随机字符串
	 * 
	 * @return
	 */
	public String getNonceStr() {
		// 随机数
		String currTime = TenpayUtil.getCurrTime();
		// 8位日期
		String strTime = currTime.substring(8, currTime.length());
		// 四位随机数
		String strRandom = TenpayUtil.buildRandom(4) + "";
		// 10位序列号,可以自行调整。
		return strTime + strRandom;
	}

	/**
	 * 元转换成分
	 * 
	 * @param money
	 * @return
	 */
	public String getMoney(String amount) {
		if (amount == null) {
			return "";
		}
		// 金额转化为分为单位
		String currency = amount.replaceAll("\\$|\\￥|\\,", ""); // 处理包含, ￥ 或者$的金额
		int index = currency.indexOf(".");
		int length = currency.length();
		Long amLong = 0l;
		if (index == -1) {
			amLong = Long.valueOf(currency + "00");
		} else if (length - index >= 3) {
			amLong = Long.valueOf((currency.substring(0, index + 3)).replace(".", ""));
		} else if (length - index == 2) {
			amLong = Long.valueOf((currency.substring(0, index + 2)).replace(".", "") + 0);
		} else {
			amLong = Long.valueOf((currency.substring(0, index + 1)).replace(".", "") + "00");
		}
		return amLong.toString();
	}
	
	public void saveRefundOrder(Refund refund, User user) {
		Order oldOrder = findOrderByOrderNo(refund.getOrderNo(), Commons.ORDER_STATE_FINISH);
		if (null == oldOrder) {
			logger.info("*****订单信息不存在*****");			
			throw new BusinessException("订单信息不存在");
		}
		
		logger.info("*****refund.getSkuId()：*****：" + refund.getSkuId());
		OrderDetail orderDetail = findOrderDetailBySkuId(oldOrder.getCode(), refund.getSkuId());
		if (null == orderDetail) {
			logger.info("*****订单详情信息不存在*****");
			throw new BusinessException("订单详情信息不存在");
		}
		
		// 抽奖活动订单（传过来的skuId是抽奖活动商品的skuId，不是真实出货的商品skuId）并且是自动退款（注：后台手动退款的话，refund.getProductNo()值为空）
		if (Commons.ORDER_TYPE_LOTTERY == orderDetail.getOrderType().intValue() && !StringUtils.isEmpty(refund.getProductNo())) {
			// 根据真实商品编码查出skuId
			Product product = findProductByCode(refund.getProductNo(), oldOrder.getOrgId());
			if (null == product) {
				logger.info("*****真实商品信息不存在*****");
				throw new BusinessException("真实商品信息不存在");
			}
			
			refund.setSkuId(product.getId());// 将抽奖商品的skuId重置为真实的skuId
		}
		
		logger.info("*****refund.getRefundQty()：*****：" + refund.getRefundQty());
		if (refund.getRefundQty() <= 0) {
			logger.info("*****退货数量需大于0*****");
			throw new BusinessException("退货数量需大于0");
		}
		
		// 退货数量 <= 该商品订单总数 - 退款申请数（注：退款失败除外）
		// 取得该订单该商品退款申请数（注：退款失败除外）
		Integer applyRefundQty = genericDao.findSingle(Integer.class, "SELECT COALESCE(SUM(REFUND_QTY),0) FROM T_REFUND WHERE ORDER_NO = ? AND SKU_ID = ? AND STATE != ?", refund.getOrderNo(), refund.getSkuId(), Commons.REFUND_STATE_FAILED);
		applyRefundQty = null == applyRefundQty ? 0 : applyRefundQty;
		
		if (refund.getRefundQty() > (orderDetail.getQty() - applyRefundQty)) {
			logger.info("*****退货数量大于可申请退款数量*****");
			throw new BusinessException("退货数量大于可申请退款数量");
		}
		
		// 申请的退款金额
		Double feeRefund =  MathUtil.round(MathUtil.mul(orderDetail.getPrice(), Double.valueOf(refund.getRefundQty())), 2);
		refund.setFeeRefund(feeRefund);
		logger.info("*****申请的退款金额：*****：" + feeRefund);
		
		// 取得该订单该商品已申请退款金额
		Double refundAmount = genericDao.findSingle(Double.class, "SELECT COALESCE(SUM(FEE_REFUND),0) FROM T_REFUND WHERE ORDER_NO = ? AND SKU_ID = ? AND STATE != ?", refund.getOrderNo(), refund.getSkuId(), Commons.REFUND_STATE_FAILED);
		refundAmount = MathUtil.round(null == refundAmount ? 0 : refundAmount, 2);
		
		logger.info("*****取得该订单该商品已申请退款金额：*****：" + refundAmount);
		
		if (refund.getFeeRefund().doubleValue() > MathUtil.sub(oldOrder.getAmount(), refundAmount)) {
			logger.info("*****refund.getFeeRefund().doubleValue()：*****：" + refund.getFeeRefund().doubleValue());
			logger.info("*****MathUtil.sub(oldOrder.getAmount(), null == refundAmount ? 0 : refundAmount)：*****：" + MathUtil.sub(oldOrder.getAmount(), null == refundAmount ? 0 : refundAmount));
			
			logger.info("*****超出最大可退款金额！*****");
			throw new BusinessException("超出最大可退款金额！");
		}
		
		// 取出账户余额（可提现金额）
		user.setOrgId(oldOrder.getOrgId());//取出的是该笔订单所属人的账户余额
		double withdrawalAmount = saveWithdrawalAmount(user);
		logger.info("取出账户余额（可提现金额）:" + withdrawalAmount);
		BigDecimal bdWithdrawalAmount = new BigDecimal(withdrawalAmount);
		logger.info("refund.getFeeRefund():" + refund.getFeeRefund());
		logger.info("bdWithdrawalAmount:" + MathUtil.round(bdWithdrawalAmount.doubleValue(), 2));
		if (new BigDecimal(refund.getFeeRefund()).compareTo(bdWithdrawalAmount) == 1)
			throw new BusinessException("对不起，当前账户余额不足！");
		
		refund.setPayNo(oldOrder.getPayCode());
		refund.setAmount(oldOrder.getAmount());
		refund.setOrderNo(oldOrder.getCode());
		refund.setType(oldOrder.getPayType());// 6:微信
		refund.setState(Commons.REFUND_STATE_NEW);
		refund.setOrgId(oldOrder.getOrgId());// 退款是该笔订单所属人的退款（退款是由平台来操作的）
		refund.setCode(idWorker.nextCode());
		refund.setCreateUser(user.getId());
		refund.setCreateTime(new Timestamp(System.currentTimeMillis()));
		
		genericDao.save(refund);
	}
	
	public OrderDetail findOrderDetailBySkuId(String code, Long skuId) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buffer = new StringBuffer("SELECT ");
		buffer.append(SQLUtils.getColumnsSQL(OrderDetail.class, "A"));
		buffer.append(" FROM T_ORDER_DETAIL A WHERE A.ORDER_NO=? ");
		args.add(code);
		
		buffer.append(" AND A.SKU_ID = ? ");
		args.add(skuId);
		return genericDao.findT(OrderDetail.class, buffer.toString(), args.toArray());
	}

	public Product findProductByCode(String code, Long orgId) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buffer = new StringBuffer("SELECT ");
		buffer.append(SQLUtils.getColumnsSQL(Product.class, "A"));
		buffer.append(" FROM T_PRODUCT A WHERE A.CODE=? ");
		args.add(code);
		
		buffer.append(" AND A.ORG_ID = ? ");
		args.add(orgId);
		return genericDao.findT(Product.class, buffer.toString(), args.toArray());
	}
	
	/**
	 * 取得当前用户的可提现金额
	 * @param user 当前用户
	 * @return 当前用户的可提现金额
	 */
	private double saveWithdrawalAmount(User user) {
		// 从交易流水表中查询当前用户最近时间的流水，若没有，则新增一条流水记录，交易类型为充值；
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer("SELECT ");
		String cols = SQLUtils.getColumnsSQL(TradeFlow.class, "C");
		buf.append(cols);
		buf.append(" FROM T_TRADE_FLOW C WHERE C.ORG_ID = ? AND C.TRADE_STATUS = ? ORDER BY C.TRADE_TIME DESC ");
		args.add(user.getOrgId());
		args.add(Commons.TRADE_STATUS_SUCCESS);
		TradeFlow tradeFlow = genericDao.findT(TradeFlow.class, buf.toString(), args.toArray());
		if (null == tradeFlow) {
			tradeFlow = createTradeFlow(user);
			if (null != tradeFlow && tradeFlow.getTradeAmount() > 0)
				genericDao.save(tradeFlow);
		}
		return MathUtil.round(tradeFlow.getBalance(), 2);
	}
	
	/**
	 * 构建初始化充值交易流水信息
	 * @param user 当前用户
	 * @return 初始化交易流水信息
	 */
	private TradeFlow createTradeFlow(User user) {
		Timestamp curTime = new Timestamp(System.currentTimeMillis());
		// 取得总销售额(所有商品总销售额)
		double salesTotalAmount = getSalesAmount(user, null);
		
		TradeFlow tradeFlow = new TradeFlow();
		tradeFlow.setOrgId(user.getOrgId());
		tradeFlow.setUserId(user.getId());
		tradeFlow.setTradeType(Commons.TRADE_TYPE_RECHARGE);
		tradeFlow.setTradeAmount(MathUtil.round(salesTotalAmount, 2));
		tradeFlow.setTradeTime(curTime);
		tradeFlow.setBalance(MathUtil.round(salesTotalAmount, 2));
		tradeFlow.setTradeStatus(Commons.TRADE_STATUS_SUCCESS);
		return tradeFlow;
	}
	
	public Order findOrderByOrderNo(String code, Integer state) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buffer = new StringBuffer("SELECT ");
		buffer.append(SQLUtils.getColumnsSQL(Order.class, "A"));
		buffer.append(" FROM T_ORDER A WHERE A.CODE=? ");
		args.add(code);
		if (null != state) {
			buffer.append(" AND A.STATE = ? ");
			args.add(state);
		}
		return genericDao.findT(Order.class, buffer.toString(), args.toArray());
	}
	
	public List<Refund> findRefunds(Integer state) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buffer = new StringBuffer("SELECT ");
		buffer.append(SQLUtils.getColumnsSQL(Refund.class, "A"));
		buffer.append(" FROM T_REFUND A WHERE 1=1 ");
		if (null != state) {
			buffer.append(" AND A.STATE = ? ");
			args.add(state);
		}
		return genericDao.findTs(Refund.class, buffer.toString(), args.toArray());
	}

	public List<Orgnization> getOrgnizationList(Long orgId) {
		return orgnizationService.findCyclicOrgnizations(orgId);
	}

}
