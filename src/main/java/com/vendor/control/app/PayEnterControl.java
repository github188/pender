package com.vendor.control.app;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ecarry.core.exception.BusinessException;
import com.ecarry.core.web.control.BaseControl;
import com.vendor.util.RequestMappingUtil;

/**
 * Created by Chris on 2017/3/17.
 * 支付控制器类
 */
@Controller
@RequestMapping(value = "/free/pay")
public class PayEnterControl extends BaseControl
{
    private static final Logger logger = LoggerFactory.getLogger(PayEnterControl.class);

    @Value("${wx.appid}")
    private String wxAppId;


    @Value("${user_agent.wx}")
    private String userAgentWx;

    @Value("${wx.getweixincode.url}")
    private String wechatCodeUrl;




    /**
     * 手机端扫码调用入口
     *
     * @param request  the request
     * @param response the response
     * @param facDevNo 设备出厂编号
     * @param prods    商品信息
     */

    @RequestMapping(value = "/qrcodePay.do")
    public void forwardPay(HttpServletRequest request, HttpServletResponse response,
                           @RequestParam("machNo") String facDevNo, String prods) throws IOException, ServletException
    {
        logger.info("**************客户端扫码调用**************");
        //验证设备编号和商品信息
        if (StringUtils.isEmpty(facDevNo) || StringUtils.isEmpty(prods))
            throw new BusinessException("订单商品信息为空");
        //根据客户端类型做相应处理
        String ua = request.getHeader("user-agent").toLowerCase();
        if (ua.contains(userAgentWx))//扫码客户端为微信
        {
            logger.info("**************微信扫码**************");

            StringBuilder redirectUrl = new StringBuilder();//微信授权成功后的回调地址(见WxPayControl#createOrderAndForwardToWxPay方法)
            redirectUrl.append(this.getCreateOrderAndForwardToWxPayUrl()).append("?machNo=")
                    .append(facDevNo).append("&prods=").append(prods);
            logger.info("**************回调url：" + redirectUrl);

            StringBuilder wxAuthUrl = new StringBuilder();//微信授权URL

            wxAuthUrl.append(wechatCodeUrl)
                    .append("appid=").append(wxAppId).append("&redirect_uri=")
                    .append(URLEncoder.encode(redirectUrl.toString(), "utf-8"))
                    .append("&scope=snsapi_base&state=123#wechat_redirect");
            logger.info("**************微信授权Url：" + wxAuthUrl);

            //重定向到微信授权页
            response.sendRedirect(wxAuthUrl.toString());

        }
        else
        {
            logger.info("**************支付宝扫码**************");
            //重定向到订单生成

            request.getRequestDispatcher(RequestMappingUtil.getDefaultFullRequestMapping(AliPayControl.class,"createOrderAndForwardToPayPage",
                   HttpServletResponse.class, String.class, String.class)).forward(request,response);
        }

    }

    private String getCreateOrderAndForwardToWxPayUrl()
    {
        return RequestMappingUtil.getUrl(WxPayControl.class, "createOrderAndForwardToWxPay", HttpServletResponse.class, String.class,
                String.class, String.class, String.class);
    }
    

}
