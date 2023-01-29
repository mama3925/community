package com.qiuyu;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiuyu.bean.DiscussPost;
import com.qiuyu.bean.MyPage;
import com.qiuyu.service.DiscussPostService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

/**
 * @author QiuYuSY
 * @create 2023-01-29 19:54
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class CaffeineTest {
    @Autowired
    private DiscussPostService discussPostService;

    @Test
    public void addData(){
        for (int i = 1; i <= 30_0000; i++) {
            DiscussPost discussPost = new DiscussPost();
            discussPost.setUserId(String.valueOf(162));
            discussPost.setTitle("测试第" + i + "条数据");
            discussPost.setContent("测试第" + i + "条数据");
            discussPost.setType(0);
            discussPost.setStatus(0);
            discussPost.setCreateTime(new Date());
            discussPost.setCommentCount(0);
            discussPost.setScore(0d);
            discussPostService.addDiscussPost(discussPost);
        }
    }

    @Test
    public void selectData(){
        MyPage<DiscussPost> page = new MyPage<>();
        page.setCurrent(1);
        page.setSize(5);
        page = discussPostService.findDiscussPosts(0,1,page);
        page.getRecords().forEach(o->{
            System.out.println(o);
        });

        page = new MyPage<>();
        page.setCurrent(1);
        page.setSize(5);
        page = discussPostService.findDiscussPosts(0,1,page);
        page.getRecords().forEach(o->{
            System.out.println(o);
        });

        page = new MyPage<>();
        page.setCurrent(1);
        page.setSize(5);
        page = discussPostService.findDiscussPosts(0,1,page);
        page.getRecords().forEach(o->{
            System.out.println(o);
        });

        page = new MyPage<>();
        page.setCurrent(2);
        page.setSize(5);
        page = discussPostService.findDiscussPosts(0,1,page);
        page.getRecords().forEach(o->{
            System.out.println(o);
        });

    }

}
