package com.qiuyu.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayInputStream;
import java.io.File;

/**
 * @author QiuYuSY
 * @create 2023-01-29 0:24
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class OSSTest {

    @Value("${aliyun.oss.file.end-point}")
    private String endPoint;
    @Value("${aliyun.oss.file.access-key-id}")
    private String accessKeyId;
    @Value("${aliyun.oss.file.access-key-secret}")
    private String accessKeySecret;
    @Value("${aliyun.oss.file.bucket-name}")
    private String bucketName;

    @Value("${community.path.upload-path}")
    private String uploadPath;



    @Test
    public void testUploda(){
        


        String objectName = "community/test.png";

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endPoint, accessKeyId, accessKeySecret);

        try {
            String content = "Hello OSS";
//            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(content.getBytes()));
            ossClient.putObject(bucketName, objectName, new File(uploadPath+"/0aebd4709f984d069709dc5460c4f248.png"));
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

}
