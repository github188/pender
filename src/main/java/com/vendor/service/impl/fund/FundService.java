package com.vendor.service.impl.fund;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.ecarry.core.dao.IGenericDao;
import com.ecarry.core.dao.SQLUtils;
import com.ecarry.core.exception.BusinessException;
import com.ecarry.core.util.MathUtil;
import com.ecarry.core.util.ZHConverter;
import com.ecarry.core.web.core.ContextUtil;
import com.ecarry.core.web.core.Page;
import com.vendor.po.Card;
import com.vendor.po.TradeFlow;
import com.vendor.po.User;
import com.vendor.service.IFundService;
import com.vendor.service.impl.TranxServiceImpl;
import com.vendor.thirdparty.tl.TranxCon;
import com.vendor.util.CommonUtil;
import com.vendor.util.Commons;
import com.vendor.util.DateUtil;
import com.vendor.util.XmlTools;
import com.vendor.util.msg.SendPhoneMsg;
import com.vendor.util.msg.VerificationInfo;
import com.vendor.vo.app.RestDetail;

@Service("fundService")
public class FundService implements IFundService{

	private static final Logger logger = Logger.getLogger(FundService.class);
	
	/**
	 * 通联单笔提现url
	 */
	@Value("${tl.tran.url}")
	private String tranURL;
	
	/**
	 * 通联商户号
	 */
	@Value("${tl.merchantid}")
	private String merchantId;

	/**
	 * 通联用户登录密码
	 */
	@Value("${tl.password}")
	private String tlPassword;
	
	@Autowired
	private IGenericDao genericDao;
	
	/**
	 * 取得当前登录用户的销售额
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
	
	/**
	 * 取得当前用户的可提现金额
	 * @param user 当前用户
	 * @return 当前用户的可提现金额
	 */
	private double saveWithdrawalAmount(User user) {
		// 从交易流水表中查询当前用户最近时间的流水，若没有，则新增一条流水记录，交易类型为充值；
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer("SELECT ");
		String cols = SQLUtils.getColumnsSQL(TradeFlow.class, "C");
		buf.append(cols);
		buf.append(" FROM T_TRADE_FLOW C WHERE C.ORG_ID = ? AND C.TRADE_STATUS = ? ORDER BY C.TRADE_TIME DESC ");
		args.add(user.getOrgId());
		args.add(Commons.TRADE_STATUS_SUCCESS);
		TradeFlow tradeFlow = genericDao.findT(TradeFlow.class, buf.toString(), args.toArray());
		if (null == tradeFlow) {
			tradeFlow = createTradeFlow(user);
			if (null != tradeFlow && tradeFlow.getTradeAmount() > 0)
				genericDao.save(tradeFlow);
		}
		return MathUtil.round(tradeFlow.getBalance(), 2);
	}
	
	/**
	 * 构建初始化充值交易流水信息
	 * @param user 当前用户
	 * @return 初始化交易流水信息
	 */
	private TradeFlow createTradeFlow(User user) {
		Timestamp curTime = new Timestamp(System.currentTimeMillis());
		// 取得总销售额(所有商品总销售额)
		double salesTotalAmount = getSalesAmount(user, null);
		
		TradeFlow tradeFlow = new TradeFlow();
		tradeFlow.setOrgId(user.getOrgId());
		tradeFlow.setUserId(user.getId());
		tradeFlow.setTradeType(Commons.TRADE_TYPE_RECHARGE);
		tradeFlow.setTradeAmount(MathUtil.round(salesTotalAmount, 2));
		tradeFlow.setTradeTime(curTime);
		tradeFlow.setBalance(MathUtil.round(salesTotalAmount, 2));
		tradeFlow.setTradeStatus(Commons.TRADE_STATUS_SUCCESS);
		return tradeFlow;
	}
	
	/**
	 * 取得当前用户所属机构的银行卡信息
	 * @param user 当前用户
	 * @return 当前用户所属机构的银行卡信息
	 */
	private Card getCard(User user) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer("SELECT ");
		String cols = SQLUtils.getColumnsSQL(Card.class, "C");
		buf.append(cols);
		buf.append(" FROM T_CARD C WHERE C.ORG_ID = ? ORDER BY C.CREATE_TIME DESC ");
		args.add(user.getOrgId());
		return genericDao.findT(Card.class, buf.toString(), args.toArray());
	}

	/**
	 * 保存银行卡信息
	 */
	@Override
	public void saveCard(Card card, String checkCode) {
		// 取得当前登录用户
		User user = ContextUtil.getUser(User.class);
		
		if (null == card) {
			// 非法请求
    		throw new BusinessException("非法请求");
		}
        if (null == card.getId()) {
        	// 请求合法性校验
        	StringBuilder errorMsg = new StringBuilder();
        	if (!isLegitimateRequest(card, errorMsg, checkCode)) {
        		// 非法请求
        		throw new BusinessException(errorMsg.toString());
        	}
        	// 验证通过
        	// 绑定银行卡
        	Timestamp curTime = new Timestamp(System.currentTimeMillis());
        	card.setOrgId(user.getOrgId());
        	card.setCreateUser(user.getId());
        	card.setCreateTime(curTime);
        	genericDao.save(card);
        } else {
        	// 解绑银行卡
        	Card cardDB = getCard(user);
        	if (null == cardDB) {
        		throw new BusinessException("找不到对应的银行卡信息");
        	}
        	// 短信验证码校验
    		VerificationInfo info = SendPhoneMsg.getInstall().getRegisterCache().get(cardDB.getMobileNo());
    		if (null == info) {
    			throw new BusinessException("验证码已过期，请重新获取");
    		}
    		if (!StringUtils.isEmpty(checkCode) && !checkCode.equals(info.getCode())) {
    			throw new BusinessException("验证码错误，请重新输入");
    		}
        	
        	List<Object> args = new ArrayList<Object>();
			StringBuffer buf = new StringBuffer("");
			buf.append("DELETE FROM T_CARD WHERE ORG_ID = ?");
			args.add(user.getOrgId());
        	genericDao.execute(buf.toString(), args.toArray());
        }
	}
	
	/**
     * 是否是合法的请求
     * 合法：true
     * 非法：false
     */
    private boolean isLegitimateRequest(Card card, StringBuilder errorMsg, String checkCode) {
        if (null == card) {
            errorMsg.append("非法请求");
            return false;
        }
        Integer cardType = card.getCardType();
        String cardOwner = card.getCardOwner();
        String cardNo = card.getCardNo();
        String mobileNo = card.getMobileNo();
        // 银行卡类型
        if (null == cardType) {
            errorMsg.append("请选择开户行");
            return false;
        }
        // 持卡人
        if (StringUtils.isEmpty(cardOwner)) {
            errorMsg.append("请输入持卡人");
            return false;
        }
        // 银行卡号
        if (StringUtils.isEmpty(cardNo)) {
            errorMsg.append("请输入银行卡号");
            return false;
        }
        // 手机号码
        if (StringUtils.isEmpty(mobileNo)) {
            errorMsg.append("请输入手机号码");
            return false;
        }
        mobileNo = mobileNo.split(",")[1];// 两个相同name:mobileNo
        if (!mobileNo.matches(Commons.REGEX_MOBILE)) {
        	errorMsg.append("请输入合法的手机号码");
            return false;
        }
        card.setMobileNo(mobileNo);
        // 短信验证码校验
		VerificationInfo info = SendPhoneMsg.getInstall().getRegisterCache().get(mobileNo);
		if (null == info) {
			errorMsg.append("验证码已过期，请重新获取");
            return false;
		}
		if (!StringUtils.isEmpty(checkCode) && !checkCode.equals(info.getCode())) {
			errorMsg.append("验证码错误，请重新输入");
            return false;
		}
        // 业务校验
        // 取得当前登录用户
 		User user = ContextUtil.getUser(User.class);
 		Card cardDB = getCard(user);
 		if (null != cardDB) {
 			errorMsg.append("该用户已经绑定过了银行卡！");
            return false;
 		}
        return true;
    }

    /**
     * 保存交易流水信息
     */
	@Override
	public void saveTradeFlow(TradeFlow tradeFlow, String checkCode) {
		
		//交易状态初始化
		tradeFlow.setTradeStatus(Commons.TRADE_STATUS_INIT);
		
		// 取得当前登录用户
 		User user = ContextUtil.getUser(User.class);
 		
		// 校验验证码
 		Card cardDB = getCard(user);
    	if (null == cardDB) {
    		throw new BusinessException("找不到对应的银行卡信息");
    	}
    	// 短信验证码校验
		VerificationInfo info = SendPhoneMsg.getInstall().getRegisterCache().get(cardDB.getMobileNo());
		if (null == info) {
			throw new BusinessException("验证码已过期，请重新获取");
		}
		if (!StringUtils.isEmpty(checkCode) && !checkCode.equals(info.getCode())) {
			throw new BusinessException("验证码错误，请重新输入");
		}
		
		// 用户录入的提现金额
		double tradeAmount = tradeFlow.getTradeAmount();
		// 实扣金额（交易金额）
		double bdTradeAmountFinal = MathUtil.round(MathUtil.add(tradeAmount, 1.5), 2);
		
		// 取得可提现金额
		double withdrawalAmount = saveWithdrawalAmount(user);
		if (new BigDecimal(bdTradeAmountFinal).compareTo(new BigDecimal(withdrawalAmount)) == 1) {
			throw new BusinessException("超出最大可提现金额！");
		}
		
		try {
			saveTradeFlow(user, new BigDecimal(bdTradeAmountFinal), withdrawalAmount);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new BusinessException("提现失败！");
		}
		
	}

	private void saveTradeFlow(User user, BigDecimal bdTradeAmountFinal, double withdrawalAmount) {
		// 交易金额
		double tradeAmountFinal = bdTradeAmountFinal.doubleValue();
		// 可用余额
		double balance = new BigDecimal(withdrawalAmount).subtract(bdTradeAmountFinal).doubleValue();
		
		TradeFlow flow = new TradeFlow();
		flow.setOrgId(user.getOrgId());
		flow.setUserId(user.getId());
		flow.setTradeType(Commons.TRADE_TYPE_WITHDRAW);
		flow.setTradeAmount(tradeAmountFinal);
		Timestamp curTime = new Timestamp(System.currentTimeMillis());
		flow.setTradeTime(curTime);
		flow.setBalance(MathUtil.round(balance, 2));
		flow.setTradeStatus(Commons.TRADE_STATUS_INIT);
		genericDao.save(flow);
	}
	
	private void updateTradeFlow(TradeFlow flow, int trade_status) {
		flow.setTradeTime(new Timestamp(System.currentTimeMillis()));
		flow.setTradeStatus(trade_status);
		genericDao.update(flow);
	}
	
	/**
	 * 通联单笔付款接口
	 * @return
	 * @throws Exception 
	 */
	public String singleTranx(Card card, double tradeAmount) throws Exception {
		// String testTranURL="https://113.108.182.3/aipg/ProcessServlet"; //通联测试环境，外网（商户测试使用）
		// String tranURL = "http://tlt.allinpay.com/aipg/ProcessServlet";// 通联生产环境（商户上线时使用）
		boolean isfront = false;// 是否发送至前置机（由前置机进行签名）如不特别说明，商户技术不要设置为true
		String trx_code, busicode;// 100001批量代收 100002批量代付 100011单笔实时代收 100014单笔实时代付
		TranxServiceImpl tranxService = new TranxServiceImpl();

		/**
		 * 测试的时候不用修改以下业务代码，但上生产环境的时候，必须使用业务人员提供的业务代码，否则返回“未开通业务类型”
		 * 另外，特别说明：如果生产环境对接的时候返回”未开通产品“那么说明该商户开通的接口与目前测试的接口不一样，需要找业务确认
		 * 代收是批量代收接口的简称，代付 是批量代付接口的简称，
		 * 对接报文中，info下面的用户名一般是：商户号+04，比如商户号为：200604000000445，那么对接用户一般为：20060400000044504
		 */
		trx_code = Commons.TRX_CODE;// 单笔提现
		if ("100011".equals(trx_code) || "100001".equals(trx_code))// 收款的时候，填写收款的业务代码
			busicode = "19900";
		else
			busicode = "09900";
		//设置安全提供者,注意，这一步尤为重要
		BouncyCastleProvider provider = new BouncyCastleProvider();
		XmlTools.initProvider(provider);
		
		TranxCon tranxCon = new TranxCon();
		tranxCon.setAcctName(card.getCardOwner());
		tranxCon.setAcctNo(card.getCardNo());
		tranxCon.setAmount((int)MathUtil.mul(tradeAmount, 100) + "");//单位：分
		tranxCon.setSum((int)MathUtil.mul(tradeAmount, 100) + "");
		tranxCon.setMerchantId(merchantId);
		tranxCon.setPassword(tlPassword);
		tranxCon.setPfxPath(getSrcFilePath("config/tl/bmt", "20058400001320804.p12"));
		tranxCon.setPfxPassword("111111");
		tranxCon.setTel(card.getMobileNo());
		tranxCon.setTltcerPath(getSrcFilePath("config/tl", "allinpay-pds.cer"));
		tranxCon.setUserName(merchantId + "04");
		String bankCode = CommonUtil.getBankCodeByType(card.getCardType());
		return tranxService.singleTranx(tranxCon, tranURL, trx_code, busicode,isfront, card.getMode(), bankCode);
	}
	
	public String getSrcFilePath(String dir, String fileName) {
		return this.getClass().getResource("/").getPath() + dir + File.separator + fileName;
	}
	
	/**
	 * 发送验证码
	 * @param mobileNo 手机号码
	 * @param type 类型 mobile /email
	 * @param msg 短信内容
	 * @return 
	 * ****/
	@Override
	public RestDetail<String> saveCode(String mobileNo, String type, String msg, Boolean isBinding) {
		RestDetail<String> rs = new RestDetail<>(false);
		// 取得当前登录用户
		User user = ContextUtil.getUser(User.class);
		if (isBinding) {
			// 取得数据库中绑定银行卡的手机号码
			Card card = getCard(user);
			if (null == card) {
				rs.setCode(123);
				return rs;
			}
			mobileNo = card.getMobileNo();
		}
		
		if (StringUtils.isEmpty(mobileNo) || StringUtils.isEmpty(type)) {
			rs.setCode(-1);
			rs.setMsg("mobileNo Or type Was Null!");
		}
		if (Commons.PHONE.equals(type)) {
			if (SendPhoneMsg.isMobile(mobileNo)) {
				rs.setCode(230);
				return rs;
			}
			SendPhoneMsg sendPhoneMsg = SendPhoneMsg.getInstall();
			if (sendPhoneMsg.sendMsg(mobileNo, msg, Commons.MSG_REGISTER)) {
				VerificationInfo v = sendPhoneMsg.getRegisterCache().get(mobileNo);
				if (null == v) {
					rs.setCode(226);
					rs.setMsg("PhoneCode already Expire!");
					return rs;
				}
				rs.setData(v.getCode());
				rs.setState(true);
				return rs;
			}
		} else if (Commons.EMAIL.equals(type)) {
			// TODO 邮箱发送
		}
		return rs;
	}

	private User getUser(long user_id) {
		List<Object> args = new ArrayList<Object>();
		args.add(user_id);
		StringBuffer buf = new StringBuffer();
		buf.append("SELECT * FROM SYS_USER WHERE ID=?");
		
		return genericDao.findT(User.class, buf.toString(), args.toArray());
	}
	
	@Override
	public void saveWithdrawTradeFlow(TradeFlow tradeFlow) {
		if (null == tradeFlow || null == tradeFlow.getId())
			throw new BusinessException("非法请求");
			
		tradeFlow = getTradeFlow(tradeFlow.getId());
		if (null == tradeFlow)
			throw new BusinessException("找不到对应的交易流水信息");
		
		String resultCode = Commons.WITHDRAW_CODE_SUCCESS;

		// 取得待转账用户
		User user = getUser(tradeFlow.getUserId());

		// 校验验证码
		Card cardDB = getCard(user);
		if (null == cardDB)
			throw new BusinessException("找不到对应的银行卡信息");

		// 用户录入的提现金额=交易金额-1.5（手续费）
		double tradeAmount = MathUtil.sub(tradeFlow.getTradeAmount(), 1.5);
		
		// 取得该用户的账户余额（可提现金额）
		double withdrawalAmount = saveWithdrawalAmount(user);
		BigDecimal bdWithdrawalAmount = new BigDecimal(withdrawalAmount);
		if (new BigDecimal(tradeAmount).compareTo(bdWithdrawalAmount) == 1) {
			throw new BusinessException("该用户的账户余额不足！");
		}

		try {
			// 调用通联代付接口，给当前用户转账
			resultCode = singleTranx(cardDB, tradeAmount);
			if (!Commons.WITHDRAW_CODE_FAIL.equals(resultCode)) {// 成功
				logger.error("提现成功！返回码:" + resultCode);
				
				// 提现成功后，根据最新的账户余额（可提现金额）更新流水的balance字段
				double finalBalance = MathUtil.sub(withdrawalAmount, tradeFlow.getTradeAmount());
				tradeFlow.setBalance(MathUtil.round(finalBalance, 2));
				
				updateTradeFlow(tradeFlow, Commons.TRADE_STATUS_SUCCESS);
			} else {
				logger.error("提现失败！错误码:" + resultCode);
				updateTradeFlow(tradeFlow, Commons.TRADE_STATUS_FAIL);//交易失败
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new BusinessException("提现出现异常！");
		}
		
	}

	@Override
	public Map<String, Object> saveSysData() {
		Map<String, Object> map = new HashMap<String, Object>();
		
		// 取得当前登录用户
		User user = ContextUtil.getUser(User.class);
		
		// 取得可提现金额
		double withdrawalAmount = saveWithdrawalAmount(user);
		map.put("withdrawalAmount", withdrawalAmount);
		
		// 取得当前用户所属机构的银行卡信息
		Card card = getCard(user);
		map.put("card", card);
		
		// 是否已绑定了银行卡
		map.put("isBinding", card != null);
		
		return map;
	}
	/**
	 * 同一个账户下的收与支
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	@Override
	public Map<String, Double> findPayWithDraw(Page page, Date startTime, Date endTime) {
		Map<String, Double> acount = new HashMap<String, Double>();

		Double pay = 0.0;
		Double withdraw = 0.0;

		List<TradeFlow> tradeList = findTradeFlow(page, startTime, endTime);

		if (null == tradeList) {
			throw new BusinessException("没有查询到交易流水！");
		}

		for (TradeFlow trade : tradeList) {
			if (Commons.TRADE_TYPE_WITHDRAW == trade.getTradeType()
					&& Commons.TRADE_STATUS_SUCCESS == trade.getTradeStatus()) {
				pay = pay + trade.getTradeAmount();
			} else if (Commons.TRADE_TYPE_RECHARGE == trade.getTradeType()
					&& Commons.TRADE_STATUS_SUCCESS == trade.getTradeStatus()) {
				withdraw = withdraw + trade.getTradeAmount();
			} else if (Commons.TRADE_TYPE_REFUND == trade.getTradeType()
					&& Commons.TRADE_STATUS_SUCCESS == trade.getTradeStatus()) {
				pay = pay + trade.getTradeAmount();
			}
		}
		acount.put("withdraw", withdraw);
		acount.put("pay", pay);

		return acount;
	}
	
	/**
	 * 取得交易流水信息
	 */
	private TradeFlow getTradeFlow(Long id) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer("SELECT ");
		String cols = SQLUtils.getColumnsSQL(TradeFlow.class, "C");
		buf.append(cols);
		buf.append(" FROM T_TRADE_FLOW C WHERE C.ID = ? ");
		args.add(id);
		return genericDao.findT(TradeFlow.class, buf.toString(), args.toArray());
	}
	
	@Override
	public List<TradeFlow> findTradeFlow(Page page, Date startTime, Date endTime) {
		if (page != null) {
			page.setOrder(" T.TRADE_TIME ");
			page.setDesc(true);
		}
		List<Object> args = new ArrayList<Object>();

		// 取得当前登录用户
		User user = ContextUtil.getUser(User.class);
		if (null == user) {
			throw new BusinessException("用户未登录！");
		}
		args.add(user.getOrgId());

		String condition = " 1 = 1 ";

		if (!StringUtils.isEmpty(startTime) && !StringUtils.isEmpty(endTime)) {
			condition = "(T.TRADE_TIME BETWEEN ? AND ?) ";

			args.add(DateUtil.getStartDate(startTime));
			args.add(DateUtil.getEndDate(endTime));
		}

		StringBuffer buf = new StringBuffer();
		buf.append(" SELECT ");
		String cols = SQLUtils.getColumnsSQL(TradeFlow.class, "T");
		buf.append(cols);
		buf.append(" FROM T_TRADE_FLOW T WHERE ORG_ID = ? AND " + condition);

		return genericDao.findTs(TradeFlow.class, page, buf.toString(), args.toArray());
	}
	
	/**
	 * 查询提现处理数据 
	 * @param tradeFlow
	 */
	public List<TradeFlow> findWithdrawTradeFlow(Page page, TradeFlow tradeFlow) {
		User user = ContextUtil.getUser(User.class);
		if (null == user)
			throw new BusinessException("当前用户未登录");
		
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer("SELECT ");
		String cols = SQLUtils.getColumnsSQL(TradeFlow.class, "TF");
		buf.append(cols);
		buf.append(" ,O.NAME AS orgName, C.CARD_TYPE AS cardType, C.CARD_OWNER AS cardOwner, C.CARD_NO AS cardNo, U.REAL_NAME AS realName ");
		buf.append(" FROM T_TRADE_FLOW TF ");
		buf.append(" LEFT JOIN SYS_ORG O ON O.ID = TF.ORG_ID ");
		buf.append(" LEFT JOIN SYS_USER U ON U.ID = TF.USER_ID AND U.ORG_ID = O.ID ");
		buf.append(" LEFT JOIN T_CARD C ON C.ORG_ID = O.ID ");
		buf.append(" WHERE 1=1 ");
		buf.append(" AND TF.TRADE_TYPE = ? ");
		args.add(Commons.TRADE_TYPE_WITHDRAW);//提现
		
		if (!StringUtils.isEmpty(tradeFlow.getOrgName())) {
			buf.append(" AND").append(" (O.NAME LIKE ? OR O.NAME LIKE ?)");
			args.add("%" + ZHConverter.convert(tradeFlow.getOrgName(), ZHConverter.TRADITIONAL) + "%");
			args.add("%" + ZHConverter.convert(tradeFlow.getOrgName(), ZHConverter.SIMPLIFIED) + "%");
		}
		
		if (null != tradeFlow.getTradeStatus()) {
			buf.append(" AND TF.TRADE_STATUS = ? ");
			args.add(tradeFlow.getTradeStatus());
		}
		buf.append(" ORDER BY TF.TRADE_TIME DESC ");
		
		return genericDao.findTs(TradeFlow.class, page, buf.toString(), args.toArray());
	}
	
}
