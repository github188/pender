package com.vendor.service.impl.marketing;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.ecarry.core.dao.IGenericDao;
import com.ecarry.core.dao.SQLUtils;
import com.ecarry.core.exception.BusinessException;
import com.ecarry.core.util.ZHConverter;
import com.ecarry.core.web.core.ContextUtil;
import com.ecarry.core.web.core.Page;
import com.vendor.po.DeviceAisle;
import com.vendor.po.OfflineMessage;
import com.vendor.po.ProductRecommend;
import com.vendor.po.User;
import com.vendor.service.IDictionaryService;
import com.vendor.service.IRecommendService;
import com.vendor.service.IVendingService;
import com.vendor.util.CommonUtil;
import com.vendor.util.Commons;
import com.vendor.util.MessagePusher;
import com.vendor.vo.app.RecommendGoodsInfo;
import com.vendor.vo.app.RecommendPushData;

/**
 * 关联推荐
 * @author liujia on 2016年12月12日
 */
@Service("recommendService")
public class RecommendService implements IRecommendService {
	
	private final static Logger logger = Logger.getLogger(RecommendService.class);
	
	@Autowired
	private IVendingService vendingService;
	
	@Autowired
	private IDictionaryService dictionaryService;

	@Autowired
	private IGenericDao genericDao;
	
	/**
	 * 查看关联推荐列表信息
	 * 
	 * @param productRecommend 关联推荐信息
	 * @param page 分页参数
	 * @return 关联推荐列表信息
	 */
	@Override
	public List<ProductRecommend> findProductRecommends(ProductRecommend productRecommend, Page page) {
		StringBuffer buf = new StringBuffer("SELECT ");
		String cols = SQLUtils.getColumnsSQL(ProductRecommend.class, "PR");
		buf.append(cols);
		buf.append(" ,P.SKU_NAME AS SKUNAME, P2.SKU_NAME AS RECOMMENDSKUNAME ");
		buf.append(" FROM T_PRODUCT_RECOMMEND PR ");
		buf.append(" LEFT JOIN T_PRODUCT P ON P.ID = PR.PRODUCT_ID ");
		buf.append(" LEFT JOIN T_PRODUCT P2 ON P2.ID = PR.RECOMMEND_PRODUCT_ID ");
		buf.append(" WHERE 1=1 ");
		List<Object> args = new ArrayList<Object>();

		User user = ContextUtil.getUser(User.class);
		buf.append(" AND PR.ORG_ID = ? ");
		args.add(user.getOrgId());
		
		if (productRecommend == null) {
			productRecommend = new ProductRecommend();
		}
		if (productRecommend.getSkuName() != null) {
			buf.append(" AND").append(" (P.SKU_NAME LIKE ? OR P.SKU_NAME LIKE ?)");
			args.add("%" + ZHConverter.convert(productRecommend.getSkuName(), ZHConverter.TRADITIONAL) + "%");
			args.add("%" + ZHConverter.convert(productRecommend.getSkuName(), ZHConverter.SIMPLIFIED) + "%");
		}
		if (productRecommend.getRecommendSkuName() != null) {
			buf.append(" AND").append(" (P2.SKU_NAME LIKE ? OR P2.SKU_NAME LIKE ?)");
			args.add("%" + ZHConverter.convert(productRecommend.getRecommendSkuName(), ZHConverter.TRADITIONAL) + "%");
			args.add("%" + ZHConverter.convert(productRecommend.getRecommendSkuName(), ZHConverter.SIMPLIFIED) + "%");
		}
		return genericDao.findTs(ProductRecommend.class, page, buf.toString(), args.toArray());
	}
	
	/**
	 * 更新当前关联度
	 * @param productRecommend
	 */
	public void saveRecommendRelevancy(ProductRecommend productRecommend) {
		if (null == productRecommend || null == productRecommend.getId() || null == productRecommend.getRelevancy())
			throw new BusinessException("非法请求");
		
		genericDao.execute(" UPDATE T_PRODUCT_RECOMMEND SET RELEVANCY = ? WHERE ID = ? ", productRecommend.getRelevancy(), productRecommend.getId());
		
		// 推送以该商品为主的关联度前4位的商品信息
		// 取得当前关联推荐信息
		productRecommend = genericDao.findTById(ProductRecommend.class, productRecommend.getId());
		
		List<RecommendGoodsInfo> recommendGoodsInfos = new ArrayList<RecommendGoodsInfo>();
    	// 取关联度高的前4个
    	List<DeviceAisle> deviceAisles = vendingService.findRecommendGoodList(null, productRecommend.getProductId(), productRecommend.getOrgId(), 4);
    	for (DeviceAisle deviceAisle : deviceAisles) {
    		RecommendGoodsInfo recommendInfo = new RecommendGoodsInfo();
    		recommendInfo.setMainProductNo(deviceAisle.getProductCode());// 主商品编号
    		recommendInfo.setRecomProductNo(deviceAisle.getRecommendProductCode());// 推荐商品编号
    		recommendInfo.setRecomPrice(deviceAisle.getRecomPrice());// 推荐商品价格
    		recommendInfo.setRecomDeletePrice(deviceAisle.getPriceOnLine());// 商品删除价格
    		recommendInfo.setState(deviceAisle.getSellable()); // 商品可售状态  0：不可售  1：可售
    		
    		String images = deviceAisle.getImages();// 推荐商品图片(商品详情图)
			String picUrl = StringUtils.isEmpty(images) ? "" : dictionaryService.getFileServer() + images.split(";")[0].split(",")[3];
			recommendInfo.setRecomPicUrl(picUrl);
			
			recommendGoodsInfos.add(recommendInfo);
		}
    	
    	// 将商品改价通知到指定店铺下的所有设备
		List<Object> devNos = findDeviceNosByOrgId(productRecommend.getOrgId());
		if (null != devNos && devNos.size() > 0) {
			String[] devNoArr = CommonUtil.convertToStringArr(devNos);
			// 通知各设备
			for(String devNo : devNoArr)
				pushRecommendProductMessage(devNo, recommendGoodsInfos);
		}
	}
	
	/**
	 * 推送推荐商品信息
	 */
	public void pushRecommendProductMessage(String devNo, List<RecommendGoodsInfo> lists) {
		RecommendPushData data = new RecommendPushData();
		data.setTime(new Timestamp(System.currentTimeMillis()));
		
		List<List<RecommendGoodsInfo>> fatherlist = CommonUtil.fatherList(lists, 15);
		
		for (List<RecommendGoodsInfo> list : fatherlist) {
			OfflineMessage offlines = new OfflineMessage();
			data.setList(list);
			offlines.setOfflines(ContextUtil.getJson(data));
			offlines.setDevNos(devNo);//设备号
			genericDao.save(offlines);//保存离线时数据
			data.setMessageId(offlines.getId());
			offlines.setOfflines(ContextUtil.getJson(data));
			genericDao.update(offlines);//更新离线时数据字段messageId
			
			String json = ContextUtil.getJson(data);
			if (json != null && !"".equals(json) && json.length() > 1000) {
				genericDao.delete(offlines);

				List<List<RecommendGoodsInfo>> fatherlists = CommonUtil.fatherList(list, 10);
				for (List<RecommendGoodsInfo> list2 : fatherlists) {
					OfflineMessage offline = new OfflineMessage();
					data.setList(list2);
					offline.setOfflines(ContextUtil.getJson(data));
					offline.setDevNos(devNo);// 设备号
					genericDao.save(offline);// 保存离线时数据
					data.setMessageId(offline.getId());
					offline.setOfflines(ContextUtil.getJson(data));
					genericDao.update(offline);// 更新离线时数据字段messageId
					// 主动通知
					MessagePusher pusher = new MessagePusher();
					try {
						logger.info("【推荐商品信息推送数据格式：设备编号:" + devNo + ",[" + ContextUtil.getJson(data) + "]】");
						pusher.pushMessageToAndroidDevices(Arrays.asList(devNo), ContextUtil.getJson(data), true);
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
						throw new BusinessException("推荐商品信息推送失败！");
					}
				}
			} else {
				// 主动通知
				MessagePusher pusher = new MessagePusher();
				try {
					logger.info("【推荐商品信息推送数据格式：设备编号:" + devNo + ",[" + ContextUtil.getJson(data) + "]】");
					pusher.pushMessageToAndroidDevices(Arrays.asList(devNo), ContextUtil.getJson(data), true);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					throw new BusinessException("推荐商品信息推送失败！");
				}
			}
		}
	}
	
	/**
	 * 根据组织ID查询投放的设备编号
	 * @return
	 */
	public List<Object> findDeviceNosByOrgId(Long orgId) {
		StringBuffer buffer = new StringBuffer("");
		List<Object> args = new ArrayList<Object>();
		
		buffer.append(" SELECT DISTINCT(DR.FACTORY_DEV_NO) ");
		buffer.append(" FROM T_DEVICE D ");
		buffer.append(" LEFT JOIN T_DEVICE_RELATION DR ON DR.DEV_NO = D.DEV_NO ");
		buffer.append(" WHERE 1=1 ");
		buffer.append(" AND D.ORG_ID = ? ");
		args.add(orgId);
		buffer.append(" AND D.BIND_STATE = ? ");
		args.add(Commons.BIND_STATE_SUCCESS);
		return genericDao.findListSingle(buffer.toString(), args.toArray());
	}
	
}
