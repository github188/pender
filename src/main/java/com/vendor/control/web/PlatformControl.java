/**
 *
 */
package com.vendor.control.web;

import com.ecarry.core.exception.BusinessException;
import com.ecarry.core.web.control.BaseControl;
import com.ecarry.core.web.core.ContextUtil;
import com.ecarry.core.web.core.Page;
import com.vendor.po.*;
import com.vendor.service.IMonitoringService;
import com.vendor.service.IPlatformService;
import com.vendor.service.ISystemService;
import com.vendor.util.Commons;
import com.vendor.vo.common.ResultBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.sql.Date;
import java.util.List;

/**
 * @author zhaoss on 2016年3月25日
 */
@Controller
@RequestMapping(value = "/platform")
public class PlatformControl extends BaseControl {

    @Autowired
    private ISystemService systemService;

    @Autowired
    private IPlatformService platformService;

    @Autowired
    private IMonitoringService monitoringService;

    @RequestMapping(value = "device/forward.do", method = RequestMethod.GET)
    public ModelAndView forwardDevice() {
        ModelAndView view = new ModelAndView("/store/device.jsp");
        User user = ContextUtil.getUser(User.class);
        view.addObject("_orgName", user.getOrgName());
        view.addObject("_orgId", user.getOrgId());
        return view;
    }

    @RequestMapping(value = "deviceType/forward.do", method = RequestMethod.GET)
    public ModelAndView forwardDeviceType() {
        ModelAndView view = new ModelAndView("/store/deviceType.jsp");
        User user = ContextUtil.getUser(User.class);
        view.addObject("_orgName", user.getOrgName());
        view.addObject("_orgId", user.getOrgId());
        return view;
    }

    /**
     * 分页查询城市合伙人信息
     *
     * @param page
     * @param orgnization
     * @param startDate
     * @param endDate
     * @return
     */
    @RequestMapping(value = "vender/find.json", method = RequestMethod.POST)
    @ModelAttribute("rows")
    public List<Orgnization> findCityPartner(Page page, Orgnization orgnization, Date startDate, Date endDate) {
        User user = ContextUtil.getUser(User.class);
        orgnization.setParentId(user.getOrgId());
        return systemService.findOrgnizations(page, orgnization, startDate, endDate);
    }

    /**
     * 校验所选设备号是否已上架了商品
     */
    @RequestMapping(value = "vender/findIsShelving.json", method = RequestMethod.POST)
    public void findIsShelving(String finalDevNos) {
        platformService.findIsShelving(finalDevNos);
    }

    /**
     * 校验所选设备号是否已上架了商品
     */
    @RequestMapping(value = "partner/findIsShelving.json", method = RequestMethod.POST)
    public void findIsShelvingPartner(String finalDevNos) {
        platformService.findIsShelving(finalDevNos);
    }

    /**
     * 分页查询设备信息
     *
     * @param page
     * @param device
     * @param startDevNo
     * @param endDevNo
     * @return
     */
    @RequestMapping(value = "vender/findVenderPartnerDevices.json", method = RequestMethod.POST)
    @ModelAttribute("rows")
    public List<Device> findVenderPartnerDevices(Page page, Device device, String startDevNo, String endDevNo,
                                                 String devNos) {
        User user = ContextUtil.getUser(User.class);
        device.setOrgId(user.getOrgId());
        return platformService.findVenderPartnerDevices(page, device, startDevNo, endDevNo, devNos);
    }

    @RequestMapping(value = "partner/findVenderPartnerDevices.json", method = RequestMethod.POST)
    @ModelAttribute("rows")
    public List<Device> findPartnerVenderPartnerDevices(Page page, Device device, String startDevNo, String endDevNo,
                                                        String devNos) {
        User user = ContextUtil.getUser(User.class);
        device.setOrgId(user.getOrgId());
        return platformService.findVenderPartnerDevices(page, device, startDevNo, endDevNo, devNos);
    }

    /**
     * 分页查询需解绑的设备信息
     *
     * @param page
     * @param device
     * @param startDevNo
     * @param endDevNo
     * @return
     */
    @RequestMapping(value = "vender/findUnBindingVenderPartnerDevices.json", method = RequestMethod.POST)
    @ModelAttribute("rows")
    public List<Device> findUnBindingVenderPartnerDevices(Page page, Device device, String startDevNo, String endDevNo) {
        return platformService.findVenderPartnerDevices(page, device, startDevNo, endDevNo, null);
    }

    @RequestMapping(value = "partner/findUnBindingVenderPartnerDevices.json", method = RequestMethod.POST)
    @ModelAttribute("rows")
    public List<Device> findPartnerUnBindingVenderPartnerDevices(Page page, Device device, String startDevNo,
                                                                 String endDevNo) {
        return platformService.findVenderPartnerDevices(page, device, startDevNo, endDevNo, null);
    }

    /**
     * 解绑城市合伙人/网点设备
     *
     * @param devNos
     */
    @RequestMapping(value = "vender/saveUnBindDevices.json", method = RequestMethod.POST)
    public void saveVenderUnBindDevices(String[] devNos) {
        platformService.saveVenderpartnerUnBindDevices(devNos);
    }

    @RequestMapping(value = "partner/saveUnBindDevices.json", method = RequestMethod.POST)
    public void savePartnerUnBindDevices(String[] devNos) {
        platformService.saveVenderpartnerUnBindDevices(devNos);
    }

    /**
     * 导入设备信息 功能入口：设备管理-》导入设备
     *
     * @param device
     * @param key
     * @param fileIds
     */
    @RequestMapping(value = "device/importDevices.json", method = RequestMethod.POST)
    public void importDevices(Device device, long key, String[] fileIds) {
        try {
            platformService.saveDevices(device, key, fileIds);
        } catch (Exception e) {
            // 删掉表中数据
            platformService.deleteFile(-1 * key, Commons.FILE_EXCEL);
            logger.error(e.getMessage());
            if (e instanceof BusinessException)
                throw new BusinessException(e.getMessage());
            throw new BusinessException("导入失败！");
        }
    }

    /**
     * 查询设备信息
     */
    @RequestMapping(value = "vender/findBindingDevices.json", method = RequestMethod.POST)
    @ResponseBody
    public List<Device> findBindingDevices(Long orgId) {
        return platformService.findBindingDevices(orgId);
    }

    /**
     * 查询绑定设备
     */
    @RequestMapping(value = "partner/findBindingDevices.json", method = RequestMethod.POST)
    @ResponseBody
    public List<Device> findPartnerBindingDevices(Long orgId) {
        return platformService.findBindingDevices(orgId);
    }

    /**
     * 保存城市合伙人信息
     *
     * @param org
     */
    @RequestMapping(value = "vender/save.json", method = RequestMethod.POST)
    public void saveCityPartner(Orgnization org, String devNos) {
        org.setSort("1");
        org.setState(0);
        User user = ContextUtil.getUser(User.class);
        org.setParentId(user.getOrgId());

        String[] devNosArr =
                {};
        if (!StringUtils.isEmpty(devNos))
            devNosArr = devNos.split(",");
        systemService.saveOrgnization(org, devNosArr);
    }

    /**
     * 删除城市合伙人信息
     *
     * @param ids
     */
    @RequestMapping(value = "vender/delete.json", method = RequestMethod.POST)
    public void deleteCityPartners(Long[] ids) {
        systemService.deleteOrgnizations(ids);
    }

    /**
     * 分页查询网点信息
     *
     * @param page
     * @param orgnization
     * @param startDate
     * @param endDate
     * @return
     */
    @RequestMapping(value = "partner/find.json", method = RequestMethod.POST)
    @ModelAttribute("rows")
    public List<Orgnization> findDotNet(Page page, Orgnization orgnization, Date startDate, Date endDate) {
        User user = ContextUtil.getUser(User.class);
        orgnization.setParentId(user.getOrgId());
        return systemService.findOrgnizations(page, orgnization, startDate, endDate);
    }

    /**
     * 保存网点信息
     *
     * @param org
     */
    @RequestMapping(value = "partner/save.json", method = RequestMethod.POST)
    public void saveDotNet(Orgnization org, String devNos) {
        org.setSort("1");
        org.setState(0);
        User user = ContextUtil.getUser(User.class);
        org.setParentId(user.getOrgId());

        String[] devNosArr =
                {};
        if (!StringUtils.isEmpty(devNos))
            devNosArr = devNos.split(",");
        systemService.saveOrgnization(org, devNosArr);
    }

    /**
     * 删除城市合伙人信息
     *
     * @param ids
     */
    @RequestMapping(value = "partner/delete.json", method = RequestMethod.POST)
    public void deleteDotNets(Long[] ids) {
        systemService.deleteOrgnizations(ids);
    }

    /**
     * 查询设备信息 功能入口: 店铺信息管理-》设备管理
     *
     * @param page
     * @param device
     * @return
     */
    @RequestMapping(value = "device/find.json", method = RequestMethod.POST)
    @ModelAttribute("rows")
    public List<Device> findDevice(Page page, Device device) {
        return platformService.findDevices(page, device);
    }

    /**
     * 保存设备信息
     *
     * @param device
     */
    @RequestMapping(value = "device/save.json", method = RequestMethod.POST)
    public void saveDevice(Device device) {
        device.setState(Commons.NORMAL);
        platformService.saveDevice(device);
    }

    /**
     * 删除设备信息 功能入口 : 设备管理-》删除按钮
     *
     * @param ids
     */
    @RequestMapping(value = "device/delete.json", method = RequestMethod.POST)
    public void deleteDevices(Long[] ids) {
        platformService.deleteDevices(ids);
    }

    /**
     * 查询设备产品信息
     *
     * @param page
     * @param deviceAisle
     * @return
     */
    @RequestMapping(value = "device/findDetail.json", method = RequestMethod.POST)
    @ModelAttribute("rows")
    public List<DeviceAisle> findSellerDevice(Page page, DeviceAisle deviceAisle) {
        return platformService.findSellerDevice(page, deviceAisle);
    }

    @RequestMapping(value = "/findDevices.json")
    @ResponseBody
    public List<Device> findDevice() {
        return platformService.findDevices();
    }

    /**
     * 跳转到设备监控页面
     *
     * @return
     */
    @RequestMapping(value = "monitoring/forward.do", method = RequestMethod.GET)
    public ModelAndView forwardDeviceMonitoring() {
        ModelAndView view = new ModelAndView("/store/deviceMonitoring.jsp");
        User user = ContextUtil.getUser(User.class);
        view.addObject("_orgName", user.getOrgName());
        view.addObject("_orgId", user.getOrgId());
        return view;
    }

    /**
     * 获取设备监控列表
     *
     * @param device
     * @param monitoringType 设备监控类型(0:丢包率异常,1:CPU负载率异常)
     * @return
     */
    @RequestMapping(value = "monitoring/findDeviceMonitoringPage.json")
    @ModelAttribute("rows")
    public List<Device> findDeviceMonitoringPage(Device device, Page page, Integer monitoringType) {
        return monitoringService.findDeviceMonitoringPage(device, page, monitoringType);
    }

    /**
     * 查询设备详情
     *
     * @param factoryDevNo 设备组号
     * @return
     */
    @RequestMapping(value = "monitoring/findDeviceMonitoringDateils.json")
    public DeviceMonitoring findDeviceMonitoringDateils(String factoryDevNo) {
        return monitoringService.findDeviceMonitoringDateils(factoryDevNo);
    }

    /**
     * (5分钟)定时获取网络数据
     *
     * @param factoryDevNo
     * @param startTime
     * @param endTime
     * @return
     */
    @RequestMapping(value = "monitoring/findDeviceMoitoringNetworkList.json")
    public List<DeviceMonitoringNetwork> findDeviceMoitoringNetworkList(String factoryDevNo, Date startTime,
                                                                        Date endTime) {
        return monitoringService.findDeviceMoitoringNetworkList(factoryDevNo, startTime, endTime);
    }

    /**
     * 查询设备监控日志列表
     *
     * @param factoryDevNo
     * @param logType      0：日常日志，1：异常日志.
     * @return
     */
    @RequestMapping(value = "monitoring/findDeviceMonitoringAndDeviceLog.json")
    @ModelAttribute("rows")
    public List<DeviceMonitoring> findDeviceMonitoringAndDeviceLog(String factoryDevNo, Integer logType) {
        return monitoringService.findDeviceMonitoringAndDeviceLog(factoryDevNo, logType);
    }

    /**
     * 导出异常信息
     * @param exceptionId
     */
    @RequestMapping(value = "monitoring/findExportExceptionLog.json")
    public ModelAndView findExportExceptionLog(Long exceptionId){
        return new ModelAndView(monitoringService.findExportExceptionLog(exceptionId));
    }

    /**
     * 获取丢包率异常设备 CPU负载率异常设备的数量
     *
     * @return
     */
    @RequestMapping(value = "monitoring/findDetailsAndNetworkCount.json")
    public DeviceMonitoring findDetailsAndNetworkCount() {
        return monitoringService.findDetailsAndNetworkCount();
    }

    /**
     * 更新设备组日志文件
     *
     * @param factoryDevNo
     * @return
     */
    @RequestMapping(value = "monitoring/updateMonitoring.json")
    public ResultBase updateMonitoring(String factoryDevNo) {
        ResultBase rb = new ResultBase();
        if (null == factoryDevNo) {
            rb.setResultMessage("参数不能为空");
            return rb;
        }

        monitoringService.updateMonitoring(factoryDevNo, rb);

        return rb;
    }

}
