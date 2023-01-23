package com.qiuyu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qiuyu.bean.User;
import com.qiuyu.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static com.qiuyu.utils.CommunityConstant.ENTITY_TYPE_USER;

/**
 * @author QiuYuSY
 * @create 2023-01-23 19:46
 */
@Service
public class FollowService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserService userService;

    /**
     * 关注某个实体
     * @param userId
     * @param entityType
     * @param entityId
     */
    public void follow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback(){
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                operations.multi();

                /**
                 * System.currentTimeMillis()->用于获取当前系统时间,以毫秒为单位
                 * 关注时，首先将实体(用户或帖子)id添加用户关注的集合中，再将用户id添加进实体粉丝的集合中
                 */
                redisTemplate.opsForZSet().add(followeeKey,entityId,System.currentTimeMillis());
                redisTemplate.opsForZSet().add(followerKey,userId,System.currentTimeMillis());

                return operations.exec();
            }
        });
    }

    /**
     * 取消关注
     * @param userId
     * @param entityType
     * @param entityId
     */
    public void unfollow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback(){

            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                operations.multi();

                //关注时，首先将实体(用户或帖子)id移除用户关注的集合中，再将用户id移除进实体粉丝的集合中
                redisTemplate.opsForZSet().remove(followeeKey,entityId);
                redisTemplate.opsForZSet().remove(followerKey,userId);


                return operations.exec();
            }
        });
    }

    /**
     * 某个用户的关注的实体数量
     * @param userId
     * @param entityType
     * @return
     */
    public long findFolloweeCount(int userId, int entityType) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    /**
     * 查询某个实体的粉丝数
     * @param entityType
     * @param entityId
     * @return
     */
    public long findFollowerCount(int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }


    /**
     * 当前用户是否关注了该实体
     * userId->当前登录用户  entityType->用户类型 entityId->关注的用户id
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    public boolean hasFollowed(int userId, int entityType, int entityId) {
        String followeeKey =RedisKeyUtil.getFolloweeKey(userId, entityType);
        //查下score是否为空
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }

    /**
     * 查询某用户关注的人
     * @param userId
     * @return
     */
    public List<Map<String, Object>> findFollowees(int userId, int offset, int limit){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
        // 按最新时间倒序查询目标用户id封装在set<Integet>中
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);

        if (targetIds == null) {
            return null;
        }
        // 将user信息Map和redis用户关注时间Map一起封装到list
        ArrayList<Map<String, Object>> list = new ArrayList<>();
        for (Integer targetId: targetIds) {
            HashMap<String, Object> map = new HashMap<>();
            // 用户信息map
            User user = userService.findUserById(String.valueOf(targetId));
            map.put("user", user);
            // 目标用户关注时间map(将long型拆箱成基本数据类型)
            Double score = redisTemplate.opsForZSet().score(followeeKey, targetId);
            map.put("followTime", new Date(score.longValue()));

            list.add(map);
        }
        return list;
    }

    /**
     * 查询某用户粉丝列表
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    public List<Map<String, Object>> findFollowers(int userId, int offset, int limit){
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER, userId);

        // 按最新时间倒序查询目标用户id封装在set<Integet>中
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);
        if (targetIds == null) {
            return null;
        }

        // 将user信息Map和redis用户关注时间Map一起封装到list
        ArrayList<Map<String, Object>> list = new ArrayList<>();
        for (Integer targetId: targetIds) {
            HashMap<String, Object> map = new HashMap<>();
            // 用户信息map
            User user = userService.findUserById(targetId.toString());
            map.put("user", user);
            // 目标用户关注时间map(将long型拆箱成基本数据类型)
            Double score = redisTemplate.opsForZSet().score(followerKey, targetId);
            map.put("followTime", new Date(score.longValue()));

            list.add(map);
        }
        return list;

    }
}
