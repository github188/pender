package com.vendor.service.impl.share;

import java.sql.Timestamp;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.allinpay.ets.client.StringUtil;
import com.ecarry.core.dao.IGenericDao;
import com.vendor.po.WeUser;
import com.vendor.service.IShareService;
import com.vendor.service.IWechatService;

/**
 * @author zhaoss on 2016年4月15日
 */
@Service("shareService")
public class ShareService implements IShareService {

	private static Logger logger = Logger.getLogger(ShareService.class);
	
	@Autowired
	private IGenericDao genericDao;
	
	@Autowired
	private IWechatService wechatService;
	
	/* (non-Javadoc)
	 * @see com.vendor.service.IShareService#saveWxUser(java.lang.String)
	 */
	@Override
	public boolean saveWxUser(String openId,Long devId,Long orgId, String devNo) {
		if(StringUtil.isEmpty(openId))
			return false;
		int count = genericDao.findSingle(int.class, "SELECT COUNT(*) FROM T_WE_USER WHERE OPEN_ID=?", openId);
		if (count == 0) {
			Map<String,Object> map = wechatService.getWxUser(null, null, openId);
			if(map!=null){
				if(!map.containsKey("unionid"))
					throw new RuntimeException("请先绑定微信公众平台!");
				WeUser user = new WeUser();
				user.setDeviceId(devId);
				user.setDevNo(devNo);
				user.setCreateTime(new Timestamp(System.currentTimeMillis()));
				user.setNickname(map.get("nickname")==null?"":map.get("nickname").toString());
				user.setOpenId(openId);
				user.setUnionId(map.get("unionid").toString());
				user.setOrgId(orgId);
				genericDao.save(user);
				return true;
			}
			
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.vendor.service.IShareService#saveWxUser(java.lang.String)
	 */
	@Override
	public boolean saveWeChatUser(String openId,Long devId,Long orgId, String devNo) {
		if(StringUtil.isEmpty(openId))
			return false;
		int count = genericDao.findSingle(int.class, "SELECT COUNT(*) FROM T_WE_USER WHERE OPEN_ID=?", openId);
		if (count == 0) {
			Map<String,Object> map = wechatService.getWeChatUser(null, null, openId);
			if(map!=null){
				if(!map.containsKey("unionid"))
					throw new RuntimeException("请先绑定微信公众平台!");
				WeUser user = new WeUser();
				user.setDeviceId(devId);
				user.setDevNo(devNo);
				user.setCreateTime(new Timestamp(System.currentTimeMillis()));
				user.setNickname(map.get("nickname")==null?"":map.get("nickname").toString());
				user.setOpenId(openId);
				user.setUnionId(map.get("unionid").toString());
				user.setOrgId(orgId);
				genericDao.save(user);
				return true;
			}
			
		}
		return false;
	}

}