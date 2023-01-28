package com.qiuyu.controller;

import com.qiuyu.bean.Comment;
import com.qiuyu.bean.DiscussPost;
import com.qiuyu.bean.Event;
import com.qiuyu.event.EventProducer;
import com.qiuyu.service.CommentService;
import com.qiuyu.service.DiscussPostService;
import com.qiuyu.utils.CommunityConstant;
import com.qiuyu.utils.HostHolder;
import com.qiuyu.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

/**
 * @author QiuYuSY
 * @create 2023-01-20 16:00
 */
@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {
    @Autowired
    private CommentService commentService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private EventProducer eventProducer;
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 添加回复
     * @param discussPostId
     * @param comment
     * @return
     */
    @PostMapping("/add/{discussPostId}")
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment){
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);

        //触发评论事件
        Event event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId",discussPostId); //方便之后跳到帖子上

        /**
         * event.setEntityUserId要分情况设置被发起事件的用户id
         * 1.评论的是帖子，被发起事件（评论）的用户->该帖子发布人id
         * 2.评论的是用户的评论，被发起事件（评论）的用户->该评论发布人id
         */
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            // 先找评论表对应的帖子id,在根据帖子表id找到发帖人id
            DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(Integer.valueOf(target.getUserId()));
        } else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {
            Comment target = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        eventProducer.fireEvent(event);

        //评论帖子时
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            //触发发帖事件,让消费者将帖子存入ElasticSearch
            event = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(discussPostId);
            eventProducer.fireEvent(event);
        }

        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            /**
             * 计算帖子分数
             * 将评论过的帖子id存入set去重的redis集合------addComment()
             */
            String redisKey = RedisKeyUtil.getPostScore();
            redisTemplate.opsForSet().add(redisKey, discussPostId);
        }

        return "redirect:/discuss/detail/"+discussPostId;
    }


}
