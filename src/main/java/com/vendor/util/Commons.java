/**
 * 
 */
package com.vendor.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ecarry.core.util.MathUtil;
import com.ecarry.core.web.core.ContextUtil;
import com.vendor.po.Category;
import com.vendor.po.User;
import com.vendor.service.IDictionaryService;

/**
 * 系统常量公共类
 * 
 * @author dranson on 2014年10月17日
 */
public abstract class Commons {

	public static final int ORG_HQ = 1;
	
	public static final int ORG_TYPE_THIRD =-2;
	
	/**
	 * 系统日志
	 */
	public static final int LOG_OPT_ADD = 1;
	public static final int LOG_OPT_UPDATE = 2;
	public static final int LOG_OPT_DELETE = 3;
	public static final int LOG_TYPE_SYN = 1;
	public static final int LOG_TYPE_BIZ = 2;
	/**
	 * 本地货币三字码
	 */
	public static String CURRENCY_LOCAL="CNY";
	/**
	 * 商品状态
	 */
	public static final int PRODUCT_STATE_STOCK = 2; //仓库
	public static final int PRODUCT_STATE_AUDIT = 3; // 审核中
	public static final int PRODUCT_STATE_TRASH = 9;  //删除 软删除
	/** 实物商品0 */
	public static final int PRODUCT_AND_TRUE=0;
	/** 虚拟商品1 */
	public static final int PRODUCT_AND_FALSE=1;
	/** 虚拟商品上架 0:已保存还未推送*/
	public static final int VIRTUAL_STATIC_0=0;
	/** 虚拟商品上架 1:已推送上架*/
	public static final int VIRTUAL_STATIC_1=1;
	/**
	 * 虚拟商品上架 2:已上架
	 */
	public static final int VIRTUAL_STATIC_2=2;
	/** 主题皮肤状态9:软删除 */
	public static final int THEMESKIN_STATE_TRASH = 9;
	/** 主题皮肤状态0:可用 */
	public static final int THEMESKIN_STATE_FEASIBLE = 0;
//	/** 主题性质(0:主题模板) */
//	public static final int THEMESKIN_PROPERTY_TYPE_0 = 0;
//	/** 主题性质(1:设备使用的主题) */
//	public static final int THEMESKIN_PROPERTY_TYPE_1 = 1;
	/** 主题是否是默认主题,0:是默认 */
	public static final int THEMESKIN_DEFAULT_THEME_0 = 0;
	/** 主题是否是默认主题,1:非默认 */
	public static final int THEMESKIN_DEFAULT_THEME_1 = 1;
	/** 主题 （0:横屏） */
	public static final int THEMESKIN_THEMETYPE_0 = 0;
	/** 主题 （1:竖屏） */
	public static final int THEMESKIN_THEMETYPE_1 = 1;
	/** 主题推送执行状态 0：未执行 */
	public static final int THEMESKIN_EXECUTING_STATE_0 = 0;
	/** 主题推送执行状态 1：执行中 */
	public static final int THEMESKIN_EXECUTING_STATE_1 = 1;
	/** 主题推送执行状态 2：已取消 */
	public static final int THEMESKIN_EXECUTING_STATE_2 = 2;
	/** 主题推送执行状态 3：执行成功 */
	public static final int THEMESKIN_EXECUTING_STATE_3 = 3;



	/**
	 * 活动状态
	 */
	public static final int PROMOTE_STATE_NEW = 1; // 待审核
	public static final int PROMOTE_STATE_FAIL = 2; // 审核失败
	public static final int PROMOTE_STATE_AUDIT = 3; // 已审核
	public static final int PROMOTE_STATE_APPLY = 4; // 报名中
	public static final int PROMOTE_STATE_APPLIED = 5; // 报名结束
	public static final int PROMOTE_STATE_SALE = 6; // 已上线
	public static final int PROMOTE_STATE_FINISH = 7; // 活动结束
	/**
	 * 订单状态
	 */
	public static final int ORDER_STATE_NEW = 1; // 待付款
	public static final int ORDER_STATE_CANCEL = 2; // 已取消
	public static final int ORDER_STATE_CLOSE = 3; // 已关闭
	public static final int ORDER_STATE_PAYING = 4; // 付款中
	public static final int ORDER_STATE_PAID = 5; // 待发货
	public static final int ORDER_STATE_SELF = 6; // 自提货
	public static final int ORDER_STATE_SEND = 7; // 已发货
	public static final int ORDER_STATE_FINISH = 8; // 已完成
	/**
	 * 优惠券类型
	 */
	public static final int COUPONE_TYPE_EXPRESS = 1; // 抵邮券
	public static final int COUPONE_TYPE_LIMIT = 2; // 满减券
	public static final int COUPONE_TYPE_CASH = 3; // 现金券
	/**
	 * 优惠券使用状态
	 */
	public static final int COUPONE_STATE_NEW = 0; // 未使用
	public static final int COUPONE_STATE_EXPIRE = 1; // 已过期
	public static final int COUPONE_STATE_USING = 2; // 使用中
	public static final int COUPONE_STATE_USED = 3; // 已使用

	/**
	 * 文件类型
	 */
	public static final int FILE_VENDER = 1;
	public static final int FILE_CATEGORY = 2;
	public static final int FILE_PRODUCT = 3;
	public static final int FILE_YOUPIN = 4;
	public static final int FILE_TEMPLATE = 5;
	public static final int FILE_DISCOVER = 6;
	public static final int FILE_ID_CARD = 7;
	public static final int FILE_BANNER = 8;
	public static final int FILE_DISCOVER_LOGO = 9;
	public static final int FILE_SPREAD = 10;
	public static final int FILE_PROMOTE = 11;
	public static final int FILE_ARTICEL = 12;
	public static final int FILE_QRCODE = 13;
	/** 虚拟商品二维码图片类型 */
	public static final int FILE_VIRTUAL_QRCODE=15;
	/** 主题皮肤预览图 */
	public static final int FILE_THEME=16;
	/** 主题皮肤文件类型 */
	public static final int FILE_THEME_TYPE=17;
	/** 监控设备上传文件类型 */
	public static final int FILE_MONITORING_TYPE=18;
	public static final int FILE_PRODUCT_DESC = 21;
	public static final int FILE_TEMPLATE_DESC = 22;
	public static final int FILE_DISCOVER_DESC = 23;
	public static final int FILE_ARTICEL_DESC = 24;
	public static final int FILE_INFO = 25;
	public static final int FILE_VENDER_BACKGROUD=14;
	public static final int FILE_EXCEL = 26;
	public static final int FILE_ADVERTISE = 27;
	public static final int FILE_ADVERTISE_TOP = 28;
	public static final int FILE_ADVERTISE_BG = 29;
	public static final int FILE_ADVERTISE_BOTTOM = 30;
	public static final int FILE_PRODUCT_DETAIL = 31;
	/** 抽奖首页图片类型 */
	public static final int FILE_LOTTERY_LOGO = 32;
	/** 抽奖详情页图片类型 */
	public static final int FILE_LOTTERY_DETAIL = 33;
	/** 抽奖图文描述图片类型 */
	public static final int FILE_LOTTERY_INFO = 34;


	/** 抽奖活动下线 */ 
	public static final int LOTTERY_ISOPEN_0 = 0;
	/** 抽奖活动上线 */ 
	public static final int LOTTERY_ISOPEN_1 = 1;
	/** 抽奖活动状态(0:未开始) */
	public static final String LOTTERY_STATE_0 = "0";
	/** 抽奖活动状态(1:已开始) */
	public static final String LOTTERY_STATE_1 = "1";
	/** 抽奖活动状态(2:已结束) */
	public static final String LOTTERY_STATE_2 = "2";
	/** 抽奖活动状态(5:预热中) */
	public static final String LOTTERY_STATE_5 = "5";
	/** 活动发布状态（0:未发布） */
	public static final Integer IS_PUBLISH_0 = 0;
	/** 活动发布状态（1:已发布） */
	public static final Integer IS_PUBLISH_1 = 1;
	/** 活动内容状态(5:已下线) */
	public static final String LOTTERY_PRODUCT_STATE_5 = "5";
	/** 抽奖活动内容 发布成功 */
	public static final String IS_SUCESSSTATE_0 = "0";
	/** 抽奖活动内容 发布失败 */
	public static final String IS_SUCESSSTATE_1 = "1";
	/** 抽奖活动内容 执行中*/
	public static final String IS_SUCESSSTATE_2 = "2";
	/** 抽奖活动内容 未发布 */
	public static final String IS_SUCESSSTATE_3 = "3";
	/** 抽奖活动内容  预热成功*/
	public static final String IS_SUCESSSTATE_4 = "4";
	/** 抽奖活动内容  预热失败 */
	public static final String IS_SUCESSSTATE_5 = "5";
	/** 抽奖活动内容 活动成功 */
	public static final String IS_SUCESSSTATE_6 = "6";
	/** 抽奖活动内容 活动失败*/
	public static final String IS_SUCESSSTATE_7 = "7";
	/** 抽奖活动内容 结束成功*/
	public static final String IS_SUCESSSTATE_8 = "8";
	/** 抽奖活动内容 结束失败*/
	public static final String IS_SUCESSSTATE_9 = "9";
	
	public static final String ADDRESS_SELF = "客户自提";
	
	/**
	 * 支付方式
	 */
	public static final String PAY_YS_ALIPAY = "alipay";
	public static final String PAY_YS_CARD = "masterCard";
	public static final String PAY_YS_WX = "wx";
	/**
	 * 支付状态
	 */
	public static final int PAY_STATE_FAILED = -1;
	public static final int PAY_STATE_NEW = 0;
	public static final int PAY_STATE_SUCCESS = 1;
	/**
	 * 第三方数据类型
	 */
	public static final int DATA_YS_ALIPAY = 1;
	public static final int DATA_YS_CARD = 2;
	public static final int DATA_SEND_EMAIL = 3;

	/**
	 * 发送短信类型
	 ***/
	public static final int MSG_REGISTER = 1;
	public static final int MSG_OTHER = 2;

	/**
	 * 第三方绑定类型
	 ***/
	public static String THIRD_PARTY_QQ = "qq";
	public static String THIRD_PARTY_WB = "wb";
	public static String THIRD_PARTY_WX = "wx";
	public static String THIRD_PARTY_FB = "fb";
	public static final int BIND = 1;
	public static final int UPDATE_BIND=2;

	/**
	 * 注册方式
	 ***/
	public static final String PHONE = "mobile";
	public static final String EMAIL = "email";
	
	public static final String TW = "台湾";
	public static final String HTTP = "http";

	public static final String IOS = "ios";
	public static final String ANDROID = "android";
	
	/**
	 * 设备状态
	 */
	public static final int NORMAL=0;  //正常
	public static final int OUT_OF_STOCK=1; //缺货
	public static final int FAULT=2; //故障
	
	/**
	 * 设备点位绑定状态(设备首次安装APP时须绑定点位设备)
	 */
	public static final int BIND_STATE_INIT = 0;//初始化
	public static final int BIND_STATE_SUCCESS = 1;//点位绑定成功
	
	/**
	 * 设备性质
	 */
	public static final int SELF_SUPPORT=1;  //自营
	public static final int JOIN=2; //加盟

	/**
	 * 交易类型
	 */
	public static final int TRADE_TYPE_WITHDRAW = 1; // 提现
	public static final int TRADE_TYPE_RECHARGE = 2; // 充值
	public static final int TRADE_TYPE_REFUND = 3; // 退款

	/**
	 * 通联交易类型
	 */
	public static final int TL_TRADE_TYPE_QRCODE = 0; // 扫码支付
	public static final int TL_TRADE_TYPE_JS = 1; // js支付
	public static final int TL_TRADE_TYPE_CARD = 3; // 微信刷卡支付
	public static final int TL_TRADE_TYPE_GATEWAY = 4; // 通联网关支付
	
	/**
	 * 通联支付类型
	 */
	public static final int TL_PAY_TYPE_QRCODE = 1; // 扫码支付
	public static final int TL_PAY_TYPE_GATEWAY = 2; // 网关支付

	/**
	 * 通联交易详情-补货类型
	 */
	public static final int REPLENISH_TYPE_PROXY = 1; // 代理商品补货
	public static final int REPLENISH_TYPE_SELF = 2; // 自营商品补货

	/**
	 * 交易状态
	 */
	public static final int TRADE_STATUS_INIT = 1; // 待处理
	public static final int TRADE_STATUS_SUCCESS = 2; // 交易成功
	public static final int TRADE_STATUS_FAIL = 3; // 交易失败

	/**
	 * 微信交易状态
	 */
	public static final int TL_TRADE_STATUS_INIT = 1; // 初始化
	public static final int TL_TRADE_STATUS_SUCCESS = 2; // 交易成功
	public static final int TL_TRADE_STATUS_FAIL = 3; // 交易失败

	/**
	 * 【代理商品】状态
	 */
	public static final int PROXY_PRODUCT_STATUS_INIT = -1; // 初始化
	public static final int PROXY_PRODUCT_STATUS_SUCCESS = 1; // 软删除（说明已补货到商品表中）
	
	/**
     * 手机号码-正则表达式
     */
    public static final String REGEX_MOBILE = "^(13[0-9]|15[012356789]|17[05678]|18[0-9]|14[57])[0-9]{8}$";

    /**
     * 固定电话
     */
    public static final String REGEX_TELEPHONE = "/^(([0\\+]\\d{2,3}-)?(0\\d{2,3})-)(\\d{7,8})(-(\\d{3,}))?$/";
    
    /**
     * 微信统一支付接口返回码
     */
    public static final String RETCODE_SUCCESS = "SUCCESS"; // 成功
	public static final String RETCODE_FAIL = "FAIL"; // 失败

	/**
	 * 微信支付接口-交易返回码
	 */
	public static final String WX_TRADE_CODE_SUCCESS = "0000"; // 交易成功/下单成功
	public static final String WX_TRADE_CODE_TIMEOUT = "3044"; // 交易超时
	public static final String WX_TRADE_CODE_BALANCE_NOT_ENOUGH = "3008"; // 余额不足
	public static final String WX_TRADE_CODE_FAIL = "3999"; // 交易失败
	
	/**
	 * 字符集
	 */
	public static final int INPUT_CHARSET_UTF8 = 1; // UTF-8
	public static final int INPUT_CHARSET_GBK = 2; // GBK
	public static final int INPUT_CHARSET_GB2312 = 3; // GB2312
	
	/**
	 * 网关支付接口版本
	 */
	public static final String GATEWAY_VERSION_1 = "v1.0";
	public static final String GATEWAY_VERSION_2 = "v2.0";
	
	/**
	 * 网关签名类型
	 */
	public static final int GATEWAY_SIGNTYPE_MD5 = 0; // 表示订单上送和交易结果通知都使用MD5进行签名
	public static final int GATEWAY_SIGNTYPE_CER = 1; // 表示商户用使用MD5算法验签上送订单，通联交易结果通知使用证书签名
	
	/**
	 * 【通联网关支付】支付方式
	 */
	public static final int TL_PAY_TYPE_DEFAULT = 0; // 代表未指定支付方式，即显示该商户开通的所有支付方式
	public static final int TL_PAY_TYPE_SELF = 1; // 个人网银支付
	public static final int TL_PAY_TYPE_ENTERPRISE = 4; // 企业网银支付 
	public static final int TL_PAY_TYPE_WAP = 10; // wap支付 
	public static final int TL_PAY_TYPE_CREDIT = 11; // 信用卡支付 
	public static final int TL_PAY_TYPE_QUICK = 12; // 快捷付 
	public static final int TL_PAY_TYPE_CERTIFICATION = 21; // 认证支付 
	public static final int TL_PAY_TYPE_OUT = 23; // 外卡支付 
	
	/**
	 * 【通联网关支付】语言环境
	 */
	public static final int TL_PAY_LANGUAGE_CN_SIMPLE = 1; // 中文简体
	public static final int TL_PAY_LANGUAGE_CN = 2; // 中文繁体
	public static final int TL_PAY_LANGUAGE_US = 3; // 英文
	
	/**
	 * 通联网关支付处理结果
	 */
	public static final String TL_PAY_RESULT_SUCCESS = "1"; // 支付成功
	
	/**
	 * 饮料机货柜号
	 */
	public static final Integer BEVERAGE_MACHINE_STORE_NO = 11;
	
	/**
	 * 弹簧机货柜号（出货用）
	 */
	public static final String CABINET_NO_SPRING_1 = "09";// 弹簧机1
	public static final String CABINET_NO_SPRING_2 = "08";// 弹簧机2
	public static final String CABINET_NO_SPRING_3 = "07";// 弹簧机3
	
	/**
	 * 设备异常状态
	 */
	public static final Integer EXCEPTION_STATUS_TODO = 0; // 待处理
	public static final Integer EXCEPTION_STATUS_DONE = 1; // 已处理
	public static final Integer EXCEPTION_STATUS_FINISH = 2; // 已完成
	
	/**
	 * 设备类型
	 */
	public static final int DEVICE_TYPE_DRINK = 1;// 智能饮料机（黑色定制）
	public static final int DEVICE_TYPE_DRINK_SMALL = 2;// 小型智能饮料机（黑色定制）
	public static final int DEVICE_TYPE_CENTER_CONTROL = 3;// 多媒体控制柜
	public static final int DEVICE_TYPE_SPRING = 4;// 综合商品机辅机(弹簧)
	public static final int DEVICE_TYPE_GRID64 = 5;// 64门商品柜---格子机
	public static final int DEVICE_TYPE_GRID40 = 6;// 40门商品柜---格子机
	public static final int DEVICE_TYPE_GRID60 = 7;// 60门商品柜---格子机
	public static final int DEVICE_TYPE_CATERPILLAR = 8;// 综合商品机辅机(履带)
	public static final int DEVICE_TYPE_INTELLIGENT_PRODUCT = 9;// 智能商品机
	
	/**
	 * 设备型号
	 */
	public static final String DEVICE_MODEL_DRINK = "CVM-PC21PC42";// 智能饮料机（黑色定制）
	public static final String DEVICE_MODEL_DRINK_SMALL = "CVM-PC12PC42";// 小型智能饮料机（黑色定制）
	public static final String DEVICE_MODEL_CENTER_CONTROL = "CVM-KZGPC23.6";// 多媒体控制柜
	public static final String DEVICE_MODEL_SPRING = "CVM-FD48WXT";// 综合商品机辅机(弹簧)
	public static final String DEVICE_MODEL_GRID64 = "CVM-SPG64";// 64门商品柜---格子机
	public static final String DEVICE_MODEL_GRID40 = "CVM-SPG40";// 40门商品柜---格子机
	public static final String DEVICE_MODEL_GRID60 = "CVM-SPG60";// 60门商品柜---格子机
	public static final String DEVICE_MODEL_GRID = "CVM-SPG";// 格子机; 不同于以上6种，业务需要所添加
	public static final String DEVICE_MODEL_CATERPILLAR = "CVM-FD56BWXT(C/M)";// 综合商品机辅机(履带)
	public static final String DEVICE_MODEL_SPRING_CATERPILLAR = "CVM-WXT";// (弹簧)(履带)，业务需要所添加
	public static final String DEVICE_MODEL_INTELLIGENT_PRODUCT = "CVM-FD50PC43H";// 智能商品机
	
	/**
	 * 设备类型描述
	 */
	public static final String DEVICE_TYPE_STR_DRINK = "智能饮料机";// 智能饮料机（黑色定制）
	public static final String DEVICE_TYPE_STR_DRINK_SMALL = "小型智能饮料机";// 小型智能饮料机（黑色定制）
	public static final String DEVICE_TYPE_STR_CENTER_CONTROL = "多媒体控制柜";// 多媒体控制柜
	public static final String DEVICE_TYPE_STR_SPRING = "综合商品机辅机(弹簧)";// 综合商品机辅机(弹簧)
	public static final String DEVICE_TYPE_STR_GRID64 = "64门商品柜";// 64门商品柜---格子机
	public static final String DEVICE_TYPE_STR_GRID40 = "40门商品柜";// 40门商品柜---格子机
	public static final String DEVICE_TYPE_STR_GRID60 = "60门商品柜";// 60门商品柜---格子机
	public static final String DEVICE_TYPE_STR_CATERPILLAR = "综合商品机辅机(履带)";// 综合商品机辅机(履带)
	public static final String DEVICE_TYPE_STR_INTELLIGENT_PRODUCT = "智能商品机";// 智能商品机
	
	/**
	 * 通联提现(代付)成功返回码
	 */
	public static final String WITHDRAW_CODE_SUCCESS = "0000";// 成功

	/**
	 * 通联提现(代付)失败返回码
	 */
	public static final String WITHDRAW_CODE_FAIL = "1000149999";// 失败

	/**
	 * 提现code
	 */
	public static final String TRX_CODE = "100014";
	
	/**
	 * 激活码状态
	 */
	public static final Integer ACTIVE_CODE_STATE_INIT = 0;// 未使用
	public static final Integer ACTIVE_CODE_STATE_USED = 1;// 已使用
	public static final Integer ACTIVE_CODE_STATE_FAIL = 2;// 激活失败

	/**
	 * 用户状态
	 */
	public static final Integer USER_STATE_DISABLE = 0;// 禁用
	public static final Integer USER_STATE_ENABLE = 1;// 启用
	public static final Boolean USER_ENABLE_FALSE = false;// 禁用
	public static final Boolean USER_ENABLE_TRUE = true;// 启用
	
	/**
	 * 机构状态
	 */
	public static final Integer ORG_STATE_DISABLE = 1;// 禁用
	public static final Integer ORG_STATE_ENABLE = 0;// 启用

	/**
	 * 设备绑定标识   0：解绑   1：绑定
	 */
	public static final Integer DEVICE_BIND_TRUE = 1;// 绑定
	public static final Integer DEVICE_BIND_FALSE = 0;// 解绑

	/**
	 * 商品类型    1：自有  2：平台供货
	 */
	public static final Integer PROD_TYPE_SELF = 1;// 自有
	public static final Integer PROD_TYPE_PLATFORM = 2;// 平台供货
	
	/**
	 * 广告状态
	 */
	public static final Integer ADV_STATUS_INIT = 0;// 未上线
	public static final Integer ADV_STATUS_ING = 1;// 进行中
	public static final Integer ADV_STATUS_FINISH = 2;// 已下线
	
	/**
	 * 支付类型
	 */
	public static final Integer PAY_TYPE_ALI = 1; // 支付宝
	public static final Integer PAY_TYPE_WX = 6; // 微信
	
	/**
	 * 支付类型
	 */
	public static final String PAY_TYPE_ALIPAY = "alipay";
	public static final String PAY_TYPE_WECHAT = "wechat";
	
	/**
	 * 退款状态
	 */
	public static final int REFUND_STATE_FAILED = -1; //退款失败
	public static final int REFUND_STATE_NEW = 0; //初始化
	public static final int REFUND_STATE_SUCCESS = 1; //退款成功
	public static final int REFUND_STATE_ING = 2; //退款中
	
	/**
	 * 通知广告类型
	 */
	public static final Integer NOTIFY_ADV_ADD = 1;// 新增
	public static final Integer NOTIFY_ADV_UPDATE = 2;// 修改
	public static final Integer NOTIFY_ADV_DELETE = -1;// 删除

	/**
	 * 广告通知flag
	 */
	public static final String NOTIFY_ADV = "advertisement";
	
	/**
	 * 广告位置  1：锁屏上部  2：锁屏下部  3：锁屏背景
	 */
	public static final Integer ADV_POSITION_TOP = 1;
	public static final Integer ADV_POSITION_BOTTOM = 2;
	public static final Integer ADV_POSITION_BG = 3;

	/**
	 * 广告类型  1：默认广告  2：普通广告
	 */
	public static final Integer ADV_TYPE_DEFAULT = 1;
	public static final Integer ADV_TYPE_COMMON = 2;
	
	/**
	 * 订单支付通知flag
	 */
	public static final String NOTIFY_ORDER_PAY = "orderPay";

	/**
	 * 扫码通知flag
	 */
	public static final String NOTIFY_SCAN_PAY = "scanPay";
	
	/**
	 * 商品状态变更通知flag
	 */
	public static final String NOTIFY_CHANGE_PRODUCT_STATE = "changeProductState_SD";
	/**
	 * 商品上下线变更通知flag
	 */
	public static final String NOTIFY_PRODUCT_OFF_OR_ONLINE = "productOffOrOnLine_SD";
	
	/**
	 * 打折活动开关变更通知flag
	 */
	public static final String NOTIFY_DISCOUNT_ISOPEN = "discountActiveA_SD";
	
	/** 短抽奖活动 */ 
	public static final String NOTIFY_LOTTERY_ISOPEN = "lotteryFlag_SD";
	/** 长抽奖活动 */ 
	public static final String NOTIFY_LOTTERY_LONG_ISOPEN = "longlotteryFlag_SD";
	/** 长期活动状态推送 */
	public static final String NOTIFY_LOTTERY_LONG_STATE_ISOPEN = "longlotteryStateFlag_SD";
	/** 短期活动状态推送 */
	public static final String NOTIFY_LOTTERY_STATE_ISOPEN = "lotteryStateFlag_SD";
	/**
	 * 清空商品通知flag
	 */
	public static final String NOTIFY_CLEAR_PRODUCTS = "clearProducts";

	/** 虚拟商品推送上架 */
	public static final String NOTIFY_VIRTUAL_PRODUCT = "Push_VirtualGoods";

	/** 主题皮肤 */
	public static final String NOTIFY_THEME_DEVICE = "Push_ThemeDevice";


	/**
	 * 设备状态  1：在线  2：离线   3：待补货
	 */
	public static final Integer DEVICE_STATUS_ONLINE = 1;//1：在线
	public static final Integer DEVICE_STATUS_OFFLINE = 2;//2：离线
	public static final Integer DEVICE_STATUS_STOCKOUT = 3;//3：待补货
	
	/** 商品类别-99:抽奖商品类型 */
	public static final Integer PRODUCT_CATEGORY_TYPE = -99;
	/** 商品库存 */
	public static final Integer PRODUCT_STOCK_NUM = 200000;
	/** 商品周长 */
	public static final Integer PRODUCT_PERIMETER = 200;
	/** 品牌或原产地 */
	public static final String PRODUCT_BRAND_OR_ORIGIN = "345";
	

	/**
	 * 是否可售  0：不可售  1：可售
	 */
	public static final Integer SELLABLE_FALSE = 0;//0：不可售
	public static final Integer SELLABLE_TRUE = 1;//1：可售

	/**
	 * 商品状态  1：库存不足
	 */
	public static final Integer PRODUCT_STATUS_STOCKOUT = 1;

	/**
	 * 设备厂商编码
	 */
	public static final String FACTORY_CODE_YC = "YC"; // 易触
	
	/**
	 * 是否换货  0：否  1：是
	 */
	public static final Integer IS_REPLENISH_FALSE = 0;//0：否
	public static final Integer IS_REPLENISH_TRUE = 1;//1：是

	/**
	 * 时间参数  0：今天  1：明天
	 */
	public static final Integer TIME_TODAY = 0;//0：今天
	public static final Integer TIME_TOMORROW = 1;//1：明天
	
	/**
	 * 查询店铺类型  1：待补货店铺  3：全部
	 */
	public static final Integer FIND_STORE_TYPE_REPLENISH = 1;//1：待补货店铺
	public static final Integer FIND_STORE_TYPE_ALL = 3;//3：全部

	/**
	 * 订单类型  0：普通订单  1：限时打折  2：抽奖活动
	 */
	public static final Integer ORDER_TYPE_COMMON = 0;//0：普通订单
	public static final Integer ORDER_TYPE_DISCOUNT = 1;//1：限时打折
	public static final Integer ORDER_TYPE_LOTTERY = 2;//2：抽奖活动
	
	/**
	 * 打折类型  1:店铺,2:商品
	 */
	public static final Integer DISCOUNT_TYPE_STORE = 1;//1:店铺
	public static final Integer DISCOUNT_TYPE_PRODUCT = 2;//2:商品

	/**
	 * 店铺状态
	 */
	public static final int POINT_PLACE_STATE_DEFAULT = 1; // 默认
	public static final int POINT_PLACE_STATE_DELETE = 9; // 删除 软删除

	/**
	 * 组织是否关联   1： 已关联   2：未关联
	 */
	public static final int ORG_IS_RELATE_TRUE = 1; // 已关联
	public static final int ORG_IS_RELATE_FALSE = 2; // 未关联

	/**
	 * 父节点关联申请   1:提出了申请、2：申请被拒绝、3：申请同意、0：默认值
	 */
	public static final int ORG_APPLY_RELATE_DEFAULT = 0; // 默认
	public static final int ORG_APPLY_RELATE_ING = 1; // 提出了申请
	public static final int ORG_APPLY_RELATE_REFUSE = 2; // 申请被拒绝
	public static final int ORG_APPLY_RELATE_AGREE = 3; // 申请已同意
	
	/**
	 * 组织类型  1：管理者 2：经营者
	 */
	public static final int ORG_TYPE_MANAGER= 1; // 管理者
	public static final int ORG_TYPE_OPERATOR = 2; // 经营者
	
	/**
	 * 合作方式 1：合作 2：联营 3：加盟 4：自营
	 */
	public static final int ORG_MODE_COOPERATION = 1; // 合作
	public static final int ORG_MODE_JOINT_OPERATION = 2; // 联营
	public static final int ORG_MODE_JOINT = 3; // 加盟
	public static final int ORG_MODE_SELF_SUPPORT = 4; // 自营
	
	/**
	 * 组织类型名称
	 */
	public static final String ORG_TYPE_NAME_MANAGER= "管理者"; // 管理者
	public static final String ORG_TYPE_NAME_OPERATOR = "经营者"; // 经营者
	
	/**
	 * 建立/取消关联（管理）信息   1：建立管理   2：取消管理
	 */
	public static final int ORG_APPLY_RELATE_APPLY = 1; // 申请建立管理
	public static final int ORG_APPLY_RELATE_CANCEL = 2; // 取消管理

	/**
	 * 货道是否可见 0：不可见 1：可见
	 */
	public static final int VISIABLE_FALSE = 0; // 不可见
	public static final int VISIABLE_TRUE = 1; // 可见

	/**
	 * 是否包含管理组织（下级所有有管理关系的组织）   1：是    0：否
	 */
	public static final int CONTAIN_SUB_ORG_TRUE = 1; // 包含
	public static final int CONTAIN_SUB_ORG_FALSE = 0; // 不包含
	
	/**
	 * 判断是否为系统用户
	 * @return 是或否
	 */
	public static boolean isSystemUser() {
		return ContextUtil.getUser(User.class).getCompanyId().intValue() == ORG_HQ;
	}

	/**
	 * 判断是否为系统内置用户名
	 * @param name 需要判断的用户名
	 * @return 是或否
	 */
	public static boolean isAdminName(String name) {
		Pattern p = Pattern.compile("admin\\d*");
		Matcher m = p.matcher(name);
		return m.matches();
	}

	/**
	 * 计算税费
	 * @param dictionaryService
	 * @param province 岛内岛外
	 * @param amount 购买金额
	 * @param sku SKU
	 * @return 税费
	 */
	public static double calcTax(String province, double amount, String sku) {
		IDictionaryService dictionaryService = ContextUtil.getBeanByName(IDictionaryService.class, "dictionaryService");
		if ("TWN".equals(province))
			return 0;
		String[] codes = sku.split("-");
		Category category = dictionaryService.findCategories(codes[0], codes[1], codes[2]);
		if (category != null && category.getTaxRate() != null)
			return MathUtil.mul(amount, MathUtil.div(category.getTaxRate(), 100));
		return 0d;
	}

	/**
	 * 顺丰全球顺运费计算
	 * @param weight 重量
	 * @param province 岛内岛外
	 * @param address 是否自提
	 * @return 运费
	 */
	public static double calcFeeShip(double weight, String province, String address) {
		if (weight == 0 || Commons.ADDRESS_SELF.equals(address))
			return 0d;
		double shouZhong = 134; // 首重价格
		double xuzhong = 91; // 续重价格
		double weightD = weight / 1000;
		if ("TWN".equals(province)) { // 单位是以g为单位,长+宽+高已毫米为单位
			if (weightD <= 1)
				return 80d;
			if (weightD > 1 && weightD <= 20)
				return 100d;
			if (weight > 20)
				return 999999.99d;
			return 0d;
		} else { // 如果重量小于1kg
			if (0 < weightD && weightD <= 1) {
				return shouZhong;
			} else {
				if (weightD % 1 == 0) {
					return (weightD - 1) * xuzhong + shouZhong;
				} else {
					return (Math.floor(weightD - 1) + 1) * xuzhong + shouZhong;
				}
			}
		}
	}

	/**
	 * 获取网页图片路径
	 * @param html	网页数据
	 * @return	图片路径集
	 */
	public static List<String> getImagePaths(String html) {
		IDictionaryService dictionaryService = ContextUtil.getBeanByName(IDictionaryService.class, "dictionaryService");
		String rootPath = dictionaryService.getFileServer().replace("?", "\\?");
		return findRegex(html, "<\\s?+img\\s?+src\\s?+=\\s?+\"" + rootPath + "(.+?)\"");
	}
	/**
	 * 根据正则表达式便利目标数据
	 * @param str
	 * @param regex	正则表达式
	 * @return	所有匹配的数据
	 */
	public static List<String> findRegex(String str, String regex) {
		List<String> list = new ArrayList<String>();
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(str);
		boolean found = m.find();
		while (found) {
			list.add(m.group(1));
			found = m.find();
		}
		return list;
	}
}
