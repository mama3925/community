package com.qiuyu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiuyu.bean.Comment;
import com.qiuyu.dao.CommentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author QiuYuSY
 * @create 2023-01-19 23:45
 */
@Service
public class CommentService {
    @Autowired
    private CommentMapper commentMapper;


    /**
     * 分页获得指定帖子的评论
     * @param entityType
     * @param entityId
     * @param page
     * @return
     */
    public IPage<Comment> findCommentsByEntity(int entityType, int entityId, IPage<Comment> page) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getEntityType, entityType).eq(Comment::getEntityId, entityId);
        commentMapper.selectPage(page,wrapper);
        return page;
    }

    /**
     * 获取某个帖子评论的数量
     * @param entityType
     * @param entityId
     * @return
     */
    public int findCommentCount(int entityType, int entityId){
        Integer count = commentMapper.selectCount(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getEntityType, entityType)
                .eq(Comment::getEntityId, entityId));
        return count;
    }

}
