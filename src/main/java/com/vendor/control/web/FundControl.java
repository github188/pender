package com.vendor.control.web;

import java.sql.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.ecarry.core.web.control.BaseControl;
import com.ecarry.core.web.core.ContextUtil;
import com.ecarry.core.web.core.Page;
import com.ecarry.core.web.view.ExcelView;
import com.vendor.po.Card;
import com.vendor.po.TradeFlow;
import com.vendor.po.User;
import com.vendor.service.IFundService;
import com.vendor.util.Commons;
import com.vendor.vo.app.RestStatus;

/**
 * 资金管理
 */
@Controller
@RequestMapping(value = "/fund")
public class FundControl extends BaseControl {
	
	@Value("${msg.sendMsgContent}")
	private String msg;
	
	@Autowired
	private IFundService fundServcie;
	
	@RequestMapping(value = "fundStatic/forward.do", method = RequestMethod.GET)
	public ModelAndView forwardAnalysis() {
		ModelAndView view = new ModelAndView("/fund/fundStatic.jsp");
		User user = ContextUtil.getUser(User.class);
		view.addObject("_orgName", user.getOrgName());
		view.addObject("_orgId", user.getOrgId());
		return view;
	}

	@RequestMapping(value = "withdraw/forward.do", method = RequestMethod.GET)
	public ModelAndView forwardWithdraw() {
		ModelAndView view = new ModelAndView("/fund/withdraw.jsp");
		User user = ContextUtil.getUser(User.class);
		view.addObject("_orgName", user.getOrgName());
		view.addObject("_orgId", user.getOrgId());
		return view;
	}
	
	@RequestMapping(value = "/fundStatic/saveCode.json", method = RequestMethod.POST)
	@ResponseBody
	public RestStatus saveCode(String phoneNumber, Boolean isBinding) {
		return fundServcie.saveCode(phoneNumber, Commons.PHONE, msg, isBinding);
	}
	
	@RequestMapping(value = "/fundStatic/saveCard.json", method = RequestMethod.POST)
	public void saveCard(Card card, String checkCode) {
		fundServcie.saveCard(card, checkCode);
	}

	@RequestMapping(value = "/fundStatic/save.json", method = RequestMethod.POST)
	public void saveTradeFlow(TradeFlow tradeFlow, String checkCode) {
		fundServcie.saveTradeFlow(tradeFlow, checkCode);
	}
	
	@RequestMapping(value = "/fundStatic/saveWithdrawTradeFlow.json", method = RequestMethod.POST)
	public void saveWithdrawTradeFlow(TradeFlow tradeFlow) {
		fundServcie.saveWithdrawTradeFlow(tradeFlow);
	}
	
	@RequestMapping(value = "/fundStatic/find.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<TradeFlow> findTradeFlow(Map<String, Double> all, Page page, Date startTime, Date endTime) {
		Map<String, Double> map = fundServcie.findPayWithDraw(null, startTime, endTime);
		Set<Map.Entry<String, Double>> sets = map.entrySet();
		for (Map.Entry<String, Double> set : sets)
			all.put(set.getKey(), set.getValue());

		return fundServcie.findTradeFlow(page, startTime, endTime);
	}
	
	@RequestMapping(value = "/fundStatic/findSysData.json", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> findSysData() {
		return fundServcie.saveSysData();
	}
	
	/**
	 * 导出流水信息
	 * @return
	 */
	@RequestMapping(value = "/fundStatic/export.xls", method = RequestMethod.GET)
	public ModelAndView exportProductStock(Date startTime, Date endTime) {
		Page page = new Page();
		page.setCurPage(1);
		page.setPageSize(20000);	//	最大查询2万条
		List<TradeFlow> tradeList = fundServcie.findTradeFlow(page, startTime, endTime);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Commons.TRADE_TYPE_WITHDRAW + "", "提现");
		map.put(Commons.TRADE_TYPE_RECHARGE + "", "充值");
		map.put(Commons.TRADE_TYPE_REFUND + "", "退款");
		
		Map<String, Object> mapValues = new HashMap<String, Object>();
		mapValues.put("tradeType", map);
		
		Map<String, String> titleMap = new LinkedHashMap<String, String>();
		titleMap.put("交易时间","tradeTime");
		titleMap.put("交易类型", "tradeType");
		titleMap.put("交易金额", "tradeAmount");
		titleMap.put("账户余额", "balance");
		ExcelView view = new ExcelView(tradeList, titleMap, "交易流水信息", mapValues);
		return new ModelAndView(view);
	}
	
	/**
	 * 查询提现处理数据
	 */
	@RequestMapping(value = "/withdraw/find.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<TradeFlow> findWithdrawTradeFlow(Page page, TradeFlow tradeFlow) {
		return fundServcie.findWithdrawTradeFlow(page, tradeFlow);
	}
}
