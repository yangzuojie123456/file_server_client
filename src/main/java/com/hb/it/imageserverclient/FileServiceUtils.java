package com.hb.it.imageserverclient;

import com.aliyun.oss.model.ObjectMetadata;
import com.hb.it.exception.UploadException;

import net.sf.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.ResourceBundle;

/**
 * The Class FileServiceUtils.
 */
public class FileServiceUtils {

    /**
     * The image server image bucket.
     */
    // ADD Task#从配置文件读取图片服务器路径 by xiao.miao 2015/01/27
    // Start------------------------
    private static ResourceBundle bundle = ResourceBundle.getBundle("com.hb.it.imageserverclient.FileServiceUtils");

    /**
     * Upload image to image service.
     *
     * @param imagePath      图片要保存的路径 如test/test3.jpg
     * @param imageLocalPath 图片本地路径
     * @return 保存后的图片 url 如:http://192.168.8.12:8082/images/test/test3.jpg
     */
    public static String uploadImageToImageServerByCMD(String imagePath, String imageLocalPath) {
        if (imagePath == null || "".equals(imagePath) || imageLocalPath == null || "".equals(imageLocalPath)) {
            return null;
        } else if ("/".equals(imagePath.substring(0, 1))) {
            imagePath = imagePath.substring(1);
        }
        File imageFile = new File(imageLocalPath);
        if (!imageFile.exists()) {
            System.out.println("upload image " + imageLocalPath + " not exist ");
            return null;
        }
        try {
            String imageUrl = getImageServerBucketURL(imageLocalPath) + imagePath;
            // curl -X POST -H "Content-Type:image/png" \
            // --data-binary @test.jpg
            // http://hostname:8080/_test/_image/file.png
            StringBuilder sb = new StringBuilder();
            sb.append("curl -X POST -H \"Content-Type:application/octet-stream\" --data-binary @");
            sb.append(imageLocalPath).append(" ").append(imageUrl);
            String[] cmds = {"/bin/sh", "-c", sb.toString()};
            Process pro = Runtime.getRuntime().exec(cmds);
            // if (getOutPut(pro.getErrorStream())) {
            pro.waitFor();
            if (0 == pro.exitValue()) {
                return imageUrl;
            } else {
                System.out.println("upload image error code: " + pro.exitValue());
            }
            // }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";

    }

    public static Boolean getOutPut(InputStream in) throws IOException {
        BufferedReader read = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = read.readLine()) != null) {
            sb.append(line);
        }
        in.close();
        if (sb.length() > 0) {
            System.out.println("upload image error:" + sb.toString());
            return false;
        }
        return true;
    }

    /**
     * Gets the image server bucket url.
     *
     * @return the image server bucket url
     */
    public static String getImageServerBucketURL(String localPath) {
        return bundle.getString("IMAGE_SERVER_IMAGE_BUCKET");
    }

    public static String getImageServerBucketURL() {
        return bundle.getString("IMAGE_SERVER_IMAGE_BUCKET");
    }

    public static String getExtension(String filename, String defExt) {
        if ((filename != null) && (filename.length() > 0)) {
            int i = filename.lastIndexOf('.');
            if ((i > -1) && (i < (filename.length() - 1))) {
                return filename.substring(i + 1);
            }
        }
        return defExt;
    }

    public static boolean isImg(String extend) {
        boolean ret = false;
        List<String> list = new java.util.ArrayList<String>();
        list.add("jpg");
        list.add("jpeg");
        list.add("bmp");
        list.add("gif");
        list.add("png");
        list.add("tif");
        for (String s : list) {
            if (s.equals(extend)) {
                ret = true;
            }
        }
        return ret;
    }

    /**
     * Delete image by url.
     *
     * @param imageUrl the image url
     * @return true, if successful
     */
    public static boolean deleteImageByUrl(String imageUrl) {
        String key = parseKeyInUrl(imageUrl);
        try {
            AliyunClient client = AliyunClient.getInstance();
            client.deleteObject(key);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Delete file failed from:" + imageUrl);
        }
        return false;
    }

    private static String parseKeyInUrl(String imageUrl) {
        try {
            URI uri = new URI(imageUrl);
            return uri.getPath().substring(1);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String uploadImageToImageServiceByHttp(String imagePath, InputStream input, Long length) {
        String key = uploadImageToImageService(imagePath, input, length);
        String fullUrlAddress = null;
        if (key != null && !key.trim().isEmpty()) {
            if (imagePath != null && imagePath.startsWith("/")) {
                imagePath = imagePath.substring(1);
            }
            fullUrlAddress = getImageServerBucketURL(imagePath) + imagePath;
        }
        return fullUrlAddress;
    }

    /**
     * Upload image to image service.
     *
     * @param imagePath the image path
     * @param input     the input
     * @return the string
     */
    public static String uploadImageToImageService(String imagePath, String inputImagePath) {
        try {
            AliyunClient client = AliyunClient.getInstance();
            return client.putObject(imagePath, inputImagePath);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("File not found:" + imagePath);
        }
        return "";
    }

    /**
     * @param imagePath
     * @param inputStream
     * @param length      length
     * @return
     * @author xiao.miao
     * @Date 2015年12月21日 下午12:01:50
     */
    public static String uploadImageToImageService(String imagePath, InputStream inputStream, Long length) {
        try {
            AliyunClient client = AliyunClient.getInstance();
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(length==null?inputStream.available():length);
            metadata.setCacheControl("no-cache");
            metadata.setHeader("Pragma", "no-cache");
            metadata.setContentEncoding("utf-8");
            metadata.setContentType(getContentType(imagePath));
            metadata.setContentDisposition("filename/filesize=" + imagePath + "/" + length + "Byte.");
            return client.putObject(imagePath, inputStream, metadata);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 通过文件名判断并获取OSS服务文件上传时文件的contentType
     *
     * @param fileName 文件名
     * @return 文件的contentType
     */
    public static final String getContentType(String fileName) {
        String fileExtension = fileName.substring(fileName.lastIndexOf("."));
        if ("bmp".equalsIgnoreCase(fileExtension)) return "image/bmp";
        if ("gif".equalsIgnoreCase(fileExtension)) return "image/gif";
        if ("jpeg".equalsIgnoreCase(fileExtension) || "jpg".equalsIgnoreCase(fileExtension) || "png".equalsIgnoreCase(fileExtension))
            return "image/jpeg";
        if ("html".equalsIgnoreCase(fileExtension)||"htm".equalsIgnoreCase(fileExtension)) return "text/html";
        if ("txt".equalsIgnoreCase(fileExtension)) return "text/plain";
        if ("vsd".equalsIgnoreCase(fileExtension)) return "application/vnd.visio";
        if ("ppt".equalsIgnoreCase(fileExtension) || "pptx".equalsIgnoreCase(fileExtension))
            return "application/vnd.ms-powerpoint";
        if ("doc".equalsIgnoreCase(fileExtension) || "docx".equalsIgnoreCase(fileExtension))
            return "application/msword";
        if ("xml".equalsIgnoreCase(fileExtension)) return "text/xml";
        return "text/html";
    }

    /**
     * Gets the file bytes.
     *
     * @param f the f
     * @return the file bytes
     * @throws Exception the exception
     */
    public static byte[] getFileBytes(File f) throws Exception {
        FileInputStream in = new FileInputStream(f);
        return getBytes(in);
    }

    private static byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        int n;
        while ((n = inputStream.read(b)) != -1) {
            out.write(b, 0, n);
        }
        inputStream.close();
        return out.toByteArray();
    }

    /**
     * 获取上传html路径
     *
     * @return
     */
    public static String getHtmlServerStarBucket() {
        return bundle.getString("IMAGE_SERVER_IMAGE_BUCKET");
    }

    /**
     * @param htmlPath    上传文件路径及文件名
     * @param inputStream 输入流
     * @param l           length 长度
     * @return 返回上传文件路径及文件名
     * @author xiao.miao
     */
    public static String uploadStreamToImageService(String htmlPath, InputStream inputStream, long length) {
        try {
            AliyunClient client = AliyunClient.getInstance();
            return client.putObject(htmlPath, inputStream, setHtmlMime(length));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * @param htmlPath 上传文件路径及文件名
     * @param filePath 文件路径
     * @return 返回上传文件路径及文件名
     * @author xiao.miao
     */
    public static String uploadFilePathToImageService(String htmlPath, String filePath) {
        try {
            // 获取指定文件的输入流
            File file = new File(filePath);
            InputStream inputStream = new FileInputStream(file);
            AliyunClient client = AliyunClient.getInstance();
            return client.putObject(htmlPath, inputStream, setHtmlMime(file.length()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * @param length
     * @param metadata
     */
    private static ObjectMetadata setHtmlMime(long length) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(length);
        metadata.setContentType("text/html");
        return metadata;
    }
    
    /**
     * 腾讯云上传文件
     * cosName 上传后文件的名称
     * filePath 上传文件路径
     * @return
     */
    public static String uploadFilePathToQCloud(String cosPath, String filePath) throws Exception {
    	QCloudClient client =  QCloudClient.getInstance();
    	String result = client.uploadFile(cosPath, filePath);
    	return parseQCloudResult(result);
    }
    
    /**
     * 腾讯云上传文件
     * cosName 上传后文件的名称
     * fis 文件流
     * @return
     */
    public static String uploadFilePathToQCloud(String cosPath, FileInputStream fis) throws Exception {
    	QCloudClient client =  QCloudClient.getInstance();
    	String result = client.uploadFile(cosPath, fis);
    	return parseQCloudResult(result);
    }
    
    /**
     * 腾讯云上传文件
     * cosName 上传后文件的名称
     * bytes 文件字节
     * @return
     */
    public static String uploadFilePathToQCloud(String cosPath, byte[] bytes) throws Exception {
    	QCloudClient client =  QCloudClient.getInstance();
    	String result = client.uploadFile(cosPath, bytes);
    	return parseQCloudResult(result);
    }
    
    private static String parseQCloudResult(String result) throws Exception {
    	JSONObject jsonObject = JSONObject.fromObject(result);
    	if(jsonObject.getInt("code") == 0){
    		return jsonObject.getJSONObject("data").getString("access_url");
    	}else{
    		throw new UploadException(jsonObject.getString("message"));
    	}
    }
}
