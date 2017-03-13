package com.hb.it.imageserverclient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;

import com.hb.it.exception.UploadException;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.request.UploadFileRequest;
import com.qcloud.cos.sign.Credentials;

/**
 * 腾讯图服务
 * 
 * @author xiao.miao
 * @date 2017年2月6日
 */
public class QCloudClient {

	private static final QCloudClient instance = new QCloudClient();
	private static final String defaultBucketName = "bibibx";

	private int appId;
	private String secretId;
	private String secretKey;
	private String region;
	private String downCosEndPointDomain;
	private COSClient cosClient;
	private String buckentName;

	public QCloudClient() {
		ResourceBundle bundle = ResourceBundle.getBundle("com.hb.it.imageserverclient.FileServiceUtils");
		appId = Integer.valueOf(StringUtils.isNotBlank(bundle.getString("Q_COS_APP_ID"))
				? bundle.getString("Q_COS_APP_ID") : "1251492408");
		secretId = StringUtils.isNotBlank(bundle.getString("Q_SECRET_ID")) ? bundle.getString("Q_SECRET_ID")
				: "AKIDlNo3teU2UkzEVhyhVZaeXfprkSbgQGm6";
		secretKey = StringUtils.isNotBlank(bundle.getString("Q_SECRET_KEY")) ? bundle.getString("Q_SECRET_KEY")
				: "I7mGe5YrxrZ5Vlqw1vfDbt68C9vVt56v";
		region = StringUtils.isNotBlank(bundle.getString("Q_REGION")) ? bundle.getString("Q_REGION") : "sh";
		downCosEndPointDomain = StringUtils.isNotBlank(bundle.getString("DOWN_COS_ENDPOINT_DOMAIN"))
				? bundle.getString("DOWN_COS_ENDPOINT_DOMAIN") : "bibibx-1251492408.file.myqcloud.com";
		init();
	}

	public void init() {
		ClientConfig config = new ClientConfig();
		config.setRegion(region);
		// 设置HTTP最大连接数为10
		config.setMaxConnectionsCount(10);
		// 设置TCP连接超时为5000毫秒
		config.setConnectionTimeout(5000);
		// 设置最大的重试次数为3
		config.setMaxFailedRetry(3);
		// 设置Socket传输数据超时的时间为2000毫秒
		config.setSocketTimeout(2000);
		// 设置下载链接
		config.setDownCosEndPointDomain(downCosEndPointDomain);

		Credentials cred = new Credentials(appId, secretId, secretKey);
		cosClient = new COSClient(config, cred);
	}

	public static QCloudClient getInstance() {
		return instance;
	}

	public void setBucketName(String bucketName) {
		this.buckentName = bucketName;
	}

	/**
	 * 文件路径上传
	 * @param cosName
	 * @param filePath
	 * @return
	 * @throws Exception
	 */
	public String uploadFile(String cosPath, String filePath) throws Exception {
		// 设置要操作的bucket
		// 1、检查参数
		this.checkParams(cosPath, filePath);
		// 2、创建File
		File file = this.createFile(filePath);
		byte[] contentBuffer = this.getFileBytes(file);
		return uploadFile(cosPath, contentBuffer);
	}
	
	/**
	 * 文件流上传
	 * @param cosName
	 * @param contentBuffer
	 * @return
	 */
	public String uploadFile(String cosPath, FileInputStream in) throws Exception{
		byte[] contentBuffer = getBytes(in);
		return uploadFile(cosPath, contentBuffer);
	}
	
	/**
	 * 字节上传
	 * @param cosName
	 * @param contentBuffer
	 * @return
	 */
	public String uploadFile(String cosPath, byte[] contentBuffer){
		String bucketName = StringUtils.isNotBlank(this.buckentName) ? this.buckentName : defaultBucketName;
		UploadFileRequest request = new UploadFileRequest(bucketName, cosPath, contentBuffer);
		String result = cosClient.uploadFile(request);
		return result;
	}

	/**
	 * Gets the file bytes.
	 * 
	 * @param f
	 *            the f
	 * @return the file bytes
	 * @throws Exception
	 *             the exception
	 */
	private byte[] getFileBytes(File file) throws Exception {
		FileInputStream in = new FileInputStream(file);
		byte[] fileBytes = getBytes(in);
		in.close();
		return fileBytes;
	}

	/**
	 * 获取文件流中的字节
	 * 
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	private byte[] getBytes(InputStream inputStream) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] b = new byte[1024];
		int n;
		while ((n = inputStream.read(b)) != -1) {
			out.write(b, 0, n);
		}
		byte[] fileBytes = out.toByteArray();
		inputStream.close();
		out.close();
		return fileBytes;
	}

	/**
	 * @param filePath
	 * @return
	 * @throws UploadException
	 */
	private File createFile(String filePath) throws Exception {
		return new File(filePath);
	}

	/**
	 * 检查参数
	 * 
	 * @throws UploadException
	 */
	private void checkParams(String cosPath, String filePath) throws UploadException {
		this.checkCosPath(cosPath);
		this.checkFilePath(filePath);
	}

	private void checkCosPath(String cosPath) throws UploadException {
		if (StringUtils.isBlank(cosPath)) {
			throw new UploadException("输出文件名不能为空。");
		}
	}

	private void checkFilePath(String filePath) throws UploadException {
		if (StringUtils.isBlank(filePath)) {
			throw new UploadException("输出文件名不能为空。");
		}
	}

}
