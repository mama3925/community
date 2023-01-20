package com.qiuyu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiuyu.bean.Comment;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author QiuYuSY
 * @create 2023-01-20 0:30
 */
@SpringBootTest
@RunWith(SpringRunner.class)
class CommentServiceTest {
    @Autowired
    private CommentService commentService;

    @Test
    void findCommentsByEntity() {
        IPage<Comment> page = new Page<>(1, Integer.MAX_VALUE);
        page = commentService.findCommentsByEntity(1, 228, page);
        System.out.println(page.getRecords());
        System.out.println(page.getTotal());
    }

    @Test
    public void findCommentCount() {
        System.out.println(commentService.findCommentCount(1, 228));
    }

    @Test
    public void addComment() {
        Comment comment = new Comment();
        comment.setUserId(162);
        comment.setEntityType(1);
        comment.setEntityId(287);
        comment.setTargetId(0);
        comment.setContent("回复测试");
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        System.out.println(commentService.addComment(comment));
    }
}