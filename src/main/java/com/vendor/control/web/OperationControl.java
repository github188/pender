package com.vendor.control.web;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.vendor.po.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.ecarry.core.exception.BusinessException;
import com.ecarry.core.web.control.BaseControl;
import com.ecarry.core.web.core.ContextUtil;
import com.ecarry.core.web.core.Page;
import com.ecarry.core.web.view.ExcelView;
import com.vendor.service.IOperationService;
import com.vendor.service.IOrgnizationService;
import com.vendor.util.DateUtil;
import com.vendor.util.ExcelUtil;
import com.vendor.util.FileUtil;

/**
 * 店铺运营控制层
 */
@Controller
@RequestMapping(value = "/operation")
public class OperationControl extends BaseControl {
	
	@Autowired
	private IOperationService operationService;
	
	@Autowired
	private IOrgnizationService orgnizationService;
	
	@RequestMapping(value = "analysis/forward.do", method = RequestMethod.GET)
	public ModelAndView forwardAnalysis() {
		ModelAndView view = new ModelAndView("/operation/analysis.jsp");
		User user = ContextUtil.getUser(User.class);
		view.addObject("_orgName", user.getOrgName());
		view.addObject("_orgId", user.getOrgId());
		return view;
	}
	@RequestMapping(value = "salesPlan/forward.do", method = RequestMethod.GET)
	public ModelAndView forwardSalesPlan() {
		ModelAndView view = new ModelAndView("/operation/salesPlan.jsp");
		User user = ContextUtil.getUser(User.class);
		view.addObject("_orgName", user.getOrgName());
		view.addObject("_orgId", user.getOrgId());
		return view;
	}
	@RequestMapping(value = "storeStock/forward.do", method = RequestMethod.GET)
	public ModelAndView forwardStoreStock() {
		ModelAndView view = new ModelAndView("/operation/storeStock.jsp");
		User user = ContextUtil.getUser(User.class);
		view.addObject("_orgName", user.getOrgName());
		view.addObject("_orgId", user.getOrgId());
		return view;
	}
	
	@RequestMapping(value = "order/forward.do", method = RequestMethod.GET)
	public ModelAndView forwardOrder() {
		ModelAndView view = new ModelAndView("/operation/order.jsp");
		return view;
	}
	
	/**
	 * 查询【经营分析】根据父组织ID查询子组织列表
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "analysis/findOrgnizationsByParentId.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<Orgnization> findOrgnizationsByParentId(Long id) {
		return orgnizationService.findOrgnizationsByParentId(id);
	}
	
	/**
	 * 查询【经营分析】店铺信息（包括点击按钮查询）
	 * @return
	 */
	@RequestMapping(value = "analysis/findStores.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<PointPlace> findAnalysiStores(Page page, PointPlace pointPlace) {
		return operationService.findAnalysiStores(page, pointPlace);
	}
	
	/**
	 * 取得【经营分析】各项累计数据
	 */
	@RequestMapping(value = "analysis/findSysData.json", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> findSysData(PointPlace pointPlace) {
		return operationService.findSysData(pointPlace);
	}
	
	/**
	 * 取得【经营分析】销售统计图
	 */
	@RequestMapping(value = "analysis/findSalesData.json", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> findSalesData(PointPlace pointPlace, Date startDate, Date endDate) {
		return operationService.findSalesData(pointPlace, startDate, endDate);
	}
	
	/**
	 * 导出【经营分析】销售统计数据
	 * @param pointPlace
	 * @return
	 * @throws IOException 
	 */
	@RequestMapping(value = "analysis/exportSalesData.xls", method = RequestMethod.GET)
	public ResponseEntity<byte[]> exportSalesData(Page page, PointPlace pointPlace, Date startDate, Date endDate) throws IOException {
		if (null == page)
			page = new Page();
		page.setCurPage(1);
		page.setPageSize(20000);	//	最大查询2万条
		
		// 取出数据库中的订单信息
		List<Order> exportOrders = operationService.findExportSalesData(page, pointPlace, startDate, endDate);
		
		String excelUrl = getExcelFilePath();
		logger.info("*****getExcelFilePath:*****" + excelUrl);
		String tempUrl = getExcelFilePath() + "temp" + File.separator;
		if (StringUtils.isEmpty(excelUrl) || StringUtils.isEmpty(tempUrl)) {
			logger.info("*****excel模板不存在*****");
			throw new BusinessException("excel模板不存在");
		}
		String templateSrcFilePath = excelUrl + "salesDataOrders.xls";
		String destFileName = "销售统计数据" + DateUtil.getCurDateByPattern(DateUtil.YYYYMMDD_EN) + ".xls";
		String destFilePath = tempUrl + destFileName;
		logger.info("*****templateSrcFilePath:*****" + templateSrcFilePath);
		logger.info("*****destFilePath:*****" + destFilePath);
		
		Map<String, Object> beanParams = new HashMap<String, Object>();
		beanParams.put("orders", exportOrders);
		
		ExcelUtil.createExcel(templateSrcFilePath, beanParams, destFilePath);
		
		return FileUtil.download(destFileName, new File(destFilePath));
	}
	
	/**
	 * 取得【经营分析】商品销售饼状图
	 */
	@RequestMapping(value = "analysis/findProductSalesPieChartData.json", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> findProductSalesPieChartData(PointPlace pointPlace, Date startDate, Date endDate) {
		return operationService.findProductSalesPieChartData(pointPlace, startDate, endDate);
	}

	/**
	 * 取得【经营分析】商品销售数据
	 */
	@RequestMapping(value = "analysis/findProductSalesData.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<Order> findProductSalesData(Page page, PointPlace pointPlace, Date startDate, Date endDate) {
		return operationService.findProductSalesData(page, pointPlace, startDate, endDate);
	}
	
	/**
	 * 导出【经营分析】商品销售数据
	 * @param pointPlace
	 * @return
	 * @throws IOException 
	 */
	@RequestMapping(value = "analysis/exportProductSalesData.xls", method = RequestMethod.GET)
	public ResponseEntity<byte[]> exportProductSalesData(Page page, PointPlace pointPlace, Date startDate, Date endDate) throws IOException {
		if (null == page)
			page = new Page();
		page.setCurPage(1);
		page.setPageSize(20000);	//	最大查询2万条
		
		// 取出数据库中的订单信息
		List<Order> exportOrders = operationService.findExportProductSalesData(page, pointPlace, startDate, endDate);
		
		String excelUrl = getExcelFilePath();
		logger.info("*****getExcelFilePath:*****" + excelUrl);
		String tempUrl = getExcelFilePath() + "temp" + File.separator;
		if (StringUtils.isEmpty(excelUrl) || StringUtils.isEmpty(tempUrl)) {
			logger.info("*****excel模板不存在*****");
			throw new BusinessException("excel模板不存在");
		}
		String templateSrcFilePath = excelUrl + "productSalesDataOrders.xls";
		String destFileName = "商品销售数据" + DateUtil.getCurDateByPattern(DateUtil.YYYYMMDD_EN) + ".xls";
		String destFilePath = tempUrl + destFileName;
		logger.info("*****templateSrcFilePath:*****" + templateSrcFilePath);
		logger.info("*****destFilePath:*****" + destFilePath);
		
		Map<String, Object> beanParams = new HashMap<String, Object>();
		beanParams.put("orders", exportOrders);
		
		ExcelUtil.createExcel(templateSrcFilePath, beanParams, destFilePath);
		
		return FileUtil.download(destFileName, new File(destFilePath));
	}

	/**
	 * 查询【店铺库存】店铺信息
	 * @return
	 */
	@RequestMapping(value = "storeStock/findStores.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<PointPlace> findStockStores(Page page, PointPlace pointPlace) {
		return operationService.findStockStores(page, pointPlace);
	}
	
	/**
	 * 查询【店铺库存】店铺商品补货信息
	 * @return
	 */
	@RequestMapping(value = "storeStock/findReplenishProds.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<DeviceAisle> findReplenishProds(Page page, String ids) {
		if (StringUtils.isEmpty(ids))
			throw new BusinessException("店铺信息不完整");
		
		String[] storeIds = ids.split(",");
		Long[] finalStoreIds = new Long[storeIds.length];
		for (int i = 0; i < storeIds.length; i++)
			finalStoreIds[i] = Long.valueOf(storeIds[i]);
		
		return operationService.findReplenishProds(page, finalStoreIds);
	}

	/**
	 * 根据组号查询本设备组上的商品(包括实物、虚拟商品)
	 * @param factoryDevNo
	 * @return
	 */
	@RequestMapping(value = "storeStock/findProductByFactoryDevNo.json")
	@ModelAttribute("rows")
	public List<DeviceAisle> findProductByFactoryDevNo(String factoryDevNo){
		return operationService.findProductByFactoryDevNo(factoryDevNo);
	}

	/**
	 * @Title: 更新【店铺库存】商品的排序序号
	 * @param cabId
	 *            货柜ID(都需要)
	 * @param productId
	 *            商品ID(都需要)
	 * @param serialNumber
	 *            排序序号
	 * @param stickType
	 *            (置顶状态:1置顶,2取消置顶)
	 * @return: void
	 */
	@RequestMapping(value = "storeStock/updateDeviceAisleSerialNumber.json")
	public void updateDeviceAisleSerialNumber(Long cabId, Long productId, Integer serialNumber, Integer stickType) {
		if (null == cabId || null == productId)
			throw new BusinessException("非法请求");


		operationService.updateDeviceAisleSerialNumber(cabId, productId, serialNumber, stickType);
	}


	/**
	 * 【店铺库存】更新商品序号
	 * @param sortingRecommendeds 排序对象
	 * @param factoryDevNo 设备组号
	 */
	@RequestMapping(value = "storeStock/updateSortingRecommended.json")
	public void updateSortingRecommended(List<SortingRecommended> sortingRecommendeds){
		operationService.updateSortingRecommended(sortingRecommendeds);
	}


	
	/**
	 * 导出【店铺库存】店铺补货清单信息
	 * @param pointPlace
	 * @return
	 * @throws IOException 
	 */
	@RequestMapping(value = "storeStock/exportStoreProds.xls", method = RequestMethod.GET)
	public ResponseEntity<byte[]> exportStoreProds(String ids) throws IOException {
		if (StringUtils.isEmpty(ids))
			throw new BusinessException("店铺信息不完整");
		
		String[] storeIds = ids.split(",");
		Long[] finalStoreIds = new Long[storeIds.length];
		for (int i = 0; i < storeIds.length; i++)
			finalStoreIds[i] = Long.valueOf(storeIds[i]);
		
		if (finalStoreIds == null || finalStoreIds.length <= 0)
			throw new BusinessException("请指定需要导出的设备");
		
		List<PointPlace> pointPlaces = operationService.findStoreProds(finalStoreIds);
		
		String excelUrl = getExcelFilePath();
		logger.info("*****getExcelFilePath:*****" + excelUrl);
		String tempUrl = getExcelFilePath() + "temp" + File.separator;
		if (StringUtils.isEmpty(excelUrl) || StringUtils.isEmpty(tempUrl)) {
			logger.info("*****excel模板不存在*****");
			throw new BusinessException("excel模板不存在");
		}
		String templateSrcFilePath = excelUrl + "storeProducts.xls";
		String destFilePath = tempUrl + DateUtil.getCurDateByPattern(DateUtil.YYYYMMDD_EN) + "店铺补货计划.xls";
		logger.info("*****templateSrcFilePath:*****" + templateSrcFilePath);
		logger.info("*****destFilePath:*****" + destFilePath);

		Map<String, Object> beanParams = new HashMap<String, Object>();
		beanParams.put("pointPlaces", pointPlaces);

		ExcelUtil.createExcel(templateSrcFilePath, beanParams, destFilePath);
		
		return FileUtil.download(DateUtil.getCurDateByPattern(DateUtil.YYYYMMDD_EN) + "店铺补货计划.xls", new File(destFilePath));
	}
	
	public String getExcelFilePath() {
		return this.getClass().getResource("/").getPath() + File.separator + "template" + File.separator + "excel" + File.separator;
	}
	
	/**
	 * 导出【店铺库存】仓库发货总表信息
	 * @param pointPlace
	 * @return
	 */
	@RequestMapping(value = "storeStock/exportAllProds.xls", method = RequestMethod.GET)
	public ModelAndView exportAllProds(String ids) {
		if (StringUtils.isEmpty(ids))
			throw new BusinessException("店铺信息不完整");
		
		String[] storeIds = ids.split(",");
		Long[] finalStoreIds = new Long[storeIds.length];
		for (int i = 0; i < storeIds.length; i++)
			finalStoreIds[i] = Long.valueOf(storeIds[i]);
		
		if (finalStoreIds == null || finalStoreIds.length <= 0)
			throw new BusinessException("请指定需要导出的设备");
		
		Page page = new Page();
		page.setCurPage(1);
		page.setPageSize(20000);	//	最大查询2万条
		List<DeviceAisle> deviceAisles = operationService.findReplenishProds(page, finalStoreIds);

		Map<String, String> titleMap = new LinkedHashMap<String, String>();
		titleMap.put("商品名称", "productName");
		titleMap.put("应补货数量", "totalSupplementNo");
		titleMap.put("货道容量", "totalCapacity");
		ExcelView view = new ExcelView(deviceAisles, titleMap, DateUtil.getCurDateByPattern(DateUtil.YYYYMMDD_EN) + "仓库发货总表");
		return new ModelAndView(view);
	}
	
	/**
	 * 【店铺库存】店铺一键改价
	 * @param onSale 是否打折  0：不打折  1：打折
	 * @param discount 折扣
	 * @param ids 店铺ID
	 * @return
	 */
	@RequestMapping(value = "storeStock/saveStoresPrice.json", method = RequestMethod.POST)
	public void saveStoresPrice(@RequestBody PointPlace pointPlace) {
		operationService.saveStoresPrice(pointPlace);
	}
	
	/**
	 * 【店铺库存】-【商品一键改价】查询店铺商品信息
	 * @return
	 */
	@RequestMapping(value = "storeStock/findAllProds.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<DeviceAisle> findAllProds(Page page, Product product) {
		return operationService.findAllProds(page, product);
	}
	
	/**
	 * 【店铺库存】-【商品一键改价】查询商品所在的店铺信息
	 * @return
	 */
	@RequestMapping(value = "storeStock/findStoresByProdId.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<PointPlace> findStoresByProdId(Page page, String productIds) {
		if (StringUtils.isEmpty(productIds))
			throw new BusinessException("商品信息不完整");
		
		String[] productIdArr = productIds.split(",");
		Long[] finalProductIds = new Long[productIdArr.length];
		for (int i = 0; i < productIdArr.length; i++)
			finalProductIds[i] = Long.valueOf(productIdArr[i]);
		
		return operationService.findStoresByProdId(page, finalProductIds);
	}
	
	/**
	 * 【店铺库存】商品一键改价
	 * @return
	 */
	@RequestMapping(value = "storeStock/saveProdsPrice.json", method = RequestMethod.POST)
	public void saveProdsPrice(@RequestBody DeviceAisle deviceAisle) {
		operationService.saveProdsPrice(deviceAisle);
	}
	
	/**
	 * 【店铺库存】根据店铺查询店铺下的货柜信息
	 */
	@RequestMapping(value = "storeStock/findCabinetsByStoreId.json", method = RequestMethod.POST)
	public List<Cabinet> findCabinetsByStoreId(Long storeId) {
		if (null == storeId)
			throw new BusinessException("非法请求");
		
		return operationService.findCabinetsByStoreId(storeId);
	}
	
	 /**
     * @Title: 根据货柜ID查询出所属设备的商品（包括副柜商品）
     * @param page
     * @param cabId
     * @return
     * @return: List<DeviceAisle>
     */
    @RequestMapping(value = "storeStock/findCabinetProductByCabId.json")
    public List<DeviceAisle> findCabinetProductByCabId(Long cabId, Page page) {
        if (null == cabId)
            throw new BusinessException("非法请求");

        return operationService.findReplenishProdsByCabId(page, cabId);
    }

	/**
	 * 【店铺库存】查询货柜商品补货信息
	 * @return
	 */
	@RequestMapping(value = "storeStock/findReplenishProdsByCabId.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<DeviceAisle> findReplenishProdsByCabId(Page page, Long cabId) {
		if (null == cabId)
			throw new BusinessException("非法请求");
		
		return operationService.findReplenishProdsByCabId(page, cabId);
	}
	
	/**
	 * 查询【订单信息】
	 * @return
	 */
	@RequestMapping(value = "order/find.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<Order> findOrders(Page page, Order order, Date startDate, Date endDate) {
		return operationService.findOrders(page, order, startDate, endDate);
	}

	/**
	 * 查询【订单退款明细信息】
	 * @return
	 */
	@RequestMapping(value = "order/findRefundDetail.json", method = RequestMethod.POST)
	public List<Refund> findRefundDetail(Refund refund) {
		return operationService.findRefundDetail(refund);
	}
	
	/**
	 * 订单退款
	 * @param refund
	 * @throws Exception 
	 */
	@RequestMapping(value = "order/auditOrderRefund.json", method = RequestMethod.POST)
	public void auditOrderRefund(Refund refund) {
		try {
			User user = ContextUtil.getUser(User.class);
			if (null == user)
				throw new BusinessException("当前用户未登录");
			
			operationService.saveRefund(refund, user);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new BusinessException("退款失败：" + e.getMessage());
		}
	}

	/**
	 * 查询订单退款信息
	 * @param refund
	 * @throws Exception 
	 */
	@RequestMapping(value = "order/findOrderRefund.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<Refund> findOrderRefund(Refund refund) {
		return operationService.findOrderRefund(refund);
	}
	
	/**
	 * 导出
	 * @param pointPlace
	 * @return
	 * @throws IOException 
	 */
	@RequestMapping(value = "order/export.xls", method = RequestMethod.GET)
	public ResponseEntity<byte[]> export(Page page, Order order, Date startDate, Date endDate) throws IOException {
		// 取出数据库中的订单信息
		List<OrderDetail> exportOrders = operationService.findExportOrders(page, order, startDate, endDate);
		
		List<OrderDetail> finalOrders = new ArrayList<OrderDetail>();
		if (null != exportOrders && !exportOrders.isEmpty()) {
			// 按设备组号分组
			Map<String ,List<OrderDetail>> ordersMap = group(exportOrders, new GroupBy<String>(){
				@Override
				public String groupby(Object obj) {
					OrderDetail rule = (OrderDetail)obj;
					return rule.getFactoryDevNo();
				}
			});
			
			// 遍历分组后的Map
			for (Map.Entry<String ,List<OrderDetail>> entry : ordersMap.entrySet()) {
				List<OrderDetail> orders = entry.getValue();
				
				Collections.sort(orders, new Comparator<OrderDetail>() {
					public int compare(OrderDetail arg0, OrderDetail arg1) {
						return arg1.getPayTime().compareTo(arg0.getPayTime());
					}
				});
				
				finalOrders.addAll(orders);
			}
		}
		
		
		String excelUrl = getExcelFilePath();
		logger.info("*****getExcelFilePath:*****" + excelUrl);
		String tempUrl = getExcelFilePath() + "temp" + File.separator;
		if (StringUtils.isEmpty(excelUrl) || StringUtils.isEmpty(tempUrl)) {
			logger.info("*****excel模板不存在*****");
			throw new BusinessException("excel模板不存在");
		}
		String templateSrcFilePath = excelUrl + "deviceOrders.xls";
		String destFilePath = tempUrl + DateUtil.getCurDateByPattern(DateUtil.YYYYMMDD_EN) + "设备销售信息.xls";
		logger.info("*****templateSrcFilePath:*****" + templateSrcFilePath);
		logger.info("*****destFilePath:*****" + destFilePath);
		
		Map<String, Object> beanParams = new HashMap<String, Object>();
		beanParams.put("orders", finalOrders);
		
		ExcelUtil.createExcel(templateSrcFilePath, beanParams, destFilePath);
		
		return FileUtil.download("设备销售信息.xls", new File(destFilePath));
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
        if(colls == null || colls.isEmpty()) {
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
	
}
