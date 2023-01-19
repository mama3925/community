package com.qiuyu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiuyu.bean.DiscussPost;
import com.qiuyu.bean.MyPage;
import com.qiuyu.bean.User;
import com.qiuyu.service.DiscussPostService;
import com.qiuyu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/index")
    public String getIndexPage(Model model, MyPage<DiscussPost> page) {
        page.setSize(10);
        page.setPath("/index");
        //查询到分页的结果
        page = (MyPage<DiscussPost>) discussPostService.findDiscussPosts(0, page);

        List<DiscussPost> list = page.getRecords();
        //因为这里查出来的是userid,而不是user对象,所以需要重新查出user
        List<Map<String, Object>> discussPorts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost post : list) {
                Map<String, Object> map = new HashMap<>(15);
                map.put("post", post);
                User user = userService.findUserById(post.getUserId());
                map.put("user", user);
                discussPorts.add(map);
            }
        }

        model.addAttribute("discussPorts", discussPorts);
        model.addAttribute("page", page);

        return "/index";
    }

}
