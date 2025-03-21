package com.qiuyu.utils;

/**
 * @author QiuYuSY
 * @create 2023-01-22 23:38
 */
public class RedisKeyUtil {

    private static final String SPLIT = ":";
    // 验证码
    private static final String PREFIX_KAPTCHA = "kaptcha";
    // 登录凭证
    private static final String PREFIX_TICKET = "ticket";
    // 用户缓存
    private static final String PREFIX_USER = "user";
    /**
     * 登录验证码
     * @param owner
     * @return
     */
    public static String getKaptchaKey(String owner) {
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    /**
     * 登录凭证
     * @param ticket
     * @return
     */
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    /**
     * 用户缓存
     * @param userId
     * @return
     */
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }
}
