package com.hb.it.imageserverclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;

import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.ObjectMetadata;

public class AliyunClient {

    private static final AliyunClient instance = new AliyunClient();

    private OSSClient client;
    private String accessKeyId;
    private String accessKeySecret;
    private String endpoint;
    private String bucketName;

    private AliyunClient() {
        ResourceBundle bundle = ResourceBundle.getBundle("com.hb.it.imageserverclient.FileServiceUtils");
        this.accessKeyId = StringUtils.isEmpty(bundle.getString("ALIYUN_ACCESSKEY_ID")) ? "zGbqsxgkCTmdcAr9"
                : bundle.getString("ALIYUN_ACCESSKEY_ID");
        this.accessKeySecret = StringUtils.isEmpty(bundle.getString("ALIYUN_ACCESSKEY_SECRET")) ? "hhJtXk8nAjbHC9XOK24cOrSbnpEDPj"
                : bundle.getString("ALIYUN_ACCESSKEY_SECRET");
        this.endpoint = StringUtils.isEmpty(bundle.getString("ALIYUN_SERVER_ENDPOINT")) ? "http://oss-cn-shanghai.aliyuncs.com/"
                : bundle.getString("ALIYUN_SERVER_ENDPOINT");
        this.bucketName = StringUtils.isEmpty(bundle.getString("ALIYUN_SERVER_BUCKET")) ? "laantoimages"
                : bundle.getString("ALIYUN_SERVER_BUCKET");

        init();
    }

    public void init() {
        ClientConfiguration conf = new ClientConfiguration();
        // 设置HTTP最大连接数为10
        conf.setMaxConnections(10);
        // 设置TCP连接超时为5000毫秒
        conf.setConnectionTimeout(5000);
        // 设置最大的重试次数为3
        conf.setMaxErrorRetry(3);
        // 设置Socket传输数据超时的时间为2000毫秒
        conf.setSocketTimeout(2000);

        client = new OSSClient(endpoint, accessKeyId, accessKeySecret, conf);
        validateBucket(bucketName);
    }

    private void validateBucket(String bucketName) {
        boolean isExist = client.doesBucketExist(bucketName);
        if (!isExist) {
            client.createBucket(bucketName);
        }
    }

    public static AliyunClient getInstance() {
        return instance;
    }

    public String putObject(String key, String filePath) {
        InputStream content = null;
        try {
            // 获取指定文件的输入流
            File file = new File(filePath);
            content = new FileInputStream(file);
            // 创建上传Object的Metadata
            ObjectMetadata meta = new ObjectMetadata();
            // 必须设置ContentLength
            meta.setContentLength(file.length());
            // 上传Object.
            client.putObject(bucketName, key, content, meta);
            return key;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            close(content);
        }
        return "";
    }

    public void deleteObject(String key) {
        client.deleteObject(bucketName, key);
    }

    public boolean doesObjectExist(String key) {
        return client.doesObjectExist(bucketName, key);
    }

    public String putObject(String key, InputStream inputStream, Long l) {
        try {
            // 创建上传Object的Metadata
            ObjectMetadata meta = new ObjectMetadata();
            // 必须设置ContentLength
            meta.setContentLength(l);
            // 上传Object.
            client.putObject(bucketName, key, inputStream, meta);
            return key;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            close(inputStream);
        }

    }

    /**
     * 上传文件
     *
     * @param key
     * @param inputStream
     * @param metadata
     * @return
     */
    public String putObject(String key, InputStream inputStream, ObjectMetadata metadata) {
        try {
            client.putObject(bucketName, key, inputStream, metadata);
            return key;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            close(inputStream);
        }
    }

    /**
     * @param inputStream
     */
    private void close(InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
