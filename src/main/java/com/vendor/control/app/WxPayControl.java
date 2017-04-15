package com.vendor.control.app;

import com.ecarry.core.exception.BusinessException;
import com.ecarry.core.web.control.BaseControl;
import com.ecarry.core.web.core.ContextUtil;
import com.vendor.service.IVendingService;
import com.vendor.util.Commons;
import com.vendor.util.URLPathUtil;
import com.vendor.util.WeChatUtil;
import com.vendor.vo.app.QRCodeOrders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by Chris Zhu on 2017/3/23.
 * 微信支付控制器类
 *
 * @author Chris Zhu
 */
@Controller
@RequestMapping(value = "/free/pay/wxpay")
public class WxPayControl extends BaseControl
{
    private static final Logger logger = LoggerFactory.getLogger(WxPayControl.class);

    @Value("${wx.appid}")
    private String wxAppId;

    @Value("${wx.oauth2.access_token.url}")
    private String accessTokenUrl;

    @Value("${wx.secret}")
    private String wxAppSecret;


    private final IVendingService vendingService;

    @Autowired
    public WxPayControl(IVendingService vendingService)
    {
        this.vendingService = vendingService;
    }

    /**
     * 生成订单并重定向到微信支付页面(pay.jsp)
     * 微信授权之后调用此处
     *
     * @param code   the code
     * @param openId the open id
     * @param machNo the mach no
     * @param prods  the prods
     */
    @RequestMapping(value = "/createOrderAndForwardToWxPay.do")
    public void createOrderAndForwardToWxPay(HttpServletResponse response, String code, String openId, String machNo, String prods) throws Exception
    {
        //3.获取code之后回到此方法，在通过以下获取到openid之后回到此方法执行第(1)步

        // 静默授权成功，根据code获取网页授权access_token及openId
        logger.info("*****code：*****" + code);
        String tokenUrlJsonStr = WeChatUtil.getAccessToken(accessTokenUrl, wxAppId, wxAppSecret, code);

        logger.info("*****tokenUrlJsonStr：*****" + tokenUrlJsonStr);
        Map map = ContextUtil.getTByJson(Map.class, tokenUrlJsonStr);

        String errcode = (String) map.get("errcode");
        if (StringUtils.isEmpty(errcode))
            openId = (String) map.get("openid");

        logger.info("*****openId：*****" + openId);
        if (StringUtils.isEmpty(openId))
            throw new BusinessException("openId为空");

        // 根据orders生成订单信息及订单号
        logger.info("*****根据orders生成订单信息及订单号****开始***");


        QRCodeOrders qrCodeOrders = vendingService.getQRCodeOrders(machNo, prods);
        String orderNo = vendingService.saveQRCodeOrders(qrCodeOrders, Commons.PAY_TYPE_WX);//订单编号

        logger.info("*****生成订单编号：****" + orderNo);
        logger.info("*****根据orders生成订单信息及订单号****结束***");

        response.sendRedirect(URLPathUtil.getBaseUrl() + "/vendor/html/pay.jsp?orderNo=" + orderNo + "&openId=" + openId);

    }

}
