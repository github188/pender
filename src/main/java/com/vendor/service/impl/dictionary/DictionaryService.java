/**
 * 
 */
package com.vendor.service.impl.dictionary;

import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ecarry.core.dao.IGenericDao;
import com.ecarry.core.dao.SQLUtils;
import com.ecarry.core.domain.Menu;
import com.ecarry.core.domain.SysLog;
import com.ecarry.core.domain.SysType;
import com.ecarry.core.domain.WebUploader;
import com.ecarry.core.util.IdWorker;
import com.ecarry.core.web.core.ContextUtil;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.vendor.po.Category;
import com.vendor.po.Country;
import com.vendor.po.Currency;
import com.vendor.po.FileStore;
import com.vendor.po.Orgnization;
import com.vendor.po.User;
import com.vendor.service.IDictionaryService;
import com.vendor.thirdparty.qiniu.QiniuResult;
import com.vendor.util.Commons;
import com.vendor.util.QiniuUtil;
import com.vendor.util.QrcodeUtil;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import net.sf.ehcache.search.Attribute;
import net.sf.ehcache.search.Direction;
import net.sf.ehcache.search.Query;
import net.sf.ehcache.search.Result;
import net.sf.ehcache.search.expression.EqualTo;

/**
 * @author dranson on 2015年3月13日
 */
@Service("dictionaryService")
public class DictionaryService implements IDictionaryService {

	private static final Logger logger = Logger.getLogger(DictionaryService.class);

	@Value("${system.user}")
	private String admin;

	@Value("${file.path}")
	private String rootPath;

	@Value("${system.type}")
	private int sysType;

	@Value("${db.update}")
	private Integer update;

	private Cache sysTypeCache;

	private Cache currencyCache;

	private Cache categoryCache;

	@Autowired
	private IdWorker idWorker;

	@Autowired
	private IGenericDao genericDao;

	private Timestamp systemTime;
	
	private boolean executeJob;
	
	@Override
	public void saveInitData() {
		String sql = "SELECT COUNT(*) FROM PG_STATIO_USER_TABLES WHERE RELNAME=?";
		if (genericDao.findSingle(Integer.class, sql, "sys_user") == 0) {
			genericDao.runScript("create.sql");
		} else if (update != null) {
			int updateTime = Integer.parseInt(Preferences.userRoot().get("trade_db_update", "0"));
			if (update.intValue() > updateTime) {
				genericDao.runScript("update.sql");
				Preferences.userRoot().put("trade_db_update", update.toString());
			}
		}
		addData();
		findDataToCache();
		logger.info("initialize system cache complete.");
	}

	@Override
	public void findDataToCache() {
		try {
			Timestamp curTime = new Timestamp(System.currentTimeMillis());
			List<SysType> sysTypes = findAllSysTypes(systemTime);
			for (SysType sysType : sysTypes) {
				sysTypeCache.remove(sysType.getId());
				Element element = new Element(sysType.getId(), sysType);
				sysTypeCache.put(element);
			}
			List<Currency> currencies = findCurrencies(systemTime);
			for (Currency currency : currencies) {
				currencyCache.remove(currency.getId());
				Element element = new Element(currency.getId(), currency);
				currencyCache.put(element);
			}
			List<Category> categories = findCategories(systemTime);
			for (Category category : categories) {
				categoryCache.remove(category.getId());
				if (category.getParentCode() == null)
					category.setParentCode("");
				Element element = new Element(category.getId(), category);
				categoryCache.put(element);
			}
			systemTime = curTime;
		} catch (Exception e) {
			logger.error(e);
		}
	}

	@Override
	public int deleteExpireFiles() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -1);
		Timestamp date = new Timestamp(cal.getTimeInMillis());
		List<String> smallPaths = new ArrayList<String>();
		List<String> realPaths = new ArrayList<String>();
		String sql = SQLUtils.getSelectSQLByInclude(FileStore.class, "smallPath,realPath,remote") + " WHERE INFO_ID<? AND CREATE_TIME<=?";
		List<FileStore> list = genericDao.findTs(FileStore.class, sql, 0, date);
		for (FileStore fileStore : list) {
			if (fileStore.getSmallPath() != null)
				smallPaths.add(fileStore.getSmallPath());
			if (fileStore.getRealPath() != null)
				realPaths.add(fileStore.getRealPath());
		}
		if (!smallPaths.isEmpty())
			QiniuUtil.batchDelete(smallPaths);
		if (!realPaths.isEmpty())
			QiniuUtil.batchDelete(realPaths);
		return list.size();
	}

	@Override
	public void executeSyncData() {
		if (!executeJob) {
			executeJob = true;
			try {
				findDataToCache();
				logger.info("synchronize system cache complete.");
			} catch (Exception e) {
				logger.error(e);
			}
			try {
				int count = deleteExpireFiles();
				if (count != 0)
					logger.info("delete " + count + " of expired files complete.");
			} catch (Exception e) {
				logger.error(e);
			}
			executeJob = false;
		}
	}

	private void checkDataByCache() {
		// Element element = orgCache.get(orgCache.getKeys().get(0));
		// if (element == null)
		// findDataToCache();
		// 使用持久化策略，JOB定时增量更新缓存的方式，不再依赖缓存失效机制
	}

	private List<SysType> findAllSysTypes(Timestamp curTime) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer(SQLUtils.getSelectSQL(SysType.class) + " WHERE ");
		if (curTime != null) {
			buf.append("(CREATE_TIME>=? OR UPDATE_TIME>=?) AND ");
			args.add(curTime);
			args.add(curTime);
		}
		buf.append("ORG_ID=? ORDER BY REF_ID,TYPE,CODE");
		args.add(Commons.ORG_HQ);
		return genericDao.findTs(SysType.class, buf.toString(), args.toArray());
	}

	@Override
	public String findDefaultPassword() {
		SysType sysType = findSysTypeByTypeAndCode("SYS_PARAM", "PASSWORD");
		if (sysType != null)
			return sysType.getValue();
		return "12345678";
	}

	@Override
	public int findDefaultRetryPassword() {
		SysType sysType = findSysTypeByTypeAndCode("SYS_PARAM", "RETRY_PASSWORD");
		try {
			if (sysType != null)
				return Integer.parseInt(sysType.getValue());
		} catch (Exception e) {}
		return 3;
	}

	@Override
	public int findDefaultBannerSchedule() {
		SysType sysType = findSysTypeByTypeAndCode("SYS_PARAM", "BANNER_SCHEDULE");
		try {
			if (sysType != null)
				return Integer.parseInt(sysType.getValue());
		} catch (Exception e) {}
		return 8;
	}
	
	public int findMinStock() {
		SysType sysType = findSysTypeByTypeAndCode("SYS_PARAM", "MIN_STOCK");
		try {
			if (sysType != null)
				return Integer.parseInt(sysType.getValue());
		} catch (Exception e) {}
		return 3;
	}

	@Override
	public int findMinCapacity() {
		SysType sysType = findSysTypeByTypeAndCode("SYS_PARAM", "MIN_CAPACITY");
		try {
			if (sysType != null)
				return Integer.parseInt(sysType.getValue());
		} catch (Exception e) {}
		return 30;
	}

	@Override
	public int findDefaultOrderTimeout() {
		SysType sysType = findSysTypeByTypeAndCode("SYS_PARAM", "ORDER_TIMEOUT");
		try {
			if (sysType != null)
				return Integer.parseInt(sysType.getValue());
		} catch (Exception e) {}
		return 60;
	}

	@Override
	public double findTaxMin() {
		SysType sysType = findSysTypeByTypeAndCode("SYS_PARAM", "TAX_MIN");
		try {
			if (sysType != null)
				return Double.parseDouble(sysType.getValue());
		} catch (Exception e) {}
		return 0d;
	}

	@Override
	public double findWxShare() {
		SysType sysType = findSysTypeByTypeAndCode("SYS_PARAM", "SHARING");
		try {
			if (sysType != null)
				return Double.parseDouble(sysType.getValue());
		} catch (Exception e) {}
		return 0.08d;
	}

	@Override
	public double findVendorWxShare() {
		SysType sysType = findSysTypeByTypeAndCode("SYS_PARAM", "VENDOR_SHARING");
		try {
			if (sysType != null)
				return Double.parseDouble(sysType.getValue());
		} catch (Exception e) {}
		return 0.02d;
	}

	@Override
	public String findPromoteUrl() {
		SysType sysType = findSysTypeByTypeAndCode("SYS_PARAM", "PROMOTE_URL");
		if (sysType != null)
			return sysType.getValue();
		return null;
	}

	private SysType findSysTypeByTypeAndCode(String type, String code) {
		checkDataByCache();
		Query query = sysTypeCache.createQuery();
		query.includeValues();
		query.addCriteria(new EqualTo("type", type).and(new EqualTo("code", code)));
		List<Result> list = query.execute().all();
		if (!list.isEmpty())
			return (SysType) list.get(0).getValue();
		return null;
	}

	@Override
	public List<SysType> findSysTypesByType(String type) {
		return findSysTypesByType(type, true);
	}

	@Override
	public List<SysType> findSysTypesByType(String type, boolean displayable) {
		checkDataByCache();
		List<SysType> sysTypes = new ArrayList<SysType>();
		Query query = sysTypeCache.createQuery();
		query.includeValues();
		query.addCriteria(new EqualTo("type", type));
		query.addOrderBy(new Attribute<Integer>("code"), Direction.ASCENDING);
		List<Result> list = query.execute().all();
		for (Result result : list) {
			SysType sysType = (SysType) result.getValue();
			if (displayable) {
				sysTypes.add(sysType);
			} else if (sysType.isDisplayabled()) {
				sysTypes.add(sysType);
			}
		}
		return sysTypes;
	}

	@Override
	public List<SysType> findSysTypes(Timestamp curTime) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer(SQLUtils.getSelectSQL(SysType.class) + " WHERE ");
		if (curTime != null) {
			buf.append("(CREATE_TIME>=? OR UPDATE_TIME>=?) AND ");
			args.add(curTime);
			args.add(curTime);
		}
		buf.append("DISPLAYABLE=? AND (ORG_ID=? OR ORG_ID=?) AND SYS_TYPE=? ORDER BY REF_ID,TYPE,CODE");
		args.add(true);
		args.add(Commons.ORG_HQ);
		args.add(ContextUtil.getUser(User.class).getCompanyId());
		args.add(sysType);
		return genericDao.findTs(SysType.class, buf.toString(), args.toArray());
	}

	@Override
	public List<Orgnization> findOrgnizations(Timestamp curTime) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer(SQLUtils.getSelectSQL(Orgnization.class));
		if (curTime != null) {
			buf.append(" WHERE (CREATE_TIME>=? OR UPDATE_TIME>=?)");
			args.add(curTime);
			args.add(curTime);
		}
		buf.append(" ORDER BY PARENT_ID,CODE");
		return genericDao.findTs(Orgnization.class, buf.toString(), args.toArray());
	}

	@Override
	public List<Country> findCountries(Timestamp curTime) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer(SQLUtils.getSelectSQL(Country.class));
		if (curTime != null) {
			buf.append(" WHERE (CREATE_TIME>=? OR UPDATE_TIME>=?)");
			args.add(curTime);
			args.add(curTime);
		}
		buf.append(" ORDER BY NAME_EN");
		return genericDao.findTs(Country.class, buf.toString(), args.toArray());
	}

	@Override
	public List<Currency> findCurrencies(Timestamp curTime) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer(SQLUtils.getSelectSQL(Currency.class));
		if (curTime != null) {
			buf.append(" WHERE (CREATE_TIME>=? OR UPDATE_TIME>=?)");
			args.add(curTime);
			args.add(curTime);
		}
		buf.append(" ORDER BY CODE");
		return genericDao.findTs(Currency.class, buf.toString(), args.toArray());
	}

	@Override
	public List<Category> findCategories(Timestamp curTime) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer(SQLUtils.getSelectSQL(Category.class));
		if (curTime != null) {
			buf.append(" WHERE (CREATE_TIME>=? OR UPDATE_TIME>=?)");
			args.add(curTime);
			args.add(curTime);
		}
		buf.append(" ORDER BY PARENT_ID,CODE");
		return genericDao.findTs(Category.class, buf.toString(), args.toArray());
	}

	@Override
	public String findSyncSysLog(Timestamp curTime) {
		if (curTime != null) {
			String sql = "SELECT STRING_AGG(MODULE, ',')  FROM SYS_LOG WHERE OPERATE=? AND TYPE=? AND (CREATE_TIME>=? OR UPDATE_TIME>=?)";
			return genericDao.findSingle(String.class, sql, new Object[] { Commons.LOG_OPT_DELETE, Commons.LOG_TYPE_SYN, curTime, curTime });
		}
		return null;
	}

	@Override
	public Currency findCurrencyByCode(String code) {
		Query query = currencyCache.createQuery();
		query.includeValues();
		query.addCriteria(new EqualTo("code", code));
		List<Result> list = query.execute().all();
		if (list.size() != 0)
			return (Currency) list.get(0).getValue();
		return null;
	}

	@Override
	public double findCurrencyRateByCode(String code) {
		checkDataByCache();
		Currency currency = findCurrencyByCode(code);
		if (currency != null)
			return currency.getRate();
		return 1;
	}

	@Override
	public Category findCategories(String parent, String code) {
		Query query = categoryCache.createQuery();
		query.includeValues();
		query.addCriteria(new EqualTo("code", parent).and(new EqualTo("parentCode", parent)));
		List<Result> list = query.execute().all();
		if (!list.isEmpty())
			return (Category) list.get(0).getValue();
		return null;
	}

	@Override
	public Category findCategories(String parent1, String parent2, String code) {
		Query query = categoryCache.createQuery();
		query.includeValues();
		query.addCriteria(new EqualTo("parentCode", parent1 + "-" + parent2).and(new EqualTo("code", code)));
		List<Result> list = query.execute().all();
		if (!list.isEmpty())
			return (Category) list.get(0).getValue();
		return null;
	}

	@Override
	public void saveSyncSysLog(Class<?> clazz) {
		if (clazz != null) {
			Timestamp current = new Timestamp(System.currentTimeMillis());
			User curUser = ContextUtil.getUser(User.class);
			String module = clazz.getSimpleName();
			String sql = SQLUtils.getSelectSQL(SysLog.class) + " WHERE MODULE=? AND OPERATE=? AND TYPE=? AND ORG_ID=?";
			SysLog sysLog = genericDao.findT(SysLog.class, sql, module, Commons.LOG_OPT_DELETE, Commons.LOG_TYPE_SYN, curUser.getCompanyId());
			if (sysLog == null) {
				sysLog = new SysLog();
				sysLog.setType(Commons.LOG_TYPE_SYN);
				sysLog.setModule(module);
				sysLog.setOperate(Commons.LOG_OPT_DELETE);
				sysLog.setOrgId(curUser.getCompanyId());
				sysLog.setCreateUser(curUser.getId());
				sysLog.setCreateTime(current);
				sysLog.setUpdateUser(curUser.getId());
				sysLog.setUpdateTime(current);
				genericDao.save(sysLog);
			} else {
				sysLog.setUpdateUser(curUser.getId());
				sysLog.setUpdateTime(current);
				genericDao.update(sysLog);
			}
		}
	}

	@Override
	public List<String> saveUploader(User user, WebUploader uploader) {
		List<String> paths = new ArrayList<String>();
		if (uploader.getFile() != null) {
			saveUploadFile(user, uploader.getFile(), uploader.getModule(), uploader.getKey(), paths);
		} else if (uploader.getFiles() != null) {
			for (MultipartFile multipartFile : uploader.getFiles())
				saveUploadFile(user, multipartFile, uploader.getModule(), uploader.getKey(), paths);
		}
		return paths;
	}

	private void saveUploadFile(User user, MultipartFile multipartFile, int module, long key, List<String> paths) {
		Timestamp curTime = new Timestamp(System.currentTimeMillis());
		String suffix = multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().lastIndexOf("."));
		String name = null;
		switch (module) {
			case Commons.FILE_VENDER:
				name = "vender";
				break;
			case Commons.FILE_CATEGORY:
				name = "category";
				break;
			case Commons.FILE_PRODUCT:
			case Commons.FILE_PRODUCT_DESC:
				name = "product";
				break;
			case Commons.FILE_YOUPIN:
				name = "youpin";
				break;
			case Commons.FILE_TEMPLATE:
			case Commons.FILE_TEMPLATE_DESC:
				name = "template";
				break;
			case Commons.FILE_DISCOVER:
			case Commons.FILE_DISCOVER_DESC:
				name = "discover";
				break;
			case Commons.FILE_BANNER:
				name = "banner";
				break;
			case Commons.FILE_DISCOVER_LOGO:
				name = "discover_logo";
				break;
			case Commons.FILE_SPREAD:
				name = "spread";
				break;
			case Commons.FILE_PROMOTE:
				name = "promote";
				break;
			case Commons.FILE_ARTICEL:
			case Commons.FILE_ARTICEL_DESC:
				name = "article";
				break;
			case Commons.FILE_QRCODE:
				name = "qrcode";
				break;
			case Commons.FILE_INFO:
				name = "info";
				break;
			case Commons.FILE_VENDER_BACKGROUD:
			    name = "venderBg";
			    break;
			case Commons.FILE_EXCEL:
				name = "excel";
				break;
			case Commons.FILE_ADVERTISE:
				name = "advertise";
				break;
			case Commons.FILE_ADVERTISE_TOP:
				name = "advertise_top";
				break;
			case Commons.FILE_ADVERTISE_BG:
				name = "advertise_bg";
				break;
			case Commons.FILE_ADVERTISE_BOTTOM:
				name = "advertise_bottom";
				break;
			case Commons.FILE_PRODUCT_DETAIL:
				name = "product_detail";
				break;
			default:
				name = "ecarry";
		}
		String path = user.getOrgCode() == null ? "other" : user.getOrgCode();
		String filePath = path + "/" + name + "/" + idWorker.nextId() + suffix;
		FileStore fileStore = new FileStore();
		fileStore.setType(module);
		fileStore.setInfoId(key * -1);
		fileStore.setName(multipartFile.getOriginalFilename());
		fileStore.setRealPath(filePath);
		fileStore.setCreateUser(user.getId());
		fileStore.setCreateTime(curTime);
		try {
			fileStore.setRemote(true);
			QiniuResult rs = QiniuUtil.upload(multipartFile.getBytes(), filePath);
			fileStore.setFileSize(multipartFile.getSize());
			fileStore.setThirdHash(rs.getHash());
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		genericDao.save(fileStore);
		paths.add(fileStore.getRealPath());
	}

	@Override
	public List<Integer> batchDelete(List<String> files) {
		return QiniuUtil.batchDelete(files);
	}

	@Override
	public List<Integer> batchDelete(String... files) {
		return QiniuUtil.batchDelete(files);
	}

	@Override
	public String generateQrcode(String orgCode, String data) {
		String fileName = orgCode + "/qrcode/" + idWorker.nextId() + ".png";
		Hashtable<Object, Object> hints = new Hashtable<Object, Object>();
		MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		try {
			BitMatrix bitMatrix = multiFormatWriter.encode(data, BarcodeFormat.QR_CODE, 1024, 1024, hints);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			QrcodeUtil.writeToStream(bitMatrix, "png", out);
			QiniuUtil.upload(out.toByteArray(), fileName);
		} catch (Exception e) {
			fileName = "";
			logger.error(e);
		}
		return fileName;
	}

	private void addData() {
		int count = genericDao.findSingle(Integer.class, "SELECT COUNT(*) FROM SYS_USER WHERE USERNAME=?", admin);
		if (count == 0) {
			Timestamp curTime = new Timestamp(System.currentTimeMillis());
			Orgnization org = new Orgnization();
			org.setName("系统平台");
			org.setCode("001");
			org.setSort("000001");
			org.setCompanyId(Long.valueOf(Commons.ORG_HQ));
			org.setState(0);
			org.setCreateUser(Long.valueOf(Commons.ORG_HQ));
			org.setCreateTime(curTime);
			genericDao.save(org);
			if (org.getId().intValue() != Commons.ORG_HQ) {
				genericDao.execute("UPDATE SYS_ORG SET ID=? WHERE ID=?", Commons.ORG_HQ, org.getId());
				org.setId(Long.valueOf(Commons.ORG_HQ));
			}

			User user = new User();
			user.setUsername("admin");
			user.setPassword(ContextUtil.getPassword("12345678"));
			user.setNickname("管理员");
			user.setOrgId(org.getId());
			user.setEmail("service@ziyoubang.cn");
			user.setEnable(true);
			user.setEditable(false);
			user.setCompanyId(org.getId());
			user.setCreateUser(1L);
			user.setCreateTime(curTime);
			user.initDefaultValue();
			genericDao.save(user);
			if (user.getId().intValue() != Commons.ORG_HQ)
				genericDao.execute("UPDATE SYS_USER SET ID=? WHERE ID=?", Commons.ORG_HQ, user.getId());

			Menu menu = new Menu();
			menu.setName("平台管理");
			menu.setSort("01");
			menu.setLimited(false);
			menu.setEnable(true);
			menu.setRights(0);
			menu.setSysType(sysType);
			genericDao.save(menu);
			Long id = menu.getId();
			menu = new Menu();
			menu.setName("城市合伙人");
			menu.setUrl("platform/vender");
			menu.setSort("0101");
			menu.setLimited(false);
			menu.setEnable(true);
			menu.setRights(
					ContextUtil.getRights(ContextUtil.ACCESS_FIND, ContextUtil.ACCESS_EDIT, ContextUtil.ACCESS_DELETE, ContextUtil.ACCESS_AUDIT, ContextUtil.ACCESS_UNAUDIT));
			menu.setParentId(id);
			menu.setSysType(sysType);
			genericDao.save(menu);
			menu = new Menu();
			menu.setName("网点管理");
			menu.setUrl("platform/partner");
			menu.setSort("0102");
			menu.setLimited(false);
			menu.setEnable(true);
			menu.setRights(ContextUtil.getRights(ContextUtil.ACCESS_FIND, ContextUtil.ACCESS_EDIT, ContextUtil.ACCESS_DELETE));
			menu.setParentId(id);
			menu.setSysType(sysType);
			genericDao.save(menu);
			menu = new Menu();
			menu.setName("设备管理");
			menu.setUrl("platform/device");
			menu.setSort("0103");
			menu.setLimited(false);
			menu.setEnable(true);
			menu.setRights(ContextUtil.getRights(ContextUtil.ACCESS_FIND, ContextUtil.ACCESS_EDIT, ContextUtil.ACCESS_DELETE));
			menu.setParentId(id);
			menu.setSysType(sysType);
			genericDao.save(menu);
			menu = new Menu();
			menu.setName("类目维护");
			menu.setUrl("platform/category");
			menu.setSort("0107");
			menu.setLimited(false);
			menu.setEnable(true);
			menu.setRights(ContextUtil.getRights(ContextUtil.ACCESS_FIND, ContextUtil.ACCESS_EDIT, ContextUtil.ACCESS_DELETE));
			menu.setParentId(id);
			menu.setSysType(sysType);
			genericDao.save(menu);

			menu = new Menu();
			menu.setName("统计报表");
			menu.setSort("02");
			menu.setLimited(false);
			menu.setEnable(true);
			menu.setRights(0);
			menu.setSysType(sysType);
			genericDao.save(menu);
			id = menu.getId();
			menu = new Menu();
			menu.setName("订单信息");
			menu.setUrl("report/order");
			menu.setSort("0201");
			menu.setLimited(false);
			menu.setEnable(true);
			menu.setRights(ContextUtil.getRights(ContextUtil.ACCESS_FIND, ContextUtil.ACCESS_EDIT, ContextUtil.ACCESS_AUDIT, ContextUtil.ACCESS_EXPORT));
			menu.setParentId(id);
			menu.setSysType(sysType);
			genericDao.save(menu);
			menu = new Menu();
			menu.setName("设备销量统计");
			menu.setUrl("report/device");
			menu.setSort("0202");
			menu.setLimited(false);
			menu.setEnable(true);
			menu.setRights(ContextUtil.getRights(ContextUtil.ACCESS_FIND));
			menu.setParentId(id);
			menu.setSysType(sysType);
			genericDao.save(menu);
			menu = new Menu();
			menu.setName("商品销量统计");
			menu.setUrl("report/product");
			menu.setSort("0203");
			menu.setLimited(false);
			menu.setEnable(true);
			menu.setRights(ContextUtil.getRights(ContextUtil.ACCESS_FIND));
			menu.setParentId(id);
			menu.setSysType(sysType);
			genericDao.save(menu);
			menu = new Menu();
			menu.setName("分成统计");
			menu.setUrl("share/count");
			menu.setSort("0204");
			menu.setLimited(false);
			menu.setEnable(true);
			menu.setRights(ContextUtil.getRights(ContextUtil.ACCESS_FIND));
			menu.setParentId(id);
			menu.setSysType(sysType);
			genericDao.save(menu);

			menu = new Menu();
			menu.setName("商品管理");
			menu.setSort("03");
			menu.setLimited(false);
			menu.setEnable(true);
			menu.setRights(0);
			menu.setSysType(sysType);
			genericDao.save(menu);
			id = menu.getId();
			menu = new Menu();
			menu.setName("商品发布");
			menu.setUrl("product/issue");
			menu.setSort("0301");
			menu.setLimited(false);
			menu.setEnable(true);
			menu.setRights(ContextUtil.getRights(ContextUtil.ACCESS_FIND, ContextUtil.ACCESS_EDIT, ContextUtil.ACCESS_DELETE));
			menu.setParentId(id);
			menu.setSysType(sysType);
			genericDao.save(menu);
			menu = new Menu();
			menu.setName("代理商品库");
			menu.setUrl("product/sale");
			menu.setSort("0302");
			menu.setLimited(false);
			menu.setEnable(true);
			menu.setRights(ContextUtil.getRights(ContextUtil.ACCESS_FIND, ContextUtil.ACCESS_EDIT, ContextUtil.ACCESS_DELETE));
			menu.setParentId(id);
			menu.setSysType(sysType);
			genericDao.save(menu);
			menu = new Menu();
			menu.setName("库存管理");
			menu.setUrl("product/inventory");
			menu.setSort("0303");
			menu.setLimited(false);
			menu.setEnable(true);
			menu.setRights(ContextUtil.getRights(ContextUtil.ACCESS_FIND, ContextUtil.ACCESS_EDIT));
			menu.setParentId(id);
			menu.setSysType(sysType);
			genericDao.save(menu);
			menu = new Menu();
			menu.setName("设备库存管理");
			menu.setUrl("product/device");
			menu.setSort("0304");
			menu.setLimited(false);
			menu.setEnable(true);
			menu.setRights(ContextUtil.getRights(ContextUtil.ACCESS_FIND, ContextUtil.ACCESS_EDIT));
			menu.setParentId(id);
			menu.setSysType(sysType);
			genericDao.save(menu);
			
			menu = new Menu();
			menu.setName("广告管理");
			menu.setSort("04");
			menu.setLimited(false);
			menu.setEnable(true);
			menu.setRights(0);
			menu.setSysType(sysType);
			genericDao.save(menu);
			id = menu.getId();
			menu = new Menu();
			menu.setName("屏保广告管理");
			menu.setUrl("interaction/advertise");
			menu.setSort("0401");
			menu.setLimited(false);
			menu.setEnable(true);
			menu.setRights(ContextUtil.getRights(ContextUtil.ACCESS_FIND, ContextUtil.ACCESS_EDIT, ContextUtil.ACCESS_DELETE));
			menu.setParentId(id);
			menu.setSysType(sysType);
			genericDao.save(menu);
			
			menu = new Menu();
			menu.setName("资金管理");
			menu.setSort("08");
			menu.setLimited(false);
			menu.setEnable(true);
			menu.setRights(0);
			menu.setSysType(sysType);
			genericDao.save(menu);
			id = menu.getId();
			menu = new Menu();
			menu.setName("资金统计");
			menu.setUrl("fund/fundStatic");
			menu.setSort("0801");
			menu.setLimited(false);
			menu.setEnable(true);
			menu.setRights(ContextUtil.getRights(ContextUtil.ACCESS_FIND, ContextUtil.ACCESS_EDIT, ContextUtil.ACCESS_DELETE));
			menu.setParentId(id);
			menu.setSysType(sysType);
			genericDao.save(menu);

			menu = new Menu();
			menu.setName("系统管理");
			menu.setSort("09");
			menu.setLimited(false);
			menu.setEnable(true);
			menu.setRights(0);
			menu.setSysType(sysType);
			genericDao.save(menu);
			id = menu.getId();
			menu = new Menu();
			menu.setName("类型参数");
			menu.setUrl("system/type");
			menu.setSort("0901");
			menu.setLimited(false);
			menu.setEnable(true);
			menu.setRights(ContextUtil.getRights(ContextUtil.ACCESS_FIND, ContextUtil.ACCESS_EDIT, ContextUtil.ACCESS_DELETE));
			menu.setParentId(id);
			menu.setSysType(sysType);
			genericDao.save(menu);
			menu = new Menu();
			menu.setName("权限资源");
			menu.setUrl("system/menu");
			menu.setSort("0991");
			menu.setLimited(true);
			menu.setEnable(true);
			menu.setRights(ContextUtil.getRights(ContextUtil.ACCESS_FIND, ContextUtil.ACCESS_EDIT, ContextUtil.ACCESS_DELETE));
			menu.setParentId(id);
			menu.setSysType(sysType);
			genericDao.save(menu);
			menu = new Menu();
			menu.setName("资源级别");
			menu.setUrl("system/level");
			menu.setSort("0992");
			menu.setLimited(true);
			menu.setEnable(true);
			menu.setRights(ContextUtil.getRights(ContextUtil.ACCESS_FIND, ContextUtil.ACCESS_EDIT, ContextUtil.ACCESS_DELETE));
			menu.setParentId(id);
			menu.setSysType(sysType);
			genericDao.save(menu);
			menu = new Menu();
			menu.setName("机构管理");
			menu.setUrl("system/org");
			menu.setSort("0993");
			menu.setLimited(false);
			menu.setEnable(false);
			menu.setRights(ContextUtil.getRights(ContextUtil.ACCESS_EDIT, ContextUtil.ACCESS_DELETE));
			menu.setParentId(id);
			menu.setSysType(sysType);
			genericDao.save(menu);

			menu = new Menu();
			menu.setName("用户管理");
			menu.setUrl("system/user");
			menu.setSort("0994");
			menu.setLimited(false);
			menu.setEnable(true);
			menu.setRights(ContextUtil.getRights(ContextUtil.ACCESS_FIND, ContextUtil.ACCESS_EDIT, ContextUtil.ACCESS_DELETE));
			menu.setParentId(id);
			menu.setSysType(sysType);
			genericDao.save(menu);

			menu = new Menu();
			menu.setName("权限管理");
			menu.setUrl("system/access");
			menu.setSort("0995");
			menu.setLimited(false);
			menu.setEnable(true);
			menu.setRights(ContextUtil.getRights(ContextUtil.ACCESS_FIND, ContextUtil.ACCESS_EDIT, ContextUtil.ACCESS_DELETE));
			menu.setParentId(id);
			menu.setSysType(sysType);
			genericDao.save(menu);

			menu = new Menu();
			menu.setName("系统任务");
			menu.setUrl("system/job");
			menu.setSort("0996");
			menu.setLimited(true);
			menu.setEnable(true);
			menu.setRights(ContextUtil.getRights(ContextUtil.ACCESS_FIND, ContextUtil.ACCESS_EDIT, ContextUtil.ACCESS_DELETE, ContextUtil.ACCESS_EXECUTE));
			menu.setParentId(id);
			menu.setSysType(sysType);
			genericDao.save(menu);
			menu = new Menu();
			menu.setName("版本说明");
			menu.setUrl("system/version");
			menu.setSort("0997");
			menu.setLimited(false);
			menu.setEnable(true);
			menu.setRights(ContextUtil.getRights(ContextUtil.ACCESS_FIND));
			menu.setParentId(id);
			menu.setSysType(sysType);
			genericDao.save(menu);

			menu = new Menu();
			menu.setName("帮助");
			menu.setUrl("system/help");
			menu.setSort("0998");
			menu.setLimited(false);
			menu.setEnable(true);
			menu.setRights(ContextUtil.getRights(ContextUtil.ACCESS_FIND));
			menu.setParentId(id);
			menu.setSysType(sysType);
			genericDao.save(menu);

			String sql = "INSERT INTO SYS_TYPE(TYPE,CODE,NAME,VALUE,REF_ID,SYS_TYPE,DISPLAYABLE,EDITABLE,ORG_ID,CREATE_USER,CREATE_TIME) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			genericDao.execute(sql, "SYS_TYPE", "SYS_PARAM", "系统参数", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "SYS_TYPE", "ORG_TYPE", "机构类型", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "SYS_TYPE", "VENDER_TYPE", "商家类型", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "SYS_TYPE", "DEVICE_TYPE", "设备性质", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "SYS_TYPE", "DEVICE_STATE", "设备状态", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "SYS_TYPE", "PROMOTE_TYPE", "活动类型", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "SYS_TYPE", "PROMOTE_STATE", "活动状态", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "SYS_TYPE", "LINK_TYPE", "链接类型", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "SYS_TYPE", "TEMPLATE_TYPE", "模板类型", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "SYS_TYPE", "PRODUCT_STATE", "商品状态", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "SYS_TYPE", "PRODUCT_APPLY", "商品报名状态", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "SYS_TYPE", "ORDER_STATE", "订单状态", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "SYS_TYPE", "COUPONE_TYPE", "优惠券类型", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "SYS_TYPE", "COUPONE_STATE", "优惠券状态", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "SYS_TYPE", "ARTICLE_TYPE", "文章分类", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "SYS_TYPE", "DISCOVER_TYPE", "发现分类", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "SYS_TYPE", "EXPRESS_TYPE", "承运商", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "SYS_PARAM", "PASSWORD", "默认密码", "12345678", null, sysType, false, true, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "SYS_PARAM", "RETRY_PASSWORD", "重试次数", "6", null, sysType, false, true, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "SYS_PARAM", "BANNER_SCHEDULE", "BANNER切换时间", "8", null, sysType, false, true, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "SYS_PARAM", "ORDER_TIMEOUT", "订单超时", "60", null, sysType, false, true, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "SYS_PARAM", "PROMOTE_URL", "活动地址", "http://data.ziyoubang.cn", null, sysType, false, true, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "SYS_PARAM", "MIN_STOCK", "安全库存", "3", null, sysType, false, true, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "SYS_PARAM", "MIN_PRODUCT", "最低代理商品数", "8", null, sysType, false, true, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "ORG_TYPE", "1", "平台", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "ORG_TYPE", "2", "城市合伙人", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "ORG_TYPE", "3", "网点加盟商", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "DEVICE_TYPE", "1", "自营", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "DEVICE_TYPE", "2", "加盟", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "DEVICE_STATE", "0", "正常", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "DEVICE_STATE", "1", "缺货", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "DEVICE_STATE", "2", "故障", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "VENDER_TYPE", "0", "非签约", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "VENDER_TYPE", "1", "签约", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "VENDER_TYPE", "2", "线上", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "PROMOTE_TYPE", "1", "满邮", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "PROMOTE_TYPE", "2", "满减", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "PROMOTE_TYPE", "3", "打折", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "PROMOTE_STATE", "1", "待审核", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "PROMOTE_STATE", "2", "审核失败", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "PROMOTE_STATE", "3", "已审核", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "PROMOTE_STATE", "4", "报名中", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "PROMOTE_STATE", "5", "报名结束", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "PROMOTE_STATE", "6", "已上线", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "PROMOTE_STATE", "7", "活动结束", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "LINK_TYPE", "1", "商家", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "LINK_TYPE", "2", "商品", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "LINK_TYPE", "3", "URL", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "TEMPLATE_TYPE", "1", "推广活动", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "TEMPLATE_TYPE", "2", "运营活动", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "PRODUCT_APPLY", "1", "待审核", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "PRODUCT_APPLY", "2", "已通过", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "PRODUCT_APPLY", "3", "已驳回", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "PRODUCT_STATE", "1", "已上架", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "PRODUCT_STATE", "2", "已下架", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "PRODUCT_STATE", "3", "待审核", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "PRODUCT_STATE", "9", "已删除", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			// genericDao.execute(sql, "ORDER_STATE", "0", "主订单", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "ORDER_STATE", "1", "待付款", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "ORDER_STATE", "2", "已取消", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "ORDER_STATE", "3", "已关闭", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "ORDER_STATE", "4", "付款中", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "ORDER_STATE", "5", "待发货", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "ORDER_STATE", "6", "自提货", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "ORDER_STATE", "7", "已发货", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "ORDER_STATE", "8", "已完成", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "COUPONE_TYPE", "1", "抵邮券", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "COUPONE_TYPE", "2", "满减券", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "COUPONE_TYPE", "3", "现金券", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "COUPONE_STATE", "0", "未使用", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "COUPONE_STATE", "1", "已过期", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "COUPONE_STATE", "2", "使用中", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "COUPONE_STATE", "3", "已使用", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "ARTICLE_TYPE", "1", "值得买", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "ARTICLE_TYPE", "2", "玩什么", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "ARTICLE_TYPE", "3", "住哪里", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "DISCOVER_TYPE", "1", "去过", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "DISCOVER_TYPE", "2", "发现", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "EXPRESS_TYPE", "SHUNFENG", "顺丰速递", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.execute(sql, "EXPRESS_TYPE", "YUANTONG", "圆通速递", null, null, sysType, true, false, Commons.ORG_HQ, "system", curTime);
			genericDao.runScript("data.sql");
		}
	}

	@Override
	public String getFileServer() {
		return "http://ecarry.qiniudn.com/";
	}

	public void setSysTypeCache(Cache sysTypeCache) {
		this.sysTypeCache = sysTypeCache;
	}

	public void setCurrencyCache(Cache currencyCache) {
		this.currencyCache = currencyCache;
	}

	public void setCategoryCache(Cache categoryCache) {
		this.categoryCache = categoryCache;
	}

}
