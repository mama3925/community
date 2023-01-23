package com.qiuyu.controller;

import com.qiuyu.bean.User;
import com.qiuyu.service.LikeService;
import com.qiuyu.utils.CommunityUtil;
import com.qiuyu.utils.HostHolder;
import org.apache.catalina.Host;
import org.springframework.beans.factory.annotation.Autowired;
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
public class LikeController {
    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @PostMapping("/like")
    @ResponseBody
    public String like(int entityType, int entityId,int entityUserId){
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

        return CommunityUtil.getJSONString(0,null,map);
    }
}
