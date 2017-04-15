package com.vendor.service.impl.device;

import com.aliyuncs.push.model.v20150827.GetDeviceInfosResponse;
import com.ecarry.core.dao.IGenericDao;
import com.ecarry.core.dao.SQLUtils;
import com.ecarry.core.web.core.ContextUtil;
import com.vendor.po.Device;
import com.vendor.po.DevicePush;
import com.vendor.po.Order;
import com.vendor.po.OrderDetail;
import com.vendor.service.IDeviceService;
import com.vendor.service.IOrderService;
import com.vendor.util.Commons;
import com.vendor.util.MessagePusher;
import com.vendor.vo.app.OrderData;
import com.vendor.vo.app.VProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chris Zhu on 2017/3/26.
 *
 * @author Chris Zhu
 */
@Service("deviceService")
public class DeviceService implements IDeviceService
{
    private static final Logger logger = LoggerFactory.getLogger(DeviceService.class);

    private final IGenericDao m_genericDao;

    private final IOrderService m_orderService;

    @Autowired
    public DeviceService(IGenericDao genericDao, IOrderService orderService)
    {
        this.m_genericDao = genericDao;
        this.m_orderService = orderService;
    }

    /**
     *
     * @see IDeviceService#checkDeviceOnlineByFacDevNo(String)
     */
    @Override
    public boolean checkDeviceOnlineByFacDevNo(String facDevNo)
    {
        DevicePush devicePush = findDevicePush(facDevNo);
        List<String> deviceIds = new ArrayList<>();
        deviceIds.add(devicePush.getPushDeviceId());
        // 查询设备状态
        MessagePusher pusher = new MessagePusher();
        try
        {
            for (GetDeviceInfosResponse.DeviceInfo deviceInfo : pusher.getDeviceInfos(deviceIds)) {
                if (deviceInfo.getDeviceId().equals(devicePush.getPushDeviceId())) {
                    boolean isOnline = deviceInfo.getIsOnline();
                    if (isOnline) {
                        logger.info("checkDeviceOnlineByFacDevNo(logged by Chris Zhu):设备在线,设备编号："+facDevNo);
                        return true;
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        logger.error("checkDeviceOnlineByFacDevNo(logged by Chris Zhu):设备离线,设备编号："+facDevNo);
        return false;
    }

    @Override
    public boolean checkDeviceOnlineByDevNo(String devNo)
    {
        String facDevNo = this.findFacDevNoByDevNo(devNo);
        return this.checkDeviceOnlineByFacDevNo(facDevNo);
    }

    /**
     *
     * @see IDeviceService#findDeviceByDevNo(String)
     */
    @Override
    public Device findDeviceByDevNo(String deviceNumber)
    {
        String sql = "SELECT " + SQLUtils.getColumnsSQL(Device.class, "A") +
                " FROM T_DEVICE A WHERE A.DEV_NO = ? ";
        return m_genericDao.findT(Device.class, sql, deviceNumber);
    }

    /**
     *
     * @see IDeviceService#notifyDeviceDeliver(Order)
     */
    @Override
    public boolean notifyDeviceDeliver(Order order) throws Exception
    {
        String deviceNo = order.getDeviceNo();
        boolean isOnline = checkDeviceOnlineByDevNo(deviceNo);
        if (isOnline) {// 设备在线
            logger.info("notifyDeviceDeliver(logged by Chris Zhu):设备在线，通知设备可以出货");
            // 主动通知app，告知此订单号的商品已支付，可以出货了。
            List<String> devIDs = new ArrayList<>();
            devIDs.add(findFacDevNoByDevNo(order.getDeviceNo()));
            List<VProduct> list = getVProducts(order);
            OrderData orderData = new OrderData();
            orderData.setNotifyFlag(Commons.NOTIFY_ORDER_PAY);// 订单支付通知flag
            orderData.setOrderNo(order.getCode());
            orderData.setState(1);
            orderData.setList(list);
            MessagePusher pusher = new MessagePusher();
            pusher.pushMessageToAndroidDevices(devIDs, ContextUtil.getJson(orderData), false);
            logger.info("notifyDeviceDeliver(logged by Chris Zhu):已通知设备出货");
        }
        else
        {
            logger.error("notifyDeviceDeliver(logged by Chris Zhu):设备离线，离线设备编号："+deviceNo);
        }
        return isOnline;
    }

    @SuppressWarnings("Duplicates")
    private List<VProduct> getVProducts(Order order)
    {
        List<OrderDetail> orderDetails = m_orderService.findOrderDetailOrderType(order.getCode());
        List<VProduct> list = new ArrayList<>();
        logger.info("**********【订单类型："+orderDetails.get(0).getOrderType()+"】**********");

        if(2 == orderDetails.get(0).getOrderType()){//抽奖活动
            OrderDetail orderDetail = m_orderService.findOrderDetailLotteryProduct(order.getCode());
            VProduct product = new VProduct();
            product.setProduceId(orderDetail.getProductCode());
            product.setCount(orderDetail.getQty());
            list.add(product);
        }else{
            List<OrderDetail> orderDetail2s = m_orderService.findOrderDetails(order.getCode());
            for (OrderDetail orderDetail2 : orderDetail2s) {
                VProduct product = new VProduct();
                product.setProduceId(orderDetail2.getProductCode());
                product.setCount(orderDetail2.getQty());
                list.add(product);
            }
        }
        return list;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public String findFacDevNoByDevNo(String devNo)
    {
        List<Object> args = new ArrayList<>();
        String sql = " SELECT " + " C.FACTORY_DEV_NO AS DEV_NO " +
                " FROM T_DEVICE_RELATION C " +
                " WHERE C.DEV_NO = ? ";
        args.add(devNo);
        return m_genericDao.findSingle(String.class, sql, args.toArray());
    }

    private DevicePush findDevicePush(String factoryDevNo) {
        String sql = "SELECT " + SQLUtils.getColumnsSQL(DevicePush.class, "A") +
                " FROM T_DEVICE_PUSH A WHERE A.FACTORY_DEV_NO = ? ";
        return m_genericDao.findT(DevicePush.class, sql, factoryDevNo);
    }
}
