package com.qiuyu.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiuyu.bean.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author QiuYuSY
 * @create 2023-01-20 20:36
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class MessageMapperTest {
    @Autowired MessageMapper messageMapper;

    @Test
    public void findConversations(){
        Page<Message> page = new Page<>(1,2);

        page = (Page<Message>) messageMapper.selectConversations(111, page);

        page.getRecords().forEach(o-> System.out.println(o));
        System.out.println(page.getTotal());
    }

    @Test
    public void selectConversationCount() {
        int i = messageMapper.selectConversationCount(111);
        System.out.println(i);
    }

    @Test
    public void selectLetters() {
        IPage<Message> page = new Page<>(1,2);
        page = messageMapper.selectLetters("111_112", page);

        page.getRecords().forEach(o-> System.out.println(o));
        System.out.println(page.getTotal());
    }

    @Test
    public void selectLetterCount() {
        int i = messageMapper.selectLetterCount("111_112");
        System.out.println(i);
    }

    @Test
    public void selectLetterUnreadCount() {
        int i = messageMapper.selectLetterUnreadCount(111, "111_112");
        System.out.println(i);
    }
}
