package com.vendor.service;

import com.vendor.po.Order;
import com.vendor.po.OrderDetail;

import java.util.List;

/**
 * Created by Chris Zhu on 2017/3/26.
 *
 * @author Chris Zhu
 */
public interface IOrderService
{
    /**
     * 根据内部订单号获取订单
     *
     * @param code 内部订单号
     * @return 订单
     */
    Order getOrderByCode(String code);

    /**
     * 根据内部订单号设置订单为已付款
     *
     * @param code 内部订单号
     */
    void setOrderPayedByCode(String code);


    /**
     * 更新订单
     *
     * @param order 订单对象
     */
    void updateOrder(Order order);

    /**
     * 保存充值交易流水信息
     */
    void saveTradeFlow(Long orgId, Double amount);

    /**
     * 根据订单编号查找订单详情
     *
     * @param orderNo 订单编号
     * @return 订单详情集合
     */
    List<OrderDetail> findOrderDetailOrderType(String orderNo);


    OrderDetail findOrderDetailLotteryProduct(String orderNo);

    List<OrderDetail> findOrderDetails(String orderNo);

    /**
    * 根据订单编号和订单状态查询订单
    */
    Order findOrderByOrderNoAndState(String orderNo, Integer orderState);

    
}
