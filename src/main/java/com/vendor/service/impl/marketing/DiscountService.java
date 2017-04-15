package com.vendor.service.impl.marketing;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.ecarry.core.dao.IGenericDao;
import com.ecarry.core.dao.SQLUtils;
import com.ecarry.core.exception.BusinessException;
import com.ecarry.core.util.MathUtil;
import com.ecarry.core.web.core.ContextUtil;
import com.ecarry.core.web.core.Page;
import com.vendor.po.ChangeDiscountData;
import com.vendor.po.ChangeDiscountStateData;
import com.vendor.po.DeviceLog;
import com.vendor.po.DeviceRelation;
import com.vendor.po.Discount;
import com.vendor.po.DiscountPeriod;
import com.vendor.po.DiscountProductPointPlace;
import com.vendor.po.OfflineMessage;
import com.vendor.po.Order;
import com.vendor.po.PointPlace;
import com.vendor.po.Product;
import com.vendor.po.User;
import com.vendor.service.IDiscountService;
import com.vendor.util.CommonUtil;
import com.vendor.util.Commons;
import com.vendor.util.DateUtil;
import com.vendor.util.MessagePusher;

@Service("discountService")
public class DiscountService implements IDiscountService {
	
	private final static Logger logger = Logger.getLogger(DiscountService.class);

	@Autowired
	private IGenericDao genericDao;
	
	/*
	 * @Title: *1-分页查询限时打折列表
	 */
	@Override
	public List<Discount> findDisCount(Discount discount, Page page) {
		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("当前用户未登录");
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer("select * from t_discount d where d.org_id=?");
		args.add(user.getOrgId());
		if(null != discount.getTitle()){
			buf.append(" and d.title like ?");
			args.add("%"+discount.getTitle()+"%");
		}
		if(null != discount.getId()){
			buf.append(" and d.id=?");
			args.add(discount.getId());
		}
		buf.append(" order by id desc");
		List<Discount> discountPage = genericDao.findTs(Discount.class, page, buf.toString(), args.toArray());
 		if(null != discountPage && discountPage.size()>0){
			for (int i=0;i<discountPage.size();i++) {
				List<DiscountPeriod> discountPeriods = new ArrayList<DiscountPeriod>();
				List<DiscountPeriod> findTs = genericDao.findTs(DiscountPeriod.class, "select * from T_DISCOUNT_PERIOD dp where dp.DISCOUNT_ID=?", discountPage.get(i).getId());
				if(findTs != null && findTs.size()>0){
					for (DiscountPeriod discountPeriod : findTs) {
						if(discountPeriod.getDiscountValue() !=0 ){
							 double dou=getFormatTwoNum(discountPeriod.getDiscountValue()*10);
							discountPeriod.setDiscountValue(dou);
						}
					}
					discountPeriods.addAll(findTs);
					discountPage.get(i).setDiscountPeriodList(discountPeriods);
				}
			}
		}
		return discountPage;
	}
	
	/* 
	 * @Title: *2-删除/下线打折活动
	 */
	@Override
	public void delDiscount(Long ids,  Integer index) {
		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("当前用户未登录");
		if (ids == null) 
			throw new BusinessException("请求错误!");
		
		if(index == 3){//项目下线
			Date date = new Date();
			//先判断当前时间是否在时间段中，在的话查出当前时间段的折扣值，不在的话直接删除此活动
			Discount discount = genericDao.findT(Discount.class, "SELECT * FROM T_DISCOUNT WHERE ID=? AND STATE=?", ids, "1");//查出进行中的打折活动
			if(null == discount)
				throw new BusinessException("请求错误！");
			genericDao.execute("UPDATE T_DISCOUNT SET STATE=?,REFERRALS_TIME=? WHERE ORG_ID=? AND ID IN(?)", index, new Date(), user.getOrgId(), ids);//先更新活动
			if("1".equals(discount.getDiscountWay())){//全天下线
				SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String format = sf.format(date);
				String startTime = sf.format(discount.getStartTime());
				String endTime = sf.format(discount.getEndTime());
				String[] split = discount.getCycle().split(",");
				boolean contains = Arrays.asList(split).contains(DateUtil.getWeekOfDate(new Date()));// 判断周期中是否包含当前周期(星期几)(包含为true)
				if(contains && format.compareTo(startTime) > 0 && format.compareTo(endTime) < 0 ){
					/** 更新货道商品折扣值 */
					updateDeviceAisle(1.0, discount.getId());
					discountPush(discount, 1.0);//推送消息关闭打折活动,下线时折扣值都设置为1.0
					return;
				}
			}else if("2".equals(discount.getDiscountWay())){//分时段下线
				String[] split = discount.getCycle().split(",");
				boolean contains = Arrays.asList(split).contains(DateUtil.getWeekOfDate(new Date()));// 判断周期中是否包含当前周期(星期几)(包含为true)
				List<DiscountPeriod> discountPeriods = genericDao.findTs(DiscountPeriod.class, "SELECT * FROM T_DISCOUNT_PERIOD T WHERE T.DISCOUNT_ID=?", discount.getId());//查出所有时段
				if(contains){
					for (DiscountPeriod discountPeriod : discountPeriods) {
						SimpleDateFormat sf = new SimpleDateFormat("HH:mm:ss");
						String format = sf.format(date);
						if(format.compareTo(discountPeriod.getDiscount_start()) > 0  && format.compareTo(discountPeriod.getDiscount_end()) < 0){//在时间段范围内说明活动正在进行
							/** 更新货道商品折扣值 */
							updateDeviceAisle(1.0, discount.getId());
							discountPush(discount, 1.0);//推送消息关闭打折活动,下线时折扣值都设置为1.0
							return;
						}
					}
				}
			}
		}else if(index ==9){//项目删除
			Discount discount = genericDao.findT(Discount.class, "SELECT * FROM T_DISCOUNT WHERE ID=?", ids);//查出进行中的打折活动
//			if("1".equals(discount.getState()) || (discount.getStartTime().getTime() <= new Date().getTime() && "0".equals(discount.getState())))
			if(discount.getStartTime().getTime() <= new Date().getTime()){
				if(!"2".equals(discount.getState()) && !"3".equals(discount.getState())){//2:已结束,3:已下线
					throw new BusinessException("活动已在进行中，请刷新页面重新操作！");
				}
			}
			try {
				genericDao.execute("DELETE FROM T_DISCOUNT_PRODUCT_POINTPLACE WHERE DISCOUNT_ID=?",  ids);//删除折扣商品中间表数据
				genericDao.execute("DELETE FROM T_DISCOUNT_PERIOD WHERE DISCOUNT_ID=?", ids);//删除时段表数据
				genericDao.execute("DELETE FROM T_DISCOUNT WHERE ORG_ID=? AND ID=?", user.getOrgId(), ids);//删除折扣表数据
			} catch (Exception e) {
				throw new BusinessException("****【 删除异常】****");
			}
		}
	}
	
	/*
	 * @Title: *3-1.第一步保存新增/编辑对象(保存打折基本信息)
	 */
	@Override
	public Discount saveDiscount(Discount discount) {
		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("当前用户未登录");
		if(null != discount){
			Date date = new Date();
			if(discount.getStartTime().getTime() < date.getTime()){//全天打折活动结束开始时间小于当前时间
				throw new BusinessException("活动开始时间不能小于当前时间!");
			}else if(discount.getEndTime().getTime() < date.getTime()){
				throw new BusinessException("活动结束时间不能小于当前时间!");
			}
			discount.setUserId(ContextUtil.getUser(User.class).getId());//创建人
			//时段表 先删除里面的数据 在保存新数据
			if(null != discount.getId()){
				List<DiscountPeriod> findTs = genericDao.findTs(DiscountPeriod.class, "select id from t_discount_period t where discount_id=?", discount.getId());
				if(findTs != null && findTs.size() > 0){
					genericDao.execute("delete from t_discount_period where discount_id=?", discount.getId());
				}
			}
			if("2".equals(discount.getDiscountWay())){//分时段时判断时段时间间隔
				for(int i=0;i<discount.getDiscountPeriodList().size();i++){
					for(int j=i;j<discount.getDiscountPeriodList().size();j++){
						if(discount.getDiscountPeriodList().get(i).getDiscount_end().equals(discount.getDiscountPeriodList().get(j).getDiscount_start())){
							throw new BusinessException("活动时段至少间隔一分钟!");
						}
					}
				}
			}
			for(DiscountPeriod discountPer : discount.getDiscountPeriodList()){
				if("1".equals(discount.getDiscountWay())){//全天打折
					if(null == discount.getId()){/** 新增打折活动 */
						discount.setState("0");
						discount.setPush("0");
						discount.setUserId(user.getId());
						discount.setOrgId(user.getOrgId());//所属机构
						genericDao.save(discount);
						discountPer.setDiscountId(discount.getId());
						discountPer.setDiscount_start(DateUtil.TIME_START_STR);//活动开始时间
						discountPer.setDiscount_end(DateUtil.TIME_END_STR);//活动结束时间
						if(discountPer.getDiscountValue() !=0 ){
							double dou=getFormatTwoNum(discountPer.getDiscountValue()/10);
							discountPer.setDiscountValue(dou);
						}
						genericDao.save(discountPer);
					}else{/** 修改打折活动 */
						discount.setState("0");
						discount.setPush("0");
						discount.setUserEditId(user.getId());//修改人
						discount.setOrgId(user.getOrgId());//所属机构
						genericDao.update(discount);
						discountPer.setDiscount_start(DateUtil.TIME_START_STR);//活动开始时间
						discountPer.setDiscount_end(DateUtil.TIME_END_STR);//活动结束时间
						discountPer.setDiscountId(discount.getId());
						if(discountPer.getDiscountValue() !=0 ){
							double dou=getFormatTwoNum(discountPer.getDiscountValue()/10);
							discountPer.setDiscountValue(dou);
						}
						genericDao.save(discountPer);
					}
				}else if("2".equals(discount.getDiscountWay())){//=分时段打折
					if(StringUtils.isEmpty(discount.getId())){
						discount.setState("0");
						discount.setPush("0");
						discount.setUserId(user.getId());
						discount.setOrgId(user.getOrgId());//所属机构
						genericDao.save(discount);
						discountPer.setDiscountId(discount.getId());
						if(discountPer.getDiscountValue() !=0 ){
							double dou=getFormatTwoNum(discountPer.getDiscountValue()/10);
							discountPer.setDiscountValue(dou);
						}
						genericDao.save(discountPer);
					}else{
						discount.setPush("0");
						discount.setState("0");
						discount.setUserEditId(user.getId());//修改人
						discount.setOrgId(user.getOrgId());//所属机构
						genericDao.update(discount);
						discountPer.setDiscountId(discount.getId());
						if(discountPer.getDiscountValue() !=0 ){
							double dou=getFormatTwoNum(discountPer.getDiscountValue()/10);
							discountPer.setDiscountValue(dou);
							}
						genericDao.save(discountPer);
						}
					}
				}
		}else{
			throw new BusinessException("请求参数异常!");
		}
		return discount;
	}
	
	/*
	 * @Title: *4.-选择店铺时(查询店铺)
	 */
	@Override
	public List<PointPlace> findPointPlacePage(Discount discount, Long productId, Page page) {
		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("当前用户未登录");
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer();
		if(null != discount.getId()){
			if(null != discount.getState() && "0".equals(discount.getState())){//查询所有的店铺  已选择的置顶	
				Discount discounts = genericDao.findT(Discount.class, "select * from t_discount where id=?", discount.getId());
				if(null != productId){//查询包含此商品的店铺(并且还要选中的置顶)
					buf.append(" SELECT C.ID,C.POINT_NO,C.POINT_NAME,C.POINT_ADDRESS,C.POINT_TYPE,C.ORG_ID,COALESCE(A.SORT,0)AS checked");
					buf.append(" FROM T_POINT_PLACE C");
					buf.append(" LEFT JOIN SYS_ORG SO ON C.ORG_ID=SO.ID");
					buf.append(" LEFT JOIN T_DEVICE D ON D.POINT_ID = C.ID"); 
					buf.append(" LEFT JOIN T_DEVICE_AISLE DA ON DA.DEVICE_ID = D.ID"); 
					buf.append(" LEFT JOIN ("); 
					buf.append(" SELECT C.ID,C.POINT_NO,C.POINT_NAME,C.POINT_ADDRESS,C.POINT_TYPE,C.ORG_ID,T.SORT");
					buf.append(" FROM T_POINT_PLACE C");
					buf.append(" LEFT JOIN SYS_ORG SO ON C.ORG_ID=SO.ID");
					buf.append(" LEFT JOIN T_DEVICE D ON D.POINT_ID = C.ID"); 
					buf.append(" LEFT JOIN T_DEVICE_AISLE DA ON DA.DEVICE_ID = D.ID"); 
					buf.append(" LEFT JOIN T_DISCOUNT_PRODUCT_POINTPLACE T ON T.POINTPLACE_NO=C.POINT_NO"); 
					buf.append(" WHERE T.PRODUCT_NO=? AND T.DISCOUNT_ID=?");
					args.add(productId);
					args.add(discount.getId());
					buf.append(" GROUP BY C.ID,C.POINT_NO,C.POINT_NAME,C.POINT_ADDRESS,C.POINT_TYPE,C.ORG_ID,T.SORT"); 
					buf.append(" ) A ON A.ID=C.ID");  
					buf.append(" WHERE DA.PRODUCT_ID=?");
					args.add(productId);
					
					buf.append(" AND C.POINT_NO NOT IN");
					buf.append(" (");
					buf.append(" SELECT P.POINT_NO FROM T_POINT_PLACE P"); 
 					buf.append(" LEFT JOIN T_DISCOUNT_PRODUCT_POINTPLACE T ON T.POINTPLACE_NO=P.POINT_NO");
					buf.append(" LEFT JOIN T_DISCOUNT DI ON DI.ID=T.DISCOUNT_ID");
 					buf.append(" WHERE DI.ORG_ID=?");
 					args.add(user.getOrgId());
					buf.append(" AND DI.ID  != ?");
					args.add(discounts.getId());
					buf.append(" AND ( ? BETWEEN DI.START_TIME AND DI.END_TIME ");
					args.add(discounts.getStartTime());
					buf.append(" OR ? BETWEEN DI.START_TIME AND DI.END_TIME)");
					args.add(discounts.getEndTime());
					buf.append(" AND T.SORT = 1 ");
					buf.append(" AND DI.STATE NOT IN ('2','3')");
					buf.append(" GROUP BY P.POINT_NO");
					buf.append(" )");
					
					buf.append(" AND C.STATE != ? ");
					args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
					
 					buf.append(" GROUP BY C.ID,C.POINT_NO,C.POINT_NAME,C.POINT_ADDRESS,C.POINT_TYPE,C.ORG_ID,A.SORT");
					buf.append(" ORDER BY A.SORT, C.ID DESC;");

 				}else{//单独查询出所有店铺都不选中
					buf.append(" SELECT PP.ID,PP.POINT_NO,PP.POINT_NAME,PP.POINT_ADDRESS,PP.POINT_TYPE, COALESCE(A.SORT,0) AS checked"); 
					buf.append(" FROM T_POINT_PLACE PP");
					buf.append(" LEFT JOIN");
					buf.append(" (");
					buf.append(" SELECT P.ID,T.SORT,P.POINT_NO,P.POINT_NAME,P.POINT_ADDRESS,P.POINT_TYPE,T.PRODUCT_NO FROM");
					buf.append(" T_POINT_PLACE P,T_DISCOUNT_PRODUCT_POINTPLACE T");
					buf.append(" WHERE P.POINT_NO = T.POINTPLACE_NO");
					buf.append(" AND T.DISCOUNT_ID=?");
					args.add(discounts.getId());
					buf.append(" AND T.SORT = 1 ");
					buf.append(" ) A ON A.ID=PP.ID WHERE PP.ORG_ID=?");
					args.add(user.getOrgId());
					buf.append(" AND PP.POINT_NO NOT IN");
					buf.append(" (");
					buf.append(" SELECT P.POINT_NO FROM T_POINT_PLACE P"); 
					buf.append(" LEFT JOIN T_DISCOUNT_PRODUCT_POINTPLACE T ON T.POINTPLACE_NO=P.POINT_NO");
					buf.append(" LEFT JOIN T_DISCOUNT DI ON DI.ID=T.DISCOUNT_ID");
					buf.append(" WHERE DI.ORG_ID=?");
					args.add(user.getOrgId());
					buf.append(" AND DI.ID  NOT IN(SELECT ID FROM T_DISCOUNT WHERE ID = ?)");
					args.add(discounts.getId());
					buf.append(" AND ( ? BETWEEN DI.START_TIME AND DI.END_TIME ");
					args.add(discounts.getStartTime());
					buf.append(" OR ? BETWEEN DI.START_TIME AND DI.END_TIME)");
					args.add(discounts.getEndTime());
					buf.append(" AND T.SORT = 1 ");
					buf.append(" AND DI.STATE NOT IN ('2','3')");
					buf.append(" GROUP BY P.POINT_NO");
					buf.append(" )");
					buf.append(" AND PP.STATE != ? ");
					args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
					buf.append(" GROUP BY PP.ID,PP.POINT_NO,PP.POINT_NAME,PP.POINT_ADDRESS,PP.POINT_TYPE,A.SORT");
					buf.append(" ORDER BY A.SORT,PP.ID DESC");
				}
			}else if(null != discount.getState() && !"0".equals(discount.getState())){//查询已选择的
				buf.append(" SELECT PP.ID,T.SORT,PP.POINT_NO,PP.POINT_NAME,PP.POINT_ADDRESS,PP.POINT_TYPE,T.PRODUCT_NO");
				buf.append(" FROM T_DISCOUNT_PRODUCT_POINTPLACE T");
				buf.append(" LEFT JOIN T_POINT_PLACE PP ON T.POINTPLACE_NO=PP.POINT_NO");
				buf.append(" WHERE T.DISCOUNT_ID=?");
				args.add(discount.getId());
				if(null != productId){
					buf.append(" AND T.PRODUCT_NO=?");
					args.add(productId);
				}
				buf.append(" AND PP.STATE != ? ");
				args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
				buf.append(" GROUP BY PP.ID,T.SORT,PP.POINT_NO,PP.POINT_NAME,PP.POINT_ADDRESS,PP.POINT_TYPE,T.PRODUCT_NO");
			}
		}else{//查询所有
			buf.append("SELECT PP.ID,PP.POINT_NO,PP.POINT_NAME,PP.POINT_ADDRESS,PP.POINT_TYPE FROM T_POINT_PLACE PP WHERE PP.ORG_ID=?");
			args.add(user.getOrgId());
			buf.append(" AND PP.STATE != ? ");
			args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
		}
		return genericDao.findTs(PointPlace.class, page, buf.toString(), args.toArray());
	}
	
	
	/*
	 * @Title: *5.-选择商品时(查询商品)
	 */
	@Override
	public List<Product> findProductPage(Discount discount, Page page, Integer type){
		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("当前用户未登录");
		
		discount = genericDao.findTById(Discount.class, discount.getId());
		if (null == discount)
			throw new BusinessException("非法请求");
		
		StringBuffer buffer = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		buffer.append("SELECT STRING_AGG(B.ID||','||B.NAME||','||B.TYPE||','||B.REAL_PATH, ';' ORDER BY B.ID DESC) AS images, ");
		buffer.append(" P.id ,P.SKU_NAME,DA.PRODUCT_ID,DA.PRICE, P.TYPE");
		buffer.append(" FROM T_DEVICE_AISLE DA ");
		buffer.append(" LEFT JOIN T_DEVICE D ON D. ID = DA.DEVICE_ID ");
		buffer.append(" LEFT JOIN T_PRODUCT P ON P.ID = DA.PRODUCT_ID AND D.ORG_ID = P.ORG_ID ");
		buffer.append(" LEFT JOIN SYS_ORG O ON P.ORG_ID=O.ID LEFT JOIN T_FILE B ON P.ID=B.INFO_ID AND B.TYPE=? ");
		buffer.append(" WHERE D.ORG_ID = ?  ");
		args.add(user.getOrgId());
		buffer.append(" AND P.STATE!=? ");
		args.add(Commons.PRODUCT_STATE_TRASH);
		buffer.append(" AND D.POINT_ID != 0 ");
		buffer.append(" AND DA.PRODUCT_ID IS NOT NULL ");
		if(null != discount.getId()){
			if(type != null){
				if(type == 1){//查所有已选的
					buffer.append(" AND P.ID IN(SELECT PP.PRODUCT_NO FROM T_DISCOUNT_PRODUCT_POINTPLACE PP WHERE DISCOUNT_ID=? )");
					args.add(discount.getId());
				}else if(type == 2){//排除已选商品
					buffer.append(" AND P.ID NOT IN(SELECT PP.PRODUCT_NO FROM T_DISCOUNT_PRODUCT_POINTPLACE PP WHERE DISCOUNT_ID=? )");
					args.add(discount.getId());
				}
			}
		}
		buffer.append(" GROUP BY ").append(" P.id,P.SKU_NAME,DA.PRODUCT_ID,DA.PRICE, P.TYPE ");
		args.add(0, Commons.FILE_PRODUCT);
		
		return genericDao.findTs(Product.class, page, buffer.toString(), args.toArray());
	}
	
	
	
	/*
	 * @Title: *6-1.保存第二步(保存商铺)
	 */
	@Override
	public void savePointPlaceProduct(Long ids, String[] pointPlaceList, String type, String[] productList) {
		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("当前用户未登录");
		List<Object> args = new ArrayList<Object>();
		if (null == ids)
			throw new BusinessException("请求错误!");
		
		Discount discount = genericDao.findT(Discount.class, "SELECT ID,STATE,START_TIME,END_TIME FROM T_DISCOUNT WHERE ID=?", ids);
		if(null == discount)
			throw new BusinessException("数据异常");
		if("1".equals(discount.getState()))
			throw new BusinessException("活动已在进行中,禁止操作!");
		
		// 这里先查出他已经创建的打折活动
		if (null != pointPlaceList || (null != type && "pointPlace".equals(type))) {// 保存商铺
			delectPointPlace(ids, null);/** TODO 保存商铺 */
			if (null != pointPlaceList) {
				StringBuffer buf = new StringBuffer("INSERT INTO T_DISCOUNT_PRODUCT_POINTPLACE(TYPE,POINTPLACE_NO,DISCOUNT_ID,SORT)VALUES");
				for (String point : pointPlaceList) {
					buf.append("(?,?,?,?),");
					args.add(1);
					args.add(point);
					args.add(ids);
					args.add(1);
				}
				buf.setLength(buf.length() - 1);
				genericDao.execute(buf.toString(), args.toArray());
			}
		} else if (null != productList) {// 保存商品
			StringBuffer buf = new StringBuffer();// 保存商品是查询出所有上架此商品的店铺(属于该机构的)，全选(插入T_discount_product_pointplace表)
//			buf.append(" SELECT P.POINT_NO FROM T_POINT_PLACE P LEFT JOIN t_device D ON D.point_id=P.ID LEFT JOIN t_device_aisle DA ON DA.device_id=D.ID");
//			buf.append(" WHERE DA.product_id=? GROUP BY P .POINT_NO");
			List<PointPlace> points = null;
			List<Object> arg = new ArrayList<Object>();
			StringBuffer buff = new StringBuffer();
			for (String product : productList) {
				args.clear();
				buf.setLength(0);
				buf.append(" SELECT P .POINT_NO FROM T_POINT_PLACE P");
				buf.append(" LEFT JOIN T_DEVICE D ON D.POINT_ID = P . ID");
				buf.append(" LEFT JOIN T_DEVICE_AISLE DA ON DA.DEVICE_ID = D. ID");
				buf.append(" WHERE DA.PRODUCT_ID=?");
				args.add(Long.valueOf(product));
				buf.append(" AND D.POINT_ID NOT IN (");
				buf.append(" SELECT PP.ID FROM  T_DISCOUNT_PRODUCT_POINTPLACE T");
				buf.append(" LEFT JOIN T_DISCOUNT DI ON DI. ID = T .DISCOUNT_ID");
				buf.append(" LEFT JOIN T_POINT_PLACE PP ON PP.POINT_NO = T.POINTPLACE_NO");
				buf.append(" WHERE DI.ORG_ID =?");
				args.add(user.getOrgId());
				buf.append(" AND (? BETWEEN DI.START_TIME AND DI.END_TIME OR ? BETWEEN DI.START_TIME AND DI.END_TIME)");
				args.add(discount.getStartTime());
				args.add(discount.getEndTime());
				buf.append(" AND T .SORT = 1");
				buf.append(" AND DI. ID != ?");
				args.add(ids);
				buf.append(" AND DI. STATE NOT IN ('2', '3')");
				buf.append(" GROUP BY PP.ID");
				buf.append(" )");
				buf.append(" AND P.STATE != ? ");
				args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
				buf.append(" GROUP BY P.POINT_NO");
				points = genericDao.findTs(PointPlace.class, buf.toString(), args.toArray());
				if (null != points && !points.isEmpty()) {
					buff.setLength(0);
					arg.clear();
					buff.append("INSERT INTO T_DISCOUNT_PRODUCT_POINTPLACE(PRODUCT_NO,TYPE,POINTPLACE_NO,DISCOUNT_ID,SORT)VALUES");
					for (PointPlace p : points) {
						buff.append("(?,?,?,?,?),");
						arg.add(Long.valueOf(product));
						arg.add(2);
						arg.add(p.getPointNo());
						arg.add(ids);
						arg.add(1);
					}
				}else{
					buff.setLength(0);
					arg.clear();
					buff.append("INSERT INTO T_DISCOUNT_PRODUCT_POINTPLACE(PRODUCT_NO,TYPE,DISCOUNT_ID,SORT)VALUES");
					buff.append("(?,?,?,?),");
					arg.add(Long.valueOf(product));
					arg.add(2);
					arg.add(ids);
					arg.add(1);
				}
				
				buff.setLength(buff.length() - 1);
				genericDao.execute(buff.toString(), arg.toArray());
			}
		}
	}
	
	/*
	 * @Title: *7.-更新商品(根据商品ID删除商品或者根据商品ID和商铺编号删除商品的特定店铺)
	 */
	@Override
	public void deleteProductPointPlace(Long discountId, Long productId) {
		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("当前用户未登录");
		if (null != discountId && null != productId) {// 根据-活动ID、商品ID、删除商品
			List<Object> args = new ArrayList<Object>();
			StringBuffer buf = new StringBuffer("DELETE FROM T_DISCOUNT_PRODUCT_POINTPLACE DPP WHERE DPP.DISCOUNT_ID=? AND DPP.PRODUCT_NO=?");
			args.add(0, discountId);
			args.add(1, productId);
			genericDao.execute(buf.toString(), args.toArray());
		} else {
			throw new BusinessException("请求参数异常!");
		}
	}
	
	
	/*
	 * @Title: *7-1.更新商品的(店铺)
	 */
	@Override
	public void updateProduct(Discount discount) {
		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("当前用户未登录");
		if(null == discount.getId())
			throw new BusinessException("数据ID异常");
		
		Discount discounts = genericDao.findT(Discount.class, "SELECT STATE FROM T_DISCOUNT WHERE ID=?", discount.getId());
		if(null != discounts && "1".equals(discounts.getState()))
			throw new BusinessException("活动已在进行中,禁止操作!");
		
		List<Object> args = new ArrayList<Object>();
		if (null != discount && null != discount.getProductList() && discount.getProductList().size() > 0) {
			StringBuffer buf = new StringBuffer("DELETE FROM T_DISCOUNT_PRODUCT_POINTPLACE DPP WHERE DPP.DISCOUNT_ID=?");
			args.add(0, discount.getId());
			StringBuffer buff = new StringBuffer(" AND DPP.PRODUCT_NO IN(");
			for (Product product : discount.getProductList()) {
				buff.append("?,");
				args.add(product.getId());
			}
			buff.setLength(buff.length() - 1);
			buff.append(")");
			String IndexS = buff.toString();
			buf.append(IndexS);
			genericDao.execute(buf.toString(), args.toArray());
			buf.setLength(0);
			args.clear();
			buf.append("INSERT INTO T_DISCOUNT_PRODUCT_POINTPLACE(TYPE,POINTPLACE_NO,PRODUCT_NO,DISCOUNT_ID,SORT)VALUES");
			for (Product product : discount.getProductList()) {
				if(null !=product.getPointPlaceList() && !product.getPointPlaceList().isEmpty()){
					String[] split = product.getPointPlaceList().split(",");
					for (String string : split) {
						buf.append("(?,?,?,?,?),");
						args.add(2);
						args.add(string);
						args.add(product.getId());
						args.add(discount.getId());
						if(string.isEmpty()){
							args.add(0);
						}else{
							args.add(1);
						}
					}
				}else{
					buf.append("(?,?,?,?,?),");
					args.add(2);
					args.add(null);
					args.add(product.getId());
					args.add(discount.getId());
					args.add(1);
				}
			}
			buf.setLength(buf.length() - 1);
			genericDao.execute(buf.toString(), args.toArray());
		}
	}
	
	/*
	 * @Title: *8.-删除店铺
	 */
	@Override
	public void delectPointPlace(Long discountId, Long pointPlaceId) {
		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("当前用户未登录");
		if (null != discountId) {
			List<Object> args = new ArrayList<Object>();
			StringBuffer buf = new StringBuffer("delete from t_discount_product_pointplace where discount_id=? ");
			args.add(discountId);
			if (null != pointPlaceId) {
				buf.append(" and pointplace_no=?");
				args.add(pointPlaceId);
			}
			genericDao.execute(buf.toString(), args.toArray());
		} else {
			throw new BusinessException("请求错误!");
		}
	}
	
	/*
	 * @Title:  *9.-这里是根据活动ID查询出订单数据
	 */
	@Override
	public Order findOrderPage(Long discountId) {
		//先根据ID查询出打折关联商品店铺的数据
		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("当前用户未登录");
		List<Object> args = new ArrayList<Object>();
		Discount discount = genericDao.findT(Discount.class, "SELECT * FROM T_DISCOUNT WHERE ID=?", discountId);
		Order orderNum = new Order();
		if(null != discount){
			StringBuffer buf = new StringBuffer("SELECT");
			buf.append("("); 
			buf.append(" select count(o.id) from t_order o"); 
			buf.append(" where o.point_no in");  
			buf.append(" ("); 
			buf.append(" select t.pointplace_no from t_discount_product_pointplace t"); 
			buf.append(" where t.discount_id=? and t.sort=1 group by t.pointplace_no"); 
			args.add(discount.getId());
			buf.append(" )"); 
			buf.append(" and o.org_id=?");
			args.add(user.getOrgId());
			buf.append(" and o.create_time between ? and ?"); 
			args.add(discount.getStartTime());
			if(null != discount.getReferralsTime()){
				args.add(discount.getReferralsTime());
			}else{
				args.add(discount.getEndTime());
			}
			buf.append(" ) totalOrdersNumber");/**下单总数*/
			
			buf.append(" ,(");	
			buf.append(" select count(DISTINCT o.id) from t_order o"); 
			buf.append(" where o.point_no in");  
			buf.append(" ("); 
			buf.append(" select t.pointplace_no from t_discount_product_pointplace t"); 
			buf.append(" where t.discount_id=? and sort=1 group by t.pointplace_no"); 
			args.add(discount.getId());
			buf.append(" )"); 
			buf.append(" and o.state=8");
			buf.append(" and o.org_id=?");
			args.add(user.getOrgId());
			buf.append(" and o.create_time between ? and ?"); 
			args.add(discount.getStartTime());
			if(null != discount.getReferralsTime()){
				args.add(discount.getReferralsTime());
			}else{
				args.add(discount.getEndTime());
			}
			buf.append(" ) dealQuantity");/** 成交订单数 */
			
			buf.append(" ,(");
			buf.append(" select sum(o.amount) from t_order o"); 
			buf.append(" where o.point_no in");  
			buf.append(" ("); 
			buf.append(" select t.pointplace_no from t_discount_product_pointplace t"); 
			buf.append(" where t.discount_id=? and sort=1 group by t.pointplace_no"); 
			args.add(discount.getId());
			buf.append(" )"); 
			buf.append(" and o.org_id=?");
			args.add(user.getOrgId());
			buf.append(" and o.create_time between ? and ?"); 
			args.add(discount.getStartTime());
			if(null != discount.getReferralsTime()){
				args.add(discount.getReferralsTime());
			}else{
				args.add(discount.getEndTime());
			}
			buf.append(" ) totalOrderAmount");/** 下单总金额 */
			
			buf.append(" ,(");
			buf.append(" select sum(o.amount) from t_order o"); 
			buf.append(" where o.point_no in");  
			buf.append(" ("); 
			buf.append(" select t.pointplace_no from t_discount_product_pointplace t"); 
			buf.append(" where t.discount_id=? and sort=1 group by t.pointplace_no"); 
			args.add(discount.getId());
			buf.append(" )"); 
			buf.append(" and o.state=8");
			buf.append(" and o.org_id=?");
			args.add(user.getOrgId());
			buf.append(" and o.create_time between ? and ?"); 
			args.add(discount.getStartTime());
			if(null != discount.getReferralsTime()){
				args.add(discount.getReferralsTime());
			}else{
				args.add(discount.getEndTime());
			}
			buf.append(" ) clinchDealOrder");/** 成交总金额 */
			
			buf.append(" ,(");
			buf.append(" select count(o.id)  from t_order o");
			buf.append(" left join t_we_user wu on wu.open_id=o.username");
			buf.append(" where o.point_no in");
			buf.append("(");
			buf.append(" select t.pointplace_no from t_discount_product_pointplace t");
			buf.append(" where t.discount_id=? and sort=1 group by t.pointplace_no");
			args.add(discount.getId());
			buf.append(")");
			buf.append(" and o.org_id=?");
			args.add(user.getOrgId());
			buf.append(" and o.state=8");
			buf.append(" and o.create_time between ? and ?");
			args.add(discount.getStartTime());
			if(null != discount.getReferralsTime()){
				args.add(discount.getReferralsTime());
			}else{
				args.add(discount.getEndTime());
			}
			buf.append(" and wu.create_time between ? and ?");
			args.add(discount.getStartTime());
			if(null != discount.getReferralsTime()){
				args.add(discount.getReferralsTime());
			}else{
				args.add(discount.getEndTime());
			}
			buf.append(" ) newClinchDealOrders");/** 新用户成交订单数 */
			
			buf.append(" ,(");
			buf.append(" select sum(o.amount)  from t_order o");
			buf.append(" left join t_we_user wu on wu.open_id=o.username");
			buf.append(" where o.point_no in");
			buf.append("(");
			buf.append(" select t.pointplace_no from t_discount_product_pointplace t");
			buf.append(" where t.discount_id=? and sort=1 group by t.pointplace_no");
			args.add(discount.getId());
			buf.append(")");
			buf.append(" and o.org_id=?");
			args.add(user.getOrgId());
			buf.append(" and o.state=8");
			buf.append(" and o.create_time between ? and ?");
			args.add(discount.getStartTime());
			if(null != discount.getReferralsTime()){
				args.add(discount.getReferralsTime());
			}else{
				args.add(discount.getEndTime());
			}
			buf.append(" and wu.create_time between ? and ?");
			args.add(discount.getStartTime());
			if(null != discount.getReferralsTime()){
				args.add(discount.getReferralsTime());
			}else{
				args.add(discount.getEndTime());
			}
			buf.append(" ) newClinchDealAmount");/** 新用户成交总金额 */
			
			buf.append(" ,(");
			buf.append(" select count(wu.open_id) from t_we_user wu");
			buf.append(" where wu.open_id in");
			buf.append("(");
			buf.append(" select o.username from t_order o");
			buf.append(" where o.point_no in");
			buf.append("(");
			buf.append(" select t.pointplace_no from t_discount_product_pointplace t"); 
			buf.append(" where t.discount_id=? and sort=1 group by t.pointplace_no");
			args.add(discount.getId());
			buf.append(")");
			buf.append(" and o.org_id=?");
			args.add(user.getOrgId());
			buf.append(" and o.create_time between ? and ?");
			args.add(discount.getStartTime());
			if(null != discount.getReferralsTime()){
				args.add(discount.getReferralsTime());
			}else{
				args.add(discount.getEndTime());
			}
			buf.append(")");
			buf.append(" and wu.create_time between ? and ?");
			args.add(discount.getStartTime());
			if(null != discount.getReferralsTime()){
				args.add(discount.getReferralsTime());
			}else{
				args.add(discount.getEndTime());
			}
			buf.append(") newUserNumber");/** 新用户数 */

			buf.append(" ,(");
			buf.append(" select count(distinct o.username)  from t_order o");
			buf.append(" where o.point_no in");
			buf.append("(");
			buf.append(" select t.pointplace_no from t_discount_product_pointplace t");
			buf.append(" where t.discount_id=? and sort=1 group by t.pointplace_no");
			args.add(discount.getId());
			buf.append(")");
			buf.append(" and o.org_id=?");
			args.add(user.getOrgId());
			buf.append(" and o.state=8");
			buf.append(" and o.create_time between ? and ?");
			args.add(discount.getStartTime());
			if(null != discount.getReferralsTime()){
				args.add(discount.getReferralsTime());
			}else{
				args.add(discount.getEndTime());
			}
			buf.append(" ) totalCustom");/** 成交用户数 */
			
			buf.append(" ,(");
			buf.append(" select sum(od.qty) from t_order_detail od");
			buf.append(" where od.order_no in");
			buf.append(" (");
			buf.append(" select o.code from t_order o");
			buf.append(" left join t_we_user wu on wu.open_id=o.username");
			buf.append(" where o.point_no in");
			buf.append(" (");
			buf.append(" select t.pointplace_no from t_discount_product_pointplace t");
			buf.append(" where t.discount_id=? and sort=1 group by t.pointplace_no");
			args.add(discount.getId());
			buf.append(" )");
			buf.append(" and o.org_id=?");
			args.add(user.getOrgId());
			buf.append(" and o.state=8");
			buf.append(" and o.create_time between ? and ?");
			args.add(discount.getStartTime());
			if(null != discount.getReferralsTime()){
				args.add(discount.getReferralsTime());
			}else{
				args.add(discount.getEndTime());
			}
			buf.append(" group by o.code ");
			buf.append(" )");
			buf.append(" ) totalProductNum");/** 成交商品数 */
			
			orderNum = genericDao.findT(Order.class, buf.toString(), args.toArray());
			if(null != orderNum.getDealQuantity() && orderNum.getDealQuantity()!=0 && null != orderNum.getTotalCustom() && orderNum.getTotalCustom()!=0){
				orderNum.setCapitaOrders(orderNum.getDealQuantity()/orderNum.getTotalCustom());/** 人均成交订单数 */
			}
			if(null != orderNum.getClinchDealOrder() && orderNum.getClinchDealOrder() != 0 && null != orderNum.getTotalCustom() && orderNum.getTotalCustom() != 0){
				orderNum.setCapitaAmount(MathUtil.round(MathUtil.div(orderNum.getClinchDealOrder(), orderNum.getTotalCustom()), 2));/** 人均成交额 */
			}
			if(null != orderNum.getTotalProductNum() && orderNum.getTotalProductNum() != 0 &&  null != orderNum.getTotalCustom() && orderNum.getTotalCustom() != 0){
				orderNum.setCapitaProducts(orderNum.getTotalProductNum()/orderNum.getTotalCustom());/** 人均成交商品数 */
			}
			if(null != orderNum.getTotalOrderAmount() && orderNum.getTotalOrderAmount() !=0 ){
				orderNum.setTotalOrderAmount(MathUtil.round(orderNum.getTotalOrderAmount(), 2));
			}
			if(null != orderNum.getClinchDealOrder() && orderNum.getClinchDealOrder() !=0 ){
				orderNum.setClinchDealOrder(MathUtil.round(orderNum.getClinchDealOrder(), 2));
			}
			if(null != orderNum.getNewClinchDealAmount() && orderNum.getNewClinchDealAmount() !=0 ){
				orderNum.setNewClinchDealAmount(MathUtil.round(orderNum.getNewClinchDealAmount(), 2));
			}
			
		}
		return orderNum;
	}
	
	/*
	 * 推送：有一个推送字段用来判断是否已推送，可以先判断是全天还是时段，然后判断开始于结束时间，在判断星期几，然后时段
	 * 
	 * @see com.vendor.dao.IDiscountService#updateDiscountStateJob()
	 */
	public void updateDiscountStateJob() {
		// 查询出未开始与开始的打折活动
		Date date = new Date();// 当前时间
		List<Discount> discountList = findDicountList();
		if (null != discountList && discountList.size() > 0) {
			for (Discount discount : discountList) {
				/** 全天打折活动 */
				if ("1".equals(discount.getDiscountWay())) {// 全天打折
					// 当前时间等于打折开始时间且推送状态为0(未推送),活动状态为0(未开始)
					if (date.getTime() >= discount.getStartTime().getTime() && date.getTime() < discount.getEndTime().getTime()) {
						if ("0".equals(discount.getState()) && "0".equals(discount.getPush())) {// 不管活动时段有没有到，只要活动时间到了更新活动状态
							genericDao.execute("UPDATE T_DISCOUNT SET STATE=1 WHERE ID=?", discount.getId());// 时间到了就更新活动状态
						}
						String[] split = discount.getCycle().split(",");// 这个是打折活动的周期
						boolean contains = Arrays.asList(split).contains(DateUtil.getWeekOfDate(new Date()));// 判断周期中是否包含当前周期(星期几)(包含为true)
						if (contains && ("0".equals(discount.getState()) || "1".equals(discount.getState()))
								&& "0".equals(discount.getPush())) {
							logger.info("******全天更新为已开始【 活动ID:" + discount.getId() + ",当前时段：" + date + "】");
							genericDao.execute("UPDATE T_DISCOUNT SET PUSH=1 WHERE ID=?", discount.getId());
							Double discountValue = discount.getDiscountPeriodList().get(0).getDiscountValue();
							updateDeviceAisle(discountValue, discount.getId());//更新货道商品折扣值 
							discountPush(discount, discountValue);

						} else if (!contains && "1".equals(discount.getState()) && "1".equals(discount.getPush())) {
							
							logger.info("******全天更新为暂停状态【 活动ID:" + discount.getId() + ",当前时段：" + date + "】");
							genericDao.execute("UPDATE T_DISCOUNT SET PUSH=0 WHERE ID=?", discount.getId());
							updateDeviceAisle(1.0, discount.getId());//更新货道商品折扣值 
							discountPush(discount, 1.0);//推送消息关闭打折活动,下线时折扣值都设置为1.0
						}
					}
					// 当前时间等于打折结束时间且推送状态为1(已推送),活动状态为2(已结束)
					if (date.getTime() >= discount.getEndTime().getTime()/* && "1".equals(discount.getState()) */) {
						logger.info("******全天更新为已结束【 活动ID:" + discount.getId() + ",当前时段：" + date + "】");
						genericDao.execute("UPDATE T_DISCOUNT SET STATE=2,PUSH=1 WHERE ID=?", discount.getId());
						updateDeviceAisle(1.0, discount.getId());//更新货道商品折扣值 
						discountPush(discount, 1.0);//推送消息关闭打折活动,下线时折扣值都设置为1.0

					}
				}
				/** 分时段打折 */
				if ("2".equals(discount.getDiscountWay())) {
					// 分时段开始时间
					if (date.getTime() >= discount.getStartTime().getTime()
							&& date.getTime() < discount.getEndTime().getTime()) {
						for (DiscountPeriod period : discount.getDiscountPeriodList()) {
							if ("0".equals(discount.getState()) && "0".equals(discount.getPush())) {// 不管活动时段有没有到，只要活动时间到了更新活动状态
								genericDao.execute("UPDATE T_DISCOUNT SET STATE=1 WHERE ID=?", discount.getId());// 时间到了就更新活动状态
							}
							String[] split = discount.getCycle().split(",");
							boolean contains = Arrays.asList(split).contains(DateUtil.getWeekOfDate(new Date()));// 判断周期中是否包含当前周期(星期几)(包含为true)
							if (contains) {
								if (DateUtil.dateToDateString(date, "HH:mm:ss").equals(period.getDiscount_start()) && ("0".equals(discount.getState()) || "1".equals(discount.getState()))
										&& "0".equals(discount.getPush())) {// 判断开始时段
									
									logger.info("******分时段更新为进行中【 活动ID:" + discount.getId() + ",当前时段：" + date + "】");
									genericDao.execute("UPDATE T_DISCOUNT SET PUSH=1 WHERE ID=?", discount.getId());// 时间到了就更新活动状态
									updateDeviceAisle(period.getDiscountValue(), discount.getId());//更新货道商品折扣值 
									discountPush(discount, period.getDiscountValue());//推送消息
									
								}
								if (DateUtil.dateToDateString(date, "HH:mm:ss").equals(period.getDiscount_end())) {// 判断结束时段
									
									logger.info("******分时段更新为暂停状态【 活动ID:" + discount.getId() + ",当前时段：" + date + "】");
									genericDao.execute("UPDATE T_DISCOUNT SET PUSH=0 WHERE ID=?", discount.getId());
									updateDeviceAisle(1.0, discount.getId());//更新货道商品折扣值 
									discountPush(discount, 1.0);//推送消息关闭打折活动,下线时折扣值都设置为1.0
									
								}
							} else if (!contains) {// 不包含的周期执行
								
								logger.info("******分时段更新【不包含的周期执行】暂停状态【 活动ID:" + discount.getId() + ",当前时段：" + date + "】");
								genericDao.execute("UPDATE T_DISCOUNT SET PUSH=0 WHERE ID=?", discount.getId());
								updateDeviceAisle(1.0, discount.getId());//更新货道商品折扣值 
								discountPush(discount, 1.0);//推送消息关闭打折活动,下线时折扣值都设置为1.0
							}
						}
					}
					// 分时段活动时间(如果到活动结束时间，先更新数据库，要推送的话判断周期是否符合，然后判断当前时间段是否是在活动时间段中，再去推送消息)
					if (date.getTime() >= discount.getEndTime().getTime()) {
						logger.info("******分时段更新为已结束【 活动ID:" + discount.getId() + ",当前时段：" + date + "】");
						genericDao.execute("UPDATE T_DISCOUNT SET STATE=2,PUSH=1 WHERE ID=?", discount.getId());
						for (DiscountPeriod discountPeriod : discount.getDiscountPeriodList()) {
							SimpleDateFormat sf = new SimpleDateFormat("HH:mm:ss");
							try {
								Date format = sf.parse(sf.format(date));// 当前时段
								Date discount_start = sf.parse(discountPeriod.getDiscount_start());// 活动开始时段
								Date discount_end = sf.parse(discountPeriod.getDiscount_end());// 活动结束时段
								String[] split = discount.getCycle().split(",");
								boolean contains = Arrays.asList(split).contains(DateUtil.getWeekOfDate(new Date()));// 判断周期中是否包含当前周期(星期几)(包含为true)
								if (contains && format.getTime() > discount_start.getTime() && format.getTime() < discount_end.getTime()) {// 在时间段范围内说明活动正在进行
									updateDeviceAisle(1.0, discount.getId());
									discountPush(discount, 1.0);//推送消息关闭打折活动,下线时折扣值都设置为1.0
									return;
								}
							} catch (ParseException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
		logger.info("*******【 定时器更新打折活动 end 】******");
	}
	
	
	/* 
	 * 定时执行离线信息 
	 * @see com.vendor.dao.IDiscountService#updateOfflineStateJob()
	 */
	@Override
	public void updateOfflineStateJob() {
		List<OfflineMessage> offlineMessages = genericDao.findTs(OfflineMessage.class, "SELECT * FROM T_OFFLINE_MESSAGE");
		/** TODO  在这里查询设备是否离线， */
		StringBuffer buf = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		for (OfflineMessage offlineMessage : offlineMessages) {
			buf.setLength(0);
			args.clear();
			String devNos = offlineMessage.getDevNos();
			buf.append(" SELECT LOG.ID FROM T_DEVICE_LOG LOG WHERE LOG.DEVICE_NO = (");
			buf.append(" SELECT DEV_NO FROM T_DEVICE_RELATION DR WHERE DR.FACTORY_DEV_NO=?");
			args.add(devNos);
			buf.append(" ) AND LOG.DEVICE_STATUS=2");
			DeviceLog discountLog = genericDao.findT(DeviceLog.class, buf.toString(), args.toArray());
			if(null == discountLog){//只有当deviceLog表中此设备device_state状态不为2的时候才可以发信息
				//主动通知
				MessagePusher pusher = new MessagePusher();
				try {
					logger.info("【离线消息数据格式：设备编号:"+Arrays.asList(devNos)+",["+offlineMessage.getOfflines()+"]】");
					pusher.pushMessageToAndroidDevices(Arrays.asList(devNos), offlineMessage.getOfflines(), false);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					throw new BusinessException("离线消息推送失败！");
				}
			}
		}
	}
	
	
	
	/**
	 * 打折推送参数方法
	 * @param discount
	 * @param discountValue
	 * @param isOpen
	 */
	public void discountPush(Discount discount, Double discountValue) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer();
		if (null != discount.getDiscountType() && "2".equals(discount.getDiscountType())) {// 获取选中商品的商铺1:按店铺,2:按商品
			// 先通过活动Id查询出设备编号
			buf.append(" SELECT RE.FACTORY_DEV_NO FROM T_DEVICE DE LEFT JOIN T_POINT_PLACE PP ON DE.POINT_ID=PP.ID");
			buf.append(" LEFT JOIN T_DEVICE_AISLE DA ON DA.DEVICE_ID=DE.ID");
			buf.append(" LEFT JOIN T_PRODUCT DP ON DP.ID=DA.PRODUCT_ID");
			buf.append(" LEFT JOIN T_DEVICE_RELATION RE ON RE.DEV_NO=DE.DEV_NO");
			buf.append(" LEFT JOIN T_DISCOUNT_PRODUCT_POINTPLACE T ON T.POINTPLACE_NO=PP.POINT_NO");
			buf.append(" WHERE 1=1");
			buf.append(" AND T.DISCOUNT_ID=?");
			args.add(discount.getId());
			buf.append(" AND PP.STATE != ? ");
			args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
			buf.append(" GROUP BY RE.FACTORY_DEV_NO");
			List<DeviceRelation> deviceRelationList = genericDao.findTs(DeviceRelation.class, buf.toString(), args.toArray());
			if (null != deviceRelationList && deviceRelationList.size() > 0) {
				for (DeviceRelation deviceRelation : deviceRelationList) {
					List<ChangeDiscountData> lists = new ArrayList<ChangeDiscountData>();// 商品集合
					List<String> devNos = new ArrayList<String>();
					buf.setLength(0);
					args.clear();
					buf.append(" SELECT P.CODE, DA.PRICE_ON_LINE priceOnLine, DA.SELLABLE sellable"); 
					buf.append(" FROM T_PRODUCT P");
					buf.append(" LEFT JOIN T_DEVICE_AISLE DA ON DA.PRODUCT_ID = P.ID");
					buf.append(" LEFT JOIN T_DEVICE D ON D.ID = DA.DEVICE_ID");
					buf.append(" LEFT JOIN T_DEVICE_RELATION DR ON DR.DEV_NO = D.DEV_NO"); 
					buf.append(" WHERE 1=1 AND DR.FACTORY_DEV_NO=?");
					args.add(deviceRelation.getFactoryDevNo());
					buf.append(" AND DA.PRODUCT_ID IS NOT NULL");
					buf.append(" AND DA.PRODUCT_ID IN");
					buf.append(" (");
					buf.append(" SELECT PRODUCT_NO");
					buf.append(" FROM T_DISCOUNT_PRODUCT_POINTPLACE t");
					buf.append(" WHERE t.DISCOUNT_ID=?");
					args.add(discount.getId());
					buf.append(" AND t.SORT = 1");
					buf.append(" AND T.POINTPLACE_NO IN");
					buf.append(" (");
					buf.append(" SELECT PP.POINT_NO FROM T_DEVICE DE");
					buf.append(" LEFT JOIN T_POINT_PLACE PP ON DE.POINT_ID=PP.ID");
					buf.append(" LEFT JOIN T_DEVICE_AISLE DA ON DA.DEVICE_ID=DE.ID");
					buf.append(" LEFT JOIN T_PRODUCT DP ON DP.ID=DA.PRODUCT_ID");
					buf.append(" LEFT JOIN T_DEVICE_RELATION RE ON RE.DEV_NO=DE.DEV_NO");
					buf.append(" LEFT JOIN T_DISCOUNT_PRODUCT_POINTPLACE T ON T.POINTPLACE_NO=PP.POINT_NO");
					buf.append(" WHERE RE.FACTORY_DEV_NO=?");
					args.add(deviceRelation.getFactoryDevNo());
					buf.append(" GROUP BY PP.POINT_NO");
					buf.append(" )");
					buf.append(" GROUP BY T.PRODUCT_NO");
					buf.append(" )");
					buf.append(" GROUP BY P.CODE, DA.PRICE_ON_LINE, DA.SELLABLE");

					List<Product> productList = genericDao.findTs(Product.class, buf.toString(), args.toArray());
					if (null != productList && productList.size() > 0) {
						for (Product product : productList) {// 这里放商品号
							ChangeDiscountData discountData = new ChangeDiscountData();
							discountData.setProductNo(product.getCode());
							discountData.setZhekou_num(discountValue);
							double priceOnLine = product.getPriceOnLine();
							if(null != product.getPriceOnLine() && 0 != discountValue){
								priceOnLine = MathUtil.round(MathUtil.mul(priceOnLine, discountValue), 2);
							}
							discountData.setDeletePrice(product.getPriceOnLine());// 零售价
							discountData.setPrice(priceOnLine);//打折后的价格
							discountData.setState(product.getSellable());
							lists.add(discountData);
						}
					}
					devNos.add(deviceRelation.getFactoryDevNo());// 设备编号
					pushChangeProductStateMessage(devNos, lists);// 开始推送
				}
			}
		} else if (null != discount.getDiscountType() && "1".equals(discount.getDiscountType())) {// 1:按店铺,2:按商品
			buf.append(" SELECT RE.FACTORY_DEV_NO FROM T_DEVICE DE LEFT JOIN T_POINT_PLACE PP ON DE.POINT_ID=PP.ID");
			buf.append(" LEFT JOIN T_DEVICE_AISLE DA ON DA.DEVICE_ID=DE.ID");
			buf.append(" LEFT JOIN T_DEVICE_RELATION RE ON RE.DEV_NO=DE.DEV_NO");
			buf.append(" WHERE PP.POINT_NO in");
			buf.append(" (");
			buf.append(" SELECT T.POINTPLACE_NO");
			buf.append(" FROM T_DISCOUNT_PRODUCT_POINTPLACE T");
			buf.append(" WHERE T.DISCOUNT_ID=? AND T.SORT=1");
			args.add(discount.getId());
			buf.append(" )");
			buf.append(" AND PP.STATE != ? ");
			args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
			buf.append("  GROUP BY RE.FACTORY_DEV_NO");
			List<DeviceRelation> deviceRelationList = genericDao.findTs(DeviceRelation.class, buf.toString(), args.toArray());
			if (null != deviceRelationList && deviceRelationList.size() > 0) {
				for(DeviceRelation dev : deviceRelationList){
					List<ChangeDiscountData> lists = new ArrayList<ChangeDiscountData>();// 商品集合
					List<String> devNos = new ArrayList<String>();
					buf.setLength(0);
					args.clear();
					buf.append(" SELECT T.CODE,DA.PRICE_ON_LINE priceOnLine, DA.SELLABLE sellable FROM T_PRODUCT T");
					buf.append(" LEFT JOIN T_DEVICE_AISLE DA ON DA.PRODUCT_ID=T.ID");
					buf.append(" LEFT JOIN T_DEVICE DE ON DE.ID=DA.DEVICE_ID");
					buf.append(" LEFT JOIN T_DEVICE_RELATION RE ON RE.DEV_NO=DE.DEV_NO");
					buf.append(" WHERE RE.FACTORY_DEV_NO=?");
					args.add(dev.getFactoryDevNo());
					buf.append(" GROUP BY T.CODE,DA.PRICE_ON_LINE, DA.SELLABLE");
					List<Product>  productList = genericDao.findTs(Product.class,buf.toString(), args.toArray());
					for(Product product : productList){
						ChangeDiscountData discountData = new ChangeDiscountData();
						discountData.setProductNo(product.getCode());
						discountData.setZhekou_num(discountValue);
						double priceOnLine = product.getPriceOnLine();
						if(null != product.getPriceOnLine() && 0 != discountValue){
							priceOnLine = MathUtil.round(MathUtil.mul(priceOnLine, discountValue), 2);
						}
						discountData.setDeletePrice(product.getPriceOnLine());// 零售价
						discountData.setPrice(priceOnLine);//打折后的价格
						discountData.setState(product.getSellable());
						lists.add(discountData);
					}
					devNos.add(dev.getFactoryDevNo());
					pushChangeProductStateMessage(devNos, lists);// 开始推送
				}
			}
		}
	}
	
	
	/**
	 * 推送商品改价信息
	 * @param devNos  设备编号集合
	 * @param lists 商品、折扣集合
	 * @param discountValue 折扣值
	 * @param isOpen 是否开启活动1关闭活动:,0:开启活动
	 * @param isAll: 0所有商品打折 1 商品列表打折
	 */
	public void pushChangeProductStateMessage(List<String> devNos, List<ChangeDiscountData> lists) {
		ChangeDiscountStateData data = new ChangeDiscountStateData();
		data.setNotifyFlag(Commons.NOTIFY_DISCOUNT_ISOPEN);// 打折活动开关变更通知flag
		data.setTime(new Timestamp(System.currentTimeMillis()));
		
		List<List<ChangeDiscountData>> fatherlist = CommonUtil.fatherList(lists, 15);
		
		String[] devNosArr = devNos.toArray(new String[]{});
		String devNo = CommonUtil.converToString(devNosArr, ",");
		for (List<ChangeDiscountData> list : fatherlist) {
			OfflineMessage offlines = new OfflineMessage();
			data.setList(list);
			offlines.setOfflines(ContextUtil.getJson(data));//离线消息
			offlines.setDevNos(devNo);//设备号
			genericDao.save(offlines);//保存离线时数据
			data.setMessageId(offlines.getId());
			offlines.setOfflines(ContextUtil.getJson(data));
			genericDao.update(offlines);//更新离线时数据字段messageId
			
			String json = ContextUtil.getJson(data);
			if(json != null && !"".equals(json) && json.length() > 1000){
				genericDao.delete(offlines);
				List<List<ChangeDiscountData>> fatherlists = CommonUtil.fatherList(list, 10);
				for (List<ChangeDiscountData> list2 : fatherlists) {
					OfflineMessage offline = new OfflineMessage();
					data.setList(list2);
					offline.setOfflines(ContextUtil.getJson(data));//离线消息
					offline.setDevNos(devNo);//设备号
					genericDao.save(offline);//保存离线时数据
					data.setMessageId(offline.getId());
					offline.setOfflines(ContextUtil.getJson(data));
					genericDao.update(offline);//更新离线时数据字段messageId
					//主动通知
					MessagePusher pusher = new MessagePusher();
					try {
						logger.info("【打折推送消息数据格式：设备编号:"+devNo+",["+ContextUtil.getJson(data)+"]】");
						pusher.pushMessageToAndroidDevices(devNos, ContextUtil.getJson(data), false);
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
						throw new BusinessException("打折推送失败！");
					}
				}
			}else{
				//主动通知
				MessagePusher pusher = new MessagePusher();
				try {
					logger.info("【打折推送消息数据格式：设备编号:"+devNo+",["+ContextUtil.getJson(data)+"]】");
					pusher.pushMessageToAndroidDevices(devNos, ContextUtil.getJson(data), false);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					throw new BusinessException("打折推送失败！");
				}
			}
		}
	}
	
	public static double getFormatTwoNum(double num) {
		java.text.DecimalFormat df = new java.text.DecimalFormat("#.##");
		return Double.valueOf(df.format(num));

	}
	
	/**
	 * 获取需要更新的打折数据
	 * @return
	 */
	public List<Discount> findDicountList(){
		StringBuffer buffer = new StringBuffer("SELECT ");
		buffer.append(SQLUtils.getColumnsSQL(Discount.class, "A"));
		buffer.append(" FROM T_DISCOUNT A WHERE A.STATE IN ('0','1')");
		List<Discount> discountPage = genericDao.findTs(Discount.class, buffer.toString());
		if(null != discountPage && discountPage.size()>0){
			for (Discount discount : discountPage) {
				List<DiscountPeriod> discountPeriods = new ArrayList<DiscountPeriod>();
				List<DiscountPeriod> findTs = genericDao.findTs(DiscountPeriod.class, "select * from T_DISCOUNT_PERIOD dp where dp.DISCOUNT_ID=?", discount.getId());
				if(findTs != null && findTs.size()>0){
					discountPeriods.addAll(findTs);
					discount.setDiscountPeriodList(discountPeriods);
				}
			}
		}
		return discountPage;
	}
	
	/**
	 * 更新货道商品的折扣值
	 * @param discountValue 折扣值
	 * @param discountId 活动ID
	 */
	public void updateDeviceAisle(Double discountValue, Long discountId){
		if(null == discountValue || null == discountId)
			throw new BusinessException("请求错误");
		
		List<DiscountProductPointPlace> discountProductPointPlace = genericDao.findTs(DiscountProductPointPlace.class, " SELECT * FROM T_DISCOUNT_PRODUCT_POINTPLACE WHERE SORT=1 AND DISCOUNT_ID = ? AND POINTPLACE_NO IS NOT NULL", discountId);
		if(null != discountProductPointPlace && !discountProductPointPlace.isEmpty()) {
			//更新打折中间表打折值
			genericDao.execute("UPDATE T_DISCOUNT_PRODUCT_POINTPLACE SET DISCOUNT_VALUE=? WHERE SORT=1 AND DISCOUNT_ID=? AND POINTPLACE_NO IS NOT NULL", discountValue, discountId);
			
			StringBuffer buf = new StringBuffer();
			List<Object> args = new ArrayList<Object>();
			buf.append(" UPDATE T_DEVICE_AISLE");
			buf.append(" SET DISCOUNT_VALUE = ?");
			args.add(discountValue);
			buf.append(" WHERE 1 = 1");
			buf.append(" AND PRODUCT_ID IS NOT NULL");
			
			if (2 == discountProductPointPlace.get(0).getType()) {// 按商品打折
				buf.append(" AND PRODUCT_ID IN (");
				buf.append(" SELECT PRODUCT_NO");
				buf.append(" FROM T_DISCOUNT_PRODUCT_POINTPLACE");
				buf.append(" WHERE");
				buf.append(" DISCOUNT_ID = ?");
				args.add(discountId);
				buf.append(" AND SORT = 1");
				buf.append(" )");
			}
			
			buf.append(" AND DEVICE_ID IN (");
			buf.append(" SELECT ID FROM T_DEVICE WHERE POINT_ID != 0");
			buf.append(" AND POINT_ID IN (");
			buf.append(" SELECT PP.ID");
			buf.append(" FROM T_DISCOUNT_PRODUCT_POINTPLACE DPP,T_POINT_PLACE PP");
			buf.append(" WHERE DPP.POINTPLACE_NO = PP.POINT_NO AND DPP.DISCOUNT_ID = ? AND DPP.SORT = 1");
			args.add(discountId);
			buf.append(" )");
			buf.append(" )");
			genericDao.execute(buf.toString(), args.toArray());
		}
		
	}

	/* 
	 * 删除离线的打折推送信息
	 * @see com.vendor.dao.IDiscountService#deleteOffLineMessage(java.lang.Long)
	 */
	@Override
	public void deleteOffLineMessage(Long id) throws Exception {
		try {
			genericDao.execute("DELETE FROM T_OFFLINE_MESSAGE WHERE ID=?", id);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
}
