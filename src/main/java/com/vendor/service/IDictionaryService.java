/**
 * 
 */
package com.vendor.service;

import java.sql.Timestamp;
import java.util.List;

import com.ecarry.core.domain.SysType;
import com.ecarry.core.domain.WebUploader;
import com.vendor.po.Category;
import com.vendor.po.Country;
import com.vendor.po.Currency;
import com.vendor.po.Orgnization;
import com.vendor.po.User;


/**
 * @author dranson on 2014-08-09
 *
 */
public interface IDictionaryService {
	/**
	 * 初始化数据
	 */
	void saveInitData();
	/**
	 * 同步基础数据
	 */
	void findDataToCache();
	/**
	 * 清除无效上传图片
	 * @return	清除的文件数量
	 */
	int deleteExpireFiles();
	/**
	 * 同步基础数据定时任务
	 */
	void executeSyncData();
	/**
	 * 获取默认密码
	 * @return	获取默认密码
	 */
	String findDefaultPassword();
	/**
	 * 获取默认密码重试次数
	 * @return	密码重试次数
	 */
	int findDefaultRetryPassword();
	/**
	 * 获取默认BANNER切换时间（秒）
	 * @return	BANNER切换时间（秒）
	 */
	int findDefaultBannerSchedule();

	/**
	 * 获取安全库存
	 * @return	
	 */
	int findMinStock();
	
	/**
	 * 获取默认容量
	 * **/
	int findMinCapacity();
	
	/**
	 * 获取默认订单超时分钟数
	 * @return	订单超时分钟数
	 */
	int findDefaultOrderTimeout();
	/**
	 * 获取最低征税额
	 * @return	最低征税额
	 */
	double findTaxMin();
	/**
	 * 获取微信分成比例
	 * @return	微信分成比例
	 */
	double findWxShare();
	/**
	 * 获取网点用户微信分成比例
	 * @return	网点用户微信分成比例
	 */
	double findVendorWxShare();
	/**
	 * 获取活动地址
	 * @return	活动地址
	 */
	String findPromoteUrl();
	/**
	 * 根据类型分类查询类型信息
	 * @param type	类型分类
	 * @return		类型信息
	 */
	List<SysType> findSysTypesByType(String type);
	/**
	 * 根据类型分类查询类型信息
	 * @param type	类型分类
	 * @param displayable	是否显示隐藏类型
	 * @return	类型信息
	 */
	List<SysType> findSysTypesByType(String type, boolean displayable);
	/**
	 * 根据时间戳查询该时间之后修改的系统类型数据
	 * @param curTime	查询时间
	 * @return		系统类型信息
	 */
	List<SysType> findSysTypes(Timestamp curTime);
	/**
	 * 根据时间戳查询该时间之后修改的机构数据
	 * @param curTime	查询时间
	 * @return		机构信息
	 */
	List<Orgnization> findOrgnizations(Timestamp curTime);
	/**
	 * 根据时间戳查询该时间之后修改的国家信息数据
	 * @param curTime	查询时间
	 * @return	国家信息
	 */
	List<Country> findCountries(Timestamp curTime);
	/**
	 * 根据时间戳查询该时间之后修改的币别信息数据
	 * @param curTime	查询时间
	 * @return	币别信息
	 */
	List<Currency> findCurrencies(Timestamp curTime);
	/**
	 * 根据时间戳查询该时间之后修改的产品分类数据
	 * @param curTime	查询时间
	 * @return	产品分类信息
	 */
	List<Category> findCategories(Timestamp curTime);
	/**
	 * 根据时间戳查询同步删除日志
	 * @param curTime
	 * @return	同步删除日志模块集，多个以","间隔
	 */
	String findSyncSysLog(Timestamp curTime);
	/**
	 * 根据币别代码查找币别信息
	 * @param code	币别代码
	 * @return	币别信息
	 */
	Currency findCurrencyByCode(String code);
	/**
	 * 根据币别代码查找币别兑人民币汇率
	 * @param code	币别代码
	 * @return	兑人民币汇率
	 */
	double findCurrencyRateByCode(String code);
	/**
	 * 根据所属类目与类目代码查找类目信息
	 * @param parent	所属类目代码
	 * @param code	类目代码
	 * @return	类目信息
	 */
	Category findCategories(String parent, String code);
	/**
	 * 根据所属类目与类目代码查找类目信息
	 * @param parent1	1级所属类目代码
	 * @param parent2	2级所属类目代码
	 * @param code	类目代码
	 * @return	类目信息
	 */
	Category findCategories(String parent1, String parent2, String code);
	/**
	 * 保存同步删除系统日志
	 * @param sysLog	需要保存的系统日志
	 */
	void saveSyncSysLog(Class<?> clazz);
	/**
	 * 客户端使用Web Uploader组件上传文件
	 * @param user	当前登录用户
	 * @param uploader	上传文件
	 * @return	文件路径集
	 */
	List<String> saveUploader(User user, WebUploader uploader);
	/**
	 * 批量删除文件
	 * @param files	需要删除的文件
	 * @return	失败的文件序号集
	 */
	List<Integer> batchDelete(List<String> files);
	/**
	 * 批量删除文件
	 * @param files	需要删除的文件
	 * @return	失败的文件序号集
	 */
	List<Integer> batchDelete(String... files);
	/**
	 * 生成二维码
	 * @param code	机构编码
	 * @param data	二维码数据
	 * @return	二维码图片路径
	 */
	String generateQrcode(String orgCode, String data);
	/**
	 * 获取文件服务器地址
	 * @return	文件服务器地址
	 */
	public String getFileServer();
}
