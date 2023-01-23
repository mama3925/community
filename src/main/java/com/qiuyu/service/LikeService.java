package com.qiuyu.service;

import com.qiuyu.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

/**
 * @author QiuYuSY
 * @create 2023-01-22 23:44
 */
@Service
public class LikeService {
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 点赞 (记录谁点了哪个类型哪个留言/帖子id)
     * 同时给用户的点赞数加一
     * 因为要进行多个操作,采用事务
     * @param userId
     * @param entityType
     * @param entityId
     * @param entityUserId 实体的作者的Id,这里在页面直接传进来,不然使用数据库查太慢了
     */
    public void like(int userId, int entityType, int entityId, int entityUserId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                //查询需要在事务之外
                Boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);

                //开启事务
                operations.multi();

                // 第一次点赞，第二次取消点赞
                if (isMember){
                    // 若已被点赞,实体类移除点赞者,实体作者点赞数-1
                    redisTemplate.opsForSet().remove(entityLikeKey, userId);
                    redisTemplate.opsForValue().decrement(userLikeKey);
                }else {
                    redisTemplate.opsForSet().add(entityLikeKey, userId);
                    redisTemplate.opsForValue().increment(userLikeKey);
                }

                //提交事务
                return operations.exec();
            }
        });

    }

    // 查询某实体(帖子、留言)点赞的数量 --> scard like:entity:1:110
    public long findEntityLikeCount(int entityType, int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    // 显示某人对某实体的点赞状态
    public int findEntityLikeStatus(int userId, int entityType, int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        // 1：已点赞 , 0：赞
        return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }

    //查询用户的点赞数
    public int findUserLikeCount(int userId) {
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        // 注意这里Integet封装类型！！！！
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count.intValue();
    }

}
