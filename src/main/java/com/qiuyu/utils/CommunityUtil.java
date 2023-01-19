package com.qiuyu.utils;


import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.Map;
import java.util.UUID;

/**
 * 封装项目中的一些常用工具类
 */
public class CommunityUtil {
    /**
     * 生成随机字符串
     * 用于邮件激活码，salt5位随机数加密
     */
    public static String generateUUID(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    /**
     * MD5加密
     * hello-->abc123def456
     * hello + 3e4a8-->abc123def456abc
     * @param key
     * @return
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

    /**
     * 使用fastjson，将JSON对象转为JSON字符串(前提要引入Fastjson)
     * @param code
     * @param msg
     * @param map
     * @return
     */
    public static String getJSONString(int code, String msg, Map<String,Object> map){
        JSONObject json = new JSONObject();
        json.put("code",code);
        json.put("msg",msg);
        if (map != null) {
            //从map里的key集合中取出每一个key
            for (String key : map.keySet()) {
                json.put(key, map.get(key));
            }
        }
        return json.toJSONString();
    }
    public static String getJSONString(int code, String msg) {
        return getJSONString(code, msg, null);
    }
    public static String getJSONString(int code) {
        return getJSONString(code, null, null);
    }
}
