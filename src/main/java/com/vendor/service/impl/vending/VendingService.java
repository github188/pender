package com.vendor.service.impl.vending;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xml.sax.InputSource;

import com.aliyuncs.push.model.v20150827.GetDeviceInfosResponse;
import com.allinpay.ets.client.StringUtil;
import com.ecarry.core.dao.IGenericDao;
import com.ecarry.core.dao.SQLUtils;
import com.ecarry.core.exception.BusinessException;
import com.ecarry.core.util.IdWorker;
import com.ecarry.core.util.MathUtil;
import com.ecarry.core.web.core.ContextUtil;
import com.ecarry.core.web.core.Page;
import com.vendor.po.ActiveCode;
import com.vendor.po.AppException;
import com.vendor.po.Cabinet;
import com.vendor.po.Device;
import com.vendor.po.DeviceAisle;
import com.vendor.po.DeviceLog;
import com.vendor.po.DevicePush;
import com.vendor.po.DeviceRelation;
import com.vendor.po.DeviceRule;
import com.vendor.po.LotteryProduct;
import com.vendor.po.Order;
import com.vendor.po.OrderDetail;
import com.vendor.po.Orgnization;
import com.vendor.po.PointPlace;
import com.vendor.po.PointReplenishTime;
import com.vendor.po.Product;
import com.vendor.po.ProductLog;
import com.vendor.po.ProductRecommend;
import com.vendor.po.ProductReplacement;
import com.vendor.po.Refund;
import com.vendor.po.ReplenishmentAppVersion;
import com.vendor.po.TradeFlow;
import com.vendor.po.User;
import com.vendor.po.WeUser;
import com.vendor.pojo.WxAccessConf;
import com.vendor.service.IDictionaryService;
import com.vendor.service.IOperationService;
import com.vendor.service.IProductService;
import com.vendor.service.IShareService;
import com.vendor.service.IVendingService;
import com.vendor.service.IWechatService;
import com.vendor.service.impl.platform.PlatformService.GroupBy;
import com.vendor.thirdparty.wx.WxPayDto;
import com.vendor.thirdparty.wx.WxPayResult;
import com.vendor.util.Commons;
import com.vendor.util.DateUtil;
import com.vendor.util.HttpAdapter;
import com.vendor.util.MessagePusher;
import com.vendor.util.RandomUtil;
import com.vendor.util.Sign;
import com.vendor.util.WeChatUtil;
import com.vendor.util.wechat.ClientCustomSSL;
import com.vendor.util.wxpay.GetWxOrderno;
import com.vendor.util.wxpay.RequestHandler;
import com.vendor.util.wxpay.TenpayUtil;
import com.vendor.vo.app.CabinetVisiableStateInfo;
import com.vendor.vo.app.DeviceInfo;
import com.vendor.vo.app.MachineStock;
import com.vendor.vo.app.OrderData;
import com.vendor.vo.app.ProductReplenishInfo;
import com.vendor.vo.app.QRCodeOrders;
import com.vendor.vo.app.QRCodeRequestInfo;
import com.vendor.vo.app.RecommendGoodsInfo;
import com.vendor.vo.app.ShoppingCart;
import com.vendor.vo.app.StoreInfo;
import com.vendor.vo.app.StoreReplenishTimeInfo;
import com.vendor.vo.app.SyncProductInfo;
import com.vendor.vo.app.SyncRoadSalesInfo;
import com.vendor.vo.app.VProduct;
import com.vendor.vo.common.ResultAppBase;
import com.vendor.vo.common.ResultBase;
import com.vendor.vo.common.ResultList;
import com.vendor.vo.common.Stock;
import com.vendor.vo.web.Store;

@SuppressWarnings("deprecation")
@Service("vendingService")
public class VendingService implements IVendingService {

    private static Logger log = Logger.getLogger(VendingService.class);

    @Autowired
    private HttpAdapter httpAdapter;

    @Autowired
    private IGenericDao genericDao;

    @Autowired
    private IShareService shareService;

    @Autowired
    private IWechatService wechatService;

    @Autowired
    private IDictionaryService dictionaryService;

    @Autowired
    private IdWorker idWorker;

    /**
     * sessionId存取时的时间，用于判断是否失效
     */
    private static final String ET_ACCESS_TIME = "ETAccessTime";

    // 微信支付商户开通后 微信会提供appid和appsecret和商户号partner
    @Value("${wx.appid}")
    private String wechatAppId;// appid

    @Value("${wx.secret}")
    private String wechatAppKey;// appsecret

    @Value("${pay.wechatMchId}")
    private String wechatMchId;// 商户号partner

    // 这个参数partnerkey是在商户后台配置的一个32位的key,微信商户平台-账户设置-安全设置-api安全
    @Value("${pay.wechatKey}")
    private String wechatKey;// partnerkey

    // 微信支付成功后通知地址 必须要求80端口并且地址不能带参数
    @Value("${pay.notifyUrl}")
    private String notifyUrl;

    @Value("${pay.spbillCreateIp}")
    private String spbillCreateIp;

    @Value("${pay.preorder}")
    private String preorderUrl;

    @Value("${file.path}")
    private String rootPath;

    @Value("${vendor.forward.pay.url}")
    private String vendorPayUrl;

    /**
     * 公众号支付回调通知url
     */
    @Value("${wx.official.accounts.notifyUrl}")
    private String officialAccountsNotifyUrl;

    private Map<String, Object> etLoginMap;

    private Map<String, List<Timestamp>> devOrderTimeMap;

    @Value("${pay.refund.url}")
    private String refundUrl;//退款url

    @Autowired
    private IOperationService operationService;

    @Autowired
    private IProductService productService;

    public VendingService() {
        etLoginMap = new HashMap<String, Object>();

        devOrderTimeMap = new HashMap<String, List<Timestamp>>();
    }

    /*
	 * (non-Javadoc)
	 *
	 * @see com.vendor.service.IVendingService#uploadImag(java.lang.String, java.lang.String, java.io.InputStream)
	 */
    @Override
    public String uploadImag(String id, String filename, InputStream is) {
        return null;
    }

    private boolean updateStock(String code, Integer type, Device device, Integer track, Integer qty, String cabinetNo) {
        if (code == null || device == null || track == null)
            return false;

        Cabinet cabinet = genericDao.findT(Cabinet.class, " SELECT ID FROM T_CABINET WHERE DEVICE_ID=? AND CABINET_NO=? ", device.getId(), cabinetNo);
        if (null == cabinet)
            return false;

        DeviceAisle da = findDeviceAisle(device.getId(), code, track, cabinet.getId());
        Product product = null;
        if (da != null && da.getStock() != null) {// 后台货道上的商品和设备上该货道的商品一致
            // 扣除货道库存
            if (da.getStock().intValue() > 0 && da.getStock() >= qty) {
                da.setStock(da.getStock() - qty);
                da.setSupplementNo(da.getSupplementNo() + qty);
                genericDao.update(da);
            }
            // 保存设备日志
            saveDeviceLog(device, cabinet.getModel(), da);
            product = findProduct(da.getProductId());
        } else {// 该货道后台换货了，设备上还是换之前的商品
            product = findProduct(device.getOrgId(), code, type);
        }
        log.info("*****更新库存：****da == null 或者 da.getStock() == null***");

        // 扣除该货道商品的总库存
        if (null == product || null == product.getStock()) {
            log.info("*****更新库存出错：****null == product 或者 null == product.getStock()***");
            return false;
        }
        if (product.getStock().intValue() > 0 && product.getStock() >= qty) {
            product.setStock(product.getStock() - qty);
            genericDao.update(product);
        }
        // 保存商品日志
        saveProductLog(product);

        return true;
    }

    /**
     * 保存设备日志
     * @param device
     * @param model
     */
    private void saveDeviceLog(Device device, String model, DeviceAisle da) {
        // 查询设备日志
        DeviceLog deviceLog = findDeviceLog(device.getDevNo(), Commons.DEVICE_STATUS_STOCKOUT);
        // 扣减完以后，检查货道库存是否为0
        if (Commons.DEVICE_MODEL_GRID40.equals(model) || Commons.DEVICE_MODEL_GRID64.equals(model)) {//格子柜，容量都为1
            if (da.getStock().intValue() <= 0) {
                saveDeviceLog(device.getDevNo(), deviceLog, Commons.DEVICE_STATUS_STOCKOUT);// 追加设备日志，状态为待补货

                //更新设备状态,缺货
                genericDao.execute(" UPDATE T_DEVICE SET STATE = ? WHERE DEV_NO = ? ", Commons.OUT_OF_STOCK, device.getDevNo());
            } else {
                deleteDeviceLog(deviceLog);// 删除状态为待补货的设备日志

                //更新设备状态,正常
                genericDao.execute(" UPDATE T_DEVICE SET STATE = ? WHERE DEV_NO = ? ", Commons.NORMAL, device.getDevNo());
            }
        } else {
            if (da.getStock().intValue() <= da.getStockRemind().intValue()) {//小于库存提醒值
                saveDeviceLog(device.getDevNo(), deviceLog, Commons.DEVICE_STATUS_STOCKOUT);// 追加设备日志，状态为待补货

                //更新设备状态,缺货
                genericDao.execute(" UPDATE T_DEVICE SET STATE = ? WHERE DEV_NO = ? ", Commons.OUT_OF_STOCK, device.getDevNo());
            } else {
                deleteDeviceLog(deviceLog);// 删除状态为待补货的设备日志

                //更新设备状态,正常
                genericDao.execute(" UPDATE T_DEVICE SET STATE = ? WHERE DEV_NO = ? ", Commons.NORMAL, device.getDevNo());
            }
        }
    }

    /**
     * 保存设备日志
     * @param device
     * @param deviceLog
     */
    private void saveDeviceLog(String devNo, DeviceLog deviceLog, Integer deviceStatus) {
        if (null == deviceLog) {
            deviceLog = new DeviceLog();
            deviceLog.setDeviceNo(devNo);
            deviceLog.setCreateTime(new Timestamp(System.currentTimeMillis()));
            deviceLog.setDeviceStatus(deviceStatus);
            genericDao.save(deviceLog);
        }
    }

    /**
     * 删除设备日志
     * @param device
     * @param deviceLog
     */
    private void deleteDeviceLog(DeviceLog deviceLog) {
        if (null != deviceLog)
            genericDao.delete(deviceLog);
    }

    /**
     * 保存商品日志
     */
    private void saveProductLog(Product product) {
        // 保存商品日志
        ProductLog productLog = findProductLog(product.getId(), Commons.PRODUCT_STATUS_STOCKOUT);//商品状态  1：库存不足
        if (product.getStock().intValue() <= 0)
            saveProductLog(product.getId(), productLog);// 追加商品日志，状态为待补货
        else
            deleteProductLog(productLog);// 删除状态为待补货的商品日志
    }

    /**
     * 保存商品日志
     * @param device
     * @param deviceLog
     */
    private void saveProductLog(Long productId, ProductLog productLog) {
        if (null == productLog) {
            productLog = new ProductLog();
            productLog.setProductId(productId);
            productLog.setCreateTime(new Timestamp(System.currentTimeMillis()));
            productLog.setProductStatus(Commons.PRODUCT_STATUS_STOCKOUT);//商品状态  1：库存不足
            genericDao.save(productLog);
        }
    }

    /**
     * 删除商品日志
     * @param device
     * @param deviceLog
     */
    private void deleteProductLog(ProductLog productLog) {
        if (null != productLog)
            genericDao.delete(productLog);
    }

    private boolean findOrderDetail(String orderNo) {
        if (orderNo == null)
            return false;
        OrderDetail detail = genericDao.findT(OrderDetail.class, "SELECT ID FROM T_ORDER_DETAIL WHERE ORDER_NO=?", orderNo);
        if (detail == null)
            return true;
        return false;
    }

    /**
     * 查询设备总销售额
     *
     * @param device
     * @return
     */
    public double findAmountByDevNo(Device device) {
        Double amount = genericDao.findSingle(Double.class, " SELECT COALESCE(SUM(AMOUNT), 0) FROM T_ORDER  WHERE DEVICE_NO = ? AND ORG_ID = ? AND STATE = ? GROUP BY DEVICE_NO ",
                device.getDevNo(), device.getOrgId(), Commons.ORDER_STATE_FINISH);
        return amount == null ? 0 : amount;
    }

    /**
     * 查询设备指定商品的销售额
     *
     * @param device
     * @param productId
     * @return
     */
    public double findAmountByProductId(Device device, Long productId) {
        StringBuffer buffer = new StringBuffer("SELECT COALESCE(SUM(OD.qty*OD.price), 0) ");
        buffer.append(" FROM T_ORDER O ");
        buffer.append(" LEFT JOIN T_ORDER_DETAIL OD ON O.CODE = OD.ORDER_NO ");
        buffer.append(" LEFT JOIN T_PRODUCT P ON P.ID = OD.SKU_ID ");
        buffer.append(" WHERE O.DEVICE_NO = ? AND O.ORG_ID = ? AND O.STATE = ? AND P.ID = ? GROUP BY O.DEVICE_NO ");
        Double amount = genericDao.findSingle(Double.class, buffer.toString(), device.getDevNo(), device.getOrgId(), Commons.ORDER_STATE_FINISH, productId);
        return amount == null ? 0 : amount;
    }

    private Double getActualPrice(QRCodeRequestInfo qrcodeInfo, Product product, Device device) {
        long deviceId = device.getId();
        String code = product.getCode();
        Integer trackNo = Integer.valueOf(qrcodeInfo.getRoadNo());

        Cabinet cabinet = genericDao.findT(Cabinet.class, " SELECT ID FROM T_CABINET WHERE DEVICE_ID=? AND CABINET_NO=? ", deviceId, qrcodeInfo.getCabinetNo());
        if (null == cabinet)
            return 0.0;

        List<Object> args = new ArrayList<Object>();
        StringBuffer buf = new StringBuffer("SELECT ");
        String cols = SQLUtils.getColumnsSQL(DeviceAisle.class, "C");
        buf.append(cols);
        buf.append(" FROM T_DEVICE_AISLE C WHERE C.DEVICE_ID=? AND C.PRODUCT_CODE=? AND C.CABINET_ID=? and C.AISLE_NUM=? ");
        args.add(deviceId);
        args.add(code);
        args.add(cabinet.getId());
        args.add(trackNo);
        DeviceAisle deAisle = genericDao.findT(DeviceAisle.class, buf.toString(), args.toArray());

        return MathUtil.round(deAisle.getPriceOnLine() == null ? 0 : deAisle.getPriceOnLine(), 2);
    }

    /**
     * 根据货道所在货柜型号取得出货用【货柜号】和【货道号】
     *
     * @return
     */
    public void refactoringDeviceAisle(DeviceAisle deAisle) {
        if (null != deAisle && !StringUtils.isEmpty(deAisle.getModel()) && null != deAisle.getAisleNum()) {
            String aisleNumStr = deAisle.getAisleNum() + "";
            switch (deAisle.getModel()) {
                case Commons.DEVICE_MODEL_DRINK:// 饮料机
                case Commons.DEVICE_MODEL_DRINK_SMALL:// 小型饮料机
                    deAisle.setShipmentCabinetNo(Commons.BEVERAGE_MACHINE_STORE_NO + "");
                    deAisle.setShipmentAisleNum(deAisle.getAisleNum() < 10 ? "0" + aisleNumStr : aisleNumStr);
                    break;
                case Commons.DEVICE_MODEL_CENTER_CONTROL:// 中控机
                    break;
                case Commons.DEVICE_MODEL_SPRING:// 弹簧机
                case Commons.DEVICE_MODEL_CATERPILLAR:// 履带机
                    deAisle.setShipmentCabinetNo(aisleNumStr.length() == 2 ? Commons.CABINET_NO_SPRING_1 : Commons.CABINET_NO_SPRING_2);
                    deAisle.setShipmentAisleNum(refactoringSpringAisleNum(aisleNumStr));
                    break;
                case Commons.DEVICE_MODEL_GRID64:// 64门
                case Commons.DEVICE_MODEL_GRID40:// 40门
                case Commons.DEVICE_MODEL_GRID60:// 60门
                    deAisle.setShipmentCabinetNo("0" + aisleNumStr.substring(0, 1));
                    deAisle.setShipmentAisleNum(aisleNumStr.substring(1, aisleNumStr.length()));
                    break;
                default:
                    break;

            }
        }
    }

    /**
     * 构造弹簧机的货道号：弹簧机出货货道号从01开始，但实际对应的出货料道是11的（第一个1代表第一行；第二个1是代表第1个）
     *
     * @return
     */
    public String refactoringSpringAisleNum(String aisleNumStr) {
        int first = 0;
        int last = 0;
        if (aisleNumStr.length() == 2) {// 09货柜
            first = Integer.valueOf(aisleNumStr.substring(0, 1)).intValue();
            last = Integer.valueOf(aisleNumStr.substring(1, 2)).intValue();
        } else if (aisleNumStr.length() == 3) {// 08货柜
            first = Integer.valueOf(aisleNumStr.substring(1, 2)).intValue();
            last = Integer.valueOf(aisleNumStr.substring(2, 3)).intValue();
        }
        int result = (first - 1) * 8 + last;
        return result < 10 ? "0" + result : result + "";
    }

    /**
     * 【设备APP接口】8.上传本机库存数据接口---将设备的当前每个货道的商品库存数据同步到服务器。
     *
     * @param deviceNumber 设备的厂家编号
     * @param deviceID 设备的机器号码，调用厂家Jar包获取
     * @param machineStock 上传本机库存数据接收对象
     * @return
     */
    public ResultBase saveUploadMachineStocks(ResultBase rb, MachineStock machineStock, Long lastUploadTime) throws Exception {
        log.info("**machineStock.getDeviceNumber():***" + machineStock.getDeviceNumber());
        // 转换为内部设备编号
        machineStock.setDeviceNumber(findDevNoByFacDevNo(machineStock.getDeviceNumber()));

        Device device = findDeviceByDevNo(machineStock.getDeviceNumber());
        if (null == device) {
            rb.setResultMessage("设备编号不存在");
            return rb;
        }
        String stocksStr = machineStock.getStocks();
        log.info("**machineStock.getStocks():***" + stocksStr);
        ObjectMapper objectMapper = null;
        try {
            objectMapper = new ObjectMapper();
            Stock[] stockArr = objectMapper.readValue(stocksStr, Stock[].class);
            List<Stock> stocks = Arrays.asList(stockArr);
            for (Stock stock : stocks) {
                log.info("**stock.getCabinetNo():***" + stock.getCabinetNo());
                log.info("**stock.getRoadNo():***" + stock.getRoadNo());
                log.info("**stock.getNumbers():***" + stock.getNumbers());
                log.info("**stock.getProductNo():***" + stock.getProductNo());

                if (StringUtils.isEmpty(stock.getCabinetNo()) || StringUtils.isEmpty(stock.getRoadNo()) || null == stock.getNumbers()
                        || StringUtils.isEmpty(stock.getProductNo())) {
                    rb.setResultMessage("上传失败，库存参数不完整！");
                    return rb;
                }
                // 取出设备商品库存数据
                List<DeviceAisle> deviceAisles = findDeviceAisles(machineStock.getDeviceNumber());
                for (DeviceAisle deAisle : deviceAisles) {// 货道号、商品编码、库存
                    if (deAisle.getCabinetNo().equals(stock.getCabinetNo()) && String.valueOf(deAisle.getAisleNum()).equals(stock.getRoadNo())) {// 货柜号和货道号均一致
                        if (null == deAisle.getProductId())
                            continue;
                        Product product = findProduct(deAisle.getProductId());
                        if (null == product) {
                            rb.setResultMessage("货道号【" + deAisle.getAisleNum() + "】的商品编码不存在");
                            return rb;
                        }

                        log.info("**product.getCode():***" + product.getCode());
                        log.info("**stock.getProductNo():***" + stock.getProductNo());
                        if (!product.getCode().equals(stock.getProductNo())) {
                            rb.setResultMessage("货道号【" + deAisle.getAisleNum() + "】的商品编码与服务器不一致");
                            return rb;
                        }
                        int capacity = deAisle.getCapacity() == null ? 0 : deAisle.getCapacity();// 货道容量
                        int stockNum = stock.getNumbers() == null ? 0 : stock.getNumbers();// 库存

                        if (stockNum < 0) {
                            rb.setResultMessage("货道号【" + deAisle.getAisleNum() + "】的库存【" + stockNum + "】非法");
                            return rb;
                        }
                        deAisle.setStock(stockNum);

                        // 保存设备日志
                        log.info("***保存设备日志***开始");
                        saveDeviceLog(device, deAisle.getModel(), deAisle);
                        log.info("***保存设备日志***结束");

                        if (null != lastUploadTime && lastUploadTime.intValue() == 0) {// 第一次补货，将货道容量设置为上传过来的库存
                            deAisle.setSupplementNo(0);// 应补货数量=货道容量-库存
                            deAisle.setCapacity(stockNum);
                        } else {
                            if (capacity == 0 || capacity < stockNum)
                                continue;
                            deAisle.setSupplementNo(capacity - stockNum);// 应补货数量=货道容量-库存
                        }
                        genericDao.update(deAisle);
                        break;
                    }
                }
            }
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        rb.setResultCode(0);// 成功
        rb.setResultMessage("发送成功!");
        return rb;
    }

    public void reverseDBDevice(Stock stock) {
        String cabinetNo = stock.getCabinetNo();
        String roadNo = stock.getRoadNo();
        if ("11".equals(cabinetNo)) {// 饮料机
            stock.setRoadNo(Integer.parseInt(roadNo, 10) + "");
        } else if ("08".equals(cabinetNo)) {// 第二个弹簧机，第一个货柜号为09
            stock.setRoadNo(8 + roadNo);
        } else if ("01".equals(cabinetNo) || "02".equals(cabinetNo) || "03".equals(cabinetNo) || "04".equals(cabinetNo) || "05".equals(cabinetNo)) {// 格子柜
            stock.setRoadNo(Integer.parseInt(cabinetNo, 10) + roadNo);
        }
    }

    public List<Cabinet> findCabinets(String deviceNo, String model) {
        StringBuffer buffer = new StringBuffer("SELECT ");
        buffer.append(SQLUtils.getColumnsSQL(Cabinet.class, "C"));
        buffer.append(" FROM T_CABINET C ");
        buffer.append(" LEFT JOIN T_DEVICE D ON D.ID = C.DEVICE_ID ");
        buffer.append(" WHERE 1 = 1 ");
        buffer.append(" AND D.DEV_NO = ? ");
        buffer.append(" AND C.MODEL = ? ");
        return genericDao.findTs(Cabinet.class, buffer.toString(), deviceNo, model);
    }

    /**
     * 【设备APP接口】9.微信扫码支付，模式二
     *
     * @param qrcodeInfo
     * @return 二维码信息和订单流水号
     */
    public Map<String, Object> saveQRCodePay(Map<String, Object> map, QRCodeRequestInfo qrcodeInfo) throws Exception {
        // 转换为内部设备编号
        qrcodeInfo.setDeviceNumber(findDevNoByFacDevNo(qrcodeInfo.getDeviceNumber()));

        Device device = findDeviceByDevNo(qrcodeInfo.getDeviceNumber());
        if (null == device) {
            map.put("resultMessage", "设备编号不存在");
            return map;
        }

        Product product = findProduct(qrcodeInfo.getDeviceNumber(), qrcodeInfo.getProductNo(), qrcodeInfo.getCabinetNo(), Integer.valueOf(qrcodeInfo.getRoadNo()));
        if (null == product) {
            map.put("resultMessage", "商品编号不存在");
            return map;
        }
        // 生成订单信息
        Order order = saveOrder(qrcodeInfo, product, device);
        if (null == order) {
            map.put("resultMessage", "生成订单出错！");
            return map;
        }

        // 1 微信扫码支付
        WxPayDto tpWxPay1 = new WxPayDto();
        tpWxPay1.setBody(product.getSkuName());
        tpWxPay1.setOrderId(order.getCode());
        tpWxPay1.setSpbillCreateIp(spbillCreateIp);
        tpWxPay1.setTotalFee(order.getAmount() + "");
        String qrcodeStr = getCodeurl(tpWxPay1);

        // 2 通联扫码支付
        // 根据支付接口返回的二维码串生成二维码并取得二维码地址
        // String qrcodeStr = findWxQRCodePath(order);

        if (StringUtils.isEmpty(qrcodeStr)) {
            map.put("resultMessage", "微信生成二维码出错");
            return map;
        }

        Orgnization org = findOrgById(device.getOrgId());
        String codeStr = dictionaryService.generateQrcode(org.getCode(), qrcodeStr);
        if (StringUtils.isEmpty(codeStr)) {
            map.put("resultMessage", "云文件服务器生成二维码文件出错！");
            return map;
        }

        String qrcode = dictionaryService.getFileServer() + codeStr;
        map.put("resultCode", 0);
        map.put("resultMessage", "发送成功！");
        map.put("qrcode", qrcode);
        map.put("orderNo", order.getCode());
        return map;
    }

    /**
     * 保存待付款的订单信息
     */
    public Order saveOrder(QRCodeRequestInfo qrcodeInfo, Product product, Device device) {
        // 生成订单信息
        Order order = new Order();
        order.initDefaultValue();
        order.setCode(RandomUtil.getCharAndNumr(32));// 外部订单号
        order.setCreateTime(new Timestamp(System.currentTimeMillis()));
        order.setDeviceNo(qrcodeInfo.getDeviceNumber());// 设备编号
        // order.setAmount(MathUtil.mul(product.getPrice(), qrcodeInfo.getProductCount()));
        Double actualPrice = getActualPrice(qrcodeInfo, product, device);
        if ((null == actualPrice) || (null != actualPrice && actualPrice == 0))
            return null;

        order.setAmount(MathUtil.mul(actualPrice, qrcodeInfo.getProductCount()));
        order.setState(Commons.ORDER_STATE_NEW);
        order.setOrgId(device.getOrgId());
        order.setPayType(6);// 支付类型
        order.setCoupone(0D);
        int count = genericDao.save(order);
        if (count > 0) {
            if (findOrderDetail(order.getCode())) {// 之前没有该订单号的订单详情信息
                OrderDetail detail = new OrderDetail();
                detail.setOrderNo(order.getCode());// 外部订单号
                detail.setOrgId(device.getOrgId());
                // detail.setPrice(product.getPrice());
                detail.setPrice(actualPrice);// 根据设备同步时间情况，确定产品实际价格
                detail.setQty(qrcodeInfo.getProductCount());
                detail.setSkuId(product.getId());
                detail.setSku(product.getSku());
                detail.setCreateTime(new Timestamp(System.currentTimeMillis()));
                detail.setCurrency("CNY");
                order.addOrderDetail(detail);
                genericDao.save(detail);
            }
        }
        return order;
    }

    /**
     * 获取统一下单预支付交易回话标识
     */
    public String getUnifiedorderStr(WxPayDto tpWxPayDto) {
        // 1 参数
        // 订单号
        String orderId = tpWxPayDto.getOrderId();
        // 附加数据 原样返回
        String attach = "";
        // 总金额以分为单位，不带小数点
        String totalFee = getMoney(tpWxPayDto.getTotalFee());
        // 订单生成的机器 IP
        String spbill_create_ip = tpWxPayDto.getSpbillCreateIp();
        // 这里notify_url是 支付完成后微信发给该链接信息，可以判断会员是否支付成功，改变订单状态等。
        String notify_url = officialAccountsNotifyUrl;
        String trade_type = "JSAPI";
        // 商户号
        String mch_id = wechatMchId;
        // 随机字符串
        String nonce_str = getNonceStr();
        // 商品描述根据情况修改
        String body = tpWxPayDto.getBody();
        // 商户订单号
        String out_trade_no = orderId;
        // openId
        String openid = tpWxPayDto.getOpenId();

        SortedMap<String, String> packageParams = new TreeMap<String, String>();
        packageParams.put("appid", wechatAppId);
        packageParams.put("mch_id", mch_id);
        packageParams.put("nonce_str", nonce_str);
        packageParams.put("body", body);
        packageParams.put("attach", attach);
        packageParams.put("out_trade_no", out_trade_no);
        packageParams.put("total_fee", totalFee);
        packageParams.put("spbill_create_ip", spbill_create_ip);
        packageParams.put("notify_url", notify_url);
        packageParams.put("trade_type", trade_type);
        packageParams.put("openid", openid);

        RequestHandler reqHandler = new RequestHandler(null, null);
        reqHandler.init(wechatAppId, wechatAppKey, wechatKey);

        String sign = reqHandler.createSign(packageParams);
        String xml = "<xml>" + "<appid>" + wechatAppId + "</appid>" + "<mch_id>" + mch_id + "</mch_id>" + "<nonce_str>" + nonce_str + "</nonce_str>" + "<sign>" + sign + "</sign>"
                + "<body><![CDATA[" + body + "]]></body>" + "<out_trade_no>" + out_trade_no + "</out_trade_no>" + "<attach>" + attach + "</attach>" + "<total_fee>" + totalFee
                + "</total_fee>" + "<spbill_create_ip>" + spbill_create_ip + "</spbill_create_ip>" + "<notify_url>" + notify_url + "</notify_url>" + "<openid>" + openid
                + "</openid>" + "<trade_type>" + trade_type + "</trade_type>" + "</xml>";

        String unifiedorderStr = httpAdapter.postData(preorderUrl, xml);
        log.info("*******公众号支付-统一下单返回：***********" + unifiedorderStr);
        return unifiedorderStr;
    }

    /**
     * 获取微信扫码支付二维码连接
     */
    public String getCodeurl(WxPayDto tpWxPayDto) {
        // 1 参数
        // 订单号
        String orderId = tpWxPayDto.getOrderId();
        // 附加数据 原样返回
        String attach = "";
        // 总金额以分为单位，不带小数点
        String totalFee = getMoney(tpWxPayDto.getTotalFee());
        // 订单生成的机器 IP
        String spbill_create_ip = tpWxPayDto.getSpbillCreateIp();
        // 这里notify_url是 支付完成后微信发给该链接信息，可以判断会员是否支付成功，改变订单状态等。
        String notify_url = notifyUrl;
        String trade_type = "NATIVE";
        // 商户号
        String mch_id = wechatMchId;
        // 随机字符串
        String nonce_str = getNonceStr();
        // 商品描述根据情况修改
        String body = tpWxPayDto.getBody();
        // 商户订单号
        String out_trade_no = orderId;

        SortedMap<String, String> packageParams = new TreeMap<String, String>();
        packageParams.put("appid", wechatAppId);
        packageParams.put("mch_id", mch_id);
        packageParams.put("nonce_str", nonce_str);
        packageParams.put("body", body);
        packageParams.put("attach", attach);
        packageParams.put("out_trade_no", out_trade_no);
        packageParams.put("total_fee", totalFee);
        packageParams.put("spbill_create_ip", spbill_create_ip);
        packageParams.put("notify_url", notify_url);
        packageParams.put("trade_type", trade_type);

        RequestHandler reqHandler = new RequestHandler(null, null);
        reqHandler.init(wechatAppId, wechatAppKey, wechatKey);

        String sign = reqHandler.createSign(packageParams);
        String xml = "<xml>" + "<appid>" + wechatAppId + "</appid>" + "<mch_id>" + mch_id + "</mch_id>" + "<nonce_str>" + nonce_str + "</nonce_str>" + "<sign>" + sign + "</sign>"
                + "<body><![CDATA[" + body + "]]></body>" + "<out_trade_no>" + out_trade_no + "</out_trade_no>" + "<attach>" + attach + "</attach>" + "<total_fee>" + totalFee
                + "</total_fee>" + "<spbill_create_ip>" + spbill_create_ip + "</spbill_create_ip>" + "<notify_url>" + notify_url + "</notify_url>" + "<trade_type>" + trade_type
                + "</trade_type>" + "</xml>";
        String code_url = "";
        code_url = new GetWxOrderno().getCodeUrl(preorderUrl, xml);
        log.info("*******扫码支付-返回二维码：***********" + code_url);
        return code_url;
    }

    /**
     * 获取随机字符串
     *
     * @return
     */
    public String getNonceStr() {
        // 随机数
        String currTime = TenpayUtil.getCurrTime();
        // 8位日期
        String strTime = currTime.substring(8, currTime.length());
        // 四位随机数
        String strRandom = TenpayUtil.buildRandom(4) + "";
        // 10位序列号,可以自行调整。
        return strTime + strRandom;
    }

    /**
     * 元转换成分
     *
     * @param money
     * @return
     */
    public String getMoney(String amount) {
        if (amount == null) {
            return "";
        }
        // 金额转化为分为单位
        String currency = amount.replaceAll("\\$|\\￥|\\,", ""); // 处理包含, ￥ 或者$的金额
        int index = currency.indexOf(".");
        int length = currency.length();
        Long amLong = 0l;
        if (index == -1) {
            amLong = Long.valueOf(currency + "00");
        } else if (length - index >= 3) {
            amLong = Long.valueOf((currency.substring(0, index + 3)).replace(".", ""));
        } else if (length - index == 2) {
            amLong = Long.valueOf((currency.substring(0, index + 2)).replace(".", "") + 0);
        } else {
            amLong = Long.valueOf((currency.substring(0, index + 1)).replace(".", "") + "00");
        }
        return amLong.toString();
    }

    /**
     * 【设备APP接口】9.扫码支付通知回调
     *
     * @return
     */
    public boolean saveQRCodeAsyncNotify(String notityXml) throws Exception {
        WxPayResult wpr = getWxPayResult(notityXml);
        if (null == wpr) {
            log.error("****【扫码支付通知回调】*****notityXml解析失败***");
            return false;
        }
        if (!wechatAppId.equals(wpr.getAppid()) || !wechatMchId.equals(wpr.getMchId())) {
            log.error("****【扫码支付通知回调】*****appid和appsecret不匹配***");
            return false;
        }
        if (!"SUCCESS".equals(wpr.getResultCode())) {
            log.error("****【扫码支付通知回调】*****订单信息不存在，返回码不是SUCCESS***");
            return false;
        }
        // 支付成功
        // 更新订单状态
        Order order = findOrderByOrderNo(wpr.getOutTradeNo(), Commons.ORDER_STATE_NEW);
        if (null == order) {
            log.error("****【扫码支付通知回调】*****订单信息不存在，编号【" + wpr.getOutTradeNo() + "】***");
            return false;
        }
        order.setUsername(wpr.getOpenid());
        order.setPayCode(wpr.getTransactionId());
        order.setPayTime(DateUtil.stringToTimestamp(wpr.getTimeEnd(), DateUtil.YYYYMMDDHHMMSS_EN));
        order.setState(Commons.ORDER_STATE_FINISH);
        genericDao.update(order);
        log.info("***更改订单状态成功***" + order.getCode());

        Device device = findDeviceByCode(order.getDeviceNo());
        log.info("***device：***" + device);

        shareService.saveWxUser(wpr.getOpenid(), device.getId(), device.getOrgId(), order.getDeviceNo());
        log.info("***saveWxUser成功***");

        // 追加一条充值交易流水
        saveTradeFlow(device.getOrgId(), order.getAmount());
        log.info("***saveTradeFlow成功***");

        // 此处需要判断设备是否成功出货给客户
        log.info("return true!!!");

        return true;
    }

    /**
     * 微信扫码支付异步通知回调
     */
    @Override
    public void saveWxAsyncNotify(Map<String, String> params) throws Exception {
        log.debug("----------微信扫码支付异步通知回调---正式开始-------------");
        // 获取微信的通知返回参数
        // appid
        String appid = params.containsKey("appid") ? params.get("appid") : "";
        if (StringUtils.isEmpty(appid)) {
            log.error("非法回调，appid为空。");
            throw new BusinessException("非法回调，appid为空。");
        }
        // 第三方app交易号(可保存商户交易流水号)
        String outtrxid = params.containsKey("outtrxid") ? params.get("outtrxid") : "";
        if (StringUtils.isEmpty(outtrxid)) {
            log.error("非法回调，第三方app交易号为空。");
            throw new BusinessException("非法回调，第三方app交易号为空。");
        }
        log.info("outtrxid:" + outtrxid);
        // 商户订单号
        String cusorderid = params.containsKey("cusorderid") ? params.get("cusorderid") : "";
        if (StringUtils.isEmpty(cusorderid)) {
            log.error("非法回调，商户订单号为空。");
            throw new BusinessException("非法回调，商户订单号为空。");
        }
        log.info("cusorderid:" + cusorderid);
        // 通联交易单号
        String trxid = params.containsKey("trxid") ? params.get("trxid") : "";
        if (StringUtils.isEmpty(trxid)) {
            log.error("非法回调，通联交易单号为空。");
            throw new BusinessException("非法回调，通联交易单号为空。");
        }
        // 交易金额
        String trxamt = params.containsKey("trxamt") ? params.get("trxamt") : "";
        if (StringUtils.isEmpty(trxamt)) {
            log.error("非法回调，交易金额为空。");
            throw new BusinessException("非法回调，交易金额为空。");
        }
        // 微信交易号
        String chnltrxid = params.containsKey("chnltrxid") ? params.get("chnltrxid") : "";
        if (StringUtils.isEmpty(chnltrxid)) {
            log.error("非法回调，微信交易号为空。");
            throw new BusinessException("非法回调，微信交易号为空。");
        }
        // 交易状态
        String trxstatus = params.containsKey("trxstatus") ? params.get("trxstatus") : "";
        if (StringUtils.isEmpty(trxstatus)) {
            log.error("非法回调，交易状态为空。");
            throw new BusinessException("非法回调，交易状态为空。");
        }
        // 交易完成时间
        String paytime = params.containsKey("paytime") ? params.get("paytime") : "";
        if (StringUtils.isEmpty(paytime)) {
            log.error("非法回调，交易完成时间为空。");
            throw new BusinessException("非法回调，交易完成时间为空。");
        }
        // 支付人帐号(openid)
        String openId = params.containsKey("acct") ? params.get("acct") : "";
        if (StringUtils.isEmpty(openId)) {
            log.error("非法回调，openId为空。");
            throw new BusinessException("非法回调，openId为空。");
        }

        // 判断该笔订单是否在商户网站中已经做过处理
        // 如果没有做过处理，根据订单号（outtrxid）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
        // 如果有做过处理，不执行商户的业务程序
        Order order = findOrderByOrderNo(outtrxid, null);
        if (null == order) {
            log.error("非法回调，商户交易单号不存在。");
            throw new BusinessException("非法回调，商户交易单号不存在。");
        }
        if (Commons.ORDER_STATE_FINISH == order.getState()) {
            log.error("重复回调");
            throw new BusinessException("重复回调");
        }
        order.setPayCode(chnltrxid);// 微信交易号

        log.debug("-----------------微信通知验证成功-------------");
        if (Commons.WX_TRADE_CODE_SUCCESS.equals(trxstatus)) {
            log.debug("-----------------微信回调交易状态:" + trxstatus + "-------------");
            // 交易金额
            BigDecimal total_fee_return = new BigDecimal(trxamt);
            BigDecimal total_fee_db = new BigDecimal(order.getAmount()).multiply(new BigDecimal(100));
            // 判断请求时的appid、交易金额与通知时获取的交易金额为一致的
            if (total_fee_db.intValue() == total_fee_return.intValue()) {
                log.debug("-----------------交易金额比对后一致-------------");
                log.debug("-----------------业务处理执行---开始-------------");

                // 更新订单状态
                order.setUsername(openId);
                order.setPayTime(DateUtil.stringToTimestamp(paytime, DateUtil.YYYYMMDDHHMMSS_EN));
                order.setState(Commons.ORDER_STATE_FINISH);
                genericDao.update(order);
                log.info("***更改订单状态成功***");

                Device device = findDeviceByCode(order.getDeviceNo());
                log.info("***device：***" + device);

                shareService.saveWxUser(openId, device.getId(), device.getOrgId(), order.getDeviceNo());
                log.info("***saveWxUser成功***");

                // 追加一条充值交易流水
                saveTradeFlow(device.getOrgId(), order.getAmount());
                log.info("***saveTradeFlow成功***");

                log.debug("-----------------业务处理执行---结束-------------");
            }
        } else {
            // 更新交易状态(失败)
            order.setState(Commons.TL_TRADE_STATUS_FAIL);
            String remark = "";
            switch (trxstatus) {
                case Commons.WX_TRADE_CODE_TIMEOUT:
                    remark = "交易超时";
                    break;
                case Commons.WX_TRADE_CODE_BALANCE_NOT_ENOUGH:
                    remark = "余额不足";
                    break;
                case Commons.WX_TRADE_CODE_FAIL:
                    remark = "交易失败";
                    break;
                default:
                    break;
            }
            order.setRemark(remark);
            genericDao.update(order);
        }

        // 该页面可做页面美工编辑
        log.debug("----------微信回调success，微信交易号：【" + chnltrxid + "】-------------");
        log.debug("----------微信扫码支付异步通知回调---正式结束-------------");
    }

    /**
     * 保存充值交易流水信息
     */
    public void saveTradeFlow(Long orgId, Double amount) {
        // 从交易流水表中查询当前用户最近时间的流水
        List<Object> args = new ArrayList<Object>();
        StringBuffer buf = new StringBuffer("SELECT ");
        String cols = SQLUtils.getColumnsSQL(TradeFlow.class, "C");
        buf.append(cols);
        buf.append(" FROM T_TRADE_FLOW C WHERE C.ORG_ID = ? AND C.TRADE_STATUS = ? ORDER BY C.TRADE_TIME DESC ");
        args.add(orgId);
        args.add(Commons.TRADE_STATUS_SUCCESS);
        TradeFlow tradeFlow = genericDao.findT(TradeFlow.class, buf.toString(), args.toArray());
        if (null == tradeFlow) {
            tradeFlow = createTradeFlow(orgId);
            if (null != tradeFlow && tradeFlow.getTradeAmount() > 0)
                genericDao.save(tradeFlow);
        } else {// 之前有交易流水记录
            TradeFlow tFlow = new TradeFlow();
            tFlow.setOrgId(orgId);
            tFlow.setTradeType(Commons.TRADE_TYPE_RECHARGE);
            tFlow.setTradeAmount(amount);// 交易金额
            tFlow.setTradeTime(new Timestamp(System.currentTimeMillis()));
            tFlow.setBalance(MathUtil.round(MathUtil.add(tradeFlow.getBalance(), amount), 2));
            tFlow.setTradeStatus(Commons.TRADE_STATUS_SUCCESS);
            genericDao.save(tFlow);
        }
    }

    /**
     * 构建初始化交易流水信息
     *
     * @param user 当前用户
     * @return 初始化交易流水信息
     */
    private TradeFlow createTradeFlow(Long orgId) {
        Timestamp curTime = new Timestamp(System.currentTimeMillis());
        // 取得总销售额(所有商品总销售额)
        User user = new User();
        user.setOrgId(orgId);
        double salesTotalAmount = getSalesAmount(user, null);

        TradeFlow tradeFlow = new TradeFlow();
        tradeFlow.setOrgId(orgId);
        tradeFlow.setTradeType(Commons.TRADE_TYPE_RECHARGE);
        tradeFlow.setTradeAmount(MathUtil.round(salesTotalAmount, 2));
        tradeFlow.setTradeTime(curTime);
        tradeFlow.setBalance(MathUtil.round(salesTotalAmount, 2));
        tradeFlow.setTradeStatus(Commons.TRADE_STATUS_SUCCESS);
        return tradeFlow;
    }

    /**
     * 取得当前登录用户的销售额
     *
     * @param user 当前登录用户
     * @param date 指定日期
     * @return 当前登录用户的销售额
     */
    private double getSalesAmount(User user, Date date) {
        List<Object> args = new ArrayList<Object>();
        StringBuffer buf = new StringBuffer(" SELECT COALESCE(SUM(O.AMOUNT), 0) FROM T_ORDER O WHERE 1 = 1 ");
        buf.append(" AND O.ORG_ID = ? ");
        args.add(user.getOrgId());

        // 指定日期
        if (null != date) {
            // 开始日期
            buf.append(" AND O.PAY_TIME>=? ");
            args.add(DateUtil.getStartDate(date));
            // 结束日期
            buf.append(" AND O.PAY_TIME<=? ");
            args.add(DateUtil.getEndDate(date));
        }

        buf.append(" AND O.STATE = ? ");
        args.add(Commons.ORDER_STATE_FINISH);

        Double amount = genericDao.findSingle(Double.class, buf.toString(), args.toArray());
        return amount == null ? 0 : amount;
    }

    @SuppressWarnings({"rawtypes"})
    public WxPayResult getWxPayResult(String notityXml) {
        WxPayResult wpr = null;
        Map m = parseXmlToList2(notityXml);
        if (m.isEmpty()) {
            return wpr;
        }
        wpr = new WxPayResult();
        wpr.setAppid(m.get("appid").toString());
        wpr.setBankType(m.get("bank_type").toString());
        wpr.setCashFee(m.get("cash_fee").toString());
        wpr.setFeeType(m.get("fee_type").toString());
        wpr.setIsSubscribe(m.get("is_subscribe").toString());
        wpr.setMchId(m.get("mch_id").toString());
        wpr.setNonceStr(m.get("nonce_str").toString());
        wpr.setOpenid(m.get("openid").toString());
        wpr.setOutTradeNo(m.get("out_trade_no").toString());
        wpr.setResultCode(m.get("result_code").toString());
        wpr.setReturnCode(m.get("return_code").toString());
        wpr.setSign(m.get("sign").toString());
        wpr.setTimeEnd(m.get("time_end").toString());
        wpr.setTotalFee(m.get("total_fee").toString());
        wpr.setTradeType(m.get("trade_type").toString());
        wpr.setTransactionId(m.get("transaction_id").toString());// 微信支付订单号
        return wpr;
    }

    /**
     * description: 解析微信通知xml
     *
     * @param xml
     * @return
     * @author ex_yangxiaoyi
     * @see
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Map parseXmlToList2(String xml) {
        Map retMap = new HashMap();
        try {
            StringReader read = new StringReader(xml);
            // 创建新的输入源SAX 解析器将使用 InputSource 对象来确定如何读取 XML 输入
            InputSource source = new InputSource(read);
            // 创建一个新的SAXBuilder
            SAXBuilder sb = new SAXBuilder();
            // 通过输入源构造一个Document
            Document doc = (Document) sb.build(source);
            Element root = doc.getRootElement();// 指向根节点
            List<Element> es = root.getChildren();
            if (es != null && es.size() != 0) {
                for (Element element : es) {
                    retMap.put(element.getName(), element.getValue());
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return retMap;
    }

    /**
     * 【设备APP接口】10.根据订单流水号查询交易状态的接口
     *
     * @param orderNo 订单流水号
     * @return
     */
    @Override
    public Map<String, Object> findOrderStatus(Map<String, Object> map, String orderNo) throws Exception {
        Order order = findOrderByOrderNo(orderNo, null);
        if (null == order) {
            map.put("resultMessage", "订单流水号不存在");
            return map;
        }
        map.put("resultCode", 0);
        map.put("resultMessage", "发送成功！");
        map.put("state", order.getState());
        return map;
    }

    /**
     * 【设备APP接口】11.获取激活码接口
     *
     * @return
     */
    public Map<String, Object> syncActiveCode(Map<String, Object> map) throws Exception {
        ActiveCode activeCode = findActiveCode(Commons.ACTIVE_CODE_STATE_INIT);
        if (null == activeCode) {
            map.put("resultMessage", "抱歉，激活码用完了！");
        } else {
            log.info("********返回激活码:*****" + activeCode.getActiveCode());
            map.put("resultCode", 0);
            map.put("resultMessage", "发送成功！");
            map.put("activeCode", activeCode.getActiveCode());
        }
        return map;
    }

    /**
     * 【设备APP接口】12.同步激活码状态接口
     *
     * @return
     */
    public ResultBase syncActiveCodeState(ResultBase rb, String deviceNumber, String activeCode, Integer state) throws Exception {
        Device device = null;
        // 转换为系统内部设备号
        if (!StringUtils.isEmpty(deviceNumber)) {
            deviceNumber = findDevNoByFacDevNo(deviceNumber);

            device = findDeviceByCode(deviceNumber);
            if (null == device) {
                rb.setResultMessage("设备编号不存在");
                return rb;
            }
        }

        ActiveCode ac = findActiveCode(activeCode, Commons.ACTIVE_CODE_STATE_INIT);
        if (null == ac) {
            rb.setResultMessage("激活码信息不存在");
            return rb;
        }
        ac.setDeviceId(device == null ? 0L : device.getId());
        ac.setState(Commons.ACTIVE_CODE_STATE_USED);
        ac.setActiveTime(new Timestamp(System.currentTimeMillis()));
        genericDao.update(ac);

        rb.setResultCode(0);// 成功
        rb.setResultMessage("发送成功!");
        return rb;
    }

    /**
     * 【设备APP接口】13.获取设备点位绑定状态接口
     *
     * @return
     */
    public Map<String, Object> findDeviceBindState(Map<String, Object> map, String deviceNumber) throws Exception {
        // 转换为系统内部设备号
        deviceNumber = findDevNoByFacDevNo(deviceNumber);

        Device device = findDeviceByCode(deviceNumber);
        if (null == device) {
            map.put("resultMessage", "设备编号不存在！");
            return map;
        }
        map.put("resultCode", 0);
        map.put("resultMessage", "发送成功！");
        map.put("bindState", device.getBindState());
        return map;
    }

    /**
     * 【设备APP接口】17.接受APP上传过来的日志文件，并保存到服务器
     *
     * @return
     */
    public ResultBase uploadLog(ResultBase rb, HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            request.setCharacterEncoding("UTF-8"); // 设置处理请求参数的编码格式
            response.setContentType("text/html;charset=UTF-8"); // 设置Content-Type字段值
            PrintWriter out = response.getWriter();

            // 下面的代码开始使用Commons-UploadFile组件处理上传的文件数据
            FileItemFactory factory = new DiskFileItemFactory(); // 建立FileItemFactory对象
            ServletFileUpload upload = new ServletFileUpload(factory);
            // 分析请求，并得到上传文件的FileItem对象
            List<FileItem> items = upload.parseRequest(request);
            // 从web.xml文件中的参数中得到上传文件的路径
            String uploadPath = rootPath + "/apk/logs/";
            File file = new File(uploadPath);
            if (!file.exists()) {
                file.mkdir();
            }
            String filename = ""; // 上传文件保存到服务器的文件名
            InputStream is = null; // 当前上传文件的InputStream对象
            // 循环处理上传文件
            for (FileItem item : items) {
                // 处理普通的表单域
                if (item.isFormField()) {
                    if (item.getFieldName().equals("filename")) {
                        // 如果新文件不为空，将其保存在filename中
                        if (!item.getString().equals(""))
                            filename = item.getString("UTF-8");
                    }
                } else if (item.getName() != null && !item.getName().equals("")) {// 处理上传文件
                    // 从客户端发送过来的上传文件路径中截取文件名
                    filename = item.getName().substring(item.getName().lastIndexOf("\\") + 1);
                    is = item.getInputStream(); // 得到上传文件的InputStream对象
                }
            }
            // 将路径和上传文件名组合成完整的服务端路径
            filename = uploadPath + filename;
            // 如果服务器已经存在和上传文件同名的文件，则输出提示信息
            if (new File(filename).exists()) {
                new File(filename).delete();
            }
            // 开始上传文件
            if (!filename.equals("")) {
                // 用FileOutputStream打开服务端的上传文件
                FileOutputStream fos = new FileOutputStream(filename);
                byte[] buffer = new byte[8192]; // 每次读8K字节
                int count = 0;
                // 开始读取上传文件的字节，并将其输出到服务端的上传文件输出流中
                while ((count = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, count); // 向服务端文件写入字节流

                }
                fos.close(); // 关闭FileOutputStream对象
                is.close(); // InputStream对象
                out.println("文件上传成功!");

            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            rb.setResultMessage("发送失败！");
            return rb;
        }

        rb.setResultCode(0);
        rb.setResultMessage("发送成功！");
        return rb;
    }

    // 流转化成文件
    public static void inputStream2File(InputStream is, String savePath) throws Exception {
        log.info("文件保存路径为:" + savePath);
        File file = new File(savePath);
        InputStream inputSteam = is;
        BufferedInputStream fis = new BufferedInputStream(inputSteam);
        FileOutputStream fos = new FileOutputStream(file);
        int f;
        while ((f = fis.read()) != -1) {
            fos.write(f);
        }
        fos.flush();
        fos.close();
        fis.close();
        inputSteam.close();
    }

    /**
     * 【设备APP接口】18.接受APP上传过来的异常信息，并保存到服务器
     *
     * @return
     */
    public ResultBase uploadExceptionMsg(ResultBase rb, String deviceNumber, String version, String exception) throws Exception {
        // 转换为系统内部设备号
        deviceNumber = findDevNoByFacDevNo(deviceNumber);

        AppException appException = new AppException();

        Device device = findDeviceByCode(deviceNumber);
        if (null == device) {
            rb.setResultMessage("设备编号不存在");
            return rb;
        }
        appException.setDeviceId(device.getId());
        appException.setDeviceNo(deviceNumber);
        appException.setVersion(version);
        appException.setExceptions(exception);
        appException.setCreateTime(new Timestamp(System.currentTimeMillis()));
        genericDao.save(appException);

        rb.setResultCode(0);// 成功
        rb.setResultMessage("发送成功!");
        return rb;
    }

    /**
     * 获取商品金额总数
     *
     * @param products
     * @return
     */
    public Double getAmount(List<com.vendor.vo.app.Product> products, Device device) {
        Double amount = 0.0;
        for (com.vendor.vo.app.Product appProduct : products) {
            List<Product> product = findProduct(device.getDevNo(), appProduct.getProductNo());
            if (null != product && !product.isEmpty()) {// 设备上的商品和后台货道的商品一致
//				Double actualPrice = getDeviceActualPrice(appProduct.getCabinetNo(), appProduct.getRoadNo(), product, device);
//				if ((null == actualPrice) || (null != actualPrice && actualPrice == 0)) {
//					log.error("*****生成订单出错：商品实际价格为0*****");
//					return amount;
//				}
                amount += MathUtil.mul(appProduct.getPrice(), appProduct.getProductCount());
            } else {// 设备上的商品和后台货道的商品不一致，从商品总表中查询
                Product productDB = findProduct(device.getOrgId(), appProduct.getProductNo());
                if (null == productDB) {
                    log.error("*****生成订单出错：商品信息为空*****");
                }
                amount += MathUtil.mul(appProduct.getPrice(), appProduct.getProductCount());
            }
        }
        return amount;
    }

    /**
     * 获取当前设备商品的实际价格
     *
     * @param cabinetNo
     * @param roadNo
     * @param product
     * @param device
     * @return
     */
    @SuppressWarnings("unused")
    private Double getDeviceActualPrice(String cabinetNo, String roadNo, Product product, Device device) {
        long deviceId = device.getId();
        String code = product.getCode();
        Integer trackNo = Integer.valueOf(roadNo);

        Cabinet cabinet = genericDao.findT(Cabinet.class, " SELECT ID FROM T_CABINET WHERE DEVICE_ID=? AND CABINET_NO=? ", deviceId, cabinetNo);
        if (null == cabinet)
            return 0.0;

        List<Object> args = new ArrayList<Object>();
        StringBuffer buf = new StringBuffer("SELECT ");
        String cols = SQLUtils.getColumnsSQL(DeviceAisle.class, "C");
        buf.append(cols);
        buf.append(" FROM T_DEVICE_AISLE C WHERE C.DEVICE_ID=? AND C.PRODUCT_CODE=? AND C.CABINET_ID=? and C.AISLE_NUM=? ");
        args.add(deviceId);
        args.add(code);
        args.add(cabinet.getId());
        args.add(trackNo);
        DeviceAisle deAisle = genericDao.findT(DeviceAisle.class, buf.toString(), args.toArray());
        return MathUtil.round(deAisle.getPriceOnLine() == null ? 0 : deAisle.getPriceOnLine(), 2);
    }

    /**
     * 构建订单详情信息
     *
     * @param products
     */
    public boolean saveOrderDetails(List<com.vendor.vo.app.Product> products, Order order, Device device) {
        boolean successFlag = false;
        if (findOrderDetail(order.getCode())) {// 之前没有该订单号的订单详情信息
            // 将商品信息按商品编号分组
            Map<String, List<com.vendor.vo.app.Product>> map = new HashMap<String, List<com.vendor.vo.app.Product>>();
            for (com.vendor.vo.app.Product appProduct : products) {
                if (map.keySet().contains(appProduct.getProductNo())) {
                    map.get(appProduct.getProductNo()).add(appProduct);
                } else {
                    List<com.vendor.vo.app.Product> list = new ArrayList<com.vendor.vo.app.Product>();
                    list.add(appProduct);
                    map.put(appProduct.getProductNo(), list);
                }
            }

            // 遍历Map，构建订单详情信息
            for (Entry<String, List<com.vendor.vo.app.Product>> entry : map.entrySet()) {
                com.vendor.vo.app.Product appProduct = entry.getValue().get(0);

                List<Product> productList = findProduct(device.getDevNo(), entry.getKey());
                Double actualPrice = 0.0;
                Product product = null;
                if (null != productList && !productList.isEmpty()) {// 设备上的商品和后台货道的商品一致
                    if (null == entry.getValue() || (null != entry.getValue() && entry.getValue().isEmpty())) {
                        log.error("*****构建订单详情信息：商品集合为空*****");
                        return successFlag;
                    }
//					actualPrice = getDeviceActualPrice(appProduct.getCabinetNo(), appProduct.getRoadNo(), product, device);
//					if ((null == actualPrice) || (null != actualPrice && actualPrice == 0)) {
//						log.error("*****构建订单详情信息：商品实际价格为0*****");
//						return successFlag;
//					}
                    product = productList.get(0);
                    actualPrice = appProduct.getPrice();
                } else {// 设备上的商品和后台货道的商品不一致
                    product = findProduct(device.getOrgId(), appProduct.getProductNo());
                    if (null == product) {
                        log.error("*****构建订单详情信息：商品信息为空*****");
                        return successFlag;
                    }
                    actualPrice = appProduct.getPrice();
                }

                OrderDetail detail = new OrderDetail();
                detail.initDefaultValue();
                detail.setOrderNo(order.getCode());// 外部订单号
                detail.setOrgId(device.getOrgId());
                detail.setPrice(actualPrice);// 根据设备同步时间情况，确定产品实际价格
                detail.setQty(getQty(entry.getValue()));
                detail.setSkuId(product.getId());
                detail.setSku(product.getSku());
                if(null != appProduct.getOrderType() && 2 == appProduct.getOrderType()){//只有抽奖活动才存商品编号
                    detail.setLotteryProductNo(product.getCode());
                }
                detail.setCreateTime(new Timestamp(System.currentTimeMillis()));
                detail.setCurrency("CNY");
                detail.setOrderType(null == appProduct.getOrderType() ? Commons.ORDER_TYPE_COMMON : appProduct.getOrderType());// 订单类型  0：普通订单  1：限时打折  2：抽奖活动
                detail.setDiscount(null == appProduct.getDiscount() ? 1 : MathUtil.round(appProduct.getDiscount(), 2));// 折扣
                order.addOrderDetail(detail);
                genericDao.save(detail);
            }
        }
        successFlag = true;
        return successFlag;
    }

    /**
     * 获取相同商品编码的总购买数量
     *
     * @param products
     * @return
     */
    public Integer getQty(List<com.vendor.vo.app.Product> products) {
        Integer qty = 0;
        for (com.vendor.vo.app.Product appProduct : products)
            qty += null == appProduct.getProductCount() ? 0 : appProduct.getProductCount();
        return qty;
    }

	/**
	 * 购物车订单的情况下， 商品间的关联度均加1
	 * @param order
	 */
	public void savePrductRelevancy(Order order) {
		List<OrderDetail> orderDetails = findOrderDetails(order.getCode());
		if (!orderDetails.isEmpty() && orderDetails.size() > 1) {
			for (OrderDetail outerOrderDetail : orderDetails) {
				for (OrderDetail innerOrderDetail : orderDetails) {
					if (outerOrderDetail.getSkuId().longValue() == innerOrderDetail.getSkuId().longValue())
						continue;
					
					// 保存关联推荐信息
					saveProductRecommend(order.getOrgId(), outerOrderDetail.getSkuId(), innerOrderDetail.getSkuId());
				}
			}
		}
	}
	
	/**
	 * 保存关联推荐信息
	 * @param mainProductId 主商品ID
	 * @param recommendProductId 推荐商品ID
	 */
	public void saveProductRecommend(Long orgId, Long mainProductId, Long recommendProductId) {
		ProductRecommend productRecommend = findProductRecommend(mainProductId, recommendProductId);
		if (null == productRecommend) { // 新增
			productRecommend = new ProductRecommend();
			productRecommend.setOrgId(orgId);
			productRecommend.setProductId(mainProductId);
			productRecommend.setRecommendProductId(recommendProductId);
			productRecommend.setRelevancy(1);
			genericDao.save(productRecommend);
		} else {// 修改
			productRecommend.setRelevancy(productRecommend.getRelevancy() + 1);
			genericDao.update(productRecommend);
		}
	}
	
	public ProductRecommend findProductRecommend(Long mainProductId, Long recommendProductId) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer("SELECT ");
		String cols = SQLUtils.getColumnsSQL(ProductRecommend.class, "C");
		buf.append(cols);
		buf.append(" FROM T_PRODUCT_RECOMMEND C WHERE C.PRODUCT_ID = ? AND C.RECOMMEND_PRODUCT_ID = ? ");
		args.add(mainProductId);
		args.add(recommendProductId);
		return genericDao.findT(ProductRecommend.class, buf.toString(), args.toArray());
	}
	
	public void saveRefundOrder(Refund refund, User user) {
		Order oldOrder = findOrderByOrderNo(refund.getOrderNo(), Commons.ORDER_STATE_FINISH);
		// 取得该订单已退款金额
		Double refundAmount = genericDao.findSingle(Double.class, "SELECT COALESCE(SUM(FEE_REFUND),0) FROM T_REFUND WHERE ORDER_NO = ? AND STATE = ?", refund.getOrderNo(), Commons.REFUND_STATE_SUCCESS);
		refundAmount = MathUtil.round(null == refundAmount ? 0 : refundAmount, 2);
		
		if (refund.getFeeRefund().doubleValue() > MathUtil.sub(oldOrder.getAmount(), refundAmount))
			throw new BusinessException("超出最大可退款金额！");
		
		refund.setPayNo(oldOrder.getPayCode());
		refund.setAmount(oldOrder.getAmount());
		refund.setOrderNo(oldOrder.getCode());
		refund.setType(oldOrder.getPayType());// 6:微信
		refund.setState(Commons.REFUND_STATE_NEW);
		refund.setOrgId(user.getOrgId());
		refund.setCode(idWorker.nextCode());
		refund.setCreateUser(user.getId());
		refund.setCreateTime(new Timestamp(System.currentTimeMillis()));
		
		genericDao.save(refund);
	}
	
    /**
     * 【设备APP接口】21.微信公众号支付
     *
     * @param orderNo
     * @return 预支付交易回话标识
     */
    @SuppressWarnings("rawtypes")
    public Map<String, Object> unifiedorder(Map<String, Object> map, String orderNo, String openId) throws Exception {
        Order order = findOrderByOrderNo(orderNo, Commons.ORDER_STATE_NEW);
        if (null == order) {
            map.put("resultMessage", "订单编号不存在");
            return map;
        }

        // 转换为设备组号
        String factoryDevNo = findFacDevNoByDevNo(order.getDeviceNo());

        // 检测设备是否离线，离线的话提示用户
        DevicePush devicePush = findDevicePush(factoryDevNo);
        List<String> deviceIds = new ArrayList<String>();
        deviceIds.add(devicePush.getPushDeviceId());
        // 查询设备状态
        MessagePusher pusher = new MessagePusher();
        for (GetDeviceInfosResponse.DeviceInfo deviceInfo : pusher.getDeviceInfos(deviceIds)) {
            if (deviceInfo.getDeviceId().equals(devicePush.getPushDeviceId())) {
                boolean isOnline = deviceInfo.getIsOnline();
                DeviceLog deviceLog = findDeviceLog(order.getDeviceNo(), Commons.DEVICE_STATUS_OFFLINE);//离线设备
                if (!isOnline) {// 离线
                    saveDeviceLog(order.getDeviceNo(), deviceLog, Commons.DEVICE_STATUS_OFFLINE);
                    //更新设备状态,异常
                    genericDao.execute(" UPDATE T_DEVICE SET STATE = ? WHERE DEV_NO = ? ", Commons.FAULT, order.getDeviceNo());

                    map.put("resultCode", -2);
                    map.put("resultMessage", "该设备处于离线状态，暂时不支持支付。");
                    return map;
                } else {
                    deleteDeviceLog(deviceLog);
                    //更新设备状态,正常
                    genericDao.execute(" UPDATE T_DEVICE SET STATE = ? WHERE DEV_NO = ? ", Commons.NORMAL, order.getDeviceNo());
                }
            }
        }

        // 设备在线
        // 调用统一下单接口，获取预支付交易回话标识
        // 微信公众号支付
        WxPayDto tpWxPay = new WxPayDto();
        tpWxPay.setBody("邦马特自动售货机商品订单");
        tpWxPay.setOrderId(order.getCode());
        tpWxPay.setSpbillCreateIp(spbillCreateIp);
        tpWxPay.setTotalFee(order.getAmount() + "");
        tpWxPay.setOpenId(openId);
        String unifiedorderStr = getUnifiedorderStr(tpWxPay);

        if (unifiedorderStr.indexOf("FAIL") != -1) {
            map.put("resultMessage", "统一下单接口返回错误");
            return map;
        }
        Map resultMap = GetWxOrderno.doXMLParse(unifiedorderStr);
        map.put("resultCode", 0);
        map.put("resultMessage", "发送成功！");

        Map<String, Object> signMap = new HashMap<String, Object>();
        signMap.put("appId", wechatAppId);
        signMap.put("timeStamp", Long.toString(System.currentTimeMillis() / 1000));
        signMap.put("nonceStr", getNonceStr());
        signMap.put("package", "prepay_id=" + (null == resultMap.get("prepay_id") ? "" : resultMap.get("prepay_id").toString()));
        signMap.put("signType", "MD5");
        map.put("appId", wechatAppId);
        map.put("timeStamp", signMap.get("timeStamp"));
        map.put("nonceStr", signMap.get("nonceStr"));
        map.put("package", signMap.get("package"));
        map.put("signType", signMap.get("signType"));
        map.put("paySign", WeChatUtil.getPaySignature(signMap, wechatKey));
        return map;
    }

    /**
     * 查询设备是否在线
     * @param deviceNo
     * @return
     * @throws Exception
     */
    public boolean saveDeviceIsOnline(String deviceNo) throws Exception {
        boolean isOnline = false;

        String factoryDevNo = findFacDevNoByDevNo(deviceNo);
        DevicePush devicePush = findDevicePush(factoryDevNo);
        List<String> deviceIds = new ArrayList<String>();
        deviceIds.add(devicePush.getPushDeviceId());
        // 查询设备状态
        MessagePusher pusher = new MessagePusher();
        for (GetDeviceInfosResponse.DeviceInfo deviceInfo : pusher.getDeviceInfos(deviceIds)) {
            if (deviceInfo.getDeviceId().equals(devicePush.getPushDeviceId())) {
                isOnline = deviceInfo.getIsOnline();
                log.info("*****【" + devicePush.getFactoryDevNo() +"】设备是否在线：*****" + isOnline);

                Device device = findDeviceByFacDevNo(devicePush.getFactoryDevNo());
                if (null == device)
                    continue;

                // 更新设备状态，并通知观察者
                device.setState(!isOnline ? Commons.FAULT : Commons.NORMAL);
                log.info("*****更新设备状态，并通知观察者*****");
                device.changeState(device.getState());
                genericDao.update(device);
            }
        }
        return isOnline;
    }

    /**
     * 【设备APP接口】22.微信公众号支付通知回调
     *
     * @return
     */
    @SuppressWarnings("rawtypes")
    public boolean saveOfficialAccountsAsyncNotify(String notityXml) throws Exception {
        WxPayResult wpr = getWxPayResult(notityXml);
        if (null == wpr) {
            log.error("****【微信公众号支付通知回调】*****notityXml解析失败***");
            return false;
        }
        if (!wechatAppId.equals(wpr.getAppid()) || !wechatMchId.equals(wpr.getMchId())) {
            log.error("****【微信公众号支付通知回调】*****appid和appsecret不匹配***");
            return false;
        }
        if (!"SUCCESS".equals(wpr.getResultCode())) {
            log.error("****【微信公众号支付通知回调】*****订单信息不存在，返回码不是SUCCESS***");
            return false;
        }
        // 支付成功
        // 更新订单状态
        Order order = findOrderByOrderNo(wpr.getOutTradeNo(), Commons.ORDER_STATE_NEW);
        if (null == order) {
            log.error("****【微信公众号支付通知回调】*****订单信息不存在，编号【" + wpr.getOutTradeNo() + "】***");
            return false;
        }
        order.setUsername(wpr.getOpenid());
        order.setPayCode(wpr.getTransactionId());
        order.setPayTime(DateUtil.stringToTimestamp(wpr.getTimeEnd(), DateUtil.YYYYMMDDHHMMSS_EN));
        order.setState(Commons.ORDER_STATE_FINISH);
        genericDao.update(order);
        log.info("***更改订单状态成功***" + order.getCode());

        Device device = findDeviceByDevNo(order.getDeviceNo());
        if (null == device) {
            log.error("****【微信公众号支付通知回调】*****设备不存在，订单的设备编号【" + order.getDeviceNo() + "】***");
            return false;
        }

        // 追加t_we_user表
        shareService.saveWeChatUser(order.getUsername(), device.getId(), device.getOrgId(), order.getDeviceNo());
        log.info("***saveWxUser成功***");

        // 追加一条充值交易流水
        saveTradeFlow(device.getOrgId(), order.getAmount());
        log.info("***saveTradeFlow成功***");

        // 通知之前，检测设备是否离线，离线的话不做通知出货，直接退款
        boolean isOnline = saveDeviceIsOnline(order.getDeviceNo());
        if (isOnline) {// 设备在线
            log.info("***设备在线，通知设备可以出货***");
            // 主动通知app，告知此订单号的商品已支付，可以出货了。
            MessagePusher pusher = new MessagePusher();
            List<String> devIDs = new ArrayList<String>();
            devIDs.add(findFacDevNoByDevNo(order.getDeviceNo()));

            // 查询订单详情
            List<OrderDetail> orderDetails = findOrderDetailOrderType(order.getCode());
            List<VProduct> list = new ArrayList<VProduct>();
            log.info("**********【订单类型："+orderDetails.get(0).getOrderType()+"】**********");

            if(2 == orderDetails.get(0).getOrderType()){//抽奖活动
                OrderDetail orderDetail2s = findOrderDetailLotteryProduct(order.getCode());
                VProduct product = new VProduct();
                product.setProduceId(orderDetail2s.getProductCode());
                product.setCount(orderDetail2s.getQty());
                list.add(product);
            }else{
                List<OrderDetail> orderDetail2s = findOrderDetails(order.getCode());
                for (OrderDetail orderDetail2 : orderDetail2s) {
                    VProduct product = new VProduct();
                    product.setProduceId(orderDetail2.getProductCode());
                    product.setCount(orderDetail2.getQty());
                    list.add(product);
                }
            }
            OrderData orderData = new OrderData();
            orderData.setNotifyFlag(Commons.NOTIFY_ORDER_PAY);// 订单支付通知flag
            orderData.setOrderNo(order.getCode());
            orderData.setState(1);
            orderData.setList(list);
            pusher.pushMessageToAndroidDevices(devIDs, ContextUtil.getJson(orderData), false);
        } else {
            log.info("***设备离线，进行退款操作***");
            // 设备离线，做退款操作，全额退款。
            Refund refund = new Refund();
            refund.setOrderNo(order.getCode());
            refund.setFeeRefund(order.getAmount());
            refund.setReason("支付成功后，设备离线。");
            User user = new User();
            user.setOrgId(order.getOrgId());
            user.setId(1L);
            saveRefundOrder(refund, user);// 保存退款信息

            if (Commons.PAY_TYPE_WX == refund.getType()) {//微信退款
                String refundStr = getWxRefundStr(refund);
                Map resultMap = GetWxOrderno.doXMLParse(refundStr);
                if (refundStr.indexOf("FAIL") != -1)
                    throw new BusinessException("*****微信退款接口返回错误:*****" + (resultMap.keySet().contains("return_msg") ? resultMap.get("return_msg") : ""));

                String resultCode = null == resultMap.get("result_code") ? "" : resultMap.get("result_code").toString();
                if (!"SUCCESS".equals(resultCode))
                    throw new BusinessException("*****微信退款提交业务失败*****");

                // 退款申请接收成功，结果通过退款查询接口查询
                refund.setState(Commons.REFUND_STATE_ING);//退款中
                genericDao.update(refund);
            } else if (Commons.PAY_TYPE_ALI == refund.getType()) {//支付宝退款
                // TODO
            }
        }

        // TODO 获取微信用户详细信息，创建后台唯一账户

        log.info("return true!!!");
        return true;
    }

    /**
     * 获取微信退款返回信息
     * @throws Exception
     */
    public String getWxRefundStr(Refund refund) throws Exception {
        // 1 参数
        // 商户号
        String mch_id = wechatMchId;
        // 随机字符串
        String nonce_str = getNonceStr();
        // 商户订单号
        String out_trade_no = refund.getOrderNo();
        // 商户退款单号
        String out_refund_no = refund.getCode();
        // 订单总金额以分为单位，不带小数点
        String totalFee = getMoney(refund.getAmount() + "");
        // 退款金额以分为单位，不带小数点
        String refund_fee = getMoney(refund.getFeeRefund() + "");
        // 操作员帐号, 默认为商户号
        String op_user_id = wechatMchId;

        SortedMap<String, String> packageParams = new TreeMap<String, String>();
        packageParams.put("appid", wechatAppId);
        packageParams.put("mch_id", mch_id);
        packageParams.put("nonce_str", nonce_str);
        packageParams.put("out_trade_no", out_trade_no);
        packageParams.put("out_refund_no", out_refund_no);
        packageParams.put("total_fee", totalFee);
        packageParams.put("refund_fee", refund_fee);
        packageParams.put("op_user_id", op_user_id);

        RequestHandler reqHandler = new RequestHandler(null, null);
        reqHandler.init(wechatAppId, wechatAppKey, wechatKey);

        String sign = reqHandler.createSign(packageParams);
        String xml = "<xml>" + "<appid>" + wechatAppId + "</appid>" + "<mch_id>" + mch_id + "</mch_id>" + "<nonce_str>" + nonce_str + "</nonce_str>" + "<sign>" + sign + "</sign>"
                + "<out_trade_no>" + out_trade_no + "</out_trade_no>" + "<out_refund_no>" + out_refund_no + "</out_refund_no>" + "<total_fee>" + totalFee
                + "</total_fee>" + "<refund_fee>" + refund_fee + "</refund_fee>" + "<op_user_id>" + op_user_id + "</op_user_id>" + "</xml>";

        // 证书验证+退款
        String refundStr = ClientCustomSSL.doRefund(getSrcFilePath("config/wechat", "apiclient_cert.p12"), wechatMchId, refundUrl, xml);
        log.info("*******微信退款返回信息：***********" + refundStr);
        return refundStr;
    }

    public String getSrcFilePath(String dir, String fileName) {
        return this.getClass().getResource("/").getPath() + dir + File.separator + fileName;
    }

    /**
     * 【设备APP接口】23.下载订单商品信息
     *
     * @param orderNo 订单编号
     * @return
     */
    public Map<String, Object> findSyncCartProductInfo(Map<String, Object> map, String orderNo) throws Exception {
        List<com.vendor.vo.app.Product> products = new ArrayList<com.vendor.vo.app.Product>();
        Order order = findOrderByOrderNo(orderNo, null);
        if (null == order) {
            map.put("resultMessage", "订单编号不存在");
            return map;
        }

        // 获取订单详情（商品详情）
        List<OrderDetail> orderDetails = findOrderDetails(orderNo);
        for (OrderDetail detail : orderDetails) {
            com.vendor.vo.app.Product product = new com.vendor.vo.app.Product();
            product.setProductName(detail.getSkuName());
            product.setProductCount(detail.getQty());
            products.add(product);
        }

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setOrderState(order.getState());
        shoppingCart.setAmount(order.getAmount());
        shoppingCart.setProducts(products);

        map.put("resultCode", 0);
        map.put("resultMessage", "发送成功！");
        map.put("data", shoppingCart);
        return map;
    }

    /**
     * 【设备APP接口】24.更改后台库存接口：设备出货指令成功后，通知后台更改库存
     *
     * @param qrCodeOrders
     * @return
     */
    public Map<String, Object> saveUploadVendorStock(Map<String, Object> map, QRCodeOrders qrCodeOrders) throws Exception {
        // 转换为内部设备编号
        qrCodeOrders.setMachineNum(findDevNoByFacDevNo(qrCodeOrders.getMachineNum()));

        Device device = findDeviceByDevNo(qrCodeOrders.getMachineNum());
        if (null == device) {
            log.info("*****【发生错误】***设备编号不存在*****");
            map.put("resultMessage", "设备编号不存在");
            return map;
        }

        Order order = findOrderByOrderNo(qrCodeOrders.getOrderNo(), Commons.ORDER_STATE_FINISH);
        if (null == order) {
            log.info("*****【发生错误】***订单信息不存在*****");
            map.put("resultMessage", "订单信息不存在");
            return map;
        }

        if (!StringUtils.isEmpty(qrCodeOrders.getOrderType()) && "2".equals(qrCodeOrders.getOrderType())) {
            log.info("********抽奖活动的订单，需要重置下skuId*****");
            Product product = genericDao.findT(Product.class, "SELECT ID,SKU FROM T_PRODUCT WHERE CODE=? AND ORG_ID=? AND STATE!=?", qrCodeOrders.getProductNo(), order.getOrgId(), Commons.PRODUCT_STATE_TRASH);
            OrderDetail orderDetail = genericDao.findT(OrderDetail.class, "SELECT * FROM T_ORDER_DETAIL WHERE ORDER_NO=?", qrCodeOrders.getOrderNo());
            orderDetail.setSkuId(product.getId());
            orderDetail.setSku(product.getSku());
            genericDao.update(orderDetail);
            log.info("********重置成功*****");
        }

        // 更改库存
        log.info("*****更改库存***开始***");
        boolean updateStock = updateStock(qrCodeOrders.getProductNo(), qrCodeOrders.getType(), device, Integer.parseInt(qrCodeOrders.getRoadNo()), qrCodeOrders.getProductCount(), qrCodeOrders.getCabinetNo());
        if (!updateStock) {
            log.error("***qrCodeOrders.getProductNo():**" + qrCodeOrders.getProductNo());
            log.error("***qrCodeOrders.getType():**" + qrCodeOrders.getType());
            log.error("***device.getId():**" + device.getId());
            log.error("***Integer.parseInt(qrCodeOrders.getRoadNo()):**" + Integer.parseInt(qrCodeOrders.getRoadNo()));
            log.error("***qrCodeOrders.getProductCount():**" + qrCodeOrders.getProductCount());
            log.error("***qrCodeOrders.getCabinetNo():**" + qrCodeOrders.getCabinetNo());
            log.error("***updateStock失败***");
            throw new BusinessException("***updateStock失败***");
        }
        log.info("***updateStock成功***");
        log.info("*****更改库存***结束***");


        map.put("resultCode", 0);
        map.put("resultMessage", "发送成功！");
        return map;
    }

    /**
     * 【设备APP接口】25.获取jsconfig参数信息
     *
     * @param url
     * @return
     */
    public Map<String, Object> findJsConfig(Map<String, Object> map, String url, String openId) throws Exception {
        // 获取JsConfig参数信息
        WxAccessConf wxAccessConf = WeChatUtil.getJsapiTicket(openId, wechatAppId, wechatAppKey);
        if (null == wxAccessConf) {
            map.put("resultMessage", "获取jsconfig参数信息失败");
            return map;
        }

        String jsTicket = wxAccessConf.getJsapiTicket();
        Map<String, String> ret = Sign.sign(jsTicket, url);

        map.put("appId", wechatAppId);
        map.put("url", ret.get("url"));
        map.put("timestamp", ret.get("timestamp"));
        map.put("nonceStr", ret.get("nonceStr"));
        map.put("signature", ret.get("signature"));
        map.put("resultCode", 0);
        map.put("resultMessage", "发送成功！");
        return map;
    }

    /**
     * 【设备APP接口】29.获取商品信息接口---将服务器端设置的，本机设备销售的商品数据下载到本地。
     * @param machineNum 设备组号
     * @return
     */
    public ResultList<SyncProductInfo> findGoodsInfo(ResultList<SyncProductInfo> rl, String machineNum) throws Exception {
        // 转换为内部设备编号
        machineNum = findDevNoByFacDevNo(machineNum);

        List<SyncProductInfo> prodList = new ArrayList<SyncProductInfo>();
        Device device = findDeviceByDevNo(machineNum);
        if (null == device) {
            rl.setResultMessage("设备编号不存在");
            return rl;
        }
        List<Product> products = findProductByDevNo(machineNum);
        // 查询设备总销售额
        double deviceAmount = MathUtil.round(findAmountByDevNo(device), 2);
        for (Product product : products) {
            SyncProductInfo prodInfo = new SyncProductInfo();
            prodInfo.setProductNo(product.getCode());
            prodInfo.setProductName(product.getSkuName());
            prodInfo.setOriginal(product.getOrigin());
            prodInfo.setSpec(null == product.getWeight() ? "" : product.getWeight() + "");// 规格
            prodInfo.setPrice(null == product.getPriceOnLine() ? 0 : product.getPriceOnLine());
            // 热销指数
            if (deviceAmount == 0) {
                prodInfo.setHots(0L);
            } else {
                // 查询设备指定商品的销售额
                double prodAmount = MathUtil.round(findAmountByProductId(device, product.getId()), 2);
                Long hots = Math.round(MathUtil.div(MathUtil.mul(prodAmount, 100), deviceAmount));
                prodInfo.setHots(hots);
            }
            prodInfo.setDesc(product.getDescription());
            String images = product.getImages();
            String pic = StringUtils.isEmpty(images) ? "" : dictionaryService.getFileServer() + images.split(";")[0].split(",")[3];
            prodInfo.setPic(pic);
            prodList.add(prodInfo);
        }
        rl.setResultCode(0);// 成功
        rl.setResultList(prodList);
        rl.setResultMessage("发送成功!");
        return rl;
    }

    /**
     * 查询本机设备销售的商品数据
     *
     * @param deviceNumber 设备的厂家编号
     * @return
     */
    public List<Product> findProductByDevNo(String devNo) {
        StringBuffer buf = new StringBuffer(" SELECT ");
        String columns = SQLUtils.getColumnsSQL(Product.class, "A") + ",O.NAME";
        buf.append(columns);
        buf.append(" AS orgName, DA.PRICE_ON_LINE as priceOnLine, STRING_AGG(B.ID||','||B.NAME||','||B.TYPE||','||B.REAL_PATH, ';' ORDER BY B.ID DESC) AS images");
        buf.append(" FROM T_PRODUCT A LEFT JOIN SYS_ORG O ON A.ORG_ID=O.ID LEFT JOIN T_FILE B ON A.ID=B.INFO_ID AND B.TYPE=? ");
        buf.append(" LEFT JOIN T_DEVICE_AISLE DA ON A.ID = DA.PRODUCT_ID ");
        buf.append(" LEFT JOIN T_DEVICE D ON D.ID = DA.DEVICE_ID ");
        List<Object> args = new ArrayList<>();
        buf.append(" WHERE A.STATE!=? ");
        args.add(Commons.PRODUCT_STATE_TRASH);
        buf.append(" AND D.DEV_NO=? ");
        args.add(devNo);

        buf.append(" GROUP BY ").append(columns).append(", DA.PRICE_ON_LINE");
        args.add(0, Commons.FILE_PRODUCT);
        return genericDao.findTs(Product.class, buf.toString(), args.toArray());
    }

    /**
     * 【设备APP接口】31.货柜商品同步数据接口
     * @param machineNum 设备组号
     * @param cabinetNo 货柜号
     * @return
     */
    public ResultList<SyncRoadSalesInfo> saveUpdataMachineCabGoods(ResultList<SyncRoadSalesInfo> rl, String machineNum, String cabinetNo) throws Exception {
        // 转换为内部设备编号
        machineNum = findDevNoByFacDevNo(machineNum);

        List<SyncRoadSalesInfo> roadSalesList = new ArrayList<SyncRoadSalesInfo>();
        Device device = findDeviceByDevNo(machineNum);
        if (null == device) {
            rl.setResultMessage("设备编号不存在");
            return rl;
        }
        List<DeviceAisle> deviceAisles = findProductShelveInfoDeviceAisles(device, cabinetNo);

        // 查询设备总销售额
        double deviceAmount = MathUtil.round(findAmountByDevNo(device), 2);

        for (DeviceAisle deAisle : deviceAisles) {
            SyncRoadSalesInfo roadSalesInfo = new SyncRoadSalesInfo();

            List<Object> exceptRoadNos = findDeviceRoadExcepts(Commons.FACTORY_CODE_YC, deAisle.getModel());
            if (null != exceptRoadNos && !exceptRoadNos.isEmpty()) {
                // 弹簧机或履带机的情况下，去除双货道的第二个货道号
                if (exceptRoadNos.contains(deAisle.getAisleNum()))
                    continue;
            }

            // 构造出货用【货柜号】和【货道号】
            refactoringDeviceAisle(deAisle);
            roadSalesInfo.setCabinetNo(StringUtils.isEmpty(deAisle.getShipmentCabinetNo()) ? "" : deAisle.getShipmentCabinetNo());// 出货指令用货柜号
            roadSalesInfo.setRoadNo(StringUtils.isEmpty(deAisle.getShipmentAisleNum()) ? "" : deAisle.getShipmentAisleNum());// 出货指令用货道号
            roadSalesInfo.setDbCabinetNo(StringUtils.isEmpty(deAisle.getCabinetNo()) ? "" : deAisle.getCabinetNo());// DB用货柜号
            roadSalesInfo.setDbRoadNo(deAisle.getAisleNum() == null ? "" : deAisle.getAisleNum() + "");// DB用货道号
            roadSalesInfo.setProductNo(StringUtils.isEmpty(deAisle.getProductCode()) ? "" : deAisle.getProductCode());// 商品编码
            roadSalesInfo.setProductName(StringUtils.isEmpty(deAisle.getProductName()) ? "" : deAisle.getProductName());// 商品名称
            roadSalesInfo.setOriginal(StringUtils.isEmpty(deAisle.getOriginal()) ? "" : deAisle.getOriginal());// 产地
            roadSalesInfo.setSpec(StringUtils.isEmpty(deAisle.getSpec()) ? "" : deAisle.getSpec());// 商品规格
            roadSalesInfo.setModel(StringUtils.isEmpty(deAisle.getModel()) ? "" : deAisle.getModel());//设备型号
            roadSalesInfo.setSpuCode(StringUtils.isEmpty(deAisle.getProductCode()) ? "" : deAisle.getProductCode());//-----新增的聚合code

            if(!StringUtils.isEmpty(deAisle.getProductCode())){//是否是显示的主商品 0不是 1 是
                roadSalesInfo.setWetherSpuMainGoods(deAisle.getProductCode().equals(deAisle.getProductCombination()) ? 1 : 0);
            }

            roadSalesInfo.setGoodsSorting(StringUtils.isEmpty(deAisle.getSerialNumber()) ? null : deAisle.getSerialNumber());

            // 商品图文描述
            StringBuffer buffer = new StringBuffer("");
            if (!StringUtils.isEmpty(deAisle.getDesc())) {
                String[] arr = deAisle.getDesc().split("src=\"");
                for (String img : arr)
                    if (img.indexOf("\"") != -1)
                        buffer.append(img.substring(0, img.indexOf("\""))).append(";");
                if (buffer.length() > 0)
                    buffer.setLength(buffer.length() - 1);
            }
            roadSalesInfo.setDesc(buffer.toString());

            roadSalesInfo.setType(deAisle.getType() == null ? 0 : deAisle.getType());//商品类型
            roadSalesInfo.setCagetory_type(deAisle.getCategory() == null ? 0 : deAisle.getCategory());// 商品类别
            roadSalesInfo.setBasePrice(MathUtil.round(deAisle.getPrice() == null ? 0 : deAisle.getPrice(), 2));//商品标准价格
            roadSalesInfo.setZhekou_num(MathUtil.round(deAisle.getDiscountValue() == null ? 1 : deAisle.getDiscountValue(), 2));// 折扣值

            roadSalesInfo.setDeletePrice(MathUtil.round(deAisle.getPriceOnLine() == null ? 0 : deAisle.getPriceOnLine(), 2));//零售价
            roadSalesInfo.setPrice(MathUtil.round(MathUtil.mul(roadSalesInfo.getDeletePrice(), roadSalesInfo.getZhekou_num()), 2));// 折后价

            roadSalesInfo.setState(deAisle.getSellable() == null ? Commons.SELLABLE_TRUE : deAisle.getSellable());//商品可售状态  0：不可售  1：可售
            // 商品图片
            String images = deAisle.getImages();
            if (StringUtils.isEmpty(images)) {
                roadSalesInfo.setPicUrl("");
                roadSalesInfo.setPicDetailUrl("");
            } else {
                String[] imagesArr = images.split(";");
                boolean isSetProduct = false;
                boolean isSetProductDetail = false;
                for (String image : imagesArr) {
                    if (isSetProduct && isSetProductDetail)
                        break;
                    if (!isSetProduct && Commons.FILE_PRODUCT == Integer.valueOf(image.split(",")[2])) {
                        isSetProduct = true;
                        roadSalesInfo.setPicUrl(dictionaryService.getFileServer() + image.split(",")[3]);
                    } else if (!isSetProductDetail && Commons.FILE_PRODUCT_DETAIL == Integer.valueOf(image.split(",")[2])) {
                        isSetProductDetail = true;
                        roadSalesInfo.setPicDetailUrl(dictionaryService.getFileServer() + image.split(",")[3]);
                    }
                }
            }

            // 热销指数
            if (deviceAmount == 0) {
                roadSalesInfo.setHots(0L);
            } else {
                // 查询设备指定商品的销售额
                double prodAmount = MathUtil.round(findAmountByProductId(device, deAisle.getProductId()), 2);
                Long hots = Math.round(MathUtil.div(MathUtil.mul(prodAmount, 100), deviceAmount));
                roadSalesInfo.setHots(hots);
            }
            // 货道容量
            roadSalesInfo.setShelf_amount(deAisle.getCapacity());//货道容量

            roadSalesList.add(roadSalesInfo);
        }
        rl.setResultCode(0);// 成功
        rl.setResultList(roadSalesList);
        rl.setResultMessage("发送成功!");
        return rl;
    }

    /**
     * 取得货道容量
     * @param deviceAisle
     */
    public int getShelfAmount(DeviceAisle deAisle) {
        //货道容量
        int shelfAmount = 0;

        Integer perimeter = deAisle.getPerimeter() == null ? 0 : deAisle.getPerimeter();// 商品周长，单位mm
        if (perimeter == 0)
            return shelfAmount;

        // 直径
        double diameter = MathUtil.div(0, BigDecimal.ROUND_CEILING, Double.valueOf(perimeter + ""), Math.PI);
        String aisleNum = deAisle.getAisleNum() + "";


        // 取得货道规则
        List<DeviceRule> deviceRules = findDeviceRules(Commons.FACTORY_CODE_YC);// 易触

        // 按型号分组
        Map<String ,List<DeviceRule>> deviceRulesMap = group(deviceRules, new GroupBy<String>(){
            @Override
            public String groupby(Object obj) {
                DeviceRule rule = (DeviceRule)obj;
                return rule.getModel();
            }
        });

        forDeviceRule : for (Map.Entry<String ,List<DeviceRule>> entry : deviceRulesMap.entrySet()) {
            String model = entry.getKey();
            if (!deAisle.getModel().equals(model))
                continue;

            // 同一型号
            for (DeviceRule rule : entry.getValue()) {
                // 货道容量 = 货道长度/直径
                switch (deAisle.getModel()) {
                    case Commons.DEVICE_MODEL_DRINK_SMALL://小型饮料机
                    case Commons.DEVICE_MODEL_DRINK://大型饮料机
                        if (Arrays.asList(rule.getRoadCombo().split(",")).contains(aisleNum)) {
                            shelfAmount = calcShelfAmount(rule.getRoadLength(), diameter);
                            break forDeviceRule;
                        }
                        break;
                    case Commons.DEVICE_MODEL_SPRING://弹簧机
                        if (Arrays.asList(rule.getRoadCombo().split(",")).contains(aisleNum)) {
                            shelfAmount = rule.getRoadCapacity();
                            break forDeviceRule;
                        }
                        break;
                    case Commons.DEVICE_MODEL_CATERPILLAR://履带机（暂时都是5）
                    case Commons.DEVICE_MODEL_GRID64://64门格子柜
                    case Commons.DEVICE_MODEL_GRID40://40门格子柜
                    case Commons.DEVICE_MODEL_GRID60://60门格子柜
                        shelfAmount = rule.getRoadCapacity();
                        break forDeviceRule;
                    default:
                        break;
                }
            }

        }
        return shelfAmount;
    }

    /**
     * 计算货道容量
     * @param roadLength 货道长度
     * @param diameter 货道当前商品直径
     * @return
     */
    public int calcShelfAmount(Integer roadLength, double diameter) {
        return (int) MathUtil.div(0, BigDecimal.ROUND_FLOOR, Double.valueOf(roadLength + ""), diameter);
    }

    /**
     * 根据orders生成订单信息及订单号
     *
     * @param order
     * @param payType 6 微信 1支付宝
     * @return 订单流水号
     */
    public String saveQRCodeOrders(QRCodeOrders qrCodeOrders, Integer payType) throws Exception {
        if (null == qrCodeOrders)
            throw new BusinessException("订单商品信息为空！");
        if (StringUtils.isEmpty(qrCodeOrders.getMachineNum()) || null == qrCodeOrders.getProducts() || qrCodeOrders.getProducts().isEmpty())
            throw new BusinessException("订单商品信息不完整！");

        Device device = findDeviceByDevNo(qrCodeOrders.getMachineNum());
        if (null == device)
            throw new BusinessException("设备编号不存在");

        // 生成订单信息
        Order order = saveOrder(qrCodeOrders, device, payType);
        if (null == order)
            throw new BusinessException("生成订单出错！");

        // 推送通知设备用户扫码进入了收银台界面
//		ScanPayData scanPayData = new ScanPayData();
//		scanPayData.setNotifyFlag(Commons.NOTIFY_SCAN_PAY);// 扫描二维码通知Flag
//		MessagePusher pusher = new MessagePusher();
//		List<String> devIDs = new ArrayList<String>();
//		devIDs.add(findFacDevNoByDevNo(order.getDeviceNo()));
//		pusher.pushMessageToAndroidDevices(devIDs, ContextUtil.getJson(scanPayData), false);

        return order.getCode();
    }

    /**
     * 生成订单信息
     *
     * @param shoppingCart
     * @param device
     * @param payType 6 微信 1支付宝
     * @return
     */
    public Order saveOrder(QRCodeOrders qrCodeOrders, Device device, Integer payType) {
        // 生成订单信息
        Order order = new Order();
        order.initDefaultValue();
        order.setCode(RandomUtil.getCharAndNumr(32));// 外部订单号
        order.setCreateTime(new Timestamp(System.currentTimeMillis()));
        order.setDeviceNo(qrCodeOrders.getMachineNum());// 设备编号

        // 店铺信息
        PointPlace pointPlace = findPointPlaceById(device.getPointId());
        if (null == pointPlace)
            return null;
        order.setPointNo(pointPlace.getPointNo());

        // 商品总金额
        Double amount = getAmount(qrCodeOrders.getProducts(), device);
        if ((null == amount) || (null != amount && amount == 0))
            return null;
        order.setAmount(amount);// 订单总金额
        order.setState(Commons.ORDER_STATE_NEW);// 待付款
        order.setOrgId(device.getOrgId());
        order.setPayType(payType);// 支付类型 6 微信 1支付宝
        order.setCoupone(0D);
        int count = genericDao.save(order);
        if (count > 0) {
            // 构建订单详情信息
            boolean successFlag = saveOrderDetails(qrCodeOrders.getProducts(), order, device);
            if (!successFlag)
                return null;
        }
        return order;
    }

    /**
     * 构建QRCodeOrders对象
     * @param machNo
     * @param prods
     * @return
     * @throws Exception
     */
    public QRCodeOrders getQRCodeOrders(String machNo, String prods) throws Exception {
        // 转换为内部设备编号
        machNo = findDevNoByFacDevNo(machNo);

        if (StringUtils.isEmpty(machNo) || StringUtils.isEmpty(prods))
            return null;

        QRCodeOrders qrCodeOrders = new QRCodeOrders();
        List<com.vendor.vo.app.Product> products = new ArrayList<com.vendor.vo.app.Product>();
        qrCodeOrders.setMachineNum(machNo);

        String[] prodsArr = prods.split(";");
        for (String prodStr : prodsArr) {
            // 格式：商品编码,购买数量,货柜号,货道号，价格，商品类型
            // 格式：商品编码,购买数量,货柜号,货道号，价格，商品类型，订单类型，折扣数
            // 格式：商品编码,购买数量,价格，订单类型，折扣数
            String[] prodStrArr = prodStr.split(",");

            if (prodStrArr.length != 6 && prodStrArr.length != 8 && prodStrArr.length != 5)
                return null;
            com.vendor.vo.app.Product product = new com.vendor.vo.app.Product();
            product.setProductNo(prodStrArr[0]);
            product.setProductCount(Integer.valueOf(prodStrArr[1]));
            if (prodStrArr.length == 6) {// 格式：商品编码,购买数量,货柜号,货道号，价格，商品类型
                product.setPrice(Double.valueOf(prodStrArr[4]));
            } else if (prodStrArr.length == 8) {// 格式：商品编码,购买数量,货柜号,货道号，价格，商品类型，订单类型，折扣数
                product.setPrice(Double.valueOf(prodStrArr[4]));

                Integer orderType = Integer.valueOf(prodStrArr[6]);
                if (Commons.ORDER_TYPE_COMMON != orderType && Commons.ORDER_TYPE_DISCOUNT != orderType && Commons.ORDER_TYPE_LOTTERY != orderType)
                    return null;

                product.setOrderType(orderType);
                product.setDiscount(Double.valueOf(prodStrArr[7]));
            } else if (prodStrArr.length == 5) {// 格式：商品编码,购买数量,价格，订单类型，折扣数
                product.setPrice(Double.valueOf(prodStrArr[2]));

                Integer orderType = Integer.valueOf(prodStrArr[3]);
                if (Commons.ORDER_TYPE_COMMON != orderType && Commons.ORDER_TYPE_DISCOUNT != orderType && Commons.ORDER_TYPE_LOTTERY != orderType)
                    return null;

                product.setOrderType(orderType);
                product.setDiscount(Double.valueOf(prodStrArr[4]));
            }
            products.add(product);
        }
        qrCodeOrders.setProducts(products);

        return qrCodeOrders;
    }

    /**
     * 【设备APP接口】32.同步换货商品数据接口
     * @return
     */
    public ResultBase saveUpdateReplaceProducts(ResultBase rb, ProductReplacement replacement) throws Exception {
        // 转换为内部设备编号
        replacement.setMachineNum(findDevNoByFacDevNo(replacement.getMachineNum()));

        Device device = findDeviceByDevNo(replacement.getMachineNum());
        if (null == device) {
            log.error("设备编号不存在");
            rb.setResultMessage("设备编号不存在");
            return rb;
        }
        User user = findUserById(replacement.getUserId());
        if (null == user) {
            log.error("当前用户不存在");
            rb.setResultMessage("当前用户不存在");
            return rb;
        }

        // 最新的货道信息
        DeviceAisle deviceAisle = findDeviceAisle(replacement.getMachineNum(), replacement.getCabinetNo(), Integer.valueOf(replacement.getRoadNo()));
        if (null == deviceAisle) {
            log.error("设备货道信息不存在");
            rb.setResultMessage("设备货道信息不存在");
            return rb;
        }
        // 换货前的商品
        Product oldProduct = findProduct(device.getOrgId(), replacement.getProductCode(), replacement.getType());
        if (null != oldProduct) {
            replacement.setProductId(oldProduct.getId());
            replacement.setProductCode(oldProduct.getCode());
            replacement.setProductName(oldProduct.getSkuName());
        }

        // 追加换货记录信息
        replacement.setDeviceNo(replacement.getMachineNum());
        replacement.setStock(deviceAisle.getStock());
        replacement.setCapacity(deviceAisle.getCapacity());
        replacement.setSupplementNo(deviceAisle.getSupplementNo());
        replacement.setReplaceProductid(deviceAisle.getProductId());
        replacement.setReplaceProductCode(deviceAisle.getProductCode());
        replacement.setReplaceProductName(deviceAisle.getProductName());
        replacement.setOrgId(user.getOrgId());
        replacement.setCreateUser(user.getId());
        replacement.setCreateTime(new Timestamp(System.currentTimeMillis()));
        genericDao.save(replacement);

        rb.setResultCode(0);// 成功
        rb.setResultMessage("发送成功!");
        return rb;
    }

    /**
     * 【设备APP接口】33.上传机器阿里推送设备ID接口
     *
     * @return
     */
    public ResultBase savcePushDeviceId(ResultBase rb, String machineNum, String deviceId) throws Exception {
        // 转换为内部设备编号
        String devNo = findDevNoByFacDevNo(machineNum);

        Device device = findDeviceByCode(devNo);
        if (null == device) {
            rb.setResultMessage("设备编号不存在");
            return rb;
        }

        // 查询设备推送关联信息
        DevicePush devicePush = findDevicePush(machineNum);
        if (null == devicePush) {
            devicePush = new DevicePush();
            devicePush.setFactoryDevNo(machineNum);
            devicePush.setPushDeviceId(deviceId);
            genericDao.save(devicePush);
        } else {
            devicePush.setPushDeviceId(deviceId);
            genericDao.update(devicePush);
        }

        rb.setResultCode(0);// 成功
        rb.setResultMessage("发送成功!");
        return rb;
    }

    /**
     * 定时任务更新设备的状态
     * @throws Exception
     */
    public void saveDeviceStateJob() throws Exception {
        log.info("*****【更新设备状态】定时任务***开始***");
        // 取得所有的设备推送ID信息
        List<DevicePush> devicePushs = findDevicePushs();
        List<String> deviceIds = new ArrayList<String>();
        for (DevicePush push : devicePushs)
            deviceIds.add(push.getPushDeviceId());

        // 查询设备状态
        MessagePusher pusher = new MessagePusher();
        List<GetDeviceInfosResponse.DeviceInfo> deviceInfos = pusher.getDeviceInfos(deviceIds);
        for (DevicePush push : devicePushs) {
            for (GetDeviceInfosResponse.DeviceInfo deviceInfo : deviceInfos) {
                if (deviceInfo.getDeviceId().equals(push.getPushDeviceId())) {
                    boolean isOnline = deviceInfo.getIsOnline();
                    log.info("*****【" + push.getFactoryDevNo() +"】设备是否在线：*****" + isOnline);

                    Device device = findDeviceByFacDevNo(push.getFactoryDevNo());
                    if (null == device)
                        continue;

                    // 更新设备状态，并通知观察者
                    device.setState(!isOnline ? Commons.FAULT : Commons.NORMAL);
                    log.info("*****更新设备状态，并通知观察者*****");
                    device.changeState(device.getState());
                    genericDao.update(device);
                }
            }
        }
        log.info("*****【更新设备状态】定时任务***结束***");
    }

    public DeviceAisle findDeviceAisle(String devNo, String cabinetNo, Integer roadNo) {
        StringBuffer buffer = new StringBuffer("SELECT ");
        buffer.append(SQLUtils.getColumnsSQL(DeviceAisle.class, "A"));
        buffer.append(" FROM T_DEVICE_AISLE A ");
        buffer.append(" LEFT JOIN T_DEVICE D ON A.DEVICE_ID = D.ID ");
        buffer.append(" LEFT JOIN T_CABINET C ON A.CABINET_ID = C.ID AND C.DEVICE_ID = D.ID ");
        buffer.append(" WHERE 1=1 ");
        buffer.append(" AND D.DEV_NO = ? AND C.CABINET_NO = ? AND A.AISLE_NUM = ? ");
        return genericDao.findT(DeviceAisle.class, buffer.toString(), devNo, cabinetNo, roadNo);
    }

    public List<DeviceAisle> findProductShelveInfoDeviceAisles(Device device, String cabinetNo) {
        StringBuffer buffer = new StringBuffer();
        List<Object> args = new ArrayList<Object>();

        buffer.append(" SELECT DA.ID,DA.AISLE_NUM,DA.PRODUCT_ID,DA.PRICE, DA.SELLABLE, DA.PRICE_ON_LINE,DA.CAPACITY, DA.DISCOUNT_VALUE, T.productCode,T.productName,T.perimeter,T.TYPE,T.category,T.original,T.spec,T.DESC,CAB.CABINET_NO as cabinetNo,CAB.model,T.images,T.productCombination,SR.SERIAL_NUMBER serialNumber");
        buffer.append(" FROM T_DEVICE_AISLE DA ");
        buffer.append(" LEFT JOIN ( ");

        buffer.append(" SELECT A.ID, A.AISLE_NUM, A.PRODUCT_ID, A.PRICE, A.SELLABLE, A.PRICE_ON_LINE, A.CAPACITY, A.DISCOUNT_VALUE, P.CODE as productCode, P.SKU_NAME as productName, P.PERIMETER as perimeter, P.TYPE as type, P.CATEGORY as category ");
        buffer.append(" , P.ORIGIN as original, P.SPEC as spec, P.DESCRIPTION as desc,P.PRODUCT_COMBINATION productCombination ");
        buffer.append(" , C.CABINET_NO as cabinetNo, C.MODEL as model, STRING_AGG(B.ID||','||B.NAME||','||B.TYPE||','||B.REAL_PATH, ';' ORDER BY B.ID DESC) AS images");
        buffer.append(" FROM T_DEVICE_AISLE A ");
        buffer.append(" LEFT JOIN T_PRODUCT P ON A.PRODUCT_ID = P.ID ");
        buffer.append(" LEFT JOIN SYS_ORG O ON P.ORG_ID=O.ID LEFT JOIN T_FILE B ON P.ID=B.INFO_ID AND (B.TYPE=? OR B.TYPE = ?) ");
        buffer.append(" LEFT JOIN T_CABINET C ON A.CABINET_ID = C.ID ");
        buffer.append(" WHERE 1 = 1 ");
        buffer.append(" AND P.STATE!=? ");
        args.add(Commons.PRODUCT_STATE_TRASH);
        buffer.append(" GROUP BY ").append(" A.ID , A.AISLE_NUM, A.PRODUCT_ID, A.PRICE, A.SELLABLE, A.PRICE_ON_LINE, A.CAPACITY, A.DISCOUNT_VALUE, P.CODE, P.SKU_NAME, P.PERIMETER, P.TYPE, P.CATEGORY, C.CABINET_NO, C.MODEL ").append(",P.ORIGIN,P.SPEC,P.DESCRIPTION,P.PRODUCT_COMBINATION ");
        args.add(0, Commons.FILE_PRODUCT);
        args.add(1, Commons.FILE_PRODUCT_DETAIL);

        buffer.append(" ) T ON T.ID = DA.ID ");
        buffer.append(" LEFT JOIN T_CABINET CAB ON DA.CABINET_ID = CAB.ID ");
        buffer.append(" LEFT JOIN T_SORTING_RECOMMENDED SR ON SR.PRODUCT_ID=DA.PRODUCT_ID ");
        buffer.append(" WHERE 1 = 1 ");
        buffer.append(" AND DA.DEVICE_ID = ? ");
        args.add(device.getId());

        if (!StringUtils.isEmpty(cabinetNo)) {
            buffer.append(" AND CAB.CABINET_NO = ? ");
            args.add(cabinetNo);
        }
        buffer.append(" GROUP BY DA.ID,DA.AISLE_NUM,DA.PRODUCT_ID,DA.PRICE, DA.SELLABLE, DA.PRICE_ON_LINE,DA.CAPACITY, DA.DISCOUNT_VALUE, T.productCode,T.productName,T.perimeter,T.TYPE,T.category,T.original,T.spec,T.DESC,CAB.CABINET_NO,CAB.model,T.images,T.productCombination,SR.SERIAL_NUMBER");
        buffer.append(" ORDER BY DA.AISLE_NUM ");

        return genericDao.findTs(DeviceAisle.class, buffer.toString(), args.toArray());
    }

    public PointPlace findPointPlaceById(Long id) {
        StringBuffer buffer = new StringBuffer("SELECT ");
        buffer.append(SQLUtils.getColumnsSQL(PointPlace.class, "A"));
        buffer.append(" FROM T_POINT_PLACE A WHERE A.ID = ? ");

        buffer.append(" AND A.STATE != ? ");
        return genericDao.findT(PointPlace.class, buffer.toString(), id, Commons.POINT_PLACE_STATE_DELETE);
    }
    public PointPlace findPointPlace(String pointNo) {
        StringBuffer buffer = new StringBuffer("SELECT ");
        buffer.append(SQLUtils.getColumnsSQL(PointPlace.class, "A"));
        buffer.append(" FROM T_POINT_PLACE A WHERE A.POINT_NO = ? ");

        buffer.append(" AND A.STATE != ? ");
        return genericDao.findT(PointPlace.class, buffer.toString(), pointNo, Commons.POINT_PLACE_STATE_DELETE);
    }

    public User findUserById(Long Id) {
        StringBuffer buffer = new StringBuffer("SELECT ");
        buffer.append(SQLUtils.getColumnsSQL(User.class, "A"));
        buffer.append(" FROM SYS_USER A WHERE A.ID = ? ");
        return genericDao.findT(User.class, buffer.toString(), Id);
    }

    public Device findDeviceById(Long Id) {
        StringBuffer buffer = new StringBuffer("SELECT ");
        buffer.append(SQLUtils.getColumnsSQL(Device.class, "A"));
        buffer.append(" FROM T_DEVICE A WHERE A.DEVICE_ID=? AND A.PRODUCT_CODE=? AND A.AISLE_NUM=? AND A.CABINET_ID = ? ");
        return genericDao.findT(Device.class, buffer.toString(), Id);
    }

    public DeviceAisle findDeviceAisle(Long deviceId, String code, Integer track, Long cabinetId) {
        StringBuffer buffer = new StringBuffer("SELECT ");
        buffer.append(SQLUtils.getColumnsSQL(DeviceAisle.class, "A"));
        buffer.append(" FROM T_DEVICE_AISLE A WHERE A.DEVICE_ID=? AND A.PRODUCT_CODE=? AND A.AISLE_NUM=? AND A.CABINET_ID = ? ");
        return genericDao.findT(DeviceAisle.class, buffer.toString(), deviceId, code, track, cabinetId);
    }

    public DeviceAisle findDeviceAisleById(Long Id) {
        StringBuffer buffer = new StringBuffer("SELECT ");
        buffer.append(SQLUtils.getColumnsSQL(DeviceAisle.class, "A"));
        buffer.append(" FROM T_DEVICE_AISLE A WHERE A.ID = ? ");
        return genericDao.findT(DeviceAisle.class, buffer.toString(), Id);
    }

    public ActiveCode findActiveCode(Integer state) {
        StringBuffer buffer = new StringBuffer("SELECT ");
        buffer.append(SQLUtils.getColumnsSQL(ActiveCode.class, "A"));
        buffer.append(" FROM T_ACTIVE_CODE A WHERE A.STATE = ? ORDER BY A.CREATE_TIME ");
        return genericDao.findT(ActiveCode.class, buffer.toString(), state);
    }

    public ActiveCode findActiveCode(String activeCode, Integer state) {
        StringBuffer buffer = new StringBuffer("SELECT ");
        buffer.append(SQLUtils.getColumnsSQL(ActiveCode.class, "A"));
        buffer.append(" FROM T_ACTIVE_CODE A WHERE A.ACTIVE_CODE = ? AND A.STATE = ? ");
        return genericDao.findT(ActiveCode.class, buffer.toString(), activeCode, state);
    }

    public DevicePush findDevicePush(String factoryDevNo) {
        StringBuffer buffer = new StringBuffer("SELECT ");
        buffer.append(SQLUtils.getColumnsSQL(DevicePush.class, "A"));
        buffer.append(" FROM T_DEVICE_PUSH A WHERE A.FACTORY_DEV_NO = ? ");
        return genericDao.findT(DevicePush.class, buffer.toString(), factoryDevNo);
    }

    public List<DevicePush> findDevicePushs() {
        StringBuffer buffer = new StringBuffer("SELECT ");
        buffer.append(SQLUtils.getColumnsSQL(DevicePush.class, "A"));
        buffer.append(" FROM T_DEVICE_PUSH A ");
        return genericDao.findTs(DevicePush.class, buffer.toString());
    }

    public List<DeviceRule> findDeviceRules(String factoryCode) {
        StringBuffer buffer = new StringBuffer("SELECT ");
        buffer.append(SQLUtils.getColumnsSQL(DeviceRule.class, "A"));
        buffer.append(" FROM T_DEVICE_RULE A WHERE A.FACTORY_CODE = ? ");
        return genericDao.findTs(DeviceRule.class, buffer.toString(), factoryCode);
    }

    public List<Object> findDeviceRoadExcepts(String factoryCode, String model) {
        StringBuffer buffer = new StringBuffer("SELECT ");
        buffer.append(" A.EXCEPT_ROAD_NO FROM T_DEVICE_ROAD_EXCEPT A WHERE A.FACTORY_CODE = ? AND A.MODEL = ? ");
        return genericDao.findListSingle(buffer.toString(), factoryCode, model);
    }

    public List<OrderDetail> findOrderDetails(String orderNo) {
        StringBuffer buffer = new StringBuffer("SELECT ");
        buffer.append(SQLUtils.getColumnsSQL(OrderDetail.class, "A"));
        buffer.append(" , P.CODE as productCode, P.SKU_NAME as skuName ");
        buffer.append(" FROM T_ORDER_DETAIL A ");
        buffer.append(" LEFT JOIN T_PRODUCT P ON P.ID = A.SKU_ID ");
        buffer.append(" WHERE A.ORDER_NO=? ");
        return genericDao.findTs(OrderDetail.class, buffer.toString(), orderNo);
    }

    public OrderDetail findOrderDetailLotteryProduct(String orderNo) {
        StringBuffer buffer = new StringBuffer("SELECT ");
        buffer.append(SQLUtils.getColumnsSQL(OrderDetail.class, "A"));
        buffer.append(" , P.CODE as productCode, P.SKU_NAME as skuName ");
        buffer.append(" FROM T_ORDER_DETAIL A ");
        buffer.append(" LEFT JOIN T_PRODUCT P ON P.ID = A.SKU_ID ");
        buffer.append(" WHERE A.ORDER_NO=? ");
        log.info("***************【抽奖活动出货查询:"+buffer.toString()+",orderNo="+orderNo+"】****************");
        return genericDao.findT(OrderDetail.class, buffer.toString(), orderNo);
    }

    public List<OrderDetail> findOrderDetailOrderType(String orderNo) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(" SELECT A.ORDER_TYPE FROM T_ORDER_DETAIL A ");
        buffer.append(" WHERE A.ORDER_NO=? ");
        return genericDao.findTs(OrderDetail.class, buffer.toString(), orderNo);
    }


    public List<DeviceAisle> findDeviceAisles(String deviceNumber) {
        StringBuffer buffer = new StringBuffer("SELECT ");
        buffer.append(SQLUtils.getColumnsSQL(DeviceAisle.class, "A"));
        buffer.append(" , C.CABINET_NO as cabinetNo, C.MODEL as model ");
        buffer.append(" FROM T_DEVICE_AISLE A ");
        buffer.append(" LEFT JOIN T_DEVICE D ON A.DEVICE_ID = D.ID ");
        buffer.append(" LEFT JOIN T_CABINET C ON A.CABINET_ID = C.ID ");
        buffer.append(" WHERE D.DEV_NO=? ");
        return genericDao.findTs(DeviceAisle.class, buffer.toString(), deviceNumber);
    }

    public List<DeviceAisle> findRoadSalesDeviceAisles(Device device) {
        StringBuffer buffer = new StringBuffer("SELECT A.ID, A.AISLE_NUM, A.PRICE_ON_LINE, P.CODE as productCode, C.CABINET_NO as cabinetNo, C.MODEL as model ");
        buffer.append(" FROM T_DEVICE_AISLE A ");
        buffer.append(" LEFT JOIN T_PRODUCT P ON A.PRODUCT_ID = P.ID ");
        buffer.append(" LEFT JOIN T_CABINET C ON A.CABINET_ID = C.ID ");
        buffer.append(" WHERE A.DEVICE_ID=? ");
        return genericDao.findTs(DeviceAisle.class, buffer.toString(), device.getId());
    }

    public Device findDeviceByFacDevNo(String facDevNo) {
        StringBuffer buffer = new StringBuffer("SELECT ");
        buffer.append(SQLUtils.getColumnsSQL(Device.class, "D"));
        buffer.append(" FROM T_DEVICE D  ");
        buffer.append(" LEFT JOIN T_DEVICE_RELATION DR ON D.DEV_NO = DR.DEV_NO ");
        buffer.append(" WHERE 1=1 ");
        buffer.append(" AND DR.FACTORY_DEV_NO = ? ");
        return genericDao.findT(Device.class, buffer.toString(), facDevNo);
    }

    public String findDevNoByFacDevNo(String facDevNo) {
        List<Object> args = new ArrayList<Object>();
        StringBuffer buffer = new StringBuffer(" SELECT ");
        buffer.append(" C.DEV_NO AS DEV_NO ");
        buffer.append(" FROM T_DEVICE_RELATION C ");
        buffer.append(" WHERE C.FACTORY_DEV_NO = ? ");
        args.add( facDevNo );
        return genericDao.findSingle(String.class, buffer.toString(), args.toArray());
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


    public List<Device> findDeviceUnbindedByStore(String storeNo) {
        List<Object> args = new ArrayList<Object>();
        StringBuffer buffer = new StringBuffer(" SELECT ");
        buffer.append(SQLUtils.getColumnsSQL(Device.class, "C"));
        buffer.append(" FROM T_DEVICE C LEFT JOIN T_POINT_PLACE P ON P.ID = C.POINT_ID ");
        buffer.append(" WHERE C.BIND_STATE = 0 ");
        buffer.append(" AND P.POINT_NO = ? ");
        args.add(storeNo);
        buffer.append(" AND P.STATE != ? ");
        args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
        return genericDao.findTs(Device.class, buffer.toString(), args.toArray());
    }

    public Device findDeviceByDevNo(String deviceNumber) {
        StringBuffer buffer = new StringBuffer("SELECT ");
        buffer.append(SQLUtils.getColumnsSQL(Device.class, "A"));
        buffer.append(" FROM T_DEVICE A WHERE A.DEV_NO = ? ");
        return genericDao.findT(Device.class, buffer.toString(), deviceNumber);
    }

    public Order findOrderByOrderNoAndAmount(String code, Double totalFee, Integer state, Integer payType) {
        List<Object> args = new ArrayList<Object>();
        StringBuffer buffer = new StringBuffer("SELECT ");
        buffer.append(SQLUtils.getColumnsSQL(Order.class, "A"));
        buffer.append(" FROM T_ORDER A WHERE A.CODE=? AND A.PAY_TYPE=?");
        args.add(code);
        args.add(payType);
        if(null != totalFee){
            buffer.append(" AND A.AMOUNT=?");
            args.add(totalFee);
        }
        if (null != state) {
            buffer.append(" AND A.STATE=?");
            args.add(state);
        }
        return genericDao.findT(Order.class, buffer.toString(), args.toArray());
    }


    public Order findOrderByOrderNo(String code, Integer state) {
        List<Object> args = new ArrayList<Object>();
        StringBuffer buffer = new StringBuffer("SELECT ");
        buffer.append(SQLUtils.getColumnsSQL(Order.class, "A"));
        buffer.append(" FROM T_ORDER A WHERE A.CODE=? ");
        args.add(code);
        if (null != state) {
            buffer.append(" AND A.STATE = ? ");
            args.add(state);
        }
        return genericDao.findT(Order.class, buffer.toString(), args.toArray());
    }

    public Product findProduct(String devNo, String productNo, String cabinetNo, Integer aisleNum) {
        StringBuffer buffer = new StringBuffer("SELECT ");
        buffer.append(SQLUtils.getColumnsSQL(Product.class, "P"));
        buffer.append(" FROM T_PRODUCT P ");
        buffer.append(" LEFT JOIN T_DEVICE_AISLE DA ON DA.PRODUCT_ID = P.ID ");
        buffer.append(" LEFT JOIN T_DEVICE D ON D.ID = DA.DEVICE_ID ");
        buffer.append(" LEFT JOIN T_CABINET C ON C.ID = DA.CABINET_ID ");
        buffer.append(" WHERE 1 = 1 ");
        buffer.append(" AND D.DEV_NO=? AND DA.PRODUCT_CODE = ? AND P.STATE != ? AND C.CABINET_NO = ? AND DA.AISLE_NUM = ? ");
        return genericDao.findT(Product.class, buffer.toString(), devNo, productNo, Commons.PRODUCT_STATE_TRASH, cabinetNo, aisleNum);
    }


    public List<Product> findProduct(String devNo, String productNo) {
        StringBuffer buffer = new StringBuffer("SELECT ");
        buffer.append(SQLUtils.getColumnsSQL(Product.class, "P"));
        buffer.append(" FROM T_PRODUCT P ");
        buffer.append(" LEFT JOIN T_DEVICE_AISLE DA ON DA.PRODUCT_ID = P.ID ");
        buffer.append(" LEFT JOIN T_DEVICE D ON D.ID = DA.DEVICE_ID ");
        buffer.append(" LEFT JOIN T_CABINET C ON C.ID = DA.CABINET_ID ");
        buffer.append(" WHERE 1 = 1 ");
        buffer.append(" AND D.DEV_NO=? AND DA.PRODUCT_CODE = ? AND P.STATE != ?");
        return genericDao.findTs(Product.class, buffer.toString(), devNo, productNo, Commons.PRODUCT_STATE_TRASH);
    }

    public Product findProduct(Long orgId, String productNo, Integer type) {
        StringBuffer buffer = new StringBuffer("SELECT ");
        buffer.append(SQLUtils.getColumnsSQL(Product.class, "P"));
        buffer.append(" FROM T_PRODUCT P ");
        buffer.append(" WHERE 1 = 1 ");
        buffer.append(" AND P.ORG_ID=? AND P.CODE = ? AND P.TYPE = ? AND P.STATE != ? ");
        return genericDao.findT(Product.class, buffer.toString(), orgId, productNo, type, Commons.PRODUCT_STATE_TRASH);
    }

    public Product findProduct(Long orgId, String productNo) {
        StringBuffer buffer = new StringBuffer("SELECT ");
        buffer.append(SQLUtils.getColumnsSQL(Product.class, "P"));
        buffer.append(" FROM T_PRODUCT P ");
        buffer.append(" WHERE 1 = 1 ");
        buffer.append(" AND P.ORG_ID=? AND P.CODE = ? AND P.STATE != ? ");
        return genericDao.findT(Product.class, buffer.toString(), orgId, productNo,  Commons.PRODUCT_STATE_TRASH);
    }

    public LotteryProduct findLotteryProduct(String productNo) {
        StringBuffer buffer = new StringBuffer("SELECT ");
        buffer.append(SQLUtils.getColumnsSQL(LotteryProduct.class, "LD"));
        buffer.append(" FROM T_LOTTERY_PRODUCT LD ");
        buffer.append(" WHERE 1 = 1 ");
        buffer.append(" AND LD.PRODUCT_NO=?");
        log.info("***************【抽奖活动查询:"+buffer.toString()+",ProductNo="+productNo+"】****************");
        return genericDao.findT(LotteryProduct.class, buffer.toString(), productNo);
    }

    private Orgnization findOrgById(Long id) {
        return genericDao.findT(Orgnization.class, "SELECT TYPE,CODE FROM SYS_ORG WHERE ID=?", id);
    }

    public boolean findOrderByCode(String code) {
        if (StringUtils.isEmpty(code))
            return false;
        int count = genericDao.findSingle(Integer.class, "SELECT COUNT(*) FROM T_ORDER WHERE CODE=?", code);
        if (count > 0)
            return true;
        return false;
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

    public Product findProduct(Long id) {
        List<Object> args = new ArrayList<Object>();
        StringBuffer buf = new StringBuffer("SELECT ");
        String cols = SQLUtils.getColumnsSQL(Product.class, "C");
        buf.append(cols);
        buf.append(" FROM T_PRODUCT C WHERE C.ID = ? ");
        args.add(id);
        return genericDao.findT(Product.class, buf.toString(), args.toArray());
    }

    public DeviceLog findDeviceLog(String deviceNo, Integer deviceStatus) {
        List<Object> args = new ArrayList<Object>();
        StringBuffer buf = new StringBuffer("SELECT ");
        String cols = SQLUtils.getColumnsSQL(DeviceLog.class, "C");
        buf.append(cols);
        buf.append(" FROM T_DEVICE_LOG C WHERE C.DEVICE_NO = ? AND C.DEVICE_STATUS = ? ");
        args.add(deviceNo);
        args.add(deviceStatus);
        return genericDao.findT(DeviceLog.class, buf.toString(), args.toArray());
    }

    public ProductLog findProductLog(Long productId, Integer productStatus) {
        List<Object> args = new ArrayList<Object>();
        StringBuffer buf = new StringBuffer("SELECT ");
        String cols = SQLUtils.getColumnsSQL(ProductLog.class, "C");
        buf.append(cols);
        buf.append(" FROM T_PRODUCT_LOG C WHERE C.PRODUCT_ID = ? AND C.PRODUCT_STATUS = ? ");
        args.add(productId);
        args.add(productStatus);
        return genericDao.findT(ProductLog.class, buf.toString(), args.toArray());
    }

    public Product findProductByCode(String code, Long orgId) {
        if (StringUtils.isEmpty(code) || null == orgId)
            return null;
        return genericDao.findT(Product.class, "SELECT ID,SKU,SKU_NAME,PRICE,ORG_ID FROM T_PRODUCT WHERE CODE=? AND ORG_ID=?", code, orgId);
    }

    public Device findDeviceByCode(String code) {
        if (StringUtils.isEmpty(code))
            return null;

        List<Object> args = new ArrayList<Object>();
        StringBuffer buf = new StringBuffer("SELECT ");
        String cols = SQLUtils.getColumnsSQL(Device.class, "C");
        buf.append(cols);
        buf.append(" FROM T_DEVICE C WHERE C.DEV_NO = ? ");
        args.add(code);
        return genericDao.findT(Device.class, buf.toString(), args.toArray());
    }

    /**
     * 根据openID获取unionId
     */
    public String findWxUnionId(String openId) {
        Map<String, Object> map = wechatService.getWxUser(null, null, openId);
        if (map != null && map.containsKey("unionid"))
            return map.get("unionid").toString();
        return null;
    }

    public Map<String, Object> findWxUser(String openId) {
        return wechatService.getWxUser(null, null, openId);
    }

    /**
     * 【易触】登录接口sessionId是否失效 true:失效 false:未失效
     *
     * @return
     */
    public boolean isSessionIdExpire() {
        // 存取时间
        Timestamp accessTime = (Timestamp) etLoginMap.get(ET_ACCESS_TIME);
        if (null == accessTime)
            return false;

        // 根据存取时间计算出过期时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(accessTime);
        calendar.add(Calendar.HOUR_OF_DAY, 24);
        Timestamp expireTime = new Timestamp(calendar.getTime().getTime());

        // 当前时间大于过期时间，则失效
        if (new Timestamp(System.currentTimeMillis()).after(expireTime))
            return true;
        return false;
    }

    public DeviceRelation findDeviceRelationByDevNo(String devNo) {
        List<Object> args = new ArrayList<Object>();
        String columns = SQLUtils.getColumnsSQL(DeviceRelation.class, "C");
        StringBuffer buf = new StringBuffer();
        buf.append(" SELECT ");
        buf.append(columns);
        buf.append(" FROM T_DEVICE_RELATION C ");
        buf.append(" WHERE C.DEV_NO = ? ");
        args.add(devNo);

        return genericDao.findT(DeviceRelation.class, buf.toString(), args.toArray());
    }

    public DeviceRelation findDeviceRelationByFacNo(String deviceNumber) {
        List<Object> args = new ArrayList<Object>();
        String columns = SQLUtils.getColumnsSQL(DeviceRelation.class, "C");
        StringBuffer buf = new StringBuffer();
        buf.append(" SELECT ");
        buf.append(columns);
        buf.append(" FROM T_DEVICE_RELATION C ");
        buf.append(" WHERE C.FACTORY_DEV_NO = ? ");
        args.add(deviceNumber);

        return genericDao.findT(DeviceRelation.class, buf.toString(), args.toArray());
    }

    /**
     * 店铺与设备真正的绑定
     */
    @Override
    public ResultBase updateRegisterDevice(ResultBase rb, String storeNo, String deviceNumber) {
        log.info("*****storeNo:*****" + storeNo);
        log.info("*****deviceNumber:*****" + deviceNumber);
        PointPlace pp = findPointPlace(storeNo);
        if (null == pp) {
            rb.setResultMessage("无效的店铺编号!");
            return rb;
        }
        Long storeId = pp.getId();
        log.info("*****storeId:*****" + storeId);

        // 店铺未绑定的设备
        List<Device> devices = findDeviceUnbindedByStore(storeNo);
        if (0 == devices.size()) {
            rb.setResultMessage("店铺没有指定设备!");
            return rb;
        }

        // 通过厂家设备号，找到内部设备号
        // 编号只在模式相同的设备间转换
        String devNo = findDevNoByFacDevNo(deviceNumber);
        if (null == devNo) {
            rb.setResultMessage("无效的设备组号!");
            return rb;
        }
        Device device = findDeviceByDevNo(devNo);
        log.info("*****device.getPointId():*****" + device.getPointId());
        log.info("*****device.getPointId().longValue() == storeId.longValue():*****" + (device.getPointId().longValue() == storeId.longValue()));

        if (0 != device.getBindState()) {
            rb.setResultCode(1);
            rb.setResultMessage("设备已经绑定，不能重复绑定!");
            return rb;
        } else if (device.getPointId().longValue() == storeId.longValue()) {// 上报绑定的设备与此店铺的设备模式一样
            device.setBindState(1);
            genericDao.update(device);

            rb.setResultCode(0);
            rb.setResultMessage("设备绑定成功!");
            return rb;
        }

        Integer combNo = device.getCombinationNo();
        log.info("*****combNo:*****" + combNo);

        // 按设备编号升序排序
        Collections.sort(devices, new Comparator<Device>() {
            public int compare(Device arg0, Device arg1) {
                return arg0.getCombinationNo().compareTo(arg1.getCombinationNo());
            }
        });

        Map<Integer, List<Device>> combNoMap = group(devices, new GroupBy<Integer>() {
            @Override
            public Integer groupby(Object obj) {
                Device dc = (Device) obj;
                return dc.getCombinationNo();
            }
        });

        boolean exits = false;
        for (Map.Entry<Integer, List<Device>> entry : combNoMap.entrySet()) {
            log.info("*****entry.getKey():*****" + entry.getKey());
            log.info("*****entry.getKey().intValue() == combNo.intValue():*****" + (entry.getKey().intValue() == combNo.intValue()));
            if (entry.getKey().intValue() == combNo.intValue()) {// 设备模式相同
                exits = true;

                List<Device> devs = entry.getValue();
                Device dev = devs.get(0);// 模式相同，默认取第一个
                String dev_no = dev.getDevNo();
                DeviceRelation devR = findDeviceRelationByDevNo(dev_no);
                DeviceRelation devRel = findDeviceRelationByFacNo(deviceNumber);

                devRel.setFactoryDevNo(devR.getFactoryDevNo());// 调换厂家设备号
                devR.setFactoryDevNo(deviceNumber);// 调换厂家设备号
                genericDao.update(devRel);
                genericDao.update(devR);

                dev.setBindState(1);// 绑定
                genericDao.update(dev);
                break;
            }
        }

        if (!exits) {
            rb.setResultMessage("设备绑定失败!");
            return rb;
        }

        rb.setResultCode(0);
        rb.setResultMessage("设备绑定成功!");
        return rb;
    }

    /**
     * 查询设备绑定状态
     */
    @Override
    public ResultBase queryDeviceBindState(ResultBase rb, String deviceNumber) {
        List<Object> args = new ArrayList<Object>();
        StringBuffer buf = new StringBuffer();
        buf.append(" SELECT ");
        buf.append(SQLUtils.getColumnsSQL(Device.class, "C"));
        buf.append(" ,R.FACTORY_DEV_NO AS FACTORY_DEV_NO ");
        buf.append(" FROM T_DEVICE C LEFT JOIN T_DEVICE_RELATION R ON C.DEV_NO = R.DEV_NO ");
        buf.append(" WHERE FACTORY_DEV_NO = ? ");
        args.add(deviceNumber);
        Device dev = genericDao.findT(Device.class, buf.toString(), args.toArray());
        if (null == dev) {
            rb.setResultMessage("厂家设备号不正确");
            return rb;
        }

        if (0 == dev.getBindState()) {
            rb.setResultCode(1);
            rb.setResultMessage("发送成功");
        } else {
            rb.setResultCode(0);
            rb.setResultMessage("发送成功");
        }

        return rb;
    }

    /**
     * 校验登录用户的合法性
     * @param userId
     * @param deviceNumber
     * @param map
     * @return
     */
    @Override
    public Map<String, Object> authorizeUserAndDevice(Long userId, String deviceNumber, Map<String, Object> map) {
        User user = findUserById(userId);
        if (null == user) {
            log.info("*****用户不存在*****");
            map.put("resultMessage", "用户不存在");
            return map;
        }

        // 转换为内部设备号
        String devNo = findDevNoByFacDevNo(deviceNumber);
        Device device = findDeviceByDevNo(devNo);
        if (null == device) {
            log.info("*****设备编号不存在*****");
            map.put("resultMessage", "设备编号不存在");
            return map;
        }
        if (user.getOrgId().longValue() != device.getOrgId().longValue()) {
            log.info("*****非法用户，请检测该设备是否属于当前用户*****");
            map.put("resultMessage", "非法用户，请检测该设备是否属于当前用户");
            return map;
        }

        // 该设备已经绑定了店铺，需要校验该店铺是否属于当前登录用户
        if (device.getPointId() != 0 && device.getPointId() != null) {
            PointPlace pointPlace = findPointPlaceById(device.getPointId());
            if (pointPlace.getOrgId().longValue() != user.getOrgId().longValue()) {
                log.info("*****非法用户，请检测该设备绑定的店铺是否属于当前用户*****");
                map.put("resultMessage", "非法用户，请检测该设备绑定的店铺是否属于当前用户");
                return map;
            }
            if (pointPlace.getCreateUser().longValue() != user.getId().longValue() && pointPlace.getUserId().longValue() != user.getId().longValue()) {
                log.info("*****非法用户，请检测该设备绑定的店铺的所有者是否属于当前用户*****");
                map.put("resultMessage", "非法用户，请检测该设备绑定的店铺的所有者是否属于当前用户");
                return map;
            }
        }

        map.put("resultCode", 0);
        map.put("resultMessage", "登录成功");
        map.put("userId", userId);
        return map;
    }

    /**
     * 修改密码
     * @param oldPassword	旧密码
     * @param password	新密码
     */
    @SuppressWarnings("rawtypes")
    @Override
    public ResultAppBase savePassword(ResultAppBase rab, String oldPassword, String password) {
        User user = ContextUtil.getUser(User.class);

        if (user == null) {
            rab.setResultMessage("非法请求！");
            return rab;
        }

        Timestamp curTime = new Timestamp(System.currentTimeMillis());
        if (oldPassword.equals(password)) {
            rab.setResultCode(201);
            rab.setResultMessage("新密码不能与旧密码相同！");
            return rab;
        }
        String encodePassword = genericDao.findSingle(String.class, "SELECT PASSWORD FROM SYS_USER WHERE ID=?", user.getId());
        if (!ContextUtil.matches(oldPassword, encodePassword)) {
            rab.setResultCode(202);
            rab.setResultMessage("原密码错误！");
            return rab;
        }
        encodePassword = ContextUtil.getPassword(password);
        genericDao.execute("UPDATE SYS_USER SET PASSWORD=?,PWD_UPDATE_TIME=? WHERE ID=?", encodePassword, curTime, user.getId());
        user.setPassword(encodePassword);

        rab.setResultCode(0);// 成功
        rab.setResultMessage("操作成功!");
        return rab;
    }

    /**
     * 【补货APP接口】5.查询店铺信息(补货/全部)
     *
     * @param page 分页参数
     * @param time 时间参数  今天0，明天1
     * @param type 查询类型   1：待补货的店铺  2：全部店铺
     * @param prov 省
     * @param city 市
     * @param dist 区
     * @return
     */
    @Override
    public ResultAppBase<List<StoreInfo>> findStores(ResultAppBase<List<StoreInfo>> rab, Page page, Integer time, Integer type, String prov, String city, String dist) {
        User user = ContextUtil.getUser(User.class);

        if (user == null) {
            rab.setResultMessage("非法请求！");
            return rab;
        }

        List<Object> args = new ArrayList<Object>();
        StringBuffer buf = new StringBuffer();
        buf.append(" SELECT ");
        buf.append(SQLUtils.getColumnsSQL(PointPlace.class, "PP"));
        buf.append(" FROM T_POINT_PLACE PP WHERE 1=1 ");
        buf.append(" AND PP.ID IN( ");
        buf.append(" SELECT D.POINT_ID FROM T_DEVICE D LEFT JOIN T_DEVICE_AISLE DA ON DA.DEVICE_ID = D.ID ");

        buf.append(" WHERE 1=1 ");
        buf.append(" AND D.POINT_ID != 0 ");
        buf.append(" AND D.ORG_ID = ? ");
        args.add(user.getOrgId());

        if (Commons.FIND_STORE_TYPE_REPLENISH == type)// 查询待补货店铺
            buf.append(" AND DA.PRODUCT_ID IS NOT NULL AND DA.SUPPLEMENT_NO > 0 ");

        if (!StringUtils.isEmpty(prov)) {
            buf.append(" AND PP.PROV = ? ");
            args.add(prov);
        }
        if (!StringUtils.isEmpty(city)) {
            buf.append(" AND PP.CITY = ? ");
            args.add(city);
        }
        if (!StringUtils.isEmpty(dist)) {
            buf.append(" AND PP.DIST = ? ");
            args.add(dist);
        }

        buf.append(" GROUP BY D.POINT_ID ");
        buf.append(" ) ");

        buf.append(" AND PP.STATE != ? ");
        args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除

        List<PointPlace> pointPlaces = genericDao.findTs(PointPlace.class, page, buf.toString(), args.toArray());

        List<StoreInfo> storeInfos = new ArrayList<StoreInfo>();
        for (PointPlace pointPlace : pointPlaces) {
            StoreInfo storeInfo = new StoreInfo();
            storeInfo.setStoreNo(pointPlace.getPointNo());
            storeInfo.setStoreName(pointPlace.getPointName());
            storeInfo.setStoreAddress(pointPlace.getPointAddress());
            storeInfo.setProv(pointPlace.getProv());
            storeInfo.setCity(pointPlace.getCity());
            storeInfo.setDist(pointPlace.getDist());

            // 建议补货时间
            List<PointReplenishTime> pointReplenishTimes = findPointReplenishTimes(pointPlace.getPointNo());
            List<StoreReplenishTimeInfo> storeReplenishTimeInfos = new ArrayList<StoreReplenishTimeInfo>();
            for (PointReplenishTime pointTime : pointReplenishTimes) {
                StoreReplenishTimeInfo replenishTimeInfo = new StoreReplenishTimeInfo();

                replenishTimeInfo.setStartTime(pointTime.getStartTime());
                replenishTimeInfo.setEndTime(pointTime.getEndTime());
                storeReplenishTimeInfos.add(replenishTimeInfo);
            }
            storeInfo.setStoreReplenishTimeInfos(storeReplenishTimeInfos);

            // 设备信息
            List<Device> devices = getDevicesByPointId(pointPlace.getId(), type);
            List<DeviceInfo> deviceInfos = new ArrayList<DeviceInfo>();
            StringBuffer buffer = new StringBuffer();
            for (Device device : devices) {
                DeviceInfo deviceInfo = new DeviceInfo();
                List<Cabinet> cabinets = getCabinetsByDeviceId(device.getId());
                for (Cabinet cabinet : cabinets) {
                    String typeStr = getDeviceTypeStrByModel(cabinet.getModel());
                    if (StringUtils.isEmpty(typeStr))
                        continue;

                    buffer.append(typeStr + "+");
                }
                buffer.setLength(buffer.length() - 1);
                deviceInfo.setFactoryDevNo(device.getFactoryDevNo());
                deviceInfo.setTypeStr(buffer.toString());

                if (Commons.FIND_STORE_TYPE_ALL == type) {// 查询全部
                    DeviceLog deviceLog = findDeviceLog(device.getDevNo(), Commons.DEVICE_STATUS_OFFLINE);//离线设备
                    if (null != deviceLog)
                        deviceInfo.setIsOnOffLine(Commons.DEVICE_STATUS_OFFLINE);//离线
                    else
                        deviceInfo.setIsOnOffLine(Commons.DEVICE_STATUS_ONLINE);//在线
                }
                deviceInfos.add(deviceInfo);

                buffer.setLength(0);
            }
            storeInfo.setDeviceInfos(deviceInfos);

            storeInfos.add(storeInfo);
        }

        rab.setResultCode(0);
        rab.setResultMessage("操作成功!");
        rab.setData(storeInfos);
        return rab;
    }

    /**
     * 【补货APP接口】6.查询店铺商品补货信息
     *
     * @param page 分页参数
     * @param storeNo 区
     * @return
     */
    @Override
    public ResultAppBase<List<ProductReplenishInfo>> findReplenishProducts(ResultAppBase<List<ProductReplenishInfo>> rab, Page page, String factoryDevNo) {
        List<DeviceAisle> deviceAisles = findReplenishProds(page, factoryDevNo);
        List<ProductReplenishInfo> productReplenishInfos = new ArrayList<ProductReplenishInfo>();

        for (DeviceAisle deviceAisle : deviceAisles) {
            ProductReplenishInfo pInfo = new ProductReplenishInfo();

            pInfo.setProductNo(deviceAisle.getProductCode());
            pInfo.setProductName(deviceAisle.getProductName());

            String images = deviceAisle.getImages();
            String picUrl = StringUtils.isEmpty(images) ? "" : dictionaryService.getFileServer() + images.split(";")[0].split(",")[3];
            pInfo.setPicUrl(picUrl);

            pInfo.setTotalSupplementNo(deviceAisle.getTotalSupplementNo());

            productReplenishInfos.add(pInfo);
        }

        rab.setResultCode(0);
        rab.setResultMessage("操作成功");
        rab.setData(productReplenishInfos);
        return rab;
    }

    /**
     * 【补货APP接口】7.获取APP最新版本
     *
     * @return
     */
    public ResultAppBase<ReplenishmentAppVersion> findReplenishAppVersion(ResultAppBase<ReplenishmentAppVersion> rab) {
        // 获取最新版本
        ReplenishmentAppVersion version = getReplenishmentAppVersion();
        if (null == version) {
            rab.setResultMessage("版本信息不存在！");
            return rab;
        }

        rab.setResultCode(0);
        rab.setResultMessage("操作成功");
        rab.setData(version);
        return rab;
    }

    /**
     * 查询店铺商品补货信息
     * @param page
     * @return
     */
    public List<DeviceAisle> findReplenishProds(Page page, String factoryDevNo) {
        User user = ContextUtil.getUser(User.class);
        if (null == user)
            throw new BusinessException("当前用户未登录");

        StringBuffer buffer = new StringBuffer();
        List<Object> args = new ArrayList<Object>();

        buffer.append("SELECT STRING_AGG(B.ID||','||B.NAME||','||B.TYPE||','||B.REAL_PATH, ';' ORDER BY B.ID DESC) AS images, ");
        buffer.append(" P.CODE as productCode,P.SKU_NAME as productName,DA.PRODUCT_ID,SUM(DA.STOCK) as totalStock,SUM(DA.SUPPLEMENT_NO) as totalSupplementNo,SUM(DA.CAPACITY) as totalCapacity ");
        buffer.append(" FROM T_DEVICE_AISLE DA ");
        buffer.append(" LEFT JOIN T_DEVICE D ON D. ID = DA.DEVICE_ID ");
        buffer.append(" LEFT JOIN T_DEVICE_RELATION DR ON DR.DEV_NO = D.DEV_NO ");
        buffer.append(" LEFT JOIN T_PRODUCT P ON P.ID = DA.PRODUCT_ID AND D.ORG_ID = P.ORG_ID ");
        buffer.append(" LEFT JOIN SYS_ORG O ON P.ORG_ID=O.ID LEFT JOIN T_FILE B ON P.ID=B.INFO_ID AND B.TYPE=? ");
        buffer.append(" WHERE D.ORG_ID = ?  ");
        args.add(user.getOrgId());

        buffer.append(" AND P.STATE!=? ");
        args.add(Commons.PRODUCT_STATE_TRASH);

        buffer.append(" AND D.POINT_ID != 0 ");

        buffer.append(" AND D.BIND_STATE = ? ");
        args.add(Commons.BIND_STATE_SUCCESS);//点位绑定成功

        if (!StringUtils.isEmpty(factoryDevNo)) {
            buffer.append(" AND DR.FACTORY_DEV_NO = ? ");
            args.add(factoryDevNo);
        }

        buffer.append(" AND DA.PRODUCT_ID IS NOT NULL ");
        buffer.append(" GROUP BY ").append(" P.CODE,P.SKU_NAME,DA.PRODUCT_ID ");
        args.add(0, Commons.FILE_PRODUCT);

        return genericDao.findTs(DeviceAisle.class, page, buffer.toString(), args.toArray());
    }

    public List<Device> getDevicesByPointId(Long pointId, Integer type) {
        List<Object> args = new ArrayList<Object>();
        StringBuffer buffer = new StringBuffer("SELECT ");
        buffer.append(SQLUtils.getColumnsSQL(Device.class, "D"));
        buffer.append(" , DR.FACTORY_DEV_NO as factoryDevNo ");
        buffer.append(" FROM T_DEVICE D LEFT JOIN T_DEVICE_RELATION DR ON DR.DEV_NO = D.DEV_NO ");
        buffer.append(" LEFT JOIN T_DEVICE_AISLE DA ON DA.DEVICE_ID = D.ID ");
        buffer.append(" WHERE 1=1 ");
        buffer.append(" AND D.POINT_ID = ? ");
        args.add(pointId);
        buffer.append(" AND D.BIND_STATE = ? ");
        args.add(Commons.BIND_STATE_SUCCESS);//点位绑定成功

        if (Commons.FIND_STORE_TYPE_REPLENISH == type)// 查询待补货店铺
            buffer.append(" AND DA.PRODUCT_ID IS NOT NULL AND DA.SUPPLEMENT_NO > 0 ");

        buffer.append(" GROUP BY ").append(SQLUtils.getColumnsSQL(Device.class, "D")).append(", DR.FACTORY_DEV_NO");

        return genericDao.findTs(Device.class, buffer.toString(), args.toArray());
    }

    public List<Cabinet> getCabinetsByDeviceId(Long deviceId) {
        List<Object> args = new ArrayList<Object>();
        StringBuffer buffer = new StringBuffer("SELECT ");
        buffer.append(SQLUtils.getColumnsSQL(Cabinet.class, "C"));
        buffer.append(" FROM T_CABINET C  ");
        buffer.append(" WHERE 1=1 ");
        buffer.append(" AND C.DEVICE_ID = ? ");
        args.add(deviceId);
        buffer.append(" ORDER BY C.CABINET_NO ");
        return genericDao.findTs(Cabinet.class, buffer.toString(), args.toArray());
    }

    public ReplenishmentAppVersion getReplenishmentAppVersion() {
        List<Object> args = new ArrayList<Object>();
        StringBuffer buffer = new StringBuffer("SELECT ");
        buffer.append(SQLUtils.getColumnsSQL(ReplenishmentAppVersion.class, "C"));
        buffer.append(" FROM T_REPLENISHMENT_APP_VERSION C  ");
        buffer.append(" WHERE 1=1 ");
        buffer.append(" ORDER BY C.VERSION_CODE DESC, C.CREATE_TIME DESC ");
        return genericDao.findT(ReplenishmentAppVersion.class, buffer.toString(), args.toArray());
    }

    /**
     * 根据货柜型号取到设备类型描述
     * @param model
     * @return
     */
    public String getDeviceTypeStrByModel(String model) {
        switch(model) {
            case Commons.DEVICE_MODEL_DRINK:
                return Commons.DEVICE_TYPE_STR_DRINK;// 饮料机
            case Commons.DEVICE_MODEL_DRINK_SMALL:
                return Commons.DEVICE_TYPE_STR_DRINK_SMALL;// 小型饮料机
            case Commons.DEVICE_MODEL_CENTER_CONTROL:
                return Commons.DEVICE_TYPE_STR_CENTER_CONTROL;// 中控机
            case Commons.DEVICE_MODEL_SPRING:
                return Commons.DEVICE_TYPE_STR_SPRING;// 弹簧机
            case Commons.DEVICE_MODEL_CATERPILLAR:
                return Commons.DEVICE_TYPE_STR_CATERPILLAR;// 履带机
            case Commons.DEVICE_MODEL_GRID64:
                return Commons.DEVICE_TYPE_STR_GRID64;// 64门格子柜
            case Commons.DEVICE_MODEL_GRID40:
                return Commons.DEVICE_TYPE_STR_GRID40;// 40门格子柜
            case Commons.DEVICE_MODEL_GRID60:
                return Commons.DEVICE_TYPE_STR_GRID60;// 60门格子柜
            default:
                return "";
        }
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

    /**
     * 【设备APP接口】36.上传设备版本号
     *
     * @param deviceNumber 设备组号
     * @param version 版本号
     * @return
     */
    @Override
    public ResultBase syncDeviceVersion(ResultBase rb, String deviceNumber, String version) {
        // 转换为内部设备编号
        String devNo = findDevNoByFacDevNo(deviceNumber);

        Device device = findDeviceByCode(devNo);
        if (null == device) {
            rb.setResultMessage("设备编号不存在");
            return rb;
        }

        // 更新版本号
        device.setVersion(version);
        genericDao.update(device);

        rb.setResultCode(0);// 成功
        rb.setResultMessage("上传设备版本号成功!");
        return rb;
    }

    /**
     * 【设备APP接口】37.自动退款接口
     *
     * @param orderNo 订单编号
     * @param productNo 商品编号
     * @param qty 退货数量
     * @param activityNo 金蛋/银蛋/铜蛋编码（抽奖活动用）
     * @return
     */
    public ResultBase saveRefund(ResultBase rb, String orderNo, String productNo, Integer qty, String activityNo) throws Exception {
        List<OrderDetail> orderDetails = findOrderDetails(orderNo);
        if (null == orderDetails || orderDetails.isEmpty()) {
            log.info("*****订单详情信息不存在*****");
            rb.setResultMessage("订单详情信息不存在");
            return rb;
        }

        OrderDetail orderDetail = orderDetails.get(0);
        if (Commons.ORDER_TYPE_LOTTERY != orderDetail.getOrderType().intValue()) { // 普通订单或打折活动订单
            orderDetail = findOrderDetailByCodeAndProductNo(orderNo, productNo);
            if (null == orderDetail) {
                log.info("*****订单信息(详情)不存在*****");
                rb.setResultMessage("订单信息(详情)不存在");
                return rb;
            }
        }

        // 自动退款
        Refund refund = new Refund();
        refund.setOrderNo(orderNo);
        refund.setSkuId(orderDetail.getSkuId());
        refund.setRefundQty(qty);
        refund.setProductNo(productNo); // 抽奖活动对应的真实商品编码（只在抽奖活动时有用）
        refund.setReason("售货机自动退款");

        log.info("*****operationService.saveRefund*****开始****");
        User user = new User();
        user.setId(1L);// 自动退款为虚拟用户
        operationService.saveRefund(refund, user);
        log.info("*****operationService.saveRefund*****结束****");

        rb.setResultCode(0);// 成功
        rb.setResultMessage("自动退款申请成功!");
        log.info("*****自动退款申请成功*****");
        return rb;
    }

    /**
     * 【设备APP接口】40.根据设备查询货柜货道可见状态
     *
     * @param machineNum 设备编号
     * @return
     */
    public ResultList<CabinetVisiableStateInfo> findCabinetVisiableState(ResultList<CabinetVisiableStateInfo> rl, String machineNum) throws Exception {
        List<Cabinet> cabinets = findCabinetByFactoryDevNo(machineNum);

        List<CabinetVisiableStateInfo> cabinetVisiableStateInfos = new ArrayList<CabinetVisiableStateInfo>();
        for (Cabinet cabinet : cabinets) {
            CabinetVisiableStateInfo stateInfo = new CabinetVisiableStateInfo();
            stateInfo.setDbCabinetNo(cabinet.getCabinetNo() + "");
            stateInfo.setVisiable(cabinet.getVisiable());
            cabinetVisiableStateInfos.add(stateInfo);
        }

        rl.setResultCode(0);// 成功
        rl.setResultList(cabinetVisiableStateInfos);
        rl.setResultMessage("发送成功!");
        log.info("*****根据设备查询货柜货道可见状态SUCCESS*****");
        return rl;
    }

    /**
     * 【设备APP接口】41.获取推荐商品数据接口
     *
     * @param deviceNumber 设备编号
     * @return
     */
    public ResultList<RecommendGoodsInfo> findRecommendGoods(ResultList<RecommendGoodsInfo> rl, String deviceNumber) throws Exception {
    	List<RecommendGoodsInfo> recommendGoodsInfos = new ArrayList<RecommendGoodsInfo>();
    	
    	Device device = findDeviceByFactoryDevNo(deviceNumber);
    	if (null == device) {
    		rl.setResultMessage("设备编号不存在");
			return rl;
		}
    	
    	// 取关联度高的前4个
    	List<DeviceAisle> deviceAisles = findRecommendGoodList(deviceNumber, null, device.getOrgId(), 4);
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
    	
    	rl.setResultCode(0);// 成功
    	rl.setResultList(recommendGoodsInfos);
    	rl.setResultMessage("发送成功!");
		log.info("*****获取推荐商品数据接口SUCCESS*****");
		return rl;
    }
    
    /**
     * 获取推荐商品数据
     * @param deviceNumber
     * @param orgId
     * @param limitCount 取关联度前几位的推荐商品
     * @return
     */
    @Override
    public List<DeviceAisle> findRecommendGoodList(String deviceNumber, Long mainProductId, Long orgId, int limitCount) {
    	List<Object> args = new ArrayList<Object>();
    	StringBuffer buffer = new StringBuffer("SELECT ");
		buffer.append(" pr.product_id,t.product_name ,t.product_code, pr.recommend_product_id,t2.product_name,t2.product_code as recommendProductCode,T2.price_on_line, t2.sellable, pr.relevancy, t3.priceCombo as recomPrice, t3.images ");
		buffer.append(" FROM ( ");
		buffer.append(" select da.product_id, da.product_name,da.product_code ");
		buffer.append(" from t_device_aisle da ");
		buffer.append(" left join t_device d on d.id = da.device_id ");
		buffer.append(" left join t_device_relation dr on dr.dev_no = d.dev_no ");
		buffer.append(" where 1=1 ");
		
		if (!StringUtils.isEmpty(deviceNumber)) {
			buffer.append(" and dr.factory_dev_no = ? ");
			args.add(deviceNumber);
		}
		
		buffer.append(" and da.product_id is not null ");
		buffer.append(" group by da.product_id, da.product_name,da.product_code ");
		buffer.append(" ) T ");
		
		buffer.append(" left join t_product_recommend pr on t.product_id = pr.product_id ");
		
		buffer.append(" left join ( ");
		buffer.append(" select da.product_id, da.product_name,da.product_code, da.price_on_line, da.sellable ");
		buffer.append(" from t_device_aisle da ");
		buffer.append(" left join t_device d on d.id = da.device_id ");
		buffer.append(" left join t_device_relation dr on dr.dev_no = d.dev_no ");
		buffer.append(" where 1=1 ");
		
		if (!StringUtils.isEmpty(deviceNumber)) {
			buffer.append(" and dr.factory_dev_no = ? ");
			args.add(deviceNumber);
		}
		
		buffer.append(" and da.product_id is not null ");
		buffer.append(" group by da.product_id, da.product_name,da.product_code, da.price_on_line, da.sellable ");
		buffer.append(" ) T2 on T2.product_id = pr.recommend_product_id ");

		buffer.append(" left join ( ");
		buffer.append(" select A.ID, COALESCE(a.price_combo, 0) as priceCombo, STRING_AGG(B.ID || ',' || B.NAME || ',' || B.TYPE || ',' || B.REAL_PATH, ';' ORDER BY B. ID DESC) AS images  ");
		buffer.append(" from T_PRODUCT A ");
		buffer.append(" LEFT JOIN T_FILE B ON A.ID = B.INFO_ID AND B.TYPE = ? ");
		args.add(Commons.FILE_PRODUCT_DETAIL); // 详情页图
		buffer.append(" where 1=1 ");
		buffer.append(" AND A.ORG_ID = ? ");
		args.add(orgId);
		buffer.append(" AND A.STATE != ? ");
		args.add(Commons.PRODUCT_STATE_TRASH);
		buffer.append(" AND A.CATEGORY != ? ");
		args.add(Commons.PRODUCT_CATEGORY_TYPE);
		buffer.append(" group by A.ID,a.price_combo ");
		buffer.append(" ORDER BY A.CREATE_TIME DESC ");
		buffer.append(" ) T3 on t3.id = pr.recommend_product_id ");
		
		buffer.append(" WHERE 1=1 ");
		buffer.append(" and (select count(1) from t_product_recommend where product_id = pr.product_id and relevancy >= pr.relevancy) <= ? ");
		args.add(limitCount);
		buffer.append(" and pr.product_id is not null ");
		
		if (null != mainProductId) {
			buffer.append(" AND pr.product_id = ? ");
			args.add(mainProductId);
		}
		
		buffer.append(" and t2.product_id is not null ");
		buffer.append(" and t3.id is not null ");
		buffer.append(" group by pr.product_id,t.product_name,t.product_code, pr.recommend_product_id,t2.product_name,t2.product_code,T2.price_on_line, t2.sellable, pr.relevancy, t3.priceCombo, t3.images ");
		buffer.append(" order by pr.product_id desc, pr.relevancy desc ");
		return genericDao.findTs(DeviceAisle.class, buffer.toString(), args.toArray());
    }
    
    public List<Cabinet> findCabinetByFactoryDevNo(String factoryDevNo) {
        StringBuffer buffer = new StringBuffer("SELECT ");
        buffer.append(SQLUtils.getColumnsSQL(Cabinet.class, "C"));
        buffer.append(" FROM T_CABINET C ");
        buffer.append(" LEFT JOIN T_DEVICE D ON D.ID = C.DEVICE_ID ");
        buffer.append(" LEFT JOIN T_DEVICE_RELATION DR ON DR.DEV_NO = D.DEV_NO ");
        buffer.append(" WHERE 1=1 ");
        buffer.append(" AND DR.FACTORY_DEV_NO = ? ");
        return genericDao.findTs(Cabinet.class, buffer.toString(), factoryDevNo);
    }

    public OrderDetail findOrderDetailByCodeAndProductNo(String orderNo, String productNo) {
        List<Object> args = new ArrayList<Object>();
        StringBuffer buffer = new StringBuffer(" SELECT ");
        buffer.append(SQLUtils.getColumnsSQL(OrderDetail.class, "A"));
        buffer.append(" FROM T_ORDER_DETAIL A ");
        buffer.append(" LEFT JOIN T_PRODUCT P ON P.ID = A.SKU_ID ");
        buffer.append(" WHERE A.ORDER_NO = ? ");
        args.add(orderNo);

        buffer.append(" AND P.CODE = ? ");
        args.add(productNo);

        log.info("*****orderNo：*****" + orderNo);
        log.info("*****productNo：*****" + productNo);
        log.info("*****SQL：*****" + buffer.toString());
        return genericDao.findT(OrderDetail.class, buffer.toString(), args.toArray());
    }

    /**
     * 查询店铺信息
     * @return
     * @throws Exception
     */
    @Override
    public List<Store> findStores() throws Exception {
        List<PointPlace> pointPlaces = findPointPlaces();

        List<Store> storeList = new ArrayList<Store>();
        for (PointPlace pointPlace : pointPlaces) {
            Store store = new Store();
            store.setStoreNo(pointPlace.getPointNo());
            store.setStoreAddress(pointPlace.getPointAddress());
            store.setCreateTime(pointPlace.getCreateTime());
            storeList.add(store);
        }
        return storeList;
    }

    public List<PointPlace> findPointPlaces() {
        StringBuffer buffer = new StringBuffer("SELECT ");
        buffer.append(SQLUtils.getColumnsSQL(PointPlace.class, "A"));
        buffer.append(" FROM T_POINT_PLACE A WHERE 1=1 ");

        buffer.append(" AND A.STATE != ? ");
        return genericDao.findTs(PointPlace.class, buffer.toString(), Commons.POINT_PLACE_STATE_DELETE);
    }

    /**
     * 查找所有需要闪烁的店铺
     * @return
     * @throws Exception
     */
    @Override
    public List<Store> saveStoreState(Timestamp lastUpdateTime) throws Exception {
        List<Store> storeList = new ArrayList<Store>();
        List<Order> orders = findOrdersByPointNo(lastUpdateTime);
        for (Order order : orders) {
            Store store = new Store();
            store.setStoreNo(order.getPointNo());
            store.setStoreAddress(order.getPointAddress());
            storeList.add(store);
        }
        return storeList;
    }

    public List<Order> findOrdersByPointNo(Timestamp lastUpdateTime) {
        List<Object> args = new ArrayList<Object>();
        StringBuffer buffer = new StringBuffer("SELECT ");
        buffer.append(" O.POINT_NO, PP.POINT_ADDRESS AS POINTADDRESS ");
        buffer.append(" FROM T_ORDER O ");
        buffer.append(" LEFT JOIN T_POINT_PLACE PP ON PP.POINT_NO = O.POINT_NO ");
        buffer.append(" WHERE 1=1 ");

        buffer.append(" AND PP.STATE != ? ");
        args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除

        if (null != lastUpdateTime) {
            buffer.append(" AND O.PAY_TIME >= ? ");
            args.add(lastUpdateTime);
        }
        buffer.append(" GROUP BY O.POINT_NO, PP.POINT_ADDRESS ");
        return genericDao.findTs(Order.class, buffer.toString(), args.toArray());
    }

    /**
     * 【地图】页面相关数据
     */
    @Override
    public Map<String, Object> findMapOperData() {
        Map<String, Object> map = new HashMap<String, Object>();

        // 销售额
        double salesAmount = MathUtil.round(getSalesAmount(), 2);
        map.put("salesAmount", salesAmount);

        // 销售量
        int salesQty = getSalesQty();
        map.put("salesQty", salesQty);

        // 用户总数
        int userCount = getUserCount();
        map.put("userCount", userCount);

        // 店铺数量
        int storeQty = getStoreCount();
        map.put("storeQty", storeQty);

        // 商品销售排行榜
        List<Order> topProducts = getTopProducts(salesAmount);
        map.put("topProducts", topProducts);

        // 设备销售排行榜
        List<Device> topDevices = getTopDevices();
        map.put("topDevices", topDevices);

        return map;
    }

    /**
     * 取得当前登录用户的销售额
     * @param: user 当前登录用户
     * @param: date 指定日期
     * @return: 当前登录用户的销售额
     */
    private double getSalesAmount() {
        List<Object> args = new ArrayList<Object>();
        StringBuffer buf = new StringBuffer(" SELECT COALESCE(SUM(O.AMOUNT), 0) FROM T_ORDER O WHERE 1 = 1 ");

        buf.append(" AND O.PAY_TIME>=? ");
        args.add(DateUtil.getStartDate(new Date(System.currentTimeMillis())));
        buf.append(" AND O.PAY_TIME<=? ");
        args.add(DateUtil.getEndDate(new Date(System.currentTimeMillis())));

        buf.append(" AND O.STATE = ? ");
        args.add(Commons.ORDER_STATE_FINISH);

        Double amount = genericDao.findSingle(Double.class, buf.toString(), args.toArray());
        return amount == null ? 0 : amount;
    }

    /**
     * 取得当前用户的销售量
     * @param user 当前登录用户
     * @param date 指定日期
     * @return 当前登录用户的销售量
     */
    private int getSalesQty() {
        List<Object> args = new ArrayList<Object>();
        StringBuffer buf = new StringBuffer(" SELECT COALESCE(SUM(OD.QTY), 0) FROM T_ORDER_DETAIL OD LEFT JOIN T_ORDER O ON O.CODE = OD.ORDER_NO WHERE 1 = 1 ");

        buf.append(" AND O.PAY_TIME>=? ");
        args.add(DateUtil.getStartDate(new Date(System.currentTimeMillis())));
        buf.append(" AND O.PAY_TIME<=? ");
        args.add(DateUtil.getEndDate(new Date(System.currentTimeMillis())));

        buf.append(" AND O.STATE = ? ");
        args.add(Commons.ORDER_STATE_FINISH);

        return genericDao.findSingle(int.class, buf.toString(), args.toArray());
    }

    /**
     * 取得当前用户的用户数
     */
    private int getUserCount() {
        List<Object> args = new ArrayList<Object>();
        StringBuffer buf = new StringBuffer(" SELECT COALESCE(COUNT(T.USERNAME), 0) FROM ( SELECT O.USERNAME, COUNT(O.USERNAME) FROM T_ORDER O WHERE 1 = 1 ");

        buf.append(" AND O.PAY_TIME>=? ");
        args.add(DateUtil.getStartDate(new Date(System.currentTimeMillis())));
        buf.append(" AND O.PAY_TIME<=? ");
        args.add(DateUtil.getEndDate(new Date(System.currentTimeMillis())));

        buf.append(" AND O.STATE = ? ");
        args.add(Commons.ORDER_STATE_FINISH);
        buf.append(" GROUP BY O.USERNAME ");

        buf.append(" ) T ");

        return genericDao.findSingle(int.class, buf.toString(), args.toArray());
    }

    /**
     * 新增店铺数量
     * @param user 当前登录用户
     * @param date 时间
     * @return 新增店铺数量
     */
    private int getStoreCount() {
        List<Object> args = new ArrayList<Object>();
        StringBuffer buf = new StringBuffer("");
        buf.append("SELECT COUNT(*) FROM T_POINT_PLACE WHERE 1=1 ");

        buf.append(" AND STATE != ? ");
        args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除
        return genericDao.findSingle(int.class, buf.toString(), args.toArray());
    }

    /**
     * 商品销售占比
     * @return
     */
    private List<Order> getTopProducts(Double totalAmount) {
        List<Object> args = new ArrayList<Object>();
        StringBuffer buf = new StringBuffer(" SELECT P.SKU_NAME AS name, COALESCE(SUM(OD.qty*OD.price), 0) AS salesAmount, COALESCE(SUM(OD.qty), 0) AS salesVolume ");
        buf.append(" FROM T_ORDER O ");
        buf.append(" LEFT JOIN T_ORDER_DETAIL OD ON OD.ORDER_NO = O.CODE ");
        buf.append(" LEFT JOIN SYS_ORG ORG ON O.ORG_ID = ORG.ID ");
        buf.append(" LEFT JOIN T_PRODUCT P ON P.ID = OD.SKU_ID ");
        buf.append(" WHERE 1=1  ");

        buf.append(" AND O.PAY_TIME>=? ");
        args.add(DateUtil.getStartDate(new Date(System.currentTimeMillis())));
        buf.append(" AND O.PAY_TIME<=? ");
        args.add(DateUtil.getEndDate(new Date(System.currentTimeMillis())));

        buf.append(" AND O.STATE=? ");
        args.add(Commons.ORDER_STATE_FINISH);

        buf.append(" AND P.SKU_NAME IS NOT NULL ");

        buf.append(" GROUP BY P.SKU_NAME ");
        buf.append(" ORDER BY salesAmount DESC, salesVolume DESC ");
        List<Order> orders = genericDao.findTs(Order.class, buf.toString(), args.toArray());

        for (Order order : orders) {
            double salesRate = totalAmount == 0 ? 0.0 : MathUtil.div(MathUtil.mul(order.getSalesAmount(), 100), totalAmount);
            order.setSalesRate(salesRate);
        }

        return orders;
    }

    /**
     * 设备销售排行榜
     * @return
     */
    private List<Device> getTopDevices() {
        List<Object> args = new ArrayList<Object>();
        StringBuffer buf = new StringBuffer(" SELECT D.DEV_NO, PP.POINT_ADDRESS AS POINTADDRESS, D.STATE, COALESCE(SUM(OD.QTY*OD.PRICE), 0) AS SALESAMOUNT, COALESCE(SUM(OD.QTY), 0) AS SALESVOLUME ");
        buf.append(" FROM T_DEVICE D ");
        buf.append(" LEFT JOIN T_ORDER O ON D.DEV_NO = O.DEVICE_NO ");
        buf.append(" LEFT JOIN T_ORDER_DETAIL OD ON OD.ORDER_NO = O.CODE ");
        buf.append(" LEFT JOIN SYS_ORG ORG ON O.ORG_ID = ORG.ID ");
        buf.append(" LEFT JOIN T_POINT_PLACE PP ON PP.ID = D.POINT_ID ");
        buf.append(" WHERE 1=1  ");
        buf.append(" AND D.dev_no IS NOT NULL ");

        buf.append(" AND O.PAY_TIME>=? ");
        args.add(DateUtil.getStartDate(new Date(System.currentTimeMillis())));
        buf.append(" AND O.PAY_TIME<=? ");
        args.add(DateUtil.getEndDate(new Date(System.currentTimeMillis())));

        buf.append(" AND O.STATE=? ");
        args.add(Commons.ORDER_STATE_FINISH);

        buf.append(" AND PP.STATE != ? ");
        args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除

        buf.append(" GROUP BY D.DEV_NO, PP.POINT_ADDRESS, D.STATE ");
        buf.append(" ORDER BY D.STATE = 2 DESC, SALESAMOUNT DESC, SALESVOLUME DESC ");

        return genericDao.findTs(Device.class, buf.toString(), args.toArray());
    }

    /**
     * 统计销售数据
     * @param startTime
     * @param endTime
     * @return
     */
    @Override
    public List<Order> findSalesData(Date startTime, Date endTime) {
        startTime = null == startTime ? new Date(System.currentTimeMillis()) : startTime;
        endTime = null == endTime ? new Date(System.currentTimeMillis()) : endTime;

        StringBuffer buf = new StringBuffer(" SELECT ");
        List<Object> args = new ArrayList<Object>();
        buf.append(" TO_CHAR(O.pay_time, ");
        if (compareSomeDay(startTime, endTime)) {
            buf.append(" 'HH24' ");
        } else {
            buf.append(" 'YYYY-MM-DD' ");
        }
        buf.append(" ) ");
        buf.append(" AS date , COALESCE(SUM(OD.qty * OD.price), 0) AS salesAmount, COALESCE (SUM(OD.qty), 0) as salesVolume ");
        buf.append(" FROM T_ORDER O ");
        buf.append(" LEFT JOIN T_ORDER_DETAIL OD ON O.CODE = OD.ORDER_NO ");
        buf.append(" WHERE 1 = 1 ");
        buf.append(" AND O.PAY_TIME>=? ");
        args.add(DateUtil.getStartDate(startTime));
        buf.append(" AND O.PAY_TIME<=? ");
        args.add(DateUtil.getEndDate(endTime));

        buf.append(" AND O.STATE = ? ");
        args.add(Commons.ORDER_STATE_FINISH);
        buf.append(" GROUP BY date ORDER BY date ");

        return genericDao.findTs(Order.class, buf.toString(), args.toArray());
    }

    /**
     * 是否为同一天
     * @param startTime
     * @param endTime
     * @return
     */
    private boolean compareSomeDay(Date startTime, Date endTime) {
        if (null == startTime || null == endTime) {
            throw new BusinessException("参数非法");
        }
        return (DateUtil.getDay(startTime) == DateUtil.getDay(endTime)) ? true : false;
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

    /************************演示数据**********开始***************/
    /**
     * 定时改变订单生成时间，每隔一小时执行
     */
    @Override
    public void changeOrderMapJob() {
        log.info("*****定时【改变订单生成时间】********开始***");

        java.util.Date curDate = new java.util.Date();

        devOrderTimeMap = new HashMap<String, List<Timestamp>>();

        int year = DateUtil.getYear(curDate);
        int month = DateUtil.getMonth(curDate) - 1;
        int day = DateUtil.getDay(curDate);
        int hour = DateUtil.getHour(curDate);

        int weekDay = Integer.valueOf(DateUtil.getWeekOfDate(curDate));// 周几，大于5为周末
        Map<String, Map<String, String>> map = weekDay > 5 ? initHolidayDevTimeOrderData() : initDevTimeOrderData();

        String[] factoryDevNoArr = {"83000001", "83000002", "83000003", "83000004", "83000005", "83000006", "83000007", "83000008",
                "83000009", "83000010", "83000011", "83000012", "83000013", "83000014", "83000015", "83000016",
                "83000017", "83000018", "83000019", "83000020", "83000021", "83000022", "83000023", "83000024",
                "83000025"};

        for (String factoryDevNo : factoryDevNoArr) {
            List<Timestamp> orderTimes = new ArrayList<Timestamp>();
            int min = 0;
            int max = 0;

            out : for (Map.Entry<String, Map<String, String>> entry : map.entrySet()) {
                List<String> devList = Arrays.asList(entry.getKey().split(","));
                if (!devList.contains(factoryDevNo))
                    continue;

                Map<String, String> orderCountMap = entry.getValue();
                for (Map.Entry<String, String> orderCountEntry : orderCountMap.entrySet()) {
                    List<String> hourList = Arrays.asList(orderCountEntry.getKey().split(","));
                    if (!hourList.contains(hour + ""))
                        continue;

                    min = Integer.valueOf(orderCountEntry.getValue().split(",")[0]);
                    max = Integer.valueOf(orderCountEntry.getValue().split(",")[1]);

                    break out;
                }
            }
            int random = getRandom(min, max);// 生成随机订单数

            for (int i = 0; i < random; i++) {// 随机生成订单时间
                // 随机生成一个支付时间
                Timestamp randomTime = new Timestamp(DateUtil.getDate(getRandomTime(year, month, day, hour), DateUtil.YYYY_MM_DD_HH_MM_SS_EN).getTime());
                while (randomTime.before(new Timestamp(System.currentTimeMillis()))) {// 小于当前时间，重新生成
                    randomTime = new Timestamp(DateUtil.getDate(getRandomTime(year, month, day, hour), DateUtil.YYYY_MM_DD_HH_MM_SS_EN).getTime());
                }
                orderTimes.add(randomTime);
            }

            devOrderTimeMap.put(factoryDevNo, orderTimes);
        }

        for (Map.Entry<String, List<Timestamp>> entry : devOrderTimeMap.entrySet()) {
            System.out.println("设备号：" + entry.getKey());

            for (Timestamp t : entry.getValue()) {
                System.out.println("\t" + t);
            }
            System.out.println("************************************");
        }

        log.info("*****定时【改变订单生成时间】********结束***");
    }

    /**
     * 定时生成演示用订单数据
     */
    @Override
    public void createOrderJob() {
        log.info("*****定时生成演示用订单数据 ********开始***");

        if (null != devOrderTimeMap && devOrderTimeMap.size() > 0) {
            String[] factoryDevNoArr = {"83000001", "83000002", "83000003", "83000004", "83000005", "83000006", "83000007", "83000008",
                    "83000009", "83000010", "83000011", "83000012", "83000013", "83000014", "83000015", "83000016",
                    "83000017", "83000018", "83000019", "83000020", "83000021", "83000022", "83000023", "83000024",
                    "83000025"};

            for (String factoryDevNo : factoryDevNoArr) {
                Device device = findDeviceByFactoryDevNo(factoryDevNo);
                if (null == device)
                    continue;

                List<DeviceAisle> deviceAisles = findEffectDeviceAisles(device.getDevNo());

                for (Map.Entry<String, List<Timestamp>> entry : devOrderTimeMap.entrySet()) {
                    if (entry.getKey().equals(factoryDevNo)) {
                        List<Timestamp> orderTimes = entry.getValue();
                        for (Timestamp orderTime : orderTimes ) {
                            Timestamp curTime = new Timestamp(System.currentTimeMillis());
                            // 5秒之内的时间都可以
                            if (!curTime.before(orderTime) && curTime.getTime() - orderTime.getTime() <= 5000) {
                                log.info("*****定时生成演示用订单数据 ********生成一条订单！！！***");
                                log.info("*****设备号：" + factoryDevNo + "********生成一条订单！！！***下单时间：****"+ orderTime +"*****");

                                // 新订单或者是老用户的二次购买
                                boolean newOrder =  getRandom(1, 10) >= 4 ? true : false;
                                Order oldOrder = null;
                                if (!newOrder) {// 老用户
                                    List<Order> oldOrders = getOrders(device.getDevNo(), device.getPointNo());
                                    if (oldOrders.size() > 0)
                                        oldOrder = oldOrders.get(getRandom(0, oldOrders.size() - 1));
                                }

                                // 生成订单、订单详情、t_we_user、交易流水（每个商品一次购买1~2个）
                                DeviceAisle randomDeviceAisle = deviceAisles.get(getRandom(0, deviceAisles.size() - 1));
                                saveOrder(oldOrder, device, orderTime, randomDeviceAisle, 1);

                                // 更改剩余数量，应补货数量
                                log.info("*****定时生成演示用订单数据 ********更改剩余数量，应补货数量！！！***");
                                if (randomDeviceAisle.getStock() != null && randomDeviceAisle.getStock() > 0) {
                                    randomDeviceAisle.setStock(randomDeviceAisle.getStock() - 1);
                                    randomDeviceAisle.setSupplementNo(randomDeviceAisle.getSupplementNo() + 1);
                                } else {
                                    randomDeviceAisle.setStock(randomDeviceAisle.getCapacity());
                                    randomDeviceAisle.setSupplementNo(0);
                                }

                                genericDao.update(randomDeviceAisle);

                                // 保存设备日志
                                log.info("*****定时生成演示用订单数据 ********保存设备日志！！！***");
                                saveDeviceLog(device, randomDeviceAisle.getModel(), randomDeviceAisle);
                            }
                        }

                    }

                }
            }
        }

        log.info("*****定时生成演示用订单数据 ********结束***");
    }

    public Map<String, Map<String, String>> initDevTimeOrderData() {
        Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();

        Map<String, String> map1 = new HashMap<String, String>();
        map1.put("0,6,14,15,19,22,23", "0,4");//0点，6点，...   ：生成0至3条随机订单
        map1.put("7,8,10,11,17,18,20", "3,8");
        map1.put("9,12", "3,8");
        map1.put("13,16", "2,5");
        map1.put("21", "2,4");
        map.put("93004090,93004091,93004092", map1);//设备93003116   ：月销售额在5000~8000之间 （设备数量占所有设备的30%）

        Map<String, String> map2 = new HashMap<String, String>();
        map2.put("6,7", "0,2");
        map2.put("8,11,20", "1,3");
        map2.put("9,12", "2,6");
        map2.put("10", "0,3");
        map2.put("13,14,15,16,17,18,19", "0,3");//设备83000001   ：月销售额在2000~5000之间 （设备数量占所有设备的70%）
        map.put("93003116,93004089,83000002,83000004,83000005,83000008,83000013,83000022,83000023,83000024,83000025", map2);

        Map<String, String> map3 = new HashMap<String, String>();
        map3.put("0,6,14,15,19,22,23", "0,1");//0点，6点，...   ：生成0至3条随机订单
        map3.put("7,8,10,11,17,18,20", "0,2");
        map3.put("9,12", "1,3");
        map3.put("13,16", "0,2");
        map3.put("21", "0,1");
        map.put("83000020,83000021,83000001,83000003,83000016,83000006,83000007,83000009,83000010,83000011,83000014,83000012,83000018,83000017,83000019,83000015", map3);//设备93003116   ：月销售额在5000~8000之间 （设备数量占所有设备的30%）

        return map;
    }

    /**
     * 周末订单数
     */
    public Map<String, Map<String, String>> initHolidayDevTimeOrderData() {
        Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();

        Map<String, String> map1 = new HashMap<String, String>();
        map1.put("9,12", "3,6");//0点，6点，...   ：生成0至3条随机订单
        map1.put("10,14,15,16", "2,5");
        map1.put("11,13,19", "2,4");
        map1.put("17,18", "1,3");
        map.put("93004090,93004091,93004092", map1);//设备93003116   ：月销售额在5000~8000之间 （设备数量占所有设备的30%）

        Map<String, String> map2 = new HashMap<String, String>();
        map2.put("9,11,16", "1,3");
        map2.put("10,12,14,18,19", "0,2");
        map2.put("13,15,17", "0,2");
        map2.put("10", "0,2");
        map2.put("13,14,15,16,17,18,19", "1,3");//设备83000001   ：月销售额在2000~5000之间 （设备数量占所有设备的70%）
        map.put("93003116,93004089,83000002,83000004,83000005,83000008,83000013,83000022,83000023,83000024,83000025", map2);

        Map<String, String> map3 = new HashMap<String, String>();
        map3.put("7,8,10,11,17,18,20", "0,1");//0点，6点，...   ：生成0至1条随机订单
        map3.put("9,12", "0,2");
        map3.put("13,16", "0,1");
        map.put("83000020,83000021,83000001,83000003,83000016,83000006,83000007,83000009,83000010,83000011,83000014,83000012,83000018,83000017,83000019,83000015", map3);//设备93003116   ：月销售额在5000~8000之间 （设备数量占所有设备的30%）

        return map;
    }

    /**
     * 设备开业时间
     * @return
     */
    public Map<String, String> initDevOpeningTimeData() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("83000001", "2016-08-21");
        map.put("83000002", "2016-09-02");
        map.put("83000003,83000016", "2016-09-08");
        map.put("83000004", "2016-09-11");
        map.put("83000005,83000006", "2016-09-15");
        map.put("83000007", "2016-09-18");
        map.put("83000008,83000009,83000010", "2016-09-20");
        map.put("83000011", "2016-09-21");
        map.put("83000012,83000013,83000014", "2016-09-22");
        map.put("83000015", "2016-09-27");
        map.put("83000017,83000018,83000019", "2016-09-25");
        map.put("83000020,83000021", "2016-08-15");
        map.put("83000022", "2016-08-03");
        map.put("83000023", "2016-12-05");
        map.put("83000024,83000025", "2016-08-18");
        map.put("93003116,93004089,93004090,93004091,93004092", "2016-10-24");
        return map;
    }

    /**
     * 初始化设备销售数据
     */
    @Override
    public void initData() {
//		String[] factoryDevNoArr = {"83000001", "83000002", "83000003", "83000004", "83000005", "83000006", "83000007", "83000008",
//									"83000009", "83000010", "83000011", "83000012", "83000013", "83000014", "83000015", "83000016",
//									"83000017", "83000018", "83000019", "83000020", "83000021", "83000022", "83000023", "83000024",
//									"83000025", "93003116", "93004089", "93004090", "93004091", "93004092"};
        String[] factoryDevNoArr = {};

//		String[] factoryDevNoArr = {"83000020","83000021","83000001","83000003","83000016","83000006","83000007","83000009","83000010","83000011","83000014","83000012","83000018","83000017","83000019","83000015","93004090", "93004091","93004092"};

//		String[] factoryDevNoArr = {"93004090", "93004091","93004092"};

        for (String factoryDevNo : factoryDevNoArr) {
            Device device = findDeviceByFactoryDevNo(factoryDevNo);
            if (null == device)
                continue;

            List<DeviceAisle> deviceAisles = findEffectDeviceAisles(device.getDevNo());
            // 开业时间
            String openingTimeStr = getDeviceOpeningTime(factoryDevNo);

            Timestamp openingTime = DateUtil.stringToTimestamp(openingTimeStr, DateUtil.YYYY_MM_DD_EN);
            // 跑今天之前的数据
            Timestamp curTime = DateUtil.stringToTimestamp(DateUtil.getCurDate(), DateUtil.YYYY_MM_DD_EN);

            while (openingTime.before(curTime)) {// 生成到当前时间为止每天的订单
                int year = DateUtil.getYear(new Date(openingTime.getTime()));
                int month = DateUtil.getMonth(new Date(openingTime.getTime())) - 1;
                int day = DateUtil.getDay(new Date(openingTime.getTime()));
                int weekDay = Integer.valueOf(DateUtil.getWeekOfDate(new Date(openingTime.getTime())));// 周几，大于5为周末
                Map<String, Map<String, String>> devTimeOrderMap = weekDay > 5 ? initHolidayDevTimeOrderData() : initDevTimeOrderData();

                // 订单时间
                List<Timestamp> orderTimes = new ArrayList<Timestamp>();
                int min = 0;
                int max = 0;

                out : for (Map.Entry<String, Map<String, String>> entry : devTimeOrderMap.entrySet()) {
                    List<String> devList = Arrays.asList(entry.getKey().split(","));
                    if (!devList.contains(factoryDevNo))
                        continue;

                    Map<String, String> orderCountMap = entry.getValue();
                    for (Map.Entry<String, String> orderCountEntry : orderCountMap.entrySet()) {
                        List<String> hourList = Arrays.asList(orderCountEntry.getKey().split(","));
                        min = Integer.valueOf(orderCountEntry.getValue().split(",")[0]);
                        max = Integer.valueOf(orderCountEntry.getValue().split(",")[1]);

                        for (String hour : hourList) {
                            int random = getRandom(min, max);// 生成随机订单数
                            for (int i = 0; i < random; i++) {// 随机生成订单时间
                                // 随机生成一个支付时间
                                Timestamp randomTime = new Timestamp(DateUtil.getDate(getRandomTime(year, month, day, Integer.valueOf(hour)), DateUtil.YYYY_MM_DD_HH_MM_SS_EN).getTime());
                                orderTimes.add(randomTime);
                            }
                        }

                    }

                    break out;
                }

                // 生成订单
                for (Timestamp orderTime : orderTimes ) {
                    log.info("*****定时生成演示用订单数据 ********生成一条订单！！！***");
                    log.info("*****设备号：" + factoryDevNo + "********生成一条订单！！！***下单时间：****"+ orderTime +"*****");

                    // 新订单或者是老用户的二次购买
                    boolean newOrder =  getRandom(1, 10) >= 4 ? true : false;
                    Order oldOrder = null;
                    if (!newOrder) {// 老用户
                        List<Order> oldOrders = getOrders(device.getDevNo(), device.getPointNo());
                        if (oldOrders.size() > 0)
                            oldOrder = oldOrders.get(getRandom(0, oldOrders.size() - 1));
                    }

                    // 生成订单、订单详情、t_we_user、交易流水（每个商品一次购买1~2个）
                    DeviceAisle randomDeviceAisle = deviceAisles.get(getRandom(0, deviceAisles.size() - 1));
                    saveOrder(oldOrder, device, orderTime, randomDeviceAisle, 1);

                    // 更改剩余数量，应补货数量
                    log.info("*****定时生成演示用订单数据 ********更改剩余数量，应补货数量！！！***");
                    if (randomDeviceAisle.getStock() != null && randomDeviceAisle.getStock() > 0) {
                        randomDeviceAisle.setStock(randomDeviceAisle.getStock() - 1);
                        randomDeviceAisle.setSupplementNo(randomDeviceAisle.getSupplementNo() + 1);
                    } else {
                        randomDeviceAisle.setStock(randomDeviceAisle.getCapacity());
                        randomDeviceAisle.setSupplementNo(0);
                    }

                    genericDao.update(randomDeviceAisle);

                    // 保存设备日志
                    log.info("*****定时生成演示用订单数据 ********保存设备日志！！！***");
                    saveDeviceLog(device, randomDeviceAisle.getModel(), randomDeviceAisle);
                }

                // 加一天
                String nextDayStr = DateUtil.getDateStringOfDay(DateUtil.dateToDateString(new Date(openingTime.getTime()), DateUtil.YYYY_MM_DD_EN), 1, DateUtil.YYYY_MM_DD_EN);
                openingTime = DateUtil.stringToTimestamp(nextDayStr, DateUtil.YYYY_MM_DD_EN);
            }
        }


    }

    /**
     * 获取设备开业时间
     * @param factoryDevNo
     * @return
     */
    public String getDeviceOpeningTime(String factoryDevNo) {
        String openingTimeStr = "2016-12-01";
        Map<String, String> openingTimeMap = initDevOpeningTimeData();
        for (Map.Entry<String, String> entry : openingTimeMap.entrySet()) {
            List<String> factoryDevNos = Arrays.asList(entry.getKey().split(","));
            if (factoryDevNos.contains(factoryDevNo)) {
                openingTimeStr = entry.getValue();
                break;
            }
        }
        return openingTimeStr;
    }

    /**
     * 设备列表
     */
    public List<Device> findDevices(Long orgId) {
        StringBuffer buf = new StringBuffer("SELECT ");
        List<Object> args = new ArrayList<Object>();
        String cols = SQLUtils.getColumnsSQL(Device.class, "D");
        buf.append(cols);
        buf.append(" ,SO.NAME AS ORGNAME,PP.POINT_NO AS pointNo ");
        buf.append(" ,R.FACTORY_DEV_NO AS FACTORYDEVNO ");
        buf.append(" FROM T_DEVICE D LEFT JOIN T_CABINET C ON D.ID = C.DEVICE_ID ");
        buf.append(" LEFT JOIN SYS_ORG SO ON D.ORG_ID=SO.ID ");
        buf.append(" LEFT JOIN T_POINT_PLACE PP ON PP.ID = D.POINT_ID ");
        buf.append(" LEFT JOIN T_DEVICE_RELATION R ON R.DEV_NO = D.DEV_NO ");
        buf.append(" WHERE 1 = 1  ");

        buf.append(" AND D.ORG_ID=? ");
        args.add(orgId);

        buf.append(" AND D.BIND_STATE = ? ");
        args.add(Commons.BIND_STATE_SUCCESS);

        buf.append(" AND PP.STATE != ? ");
        args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除

//		buf.append(" and D.dev_no not in (select device_no from t_order where org_id = ? group by device_no) ");
//		args.add(orgId);

//		buf.append(" and D.id >= 346 ");

        buf.append(" group by ").append(cols).append(" ,SO.NAME,PP.POINT_NO ,R.FACTORY_DEV_NO ");
        List<Device> devices = genericDao.findTs(Device.class, buf.toString(), args.toArray());

        return devices;
    }

    /**
     * 取得设备信息
     */
    public Device findDeviceByFactoryDevNo(String factoryDevNo) {
        StringBuffer buf = new StringBuffer("SELECT ");
        List<Object> args = new ArrayList<Object>();
        String cols = SQLUtils.getColumnsSQL(Device.class, "D");
        buf.append(cols);
        buf.append(" ,SO.NAME AS ORGNAME,PP.POINT_NO AS pointNo ");
        buf.append(" ,R.FACTORY_DEV_NO AS FACTORYDEVNO ");
        buf.append(" FROM T_DEVICE D ");
        buf.append(" LEFT JOIN SYS_ORG SO ON D.ORG_ID=SO.ID ");
        buf.append(" LEFT JOIN T_POINT_PLACE PP ON PP.ID = D.POINT_ID ");
        buf.append(" LEFT JOIN T_DEVICE_RELATION R ON R.DEV_NO = D.DEV_NO ");
        buf.append(" WHERE 1 = 1  ");

        buf.append(" AND R.FACTORY_DEV_NO=? ");
        args.add(factoryDevNo);

        buf.append(" AND D.BIND_STATE = ? ");
        args.add(Commons.BIND_STATE_SUCCESS);

        buf.append(" AND PP.STATE != ? ");
        args.add(Commons.POINT_PLACE_STATE_DELETE);// 删除

        return genericDao.findT(Device.class, buf.toString(), args.toArray());
    }

    /**
     * 取得指定年月的随机时间
     * @param year
     * @param month
     * @return
     */
    public static String getRandomTime(int year, int month) {
        Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, getRandom(1, 28));
        calendar.set(Calendar.HOUR_OF_DAY, getRandom(0, 23));
        calendar.set(Calendar.MINUTE, getRandom(0, 59));
        calendar.set(Calendar.SECOND, getRandom(0, 59));

        return DateUtil.dateToDateString(calendar.getTime());
    }

    public static String getRandomTime(int year, int month, int day, int hour) {
        Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, getRandom(0, 59));
        calendar.set(Calendar.SECOND, getRandom(0, 59));

        return DateUtil.dateToDateString(calendar.getTime());
    }

    public static int getRandom(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }

    public List<DeviceAisle> findEffectDeviceAisles(String deviceNumber) {
        StringBuffer buffer = new StringBuffer("SELECT ");
        buffer.append(SQLUtils.getColumnsSQL(DeviceAisle.class, "A"));
        buffer.append(" , C.CABINET_NO as cabinetNo, C.MODEL as model ");
        buffer.append(" FROM T_DEVICE_AISLE A ");
        buffer.append(" LEFT JOIN T_DEVICE D ON A.DEVICE_ID = D.ID ");
        buffer.append(" LEFT JOIN T_CABINET C ON A.CABINET_ID = C.ID ");
        buffer.append(" WHERE D.DEV_NO=? AND A.PRODUCT_ID IS NOT NULL AND A.PRICE_ON_LINE IS NOT NULL ");
        return genericDao.findTs(DeviceAisle.class, buffer.toString(), deviceNumber);
    }

    public List<Order> getOrders(String devNo, String pointNo) {
        StringBuffer buf = new StringBuffer("SELECT ");
        List<Object> args = new ArrayList<Object>();
        String cols = SQLUtils.getColumnsSQL(Order.class, "O");
        buf.append(cols);
        buf.append(" FROM T_ORDER O ");
        buf.append(" WHERE 1 = 1  ");

        buf.append(" AND O.DEVICE_NO=? ");
        args.add(devNo);
        buf.append(" AND O.POINT_NO=? ");
        args.add(pointNo);

        buf.append(" AND O.STATE=? ");
        args.add(Commons.ORDER_STATE_FINISH);

        return genericDao.findTs(Order.class, buf.toString(), args.toArray());
    }

    /**
     * 生成订单信息
     *
     * @param shoppingCart
     * @param device
     * @return
     */
    public Order saveOrder(Order oldOrder, Device device, Timestamp payTime, DeviceAisle deviceAisle, Integer qty) {
        // 生成订单信息
        Order order = new Order();
        order.initDefaultValue();
        order.setCode(RandomUtil.getCharAndNumr(32));// 外部订单号
        order.setCreateTime(payTime);
        order.setDeviceNo(device.getDevNo());// 设备编号
        order.setPointNo(device.getPointNo());
        // 商品总金额
        order.setAmount(MathUtil.mul(deviceAisle.getPriceOnLine(), qty));// 订单总金额
        order.setState(Commons.ORDER_STATE_FINISH);// 已完成
        order.setOrgId(device.getOrgId());
        order.setPayType(6);// 支付类型  6:微信  7：支付宝
        order.setCoupone(0D);
        order.setUsername(null != oldOrder ? oldOrder.getUsername() : "openid-" + RandomUtil.getCharAndNumr(25));
        order.setPayCode("paycode-" + RandomUtil.getCharAndNumr(21));
        order.setPayTime(payTime);

        int count = genericDao.save(order);
        if (count > 0) {
            // 构建订单详情信息
            boolean successFlag = saveOrderDetails(deviceAisle, order, device, payTime, qty);
            if (!successFlag)
                return null;
        }

        // t_we_user
        saveWeChatUser(order.getUsername(), device.getId(), device.getOrgId(), device.getDevNo(), payTime);

        // 追加一条充值交易流水
        saveTradeFlow(device.getOrgId(), order.getAmount(), payTime);
        log.info("***saveTradeFlow成功***");

        return order;
    }

    /**
     * 取得销售额
     */
    @SuppressWarnings("unused")
    private double getSalesAmount(Date startTime, Date endTime, String devNo, String pointNo) {
        List<Object> args = new ArrayList<Object>();
        StringBuffer buf = new StringBuffer(" SELECT COALESCE(sum(OD.qty*OD.price), 0) as salesAmount ");
        buf.append(" FROM T_ORDER O ");
        buf.append(" LEFT JOIN T_ORDER_DETAIL OD ON O.CODE = OD.ORDER_NO ");
        buf.append(" LEFT JOIN T_PRODUCT P ON OD.SKU_ID = P.ID ");
        buf.append(" WHERE 1 = 1 ");

        // 开始日期
        if (null != startTime) {
            buf.append(" AND O.PAY_TIME>=? ");
            args.add(DateUtil.getStartDate(startTime));
        }
        // 结束日期
        if (null != endTime) {
            buf.append(" AND O.PAY_TIME<=? ");
            args.add(DateUtil.getEndDate(endTime));
        }

        buf.append(" AND O.DEVICE_NO = ? ");
        args.add(devNo);

        buf.append(" AND O.POINT_NO =? ");
        args.add(pointNo);

        buf.append(" AND O.STATE = ? ");
        args.add(Commons.ORDER_STATE_FINISH);

        buf.append(" AND P.ID IS NOT NULL ");

        Double amount = genericDao.findSingle(Double.class, buf.toString(), args.toArray());
        return amount == null ? 0 : amount;
    }

    /**
     * 构建订单详情信息
     *
     * @param products
     */
    public boolean saveOrderDetails(DeviceAisle deviceAisle, Order order, Device device, Timestamp payTime, Integer qty) {
        boolean successFlag = false;
        if (findOrderDetail(order.getCode())) {// 之前没有该订单号的订单详情信息
            // 遍历Map，构建订单详情信息
            OrderDetail detail = new OrderDetail();
            detail.setOrderNo(order.getCode());// 外部订单号
            detail.setOrgId(device.getOrgId());
            detail.setPrice(deviceAisle.getPriceOnLine());// 根据设备同步时间情况，确定产品实际价格
            detail.setQty(qty);
            detail.setSkuId(deviceAisle.getProductId());
            detail.setCreateTime(payTime);
            detail.setCurrency("CNY");
            detail.setOrderType(Commons.ORDER_TYPE_COMMON);
            detail.setDiscount(1.0);
            order.addOrderDetail(detail);
            genericDao.save(detail);
        }
        successFlag = true;
        return successFlag;
    }

    public boolean saveWeChatUser(String openId, Long devId, Long orgId, String devNo, Timestamp payTime) {
        if(StringUtil.isEmpty(openId))
            return false;
        int count = genericDao.findSingle(int.class, "SELECT COUNT(*) FROM T_WE_USER WHERE OPEN_ID=?", openId);
        if (count == 0) {
            WeUser user = new WeUser();
            user.setDeviceId(devId);
            user.setDevNo(devNo);
            user.setCreateTime(payTime);
            user.setNickname("");
            user.setOpenId(openId);
            user.setUnionId("os-SVswhjY7Cab4kURTfFiJAVApM");
            user.setOrgId(orgId);
            genericDao.save(user);
            return true;
        }
        return false;
    }

    /**
     * 保存充值交易流水信息
     */
    public void saveTradeFlow(Long orgId, Double amount, Timestamp payTime) {
        // 从交易流水表中查询当前用户最近时间的流水
        List<Object> args = new ArrayList<Object>();
        StringBuffer buf = new StringBuffer("SELECT ");
        String cols = SQLUtils.getColumnsSQL(TradeFlow.class, "C");
        buf.append(cols);
        buf.append(" FROM T_TRADE_FLOW C WHERE C.ORG_ID = ? AND C.TRADE_STATUS = ? ORDER BY C.TRADE_TIME DESC ");
        args.add(orgId);
        args.add(Commons.TRADE_STATUS_SUCCESS);
        TradeFlow tradeFlow = genericDao.findT(TradeFlow.class, buf.toString(), args.toArray());
        if (null == tradeFlow) {
            tradeFlow = genTradeFlow(orgId, payTime);
            if (null != tradeFlow && tradeFlow.getTradeAmount() > 0)
                genericDao.save(tradeFlow);
        } else {// 之前有交易流水记录
            TradeFlow tFlow = new TradeFlow();
            tFlow.setOrgId(orgId);
            tFlow.setTradeType(Commons.TRADE_TYPE_RECHARGE);
            tFlow.setTradeAmount(amount);// 交易金额
            tFlow.setTradeTime(payTime);
            tFlow.setBalance(MathUtil.round(MathUtil.add(tradeFlow.getBalance(), amount), 2));
            tFlow.setTradeStatus(Commons.TRADE_STATUS_SUCCESS);
            genericDao.save(tFlow);
        }
    }

    /**
     * 构建初始化交易流水信息
     *
     * @param user 当前用户
     * @return 初始化交易流水信息
     */
    private TradeFlow genTradeFlow(Long orgId, Timestamp payTime) {
        // 取得总销售额(所有商品总销售额)
        User user = new User();
        user.setOrgId(orgId);
        double salesTotalAmount = getSalesAmount(user, null);

        TradeFlow tradeFlow = new TradeFlow();
        tradeFlow.setOrgId(orgId);
        tradeFlow.setTradeType(Commons.TRADE_TYPE_RECHARGE);
        tradeFlow.setTradeAmount(MathUtil.round(salesTotalAmount, 2));
        tradeFlow.setTradeTime(payTime);
        tradeFlow.setBalance(MathUtil.round(salesTotalAmount, 2));
        tradeFlow.setTradeStatus(Commons.TRADE_STATUS_SUCCESS);
        return tradeFlow;
    }
    /************************演示数据**********结束***************/

    /**
     * 【设备APP接口】39.查询所有商品编码为负数的商品信息
     * @return
     */
    @Override
    public ResultList<SyncRoadSalesInfo> syncMinusProductNoGoods(ResultList<SyncRoadSalesInfo> rl) throws Exception {
        List<SyncRoadSalesInfo> roadSalesList = new ArrayList<SyncRoadSalesInfo>();

        // 查询所有商品编码为负数的商品信息
        List<Product> products = productService.findMinusProductNoGoods();
        for (Product product : products) {
            SyncRoadSalesInfo roadSalesInfo = new SyncRoadSalesInfo();

            roadSalesInfo.setProductNo(StringUtils.isEmpty(product.getCode()) ? "" : product.getCode());// 商品编码
            roadSalesInfo.setProductName(StringUtils.isEmpty(product.getSkuName()) ? "" : product.getSkuName());// 商品名称
            roadSalesInfo.setOriginal(StringUtils.isEmpty(product.getOrigin()) ? "" : product.getOrigin());// 产地
            roadSalesInfo.setSpec(StringUtils.isEmpty(product.getSpec()) ? "" : product.getSpec());// 商品规格

            // 商品图文描述
            StringBuffer buffer = new StringBuffer("");
            if (!StringUtils.isEmpty(product.getDescription())) {
                String[] arr = product.getDescription().split("src=\"");
                for (String img : arr)
                    if (img.indexOf("\"") != -1)
                        buffer.append(img.substring(0, img.indexOf("\""))).append(";");
                if (buffer.length() > 0)
                    buffer.setLength(buffer.length() - 1);
            }
            roadSalesInfo.setDesc(buffer.toString());

            roadSalesInfo.setType(product.getType() == null ? 0 : product.getType());//商品类型
            roadSalesInfo.setCagetory_type(product.getCategory() == null ? 0 : product.getCategory());// 商品类别
            roadSalesInfo.setBasePrice(MathUtil.round(product.getPrice() == null ? 0 : product.getPrice(), 2));//商品标准价格
            roadSalesInfo.setZhekou_num(1d);// 折扣值
            roadSalesInfo.setDeletePrice(MathUtil.round(product.getPrice() == null ? 0 : product.getPrice(), 2));//零售价
            roadSalesInfo.setPrice(MathUtil.round(MathUtil.mul(roadSalesInfo.getDeletePrice(), roadSalesInfo.getZhekou_num()), 2));// 折后价

            roadSalesInfo.setState(Commons.SELLABLE_TRUE);//商品可售状态  0：不可售  1：可售
            // 商品图片
            String images = product.getImages();
            if (StringUtils.isEmpty(images)) {
                roadSalesInfo.setPicUrl("");
                roadSalesInfo.setPicDetailUrl("");
            } else {
                String[] imagesArr = images.split(";");
                boolean isSetProduct = false;
                boolean isSetProductDetail = false;
                for (String image : imagesArr) {
                    if (isSetProduct && isSetProductDetail)
                        break;
                    if (!isSetProduct && Commons.FILE_PRODUCT == Integer.valueOf(image.split(",")[2])) {
                        isSetProduct = true;
                        roadSalesInfo.setPicUrl(dictionaryService.getFileServer() + image.split(",")[3]);
                    } else if (!isSetProductDetail && Commons.FILE_PRODUCT_DETAIL == Integer.valueOf(image.split(",")[2])) {
                        isSetProductDetail = true;
                        roadSalesInfo.setPicDetailUrl(dictionaryService.getFileServer() + image.split(",")[3]);
                    }
                }
            }

            roadSalesInfo.setHots(0L);
            roadSalesList.add(roadSalesInfo);
        }
        rl.setResultCode(0);// 成功
        rl.setResultList(roadSalesList);
        rl.setResultMessage("发送成功!");
        return rl;
    }
}
