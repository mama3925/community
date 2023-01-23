package com.qiuyu.controller;

import com.qiuyu.bean.MyPage;
import com.qiuyu.bean.User;
import com.qiuyu.service.FollowService;
import com.qiuyu.service.UserService;
import com.qiuyu.utils.CommunityConstant;
import com.qiuyu.utils.CommunityUtil;
import com.qiuyu.utils.HostHolder;
import com.qiuyu.utils.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author QiuYuSY
 * @create 2023-01-23 20:04
 */
@Controller
public class FollowController implements CommunityConstant {
    @Autowired
    private FollowService followService;
    @Autowired
    private UserService userService;
    @Autowired
    private HostHolder hostHolder;


    /**
     * 关注
     * @param entityType
     * @param entityId
     * @return
     */
    @PostMapping("/follow")
    @ResponseBody
    public String follow(int entityType, int entityId) {
        followService.follow(hostHolder.getUser().getId(), entityType, entityId);
        return CommunityUtil.getJSONString(0,"已关注");
    }

    /**
     * 取消关注
     * @param entityType
     * @param entityId
     * @return
     */
    @PostMapping("/unfollow")
    @ResponseBody
    public String unfollow(int entityType, int entityId) {
        followService.unfollow(hostHolder.getUser().getId(), entityType, entityId);
        return CommunityUtil.getJSONString(0,"已取消关注");
    }


    //查询某用户关注列表
    @GetMapping("/followees/{userId}")
    public String getFollowees(@PathVariable("userId")int userId, Page page, Model model) {
        // 当前访问的用户信息
        User user = userService.findUserById(String.valueOf(userId));
        // Controller层统一处理异常
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user", user);
        // 设置分页信息
        page.setLimit(3);
        page.setPath("/followees/" + userId);
        page.setRows((int) followService.findFolloweeCount(userId, ENTITY_TYPE_USER));

        List<Map<String, Object>> userList = followService.findFollowees(userId, page.getOffset(), page.getLimit());
        if (userList != null) {
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users", userList);

        return "/site/followee";
    }

    /**
     * 判端当前登录用户与关注、粉丝列表的关注关系
     * @param userId
     * @return
     */
    private Boolean hasFollowed(int userId) {
        if (hostHolder.getUser() == null) {
            return false;
        }
        // 调用当前用户是否已关注user实体Service
        return followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
    }

    //查询某用户粉丝列表
    @GetMapping("/followers/{userId}")
    public String getFollowers(@PathVariable("userId")int userId, Page page, Model model) {
        // 当前访问的用户信息
        User user = userService.findUserById(String.valueOf(userId));
        // Controller层统一处理异常
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user", user);
        // 设置分页信息
        page.setLimit(3);
        page.setPath("/followers/" + userId);
        page.setRows((int) followService.findFollowerCount(ENTITY_TYPE_USER, userId));

        List<Map<String, Object>> userList = followService.findFollowers(userId, page.getOffset(), page.getLimit());
        if (userList != null) {
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users", userList);

        return "/site/follower";
    }
}
