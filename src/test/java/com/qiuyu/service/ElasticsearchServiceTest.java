package com.qiuyu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qiuyu.bean.DiscussPost;
import com.qiuyu.bean.MyPage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;


@SpringBootTest
@RunWith(SpringRunner.class)
public class ElasticsearchServiceTest {
    @Autowired ElasticsearchService elasticsearchService;
    @Test
    public void searchDiscussPost() {
        IPage<DiscussPost> page = new MyPage<>();
        page.setCurrent(1);
        page.setSize(5);
        page = elasticsearchService.searchDiscussPost("为什么互联网会寒冬呢",page);

        for (DiscussPost record : page.getRecords()) {
            System.out.println(record);
        }

        System.out.println(page.getPages());
        System.out.println(page.getTotal());
        System.out.println(page.getCurrent());

    }
}