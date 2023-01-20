package com.qiuyu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiuyu.bean.Comment;
import com.qiuyu.dao.CommentMapper;
import com.qiuyu.utils.CommunityConstant;
import com.qiuyu.utils.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @author QiuYuSY
 * @create 2023-01-19 23:45
 */
@Service
public class CommentService implements CommunityConstant {
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private SensitiveFilter sensitiveFilter;
    @Autowired
    private DiscussPostService discussPostService;

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


    /**
     * 添加评论(涉及事务)
     * 先添加评论，后修改discuss_post中的评论数（作为一个整体事务，出错需要整体回滚！）
     * @param comment
     * @return
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ,propagation = Propagation.REQUIRED)
    public int addComment(Comment comment){
        if(comment == null){
            throw new IllegalArgumentException("参数不能为空！");
        }

        /**添加评论**/
        //过滤标签
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        //过滤敏感词
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        //添加评论
        int rows =commentMapper.insert(comment);

        /**
         * 更新帖子评论数量
         * 如果是帖子类型才更改帖子评论数量，并且获取帖子评论的id
         */
        if(comment.getEntityType() == ENTITY_TYPE_POST){
            //评论数
            int count = findCommentCount(comment.getEntityType(), comment.getEntityId());
            //更新数量
            discussPostService.updateCommentCount(comment.getEntityId(),count);
        }

        return rows;
    }

}
