package com.qiuyu.utils;


import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

public class CommunityUtil {
    /*
     * 生成随机字符串
     * 用于邮件激活码，salt5位随机数加密
     **/
    public static String generateUUID(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }
    /* MD5加密
     * hello-->abc123def456
     * hello + 3e4a8-->abc123def456abc
     */
    public static String md5(String key){
        //检查时候为null 空 空格
        if (StringUtils.isBlank(key)){
            return null;
        }
        //MD5加密方法
        return DigestUtils.md5DigestAsHex(key.getBytes());
        //参数是bytes型
    }
}
