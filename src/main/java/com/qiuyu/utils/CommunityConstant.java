package com.qiuyu.utils;

/**
 * @author QiuYuSY
 * @create 2023-01-17 22:06
 * 一些常量
 */
public interface CommunityConstant {
    /*      以下用于注册功能      */
    /** 激活成功*/
    int ACTIVATION_SUCCESS=0;
    /** 重复激活 */
    int ACTIVATION_REPEAT=1;
    /** 激活失败 */
    int ACTIVATION_FAILURE=2;


    /*以下用于登录功能*/

    /**
     * 默认状态的登录凭证的超时时间
     * 12h
     */
    int DEFAULT_EXPIRED_SECONDS=3600*12;
    /**
     * 记住状态的登录凭证超时时间
     * 7 days
     */
    int REMEMBER_EXPIRED_SECONDS=3600*24*7;

    /**
     * 实体类型:帖子
     */
    int ENTITY_TYPE_POST = 1;
    /**
     * 实体类型:评论
     */
    int ENTITY_TYPE_COMMENT = 2;

    /**
     * 实体类型:User
     */
    int ENTITY_TYPE_USER = 3;

    /**
     * Kafka主题: 评论
     */
    String TOPIC_COMMENT = "comment";
    /**
     * Kafka主题: 点赞
     */
    String TOPIC_LIKE = "like";
    /**
     * Kafka主题: 关注
     */
    String TOPIC_FOLLOW = "follow";
    /**
     * Kafka主题: 发帖
     */
    String TOPIC_PUBLISH = "publish";

    /**
     * Kafka主题: 删帖
     */
    String TOPIC_DELETE = "delete";

    /**
     * 系统用户ID
     */
    int SYSTEM_USER_ID = 1;

    /**
     * 权限:普通用户
     */
    String AUTHORITY_USER = "user";
    /**
     * 权限:管理员
     */
    String AUTHORITY_ADMIN = "admin";
    /**
     * 权限:版主
     */
    String AUTHORITY_MODERATOR = "moderator";


}