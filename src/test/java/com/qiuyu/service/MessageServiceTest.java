package com.qiuyu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiuyu.bean.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author QiuYuSY
 * @create 2023-01-20 18:08
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class MessageServiceTest {

    @Autowired
    private MessageService messageService;

    @Test
    public void findConversations() {
        IPage<Message> page = new Page<>(1,5);
        page = messageService.findConversations(111, page);

        System.out.println(page.getRecords());
        System.out.println(page.getTotal());
    }

    @Test
    public void findNotices(){
        Page<Message> page = new Page<>(1, 10);
        page = (Page<Message>) messageService.findNotices(163,"follow",page);
        page.getRecords().forEach(o->{
            System.out.println(o);
        });
        System.out.println(page.getTotal());

    }
}