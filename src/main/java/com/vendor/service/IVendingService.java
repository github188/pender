package com.vendor.service;

import java.io.InputStream;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ecarry.core.web.core.Page;
import com.vendor.po.DeviceAisle;
import com.vendor.po.Order;
import com.vendor.po.ProductReplacement;
import com.vendor.po.ReplenishmentAppVersion;
import com.vendor.vo.app.CabinetVisiableStateInfo;
import com.vendor.vo.app.MachineStock;
import com.vendor.vo.app.ProductReplenishInfo;
import com.vendor.vo.app.QRCodeOrders;
import com.vendor.vo.app.QRCodeRequestInfo;
import com.vendor.vo.app.RecommendGoodsInfo;
import com.vendor.vo.app.StoreInfo;
import com.vendor.vo.app.SyncProductInfo;
import com.vendor.vo.app.SyncRoadSalesInfo;
import com.vendor.vo.common.ResultAppBase;
import com.vendor.vo.common.ResultBase;
import com.vendor.vo.common.ResultList;
import com.vendor.vo.web.Store;

public interface IVendingService {

    /**
     * 上传商品
     * ****/
    String uploadImag(String id,String filename,InputStream is);

    /**
     * 【设备APP接口】8.上传本机库存数据接口---将设备的当前每个货道的商品库存数据同步到服务器。
     *
     * @param machineStock 上传本机库存数据接收对象
     * @return
     */
    ResultBase saveUploadMachineStocks(ResultBase rb, MachineStock machineStock, Long lastUploadTime) throws Exception;

    /**
     * 【设备APP接口】9.微信扫码支付，模式二
     *
     * @param qrcodeInfo
     * @return 二维码信息和订单流水号
     */
    Map<String, Object> saveQRCodePay(Map<String, Object> map, QRCodeRequestInfo qrcodeInfo) throws Exception;

    /**
     * 【设备APP接口】9.扫码支付通知回调
     *
     * @return
     */
    boolean saveQRCodeAsyncNotify(String notityXml) throws Exception;

    /**
     * 微信扫码支付异步通知回调
     *
     */
    void saveWxAsyncNotify(Map<String, String> param) throws Exception;

    /**
     * 【设备APP接口】10.根据订单流水号查询交易状态的接口
     *
     * @param orderNo 订单流水号
     * @return
     */
    Map<String, Object> findOrderStatus(Map<String, Object> map, String orderNo) throws Exception;

    /**
     * 【设备APP接口】11.获取激活码接口
     *
     * @return
     */
    Map<String, Object> syncActiveCode(Map<String, Object> map) throws Exception;

    /**
     * 【设备APP接口】12.同步激活码状态接口
     *
     * @return
     */
    ResultBase syncActiveCodeState(ResultBase rb, String deviceNumber, String activeCode, Integer state) throws Exception;

    /**
     * 【设备APP接口】13.获取设备点位绑定状态接口
     *
     * @return
     */
    Map<String, Object> findDeviceBindState(Map<String, Object> map, String deviceNumber) throws Exception;

    /**
     * 【设备APP接口】17.接受APP上传过来的日志文件，并保存到服务器
     *
     * @return
     */
    ResultBase uploadLog(ResultBase rb, HttpServletRequest request, HttpServletResponse response) throws Exception;

    /**
     * 【设备APP接口】18.接受APP上传过来的异常信息，并保存到服务器
     *
     * @return
     */
    ResultBase uploadExceptionMsg(ResultBase rb, String deviceNumber, String version, String exception) throws Exception;

    /**
     * 【设备APP接口】21.微信公众号支付
     * @param orderNo
     * @return 预支付交易回话标识
     */
    Map<String, Object> unifiedorder(Map<String, Object> map, String orderNo, String openId) throws Exception;

    /**
     * 【设备APP接口】22.微信公众号支付通知回调
     *
     * @return
     */
    boolean saveOfficialAccountsAsyncNotify(String notityXml) throws Exception;

    /**
     * 【设备APP接口】23.下载订单商品信息
     *
     * @param orderNo 订单编号
     * @return
     */
    Map<String, Object> findSyncCartProductInfo(Map<String, Object> map, String orderNo) throws Exception;

    /**
     * 【设备APP接口】24.更改后台库存接口：设备出货指令成功后，通知后台更改库存
     * @param qrCodeOrders
     * @return
     */
    Map<String, Object> saveUploadVendorStock(Map<String, Object> map, QRCodeOrders qrCodeOrders) throws Exception;

    /**
     * 【设备APP接口】25.获取jsconfig参数信息
     *
     * @param url
     * @return
     */
    Map<String, Object> findJsConfig(Map<String, Object> map, String url, String openId) throws Exception;

    /**
     * 【设备APP接口】29.获取商品信息接口---将服务器端设置的，本机设备销售的商品数据下载到本地。
     * @param machineNum 设备组号
     * @return
     */
    ResultList<SyncProductInfo> findGoodsInfo(ResultList<SyncProductInfo> rl, String machineNum) throws Exception;

    /**
     * 【设备APP接口】31.货柜商品同步数据接口
     * @param machineNum 设备组号
     * @param cabinetNo 货柜号
     * @return
     */
    ResultList<SyncRoadSalesInfo> saveUpdataMachineCabGoods(ResultList<SyncRoadSalesInfo> rl, String machineNum, String cabinetNo) throws Exception;

    /**
     * 根据orders生成订单信息及订单号
     *
     * @param order
     * @param payType 6 微信 1支付宝
     * @return 订单流水号
     */
    String saveQRCodeOrders(QRCodeOrders qrCodeOrders, Integer payType) throws Exception;

    /**
     * 构建QRCodeOrders对象
     * @param machNo
     * @param prods
     * @return
     * @throws Exception
     */
    QRCodeOrders getQRCodeOrders(String machNo, String prods) throws Exception;

    /**
     * 【设备APP接口】32.同步换货商品数据接口
     * @return
     */
    ResultBase saveUpdateReplaceProducts(ResultBase rb, ProductReplacement replacement) throws Exception;

    /**
     * 【设备APP接口】2.店铺与设备绑定
     * @param rb
     * @param storeId
     * @param deviceNumber
     * @param deviceID
     * @param latitude
     * @param longtitude
     * @return
     */
    ResultBase updateRegisterDevice(ResultBase rb, String storeId, String deviceNumber);

    ResultBase queryDeviceBindState(ResultBase rb, String deviceNumber);

    /**
     * 【设备APP接口】33.上传机器阿里推送设备ID接口
     *
     * @return
     */
    ResultBase savcePushDeviceId(ResultBase rb, String machineNum, String deviceId) throws Exception;

    /**
     * 定时任务更新设备的状态
     * @throws Exception
     */
    void saveDeviceStateJob() throws Exception;

    /**
     * 校验登录用户的合法性
     * @param userId
     * @param deviceNumber
     * @param map
     * @return
     */
    Map<String, Object> authorizeUserAndDevice(Long userId, String deviceNumber, Map<String, Object> map);

    /**
     * 修改密码
     * @param oldPassword	旧密码
     * @param password	新密码
     */
    ResultAppBase savePassword(ResultAppBase rab, String oldPassword, String password);

    /**
     * 【补货APP接口】5.查询店铺信息(补货/全部)
     *
     * @param page 分页参数
     * @param time 时间参数  今天0，明天1
     * @param type 查询类型   1：待补货的店铺  2：全部店铺
     * @param prov 省
     * @param city 市
     * @param dist 区
     * @return
     */
    ResultAppBase<List<StoreInfo>> findStores(ResultAppBase<List<StoreInfo>> rab, Page page, Integer time, Integer type, String prov, String city, String dist);

    /**
     * 【补货APP接口】6.查询店铺商品补货信息
     *
     * @param page 分页参数
     * @param storeNo 区
     * @return
     */
    ResultAppBase<List<ProductReplenishInfo>> findReplenishProducts(ResultAppBase<List<ProductReplenishInfo>> rab, Page page, String factoryDevNo);

    /**
     * 【补货APP接口】7.获取APP最新版本
     *
     * @return
     */
    ResultAppBase<ReplenishmentAppVersion> findReplenishAppVersion(ResultAppBase<ReplenishmentAppVersion> rab);

    /**
     * 【设备APP接口】36.上传设备版本号
     *
     * @param deviceNumber 设备组号
     * @param version 版本号
     * @return
     */
    ResultBase syncDeviceVersion(ResultBase rb, String deviceNumber, String version);

    /**
     * 【设备APP接口】37.自动退款接口
     *
     * @param orderNo 订单编号
     * @param productNo 商品编号
     * @param qty 退货数量
     * @param activityNo 金蛋/银蛋/铜蛋编码（抽奖活动用）
     * @return
     */
    ResultBase saveRefund(ResultBase rb, String orderNo, String productNo, Integer qty, String activityNo) throws Exception;

    /**
     * 查询店铺信息
     * @return
     * @throws Exception
     */
    List<Store> findStores() throws Exception;

    /**
     * 查找所有需要闪烁的店铺
     * @return
     * @throws Exception
     */
    List<Store> saveStoreState(Timestamp lastUpdateTime) throws Exception;

    /**
     * 【地图】页面相关数据
     */
    Map<String, Object> findMapOperData();

    /**
     * 查询销售数据
     * @param startTime
     * @param endTime
     * @return
     */
    List<Order> findSalesData(Date startTime, Date endTime);
    
    /**
     * 【设备APP接口】40.根据设备查询货柜货道可见状态
     *
     * @param machineNum 设备编号
     * @return
     */
    ResultList<CabinetVisiableStateInfo> findCabinetVisiableState(ResultList<CabinetVisiableStateInfo> rl, String machineNum) throws Exception;
    
    /**
     * 【设备APP接口】41.获取推荐商品数据接口
     *
     * @param deviceNumber 设备编号
     * @return
     */
    ResultList<RecommendGoodsInfo> findRecommendGoods(ResultList<RecommendGoodsInfo> rl, String deviceNumber) throws Exception;
    
    /**
     * 获取推荐商品数据
     * @param deviceNumber
     * @param orgId
     * @param limitCount 取关联度前几位的推荐商品
     * @return
     */
    List<DeviceAisle> findRecommendGoodList(String deviceNumber, Long mainProductId, Long orgId, int limitCount);

    /************************演示数据**********开始***************/
    /**
     * 定时改变订单生成时间
     */
    void changeOrderMapJob();

    /**
     * 定时生成演示用订单数据
     */
    void createOrderJob();

    /**
     * 初始化设备销售数据
     */
    void initData();
    /************************演示数据**********结束***************/

    /**
     * 【设备APP接口】39.查询所有商品编码为负数的商品信息
     * @return
     */
    ResultList<SyncRoadSalesInfo> syncMinusProductNoGoods(ResultList<SyncRoadSalesInfo> rl) throws Exception;
}