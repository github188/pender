package com.vendor.control.web;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.ecarry.core.web.control.BaseControl;
import com.ecarry.core.web.core.ContextUtil;
import com.ecarry.core.web.core.Page;
import com.ecarry.core.web.view.ExcelView;
import com.vendor.po.Cabinet;
import com.vendor.po.Category;
import com.vendor.po.Device;
import com.vendor.po.DeviceAisle;
import com.vendor.po.Orgnization;
import com.vendor.po.PointPlace;
import com.vendor.po.Product;
import com.vendor.po.User;
import com.vendor.service.IProductService;
import com.vendor.util.Commons;


@Controller
@RequestMapping(value = "/product")
public class ProductControl extends BaseControl {

	@Autowired
	private IProductService productService;

	@RequestMapping(value = "issue/forward.do")
	public ModelAndView forwardProduct(Long id) {
		ModelAndView view = new ModelAndView("/product/issue.jsp");
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
	
	/**
	 * 查询当前设备下的货柜信息
	 * @return
	 */
	@RequestMapping(value = "shelf/findCabinets.json", method = RequestMethod.POST)
	public List<Cabinet> findCabinets(Long deviceId) {
		return productService.findCabinets(deviceId);
	}

	/**
	 * 查询当前设备下的设备商品信息
	 * @return
	 */
	//@RequestMapping(value = "shelf/findDeviceProds.json", method = RequestMethod.POST)
	public List<DeviceAisle> findDeviceProds(Long deviceId, Long cabinetId) {
		return productService.findDeviceProds(deviceId, cabinetId);
	}

	/**
	 * 查询符合设备模式的设备下的设备商品信息
	 * @param devCombinationNo
	 * @param cabinetNo
	 * @return
	 */
	@RequestMapping(value = "salesPlan/findDeviceProds.json", method = RequestMethod.POST)
	public List<DeviceAisle> findDeviceProds(Long deviceId, Integer cabinetNo) {
		return productService.findDeviceProds(deviceId, cabinetNo);
	}
	
	/***************************************【虚拟商品 start】******************************************/
	
	/**
	 * @Title: 虚拟商品【01】查看机构的虚拟商品(选中未选中的)
	 * @param devNo 设备编号 
	 * @return
	 * @return: List<Product>
	 */
	@RequestMapping(value = "salesPlan/findDeviceVirtualProduct.json")
	@ModelAttribute("rows")
	public List<Product> findDeviceVirtualProduct(String devNo, Page page){
		return productService.findDeviceVirtualProduct(devNo, page);
	}
	
    /**
     * @Title: 虚拟商品【02】保存上架信息(且上架)
     * @param factoryDevNo
     *            设备组号
     * @param productIds
     *            商品集合数组
     * @return: void
     */
    @RequestMapping(value = "salesPlan/saveDeviceVirtualProduct.json")
    public void saveDeviceVirtualProduct(String devNo, Long[] productIds) {
        productService.saveDeviceVirtualProduct(devNo, productIds);
    }
	
	/***************************************【虚拟商品 end】******************************************/
	
	/**
	 * 保存商品上架信息
	 * @param org
	 */
	@RequestMapping(value = "salesPlan/save.json", method = RequestMethod.POST)
	public void saveShelf(Cabinet cabinet) {
		productService.saveShelf(cabinet);
	}
	
	/**
	 * 查询【销售计划】店铺信息
	 * @return
	 */
	@RequestMapping(value = "salesPlan/findStores.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<PointPlace> findSalesPlanStores(Page page, PointPlace pointPlace) {
		return productService.findSalesPlanStores(page, pointPlace);
	}

	@RequestMapping(value = "/inventory/find.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<Product> findInventoryProd(Product product, Page page) {
		return productService.findInventory(product, page);
	}
	
	@RequestMapping(value = "/salesPlan/find.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<Product> findInventory(Product product, Page page) {
		return productService.findInventory(product, page);
	}

	@RequestMapping(value = "/device/find.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<Device> findDevice(Device device, Page page) {
		return productService.findDeVice(device, page);
	}

	@RequestMapping(value = "/device/findDetail.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<DeviceAisle> findDeviceAisle(DeviceAisle aisle, Page page) {
		return productService.findDeviceProduct(aisle, page);
	}

	/****************************开始********************************************/
	@RequestMapping(value = "productStock/forward.do")
	public ModelAndView forwardProductStock(Long id) {
		ModelAndView view = new ModelAndView("/product/productStock.jsp");
		return view;
	}
	@RequestMapping(value = "platformSupply/forward.do")
	public ModelAndView forwardPlatformSupply(Long id) {
		ModelAndView view = new ModelAndView("/product/platformSupply.jsp");
		return view;
	}
	@RequestMapping(value = "categoryMaintain/forward.do")
	public ModelAndView forwardCategoryMaintain(Long id) {
		ModelAndView view = new ModelAndView("/product/categoryMaintain.jsp");
		return view;
	}
	
	/**
	 * 分页查询商品库存管理列表
	 */
	@RequestMapping(value = "/productStock/find.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<Product> findProductStock(Product product, Page page) {
		return productService.findProductStock(product, page);
	}
	
	/**
	 * 保存商品
	 */
	@RequestMapping(value = "/productStock/save.json", method = RequestMethod.POST)
	public void saveProductStock(Product product, long key, long[] fileIds) {
		productService.saveProductStock(product, key, fileIds);
	}
	
	/**
	 * 删除商品
	 * @param ids
	 */
	@RequestMapping(value = "/productStock/delete.json", method = RequestMethod.POST)
	public void deleteProductStock(Long[] ids) {
		productService.deleteProductStock(ids);
	}
	
	/**
	 * 导出商品信息
	 * @return
	 */
	@RequestMapping(value = "/productStock/export.xls", method = RequestMethod.GET)
	public ModelAndView exportProductStock(Product product) {
		Page page = new Page();
		page.setCurPage(1);
		page.setPageSize(20000);	//	最大查询2万条
		List<Product> products = productService.findProductStock(product, page);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Commons.PROD_TYPE_SELF + "", "自有");
		map.put(Commons.PROD_TYPE_PLATFORM + "", "平台供货");
		
		Map<String, Object> mapValues = new HashMap<String, Object>();
		mapValues.put("type", map);
		Map<String, String> titleMap = new LinkedHashMap<String, String>();
		titleMap.put("商品编码", "code");
		titleMap.put("商品名称", "skuName");
		titleMap.put("品牌", "brand");
		titleMap.put("原产地", "origin");
		titleMap.put("规格", "spec");
		titleMap.put("标准价", "price");
		titleMap.put("组合价", "priceCombo");
		titleMap.put("商品库存", "stock");
		titleMap.put("商品类型", "type");
		ExcelView view = new ExcelView(products, titleMap, "商品库存管理列表", mapValues);
		return new ModelAndView(view);
	}
	
	/**
	 * 分页查询平台供货商品列表
	 */
	@RequestMapping(value = "/platformSupply/find.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<Product> findPlatformSupply(Product product, Page page) {
		return productService.findPlatformSupply(product, page);
	}
	
	/**
	 * 查询供货对象
	 * @return
	 */
	@RequestMapping(value = "/platformSupply/findSupplyObject.json")
	@ResponseBody
	public List<Orgnization> findSupplyObject() {
		return productService.findSupplyObject();
	}
	
	/**
	 * 进行平台供货
	 * @return
	 */
	@RequestMapping(value = "/platformSupply/savePlatformSupply.json")
	public void savePlatformSupply(Long supplyOrgId, List<Product> products) {
		productService.savePlatformSupply(supplyOrgId, products);
	}
	
	/**
	 * 查询类目维护数据
	 * @param page
	 * @return
	 */
	@RequestMapping(value = "categoryMaintain/find.json")
	@ModelAttribute("rows")
	public List<Category> findCategorieMaintains(Page page) {
		return productService.findCategorieMaintains(page);
	}

	/**
	 * 保存类目维护数据
	 * @param category
	 */
	@RequestMapping(value = "categoryMaintain/save.json")
	public void saveCategorieMaintains(Category category) {
		productService.saveCategorieMaintains(category);
	}

	/**
	 * 删除类目维护数据
	 * @param ids
	 */
	@RequestMapping(value = "categoryMaintain/delete.json")
	public void deleteCategorieMaintains(Long[] ids) {
		productService.deleteCategorieMaintains(ids);
	}
	/****************************结束********************************************/
	
	
	@RequestMapping(value = "productOffLine/forward.do", method = RequestMethod.GET)
	public ModelAndView forwardProductOffline(Long id) {
		ModelAndView view = new ModelAndView("/product/productOffLine.jsp");
		return view;
	}
	
	/**
	 * 分页查询商品下线
	 */
	@RequestMapping(value = "/productOffLine/find.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<Product> findProductOffline(Product product, Page page) {
		return productService.findProductOffline(product, page);
	}
	
	/**
	 * 商品下线
	 */
	@RequestMapping(value = "/productOffLine/save.json", method = RequestMethod.POST)
	public void saveProductOffline(Product product) {
		productService.saveProductOffline(product);
	}
}
