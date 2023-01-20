package com.qiuyu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiuyu.bean.DiscussPost;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class DiscussPostServiceTest {
    @Autowired
    private DiscussPostService discussPostService;
    @Test
    public void testFindDiscussPosts(){
        int userId = 101;

        Page<DiscussPost> page = new Page<>();
        page.setSize(5);
        page.setCurrent(1);

        page = (Page<DiscussPost>) discussPostService.findDiscussPosts(userId, page);
        List<DiscussPost> discussPosts = page.getRecords();
        discussPosts.forEach(System.out::println);
    }

    @Test
    public void testFindDiscussPostRows(){
        int nums = discussPostService.findDiscussPostRows(149);
        System.out.println(nums);
    }

    @Test
    public void testAddDiscussPost(){
        DiscussPost post = new DiscussPost();
        post.setUserId("162");
        post.setTitle("测试赌博 许久");
        post.setContent("测试fuck");
        post.setType(0);
        post.setStatus(0);
        post.setCreateTime(new Date());
        post.setCommentCount(0);
        post.setScore(0.0);

        System.out.println(discussPostService.addDiscussPost(post));

    }

    @Test
    public void updateCommentCount() {
        System.out.println(discussPostService.updateCommentCount(287, 1));
    }
}