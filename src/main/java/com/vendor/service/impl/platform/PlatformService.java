/**
 * 
 */
package com.vendor.service.impl.platform;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.ecarry.core.dao.IGenericDao;
import com.ecarry.core.dao.SQLUtils;
import com.ecarry.core.domain.BoxValue;
import com.ecarry.core.exception.BusinessException;
import com.ecarry.core.util.ZHConverter;
import com.ecarry.core.web.core.ContextUtil;
import com.ecarry.core.web.core.Page;
import com.vendor.po.ActiveCode;
import com.vendor.po.Cabinet;
import com.vendor.po.Category;
import com.vendor.po.DevCombination;
import com.vendor.po.Device;
import com.vendor.po.DeviceAisle;
import com.vendor.po.DeviceRelation;
import com.vendor.po.FileStore;
import com.vendor.po.Orgnization;
import com.vendor.po.PPDevCombination;
import com.vendor.po.PointPlace;
import com.vendor.po.PointReplenishTime;
import com.vendor.po.User;
import com.vendor.service.IDictionaryService;
import com.vendor.service.IOrgnizationService;
import com.vendor.service.IPlatformService;
import com.vendor.util.CommonUtil;
import com.vendor.util.Commons;
import com.vendor.util.DateUtil;
import com.vendor.util.MessagePusher;
import com.vendor.util.RandomUtil;

import net.sf.jxls.reader.ReaderBuilder;
import net.sf.jxls.reader.XLSReader;

/**
 * @author zhaoss on 2016年3月25日
 */
@Service("platformService")
public class PlatformService implements IPlatformService {
	
	private static final Logger logger = Logger.getLogger(PlatformService.class);
	
	@Autowired
	private IDictionaryService dictionaryService;

	@Autowired
	private IGenericDao genericDao;
	
	@Autowired
	private IOrgnizationService orgnizationService;

	/*
	 * (non-Javadoc)
	 * @see com.ecarry.service.IPlatformService#findCyclicCategories(java.lang.Long)
	 */
	@Override
	public List<Category> findCyclicCategories(Long parentId) {
		StringBuffer buf = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		buf.append(SQLUtils.getSelectSQL(Category.class)).append(" WHERE ");
		if (parentId == null) {
			buf.append("PARENT_ID IS NULL");
		} else {
			buf.append("ID=?");
			args.add(parentId);
		}
		buf.append(" ORDER BY CODE");
		List<Category> categories = genericDao.findTs(Category.class, buf.toString(), args.toArray());
		searchCategoryChildren(categories);
		return categories;
	}

	private void searchCategoryChildren(List<Category> parentCategorys) {
		if (parentCategorys != null && parentCategorys.size() != 0) {
			StringBuffer buf = new StringBuffer();
			List<Object> args = new ArrayList<Object>();
			buf.append(SQLUtils.getSelectSQL(Category.class)).append(" WHERE PARENT_ID IN(");
			for (Category category : parentCategorys) {
				buf.append("?,");
				args.add(category.getId());
			}
			buf.setLength(buf.length() - 1);
			buf.append(")");
			buf.append(" ORDER BY PARENT_ID,CODE");
			List<Category> categories = genericDao.findTs(Category.class, buf.toString(), args.toArray());
			int start = 0;
			for (Category category : categories) {
				if (parentCategorys.get(start).getId().longValue() == category.getParentId().longValue()) {
					parentCategorys.get(start).addCategory(category);
				} else {
					for (int i = 0; i < parentCategorys.size(); i++) {
						if (parentCategorys.get(i).getId().longValue() == category.getParentId().longValue()) {
							parentCategorys.get(i).addCategory(category);
							start = i;
							break;
						}
					}
				}
			}
			if (categories.size() != 0) {
				searchCategoryChildren(categories);
			}
		}
	}

	/**
	 * 校验所选设备号是否已上架了商品
	 * @param finalDevNos
	 */
	public void findIsShelving(String finalDevNos) {
		if (StringUtils.isEmpty(finalDevNos))
			throw new BusinessException("非法请求！");
		for (String devNo : Arrays.asList(finalDevNos.split(","))) {
			List<DeviceAisle> deviceAisles = findDeviceAislesByDevNo(devNo);
			if (null != deviceAisles && !deviceAisles.isEmpty())
				throw new BusinessException("设备编号【"+ devNo +"】已上架过商品，请先下架后再做绑定。");
		}
	}
	
	@Override
	public List<Device> findVenderPartnerDevices(Page page, Device device, String startDevNo, String endDevNo, String devNos) {
		if (page != null)
			page.setOrder("FACTORY_DEV_NO");
		StringBuffer buf = new StringBuffer("SELECT ");
		String cols = SQLUtils.getColumnsSQL(Device.class, "C");
		buf.append(cols);
		buf.append(" ,R.FACTORY_DEV_NO AS FACTORYDEVNO ");
		buf.append(" FROM T_DEVICE C ");
		buf.append(" LEFT JOIN T_DEVICE_RELATION R ");
		buf.append(" ON R.DEV_NO = C.DEV_NO ");
		buf.append(" WHERE C.ORG_ID=? ");
		List<Object> args = new ArrayList<Object>();
		args.add(device.getOrgId());
		
		if (!StringUtils.isEmpty(devNos)) {
			String[] devNosArr = devNos.split(",");
			if (null != devNosArr && devNosArr.length > 0) {
				buf.append(" AND C.DEV_NO NOT IN( ");
				for (String devNo : devNosArr) {
					buf.append("?,");
					args.add(devNo);
				}
				buf.setLength(buf.length() - 1);
				buf.append(")");
			}
		}
		
		if (!StringUtils.isEmpty(startDevNo)) {
			buf.append(" AND R.FACTORY_DEV_NO >= ?");
			args.add(startDevNo);
		}
		if (!StringUtils.isEmpty(endDevNo)) {
			buf.append(" AND R.FACTORY_DEV_NO <= ?");
			args.add(endDevNo);
		}
		return genericDao.findTs(Device.class, page, buf.toString(), args.toArray());
	}

	/**
	 * 主机绑定机柜
	 */
	public void saveDeviceAddCabinet(Device device, String... cabNum)
	{
		User user = ContextUtil.getUser(User.class);
		if(user == null)
		{
			throw new BusinessException("当前用户未登录！");
		}
		
		List<Cabinet> cabs = findCabinets(cabNum);
		
		for(Cabinet cab : cabs){
			cab.setDeviceId(device.getId());
			genericDao.save(cab);
		}		
	}
	
	/**
	 * 主机解绑机柜
	 */
	public void saveDeviceUnbindCabinet(Device device, String... cabNum)
	{
		User user = ContextUtil.getUser(User.class);
		if(user == null)
		{
			throw new BusinessException("当前用户未登录！");
		}
		
		List<Cabinet> cabs = findCabinets(cabNum);
		
		for(Cabinet cab : cabs){
			cab.setDeviceId(0L);
			genericDao.save(cab);
		}
	}
	
	/**
	 * 根据货柜号查询货柜
	 * @param store
	 * @return
	 */
	public List<Cabinet> findCabinets(String... cabNums) {
		
		List<Object> args = new ArrayList<Object>();
		StringBuffer in = new StringBuffer(" IN(");
		if (cabNums != null && cabNums.length != 0) {
			
			for (String cabNum : cabNums) {
				in.append("?,");
				args.add(cabNum);
			}
			in.setLength(in.length() - 1);
			in.append(")");
			String inCabNums = in.toString();
			in.setLength(0);
			in.append(" FROM T_CABINET C WHERE  C.CABINET_NO ").append(inCabNums);
		}	
		
		return genericDao.findTs(Cabinet.class, in.toString(), args.toArray());
	}
	
	/**
	 * 解绑店铺设备
	 */
	public void saveStoreUnBindDevices(String... devNums)
	{
		User user = ContextUtil.getUser(User.class);
		if (null == user) throw new BusinessException("当前用户未登录！");
		if (devNums != null && devNums.length != 0) {
			List<Object> args = new ArrayList<Object>();
			StringBuffer buf = new StringBuffer(" IN(");
			for (String devNo : devNums) {
				buf.append("?,");
				args.add(devNo);
			}
			buf.setLength(buf.length() - 1);
			buf.append(")");
			String inDevNos = buf.toString();
			buf.setLength(0);
			buf.append(" UPDATE T_DEVICE SET STORE_ID = 0 WHERE DEV_NO ").append(inDevNos).append(" AND ORG_ID = ? ");
			args.add(user.getOrgId());
			genericDao.execute(buf.toString(), args.toArray());
		}
	}
	
	/**
	 * 解绑城市合伙人/网点设备
	 */
	public void saveVenderpartnerUnBindDevices(String... devNos) {
		User user = ContextUtil.getUser(User.class);
		if (null == user) throw new BusinessException("当前用户未登录！");
		if (devNos != null && devNos.length != 0) {
			List<Object> args = new ArrayList<Object>();
			StringBuffer buf = new StringBuffer(" IN(");
			for (String devNo : devNos) {
				Device device = findDeviceByDevNo(devNo);
				if (null == device) 
					throw new BusinessException("设备编号【"+ devNo +"】不存在");
				User deviceUser = findUserById(device.getCreateUser());
				if (null == deviceUser) 
					throw new BusinessException("设备编号【"+ devNo +"】的创建人不存在");
				if (device.getOrgId() == deviceUser.getOrgId())
					throw new BusinessException("设备编号【"+ devNo +"】是当前机构自己创建的，不能解绑。");
				
				buf.append("?,");
				args.add(devNo);
			}
			buf.setLength(buf.length() - 1);
			buf.append(")");
			String inDevNos = buf.toString();
			buf.setLength(0);
			
			// 将当前机构（合伙人/网点）下的设备转移到上一级机构下
			buf.append(" UPDATE T_DEVICE SET org_id = ?, update_user=?, update_time = ? WHERE DEV_NO ").append(inDevNos);
			List<Object> args2 = new ArrayList<Object>();
			args2.add(user.getOrgId());
			args2.add(user.getId());
			args2.add(new Timestamp(System.currentTimeMillis()));
			args2.addAll(args);
			genericDao.execute(buf.toString(), args2.toArray());
			
			// 将t_we_user表中的该设备（如果存在）所属orgId更改为上一级机构下
			args2.clear();
			args2.add(user.getOrgId());
			args2.addAll(args);
			genericDao.execute("UPDATE T_WE_USER SET ORG_ID=? WHERE DEV_NO" + inDevNos, args2.toArray());
		}
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
	
	/**
	 * 导入设备信息
	 * @param device
	 * @param key
	 * @param fileIds
	 */
	@SuppressWarnings("unchecked")
	public void saveDevices(Device device, long key, String[] fileIds) throws Exception {
		logger.info("*****导入成功*****");
		User user = ContextUtil.getUser(User.class);
		if (null == user) 
			throw new BusinessException("当前用户未登录！");
		if (key == 0) 
			throw new BusinessException("非法操作！");
		FileStore fileStore = findFile(-1 * key, Commons.FILE_EXCEL);
		if (null == fileStore) {
			logger.error("上传文件不存在，请删除后重新导入。");
			throw new BusinessException("上传文件不存在，请删除后重新导入。");			
		}
		// 读取刚上传的excel文件，初始化设备信息（若excel格式有问题，则直接删掉表中数据）
		logger.info("*****fileStore.realPath:*****" + dictionaryService.getFileServer() + fileStore.getRealPath());
		Map<String, Object> beanParams;
		
		beanParams = findByExcel(dictionaryService.getFileServer() + fileStore.getRealPath());
		List<Device> devices = (List<Device>) beanParams.get("devices");// 设备
		List<ActiveCode> activeCodes = (List<ActiveCode>) beanParams.get("activeCodes");// 激活码
		if (null == devices || devices.isEmpty())
			throw new BusinessException("设备信息为空，请上传完整的文件。");
		if (null == activeCodes || activeCodes.isEmpty())
			throw new BusinessException("激活码信息为空，请上传完整的文件。");
		
		// 基础校验
		for (Device dev : devices) {
			if (StringUtils.isEmpty(dev.getDevNo()) || StringUtils.isEmpty(dev.getModel()) || StringUtils.isEmpty(dev.getCabinetNo())
					|| StringUtils.isEmpty(dev.getFactoryNo()) || StringUtils.isEmpty(dev.getFactoryTimeStr()))
				throw new BusinessException("设备数据不完整，请上传完整的设备信息。");
			
			// 校验设备编号是否已存在
			String devNo = findDevNoByFacDevNo(dev.getDevNo());
			if (!StringUtils.isEmpty(devNo))
				throw new BusinessException("导入失败，设备编号【"+ dev.getDevNo() +"】已存在");
		}
		
		// 按设备编号升序排序
		Collections.sort(devices, new Comparator<Device>() {
            public int compare(Device arg0, Device arg1) {
                return arg0.getDevNo().compareTo(arg1.getDevNo());
            }
        });

		// 按设备编号进行分组
        Map<String ,List<Device>> devNoMap = group(devices, new GroupBy<String>() {
            @Override
            public String groupby(Object obj) {
            	Device d = (Device)obj ;
                return d.getDevNo() ;	// 分组依据为设备编号
            }
        });
                
        // 遍历分组后的Map
        for (Map.Entry<String ,List<Device>> entry : devNoMap.entrySet()) {
        	  
            List<Device> finalDevices = entry.getValue();
            if (finalDevices.size() == 1) {// 独立设备，不带托柜，只能是饮料机或中控机或智能商品机
            	Device standaloneDevice = finalDevices.get(0);
            	if (!Commons.DEVICE_MODEL_DRINK.equals(standaloneDevice.getModel()) && !Commons.DEVICE_MODEL_DRINK_SMALL.equals(standaloneDevice.getModel())
            			&& !Commons.DEVICE_MODEL_CENTER_CONTROL.equals(standaloneDevice.getModel()) && !Commons.DEVICE_MODEL_INTELLIGENT_PRODUCT.equals(standaloneDevice.getModel()))
            		throw new BusinessException("导入失败，设备编号【"+ standaloneDevice.getDevNo() +"】不属于独立设备，不能单独存在！");
            	if (!"1".equals(standaloneDevice.getCabinetNo()))
            		throw new BusinessException("导入失败，设备编号【"+ standaloneDevice.getDevNo() +"】的货柜号不是1，请重新设置！");
            	
            	// 创建设备信息
            	Device finalDevice = createDevice(standaloneDevice, user);
            	genericDao.save(finalDevice);// 保存
            	// 创建货柜信息
            	Cabinet finalCabinet = createCabinet(standaloneDevice, user);
            	finalCabinet.setDeviceId(finalDevice.getId());
            	genericDao.save(finalCabinet);// 保存
            	if (Commons.DEVICE_MODEL_CENTER_CONTROL.equals(standaloneDevice.getModel()))// 中控机，不需要创建货道信息
            		continue;
            	// 创建货道信息
            	List<DeviceAisle> finalDeviceAisles = null;
            	switch (standaloneDevice.getModel()) {
            		case Commons.DEVICE_MODEL_DRINK: // 智能饮料机（黑色定制）
            			finalDeviceAisles = createDrinkDeviceAisles(finalDevice, finalCabinet, user);
            		case Commons.DEVICE_MODEL_DRINK_SMALL:// 小型智能饮料机（黑色定制）
            			finalDeviceAisles = createDrinkSmallDeviceAisles(finalDevice, finalCabinet, user);
            		case Commons.DEVICE_MODEL_INTELLIGENT_PRODUCT:// 智能商品机
            			finalDeviceAisles = createIntelligentProductDeviceAisles(finalDevice, finalCabinet, user);
            		default:
            			finalDeviceAisles = new ArrayList<DeviceAisle>();
            	}
            	for (DeviceAisle finalDeviceAisle : finalDeviceAisles)
            		genericDao.save(finalDeviceAisle);// 保存
            	
            } else {// 带托柜的设备，1号货柜只能是饮料机或中控机或智能商品机
            	// 按货柜号升序排序
            	Collections.sort(finalDevices, new Comparator<Device>() {
            		public int compare(Device arg0, Device arg1) {
            			return arg0.getCabinetNo().compareTo(arg1.getCabinetNo());
            		}
            	});
            	// 按型号进行分组
            	Map<String ,List<Device>> modelMap = group(finalDevices, new GroupBy<String>() {
            		@Override
            		public String groupby(Object obj) {
            			Device d = (Device)obj ;
            			return d.getGridModel() ;	// 分组依据为型号备用字段，将【64门格子机】和【40门格子机】合并为格子机
            		}
            	});
            	
            	// 货柜1作为主设备
            	Device mainDevice = finalDevices.get(0);

            	// 基础校验
	        	Set<Device> set = new HashSet<Device>();
	            for(Device dev : finalDevices)
	               set.add(dev);
	            if (set.size() != finalDevices.size()) 
	            	throw new BusinessException("导入失败，设备编号【"+ mainDevice.getDevNo() +"】中存在相同货柜号！");
            	if (!Commons.DEVICE_MODEL_DRINK.equals(mainDevice.getModel()) && !Commons.DEVICE_MODEL_DRINK_SMALL.equals(mainDevice.getModel())
            			&& !Commons.DEVICE_MODEL_CENTER_CONTROL.equals(mainDevice.getModel()) && !Commons.DEVICE_MODEL_INTELLIGENT_PRODUCT.equals(mainDevice.getModel()))
            		throw new BusinessException("导入失败，设备编号【"+ mainDevice.getDevNo() +"】不属于独立设备，不能单独存在！");
            	if (!"1".equals(mainDevice.getCabinetNo()))
            		throw new BusinessException("导入失败，设备编号【"+ mainDevice.getDevNo() +"】的货柜号不是1，请重新设置！");
            	
            	// 创建设备信息
            	Device finalDevice = createDevice(mainDevice, user);
            	genericDao.save(finalDevice);// 保存
            	// 遍历分组后的Map
                for (Map.Entry<String ,List<Device>> modelEntry : modelMap.entrySet()) {
                	// 取得按型号分组后的设备信息
                	List<Device> modelDevices = modelEntry.getValue();
                	
                	// 创建货道信息
            		List<DeviceAisle> modelDeviceAisles = new ArrayList<DeviceAisle>();
               		for (int i = 1; i <= modelDevices.size(); i++) {
                		Device modelDevice = modelDevices.get(i - 1);
                		// 创建货柜信息
                		Cabinet modelCabinet = createCabinet(modelDevice, user);
                		modelCabinet.setDeviceId(finalDevice.getId());
                		genericDao.save(modelCabinet);// 保存
                		if (Commons.DEVICE_MODEL_CENTER_CONTROL.equals(modelDevice.getGridModel()))// 中控机，不需要创建货道信息
                    		continue;
                		if (Commons.DEVICE_MODEL_DRINK.equals(modelDevice.getGridModel())) {//饮料机
                			if (modelDevices.size() > 1)
                				throw new BusinessException("导入失败，设备编号【"+ modelDevice.getDevNo() +"】的型号【"+ modelDevice.getModel() +"】最多只能有一个，请重新设置！");
                			modelDeviceAisles = createDrinkDeviceAisles(finalDevice, modelCabinet, user);
                		} else if (Commons.DEVICE_MODEL_DRINK_SMALL.equals(modelDevice.getGridModel())) {//小型饮料机
                			if (modelDevices.size() > 1)
                				throw new BusinessException("导入失败，设备编号【"+ modelDevice.getDevNo() +"】的型号【"+ modelDevice.getModel() +"】最多只能有一个，请重新设置！");
                			modelDeviceAisles = createDrinkSmallDeviceAisles(finalDevice, modelCabinet, user);
                		} else if (Commons.DEVICE_MODEL_INTELLIGENT_PRODUCT.equals(modelDevice.getGridModel())) {//智能商品机
                			if (modelDevices.size() > 1)
                				throw new BusinessException("导入失败，设备编号【"+ modelDevice.getDevNo() +"】的型号【"+ modelDevice.getModel() +"】最多只能有一个，请重新设置！");
                			modelDeviceAisles = createIntelligentProductDeviceAisles(finalDevice, modelCabinet, user);
                		} else if (Commons.DEVICE_MODEL_SPRING_CATERPILLAR.equals(modelDevice.getGridModel())) {//弹簧柜或履带柜
                			if (!Commons.DEVICE_MODEL_INTELLIGENT_PRODUCT.equals(mainDevice.getModel())) {// 主机不是智能商品机
                				if (modelDevices.size() > 2)
                					throw new BusinessException("导入失败，设备编号【"+ modelDevice.getDevNo() +"】的综合商品机辅机最多只能有两个，请重新设置！");
                				modelDeviceAisles = createSpringDeviceAisles(finalDevice, modelCabinet, user, i);
                    		} else {// 主机是智能商品机，只能再拖一个弹簧机/履带机
                    			if (modelDevices.size() > 1)
                    				throw new BusinessException("导入失败，设备编号【"+ modelDevice.getDevNo() +"】的综合商品机辅机最多只能有一个，请重新设置！");
                    			modelDeviceAisles = createSpringIntelligentProductDeviceAisles(finalDevice, modelCabinet, user, i);
                    		}
                		} else if (Commons.DEVICE_MODEL_GRID.equals(modelDevice.getGridModel())) {//格子柜（64门或40门或64和40组合）
                			int stockRemind = dictionaryService.findMinStock();
            				if (i == 1) {
            					if (Commons.DEVICE_MODEL_GRID64.equals(modelDevice.getModel())) {// 64门
            						modelDeviceAisles = createGrid64DeviceAisles(finalDevice, modelCabinet, user, i);
            					} else if (Commons.DEVICE_MODEL_GRID40.equals(modelDevice.getModel())) {//40门
            						modelDeviceAisles = createGrid40DeviceAisles(finalDevice, modelCabinet, user, i);
            					} else if (Commons.DEVICE_MODEL_GRID60.equals(modelDevice.getModel())) {//60门
            						modelDeviceAisles = createGrid60DeviceAisles(finalDevice, modelCabinet, user, i);
            					}
            				} else {
            					DeviceAisle lastDeviceAisle = modelDeviceAisles.get(modelDeviceAisles.size() - 1);
            					if (null == lastDeviceAisle)
            						throw new BusinessException("系统异常！");
            					int index = Integer.valueOf(lastDeviceAisle.getAisleNum().toString().substring(0, 1));
            					if (Commons.DEVICE_MODEL_GRID64.equals(modelDevice.getModel())) {// 64门
            						if (index > 3)
            							throw new BusinessException("导入失败，设备编号【"+ modelDevice.getDevNo() +"】的格子机货道编号大于5，将会导致无法出货，请重新设置！");
            						int start = Integer.valueOf((index + 1) + "01").intValue();
            						int end = Integer.valueOf((index + 2) + "24").intValue();
            						int minExcept = Integer.valueOf((index + 1) + "40").intValue();
            						int maxExcept = Integer.valueOf((index + 2) + "01").intValue();
            						
            						modelDeviceAisles.clear();// 清空
            						for (int k = start; k <= end; k++) {
            							if (k > minExcept && k < maxExcept)
            								continue;
            							modelDeviceAisles.add(createDeviceAisle(finalDevice, modelCabinet, user, k, stockRemind));
            						}
            					} else if (Commons.DEVICE_MODEL_GRID40.equals(modelDevice.getModel())) {//40门
            						if (index > 4)
            							throw new BusinessException("导入失败，设备编号【"+ modelDevice.getDevNo() +"】的格子机货道编号大于5，将会导致无法出货，请重新设置！");
            						modelDeviceAisles = createGrid40DeviceAisles(finalDevice, modelCabinet, user, index + 1);
            					} else if (Commons.DEVICE_MODEL_GRID60.equals(modelDevice.getModel())) {// 60门
            						if (index > 3)
            							throw new BusinessException("导入失败，设备编号【"+ modelDevice.getDevNo() +"】的格子机货道编号大于5，将会导致无法出货，请重新设置！");
            						int start = Integer.valueOf((index + 1) + "01").intValue();
            						int end = Integer.valueOf((index + 2) + "20").intValue();
            						int minExcept = Integer.valueOf((index + 1) + "40").intValue();
            						int maxExcept = Integer.valueOf((index + 2) + "01").intValue();
            						
            						modelDeviceAisles.clear();// 清空
            						for (int k = start; k <= end; k++) {
            							if (k > minExcept && k < maxExcept)
            								continue;
            							modelDeviceAisles.add(createDeviceAisle(finalDevice, modelCabinet, user, k, stockRemind));
            						}
            					}
            				}
                			
                		}
						for (DeviceAisle modelDeviceAisle : modelDeviceAisles)
							genericDao.save(modelDeviceAisle);// 保存
                	}
                }
            	
            }// 带托柜的设备，1号货柜只能是饮料机或中控机
            
          
        }
        
        // 插入激活码
        for (ActiveCode activeCode : activeCodes) {
        	if (StringUtils.isEmpty(activeCode.getActiveCode()))
        		continue;
        	genericDao.save(createActiveCode(activeCode.getActiveCode(), user));// 保存
        }
        
    	//更新每个设备设备组合模式值
    	updateDevCombinationNo();
	}
	
	private void updateDevCombinationNo() {
		List<DevCombination> devCombList = new ArrayList<DevCombination>();
		List<Device> devs = findDevices(null, null);

		Map<String, List<Device>> devCombMap = getDevCombination(devs);

		Set<Entry<String, List<Device>>> compSet = devCombMap.entrySet();
		for (Map.Entry<String, List<Device>> entry : compSet) {
			devCombList.addAll(saveDevCombination(entry.getValue()));
		}

		// 按组合号升序排序
		Collections.sort(devCombList, new Comparator<DevCombination>() {
			public int compare(DevCombination arg0, DevCombination arg1) {
				return arg0.getCombinationNo().compareTo(arg1.getCombinationNo());
			}
		});

		// 按组合号分组
		Map<Integer, List<DevCombination>> combNoMap = group(devCombList, new GroupBy<Integer>() {
			@Override
			public Integer groupby(Object obj) {
				DevCombination dc = (DevCombination) obj;
				return dc.getCombinationNo();
			}
		});

		// 按设备编号升序排序
		Collections.sort(devs, new Comparator<Device>() {
			public int compare(Device arg0, Device arg1) {
				return arg0.getDevNo().compareTo(arg1.getDevNo());
			}
		});
		// 按设备编号进行分组
		Map<String, List<Device>> devNoMap = group(devs, new GroupBy<String>() {
			@Override
			public String groupby(Object obj) {
				Device d = (Device) obj;
				return d.getDevNo(); // 分组依据为设备编号
			}
		});

		Set<Entry<String, List<Device>>> devNoSet = devNoMap.entrySet();
		for (Map.Entry<String, List<Device>> entry : devNoSet) {
			DevCombination devComb = getDevCombination(combNoMap, entry);
			if (null != devComb) {
				Device dev = findDeviceByDevNo(entry.getKey());
				dev.setCombinationNo(devComb.getCombinationNo());
				genericDao.update(dev);
			} else {
				throw new BusinessException("没有匹配的设备组合模式");
			}
		}

	}
	
	/**
	 * 比较设备是否符合某个设备组合模式
	 * @param devCombList
	 * @param entry
	 */
	@SuppressWarnings("unchecked")
	private DevCombination getDevCombination(Map<Integer, List<DevCombination>> combNoMap, Map.Entry<String, List<Device>> entry) {
		List<DevCombination> devComSet = new ArrayList<DevCombination>();
		for (Device dev : entry.getValue()) {
			DevCombination devComb = new DevCombination();
			devComb.setCabinetNo(Integer.valueOf(dev.getCabinetNo()));
			devComb.setAisleCount(CommonUtil.getAisleCountByModel(dev.getModel()));// t_device与t_cabinet都有aisle_count属性，此处要以t_cabinet的货道数为准
			devComb.setModel(CommonUtil.getDeviceTypeByModel(dev.getModel()));
			devComSet.add(devComb);
		}

		Set<Entry<Integer, List<DevCombination>>> devNoSet = combNoMap.entrySet();
		for (Map.Entry<Integer, List<DevCombination>> devEntry : devNoSet) {
			List<DevCombination> devList = devEntry.getValue();
			if (devList.size() == devComSet.size()) {
				if (compare(devComSet, devList)) {
					return devList.get(0);
				}
			}
		}

		return null;
	}
	
	
	private String saveDevNoRelation(String facDevNo) {
		String random = String.valueOf(RandomUtil.buildRandom(8));
		
		DeviceRelation devRelation = new DeviceRelation();
		devRelation.setFactoryDevNo(facDevNo);
		devRelation.setDevNo(random);
		genericDao.save(devRelation);
		
		return random;
	}
	
	private List<DevCombination> saveDevCombination(List<Device> devs) {
		List<DevCombination> devCombList = new ArrayList<DevCombination>();

		Integer combNO = RandomUtil.buildRandom(4);// 四位随机数
		for (Device dev : devs) {
			DevCombination devComb = new DevCombination();
			devComb.setCabinetNo(Integer.valueOf(dev.getCabinetNo()));
			devComb.setAisleCount(dev.getAisleCount());
			devComb.setModel(CommonUtil.getDeviceTypeByModel(dev.getModel()));
			devComb.setCombinationNo(combNO);
			genericDao.save(devComb);

			devCombList.add(devComb);
		}

		return devCombList;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, List<Device>> getDevCombination(List<Device> devices) {
		Map<String, List<Device>> compDevs = new HashMap<String, List<Device>>();

		// 按设备类型升序排序
		Collections.sort(devices, new Comparator<Device>() {
			public int compare(Device arg0, Device arg1) {
				return arg0.getType().compareTo(arg1.getType());
			}
		});
		if (!devices.isEmpty()) {
			// 按设备编号进行分组
			Map<String, List<Device>> modelMap = group(devices, new GroupBy<String>() {
				@Override
				public String groupby(Object obj) {
					Device d = (Device) obj;
					return d.getDevNo();
				}
			});

			for (String devNo : modelMap.keySet()) {
				List<Device> devs = modelMap.get(devNo);
				if (compDevs.isEmpty()) {
					compDevs.put(devNo, devs);
					continue;
				}

				Set<Entry<String, List<Device>>> set = compDevs.entrySet();
				Set<Entry<String, List<Device>>> tmpSet = new HashSet<>(set);
				boolean exists = false;
				for (Map.Entry<String, List<Device>> entry : tmpSet) {
					// 货柜号和货柜类型都必须一致
					if (!compare(devs, compDevs.get(entry.getKey()))) {
						continue;
					} else {
						exists = true;
					}
				}

				if (!exists) {
					// 遍历compDevs，直到没有一个与当前设备相同的，就添加进去
					compDevs.put(devNo, modelMap.get(devNo));
				}
			}
		}

		return compDevs;
	}
	
	
	
	/**
	 * 创建激活码信息
	 * @param code
	 * @param user
	 * @return
	 */
	public ActiveCode createActiveCode(String code, User user) {
		ActiveCode activeCode = new ActiveCode();
		activeCode.setActiveCode(code);
		activeCode.setState(Commons.ACTIVE_CODE_STATE_INIT);//未使用
		activeCode.setCreateUser(user.getId());
		activeCode.setCreateTime(new Timestamp(System.currentTimeMillis()));
		return activeCode;
	}
	
	/**
	 * 构建初始化设备信息
	 * @param dev
	 * @param user
	 * @return
	 */
	public Device createDevice(Device dev, User user) throws Exception {
		Device finalDevice = new Device();
		finalDevice.setDevNo(saveDevNoRelation(dev.getDevNo()));
		finalDevice.setAddress("");
		finalDevice.setManufacturer(dev.getManufacturer());
		finalDevice.setAisleCount(CommonUtil.getAisleCountByModel(dev.getModel()));
		finalDevice.setFactoryNo(dev.getFactoryNo());
		finalDevice.setState(Commons.NORMAL);
		finalDevice.setOrgId(user.getOrgId());
		finalDevice.setCreateUser(user.getId());
		finalDevice.setCreateTime(new Timestamp(System.currentTimeMillis()));
		finalDevice.setType(CommonUtil.getDeviceTypeByModel(dev.getModel()));
		finalDevice.initDefaultValue();
		return finalDevice;
	}
	
	/**
	 * 构建初始化货柜信息
	 * @param dev
	 * @param user
	 * @return
	 */
	public Cabinet createCabinet(Device dev, User user) throws Exception {
		Cabinet finalCabinet = new Cabinet();
		finalCabinet.setCabinetNo(Integer.valueOf(dev.getCabinetNo()) + "");//校验货柜号是否是数字
		finalCabinet.setAisleCount(CommonUtil.getAisleCountByModel(dev.getModel()));
		
		if (Commons.VISIABLE_FALSE != dev.getVisiable() && Commons.VISIABLE_TRUE != dev.getVisiable())
    		throw new BusinessException("导入失败，设备编号【"+ dev.getDevNo() +"】的货道可见状态有误（0：不可见；1：可见），请重新设置！");
		finalCabinet.setVisiable(dev.getVisiable());
		
		finalCabinet.setManufacturer(dev.getManufacturer());
		finalCabinet.setFactoryNo(dev.getFactoryNo());
		finalCabinet.setModel(dev.getModel());		
		finalCabinet.setFactoryTime(new Timestamp(DateUtil.getDate(dev.getFactoryTimeStr()).getTime()));
		finalCabinet.setCreateUser(user.getId());
		finalCabinet.setCreateTime(new Timestamp(System.currentTimeMillis()));
		finalCabinet.setType(CommonUtil.getDeviceTypeByModel(dev.getModel()));
		return finalCabinet;
	}
	
	/**
	 * 构建饮料机的货道信息（货道号：1~21）
	 * @return
	 */
	public List<DeviceAisle> createDrinkDeviceAisles(Device finalDevice, Cabinet finalCabinet, User user) throws Exception {
		List<DeviceAisle> finalDeviceAisles = new ArrayList<DeviceAisle>();
		int stockRemind = dictionaryService.findMinStock();
		for (int i = 1; i <= 21; i++) {
			DeviceAisle aisle = new DeviceAisle();
			aisle.setAisleNum(i);// 货道号
			aisle.setStock(0);// 库存
			aisle.setStockRemind(stockRemind);//库存提醒
			aisle.setDeviceId(finalDevice.getId());
			aisle.setCapacity(0);//货道容量
			aisle.setSupplementNo(0);//应补货数量
			aisle.setCreateUser(user.getId());
			aisle.setCreateTime(new Timestamp(System.currentTimeMillis()));
			aisle.setCabinetId(finalCabinet.getId());
			aisle.setSellable(Commons.SELLABLE_TRUE);
			finalDeviceAisles.add(aisle);
		}
		return finalDeviceAisles;
	}

	/**
	 * 构建小型饮料机的货道信息（货道号：3~12）
	 * @return
	 */
	public List<DeviceAisle> createDrinkSmallDeviceAisles(Device finalDevice, Cabinet finalCabinet, User user) throws Exception {
		List<DeviceAisle> finalDeviceAisles = new ArrayList<DeviceAisle>();
		int stockRemind = dictionaryService.findMinStock();
		for (int i = 3; i <= 12; i++) {
			DeviceAisle aisle = new DeviceAisle();
			aisle.setAisleNum(i);// 货道号
			aisle.setStock(0);// 库存
			aisle.setStockRemind(stockRemind);//库存提醒
			aisle.setDeviceId(finalDevice.getId());
			aisle.setCapacity(0);//货道容量
			aisle.setSupplementNo(0);//应补货数量
			aisle.setCreateUser(user.getId());
			aisle.setCreateTime(new Timestamp(System.currentTimeMillis()));
			aisle.setCabinetId(finalCabinet.getId());
			aisle.setSellable(Commons.SELLABLE_TRUE);
			finalDeviceAisles.add(aisle);
		}
		return finalDeviceAisles;
	}
	
	/**
	 * 构建智能商品机的货道信息（货道号：10~59）
	 * @return
	 */
	public List<DeviceAisle> createIntelligentProductDeviceAisles(Device finalDevice, Cabinet finalCabinet, User user) throws Exception {
		List<DeviceAisle> finalDeviceAisles = new ArrayList<DeviceAisle>();
		int stockRemind = dictionaryService.findMinStock();
		for (int i = 10; i <= 59; i++) {
			DeviceAisle aisle = new DeviceAisle();
			aisle.setAisleNum(i);// 货道号
			aisle.setStock(0);// 库存
			aisle.setStockRemind(stockRemind);//库存提醒
			aisle.setDeviceId(finalDevice.getId());
			aisle.setCapacity(0);//货道容量
			aisle.setSupplementNo(0);//应补货数量
			aisle.setCreateUser(user.getId());
			aisle.setCreateTime(new Timestamp(System.currentTimeMillis()));
			aisle.setCabinetId(finalCabinet.getId());
			aisle.setSellable(Commons.SELLABLE_TRUE);
			finalDeviceAisles.add(aisle);
		}
		return finalDeviceAisles;
	}

	/**
	 * 构建弹簧机的货道信息，最多托两个
	 * （货道号：11~18,21~28,31~38,41~48,51~58,61~68）
	 * （货道号：811~818,821~828,831~38,841~848,851~858,861~868）
	 * @return
	 */
	public List<DeviceAisle> createSpringDeviceAisles(Device finalDevice, Cabinet finalCabinet, User user, int index) throws Exception {
		List<DeviceAisle> finalDeviceAisles = new ArrayList<DeviceAisle>();
		int stockRemind = dictionaryService.findMinStock();
		if (index == 1) {
			for (int i = 11; i <= 68; i++) {
				if (i == 19 || i == 20 || i == 29 || i == 30 || i == 39 || i == 40 || i == 49 || i == 50 || i == 59 || i == 60)
					continue;
				finalDeviceAisles.add(createDeviceAisle(finalDevice, finalCabinet, user, i, stockRemind));
			}
		} else if (index == 2) {
			for (int i = 811; i <= 868; i++) {
				if (i == 819 || i == 820 || i == 829 || i == 830 || i == 839 || i == 840 || i == 849 || i == 850 || i == 859 || i == 860)
					continue;
				finalDeviceAisles.add(createDeviceAisle(finalDevice, finalCabinet, user, i, stockRemind));
			}
		}
		return finalDeviceAisles;
	}

	/**
	 * （主托挂是智能商品机的情况）构建弹簧机的货道信息，最多托一个
	 * （货道号：711~718,721~728,731~738,741~748,751~758,761~768）
	 * @return
	 */
	public List<DeviceAisle> createSpringIntelligentProductDeviceAisles(Device finalDevice, Cabinet finalCabinet, User user, int index) throws Exception {
		List<DeviceAisle> finalDeviceAisles = new ArrayList<DeviceAisle>();
		int stockRemind = dictionaryService.findMinStock();
		if (index == 1) {
			for (int i = 711; i <= 768; i++) {
				if (i == 719 || i == 720 || i == 729 || i == 730 || i == 739 || i == 740 || i == 749 || i == 750 || i == 759 || i == 760)
					continue;
				finalDeviceAisles.add(createDeviceAisle(finalDevice, finalCabinet, user, i, stockRemind));
			}
		}
		return finalDeviceAisles;
	}
	
	/**
	 * 构建只有64门格子柜的货道信息，最对托两个（64门和40门混合的情况）
	 * （货道号：101~140,201~224）
	 * （货道号：301~340,401~424）
	 * @return
	 */
	public List<DeviceAisle> createOnlyGrid64DeviceAisles(Device finalDevice, Cabinet finalCabinet, User user, int index) throws Exception {
		List<DeviceAisle> finalDeviceAisles = new ArrayList<DeviceAisle>();
		int stockRemind = dictionaryService.findMinStock();
		if (index == 1) {
			for (int i = 101; i <= 224; i++) {
				if (i > 140 && i < 201)
					continue;
				finalDeviceAisles.add(createDeviceAisle(finalDevice, finalCabinet, user, i, stockRemind));
			}
		} else if (index == 2) {
			for (int i = 301; i <= 424; i++) {
				if (i > 340 && i < 401)
					continue;
				finalDeviceAisles.add(createDeviceAisle(finalDevice, finalCabinet, user, i, stockRemind));
			}
		}
		return finalDeviceAisles;
	}

	/**
	 * 构建64门格子柜的货道信息，最对托四个
	 * （货道号：101~140,201~224）
	 * （货道号：301~340,401~424）
	 * （货道号：501~540,601~624）
	 * （货道号：701~740,801~824）
	 * @return
	 */
	public List<DeviceAisle> createGrid64DeviceAisles(Device finalDevice, Cabinet finalCabinet, User user, int index) throws Exception {
		List<DeviceAisle> finalDeviceAisles = new ArrayList<DeviceAisle>();
		int stockRemind = dictionaryService.findMinStock();
		if (index == 1) {
			for (int i = 101; i <= 224; i++) {
				if (i > 140 && i < 201)
					continue;
				finalDeviceAisles.add(createDeviceAisle(finalDevice, finalCabinet, user, i, stockRemind));
			}
		} else if (index == 2) {
			for (int i = 301; i <= 424; i++) {
				if (i > 340 && i < 401)
					continue;
				finalDeviceAisles.add(createDeviceAisle(finalDevice, finalCabinet, user, i, stockRemind));
			}
		} else if (index == 3) {
			for (int i = 501; i <= 624; i++) {
				if (i > 540 && i < 601)
					continue;
				finalDeviceAisles.add(createDeviceAisle(finalDevice, finalCabinet, user, i, stockRemind));
			}
		} else if (index == 4) {
			for (int i = 701; i <= 824; i++) {
				if (i > 740 && i < 801)
					continue;
				finalDeviceAisles.add(createDeviceAisle(finalDevice, finalCabinet, user, i, stockRemind));
			}
		}
		return finalDeviceAisles;
	}
	
	/**
	 * 构建60门格子柜的货道信息，最对托四个
	 * （货道号：101~140,201~224）
	 * （货道号：301~340,401~424）
	 * （货道号：501~540,601~624）
	 * （货道号：701~740,801~824）
	 * @return
	 */
	public List<DeviceAisle> createGrid60DeviceAisles(Device finalDevice, Cabinet finalCabinet, User user, int index) throws Exception {
		List<DeviceAisle> finalDeviceAisles = new ArrayList<DeviceAisle>();
		int stockRemind = dictionaryService.findMinStock();
		if (index == 1) {
			for (int i = 101; i <= 220; i++) {
				if (i > 140 && i < 201)
					continue;
				finalDeviceAisles.add(createDeviceAisle(finalDevice, finalCabinet, user, i, stockRemind));
			}
		} else if (index == 2) {
			for (int i = 301; i <= 420; i++) {
				if (i > 340 && i < 401)
					continue;
				finalDeviceAisles.add(createDeviceAisle(finalDevice, finalCabinet, user, i, stockRemind));
			}
		} else if (index == 3) {
			for (int i = 501; i <= 620; i++) {
				if (i > 540 && i < 601)
					continue;
				finalDeviceAisles.add(createDeviceAisle(finalDevice, finalCabinet, user, i, stockRemind));
			}
		} else if (index == 4) {
			for (int i = 701; i <= 820; i++) {
				if (i > 740 && i < 801)
					continue;
				finalDeviceAisles.add(createDeviceAisle(finalDevice, finalCabinet, user, i, stockRemind));
			}
		}
		return finalDeviceAisles;
	}
	
	/**
	 * 构建40门格子柜的货道信息
	 * （货道号：101~140）
	 * （货道号：201~240）
	 * （货道号：301~340）
	 * （货道号：401~440）
	 * （货道号：501~540）
	 * @return
	 */
	public List<DeviceAisle> createGrid40DeviceAisles(Device finalDevice, Cabinet finalCabinet, User user, int index) throws Exception {
		List<DeviceAisle> finalDeviceAisles = new ArrayList<DeviceAisle>();
		int stockRemind = dictionaryService.findMinStock();
		
		if (index >= 1 && index <= 5) {
			int start = Integer.valueOf(index + "01");
			int end = Integer.valueOf(index + "40");
			for (int i = start; i <= end; i++) {
				finalDeviceAisles.add(createDeviceAisle(finalDevice, finalCabinet, user, i, stockRemind));
			}
		}
		return finalDeviceAisles;
	}
	
	/**
	 * 创建货道信息
	 * @param finalDevice
	 * @param finalCabinet
	 * @param user
	 * @param aisleNum
	 * @param stockRemind
	 * @return
	 */
	public DeviceAisle createDeviceAisle(Device finalDevice, Cabinet finalCabinet, User user, int aisleNum, int stockRemind) {
		DeviceAisle aisle = new DeviceAisle();
		aisle.setAisleNum(aisleNum);// 货道号
		aisle.setStock(0);// 库存
		aisle.setStockRemind(stockRemind);//库存提醒
		aisle.setDeviceId(finalDevice.getId());
		aisle.setCapacity(0);//货道容量
		aisle.setSupplementNo(0);//应补货数量
		aisle.setCreateUser(user.getId());
		aisle.setCreateTime(new Timestamp(System.currentTimeMillis()));
		aisle.setCabinetId(finalCabinet.getId());
		aisle.setSellable(Commons.SELLABLE_TRUE);
		return aisle;
	}
	
	/**
	 * 读取excel
	 * @param srcReadFilePath
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> findByExcel(String srcReadFilePath) throws Exception {
		URL url = new URL(srcReadFilePath); 
		URLConnection conn = url.openConnection();  
		
		Map<String, Object> beanParams = new HashMap<String, Object>();
		List<Device> devices = new ArrayList<Device>();
		beanParams.put("devices", devices);
		List<ActiveCode> activeCodes = new ArrayList<ActiveCode>();
		beanParams.put("activeCodes", activeCodes);
		
		InputStream inputXML = new BufferedInputStream(this.getClass().getResourceAsStream("/template/excel/device.xml"));
		XLSReader mainReader = ReaderBuilder.buildFromXML(inputXML);
		InputStream inputXLS = new BufferedInputStream(conn.getInputStream());
		mainReader.read(inputXLS, beanParams);
		
		return beanParams;
	}
	
	/**
	 * 根据org_id查询设备
	 * @param orgId
	 * @return
	 */
	public List<Device> findBindingDevices(Long orgId) {
		StringBuffer buf = new StringBuffer("SELECT ");
		String cols = SQLUtils.getColumnsSQL(Device.class, "C");
		buf.append(cols);
		buf.append(" FROM T_DEVICE C WHERE C.ORG_ID=? ");
		List<Object> args = new ArrayList<Object>();
		args.add(orgId);
		return genericDao.findTs(Device.class, buf.toString(), args.toArray());
	}

	@Override
	public List<DeviceAisle> findDeviceAisles(DeviceAisle deviceAisles) {
		StringBuffer buf = new StringBuffer();
		buf.append(SQLUtils.getSelectSQL(DeviceAisle.class));
		if (deviceAisles == null) {
			deviceAisles = new DeviceAisle();
		}
		BoxValue<String, List<Object>> box = SQLUtils.getCondition(deviceAisles);
		buf.append(box.getKey());
		return genericDao.findTs(DeviceAisle.class, buf.toString(), box.getValue().toArray());
	}

	@Override
	public List<DeviceAisle> findSellerDevice(Page page, DeviceAisle deviceAisle) {
		if (page != null) {
			page.setOrder("aisleNum");
		}
		StringBuffer buf = new StringBuffer("SELECT ");
		String cols = SQLUtils.getColumnsSQL(DeviceAisle.class, "C");
		buf.append(cols);
		buf.append(" FROM T_DEVICE_AISLE C WHERE 1=1 ");
		if (deviceAisle == null) {
			deviceAisle = new DeviceAisle();
		}
		if (deviceAisle.getDeviceId() != null) {
			buf.append(" AND C.DEVICE_ID = ? ");
		}
		BoxValue<String, List<Object>> box = SQLUtils.getCondition(deviceAisle, null, "C");
		List<Object> args = box.getValue();
		return genericDao.findTs(DeviceAisle.class, page, buf.toString(), args.toArray());
	}

	/**
	 * 分页查询导入的设备
	 */
	@Override
	public List<Device> findDevices(Page page, Device device) {
		if (page != null) {
			page.setOrder(" FACTORYDEVNO , CABINETNO ");
		}
		StringBuffer buf = new StringBuffer("SELECT ");
		List<Object> args = new ArrayList<Object>();
		String cols = SQLUtils.getColumnsSQL(Device.class, "D", "TYPE,AISLECOUNT,FACTORYNO");
		buf.append(cols);
		buf.append(" ,SO.NAME AS ORGNAME ");
		buf.append(" ,C.AISLE_COUNT AS AISLECOUNT ,C.MANUFACTURER AS MANUFACTURER ");
		buf.append(" ,C.FACTORY_NO AS FACTORYNO , C.CABINET_NO AS CABINETNO ");
		buf.append(" ,C.MODEL AS MODEL ,C.TYPE AS TYPE ");
		buf.append(" ,R.FACTORY_DEV_NO AS FACTORYDEVNO ");
		buf.append(" FROM T_CABINET C LEFT JOIN T_DEVICE D ON D.ID = C.DEVICE_ID ");
		buf.append(" LEFT JOIN SYS_ORG SO ON D.ORG_ID=SO.ID ");
		buf.append(" LEFT JOIN T_DEVICE_RELATION R ON R.DEV_NO = D.DEV_NO ");
		buf.append(" WHERE 1 = 1  ");

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
				
				buf.append(" AND D.ORG_ID ").append(orgIdsSQL);
			}
		}
		
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("number", SQLUtils.IGNORE);

		if (device == null) {
			device = new Device();
		}
		if (device.getDevNo() != null) {
			buf.append(" AND").append(" (D.DEV_NO LIKE ? OR D.DEV_NO LIKE ?)");
			args.add("%" + ZHConverter.convert(device.getDevNo(), ZHConverter.TRADITIONAL) + "%");
			args.add("%" + ZHConverter.convert(device.getDevNo(), ZHConverter.SIMPLIFIED) + "%");
		}
		if (device.getFactoryDevNo() != null) {
			buf.append(" AND").append(" (R.FACTORY_DEV_NO LIKE ? OR R.FACTORY_DEV_NO LIKE ?)");
			args.add("%" + ZHConverter.convert(device.getFactoryDevNo(), ZHConverter.TRADITIONAL) + "%");
			args.add("%" + ZHConverter.convert(device.getFactoryDevNo(), ZHConverter.SIMPLIFIED) + "%");
		}
		if (device.getOrgName() != null) {
			buf.append(" AND").append(" (SO.NAME LIKE ? OR SO.NAME LIKE ?)");
			args.add("%" + ZHConverter.convert(device.getOrgName(), ZHConverter.TRADITIONAL) + "%");
			args.add("%" + ZHConverter.convert(device.getOrgName(), ZHConverter.SIMPLIFIED) + "%");
		}
		if (device.getBindState() != null) {
			buf.append(" AND D.BIND_STATE = ? ");
			args.add(device.getBindState());
		}
		if (device.getType() != null) {
			buf.append(" AND (D.TYPE = ? OR C.TYPE = ?) ");
			args.add(device.getType());
			args.add(device.getType());
		}
		
		return genericDao.findTs(Device.class, page, buf.toString(), args.toArray());
	}
	
	public PointPlace findPointPlace(Long pointPlaceId) {
		StringBuffer buf = new StringBuffer("SELECT ");
		String cols = SQLUtils.getColumnsSQL(PointPlace.class, "C");
		buf.append(cols);
		buf.append(" FROM T_POINT_PLACE C WHERE C.ID=? ");
		List<Object> args = new ArrayList<Object>();
		args.add(pointPlaceId);
		
		buf.append(" AND C.STATE != ? ");
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
		return genericDao.findT(PointPlace.class, buf.toString(), args.toArray());
	}
	
	/**
	 * 查询点位信息
	 * 
	 * 1、店铺信息显示该机构及关联的下属所有机构的店铺；
	 * @param page
	 * @param pointPlace
	 * @return
	 */
	@Override
	public List<PointPlace> findPointPlaces(Page page, PointPlace pointPlace) {
		StringBuffer buf = new StringBuffer("SELECT ");
		String cols = SQLUtils.getColumnsSQL(PointPlace.class, "C");
		buf.append(cols);
		buf.append(" ,SO.NAME AS orgName ");
		buf.append(" FROM T_POINT_PLACE C LEFT JOIN SYS_ORG SO ON C.ORG_ID=SO.ID WHERE 1=1 ");
		List<Object> args = new ArrayList<Object>();;
		User user = ContextUtil.getUser(User.class);
		if (pointPlace == null) {
			pointPlace = new PointPlace();
		}
		
		if (null != pointPlace.getOrgId()) {
			buf.append(" AND C.ORG_ID = ? ");
			args.add(pointPlace.getOrgId());
		} else {
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
		
		buf.append(" AND SO.NAME IS NOT NULL ");
		
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
		if (pointPlace.getPointAddress() != null) {// 店铺地址
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
		buf.append(" ORDER BY SO.NAME, C.CREATE_TIME DESC ");
		
		List<PointPlace> pointPlaces = genericDao.findTs(PointPlace.class, page, buf.toString(), args.toArray());
		
		for (PointPlace point : pointPlaces) {
			List<PointReplenishTime> pointReplenishTimes = findPointReplenishTimes(point.getPointNo());
			point.setPointReplenishTimes(pointReplenishTimes);
		}
		
		return pointPlaces;
	}
	
	/**
	 * 根据店铺编号查询店铺建议补货时间
	 * @param orgId
	 * @return
	 */
	public List<PointReplenishTime> findPointReplenishTimes(String pointNo) {
		StringBuffer buf = new StringBuffer(" SELECT ");
		String cols = SQLUtils.getColumnsSQL(PointReplenishTime.class, "C");
		buf.append(cols);
		buf.append(" FROM T_POINT_REPLENISH_TIME C WHERE C.POINT_NO = ? ");
		List<Object> args = new ArrayList<Object>();
		args.add(pointNo);
		return genericDao.findTs(PointReplenishTime.class, buf.toString(), args.toArray());
	}

	@Override
	public void saveDevice(Device device) {
		User curUser = ContextUtil.getUser(User.class);
		Timestamp curTime = new Timestamp(System.currentTimeMillis());
		if(StringUtils.isEmpty(device.getDevNo()))
			throw new BusinessException("设备编码不能为空!");
		device.initDefaultValue();
		if (device.getId() == null) {
			Long id = genericDao.findSingle(Long.class, "SELECT ID FROM T_DEVICE WHERE DEV_NO=?", device.getDevNo());
			if (id != null)
				throw new BusinessException("设备编码已经存在！");
			device.setCreateUser(curUser.getId());
			device.setCreateTime(curTime);
			genericDao.save(device);
			// 将t_we_user表中的该设备（如果存在）所属orgId更改为当前orgId
			genericDao.execute("UPDATE T_WE_USER SET ORG_ID=? WHERE DEV_NO=?", curUser.getOrgId(), device.getDevNo());
		} else {
			Long id = genericDao.findSingle(Long.class, "SELECT ID FROM T_DEVICE WHERE DEV_NO=? AND ID!=?", device.getDevNo(), device.getId());
			if (id != null)
				throw new BusinessException("设备编码已经存在！");
			device.setUpdateUser(curUser.getId());
			device.setUpdateTime(curTime);
			genericDao.update(device);
		}
	}

	/**
	 * 保存点位信息
	 */
	@Override
	public void savePoint(PointPlace pointPlace) {
		User curUser = ContextUtil.getUser(User.class);
		Timestamp curTime = new Timestamp(System.currentTimeMillis());
		
		// 店铺基础信息校验
		dataValid(pointPlace);
		
		pointPlace.setState(Commons.POINT_PLACE_STATE_DEFAULT);
		
		if (pointPlace.getId() == null) {// 新增
			pointPlace.setCreateUser(curUser.getId());
			pointPlace.setCreateTime(curTime);
			genericDao.save(pointPlace);
		} else {// 修改
			PointPlace pointPlaceDB = findPointPlace(pointPlace.getId());
			
			// 上级可以跨级帮助新建店铺，新建的店铺，上级和对应下级可以进行增删改查；不是上级新建店铺，无法操作；
			User user = findUserById(pointPlaceDB.getCreateUser());
			if (pointPlace.getOrgId() != curUser.getOrgId() && user.getOrgId() != curUser.getOrgId())// 既不是自己的店铺，也不是自己帮下级创建的店铺
				throw new BusinessException("该店铺不是您所属组织创建的，无法操作修改");
			
			// 更换所属人时，须先解绑设备。当店铺产生经营数据后，不允许更换所属人；
			if (pointPlaceDB.getOrgId() != pointPlace.getOrgId()) {
				StringBuffer buf = new StringBuffer("");
				buf.append("SELECT COUNT(ID) FROM T_ORDER WHERE STATE = ? AND ORG_ID = ? AND POINT_NO = ? ");
				int count = genericDao.findSingle(int.class, buf.toString(), Commons.ORDER_STATE_FINISH, pointPlaceDB.getOrgId(), pointPlaceDB.getPointNo());
				if (count != 0)
					throw new BusinessException("该店铺已经产生经营数据，不允许更换所属人。");
				
				List<DevCombination> devCombinations = findBindDevCombination(null, pointPlace.getId());
				if (null != devCombinations && !devCombinations.isEmpty())
					throw new BusinessException("更换所属人时，须先解绑该店铺下的设备。");
			}
			
			pointPlace.setPointNo(pointPlaceDB.getPointNo());
			pointPlace.setCreateUser(pointPlaceDB.getCreateUser());
			pointPlace.setCreateTime(pointPlaceDB.getCreateTime());
			pointPlace.setUpdateUser(curUser.getId());
			pointPlace.setUpdateTime(curTime);
			pointPlace.setUserId(pointPlaceDB.getUserId());
			genericDao.update(pointPlace);
		}
		
		// 店铺补货时间配置(先删后加)
		genericDao.execute(" DELETE FROM T_POINT_REPLENISH_TIME WHERE POINT_NO = ? ", pointPlace.getPointNo());
		for (PointReplenishTime pointTime : pointPlace.getPointReplenishTimes()) {
			pointTime.setPointNo(pointPlace.getPointNo());
			genericDao.save(pointTime);
		}
	}
	
	/**
	 * 店铺信息基础校验
	 * @param pointPlace
	 */
	public void dataValid(PointPlace pointPlace) {
		if (null == pointPlace.getPointReplenishTimes() || pointPlace.getPointReplenishTimes().isEmpty())
			throw new BusinessException("请设置建议补货时间！");
		
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer("SELECT ");
		String cols = SQLUtils.getColumnsSQL(PointPlace.class, "C");
		buf.append(cols);
		buf.append(" FROM T_POINT_PLACE C WHERE 1=1 ");
		
		if (null != pointPlace.getId()) {
			buf.append(" AND ID!=? ");
			args.add(pointPlace.getId());
		} else
			pointPlace.setPointNo(String.valueOf(RandomUtil.buildRandom(8)));//点位编号
		
		buf.append(" AND (POINT_NO=? ");
		args.add(pointPlace.getPointNo());
		buf.append(" OR POINT_NAME=?) ");
		args.add(pointPlace.getPointName());
		
		buf.append(" AND C.STATE != ? ");
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除

		PointPlace dbPointPlace = genericDao.findT(PointPlace.class, buf.toString(), args.toArray());
		
		if (null != dbPointPlace) {
			if (dbPointPlace.getPointNo().equals(pointPlace.getPointNo()))
				throw new BusinessException("店铺编号已经存在！");
			if (dbPointPlace.getPointName().equals(pointPlace.getPointName()))
				throw new BusinessException("店铺名称已被使用，请重新输入！");
		}
	}
	
	/**
	 * 根据org_id查询设备
	 * @param orgId
	 * @return
	 */
	public List<Device> findDBBindingPointDevices(PointPlace pointPlace) {
		StringBuffer buf = new StringBuffer("SELECT ");
		String cols = SQLUtils.getColumnsSQL(Device.class, "C");
		buf.append(cols);
		buf.append(" FROM T_DEVICE C WHERE C.POINT_ID = ? AND C.ORG_ID=? ");
		List<Object> args = new ArrayList<Object>();
		args.add(pointPlace.getId());
		args.add(pointPlace.getOrgId());
		return genericDao.findTs(Device.class, buf.toString(), args.toArray());
	}
	
	@Override
	public void deleteDevices(Long[] ids) {
		if (ids != null && ids.length != 0) {
			List<Object> args = new ArrayList<Object>();
			StringBuffer buf = new StringBuffer(" IN(");
			for (Long id : ids) {
				buf.append("?,");
				args.add(id);
			}
			buf.setLength(buf.length() - 1);
			buf.append(")");
			String inIds = buf.toString();
			
			int count = 0;
			count = genericDao.findSingle(int.class, " SELECT COUNT(*) FROM T_PP_DEV_COMBINATION WHERE DEVICE_ID " + inIds, args.toArray());
			if (count != 0)
				throw new BusinessException("所选设备已绑定到店铺，请先从店铺中解绑后再操作！");
			
			// 检查设备组合其他设备是否用到，若没有则删除
			List<Object> args2 = new ArrayList<Object>();
			args2.addAll(args);
			args2.addAll(args);
			count = genericDao.findSingle(int.class, " SELECT COUNT(*) FROM T_DEVICE WHERE ID NOT " + inIds + " AND COMBINATION_NO IN (SELECT COMBINATION_NO FROM T_DEVICE WHERE ID " + inIds + ")", args2.toArray());
			if (count == 0)
				genericDao.execute("DELETE FROM T_DEV_COMBINATION WHERE COMBINATION_NO IN (SELECT COMBINATION_NO FROM T_DEVICE WHERE ID " + inIds + ")", args.toArray());
			
			// 删除货道信息
			buf.setLength(0);
			buf.append("DELETE FROM T_DEVICE_AISLE WHERE DEVICE_ID").append(inIds);
			genericDao.execute(buf.toString(), args.toArray());
			// 删除货柜信息
			buf.setLength(0);
			buf.append("DELETE FROM T_CABINET WHERE DEVICE_ID").append(inIds);
			genericDao.execute(buf.toString(), args.toArray());
			// 删除设备日志信息
			buf.setLength(0);
			buf.append("DELETE FROM T_DEVICE_LOG WHERE DEVICE_NO IN (SELECT DEV_NO FROM T_DEVICE WHERE ID ").append(inIds).append(")");
			genericDao.execute(buf.toString(), args.toArray());
			// 删除设备推送信息
			buf.setLength(0);
			buf.append("DELETE FROM T_DEVICE_PUSH WHERE FACTORY_DEV_NO IN (SELECT FACTORY_DEV_NO FROM T_DEVICE_RELATION WHERE DEV_NO IN (SELECT DEV_NO FROM T_DEVICE WHERE ID ").append(inIds).append("))");
			genericDao.execute(buf.toString(), args.toArray());
			// 删除设备关系信息
			buf.setLength(0);
			buf.append("DELETE FROM T_DEVICE_RELATION WHERE DEV_NO IN (SELECT DEV_NO FROM T_DEVICE WHERE ID ").append(inIds).append(")");
			genericDao.execute(buf.toString(), args.toArray());
			// 删除设备信息
			buf.setLength(0);
			buf.append("DELETE FROM T_DEVICE WHERE ID").append(inIds);
			genericDao.execute(buf.toString(), args.toArray());
		}
	}

	/**
	 * 删除点位信息
	 */
	@Override
	public void deletePoints(Long[] ids) {
		User curUser = ContextUtil.getUser(User.class);
		if (ids != null && ids.length != 0) {
			for (Long id : ids) {
				List<DevCombination> devCombinations = findBindDevCombination(null, id);
				if (null != devCombinations && !devCombinations.isEmpty())
					throw new BusinessException("请先删除该店铺下的设备，再删除该店铺。");
				
				PointPlace pointPlaceDB = findPointPlace(id);
				
				// 不是上级新建店铺，无法删除；
				User user = findUserById(pointPlaceDB.getCreateUser());
				if (pointPlaceDB.getOrgId() != curUser.getOrgId() && user.getOrgId() != curUser.getOrgId())// 既不是自己的店铺，也不是自己帮下级创建的店铺
					throw new BusinessException("该店铺不是您所属组织创建的，无法删除");
			}
			
			List<Object> args = new ArrayList<Object>();
			
			args.add(Commons.POINT_PLACE_STATE_DELETE);// 软删除
			
			StringBuffer buf = new StringBuffer(" IN(");
			for (Long id : ids) {
				buf.append("?,");
				args.add(id);
			}
			buf.setLength(buf.length() - 1);
			buf.append(")");
			String inIds = buf.toString();
			buf.setLength(0);
			
			buf.append("UPDATE T_POINT_PLACE SET STATE = ? WHERE ID").append(inIds);
			genericDao.execute(buf.toString(), args.toArray());
		}
	}

	
	/* (non-Javadoc)
	 * @see com.vendor.service.IPlatformService#findDevices()
	 */
	@Override
	public List<Device> findDevices() {
		return genericDao.findTs(Device.class,"SELECT ID,DEV_NO FROM T_DEVICE WHERE STATE=?",0);
	}
	
	public List<DeviceAisle> findDeviceAislesByDevNo(String devNo) {
		StringBuffer buffer = new StringBuffer("SELECT ");
		buffer.append(SQLUtils.getColumnsSQL(DeviceAisle.class, "A"));
		buffer.append(" FROM T_DEVICE_AISLE A  ");
		buffer.append(" LEFT JOIN T_DEVICE D ON D.ID = A.DEVICE_ID ");
		buffer.append(" WHERE D.DEV_NO = ? AND A.PRODUCT_ID IS NOT NULL ");
		return genericDao.findTs(DeviceAisle.class, buffer.toString(), devNo);
	}
	
	public Device findDeviceById(Long deviceId) {
		StringBuffer buffer = new StringBuffer("SELECT ");
		buffer.append(SQLUtils.getColumnsSQL(Device.class, "A"));
		buffer.append(" FROM T_DEVICE A WHERE A.ID=? ");
		return genericDao.findT(Device.class, buffer.toString(), deviceId);
	}
	
	public FileStore findFile(Long infoId, Integer type) {
		StringBuffer buffer = new StringBuffer("SELECT ");
		buffer.append(SQLUtils.getColumnsSQL(FileStore.class, "A"));
		buffer.append(" FROM T_FILE A WHERE A.INFO_ID=? AND A.TYPE = ? ");
		return genericDao.findT(FileStore.class, buffer.toString(), infoId, type);
	}

	public void deleteFile(Long infoId, Integer type) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(" DELETE FROM T_FILE WHERE INFO_ID=? AND TYPE = ? ");
		genericDao.execute(buffer.toString(), infoId, type);
	}
	
	public Device findDeviceByDevNo(String deviceNumber) {
		StringBuffer buffer = new StringBuffer("SELECT ");
		buffer.append(SQLUtils.getColumnsSQL(Device.class, "A"));
		buffer.append(" FROM T_DEVICE A WHERE A.DEV_NO=? ");
		return genericDao.findT(Device.class, buffer.toString(), deviceNumber);
	}
	
	public User findUserById(Long userId) {
		StringBuffer buffer = new StringBuffer("SELECT ");
		buffer.append(SQLUtils.getColumnsSQL(User.class, "A"));
		buffer.append(" FROM SYS_USER A WHERE A.ID=? ");
		return genericDao.findT(User.class, buffer.toString(), userId);
	}

	/**
     * 分組依據接口，用于集合分組時，獲取分組依據
     * @author	ZhangLiKun
     * @title	GroupBy
     * @date	2013-4-23
     */
    public interface GroupBy<T> {
        T groupby(Object obj) ;
    }
    
    /**
     * 
     * @param colls
     * @param gb
     * @return
     */
    public static final <T extends Comparable<T> ,D> Map<T ,List<D>> group(Collection<D> colls ,GroupBy<T> gb){
        if(colls == null) {
            System.out.println("分組集合不能為空!");
            return null ;
        }
        if(gb == null) {
            System.out.println("分組依據接口不能為Null!");
            return null ;
        }
        Iterator<D> iter = colls.iterator() ;
        Map<T ,List<D>> map = new HashMap<T, List<D>>() ;
        while(iter.hasNext()) {
            D d = iter.next() ;
            T t = gb.groupby(d) ;
            if(map.containsKey(t)) {
                map.get(t).add(d) ;
            } else {
                List<D> list = new ArrayList<D>() ;
                list.add(d) ;
                map.put(t, list) ;
            }
        }
        return map ;
    }
    
    /**
     * 队列比较
     * @param <T>
     * @param a
     * @param b
     * @return
     */
    public static <T extends Comparable<T>> boolean compare(List<T> a, List<T> b) {
        if(a.size() != b.size())
            return false;
        Collections.sort(a);
        Collections.sort(b);
        for(int i=0;i<a.size();i++){
            if(!a.get(i).equals(b.get(i)))
                return false;
        }
        return true;
    }

    /**
	 * 根据设备模式，选定一个具体的设备
	 * @param devComb
	 * @return
	 */
	public Long saveDevicePointId(Long orgId, Long pointId, Integer devCombinationNo) {
		List<Object> args = new ArrayList<Object>();

		StringBuffer buf = new StringBuffer();
		String cols = SQLUtils.getColumnsSQL(Device.class, "C");
		buf.append(" SELECT ");
		buf.append(cols);
		buf.append(" FROM T_DEVICE C ");
		buf.append(" WHERE (C.POINT_ID = 0 ");
		buf.append(" OR C.POINT_ID IS NULL) ");
		buf.append(" AND C.COMBINATION_NO = ? ");
		buf.append(" AND C.ORG_ID = ? ");
		args.add(devCombinationNo);
		args.add(orgId);
		List<Device> devs = genericDao.findTs(Device.class, buf.toString(), args.toArray());

		if (devs.isEmpty()) {
			throw new BusinessException("不存在未绑定设备");
		}

		PointPlace pp = findPointPlace(pointId);

		// 设备与店铺关联起来，不是真正意义上的绑定
		Device dev = devs.get(0);
		dev.setPointId(pointId);
		if (null != pp) {
			dev.setAddress(pp.getPointAddress());// 更新设备所在地址
		}
		genericDao.update(dev);

		return dev.getId();// 未绑定的设备，默认选择第一个
	}
	
    /**
     * 添加设备组合
     */
	@Override
	public void addDevCombination(Integer combinationNo, Long pointplaceID) {
		
		PPDevCombination ppDev = new PPDevCombination();
		ppDev.setCombinationNo(combinationNo);
		ppDev.setPpId(pointplaceID);
		ppDev.setIdentity(String.valueOf(RandomUtil.next()));
		
		PointPlace pointPlace = findPointPlace(pointplaceID);
		
		Long devId = saveDevicePointId(pointPlace.getOrgId(), pointplaceID, combinationNo);
		if(StringUtils.isEmpty(devId))
			throw new BusinessException("指定设备发生异常");
		
		ppDev.setDeviceId(devId);
		
		genericDao.save(ppDev);
	}

	/**
	 * 删除设备组合
	 */
	@Override
	public void deleteDevCombination(String identity) {
		List<Object> args = new ArrayList<Object>();

		StringBuffer buffer = new StringBuffer();
		buffer.append(" SELECT ");
		String cols = SQLUtils.getColumnsSQL(PPDevCombination.class, "PD");
		buffer.append(cols);
		buffer.append(" FROM T_PP_DEV_COMBINATION PD ");
		buffer.append(" WHERE PD.IDENTITY = ? ");
		args.add(identity);
		PPDevCombination ppDev = genericDao.findT(PPDevCombination.class, buffer.toString(), args.toArray());
		Long deviceId = ppDev.getDeviceId();

		StringBuffer buf = new StringBuffer();
		buf.append(" DELETE FROM T_PP_DEV_COMBINATION PD ");
		buf.append(" WHERE PD.IDENTITY = ? ");

		genericDao.execute(buf.toString(), args.toArray());

		Device dev = findDeviceById(deviceId);
		Integer bindStateBefore = dev.getBindState();
		dev.setPointId(0L);
		dev.setAddress("");
		dev.setBindState(Commons.DEVICE_BIND_FALSE);// 解绑
		genericDao.update(dev);
		
		// 将该设备的商品全部下架
		genericDao.execute("UPDATE T_DEVICE_AISLE SET PRODUCT_ID=NULL, PRICE = NULL,STOCK=0,CAPACITY=0,PRODUCT_NAME=NULL, PRODUCT_CODE=NULL,SUPPLEMENT_NO=0,PRICE_ON_LINE=NULL,SELLABLE=1 WHERE DEVICE_ID = ? ", deviceId);
		
		// 如果设备是绑定状态，通知设备清空商品
		if (null != bindStateBefore && bindStateBefore == Commons.DEVICE_BIND_TRUE) {
			// 主动通知
			MessagePusher pusher = new MessagePusher();
			try {
				List<String> devNos = new ArrayList<String>();
				String facDevNo = findFacDevNoByDevNo(dev.getDevNo());
				if (!StringUtils.isEmpty(facDevNo)) {
					devNos.add(facDevNo);
					pusher.pushMessageToAndroidDevices(devNos, "{\"notifyFlag\":\"clearProducts\"}", true);
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw new BusinessException("清空商品通知失败！");
			}
		}
	}
	
	public String findFacDevNoByDevNo(String devNo) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buffer = new StringBuffer(" SELECT ");
		buffer.append(" C.FACTORY_DEV_NO AS DEV_NO ");
		buffer.append(" FROM T_DEVICE_RELATION C ");
		buffer.append(" WHERE C.DEV_NO = ? ");
		args.add(devNo);
		return genericDao.findSingle(String.class, buffer.toString(), args.toArray());
	}

	/**
	 * 查找设备组合模式
	 */
	@Override
	public List<Device> findDevCombination(Page page, Long orgId) {
		List<Object> args = new ArrayList<Object>();
		String cols = SQLUtils.getColumnsSQL(Device.class, "D");
		
		StringBuffer buf = new StringBuffer();
		buf.append(" SELECT ");		
		buf.append(cols);
		buf.append(" ,C.cabinet_no as cabinetNo, C.combination_no as combinationNo, C.model as model, C.aisle_count as aisleCounts ");
		buf.append(" FROM T_DEVICE D ");
		buf.append(" LEFT JOIN T_DEV_COMBINATION C ON D.COMBINATION_NO = C.COMBINATION_NO ");
		buf.append(" WHERE 1=1 ");
		buf.append(" AND D.ORG_ID = ? ");
		
		orgId = orgId == null ? ContextUtil.getUser(User.class).getOrgId() : orgId;
		args.add(orgId);
		
		buf.append(" AND D.ID NOT IN (SELECT DEVICE_ID FROM T_PP_DEV_COMBINATION) ");
		
		buf.append(" GROUP BY ").append(cols).append(" ,C .cabinet_no, C .combination_no, C .model, C .aisle_count ");
		buf.append(" ORDER BY C.COMBINATION_NO, C.CABINET_NO ");
		return genericDao.findTs(Device.class, page, buf.toString(), args.toArray());
	}

	/**
	 * 查找点位已绑定了的设备组合
	 */
	@Override
	public List<DevCombination> findBindDevCombination(Page page, Long pointplaceId) {
		List<Object> args = new ArrayList<Object>();
		String cols = SQLUtils.getColumnsSQL(DevCombination.class, "C");
		
		StringBuffer buf = new StringBuffer();
		buf.append(" SELECT ");		
		buf.append(cols);
		buf.append(" ,PD.IDENTITY AS IDENTITY ");
		buf.append(" ,PD.DEVICE_ID AS DEVICEID ");
		buf.append(" ,R.FACTORY_DEV_NO AS FACTORYDEVNO ");
		buf.append(" ,D.BIND_STATE AS BINDSTATE ");
		buf.append(" FROM T_DEV_COMBINATION C ");
		buf.append(" LEFT JOIN T_PP_DEV_COMBINATION PD ON PD.COMBINATION_NO = C.COMBINATION_NO ");
		buf.append(" LEFT JOIN T_POINT_PLACE PP ON PP.ID = PD.PP_ID ");
		buf.append(" LEFT JOIN T_DEVICE D ON D.ID = PD.DEVICE_ID ");
		buf.append(" LEFT JOIN T_DEVICE_RELATION R ON D.DEV_NO = R.DEV_NO ");
		buf.append(" WHERE PP.ID = ? ");
		args.add(pointplaceId);
		
		buf.append(" AND PP.STATE != ? ");
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
		
		buf.append(" ORDER BY IDENTITY, C.CABINET_NO ");
		return genericDao.findTs(DevCombination.class, page, buf.toString(), args.toArray());
	}
	
	public List<Orgnization> getOrgnizationList(Long orgId) {
		return orgnizationService.findCyclicOrgnizations(orgId);
	}
	
	/**
	 * 我的店铺---查询点位信息
	 * 
	 * 1、显示本机构下，本账号创建的店铺或分配给该账号的店铺；
	 * @param page
	 * @param pointPlace
	 * @return
	 */
	@Override
	public List<PointPlace> findMyStores(Page page, PointPlace pointPlace) {
		StringBuffer buf = new StringBuffer("SELECT ");
		String cols = SQLUtils.getColumnsSQL(PointPlace.class, "C");
		buf.append(cols);
		buf.append(" ,SO.NAME AS orgName ");
		buf.append(" FROM T_POINT_PLACE C LEFT JOIN SYS_ORG SO ON C.ORG_ID=SO.ID WHERE 1=1 ");
		List<Object> args = new ArrayList<Object>();;
		User user = ContextUtil.getUser(User.class);
		
		buf.append(" AND C.ORG_ID = ? ");
		args.add(user.getOrgId());

		buf.append(" AND (C.CREATE_USER = ? OR C.USER_ID = ?) ");// 本账号创建的店铺或分配给该账号的店铺；
		args.add(user.getId());
		args.add(user.getId());
		
		buf.append(" AND SO.NAME IS NOT NULL ");
		
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
		if (pointPlace.getPointAddress() != null) {// 店铺地址
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
		buf.append(" ORDER BY SO.NAME, C.CREATE_TIME DESC ");
		
		List<PointPlace> pointPlaces = genericDao.findTs(PointPlace.class, page, buf.toString(), args.toArray());
		
		for (PointPlace point : pointPlaces) {
			List<PointReplenishTime> pointReplenishTimes = findPointReplenishTimes(point.getPointNo());
			point.setPointReplenishTimes(pointReplenishTimes);
		}
		
		return pointPlaces;
	}
	
}