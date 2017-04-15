/**
 * 
 */
package com.vendor.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ecarry.core.domain.BoxValue;

/**
 * @author dranson on 2014年12月26日
 */
@Component
public class HttpAdapter {

	private static final Logger logger = Logger.getLogger(HttpAdapter.class);

	@Value("${http.maxTotal}")
	private int maxTotal = 10;

	@Value("${http.maxPerRoute}")
	private int maxPerRoute = 3;

	private CloseableHttpClient client;

	private CloseableHttpClient getHttpClient() {
		if (client != null)
			return client;
		try {
			SSLContext ctx = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(ctx);
			Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create().register("http", PlainConnectionSocketFactory.getSocketFactory())
					.register("https", sslsf).build();
			PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
			cm.setMaxTotal(maxTotal);
			cm.setDefaultMaxPerRoute(maxPerRoute);
			client = HttpClients.custom().setConnectionManager(cm).setSSLSocketFactory(sslsf).build();
			return client;
		} catch (Exception e) {
			logger.error("init httpclient error", e);
		}
		return null;
	}

	public String postData(String url, String body) {
		return postData(url, body, "UTF-8");
	}

	public String postData(String url, String body, String charset) {
		HttpPost post = new HttpPost(url);
		if (body != null)
			post.setEntity(new StringEntity(body, charset));
		if (charset == null)
			charset = "UTF-8";
		try {
			CloseableHttpResponse response = getHttpClient().execute(post);
			HttpEntity entity = response.getEntity();
			String content = EntityUtils.toString(entity, charset);
			EntityUtils.consume(entity);
			response.close();
			return content;
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}
	
	public String postJsonData(String url, String json) {
		return postJsonData(url, json, "UTF-8");
	}

	public String postJsonData(String url, String json, String charset) {
		HttpPost post = new HttpPost(url);
		
		if (null == json || "".equals(json))
			return null;
		
		StringEntity sEntity = new StringEntity(json, charset);
		sEntity.setContentType("application/json");
		post.setEntity(sEntity);
			
		if (charset == null)
			charset = "UTF-8";
		try {
			CloseableHttpResponse response = getHttpClient().execute(post);
			HttpEntity entity = response.getEntity();
			String content = EntityUtils.toString(entity, charset);
			EntityUtils.consume(entity);
			response.close();
			return content;
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}

	public String postData(String url, Map<String, Object> data) {
		return postData(url, data, "UTF-8");
	}

	public String postData(String url, Map<String, Object> data, String charset) {
		HttpPost post = new HttpPost(url);
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		if (data != null)
			for (String key : data.keySet())
				nvps.add(new BasicNameValuePair(key, String.valueOf(data.get(key))));
		if (charset == null)
			charset = "UTF-8";
		try {
			post.setEntity(new UrlEncodedFormEntity(nvps, charset));
			CloseableHttpResponse response = getHttpClient().execute(post);
			HttpEntity entity = response.getEntity();
			String content = EntityUtils.toString(entity, charset);
			EntityUtils.consume(entity);
			response.close();
			return content;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
		return null;
	}

	public String getData(String url) {
		return getData(url, null);
	}

	public String getData(String url, Map<String, String> headers) {
		HttpGet get = new HttpGet(url);
		if (headers != null)
			for (String key : headers.keySet())
				get.addHeader(key, headers.get(key));
		try {
			CloseableHttpResponse response = getHttpClient().execute(get);
			HttpEntity entity = response.getEntity();
			String content = EntityUtils.toString(entity, Consts.UTF_8);
			EntityUtils.consume(entity);
			response.close();
			return content;
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}

	public byte[] getDataByte(String url) {
		return getDataByte(url, null);
	}

	public byte[] getDataByte(String url, Map<String, String> headers) {
		HttpGet get = new HttpGet(url);
		if (headers != null)
			for (String key : headers.keySet())
				get.addHeader(key, headers.get(key));
		try {
			CloseableHttpResponse response = getHttpClient().execute(get);
			HttpEntity entity = response.getEntity();
			byte[] content = EntityUtils.toByteArray(entity);
			EntityUtils.consume(entity);
			response.close();
			return content;
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}

	public BoxValue<Header[], InputStream> getBoxValue(String url) {
		HttpGet get = new HttpGet(url);
		try {
			CloseableHttpResponse response = getHttpClient().execute(get);
			HttpEntity entity = response.getEntity();
			return new BoxValue<Header[], InputStream>(response.getAllHeaders(), entity.getContent());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}

	public void close() {
		try {
			if (client != null)
				client.close();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

}
