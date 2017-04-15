package com.vendor.control.app;

import com.ecarry.core.web.control.BaseControl;
import com.ecarry.core.web.core.ContextUtil;
import com.ecarry.core.web.view.ByteView;
import com.ecarry.core.web.view.DownloadView;
import com.ecarry.core.web.view.StringToImageView;
import com.ecarry.core.web.view.TextView;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.vendor.po.InfoBean;
import com.vendor.po.Order;
import com.vendor.po.ProductReplacement;
import com.vendor.po.User;
import com.vendor.service.*;
import com.vendor.util.Commons;
import com.vendor.vo.app.*;
import com.vendor.vo.common.ResultAppBase;
import com.vendor.vo.common.ResultBase;
import com.vendor.vo.common.ResultList;
import com.vendor.vo.web.JoinUsData;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.*;

/**
 * @author dranson on 2015年7月30日
 */
@SuppressWarnings("deprecation")
@Controller
@RequestMapping(value = "/free")
public class FreeControl extends BaseControl {

    private static Logger logger = Logger.getLogger(FreeControl.class);

    @Value("${file.path}")
    private String rootPath;

    @Value("${wx.oauth2.access_token.url}")
    private String accessTokenUrl;

    @Value("${wx.appid}")
    private String wxAppId;

    @Value("${wx.secret}")
    private String wxAppSecret;

    @Value("${vendor.forward.pay.url}")
    private String vendorPayUrl;

    @Autowired
    private IDiscountService discountService;

    @Autowired
    private ILotteryService lotteryService;

    @Autowired
    private IProductService productService;

    @Autowired
    private IVendingService vendingService;

    @Autowired
    private IInteractionService interactionService;

    @Autowired
    private IMonitoringService monitoringService;

    @Value("${mail.to}")
    private String mailTo;

    @Value("${wx.getweixincode.url}")
    private String wechatCodeUrl;

    @Autowired
    private IMailService mailService;

    @Autowired
    private IOrgnizationService orgnizationService;

    @RequestMapping(value = "authorize", method = RequestMethod.GET)
    public ModelAndView authorize(String code, String state, HttpServletResponse response) {
        if (state == null)
            return new ModelAndView(new TextView("{\"code\":\"" + code + "\"}"));
        return new ModelAndView(new TextView("{\"code\":\"" + code + "\",\"user\":" + state + "}"));
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "redirect_authorize", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> redirectAuthorize(String code, String state, String deviceNumber, HttpServletResponse response) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("resultCode", -1);
        logger.info("*****【设备APP接口---登录成功重定向】***开始**");
        logger.info("*********code***"+ code +"**");
        logger.info("*********state***"+ state +"**");
        logger.info("*********deviceNumber***"+ deviceNumber +"**");
        if (state == null) {
            map.put("resultMessage", "登录失败");
            return map;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> userMap = objectMapper.readValue(state, Map.class);

        // 校验登录用户的合法性
        Integer userId = (Integer) userMap.get("id");
        map = vendingService.authorizeUserAndDevice(Long.valueOf(userId), deviceNumber, map);

        logger.info("*****【设备APP接口---登录成功重定向】***结束**");
        return map;
    }

    @RequestMapping(value = "downloadChrome.file", method = RequestMethod.GET)
    public ModelAndView downloadChrome() {
        return new ModelAndView(new DownloadView(rootPath + "/Chrome.exe"));
    }

    @RequestMapping(value = "data/readImage.file", method = RequestMethod.GET)
    public ModelAndView readImage(String path) {
        ModelAndView view = new ModelAndView();
        try {
            ByteView outView = new ByteView(new FileInputStream(rootPath + "/" + path));
            outView.setContentType("image/*");
            outView.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
            outView.addHeader("Cache-Control", "post-check=0, pre-check=0");
            outView.setHeader("Pragma", "no-cache");
            int pos = path.lastIndexOf("/") + 1;
            outView.setHeader("Content-Disposition", "attachment;filename=" + path.substring(pos));
            view.setView(outView);
        } catch (Exception e) {
            view.setView(new StringToImageView("图片不存在"));
        }
        return view;
    }

    /**
     * 【设备APP接口】2.店铺与设备绑定
     * @param storeId 店铺编号
     * @param deviceNumber 设备的厂家编号
     * @return
     */
    @RequestMapping(value = "registerDevice.json", method = RequestMethod.POST)
    @ResponseBody
    public ResultBase registerDevice(String storeId , String deviceNumber) {
        logger.info("********【设备APP接口】2.认证设备接口*****开始*****");
        ResultBase rb = new ResultBase();
        if (StringUtils.isEmpty(deviceNumber) || StringUtils.isEmpty(storeId)) {
            rb.setResultMessage("参数不允许为空");
            return rb;
        }
        try {
            rb = vendingService.updateRegisterDevice(rb, storeId, deviceNumber);
        } catch (Exception e) {
            rb.setResultMessage("设备认证失败");
            logger.error(e.getMessage(), e);
        }
        logger.info("********【设备APP接口】2.认证设备接口*****结束*****");
        return rb;
    }

    @RequestMapping(value = "syncDeviceBindState.json", method = RequestMethod.POST)
    @ResponseBody
    public ResultBase syncDeviceBindState(String deviceNumber) {
        logger.info("********【设备APP接口】3.获取设备绑定状态*****开始*****");
        logger.info("*********deviceNumber***"+ deviceNumber +"**");
        ResultBase rb = new ResultBase();
        if (StringUtils.isEmpty(deviceNumber)) {
            rb.setResultMessage("参数不允许为空");
            return rb;
        }
        try {
            rb = vendingService.queryDeviceBindState(rb, deviceNumber);
        } catch (Exception e) {
            rb.setResultMessage("获取设备绑定状态失败");
            logger.error(e.getMessage(), e);
        }
        logger.info("********【设备APP接口】3.获取设备绑定状态*****结束*****");
        return rb;
    }

    /**
     * 【设备APP接口】8.上传本机库存数据接口---将设备的当前每个货道的商品库存数据同步到服务器。
     * @param machineStock 上传本机库存数据接收对象
     * @param lastUploadTime 上次上传时间，第一次为0
     * @return
     */
    @RequestMapping(value = "uploadMachineStocks.json", method = RequestMethod.POST)
    @ResponseBody
    public ResultBase uploadMachineStocks(MachineStock machineStock, Long lastUploadTime) {
        logger.info("********【设备APP接口】8.上传本机库存数据接口*****开始*****");
        ResultBase rb = new ResultBase();
        if (null == machineStock || StringUtils.isEmpty(machineStock.getDeviceNumber()) || StringUtils.isEmpty(machineStock.getStocks())) {
            rb.setResultMessage("参数不允许为空");
            return rb;
        }
        try {
            rb = vendingService.saveUploadMachineStocks(rb, machineStock, lastUploadTime);
        } catch (Exception e) {
            rb.setResultMessage("上传本机库存数据失败");
            logger.error(e.getMessage(), e);
        }
        logger.info("********【设备APP接口】8.上传本机库存数据接口*****结束*****");
        return rb;
    }

    /**
     * 【设备APP接口】9.微信扫码支付，模式二
     * @param qrcodeInfo
     * @return 二维码信息和订单流水号
     */
    @Deprecated
    @RequestMapping(value = "qrCodePay.json", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> qrCodePay(QRCodeRequestInfo qrcodeInfo) {
        logger.info("********【设备APP接口】9.微信扫码支付*****开始*****");
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("resultCode", -1);
        if (null == qrcodeInfo || StringUtils.isEmpty(qrcodeInfo.getDeviceNumber()) || StringUtils.isEmpty(qrcodeInfo.getProductNo())
                || null == qrcodeInfo.getProductCount() || StringUtils.isEmpty(qrcodeInfo.getPayType())) {
            map.put("resultMessage", "参数不允许为空");
            return map;
        }

        try {
            map = vendingService.saveQRCodePay(map, qrcodeInfo);
        } catch (Exception e) {
            map.put("resultMessage", "微信扫码支付请求失败");
            logger.error(e.getMessage(), e);
        }
        logger.info("********【设备APP接口】9.微信扫码支付*****结束*****");
        return map;
    }

    /**
     * 【设备APP接口】9.扫码支付通知回调
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "qrCodeAsyncNotify.do", method = RequestMethod.POST)
    public void qrCodeAsyncNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {
        boolean notifySuccess = false;
        logger.info("********【设备APP接口】9.扫码支付通知回调*****开始*****");
        //示例报文
//		String xml = "<xml><appid><![CDATA[wxb4dc385f953b356e]]></appid><bank_type><![CDATA[CCB_CREDIT]]></bank_type><cash_fee><![CDATA[1]]></cash_fee><fee_type><![CDATA[CNY]]></fee_type><is_subscribe><![CDATA[Y]]></is_subscribe><mch_id><![CDATA[1228442802]]></mch_id><nonce_str><![CDATA[1002477130]]></nonce_str><openid><![CDATA[o-HREuJzRr3moMvv990VdfnQ8x4k]]></openid><out_trade_no><![CDATA[1000000000051249]]></out_trade_no><result_code><![CDATA[SUCCESS]]></result_code><return_code><![CDATA[SUCCESS]]></return_code><sign><![CDATA[1269E03E43F2B8C388A414EDAE185CEE]]></sign><time_end><![CDATA[20150324100405]]></time_end><total_fee>1</total_fee><trade_type><![CDATA[JSAPI]]></trade_type><transaction_id><![CDATA[1009530574201503240036299496]]></transaction_id></xml>";
        String inputLine;
        String notityXml = "";
        String resXml = "";
        BufferedOutputStream out = null;

        try {
            while ((inputLine = request.getReader().readLine()) != null) {
                notityXml += inputLine;
            }
            logger.info("****【扫码支付通知回调】*****接收到微信回调信息***" + notityXml);

            notifySuccess = vendingService.saveQRCodeAsyncNotify(notityXml);
            if (notifySuccess) // 通知成功
                resXml = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";

            out = new BufferedOutputStream(response.getOutputStream());
            out.write(resXml.getBytes());
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            try {
                if (request.getReader() != null)
                    request.getReader().close();
                if (out != null) {
                    out.flush();
                    out.close();
                }
            } catch (Exception e2) {
                logger.error(e2.getMessage());
            }
        }

        logger.info("********【设备APP接口】9.扫码支付通知回调*****结束*****");
    }

    /**
     * 通联微信扫码支付异步通知回调
     */
    @RequestMapping(value = "/wxAsyncNotify.do", method = RequestMethod.POST)
    public void wxAsyncNotify(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.info("********【设备APP接口】9.通联微信扫码支付异步通知回调*****开始*****");
        request.setCharacterEncoding("gbk");// 通知传输的编码为GBK
        response.setCharacterEncoding("gbk");
        TreeMap<String, String> params = getParams(request);// 动态遍历获取所有收到的参数,此步非常关键,因为收银宝以后可能会加字段,动态获取可以兼容
        try {
            // 验签完毕进行业务处理
            vendingService.saveWxAsyncNotify(params);
        } catch (Exception e) {// 处理异常
            logger.error(e.getMessage(), e);
        } finally { // 收到通知,返回success
            response.getOutputStream().write("success".getBytes());
            response.flushBuffer();
        }
        logger.info("********【设备APP接口】9.通联微信扫码支付异步通知回调*****结束*****");

    }

    /**
     * 动态遍历获取所有收到的参数,此步非常关键,因为收银宝以后可能会加字段,动态获取可以兼容由于收银宝加字段而引起的签名异常
     * @param request
     * @return
     */
    @SuppressWarnings("rawtypes")
    private TreeMap<String, String> getParams(HttpServletRequest request) {
        TreeMap<String, String> map = new TreeMap<String, String>();
        Map reqMap = request.getParameterMap();
        for (Object key : reqMap.keySet()) {
            String value = ((String[]) reqMap.get(key))[0];
            System.out.println(key + ";" + value);
            map.put(key.toString(), value);
        }
        return map;
    }

    /**
     * 【设备APP接口】10.根据订单流水号查询交易状态的接口
     * @param orderNo 订单流水号
     * @return
     */
    @RequestMapping(value = "queryOrderStatus.json", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> queryOrderStatus(String orderNo) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("resultCode", -1);
        logger.info("********orderNo:*****" + orderNo);
        if (StringUtils.isEmpty(orderNo)) {
            map.put("resultMessage", "参数不允许为空");
            return map;
        }

        try {
            map = vendingService.findOrderStatus(map, orderNo);
        } catch (Exception e) {
            map.put("resultMessage", "查询交易状态请求失败");
            logger.error(e.getMessage(), e);
        }
        return map;
    }

    /**
     * 【设备APP接口】11.获取激活码接口
     * @return
     */
    @RequestMapping(value = "syncActiveCode.json")
    @ResponseBody
    public Map<String, Object> syncActiveCode() {
        logger.info("********【设备APP接口】11.获取激活码接口*****开始*****");
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("resultCode", -1);
        try {
            map = vendingService.syncActiveCode(map);
        } catch (Exception e) {
            map.put("resultMessage", "获取激活码失败");
            logger.error(e.getMessage(), e);
        }
        logger.info("********【设备APP接口】11.获取激活码接口*****结束*****");
        return map;
    }

    /**
     * 【设备APP接口】12.同步激活码状态接口
     * @return
     */
    @RequestMapping(value = "syncActiveCodeState.json", method = RequestMethod.POST)
    @ResponseBody
    public ResultBase syncActiveCodeState(String deviceNumber, String activeCode, Integer state) {
        ResultBase rb= new ResultBase();
        logger.info("********deviceNumber:*****" + deviceNumber);
        logger.info("********activeCode:*****" + activeCode);
        logger.info("********state:*****" + state);
        if (StringUtils.isEmpty(deviceNumber) || StringUtils.isEmpty(activeCode) || null == state) {
            rb.setResultMessage("参数不允许为空");
            return rb;
        }
        try {
            rb = vendingService.syncActiveCodeState(rb, deviceNumber, activeCode, state);
        } catch (Exception e) {
            rb.setResultMessage("同步激活码状态失败");
            logger.error(e.getMessage(), e);
        }
        return rb;
    }

    /**
     * 【设备APP接口】13.获取设备点位绑定状态接口
     * @return
     */
    @RequestMapping(value = "findDeviceBindState.json", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> findDeviceBindState(String deviceNumber) {
        logger.info("********【设备APP接口】13.获取设备点位绑定状态接口*****开始*****");
        logger.info("********deviceNumber:*****" + deviceNumber);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("resultCode", -1);
        if (StringUtils.isEmpty(deviceNumber)) {
            map.put("resultMessage", "参数不允许为空");
            return map;
        }
        try {
            map = vendingService.findDeviceBindState(map, deviceNumber);
        } catch (Exception e) {
            map.put("resultMessage", "获取设备点位绑定状态失败");
            logger.error(e.getMessage(), e);
        }
        logger.info("********【设备APP接口】13.获取设备点位绑定状态接口*****结束*****");
        return map;
    }

    /**
     * 【设备APP接口】15.检查版本更新
     * @return
     */
    @RequestMapping(value = "downloadAppVersion.file", method = RequestMethod.GET)
    public ModelAndView downloadAppVersion() {
        logger.info("********【设备APP接口】15.检查版本更新*****开始*****");
        return new ModelAndView(new DownloadView(rootPath + "/update_zjwl_bmtsystem.xml"));
    }

    /**
     * 【设备APP接口】16.下载指定版本的售货机APP
     * @return
     */
    @RequestMapping(value = "{version}/downloadVendingMachine.file", method = RequestMethod.GET)
    public ModelAndView downloadVendingMachine(@PathVariable("version") String version) {
        logger.info("********【设备APP接口】16.下载指定版本的售货机APP*****开始*****");
        logger.info("********version:*****" + version);
        return new ModelAndView(new DownloadView(rootPath + "/apk/bmtsystem/vendingMachine/" + version + "/VendingMachine.apk"));
    }

    /**
     * 【设备APP接口】17.接受APP上传过来的日志文件，并保存到服务器
     * @return
     */
    @RequestMapping(value = "/uploadLog.file", method = RequestMethod.POST)
    @ResponseBody
    public ResultBase uploadLog(HttpServletRequest request, HttpServletResponse response) {
        logger.info("********【设备APP接口】17.接受APP上传过来的日志文件，并保存到服务器*****开始*****");
        ResultBase rb = new ResultBase();
        try {
            rb = vendingService.uploadLog(rb, request, response);
        } catch (Exception e) {
            rb.setResultMessage("接受APP日志失败");
            logger.error(e.getMessage(), e);
        }
        logger.info("********【设备APP接口】17.接受APP上传过来的日志文件，并保存到服务器*****结束*****");
        return rb;
    }

    /**
     * 【设备APP接口】18.接受APP上传过来的异常信息，并保存到服务器
     * @return
     */
    @RequestMapping(value = "/uploadExceptionMsg.json", method = RequestMethod.POST)
    @ResponseBody
    public ResultBase uploadExceptionMsg(String deviceNumber, String version, String exception) {
        logger.info("********【设备APP接口】18.接受APP上传过来的异常信息，并保存到服务器*****开始*****");
        logger.info("********deviceNumber:*****" + deviceNumber);
        logger.info("********version:*****" + version);
        ResultBase rb = new ResultBase();
        if (StringUtils.isEmpty(deviceNumber) || StringUtils.isEmpty(version) || StringUtils.isEmpty(exception)) {
            rb.setResultMessage("参数不允许为空");
            return rb;
        }
        try {
            rb = vendingService.uploadExceptionMsg(rb, deviceNumber, version, exception);
        } catch (Exception e) {
            rb.setResultMessage("接受APP异常信息失败");
            logger.error(e.getMessage(), e);
        }
        logger.info("********【设备APP接口】18.接受APP上传过来的异常信息，并保存到服务器*****结束*****");
        return rb;
    }

    /**
     * 【设备APP接口】21.获取H5支付信息接口
     * @param orderNo
     * @return 预支付交易回话标识
     */
    @RequestMapping(value = "unifiedorder.json", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> unifiedorder(String orderNo, String openId) {
        logger.info("********【设备APP接口】21.获取H5支付信息接口*****开始*****");
        logger.info("********orderNo:*****" + orderNo);
        logger.info("********openId:*****" + openId);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("resultCode", -1);
        if (StringUtils.isEmpty(orderNo) || StringUtils.isEmpty(openId)) {
            map.put("resultMessage", "参数不允许为空");
            return map;
        }

        try {
            map = vendingService.unifiedorder(map, orderNo, openId);
        } catch (Exception e) {
            map.put("resultMessage", "获取H5支付信息接口请求失败");
            logger.error(e.getMessage(), e);
        }
        logger.info("********【设备APP接口】21.获取H5支付信息接口*****结束*****");
        return map;
    }

    /**
     * 【设备APP接口】22.微信公众号支付通知回调
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "officialAccountsAsyncNotify.do", method = RequestMethod.POST)
    public void officialAccountsAsyncNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {
        boolean notifySuccess = false;
        logger.info("********【设备APP接口】22.微信公众号支付通知回调*****开始*****");
        String inputLine;
        String notityXml = "";
        String resXml = "";
        BufferedOutputStream out = null;

        try {
            while ((inputLine = request.getReader().readLine()) != null) {
                notityXml += inputLine;
            }
            logger.info("****【微信公众号支付通知回调】*****接收到微信回调信息***" + notityXml);

            notifySuccess = vendingService.saveOfficialAccountsAsyncNotify(notityXml);
            if (notifySuccess) // 通知成功
                resXml = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";

            out = new BufferedOutputStream(response.getOutputStream());
            out.write(resXml.getBytes());
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            try {
                if (request.getReader() != null)
                    request.getReader().close();
                if (out != null) {
                    out.flush();
                    out.close();
                }
            } catch (Exception e2) {
                logger.error(e2.getMessage());
            }
        }

        logger.info("********【设备APP接口】22.微信公众号支付通知回调*****结束*****");
    }

    /**
     * 【设备APP接口】23.下载订单商品信息
     * @param orderNo 订单编号
     * @return
     */
    @RequestMapping(value = "syncCartProductInfo.json", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> syncCartProductInfo(String orderNo) {
        logger.info("********【设备APP接口】23.下载订单商品信息*****开始*****");
        logger.info("********orderNo:*****" + orderNo);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("resultCode", -1);
        if (StringUtils.isEmpty(orderNo)) {
            map.put("resultMessage", "参数不允许为空");
            return map;
        }

        try {
            map = vendingService.findSyncCartProductInfo(map, orderNo);
        } catch (Exception e) {
            map.put("resultMessage", "下载订单商品信息失败");
            logger.error(e.getMessage(), e);
        }
        logger.info("********【设备APP接口】23.下载订单商品信息*****结束*****");
        return map;
    }

    /**
     * 【设备APP接口】24.更改后台库存接口：设备出货指令成功后，通知后台更改库存
     * @return
     */
    @RequestMapping(value = "uploadVendorStock.json", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> saveUploadVendorStock(QRCodeOrders qrCodeOrders) {
        logger.info("********【设备APP接口】24.更改后台库存接口*****开始*****");
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("resultCode", -1);

        logger.info("*************qrCodeOrders.getMachineNum():*****" + qrCodeOrders.getMachineNum());
        logger.info("*************qrCodeOrders.getOrderNo():*****" + qrCodeOrders.getOrderNo());
        logger.info("*************qrCodeOrders.getProductNo():*****" + qrCodeOrders.getProductNo());
        logger.info("*************qrCodeOrders.getProductCount():*****" + qrCodeOrders.getProductCount());
        logger.info("*************qrCodeOrders.getCabinetNo():*****" + qrCodeOrders.getCabinetNo());
        logger.info("*************qrCodeOrders.getRoadNo():*****" + qrCodeOrders.getRoadNo());
        logger.info("*************qrCodeOrders.getType():*****" + qrCodeOrders.getType());
        logger.info("*************qrCodeOrders.getOrderType():*****" + qrCodeOrders.getOrderType());
        if (null == qrCodeOrders || StringUtils.isEmpty(qrCodeOrders.getMachineNum()) || StringUtils.isEmpty(qrCodeOrders.getOrderNo())
                || StringUtils.isEmpty(qrCodeOrders.getProductNo()) || null == qrCodeOrders.getProductCount() || 0 == qrCodeOrders.getProductCount()
                || StringUtils.isEmpty(qrCodeOrders.getCabinetNo()) || StringUtils.isEmpty(qrCodeOrders.getRoadNo())
                || null == qrCodeOrders.getType()) {
            map.put("resultMessage", "参数不允许为空");
            return map;
        }

        try {
            map = vendingService.saveUploadVendorStock(map, qrCodeOrders);
        } catch (Exception e) {
            map.put("resultMessage", "更改后台库存接口请求失败");
            logger.error(e.getMessage(), e);
        }
        logger.info("********【设备APP接口】24.更改后台库存接口*****结束*****");
        return map;
    }

    /**
     * 【设备APP接口】25.获取jsconfig参数信息
     * @param url
     * @return
     */
    @RequestMapping(value = "findJsConfig.json", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> findJsConfig(String url, String openId) {
        logger.info("********【设备APP接口】25.获取jsconfig参数信息*****开始*****");
        logger.info("********openId:*****" + openId);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("resultCode", -1);
        if (StringUtils.isEmpty(url) || StringUtils.isEmpty(openId)) {
            map.put("resultMessage", "参数不允许为空");
            return map;
        }

        try {
            map = vendingService.findJsConfig(map, url, openId);
        } catch (Exception e) {
            map.put("resultMessage", "获取jsconfig参数信息请求失败");
            logger.error(e.getMessage(), e);
        }
        logger.info("********【设备APP接口】25.获取jsconfig参数信息*****结束*****");
        return map;
    }

    /**
     * 【设备APP接口】26.检查守护进程版本更新
     * @return
     */
    @RequestMapping(value = "downloadDaemonVersion.file", method = RequestMethod.GET)
    public ModelAndView downloadDaemonVersion() {
        logger.info("********【设备APP接口】26.检查守护进程版本更新*****开始*****");
        return new ModelAndView(new DownloadView(rootPath + "/update_daemon_bmtsystem.xml"));
    }

    /**
     * 【设备APP接口】27.下载指定版本的守护进程APP
     * @return
     */
    @RequestMapping(value = "{version}/downloadDaemon.file", method = RequestMethod.GET)
    public ModelAndView downloadDaemon(@PathVariable("version") String version) {
        logger.info("********【设备APP接口】27.下载指定版本的守护进程APP*****开始*****");
        logger.info("********version:*****" + version);
        return new ModelAndView(new DownloadView(rootPath + "/apk/bmtsystem/daemon/" + version + "/Daemon.apk"));
    }

    /**
     * 【设备APP接口】29.获取商品信息接口---将服务器端设置的，本机设备销售的商品数据下载到本地。
     * @param machineNum 设备组号
     * @return
     */
    @RequestMapping(value = "getGoodsInfo.json")
    @ResponseBody
    public ResultList<SyncProductInfo> getGoodsInfo(String machineNum) {
        logger.info("********【设备APP接口】29.获取商品信息接口*****开始*****");
        logger.info("********machineNum:*****" + machineNum);
        ResultList<SyncProductInfo> rl = new ResultList<SyncProductInfo>();
        if (StringUtils.isEmpty(machineNum)) {
            rl.setResultMessage("参数不允许为空");
            return rl;
        }
        try {
            rl = vendingService.findGoodsInfo(rl, machineNum);
        } catch (Exception e) {
            rl.setResultMessage("获取商品信息失败");
            logger.error(e.getMessage(), e);
        }
        logger.info("********【设备APP接口】29.获取商品信息接口*****结束*****");
        return rl;
    }

    /**
     * 【设备APP接口】31.货柜商品同步数据接口---将服务器端设置的，本机设备销售的商品数据下载到本地。
     * @param machineNum 设备组号
     * @param cabinetNo 货柜号
     * @return
     */
    @RequestMapping(value = "updateMachineCabGoods.json")
    @ResponseBody
    public ResultList<SyncRoadSalesInfo> getUpdateMachineCabGoods(String machineNum, String cabinetNo) {
        logger.info("********【设备APP接口】31.货柜商品同步数据接口*****开始*****");
        logger.info("********machineNum:*****" + machineNum);
        logger.info("********cabinetNo:*****" + cabinetNo);
        ResultList<SyncRoadSalesInfo> rl = new ResultList<SyncRoadSalesInfo>();
        if (StringUtils.isEmpty(machineNum)) {
            rl.setResultMessage("参数不允许为空");
            return rl;
        }
        try {
            rl = vendingService.saveUpdataMachineCabGoods(rl, machineNum, cabinetNo);
        } catch (Exception e) {
            rl.setResultMessage("货柜商品同步数据接口失败");
            logger.error(e.getMessage(), e);
        }
        logger.info("********【设备APP接口】31.货柜商品同步数据接口*****结束*****");
        return rl;
    }

    /**
     * 【设备APP接口】32.同步换货商品数据接口
     */
    @RequestMapping(value= "/updateReplaceProducts.json" , method = RequestMethod.POST)
    @ResponseBody
    public ResultBase updateReplaceProducts(ProductReplacement replacement) {
        logger.info("********【设备APP接口】32.同步换货商品数据接口*****开始*****");
        logger.info("********replacement.getMachineNum():*****" + replacement.getMachineNum());
        logger.info("********replacement.getCabinetNo():*****" + replacement.getCabinetNo());
        logger.info("********replacement.getRoadNo():*****" + replacement.getRoadNo());
        logger.info("********replacement.getReplaceCapacity():*****" + replacement.getReplaceCapacity());
        logger.info("********replacement.getUserId():*****" + replacement.getUserId());
        ResultBase rb = new ResultBase();
        if (null == replacement || StringUtils.isEmpty(replacement.getMachineNum()) || StringUtils.isEmpty(replacement.getCabinetNo())
                || StringUtils.isEmpty(replacement.getRoadNo()) || null == replacement.getReplaceCapacity() || replacement.getReplaceCapacity() < 0
                || null == replacement.getUserId()) {
            rb.setResultMessage("参数不允许为空");
            return rb;
        }
        try {
            rb = vendingService.saveUpdateReplaceProducts(rb, replacement);
        } catch (Exception e) {
            rb.setResultMessage("同步换货商品数据失败");
            logger.error(e.getMessage(), e);
        }
        logger.info("********【设备APP接口】32.同步换货商品数据接口*****结束*****");
        return rb;
    }

    /**
     * 【设备APP接口】33.上传机器阿里推送设备ID接口
     * @return
     */
    @RequestMapping(value = "syncPushDeviceId.json", method = RequestMethod.POST)
    @ResponseBody
    public ResultBase syncActiveCodeState(String machineNum, String deviceId) {
        logger.info("********【设备APP接口】33.上传机器阿里推送设备ID接口*****开始*****");
        logger.info("********machineNum:*****" + machineNum);
        logger.info("********deviceId:*****" + deviceId);
        ResultBase rb= new ResultBase();
        if (StringUtils.isEmpty(machineNum) || StringUtils.isEmpty(deviceId)) {
            rb.setResultMessage("参数不允许为空");
            return rb;
        }
        try {
            rb = vendingService.savcePushDeviceId(rb, machineNum, deviceId);
        } catch (Exception e) {
            rb.setResultMessage("上传机器阿里推送设备ID失败");
            logger.error(e.getMessage(), e);
        }
        logger.info("********【设备APP接口】33.上传机器阿里推送设备ID接口*****结束*****");
        return rb;
    }

    /**
     * www.ziyoubang.cn首页【申请加入】， 发送申请邮件
     * @param userName 用户姓名
     * @param mobile 电话
     * @param city 所在城市
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "sendMail.json")
    public void sendMail(String userName, String mobile, String city, String callback, HttpServletResponse response) throws IOException {
        response.setContentType("application/javascript");
        response.setCharacterEncoding("UTF-8");
        JoinUsData data = new JoinUsData();
        if (!mobile.matches(Commons.REGEX_MOBILE) && !mobile.matches(Commons.REGEX_TELEPHONE)) {
            data.setMsg("请输入合法的联系方式");
        } else {
            SimpleMailMessage mail = new SimpleMailMessage();
            if (!StringUtils.isEmpty(mailTo)) {
                String[] mailToArr = mailTo.split(",");
                mail.setText("申请人：" + userName + "\r\n联系方式：" + mobile + "\r\n所在城市:" + city);
                mail.setTo(mailToArr);
                mail.setSubject("www.ziyoubang.cn首页【申请加入】");
                boolean sendMail = mailService.sendMailText(mail);
                if (!sendMail)
                    data.setMsg("申请失败，请稍后重试");
                else {
                    data.setErr(0);//成功
                    data.setMsg("申请成功！");
                }
            } else
                data.setMsg("申请失败，请稍后重试");
        }

        response.getWriter().print(callback + "(" + ContextUtil.getJson(data) + ")");
        response.getWriter().close();
    }

    /**
     * 【设备APP接口】34.微信JS接口安全域名和网页授权用文件
     * @return
     */
    @RequestMapping(value = "MP_verify_l7RkUtvtgrgR1wxM.txt", method = RequestMethod.GET)
    public ModelAndView downloadWxMP() {
        logger.info("********【设备APP接口】34.微信JS接口安全域名和网页授权用文件*****");
        return new ModelAndView(new DownloadView(rootPath + "/MP_verify_l7RkUtvtgrgR1wxM.txt"));
    }

    /****************************补货APP接口**************开始***********************/

    /**
     * 【补货APP接口】1.登录成功重定向
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "redirect_putaway_authorize", method = RequestMethod.GET)
    @ResponseBody
    public ResultAppBase<UserInfo> redirectPutAwayAuthorize(String code, String state) throws Exception {
        ResultAppBase<UserInfo> rab = new ResultAppBase<UserInfo>();
        logger.info("*****【补货APP接口】1.登录成功重定向***开始**");
        logger.info("*********code***"+ code +"**");
        logger.info("*********state***"+ state +"**");
        if (state == null) {
            rab.setResultMessage("登录失败");
            return rab;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> userMap = objectMapper.readValue(state, Map.class);
        UserInfo userInfo = new UserInfo();
        userInfo.setCode(code);// 获取access_token需要的code
        userInfo.setUserId((Integer) userMap.get("id"));
        userInfo.setUsername((String) userMap.get("username"));
        userInfo.setRealName((String) userMap.get("realName"));
        userInfo.setEmail((String) userMap.get("email"));

        rab.setResultCode(0);
        rab.setResultMessage("登录成功");
        rab.setData(userInfo);
        logger.info("*****【补货APP接口】1.登录成功重定向***结束**");
        return rab;
    }

    /****************************补货APP接口**************结束***********************/

    /**
     * 【设备APP接口】35.砸金蛋演示图片下载
     * @return
     */
    @RequestMapping(value = "downloadGoldenEggs.file", method = RequestMethod.GET)
    public ModelAndView downloadGoldenEggs(String name) {
        logger.info("********【设备APP接口】35.砸金蛋演示图片下载*****开始*****");

        DownloadView downloadView = null;

        if (name == null)
            return null;

        switch (name) {
            case "dandan1":
                downloadView = new DownloadView(rootPath + "/dandan1.png");
                break;
            case "dandan2":
                downloadView = new DownloadView(rootPath + "/dandan2.png");
                break;
            case "dandan3":
                downloadView = new DownloadView(rootPath + "/dandan3.jpg");
                break;

            default:
                break;
        }

        return new ModelAndView(downloadView);
    }

    /**
     * 根据离线消息ID删除离线消息
     * @param messageId
     */
    @RequestMapping(value = "deleteOffLineMessage.json", method = RequestMethod.POST)
    @ResponseBody
    public ResultBase deleteOffLineMessage(Long messageId) {
        logger.info("*****根据【离线消息】ID删除离线消息***开始**");
        logger.info("*****【离线活动ID:"+messageId+"】********");
        ResultBase rb = new ResultBase();
        if (null == messageId) {
            rb.setResultMessage("参数不允许为空");
            return rb;
        }
        try {
            discountService.deleteOffLineMessage(messageId);
            rb.setResultCode(0);// 成功
            rb.setResultMessage("根据离线消息ID删除离线消息成功!");
        } catch (Exception e) {
            rb.setResultMessage("根据离线消息ID删除离线消息失败");
            logger.error(e.getMessage(), e);
        }
        logger.info("*****根据【离线消息】ID删除离线消息***结束**");
        return rb;
    }

    /**
     * 更新抽奖活动内容 isSucessState
     * @param activityId 活动商品编号
     * @param machineNum 设备组号
     * @param isSucessState 活动执行状态 0成功 1 失败
     * @return
     */
    @RequestMapping(value = "syncActivityIsSucess.json", method = RequestMethod.POST)
    @ResponseBody
    public ResultBase syncActivityIsSucess(String activityId, String	machineNum, String	isSucessState){
        logger.info("*****更新抽奖活动内容 isSucessState接口***开始**");
        logger.info("*****【activityId:"+activityId+",machineNum:"+machineNum+",isSucessState:"+isSucessState+"】********");
        ResultBase rb = new ResultBase();
        if (null == activityId || null == machineNum || null == isSucessState) {
            rb.setResultMessage("参数不允许为空");
            return rb;
        }

        try {
            lotteryService.syncActivityIsSucess(activityId, machineNum, isSucessState);
            rb.setResultCode(0);// 成功
            rb.setResultMessage("发送成功");
        } catch (Exception e) {
            rb.setResultMessage("更新抽奖活动内容 isSucessState接口失败");
            logger.error(e.getMessage(), e);
        }
        logger.info("*****更新抽奖活动内容 isSucessState接口***结束**");
        return rb;
    }

    /**
     * 虚拟商品推送上下架状态接口
     * @param machineNum 设备组号
     * @param messageId 消息id
     * @param exeState 执行状态
     * @return
     */
    @RequestMapping(value = "virtualGoodsExeState.json", method = RequestMethod.POST)
    public ResultBase virtualGoodsExeState(String machineNum, String messageId, String exeState){
        logger.info("*****虚拟商品推送上下架状态接口 start *****");
        logger.info("*****【machineNum:"+machineNum+",messageId:"+messageId+",exeState:"+exeState+"】*****");
        ResultBase rb = new ResultBase();
        if(null == machineNum || null == messageId || null == exeState){
            rb.setResultMessage("参数不允许为空");
            return rb;
        }
        try {
            productService.virtualGoodsExeState(machineNum, messageId, exeState);
            rb.setResultCode(0);
            rb.setResultMessage("发送成功");
        } catch (Exception e){
            rb.setResultMessage("虚拟商品推送上下架状态接口失败");
            logger.error(e.getMessage(), e);
        }
        logger.info("*****虚拟商品推送上下架状态接口 end *****");
        return rb;
    }

    /**
     * 更新主题设备状态
     * @param machineNum 设备组号
     * @param themeId 主题Id
     * @param execuTingState 状态值
     * @return
     */
    @RequestMapping(value = "themeDeviceExecutingState.json")
    public ResultBase themeDeviceExecutingState(String machineNum, String themeId, Integer execuTingState){
        logger.info("*****更新主题设备状态接口 start *****");
        logger.info("*****【machineNum:"+ machineNum +"themeId:"+themeId+",execuTingState:"+execuTingState+"】*****");
        ResultBase rb = new ResultBase();
        if(null == machineNum || null == themeId || null == execuTingState){
            rb.setResultMessage("参数不允许为空");
            return rb;
        }
        try {
        interactionService.themeDeviceExecutingState(machineNum, themeId, execuTingState);
        rb.setResultCode(0);
        rb.setResultMessage("发送成功");
    } catch (Exception e){
        rb.setResultMessage("更新主题设备状态接口失败");
        logger.error(e.getMessage(), e);
    }
        logger.info("*****更新主题设备状态接口 end *****");
        return rb;
    }



    /**
     * 【设备APP接口】36.上传设备版本号
     *
     * @param deviceNumber 设备组号
     * @param version 版本号
     * @return
     */
    @RequestMapping(value = "syncDeviceVersion.json", method = RequestMethod.POST)
    @ResponseBody
    public ResultBase syncDeviceVersion(String deviceNumber, String version) {
        logger.info("********【设备APP接口】36.上传设备版本号*****开始*****");
        logger.info("********deviceNumber:*****" + deviceNumber);
        logger.info("********version:*****" + version);
        ResultBase rb = new ResultBase();
        if (StringUtils.isEmpty(deviceNumber) || StringUtils.isEmpty(version)) {
            rb.setResultMessage("参数不允许为空");
            return rb;
        }
        try {
            rb = vendingService.syncDeviceVersion(rb, deviceNumber, version);
        } catch (Exception e) {
            rb.setResultMessage("上传设备版本号失败");
            logger.error(e.getMessage(), e);
        }
        logger.info("********【设备APP接口】36.上传设备版本号*****结束*****");
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
    @RequestMapping(value = "syncRefund.json", method = RequestMethod.POST)
    @ResponseBody
    public ResultBase syncRefund(String orderNo, String productNo, Integer qty, String activityNo) {
        logger.info("********【设备APP接口】37.自动退款接口*****开始*****");
        logger.info("********orderNo:*****" + orderNo);
        logger.info("********productNo:*****" + productNo);
        logger.info("********qty:*****" + qty);
        logger.info("********activityNo:*****" + activityNo);
        ResultBase rb = new ResultBase();
        if (StringUtils.isEmpty(orderNo) || StringUtils.isEmpty(productNo) || null == qty || qty <= 0) {
            rb.setResultMessage("参数不允许为空");
            return rb;
        }
        try {
            rb = vendingService.saveRefund(rb, orderNo, productNo, qty, activityNo);
        } catch (Exception e) {
            rb.setResultMessage("自动退款失败");
            logger.error(e.getMessage(), e);
        }
        logger.info("********【设备APP接口】37.自动退款接口*****结束*****");
        return rb;
    }


    /*************************地图接口**********开始*************************/
    /**
     * 查找所有店铺
     */
    @RequestMapping(value = "/findStores.json")
    @ResponseBody
    public Map<String, Object> findStores() throws Exception {
        logger.info("********【地图接口】查找所有店铺******开始****");
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("storeList", vendingService.findStores());
        logger.info("********【地图接口】查找所有店铺******结束****");
        return map;
    }

    /**
     * 查找所有需要闪烁的店铺
     */
    @RequestMapping(value = "/saveStoreState.json")
    @ResponseBody
    public Map<String, Object> saveStoreState(Timestamp lastUpdateTime) throws Exception {
        logger.info("********【地图接口】查找所有需要闪烁的店铺******开始****");
        Map<String, Object> map = new HashMap<String, Object>();
        logger.info("lastUpdateTime:" + lastUpdateTime);
        long start = System.currentTimeMillis();
        map.put("storeList", vendingService.saveStoreState(lastUpdateTime));
        System.out.println("用时：" + (System.currentTimeMillis() - start) / 1000 + "秒");
        logger.info("********【地图接口】查找所有需要闪烁的店铺******结束****");
        return map;
    }

    /**
     * 【地图】页面相关数据
     */
    @RequestMapping(value = "/findMapOperData.json", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> findMapOperData() {
        return vendingService.findMapOperData();
    }

    /**
     * 【地图】销售统计图
     */
    @RequestMapping(value = "/findSalesAndAmount.json", method = RequestMethod.POST)
    public List<Order> findSalesAndAmount(Date startTime, Date endTime) {
        return vendingService.findSalesData(startTime, endTime);
    }

    /*************************地图接口**********结束*************************/


    /************************演示数据**********开始***************/
    /**
     * 初始化设备销售数据
     */
    @RequestMapping(value = "/initData.json")
    public void initData() {
        logger.info("********【初始化】设备销售数据******开始****");
        vendingService.initData();
        logger.info("********【初始化】设备销售数据******结束****");
    }

    /**
     * 定时改变订单生成时间，须手动执行一次
     */
    @RequestMapping(value = "/changeOrderMapJob.json")
    public void changeOrderMapJob() {
        logger.info("********定时【改变订单生成时间】******开始****");
        vendingService.changeOrderMapJob();
        logger.info("********定时【改变订单生成时间】******结束****");
    }
    /************************演示数据**********结束***************/

    /**
     * 【设备APP接口】39.查询所有商品编码为负数的商品信息
     * @return
     */
    @RequestMapping(value = "syncMinusProductNoGoods.json")
    @ResponseBody
    public ResultList<SyncRoadSalesInfo> syncMinusProductNoGoods() {
        logger.info("********【设备APP接口】39.查询所有商品编码为负数的商品信息*****开始*****");
        ResultList<SyncRoadSalesInfo> rl = new ResultList<SyncRoadSalesInfo>();
        try {
            rl = vendingService.syncMinusProductNoGoods(rl);
        } catch (Exception e) {
            rl.setResultMessage("查询所有商品编码为负数的商品信息失败");
            logger.error(e.getMessage(), e);
        }
        logger.info("********【设备APP接口】39.查询所有商品编码为负数的商品信息*****结束*****");
        return rl;
    }

    /**
     * 【设备APP接口】38.下载指定版本的补货APP
     * @return
     */
    @RequestMapping(value = "{version}/downloadPutaway.file", method = RequestMethod.GET)
    public ModelAndView downloadPutaway(@PathVariable("version") String version) {
        logger.info("********【设备APP接口】38.下载指定版本的补货APP*****开始*****");
        logger.info("********version:*****" + version);
        return new ModelAndView(new DownloadView(rootPath + "/apk/putaway/" + version + "/Putaway.apk"));
    }
    /**
     * 定时改变订单生成时间，须手动执行一次
     */
    /************************演示数据**********结束***************/


    /**
     * 【设备APP接口】40.根据设备查询货柜货道可见状态
     *
     * @param machineNum 设备编号
     * @return
     */
    @RequestMapping(value = "findCabinetVisiableState.json", method = RequestMethod.POST)
    @ResponseBody
    public ResultList<CabinetVisiableStateInfo> findCabinetVisiableState(String machineNum) {
        logger.info("********【设备APP接口】40.根据设备查询货柜货道可见状态*****开始*****");
        logger.info("********machineNum:*****" + machineNum);
        ResultList<CabinetVisiableStateInfo> rl = new ResultList<CabinetVisiableStateInfo>();
        if (StringUtils.isEmpty(machineNum)) {
            rl.setResultMessage("参数不允许为空");
            return rl;
        }
        try {
            rl = vendingService.findCabinetVisiableState(rl, machineNum);
        } catch (Exception e) {
            rl.setResultMessage("根据设备查询货柜货道可见状态失败");
            logger.error(e.getMessage(), e);
        }
        logger.info("********【设备APP接口】40.根据设备查询货柜货道可见状态*****结束*****");
        return rl;
    }

    /**
     * 【设备APP接口】41.获取推荐商品数据接口
     *
     * @param deviceNumber 设备编号
     * @return
     */
    @RequestMapping(value = "getRecommendGoods.json", method = RequestMethod.POST)
    @ResponseBody
    public ResultList<RecommendGoodsInfo> findRecommendGoods(String deviceNumber) {
    	logger.info("********【设备APP接口】41.获取推荐商品数据接口*****开始*****");
    	logger.info("********deviceNumber:*****" + deviceNumber);
    	ResultList<RecommendGoodsInfo> rl = new ResultList<RecommendGoodsInfo>();
    	if (StringUtils.isEmpty(deviceNumber)) {
    		rl.setResultMessage("参数不允许为空");
    		return rl;
    	}
    	try {
    		rl = vendingService.findRecommendGoods(rl, deviceNumber);
    	} catch (Exception e) {
    		rl.setResultMessage("获取推荐商品数据接口失败");
    		logger.error(e.getMessage(), e);
    	}
    	logger.info("********【设备APP接口】41.获取推荐商品数据接口*****结束*****");
    	return rl;
    }

    /**
     * 获取当前登录用户信息
     */
    @RequestMapping(value = "/findCurrentUser.json", method = RequestMethod.POST)
    public User findCurrentUser() {
        return orgnizationService.findCurrentUser();
    }

    /**
	 * 修改当前登录用户信息
	 */
	@RequestMapping(value = "/saveCurrentUser.json", method = RequestMethod.POST)
	public void saveCurrentUser(User user) {
		orgnizationService.saveCurrentUser(user);
	}

    /**
     * 上传监控数据
     * @param file
     * @param factoryDevNo
     * @return
     */
    @RequestMapping(value = "uploadMonitoringFile.json")
	public ResultBase uploadFile(HttpServletRequest request) throws Exception{
        ResultBase rb = new ResultBase();
		//将当前上下文初始化给  CommonsMutipartResolver （多部分解析器）
		CommonsMultipartResolver multipartResolver=new CommonsMultipartResolver(request.getSession().getServletContext());
		MultipartFile file = null;
        String paramType = null;
        //检查form中是否有enctype="multipart/form-data"
		if(multipartResolver.isMultipart(request)){
			//将request变成多部分request
			MultipartHttpServletRequest multiRequest=(MultipartHttpServletRequest)request;
			//获取multiRequest 中所有的文件名
			Iterator iter=multiRequest.getFileNames();
			while(iter.hasNext()){
				//一次遍历所有文件
				file = multiRequest.getFile(iter.next().toString());
            }
            paramType = multiRequest.getParameter("paramType").toString();
        }

		rb = monitoringService.uploadMonitoringFile(file, file.getOriginalFilename().split("_")[0], paramType, rb);
		return rb;
	}


    /**
     * 更新设备监控设备信息
     * @param monitoringDetails
     * @return
     */
	@RequestMapping(value = "updateDeviceMonitoringDeteils.json")
	public ResultBase updateDeviceMonitoringDeteils(String monitoringDetails) {
        ResultBase rb = new ResultBase();
        if (null == monitoringDetails){
            rb.setResultMessage("参数不能为空");
            return rb;
        }
        try {
            Gson json = new Gson();
            InfoBean infoBean = json.fromJson(monitoringDetails, InfoBean.class);
            monitoringService.updateDeviceMonitoringDeteils(infoBean);
            rb.setResultCode(0);
            rb.setResultMessage("更新成功");
        } catch (JsonSyntaxException e) {
            rb.setResultCode(-1);
            e.printStackTrace();
        }
        return rb;
    }


}