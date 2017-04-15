package com.vendor.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.ecarry.core.dao.IGenericDao;
import com.ecarry.core.exception.BusinessException;
import com.ecarry.core.util.IdWorker;
import com.ecarry.core.util.MathUtil;
import com.google.gson.Gson;
import com.vendor.po.Order;
import com.vendor.po.OrderDetail;
import com.vendor.po.Refund;
import com.vendor.po.User;
import com.vendor.service.IOrderService;
import com.vendor.service.IRefundService;
import com.vendor.util.Commons;
import com.vendor.util.StringUtil;
import com.vendor.util.alipay.AlipayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Chris Zhu on 2017/3/27.
 *
 * @author Chris Zhu
 */
@Service("refundService")
public class RefundService implements IRefundService
{
	private static final Logger logger = LoggerFactory.getLogger(RefundService.class);

	private final IOrderService mIOrderService;

	private final IGenericDao mIGenericDao;

	private final IdWorker mIdWorker;

	@Autowired
	public RefundService(IOrderService mIOrderService, IGenericDao mIGenericDao, IdWorker idWorker)
	{
		this.mIOrderService = mIOrderService;
		this.mIGenericDao = mIGenericDao;
		this.mIdWorker = idWorker;
	}

	/**
	 * @see IRefundService#refund(Order)
	 */
	@Override
	public void refund(Order order)
	{
		logger.info("refund(logged by Chris Zhu):进行退款，内部订单号：" + order.getCode());

		if (Objects.equals(Commons.PAY_TYPE_WX, order.getPayType()))
		{//微信退款
			// TODO: 2017/3/27 微信退款已在 VendingService#saveOfficialAccountsAsyncNotify 实现，但需重构到此
			logger.info("refund(logged by Chris Zhu):微信退款");
		} else if (Objects.equals(Commons.PAY_TYPE_ALI, order.getPayType()))
		{//支付宝退款
			alipayRefund(order);
		}
	}

	private void alipayRefund(Order order)
	{
		logger.info("refund(logged by Chris Zhu):支付宝退款");
		List<Refund> refunds = findRefundByOrderNo(order.getCode());
		AlipayClient alipayClient = AlipayUtil.getAliPayClient();
		AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();//创建API对应的request类
		for (Refund refund : refunds)
		{
			request.setBizContent(this.getBizJsonString(String.valueOf(refund.getFeeRefund()), order.getCode(),
					refund.getCode()));//设置业务参数
			AlipayTradeRefundResponse response = null;//通过alipayClient调用API，获得对应的response类
			try
			{
				response = alipayClient.execute(request);
			} catch (AlipayApiException e)
			{
				String msg = e.getMessage();
				logger.error("refund(logged by Chris Zhu):退款失败：" + msg, e);
				handleRefundFail(refund, StringUtil.truncateString(msg, 250));
			}
			if (response != null)
			{
				if (response.isSuccess())//接口调用成功
				{
					logger.info("refund(logged by Chris Zhu):支付宝退款接口调用成功");
					//此处不同于微信支付，无需设置退款中的状态
					refund.setState(Commons.REFUND_STATE_SUCCESS);
					refund.setUpdateTime(new Timestamp(System.currentTimeMillis()));
					refund.setRemark(null);
					mIGenericDao.update(refund);
				} else
				{
					String code = response.getCode();
					String subMsg = response.getSubMsg();
					logger.error("refund(logged by Chris Zhu):支付宝退款接口调用失败：code=" + code + " msg=" + response
							.getMsg() + " sub_code=" + response.getSubCode() + " sub_msg=" + subMsg);
					handleRefundFail(refund,
							StringUtil.truncateString(code + ":" + subMsg, 250));
				}
			}
		}

	}

	private void handleRefundFail(Refund refund, String remark)
	{
		refund.setState(Commons.REFUND_STATE_FAILED);
		refund.setUpdateTime(new Timestamp(System.currentTimeMillis()));
		refund.setRemark(remark);
		mIGenericDao.update(refund);
	}

	private String getBizJsonString(String amount, String orderNo, String outRequestNo)
	{
		BizContent bizContent = new BizContent();
		bizContent.setOut_trade_no(orderNo);
		bizContent.setRefund_amount(amount);
		bizContent.setOut_request_no(outRequestNo);
		Gson gson = new Gson();
		return gson.toJson(bizContent);
	}

	@SuppressWarnings("unused")
	private class BizContent
	{
		private String out_trade_no;//商家内部订单编号
		private String refund_amount;//总退款金额
		private String out_request_no;//退款流水号

		void setOut_request_no(String out_request_no)
		{
			this.out_request_no = out_request_no;
		}

		void setOut_trade_no(String out_trade_no)
		{
			this.out_trade_no = out_trade_no;
		}

		void setRefund_amount(String refund_amount)
		{
			this.refund_amount = refund_amount;
		}
	}

	@Override
	public List<Refund> saveRefundOrder(Order order)
	{
		List<OrderDetail> orderDetails = mIOrderService.findOrderDetails(order.getCode());
		List<Refund> refunds = new ArrayList<>();
		for (OrderDetail orderDetail : orderDetails)
		{
			Refund refund = new Refund();
			refund.setOrderNo(order.getCode());
			refund.setFeeRefund(orderDetail.getPrice() * orderDetail.getQty());
			refund.setAmount(order.getAmount());
			refund.setPayNo(order.getPayCode());
			refund.setType(order.getPayType());
			refund.setReason("支付成功后，设备离线。");
			refund.setState(Commons.REFUND_STATE_NEW);
			refund.setSkuId(orderDetail.getSkuId());
			refund.setRefundQty(orderDetail.getQty());
			User user = new User();
			user.setOrgId(order.getOrgId());
			user.setId(1L);
			refund.setOrgId(user.getOrgId());
			refund.setCode(mIdWorker.nextCode());
			refund.setCreateUser(user.getId());
			refund.setCreateTime(new Timestamp(System.currentTimeMillis()));
			mIGenericDao.save(refund);
			refunds.add(refund);
		}
		return refunds;
	}

	/**
	 * @see IRefundService#findRefundByOrderNo(String)
	 */
	@Override
	public List<Refund> findRefundByOrderNo(String orderNo)
	{
		String sql = "SELECT * FROM t_refund WHERE order_no=? AND (state=0 OR state=-1)";
		return mIGenericDao.findTs(Refund.class, sql, orderNo);
	}

	/**
	 * @see IRefundService#timingRefund()
	 */
	@Override
	public void timingRefund()
	{
		logger.info("timingRefund(logged by Chris Zhu):定时处理退款单(支付宝)");
		List<Order> orders = this.findPendingRefundOrder();
		for (Order order : orders)
		{
			this.refund(order);
		}
	}

	/**
	 * @return 退款未处理或者处理失败的订单集合
	 */
	private List<Order> findPendingRefundOrder()
	{
		String sql = "SELECT * FROM t_order WHERE code IN (SELECT DISTINCT order_no FROM t_refund WHERE  state=0 OR state=-1)";
		return mIGenericDao.findTs(Order.class, sql);
	}

	@Override
	public void saveRefundOrder(Refund refund, User user)
	{
		Order oldOrder = mIOrderService.findOrderByOrderNoAndState(refund.getOrderNo(), Commons.ORDER_STATE_FINISH);
		// 取得该订单已退款金额
		Double refundAmount = mIGenericDao.findSingle(Double.class,
				"SELECT COALESCE(SUM(FEE_REFUND),0) FROM T_REFUND WHERE ORDER_NO = ? AND STATE = ?",
				refund.getOrderNo(), Commons.REFUND_STATE_SUCCESS);
		refundAmount = MathUtil.round(null == refundAmount ? 0 : refundAmount, 2);

		if (refund.getFeeRefund() > MathUtil.sub(oldOrder.getAmount(), refundAmount))
			throw new BusinessException("超出最大可退款金额！");

		refund.setPayNo(oldOrder.getPayCode());
		refund.setAmount(oldOrder.getAmount());
		refund.setOrderNo(oldOrder.getCode());
		refund.setType(oldOrder.getPayType());// 6:微信
		refund.setState(Commons.REFUND_STATE_NEW);
		refund.setOrgId(user.getOrgId());
		refund.setCode(mIdWorker.nextCode());
		refund.setCreateUser(user.getId());
		refund.setCreateTime(new Timestamp(System.currentTimeMillis()));

		mIGenericDao.save(refund);
	}
}
