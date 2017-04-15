/**
 * 
 */
package com.vendor.service.impl.report;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecarry.core.dao.IGenericDao;
import com.ecarry.core.dao.SQLUtils;
import com.ecarry.core.domain.BoxValue;
import com.ecarry.core.exception.BusinessException;
import com.ecarry.core.util.ZHConverter;
import com.ecarry.core.web.core.ContextUtil;
import com.ecarry.core.web.core.Page;
import com.vendor.po.AppException;
import com.vendor.po.Device;
import com.vendor.po.DeviceAisle;
import com.vendor.po.Order;
import com.vendor.po.Refund;
import com.vendor.po.User;
import com.vendor.service.IReportService;
import com.vendor.util.Commons;

/**
 * @author dranson on 2015年12月24日
 */
@Service("reportService")
public class ReportService implements IReportService {

	@Autowired
	private IGenericDao genericDao;

	/*
	 * (non-Javadoc)
	 * @see com.ecarry.service.IReportService#findOrders(com.ecarry.core.web.core.Page, com.ecarry.domain.Order, java.sql.Date, java.sql.Date)
	 */
	@Override
	public List<Order> findOrders(Page page, Order order, Date startDate, Date endDate) {
		StringBuffer buf = new StringBuffer("SELECT ");
		String columns = SQLUtils.getColumnsSQL(Order.class, "A") + ",O.NAME";
		buf.append(columns);
		buf.append(" AS orgName,STRING_AGG(B.SKU||','||B.SKU_NAME||','||B.CURRENCY||','||B.PRICE||','||B.QTY||','||COALESCE(B.PRODUCT_MODEL, ''), ';' ORDER BY B.ID) AS details");
		buf.append(" FROM T_ORDER A LEFT JOIN SYS_ORG O ON A.ORG_ID=O.ID LEFT JOIN (SELECT D.SKU,D.SKU_NAME,C.CURRENCY,C.PRICE,C.QTY,C.PRODUCT_MODEL");
		buf.append(",C.ID,C.ORDER_NO FROM T_ORDER_DETAIL C LEFT JOIN T_PRODUCT D ON C.SKU_ID=D.ID) B ON A.CODE=B.ORDER_NO");
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("orgId", SQLUtils.IGNORE);
		BoxValue<String, List<Object>> box = SQLUtils.getCondition(order, map, "A");
		buf.append(box.getKey());
		List<Object> args = box.getValue();
		if (order.getSku() != null) {
			buf.append(args.size() == 0 ? " WHERE" : " AND").append(" B.SKU=?");
			args.add(order.getSku());
		}
		if (order.getCategory() != null) {
			buf.append(args.size() == 0 ? " WHERE" : " AND").append(" B.SKU LIKE ?");
			args.add(order.getCategory() + "%");
		}
		if (order.getSkuName() != null) {
			buf.append(args.size() == 0 ? " WHERE" : " AND").append(" (B.SKU_NAME LIKE ? OR B.SKU_NAME LIKE ?)");
			args.add("%" + ZHConverter.convert(order.getSkuName(), ZHConverter.TRADITIONAL) + "%");
			args.add("%" + ZHConverter.convert(order.getSkuName(), ZHConverter.SIMPLIFIED) + "%");
		}
		if (order.getOrgName() != null) {
			buf.append(args.size() == 0 ? " WHERE" : " AND").append(" (O.NAME LIKE ? OR O.NAME LIKE ?)");
			args.add("%" + ZHConverter.convert(order.getOrgName(), ZHConverter.TRADITIONAL) + "%");
			args.add("%" + ZHConverter.convert(order.getOrgName(), ZHConverter.SIMPLIFIED) + "%");
		}
		if (order.getOrgId() != null) {
			buf.append(args.size() == 0 ? " WHERE" : " AND").append(order.getOrgId().longValue() == 0 ? " A.ORG_ID=?" : " A.ORG_ID!=?");
			args.add(0);
		}
		User user = ContextUtil.getUser(User.class);
		if(user.getOrgId()!=Commons.ORG_HQ){
			buf.append(args.size() == 0 ? " WHERE" : " AND").append(" A.ORG_ID=?");
			args.add(user.getOrgId());
		}
		
		if (startDate != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(startDate);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			Timestamp date = new Timestamp(cal.getTimeInMillis());
			buf.append(args.size() == 0 ? " WHERE " : " AND ").append("A.PAY_TIME>=?");
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
			buf.append(args.size() == 0 ? " WHERE " : " AND ").append("A.PAY_TIME<=?");
			args.add(date);
		}
		buf.append(args.size() == 0 ? " WHERE" : " AND").append(" A.STATE=?");
		args.add(Commons.ORDER_STATE_FINISH);
		
		buf.append(" GROUP BY ").append(columns);
		return genericDao.findTs(Order.class, page, buf.toString(), args.toArray());
	}

	/*
	 * (non-Javadoc)
	 * @see com.ecarry.service.IReportService#saveOrderPaidState(java.lang.Long)
	 */
	@Override
	public void saveOrderPaidState(Long id) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * @see com.ecarry.service.IReportService#findRefunds(com.ecarry.core.web.core.Page, com.ecarry.domain.Refund, java.sql.Date, java.sql.Date)
	 */
	@Override
	public List<Refund> findRefunds(Page page, Refund refund, Date startDate, Date endDate) {
		StringBuffer buf = new StringBuffer("SELECT ");
		String columns = SQLUtils.getColumnsSQL(Refund.class, "A");
		buf.append(columns);
		buf.append(" FROM T_REFUND A");
		BoxValue<String, List<Object>> box = SQLUtils.getCondition(refund, null, "A");
		buf.append(box.getKey());
		List<Object> args = box.getValue();
		if (startDate != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(startDate);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			Timestamp date = new Timestamp(cal.getTimeInMillis());
			buf.append(args.size() == 0 ? " WHERE " : " AND ").append("A.CREATE_TIME>=?");
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
			buf.append(args.size() == 0 ? " WHERE " : " AND ").append("A.CREATE_TIME<=?");
			args.add(date);
		}
		return genericDao.findTs(Refund.class, page, buf.toString(), args.toArray());
	}

	/*
	 * (non-Javadoc)
	 * @see com.ecarry.service.ISaleService#saveUncompleteRefundOrder(com.ecarry.domain.Refund)
	 */
	@Override
	public void saveUncompleteRefundOrder(Refund refund) {
		boolean throwable = true;
		if (refund == null) {
			refund = new Refund();
			throwable = false;
		}
		List<Refund> refunds = genericDao.findTs(refund);
		if (refunds.isEmpty() && throwable)
			throw new BusinessException("所选订单没有退款信息！");
		// for (Refund record : refunds) {
		// if (Commons.PAY_YS_ALIPAY.equals(record.getType())) {
		// Map<String, Object> params = new HashMap<String, Object>();
		// params.put("seller_id", ysSellerId);
		// params.put("pno", record.getPayNo());
		// params.put("refund_no", record.getCode());
		// String content = httpAdapter.postData(ysAlipayRefundQuery, params);
		// YSAlipayRefundQueryXML xml = (YSAlipayRefundQueryXML) jaxbMarshall.unmarshal(new StreamSource(new ByteArrayInputStream(content.getBytes())));
		// if (YSAlipayRefundQueryXML.SUCCESS.equals(xml.getResultCode())) {
		// int type = xml.getRefundState();
		// if (type != Commons.PAY_STATE_NEW)
		// genericDao.execute("UPDATE T_REFUND SET UPDATE_TIME=?,STATE=?,REMARK=? WHERE ID=?", new Timestamp(System.currentTimeMillis()), type, xml.getState(),
		// record.getId());
		// }
		// } else {
		//
		// }
		// }
	}

	/**
	 * 查询APP异常信息
	 */
	public List<AppException> findAppEceptions(Page page, AppException appException, Date startDate, Date endDate) {
		StringBuffer buf = new StringBuffer("SELECT ");
		String cols = SQLUtils.getColumnsSQL(AppException.class, "C");
		buf.append(cols);
		buf.append(" FROM T_APP_EXCEPTION C LEFT JOIN T_DEVICE D ON C.DEVICE_ID=D.ID ");// 设备表
		buf.append(" WHERE 1 = 1 ");
		List<Object> args = new ArrayList<Object>();
		User user = ContextUtil.getUser(User.class);
		buf.append(" AND D.ORG_ID = ? ");
		args.add(user.getOrgId());
		if (appException == null) {
			appException = new AppException();
		}
		if (appException.getDeviceNo() != null) {
			buf.append(" AND").append(" (C.DEVICE_NO LIKE ? OR C.DEVICE_NO LIKE ?)");
			args.add("%" + ZHConverter.convert(appException.getDeviceNo(), ZHConverter.TRADITIONAL) + "%");
			args.add("%" + ZHConverter.convert(appException.getDeviceNo(), ZHConverter.SIMPLIFIED) + "%");
		}
		if (startDate != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(startDate);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			Timestamp date = new Timestamp(cal.getTimeInMillis());
			buf.append(" AND C.CREATE_TIME>=?");
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
			buf.append(" AND C.CREATE_TIME<=?");
			args.add(date);
		}
		buf.append(" ORDER BY C.CREATE_TIME DESC");
		return genericDao.findTs(AppException.class, page, buf.toString(), args.toArray());
	}
	
	/**
	 * 查询自己的设备
	 * @return
	 */
	public List<Device> findOwnDevices() {
		User user = ContextUtil.getUser(User.class);
		if (null == user) throw new BusinessException("当前用户还未登录！");
		return genericDao.findTs(Device.class,"SELECT ID,DEV_NO FROM T_DEVICE WHERE ORG_ID = ?", user.getOrgId());
	}
	
	@Override
	public List<Device> findDevices(Page page, Device device, Date startDate, Date endDate) {
		if (page != null) {
			page.setOrder("SALEPRICE");
			page.setDesc(true);
		}
		StringBuffer buf = new StringBuffer("SELECT ");
		String cols = SQLUtils.getColumnsSQL(Device.class, "C");
		buf.append(cols);
		buf.append(" ,SO.NAME AS orgName, COALESCE(O.AMOUNT , 0) AS salePrice,PP.POINT_ADDRESS as pointAddress ");
		buf.append(" FROM T_DEVICE C ");
		
		
		
		if (device == null) {
			device = new Device();
		}
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("devNo", SQLUtils.IGNORE);
		map.put("address", SQLUtils.IGNORE);
		map.put("orgName", SQLUtils.IGNORE);
		BoxValue<String, List<Object>> box = SQLUtils.getCondition(device, map, "C");
		List<Object> args = box.getValue();
		
		buf.append("LEFT JOIN (select DEVICE_NO, ORG_ID, sum(amount) as amount from t_order where 1 = 1 ");
		if (startDate != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(startDate);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			Timestamp date = new Timestamp(cal.getTimeInMillis());
			buf.append(" AND PAY_TIME>=?");
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
			buf.append(" AND PAY_TIME<=?");
			args.add(date);
		}
		buf.append(" AND STATE = ? ");
		args.add(Commons.ORDER_STATE_FINISH);// 已完成
		buf.append(" group by DEVICE_NO, ORG_ID ) O ");
		buf.append(" ON C.DEV_NO = O.DEVICE_NO AND C.ORG_ID = O.ORG_ID ");
		
		buf.append(" LEFT JOIN SYS_ORG SO ON SO.ID = C.ORG_ID ");
		buf.append(" LEFT JOIN T_POINT_PLACE PP ON PP.ID = C.POINT_ID ");
		buf.append(" WHERE 1=1 ");
		
		buf.append(" AND PP.STATE != ? ");
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
		
		if (device.getDevNo() != null) {
			buf.append(" AND").append(" (C.DEV_NO LIKE ? OR C.DEV_NO LIKE ?)");
			args.add("%" + ZHConverter.convert(device.getDevNo(), ZHConverter.TRADITIONAL) + "%");
			args.add("%" + ZHConverter.convert(device.getDevNo(), ZHConverter.SIMPLIFIED) + "%");
		}
		if (device.getPointAddress() != null) {
			buf.append(" AND").append(" (PP.POINT_ADDRESS LIKE ? OR PP.POINT_ADDRESS LIKE ?)");
			args.add("%" + ZHConverter.convert(device.getPointAddress(), ZHConverter.TRADITIONAL) + "%");
			args.add("%" + ZHConverter.convert(device.getPointAddress(), ZHConverter.SIMPLIFIED) + "%");
		}
		if (device.getOrgName() != null) {
			buf.append(" AND").append(" (SO.NAME LIKE ? OR SO.NAME LIKE ?)");
			args.add("%" + ZHConverter.convert(device.getOrgName(), ZHConverter.TRADITIONAL) + "%");
			args.add("%" + ZHConverter.convert(device.getOrgName(), ZHConverter.SIMPLIFIED) + "%");
		}
		
		buf.append(" AND C.ORG_ID = ? ");
		User user = ContextUtil.getUser(User.class);
		if (null == user) throw new BusinessException("当前用户未登录，请登录后再操作");
		args.add(user.getOrgId());
		
		buf.append(" AND C.DEV_NO IS NOT NULL ");
		
		buf.append(" GROUP BY ").append(cols).append(", SO. NAME, O.AMOUNT, PP.POINT_ADDRESS ");
		
		return genericDao.findTs(Device.class, page, buf.toString(), args.toArray());
	}

	@Override
	public List<DeviceAisle> findSellerDevice(Page page, DeviceAisle deviceAisle, Date startDate, Date endDate) {
		if (page != null) {
			page.setOrder("SALES");
			page.setDesc(true);
		}
		
		StringBuffer buf = new StringBuffer("SELECT C.SKU_ID AS id,SUM(C.QTY) AS sales,MAX(C.PRICE) AS price,");
		buf.append(" MAX(C.PRICE)*SUM(C.QTY) AS salesVolume,P.SKU_NAME AS productName ");
		buf.append(" FROM T_ORDER_DETAIL C ");
		buf.append(" LEFT JOIN T_ORDER O ON C.ORDER_NO=O.CODE ");
		buf.append(" LEFT JOIN T_PRODUCT P ON C.SKU_ID=P.ID WHERE 1=1 ");
		if (deviceAisle == null) {
			deviceAisle = new DeviceAisle();
		}
//		BoxValue<String, List<Object>> box = SQLUtils.getCondition(deviceAisle, null, "C");
		List<Object> args = new ArrayList<Object>();
		buf.append(" AND O.ORG_ID = ? ");
		User user = ContextUtil.getUser(User.class);
		if (null == user) throw new BusinessException("该用户还未登录，请先登录后再操作"); 
		args.add(user.getOrgId());
		
		buf.append(" AND O.STATE = ? ");
		args.add(Commons.ORDER_STATE_FINISH);
		
		
		if (deviceAisle.getDeviceId() != null) {
			Device device = findDeviceById(deviceAisle.getDeviceId());
			if (null != device) {
				buf.append(" AND O.DEVICE_NO=? ");
				args.add(device.getDevNo());
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
			buf.append(" AND O.PAY_TIME>=?");
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
			buf.append(" AND O.PAY_TIME<=?");
			args.add(date);
		}
		
		buf.append(" GROUP BY C.SKU_ID, P.SKU_NAME ");
		
		return genericDao.findTs(DeviceAisle.class, page, buf.toString(), args.toArray());
	}

	@Override
	public List<DeviceAisle> findProducts(Page page, DeviceAisle deviceAisle, Date startDate, Date endDate) {
		if (page != null) {
			page.setOrder("sales");
		}
		StringBuffer buf = new StringBuffer("SELECT MAX(C.ID) AS id,SUM(C.QTY) AS sales,MAX(C.PRICE) AS price,");
		buf.append(
				"MAX(C.PRICE)*SUM(C.QTY) AS salesVolume,STRING_AGG(B.ID||','||B.NAME||','||B.TYPE||','||B.REAL_PATH, ';' ORDER BY B.ID DESC) AS images,MAX(P.SKU_NAME) AS productName ");
		buf.append(" FROM T_ORDER_DETAIL C ");
		buf.append(" LEFT JOIN T_ORDER O ON C.ORDER_NO=O.CODE ");
		buf.append(" LEFT JOIN SYS_ORG SO ON C.ORG_ID=SO.ID ");
		buf.append(" LEFT JOIN T_PRODUCT P ON C.SKU_ID=P.ID ");
		buf.append(" LEFT JOIN T_FILE B ON P.ID=B.INFO_ID AND (B.TYPE=?) WHERE 1=1 ");
		if (deviceAisle == null) {
			deviceAisle = new DeviceAisle();
		}
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("productName", SQLUtils.IGNORE);
		BoxValue<String, List<Object>> box = SQLUtils.getCondition(deviceAisle, map, "C");
		List<Object> args = box.getValue();
		args.add(Commons.FILE_PRODUCT);
		if (deviceAisle.getProductName() != null) {
			buf.append(" AND").append(" (P.SKU_NAME LIKE ? OR P.SKU_NAME LIKE ?)");
			args.add("%" + ZHConverter.convert(deviceAisle.getProductName(), ZHConverter.TRADITIONAL) + "%");
			args.add("%" + ZHConverter.convert(deviceAisle.getProductName(), ZHConverter.SIMPLIFIED) + "%");
		}
		if (startDate != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(startDate);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			Timestamp date = new Timestamp(cal.getTimeInMillis());
			buf.append(" AND O.PAY_TIME>=?");
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
			buf.append(" AND O.PAY_TIME<=?");
			args.add(date);
		}
		buf.append(" AND C.ORG_ID = ? ");
		User user = ContextUtil.getUser(User.class);
		if (null == user) throw new BusinessException("当前用户未登录，请登录后再操作");
		args.add(user.getOrgId());
		
		buf.append(" AND O.STATE = ? ");
		args.add(Commons.ORDER_STATE_FINISH);
		
		buf.append(" GROUP BY C.SKU_ID,C.ORG_ID ");
		return genericDao.findTs(DeviceAisle.class, page, buf.toString(), args.toArray());
	}
	
	public List<DeviceAisle> findProducts(Timestamp startDate, Timestamp endDate) {
		
		DeviceAisle deviceAisle = new DeviceAisle();
		
		StringBuffer buf = new StringBuffer("SELECT MAX(C.ID) AS id,SUM(C.QTY) AS sales,MAX(C.PRICE) AS price,");
		buf.append(
				"MAX(C.PRICE)*SUM(C.QTY) AS salesVolume,STRING_AGG(B.ID||','||B.NAME||','||B.TYPE||','||B.REAL_PATH, ';' ORDER BY B.ID DESC) AS images,MAX(P.SKU_NAME) AS productName ");
		buf.append(" FROM T_ORDER_DETAIL C ");
		buf.append(" LEFT JOIN T_ORDER O ON C.ORDER_NO=O.CODE ");
		buf.append(" LEFT JOIN SYS_ORG SO ON C.ORG_ID=SO.ID ");
		buf.append(" LEFT JOIN T_PRODUCT P ON C.SKU_ID=P.ID ");
		buf.append(" LEFT JOIN T_FILE B ON P.ID=B.INFO_ID AND (B.TYPE=?) WHERE 1=1 ");
		
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("productName", SQLUtils.IGNORE);
		BoxValue<String, List<Object>> box = SQLUtils.getCondition(deviceAisle, map, "C");
		List<Object> args = box.getValue();
		args.add(Commons.FILE_PRODUCT);
		if (deviceAisle.getProductName() != null) {
			buf.append(" AND").append(" (P.SKU_NAME LIKE ? OR P.SKU_NAME LIKE ?)");
			args.add("%" + ZHConverter.convert(deviceAisle.getProductName(), ZHConverter.TRADITIONAL) + "%");
			args.add("%" + ZHConverter.convert(deviceAisle.getProductName(), ZHConverter.SIMPLIFIED) + "%");
		}
		if (startDate != null) {			
			buf.append(" AND O.PAY_TIME>=?");
			args.add(startDate);
		}
		if (endDate != null) {			
			buf.append(" AND O.PAY_TIME<=?");
			args.add(endDate);
		}
		buf.append(" AND C.ORG_ID = ? ");
		User user = ContextUtil.getUser(User.class);
		if (null == user) throw new BusinessException("当前用户未登录，请登录后再操作");
		args.add(user.getOrgId());
		
		buf.append(" AND O.STATE = ? ");
		args.add(Commons.ORDER_STATE_FINISH);
		
		buf.append(" GROUP BY C.SKU_ID,C.ORG_ID ");
		return genericDao.findTs(DeviceAisle.class, buf.toString(), args.toArray());
	}
	
	public Device findDeviceById(Long id) {
		if (null == id) return null;
		return genericDao.findT(Device.class, "SELECT D.* FROM T_DEVICE D WHERE 1=1 AND D.ID = ?", id);
	}

}
