package com.vendor.control.web;

import com.ecarry.core.web.control.BaseControl;
import com.ecarry.core.web.core.ContextUtil;
import com.ecarry.core.web.core.Page;
import com.vendor.control.app.PayEnterControl;
import com.vendor.po.Device;
import com.vendor.po.User;
import com.vendor.service.IDeviceService;
import com.vendor.service.ISystemService;
import com.vendor.service.IUserInfoService;
import com.vendor.util.Commons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Objects;

/**
 * Created by Chris on 2017/2/22.
 * 设备查找Control
 */
@Controller
@RequestMapping(value = {"/orgnization", "/device"})
public class DeviceControl extends BaseControl
{

    private static final Logger logger = LoggerFactory.getLogger(PayEnterControl.class);

    private final IUserInfoService m_userInfoService;
    private final ISystemService m_systemService;
    private final IDeviceService m_deviceService;


    @Autowired
    public DeviceControl(IUserInfoService userInfoService, ISystemService systemService, IDeviceService deviceService)
    {
        this.m_userInfoService = userInfoService;
        this.m_systemService = systemService;
        this.m_deviceService = deviceService;
    }

    /**
     * 根据组织id查询设备.
     * 目前在页面上设备归属是"绑定"和"未绑定"，上级组织可以分配自己的设备到下级(绑定)，可以取消下级组织已分配的设备(解绑)
     *
     * @param page              the page
     * @param device            the device
     * @param startFactoryDevNo the start factory dev no
     * @param endFactoryDevNo   the end factory dev no
     * @return the list
     */
    @RequestMapping(value = "orgnizationList/findDevices.json", method = RequestMethod.POST)
    @ModelAttribute("rows")
    public List<Device> findDevicesByOrigId(Page page, Device device, String startFactoryDevNo, String endFactoryDevNo)
    {
        if (device.getOrgId() == null)
        {         //查找未绑定设备
            //获取当前登录用户的组织ID
            User user = ContextUtil.getUser(User.class);
            device.setOrgId(user.getOrgId());
        }
        return m_userInfoService.findVenderPartnerDevices(page, device, startFactoryDevNo, endFactoryDevNo);
    }

    /**
     * 绑定或解绑设备
     *
     * @param bindingFlag 设备绑定标识   0：解绑   1：绑定
     * @param orgId       the org id
     * @param devNos      the dev nos
     * @see com.vendor.util.Commons#DEVICE_BIND_TRUE
     * @see com.vendor.util.Commons#DEVICE_BIND_FALSE
     */
    @RequestMapping(value = "orgnizationList/saveBindOrUnBindDevices.json", method = RequestMethod.POST)
    @ModelAttribute("rows")
    public void bindOrUnBindDevices(Integer bindingFlag, Long orgId, String[] devNos)
    {
        if (Objects.equals(bindingFlag, Commons.DEVICE_BIND_TRUE))
        {
            m_systemService.saveBindDevices(bindingFlag, orgId, devNos);
        }
        else if (bindingFlag.equals(Commons.DEVICE_BIND_FALSE))
        {
            m_userInfoService.saveVenderpartnerUnBindDevices(devNos);
        }
    }

    /**
     * 检测设备是否在线
     *
     * @param facDevNo the fac dev no
     * @return the string
     */
    @RequestMapping(value = "checkDeviceOnlineByFacDevNo")
    @ResponseBody
    public boolean checkDeviceOnlineByFacDevNo(String facDevNo)
    {
        return m_deviceService.checkDeviceOnlineByFacDevNo(facDevNo);
    }
}
