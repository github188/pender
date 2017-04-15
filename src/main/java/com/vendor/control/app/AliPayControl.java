package com.vendor.control.app;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.ecarry.core.web.control.BaseControl;
import com.google.gson.Gson;
import com.vendor.po.Device;
import com.vendor.po.Order;
import com.vendor.service.IDeviceService;
import com.vendor.service.IOrderService;
import com.vendor.service.IRefundService;
import com.vendor.service.IVendingService;
import com.vendor.util.Commons;
import com.vendor.util.RequestMappingUtil;
import com.vendor.util.URLPathUtil;
import com.vendor.util.alipay.AlipayUtil;
import com.vendor.vo.app.QRCodeOrders;

/**
 * Created by Chris Zhu on 2017/3/23.
 * 支付宝支付控制器类
 *
 * @author Chris Zhu
 */
@Controller
@RequestMapping(value = "/free/pay/alipay")
public class AliPayControl extends BaseControl
{
    private static final String TRADE_SUCCESS = "TRADE_SUCCESS";//交易成功可退款

    private static final String TRADE_FINISHED = "TRADE_FINISHED";//交易成功不可退款或者超过可退款期限

    private static final Logger logger = LoggerFactory.getLogger(PayEnterControl.class);

    private final IVendingService mIVendingService;

    private final IOrderService mIOrderService;

    private final IDeviceService mIDeviceService;

    private final IRefundService mIRefundService;




    @Autowired
    public AliPayControl(IVendingService vendingService, IOrderService orderService, IDeviceService deviceService, IRefundService mIRefundService)
    {
        this.mIVendingService = vendingService;
        this.mIOrderService = orderService;
        this.mIDeviceService = deviceService;
        this.mIRefundService = mIRefundService;
    }

    /**
     * 生成订单并跳转到支付页面
     *
     * @param response the response
     * @param facDevNo the fac dev no
     * @param prods    the prods
     */
    @RequestMapping(value = "/createOrderAndForwardToPayPage.do")
    public void createOrderAndForwardToPayPage(HttpServletResponse response, @RequestParam("machNo") String facDevNo, String prods) throws Exception
    {
        QRCodeOrders qrCodeOrders = mIVendingService.getQRCodeOrders(facDevNo, prods);
        String orderNo = mIVendingService.saveQRCodeOrders(qrCodeOrders, Commons.PAY_TYPE_ALI);//订单编号
        logger.info("createOrderAndForwardToPayPage(logged by Chris Zhu):orderNo=" + orderNo);
        response.sendRedirect(URLPathUtil.getBaseUrl()+ "/vendor/html/aliPay.jsp?orderNo=" + orderNo +
                "&facDevNo=" + facDevNo);
    }

    /**
     * 发起支付
     *
     * @param response the response
     * @param orderNo  the order no
     * @param amount   the amount
     * @throws AlipayApiException the alipay api exception
     * @throws IOException        the io exception
     */
    @RequestMapping(value = "/launchPay.do")
    public void launchPay(HttpServletResponse response, String orderNo, String amount) throws AlipayApiException, IOException
    {
        logger.info("launchPay(logged by Chris Zhu):发起支付，内部订单号：" + orderNo + "，金额：" + amount);
        AlipayClient alipayClient = AlipayUtil.getAliPayClient();
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();//创建API对应的request

        //设置支付完成后阿里后台回调我方服务后台的地址
        String notifyUrl = RequestMappingUtil.getUrl(this.getClass(), "notifyUrl", HttpServletRequest.class);
        logger.info("launchPay(logged by Chris Zhu):notifyUrl="+notifyUrl);
        alipayRequest.setNotifyUrl(notifyUrl);

        //设置支付完成后的手机端(支付宝)的回调地址
        String returnUrl = RequestMappingUtil.getUrl(this.getClass(), "returnUrl", HttpServletRequest.class, HttpServletResponse.class);
        logger.info("launchPay(logged by Chris Zhu):returnUrl="+returnUrl);
        alipayRequest.setReturnUrl(returnUrl);

        alipayRequest.setBizContent(this.getBizJsonString(amount,orderNo));//填充业务参数
        String form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(form);//直接将完整的表单html输出到页面
        response.getWriter().flush();
    }

    private String getBizJsonString(String amount,String orderNo)
    {
        BizContent bizContent = new BizContent();
        bizContent.setTotal_amount(amount);
        bizContent.setOut_trade_no(orderNo);
        Gson gson = new Gson();
        return gson.toJson(bizContent);
    }

    /**
     * 支付完成后的回调地址，在此判断支付是否成功，如果成功则跳转到aliPaySucces.html
     * 重要：此回调依赖于扫码手机的网络状态和其他手机环境因素，所以只应用作支付宝客户端的反馈，相关订单的处理需要在nitifyUrl里处理
     * @param request the request
     */
    @RequestMapping(value = "/returnUrl.do")
    public void returnUrl(HttpServletRequest request, HttpServletResponse response) throws IOException, AlipayApiException
    {
        logger.info("returnUrl(logged by Chris Zhu):支付完成阿里前台回调");
        if (verifyResult(request))//支付成功
        {
            logger.info("returnUrl(logged by Chris Zhu):支付成功，重定向到成功页");
            String orderNo = request.getParameter("out_trade_no");
            response.sendRedirect(URLPathUtil.getBaseUrl() + "/vendor/html/aliPaySuccess.html?orderNo=" + orderNo);
        }
        else
        {
            logger.info("returnUrl(logged by Chris Zhu):支付失败");
        }
    }

    /**
     * 支付完成后阿里后台回调的地址，在此对订单、出货等进行相关处理
     * 注意：退款等订单相关的操作完成后阿里也会调用此处，此处我们只处理还未完成的订单
     * @param request  the request
     */
    @RequestMapping(value = "/notifyUrl.do")
    public void notifyUrl(HttpServletRequest request) throws UnsupportedEncodingException, AlipayApiException
    {
        logger.info("notifyUrl(logged by Chris Zhu):阿里支付宝后台回调");
        if (this.verifyResult(request))
        {
            String trade_status = request.getParameter("trade_status");
            logger.info("notifyUrl(logged by Chris Zhu):trade_status="+trade_status);
            if (TRADE_SUCCESS.equals(trade_status) || TRADE_FINISHED.equals(trade_status))
            {
                //验证回调通知是否异常

                String orderNo = request.getParameter("out_trade_no");//内部订单号
                Order order = mIOrderService.getOrderByCode(orderNo);
                if (order.getState() == 8)//对于已经完成订单不需要处理
                {
                    logger.info("notifyUrl(logged by Chris Zhu):订单已完成，无需处理！");
                    return;
                }
                if (!AlipayUtil.verifyNotify(order, request))
                {
                    logger.error("notifyUrl(logged by Chris Zhu):回调有效性验证失败，忽略此支付！");
                    return;
                }
                //更新订单信息
                updateOrder(request,order);

                Device device = mIDeviceService.findDeviceByDevNo(order.getDeviceNo());
                if (device == null)
                {
                    logger.error("notifyUrl(logged by Chris Zhu):设备不存在，忽略此支付");
                    return;
                }
                boolean deliverResult = false;
                try
                {
                    deliverResult = mIDeviceService.notifyDeviceDeliver(order);
                }
                catch (Exception e)
                {
                    logger.error("notifyUrl(logged by Chris Zhu):通知设备出货出错："+e.getMessage(),e);
                }
                if (!deliverResult)//通知设备出货失败
                {
                    logger.error("notifyUrl(logged by Chris Zhu):设备离线通知出货失败！");
                    if(TRADE_FINISHED.equals(trade_status))
                    {
                        logger.info("notifyUrl(logged by Chris Zhu):该订单不能退款或则超过可退款期限！");
                    }
                    else
                    {
                        logger.info("notifyUrl(logged by Chris Zhu):生成退款单，待定时任务执行退款操作");
                        //此处只生成退款单，具体退款操作由定时任务完成
                        mIRefundService.saveRefundOrder(order);
                    }
                }
                // 追加一条充值交易流水
                mIOrderService.saveTradeFlow(device.getOrgId(), order.getAmount());

            }
        }
    }

    //更新订单信息
    private void updateOrder(HttpServletRequest request,Order order)
    {

        String aliPayOrderNo = request.getParameter("trade_no");//支付宝订单号
        String userId = request.getParameter("buyer_id");//买家支付宝id
        String userName = request.getParameter("buyer_logon_id");//买家支付宝账号
        Timestamp payTime = Timestamp.valueOf(request.getParameter("gmt_payment"));//付款时间

        if (order!=null)
        {
            order.setPayCode(aliPayOrderNo);
            order.setUsername(userName);
            order.setPayTime(payTime);
            order.setUserId(Long.decode(userId));
            order.setState(8);
            mIOrderService.updateOrder(order);
        }
    }

    private boolean verifyResult(HttpServletRequest request) throws UnsupportedEncodingException, AlipayApiException
    {
        Map<String, String> params = new HashMap<>();
        Map requestParams = request.getParameterMap();
        for (Object requestParam : requestParams.keySet())
        {
            String name = (String) requestParam;
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++)
            {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }
        return AlipayUtil.rsaCheckV1(params);
    }




    @SuppressWarnings("unused")
    private class BizContent
    {
        private String out_trade_no;//商家内部订单编号
        private String total_amount;//总支付金额
        private String subject = "支付确认";//标题

        void setOut_trade_no(String out_trade_no)
        {
            this.out_trade_no = out_trade_no;
        }

        void setTotal_amount(String total_amount)
        {
            this.total_amount = total_amount;
        }
    }
}
