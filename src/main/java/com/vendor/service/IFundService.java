package com.vendor.service;

import java.sql.Date;
import java.util.List;
import java.util.Map;

import com.ecarry.core.web.core.Page;
import com.vendor.po.Card;
import com.vendor.po.TradeFlow;
import com.vendor.vo.app.RestStatus;

public interface IFundService {

	/**
	 * 首页用系统各数据
	 */
	Map<String, Object> saveSysData();
	
	/**
	 * 保存银行卡信息
	 */
	void saveCard(Card card, String checkCode);
	
	/**
	 * 保存交易流水信息
	 */
	void saveTradeFlow(TradeFlow tradeFlow, String checkCode);
	
	/**
	 * 提交交易到第三方支付通道
	 */
	void saveWithdrawTradeFlow(TradeFlow tradeFlow);
	
	/**
	 * 发送验证码
	 * @param mobileNo 手机号码
	 * @param type  类型 mobile /email
	 * @return 
	 * ****/
	RestStatus saveCode(String mobileNo, String type, String msg, Boolean isBinding);

	/**
	 * 查找提现状态为“初始化”状态的流水
	 * @param tradeFlow
	 */
	List<TradeFlow> findTradeFlow(Page page, Date startTime, Date endTime);
	
	/**
	 * 一段时间内的收支情况
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	Map<String,Double> findPayWithDraw(Page page, Date startTime, Date endTime);
	
	/**
	 * 查询提现处理数据
	 * @param tradeFlow
	 */
	List<TradeFlow> findWithdrawTradeFlow(Page page, TradeFlow tradeFlow);
	
}
