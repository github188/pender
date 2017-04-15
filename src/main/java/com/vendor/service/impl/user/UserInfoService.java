package com.vendor.service.impl.user;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.ecarry.core.dao.IGenericDao;
import com.ecarry.core.dao.SQLUtils;
import com.ecarry.core.exception.BusinessException;
import com.ecarry.core.web.core.ContextUtil;
import com.ecarry.core.web.core.Page;
import com.vendor.po.Device;
import com.vendor.po.DeviceAisle;
import com.vendor.po.User;
import com.vendor.service.IUserInfoService;

@Service("userInfoService")
public class UserInfoService implements IUserInfoService {
	
	@Autowired
	private IGenericDao genericDao;
	
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
	
	/**
	 * type  1: 绑定  0：解绑
	 */
	@Override
	public List<Device> findVenderPartnerDevices(Page page, Device device, String startDevNo, String endDevNo) {
		if (page != null)
			page.setOrder("FACTORY_DEV_NO");
		StringBuffer buf = new StringBuffer("SELECT ");
		List<Object> args = new ArrayList<Object>();
		String cols = SQLUtils.getColumnsSQL(Device.class, "C");
		buf.append(cols);
		buf.append(" ,R.FACTORY_DEV_NO AS FACTORYDEVNO ");
		buf.append(" FROM T_DEVICE C LEFT JOIN T_DEVICE_AISLE DA ON C.ID = DA.DEVICE_ID ");
		buf.append(" LEFT JOIN T_DEVICE_RELATION R ");
		buf.append(" ON R.DEV_NO = C.DEV_NO ");
		buf.append(" WHERE C.ORG_ID=? ");
		args.add(device.getOrgId());

		buf.append(" AND (C.POINT_ID = 0 OR C.POINT_ID IS NULL) ");
		
		if (!StringUtils.isEmpty(startDevNo)) {
			buf.append(" AND R.FACTORY_DEV_NO >= ?");
			args.add(startDevNo);
		}
		if (!StringUtils.isEmpty(endDevNo)) {
			buf.append(" AND R.FACTORY_DEV_NO <= ?");
			args.add(endDevNo);
		}
		
		cols = cols + ",R.FACTORY_DEV_NO";
		buf.append(" GROUP BY ").append(cols);
		return genericDao.findTs(Device.class, page, buf.toString(), args.toArray());
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
	
	public List<DeviceAisle> findDeviceAislesByDevNo(String devNo) {
		StringBuffer buffer = new StringBuffer("SELECT ");
		buffer.append(SQLUtils.getColumnsSQL(DeviceAisle.class, "A"));
		buffer.append(" FROM T_DEVICE_AISLE A  ");
		buffer.append(" LEFT JOIN T_DEVICE D ON D.ID = A.DEVICE_ID ");
		buffer.append(" WHERE D.DEV_NO = ? AND A.PRODUCT_ID IS NOT NULL ");
		return genericDao.findTs(DeviceAisle.class, buffer.toString(), devNo);
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
			buf.append(" UPDATE T_DEVICE SET org_id = ?, update_user=?, update_time = ?, point_id = 0, bind_state = 0 WHERE DEV_NO ").append(inDevNos);
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
	
}
