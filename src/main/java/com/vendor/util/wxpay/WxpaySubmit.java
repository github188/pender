package com.vendor.util.wxpay;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.vendor.util.MD5Util;

/* *
 *类名：WxpaySubmit
 *功能：微信接口请求提交类
 *详细：构造微信接口表单HTML文本，获取远程HTTP数据
 */

public class WxpaySubmit {
    
    /**
     * 统一支付接口
     */
    public static final String WXPAY_GATEWAY = "https://vsp.allinpay.com/apiweb/weixin/pay";
	
    /**
     * 生成要请求给收银宝的参数数组
     * @param sParaTemp 请求前的参数数组
     * @return 要请求的参数数组
     */
    public static Map<String, Object> buildRequestPara(Map<String, Object> sParaTemp) {
        //除去数组中的空值和签名参数
        Map<String, Object> sPara = WxpayCore.paraFilter(sParaTemp);
        //生成签名结果
        String prestr = WxpayCore.createLinkString(sPara);// appid=00000000&cusid=990440153996000&paytype=0&randomstr=123&reqsn=123456789&trxamt=1
        String mysign = MD5Util.MD5(prestr).toUpperCase();// 67ADD6E20B8D0BDCA0E0430A83839451
        //签名结果加入请求提交参数组中
        sPara.put("sign", mysign);

        sPara.remove("key");
        return sPara;
    }

    /**
     * 建立请求，以表单HTML形式构造（默认）
     * @param sParaTemp 请求参数数组
     * @param strMethod 提交方式。两个值可选：post、get
     * @param strButtonName 确认按钮显示文字
     * @return 提交表单HTML文本
     */
    public static String buildRequest(Map<String, Object> sParaTemp, String strMethod, String strButtonName) {
        //待请求参数数组
        Map<String, Object> sPara = buildRequestPara(sParaTemp);
        List<String> keys = new ArrayList<String>(sPara.keySet());

        StringBuffer sbHtml = new StringBuffer();

        sbHtml.append("<form id=\"wxpaysubmit\" name=\"wxpaysubmit\" action=\"" + WXPAY_GATEWAY
                      + "\" method=\"" + strMethod
                      + "\">");

        for (int i = 0; i < keys.size(); i++) {
            String name = String.valueOf(keys.get(i));
            String value = String.valueOf(sPara.get(name));

            sbHtml.append("<input type=\"hidden\" name=\"" + name + "\" value=\"" + value + "\"/>");
        }

        //submit按钮控件请不要含有name属性
        sbHtml.append("<input type=\"submit\" value=\"" + strButtonName + "\" style=\"display:none;\"></form>");
        sbHtml.append("<script>document.forms['wxpaysubmit'].submit();</script>");

        return sbHtml.toString();
    }
    
}
