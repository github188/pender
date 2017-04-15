package com.vendor.service;

import java.io.File;
import java.sql.Timestamp;
import java.util.List;

import com.ecarry.core.web.core.Page;
import com.vendor.po.Advertisement;
import com.vendor.po.PointPlace;
import com.vendor.po.ThemeDevice;
import com.vendor.po.ThemeSkin;
import org.springframework.web.multipart.MultipartFile;

public interface IInteractionService {
	
	/**
	 * 屏保广告列表
	 ***/
	public List<Advertisement> findAdvertisement(Advertisement Advertisement, Page page);
	
	/**
	 * 保存屏保商品信息
	 ***/
	public void saveScreenAdv(Advertisement Advertisement, long key, long[] fileIds, long[] pointIds) throws Exception ;
	
	/**
	 * 删除屏保广告信息
	 ***/
	public void deleteScreen(Long[] ids);
	
	/**
	 * 删除广告文件
	 * @param infoId
	 * @param type
	 */
	void deleteFile(Long infoId, Integer type);
	
	/**
	 * 更改广告的状态
	 */
	public void saveAdvStatus(Long advertiseId, Integer status);

	/**
	 * 查询店铺信息
	 */
	public List<PointPlace> findPointPlaces(Long advertiseId);

	/**
	 * 保存主题皮肤
	 * @param themeSkin
	 */
	void saveThemeSkinEntity(ThemeSkin themeSkin);

	/**
	 * 保存设备主题关系
     * @param themeSkins
     * @param hTheme
     * @param vTheme
     * @param startTime
     */
	void saveThemeSkinAndDevice(Integer[] themeDevices, Long hTheme, Long vTheme, Timestamp startTime);

    /**
     * 查询本机构的主题
     * @param page
     * @return
     */
    List<ThemeSkin> findThemeSkinList(Page page);

    /**
     * 查询出机构的设备以及设备的主题
     *
     * @param themeSkin
     * @param page
     * @return
     */
	List<ThemeDevice> updateAndFindDeviceThemeSkinList(ThemeDevice themeSkin, Page page);

	/**
	 * 删除主题皮肤
	 * @param themeId
	 */
	void deleteThemeSkin(Long themeId);

	/**
	 * 更改默认主题
	 * @param themeId
	 */
	void updateThemeSkinDefaultTheme(Long themeId);

	/**
	 * 定时更新主题皮肤
	 */
	void updateThemeStateJob();

	/**
	 * 取消主题推送
	 * @param factoryDevNo
	 */
	void updateCancelTheme(String factoryDevNo);

	/**
	 * 主题实时推送
	 * @param factoryDevNo
	 */
	void updateThemeRealTimePush(String factoryDevNo);

	/**
	 * 更新主题设备状态
	 * @param machineNum 设备组号
	 * @param themeId 主题Id
	 * @param execuTingState 状态值
	 * @return
	 */
    void themeDeviceExecutingState(String machineNum, String themeId, Integer execuTingState);
}
