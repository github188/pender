package com.vendor.service.impl.order;

import com.ecarry.core.dao.IGenericDao;
import com.ecarry.core.dao.SQLUtils;
import com.ecarry.core.util.MathUtil;
import com.vendor.control.app.PayEnterControl;
import com.vendor.po.Order;
import com.vendor.po.OrderDetail;
import com.vendor.po.TradeFlow;
import com.vendor.po.User;
import com.vendor.service.IOrderService;
import com.vendor.util.Commons;
import com.vendor.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chris Zhu on 2017/3/26.
 *
 * @author Chris Zhu
 */
@Service("orderService")
public class OrderService implements IOrderService
{
    private static final Logger logger = LoggerFactory.getLogger(PayEnterControl.class);
    
    private final IGenericDao genericDao;

    @Autowired
    public OrderService(IGenericDao genericDao)
    {
        this.genericDao = genericDao;
    }

    /**
     * @see IOrderService#getOrderByCode(String)
     */
    @Override
    public Order getOrderByCode(String code)
    {
        if (StringUtils.isEmpty(code))
            return null;
        return genericDao.findT(Order.class, "SELECT * FROM T_ORDER WHERE CODE=?", code);
    }

    /**
     * @see IOrderService#setOrderPayedByCode(String)
     */
    @Override
    public void setOrderPayedByCode(String code)
    {
        genericDao.execute("UPDATE t_order SET state=8 WHERE code=?", code);
    }
    /**
     * @see IOrderService#updateOrder(Order)
     */
    @Override
    public void updateOrder(Order order)
    {
        genericDao.update(order);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void saveTradeFlow(Long orgId, Double amount)
    {
        // 从交易流水表中查询当前用户最近时间的流水
        List<Object> args = new ArrayList<Object>();
        StringBuilder sb = new StringBuilder("SELECT ");
        String cols = SQLUtils.getColumnsSQL(TradeFlow.class, "C");
        sb.append(cols);
        sb.append(" FROM T_TRADE_FLOW C WHERE C.ORG_ID = ? AND C.TRADE_STATUS = ? ORDER BY C.TRADE_TIME DESC ");
        args.add(orgId);
        args.add(Commons.TRADE_STATUS_SUCCESS);
        TradeFlow tradeFlow = genericDao.findT(TradeFlow.class, sb.toString(), args.toArray());
        if (null == tradeFlow) {
            tradeFlow = createTradeFlow(orgId);
            if (null != tradeFlow && tradeFlow.getTradeAmount() > 0)
                genericDao.save(tradeFlow);
        } else {// 之前有交易流水记录
            TradeFlow tFlow = new TradeFlow();
            tFlow.setOrgId(orgId);
            tFlow.setTradeType(Commons.TRADE_TYPE_RECHARGE);
            tFlow.setTradeAmount(amount);// 交易金额
            tFlow.setTradeTime(new Timestamp(System.currentTimeMillis()));
            tFlow.setBalance(MathUtil.round(MathUtil.add(tradeFlow.getBalance(), amount), 2));
            tFlow.setTradeStatus(Commons.TRADE_STATUS_SUCCESS);
            genericDao.save(tFlow);
        }
    }

    /**
     * @see IOrderService#findOrderDetailOrderType(String) 
     */
    @Override
    public List<OrderDetail> findOrderDetailOrderType(String orderNo)
    {
        String sql = " SELECT A.ORDER_TYPE FROM T_ORDER_DETAIL A " +
                " WHERE A.ORDER_NO=? ";
        return genericDao.findTs(OrderDetail.class, sql, orderNo);
    }

    @Override
    public OrderDetail findOrderDetailLotteryProduct(String orderNo)
    {
        StringBuilder builder = new StringBuilder("SELECT ");
        builder.append(SQLUtils.getColumnsSQL(OrderDetail.class, "A"));
        builder.append(" , P.CODE as productCode, P.SKU_NAME as skuName ");
        builder.append(" FROM T_ORDER_DETAIL A ");
        builder.append(" LEFT JOIN T_PRODUCT P ON P.ID = A.SKU_ID ");
        builder.append(" WHERE A.ORDER_NO=? ");
        logger.info("***************【抽奖活动出货查询:"+builder.toString()+",orderNo="+orderNo+"】****************");
        return genericDao.findT(OrderDetail.class, builder.toString(), orderNo);
    }

    @Override
    public List<OrderDetail> findOrderDetails(String orderNo)
    {
        String sql = "SELECT " + SQLUtils.getColumnsSQL(OrderDetail.class, "A") +
                " , P.CODE as productCode, P.SKU_NAME as skuName " +
                " FROM T_ORDER_DETAIL A " +
                " LEFT JOIN T_PRODUCT P ON P.ID = A.SKU_ID " +
                " WHERE A.ORDER_NO=? ";
        return genericDao.findTs(OrderDetail.class, sql, orderNo);
    }

    @Override
    public Order findOrderByOrderNoAndState(String orderNo, Integer orderState)
    {
        List<Object> args = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(SQLUtils.getColumnsSQL(Order.class, "A"));
        sql.append(" FROM T_ORDER A WHERE A.CODE=? ");
        args.add(orderNo);
        if (null != orderState) {
            sql.append(" AND A.STATE = ? ");
            args.add(orderState);
        }
        return genericDao.findT(Order.class, sql.toString(), args.toArray());
    }

    /**
     * 构建初始化交易流水信息
     *
     * @param user 当前用户
     * @return 初始化交易流水信息
     */
    @SuppressWarnings("Duplicates")
    private TradeFlow createTradeFlow(Long orgId) {
        Timestamp curTime = new Timestamp(System.currentTimeMillis());
        // 取得总销售额(所有商品总销售额)
        User user = new User();
        user.setOrgId(orgId);
        double salesTotalAmount = getSalesAmount(user, null);

        TradeFlow tradeFlow = new TradeFlow();
        tradeFlow.setOrgId(orgId);
        tradeFlow.setTradeType(Commons.TRADE_TYPE_RECHARGE);
        tradeFlow.setTradeAmount(MathUtil.round(salesTotalAmount, 2));
        tradeFlow.setTradeTime(curTime);
        tradeFlow.setBalance(MathUtil.round(salesTotalAmount, 2));
        tradeFlow.setTradeStatus(Commons.TRADE_STATUS_SUCCESS);
        return tradeFlow;
    }

    /**
     * 取得当前登录用户的销售额
     *
     * @param user 当前登录用户
     * @param date 指定日期
     * @return 当前登录用户的销售额
     */
    @SuppressWarnings("Duplicates")
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
}
