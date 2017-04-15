/**
 * 
 */
package com.vendor.service.impl.sales.wechat;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.ecarry.core.dao.IGenericDao;
import com.ecarry.core.dao.SQLUtils;
import com.ecarry.core.exception.BusinessException;
import com.ecarry.core.util.MathUtil;
import com.ecarry.core.web.core.ContextUtil;
import com.ecarry.core.web.core.Page;
import com.vendor.po.Cabinet;
import com.vendor.po.Device;
import com.vendor.po.DeviceAisle;
import com.vendor.po.DeviceLog;
import com.vendor.po.Order;
import com.vendor.po.Orgnization;
import com.vendor.po.PointPlace;
import com.vendor.po.User;
import com.vendor.pojo.WxAccessConf;
import com.vendor.service.IOrgnizationService;
import com.vendor.service.IWechatService;
import com.vendor.thirdparty.et.ETEmptyHistory;
import com.vendor.thirdparty.et.ETRealtimeStatus;
import com.vendor.thirdparty.et.ETVendingOrderReponse;
import com.vendor.util.CommonUtil;
import com.vendor.util.Commons;
import com.vendor.util.DateUtil;
import com.vendor.util.HttpAdapter;
import com.vendor.util.VendingUtil;
import com.vendor.util.WeChatUtil;
import com.vendor.vo.web.ConsumerData;
import com.vendor.vo.web.Consumption;
import com.vendor.vo.web.WechatSaleData;

/**
 * @author liujia on 2016年4月20日
 */
@Service("wechatService")
public class WechatService implements IWechatService {

	private final static Logger logger = Logger.getLogger(WechatService.class);

	@Autowired
	private IGenericDao genericDao;

	@Autowired
	private HttpAdapter httpAdapter;

	private Map<String, Object> accessMap;

	@Value("${url.token}")
	private String tokenUrl;

	@Value("${wx.appid}")
	private String appId;

	@Value("${wx.secret}")
	private String secret;
	
	@Autowired
	private IOrgnizationService orgnizationService;
	
	/**
	 * sessionId存取时的时间，用于判断是否失效
	 */
	private static final String ET_ACCESS_TIME = "ETAccessTime";
	
	private Map<String, Object> etLoginMap;

	public WechatService() {
		this.accessMap = new HashMap<String, Object>();
		this.etLoginMap = new HashMap<String, Object>();
	}

	/**
	 * 获取用户信息
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getWxUser(String token, String url, String openId) {
		if (StringUtils.isEmpty(openId))
			return null;
		if (StringUtils.isEmpty(token))
			token = getAccessToken();
		Map<String, Object> map = null;
		try {
			String content = httpAdapter.getData(MessageFormat.format(
					"https://api.weixin.qq.com/cgi-bin/user/info?access_token={0}&openid={1}&lang=zh_CN", token,
					openId));
			if (content != null) {
				map = ContextUtil.getTByJson(Map.class, content);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		if (map != null && map.containsKey("openid"))
			return map;

		return null;
	}

	/**
	 * 获取用户信息
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getWeChatUser(String token, String url, String openId) {
		logger.info("***获取用户信息---token:***" + token);
		logger.info("***获取用户信息---url:***" + url);
		logger.info("***获取用户信息---openId:***" + openId);
		if (StringUtils.isEmpty(openId))
			return null;
		WxAccessConf wxAccessConf = new WxAccessConf(); 
		if (StringUtils.isEmpty(token)) {
			wxAccessConf = WeChatUtil.getAccessToken(openId, appId, secret);
			if (null == wxAccessConf) {
				logger.error("获取accessToken出错");
				return null;
			}
			token = wxAccessConf.getAccessToken();
		}
		
		Map<String, Object> map = null;
		try {
			String content = httpAdapter.getData(MessageFormat.format(
					"https://api.weixin.qq.com/cgi-bin/user/info?access_token={0}&openid={1}&lang=zh_CN", token,
					openId));
			logger.info("***获取用户信息返回json:***" + content);
			
			if (content != null) {
				map = ContextUtil.getTByJson(Map.class, content);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		if (map != null && map.containsKey("openid"))
			return map;
		
		return null;
	}

	@SuppressWarnings("unchecked")
	private String getAccessToken() {
		long time = System.currentTimeMillis();
		String token = (String) accessMap.get(ACCESS_TOKEN);
		if (token != null)
			if (time - (Long) accessMap.get(TOKEN_TIME) < (Integer) accessMap.get(TOKEN_EXPIRED) * 1000)
				return token;
		String content = httpAdapter.getData(MessageFormat.format(tokenUrl, appId, secret));
		Map<String, Object> map = null;
		try {
			map = ContextUtil.getTByJson(Map.class, content);
		} catch (Exception e) {
		}
		token = (String) map.get("access_token");
		if (token != null) {
			accessMap.put(ACCESS_TOKEN, token);
			accessMap.put(TOKEN_EXPIRED, (Integer) map.get("expires_in"));
			accessMap.put(TOKEN_TIME, time);
		}
		return token;
	}

	/**
	 * 售货机实时状态
	 * @param json 订单接口URL返回JSON串
	 */
	@SuppressWarnings("unchecked")
	public ETVendingOrderReponse queryRealtimeStatus(String json) {
		if (StringUtils.isEmpty(json))
			throw new BusinessException("易触售货机实时状态查询失败!");
		ETVendingOrderReponse reponse = (ETVendingOrderReponse) VendingUtil.toBean(ETVendingOrderReponse.class, ETRealtimeStatus.class, ContextUtil.getTByJson(Map.class, json), true);
		if (reponse != null && (reponse.getSuccess() != null && reponse.getSuccess())) {
			return reponse;
		} else {
			if (reponse == null)
				throw new BusinessException("请求服务失败");
			else {
				throw new BusinessException(reponse.getMsg());
			}
		}
	}
	
	/**
	 * 【易触】登录接口sessionId是否失效  true:失效   false:未失效
	 * @return
	 */
	public boolean isSessionIdExpire() {
		// 存取时间
		Timestamp accessTime = (Timestamp) etLoginMap.get(ET_ACCESS_TIME);
		if (null == accessTime) return false;
		
		// 根据存取时间计算出过期时间
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(accessTime);
		calendar.add(Calendar.HOUR_OF_DAY, 24);
		Timestamp expireTime = new Timestamp(calendar.getTime().getTime());
		
		// 当前时间大于过期时间，则失效
		if (new Timestamp(System.currentTimeMillis()).after(expireTime))
			return true;
		return false;
	}

	/**
	 * 售货机售空历史记录
	 * @param json 订单接口URL返回JSON串
	 */
	@SuppressWarnings("unchecked")
	public ETVendingOrderReponse findEmptyHistory(String json) {
		if (StringUtils.isEmpty(json))
			throw new BusinessException("易触售货机售空历史记录查询失败!");
		ETVendingOrderReponse reponse = (ETVendingOrderReponse) VendingUtil.toBean(ETVendingOrderReponse.class, ETEmptyHistory.class, ContextUtil.getTByJson(Map.class, json), true);
		if (reponse != null && (reponse.getSuccess() != null && reponse.getSuccess())) {
			return reponse;
		} else {
			if (reponse == null)
				throw new BusinessException("请求服务失败");
			else {
				throw new BusinessException(reponse.getMsg());
			}
		}
	}

	/***微信看店**********开始******/
	/**
	 * 店铺经营页面相关数据
	 */
	@Override
	public Map<String, Object> findStoreOperData(Page page, Date startTime, Date endTime, String pointNo, String factorDevNo) {
		Map<String, Object> map = new HashMap<String, Object>();

		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("当前用户未登录");
		
		startTime = null == startTime ? new Date(System.currentTimeMillis()) : startTime;
		endTime = null == endTime ? new Date(System.currentTimeMillis()) : endTime;
		
		// 店铺数量
		int storeQty = getStoreCount(user, null);
		map.put("storeQty", storeQty);
		
		// 设备数量
		int devQty = getDeviceCount(user, null);
		map.put("devQty", devQty);
		
		// 销售数据
		WechatSaleData saleData = findSalesData(user, startTime, endTime, pointNo, factorDevNo);
		map.put("saleData", saleData);

		// 用户消费数据
		ConsumerData consumerData = findConsumerData(startTime, endTime, pointNo, factorDevNo);
		map.put("consumerData", consumerData);
		
		// 店铺销售排行榜
		List<Order> topStores = getTopStores(page, user, startTime, endTime, pointNo, factorDevNo);
		map.put("topStores", topStores);

		// 商品销售占比
		List<Order> topProducts = getTopProducts(page, user, startTime, endTime, pointNo, factorDevNo);
		map.put("topProducts", topProducts);
		
		return map;
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
		
		buf.append(" AND STATE != ? ");
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
		
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
	 * 统计销售数据
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public WechatSaleData findSalesData(User user, Date startTime, Date endTime, String pointNo, String factorDevNo) {
		WechatSaleData data = new WechatSaleData();
		
		StringBuffer buf = new StringBuffer(" SELECT ");
		List<Object> args = new ArrayList<Object>();
		buf.append(" TO_CHAR(O.pay_time, ");
		if (compareSomeDay(startTime, endTime)) {
			buf.append(" 'HH24' ");
		} else if (compareSomeMonth(startTime, endTime)) {
			buf.append(" 'YYYY-MM-DD' ");
		} else if (compareSomeYear(startTime, endTime)) {
			buf.append(" 'yyyy-MM' ");
		}
		buf.append(" ) ");
		buf.append(" AS date , COALESCE(SUM(OD.qty * OD.price), 0) AS salesAmount, COALESCE (SUM(OD.qty), 0) as salesVolume, COALESCE (SUM(OD.qty * OD.price)/SUM(OD.qty), 0) AS averagePrice ");
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
		
		if (!StringUtils.isEmpty(pointNo)) { // 按店铺
			buf.append(" AND O.POINT_NO = ? ");
			args.add(pointNo);
		} else if (!StringUtils.isEmpty(factorDevNo)) { // 按设备
			PointPlace pointPlace = findPointPlaceByFactoryDevNo(factorDevNo);
			
			buf.append(" AND O.POINT_NO = ? ");
			args.add(pointPlace.getPointNo());
			buf.append(" AND O.DEVICE_NO = ? ");
			
			// 通过厂家设备号，找到内部设备号
			String devNo = findDevNoByFacDevNo(factorDevNo);
			args.add(devNo);
		}
		
		buf.append(" AND O.STATE = ? ");
		args.add(Commons.ORDER_STATE_FINISH);
		buf.append(" GROUP BY date ORDER BY date ");

		List<Order> orders = genericDao.findTs(Order.class, buf.toString(), args.toArray());
		Double allSalesAmount = 0.0;
		Integer allSalesVolume = 0;
		for (Order order : orders) {
			allSalesAmount = MathUtil.add(allSalesAmount, order.getSalesAmount());
			allSalesVolume += order.getSalesVolume();
		}
		
		data.setAllSalesAmount(allSalesAmount);
		data.setAllSalesVolume(allSalesVolume);
		data.setAllAveragePrice(allSalesVolume == 0 ? 0.0 : MathUtil.div(allSalesAmount, allSalesVolume));
		data.setOrderList(orders);
		return data;
	}
	
	public String[] getDevNosArr(String[] factorDevNosArr) {
		String[] devNosArr = null;
		if (null != factorDevNosArr && factorDevNosArr.length > 0) {
			devNosArr = new String[factorDevNosArr.length];
			for (int i = 0; i < factorDevNosArr.length; i++) {
				String devNo = findDevNoByFacDevNo(factorDevNosArr[i]);
				if (StringUtils.isEmpty(devNo))
					throw new BusinessException("设备组号【" + factorDevNosArr[i] + "】不存在");
				
				devNosArr[i] = devNo;
			}
		}
		return devNosArr;
	}
	
	public String[] getPointNosArr(String[] devNosArr) {
		String[] pointNosArr = null;
		if (null != devNosArr && devNosArr.length > 0) {
			pointNosArr = new String[devNosArr.length];
			for (int i = 0; i < devNosArr.length; i++) {
				String devNo = devNosArr[i];
				
				PointPlace pointPlace = findPointPlace(devNo);
				if (null == pointPlace)
					throw new BusinessException("设备组号【" + devNo + "】所属店铺不存在");
				
				pointNosArr[i] = pointPlace.getPointNo();
			}
		}
		return pointNosArr;
	}
	
	/**
	 * 统计用户消费数据
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public ConsumerData findConsumerData(Date startTime, Date endTime, String pointNo, String factorDevNo) {
		ConsumerData data = new ConsumerData();
		
		// 用户总数
		int allUserCount = getUserCount(startTime, endTime, pointNo, factorDevNo, null, null);
		data.setAllUserCount(allUserCount);
		
		// 新用户数
		int newUserCount = getNewUserCount(startTime, endTime, pointNo, factorDevNo);
		data.setNewUserCount(newUserCount);
		
		// 复购率 = （用户总数－新用户数）÷用户总数×100%
		double rebuyRate = allUserCount == 0 ? 0.0 : MathUtil.div(MathUtil.mul(MathUtil.sub(Double.valueOf(allUserCount), Double.valueOf(newUserCount)), 100), Double.valueOf(allUserCount));
		data.setRebuyRate(rebuyRate);
		
		// 用户消费明细
		List<Consumption> consumptions = new ArrayList<Consumption>();
		consumptions.add(new Consumption("消费1次", getUserCount(startTime, endTime, pointNo, factorDevNo, 1, 1)));
		consumptions.add(new Consumption("消费2~3次", getUserCount(startTime, endTime, pointNo, factorDevNo, 2, 3)));
		consumptions.add(new Consumption("消费4~6次", getUserCount(startTime, endTime, pointNo, factorDevNo, 4, 6)));
		consumptions.add(new Consumption("消费7~9次", getUserCount(startTime, endTime, pointNo, factorDevNo, 7, 9)));
		consumptions.add(new Consumption("消费10次以上", getUserCount(startTime, endTime, pointNo, factorDevNo, 10, null)));
		data.setConsumptions(consumptions);
		
		return data;
	}
	
	/**
	 * 取得当前用户的用户数
	 */
	private int getUserCount(Date startTime, Date endTime, String pointNo, String factorDevNo, Integer startCount, Integer endCount) {
		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("当前用户未登录");
		
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer(" SELECT COALESCE(COUNT(T.USERNAME), 0) FROM ( SELECT O.USERNAME, COUNT(O.USERNAME) FROM T_ORDER O WHERE 1 = 1 ");
		
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
		
		if (!StringUtils.isEmpty(pointNo)) { // 按店铺
			buf.append(" AND O.POINT_NO = ? ");
			args.add(pointNo);
		} else if (!StringUtils.isEmpty(factorDevNo)) { // 按设备
			PointPlace pointPlace = findPointPlaceByFactoryDevNo(factorDevNo);
			
			buf.append(" AND O.POINT_NO = ? ");
			args.add(pointPlace.getPointNo());
			buf.append(" AND O.DEVICE_NO = ? ");
			
			// 通过厂家设备号，找到内部设备号
			String devNo = findDevNoByFacDevNo(factorDevNo);
			args.add(devNo);
		}
		
		buf.append(" AND O.STATE = ? ");
		args.add(Commons.ORDER_STATE_FINISH);
		buf.append(" GROUP BY O.USERNAME ");
		
		if (null != startCount) {
			buf.append(" HAVING COUNT(O.USERNAME) >= ? ");
			args.add(startCount);
			
			if (null != endCount) {
				buf.append(" AND COUNT(O.USERNAME) <= ? ");
				args.add(endCount);
			}
		}
		
		buf.append(" ) T ");
		
		return genericDao.findSingle(int.class, buf.toString(), args.toArray());
	}

	/**
	 * 取得新的用户数
	 */
	private int getNewUserCount(Date startTime, Date endTime, String pointNo, String factorDevNo) {
		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("当前用户未登录");
		
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer(" SELECT COALESCE(COUNT(*), 0) FROM T_WE_USER WHERE 1 = 1 ");
		
		buf.append(" AND CREATE_TIME>=? ");
		args.add(DateUtil.getStartDate(startTime));
		buf.append(" AND CREATE_TIME<=? ");
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
				
				buf.append(" AND ORG_ID ").append(orgIdsSQL);
			}
		}
		
		if (!StringUtils.isEmpty(pointNo)) { // 按店铺
			buf.append(" AND DEV_NO IN (SELECT DEV_NO FROM T_DEVICE WHERE POINT_ID = (SELECT ID FROM T_POINT_PLACE WHERE POINT_NO = ? AND STATE != ? )) ");
			args.add(pointNo);
			args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
		} else if (!StringUtils.isEmpty(factorDevNo)) { // 按设备
			String devNo = findDevNoByFacDevNo(factorDevNo);
			
			buf.append(" AND DEV_NO = ? ");
			args.add(devNo);
		}
		
		return genericDao.findSingle(int.class, buf.toString(), args.toArray());
	}
	
	/**
	 * 店铺销售排行榜
	 * @return
	 */
	private List<Order> getTopStores(Page page, User user, Date startTime, Date endTime, String pointNo, String factorDevNo) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer(" SELECT PP.POINT_NAME AS name, COALESCE(SUM(OD.qty*OD.price), 0) AS salesAmount ");
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
		
		buf.append(" AND PP.STATE != ? ");
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
		
		buf.append(" AND O.STATE=? ");
		args.add(Commons.ORDER_STATE_FINISH);
		
		//开始时间
		buf.append(" AND O.PAY_TIME >= ? ");
		args.add(DateUtil.getStartDate(startTime));
		//结束时间
		buf.append(" AND O.PAY_TIME<= ? ");
		args.add(DateUtil.getEndDate(endTime));
		
		if (!StringUtils.isEmpty(pointNo)) { // 按店铺
			buf.append(" AND O.POINT_NO = ? ");
			args.add(pointNo);
		} else if (!StringUtils.isEmpty(factorDevNo)) { // 按设备
			PointPlace pointPlace = findPointPlaceByFactoryDevNo(factorDevNo);
			
			buf.append(" AND O.POINT_NO = ? ");
			args.add(pointPlace.getPointNo());
			buf.append(" AND O.DEVICE_NO = ? ");
			
			// 通过厂家设备号，找到内部设备号
			String devNo = findDevNoByFacDevNo(factorDevNo);
			args.add(devNo);
		}
		
		buf.append(" GROUP BY PP.POINT_NAME ");
		buf.append(" ORDER BY salesAmount DESC ");
		
		return genericDao.findTs(Order.class, page, buf.toString(), args.toArray());
	}
	
	/**
	 * 商品销售排行榜
	 * @return
	 */
	private List<Order> getTopProducts(Page page, User user, Date startTime, Date endTime, String pointNo, String factorDevNo) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer(" SELECT P.SKU_NAME AS name, COALESCE(SUM(OD.qty*OD.price), 0) AS salesAmount, COALESCE(SUM(OD.qty), 0) AS salesVolume ");
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
		
		buf.append(" AND O.STATE=? ");
		args.add(Commons.ORDER_STATE_FINISH);
		
		//开始时间
		buf.append(" AND O.PAY_TIME >= ? ");
		args.add(DateUtil.getStartDate(startTime));
		//结束时间
		buf.append(" AND O.PAY_TIME<= ? ");
		args.add(DateUtil.getEndDate(endTime));
		
		if (!StringUtils.isEmpty(pointNo)) { // 按店铺
			buf.append(" AND O.POINT_NO = ? ");
			args.add(pointNo);
		} else if (!StringUtils.isEmpty(factorDevNo)) { // 按设备
			PointPlace pointPlace = findPointPlaceByFactoryDevNo(factorDevNo);
			
			buf.append(" AND O.POINT_NO = ? ");
			args.add(pointPlace.getPointNo());
			buf.append(" AND O.DEVICE_NO = ? ");
			
			// 通过厂家设备号，找到内部设备号
			String devNo = findDevNoByFacDevNo(factorDevNo);
			args.add(devNo);
		}
		
		buf.append(" GROUP BY P.SKU_NAME ");
		buf.append(" ORDER BY salesAmount DESC, salesVolume DESC ");
		List<Order> orders = genericDao.findTs(Order.class, page, buf.toString(), args.toArray());
		
		// 取得总销售额
		double totalAmount = getSalesAmount(startTime, endTime, pointNo, factorDevNo);
		for (Order order : orders) {
			double salesRate = totalAmount == 0 ? 0.0 : MathUtil.div(MathUtil.mul(order.getSalesAmount(), 100), totalAmount);
			order.setSalesRate(salesRate);
		}
		
		return orders;
	}
	
	/**
	 * 取得销售额
	 */
	private double getSalesAmount(Date startTime, Date endTime, String pointNo, String factorDevNo) {
		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("当前用户未登录");
		
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer(" SELECT COALESCE(sum(OD.qty*OD.price), 0) as salesAmount ");
		buf.append(" FROM T_ORDER O ");
		buf.append(" LEFT JOIN T_ORDER_DETAIL OD ON O.CODE = OD.ORDER_NO ");
		buf.append(" LEFT JOIN T_PRODUCT P ON OD.SKU_ID = P.ID ");
		buf.append(" WHERE 1 = 1 ");
		
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
		
		// 开始日期
		if (null != startTime) {
			buf.append(" AND O.PAY_TIME>=? ");
			args.add(DateUtil.getStartDate(startTime));
		}
		// 结束日期
		if (null != endTime) {
			buf.append(" AND O.PAY_TIME<=? ");
			args.add(DateUtil.getEndDate(endTime));
		}
		
		if (!StringUtils.isEmpty(pointNo)) { // 按店铺
			buf.append(" AND O.POINT_NO = ? ");
			args.add(pointNo);
		} else if (!StringUtils.isEmpty(factorDevNo)) { // 按设备
			PointPlace pointPlace = findPointPlaceByFactoryDevNo(factorDevNo);
			
			buf.append(" AND O.POINT_NO = ? ");
			args.add(pointPlace.getPointNo());
			buf.append(" AND O.DEVICE_NO = ? ");
			
			// 通过厂家设备号，找到内部设备号
			String devNo = findDevNoByFacDevNo(factorDevNo);
			args.add(devNo);
		}
		
		buf.append(" AND O.STATE = ? ");
		args.add(Commons.ORDER_STATE_FINISH);

		Double amount = genericDao.findSingle(Double.class, buf.toString(), args.toArray());
		return amount == null ? 0 : amount;
	}
	
	/**
	 * 【店铺经营】查询店铺设备数据
	 */
	@Override
	public List<PointPlace> findStoreDevices(Page page) {
		StringBuffer buf = new StringBuffer("SELECT ");
		String cols = SQLUtils.getColumnsSQL(PointPlace.class, "C");
		buf.append(cols);
		buf.append(" ,SO.NAME AS orgName ");
		buf.append(" FROM T_POINT_PLACE C LEFT JOIN SYS_ORG SO ON C.ORG_ID=SO.ID WHERE 1=1 ");
		List<Object> args = new ArrayList<Object>();;
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
				
				buf.append(" AND C.ORG_ID ").append(orgIdsSQL);
			}
		}
		
		buf.append(" AND C.STATE != ? ");
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
		
		List<PointPlace> pointPlaces = genericDao.findTs(PointPlace.class, page, buf.toString(), args.toArray());
		for (PointPlace pointPlace : pointPlaces) {
			List<Device> devices = getDevicesByPointId(pointPlace.getId());
			
			StringBuffer buffer = new StringBuffer();
			for (Device device : devices) {
				List<Cabinet> cabinets = getCabinetsByDeviceId(device.getId());
				for (Cabinet cabinet : cabinets) {
					String typeStr = CommonUtil.getDeviceTypeStrByModel(cabinet.getModel());
					if (StringUtils.isEmpty(typeStr))
						continue;
					
					buffer.append(typeStr + "+");
				}
				buffer.setLength(buffer.length() - 1);
				device.setTypeStr(buffer.toString());
				
				buffer.setLength(0);
			}
			
			pointPlace.setDevices(devices);
		}
		
		return pointPlaces;
	}
	
	/**
	 * 【设备管理】页面相关数据
	 */
	@Override
	public Map<String, Object> findDeviceOperData(Page page, Date startTime, Date endTime) {
		Map<String, Object> map = new HashMap<String, Object>();
		
		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("当前用户未登录");
		
		// 设备数量
		int devQty = getDeviceCount(user, null);
		map.put("devQty", devQty);
		
		// 离线设备数量
		int offlineDeviceCount = getDeviceLogQty(user, Commons.DEVICE_STATUS_OFFLINE, null); // 离线
		map.put("offlineDeviceCount", offlineDeviceCount);

		// 设备明细信息
		List<Device> devices = findDevices(page, user, startTime, endTime);
		map.put("devices", devices);
		
		return map;
	}
	
	/**
	 * 设备列表
	 */
	public List<Device> findDevices(Page page, User user, Date startTime, Date endTime) {
		StringBuffer buf = new StringBuffer("SELECT ");
		List<Object> args = new ArrayList<Object>();
		String cols = SQLUtils.getColumnsSQL(Device.class, "D", "TYPE,AISLECOUNT,FACTORYNO");
		buf.append(cols);
		buf.append(" ,SO.NAME AS ORGNAME,PP.POINT_ADDRESS AS POINTADDRESS, DL.DEVICE_NO ");
		buf.append(" ,R.FACTORY_DEV_NO AS FACTORYDEVNO ");
		buf.append(" FROM T_DEVICE D LEFT JOIN T_CABINET C ON D.ID = C.DEVICE_ID ");
		buf.append(" LEFT JOIN SYS_ORG SO ON D.ORG_ID=SO.ID ");
		buf.append(" LEFT JOIN T_POINT_PLACE PP ON PP.ID = D.POINT_ID ");
		buf.append(" LEFT JOIN T_DEVICE_RELATION R ON R.DEV_NO = D.DEV_NO ");
		buf.append(" LEFT JOIN T_DEVICE_LOG DL ON DL.DEVICE_NO = D.DEV_NO AND DL.DEVICE_STATUS = ? ");
		buf.append(" WHERE 1 = 1  ");
		args.add(Commons.DEVICE_STATUS_OFFLINE);

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
		
		buf.append(" AND PP.STATE != ? ");
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
		
//		buf.append(" AND D.BIND_STATE = ? ");
//		args.add(Commons.BIND_STATE_SUCCESS);

		buf.append(" GROUP BY ").append(cols).append(" ,SO.NAME ,PP.POINT_ADDRESS, R.FACTORY_DEV_NO, DL.DEVICE_NO ");
		buf.append(" ORDER BY DL.DEVICE_NO ");// 离线的设备排在前面
		
		List<Device> devices = genericDao.findTs(Device.class, page, buf.toString(), args.toArray());
		
		StringBuffer buffer = new StringBuffer();
		for (Device device : devices) {
			// 设备类型描述
			List<Cabinet> cabinets = getCabinetsByDeviceId(device.getId());
			for (Cabinet cabinet : cabinets) {
				String typeStr = CommonUtil.getDeviceTypeStrByModel(cabinet.getModel());
				if (StringUtils.isEmpty(typeStr))
					continue;
				
				buffer.append(typeStr + "+");
			}
			buffer.setLength(buffer.length() - 1);
			device.setTypeStr(buffer.toString());
			
			buffer.setLength(0);
			
			DeviceLog deviceLog = findDeviceLog(device.getDevNo(), Commons.DEVICE_STATUS_OFFLINE);//是否离线
			device.setIsOffLine(null != deviceLog ? true : false);
		}
		
		return devices;
	}
	
	public DeviceLog findDeviceLog(String deviceNo, Integer deviceStatus) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer("SELECT ");
		String cols = SQLUtils.getColumnsSQL(DeviceLog.class, "C");
		buf.append(cols);
		buf.append(" FROM T_DEVICE_LOG C WHERE C.DEVICE_NO = ? AND C.DEVICE_STATUS = ? ");
		args.add(deviceNo);
		args.add(deviceStatus);
		return genericDao.findT(DeviceLog.class, buf.toString(), args.toArray());
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
	 * 【补货计划】页面相关数据
	 */
	@Override
	public Map<String, Object> findReplenishmentData(Page page) {
		Map<String, Object> map = new HashMap<String, Object>();
		
		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("当前用户未登录");
		
		// 待补货设备数量
		int restockDeviceCount = getDeviceLogQty(user, Commons.DEVICE_STATUS_STOCKOUT, null); // 待补货
		map.put("restockDeviceCount", restockDeviceCount);
		
		// 店铺设备明细
		List<PointPlace> pointPlaces = findStoreDevices(page);
		map.put("pointPlaces", pointPlaces);
		
		return map;
	}
	
	/**
	 * 【补货计划】店铺商品补货信息
	 */
	public Map<String, Object> findReplenishProds(Page page, Long storeId) {
		Map<String, Object> map = new HashMap<String, Object>();
		
		// 店铺商品数量
		List<DeviceAisle> countDeviceAisles = findReplenishProdList(null, storeId);
		map.put("prodsCount", null == countDeviceAisles ? 0 : countDeviceAisles.size());
		
		// 店铺商品补货明细
		List<DeviceAisle> deviceAisles = findReplenishProdList(page, storeId);
		map.put("replenishProds", deviceAisles);
		
		return map;
	}
	
	/**
	 * 查询【店铺库存】店铺商品补货信息
	 * @param page
	 * @return
	 */
	public List<DeviceAisle> findReplenishProdList(Page page, Long storeId) {
		StringBuffer buffer = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		
		buffer.append("SELECT STRING_AGG(B.ID||','||B.NAME||','||B.TYPE||','||B.REAL_PATH, ';' ORDER BY B.ID DESC) AS images, ");
		buffer.append(" P.CODE as productCode,P.SKU_NAME as productName,DA.PRODUCT_ID,DA.PRICE, DA.PRICE_ON_LINE, DA.SELLABLE, P.TYPE as type,SUM(DA.STOCK) as totalStock,SUM(DA.SUPPLEMENT_NO) as totalSupplementNo ");
		buffer.append(" FROM T_DEVICE_AISLE DA ");
		buffer.append(" LEFT JOIN T_DEVICE D ON D. ID = DA.DEVICE_ID ");
		buffer.append(" LEFT JOIN T_PRODUCT P ON P.ID = DA.PRODUCT_ID AND D.ORG_ID = P.ORG_ID ");
		buffer.append(" LEFT JOIN SYS_ORG O ON P.ORG_ID=O.ID LEFT JOIN T_FILE B ON P.ID=B.INFO_ID AND B.TYPE=? ");
		buffer.append(" WHERE 1=1  ");
		
		buffer.append(" AND P.STATE!=? ");
		args.add(Commons.PRODUCT_STATE_TRASH);
		
		buffer.append(" AND D.POINT_ID != 0 ");
		buffer.append(" AND D.POINT_ID = ? ");
		args.add(storeId);
		
		buffer.append(" AND DA.PRODUCT_ID IS NOT NULL ");
		buffer.append(" GROUP BY ").append(" P.CODE,P.SKU_NAME,DA.PRODUCT_ID,DA.PRICE, DA.PRICE_ON_LINE, DA.SELLABLE, P.TYPE ");
		args.add(0, Commons.FILE_PRODUCT);
		
		return genericDao.findTs(DeviceAisle.class, page, buffer.toString(), args.toArray());
	}
	
	public List<Device> getDevicesByPointId(Long pointId) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buffer = new StringBuffer("SELECT ");
		buffer.append(SQLUtils.getColumnsSQL(Device.class, "D"));
		buffer.append(" , DR.FACTORY_DEV_NO as factoryDevNo ");
		buffer.append(" FROM T_DEVICE D LEFT JOIN T_DEVICE_RELATION DR ON DR.DEV_NO = D.DEV_NO ");
		buffer.append(" WHERE 1=1 ");
		buffer.append(" AND D.POINT_ID = ? ");
		args.add(pointId);
		buffer.append(" AND D.BIND_STATE = ? ");
		args.add(Commons.BIND_STATE_SUCCESS);//点位绑定成功
		return genericDao.findTs(Device.class, buffer.toString(), args.toArray());
	}
	
	public List<Cabinet> getCabinetsByDeviceId(Long deviceId) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buffer = new StringBuffer("SELECT ");
		buffer.append(SQLUtils.getColumnsSQL(Cabinet.class, "C"));
		buffer.append(" FROM T_CABINET C  ");
		buffer.append(" WHERE 1=1 ");
		buffer.append(" AND C.DEVICE_ID = ? ");
		args.add(deviceId);
		buffer.append(" ORDER BY C.CABINET_NO ");
		return genericDao.findTs(Cabinet.class, buffer.toString(), args.toArray());
	}

	public String findDevNoByFacDevNo(String facDevNo) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buffer = new StringBuffer(" SELECT ");
		buffer.append(" C.DEV_NO AS DEV_NO ");
		buffer.append(" FROM T_DEVICE_RELATION C ");
		buffer.append(" WHERE C.FACTORY_DEV_NO = ? ");
		args.add(facDevNo);
		return genericDao.findSingle(String.class, buffer.toString(), args.toArray());
	}
	
	public PointPlace findPointPlace(String devNo) {
		StringBuffer buffer = new StringBuffer("SELECT ");
		buffer.append(SQLUtils.getColumnsSQL(PointPlace.class, "A"));
		buffer.append(" FROM T_POINT_PLACE A WHERE A.ID = (SELECT POINT_ID FROM T_DEVICE WHERE DEV_NO = ?) ");
		
		buffer.append(" AND A.STATE != ? ");
		return genericDao.findT(PointPlace.class, buffer.toString(), devNo, Commons.POINT_PLACE_STATE_DELETE);
	}

	public PointPlace findPointPlaceByFactoryDevNo(String factoryDevNo) {
		StringBuffer buffer = new StringBuffer("SELECT ");
		buffer.append(SQLUtils.getColumnsSQL(PointPlace.class, "A"));
		buffer.append(" FROM T_POINT_PLACE A WHERE A.ID = (SELECT POINT_ID FROM T_DEVICE WHERE DEV_NO = (SELECT DEV_NO FROM T_DEVICE_RELATION WHERE FACTORY_DEV_NO = ?)) ");
		
		buffer.append(" AND A.STATE != ? ");
		return genericDao.findT(PointPlace.class, buffer.toString(), factoryDevNo, Commons.POINT_PLACE_STATE_DELETE);
	}
	
	/**
	 * 是否为同一天
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	private boolean compareSomeDay(Date startTime, Date endTime) {
		if (null == startTime || null == endTime)
			throw new BusinessException("参数非法");
		
		return (DateUtil.getDay(startTime) == DateUtil.getDay(endTime)) ? true : false;
	}
	
	/**
	 * 是否为同一月
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	private boolean compareSomeMonth(Date startTime, Date endTime) {
		if (null == startTime || null == endTime)
			throw new BusinessException("参数非法");
		
		return (DateUtil.getMonth(startTime) == DateUtil.getMonth(endTime)) ? true : false;
	}
	
	/**
	 * 是否为同一年
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	private boolean compareSomeYear(Date startTime, Date endTime) {
		if (null == startTime || null == endTime)
			throw new BusinessException("参数非法");
		
		return (DateUtil.getYear(startTime) == DateUtil.getYear(endTime)) ? true : false;
	}
	/***微信看店**********结束******/
	
	public List<Orgnization> getOrgnizationList(Long orgId) {
		return orgnizationService.findCyclicOrgnizations(orgId);
	}
}
