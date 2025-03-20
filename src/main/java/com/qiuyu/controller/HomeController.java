package com.qiuyu.controller;

import com.qiuyu.bean.DiscussPost;
import com.qiuyu.bean.MyPage;
import com.qiuyu.bean.User;
import com.qiuyu.service.DiscussPostService;
import com.qiuyu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author QiuYuSY
 * @create 2023-01-16 20:54
 */
@Controller
public class HomeController {

    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private UserService userService;

    /**
     * 分页获取所有帖子
     * @param model
     * @param page
     * @return
     */
    @GetMapping("/index")
    public String getIndexPage(Model model, MyPage<DiscussPost> page) {

        page.setSize(10);
        page.setPath("/index");

        // 查询到分页的结果
        page = (MyPage<DiscussPost>) discussPostService.findDiscussPosts(0, page);

        List<DiscussPost> list = page.getRecords(); // 获取所有帖子
        // 这里查了所有帖子，以此能得到所有userId，再去查用户DAO

        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list == null) {
            model.addAttribute("discussPosts", discussPosts);
            model.addAttribute("page", page);
            return "/index"; // 提前返回
        }
        for (DiscussPost discussPost : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("post", discussPost);
            User user = userService.findUserById(discussPost.getUserId());
            map.put("user", user);
            discussPosts.add(map);
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("page", page);


        return "/index";
    }

    /**
     * 500错误页跳转
     *
     * @return
     */
    @GetMapping("/error")
    public String getErrorPage() {
        return "/error/500";
    }

    /**
     * 权限不足
     *
     * @return
     */
    @GetMapping("/denied")
    public String getDeniedPage() {
        return "/error/404";
    }
}
