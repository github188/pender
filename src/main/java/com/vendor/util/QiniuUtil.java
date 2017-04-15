/**
 * 
 */
package com.vendor.util;

import java.util.ArrayList;
import java.util.List;

import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.BatchStatus;
import com.qiniu.util.Auth;
import com.vendor.thirdparty.qiniu.QiniuResult;

/**
 * @author dranson on 2015年12月17日
 */
public abstract class QiniuUtil {
	
	private static UploadManager uploadManager;
	private static BucketManager bucketManager;
	private static Auth auth = Auth.create("_w8cWAE1MCJ-upRg3fH6_Mn24i4JylaW6xwikDPx", "S8WMsBKKVYgIEX29wvkkgXbaob1gjwGJ3aVwo4nt");
	private static String bucket = "ecarry";

	/**
	 * 上传文件
	 * @param data	需要上传的文件数据
	 * @param key	上传的文件名
	 * @return	上传的结果
	 */
	public static QiniuResult upload(byte[] data, String key) {
		synchronized (QiniuUtil.class) {
			if (uploadManager == null)
				uploadManager = new UploadManager();
		}
		try {
			Response response = uploadManager.put(data, key, getUpToken(key));
			if (response.statusCode != 200)
				throw new RuntimeException("qiniu upload failed.");
			return response.jsonToObject(QiniuResult.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * 批量删除文件
	 * @param keys	需要删除的文件名集
	 * @return	删除失败的文件序号
	 */
	public static List<Integer> batchDelete(List<String> keys) {
		String[] ary = new String[keys.size()];
		keys.toArray(ary);
		return batchDelete(ary);
	}
	/**
	 * 批量删除文件
	 * @param keys	需要删除的文件名集
	 * @return	删除失败的文件序号
	 */
	public static List<Integer> batchDelete(String... keys) {
		synchronized (QiniuUtil.class) {
			if (bucketManager == null)
				bucketManager = new BucketManager(auth);
		}
		List<Integer> args = new ArrayList<Integer>();
		try {
			BucketManager.Batch opts = new BucketManager.Batch().delete(bucket, keys);
			Response response = bucketManager.batch(opts);
			if (response.statusCode != 200 && response.statusCode != 298)
				throw new RuntimeException("qiniu delete failed.");
			if (response.statusCode == 298) {	//	部分成功，查找出失败的序号
				BatchStatus[] bs = response.jsonToObject(BatchStatus[].class);
			    for (int i = 0; i < bs.length; i++)
			    	if (bs[i].code != 200)
			    		args.add(i);
			}
		    return args;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * 删除单个文件
	 * @param key	需要删除的文件名
	 */
	public static void delete(String key) {
		synchronized (QiniuUtil.class) {
			if (bucketManager == null)
				bucketManager = new BucketManager(auth);
		}
		try {
			bucketManager.delete(bucket, key);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static String getUpToken(String key) {
	    return auth.uploadToken(bucket, key);
	}
}
