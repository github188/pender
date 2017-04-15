package com.vendor.util.msg;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.vendor.util.Commons;
import com.vendor.util.RandomUtil;

/**
 * 短信发送工具类
 * 
 **/
public class SendPhoneMsg {
	private static final Logger log = LoggerFactory.getLogger(SendPhoneMsg.class);
	private static SendPhoneMsg sendPhoneMsg = null;
	private ConcurrentHashMap<String, VerificationInfo> registerMap = null;
	private ConcurrentHashMap<String, VerificationInfo> otherMap = null;
	private String f_sendMsgUrl = null;
	private String f_sendMsgAccount = null ;
	private String f_sendMsgPwd = null;
	private String f_sendMsgContent = null;
	private String f_sendMsgOther = null;


	public static SendPhoneMsg getInstall() {
		if (null == sendPhoneMsg) {
			synchronized (SendPhoneMsg.class) {
				if (sendPhoneMsg == null) {
					sendPhoneMsg = new SendPhoneMsg();
				}
			}
		}
		return sendPhoneMsg;
	}

	private SendPhoneMsg() {
		init();
	}
	
	private void init() {
		registerMap = new ConcurrentHashMap<>();
		otherMap = new ConcurrentHashMap<>();
		f_sendMsgUrl = PropertUtil.getInstall().prop.getProperty("msg.sendMsgUrl");
		f_sendMsgAccount = PropertUtil.getInstall().prop.getProperty("msg.sendMsgAccount");
		f_sendMsgPwd = PropertUtil.getInstall().prop.getProperty("msg.sendMsgPwd");
		f_sendMsgContent = PropertUtil.getInstall().prop.getProperty("msg.sendMsgContent");
		f_sendMsgOther=PropertUtil.getInstall().prop.getProperty("msg.sendMsgActivityContent");
	}

	/***
	 * 发送短信
	 * @param phone 手机号
	 * @param msg 消息内容
	 * @param type 短信类型
	 */
	public boolean sendMsg(String phone, String msg, int type) {
		if (StringUtils.isEmpty(phone)||StringUtils.isEmpty(f_sendMsgUrl)) {
			return false;
		}
		String code = getCode(phone, type);
		try {
			if (type == Commons.MSG_REGISTER) {
				msg = StringUtils.isEmpty(msg) ? f_sendMsgContent : msg;
				msg = f_sendMsgContent.replace("{yzm}", code);
			} else if (type == Commons.MSG_OTHER) {
				msg = StringUtils.isEmpty(msg) ? f_sendMsgOther : msg;
			}
			StringBuffer sb = new StringBuffer();
			sb.append("userid=").append(f_sendMsgAccount).append("&password=").append(f_sendMsgPwd)
					.append("&destnumbers=").append(phone).append("&msg=").append(msg);
			String result = sendMsg(sb.toString(), f_sendMsgUrl);
			if(StringUtils.isEmpty(result)){
				return false;
			}
			if("0".equals(getCenterStr(result, "return"))){
				return true;
			}
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			return false;
		}
		return false;
	}
	


	/**
	 * 获取验证码
	 * @param phone 手机号
	 * @param type 短信发送类型 注册就送，活动发送
	 ***/
	private String getCode(String phone, int type) {
		String random = String.valueOf(RandomUtil.buildRandom(4));
		VerificationInfo info = new VerificationInfo();
		info.setCode(random);
		info.setTime(System.currentTimeMillis());
		if (type == Commons.MSG_REGISTER) {
			getRegisterCache().put(phone, info);
		} else if (type == Commons.MSG_OTHER) {
			getOtherCache().put(phone, info);
		}
		return random;
	}
	
	 /**
     * 根据发送的地址和信息发送短信信息
     *
     * @param postData 发送的信息,包含用户名密码,短信内容,企业id等
     * @param postUrl  发送的短信接口地址
     *
     * @return 返回的响应的字符串信息
     */
    private String sendMsg(String postData,String postUrl){
        try{
            //发送POST请求
            URL url=new URL(postUrl);
            HttpURLConnection conn=(HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            conn.setRequestProperty("Connection","Keep-Alive");
            conn.setUseCaches(false);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Length",String.valueOf(postData.getBytes().length));
            OutputStreamWriter out=new OutputStreamWriter(conn.getOutputStream(),"UTF-8");
            out.write(postData);
            out.flush();
            out.close();
            //获取响应状态
            if(conn.getResponseCode()!=HttpURLConnection.HTTP_OK){
                log.info("短信连接失败");
                return "";
            }
            //获取响应内容体
            String line, result="";
            BufferedReader in=new BufferedReader(new InputStreamReader(conn.getInputStream(),"utf-8"));
            while((line=in.readLine())!=null){
                result+=line+"\n";
            }
            in.close();
            return result;
        }catch(Exception e){
            log.error("短信发送异常,错误信息{}"+e,e.getMessage());
        }
        return "";
    }
	
    private static String getCenterStr(String xml,String xmlName){
        String str="";
        //获取以>{ 开头  }</ 结尾的字符串
        Pattern mpattern=Pattern.compile(xmlName+"=\"(\\d+)\"");
        Matcher mmatcher=mpattern.matcher(xml);
        while(mmatcher.find()){
            str+=(mmatcher.group(1));
        }
        str+="";
        return str;
    }
    
    /**
     * 手机号验证
     *
     * @param  str
     * @return 验证通过返回true
     */
    public static boolean isMobile(String str) {
        if(StringUtils.isEmpty(str)) return false;
        Pattern p = Pattern.compile("^[1][3,4,5,8][0-9]{8}$"); // 验证手机号
        Matcher m = p.matcher(str);
        return m.matches();
    }

	public ConcurrentHashMap<String, VerificationInfo> getRegisterCache() {
		if (this.registerMap == null) {
			return new ConcurrentHashMap<>();
		}
		return this.registerMap;
	}

	public ConcurrentHashMap<String, VerificationInfo> getOtherCache() {
		if (this.otherMap == null) {
			return new ConcurrentHashMap<>();
		}
		return this.otherMap;
	}
	
	public void remove(String key){
		getRegisterCache().remove(key);
	}
}
