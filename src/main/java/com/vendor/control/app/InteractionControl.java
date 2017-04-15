package com.vendor.control.app;

import java.sql.Timestamp;
import java.util.List;

import com.vendor.po.*;
import com.vendor.thirdparty.tl.aipg.acctvalid.ValbSum;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.ecarry.core.exception.BusinessException;
import com.ecarry.core.web.control.BaseControl;
import com.ecarry.core.web.core.ContextUtil;
import com.ecarry.core.web.core.Page;
import com.vendor.service.IInteractionService;

/**
 * 用户互动
 * @author liujia on 2016年7月12日
 */
@Controller
@RequestMapping(value = "/interaction")
public class InteractionControl extends BaseControl {

	private static final Logger logger = Logger.getLogger(InteractionControl.class);

	@Autowired
	private IInteractionService interactionService;

	/**
	 * 跳转到广告管理页面
	 * @return
	 */
	@RequestMapping(value = "advertise/forward.do", method = RequestMethod.GET)
	public ModelAndView forwardScreen() {
		ModelAndView view = new ModelAndView("/interaction/advertise.jsp");
		User user = ContextUtil.getUser(User.class);
		view.addObject("_orgName", user.getOrgName());
		view.addObject("_orgId", user.getOrgId());
		return view;
	}
	
	/**
	 * 查找广告信息
	 * @param advertisement
	 * @param page
	 * @return
	 */
	@RequestMapping(value = "advertise/find.json", method = RequestMethod.POST)
	@ModelAttribute("rows")
	public List<Advertisement> findAdvertisement(Advertisement advertisement, Page page) {
		return interactionService.findAdvertisement(advertisement, page);
	}
	
	/**
	 * 保存广告信息
	 * @param advertisement
	 * @param key
	 * @param fileIds
	 * @param pointIds
	 */
	@RequestMapping(value = "advertise/save.json", method = RequestMethod.POST)
	public void saveScreen(Advertisement advertisement, long key, long[] fileIds, long[] pointIds) {
		try {
			interactionService.saveScreenAdv(advertisement, key, fileIds, pointIds);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			if (e instanceof BusinessException)
				throw new BusinessException(e.getMessage());
			throw new BusinessException("保存失败！");
		}
	}
	
	/**
	 * 删除广告信息
	 * @param ids
	 */
	@RequestMapping(value = "advertise/delete.json", method = RequestMethod.POST)
	public void deleteScreen(Long[] ids) {
		interactionService.deleteScreen(ids);
	}
	
	/**
	 * 更改广告的状态
	 */
	@RequestMapping(value = "advertise/saveAdvStatus.json", method = RequestMethod.POST)
	public void saveAdvStatus(Long advertiseId, Integer status) {
		interactionService.saveAdvStatus(advertiseId, status);
	}
	
	/**
	 * 查询店铺信息
	 */
	@RequestMapping(value = "advertise/findPointPlaces.json", method = RequestMethod.POST)
	public List<PointPlace> findPointPlaces(Long advertiseId) {
		return interactionService.findPointPlaces(advertiseId);
	}



	/********************************************* 主题皮肤 start ********************************************/

    /**
     * 跳转到主题管理页面
     * @return
     */
	@RequestMapping(value = "theme/forward.do")
	public ModelAndView forwardTheme(){
	    return new ModelAndView("/interaction/theme.jsp");
    }

	/**
	 * 保存主题皮肤(模板)
	 * @param themeSkin
	 */
	@RequestMapping(value = "theme/saveSkinEntity.json")
    public void saveThemeSkinEntity(ThemeSkin themeSkin){
		interactionService.saveThemeSkinEntity(themeSkin);
	}

    /**
     * 删除主题皮肤
     * @param themeId
     */
	@RequestMapping(value = "theme/deleteThemeSkin.json")
	public void deleteThemeSkin(Long themeId){
	    interactionService.deleteThemeSkin(themeId);
    }


    /**
     * 更改默认主题
     * @param themeId
     */
    @RequestMapping(value = "theme/updateThemeSkinDefaultTheme.json")
    public void updateThemeSkinDefaultTheme(Long themeId) {
        interactionService.updateThemeSkinDefaultTheme(themeId);
    }

    /**
	 * 保存设备主题关系
	 * @param themeSkins
	 * @param hThemeId
	 * @param vThemeId
	 * @param startTime
	 */
	@RequestMapping(value = "theme/saveThemeAndDivce.json")
	public void saveThemeSkinAndDevice(Integer[] themeDevices, Long hThemeId, Long vThemeId, Timestamp startTime){
		interactionService.saveThemeSkinAndDevice(themeDevices, hThemeId, vThemeId, startTime);
	}

	/**
	 * 主题实时推送
	 * @param factoryDevNo
	 */
	@RequestMapping(value = "theme/updateThemeRealTimePush")
	public void updateThemeRealTimePush(String factoryDevNo){
		interactionService.updateThemeRealTimePush(factoryDevNo);
	}

	/**
	 * 取消主题推送
	 * @param factoryDevNo
	 */
	@RequestMapping(value = "theme/updateCancelTheme.json")
	public void updateCancelTheme(String factoryDevNo){
		interactionService.updateCancelTheme(factoryDevNo);
	}

    /**
     * 查询出本机构的主题
     * @param page
     * @return
     */
    @RequestMapping(value = "theme/findThemeSkinList.json")
    @ModelAttribute("rows")
    public List<ThemeSkin> findThemeSkinList(Page page){
	    return interactionService.findThemeSkinList(page);
    }

    /**
     * 查询出机构的设备以及设备的主题
     * @param themeSkin
     * @param page
     * @return
     */
    @RequestMapping(value = "theme/findDeviceThemeSkinList.json")
	@ModelAttribute("rows")
	public List<ThemeDevice> updateAndFindDeviceThemeSkinList(ThemeDevice themeSkin, Page page){
        return interactionService.updateAndFindDeviceThemeSkinList(themeSkin, page);
    }

	/********************************************* 主题皮肤 end **********************************************/


	
}
