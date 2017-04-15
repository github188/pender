package com.vendor.service.impl.interaction;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

import com.ecarry.core.domain.WebUploader;
import com.vendor.po.*;
import com.vendor.util.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.context.Theme;
import org.springframework.util.StringUtils;

import com.ecarry.core.dao.IGenericDao;
import com.ecarry.core.dao.SQLUtils;
import com.ecarry.core.exception.BusinessException;
import com.ecarry.core.web.core.ContextUtil;
import com.ecarry.core.web.core.Page;
import com.vendor.service.IDictionaryService;
import com.vendor.service.IInteractionService;
import com.vendor.vo.app.AdvData;
import com.vendor.vo.app.VAdvertisement;
import org.springframework.web.multipart.MultipartFile;

@Service("interactionService")
public class InteractionService implements IInteractionService {

	private static final Logger LOGGER = Logger.getLogger(InteractionService.class);

	@Autowired
	private IGenericDao genericDao;

	@Autowired
	private IDictionaryService dictionaryService;

	/**
	 * 屏保广告列表
	 ***/
	public List<Advertisement> findAdvertisement(Advertisement advertisement, Page page) {
		StringBuffer buf = new StringBuffer("SELECT ");
		String columns = SQLUtils.getColumnsSQL(Advertisement.class, "A");
		buf.append(columns);
		buf.append(" FROM T_ADVERTISEMENT A WHERE A.ORG_ID=? ");
		List<Object> args = new ArrayList<>();
		args.add(ContextUtil.getUser(User.class).getOrgId());

		if (!StringUtils.isEmpty(advertisement.getAdvName())) {
			buf.append(" AND A.ADV_NAME LIKE ? ");
			args.add("%" + advertisement.getAdvName() + "%");
		}

		if (null != advertisement.getStartDate()) {// 开始日期
			buf.append(" AND A.START_DATE>=? ");
			args.add(DateUtil.getStartDate(new java.sql.Date(advertisement.getStartDate().getTime())));
		}
		if (null != advertisement.getEndDate()) {// 结束日期
			buf.append(" AND A.END_DATE<=? ");
			args.add(DateUtil.getEndDate(new java.sql.Date(advertisement.getEndDate().getTime())));
		}

		if (null != advertisement.getStatus()) {//广告状态
			buf.append(" AND A.STATUS=? ");
			args.add(advertisement.getStatus());
		}
		if (null != advertisement.getType()) {//广告类型
			buf.append(" AND A.TYPE=? ");
			args.add(advertisement.getType());
		}
		buf.append(" GROUP BY ").append(columns);

		List<Advertisement> advertisements = genericDao.findTs(Advertisement.class, page, buf.toString(), args.toArray());

		// 追加广告店铺关联信息
		for (Advertisement adv : advertisements) {
			// 查询绑定了指定广告的店铺信息
			List<PointPlace> pointPlaces = findPointPlacesByAdvId(adv.getId());
			adv.setPointPlaces(pointPlaces);

			// 查询广告对应的图片或视频文件
			args.clear();
			buf.setLength(0);
			buf.append(" SELECT STRING_AGG(B.ID||','||B.NAME||','||B.TYPE||','||B.REAL_PATH, ';' ORDER BY B.ID DESC) AS images");
			buf.append(" FROM T_FILE B WHERE 1 = 1 ");
			buf.append(" AND B.INFO_ID = ? ");
			args.add(adv.getId());
			if (Commons.ADV_POSITION_TOP == adv.getAdvPosition()) {
				buf.append(" AND B.TYPE = ? ");
				args.add(Commons.FILE_ADVERTISE_TOP);
			} else if (Commons.ADV_POSITION_BOTTOM == adv.getAdvPosition()) {
				buf.append(" AND B.TYPE = ? ");
				args.add(Commons.FILE_ADVERTISE_BOTTOM);
			} else if (Commons.ADV_POSITION_BG == adv.getAdvPosition()) {
				buf.append(" AND B.TYPE = ? ");
				args.add(Commons.FILE_ADVERTISE_BG);
			}
			FileStore fileStore = genericDao.findT(FileStore.class, buf.toString(), args.toArray());
			adv.setImages(null != fileStore ? fileStore.getImages() : "");
		}
		return advertisements;
	}

	/**
	 * 保存屏保商品信息
	 ***/
	public void saveScreenAdv(Advertisement advertisement, long key, long[] fileIds, long[] pointIds) throws Exception {
		StringBuffer buf = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		User user = ContextUtil.getUser(User.class);
		if (user == null)
			throw new BusinessException("当前用户未登录！");

		//广告投放开始时间/结束时间
		if (Commons.ADV_TYPE_COMMON == advertisement.getType()) {
			Timestamp startDate = advertisement.getStartDate();
			Timestamp endDate = advertisement.getEndDate();
			if (startDate.after(endDate))
				throw new BusinessException("投放起始日期大于投放结束日期");
			
			String beginTime = advertisement.getBeginTime();
			String endTime = advertisement.getEndTime();
			if (beginTime.compareTo(endTime) >= 0)
				throw new BusinessException("播放结束时间必须大于播放起始时间");

			String finalStartDateStr = DateUtil.timeStamp2String(startDate, DateUtil.YYYY_MM_DD_EN) + " " + beginTime + ":00";
			String finalEndDateStr = DateUtil.timeStamp2String(endDate, DateUtil.YYYY_MM_DD_EN) + " " + endTime + ":00";
			
			// 广告状态
			Timestamp now = new Timestamp(System.currentTimeMillis());
			if (now.before(DateUtil.stringToTimestamp(finalStartDateStr))) // 未开始
				advertisement.setStatus(Commons.ADV_STATUS_INIT);
			else if (now.before(DateUtil.stringToTimestamp(finalEndDateStr))) // 进行中
				advertisement.setStatus(Commons.ADV_STATUS_ING);
			else // 结束
				advertisement.setStatus(Commons.ADV_STATUS_FINISH);
		} else {
			advertisement.setStatus(Commons.ADV_STATUS_ING);
		}

		// 背景图片的广告在同一店铺同一时间段内只能有一个
		if (Commons.ADV_POSITION_BG == advertisement.getAdvPosition() && null != pointIds && pointIds.length > 0) {
			for (Long pointId : pointIds) {
				if (Commons.ADV_TYPE_DEFAULT == advertisement.getType()) {// 默认广告
					List<Advertisement> advertisements = findAdvByPointIdAndType(pointId, advertisement);
					if (null != advertisements && !advertisements.isEmpty()) {
						PointPlace pointPlace = findPointPlace(pointId);
						throw new BusinessException("店铺【" + pointPlace.getPointName() + "】已存在默认的背景广告，不可重复绑定。");
					}
				} else {// 普通广告
					List<Advertisement> advertisements = findAdvByPointIdAndType(pointId, advertisement);
					for (Advertisement adv : advertisements) {
						Timestamp startDate = advertisement.getStartDate();
						Timestamp endDate = advertisement.getEndDate();
						String beginTime = advertisement.getBeginTime();
						String endTime = advertisement.getEndTime();
//						if (endDate.before(adv.getStartDate()) || startDate.after(adv.getEndDate()))
						if (!endDate.before(adv.getStartDate()) && !startDate.after(adv.getEndDate()) && endTime.compareTo(adv.getBeginTime()) >= 0 && beginTime.compareTo(adv.getEndTime()) <= 0) {
							PointPlace pointPlace = findPointPlace(pointId);
							throw new BusinessException("店铺【" + pointPlace.getPointName() + "】投放日期和播放时间段内已存在有背景广告，不可重复绑定。");
						}
					}
				}
			}// for
		}

		boolean isUpdate = false;
		if (advertisement.getId() == null) {// 新增
			advertisement.setOrgId(user.getOrgId());
			advertisement.setCreatime(new Timestamp(System.currentTimeMillis()));
			genericDao.save(advertisement);
		} else {
			isUpdate = true;
			genericDao.update(advertisement);

			if (fileIds != null) { // 删除图片
				args.clear();
				args.add(Commons.FILE_ADVERTISE_TOP);
				args.add(Commons.FILE_ADVERTISE_BG);
				args.add(Commons.FILE_ADVERTISE_BOTTOM);
				args.add(advertisement.getId());
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
				buf.append("SELECT REAL_PATH FROM T_FILE WHERE (TYPE=? OR TYPE=? OR TYPE=?) AND INFO_ID=? AND ID").append(inIds);
				List<Object> realPaths = genericDao.findListSingle(buf.toString(), args.toArray());
				if (realPaths.size() == fileIds.length) {
					String[] ary = new String[realPaths.size()];
					realPaths.toArray(ary);
					List<Integer> idxs = dictionaryService.batchDelete(ary);
					buf.setLength(0);
					buf.append("DELETE FROM T_FILE WHERE (TYPE=? OR TYPE=? OR TYPE=?) AND INFO_ID=? AND ID");
					if (idxs.isEmpty()) {
						buf.append(inIds);
						genericDao.execute(buf.toString(), args.toArray());
					} else if (idxs.size() < fileIds.length) { // 存在删除失败的文件，要过滤出来，不删除数据库记录
						buf.append(" IN(");
						args.clear();
						args.add(Commons.FILE_ADVERTISE_TOP);
						args.add(Commons.FILE_ADVERTISE_BG);
						args.add(Commons.FILE_ADVERTISE_BOTTOM);
						args.add(advertisement.getId());
						for (int i = 0; i < fileIds.length; i++) {
							boolean exists = false;
							for (int j = 0; j < idxs.size(); j++) {
								if (i == idxs.get(j)) {
									idxs.remove(j);
									exists = true;
									break;
								}
							}
							if (!exists) {
								buf.append("?,");
								args.add(fileIds[i]);
							}
						}
						buf.setLength(buf.length() - 1);
						buf.append(")");
						genericDao.execute(buf.toString(), args.toArray());
					}
				}
			}
		}

		buf.setLength(0);
		buf.append("UPDATE T_FILE SET INFO_ID=? WHERE (TYPE=? OR TYPE=? OR TYPE=?) AND INFO_ID=? AND CREATE_USER=?");
		genericDao.execute(buf.toString(), advertisement.getId(), Commons.FILE_ADVERTISE_TOP, Commons.FILE_ADVERTISE_BG, Commons.FILE_ADVERTISE_BOTTOM, key * -1, user.getId());

		// 绑定店铺广告关系，并通知设备
		saveBindAdvPoints(isUpdate, advertisement.getId(), pointIds);

	}

	/**
	 * 绑定店铺广告关系
	 */
	public void saveBindAdvPoints(boolean isUpdate, Long advertiseId, long[] pointIds) {
		User curUser = ContextUtil.getUser(User.class);
		if (null == curUser) throw new BusinessException("当前用户未登录，请登录后操作。");

		Advertisement adv = findScreenAdvFile(advertiseId);
		if (null == adv) throw new BusinessException("广告不存在");

		if (null != pointIds && pointIds.length > 0) {
			if (!isUpdate) {// insert操作
				// 该广告绑定到指定店铺下
				for (Long pointId : pointIds) {
					PointAdv pointAdv = new PointAdv();
					pointAdv.setAdvertiseId(advertiseId);
					pointAdv.setPointId(pointId);
					genericDao.save(pointAdv);
				}
				// 将该广告通知到指定店铺下的所有设备上
				List<Object> devNos = findDeviceNosByPointIds(pointIds);
				if (null != devNos && devNos.size() > 0) {
					String[] devNoArr = CommonUtil.convertToStringArr(devNos);
					// 通知各设备，追加该广告
					pushAdvDataMessage(Arrays.asList(devNoArr), Commons.NOTIFY_ADV_ADD, adv);
				}
			} else {// update操作
				saveUpdateAdvPoints(curUser, adv, pointIds);
			}
		} else {//delete操作
			// 取出当前广告投放的设备
			List<Object> devNos = findDeviceNosByAdvertiseId(advertiseId);
			if (null != devNos && devNos.size() > 0) {
				String[] devNoArr = CommonUtil.convertToStringArr(devNos);
				// 通知各设备，停放该广告
				pushAdvDataMessage(Arrays.asList(devNoArr), Commons.NOTIFY_ADV_DELETE, adv);
			}

			// 删除绑定关系
			genericDao.execute(" DELETE FROM T_POINT_ADV WHERE ADVERTISE_ID = ? ", advertiseId);
		}
	}

	/**
	 * 转换成前端用对象
	 */
	public VAdvertisement generateVAdv(Advertisement adv) {
		VAdvertisement advertisement = new VAdvertisement();
		advertisement.setAdvId(adv.getId());
		advertisement.setAdvName(adv.getAdvName());
		advertisement.setAdvIndex(adv.getIndex());
		advertisement.setAdvPosition(adv.getAdvPosition());
		advertisement.setType(adv.getType());
		if (Commons.ADV_TYPE_COMMON == adv.getType()) {//普通广告
			advertisement.setStartDate(DateUtil.timeStamp2String(adv.getStartDate(), DateUtil.YYYY_MM_DD_EN));
			advertisement.setEndDate(DateUtil.timeStamp2String(adv.getEndDate(), DateUtil.YYYY_MM_DD_EN));
			advertisement.setBeginTime(adv.getBeginTime() + ":00");
			advertisement.setEndTime(adv.getEndTime() + ":00");
		}

		advertisement.setScreenType(adv.getScreenType());

		String images = adv.getImages();
		if (StringUtils.isEmpty(images))
			throw new BusinessException("广告文件不存在");

		String[] imageArr = images.split(";");
		String[] fileUrls = new String[imageArr.length];
		for (int i = 0; i < imageArr.length; i++)
			fileUrls[i] = dictionaryService.getFileServer() + imageArr[i].split(",")[3];

		advertisement.setFileUrls(CommonUtil.converToString(fileUrls, ";"));
		return advertisement;
	}

	/**
	 * 推送广告信息
	 */
	public void pushAdvDataMessage(List<String> devNos, Integer type, Advertisement adv) {
		AdvData advData = new AdvData();
		advData.setNotifyFlag(Commons.NOTIFY_ADV);// 广告通知
		advData.setType(type);

		// 转换成前端用对象
		VAdvertisement advertisement = generateVAdv(adv);
		advData.setAdvertisement(advertisement);
		// 主动通知
		MessagePusher pusher = new MessagePusher();
		try {
			pusher.pushMessageToAndroidDevices(devNos, ContextUtil.getJson(advData), true);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new BusinessException("广告通知失败！");
		}
	}

	/**
	 * 更新绑定广告店铺信息
	 */
	public void saveUpdateAdvPoints(User user, Advertisement adv, long[] pointIds) {
        // 表中原始数据
        List<PointAdv> originPointAdvs = findDBBindingAdvPoints(adv.getId());
        // 获取表中原始数据的Map
        Map<Long, PointAdv> originPointAdvsMap = new HashMap<Long, PointAdv>();
        for (PointAdv originPointAdv : originPointAdvs)
        	originPointAdvsMap.put(originPointAdv.getPointId(), originPointAdv);
        // 获取页面传递过来数据的Map
        Map<Long, Long> finalPointAdvsMap = new HashMap<Long, Long>();
        for (Long pointId : pointIds)
        	finalPointAdvsMap.put(pointId, pointId);

        // 1. 以【页面传递过来数据的Map】为基准：如果包含DB中的原始数据，则做更新操作；否则做删除操作
        for (PointAdv pointAdv : originPointAdvs) {
            Long key = pointAdv.getPointId();
            if (finalPointAdvsMap.keySet().contains(key)) {// 更新
            	List<Object> devNos = findDeviceNosByPointAndOrg(key,user.getOrgId());
            	if (null != devNos && devNos.size() > 0) {
            		String[] devNoArr = CommonUtil.convertToStringArr(devNos);
            		pushAdvDataMessage(Arrays.asList(devNoArr), Commons.NOTIFY_ADV_UPDATE, adv);
            	}
            } else {// 删除
            	// 将该广告从投放的店铺中删除
            	genericDao.execute("DELETE FROM T_POINT_ADV WHERE ADVERTISE_ID = ? AND POINT_ID = ?", adv.getId(), key);
            	List<Object> devNos = findDeviceNosByPointAndOrg(key,user.getOrgId());
            	if (null != devNos && devNos.size() > 0) {
            		String[] devNoArr = CommonUtil.convertToStringArr(devNos);
            		pushAdvDataMessage(Arrays.asList(devNoArr), Commons.NOTIFY_ADV_DELETE, adv);
            	}
            }
        }

        // 2. 以【表中原始数据的Map】为基准：如果不包含页面传递过来数据，则做新增操作
        for (Long pointId : pointIds) {
            if (!originPointAdvsMap.keySet().contains(pointId)) {// 新增
            	PointAdv pointAdv = new PointAdv();
				pointAdv.setAdvertiseId(adv.getId());
				pointAdv.setPointId(pointId);
				genericDao.save(pointAdv);

				// 将该广告通知到指定店铺下的所有设备上
				List<Object> devNos = findDeviceNosByPointAndOrg(pointId,user.getOrgId());
				if (null != devNos && devNos.size() > 0) {
					String[] devNoArr = CommonUtil.convertToStringArr(devNos);
					// 通知各设备，追加该广告
					pushAdvDataMessage(Arrays.asList(devNoArr), Commons.NOTIFY_ADV_ADD, adv);
				}
            }
        }
    }

	/**
	 * 根据店铺ID查询投放的设备编号
	 * @return
	 */
	public List<Object> findDeviceNosByPointIds(long[] pointIds) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buffer = new StringBuffer(" IN(");
		for (Long id : pointIds) {
			buffer.append("?,");
			args.add(id);
		}
		buffer.setLength(buffer.length() - 1);
		buffer.append(")");
		String inIds = buffer.toString();

		buffer.setLength(0);
		buffer.append(" SELECT DISTINCT(DR.FACTORY_DEV_NO) ");
		buffer.append(" FROM T_DEVICE D ");
		buffer.append(" LEFT JOIN T_POINT_PLACE PP ON D.POINT_ID = PP.ID ");
		buffer.append(" LEFT JOIN T_DEVICE_RELATION DR ON DR.DEV_NO = D.DEV_NO ");
		buffer.append(" WHERE D.POINT_ID ").append(inIds);
		buffer.append(" AND D.ORG_ID = ? ");
		args.add(ContextUtil.getUser(User.class).getOrgId());

		buffer.append(" AND PP.STATE != ? ");
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
		
		return genericDao.findListSingle(buffer.toString(), args.toArray());
	}

	/**
	 * 根据广告ID查询投放的设备编号
	 * @return
	 */
	public List<Object> findDeviceNosByAdvertiseId(Long advertiseId) {
		StringBuffer buffer = new StringBuffer("SELECT DISTINCT(DR.FACTORY_DEV_NO) ");
		buffer.append(" FROM T_DEVICE D ");
		buffer.append(" LEFT JOIN T_POINT_PLACE PP ON D.POINT_ID = PP.ID ");
		buffer.append(" LEFT JOIN T_DEVICE_RELATION DR ON DR.DEV_NO = D.DEV_NO ");
		buffer.append(" WHERE D.POINT_ID IN (SELECT POINT_ID FROM T_POINT_ADV WHERE ADVERTISE_ID = ?) ");
		buffer.append(" AND PP.STATE != ? ");
		return genericDao.findListSingle(buffer.toString(), advertiseId, Commons.POINT_PLACE_STATE_DELETE);
	}

	public List<Object> findDeviceNosByPointAndOrg(Long pointId, Long orgId) {
		return genericDao.findListSingle(" SELECT DISTINCT(DR.FACTORY_DEV_NO) FROM T_DEVICE D LEFT JOIN T_DEVICE_RELATION DR ON DR.DEV_NO = D.DEV_NO WHERE D.POINT_ID = ? AND D.ORG_ID = ? ", pointId, orgId);
	}

	/**
	 * 根据广告ID查询投放的设备信息
	 * @return
	 */
	public List<Device> findDeviceByAdvertiseId(Long advertiseId) {
		StringBuffer buffer = new StringBuffer("SELECT ");
		buffer.append(SQLUtils.getColumnsSQL(Device.class, "D"));
		buffer.append(" FROM T_DEVICE D ");
		buffer.append(" LEFT JOIN T_POINT_PLACE PP ON D.POINT_ID = PP.ID ");
		buffer.append(" WHERE D.POINT_ID IN (SELECT POINT_ID FROM T_POINT_ADV WHERE ADVERTISE_ID = ?) ");
		buffer.append(" AND PP.STATE != ? ");
		return genericDao.findTs(Device.class, buffer.toString(), advertiseId, Commons.POINT_PLACE_STATE_DELETE);
	}

	/**
	 * 根据广告ID查询设备广告关联信息
	 * @param orgId
	 * @return
	 */
	public List<PointAdv> findDBBindingAdvPoints(Long advertiseId) {
		StringBuffer buf = new StringBuffer("SELECT ");
		String cols = SQLUtils.getColumnsSQL(PointAdv.class, "C");
		buf.append(cols);
		buf.append(" FROM T_POINT_ADV C WHERE C.ADVERTISE_ID = ? ");
		List<Object> args = new ArrayList<Object>();
		args.add(advertiseId);
		return genericDao.findTs(PointAdv.class, buf.toString(), args.toArray());
	}

	public Device findDeviceByDevNo(String deviceNumber) {
		StringBuffer buffer = new StringBuffer("SELECT ");
		buffer.append(SQLUtils.getColumnsSQL(Device.class, "A"));
		buffer.append(" FROM T_DEVICE A WHERE A.DEV_NO=? ");
		return genericDao.findT(Device.class, buffer.toString(), deviceNumber);
	}

	/**
	 * 删除屏保广告信息
	 ***/
	public void deleteScreen(Long[] ids) {
		if (ids != null && ids.length > 0) {
			List<Object> args = new ArrayList<Object>();
			StringBuffer sb = new StringBuffer(" IN(");
			for (Long id : ids) {
				sb.append("?,");
				args.add(id);

				// 通知各店铺设备，停放该广告
				Advertisement adv = findScreenAdvFile(id);
				// 取出当前广告投放的设备
				List<Object> devNos = findDeviceNosByAdvertiseId(id);
				if (null != devNos && devNos.size() > 0) {
					String[] devNoArr = CommonUtil.convertToStringArr(devNos);
					pushAdvDataMessage(Arrays.asList(devNoArr), Commons.NOTIFY_ADV_DELETE, adv);
				}
			}
			sb.setLength(sb.length() - 1);
			sb.append(")");
			String inIds = sb.toString();

			// 删除店铺广告关联信息
			sb.setLength(0);
			sb.append("DELETE FROM T_POINT_ADV WHERE ADVERTISE_ID").append(inIds);
			genericDao.execute(sb.toString(), args.toArray());

			// 删除广告信息
			sb.setLength(0);
			sb.append("DELETE FROM T_ADVERTISEMENT WHERE ID").append(inIds);
			int count = genericDao.execute(sb.toString(), args.toArray());
			if (count != ids.length)
				throw new BusinessException("非法请求!");
		}
	}

	public void deleteFile(Long infoId, Integer type) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(" DELETE FROM T_FILE WHERE INFO_ID=? AND TYPE = ? ");
		genericDao.execute(buffer.toString(), infoId, type);
	}

	/**
	 * 更改广告的状态
	 */
	@Override
	public void saveAdvStatus(Long advertiseId, Integer status) {
		if (null == status || Commons.ADV_STATUS_INIT != status || Commons.ADV_STATUS_ING != status || Commons.ADV_STATUS_FINISH != status)
			throw new BusinessException("非法的广告状态！");

		int count = genericDao.execute(" UPDATE T_ADVERTISEMENT SET STATUS = ? WHERE ID = ? ", status, advertiseId);
		if (count <= 0)
			throw new BusinessException("广告不存在！");
	}

	/**
	 * 查询店铺信息
	 */
	@Override
	public List<PointPlace> findPointPlaces(Long advertiseId) {
		// 查询所有店铺信息
		List<PointPlace> pointPlaces = findAllPointPlaces();
		if (null == advertiseId)
			return pointPlaces;

		// 查询绑定了指定广告的店铺信息
		return findPointPlacesByAdvId(advertiseId);
	}

	/**
	 * 保存主题皮肤(模板)
	 * @param themeSkin
	 */
	@Override
	public void saveThemeSkinEntity(ThemeSkin themeSkin) {
		User user = ContextUtil.getUser(User.class);
		if(null == user)
			throw new BusinessException("当前用户未登录");
		if(null == themeSkin)
			throw new BusinessException("请求错误");

		if(null == themeSkin.getId()){//新增
			themeSkin.setCreateTime(new Timestamp(System.currentTimeMillis()));
			themeSkin.setOrgId(user.getOrgId());
			themeSkin.setDefaultTheme(Commons.THEMESKIN_DEFAULT_THEME_1);
			themeSkin.setState(Commons.THEMESKIN_STATE_FEASIBLE);
			genericDao.save(themeSkin);
		}else{//编辑
			ThemeSkin themeSkinSql = genericDao.findT(ThemeSkin.class, "SELECT * FROM T_THEME_SKIN WHERE ID=? AND ORG_ID=?",
					themeSkin.getId(), user.getOrgId());
			themeSkinSql.setThemeName(themeSkin.getThemeName());
			themeSkinSql.setUpdateTime(new Timestamp(System.currentTimeMillis()));
			genericDao.update(themeSkinSql);
		}
		//更新主题图片与文件
		updateFile(themeSkin.getId(), themeSkin.getFileIds(), themeSkin.getKey(), new int[]{Commons.FILE_THEME,Commons.FILE_THEME_TYPE}, user.getId());
	}

	/**
	 * 保存设备主题关系
	 * @param hThemeId
	 * @param vThemeId
	 * @param themeSkins
	 * @param startTime
	 */
	@Override
	public void saveThemeSkinAndDevice(Integer[] themeDevices, Long hthemeId, Long vthemeId, Timestamp startTime){
		User user = ContextUtil.getUser(User.class);
		if(null == user)
			throw new BusinessException("当前用户未登录");
		if(null != themeDevices && themeDevices.length > 0 && (null != hthemeId || null != vthemeId)){
			//这里根据主题Id去查询出主题,如果没找到可能主题被删除或不存在,
			Long themeSkinVThemeId = genericDao.findSingle(Long.class, "SELECT ID FROM T_THEME_SKIN WHERE ID=? AND ORG_ID=? AND STATE!=?", vthemeId, user.getOrgId(), Commons.THEMESKIN_STATE_TRASH);
			Long themeSkinHThemeId = genericDao.findSingle(Long.class, "SELECT ID FROM T_THEME_SKIN WHERE ID=? AND ORG_ID=? AND STATE!=?", hthemeId, user.getOrgId(), Commons.THEMESKIN_STATE_TRASH);

			if(null == themeSkinHThemeId && null == themeSkinVThemeId)
				throw new BusinessException("所选择的主题已被删除或不存在!");

			for(Integer factoryDevNo : themeDevices){
				//先查询此设备是横屏还是竖屏,确定他是新增还是编辑,然后在去保存
				Long deviceType = getaLong(user.getOrgId(), String.valueOf(factoryDevNo));
				//查询设备主题表主是否存在
				ThemeDevice themeDeviced = genericDao.findT(ThemeDevice.class, "SELECT * FROM T_THEME_DEVICE WHERE FACTORY_DEV_NO=? AND ORG_ID=?", String.valueOf(factoryDevNo), user.getOrgId());
				if(null == themeDeviced){//为空时新增主题
					if(deviceType==1 && null != themeSkinHThemeId){//横屏
						saveTheme(startTime, user, themeSkinHThemeId, factoryDevNo, deviceType);
					}
					if(deviceType==2 && null != themeSkinVThemeId){//竖屏
						saveTheme(startTime, user, themeSkinVThemeId, factoryDevNo, deviceType);
					}
				}else{//说明是修改主题
                    if(deviceType == 1 && null!=themeSkinHThemeId){
                        themeDeviced.setToenableId(themeSkinHThemeId);//待启用主题
                        themeDeviced.setStartTime(startTime);
                        themeDeviced.setExecutingState(Commons.THEMESKIN_EXECUTING_STATE_0);//未执行
                        genericDao.update(themeDeviced);
                    }
                    if(deviceType == 2 && null != themeSkinVThemeId){
                        themeDeviced.setToenableId(themeSkinVThemeId);//待启用主题
                        themeDeviced.setStartTime(startTime);
                        themeDeviced.setExecutingState(Commons.THEMESKIN_EXECUTING_STATE_0);//未执行
					    genericDao.update(themeDeviced);
                    }
				}
			}
		}
	}

	/**
	 * 保存主题(1横屏,2竖屏)
	 * @param startTime
	 * @param user
	 * @param themeSkinHThemeId
	 * @param factoryDevNo
	 * @param deviceType
	 */
	private void saveTheme(Timestamp startTime, User user, Long themeSkinHThemeId, Integer factoryDevNo, Long deviceType) {
		ThemeDevice themeDevice = new ThemeDevice();
		themeDevice.setThemeType(deviceType==1 ? Commons.THEMESKIN_THEMETYPE_0 : Commons.THEMESKIN_THEMETYPE_1);
		themeDevice.setOrgId(user.getOrgId());
		themeDevice.setStartTime(startTime);
		themeDevice.setFactoryDevNo(String.valueOf(factoryDevNo));
		themeDevice.setThemeId(themeSkinHThemeId);
		themeDevice.setExecutingState(Commons.THEMESKIN_EXECUTING_STATE_0);//未执行
        genericDao.save(themeDevice);
	}

	/**
     * 查询主柜屏幕类型(1：横屏,:2：竖屏)
     * @param orgId
     * @param factoryDevNo
     * @return //1:是大智能饮料机（横屏）2;// 小型智能饮料机（竖屏）
     */
    private Long getaLong(Long orgId, String factoryDevNo) {
        StringBuffer buf = new StringBuffer();
        List<Object> args = new ArrayList<Object>();
        buf.append(" SELECT TYPE FROM T_CABINET WHERE DEVICE_ID=(");
        buf.append(" SELECT ID FROM T_DEVICE WHERE ORG_ID=? AND DEV_NO=(");
        args.add(orgId);
        buf.append(" SELECT DEV_NO FROM T_DEVICE_RELATION WHERE FACTORY_DEV_NO=?)) AND CABINET_NO=?");
        args.add(factoryDevNo);
		args.add("1");
        return genericDao.findSingle(Long.class, buf.toString(), args.toArray());
    }

    /**
	 * 更新图片数据
	 * @param infoId
	 * @param fileIds
	 * @param key
	 * @param types
	 * @param userId
	 */
	private void updateFile(Long infoId, Long[] fileIds, Long key, int[] types, Long userId) {
		StringBuffer buf = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
        for(int type : types){
            buf.append(" TYPE=? OR");
            args.add(type);
        }
        buf.setLength(buf.length()-2);
        String inIds2 = buf.toString();

		if (null != fileIds && !"".equals(fileIds) && fileIds.length > 0) {// 删除新增时多余的图片 fileIds是多余图片的ID数组
			args.add(infoId);
			buf.setLength(0);
			buf.append("(");
			for (Long id : fileIds) {
				buf.append("?,");
				args.add(id);
			}
			buf.setLength(buf.length() - 1);
			buf.append(")");
			String inIds = buf.toString();

			buf.setLength(0);
			buf.append("SELECT REAL_PATH FROM T_FILE WHERE (").append(inIds2).append(") AND INFO_ID=? AND ID IN").append(inIds);
			List<Object> realPaths = genericDao.findListSingle(buf.toString(), args.toArray());
			if (realPaths.size() == fileIds.length) {
				String[] ary = new String[realPaths.size()];
				realPaths.toArray(ary);
				buf.setLength(0);
				buf.append("DELETE FROM T_FILE WHERE (").append(inIds2).append(") AND INFO_ID=? AND ID IN").append(inIds);
				genericDao.execute(buf.toString(), args.toArray());
			}
		}
		if(null != key){//更新图片的INFO_ID
			buf.setLength(0);
			buf.append("UPDATE T_FILE SET INFO_ID=? WHERE (").append(inIds2).append(") AND INFO_ID=? AND CREATE_USER=?");
			args.add(0, infoId);
			args.add(3, key * -1);
			args.add(4, userId);
			if(args.size()==7){
				args.remove(6);
				args.remove(5);
			}
			genericDao.execute(buf.toString(), args.toArray());
		}
	}



	/**
     * 查询出本机构的主题
     * @param page
     * @return
     */
    @Override
    public List<ThemeSkin> findThemeSkinList(Page page) {
        User user = ContextUtil.getUser(User.class);
        if(null == user)
            throw new BusinessException("当前用户未登录");
        StringBuffer buf = new StringBuffer();
        List<Object> args = new ArrayList<Object>();
        buf.append(" SELECT STRING_AGG (B. ID || ',' || B. NAME || ',' || B. TYPE || ',' || B.REAL_PATH,';'ORDER BY B. TYPE,B. ID DESC) AS IMAGES ,");
        buf.append(" TS.ID, TS.THEME_NAME, TS.THEME_TYPE, TS.DEFAULT_THEME");
        buf.append(" FROM T_THEME_SKIN TS LEFT JOIN T_FILE B ON B.INFO_ID = TS. ID AND B. TYPE =?");
        args.add(Commons.FILE_THEME);
        buf.append(" WHERE TS.ORG_ID =? AND TS.STATE !=?");
        args.add(user.getOrgId());
        args.add(Commons.PRODUCT_STATE_TRASH);
        buf.append(" group by TS.ID, TS.THEME_NAME, TS.THEME_TYPE, TS.DEFAULT_THEME");
		return genericDao.findTs(ThemeSkin.class, page, buf.toString(), args.toArray());
    }

    /**
     * 查询出机构的设备以及设备的主题
     *
     * @param themeSkin
     * @param page
     * @return
     */
    @Override
    public List<ThemeDevice> updateAndFindDeviceThemeSkinList(ThemeDevice themeSkin, Page page) {
        User user = ContextUtil.getUser(User.class);
        if(null == user)
            throw new BusinessException("当前用户未登录");

        parentThmeName(user.getOrgId());

        StringBuffer buf = new StringBuffer();
        List<Object> args = new ArrayList<Object>();
		buf.append(" SELECT");
		buf.append(" D.ID,A.ID themeId,B.ID toenableId,DR.FACTORY_DEV_NO,PP.POINT_NAME POINTNAME,PP.POINT_ADDRESS POINTADDRESS,PP.POINT_TYPE pointType,");
		buf.append(" 		A.THEME_NAME themeName,B.THEME_NAME toenableName,");
		buf.append(" 		TD.START_TIME,TD.EXECUTING_STATE,COALESCE(DL.DEVICE_STATUS,1) DEVICESTATUS");
		buf.append(" FROM T_POINT_PLACE PP");
		buf.append(" LEFT JOIN T_DEVICE D ON D.POINT_ID=PP.ID AND D.BIND_STATE=?");
		args.add(Commons.BIND_STATE_SUCCESS);
		buf.append(" LEFT JOIN T_DEVICE_LOG DL ON DL.DEVICE_NO=D.DEV_NO AND DL.DEVICE_STATUS=?");
		args.add(Commons.DEVICE_STATUS_OFFLINE);
		buf.append(" LEFT JOIN T_DEVICE_RELATION DR ON DR.DEV_NO=D.DEV_NO");
		buf.append(" LEFT JOIN T_THEME_DEVICE TD ON TD.FACTORY_DEV_NO=DR.FACTORY_DEV_NO AND TD.ORG_ID=D.ORG_ID");
		buf.append(" LEFT JOIN (");
		buf.append(" 		SELECT ID,THEME_NAME FROM T_THEME_SKIN WHERE STATE!=?");
		args.add(Commons.THEMESKIN_STATE_TRASH);
		buf.append(" )A ON A.ID=TD.THEME_ID");
		buf.append(" LEFT JOIN (");
		buf.append(" 		SELECT ID,THEME_NAME FROM T_THEME_SKIN WHERE STATE!=?");
		args.add(Commons.THEMESKIN_STATE_TRASH);
		buf.append(" )B ON B.ID=TD.TOENABLE_ID");
		buf.append(" WHERE D.ORG_ID=? AND DR.FACTORY_DEV_NO IS NOT NULL");
		args.add(user.getOrgId());
        if(null != themeSkin.getFactoryDevNo()){
            buf.append(" AND DR.FACTORY_DEV_NO LIKE ?");
            args.add("%"+themeSkin.getFactoryDevNo()+"%");
        }
        if(null != themeSkin.getPointName()){
            buf.append(" AND PP.POINT_NAME LIKE ?");
            args.add("%"+themeSkin.getPointName()+"%");
        }
        if(null != themeSkin.getPointAddress()){
            buf.append(" AND PP.POINT_ADDRESS LIKE ?");
            args.add("%"+themeSkin.getPointAddress()+"%");
        }
        if(null != themeSkin.getExecutingState()){
            buf.append(" AND TD.EXECUTING_STATE=?");
            args.add(themeSkin.getExecutingState());
        }
        if(null != themeSkin.getThemeType()){
            buf.append(" AND TD.THEME_TYPE=?");
            args.add(themeSkin.getThemeType());
        }
        buf.append(" GROUP BY D.ID,A.ID,B.ID,DR.FACTORY_DEV_NO,PP.POINT_NAME,PP.POINT_ADDRESS,PP.POINT_TYPE,A.THEME_NAME,B.THEME_NAME,TD.START_TIME,TD.EXECUTING_STATE,DL.DEVICE_STATUS");
        return genericDao.findTs(ThemeDevice.class, page, buf.toString(), args.toArray());
    }

    /**
     * 返回上级默认的主题ID
     * @param orgId
     * @param parentIds
     * @param deviceType
     * @return
     */
    private void parentThmeName(Long orgId) {
		StringBuffer buf = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		if (null != orgId) {//本机构ID不为空时,先查询本机构是否有当前主题或者待应用主题没有就去查默认主题，否则去查上级的默认主题
			//先找本机构的设备,看设备主题表中是否存在 不存在就去插入数据
			buf.append(" SELECT FACTORY_DEV_NO factoryDevNo FROM T_DEVICE D");
			buf.append(" LEFT JOIN T_DEVICE_RELATION DR ON DR.DEV_NO=D.DEV_NO");
			buf.append(" WHERE D.ORG_ID=? AND D.BIND_STATE=?");
			args.add(orgId);
			args.add(Commons.BIND_STATE_SUCCESS);
			List<Device> deviceList = genericDao.findTs(Device.class, buf.toString(), args.toArray());
			if (null != deviceList && !deviceList.isEmpty()) {
				for (Device device : deviceList) {
					args.clear();
					buf.setLength(0);
					buf.append("SELECT * FROM T_THEME_DEVICE WHERE ORG_ID=? AND FACTORY_DEV_NO=?");
					args.add(orgId);
					args.add(device.getFactoryDevNo());
					ThemeDevice themeDevice = genericDao.findT(ThemeDevice.class, buf.toString(), args.toArray());
					if (null == themeDevice) {
						//先查询此设备是横屏还是竖屏,确定他是新增还是编辑,然后在去保存  (1：横屏,:2：竖屏)
						Long deviceType = getaLong(orgId, device.getFactoryDevNo());

						Long themeSkinId = getThemeSkin(orgId, 1==deviceType?Commons.THEMESKIN_THEMETYPE_0:Commons.THEMESKIN_THEMETYPE_1);
						if (null != themeSkinId) {
							//这里添加数据库的主题表
							args.clear();
							buf.setLength(0);
							buf.append("INSERT INTO T_THEME_DEVICE(THEME_TYPE,ORG_ID,FACTORY_DEV_NO,TOENABLE_ID,START_TIME,EXECUTING_STATE)");
							buf.append("VALUES(?,?,?,?,?,?)");
							args.add(1==deviceType?Commons.THEMESKIN_THEMETYPE_0:Commons.THEMESKIN_THEMETYPE_1);
							args.add(orgId);
							args.add(device.getFactoryDevNo());
							args.add(themeSkinId);
							args.add(new Timestamp(System.currentTimeMillis()));
							args.add(Commons.THEMESKIN_EXECUTING_STATE_0);
							genericDao.execute(buf.toString(), args.toArray());
						}
					}else if(null != themeDevice && null == themeDevice.getThemeId() && null == themeDevice.getToenableId()){
						//先查询此设备是横屏还是竖屏,确定他是新增还是编辑,然后在去保存  (1：横屏,:2：竖屏)
						Long deviceType = getaLong(orgId, themeDevice.getFactoryDevNo());

						Long themeSkinId = getThemeSkin(orgId, 1==deviceType?Commons.THEMESKIN_THEMETYPE_0:Commons.THEMESKIN_THEMETYPE_1);
						if (null != themeSkinId) {
							//这里更新数据库的主题表
							themeDevice.setToenableId(themeSkinId);
							themeDevice.setStartTime(new Timestamp(System.currentTimeMillis()));
							themeDevice.setExecutingState(Commons.THEMESKIN_EXECUTING_STATE_0);
							genericDao.update(themeDevice);
						}
					}
				}
			}
		}
	}

	/**
	 * 返回主题皮肤ID
	 * @param orgId
	 * @param devicetype
	 * @return
	 */
	private Long getThemeSkin(Long orgId, int devicetype) {

		Long themeSkinId = genericDao.findSingle(Long.class, "SELECT ID FROM T_THEME_SKIN WHERE ORG_ID=? AND DEFAULT_THEME=? AND THEME_TYPE=? AND STATE!=?",
				orgId, Commons.THEMESKIN_DEFAULT_THEME_0, devicetype, Commons.THEMESKIN_STATE_TRASH);
		if(null == themeSkinId){
			if(1 == orgId)
				return null;

			Long parentId = genericDao.findSingle(Long.class, "SELECT PARENT_ID FROM SYS_ORG WHERE ID=?", orgId);
			if(null != parentId){
				return getThemeSkin(parentId, devicetype);
			}else{
				return null;
			}
		}
		return themeSkinId;
	}


	/**
	 * 删除主题皮肤
	 * @param themeId
	 */
	@Override
	public void deleteThemeSkin(Long themeId) {
		User user = ContextUtil.getUser(User.class);
		if(null == user)
			throw new BusinessException("当前用户未登录");
		if(null == themeId)
			throw new BusinessException("请求错误");

		ThemeSkin themeSkin = genericDao.findT(ThemeSkin.class, "SELECT * FROM T_THEME_SKIN WHERE ORG_ID=? AND STATE!=? AND ID=?",
				user.getOrgId(), Commons.THEMESKIN_STATE_TRASH, themeId);
		if(null != themeSkin && null != themeSkin.getDefaultTheme() && Commons.THEMESKIN_DEFAULT_THEME_0 == themeSkin.getDefaultTheme()){
			throw new BusinessException("默认主题不能删除,请取消后在操作!");
		}
		themeSkin.setState(Commons.THEMESKIN_STATE_TRASH);//更新为软删除状态
		genericDao.update(themeSkin);
	}

	/**
	 * 更改默认主题
	 * @param themeId
	 */
	@Override
	public void updateThemeSkinDefaultTheme(Long themeId) {
		User user = ContextUtil.getUser(User.class);
		if(null == user)
			throw new BusinessException("当前用户未登录");
		if(null == themeId)
			throw new BusinessException("请求错误");
        ThemeSkin themeSkins = genericDao.findT(ThemeSkin.class, "SELECT ID,THEME_TYPE,DEFAULT_THEME FROM T_THEME_SKIN WHERE ID=? AND ORG_ID=?",
            themeId, user.getOrgId());
        //如果此ID是默认主题，那么就是此时的操作是取消默认主题操作
        if(null != themeSkins && 0 == themeSkins.getDefaultTheme()){//取消默认主题操作
            genericDao.execute("UPDATE T_THEME_SKIN SET DEFAULT_THEME=? WHERE ID=? AND ORG_ID=? AND DEFAULT_THEME=? AND THEME_TYPE=?",
                    Commons.THEMESKIN_DEFAULT_THEME_1, themeId, user.getOrgId(), Commons.THEMESKIN_DEFAULT_THEME_0, themeSkins.getThemeType());
        }else {
            //否则就是更改默认主题，先取消默认主题然后根据ID去设备默认主题
            genericDao.execute("UPDATE T_THEME_SKIN SET DEFAULT_THEME=? WHERE ORG_ID=? AND DEFAULT_THEME=? AND THEME_TYPE=?",
                    Commons.THEMESKIN_DEFAULT_THEME_1, user.getOrgId(), Commons.THEMESKIN_DEFAULT_THEME_0, themeSkins.getThemeType());

            genericDao.execute("UPDATE T_THEME_SKIN SET DEFAULT_THEME=? WHERE ORG_ID=? AND ID=? AND DEFAULT_THEME=? AND THEME_TYPE=?",
                    Commons.THEMESKIN_DEFAULT_THEME_0, user.getOrgId(), themeId, Commons.THEMESKIN_DEFAULT_THEME_1, themeSkins.getThemeType());
        }
	}

    /**
     * 实时推送主题
     * @param factoryDevNo
     */
    @Override
    public void updateThemeRealTimePush(String factoryDevNo) {
        User user = ContextUtil.getUser(User.class);
        if(null == user)
            throw new BusinessException("当前用户未登录");

        StringBuffer buf = new StringBuffer();
        List<Object> args = new ArrayList<Object>();
        buf.append(" SELECT DL.DEVICE_STATUS FROM T_DEVICE_RELATION DR");
        buf.append(" LEFT JOIN T_DEVICE_LOG DL ON DL.DEVICE_NO=DR.DEV_NO");
        buf.append(" WHERE DR.FACTORY_DEV_NO=? AND DL.DEVICE_STATUS=?");
        args.add(factoryDevNo);
        args.add(Commons.DEVICE_STATUS_OFFLINE);
        //查询设备是否离线
        DeviceLog deviceState = genericDao.findT(DeviceLog.class, buf.toString(), args.toArray());
        if(null != deviceState)
            throw new BusinessException("设备离线,不能进行推送操作！");
        findThemeDeviceMess(factoryDevNo);

    }

	/**
	 * 更新主题设备状态
	 * @param machineNum 设备组号
	 * @param themeId 主题Id
	 * @param execuTingState 状态值
	 * @return
	 */
	@Override
	public void themeDeviceExecutingState(String machineNum, String themeId, Integer execuTingState) {
		if("-1".equals(themeId)){//如果主题ID为-1，这是恢复默认主题的操作
			//先找出机构ID
			ThemeDevice themeDevice = genericDao.findT(ThemeDevice.class, "SELECT ID,ORG_ID FROM T_THEME_DEVICE WHERE FACTORY_DEV_NO=?", machineNum);
			if(null != themeDevice){
				//再找出默认皮肤ID
				Long skinId = genericDao.findSingle(Long.class, "SELECT ID FROM T_THEME_SKIN WHERE ORG_ID=? AND DEFAULT_THEME=?",
						themeDevice.getOrgId(), Commons.THEMESKIN_DEFAULT_THEME_0);
				//接着更新主题设备的当前主题为默认主题
				genericDao.execute("UPDATE T_THEME_DEVICE SET THEME_ID=?,TOENABLE_ID=?,EXECUTING_STATE=? WHERE ID=?",
						null!=skinId?skinId:null, null, Commons.THEMESKIN_EXECUTING_STATE_3, themeDevice.getId());
			}
		}else{
			ThemeDevice themeDevice = genericDao.findT(ThemeDevice.class, "SELECT * FROM T_THEME_DEVICE WHERE ID=? AND EXECUTING_STATE=?",
					Long.valueOf(themeId), Commons.THEMESKIN_EXECUTING_STATE_1);
			if(null != themeDevice){//如果待应用主题不为空说明是更改主题
				if(null != themeDevice.getToenableId()){
					themeDevice.setThemeId(themeDevice.getToenableId());
					themeDevice.setToenableId(null);
					themeDevice.setExecutingState(execuTingState);
					genericDao.update(themeDevice);
				}else{
					themeDevice.setExecutingState(execuTingState);
					genericDao.update(themeDevice);
				}
			}
		}
	}

	/**
	 * 定时更新主题皮肤
	 */
	@Override
	public void updateThemeStateJob() {
		StringBuffer buf = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		Date date = new Date();
		buf.append(" SELECT FACTORY_DEV_NO,START_TIME,THEME_ID,TOENABLE_ID FROM T_THEME_DEVICE");
		buf.append(" WHERE EXECUTING_STATE=? AND FACTORY_DEV_NO IS NOT NULL");
		args.add(0);
		List<ThemeDevice> themeDevices = genericDao.findTs(ThemeDevice.class, buf.toString(), args.toArray());
		if(null != themeDevices && !themeDevices.isEmpty()){
			for(ThemeDevice themeDevice : themeDevices){
				if(date.getTime() >= themeDevice.getStartTime().getTime() && (null != themeDevice.getThemeId() || null != themeDevice.getToenableId())){
					findThemeDeviceMess(themeDevice.getFactoryDevNo());//推送消息
				}
			}
		}
	}

    /**
     * 获取需要推送的数据
     * @param factoryDevNo
     * @param user
     * @param buf
     * @param args
     */
    private String findThemeDeviceMess(String factoryDevNo) {
        StringBuffer buf = new StringBuffer();
        List<Object> args = new ArrayList<Object>();
        //查询设备是否离线
		buf.append(" SELECT DL.DEVICE_STATUS FROM T_DEVICE_RELATION DR");
		buf.append(" LEFT JOIN T_DEVICE_LOG DL ON DL.DEVICE_NO=DR.DEV_NO");
		buf.append(" WHERE DR.FACTORY_DEV_NO=? AND DL.DEVICE_STATUS=?");
		args.add(factoryDevNo);
		args.add(Commons.DEVICE_STATUS_OFFLINE);
        DeviceLog deviceState = genericDao.findT(DeviceLog.class, buf.toString(), args.toArray());
        String resultMessage = null;
        if (null != deviceState){
            resultMessage = "有设备离线,不能进行推送！";
        }else{
        	//只有在线的设备才能推送
			genericDao.execute("UPDATE T_THEME_DEVICE SET EXECUTING_STATE=? WHERE FACTORY_DEV_NO=? AND EXECUTING_STATE=? AND FACTORY_DEV_NO=?",
					Commons.THEMESKIN_EXECUTING_STATE_1, factoryDevNo, Commons.THEMESKIN_EXECUTING_STATE_0, factoryDevNo);
            //查询是否有待应用的主题
            ThemeDevice theme = genericDao.findT(ThemeDevice.class, "SELECT THEME_ID,TOENABLE_ID FROM T_THEME_DEVICE WHERE FACTORY_DEV_NO=?", factoryDevNo);

            //查询设备主题关系表
            args.clear();
            buf.setLength(0);
            buf.append(" SELECT STRING_AGG (B. ID || ',' || B. NAME || ',' || B. TYPE || ',' || B.REAL_PATH,';'ORDER BY B. TYPE,B. ID DESC) AS IMAGES ,");
            buf.append(" TD.ID, TS.THEME_NAME themeName");
            buf.append(" FROM T_THEME_DEVICE TD");
            if (null != theme.getToenableId()) {
                buf.append(" LEFT JOIN T_THEME_SKIN TS ON TS.ID=TD.TOENABLE_ID AND TS.ORG_ID=TD.ORG_ID");
            } else {
                buf.append(" LEFT JOIN T_THEME_SKIN TS ON TS.ID=TD.THEME_ID AND TS.ORG_ID=TD.ORG_ID");
            }
            buf.append(" LEFT JOIN T_FILE B ON B.INFO_ID=TS.ID AND TYPE=?");
            args.add(Commons.FILE_THEME_TYPE);
            buf.append(" WHERE TD.FACTORY_DEV_NO=?");
            buf.append(" GROUP BY TD.ID, TS.THEME_NAME");
            args.add(factoryDevNo);
            ThemeDevice themeDevice = genericDao.findT(ThemeDevice.class, buf.toString(), args.toArray());
            if (null != themeDevice) {
                themeDevice = splitImages(themeDevice);
                ChangeThemeData changeThemeDate = new ChangeThemeData();
                changeThemeDate.setThemeId(themeDevice.getId());
                changeThemeDate.setThemeUrl(themeDevice.getThemeUrl());
                changeThemeDate.setThemaName(themeDevice.getThemeName());

                ChangeThemeStateData data = new ChangeThemeStateData();
                data.setNotifyFlag(Commons.NOTIFY_THEME_DEVICE);
                data.setTime(new Timestamp(System.currentTimeMillis()));
                pushChangeThemeDeviceMessage(Arrays.asList(factoryDevNo), changeThemeDate, data);
            }
        }
        return resultMessage;
    }

    /**
     * @Title: 截取推送的图片数据
     * @param lotteryProduct
     *            －活动内容对象
     * @return: LotteryProduct
     */
    private ThemeDevice splitImages(ThemeDevice themeDevice) {
        if(null != themeDevice){
            String[] strImage = themeDevice.getImages().split(";");//准备号活动内容的图片
            boolean isSetProduct = false;
            for (String image : strImage) {
                if (isSetProduct)
                    break;
                if (!isSetProduct && Commons.FILE_THEME_TYPE == Integer.valueOf(image.split(",")[2]) && null != image.split(",")[3]) {//主图
                    isSetProduct = true;
                    themeDevice.setThemeUrl(dictionaryService.getFileServer() + image.split(",")[3]);
                }
            }
        }
        return themeDevice;
    }

    /**
     * @Title 主题推送内容
     * @param devNos
     *            －设备组号
     * @param lists
     *            －需推送的信息
     * @param state
     *            －0:下线 ,1上线 void 返回类型
     */
    public void pushChangeThemeDeviceMessage(List<String> devNos, ChangeThemeData changeLotteryData, ChangeThemeStateData data) {
        data.setChangeThemeData(changeLotteryData);
        //主动通知
        MessagePusher pusher = new MessagePusher();
        try {
            LOGGER.info("【主题推送数据格式：设备编号:" + devNos + ",[" + ContextUtil.getJson(data) + "]】");
            pusher.pushMessageToAndroidDevices(devNos, ContextUtil.getJson(data), false);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new BusinessException("主题推送失败！");
        }
    }

	/**
	 * 取消主题推送
	 * @param factoryDevNo
	 */
	@Override
	public void updateCancelTheme(String factoryDevNo) {
		User user = ContextUtil.getUser(User.class);
		if(null == user)
			throw new BusinessException("当前用户未登录");
		if(null == factoryDevNo)
			throw new BusinessException("请求错误");
		ThemeDevice themeDevice = genericDao.findT(ThemeDevice.class, "SELECT ID,EXECUTING_STATE FROM T_THEME_DEVICE WHERE ORG_ID=? AND FACTORY_DEV_NO=?", user.getOrgId(), factoryDevNo);
		if(null != themeDevice && Commons.THEMESKIN_EXECUTING_STATE_0 != themeDevice.getExecutingState())
			throw new BusinessException("主题已推送,不能取消推送!");
		//把未执行的主题变更为已取消
		genericDao.execute("UPDATE T_THEME_DEVICE SET EXECUTING_STATE=? WHERE ORG_ID=? AND ID=?", Commons.THEMESKIN_EXECUTING_STATE_2, user.getOrgId(), themeDevice.getId());
	}

	/**
	 * 查询当前用户的所有店铺
	 * @return
	 */
	public List<PointPlace> findAllPointPlaces() {
		StringBuffer buf = new StringBuffer("SELECT ");
		String cols = SQLUtils.getColumnsSQL(PointPlace.class, "C");
		buf.append(cols);
		buf.append(" FROM T_POINT_PLACE C WHERE C.ORG_ID = ? ");
		List<Object> args = new ArrayList<Object>();
		args.add(ContextUtil.getUser(User.class).getOrgId());
		
		buf.append(" AND C.STATE != ? ");
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
		
		return genericDao.findTs(PointPlace.class, buf.toString(), args.toArray());
	}
	
	/**
	 * 查询指定店铺的广告信息
	 * @return
	 */
	public List<Advertisement> findAdvByPointIdAndType(Long pointId, Advertisement adv) {
		StringBuffer buf = new StringBuffer("SELECT ");
		String cols = SQLUtils.getColumnsSQL(Advertisement.class, "C");
		buf.append(cols);
		buf.append(" FROM T_ADVERTISEMENT C WHERE C.ID IN ( SELECT ADVERTISE_ID FROM T_POINT_ADV WHERE POINT_ID = ? ) AND C.TYPE = ? AND C.ADV_POSITION = ?  ");
		List<Object> args = new ArrayList<Object>();
		args.add(pointId);
		args.add(adv.getType());
		args.add(adv.getAdvPosition());
		
		if (null != adv.getId()) {
			buf.append(" AND C.ID != ? ");
			args.add(adv.getId());
		}

		buf.append(" AND C.STATUS != ? ");
		args.add(Commons.ADV_STATUS_FINISH);
		return genericDao.findTs(Advertisement.class, buf.toString(), args.toArray());
	}
	
	/**
	 * 查询指定店铺的广告信息
	 * @return
	 */
	public List<Advertisement> findAdvByPointId(Long pointId) {
		StringBuffer buf = new StringBuffer("SELECT ");
		String cols = SQLUtils.getColumnsSQL(Advertisement.class, "C");
		buf.append(cols);
		buf.append(" FROM T_ADVERTISEMENT C WHERE C.ID IN ( SELECT ADVERTISE_ID FROM T_POINT_ADV WHERE POINT_ID = ? ) ");
		List<Object> args = new ArrayList<Object>();
		args.add(pointId);
		return genericDao.findTs(Advertisement.class, buf.toString(), args.toArray());
	}

	public List<PointPlace> findPointPlacesByAdvId(Long advId) {
		StringBuffer buf = new StringBuffer("SELECT ");
		String cols = SQLUtils.getColumnsSQL(PointPlace.class, "C");
		buf.append(cols);
		buf.append(" FROM T_POINT_PLACE C WHERE C.ORG_ID = ? AND C.ID IN (SELECT POINT_ID FROM T_POINT_ADV WHERE ADVERTISE_ID = ?) ");
		List<Object> args = new ArrayList<Object>();
		args.add(ContextUtil.getUser(User.class).getOrgId());
		args.add(advId);
		
		buf.append(" AND C.STATE != ? ");
		args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
		
		return genericDao.findTs(PointPlace.class, buf.toString(), args.toArray());
	}

	public PointPlace findPointPlace(Long id) {
		StringBuffer buffer = new StringBuffer("SELECT ");
		buffer.append(SQLUtils.getColumnsSQL(PointPlace.class, "A"));
		buffer.append(" FROM T_POINT_PLACE A WHERE A.ID=? ");
		buffer.append(" AND A.STATE != ? ");
		return genericDao.findT(PointPlace.class, buffer.toString(), id, Commons.POINT_PLACE_STATE_DELETE);
	}

	public Advertisement findScreenAdv(Long id) {
		StringBuffer buffer = new StringBuffer("SELECT ");
		buffer.append(SQLUtils.getColumnsSQL(Advertisement.class, "A"));
		buffer.append(" FROM T_ADVERTISEMENT A WHERE A.ID=? ");
		return genericDao.findT(Advertisement.class, buffer.toString(), id);
	}

	public Advertisement findScreenAdvFile(Long id) {
		Advertisement advertisement = findScreenAdv(id);

		List<Object> args = new ArrayList<>();
		StringBuffer buf = new StringBuffer("SELECT ");
		buf.append(" STRING_AGG(B.ID||','||B.NAME||','||B.TYPE||','||B.REAL_PATH, ';' ORDER BY B.ID DESC) AS images");
		buf.append(" FROM T_FILE B WHERE 1 = 1 ");
		buf.append(" AND B.INFO_ID = ? ");
		args.add(id);
		if (Commons.ADV_POSITION_TOP == advertisement.getAdvPosition()) {
			buf.append(" AND B.TYPE = ? ");
			args.add(Commons.FILE_ADVERTISE_TOP);
		} else if (Commons.ADV_POSITION_BOTTOM == advertisement.getAdvPosition()) {
			buf.append(" AND B.TYPE = ? ");
			args.add(Commons.FILE_ADVERTISE_BOTTOM);
		} else if (Commons.ADV_POSITION_BG == advertisement.getAdvPosition()) {
			buf.append(" AND B.TYPE = ? ");
			args.add(Commons.FILE_ADVERTISE_BG);
		}
		FileStore fileStore = genericDao.findT(FileStore.class, buf.toString(), args.toArray());
		advertisement.setImages(null != fileStore ? fileStore.getImages() : "");

		return advertisement;
	}

}
