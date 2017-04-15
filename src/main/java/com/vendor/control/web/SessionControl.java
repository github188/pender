/**
 * 
 */
package com.vendor.control.web;

import java.io.FileInputStream;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.ecarry.core.domain.WebUploader;
import com.ecarry.core.web.control.BaseControl;
import com.ecarry.core.web.core.ContextUtil;
import com.ecarry.core.web.view.ByteView;
import com.ecarry.core.web.view.StringToImageView;
import com.vendor.po.User;
import com.vendor.service.IDictionaryService;
import com.vendor.service.ISystemService;

/**
 * 用于只要是登录状态即可访问的资源
 * @author dranson on  2015-9-18
 */
@Controller
@RequestMapping(value = "/session")
public class SessionControl extends BaseControl {
	
	@Autowired
	private IDictionaryService dictionaryService;
	
	@Autowired
	private ISystemService systemService;
	
	@Value("${file.path}")
	private String rootPath;
	
	@RequestMapping(value = "data/findSysTypes.json", method = RequestMethod.POST)
	@ResponseBody
	public String findSysTypes(Timestamp curTime) {
		return getJsonByExcept(dictionaryService.findSysTypes(curTime), "displayable,editable,createUser,createTime,updateUser,updateTime,remark");
	}
	
	@RequestMapping(value = "data/findOrgnizations.json", method = RequestMethod.POST)
	@ResponseBody
	public String findOrgnizations(Timestamp curTime) {
//		return getJsonByExcept(dictionaryService.findOrgnizations(curTime), "createUser,createTime,updateUser,updateTime,remark");
		return "[]";
	}
	
	@RequestMapping(value = "data/findCountrys.json", method = RequestMethod.POST)
	@ResponseBody
	public String findCountries(Timestamp curTime) {
		return getJsonByExcept(dictionaryService.findCountries(curTime), "createUser,createTime,updateUser,updateTime");
	}
	
	@RequestMapping(value = "data/findCurrencys.json", method = RequestMethod.POST)
	@ResponseBody
	public String findCurrencies(Timestamp curTime) {
		return getJsonByExcept(dictionaryService.findCurrencies(curTime), "createUser,createTime,updateUser,updateTime");
	}
	
	@RequestMapping(value = "data/findCategorys.json", method = RequestMethod.POST)
	@ResponseBody
	public String findCategories(Timestamp curTime) {
		return getJsonByExcept(dictionaryService.findCategories(curTime), "createUser,createTime,updateUser,updateTime");
	}
	
	@RequestMapping(value = "data/findSyncModules.json", method = RequestMethod.POST)
	@ResponseBody
	public String findSyncSysLog(Timestamp curTime) {
		return dictionaryService.findSyncSysLog(curTime);
	}
	
	@RequestMapping(value = "data/readImage.file", method = RequestMethod.GET)
	public ModelAndView readImage(String path) {
		ModelAndView view = new ModelAndView();
		try {
			ByteView outView = new ByteView(new FileInputStream(rootPath + "/" + path));
			outView.setContentType("image/*");
			outView.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
			outView.addHeader("Cache-Control", "post-check=0, pre-check=0");
			outView.setHeader("Pragma", "no-cache");
			int pos = path.lastIndexOf("/") + 1;
			outView.setHeader("Content-Disposition", "attachment;filename=" +  path.substring(pos));
			view.setView(outView);
		} catch (Exception e) {
			view.setView(new StringToImageView("图片不存在"));
		}
		return view;
	}

	@RequestMapping(value = "data/saveUploadFile.json", method = {RequestMethod.POST })
	public Map<String, Object> saveUploadFile(WebUploader uploader) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			List<String>paths = dictionaryService.saveUploader(ContextUtil.getUser(User.class), uploader);
			if (!paths.isEmpty())
				map.put("url", dictionaryService.getFileServer() + paths.get(0));
			map.put("error", 0);
		} catch (Exception e) {
			map.put("error", 1);
			map.put("message", e.getMessage());
		}
		return map;
	}

	@RequestMapping(value = "system/saveUserInfo.json", method = {RequestMethod.POST })
	public User saveUserInfo(User user, WebUploader uploader) {
		User curUser = ContextUtil.getUser(User.class);
		user.setId(curUser.getId());
		user.setPortrait(curUser.getPortrait());
		systemService.saveUserInfo(user, uploader);
		curUser.setNickname(user.getNickname());
		curUser.setPortrait(user.getPortrait());
		return user;
	}
}
