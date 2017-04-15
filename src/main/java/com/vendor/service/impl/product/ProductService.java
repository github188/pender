package com.vendor.service.impl.product;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.ecarry.core.dao.IGenericDao;
import com.ecarry.core.dao.SQLUtils;
import com.ecarry.core.domain.BoxValue;
import com.ecarry.core.exception.BusinessException;
import com.ecarry.core.util.MathUtil;
import com.ecarry.core.util.ZHConverter;
import com.ecarry.core.web.core.ContextUtil;
import com.ecarry.core.web.core.Page;
import com.vendor.po.Advertisement;
import com.vendor.po.Cabinet;
import com.vendor.po.Category;
import com.vendor.po.ChangeVirtualPointData;
import com.vendor.po.ChangeVirtualPointStateData;
import com.vendor.po.Device;
import com.vendor.po.DeviceAisle;
import com.vendor.po.DeviceRule;
import com.vendor.po.DiscountProductPointPlace;
import com.vendor.po.FileStore;
import com.vendor.po.OfflineMessage;
import com.vendor.po.Orgnization;
import com.vendor.po.PointPlace;
import com.vendor.po.Product;
import com.vendor.po.User;
import com.vendor.po.VirtualPoint;
import com.vendor.service.IDictionaryService;
import com.vendor.service.IOrgnizationService;
import com.vendor.service.IProductService;
import com.vendor.service.impl.platform.PlatformService.GroupBy;
import com.vendor.util.CommonUtil;
import com.vendor.util.Commons;
import com.vendor.util.DateUtil;
import com.vendor.util.MessagePusher;
import com.vendor.util.RandomUtil;
import com.vendor.vo.app.ChangeProductStateData;
import com.vendor.vo.app.ChangeProductStateProductData;

@Service("productService")
public class ProductService implements IProductService {

	private static Logger LOG = Logger.getLogger(ProductService.class);

	@Autowired
	private IDictionaryService dictionaryService;

	@Autowired
	private IGenericDao genericDao;

	@Autowired
	private IOrgnizationService orgnizationService;

	/**
	 * 查询【销售计划】店铺信息（包括点击按钮查询）
	 * 
	 * @param page
	 * @param store
	 * @return
	 */
	@Override
	public List<PointPlace> findSalesPlanStores(Page page, PointPlace pointPlace) {
		StringBuffer buf = new StringBuffer("SELECT ");
		String cols = SQLUtils.getColumnsSQL(PointPlace.class, "C");
		buf.append(cols);
		buf.append(" ,SO.NAME AS orgName ");
		buf.append(" FROM T_POINT_PLACE C LEFT JOIN SYS_ORG SO ON C.ORG_ID=SO.ID WHERE 1=1 ");
		List<Object> args = new ArrayList<Object>();;

		User user = ContextUtil.getUser(User.class);
		buf.append(" AND C.ORG_ID=? ");
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
	 * 根据设备模式，选定一个具体的设备
	 * 
	 * @param devComb
	 * @return
	 */
	public Long getDeviceId(Long pointId, Integer devCombinationNo) {
		List<Object> args = new ArrayList<Object>();

		StringBuffer buf = new StringBuffer();
		String cols = SQLUtils.getColumnsSQL(Device.class, "C");
		buf.append(" SELECT ");
		buf.append(cols);
		buf.append(" FROM T_DEVICE C ");
		buf.append(" WHERE (C.POINT_ID = ? ");
		buf.append(" AND C.COMBINATION_NO = ? ");
		args.add(pointId);
		args.add(devCombinationNo);

		List<Device> devs = genericDao.findTs(Device.class, buf.toString(), args.toArray());

		if (devs.isEmpty()) {
			throw new BusinessException("不存在未绑定设备");
		}
		// 设备与店铺关联起来，不是真正意义上的绑定
		Device dev = devs.get(0);
		dev.setPointId(pointId);
		genericDao.update(dev);

		return dev.getId();// 未绑定的设备，默认选择第一个
	}

	/**
	 * 根据货柜号获取设备中的一个货柜对象
	 * 
	 * @param deviceId
	 * @param cabinet_no
	 * @return
	 */
	public Cabinet findCabinet(Long deviceId, Integer cabinetNo) {
		if (null == deviceId)
			throw new BusinessException("非法请求");
		StringBuffer buf = new StringBuffer("SELECT ");
		List<Object> args = new ArrayList<Object>();
		String cols = SQLUtils.getColumnsSQL(Cabinet.class, "C");
		buf.append(cols);
		buf.append(" FROM T_CABINET C ");
		buf.append(" WHERE C.CABINET_NO = ? ");
		buf.append(" AND C.DEVICE_ID = ? ");
		args.add(String.valueOf(cabinetNo));
		args.add(deviceId);
		buf.append(" AND C.AISLE_COUNT != 0 ");
		buf.append(" ORDER BY C.CABINET_NO ");

		return genericDao.findT(Cabinet.class, buf.toString(), args.toArray());
	}

	/**
	 * 查询当前设备下的货柜信息
	 */
	@Override
	public List<Cabinet> findCabinets(Long deviceId) {
		if (null == deviceId)
			throw new BusinessException("非法请求");
		StringBuffer buf = new StringBuffer("SELECT ");
		List<Object> args = new ArrayList<Object>();
		String cols = SQLUtils.getColumnsSQL(Cabinet.class, "C");
		buf.append(cols);
		buf.append(" FROM T_CABINET C ");
		buf.append(" WHERE 1=1 ");
		buf.append(" AND C.DEVICE_ID = ? ");
		args.add(deviceId);
		buf.append(" and C.AISLE_COUNT != 0 ");
		buf.append(" ORDER BY C.CABINET_NO ");
		return genericDao.findTs(Cabinet.class, buf.toString(), args.toArray());
	}

	/**
	 * 查询当前设备下的设备商品信息
	 */
	public List<DeviceAisle> findDeviceProdsByCabinet(Long deviceId, Integer cabinetNo) {
		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("当前用户还未登录");
		if (null == deviceId || null == cabinetNo)
			throw new BusinessException("非法请求");

		Cabinet cabinet = findCabinet(deviceId, cabinetNo);
		if (null == cabinet)
			throw new BusinessException("货柜号【" + cabinetNo + "】不存在");

		StringBuffer buf = new StringBuffer("SELECT ");
		List<Object> args = new ArrayList<Object>();
		String cols = SQLUtils.getColumnsSQL(DeviceAisle.class, "C");
		buf.append(cols);
		buf.append(" , MAX(F.REAL_PATH) as images, CAB.CABINET_NO as cabinetNo, CAB.MODEL as model ");
		buf.append(" FROM T_DEVICE_AISLE C ");
		buf.append(" LEFT JOIN T_PRODUCT P ON P.ID = C.PRODUCT_ID ");
		buf.append(" LEFT JOIN T_FILE F ON F.INFO_ID = P.ID AND (F.TYPE=?) ");

		buf.append(" AND P.STATE != ? ");
		args.add(Commons.PRODUCT_STATE_TRASH);// 删除状态

		buf.append(" LEFT JOIN T_CABINET CAB ON CAB.ID = C.CABINET_ID ");
		buf.append(" WHERE 1=1 ");
		buf.append(" AND C.DEVICE_ID = ? ");
		args.add(deviceId);
		buf.append(" AND C.CABINET_ID = ? ");
		args.add(cabinet.getId());

		List<Object> exceptRoadNos = findDeviceRoadExcepts(Commons.FACTORY_CODE_YC, cabinet.getModel());
		if (null != exceptRoadNos && !exceptRoadNos.isEmpty()) {
			buf.append(" AND C.AISLE_NUM NOT IN ( ");// 双货道，第二个货道隐藏

			for (Object exceptRoadNo : exceptRoadNos) {
				buf.append("?,");
				args.add(exceptRoadNo);
			}
			buf.setLength(buf.length() - 1);
			buf.append(")");
		}

		buf.append(" GROUP BY ").append(cols).append(" , CAB.CABINET_NO, CAB.MODEL ");
		buf.append(" ORDER BY CAB.CABINET_NO, C.AISLE_NUM ");
		args.add(0, Commons.FILE_PRODUCT);
		return genericDao.findTs(DeviceAisle.class, buf.toString(), args.toArray());
	}

	public List<Object> findDeviceRoadExcepts(String factoryCode, String model) {
		StringBuffer buffer = new StringBuffer("SELECT ");
		buffer.append(" A.EXCEPT_ROAD_NO FROM T_DEVICE_ROAD_EXCEPT A WHERE A.FACTORY_CODE = ? AND A.MODEL = ? ");
		return genericDao.findListSingle(buffer.toString(), factoryCode, model);
	}

	/**
	 * 查询设备货道信息
	 */
	public DeviceAisle findDeviceAisleByDeviceId(Long deviceId, Long productId, Integer aisleNum) {
		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("当前用户还未登录");
		if (null == deviceId || null == productId)
			throw new BusinessException("非法请求");

		StringBuffer buf = new StringBuffer("SELECT ");
		List<Object> args = new ArrayList<Object>();
		String cols = SQLUtils.getColumnsSQL(DeviceAisle.class, "C");
		buf.append(cols);
		buf.append(" FROM T_DEVICE_AISLE C ");
		buf.append(" WHERE 1=1 ");
		buf.append(" AND C.PRODUCT_ID = ? ");
		args.add(productId);
		buf.append(" AND C.DEVICE_ID = ? ");
		args.add(deviceId);
		buf.append(" AND C.AISLE_NUM != ? ");
		args.add(aisleNum);
		return genericDao.findT(DeviceAisle.class, buf.toString(), args.toArray());
	}

	/**
	 * 查询当前设备下的设备商品信息
	 */
	@Override
	public List<DeviceAisle> findDeviceProds(Long deviceId, Long cabinetId) {
		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("当前用户还未登录");
		if (null == deviceId || null == cabinetId)
			throw new BusinessException("非法请求");

		Cabinet cabinet = findCabinet(cabinetId);
		if (null == cabinet)
			throw new BusinessException("货柜ID【" + cabinetId + "】不存在");

		StringBuffer buf = new StringBuffer("SELECT ");
		List<Object> args = new ArrayList<Object>();
		String cols = SQLUtils.getColumnsSQL(DeviceAisle.class, "C");
		buf.append(cols);
		buf.append(" , MAX(F.REAL_PATH) as images, CAB.CABINET_NO as cabinetNo ");
		buf.append(" FROM T_DEVICE_AISLE C ");
		buf.append(" LEFT JOIN T_PRODUCT P ON P.ID = C.PRODUCT_ID ");
		buf.append(" LEFT JOIN T_FILE F ON F.INFO_ID = P.ID AND (F.TYPE=?) ");

		buf.append(" AND P.STATE != ? ");
		args.add(Commons.PRODUCT_STATE_TRASH);// 删除状态

		buf.append(" LEFT JOIN T_CABINET CAB ON CAB.ID = C.CABINET_ID ");
		buf.append(" WHERE 1=1 ");
		buf.append(" AND C.DEVICE_ID = ? ");
		args.add(deviceId);
		buf.append(" AND C.CABINET_ID = ? ");
		args.add(cabinetId);

		List<Object> exceptRoadNos = findDeviceRoadExcepts(Commons.FACTORY_CODE_YC, cabinet.getModel());
		if (null != exceptRoadNos && !exceptRoadNos.isEmpty()) {
			buf.append(" AND C.AISLE_NUM NOT IN ( ");// 双货道，第二个货道隐藏

			for (Object exceptRoadNo : exceptRoadNos) {
				buf.append("?,");
				args.add(exceptRoadNo);
			}
			buf.setLength(buf.length() - 1);
			buf.append(")");
		}

		buf.append(" GROUP BY ").append(cols).append(" , CAB.CABINET_NO");
		buf.append(" ORDER BY CAB.CABINET_NO, C.AISLE_NUM ");
		args.add(0, Commons.FILE_PRODUCT);
		return genericDao.findTs(DeviceAisle.class, buf.toString(), args.toArray());
	}

	public Cabinet findCabinet(Long id) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer("SELECT ");
		String cols = SQLUtils.getColumnsSQL(Cabinet.class, "C");
		buf.append(cols);
		buf.append(" FROM T_CABINET C WHERE C.ID = ? ");
		args.add(id);
		return genericDao.findT(Cabinet.class, buf.toString(), args.toArray());
	}

	/**
	 * 根据组合模式和货柜号返回设备货道信息
	 * 
	 * @param devCombinationNo
	 * @param cabinetNo
	 * @return
	 */
	@Override
	public List<DeviceAisle> findDeviceProds(Long deviceId, Integer cabinetNo) {
		// 取得货道信息
		List<DeviceAisle> deviceAisles = findDeviceProdsByCabinet(deviceId, cabinetNo);

		// 设置货道长度
		// 取得货道规则
		List<DeviceRule> deviceRules = findDeviceRules(Commons.FACTORY_CODE_YC);// 易触
		// 按型号分组
		Map<String, List<DeviceRule>> deviceRulesMap = group(deviceRules, new GroupBy<String>() {
			@Override
			public String groupby(Object obj) {
				DeviceRule rule = (DeviceRule) obj;
				return rule.getModel();
			}
		});

		for (DeviceAisle deAisle : deviceAisles) {
			String aisleNum = deAisle.getAisleNum() + "";

			forDeviceRule: for (Map.Entry<String, List<DeviceRule>> entry : deviceRulesMap.entrySet()) {
				String model = entry.getKey();
				if (!deAisle.getModel().equals(model))
					continue;

				// 同一型号
				for (DeviceRule rule : entry.getValue()) {
					// 货道容量 = 货道长度/直径
					switch (deAisle.getModel()) {
					case Commons.DEVICE_MODEL_DRINK_SMALL:// 小型饮料机
					case Commons.DEVICE_MODEL_DRINK:// 大型饮料机
						if (Arrays.asList(rule.getRoadCombo().split(",")).contains(aisleNum)) {
							deAisle.setRoadLength(rule.getRoadLength());
							break forDeviceRule;
						}
						break;
					case Commons.DEVICE_MODEL_SPRING:// 弹簧机
					case Commons.DEVICE_MODEL_INTELLIGENT_PRODUCT:// 智能商品机
						if (Arrays.asList(rule.getRoadCombo().split(",")).contains(aisleNum)) {
							deAisle.setCapacity(deAisle.getCapacity() != 0 ? deAisle.getCapacity() : rule.getRoadCapacity());
							break forDeviceRule;
						}
						break;
					case Commons.DEVICE_MODEL_CATERPILLAR:// 履带机（暂时都是5）
						if (null == deAisle.getCapacity() || 0 == deAisle.getCapacity())
							deAisle.setCapacity(rule.getRoadCapacity());
						break forDeviceRule;
					case Commons.DEVICE_MODEL_GRID64:// 64门格子柜
					case Commons.DEVICE_MODEL_GRID40:// 40门格子柜
					case Commons.DEVICE_MODEL_GRID60:// 60门格子柜
						deAisle.setCapacity(rule.getRoadCapacity());
						break forDeviceRule;
					default:
						break;
					}
				}
			}
		}

		return deviceAisles;
	}

    /**
     * @Title: 虚拟商品【01】查看机构的虚拟商品(选中未选中的)
     * @param factoryDevNo
     *            设备编号
     * @return
     * @return: List<Product>
     */
	@Override
	public List<Product> findDeviceVirtualProduct(String factoryDevNo, Page page) {
	    LOG.info("*******虚拟商品【01】查看机构的虚拟商品(选中未选中的)  start*******");
	    User user = ContextUtil.getUser(User.class);
	    if(null == user)
	        throw new BusinessException("当前用户还未登录");
	    StringBuffer buf = new StringBuffer();
	    List<Object> args = new ArrayList<Object>();
	    buf.append(" SELECT STRING_AGG(B.ID||','||B.NAME||','||B.TYPE||','||B.REAL_PATH, ';' ORDER BY B.ID DESC) AS IMAGES,");
	    buf.append(" P.SKU_NAME,P.TYPE,P.ID,COALESCE(A.PRODUCT_ID, 0) sort");
	    buf.append(" FROM T_PRODUCT P");
	    buf.append(" LEFT JOIN T_FILE B ON B.INFO_ID=P.ID AND B.TYPE=?");
	    args.add(Commons.FILE_PRODUCT);
	    if(null != factoryDevNo){
	        buf.append(" LEFT JOIN(");
	        buf.append("     SELECT V.PRODUCT_ID FROM T_VIRTUAL_POINT V WHERE V.FACTORY_DEV_NO=? AND V.PUTAWAY_TYPE IS NOT NULL AND V.ORG_ID=?");
	        args.add(factoryDevNo);
	        args.add(user.getOrgId());
	        buf.append(" )A ON A.PRODUCT_ID=P.ID");
	    }
	    buf.append(" WHERE P.ORG_ID=? AND P.TRUE_OR_FALSE=? AND P.STATE!=?");
	    args.add(user.getOrgId());
	    args.add(Commons.PRODUCT_AND_FALSE);
	    args.add(Commons.PRODUCT_STATE_TRASH);
	    buf.append(" GROUP BY P.SKU_NAME,P.TYPE,P.ID,A.PRODUCT_ID");
	    LOG.info("*******虚拟商品【01】查看机构的虚拟商品(选中未选中的) end*******");
		return genericDao.findTs(Product.class, page, buf.toString(), args.toArray());
	}

    /**
     * @Title: 虚拟商品【02】保存上架信息(且上架)
	 * @param factoryDevNo
	 *            设备组号
     * @param type
     *            1:上架,2:下架(删除)
     * @param productIds
     *            商品集合数组
     * @return: void
     */
    @Override
    public void saveDeviceVirtualProduct(String factoryDevNo, Long... productIds) {
        LOG.info("******* 虚拟商品【02】保存上架信息(且上架) start*******");
        if(null == factoryDevNo)
            throw new BusinessException("请求错误");

        User user = ContextUtil.getUser(User.class);
        if(null == user)
            throw new BusinessException("当前用户还未登录");
        //全量保存  数据库原来有的现在没有了,那么就是删除了
        List<Object> virtualProductIds = genericDao.findListSingle("SELECT PRODUCT_ID FROM T_VIRTUAL_POINT WHERE FACTORY_DEV_NO=? AND ORG_ID=?", factoryDevNo, user.getOrgId());
        if(null != productIds || null != virtualProductIds){
            StringBuffer buf = new StringBuffer();
            List<Object> args = new ArrayList<Object>();
            StringBuffer buffer = new StringBuffer();
            List<Object> args2 = new ArrayList<Object>();
            if(null == productIds){//下架所有虚拟商品
                Long[] ary = convertToStringArr1(virtualProductIds);//集合转数组
                //如果本来数据库中有，现在保存没有，说明全部删除了··
                buffer.append("(");
                for(Long virtualPointId : ary){
                    buffer.append("?,");
                    args2.add(virtualPointId);
                }
                buffer.setLength(buffer.length()-1);
                buffer.append(")");
                //推送消息数据准备
                selectVirtualPointList(factoryDevNo, user.getOrgId(), buffer.toString(), args2, Commons.SELLABLE_FALSE);


                genericDao.execute("DELETE FROM T_VIRTUAL_POINT WHERE FACTORY_DEV_NO=? AND ORG_ID=?", factoryDevNo, user.getOrgId());

            }else{
                Long[] ary = convertToStringArr1(virtualProductIds);//集合转数组

                Long[] intersect = CommonUtil.intersect(ary, productIds);//取得两个数组的交集

                Long[] saveSubstract = CommonUtil.substract(productIds, intersect);//与交集比较获取到差集

                Long[] deleteSubstract = CommonUtil.substract(ary, intersect);//与交集比较获取到差集

                if((null != saveSubstract &&saveSubstract.length > 0) || (null != deleteSubstract && deleteSubstract.length > 0)){//只有数据有变化才去操作数据库且推送
                    if(null != saveSubstract &&saveSubstract.length > 0){//此处代表新增了商品
                        args.clear();
                        buf.setLength(0);
                        //查询出设备ID
                        Long deviceId = genericDao.findSingle(Long .class, "SELECT ID FROM T_DEVICE WHERE DEV_NO=(SELECT DEV_NO FROM T_DEVICE_RELATION WHERE FACTORY_DEV_NO=?)", factoryDevNo);
                        buf.setLength(0);
                        buf.append("INSERT INTO T_VIRTUAL_POINT(DEVICE_ID,FACTORY_DEV_NO,PRODUCT_ID,ORG_ID,CREATE_TIME,PUTAWAY_TYPE)VALUES");

                        buffer.append("(");
                        for(Long substractId : saveSubstract){
                            buf.append("(?,?,?,?,?,?),");
                            args.add(deviceId);
                            args.add(factoryDevNo);
                            args.add(substractId);
                            args.add(user.getOrgId());
                            args.add(new Date(System.currentTimeMillis()));
                            args.add(Commons.VIRTUAL_STATIC_1);//初始值是1，已保存推送
                            //查询推送信息sql
                            buffer.append("?,");
                            args2.add(substractId);
                        }
                        buf.setLength(buf.length()-1);
                        genericDao.execute(buf.toString(), args.toArray());//保存虚拟商品上架信息

                        buffer.setLength(buffer.length()-1);
                        buffer.append(")");
                        //推送消息数据准备
                        selectVirtualPointList(factoryDevNo, user.getOrgId(), buffer.toString(), args2, Commons.SELLABLE_TRUE);
                    }

                    if(null != deleteSubstract && deleteSubstract.length > 0){
                        args.clear();
                        buf.setLength(0);
                        buf.append("DELETE FROM T_VIRTUAL_POINT WHERE FACTORY_DEV_NO=? AND ORG_ID=? AND PRODUCT_ID In(");
                        args.add(0, factoryDevNo);
                        args.add(1, user.getOrgId());

                        buffer.append("(");
                        for(Long deleteSubstractId : deleteSubstract){
                            buf.append("?,");
                            args.add(deleteSubstractId);

                            //查询推送信息sql
                            buffer.append("?,");
                            args2.add(deleteSubstractId);
                        }
                        buf.setLength(buf.length()-1);
                        buf.append(")");

                        //查询推送消息sql
                        buffer.setLength(buffer.length()-1);
                        buffer.append(")");
                        selectVirtualPointList(factoryDevNo, user.getOrgId(), buffer.toString(), args2, Commons.SELLABLE_FALSE);

                        genericDao.execute(buf.toString(), args.toArray());//删除虚拟商品上架信息

                    }
                }
            }
        }
        LOG.info("******* 虚拟商品【02】保存上架信息(且上架) end*******");
    }

    /**
     * TODO  这里还要准备推送的信息(这里只变更改变的  没变的)
     * @param factoryDevNo
     * @param orgId
     * @param Indexs
     * @param args2
     * @param state
     */
    private void selectVirtualPointList(String factoryDevNo, Long orgId, String Indexs, List<Object> args2, Integer state){
        StringBuffer buf = new StringBuffer();
        List<Object> args = new ArrayList<Object>();
        //查询推送信息sql
        buf.append(" SELECT STRING_AGG(B.ID||','||B.NAME||','||B.TYPE||','||B.REAL_PATH, ';' ORDER BY B.TYPE DESC) AS IMAGES,");
        buf.append(" P.CODE productNo,P.SKU_NAME productName,P.CATEGORY categoryType,p.VIRTUAL_URL virtualUrl");
        buf.append(" FROM T_VIRTUAL_POINT VP");
        buf.append(" LEFT JOIN T_PRODUCT P ON P.ID=VP.PRODUCT_ID AND P.ORG_ID=VP.ORG_ID");
        buf.append(" LEFT JOIN T_FILE B ON B.INFO_ID=P.ID AND (B.TYPE=? OR B.TYPE=? OR B.TYPE=? OR B.TYPE=?)");
        args.add(Commons.FILE_PRODUCT);
        args.add(Commons.FILE_PRODUCT_DETAIL);
        args.add(Commons.FILE_VIRTUAL_QRCODE);
        args.add(Commons.FILE_PRODUCT_DESC);
        buf.append(" WHERE VP.FACTORY_DEV_NO=? AND VP.ORG_ID=?");
        args.add(factoryDevNo);
        args.add(orgId);
        if(null != Indexs && null != args2 && !args2.isEmpty()){
            buf.append(" AND VP.PRODUCT_ID IN").append(Indexs);
            args.addAll(args2);
        }
        buf.append(" GROUP BY P.CODE,P.SKU_NAME,P.CATEGORY,p.VIRTUAL_URL");
        List<VirtualPoint> virtualPointList = genericDao.findTs(VirtualPoint.class, buf.toString(), args.toArray());
        if(null != virtualPointList && !virtualPointList.isEmpty()){
            List<ChangeVirtualPointData> virtualPointDataList = new ArrayList<ChangeVirtualPointData>();
            for(VirtualPoint virtualPoint : virtualPointList){
                virtualPoint = splitImages(virtualPoint);//截取图片
                ChangeVirtualPointData virtualPointData = new ChangeVirtualPointData();
                virtualPointData.setProductNo(virtualPoint.getProductNo());
                virtualPointData.setProductName(virtualPoint.getProductName());
                virtualPointData.setPicUrl(null != virtualPoint.getPicUrl()?virtualPoint.getPicUrl():"");
                virtualPointData.setPicDetailUrl(null != virtualPoint.getPicDetailUrl()?virtualPoint.getPicDetailUrl():"");
                virtualPointData.setState("");
                virtualPointData.setDesc(null!=virtualPoint.getDesc()?virtualPoint.getDesc():"");
                virtualPointData.setCagetory_type(virtualPoint.getCategoryType());
                virtualPointData.setQRcode_PicUrl(null != virtualPoint.getQrcode_PicUrl()?virtualPoint.getQrcode_PicUrl():"");
                virtualPointData.setQRcode_StrUrl(virtualPoint.getVirtualUrl()!=null?virtualPoint.getVirtualUrl():"");

                virtualPointDataList.add(virtualPointData);

            }
            ChangeVirtualPointStateData data = new ChangeVirtualPointStateData();
            data.setNotifyFlag(Commons.NOTIFY_VIRTUAL_PRODUCT);
            data.setTime(new Timestamp(System.currentTimeMillis()));
            data.setState(state);

            //推送消息
            pushChangeProductStateMessage(Arrays.asList(factoryDevNo), virtualPointDataList,data);
        }
    }

    /**
     * @Title: 截取推送的图片数据
     * @param lotteryProduct
     *            －活动内容对象
     * @return: LotteryProduct
     */
    private VirtualPoint splitImages(VirtualPoint virtualPoint) {
    	if(null != virtualPoint){
            String[] strImage = virtualPoint.getImages().split(";");//准备号活动内容的图片
            boolean isSetProduct = false;
            boolean isSetProductDetail = false;
            boolean isSetQrcodePicUrl = false;
            boolean isSetDesc = false;
            for (String image : strImage) {
                if (isSetProduct && isSetProductDetail && isSetDesc && isSetQrcodePicUrl)
                    break;
                if (!isSetProduct && Commons.FILE_PRODUCT == Integer.valueOf(image.split(",")[2]) && null != image.split(",")[3]) {//主图
                    isSetProduct = true;
                    virtualPoint.setPicUrl(dictionaryService.getFileServer() + image.split(",")[3]);

                } else if (!isSetProductDetail && Commons.FILE_PRODUCT_DETAIL == Integer.valueOf(image.split(",")[2]) && null != image.split(",")[3]) {//详情图
                    isSetProductDetail = true;
                    virtualPoint.setPicDetailUrl(dictionaryService.getFileServer() + image.split(",")[3]);

                }  else if (!isSetQrcodePicUrl && Commons.FILE_VIRTUAL_QRCODE == Integer.valueOf(image.split(",")[2]) && null != image.split(",")[3]) {//二维码图
					isSetQrcodePicUrl = true;
                    virtualPoint.setQrcode_PicUrl(dictionaryService.getFileServer() + image.split(",")[3]);

                } else if (!isSetDesc && Commons.FILE_PRODUCT_DESC == Integer.valueOf(image.split(",")[2]) && null != image.split(",")[3]){//商品详情大图
					isSetDesc = true;
                    virtualPoint.setDesc(dictionaryService.getFileServer() + image.split(",")[3]);
                }
            }
        }
        return virtualPoint;
    }


    /**
     * @Title 虚拟商品推送内容
     * @param devNos
     *            －设备组号
     * @param lists
     *            －需推送的信息
     * @param state
     *            －0:下线 ,1上线 void 返回类型
     */
    public void pushChangeProductStateMessage(List<String> devNos, List<ChangeVirtualPointData> lists, ChangeVirtualPointStateData data) {
        List<List<ChangeVirtualPointData>> fatherlist = CommonUtil.fatherList(lists, 10);
        String[] devNosArr = devNos.toArray(new String[]{});
        String devNo = CommonUtil.converToString(devNosArr, ",");
        for (List<ChangeVirtualPointData> list : fatherlist) {
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
                List<List<ChangeVirtualPointData>> fatherlists = CommonUtil.fatherList(list, 3);
                for (List<ChangeVirtualPointData> list2 : fatherlists) {
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
                        LOG.info("【虚拟商品推送数据格式：设备编号:" + devNo + ",[" + ContextUtil.getJson(data) + "]】");
                        pusher.pushMessageToAndroidDevices(devNos, ContextUtil.getJson(data), true);
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                        throw new BusinessException("虚拟商品推送失败！");
                    }
                }
            }else{
                //主动通知
                MessagePusher pusher = new MessagePusher();
                try {
                    LOG.info("【虚拟商品推送数据格式：设备编号:"+devNo+",["+ContextUtil.getJson(data)+"]】");
                    pusher.pushMessageToAndroidDevices(devNos, ContextUtil.getJson(data), false);
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                    throw new BusinessException("虚拟商品推送失败！");
                }
            }
        }
    }



	public List<DeviceRule> findDeviceRules(String factoryCode) {
		StringBuffer buffer = new StringBuffer("SELECT ");
		buffer.append(SQLUtils.getColumnsSQL(DeviceRule.class, "A"));
		buffer.append(" FROM T_DEVICE_RULE A WHERE A.FACTORY_CODE = ? ");
		return genericDao.findTs(DeviceRule.class, buffer.toString(), factoryCode);
	}

	/**
	 * 保存商品上架信息
	 * 
	 * @param deviceAisles
	 * @param deviceId
	 * @param orgId
	 */
	public void saveShelf(Cabinet cabinet) {
		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("当前用户还未登录");

		List<DeviceAisle> originDeviceAisles = findDeviceProds(cabinet.getDeviceId(), cabinet.getId());

		for (DeviceAisle originDevice : originDeviceAisles) {
			for (DeviceAisle finalDevice : cabinet.getDeviceAisles()) {
				if (originDevice.getAisleNum().intValue() == finalDevice.getAisleNum().intValue()) {

					if (null != finalDevice.getProductId()) {// 上架
						// 同步商品编码、商品名称、价格
						Product product = findProduct(finalDevice.getProductId());
						if (null == product)
							throw new BusinessException("商品ID【" + finalDevice.getProductId() + "】不存在");

						// 价格一致性校验
						DeviceAisle deAisle = findDeviceAisleByDeviceId(cabinet.getDeviceId(), product.getId(), originDevice.getAisleNum());
						if (null != deAisle && new BigDecimal(deAisle.getPriceOnLine()).compareTo(new BigDecimal(finalDevice.getPriceOnLine())) != 0)
							throw new BusinessException("同一个商品只能有一个价格，请重新输入。");

						originDevice.setProductCode(product.getCode());
						originDevice.setProductName(product.getSkuName());
						originDevice.setPrice(product.getPrice());
						originDevice.setPriceOnLine(finalDevice.getPriceOnLine() == null ? product.getPrice() : finalDevice.getPriceOnLine());
						originDevice.setDiscountValue(getDiscountValue(cabinet.getDeviceId(), finalDevice.getProductId()));// 更新折扣值
					} else {// 下架
						originDevice.setProductCode(null);
						originDevice.setProductName(null);
						originDevice.setPrice(null);
						originDevice.setPriceOnLine(null);
						originDevice.setDiscountValue(1.0);// 更新折扣值
					}

					// 是否可售
					if (null != finalDevice.getSellable() && (Commons.SELLABLE_TRUE == finalDevice.getSellable() || Commons.SELLABLE_FALSE == finalDevice.getSellable()))
						originDevice.setSellable(finalDevice.getSellable());

					// 货道容量
					originDevice.setCapacity(finalDevice.getCapacity() != null ? finalDevice.getCapacity() : 0);
					
					// 换货的情况下，库存改为0（应补货数量随之变为货道容量了）
					if (null != originDevice.getProductId() && null != finalDevice.getProductId() && originDevice.getProductId().longValue() != finalDevice.getProductId().longValue())
						originDevice.setStock(0);
					
					if (null != originDevice.getStock() && null != finalDevice.getCapacity() && finalDevice.getCapacity() > originDevice.getStock())
						originDevice.setSupplementNo(finalDevice.getCapacity() - originDevice.getStock());
					
					originDevice.setProductId(finalDevice.getProductId());
					originDevice.setUpdateUser(user.getId());
					originDevice.setUpdateTime(new Timestamp(System.currentTimeMillis()));
					genericDao.update(originDevice);
				}
			}
		}
	}

	/**
	 * 获取指定商品的实时折扣
	 * @return
	 */
	private Double getDiscountValue(Long deviceId, Long productId) {
		Double discountValue = 1.0;
		List<DiscountProductPointPlace> discountProductPointPlaces = findDiscountProductPointPlaces(deviceId);
		if (null != discountProductPointPlaces && !discountProductPointPlaces.isEmpty()) {
			DiscountProductPointPlace productPointPlace = discountProductPointPlaces.get(0);
			if (Commons.DISCOUNT_TYPE_STORE == productPointPlace.getType()) // 按店铺打折
				discountValue = MathUtil.round(null == productPointPlace.getDiscountValue() ? 1.0 : productPointPlace.getDiscountValue(), 2);
			else {// 按商品打折
				for (DiscountProductPointPlace dpp : discountProductPointPlaces) {
					if (dpp.getProductNo().longValue() == productId.longValue()) {
						discountValue = MathUtil.round(null == dpp.getDiscountValue() ? 1.0 : dpp.getDiscountValue(), 2);
						break;
					}
				}
			}
		}
		
		return discountValue;
	}
	
	/**
	 * 根据设备商品查出店铺商品折扣信息
	 * @param deviceId
	 * @return
	 */
	public List<DiscountProductPointPlace> findDiscountProductPointPlaces(Long deviceId) {
		StringBuffer buf = new StringBuffer("SELECT ");
		List<Object> args = new ArrayList<Object>();
		String cols = SQLUtils.getColumnsSQL(DiscountProductPointPlace.class, "DPP");
		buf.append(cols);
		buf.append(" FROM T_DISCOUNT_PRODUCT_POINTPLACE DPP ");
		buf.append(" LEFT JOIN T_DISCOUNT D ON D.ID = DPP.DISCOUNT_ID ");
		buf.append(" WHERE 1=1 ");
		buf.append(" AND D.STATE = '1' ");
		buf.append(" AND DPP.SORT = 1 ");
		buf.append(" AND DPP.POINTPLACE_NO = (SELECT POINT_NO FROM T_POINT_PLACE WHERE ID = (SELECT POINT_ID FROM T_DEVICE WHERE ID = ? AND POINT_ID != 0) AND STATE != ?) ");
		args.add(deviceId);
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除

		return genericDao.findTs(DiscountProductPointPlace.class, buf.toString(), args.toArray());
	}
	
	/**
	 * 根据设备商品查出货道信息
	 * @param deviceId
	 * @param productId
	 * @return
	 */
	public List<DeviceAisle> findDeviceAisles(Long deviceId, Long productId) {
		StringBuffer buf = new StringBuffer("SELECT ");
		List<Object> args = new ArrayList<Object>();
		String cols = SQLUtils.getColumnsSQL(DeviceAisle.class, "DA");
		buf.append(cols);
		buf.append(" FROM T_DEVICE_AISLE DA ");
		buf.append(" WHERE 1=1 ");
		buf.append(" AND DA.PRODUCT_ID = ? ");
		args.add(productId);
		buf.append(" AND DA.DEVICE_ID IN (SELECT ID FROM T_DEVICE WHERE POINT_ID != 0 AND POINT_ID = (SELECT POINT_ID FROM T_DEVICE WHERE ID = ?)) ");
		args.add(deviceId);
		
		return genericDao.findTs(DeviceAisle.class, buf.toString(), args.toArray());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vendor.service.IProductService#findInventory(com.vendor.po. Product, com.ecarry.core.web.core.Page)
	 */
	@Override
	public List<Product> findInventory(Product product, Page page) {
		StringBuffer buf = new StringBuffer("SELECT ");
		String columns = SQLUtils.getColumnsSQL(Product.class, "A") + ",O.NAME";
		buf.append(columns);
		buf.append(" AS orgName,STRING_AGG(B.ID||','||B.NAME||','||B.TYPE||','||B.REAL_PATH, ';' ORDER BY B.ID DESC) AS images");
		buf.append(" FROM T_PRODUCT A LEFT JOIN SYS_ORG O ON A.ORG_ID=O.ID LEFT JOIN T_FILE B ON A.ID=B.INFO_ID AND B.TYPE=? WHERE A.ORG_ID=? ");
		List<Object> args = new ArrayList<>();
		args.add(ContextUtil.getUser(User.class).getOrgId());
		buf.append(" and A.STATE != ? ");
		args.add(Commons.PRODUCT_STATE_TRASH);

		buf.append(" AND A.CATEGORY != ? ");
		args.add(Commons.PRODUCT_CATEGORY_TYPE);// 抽奖活动的商品除外
		
		if (product.getSkuName() != null) {
			buf.append(" AND A.SKU_NAME LIKE ? ");
			args.add("%" + product.getSkuName() + "%");
		}
		if (product.getCode() != null) {
			buf.append(" AND A.CODE LIKE ? ");
			args.add("%" + product.getCode() + "%");
		}
		if (product.getType() != null) {
			buf.append(" AND A.TYPE=? ");
			args.add(product.getType());
		}
		if (product.getId() != null) {
			buf.append(" AND A.ID=? ");
			args.add(product.getId());
		}
		if (product.getPointId() != null) {// 去掉店铺下不可售的商品
			buf.append(
					" AND A.ID NOT IN(SELECT DISTINCT(DA.PRODUCT_ID) FROM T_DEVICE_AISLE DA WHERE DA.DEVICE_ID IN (SELECT ID FROM T_DEVICE WHERE POINT_ID = ?) AND DA.PRODUCT_ID IS NOT NULL AND DA.SELLABLE = 0) ");
			args.add(product.getPointId());
		}
		buf.append(" AND A.STOCK > 0 ");
		buf.append(" GROUP BY ").append(columns);
		args.add(0, Commons.FILE_PRODUCT);
		return genericDao.findTs(Product.class, page, buf.toString(), args.toArray());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vendor.service.IProductService#resotck()
	 */
	@Override
	public void resotck() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vendor.service.IProductService#findDeVice(com.vendor.po.Device, com.ecarry.core.web.core.Page)
	 */
	@Override
	public List<Device> findDeVice(Device device, Page page) {
		List<Object> args = new ArrayList<>();
		User user = ContextUtil.getUser(User.class);
		StringBuffer sb = new StringBuffer("SELECT T.ID,T.DEV_NO,T.ADDRESS,(SELECT NAME FROM SYS_ORG WHERE ID=T.ORG_ID) AS orgName,STATE,PP.POINT_ADDRESS as pointAddress ");
		sb.append(" FROM T_DEVICE T LEFT JOIN T_POINT_PLACE PP ON PP.ID = T.POINT_ID WHERE T.ORG_ID=? ");
		args.add(user.getOrgId());
		if (!StringUtils.isEmpty(device.getDevNo())) {
			sb.append(" AND DEV_NO LIKE ?");
			args.add("%" + device.getDevNo() + "%");
		}
		if (!StringUtils.isEmpty(device.getPointAddress())) {
			sb.append(" AND PP.POINT_ADDRESS LIKE ?");
			args.add("%" + device.getPointAddress() + "%");
		}
		return genericDao.findTs(Device.class, page, sb.toString(), args.toArray());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vendor.service.IProductService#findDeviceProduct(com.vendor.po. DeviceAisle, com.ecarry.core.web.core.Page)
	 */
	@Override
	public List<DeviceAisle> findDeviceProduct(DeviceAisle aisle, Page page) {
		if (aisle.getDeviceId() == null)
			throw new BusinessException("");
		User user = ContextUtil.getUser(User.class);
		StringBuilder sql = new StringBuilder();
		sql.append(
				" SELECT A.ID,A.PRODUCT_CODE,A.PRODUCT_NAME AS productName,A.PRICE,A.STOCK,A.STOCK_REMIND, A.AISLE_NUM, A.SUPPLEMENT_NO, A.CAPACITY, P.PRICE_ONE AS priceOne, P.PRICE_TWO AS priceTwo ");
		sql.append(" FROM T_DEVICE_AISLE A ");
		sql.append(" LEFT JOIN T_DEVICE D ON D.ID = A.DEVICE_ID ");
		sql.append(" LEFT JOIN T_PRODUCT P ON P.ID=A.PRODUCT_ID ");
		sql.append(
				" WHERE A.DEVICE_ID=? AND D.ORG_ID =? AND P.STATE!=? GROUP BY A.ID,A.PRODUCT_ID,A.PRODUCT_NAME,A.PRICE,A.STOCK,A.STOCK_REMIND,A.AISLE_NUM,A.SUPPLEMENT_NO, A.CAPACITY, P.PRICE_ONE,P.PRICE_TWO");
		return genericDao.findTs(DeviceAisle.class, page, sql.toString(), aisle.getDeviceId(), user.getOrgId(), Commons.PRODUCT_STATE_TRASH);
	}

	private Product findProduct(Long id) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer("SELECT ");
		String cols = SQLUtils.getColumnsSQL(Product.class, "C");
		buf.append(cols);
		buf.append(" FROM T_PRODUCT C WHERE C.ID = ? ");
		args.add(id);
		return genericDao.findT(Product.class, buf.toString(), args.toArray());
	}

	private Product findProduct(Long orgId, String code) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer("SELECT ");
		String cols = SQLUtils.getColumnsSQL(Product.class, "C");
		buf.append(cols);
		buf.append(" FROM T_PRODUCT C WHERE C.ORG_ID = ? AND C.CODE = ? AND STATE !=? ");
		args.add(orgId);
		args.add(code);
		args.add(Commons.PRODUCT_STATE_TRASH);// 删除
		return genericDao.findT(Product.class, buf.toString(), args.toArray());
	}

	private Product findProduct(Long orgId, String sku, String skuName, String code) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer("SELECT ");
		String cols = SQLUtils.getColumnsSQL(Product.class, "C");
		buf.append(cols);
		buf.append(" FROM T_PRODUCT C WHERE C.ORG_ID = ? AND (C.SKU=? OR C.SKU_NAME=? OR C.CODE = ?) AND STATE !=? ");
		args.add(orgId);
		args.add(sku);
		args.add(skuName);
		args.add(code);
		args.add(Commons.PRODUCT_STATE_TRASH);// 删除
		return genericDao.findT(Product.class, buf.toString(), args.toArray());
	}

	/**
	 * 取出request对象中的参数值
	 * 
	 * @param request
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unused" })
	private Map<String, String> getParamsFromWxpay(HttpServletRequest request) {
		Map<String, String> params = new HashMap<String, String>();
		Map requestParams = request.getParameterMap();
		for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
			}
			// 乱码解决，这段代码在出现乱码时使用。
			// valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
			params.put(name, valueStr);
		}
		return params;
	}

	/**
	 * 分页查询商品库存管理列表
	 */
	@Override
	public List<Product> findProductStock(Product product, Page page) {
		StringBuffer buf = new StringBuffer("SELECT ");
		String columns = SQLUtils.getColumnsSQL(Product.class, "A") + ",O.NAME";
		buf.append(columns);
		buf.append(" AS orgName,STRING_AGG(B.ID||','||B.NAME||','||B.TYPE||','||B.REAL_PATH, ';' ORDER BY B.ID DESC) AS images ");
		buf.append(" FROM T_PRODUCT A LEFT JOIN SYS_ORG O ON A.ORG_ID=O.ID LEFT JOIN T_FILE B ON A.ID=B.INFO_ID AND (B.TYPE=? OR B.TYPE=? OR B.TYPE=?) WHERE A.ORG_ID=? ");
		List<Object> args = new ArrayList<>();
		args.add(ContextUtil.getUser(User.class).getOrgId());
		buf.append(" and A.STATE!=? ");
		args.add(Commons.PRODUCT_STATE_TRASH);
		buf.append(" and A.CATEGORY!=?");
		args.add(Commons.PRODUCT_CATEGORY_TYPE);
		if (product.getSkuName() != null) {
			buf.append(" AND A.SKU_NAME LIKE ? ");
			args.add("%" + product.getSkuName() + "%");
		}
		if (product.getSku() != null) {
			buf.append(" AND A.SKU LIKE ? ");
			args.add("%" + product.getSku() + "%");
		}
		if (product.getCode() != null) {
			buf.append(" AND A.CODE LIKE ? ");
			args.add("%" + product.getCode() + "%");
		}
		if (product.getType() != null) {
			buf.append(" AND A.TYPE=? ");
			args.add(product.getType());
		}
		if (product.getId() != null) {
			buf.append(" AND A.ID=? ");
			args.add(product.getId());
		}
		buf.append(" GROUP BY ").append(columns);
		buf.append(" ORDER BY A.CREATE_TIME DESC ");
//		if(product.getSort() != null && "createTime".equals(product.getSort())){
//		    buf.append(",CREATE_TIME "+product.getOrder());
//		}
//		if(product.getSort() != null && "code".equals(product.getSort())){
//		    buf.append(",CODE "+product.getOrder());
//		}
//		if(product.getSort() != null && "type".equals(product.getSort())){
//		    buf.append(",TYPE "+product.getOrder());
//		}
//
		args.add(0, Commons.FILE_PRODUCT);
		args.add(1, Commons.FILE_PRODUCT_DETAIL);
		args.add(2, Commons.FILE_VIRTUAL_QRCODE);
		List<Product> prods = genericDao.findTs(Product.class, page, buf.toString(), args.toArray());
		if(null != prods && !prods.isEmpty()){
			// 商品图片和商品详情图片分开处理
			for (Product prod : prods) {
				String strImage = prod.getImages();
				if (!StringUtils.isEmpty(strImage)) {
					Map<String, String> map = new HashMap<String, String>();
					String[] split = strImage.split(";");
					for (String string : split) {
						String[] splits = string.split(",");
						map.put(splits[2], string);
					}
					prod.setImages(null!=map.get("3")?map.get("3"):"");
					prod.setImagesDetail(null!=map.get("31")?map.get("31"):"");
					prod.setVirtualImage(null!=map.get("15")?map.get("15"):"");
				}
			}
		}
		return prods;
	}

	/**
	 * 查询所有商品编码为负数的商品信息
	 */
	@Override
	public List<Product> findMinusProductNoGoods() {
		StringBuffer buf = new StringBuffer("SELECT ");
		String columns = SQLUtils.getColumnsSQL(Product.class, "A") + ",O.NAME";
		buf.append(columns);
		buf.append(" AS orgName,STRING_AGG(B.ID||','||B.NAME||','||B.TYPE||','||B.REAL_PATH, ';' ORDER BY B.ID DESC) AS images ");
		buf.append(" FROM T_PRODUCT A LEFT JOIN SYS_ORG O ON A.ORG_ID=O.ID LEFT JOIN T_FILE B ON A.ID=B.INFO_ID AND (B.TYPE=? OR B.TYPE=?) WHERE 1=1 ");
		List<Object> args = new ArrayList<>();
		buf.append(" and A.STATE!=? ");
		args.add(Commons.PRODUCT_STATE_TRASH);
		buf.append(" and A.CATEGORY!=?");
		args.add(Commons.PRODUCT_CATEGORY_TYPE);
		buf.append(" and A.CODE LIKE ? ");
		args.add("%-%");
		buf.append(" GROUP BY ").append(columns);
		buf.append(" ORDER BY A.CREATE_TIME DESC ");
		args.add(0, Commons.FILE_PRODUCT);
		args.add(1, Commons.FILE_PRODUCT_DETAIL);
		return genericDao.findTs(Product.class, buf.toString(), args.toArray());
	}

	/**
	 * 保存商品
	 */
	@Override
	public void saveProductStock(Product product, long key, long[] fileIds) {
		User curUser = ContextUtil.getUser(User.class);
		Timestamp curTime = new Timestamp(System.currentTimeMillis());
		product.setOrgId(curUser.getOrgId());
		StringBuffer buf = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		Product oldProduct = null;
		if(1 == product.getTrueOrFalse() && null == product.getId()){//虚拟商品需自动生成可用的code商品编号
			while(true){
				product.setCode(RandomUtil.buildRandom(8)+"");
				//使用生成的code查询看是否已存在
				buf.append("SELECT ID,SKU,SKU_NAME,CODE FROM T_PRODUCT WHERE CODE=? AND ORG_ID=? AND STATE !=?");
				args.add(product.getCode());
				args.add(product.getOrgId());
				args.add(Commons.PRODUCT_STATE_TRASH);
				if (null != product.getId()) {
		            buf.append(" AND ID != ? ");
		            args.add(product.getId());
		        }
				oldProduct = genericDao.findT(Product.class, buf.toString(), args.toArray());
				if(null == oldProduct)
					break;
			}
		}
		//虚拟商品不需要使用code查询
		oldProduct = findExistsProduct(product.getSku(), product.getTrueOrFalse()==1?null:product.getCode(), product.getOrgId(), product.getId()!=null?product.getId():null);

		if(null != oldProduct){
			if (oldProduct.getCode().equals(product.getCode()))
				throw new BusinessException("商品编码已经存在！");
			if (oldProduct.getSku().equals(product.getSku()))
				throw new BusinessException("SKU已经存在！");
			/*if (oldProduct.getSkuName().equals(product.getSkuName()))//解决商品不能重名问题
				throw new BusinessException("商品名称已经存在！");*/
		}

		args.clear();
		buf.setLength(0);
		if (product.getId() == null) {
			String[] ary = product.getSku().split("-");
			Double taxRate = genericDao.findSingle(Double.class, "SELECT COALESCE(TAX_RATE, 0) FROM T_CATEGORY WHERE PARENT_CODE=? AND CODE=?", ary[0] + "-" + ary[1], ary[2]);
			product.setTaxRate(taxRate);
			product.initDefaultValue();
			product.setType(Commons.PROD_TYPE_SELF);// 自有
			product.setCreateUser(curUser.getId());
			product.setCreateTime(curTime);
			if(null == product.getProductCombination() && 0 == product.getTrueOrFalse()){//新增实物商品时组号为空时,那么以本身code为组号
			    product.setProductCombination(product.getCode());
			}
			if(1 == product.getTrueOrFalse()){//虚拟商品
				product.setStock(0);//虚拟商品没有数量
				product.setPrice(0.00);
				product.setPerimeter(Commons.PRODUCT_PERIMETER);
				product.setSpec(product.getSkuName());
				product.setBrand(Commons.PRODUCT_BRAND_OR_ORIGIN);
				product.setOrigin(Commons.PRODUCT_BRAND_OR_ORIGIN);
			}
			genericDao.save(product);

			updateFiles(product, key, curUser);


			if (product.getDescription() != null) {
				List<String> paths = Commons.getImagePaths(product.getDescription());
				if (!paths.isEmpty()) {
					args.add(product.getId());
					args.add(Commons.FILE_PRODUCT_DESC);
					args.add(curUser.getId());
					args.add(0);
					buf.setLength(0);
					buf.append("UPDATE T_FILE SET INFO_ID=? WHERE TYPE=? AND CREATE_USER=? AND INFO_ID<? AND REAL_PATH IN(");
					for (String realPath : paths) {
						buf.append("?,");
						args.add(realPath);
					}
					buf.setLength(buf.length() - 1);
					buf.append(")");
					genericDao.execute(buf.toString(), args.toArray());
				}
			}
		} else {
		    Product products = genericDao.findT(Product.class, "SELECT * FROM T_PRODUCT WHERE ID=?", product.getId());
		    if(null != product.getSku()) {
                if(!product.getSku().equals(products.getSku()))
                    products.setSku(product.getSku());
            }

		    if(null != product.getSkuName())//商品名称
		        products.setSkuName(product.getSkuName());
		    if(null != product.getBrand())//商品品牌
		        products.setBrand(product.getBrand());
		    if(null != product.getOrigin())//商品原产地
		        products.setOrigin(product.getOrigin());
		    if(null != product.getCode())//商品编码
		        products.setCode(product.getCode());
		    if(null != product.getCategory())//商品类别
                products.setCategory(product.getCategory());
		    if(null != product.getSpec())//规格名称
		        products.setSpec(product.getSpec());
		    if(null != product.getPerimeter())//商品周长
		        products.setPerimeter(product.getPerimeter());
		    if(null != product.getPrice())//标准价
		        products.setPrice(product.getPrice());
		    if(null != product.getPriceCombo())//关联推荐价
		        products.setPriceCombo(product.getPriceCombo());
		    if(null != product.getStock())//库存
		        products.setStock(product.getStock());
		    if(null != product.getDescription())//图文描述
		        products.setDescription(product.getDescription());

		    products.setVirtualUrl(product.getVirtualUrl());

			products.setUpdateUser(curUser.getId());
			products.setUpdateTime(curTime);
			products.initDefaultValue();
			genericDao.update(products);

			// APP主页显示图片
            updateFileInfoId(product, key, curUser, Commons.FILE_PRODUCT, null, null);

            // 商品详情页显示图片
            updateFileInfoId(product, key, curUser, null, Commons.FILE_PRODUCT_DETAIL, null);

            if(1 == product.getTrueOrFalse()){//虚拟商品需要更新二维码图片
                //二维码图片
                updateFileInfoId(product, key, curUser, null, null, Commons.FILE_VIRTUAL_QRCODE);

            }

			if (fileIds != null) { // 删除图片
				args.clear();
				args.add(Commons.FILE_PRODUCT);
				args.add(Commons.FILE_YOUPIN);
				args.add(Commons.FILE_PRODUCT_DETAIL);
				args.add(Commons.FILE_VIRTUAL_QRCODE);
				args.add(product.getId());
				buf.setLength(0);
				buf.append(" IN(");
				for (Long id : fileIds) {
					buf.append("?,");
					args.add(id);
				}
				buf.setLength(buf.length() - 1);
				buf.append(")");
				String inIds = buf.toString();
				buf.setLength(0);
				buf.append("SELECT REAL_PATH FROM T_FILE WHERE (TYPE=? OR TYPE=? OR TYPE=? OR TYPE=?) AND INFO_ID=? AND ID").append(inIds);
				List<Object> realPaths = genericDao.findListSingle(buf.toString(), args.toArray());
				if (realPaths.size() == fileIds.length) {
					String[] ary = new String[realPaths.size()];
					realPaths.toArray(ary);
					// List<Integer> idxs = dictionaryService.batchDelete(ary);
					buf.setLength(0);
					buf.append("DELETE FROM T_FILE WHERE (TYPE=? OR TYPE=? OR TYPE=? OR TYPE=?) AND INFO_ID=? AND ID");
					// if (idxs.isEmpty()) {
					buf.append(inIds);
					genericDao.execute(buf.toString(), args.toArray());
				}
			}
			buf.setLength(0);
			buf.append("SELECT REAL_PATH FROM T_FILE WHERE TYPE=? AND INFO_ID=?");
			List<Object> realPaths = genericDao.findListSingle(buf.toString(), Commons.FILE_PRODUCT_DESC, products.getId());
			if (product.getDescription() == null) { // 删除图文信息图片
				String[] ary = new String[realPaths.size()];
				if (ary.length > 0) {
					realPaths.toArray(ary);
					// dictionaryService.batchDelete(ary);
					genericDao.execute("DELETE FROM T_FILE WHERE TYPE=? AND INFO_ID=?", Commons.FILE_PRODUCT_DESC, products.getId());
				}
			} else {
				List<String> paths = Commons.getImagePaths(product.getDescription()!= null? product.getDescription():products.getDescription());
				buf.setLength(0);
				args.clear();
				args.add(Commons.FILE_PRODUCT_DESC);
				args.add(products.getId());
				buf.append("DELETE FROM T_FILE WHERE TYPE=? AND INFO_ID=? AND REAL_PATH IN(");
				for (Object realPath : realPaths) {
					if (paths.contains(realPath)) {
						paths.remove(realPath);
					} else {
						buf.append("?,");
						args.add(realPath);
					}
				}
				if (args.size() != 2) { // 有变更图片
					buf.setLength(buf.length() - 1);
					buf.append(")");
					genericDao.execute(buf.toString(), args.toArray());
					args.remove(0);
					args.remove(0);
					String[] ary = new String[args.size()];
					args.toArray(ary);
					// dictionaryService.batchDelete(ary);
				}
				if (!paths.isEmpty()) {
					args.clear();
					args.add(products.getId());
					args.add(Commons.FILE_PRODUCT_DESC);
					args.add(curUser.getId());
					args.add(0);
					buf.setLength(0);
					buf.append("UPDATE T_FILE SET INFO_ID=? WHERE TYPE=? AND CREATE_USER=? AND INFO_ID<? AND REAL_PATH IN(");
					for (String realPath : paths) {
						buf.append("?,");
						args.add(realPath);
					}
					buf.setLength(buf.length() - 1);
					buf.append(")");
					genericDao.execute(buf.toString(), args.toArray());
				}
			}

			// 修改完成后，更新t_device_aisle表中的标准定价
			buf.setLength(0);
			buf.append(" UPDATE T_DEVICE_AISLE SET PRICE=? WHERE 1=1 AND PRODUCT_ID=? ");
			genericDao.execute(buf.toString(), product.getPrice()!=null?product.getPrice():products.getPrice(), products.getId());
		}
	}

	/**
	 * 更新图片
	 * @param product
	 * @param key
	 * @param curUser
	 */
	private void updateFiles(Product product, long key, User curUser) {
		StringBuffer buf = new StringBuffer();
		int fileYouPin = genericDao.findSingle(int.class, "SELECT COUNT(ID) FROM T_FILE WHERE TYPE=? AND INFO_ID=? AND CREATE_USER=?", Commons.FILE_PRODUCT, key*-1, curUser.getId());
		if(0 != fileYouPin){
            // APP主页显示图片
            updateFileInfoId(product, key, curUser, Commons.FILE_PRODUCT, null, null);
        } else if(null != product.getProductCombination()){
            buf.setLength(0);
            buf.append(" SELECT * FROM T_FILE WHERE TYPE=?");
            buf.append(" AND INFO_ID=(SELECT ID FROM T_PRODUCT WHERE CODE=? AND ORG_ID=?) AND CREATE_USER=?");
            FileStore fileStore =  genericDao.findT(FileStore.class, buf.toString(), Commons.FILE_PRODUCT, product.getProductCombination(), curUser.getOrgId(), curUser.getId());
            if(null != fileStore){
                fileStore.setInfoId(product.getId());
                genericDao.save(fileStore);
            }
        }

		int fileProductDetail = genericDao.findSingle(int.class, "SELECT COUNT(ID) FROM T_FILE WHERE TYPE=? AND INFO_ID=? AND CREATE_USER=?", Commons.FILE_PRODUCT_DETAIL, key*-1, curUser.getId());
		if(0 != fileProductDetail){//本身没有上传图片的
            // 商品详情页显示图片
            updateFileInfoId(product, key, curUser, null, Commons.FILE_PRODUCT_DETAIL, null);
        } else if(null != product.getProductCombination()){
            buf.setLength(0);
            buf.append(" SELECT * FROM T_FILE WHERE TYPE=?");
            buf.append(" AND INFO_ID=(SELECT ID FROM T_PRODUCT WHERE CODE=? AND ORG_ID=?) AND CREATE_USER=?");
            FileStore fileStore =  genericDao.findT(FileStore.class, buf.toString(), Commons.FILE_PRODUCT_DETAIL, product.getProductCombination(), curUser.getOrgId(), curUser.getId());
            if(null != fileStore){
                fileStore.setInfoId(product.getId());
                genericDao.save(fileStore);
            }
        }

		if(1 == product.getTrueOrFalse()){//虚拟商品需要更新二维码图片
            updateFileInfoId(product, key, curUser, null, null, Commons.FILE_VIRTUAL_QRCODE);
        }
	}

	/**
     * @Title:
     * @param product
     * @param key
     * @param curUser
     * @param fileProduct
     * @return: void
     */
    private void updateFileInfoId(Product product, long key, User curUser, Integer fileProduct, Integer fileProductDetail, Integer fileVirtualQRCode) {
        StringBuffer buf = new StringBuffer();
        if(null != fileProduct){
            // APP主页显示图片
            buf.setLength(0);
			buf.append("UPDATE T_FILE SET INFO_ID=? WHERE TYPE=? AND INFO_ID=? AND CREATE_USER=?");
			genericDao.execute(buf.toString(), product.getId(), fileProduct, key * -1, curUser.getId());

        }

        if(null != fileProductDetail){
            // 商品详情页显示图片
            buf.setLength(0);
            buf.append("UPDATE T_FILE SET INFO_ID=? WHERE TYPE=? AND INFO_ID=? AND CREATE_USER=?");
            genericDao.execute(buf.toString(), product.getId(), fileProductDetail, key * -1, curUser.getId());
        }

        if(null != fileVirtualQRCode){
            //二维码图片
            buf.setLength(0);
            buf.append("UPDATE T_FILE SET INFO_ID=? WHERE TYPE=? AND INFO_ID=? AND CREATE_USER=?");
            genericDao.execute(buf.toString(), product.getId(), Commons.FILE_VIRTUAL_QRCODE, key * -1, curUser.getId());
        }


    }

	/**
	 * @Title: 根据条件查询商品
	 * @param sku
	 * @param skuName
	 * @param code
	 * @param orgId
	 * @param productId
	 * @return
	 * @return: Product
	 */
	private Product findExistsProduct(String sku, String code, Long orgId, Long productId) {
		StringBuffer buf = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		buf.append("SELECT ID,SKU,SKU_NAME,CODE FROM T_PRODUCT WHERE 1=1");
		buf.append(" AND (SKU=? OR CODE=?)");
		args.add(sku);
		args.add(code);
		buf.append(" AND ORG_ID=? AND STATE !=? ");
		args.add(orgId);
		args.add(Commons.PRODUCT_STATE_TRASH);
		if (productId != null) {
			buf.append(" AND ID != ? ");
			args.add(productId);
		}
		return genericDao.findT(Product.class, buf.toString(), args.toArray());
	}

	/**
	 * 删除商品
	 */
	@Override
	public void deleteProductStock(Long[] ids) {
		if (ids != null && ids.length > 0) {
			List<Object> args = new ArrayList<Object>();
			StringBuffer sb = new StringBuffer(" IN(");
			for (Long id : ids) {
				sb.append("?,");
				args.add(id);
			}
			sb.setLength(sb.length() - 1);
			sb.append(")");
			String inIds = sb.toString();
			sb.setLength(0);
			sb.append("UPDATE T_PRODUCT SET STATE=? WHERE  ID").append(inIds);
			args.add(0, Commons.PRODUCT_STATE_TRASH);
			int count = genericDao.execute(sb.toString(), args.toArray());
			if (count != ids.length)
				throw new BusinessException("非法请求!");
		}

	}

	/**
	 * 分页查询平台供货商品列表(不需要显示虚拟商品)
	 */
	@Override
	public List<Product> findPlatformSupply(Product product, Page page) {
		StringBuffer buf = new StringBuffer("SELECT ");
		String columns = SQLUtils.getColumnsSQL(Product.class, "A") + ",O.NAME";
		buf.append(columns);
		buf.append(" AS orgName,STRING_AGG(B.ID||','||B.NAME||','||B.TYPE||','||B.REAL_PATH, ';' ORDER BY B.ID DESC) AS images");
		buf.append(" FROM T_PRODUCT A LEFT JOIN SYS_ORG O ON A.ORG_ID=O.ID LEFT JOIN T_FILE B ON A.ID=B.INFO_ID AND B.TYPE=? WHERE A.ORG_ID=? ");
		List<Object> args = new ArrayList<>();
		args.add(ContextUtil.getUser(User.class).getOrgId());
		buf.append(" and A.STATE!=?  and A.TRUE_OR_FALSE!=?");
		args.add(Commons.PRODUCT_STATE_TRASH);
		args.add(Commons.PRODUCT_AND_FALSE);
		if (product.getSkuName() != null) {
			buf.append(" AND A.SKU_NAME LIKE ? ");
			args.add("%" + product.getSkuName() + "%");
		}
		if (product.getSku() != null) {
			buf.append(" AND A.SKU LIKE ? ");
			args.add("%" + product.getSku() + "%");
		}
		if (product.getCode() != null) {
			buf.append(" AND A.CODE LIKE ? ");
			args.add("%" + product.getCode() + "%");
		}
		if (product.getType() != null) {
			buf.append(" AND A.TYPE=? ");
			args.add(product.getType());
		}
		if (product.getId() != null) {
			buf.append(" AND A.ID=? ");
			args.add(product.getId());
		}
		buf.append(" GROUP BY ").append(columns);
		args.add(0, Commons.FILE_PRODUCT);
		return genericDao.findTs(Product.class, page, buf.toString(), args.toArray());
	}

	/**
	 * 查询供货对象
	 */
	public List<Orgnization> findSupplyObject() {
		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("当前用户未登录");

		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer("SELECT ");
		String cols = SQLUtils.getColumnsSQL(Orgnization.class, "C");
		buf.append(cols);
		buf.append(" FROM SYS_ORG C WHERE C.PARENT_ID = ? ");
		args.add(user.getOrgId());
		return genericDao.findTs(Orgnization.class, buf.toString(), args.toArray());
	}

	/**
	 * 进行平台供货
	 */
	public void savePlatformSupply(Long supplyOrgId, List<Product> products) {
		if (null == supplyOrgId || null == products || products.isEmpty())
			throw new BusinessException("参数信息不完整");

		// 遍历供货信息
		for (Product product : products) {
			if (null == product.getId())
				throw new BusinessException("商品ID为空");
			if (null == product.getSupplyCount())
				throw new BusinessException("商品供货数量为空");

			Product productDB = findProduct(product.getId());
			if (null == productDB)
				throw new BusinessException("商品信息不存在");

			// 供货商品的总库存
			int stockDB = null == productDB.getStock() ? 0 : productDB.getStock();
			if (stockDB < product.getSupplyCount())
				throw new BusinessException("【" + productDB.getSkuName() + "】的库存不足");

			// 将当前用户机构的该商品库存扣除
			productDB.setStock(stockDB - product.getSupplyCount());
			genericDao.update(productDB);

			Product supplySelfProduct = findProduct(supplyOrgId, productDB.getSku(), productDB.getSkuName(), productDB.getCode());
			if (null != supplySelfProduct) {
				if (supplySelfProduct.getSku().equals(productDB.getSku()))
					throw new BusinessException("该供货对象已存在sku为【" + productDB.getSku() + "】的商品");
				if (supplySelfProduct.getSkuName().equals(productDB.getSkuName()))
					throw new BusinessException("该供货对象已存在商品名称为【" + productDB.getSkuName() + "】的商品");
				if (supplySelfProduct.getCode().equals(productDB.getCode()))
					throw new BusinessException("该供货对象已存在商品编码为【" + productDB.getCode() + "】的商品");
			}

			// 将所选的供货商品拷贝到供货对象下
			Product supplyProduct = findProduct(supplyOrgId, productDB.getCode());
			if (null == supplyProduct) {// 追加
				productDB.setOrgId(supplyOrgId);
				productDB.setStock(product.getSupplyCount());
				productDB.setId(null);
				productDB.setType(Commons.PROD_TYPE_PLATFORM);// 平台供货
				genericDao.save(productDB);

				// 将所选的供货商品的file信息拷贝到供货对象下
				List<FileStore> fileStores = findFileStores(product.getId());
				for (FileStore file : fileStores) {
					file.setId(null);
					file.setInfoId(productDB.getId());
					genericDao.save(file);
				}
			} else { // 更新库存即可
				int supplyStockDB = null == supplyProduct.getStock() ? 0 : supplyProduct.getStock();
				supplyProduct.setStock(supplyStockDB + product.getSupplyCount());
				genericDao.update(supplyProduct);
			}

		}
	}

	public List<FileStore> findFileStores(Long infoId) {
		StringBuffer buf = new StringBuffer("SELECT ");
		String cols = SQLUtils.getColumnsSQL(FileStore.class, "C");
		buf.append(cols);
		buf.append(" FROM T_FILE C WHERE C.INFO_ID = ? ");
		return genericDao.findTs(FileStore.class, buf.toString(), new Object[] { infoId });
	}

	/**
	 * 查询类目维护数据
	 * 
	 * @param page
	 * @return
	 */
	@Override
	public List<Category> findCategorieMaintains(Page page) {
		if (page != null)
			page.setOrder("parentId,code");

		StringBuffer buf = new StringBuffer("SELECT ");
		String cols = SQLUtils.getColumnsSQL(Category.class, "C");
		buf.append(cols);
		buf.append(" FROM T_CATEGORY C ");
		return genericDao.findTs(Category.class, page, buf.toString());
	}

	/**
	 * 保存类目维护数据
	 * 
	 * @param category 需要保存的商品类目信息
	 * @param key 关联文件标识
	 * @param path 需删除的文件名
	 */
	@Override
	public void saveCategorieMaintains(Category category) {
		if (null == category || StringUtils.isEmpty(category.getName()))
			throw new BusinessException("请输入类目名称");
		if (category.getName().length() > 32)
			throw new BusinessException("类目名称最大长度为32位");

		User curUser = ContextUtil.getUser(User.class);
		Timestamp current = new Timestamp(System.currentTimeMillis());
		Long id;

		// 生成类目编码
		String code = "";
		if (category.getParentId() == null) {// 一级类目
			Category maxCategory = genericDao.findT(Category.class, " SELECT MAX(CODE) AS CODE FROM T_CATEGORY WHERE LEVEL=1 ");
			code = null == maxCategory ? "1000" : Integer.valueOf(maxCategory.getCode().substring(0, 1)) + 1 + maxCategory.getCode().substring(1);
		} else {
			Integer parentLevel = genericDao.findSingle(Integer.class, "SELECT LEVEL FROM T_CATEGORY WHERE ID = ?", category.getParentId());
			if (null == parentLevel)
				throw new BusinessException("类目级别异常！");

			Category maxCategory = genericDao.findT(Category.class, " SELECT MAX(CODE) AS CODE FROM T_CATEGORY WHERE LEVEL=? ", parentLevel + 1);

			if (parentLevel == 1) {// 二级类目
				code = null == maxCategory ? "1100"
						: maxCategory.getCode().substring(0, 1) + (Integer.valueOf(maxCategory.getCode().substring(1, 2)) + 1) + maxCategory.getCode().substring(2);
			} else if (parentLevel == 2) {// 三级类目
				String finalCode = "";
				if (null == maxCategory) {
					finalCode = "1101";
				} else {
					Integer lastCode = Integer.valueOf(maxCategory.getCode().substring(2)) + 1;
					finalCode = maxCategory.getCode().substring(0, 2) + (lastCode > 10 ? lastCode + "" : "0" + lastCode);
				}
				code = finalCode;
			}
		}
		category.setCode(code);

		if (category.getParentId() == null) {// 一级类目
			id = genericDao.findSingle(Long.class, "SELECT ID FROM T_CATEGORY WHERE CODE=? AND PARENT_ID IS NULL", category.getCode());
		} else {
			id = genericDao.findSingle(Long.class, "SELECT ID FROM T_CATEGORY WHERE CODE=? AND PARENT_ID=?", category.getCode(), category.getParentId());
		}
		if (id != null && id != category.getId())
			throw new BusinessException("商品分类编码已经存在！");
		if (category.getId() == null) {
			category.setCreateUser(curUser.getId());
			category.setCreateTime(current);
			if (category.getParentId() == null) {
				category.setLevel(1);
			} else {
				Category parentCategory = genericDao.findT(Category.class, "SELECT CODE,PARENT_CODE,LEVEL FROM T_CATEGORY WHERE ID=?", category.getParentId());
				category.setLevel(parentCategory.getLevel() + 1);
				if (parentCategory.getParentCode() == null) {
					category.setParentCode(parentCategory.getCode());
				} else {
					category.setParentCode(parentCategory.getParentCode() + "-" + parentCategory.getCode());
				}
			}
			category.initDefaultValue();
			genericDao.save(category);
		} else {
			category.setUpdateUser(curUser.getId());
			category.setUpdateTime(current);
			Category oldCategory = genericDao.findT(Category.class, "SELECT CODE,PARENT_ID,PARENT_CODE,TAX_RATE FROM T_CATEGORY WHERE ID=?", category.getId());
			if ((category.getParentId() == null && oldCategory.getParentId() != null)
					|| category.getParentId() != null && !category.getParentId().equals(oldCategory.getParentId()))
				throw new BusinessException("非法请求！");

			BoxValue<String, Object[]> box = SQLUtils.getUpdateSQLByExclude(category, "parentId,parentCode,createTime,createUser");
			int count = genericDao.execute(box.getKey(), box.getValue());
			if (count == 1 && !oldCategory.getCode().equals(category.getCode())) { // 如果修改类目编码，需要修改相关的SKU及修正PARENT_CODE
				if (category.getLevel() <= 2) { // 父级类目才需要修正PARENT_CODE
					String oldParent, parent;
					if (category.getLevel() == 1) {
						oldParent = oldCategory.getCode();
						parent = category.getCode();
					} else {
						oldParent = oldCategory.getParentCode() + "-" + oldCategory.getCode();
						parent = category.getParentCode() + "-" + category.getCode();
					}
					genericDao.execute("UPDATE T_CATEGORY SET PARENT_CODE=? WHERE PARENT_CODE=?", parent, oldParent);
				}
				updateAboutCategory(oldCategory, category.getCode(), category.getTaxRate(), category.getLevel());
			}
		}
	}

	/**
	 * 修改类目编码时，会影响到SKU，要统一修正SKU所涉及的表
	 * 
	 * @param category 原类目
	 * @param code 新类目编码
	 * @param taxRate 新类目税率
	 * @param level 类目级别
	 */
	private void updateAboutCategory(Category category, String code, Double taxRate, int level) {
		if (taxRate == null)
			taxRate = 0d;
		String oldCode = category.getParentCode() == null ? category.getCode() : (category.getParentCode() + "-" + category.getCode());
		StringBuffer buf = new StringBuffer();
		buf.append("UPDATE T_PRODUCT SET SKU=");
		if (level >= 2)
			code = oldCode.substring(0, oldCode.length() - 4) + code;
		buf.append("'").append(code).append("'||SUBSTR(SKU,").append(code.length() + 1).append("),TAX_RATE=?");
		buf.append(" WHERE SUBSTR(SKU,1,").append(oldCode.length()).append(")=?");
		genericDao.execute(buf.toString(), taxRate, oldCode);
	}

	/**
	 * 删除类目维护数据
	 * 
	 * @param ids 需要删除商品类目信息的ID集
	 */
	@Override
	public void deleteCategorieMaintains(Long... ids) {
		if (ids != null && ids.length != 0) {
			String sql = "WITH RECURSIVE T(ID,CODE,LEVEL) AS (SELECT ID,CODE,LEVEL,PARENT_ID FROM T_CATEGORY WHERE ID=? UNION ALL "
					+ "SELECT A.ID,A.CODE,A.LEVEL,A.PARENT_ID FROM T_CATEGORY A JOIN T ON A.ID=T.PARENT_ID) SELECT ID,CODE,LEVEL FROM T ORDER BY LEVEL";
			List<Object> args = new ArrayList<Object>();
			StringBuffer buf = new StringBuffer(" IN(");
			for (Long id : ids) {
				buf.append("?,");
				args.add(id);
				List<Category> categories = genericDao.findTs(Category.class, sql, id);
				if (findAboutCategory(categories) != 0)
					throw new BusinessException("所删除的类目已维护商品信息！");
			}
			buf.setLength(buf.length() - 1);
			buf.append(")");
			String inIds = buf.toString();
			buf.setLength(0);
			int count = genericDao.findSingle(int.class, "SELECT COUNT(*) FROM T_CATEGORY WHERE PARENT_ID" + inIds, args.toArray());
			if (count != 0)
				throw new BusinessException("请先删除子类目！");
			buf.append("DELETE FROM T_CATEGORY WHERE ID").append(inIds);
			genericDao.execute(buf.toString(), args.toArray());
			dictionaryService.saveSyncSysLog(Category.class);
		}
	}

	/**
	 * 查询类目是否已有相关的商品
	 * 
	 * @param categories 类目结构
	 * @return 相关的商品记录数
	 */
	public int findAboutCategory(List<Category> categories) {
		StringBuffer buf = new StringBuffer();
		for (Category category : categories)
			buf.append(category.getCode()).append("-");
		String code = buf.toString();
		buf.setLength(0);
		buf.append("SELECT COUNT(*) FROM T_PRODUCT WHERE SUBSTR(SKU,1,").append(code.length()).append(")=?");
		return genericDao.findSingle(int.class, buf.toString(), code);
	}

	/**
	 * 定时任务更新广告的状态
	 */
	@Override
	public void updateAdvState() {
		LOG.info("*****【更新广告的状态】定时任务***开始***");
		// 取得所有的广告信息
		List<Advertisement> advertisements = findAdvertisements();
		Timestamp now = new Timestamp(System.currentTimeMillis());
		for (Advertisement adv : advertisements) {
			Timestamp startDate = adv.getStartDate();// 广告开始日期
			Timestamp endDate = adv.getEndDate();// 广告结束日期

			String beginTime = adv.getBeginTime();// 播放开始时间
			String endTime = adv.getEndTime();// 播放结束时间

			if (null == startDate || null == endDate || StringUtils.isEmpty(beginTime) || StringUtils.isEmpty(endTime))
				continue;

			String finalStartDateStr = DateUtil.timeStamp2String(startDate, DateUtil.YYYY_MM_DD_EN) + " " + beginTime + ":00";
			String finalEndDateStr = DateUtil.timeStamp2String(endDate, DateUtil.YYYY_MM_DD_EN) + " " + endTime + ":00";

			if (now.before(DateUtil.stringToTimestamp(finalStartDateStr))) // 未开始
				adv.setStatus(Commons.ADV_STATUS_INIT);
			else if (now.before(DateUtil.stringToTimestamp(finalEndDateStr))) // 进行中
				adv.setStatus(Commons.ADV_STATUS_ING);
			else // 结束
				adv.setStatus(Commons.ADV_STATUS_FINISH);

			genericDao.update(adv);
		}
		LOG.info("*****【更新广告的状态】定时任务***结束***");
	}

	public List<Advertisement> findAdvertisements() {
		StringBuffer buf = new StringBuffer("SELECT ");
		String cols = SQLUtils.getColumnsSQL(Advertisement.class, "C");
		buf.append(cols);
		buf.append(" FROM T_ADVERTISEMENT C ");
		return genericDao.findTs(Advertisement.class, buf.toString());
	}

	/**
	 * 商品正式下线
	 * 
	 **/
	@Override
	public void saveProductOffline(Product product) {
		if (null == product) {
			throw new BusinessException("参数不能为空");
		}

		StringBuffer buf = new StringBuffer("SELECT ");
		String columns = SQLUtils.getColumnsSQL(Product.class, "A");
		buf.append(columns);
		buf.append(" FROM T_PRODUCT A ");
		buf.append(" WHERE A.ID = ? ");
		List<Object> args = new ArrayList<>();
		args.add(product.getId());
		Product prod = genericDao.findT(Product.class, buf.toString(), args.toArray());
		if (null != prod) {
			saveProductSellable(prod);
		} else {
			throw new BusinessException("商品无法下线，不存在此产品！");
		}
	}

    /**
     * 虚拟商品推送上下架状态接口
     * @param machineNum 设备组号
     * @param messageId 消息id
     * @param exeState 执行状态
     * @return
     */
    @Override
    public void virtualGoodsExeState(String machineNum, String messageId, String exeState) throws Exception {
        //先删除离线表数据
        genericDao.execute("DELETE FROM T_OFFLINE_MESSAGE WHERE ID=?", messageId);
        //在更新虚拟商品关联表的状态
        //2001 上架成功 2002 上架失败 2003下架成功 2004 下架失败
        LOG.info("******【exeState:"+ exeState +"】*****");
        String state = null;
        switch (exeState){
            case "2001"://2001 上架成功
                state = "2";
                break;
            case "2002"://2002 上架失败
                state = "3";
                break;
            case "2003"://2003下架成功
                state = "4";
                break;
            case "2004"://2004 下架失败
                state = "5";
                break;
            default:
                state = exeState;

        }
        genericDao.execute("UPDATE T_VIRTUAL_POINT SET PUTAWAY_TYPE=? WHERE FACTORY_DEV_NO=?",  state, machineNum);
    }

    /**
	 * 更新货道表中商品可售状态
	 * 
	 * @param product
	 */
	private void saveProductSellable(Product product) {
		if (null == product || null == product.getId() || StringUtils.isEmpty(product.getCode()))
			throw new BusinessException("商品信息不完整");
		Integer sellable = Commons.SELLABLE_FALSE;

		// 更新t_device_aisle
		StringBuffer buf = new StringBuffer("");
		buf.append(" UPDATE T_DEVICE_AISLE SET SELLABLE = ? WHERE PRODUCT_ID = ? ");
		genericDao.execute(buf.toString(), sellable, product.getId());

		// 将商品下线通知到指定店铺下的所有设备
		List<Object> devNos = findDeviceNosByProductId(product.getId());
		if (null != devNos && devNos.size() > 0) {
			String[] devNoArr = CommonUtil.convertToStringArr(devNos);
			// 通知各设备
			pushProductOffLineMessage(Arrays.asList(devNoArr), product.getCode(), sellable);
		}

	}

	/**
	 * 根据商品ID查询投放的设备编号
	 * 
	 * @return
	 */
	public List<Object> findDeviceNosByProductId(Long productId) {
		StringBuffer buffer = new StringBuffer("");
		List<Object> args = new ArrayList<Object>();

		buffer.append(" SELECT DISTINCT(DR.FACTORY_DEV_NO) ");
		buffer.append(" FROM T_DEVICE D ");
		buffer.append(" LEFT JOIN T_DEVICE_AISLE DA ON D.ID = DA.DEVICE_ID ");
		buffer.append(" LEFT JOIN T_DEVICE_RELATION DR ON DR.DEV_NO = D.DEV_NO ");
		buffer.append(" WHERE DA.PRODUCT_ID = ? ");
		args.add(productId);

		User user = ContextUtil.getUser(User.class);

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

		buffer.append(" AND DA.SELLABLE = ? ");
		args.add(Commons.SELLABLE_TRUE);

		return genericDao.findListSingle(buffer.toString(), args.toArray());
	}

	public Long[] convertToStringArr1(List<Object> objList) {
	    Long[] strArr = new Long[objList.size()];
        for (int i = 0; i < strArr.length; i++)
            strArr[i] = Long.valueOf(objList.get(i).toString());
        return strArr;
    }

	/**
	 * 推送商品上下线信息
	 */
	public void pushProductOffLineMessage(List<String> devNos, String productNo, Integer state) {
		ChangeProductStateData data = new ChangeProductStateData();
		data.setNotifyFlag(Commons.NOTIFY_PRODUCT_OFF_OR_ONLINE);// 商品状态变更通知flag（包括价格和商品状态）

		// 商品状态及价格信息
		List<ChangeProductStateProductData> lists = new ArrayList<ChangeProductStateProductData>();
		// 构造商品状态及价格信息
		ChangeProductStateProductData productData = new ChangeProductStateProductData();
		productData.setProductNo(productNo);
		productData.setState(state);
		lists.add(productData);

		data.setTime(new Timestamp(System.currentTimeMillis()));

		List<List<ChangeProductStateProductData>> fatherlist = CommonUtil.fatherList(lists, 15);

		String[] devNosArr = devNos.toArray(new String[] {});
		String devNo = CommonUtil.converToString(devNosArr, ",");
		for (List<ChangeProductStateProductData> list : fatherlist) {

			OfflineMessage offlines = new OfflineMessage();
			data.setList(list);
			offlines.setOfflines(ContextUtil.getJson(data));
			offlines.setDevNos(devNo);// 设备号
			genericDao.save(offlines);// 保存离线时数据
			data.setMessageId(offlines.getId());
			offlines.setOfflines(ContextUtil.getJson(data));
			genericDao.update(offlines);// 更新离线时数据字段messageI
			
			String json = ContextUtil.getJson(data);
			if(json != null && !"".equals(json) && json.length() > 1000){
				genericDao.delete(offlines);
				List<List<ChangeProductStateProductData>> fatherlists = CommonUtil.fatherList(list, 10);
				for (List<ChangeProductStateProductData> list2 : fatherlists) {
					OfflineMessage offline = new OfflineMessage();
					data.setList(list2);
					offline.setOfflines(ContextUtil.getJson(data));
					offline.setDevNos(devNo);// 设备号
					genericDao.save(offline);// 保存离线时数据
					data.setMessageId(offline.getId());
					offline.setOfflines(ContextUtil.getJson(data));
					genericDao.update(offline);// 更新离线时数据字段messageI
					// 主动通知
					MessagePusher pusher = new MessagePusher();
					try {
						pusher.pushMessageToAndroidDevices(devNos, ContextUtil.getJson(data), true);
					} catch (Exception e) {
						LOG.error(e.getMessage(), e);
						throw new BusinessException("推送商品上下线信息失败！");
					}
				}
			}else{
				// 主动通知
				MessagePusher pusher = new MessagePusher();
				try {
					pusher.pushMessageToAndroidDevices(devNos, ContextUtil.getJson(data), true);
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
					throw new BusinessException("推送商品上下线信息失败！");
				}
			}
		}
	}

	/**
	 * 商品下线，分页查询商品列表
	 */
	@Override
	public List<Product> findProductOffline(Product product, Page page) {
		StringBuffer buf = new StringBuffer("SELECT ");
		String columns = SQLUtils.getColumnsSQL(Product.class, "A") + ",O.NAME";
		buf.append(columns);
		buf.append(" AS orgName,STRING_AGG(B.ID||','||B.NAME||','||B.TYPE||','||B.REAL_PATH, ';' ORDER BY B.ID DESC) AS images");
		buf.append(" FROM T_PRODUCT A LEFT JOIN SYS_ORG O ON A.ORG_ID=O.ID LEFT JOIN T_FILE B ON A.ID=B.INFO_ID AND B.TYPE=? WHERE 1=1 ");
		List<Object> args = new ArrayList<>();

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

		buf.append(" AND A.STATE != ? ");
		args.add(Commons.PRODUCT_STATE_TRASH);
		if (product.getSkuName() != null) {
			buf.append(" AND A.SKU_NAME LIKE ? ");
			args.add("%" + product.getSkuName() + "%");
		}
		if (product.getSku() != null) {
			buf.append(" AND A.SKU LIKE ? ");
			args.add("%" + product.getSku() + "%");
		}
		if (product.getCode() != null) {
			buf.append(" AND A.CODE LIKE ? ");
			args.add("%" + product.getCode() + "%");
		}
		if (product.getType() != null) {
			buf.append(" AND A.TYPE = ? ");
			args.add(product.getType());
		}

		buf.append(" GROUP BY ").append(columns);
		buf.append(" ORDER BY A.STOCK ");
		args.add(0, Commons.FILE_PRODUCT);
		return genericDao.findTs(Product.class, page, buf.toString(), args.toArray());
	}

	public List<Orgnization> getOrgnizationList(Long orgId) {
		return orgnizationService.findCyclicOrgnizations(orgId);
	}

	/**
	 * 
	 * @param colls
	 * @param gb
	 * @return
	 */
	public static final <T extends Comparable<T>, D> Map<T, List<D>> group(Collection<D> colls, GroupBy<T> gb) {
		if (colls == null || colls.isEmpty()) {
			System.out.println("分組集合不能為空!");
			return null;
		}
		if (gb == null) {
			System.out.println("分組依據接口不能為Null!");
			return null;
		}
		Iterator<D> iter = colls.iterator();
		Map<T, List<D>> map = new HashMap<T, List<D>>();
		while (iter.hasNext()) {
			D d = iter.next();
			T t = gb.groupby(d);
			if (map.containsKey(t)) {
				map.get(t).add(d);
			} else {
				List<D> list = new ArrayList<D>();
				list.add(d);
				map.put(t, list);
			}
		}
		return map;
	}

}
