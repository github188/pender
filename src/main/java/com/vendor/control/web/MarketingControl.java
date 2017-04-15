package com.vendor.control.web;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.ecarry.core.exception.BusinessException;
import com.ecarry.core.web.control.BaseControl;
import com.ecarry.core.web.core.Page;
import com.vendor.po.Device;
import com.vendor.po.Discount;
import com.vendor.po.Lottery;
import com.vendor.po.LotteryProduct;
import com.vendor.po.Order;
import com.vendor.po.PointPlace;
import com.vendor.po.Product;
import com.vendor.po.ProductRecommend;
import com.vendor.service.IDiscountService;
import com.vendor.service.ILotteryService;
import com.vendor.service.IRecommendService;
import com.vendor.util.DateUtil;
import com.vendor.util.ExcelUtil;
import com.vendor.util.FileUtil;

/**
* @ClassName: 营销工具Controller
* @Description: TODO
* @author: duanyx
* @date: 2016年11月17日 下午3:19:01
*/
@Controller
@RequestMapping(value= "/marketing")
public class MarketingControl extends BaseControl{
	
	@Autowired
	private IDiscountService discountService;

	@Autowired
	private IRecommendService recommendService;
	
	@Autowired
	private ILotteryService lotteryService;
	

	/********************************************************  【打折活动】 start  ********************************************************************/
	
	/**
	 * @Title: *0-打折首页跳转
	 * @return
	 * @return: ModelAndView
	 */
	@RequestMapping(value = "/discount/forward.do", method = RequestMethod.GET)
	public ModelAndView index(){
		return new ModelAndView("/marketing/disCount.jsp");
	}
	
	/**
	 * @Title: *1-分页查询限时打折列表
	 * @return
	 * @return: ModelAndView
	 */
	@RequestMapping(value = "/discount/findDisCount.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<Discount> findDisCount(Discount marketing, Page page){
		return discountService.findDisCount(marketing, page);
	}
	
	/**
	 * @Title: *2-删除/下线打折活动
	 * @param ids 要删除的Id
	 * @param index 类型  3:下线,9:删除
	 * @return: void
	 */
	@RequestMapping(value = "/discount/deleteDiscount.json", method = RequestMethod.POST)
	public void deleteDiscount(Long ids, Integer index){
		discountService.delDiscount(ids, index);
	}

	/**
	 * @Title: *3-1.第一步保存新增/编辑对象(保存打折基本信息)
	 * @param discount
	 * @return void
	 * @return: Discount
	 */
	@RequestMapping(value = "/discount/saveDiscount.json", method = RequestMethod.POST)
	public Discount saveDiscount(@RequestBody Discount discount){
		return discountService.saveDiscount(discount);
	}
	
	/**
	 * @Title: *4.-选择店铺时(查询店铺)
	 * @param discount
	 * @param productId 查商品已选店铺时需要传字段
	 * @return
	 * @return: List<PointPlace>
	 */
	@RequestMapping(value = "/discount/findPointPlacePage.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<PointPlace> findPointPlacePage(Discount discount, Long productId, Page page){
		return discountService.findPointPlacePage(discount, productId, page);
	}
	
	/**
	 * @Title: *5.-选择商品时(查询商品)
	 * @param discount
	 * @param page
	 * @param type 区分是查(1：已选的)还是(2：未选的)
	 * @return
	 * @return: List<Product>
	 */
	@RequestMapping(value = "/discount/findProductPage.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<Product> findProductPage(Discount discount, Page page, Integer type){
		return discountService.findProductPage(discount, page, type);
	}
	
	/**
	 * @Title: *6-1保存第二步(保存商铺/商品)
	 * @param discount
	 * @return
	 * @return: void
	 */
	@RequestMapping(value = "/discount/savePointPlaceProduct.json", method = RequestMethod.POST)
	public void savePointPlaceProduct(Long id, String[] pointPlaceList, String type, String[] productList){
		discountService.savePointPlaceProduct(id, pointPlaceList, type, productList);
	}
	
	
	/**
	 * @Title: *7.-删除选中商品(以及商品对应的商铺)
	 * @param @param discountId  删除打折活动的商品需要带
	 * @param @param productId 删除打折活动的商品需要带  
	 * @return void
	 * @throws
	 */
	@RequestMapping(value = "/discount/deleteProductPointPlace.json", method = RequestMethod.POST)
	public void deleteProductPointPlace(Long discountId, Long productId){
		discountService.deleteProductPointPlace(discountId, productId);
	}
	
	/**
	 * @Title: *7-1.更新商品的(店铺)
	 * @param @param id
	 * @param @param productId
	 * @param @param pointPlaceList
	 * @return void
	 * @throws
	 */
	@RequestMapping(value = "/discount/updateProduct.json", method = RequestMethod.POST)
	public void updateProduct(@RequestBody Discount discount){
		discountService.updateProduct(discount);
	}

	/**
	 * @Title: *8.-删除店铺
	 * @param @param discountId
	 * @param @param pointPlaceId
	 * @return void
	 * @throws
	 */
	@RequestMapping(value = "/discount/delectPointPlace.json", method = RequestMethod.POST)
	public void delectPointPlace(Long discountId, Long pointPlaceId){
		discountService.delectPointPlace(discountId, pointPlaceId);
	}
	
	/**
	 * @Title: *9.-这里是根据活动ID查询出订单数据
	 * @param @param discountId
	 * @param @return
	 * @return List<Order>
	 */
	@RequestMapping(value = "/discount/findOrderPage.json", method = RequestMethod.POST)
	public Order findOrderPage(Long discountId){
		return discountService.findOrderPage(discountId);
	}
	
	
	/********************************************************  【打折活动】 end  ********************************************************************/
	
	
	/********************************************************  【抽奖活动】 start  ********************************************************************/
	/**
	 * @Title: *10-抽奖活动跳转
	 * @return
	 * @return: ModelAndView
	 */
	@RequestMapping(value = "/lottery/forward.do", method = RequestMethod.GET)
	public ModelAndView forwardLottery(){
		return new ModelAndView("/marketing/lottery.jsp");
	}
	
	
	/** 
	 * @Title:1.分页查询抽奖活动列表
	 * @param lotteryName 抽奖活动名称
	 * @param page
	 * @return  List<Lottery> 返回类型 
	 */
	@RequestMapping(value = "/lottery/findLotteryPage.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<Lottery> findLotteryPage(String lotteryName, Page page){
		return lotteryService.findLotteryPage(lotteryName, page);
	}
	
	/** 
	 * @Title:2.删除/下线抽奖活动
	 * @param lotteryId 活动ID
	 * @param type 1:删除;2:下线
	 * void 返回类型 
	 */
	@RequestMapping(value = "/lottery/deleteByIdLottery.json", method = RequestMethod.POST)
	public void deleteByIdLottery(Long lotteryId, Integer type){
		lotteryService.deleteByIdLottery(lotteryId, type);
	}
	
	/** 
	 * @Title: 3.获取基本信息
	 * @param lotteryId 抽奖活动ID
	 * @param page
	 * @return
	 * Lottery 返回类型 
	 */
	@RequestMapping(value = "/lottery/findByOrgIdDevice.json", method = RequestMethod.POST)
	public Lottery findByOrgIdDevice(Long lotteryId){
		return lotteryService.findByOrgIdDevice(lotteryId);
	}
	
	/** 
	 * @Title 3.1单独获取活动基本信息(不带活动内容的)
	 * @param lotteryId
	 * @return
	 * Lottery 返回类型 
	 */
	@RequestMapping(value = "/lottery/findOneLottery.json", method = RequestMethod.POST)
	public Lottery findOneLottery(Long lotteryId){
		return lotteryService.findOneLottery(lotteryId);
	}
	
	/** 
	 * @Title 4.保存第一步
	 * @param lottery 
	 * @param key 图片INFO_ID值
	 * @param fileIds 需要删除的图片ID,编辑图片时需要
	 * @return
	 * List<Lottery> 返回类型 
	 */
	@RequestMapping(value = "/lottery/saveLottery.json", method = RequestMethod.POST)
	public Lottery saveLottery(@RequestBody Lottery lottery){
		return lotteryService.saveLottery(lottery);
	}
	
	
	/**
	 * @Title: 5.0获取抽奖活动内容
	 * @Description: TODO
	 * @param lotteryId 抽奖活动Id
	 * @param lotteryProductId 抽奖活动内容Id
	 * @return
	 * @return: List<LotteryProduct>
	 */
	@RequestMapping(value = "/lottery/findLotteryProduct.json", method = RequestMethod.POST)
	public List<LotteryProduct> findLotteryProduct(Long lotteryId, Long lotteryProductId){
		return lotteryService.findLotteryProduct(lotteryId, lotteryProductId);
	}
	
	/** 
	 * @Title 5.1删除活动内容
	 * @param lotteryProductId
	 * void 返回类型 
	 */
	@RequestMapping(value = "/lottery/deleteLotteryProduct.json", method = RequestMethod.POST)
	public void deleteLotteryProduct(Long lotteryProductId){
		lotteryService.deleteLotteryProduct(lotteryProductId);
	}
	
	/** 
	 * @Title 5.根据设备号查询出设备的商品
	 * @param factoryDevNo 出厂设备号
	 * @param lotteryId 活动Id
	 * @param lotteryProductId 活动内容Id
	 * @param page
	 * @return
	 * List<Product> 返回类型 
	 */
	@RequestMapping(value = "/lottery/findByDevNoProduct.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<Product> findByDevNoProduct(String factoryDevNo, Long lotteryId, Long lotteryProductId, Page page){
		return lotteryService.findByDevNoProduct(factoryDevNo, lotteryId, lotteryProductId, page);
	}
	
	
	/** 
	 * @Title 6.保存第二步 
	 * @param lotteryProduct 保存抽奖活动内容
	 *  LotteryProduct 返回类型 
	 */
	@RequestMapping(value = "/lottery/saveLotteryDevNoProduct.json", method = RequestMethod.POST)
	public LotteryProduct saveLotteryDevNoProduct(@RequestBody LotteryProduct lotteryProduct){
		return lotteryService.saveLotteryDevNoProduct(lotteryProduct);
	}
	
	/** 
	 * @Title 7.获取抽奖活动期间订单统计
	 * @param lotteryId
	 * @return
	 * List<Order> 返回类型 
	 */
	@RequestMapping(value = "/lottery/findOrderDetail.json", method = RequestMethod.POST)
	public List<Order> findOrderDetail(Long lotteryId){
		return lotteryService.findOrderDetail(lotteryId);
	}
	

	/** 
	 * @Title 8.根据活动内容ID和设备组号查询出参与活动的商品信息(订单数据统计中的)
	 * @param lotteryProductId 
	 * @param factoryDevNo
	 * @return
	 * List<Product> 返回类型 
	 */
	@RequestMapping(value = "/lottery/findByLotteryIdProduct.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<Product> findByLotteryIdProduct(Long lotteryProductId, String factoryDevNo, Page page){
		return lotteryService.findByLotteryIdProduct(lotteryProductId, factoryDevNo, page);
	}
	
	
	/**
	 * @Title: 10.发布抽奖信息
	 * @param lotteryId 抽奖活动ID(推送整个抽奖活动时需带此ID)
	 * @param lotteryProductId 活动内容ID(推送单个设备的单个内容信息)
	 * @param factoryDevNo 活动内容ID(推送单个设备的单个内容信息)
	 * @param type 2:发布(状态位发布中)，3:取消发布(状态是未发布)
	 * @return
	 * @return: Lottery
	 */
	@RequestMapping(value = "/lottery/updatepushMessageLottery.json")
	public Lottery updatepushMessageLottery(Long lotteryId, Long lotteryProductId, String factoryDevNo, Integer type){
		return lotteryService.updatepushMessageLottery(lotteryId, lotteryProductId, factoryDevNo, type);
	}
	
	/** 
	 * @Title: 11.前台定时获取抽奖活动发布状态
	 * @param lotteryId 活动Id
	 * @return  
	 */
	@RequestMapping(value = "/lottery/findOneLotteryIsPublish.json", method = RequestMethod.POST)
	public Lottery findOneLotteryIsPublish(Long lotteryId){
		return lotteryService.findOneLotteryIsPublish(lotteryId);
	}
	
	
	/** 
	 * @Title: 12.导出单个活动内容订单数据
	 * @param lotteryProductId 活动内容ID
	 */
	@RequestMapping(value = "/lottery/findOneExportExcel.xls", method = RequestMethod.GET)
	public ResponseEntity<byte[]> findOneExportExcel(Long lotteryProductId) throws IOException {
		if (null == lotteryProductId)
			throw new BusinessException("店铺信息不完整");
		List<Order> orderList = lotteryService.findLotteryOrderList(lotteryProductId);
		String excelUrl = getExcelFilePath();
		logger.info("*****getExcelFilePath:*****" + excelUrl);
		String tempUrl = getExcelFilePath() + "temp" + File.separator;
		if (StringUtils.isEmpty(excelUrl) || StringUtils.isEmpty(tempUrl)) {
			logger.info("*****excel模板不存在*****");
			throw new BusinessException("excel模板不存在");
		}
		
		String templateSrcFilePath = excelUrl + "deviceOrders.xls";
		String destFilePath = tempUrl + DateUtil.getCurDateByPattern(DateUtil.YYYYMMDD_EN) + "抽奖活动内容订单信息.xls";
		logger.info("*****templateSrcFilePath:*****" + templateSrcFilePath);
		logger.info("*****destFilePath:*****" + destFilePath);
		
		Map<String, Object> beanParams = new HashMap<String, Object>();
		beanParams.put("orders", orderList);
		
		ExcelUtil.createExcel(templateSrcFilePath, beanParams, destFilePath);
		
		return FileUtil.download("抽奖活动内容订单信息.xls", new File(destFilePath));
	}
	
	
	public String getExcelFilePath() {
		return this.getClass().getResource("/").getPath() + File.separator + "template" + File.separator + "excel" + File.separator;
	}
	
	
	/**
	 * @Title: 13.根据活动Id查询出活动的设备以及设备所参加的活动内容
	 * @param lotteryId
	 * @return List<Device>
	 */
	@RequestMapping(value = "/lottery/findLotteryDevNoProduct.json")
	@ModelAttribute("rows")
	public List<Device> findLotteryDevNoProduct(Long lotteryId, Page page){
		return lotteryService.findLotteryDevNoProduct(lotteryId, page);
	}
	
	/********************************************************  【抽奖活动】  end  ********************************************************************/
	
	
	
	
	
	/********************************************************  【关联推荐】 start  ********************************************************************/
	
	/**
	 * @Title: *11-关联推荐跳转
	 * @return
	 * @return: ModelAndView
	 */
	@RequestMapping(value = "/recommendation/forward.do", method = RequestMethod.GET)
	public ModelAndView forwardRecommendation() {
		return new ModelAndView("/marketing/recommendation.jsp");
	}
	
	/**
	 * 查看关联推荐列表信息
	 * 
	 * @param productRecommend 关联推荐信息
	 * @param page 分页参数
	 * @return 关联推荐列表信息
	 */
	@RequestMapping(value = "/recommendation/findProductRecommends.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<ProductRecommend> findProductRecommends(ProductRecommend productRecommend, Page page){
		return recommendService.findProductRecommends(productRecommend, page);
	}

	/**
	 * 更新当前关联度
	 * @param productRecommend
	 */
	@RequestMapping(value = "/recommendation/saveRecommendRelevancy.json", method = RequestMethod.POST)
	public void saveRecommendRelevancy(ProductRecommend productRecommend){
		recommendService.saveRecommendRelevancy(productRecommend);
	}
	
	
	
	
	
	
	/********************************************************  【关联推荐】 end  ********************************************************************/
	
}
