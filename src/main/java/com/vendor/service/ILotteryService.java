package com.vendor.service;

import java.util.List;

import com.ecarry.core.web.core.Page;
import com.vendor.po.Device;
import com.vendor.po.Lottery;
import com.vendor.po.LotteryProduct;
import com.vendor.po.Order;
import com.vendor.po.Product;

/**
 * @ClassName: IIuckyDrawService
 * @Description: 抽奖活动ServiceImpl
 * @author
 * @date 2016年12月12日 下午5:17:56
 */
public interface ILotteryService {

	/**
	 * @Title: 分页查询抽奖活动列表 @param lotteryName @param page @return List<Lottery>
	 * 返回类型 @throws
	 */
	public List<Lottery> findLotteryPage(String lotteryName, Page page);

	/**
	 * @Title:2.删除/下线抽奖活动
	 * @param lotteryId
	 *            活动ID
	 * @param type
	 *            1:删除;2:下线 void 返回类型
	 */
	public void deleteByIdLottery(Long lotteryId, Integer type);

	/**
	 * @Title: 3.获取基本信息
	 * @param lotteryId
	 *            抽奖活动ID
	 * @param page
	 * @return Lottery 返回类型
	 */
	public Lottery findByOrgIdDevice(Long lotteryId);

	/**
	 * @Title 3.1单独获取活动基本信息(不带活动内容的)
	 * @param lotteryId
	 * @return Lottery 返回类型
	 */
	public Lottery findOneLottery(Long lotteryId);

	/**
	 * @Title 4.保存第一步
	 * @param lottery
	 * @param key
	 *            图片INFO_ID值
	 * @param fileIds
	 *            需要删除的图片ID,编辑图片时需要
	 * @return List<Lottery> 返回类型
	 */
	public Lottery saveLottery(Lottery lottery);

	/**
	 * @Title: 5.0获取抽奖活动内容
	 * @param lotteryId
	 *            抽奖活动Id
	 * @param lotteryProductId
	 *            抽奖活动内容Id
	 * @return: List<LotteryProduct>
	 */
	public List<LotteryProduct> findLotteryProduct(Long lotteryId, Long lotteryProductId);

	/**
	 * @Title: 5.1删除活动内容
	 * @param lotteryProductId
	 * @return: void
	 */
	public void deleteLotteryProduct(Long lotteryProductId);

	/**
	 * @Title 5.根据设备号查询出设备的商品
	 * @param factoryDevNo
	 *            出厂设备号
	 * @param lotteryId
	 *            活动Id
	 * @param lotteryProductId
	 *            活动内容Id
	 * @param page
	 * @return List<Product> 返回类型
	 */
	public List<Product> findByDevNoProduct(String factoryDevNo, Long lotteryId, Long lotteryProductId, Page page);

	/**
	 * @Title 6.保存第二步
	 * @param lotteryProduct
	 *            LotteryProduct 返回类型
	 */
	public LotteryProduct saveLotteryDevNoProduct(LotteryProduct lotteryProduct);

	/**
	 * @Title 7.获取抽奖活动期间订单统计
	 * @param lotteryId
	 * @return List<Order> 返回类型
	 */
	public List<Order> findOrderDetail(Long lotteryId);

	/**
	 * @Title 定时抽奖任务 void 返回类型
	 */
	public void updateLotteryStateJob();

	/**
	 * @Title 8.根据活动内容ID和设备组号查询出参与活动的商品信息(订单数据统计中的)
	 * @param lotteryProductId
	 * @param factoryDevNo
	 * @return List<Product> 返回类型
	 */
	public List<Product> findByLotteryIdProduct(Long lotteryProductId, String factoryDevNo, Page page);

	/**
	 * 更新抽奖活动内容 isSucessState
	 * 
	 * @param activityId
	 *            活动商品编号
	 * @param machineNum
	 *            设备组号
	 * @param isSucessState
	 *            活动执行状态 0成功 1 失败
	 * @return
	 */
	public void syncActivityIsSucess(String activityId, String machineNum, String isSucessState);

	/**
	 * @Title: 10.发布抽奖信息
	 * @param lotteryId 抽奖活动ID(推送整个抽奖活动时需带此ID)
	 * @param lotteryProductId 活动内容ID(推送单个设备的单个内容信息)
	 * @param factoryDevNo 活动内容ID(推送单个设备的单个内容信息)
	 * @param type 2:发布(状态位发布中)，3:取消发布(状态是未发布)
	 * @return
	 * @return: Lottery
	 */
	public Lottery updatepushMessageLottery(Long lotteryId, Long lotteryProductId, String factoryDevNo, Integer type);

	/**
	 * @Title: 11.前台定时获取抽奖活动发布状态
	 * @param lotteryId
	 *            活动Id
	 * @return
	 */
	public Lottery findOneLotteryIsPublish(Long lotteryId);

	/**
	 * @Title: 12.导出单个活动内容订单数据
	 * @param page
	 * @param lotteryProductId
	 *            活动内容ID
	 * @return List<Order>
	 */
	public List<Order> findLotteryOrderList(Long lotteryProductId);

	/**
	 * @Title: 13.根据活动Id查询出活动的设备以及设备所参加的活动内容
	 * @param lotteryId
	 * @return List<Device>
	 */
	public List<Device> findLotteryDevNoProduct(Long lotteryId, Page page);


}
