package com.vendor.service;

import com.vendor.po.Device;
import com.vendor.po.Order;

/**
 * Created by Chris Zhu on 2017/3/26.
 * 设备服务接口
 *
 * @author Chris Zhu
 */
public interface IDeviceService
{
    /**
     * 根据设备编号检测设备是否在线
     *
     * @param facDevNo 设备编号
     * @return the boolean
     */
    boolean checkDeviceOnlineByFacDevNo(String facDevNo);

    /**
     * 根据设备内部编号检测设备是否在线
     *
     * @param devNo 设备编号
     * @return the boolean
     */
    boolean checkDeviceOnlineByDevNo(String devNo);

    /**
     * 根据设备号查找设备
     *
     * @param deviceNumber 设备号
     * @return 设备对象
     */
    Device findDeviceByDevNo(String deviceNumber);

    /**
     * 通知设备出货
     *
     * @param order 订单
     */
    boolean notifyDeviceDeliver(Order order) throws Exception;

    /**
     * 根据设备内部编号查找设备编号
     *
     * @param devNo 设备内部编号
     * @return 设备编号
     */
    String findFacDevNoByDevNo(String devNo);
}
