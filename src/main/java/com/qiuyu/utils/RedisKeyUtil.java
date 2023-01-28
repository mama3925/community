package com.qiuyu.utils;

/**
 * @author QiuYuSY
 * @create 2023-01-22 23:38
 */
public class RedisKeyUtil {
    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    // 关注
    private static final String PREFIX_FOLLOWEE = "followee";
    // 粉丝
    private static final String PREFIX_FOLLOWER = "follower";
    // 验证码
    private static final String PREFIX_KAPTCHA = "kaptcha";
    // 登录凭证
    private static final String PREFIX_TICKET = "ticket";
    // 用户缓存
    private static final String PREFIX_USER = "user";

    // UV (网站访问用户数量---根据Ip地址统计(包括没有登录的用户))
    private static final String PREFIX_UV = "uv";
    // DAU (活跃用户数量---根据userId)
    private static final String PREFIX_DAU = "dau";

    // 热帖分数 (把需要更新的帖子id存入Redis当作缓存)
    private static final String PREFIX_POST = "post";



    /**
     * 某个实体的赞
     * key= like:entity:entityType:entityId -> set(userId)
     * @param entityType 帖子还是回复
     * @param entityId id
     * @return
     */
    public static String getEntityLikeKey(int entityType, int entityId){
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    /**
     * 某个用户收到的赞
     * @param userId
     * @return
     */
    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    /**
     * 某个用户关注的实体(用户，帖子)
     * followee:userId:entityType --> zset(entityId, date)
     */
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    /**
     * 某个实体拥有的用户粉丝
     * follower:entityType:entityId -->zset(userId, date)
     */
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT +entityType + SPLIT +entityId;
    }

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


    /**
     * 存储单日ip访问数量（uv）--HyperLogLog ---k:时间 v:ip  (HyperLogLog)
     * 示例：uv:20220526 = ip1,ip2,ip3,...
     */
    public static String getUVKey(String date) {
        return PREFIX_UV + SPLIT + date;
    }

    /**
     * 获取区间ip访问数量（uv）
     * 示例：uv:20220525:20220526 = ip1,ip2,ip3,...
     */
    public static String getUVKey(String startDate, String endDate) {
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    /**
     * 存储单日活跃用户（dau）--BitMap ---k:date v:userId索引下为true  (BitMap)
     * 示例：dau:20220526 = userId1索引--(true),userId2索引--(true),....
     */
    public static String getDAUKey(String date) {
        return PREFIX_DAU + SPLIT + date;
    }

    /**
     * 获取区间活跃用户
     * 示例：dau:20220526:20220526
     */
    public static String getDAUKey(String startDate, String endDate) {
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }

    /**
     *  帖子分数 (发布、点赞、加精、评论时放入)
     */
    public static String getPostScore() {
        return PREFIX_POST + SPLIT + "score";
    }
}
