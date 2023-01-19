package com.qiuyu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
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
        int currentPage = 1;
        int pageSize = 5;

        IPage<DiscussPost> page = discussPostService.findDiscussPosts(userId, currentPage, pageSize);
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

}