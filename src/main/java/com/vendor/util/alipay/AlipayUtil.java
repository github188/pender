package com.vendor.util.alipay;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.vendor.po.Order;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Created by Chris Zhu on 2017/3/27.
 *
 * @author Chris Zhu
 */
public class AlipayUtil
{
    private static String mGateway;

    private static String mAppid;

    private static String mAppPrivateKey;

    private static String mPublicKey;

    private static String mSellId;

    private static AlipayClient mAlipayClient;

    static {
        ResourceBundle bundle = ResourceBundle.getBundle("pay");
        mGateway = bundle.getString("alipay.gateway");
        mAppid = bundle.getString("alipay.appid");
        mAppPrivateKey = bundle.getString("alipay.app_private_key");
        mPublicKey = bundle.getString("alipay.public_key");
        mSellId = bundle.getString("alipay.sell_id");
    }

    /**
     * @see AlipayClient
     *
     * @return the ali pay client
     */
    public static  AlipayClient getAliPayClient()
    {
        if (mAlipayClient == null)
        {
            return new DefaultAlipayClient(mGateway, mAppid, mAppPrivateKey,
                    "json", "UTF-8", mPublicKey, "RSA2");
        }
        return mAlipayClient;
    }

    /**
     * @see AlipaySignature#rsaCheckV1
     *
     * @param params the params
     * @return the boolean
     * @throws AlipayApiException the alipay api exception
     */
    public static boolean rsaCheckV1(Map<String, String> params) throws AlipayApiException
    {
         return AlipaySignature.rsaCheckV1(params, mPublicKey, "UTF-8", "RSA2");
    }

    /**
     * 验证notify回调的有效性
     *
     * @param order   the order
     * @param request the request
     * @return the boolean
     */
    public static boolean verifyNotify(Order order, HttpServletRequest request)
    {
        return order.getCode().equals(request.getParameter("out_trade_no")) &&
                order.getAmount().toString().equals(request.getParameter("total_amount")) &&
                mAppid.equals(request.getParameter("app_id")) &&
                mSellId.equals(request.getParameter("seller_id"));
    }
}
