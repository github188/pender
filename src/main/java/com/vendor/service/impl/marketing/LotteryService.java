package com.vendor.service.impl.marketing;

import java.sql.Timestamp;
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
import com.vendor.po.Cabinet;
import com.vendor.po.ChangeLotteryData;
import com.vendor.po.ChangeLotteryStateData;
import com.vendor.po.Device;
import com.vendor.po.Lottery;
import com.vendor.po.LotteryDevNoProduct;
import com.vendor.po.LotteryProduct;
import com.vendor.po.Order;
import com.vendor.po.OrderDetail;
import com.vendor.po.Orgnization;
import com.vendor.po.Product;
import com.vendor.po.User;
import com.vendor.service.IDictionaryService;
import com.vendor.service.ILotteryService;
import com.vendor.service.IOrgnizationService;
import com.vendor.util.CommonUtil;
import com.vendor.util.Commons;
import com.vendor.util.MessagePusher;
import com.vendor.util.RandomUtil;

@Service("lotteryService")
public class LotteryService implements ILotteryService {

	private final static Logger logger = Logger.getLogger(LotteryService.class);

	@Autowired
	private IGenericDao genericDao;
	
	@Autowired
	private IDictionaryService dictionaryService;

	@Autowired
	private IOrgnizationService orgnizationService;

	/* @Title: 【1】.分页查询抽奖活动列表
	 * @param lotteryName
	 * @param page
	 * @return
	 * @see com.vendor.service.ILotteryService#findLotteryPage(java.lang.String, com.ecarry.core.web.core.Page)
	 */
	@Override
	public List<Lottery> findLotteryPage(String lotteryName, Page page) {
		logger.info("********* 【 获取抽奖活动分页数据 start 】********* ");
		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("当前用户未登录");

		StringBuffer buf = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		buf.append(" SELECT ID,LOTTERY_NAME,CREATE_TIME,WARM_UP_TIME,START_TIME,END_TIME,STATE,IS_PUBLISH");
		buf.append(" FROM T_LOTTERY WHERE ORG_ID=?");
		args.add(user.getOrgId());
		if (null != lotteryName) {
			buf.append(" AND LOTTERY_NAME LIKE ?");
			args.add("%" + lotteryName + "%");
		}
		buf.append("  ORDER BY CREATE_TIME DESC");
		List<Lottery> lotteryList = genericDao.findTs(Lottery.class, page, buf.toString(), args.toArray());
		logger.info("********* 【 获取抽奖活动分页数据 end 】********* ");
		return lotteryList;
	}

	/**
	 * @Title: 根据抽奖活动ID查询出活动内容
	 * @param lotteryId
	 *            －活动ID
	 * @param sqlString
	 *            －需要查询的字段,以LP.开头,如果查询所有传null
	 * @param lotteryProductId
	 *            －活动内容ID
	 * @return: List<LotteryProduct>
	 */
	private List<LotteryProduct> findLotteryProducts(Long lotteryId, String sqlString, Long lotteryProductId) {
		if (null == lotteryId && null == lotteryProductId)
			throw new BusinessException("参数异常！");
		StringBuffer buf = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		buf.append("SELECT ");
		buf.append(null == sqlString ? SQLUtils.getColumnsSQL(LotteryProduct.class, "LP") : sqlString);
		buf.append(" FROM T_LOTTERY_PRODUCT LP WHERE 1=1");
		if(null != lotteryId){
			buf.append(" AND LP.LOTTERY_ID=?");
			args.add(lotteryId);
		}
		if(null != lotteryProductId){
			buf.append(" AND LP.ID=?");
			args.add(lotteryProductId);
		}
		return genericDao.findTs(LotteryProduct.class, buf.toString(), args.toArray());
	}

	/* @Title:【2】.删除/下线抽奖活动
	 * @param lotteryId-活动ID
	 * @param type-1:删除;2:下线
	 */
	@Override
	public void deleteByIdLottery(Long lotteryId, Integer type) {
		logger.info("********* 【2.删除/下线抽奖活动】 start  *********");
		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("当前用户未登录");
		if (null == lotteryId || null == type)
			throw new BusinessException("请求错误");
		Lottery lottery = getByIdLottery(lotteryId, user.getOrgId(),"L.ID,L.STATE,L.IS_PUBLISH");// 根据抽奖活动ID活动抽奖活动
		if (null == lottery)
			throw new BusinessException("活动不存在或已删除！");

		if (1 == type) {
			
			isSucessStateBoolean(lottery.getId(), null, lottery.getState(), lottery.getIsPublish());//验证活动是否已开始
			deleteLottery(user.getOrgId(), lottery.getId());// 删除
		}
		logger.info("********* 【2.删除/下线抽奖活动】 end  *********");
	}

	/**
	 * @Title: 根据抽奖活动ID获取抽奖活动
	 * @param lotteryId
	 *            －活动ID
	 * @param orgId
	 *            －机构ID
	 * @param sqlString
	 *            －需要查询的字段,已L.开头如果查询所有传null
	 * @return: Lottery
	 */
	private Lottery getByIdLottery(Long lotteryId, Long orgId, String sqlString) {
		if (null == lotteryId)
			throw new BusinessException("参数异常！");
		StringBuffer buf = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		buf.append("SELECT ");
		buf.append(null == sqlString ? SQLUtils.getColumnsSQL(Lottery.class, "L") : sqlString);
		buf.append(" FROM T_LOTTERY L WHERE 1=1");
		buf.append(" AND L.ORG_ID=?");
		args.add(orgId);
		if(null != lotteryId){
			buf.append(" AND L.ID=?");
			args.add(lotteryId);
		}
		return genericDao.findT(Lottery.class, buf.toString(), args.toArray());
	}
	
	/**
	 * @Title: 删除抽奖活动 
	 * @param orgId-活动ID
	 * @param lottery
	 * @return: void
	 */
	private void deleteLottery(Long orgId, Long lotteryId) {
		try {
			genericDao.execute("DELETE FROM T_LOTTERY_PRODUCT WHERE 1=1 AND LOTTERY_ID=?", lotteryId);
			genericDao.execute("DELETE FROM T_LOTTERY_DEVNO_PRODUCT WHERE 1=1 AND LOTTERY_ID=?", lotteryId);
			genericDao.execute("DELETE FROM T_LOTTERY WHERE 1=1 AND ID=? AND ORG_ID=?", lotteryId, orgId);
			logger.info("********* 【2.删除/下线抽奖活动（成功）】   *********");
		} catch (Exception e) {
			logger.info("********* 【2.删除/下线抽奖活动（失败）】   *********");
			e.printStackTrace();
		}
	}

	/* @Title 【3】.获取基本信息
	 * @param 抽奖ID
	 * @return
	 */
	@Override
	public Lottery findByOrgIdDevice(Long lotteryId) {
		logger.info("********* 【获取本机构的设备集合 start 】********* ");
		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("请求错误!");
		Lottery lottery = new Lottery();
		List<Device> deviceList = new ArrayList<Device>();
		if(null == lotteryId){//查询出所有此机构的设备
			deviceList = finDeviceList(user.getOrgId());
		}else{//查询出已选择的和所有的设备组
			lottery = getByIdLottery(lotteryId, user.getOrgId(), null);
			//查询出所有已选和未选的设备
			deviceList = findSortDevice(lotteryId, user.getOrgId());
			List<LotteryProduct> lotteryProducts = findLotteryProducts(lottery.getId(), "LP.ID,LP.LOTTERY_ID", null);
			List<LotteryProduct> lotterylist = new ArrayList<LotteryProduct>();
			if(null != lotteryProducts && !lotteryProducts.isEmpty()){
				for (LotteryProduct lotteryProduct2 : lotteryProducts) {
					lotterylist.add(getLotteryProduct(lottery.getId(), lotteryProduct2.getId()));
				}
				lottery.setLotteryProductList(lotterylist);
			}
		}
		deviceList = deviceList(deviceList);//拼接设备类型
		lottery.setDeviceList(deviceList);
		logger.info("********* 【获取本机构的设备集合 end 】********* ");
		return lottery;
	}
	
	/**
	 * @Title: 查询出所有的设备
	 * @param orgId
	 *            －机构ID
	 * @return: List<Device>
	 */
	private List<Device> finDeviceList(Long orgId) {
		StringBuffer buf = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		buf.append(" SELECT T.ID, DR.factory_dev_no factoryDevNo, PP.POINT_ADDRESS pointAddress, PP.POINT_TYPE pointType");
		buf.append(" FROM T_DEVICE T");
		buf.append(" LEFT JOIN T_POINT_PLACE PP ON PP.ID=T.POINT_ID");
		buf.append(" LEFT JOIN T_DEVICE_RELATION DR ON DR.DEV_NO=T.DEV_NO");
		buf.append(" WHERE PP.ORG_ID =? AND T.BIND_STATE=?");
		args.add(orgId);
		args.add(Commons.BIND_STATE_SUCCESS);
		buf.append(" AND PP.STATE != ? ");
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
		buf.append(" GROUP BY T.ID, DR.FACTORY_DEV_NO, PP.POINT_ADDRESS, PP.POINT_TYPE");
		buf.append(" ORDER BY T.ID DESC");
		return genericDao.findTs(Device.class, buf.toString(), args.toArray());
	}
	
	/**
	 * @Title: 查询出所有已选和未选的设备
	 * @param lotteryId
	 *            －活动ID
	 * @param orgId
	 *            －机构ID
	 * @return: List<Device>
	 */
	private List<Device> findSortDevice(Long lotteryId, Long orgId) {
		StringBuffer buf = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		buf.append(" SELECT T.ID, DR.factory_dev_no factoryDevNo, PP.POINT_ADDRESS pointAddress, PP.POINT_TYPE pointType, COALESCE(A.SORT, 0) SORT");
		buf.append(" FROM T_DEVICE T");
		buf.append(" LEFT JOIN T_POINT_PLACE PP ON PP.ID = T.POINT_ID");
		buf.append(" LEFT JOIN T_DEVICE_RELATION DR ON DR.DEV_NO = T.DEV_NO");
		buf.append(" LEFT JOIN");
		buf.append(" (");
		buf.append(" SELECT FACTORY_DEV_NO,SORT FROM T_LOTTERY_DEVNO_PRODUCT WHERE LOTTERY_ID=? GROUP BY FACTORY_DEV_NO,SORT");
		args.add(lotteryId);
		buf.append(" )A ON A.FACTORY_DEV_NO = DR.FACTORY_DEV_NO");
		buf.append(" WHERE PP.ORG_ID =? AND T.BIND_STATE =? ");
		args.add(orgId);
		args.add(Commons.BIND_STATE_SUCCESS);

		buf.append(" AND PP.STATE != ? ");
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除

		buf.append(" GROUP BY T.ID, DR.FACTORY_DEV_NO, PP.POINT_ADDRESS, PP.POINT_TYPE, A.SORT ");
		buf.append(" ORDER BY T.ID DESC");

		return genericDao.findTs(Device.class, buf.toString(), args.toArray());
	}
	
	/* @Title 【3.1】单独获取活动基本信息(不带活动内容的)
	 * @param lotteryId 活动ID
	 * @return
	 */
	@Override
	public Lottery findOneLottery(Long lotteryId) {
		logger.info("********* 【3.1单独获取活动基本信息(不带活动内容的)】 start ********* ");
		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("请求错误!");
		StringBuffer buf = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		Lottery lottery  = getByIdLottery(lotteryId, user.getOrgId(), null);
		buf.append(" SELECT DE.ID,DR.FACTORY_DEV_NO factoryDevNo, PP.POINT_ADDRESS pointAddress, PP.POINT_TYPE pointType,COALESCE(A.SORT, 0) SORT");
		buf.append(" FROM T_POINT_PLACE PP");
		buf.append(" LEFT JOIN T_DEVICE DE ON DE.POINT_ID=PP.ID");
		buf.append(" LEFT JOIN T_DEVICE_RELATION DR ON DR.DEV_NO=DE.DEV_NO");
		buf.append(" LEFT JOIN");
		buf.append(" (");
		buf.append(" SELECT DE.ID,LD.FACTORY_DEV_NO factoryDevNo, PP.POINT_ADDRESS pointAddress, PP.POINT_TYPE pointType,LD.SORT");
		buf.append(" FROM T_DEVICE DE");
		buf.append(" LEFT JOIN T_POINT_PLACE PP ON PP.ID=DE.POINT_ID");
		buf.append(" LEFT JOIN T_DEVICE_RELATION DR ON DR.DEV_NO=DE.DEV_NO");
		buf.append(" LEFT JOIN T_LOTTERY_DEVNO_PRODUCT LD ON LD.FACTORY_DEV_NO=DR.FACTORY_DEV_NO");
		buf.append(" WHERE LD.LOTTERY_ID=? AND LD.SORT=1 AND DE.ORG_ID=? AND DE.BIND_STATE=?");
		args.add(lotteryId);
		args.add(user.getOrgId());
		args.add(Commons.BIND_STATE_SUCCESS);
		buf.append(" GROUP BY DE.ID,LD.FACTORY_DEV_NO, PP.POINT_ADDRESS, PP.POINT_TYPE, LD.SORT");
		buf.append(" ) A ON A.ID=DE.ID");
		buf.append(" WHERE DE.ORG_ID=? AND DE.BIND_STATE=?");
		args.add(user.getOrgId());
		args.add(Commons.BIND_STATE_SUCCESS);

		buf.append(" AND PP.STATE != ? ");
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除

		buf.append(" GROUP BY DE.ID,DR.FACTORY_DEV_NO, PP.POINT_ADDRESS, PP.POINT_TYPE, A.SORT");
		buf.append(" ORDER BY A.SORT,DE.ID");
		List<Device> deviceList = genericDao.findTs(Device.class, buf.toString(), args.toArray());
		deviceList = deviceList(deviceList);//拼接设备类型
		lottery.setDeviceList(deviceList);
		logger.info("********* 【3.1单独获取活动基本信息(不带活动内容的)】 end ********* ");
		return lottery;
	}
	
	/**
	 * @Title: 拼接设备类型
	 * @param deviceList
	 * @return: List<Device>
	 */
	public List<Device> deviceList(List<Device> deviceList){
		StringBuffer buf = new StringBuffer();
		if(null != deviceList && !deviceList.isEmpty()){
			for (Device device : deviceList) {//拼接此设备下所有附属设备类型
				buf.setLength(0);
				List<Cabinet> cabinets = getCabinetsByDeviceId(device.getId());
				for (Cabinet cabinet : cabinets) {
					String typeStr = CommonUtil.getDeviceTypeStrByModel(cabinet.getModel());
					if (StringUtils.isEmpty(typeStr))
						continue;
					buf.append(typeStr + "+");
				}
				buf.setLength(buf.length() - 1);
				device.setTypeStr(buf.toString());
			}
		}
		return deviceList;
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
	
	/* @Title 【4】.保存第一步
	 * @param lottery 
	 * @param key 图片INFO_ID值
	 * @param fileIds 需要删除的图片ID,编辑图片时需要
	 * @return
	 */
	@Override
	public Lottery saveLottery(Lottery lottery) {
		logger.info("********* 【4.保存第一步】 start  *********");
		User user = ContextUtil.getUser(User.class);
		if (user == null)
			throw new BusinessException("当前用户未登录！");
		if (null == lottery)
			throw new BusinessException("请求错误!");
		StringBuffer buf = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		//时间验证
		booleanTime(lottery);
		
		if(null != lottery.getId()){
			Lottery lotterys = getByIdLottery(lottery.getId(), user.getOrgId(), "L.STATE");
			if(null != lotterys){
				//活动已开始或者已预热  第一步都不可以操作
				if(!Commons.LOTTERY_STATE_0.equals(lotterys.getState()))
					throw new BusinessException("活动已发布或已开始，不能进行其他操作！");
			}
		}
		
		deviceIsExist(lottery, user.getOrgId());//如果此时段已经存在抽奖活动，返回告知客户
		
		if (lottery.getId() == null) {// 新增
			lottery.setState(Commons.LOTTERY_STATE_0);
			lottery.setIsPublish(Commons.IS_PUBLISH_0);
			lottery.setOrgId(user.getOrgId());
			lottery.setCreateTime(new Timestamp(System.currentTimeMillis()));
			genericDao.save(lottery);// 保存抽奖活动
			if(lottery.getLotteryDevNoProductList() != null && !lottery.getLotteryDevNoProductList().isEmpty()){
				buf.setLength(0);
				args.clear();
				buf.append("INSERT INTO T_LOTTERY_DEVNO_PRODUCT(FACTORY_DEV_NO,LOTTERY_ID,SORT)VALUES");
				for(LotteryDevNoProduct lotteryDevNoProduct : lottery.getLotteryDevNoProductList()){
					buf.append("(?,?,?),");
					args.add(lotteryDevNoProduct.getFactoryDevNo());
					args.add(lottery.getId());
					args.add(1);
				}
				buf.setLength(buf.length()-1);
				genericDao.execute(buf.toString(), args.toArray());//保存设备
			}
		} else {
			Lottery lotterys = getByIdLottery(lottery.getId(), user.getOrgId(), "L.ID,L.STATE,L.IS_PUBLISH,L.CREATE_TIME");
			if(null == lotterys)
				throw new BusinessException("抽奖活动不存在或已删除！");
			isSucessStateBoolean(lottery.getId(), null, lotterys.getState(), lotterys.getIsPublish());//验证活动内容是否发布
			
			lotterys.setLotteryName(lottery.getLotteryName());
			lotterys.setWarmUpTime(lottery.getWarmUpTime());
			lotterys.setStartTime(lottery.getStartTime());
			lotterys.setEndTime(lottery.getEndTime());
			lotterys.setOrgId(user.getOrgId());
			lotterys.setIsProbabil(lottery.getIsProbabil());
			genericDao.update(lotterys);//更新抽奖活动数据
			
			lotteryDevNoProductIsExist(lottery);//判断中间表lotteryDevnoProduct是否存在此设备
		}
		logger.info("********* 【4.保存第一步】 end  *********");
		return lottery;
	}

	/** 
	 * @Title 判断设备是否存在库中(进行增删改)
	 * @param lottery
	 */
	private void lotteryDevNoProductIsExist(Lottery lottery) {
		List<LotteryDevNoProduct> lotterDNP = findLotteryDevNoProductList(lottery.getId(), null, "LD.ID,LD.FACTORY_DEV_NO");
		if(null != lotterDNP && null != lottery.getLotteryDevNoProductList() && !lottery.getLotteryDevNoProductList().isEmpty() && !lotterDNP.isEmpty()){
			String[] convertToStringArr = convertToStringArr(lotterDNP);
			String[] convertToStringArr2 = convertToStringArr(lottery.getLotteryDevNoProductList());
			for(LotteryDevNoProduct  lotteryDevNoProduct : lottery.getLotteryDevNoProductList()){
				boolean contains = Arrays.asList(convertToStringArr).contains(lotteryDevNoProduct.getFactoryDevNo());//老数据中不包含了现有的ID,新增了数据
				if(!contains){
					lotteryDevNoProduct.setLotteryId(lottery.getId());
					lotteryDevNoProduct.setSort(1);
					genericDao.save(lotteryDevNoProduct);
				}
			}
			for(LotteryDevNoProduct  lotteryDevNoProduct : lotterDNP){
				boolean contains = Arrays.asList(convertToStringArr2).contains(lotteryDevNoProduct.getFactoryDevNo());//新数据中不包含了老数据ID,说明删除了数据
				if(!contains){
					deleteLotteryDevNoProduct(lottery.getId(), lotteryDevNoProduct.getFactoryDevNo(), null);
				}
			}
		}else if((null == lottery.getLotteryDevNoProductList() || lottery.getLotteryDevNoProductList().isEmpty()) && null != lotterDNP){//原来数据库中有，编辑时没有了，需删除
			for(LotteryDevNoProduct  lotteryDevNoProduct : lotterDNP){
				deleteLotteryDevNoProduct(lottery.getId(), lotteryDevNoProduct.getFactoryDevNo(), null);
			}
		}else if(lottery.getLotteryDevNoProductList() != null && (lotterDNP == null || lotterDNP.isEmpty())){//原来数据库中没有，编辑时有,需保存
			for(LotteryDevNoProduct  lotteryDevNoProduct : lottery.getLotteryDevNoProductList() ){
				lotteryDevNoProduct.setLotteryId(lottery.getId());
				lotteryDevNoProduct.setSort(1);
				genericDao.save(lotteryDevNoProduct);
			}
		}
	}

	public String[] convertToStringArr(List<LotteryDevNoProduct> objList) {
		String[] strArr = new String[objList.size()];
		for (int i = 0; i < strArr.length; i++)
			strArr[i] = objList.get(i).getFactoryDevNo().toString();
		return strArr;
	}

	/**
	 * @Title: 根据抽奖活动ID查询活动的中间表数据(LotteryDevNoProduct)
	 * @param lotteryId
	 *            －活动ID
	 * @param lotteryProductId
	 *            －活动内容ID
	 * @param sqlString
	 *            －需要查询的字段,已LD.开头如果查询所有传null
	 * @return: List<LotteryDevNoProduct>
	 */
	private List<LotteryDevNoProduct> findLotteryDevNoProductList(Long lotteryId, Long lotteryProductId, String sqlString) {
		if (null == lotteryId && null == lotteryProductId)
			throw new BusinessException("参数异常！");
		StringBuffer buf = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		buf.append("SELECT ");
		buf.append(null == sqlString ? SQLUtils.getColumnsSQL(LotteryDevNoProduct.class, "LD") : sqlString);
		buf.append(" FROM T_LOTTERY_DEVNO_PRODUCT LD WHERE 1=1 AND LD.SORT=1");
		if(null != lotteryId){
			buf.append(" AND LD.LOTTERY_ID=?");
			args.add(lotteryId);
		}
		if(null != lotteryProductId){
			buf.append(" AND LD.LOTTERY_PRODUCT_ID=?");
			args.add(lotteryProductId);
		}
		buf.append(" GROUP BY ");
		buf.append(null == sqlString ? SQLUtils.getColumnsSQL(LotteryDevNoProduct.class, "LD") : sqlString);
		return genericDao.findTs(LotteryDevNoProduct.class, buf.toString(), args.toArray());
	}

	/**
	 * @Title: 同一时段内一个设备只能做一个活动
	 * @param lottery
	 *            －活动
	 * @param orgId
	 *            －机构ID
	 * @return: void
	 */
	private void deviceIsExist(Lottery lottery, Long orgId) {
		StringBuffer buf = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		for(LotteryDevNoProduct lotteryDevNo : lottery.getLotteryDevNoProductList()){
			buf.setLength(0);
			args.clear();
			buf.append(" SELECT LD.FACTORY_DEV_NO FROM T_LOTTERY_DEVNO_PRODUCT LD");
			buf.append(" LEFT JOIN T_LOTTERY L ON L.ID=LD.LOTTERY_ID");
			buf.append(" WHERE LD.FACTORY_DEV_NO=? AND L.ORG_ID=?");
			args.add(lotteryDevNo.getFactoryDevNo());
			args.add(orgId);
			if(null != lottery.getId()){
				buf.append(" AND L.ID !=?");
				args.add(lottery.getId());
			}
			buf.append(" AND (? BETWEEN L.START_TIME AND L.END_TIME OR ? BETWEEN L.START_TIME AND L.END_TIME) AND L.STATE!=?");
			args.add(lottery.getWarmUpTime());
			args.add(lottery.getEndTime());
			args.add(Commons.LOTTERY_STATE_2);
			LotteryDevNoProduct lotteryDevNoProduct = genericDao.findT(LotteryDevNoProduct.class, buf.toString(), args.toArray());
			if(null != lotteryDevNoProduct && lotteryDevNoProduct.getFactoryDevNo() != null)
				throw new BusinessException("同一时段一个设备只能有一个活动:["+lotteryDevNoProduct.getFactoryDevNo()+"]");
		}
	}

	/**
	 * @Title: 判断活动内容是否已发布、进行中(发布失败，发布中，发布成功都属于已发布)
	 * @param lotteryId
	 *            －活动ID
	 * @param lotteryProductId
	 *            －活动内容ID
	 * @param state
	 *            －活动状态
	 * @param isPublish
	 *            －活动发布状态
	 * @return: void
	 */
	private void isSucessStateBoolean(Long lotteryId, Long lotteryProductId, String state, Integer isPublish) {
		if(Commons.LOTTERY_STATE_1.equals(state) || Commons.LOTTERY_STATE_5.equals(state) || (Commons.IS_PUBLISH_1.equals(isPublish) && !Commons.LOTTERY_STATE_2.equals(state))){
			throw new BusinessException("活动已在进行中或已发布,不能进行此操作!");
		}
	}
	
	/** 
	 * @Title 根据活动ID和出厂设备号删除活动*设备*商品中间表数据
	 * @param lotteryId 抽奖活动ID
	 * @param factoryDevNo	设备出厂编号
	 * @param lotteryProductId	活动内容ID
	 * void 返回类型 
	 */
	public void deleteLotteryDevNoProduct(Long lotteryId, String factoryDevNo, Long lotteryProductId){
		logger.info("********* 【根据活动ID和出厂设备号删除活动*设备*商品中间表数据】 start  *********");
		if(null == lotteryId)
			throw new BusinessException("请求错误！");
		StringBuffer buf = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		buf.append("DELETE FROM T_LOTTERY_DEVNO_PRODUCT WHERE LOTTERY_ID=?");
		args.add(lotteryId);
		if(null != factoryDevNo){
			buf.append(" AND FACTORY_DEV_NO=?");
			args.add(factoryDevNo);
		}
		if(null != lotteryProductId){
			buf.append(" AND LOTTERY_PRODUCT_ID=?");
			args.add(lotteryProductId);
		}
		genericDao.execute(buf.toString(), args.toArray());
		logger.info("********* 【根据活动ID和出厂设备号删除活动*设备*商品中间表数据】 end  *********");
	}
	
	/* @Title 【5.0】获取抽奖活动内容
	 * @param lotteryId 抽奖活动Id
	 * @param lotteryProductId 抽奖活动内容Id
	 * @return
	 */
	@Override
	public List<LotteryProduct> findLotteryProduct(Long lotteryId, Long lotteryProductId) {
		logger.info("********* 【5.0获取抽奖活动内容】 start  *********");
		User user = ContextUtil.getUser(User.class);
		if(null == user)
			throw new BusinessException("当前用户未登录！");
		if(null == lotteryId)
			throw new BusinessException("抽奖活动ID错误");
		
		List<LotteryProduct> lotteryProductList = new ArrayList<LotteryProduct>();
		if(null != lotteryProductId){
			lotteryProductList.add(getLotteryProduct(lotteryId, lotteryProductId));
		}else{
			List<LotteryProduct> lotteryProducts = findLotteryProducts(lotteryId, "LP.ID,LP.LOTTERY_ID", null);
			if(null != lotteryProducts && !lotteryProducts.isEmpty()){
				for (LotteryProduct lotteryProduct2 : lotteryProducts) {
					lotteryProductList.add(getLotteryProduct(lotteryId, lotteryProduct2.getId()));
				}
			}
		}
		logger.info("********* 【5.0获取抽奖活动内容】 end  *********");
		return lotteryProductList	;
	}
	
	/**
	 * @Title: 获取单个活动内容的信息
	 * @param lotteryId
	 *            －活动ID
	 * @param lotteryProductId
	 *            －活动内容ID
	 * @return: LotteryProduct
	 */
	public LotteryProduct getLotteryProduct(Long lotteryId, Long lotteryProductId){
		logger.info("********* 【获取单个活动内容】 start  *********");
		User user = ContextUtil.getUser(User.class);
		if(null == user)
			throw new BusinessException("当前用户未登录！");
		if(null == lotteryId)
			throw new BusinessException("抽奖活动ID错误");
		StringBuffer buf = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		LotteryProduct lotteryProducts = new LotteryProduct();
		if(null != lotteryProductId){//如果是编辑的情况下，根据活动内容ID查询出活动内容，以及图片信息
			lotteryProducts = findOneLotteryProduct(lotteryId, lotteryProductId, user.getOrgId());
		}
		//查询已选择的设备
		buf.setLength(0);
		args.clear();
		buf.append(" SELECT DE.ID,LD.FACTORY_DEV_NO factoryDevNo, PP.POINT_ADDRESS pointAddress, PP.POINT_TYPE pointType,LD.SORT");
		buf.append(" FROM T_POINT_PLACE PP");
		buf.append(" LEFT JOIN T_DEVICE DE ON DE.POINT_ID = PP.ID");
		buf.append(" LEFT JOIN T_DEVICE_RELATION DR ON DR.DEV_NO=DE.DEV_NO");
		buf.append(" LEFT JOIN T_LOTTERY_DEVNO_PRODUCT LD ON LD.FACTORY_DEV_NO=DR.FACTORY_DEV_NO");
		buf.append(" WHERE LD.LOTTERY_ID=? AND LD.SORT=1 AND DE.ORG_ID=? AND DE.BIND_STATE=?");
		args.add(lotteryId);
		args.add(user.getOrgId());
		args.add(Commons.BIND_STATE_SUCCESS);
		buf.append(" AND PP.STATE != ? ");
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
		buf.append(" GROUP BY DE.ID,LD.FACTORY_DEV_NO, PP.POINT_ADDRESS, PP.POINT_TYPE, LD.SORT");
		List<Device> devices = genericDao.findTs(Device.class, buf.toString(), args.toArray());
		if(devices != null && !devices.isEmpty()){
			devices = deviceList(devices);//拼接设备类型
			lotteryProducts.setDeviceList(devices);
		}
		logger.info("********* 【获取单个活动内容】 end  *********");
		return lotteryProducts;
	}

	
	/* @Title 【5.1】删除活动内容
	 * @param lotteryProductId
	 */
	@Override
	public void deleteLotteryProduct(Long lotteryProductId) {
		logger.info("********* 【5.1删除活动内容】 start  *********");
		User user = ContextUtil.getUser(User.class);
		if(null == user)
			throw new BusinessException("当前用户未登录！");
		if(null == lotteryProductId)
			throw new BusinessException("请求错误!");
		
		LotteryProduct lotteryProduct = findLotteryProducts(null, "LP.ID,LP.LOTTERY_ID", lotteryProductId).get(0);
		if(null == lotteryProduct)
			throw new BusinessException("活动内容不存在或已删除！");
		
		Lottery lottery = getByIdLottery(lotteryProduct.getLotteryId(), user.getOrgId(), "L.STATE,L.IS_PUBLISH");
		if(!"0".equals(lottery.getState()))
			throw new BusinessException("活动已开始或已结束，不能进行其他操作！");
		
		isSucessStateBoolean(null, lotteryProductId, lottery.getState(), lottery.getIsPublish());//判断活动内容是否发布
		logger.info("********* 【5.1删除活动内容】 end  *********");
		genericDao.delete(lotteryProduct);
		genericDao.execute("DELETE FROM T_LOTTERY_DEVNO_PRODUCT WHERE LOTTERY_PRODUCT_ID=? AND LOTTERY_ID=?", lotteryProduct.getId(), lotteryProduct.getLotteryId());
		genericDao.execute("DELETE FROM T_FILE WHERE INFO_ID=? AND TYPE IN(?,?,?)", lotteryProduct.getId(), Commons.FILE_LOTTERY_LOGO, Commons.FILE_LOTTERY_INFO, Commons.FILE_LOTTERY_DETAIL);
	}

	/* @Title 【5】.获取抽奖活动内容中的商品
	 * @param factoryDevNo 出厂设备号
	 * @param lotteryId 抽奖活动Id
	 * @param lotteryProductId 活动内容Id
	 * @param page
	 * @return
	 */
	@Override
	public List<Product> findByDevNoProduct(String factoryDevNo, Long lotteryId, Long lotteryProductId, Page page) {
		logger.info("********* 【5.获取抽奖活动内容(--商品--)】 start  *********");
		User user = ContextUtil.getUser(User.class);
		if(null == user)
			throw new BusinessException("当前用户未登录！");
		if(null == lotteryId || null == factoryDevNo)
			throw new BusinessException("请求错误!");
		StringBuffer buf = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		if(null != lotteryProductId){//如果有内容ID,查内容的具体商品
			args.clear();
			buf.setLength(0);
			buf.append(" SELECT STRING_AGG(B.ID||','||B.NAME||','||B.TYPE||','||B.REAL_PATH, ';' ORDER BY B.ID DESC) AS images,");
			buf.append(" P.SKU_NAME as skuName,DA.PRODUCT_ID,DA.PRICE_ON_LINE priceOnLine, SUM(DA.STOCK) as Stock,P.ID,COALESCE(A.SORT,0) SORT, A.NUM");
			buf.append(" FROM T_DEVICE_AISLE DA");
			buf.append(" LEFT JOIN T_DEVICE D ON D. ID = DA.DEVICE_ID");
			buf.append(" LEFT JOIN T_DEVICE_RELATION DR ON DR.DEV_NO=D.DEV_NO");
			buf.append(" LEFT JOIN T_PRODUCT P ON P.ID = DA.PRODUCT_ID AND D.ORG_ID = P.ORG_ID");
			buf.append(" LEFT JOIN T_FILE B ON P.ID=B.INFO_ID AND B.TYPE=?");
			args.add(0, Commons.FILE_PRODUCT);
			buf.append(" LEFT JOIN");
			buf.append(" (");
			buf.append(" SELECT P.ID,LD.SORT,LD.NUM");
			buf.append(" FROM T_LOTTERY_DEVNO_PRODUCT LD");
			buf.append(" LEFT JOIN T_PRODUCT P ON P.ID=LD.PRODUCT_ID");
			buf.append(" WHERE LD.SORT=1 AND P.ORG_ID=? AND LD.LOTTERY_ID=? AND LD.FACTORY_DEV_NO IN (?) AND LD.LOTTERY_PRODUCT_ID=?");
			args.add(user.getOrgId());
			args.add(lotteryId);
			args.add(factoryDevNo);
			args.add(lotteryProductId);
			buf.append(" AND LD.PRODUCT_ID IN(");
			buf.append("SELECT PRODUCT_ID FROM T_LOTTERY_DEVNO_PRODUCT WHERE FACTORY_DEV_NO=?");
			args.add(factoryDevNo);
			buf.append(" AND LOTTERY_ID=? AND LOTTERY_PRODUCT_ID=?");
			args.add(lotteryId);
			args.add(lotteryProductId);
			buf.append(" AND PRODUCT_ID IS NOT NULL");
			buf.append(")");
			buf.append(" GROUP BY P.ID,LD.SORT,LD.NUM");
			buf.append(" )A ON A.ID=P.ID");
			buf.append(" WHERE D.ORG_ID = ? AND P.STATE!=? AND D.POINT_ID !=0 AND DR.FACTORY_DEV_NO IN (?)");
			args.add(user.getOrgId());
			args.add(Commons.PRODUCT_STATE_TRASH);
			args.add(factoryDevNo);
			buf.append(" AND DA.PRODUCT_ID IS NOT NULL");
			buf.append(" GROUP BY P.SKU_NAME,DA.PRODUCT_ID,DA.PRICE_ON_LINE,P.ID,A.SORT, A.NUM");
			buf.append(" ORDER BY A.SORT,P.ID DESC");
		}else{//查所有商品
			buf.append(" SELECT STRING_AGG(B.ID||','||B.NAME||','||B.TYPE||','||B.REAL_PATH, ';' ORDER BY B.ID DESC) AS images,");
			buf.append(" P.SKU_NAME as skuName,DA.PRODUCT_ID,DA.PRICE_ON_LINE priceOnLine, SUM(DA.STOCK) as Stock,P.ID");
			buf.append(" FROM T_DEVICE_AISLE DA LEFT JOIN T_DEVICE D ON D. ID = DA.DEVICE_ID LEFT JOIN T_DEVICE_RELATION DR ON DR.DEV_NO=D.DEV_NO");
			buf.append(" LEFT JOIN T_PRODUCT P ON P.ID = DA.PRODUCT_ID AND D.ORG_ID = P.ORG_ID LEFT JOIN T_FILE B ON P.ID=B.INFO_ID");
			buf.append(" AND B.TYPE=?");
			args.add(0, Commons.FILE_PRODUCT);
			buf.append(" WHERE D.ORG_ID =? AND P.STATE!=? AND D.POINT_ID !=0 AND DR.FACTORY_DEV_NO IN (?) AND DA.PRODUCT_ID IS NOT NULL");
			args.add(user.getOrgId());
			args.add(Commons.PRODUCT_STATE_TRASH);
			args.add(factoryDevNo);
			buf.append(" GROUP BY P.SKU_NAME,DA.PRODUCT_ID,DA.PRICE_ON_LINE,P.ID");
			buf.append(" ORDER BY P.ID DESC");
		}
		logger.info("********* 【5.获取抽奖活动内容(--商品--)】 end  *********");
		return genericDao.findTs(Product.class, page, buf.toString(), args.toArray());
	}
	
	/* @Title 【6】.保存第二步 保存抽奖活动内容 以及图片 商品信息
	 * @param lotteryproduct
	 * @return
	 */
	@Override
	public  LotteryProduct saveLotteryDevNoProduct(LotteryProduct lotteryproduct) {
		logger.info("********* 【6.保存第二步】 start  *********");
		User user = ContextUtil.getUser(User.class);
		if(null == user)
			throw new BusinessException("当前用户未登录！");
		Lottery lottery = getByIdLottery(lotteryproduct.getLotteryId(), user.getOrgId(), null);
		if(null == lottery)
			throw new BusinessException("抽奖活动不存在或已删除！");
		if(!"0".equals(lottery.getState()))
			throw new BusinessException("活动已开始或已结束，不能进行其他操作！");
		Product productCode = new Product();
		if(null != lotteryproduct.getId()){//编辑抽奖活动内容
			updateLotteryDevNoProduct(lotteryproduct, user, lottery, productCode);
		}else{//新增抽奖活动内容
			saveLotteryDevNoProduct(lotteryproduct, user, lottery, productCode);
		}
		updateFile(lotteryproduct, user);
		logger.info("********* 【6.保存第二步】 end  *********");
		return lotteryproduct;
	}

	/** 
	 * @Title 更新图片数据
	 * @param lotteryproduct
	 * @param user
	 * @param buf
	 * @param args
	 */
	private void updateFile(LotteryProduct lotteryproduct, User user) {
		StringBuffer buf = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		if (null != lotteryproduct.getFileIds() && !"".equals(lotteryproduct.getFileIds()) && lotteryproduct.getFileIds().length > 0) {// 删除新增时多余的图片 fileIds是多余图片的ID数组
			args.clear();
			args.add(Commons.FILE_LOTTERY_LOGO);
			args.add(Commons.FILE_LOTTERY_DETAIL);
			args.add(Commons.FILE_LOTTERY_INFO);
			args.add(lotteryproduct.getId());
			buf.setLength(0);
			buf.append(" IN(");
			for (Long id : lotteryproduct.getFileIds()) {
				buf.append("?,");
				args.add(id);
			}
			buf.setLength(buf.length() - 1);
			buf.append(")");
			String inIds = buf.toString();
			buf.setLength(0);
			buf.append("SELECT REAL_PATH FROM T_FILE WHERE (TYPE=? OR TYPE=? OR TYPE=?) AND INFO_ID=? AND ID").append(inIds);
			List<Object> realPaths = genericDao.findListSingle(buf.toString(), args.toArray());
			if (realPaths.size() == lotteryproduct.getFileIds().length) {
				String[] ary = new String[realPaths.size()];
				realPaths.toArray(ary);
				buf.setLength(0);
				buf.append("DELETE FROM T_FILE WHERE (TYPE=? OR TYPE=? OR TYPE=?) AND INFO_ID=? AND ID");
				buf.append(inIds);
				genericDao.execute(buf.toString(), args.toArray());
			}
		}
		if(null != lotteryproduct.getKey()){//更新图片的INFO_ID
			buf.setLength(0);
			buf.append("UPDATE T_FILE SET INFO_ID=? WHERE (TYPE=? OR TYPE=? OR TYPE=?) AND INFO_ID=? AND CREATE_USER=?");
			args.clear();
			args.add(lotteryproduct.getId());
			args.add(Commons.FILE_LOTTERY_LOGO);
			args.add(Commons.FILE_LOTTERY_DETAIL);
			args.add(Commons.FILE_LOTTERY_INFO);
			args.add(lotteryproduct.getKey() * -1);
			args.add(user.getId());
			genericDao.execute(buf.toString(), args.toArray());
		}
	}

	/** 
	 * @Title 保存 活动设备商品中间表数据
	 * @param lotteryproduct
	 * @param user
	 * @param lottery
	 */
	private void saveLotteryDevNoProduct(LotteryProduct lotteryproduct, User user, Lottery lottery, Product productCode) {
		isSucessStateBoolean(lottery.getId(), null, lottery.getState(), lottery.getIsPublish());//判断活动内容是否发布
		String productNo = null;
		while(true){//生成活动内容的商品编号
			productNo = RandomUtil.random(8);
			Product product = genericDao.findT(Product.class, "SELECT * FROM T_PRODUCT WHERE CODE=? AND ORG_ID=?", productNo, user.getOrgId());
			LotteryProduct lotteryProduct = genericDao.findT(LotteryProduct.class, "SELECT * FROM T_LOTTERY_PRODUCT WHERE PRODUCT_NO=?", productNo);
			if(null == product && null == lotteryProduct)//只有抽奖内容表和商品表都没有此商品编号才可以
				break;
		}
		lotteryproduct.setProductNo(productNo);
		productCode = createProduct(lotteryproduct, user, productNo);//同时以抽奖内容数据创建一个商品对象
		try {
			genericDao.save(lotteryproduct);//保存活动内容表
			genericDao.save(productCode);//保存商品表
		} catch (Exception e) {
			logger.info("********* 【6.保存第二步】 保存失败  *********");
			e.printStackTrace();
		}
		if(null != lotteryproduct.getDeviceList() && !lotteryproduct.getDeviceList().isEmpty()){
			//新增的时候先删除第一步保存的数据，然后在循环保存设备与设备商品
			deleteLotteryDevNoProduct(lotteryproduct.getLotteryId(), null, lotteryproduct.getId());
			for(Device device : lotteryproduct.getDeviceList()){
				if(null != device.getProductList() && !device.getProductList().isEmpty()){
					for(Product product: device.getProductList()){
						LotteryDevNoProduct lotteryDevNoProduct = new LotteryDevNoProduct();
						lotteryDevNoProduct.setIsSucessState(Commons.IS_SUCESSSTATE_3);//设备的初始状态是:3:未发布
						lotteryDevNoProduct.setFactoryDevNo(device.getFactoryDevNo());
						lotteryDevNoProduct.setLotteryId(lotteryproduct.getLotteryId());
						lotteryDevNoProduct.setLotteryProductId(lotteryproduct.getId());
						lotteryDevNoProduct.setProductId(product.getId());
						lotteryDevNoProduct.setSort(1);
						if(lottery.getIsProbabil() == 1 && product.getNum() != null){
							//这里要验证数量之后在存进去
							Integer stock = getProductStock(user.getOrgId(), device.getFactoryDevNo(), product.getId());
							//当库存小于输入的数量时
							if(stock < product.getNum())
								throw new BusinessException("商品活动数量大于库存!");
							
							lotteryDevNoProduct.setNum(product.getNum());
						}
						genericDao.save(lotteryDevNoProduct);
					}
				}
			}
		}
	}

	/** 
	 * @Title 编辑活动设备商品中间表数据
	 * @param lotteryproduct
	 * @param user
	 * @param lottery
	 */
	private void updateLotteryDevNoProduct(LotteryProduct lotteryproduct, User user, Lottery lottery, Product productCode) {
		LotteryProduct lotteryProduct = findLotteryProducts(null, null, lotteryproduct.getId()).get(0);
		if(null == lotteryProduct)
			throw new BusinessException("抽奖活动不存在或已删除！");
		//判断活动是否已开始
		isSucessStateBoolean(lotteryProduct.getLotteryId(), lotteryProduct.getId(), lottery.getState(), lottery.getIsPublish());
		
		productCode = genericDao.findT(Product.class, "SELECT * FROM T_PRODUCT WHERE CODE=?", lotteryProduct.getProductNo());
		if(null == productCode)
			throw new BusinessException("商品表中不存在此商品");
		
		lotteryProduct.setProductName(lotteryproduct.getProductName());
		lotteryProduct.setProductPrice(lotteryproduct.getProductPrice());
		lotteryProduct.setProductNorms(lotteryproduct.getProductNorms());
		lotteryProduct.setLotteryId(lotteryproduct.getLotteryId());
		productCode.setSkuName(lotteryproduct.getProductName());
		productCode.setPrice(lotteryproduct.getProductPrice());
		productCode.setPriceCombo(lotteryproduct.getProductPrice());
		productCode.setSpec(lotteryproduct.getProductNorms());
		try {
			genericDao.update(lotteryProduct);//更新活动内容表数据
			genericDao.update(productCode);//更新商品表数据
		} catch (Exception e) {
			logger.info("********* 【6.保存第二步】 编辑失败  *********");
			e.printStackTrace();
		}
		for(Device device : lotteryproduct.getDeviceList()){
			//而编辑的时候是根据设备号来删除商品的，然后重新保存选中的商品
			deleteLotteryDevNoProduct(lotteryProduct.getLotteryId(), device.getFactoryDevNo(), lotteryProduct.getId());
			if(device.getProductList() == null || device.getProductList().isEmpty() || device.getProductList().size() <= 0){
				LotteryDevNoProduct lotteryDevNoProduct = new LotteryDevNoProduct();
				lotteryDevNoProduct.setLotteryId(lotteryproduct.getLotteryId());
				lotteryDevNoProduct.setFactoryDevNo(device.getFactoryDevNo());
				lotteryDevNoProduct.setLotteryProductId(lotteryProduct.getId());
				lotteryDevNoProduct.setSort(1);
				genericDao.save(lotteryDevNoProduct);
			}
			for(Product product: device.getProductList()){
				LotteryDevNoProduct lotteryDevNoProduct = new LotteryDevNoProduct();
				lotteryDevNoProduct.setFactoryDevNo(device.getFactoryDevNo());
				lotteryDevNoProduct.setLotteryId(lotteryproduct.getLotteryId());
				lotteryDevNoProduct.setLotteryProductId(lotteryproduct.getId());
				lotteryDevNoProduct.setProductId(product.getId());
				lotteryDevNoProduct.setSort(1);
				
				if(lottery.getIsProbabil() == 1){
					Integer stock = getProductStock(user.getOrgId(), device.getFactoryDevNo(), product.getId());//这里要验证数量之后在存进去
					if(stock < product.getNum())//当库存小于输入的数量时
						throw new BusinessException("商品数量大于库存!");
					
					lotteryDevNoProduct.setNum(product.getNum());
				}
				genericDao.save(lotteryDevNoProduct);
			}	
		}
	}

	/** 
	 * @Title 构建商品数据 
	 * @param lotteryproduct 抽奖活动对象
	 * @param user 登录用户
	 * @param productNo
	 * @return
	 */
	private Product createProduct(LotteryProduct lotteryproduct, User user, String productNo) {
		Product product = new Product();
		StringBuffer buf = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		product.setSku("1000-1100-1105-"+user.getOrgCode()+"-"+RandomUtil.buildRandom(4));
		product.setCode(productNo);
		product.setSkuName(lotteryproduct.getProductName());
		product.setCategory(Commons.PRODUCT_CATEGORY_TYPE);
		product.setPrice(lotteryproduct.getProductPrice());
		product.setPriceCombo(lotteryproduct.getProductPrice());
		product.setStock(Commons.PRODUCT_STOCK_NUM);
		product.setSpec(lotteryproduct.getProductNorms());
		product.setPerimeter(Commons.PRODUCT_PERIMETER);//周长
		product.setBrand(Commons.PRODUCT_BRAND_OR_ORIGIN);
		product.setCombo(false);
		product.setOrgId(user.getOrgId());
		buf.append("SELECT ID,SKU,SKU_NAME,CODE FROM T_PRODUCT WHERE (SKU=? OR SKU_NAME=? OR CODE = ?) AND ORG_ID=? AND STATE !=? ");
		args.add(product.getSku());
		args.add(product.getSkuName());
		args.add(product.getCode());
		args.add(user.getOrgId());
		args.add(Commons.PRODUCT_STATE_TRASH);
		Product oldProduct = genericDao.findT(Product.class, buf.toString(), args.toArray());
		if (oldProduct != null) {
			if (oldProduct.getSku().equals(product.getSku()))
				throw new BusinessException("SKU已经存在！");
			if (oldProduct.getCode().equals(product.getCode()))
				throw new BusinessException("商品编码已经存在！");
		}
		String[] ary = product.getSku().split("-");
		Double taxRate = genericDao.findSingle(Double.class, "SELECT COALESCE(TAX_RATE, 0) FROM T_CATEGORY WHERE PARENT_CODE=? AND CODE=?", ary[0] + "-" + ary[1], ary[2]);
		product.setTaxRate(taxRate);
		product.initDefaultValue();
		product.setType(Commons.PROD_TYPE_SELF);// 自有
		product.setCreateUser(user.getId());
		product.setCreateTime(new Timestamp(System.currentTimeMillis()));
		return product;
	}

	/* @Title 【7】.获取抽奖活动期间订单统计
	 * @param lotteryId 抽奖活动ID
	 * @return
	 */
	@Override
	public List<Order> findOrderDetail(Long lotteryId) {
		logger.info("********* 【7.获取抽奖活动期间订单统计】 start  *********");
		User user = ContextUtil.getUser(User.class);
		if(null == user)
			throw new BusinessException("当前用户未登陆");
		if(null == lotteryId)
			throw new BusinessException("请求错误");
		Lottery lottery = getByIdLottery(lotteryId, user.getOrgId(), null);
		List<Device> devices = pitchOnDevice(user.getOrgId(), lottery);
		List<LotteryProduct> lotteryProductList = findLotteryProducts(lottery.getId(), "LP.ID,LP.PRODUCT_NAME,LP.PRODUCT_PRICE,LP.REFERRALS_TIME,LP.PRODUCT_NO" , null);
		List<Order> orderList = new ArrayList<Order>();
		if(null != lotteryProductList && !lotteryProductList.isEmpty()){
			for (LotteryProduct lotteryProduct : lotteryProductList) {//活动内容
				Order order = findOneOrder(user.getOrgId(), lottery, lotteryProduct);//获取单个活动内容的统计数据
				
				if(devices != null && !devices.isEmpty()){
					devices = deviceList(devices);//拼接设备类型
					order.setDeviceList(devices);
				}
				orderList.add(order);
			}
		}
		logger.info("********* 【7.获取抽奖活动期间订单统计】 end  *********");
		return orderList;
	}

	/**
	 * @Title: (数据统计中)获取选中的设备
	 * @param orgId
	 *            －机构ID
	 * @param lottery
	 *            －活动对象
	 * @return
	 * @return: List<Device>
	 */
	private List<Device> pitchOnDevice(Long orgId, Lottery lottery) {
		StringBuffer buf = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		buf.append(" SELECT DE.ID,LD.FACTORY_DEV_NO factoryDevNo, PP.POINT_ADDRESS pointAddress, PP.POINT_TYPE pointType,LD.SORT");
		buf.append(" FROM T_DEVICE DE");
		buf.append(" LEFT JOIN T_POINT_PLACE PP ON PP.ID=DE.POINT_ID");
		buf.append(" LEFT JOIN T_DEVICE_RELATION DR ON DR.DEV_NO=DE.DEV_NO");
		buf.append(" LEFT JOIN T_LOTTERY_DEVNO_PRODUCT LD ON LD.FACTORY_DEV_NO=DR.FACTORY_DEV_NO");
		buf.append(" WHERE LD.LOTTERY_ID=? AND LD.SORT=1 AND DE.ORG_ID=? AND DE.BIND_STATE=?");
		args.add(lottery.getId());
		args.add(orgId);
		args.add(Commons.BIND_STATE_SUCCESS);
		buf.append(" AND PP.STATE != ? ");
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
		buf.append(" GROUP BY DE.ID,LD.FACTORY_DEV_NO, PP.POINT_ADDRESS, PP.POINT_TYPE, LD.SORT");
		return genericDao.findTs(Device.class, buf.toString(), args.toArray());
	}

	/* @Title 定时抽奖任务
	 * 
	 */
	@Override
	public void updateLotteryStateJob() {
		logger.info("********* 【 定时抽奖任务】 start  *********");
		//1.先查询出所有抽奖活动
		StringBuffer buf = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		buf.append(" SELECT L.ID,L.WARM_UP_TIME,L.START_TIME,L.END_TIME,L.STATE,L.IS_PROBABIL,L.IS_PUBLISH,");
		buf.append(" LP.ID lotteryProductId,LP.PRODUCT_NO productNo,LD.NUM,LD.FACTORY_DEV_NO factoryDevNo");
		buf.append(" FROM T_LOTTERY L");
		buf.append(" LEFT JOIN T_LOTTERY_PRODUCT LP ON LP.LOTTERY_ID=L.ID");
		buf.append(" LEFT JOIN T_LOTTERY_DEVNO_PRODUCT LD ON LD.LOTTERY_PRODUCT_ID=LP.ID AND LD.LOTTERY_ID=LP.LOTTERY_ID");
		buf.append(" WHERE L.STATE IN(?,?,?) AND L.IS_PUBLISH=? AND LD.IS_SUCESSSTATE IN (?,?,?,?)");
		args.add(Commons.LOTTERY_STATE_0);
		args.add(Commons.LOTTERY_STATE_1);
		args.add(Commons.LOTTERY_STATE_5);
		args.add(Commons.IS_PUBLISH_1);
		args.add(Commons.IS_SUCESSSTATE_0);
		args.add(Commons.IS_SUCESSSTATE_4);
		args.add(Commons.IS_SUCESSSTATE_6);
		args.add(Commons.IS_SUCESSSTATE_8);
		buf.append(" GROUP BY L.ID, L.WARM_UP_TIME, L.START_TIME, L.END_TIME, L.STATE, L.IS_PROBABIL,L.IS_PUBLISH, LP.ID, LP.PRODUCT_NO, LD.NUM, LD.FACTORY_DEV_NO");
		List<Lottery> lotteryList = genericDao.findTs(Lottery.class, buf.toString(), args.toArray());
		
		if(null != lotteryList && !lotteryList.isEmpty()){
			Date date = new Date();// 当前时间
			for (Lottery lottery : lotteryList) {
				if(date.getTime() >= lottery.getWarmUpTime().getTime() && date.getTime() < lottery.getStartTime().getTime() && Commons.LOTTERY_STATE_0.equals(lottery.getState())){
					//TODO//推送消息
//					lotteryMessagePush(lottery, Commons.LOTTERY_ISOPEN_1);
					logger.info("********* 【 活动预热更新结束】 *********");
					//更新数据库
					updateLottery(lottery.getId(), Commons.LOTTERY_STATE_5, Commons.IS_SUCESSSTATE_0, Commons.IS_SUCESSSTATE_4, lottery.getFactoryDevNo(), lottery.getLotteryProductId());
				}
				//2.到活动开始时间(当前时间大于活动开始时间小于活动结束时间)
				if(date.getTime() >= lottery.getStartTime().getTime() && date.getTime() < lottery.getEndTime().getTime() && Commons.LOTTERY_STATE_5.equals(lottery.getState())){
					//推送消息
					lotteryPushMessage(lottery, Commons.LOTTERY_ISOPEN_1);

					updateLottery(lottery.getId(), Commons.LOTTERY_STATE_1, Commons.IS_SUCESSSTATE_4, Commons.IS_SUCESSSTATE_2, lottery.getFactoryDevNo(), lottery.getLotteryProductId());
					logger.info("********* 【 活动开始更新结束】 *********");
				}
				
				//3.到活动结束时间(当前时间大于活动结束时间)
				if(date.getTime() > lottery.getEndTime().getTime() && Commons.LOTTERY_STATE_1.equals(lottery.getState())){
					lotteryPushMessage(lottery, Commons.LOTTERY_ISOPEN_0);

					updateLottery(lottery.getId(), Commons.LOTTERY_STATE_2, Commons.IS_SUCESSSTATE_6, Commons.IS_SUCESSSTATE_2, lottery.getFactoryDevNo(), lottery.getLotteryProductId());
				}
			}
		}
		
		
		logger.info("********* 【 定时抽奖任务】 end  *********");
	}

	private void lotteryPushMessage(Lottery lottery, Integer state) {
		StringBuffer buf = new StringBuffer();
		List<Object> args = new ArrayList<Object>();

		List<ChangeLotteryData> lists = new ArrayList<ChangeLotteryData>();
		buf.append(" SELECT LD.PRODUCT_ID,LD.NUM,P.CODE productCode FROM T_LOTTERY_DEVNO_PRODUCT LD");
		buf.append(" LEFT JOIN T_PRODUCT P ON P.ID=LD.PRODUCT_ID");
		buf.append(" WHERE LD.FACTORY_DEV_NO=? AND LD.LOTTERY_PRODUCT_ID=?");
		args.add(lottery.getFactoryDevNo());
		args.add(lottery.getLotteryProductId());
		buf.append(" AND LD.IS_SUCESSSTATE IN(?,?,?)");
		args.add(Commons.IS_SUCESSSTATE_0);
		args.add(Commons.IS_SUCESSSTATE_4);
		args.add(Commons.IS_SUCESSSTATE_6);
		buf.append(" GROUP BY LD.PRODUCT_ID,LD.NUM,P.CODE");
		List<LotteryDevNoProduct> lotteryDevNoProducts = genericDao.findTs(LotteryDevNoProduct.class, buf.toString(), args.toArray());
		for(LotteryDevNoProduct product : lotteryDevNoProducts){
			ChangeLotteryData changeLotteryData = new ChangeLotteryData();
			if(1 == lottery.getIsProbabil()){//可修改概率（设置数量）
				changeLotteryData.setNum(product.getNum());
			}
			changeLotteryData.setProductNo(product.getProductCode());
			lists.add(changeLotteryData);
		}
		String notifyFlag = null;
		if(1 == lottery.getIsProbabil()){// 此字段区分发送状态时长期还是短期
			notifyFlag = Commons.NOTIFY_LOTTERY_STATE_ISOPEN;
		}else{
			notifyFlag = Commons.NOTIFY_LOTTERY_LONG_STATE_ISOPEN;
		}
		//推送信息
		ChangeLotteryStateData data = new ChangeLotteryStateData();
		data.setNotifyFlag(notifyFlag);// 此字段区分发送状态时长期还是短期
		data.setTime(new Timestamp(System.currentTimeMillis()));
		data.setState(state);//0:下线 1：上线 
		data.setProductNo(lottery.getProductNo());
		pushChangeProductStateMessage(Arrays.asList(lottery.getFactoryDevNo()), lists, data);
	}
	

	/**
	 * @Title 抽奖活动推送内容
	 * @param devNos
	 *            －设备组号
	 * @param lists
	 *            －需推送的信息
	 * @param state
	 *            －0:下线 ,1上线 void 返回类型
	 */
	public void pushChangeProductStateMessage(List<String> devNos, List<ChangeLotteryData> lists, ChangeLotteryStateData data) {
		List<List<ChangeLotteryData>> fatherlist = CommonUtil.fatherList(lists, 15);
		String[] devNosArr = devNos.toArray(new String[]{});
		String devNo = CommonUtil.converToString(devNosArr, ",");
		for (List<ChangeLotteryData> list : fatherlist) {
			data.setList(list);
			String json = ContextUtil.getJson(data);
			if(json != null && !"".equals(json) && json.length() > 1000){
				List<List<ChangeLotteryData>> fatherlists = CommonUtil.fatherList(list, 10);
				for (List<ChangeLotteryData> list2 : fatherlists) {
					data.setList(list2);
					//主动通知
					MessagePusher pusher = new MessagePusher();
					try {
						logger.info("【抽奖活动内容推送数据格式：设备编号:"+devNo+",["+ContextUtil.getJson(data)+"]】");
						pusher.pushMessageToAndroidDevices(devNos, ContextUtil.getJson(data), false);
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
						throw new BusinessException("抽奖活动内容推送失败！");
					}
				}
			}else{
				//主动通知
				MessagePusher pusher = new MessagePusher();
				try {
					logger.info("【抽奖活动内容推送数据格式：设备编号:"+devNo+",["+ContextUtil.getJson(data)+"]】");
					pusher.pushMessageToAndroidDevices(devNos, ContextUtil.getJson(data), false);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					throw new BusinessException("抽奖活动内容推送失败！");
				}
			}
		}
	}

	/* @Title 【8】.根据活动内容ID和设备组号查询出参与活动的商品信息(订单数据统计中的)
	 * @param lotteryProductId 抽奖活动内容ID
	 * @param factoryDevNo 设备出厂编号
	 * @param page
	 * @return
	 */
	@Override
	public List<Product> findByLotteryIdProduct(Long lotteryProductId, String factoryDevNo, Page page) {
		User user = ContextUtil.getUser(User.class);
		if(null == user)
			throw new BusinessException("当前用户未登陆");
		if(null == lotteryProductId || null == factoryDevNo)
			throw new BusinessException("请求错误");
		StringBuffer buf = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		LotteryProduct lotteryProduct = genericDao.findT(LotteryProduct.class, "SELECT LOTTERY_ID,PRODUCT_NO FROM T_LOTTERY_PRODUCT WHERE ID=?", lotteryProductId);
		Lottery lottery = genericDao.findT(Lottery.class, "SELECT STATE,START_TIME,END_TIME FROM T_LOTTERY WHERE ID =?", lotteryProduct.getLotteryId());
		if(null == lottery)
			throw new BusinessException("没有可用数据!");

		buf.append(" SELECT T.IMAGES, T.SKUNAME, T.PRODUCT_ID, T.PRICEONLINE, T.STOCK, T.ID, T.SORT, T.NUM, SUM(COALESCE(A.QTY, 0)) qty");
		buf.append(" FROM (");
		buf.append(" SELECT STRING_AGG(B.ID||','||B.NAME||','||B.TYPE||','||B.REAL_PATH, ';' ORDER BY B.ID DESC) AS IMAGES,");
		buf.append(" P.SKU_NAME AS SKUNAME,DA.PRODUCT_ID,DA.PRICE_ON_LINE PRICEONLINE, SUM(DA.STOCK) AS STOCK,P.ID,LD.SORT,LD.NUM");
		buf.append(" FROM T_LOTTERY_DEVNO_PRODUCT LD");
		buf.append(" LEFT JOIN T_PRODUCT P ON P.ID=LD.PRODUCT_ID");
		buf.append(" LEFT JOIN T_FILE B ON P.ID=B.INFO_ID AND B.TYPE=?");
		args.add(0, Commons.FILE_PRODUCT);
		buf.append(" LEFT JOIN T_DEVICE_AISLE DA ON DA.PRODUCT_ID=P.ID");
		buf.append(" LEFT JOIN T_DEVICE D ON D.ID=DA.DEVICE_ID");
		buf.append(" LEFT JOIN T_DEVICE_RELATION DR ON DR.FACTORY_DEV_NO = LD.FACTORY_DEV_NO");
		buf.append(" WHERE LD.SORT=1 AND P.ORG_ID=? AND LD.FACTORY_DEV_NO=? AND LD.LOTTERY_PRODUCT_ID=? AND DR.DEV_NO=D.DEV_NO");
		args.add(user.getOrgId());
		args.add(factoryDevNo);
		args.add(lotteryProductId);
		buf.append(" GROUP BY  P.SKU_NAME, DA.PRODUCT_ID, DA.PRICE_ON_LINE, P.ID,LD.SORT, LD.NUM");
		buf.append(" ) T");
		buf.append(" LEFT JOIN");
		buf.append(" (");
		buf.append("  SELECT OD.ID, LD.PRODUCT_ID, OD.QTY FROM T_LOTTERY_DEVNO_PRODUCT LD");
		buf.append("  LEFT JOIN T_ORDER_DETAIL OD ON OD.SKU_ID=LD.PRODUCT_ID");
		buf.append("  LEFT JOIN T_ORDER O ON O.CODE = OD.ORDER_NO");
		buf.append("  LEFT JOIN T_LOTTERY_PRODUCT LP ON LP.ID=LOTTERY_PRODUCT_ID AND LP.PRODUCT_NO=OD.LOTTERY_PRODUCT_NO");
		buf.append("  WHERE LD.FACTORY_DEV_NO=? AND LD.LOTTERY_PRODUCT_ID=? AND OD.ORG_ID=? AND LP.PRODUCT_NO=?");
		args.add(factoryDevNo);
		args.add(lotteryProductId);
		args.add(user.getOrgId());
		args.add(lotteryProduct.getProductNo());
		buf.append("    AND O.DEVICE_NO=(SELECT DEV_NO FROM T_DEVICE_RELATION WHERE FACTORY_DEV_NO=?)");
        args.add(factoryDevNo);
		buf.append("    AND (OD.CREATE_TIME BETWEEN ? AND ?)");
		args.add(lottery.getStartTime());
		args.add(lottery.getEndTime());
		buf.append("    GROUP BY OD.ID, LD.PRODUCT_ID, OD.QTY");
		buf.append(" ) A ON A.PRODUCT_ID=T.ID");
		buf.append(" GROUP BY T.IMAGES,T.SKUNAME,T.PRODUCT_ID,T.PRICEONLINE,T.STOCK,T.ID,T.SORT,T.NUM");
		logger.info("********* 【8.根据活动内容ID和设备组号查询出订单详情】 end  *********");
		return genericDao.findTs(Product.class, buf.toString(), args.toArray());
	}

	/* @Title 更新抽奖活动内容 isSucessState
	 * @param activityId 活动商品编号
	 * @param machineNum 设备组号
	 * @param isSucessState 活动执行状态 isSucessState: 1000:发布成功,1001:发布失败,1002:上线成功,1003:上线失败,1004:下线成功,1005:下线失败
	 * 活动执行状态:0:发布成功、1:发布失败、2:执行中、3:未发布、6:活动成功、7:活动失败、8:结束成功、9:结束失败
	 */
	@Override
	public void syncActivityIsSucess(String activityId, String machineNum, String isSucessState) {
		logger.info("********* 【更新抽奖活动内容 isSucessState】 start  *********");
		String state = null;
		Lottery lottery = genericDao.findT(Lottery.class, "SELECT STATE FROM T_LOTTERY WHERE ID=(SELECT LOTTERY_ID FROM T_LOTTERY_PRODUCT WHERE PRODUCT_NO=?)", activityId);
		logger.info("********* 【state】:"+lottery.getState()+",【isSucessState】:"+isSucessState+"*********");
		//活动是未开始(取消发布)不管是失败或成功,那活动状态更新为未开始
		if(Commons.LOTTERY_STATE_0.equals(lottery.getState()) && ("1004".equals(isSucessState) || "1005".equals(isSucessState))){
			isSucessState = Commons.IS_SUCESSSTATE_3;
		}
		switch (isSucessState) {
			case "1000":
				state = "0";//发布成功
				break;
			case "1001":
				state = "1";//发布失败
				break;
			case "1002":
				state = "6";//活动成功
				break;
			case "1003":
				state = "7";//活动失败
				break;
			case "1004":
				state = "8";//结束成功
				break;
			case "1005":
				state = "9";//结束失败
				break;
			default:
				state = isSucessState;
				break;
		}
		logger.info("********* 【state】:"+state+"*********");
		StringBuffer buf = new StringBuffer();
		List<Object> args = new ArrayList<>();
		buf.append("UPDATE T_LOTTERY_DEVNO_PRODUCT SET IS_SUCESSSTATE=?");
		args.add(state);
		buf.append(" WHERE LOTTERY_PRODUCT_ID = (SELECT ID FROM T_LOTTERY_PRODUCT WHERE PRODUCT_NO=?)");
		args.add(activityId);
		buf.append(" AND FACTORY_DEV_NO=?");
		args.add(machineNum);
		logger.info("********* 【sql:"+ buf.toString() +",参数:"+args.toArray() +"】 end  *********");
		genericDao.execute(buf.toString(), args.toArray());
		logger.info("********* 【更新抽奖活动内容 isSucessState】 end  *********");
	}


	/*
	 * @Title: 10.发布抽奖信息
	 * @param lotteryId 抽奖活动ID(推送整个抽奖活动时需带此ID)
	 * @param lotteryPoroductId 活动内容ID(推送单个设备的单个内容信息)
	 * @param factoryDevNo 活动内容ID(推送单个设备的单个内容信息)
	 * @param type 2:发布(状态位发布中)，3:取消发布(状态是未发布)
	 * @return
	 * @return: Lottery
	 */
	@Override
	public Lottery updatepushMessageLottery(Long lotteryId, Long lotteryProductId, String factoryDevNo, Integer type){
		logger.info("******** 【10】.发布抽奖信息 start ********");
		User user = ContextUtil.getUser(User.class);
		if(null == user)
			throw new BusinessException("当前用户为登录");
		
		if(null == lotteryId && (null == lotteryProductId || null == factoryDevNo))
			throw new BusinessException("请求参数错误！");
		
		Lottery lottery = new Lottery();
		if(null != lotteryId){
			lottery = getByIdLottery(lotteryId, user.getOrgId(), null);
		}else if(null != lotteryProductId && null != factoryDevNo){
			lottery = genericDao.findT(Lottery.class,
					"SELECT * FROM T_LOTTERY WHERE ID=(SELECT LOTTERY_ID FROM T_LOTTERY_DEVNO_PRODUCT WHERE LOTTERY_PRODUCT_ID=? AND FACTORY_DEV_NO=? GROUP BY LOTTERY_ID)", lotteryProductId, factoryDevNo);
		}
		if(null == lottery)
			throw new BusinessException("活动不存在或已删除！");
		
		if(2 == type){
			if(new Date().getTime() > lottery.getWarmUpTime().getTime() || new Date().getTime() > lottery.getStartTime().getTime())
				throw new BusinessException("超过预热时间的活动,不能发布！");
		}
		
		StringBuffer buf = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		buf.append(" SELECT STRING_AGG(B.ID||','||B.NAME||','||B.TYPE||','||B.REAL_PATH, ';' ORDER BY B.TYPE) AS Images,");
		buf.append(" LP.ID,LP.PRODUCT_NAME,LP.PRODUCT_PRICE,LP.PRODUCT_NO,LD.FACTORY_DEV_NO factoryDevNo,LD.LOTTERY_PRODUCT_ID lotteryProductId,LD.IS_SUCESSSTATE isSucessState,DL.DEVICE_STATUS deviceState");
		buf.append(" FROM T_LOTTERY_PRODUCT LP");
		buf.append(" LEFT JOIN T_FILE B ON B.INFO_ID=LP.ID AND(TYPE=? OR TYPE=? OR TYPE=?)");
		args.add(Commons.FILE_LOTTERY_LOGO);
		args.add(Commons.FILE_LOTTERY_INFO);
		args.add(Commons.FILE_LOTTERY_DETAIL);
		buf.append(" LEFT JOIN T_LOTTERY_DEVNO_PRODUCT LD ON LD.LOTTERY_PRODUCT_ID=LP.ID AND LD.LOTTERY_ID=LP.LOTTERY_ID");
		buf.append(" LEFT JOIN T_DEVICE_RELATION DR ON DR.FACTORY_DEV_NO=LD.FACTORY_DEV_NO");
		buf.append(" LEFT JOIN T_DEVICE_LOG DL ON DL.DEVICE_NO=DR.DEV_NO AND DL.DEVICE_STATUS=?");
		args.add(Commons.DEVICE_STATUS_OFFLINE);
		buf.append(" WHERE 1=1");
		if(null != lotteryId){
			buf.append(" AND LD.LOTTERY_ID=?");
			args.add(lotteryId);
		}
		if(null != lotteryProductId && null != factoryDevNo){
			buf.append(" AND LD.LOTTERY_PRODUCT_ID=? AND LD.FACTORY_DEV_NO=?");
			args.add(lotteryProductId);
			args.add(factoryDevNo);
		}
		buf.append(" GROUP BY LP.ID,LP.PRODUCT_NAME,LP.PRODUCT_PRICE,LP.PRODUCT_NO,LD.FACTORY_DEV_NO,LD.LOTTERY_PRODUCT_ID,LD.IS_SUCESSSTATE,dl.device_status");
		List<LotteryProduct> lotteryProductList = genericDao.findTs(LotteryProduct.class, buf.toString(), args.toArray());
		if(null != lotteryProductList && !lotteryProductList.isEmpty()){
			for (LotteryProduct lotteryProduct : lotteryProductList) {
				if(null == lotteryProduct.getDeviceState()){//设备不离线
					if(2 == type)
						lotteryProduct = splitImages(lotteryProduct);
					
					if(Commons.IS_PUBLISH_0 == lottery.getIsPublish())//如果活动未发布，操作直接针对单个内容发布的话，活动状态也要更新为已发布
						genericDao.execute("UPDATE T_LOTTERY SET IS_PUBLISH=? WHERE ID=? AND IS_PUBLISH=?", Commons.IS_PUBLISH_1, lottery.getId(), Commons.IS_PUBLISH_0);
					
					if(Commons.IS_PUBLISH_1 == lottery.getIsPublish()){//如果已发布
						
						if(Commons.LOTTERY_STATE_0.equals(lottery.getState()))//活动未开始(变更发布状态)
							genericDao.execute("UPDATE T_LOTTERY SET IS_PUBLISH=? WHERE ID=? AND IS_PUBLISH=?", Commons.IS_PUBLISH_0, lottery.getId(), Commons.IS_PUBLISH_1);
							
						if(Commons.LOTTERY_STATE_1.equals(lottery.getState()) || Commons.LOTTERY_STATE_5.equals(lottery.getState()))//活动已开始或已预热(变更活动状态)
							genericDao.execute("UPDATE T_LOTTERY SET STATE=? WHERE ID=?", Commons.LOTTERY_STATE_2, lottery.getId());
					
					}
					
					if(null != lotteryProduct.getId() && null != lotteryProduct.getFactoryDevNo()){
						if(2 == type){//发布活动
							genericDao.execute("UPDATE T_LOTTERY_DEVNO_PRODUCT SET IS_SUCESSSTATE=? WHERE LOTTERY_PRODUCT_ID=? AND FACTORY_DEV_NO=?",
									Commons.IS_SUCESSSTATE_2, lotteryProduct.getId(), lotteryProduct.getFactoryDevNo());
						}
						
						if(3 ==type){//取消发布活动
							if(Commons.LOTTERY_STATE_0.equals(lottery.getState())){//活动未开始(未发布的不操作)
								genericDao.execute("UPDATE T_LOTTERY_DEVNO_PRODUCT SET IS_SUCESSSTATE=? WHERE LOTTERY_PRODUCT_ID=? AND FACTORY_DEV_NO=? AND IS_SUCESSSTATE!=?",
										Commons.IS_SUCESSSTATE_2, lotteryProduct.getId(), lotteryProduct.getFactoryDevNo(), Commons.IS_SUCESSSTATE_3);
							}
							
							if(Commons.LOTTERY_STATE_1.equals(lottery.getState()) || Commons.LOTTERY_STATE_5.equals(lottery.getState())){//活动已开始或已预热(未发布或发布或预热或活动失败的都不要操作)
								genericDao.execute("UPDATE T_LOTTERY_DEVNO_PRODUCT SET IS_SUCESSSTATE=? WHERE LOTTERY_PRODUCT_ID=? AND FACTORY_DEV_NO=? AND IS_SUCESSSTATE NOT IN (?,?,?,?)",
										Commons.IS_SUCESSSTATE_2, lotteryProduct.getId(), lotteryProduct.getFactoryDevNo(),
										Commons.IS_SUCESSSTATE_3, Commons.IS_SUCESSSTATE_5, Commons.IS_SUCESSSTATE_1, Commons.IS_SUCESSSTATE_7);
							}
							
							if(Commons.LOTTERY_STATE_2.equals(lottery.getState())){//如果活动已结束,但是活动内容结束失败(还可以操作下线功能)
								genericDao.execute("UPDATE T_LOTTERY_DEVNO_PRODUCT SET IS_SUCESSSTATE=? WHERE LOTTERY_PRODUCT_ID=? AND FACTORY_DEV_NO=? AND IS_SUCESSSTATE=?",
										Commons.IS_SUCESSSTATE_2, lotteryProduct.getId(), lotteryProduct.getFactoryDevNo(), Commons.IS_SUCESSSTATE_9);
							}
							
							//TODO 当在设备列表中取消活动，且都取消了，那么活动的状态也需要更新
							List<LotteryDevNoProduct> lotteryDevNoProductList = genericDao.findTs(LotteryDevNoProduct.class,
									"SELECT LOTTERY_PRODUCT_ID,FACTORY_DEV_NO FROM T_LOTTERY_DEVNO_PRODUCT WHERE LOTTERY_ID=? AND IS_SUCESSSTATE NOT IN (?) GROUP BY LOTTERY_PRODUCT_ID,FACTORY_DEV_NO",
									lottery.getId(), Commons.IS_SUCESSSTATE_3);
							
							if(null != lotteryDevNoProductList && lotteryDevNoProductList.size() == 1){//如果在设备内容中取消活动时，最后一个活动内容取消活动时把活动的状态也更新
								if(Commons.LOTTERY_STATE_1.equals(lottery.getState()) || Commons.LOTTERY_STATE_5.equals(lottery.getState()))
									genericDao.execute("UPDATE T_LOTTERY SET STATE=? WHERE ID=?", Commons.LOTTERY_STATE_2, lottery.getId());
								
								if(Commons.LOTTERY_STATE_0.equals(lottery.getState()))
									genericDao.execute("UPDATE T_LOTTERY SET IS_PUBLISH=? WHERE ID=?", Commons.IS_PUBLISH_0, lottery.getId());
							}
						}
					}
					List<ChangeLotteryData> lists = new ArrayList<ChangeLotteryData>();
					List<LotteryDevNoProduct> lotteryProductIDList = findProductCodeNumSellable(lottery.getId(), lotteryProduct.getId(), lotteryProduct.getFactoryDevNo());
					String notifyFlag = null;
					if(null != lotteryProductIDList && !lotteryProductIDList.isEmpty()){
						for (LotteryDevNoProduct product : lotteryProductIDList) {
							ChangeLotteryData changeLotteryData = new ChangeLotteryData();
							if(1 == lottery.getIsProbabil()){//不可修改概率
								changeLotteryData.setNum(product.getNum());
							}
							changeLotteryData.setProductNo(product.getProductCode());
							lists.add(changeLotteryData);
						}
						if(2 == type){
							if(1 == lottery.getIsProbabil()){//可修改短期的
								notifyFlag = Commons.NOTIFY_LOTTERY_ISOPEN;
							}else{
								notifyFlag = Commons.NOTIFY_LOTTERY_LONG_ISOPEN;
							}
						}else if(3 == type){
							notifyFlag = Commons.NOTIFY_LOTTERY_STATE_ISOPEN;
						}
					}
					//推送信息
					ChangeLotteryStateData data = new ChangeLotteryStateData();
					data.setNotifyFlag(notifyFlag);// 打折活动开关变更通知flag
					data.setMessageId(null);
					data.setTime(new Timestamp(System.currentTimeMillis()));
					data.setProductNo(lotteryProduct.getProductNo());
					if(3 == type){
						data.setState(Commons.LOTTERY_ISOPEN_0);
					}else if(2 == type){
						data.setPicUrl(lotteryProduct.getPicUrl());
						data.setPicDetailUrl(lotteryProduct.getPicDetailUrl());
						data.setDesc(lotteryProduct.getDesc());
						data.setProductName(lotteryProduct.getProductName());
						data.setPrice(lotteryProduct.getProductPrice());
					}
					pushChangeProductStateMessage(Arrays.asList(lotteryProduct.getFactoryDevNo()), lists, data);
				}else{//设备离线
					
					if(null != lotteryId){
						lottery.setPushState("离线的设备将不会推送，请 查看设备 后再操作！");
					}
					if(null != lotteryProductId && null != factoryDevNo){
						lottery.setPushState("设备离线不能操作，请在设备在线时在操作！");
					}
				}
			}
		}else{
			throw new BusinessException("没有活动内容,请添加活动内容后再操作！");
		}
		logger.info("******** 【10】.发布抽奖信息  end ********");
		return lottery;
	}
	
	
	
	
	
	

	/* @Title 【11】.前台定时获取抽奖活动发布状态
	 * @param lotteryId 抽奖活动ID
	 * @return
	 */
	@Override
	public Lottery findOneLotteryIsPublish(Long lotteryId) {
		logger.info("********* 【11.前台定时获取抽奖活动发布状态】 start  *********");
		User user = ContextUtil.getUser(User.class);
		if(null == user)
			throw new BusinessException("当前用户未登陆");
		if(null == lotteryId)
			throw new BusinessException("请求错误");
		StringBuffer buf = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		buf.append(" SELECT ID,LOTTERY_NAME,CREATE_TIME,WARM_UP_TIME,START_TIME,END_TIME,STATE");
		buf.append(" FROM T_LOTTERY WHERE ORG_ID=?");
		args.add(user.getOrgId());
		buf.append(" AND ID=?");
		args.add(lotteryId);
		Lottery lottery = genericDao.findT(Lottery.class, buf.toString(), args.toArray());
		args.clear();
		buf.setLength(0);
		if(null != lotteryId){
			buf.append("SELECT * FROM T_LOTTERY_PRODUCT WHERE LOTTERY_ID=?");
			args.add(lotteryId);
			List<LotteryProduct> lotteryProductList = genericDao.findTs(LotteryProduct.class, buf.toString(), args.toArray());
			if(null != lotteryProductList && !lotteryProductList.isEmpty())
				lottery.setLotteryProductList(lotteryProductList);
		}
		logger.info("********* 【11.前台定时获取抽奖活动发布状态】 end  *********");
		return lottery;
	}
	
	/* @Title 【12】.导出单个活动内容订单数据
	 * @param lotteryProductId 活动内容ID
	 * @return
	 */
	@Override
	public List<Order> findLotteryOrderList(Long lotteryProductId) {
		logger.info("********* 【12.导出单个活动内容订单数据】 start  *********");
		User user = ContextUtil.getUser(User.class);
		if(null == user)
			throw new BusinessException("当前用户未登陆");
		if(null == lotteryProductId)
			throw new BusinessException("请求错误");
		Lottery lottery = genericDao.findT(Lottery.class, "SELECT ID,START_TIME,END_TIME,WARM_UP_TIME FROM T_LOTTERY WHERE ID=(SELECT LOTTERY_ID FROM T_LOTTERY_PRODUCT WHERE ID=?)", lotteryProductId);
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer("SELECT ");
		String columns = SQLUtils.getColumnsSQL(OrderDetail.class, "OD") + ",A.NAME";
		buf.append(columns);
		buf.append(" AS orgName, p.sku_name as skuName, OD.qty * OD.price as amount, O.pay_time as payTime, O.pay_code as payCode, O.username as openId, PP.POINT_ADDRESS as pointAddress, DR.FACTORY_DEV_NO AS factoryDevNo");
		buf.append(" FROM T_ORDER_DETAIL OD");
		buf.append(" LEFT JOIN T_ORDER O ON O.CODE=OD.ORDER_NO");
		buf.append(" LEFT JOIN SYS_ORG A ON A.ID=O.ORG_ID");
		buf.append(" LEFT JOIN T_DEVICE D ON D.DEV_NO=O.DEVICE_NO");
		buf.append(" LEFT JOIN T_DEVICE_RELATION DR ON DR.DEV_NO=D.DEV_NO");
		buf.append(" LEFT JOIN T_POINT_PLACE PP ON PP.POINT_NO=O.POINT_NO");
		buf.append(" LEFT JOIN T_PRODUCT P ON P.ID = OD.SKU_ID ");
		buf.append(" LEFT JOIN T_LOTTERY_DEVNO_PRODUCT LD ON LD.FACTORY_DEV_NO=DR.FACTORY_DEV_NO");
		buf.append(" WHERE LD.LOTTERY_PRODUCT_ID=? AND OD.ORDER_TYPE=? AND O.STATE=?");
		args.add(lotteryProductId);
		args.add(Commons.ORDER_TYPE_LOTTERY);
		args.add(Commons.ORDER_STATE_FINISH);
		buf.append(" AND PP.STATE != ? ");
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
		buf.append(" AND (O.CREATE_TIME BETWEEN ? AND ?)");
		args.add(lottery.getStartTime());
		args.add(lottery.getEndTime());

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

		buf.append("GROUP BY ").append(columns);
		buf.append(", p.sku_name, O.pay_time, O.pay_code, O.username, PP.POINT_ADDRESS, DR.FACTORY_DEV_NO");
		logger.info("********* 【12.导出单个活动内容订单数据】 end  *********");
		return genericDao.findTs(Order.class, buf.toString(), args.toArray());
	}
	
	/** 
	 * @Title: 时间判断
	 * @param lottery  
	 */
	private void booleanTime(Lottery lottery) {
		if(null == lottery.getWarmUpTime() || null == lottery.getStartTime() || null == lottery.getEndTime())
			throw new BusinessException("活动时间都不能为空");
		if(new Date().getTime() > lottery.getWarmUpTime().getTime())
			throw new BusinessException("活动预热时间不能小于当前时间");
		if (lottery.getWarmUpTime().getTime() >= lottery.getStartTime().getTime())
			throw new BusinessException("活动预热时间不能大于等于活动开始时间");
		if (lottery.getStartTime().getTime() >= lottery.getEndTime().getTime())
			throw new BusinessException("活动开始时间不能大于等于活动结束时间");
		boolean Statefalse = ((lottery.getStartTime().getTime() - lottery.getWarmUpTime().getTime()) / (1000 * 60)) < 1;
		if(Statefalse)	
			throw new BusinessException("预热时间与开始时间必须间隔一分钟！");
		Statefalse = ((lottery.getEndTime().getTime() - lottery.getStartTime().getTime()) / (1000 * 60)) < 1;
		if(Statefalse)	
			throw new BusinessException("开始时间与结束时间必须间隔一分钟！");
	}
	
	/**
	 * @Title: 获取单个活动的订单统计数据
	 * @param orgId
	 *            －机构ID
	 * @param lottery
	 *            －活动对象
	 * @param lotteryProduct
	 *            －活动内容对象
	 * @return: Order
	 */
	private Order findOneOrder(Long orgId, Lottery lottery, LotteryProduct lotteryProduct) {
		StringBuffer buf = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		
		buf.append(" SELECT (");
		/** 订单总数 */
		buf.append(" SELECT COUNT(O.ID) FROM T_ORDER O");
		buf.append(" WHERE O.CODE IN (");
		buf.append(" 	SELECT OD.ORDER_NO FROM T_ORDER_DETAIL OD");
		buf.append(" 	LEFT JOIN T_LOTTERY_PRODUCT LP ON LP.PRODUCT_NO=OD.LOTTERY_PRODUCT_NO");
		buf.append(" 	WHERE LP.ID=? AND LOTTERY_ID=? AND OD.ORDER_TYPE=? AND OD.ORG_ID=?");
		args.add(lotteryProduct.getId());
		args.add(lottery.getId());
		args.add(Commons.ORDER_TYPE_LOTTERY);
		args.add(orgId);
		buf.append(" 	AND OD.LOTTERY_PRODUCT_NO=?");
		args.add(lotteryProduct.getProductNo());
		buf.append(" 	GROUP BY OD.ORDER_NO");
		buf.append(" ) AND O.CREATE_TIME BETWEEN ? AND ?");
		args.add(lottery.getStartTime());
		args.add(lottery.getEndTime());
		buf.append(") totalOrdersNumber,");
		/** 成交订单总数 */
		buf.append(" (");
		buf.append(" SELECT COUNT(O.ID) FROM T_ORDER O");
		buf.append(" WHERE O.CODE IN (");
		buf.append(" 	SELECT OD.ORDER_NO FROM T_ORDER_DETAIL OD");
		buf.append(" 	LEFT JOIN T_LOTTERY_PRODUCT LP ON LP.PRODUCT_NO=OD.LOTTERY_PRODUCT_NO");
		buf.append(" 	WHERE LP.ID=? AND LOTTERY_ID=? AND OD.ORDER_TYPE=? AND OD.ORG_ID=?");
		args.add(lotteryProduct.getId());
		args.add(lottery.getId());
		args.add(Commons.ORDER_TYPE_LOTTERY);
		args.add(orgId);
		buf.append(" 	AND OD.LOTTERY_PRODUCT_NO=?");
		args.add(lotteryProduct.getProductNo());
		buf.append(" 	GROUP BY OD.ORDER_NO");
		buf.append(" ) AND O.STATE=? AND O.CREATE_TIME BETWEEN ? AND ?");
		args.add(Commons.ORDER_STATE_FINISH);
		args.add(lottery.getStartTime());
		args.add(lottery.getEndTime());
		buf.append(") dealQuantity,");
		/** 下单总金额  */
		buf.append(" (");
		buf.append(" SELECT SUM(O.AMOUNT) FROM T_ORDER O");
		buf.append(" WHERE O.CODE IN (");
		buf.append(" 	SELECT OD.ORDER_NO FROM T_ORDER_DETAIL OD");
		buf.append(" 	LEFT JOIN T_LOTTERY_PRODUCT LP ON LP.PRODUCT_NO=OD.LOTTERY_PRODUCT_NO");
		buf.append(" 	WHERE LP.ID=? AND LOTTERY_ID=? AND OD.ORDER_TYPE=? AND OD.ORG_ID=?");
		args.add(lotteryProduct.getId());
		args.add(lottery.getId());
		args.add(Commons.ORDER_TYPE_LOTTERY);
		args.add(orgId);
		buf.append(" 	AND OD.LOTTERY_PRODUCT_NO=?");
		args.add(lotteryProduct.getProductNo());
		buf.append(" 	GROUP BY OD.ORDER_NO");
		buf.append(" )AND O.CREATE_TIME BETWEEN ? AND ?");
		args.add(lottery.getStartTime());
		args.add(lottery.getEndTime());
		buf.append(") totalOrderAmount,");
		/** 成交总金额  */
		buf.append(" (");
		buf.append(" SELECT SUM(O.AMOUNT) FROM T_ORDER O");
		buf.append(" WHERE O.CODE IN (");
		buf.append(" 	SELECT OD.ORDER_NO FROM T_ORDER_DETAIL OD");
		buf.append(" 	LEFT JOIN T_LOTTERY_PRODUCT LP ON LP.PRODUCT_NO=OD.LOTTERY_PRODUCT_NO");
		buf.append(" 	WHERE LP.ID=? AND LOTTERY_ID=? AND OD.ORDER_TYPE=? AND OD.ORG_ID=?");
		args.add(lotteryProduct.getId());
		args.add(lottery.getId());
		args.add(Commons.ORDER_TYPE_LOTTERY);
		args.add(orgId);
		buf.append(" 	AND OD.LOTTERY_PRODUCT_NO=?");
		args.add(lotteryProduct.getProductNo());
		buf.append(" 	GROUP BY OD.ORDER_NO");
		buf.append(" ) AND O.STATE=? AND O.CREATE_TIME BETWEEN ? AND ?");
		args.add(Commons.ORDER_STATE_FINISH);
		args.add(lottery.getStartTime());
		args.add(lottery.getEndTime());
		buf.append(") clinchDealOrder");
		
		Order order = genericDao.findT(Order.class, buf.toString(), args.toArray());
		order.setSkuName(lotteryProduct.getProductName());
		order.setLotteryPrice(lotteryProduct.getProductPrice());
		order.setLotteryProductId(lotteryProduct.getId());
		
		if(null != order.getTotalOrderAmount() && order.getTotalOrderAmount() != 0){
			order.setTotalOrderAmount(MathUtil.round(order.getTotalOrderAmount(), 2));
		}
		if(null != order.getClinchDealOrder() && order.getClinchDealOrder() != 0){
			order.setClinchDealOrder(MathUtil.round(order.getClinchDealOrder(), 2));
		}
		if(null != order && null != order.getClinchDealOrder() && order.getClinchDealOrder() != 0.0 &&  null != order.getDealQuantity() && order.getDealQuantity() != 0){
			order.setLotteryMeanPrice(MathUtil.round(MathUtil.div(order.getClinchDealOrder(), order.getDealQuantity()), 2));//平均零售价
		}
		return order;
	}

	/**
	 * @Title: 获取商品的库存
	 * @param orgId
	 *            －机构ID
	 * @param factoryDevNo
	 *            －设备组号
	 * @param productId
	 *            －商品ID
	 * @return: Integer
	 */
	private Integer getProductStock(Long orgId, String factoryDevNo, Long productId) {
		StringBuffer buf = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		buf.append(" SELECT SUM(DA.STOCK) as Stock,P.ID");
		buf.append(" FROM T_DEVICE_AISLE DA");
		buf.append(" LEFT JOIN T_DEVICE D ON D.ID = DA.DEVICE_ID");
		buf.append(" LEFT JOIN T_DEVICE_RELATION DR ON DR.DEV_NO=D.DEV_NO");
		buf.append(" LEFT JOIN T_PRODUCT P ON P.ID = DA.PRODUCT_ID");
		buf.append(" WHERE D.ORG_ID = ? AND P.STATE!=? AND D.POINT_ID !=0 AND DR.FACTORY_DEV_NO IN (?) AND P.ID=?");
		args.add(orgId);
		args.add(Commons.PRODUCT_STATE_TRASH);
		args.add(factoryDevNo);
		args.add(productId);
		buf.append(" AND DA.PRODUCT_ID IS NOT NULL");
		buf.append(" GROUP BY P.ID");
		buf.append(" ORDER BY P.ID DESC");
		Product productStock = genericDao.findT(Product.class, buf.toString(), args.toArray());
		Integer stock = 0;
		if(null != productStock && productStock.getStock() != null){
			stock = productStock.getStock();
		}
		return stock;
	}

	/**
	 * @Title: 获取活动内容以及内容图片信息
	 * @param lotteryId
	 *            －抽奖活动ID
	 * @param lotteryProductId
	 *            －内容ID
	 * @param orgId
	 *            －机构ID
	 * @return: LotteryProduct
	 */
	private LotteryProduct findOneLotteryProduct(Long lotteryId, Long lotteryProductId, Long orgId) {
		StringBuffer buf = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		buf.append(" SELECT LP.ID,LP.PRODUCT_NAME,LP.PRODUCT_PRICE,LP.PRODUCT_NORMS,L.IS_PROBABIL,LD.IS_SUCESSSTATE issucessstate,");
		buf.append(" STRING_AGG(B.ID||','||B.NAME||','||B.TYPE||','||B.REAL_PATH, ';' ORDER BY B.TYPE,B.ID DESC) AS IMAGES");
		buf.append(" FROM T_LOTTERY_PRODUCT LP");
		buf.append(" LEFT JOIN T_LOTTERY L ON L.ID = LP.LOTTERY_ID");
		buf.append(" LEFT JOIN T_FILE B ON B.INFO_ID=LP.ID AND ( B.TYPE=? OR B.TYPE=? OR B.TYPE=?) AND INFO_ID=?");
		buf.append(" LEFT JOIN T_LOTTERY_DEVNO_PRODUCT LD ON LD.LOTTERY_PRODUCT_ID=LP.ID AND LD.LOTTERY_ID=L.ID");
		args.add(Commons.FILE_LOTTERY_LOGO);
		args.add(Commons.FILE_LOTTERY_DETAIL);
		args.add(Commons.FILE_LOTTERY_INFO);
		args.add(lotteryProductId);
		buf.append(" WHERE L.ID=? AND L.ORG_ID=? AND LP.ID=?");
		args.add(lotteryId);
		args.add(orgId);
		args.add(lotteryProductId);
		buf.append(" GROUP BY LP.ID,LP.PRODUCT_NAME,LP.PRODUCT_PRICE,LP.PRODUCT_NORMS,L.IS_PROBABIL,LD.IS_SUCESSSTATE");
		return genericDao.findT(LotteryProduct.class, buf.toString(), args.toArray());
	}
	
	/**
	 * @Title: 根据抽奖活动Id,以及活动内容ID，以及出厂设备编号查询出商品编号、以及参加活动的商品数量、是否可售
	 * @param lotteryId
	 *            －抽奖活动ID
	 * @param lotteryProductId
	 *            －活动内容ID
	 * @param factoryDevNo
	 *            －设备出厂编号
	 * @return: List<LotteryDevNoProduct>
	 */
	private List<LotteryDevNoProduct> findProductCodeNumSellable(Long lotteryId, Long lotteryProductId, String factoryDevNo) {
		StringBuffer buf = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		buf.append(" SELECT P.CODE productCode,LD.NUM,DA.SELLABLE sellable");
		buf.append(" FROM T_LOTTERY_DEVNO_PRODUCT LD");
		buf.append(" LEFT JOIN T_PRODUCT P ON P.ID=LD.PRODUCT_ID");
		buf.append(" LEFT JOIN T_DEVICE_AISLE DA ON DA.PRODUCT_ID=P.ID");
		buf.append(" WHERE LD.PRODUCT_ID IS NOT NULL AND LD.LOTTERY_ID=?");
		args.add(lotteryId);
		buf.append(" AND LD.LOTTERY_PRODUCT_ID=? AND LD.FACTORY_DEV_NO=? AND LD.SORT=1 AND DA.SELLABLE=?");
		args.add(lotteryProductId);
		args.add(factoryDevNo);
		args.add(Commons.SELLABLE_TRUE);//是否可售
		buf.append(" GROUP BY P.CODE,LD.NUM,DA.SELLABLE");
		return genericDao.findTs(LotteryDevNoProduct.class, buf.toString(), args.toArray());
	}
	
	/**
	 * @Title: 截取推送的图片数据
	 * @param lotteryProduct
	 *            －活动内容对象
	 * @return: LotteryProduct
	 */
	private LotteryProduct splitImages(LotteryProduct lotteryProduct) {
		if(null != lotteryProduct.getImages()){
			String[] strImage = lotteryProduct.getImages().split(";");//准备号活动内容的图片
			boolean isSetProduct = false;
			boolean isSetProductDetail = false;
			boolean isSetProductInfo = false;
			for (String image : strImage) {
				if (isSetProduct && isSetProductDetail && isSetProductInfo)
					break;
				if (!isSetProduct && Commons.FILE_LOTTERY_LOGO == Integer.valueOf(image.split(",")[2]) && null != image.split(",")[3]) {
					isSetProduct = true;
					lotteryProduct.setPicUrl(dictionaryService.getFileServer() + image.split(",")[3]);
				} else if (!isSetProductDetail && Commons.FILE_LOTTERY_DETAIL == Integer.valueOf(image.split(",")[2]) && null != image.split(",")[3]) {
					isSetProductDetail = true;
					lotteryProduct.setPicDetailUrl(dictionaryService.getFileServer() + image.split(",")[3]);
				} else if (!isSetProductInfo && Commons.FILE_LOTTERY_INFO == Integer.valueOf(image.split(",")[2]) && null != image.split(",")[3]){
					isSetProductInfo = true;
					lotteryProduct.setDesc(dictionaryService.getFileServer() + image.split(",")[3]);
				}
			}
		}
		return lotteryProduct;
	}

	/**
	 * @Title: 定时器更新抽奖活动状态和更新内容状态
	 * @param lotteryId
	 *            －抽奖活动ID
	 * @param state
	 *            －活动需更新的状态
	 * @param isSucessState
	 *            －设备活动内容的状态
	 * @param updateIsSucessSatate
	 *            －设备活动内容需更新的状态
	 * @param factoryDevNo
	 *            －设备组号
	 * @param lotteryProductId
	 *            －活动内容ID(指定设备组号和活动内容ID更新设备活动关联表)
	 * @return: void
	 */
	private void updateLottery(Long lotteryId, String state, String isSucessState, String updateIsSucessSatate, String factoryDevNo, Long lotteryProductId) {
		StringBuffer buf = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		buf.append(" UPDATE T_LOTTERY SET STATE=? WHERE ID=?");
		args.add(state);
		args.add(lotteryId);
		genericDao.execute(buf.toString(), args.toArray());
		args.clear();
		buf.setLength(0);
		buf.append(" UPDATE T_LOTTERY_DEVNO_PRODUCT SET IS_SUCESSSTATE=? WHERE LOTTERY_ID=? AND IS_SUCESSSTATE=? AND FACTORY_DEV_NO=? AND LOTTERY_PRODUCT_ID=?");
		args.add(updateIsSucessSatate);
		args.add(lotteryId);
		args.add(isSucessState);
		args.add(factoryDevNo);
		args.add(lotteryProductId);
		genericDao.execute(buf.toString(), args.toArray());
		
	}

	/*
	 * @Title: 13.根据活动Id查询出活动的设备以及设备所参加的活动内容
	 * @param lotteryId
	 * @return List<Device>
	 */
	@Override
	public List<Device> findLotteryDevNoProduct(Long lotteryId, Page page) {
		logger.info("********* 【13.根据活动Id查询出活动的设备以及设备所参加的活动内容】 start  *********");
		User user = ContextUtil.getUser(User.class);
		if(null == user)
			throw new BusinessException("当前用户未登录！");
		if(null == lotteryId)
			throw new BusinessException("lotteryId参数异常！");
		//1.线根据活动Id查询出所有的设备
		StringBuffer buf = new StringBuffer();
		List<Object> args = new ArrayList<>();
		buf.append(" SELECT T.ID, DR.FACTORY_DEV_NO FACTORYDEVNO, PP.POINT_NAME pointName, PP.POINT_ADDRESS pointaddress, PP.POINT_TYPE POINTTYPE,COALESCE(DL.DEVICE_STATUS, 1) deviceStatus");
		buf.append(" FROM T_DEVICE T");
		buf.append(" LEFT JOIN T_POINT_PLACE PP ON PP.ID=T.POINT_ID");
		buf.append(" LEFT JOIN T_DEVICE_RELATION DR ON DR.DEV_NO=T.DEV_NO");
		buf.append(" LEFT JOIN T_DEVICE_LOG DL ON DL.DEVICE_NO=T.DEV_NO AND DL.DEVICE_STATUS=?");
		args.add(Commons.DEVICE_STATUS_OFFLINE);
		buf.append(" WHERE DR.FACTORY_DEV_NO IN ");
		buf.append(" (");
		buf.append(" 	SELECT FACTORY_DEV_NO FROM T_LOTTERY_DEVNO_PRODUCT WHERE LOTTERY_ID=? GROUP BY FACTORY_DEV_NO");
		args.add(lotteryId);
		buf.append(" )");
		buf.append(" AND PP.STATE != ? ");
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
		buf.append(" GROUP BY T.ID, DR.FACTORY_DEV_NO, PP.POINT_NAME, PP.POINT_ADDRESS, PP.POINT_TYPE, DL.DEVICE_STATUS");
		List<Device> deviceList = genericDao.findTs(Device.class, page, buf.toString(), args.toArray());
		if(null != deviceList && !deviceList.isEmpty()){
			//拼接类型
			deviceList = deviceList(deviceList);
			//2.然后查询出每台设备所有参加的活动内容
			for(Device device : deviceList){
				buf.setLength(0);
				args.clear();
				buf.append(" SELECT LP.ID,LP.PRODUCT_NAME,LP.PRODUCT_PRICE,LP.PRODUCT_NORMS,LD.IS_SUCESSSTATE isSucessState");
				buf.append(" FROM T_LOTTERY_PRODUCT LP ");
				buf.append(" LEFT JOIN T_LOTTERY_DEVNO_PRODUCT LD ON LD.LOTTERY_PRODUCT_ID=LP.ID AND LD.LOTTERY_ID=LP.LOTTERY_ID");
				buf.append(" WHERE LP.ID IN ( ");
				buf.append(" SELECT LOTTERY_PRODUCT_ID FROM T_LOTTERY_DEVNO_PRODUCT WHERE FACTORY_DEV_NO=? AND LOTTERY_ID=? AND LOTTERY_PRODUCT_ID IS NOT NULL");
				args.add(device.getFactoryDevNo());
				args.add(lotteryId);
				buf.append(" GROUP BY LOTTERY_PRODUCT_ID");
				buf.append(" ) AND LD.FACTORY_DEV_NO=?");
				args.add(device.getFactoryDevNo());
				buf.append(" GROUP BY LP.ID,LP.PRODUCT_NAME,LP.PRODUCT_PRICE,LP.PRODUCT_NORMS,LD.IS_SUCESSSTATE");
				List<LotteryProduct> lotteryProductList = genericDao.findTs(LotteryProduct.class, buf.toString(), args.toArray());
				if(null != lotteryProductList && !lotteryProductList.isEmpty())
					device.setLotteryProductList(lotteryProductList);
			}
		}
		logger.info("********* 【13.根据活动Id查询出活动的设备以及设备所参加的活动内容】 end  *********");
		return deviceList;
	}

	public List<Orgnization> getOrgnizationList(Long orgId) {
		return orgnizationService.findCyclicOrgnizations(orgId);
	}

}