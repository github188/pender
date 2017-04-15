package com.vendor.util.alipusher;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.Properties;

import org.junit.BeforeClass;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;

/**
 * 推送的OpenAPI文档 https://help.aliyun.com/document_detail/mobilepush/api-reference/openapi.html
 */
public class Config {
    private static final String REGION_HANGZHOU = "cn-hangzhou";

    public static long appKey = Long.valueOf("23428104");
    public static String deviceIds;
    public static String accounts;
    public static String accessKeyId = "0zpE3D9xVmvMHAM6";
    public static String accessKeySecret = "gl9nV9Pax7G7UOYtqJc0AdPp2DaBVf";

    public static DefaultAcsClient client;
    
    static {
    	IClientProfile profile = DefaultProfile.getProfile(REGION_HANGZHOU, accessKeyId, accessKeySecret);
        client = new DefaultAcsClient(profile);
    }

    /**
     * 从配置文件中读取配置值，初始化Client
     * <p>
     * 1. 如何获取 accessKeyId/accessKeySecret/appKey 照见README.md 中的说明<br/>
     * 2. 先在 push.properties 配置文件中 填入你的获取的值
     */
    @BeforeClass
    public static void beforeClass() throws Exception {
        InputStream inputStream = Config.class.getClassLoader().getResourceAsStream("push.properties");
        Properties properties = new Properties();
        properties.load(inputStream);

        String accessKeyId = properties.getProperty("accessKeyId");
        assertNotNull("先在 push.properties 配置文件中配置 accessKeyId", accessKeyId);

        String accessKeySecret = properties.getProperty("accessKeySecret");
        assertNotNull("先在 push.properties 配置文件中配置 accessKeySecret", accessKeySecret);

        String key = properties.getProperty("appKey");
        assertNotNull("先在 push.properties 配置文件中配置 appKey", key);
        Config.appKey = Long.valueOf(key);

        deviceIds = properties.getProperty("deviceIds");
        accounts = properties.getProperty("accounts");

        IClientProfile profile = DefaultProfile.getProfile(REGION_HANGZHOU, accessKeyId, accessKeySecret);
        client = new DefaultAcsClient(profile);
    }
}

