package com.qiuyu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qiuyu.bean.DiscussPost;
import com.qiuyu.bean.MyPage;
import com.qiuyu.service.ElasticsearchService;
import com.qiuyu.service.LikeService;
import com.qiuyu.service.UserService;
import com.qiuyu.utils.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author QiuYuSY
 * @create 2023-01-26 2:23
 */
@Controller
public class SearchController implements CommunityConstant {
    @Autowired
    private UserService userService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private ElasticsearchService elasticsearchService;

    // search?keyword=xxx
    @GetMapping("/search")
    public String search(String keyword, MyPage<DiscussPost> page, Model model) {
        // 搜索帖子
        page.setSize(10);
        page = (MyPage<DiscussPost>) elasticsearchService.searchDiscussPost(keyword, page);
        List<DiscussPost> searchResult = page.getRecords();

        // 聚合数据
        List<Map<String, Object>> discussPostVo = new ArrayList<>();
        if (searchResult != null) {
            for (DiscussPost post : searchResult) {
                Map<String, Object> map = new HashMap<>();
                // 帖子
                map.put("post", post);
                // 作者
                map.put("user", userService.findUserById(post.getUserId()));
                // 点赞数量
                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));

                discussPostVo.add(map);
            }
        }

        model.addAttribute("discussPostVo", discussPostVo);
        // 为了页面上取的默认值方便
        model.addAttribute("keyword", keyword);
        model.addAttribute("page", page);

        page.setPath("/search?keyword=" + keyword);

        return "/site/search";
    }


}
