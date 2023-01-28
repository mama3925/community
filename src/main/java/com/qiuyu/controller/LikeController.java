package com.qiuyu.controller;

import com.qiuyu.bean.Event;
import com.qiuyu.bean.User;
import com.qiuyu.event.EventProducer;
import com.qiuyu.service.CommentService;
import com.qiuyu.service.LikeService;
import com.qiuyu.utils.CommunityConstant;
import com.qiuyu.utils.CommunityUtil;
import com.qiuyu.utils.HostHolder;
import com.qiuyu.utils.RedisKeyUtil;
import org.apache.catalina.Host;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @author QiuYuSY
 * @create 2023-01-22 23:52
 */
@Controller
public class LikeController implements CommunityConstant {
    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private EventProducer eventProducer;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("/like")
    @ResponseBody
    // 加了一个postId变量，对应的前端和js需要修改
    public String like(int entityType, int entityId,int entityUserId, int postId){
        User user = hostHolder.getUser();

        // 点赞
        likeService.like(user.getId(), entityType,entityId,entityUserId);
        // 获取对应帖子、留言的点赞数量
        long entityLikeCount = likeService.findEntityLikeCount(entityType, entityId);
        // 获取当前登录用户点赞状态（1：已点赞 0：赞）
        int entityLikeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);

        Map<String,Object> map = new HashMap<>();
        map.put("likeCount",entityLikeCount);
        map.put("likeStatus",entityLikeStatus);

        /**
         * 触发点赞事件
         * 只有点赞完后，才会调用Kafka生产者，发送系统通知，取消点赞不会调用事件
         */
        if (entityLikeStatus == 1) {
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setEntityId(entityId)
                    .setEntityType(entityType)
                    .setUserId(user.getId())
                    .setEntityUserId(entityUserId)
                    .setData("postId", postId);
            // 注意：data里面存postId是因为点击查看后链接到具体帖子的页面

            eventProducer.fireEvent(event);
        }


        /**
         * 计算帖子分数
         * 将点赞过的帖子id存入set去重的redis集合------like()
         */
        if (entityType == ENTITY_TYPE_POST) {
            String redisKey = RedisKeyUtil.getPostScore();
            redisTemplate.opsForSet().add(redisKey, postId);
        }

        return CommunityUtil.getJSONString(0,null,map);
    }
}
