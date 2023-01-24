package com.qiuyu.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qiuyu.bean.Message;
import com.qiuyu.dao.MessageMapper;
import com.qiuyu.utils.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @author QiuYuSY
 * @create 2023-01-20 17:33
 */
@Service
public class MessageService {
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private SensitiveFilter sensitiveFilter;

    /**
     * 查询当前用户的会话列表,每个会话只返回一条最新消息
     * @param userId
     * @param page
     * @return
     */
    public IPage<Message> findConversations(int userId, IPage<Message> page) {
        return page = messageMapper.selectConversations(userId, page);
    }

    /**
     * 查询当前用户的会话数量
     * @param userId
     * @return
     */
    public int findConversationCount(int userId) {
        return messageMapper.selectConversationCount(userId);
    }

    /**
     * 查询某个会话中包含的所有消息
     * @param conversationId
     * @param page
     * @return
     */
    public IPage<Message> findLetters(String conversationId, IPage<Message> page) {
        return messageMapper.selectLetters(conversationId, page);
    }

    /**
     * 查询某个会话中包含的消息数量
     * @param conversationId
     * @return
     */
    public int findLetterCount(String conversationId) {
        return messageMapper.selectLetterCount(conversationId);
    }

    /**
     * 查询未读的私信的数量
     * @param userId
     * @param conversationId
     * @return
     */
    public int findLetterUnreadCount(int userId, String conversationId) {
        return messageMapper.selectLetterUnreadCount(userId, conversationId);
    }

    /**
     * 添加消息
     * @param message
     * @return
     */
    public int addMessage(Message message){
        //转义标签
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        //过滤敏感词
        message.setContent(sensitiveFilter.filter(message.getContent()));

        return messageMapper.insertMessage(message);
    }

    /**
     * 把多个消息都设为已读
     * @param ids
     * @return
     */
    public int readMessage(List<Integer> ids){
        return messageMapper.updateStatus(ids, 1);
    }

    /**
     * 查询某个主题最新通知
     * @param userId
     * @param topic
     * @return
     */
    public Message findLatestNotice(int userId, String topic) {
        return messageMapper.selectLatestNotice(userId, topic);
    }

    /**
     * 查询某个主题通知个数
     * @param userId
     * @param topic
     * @return
     */
    public int findNoticeCount(int userId, String topic) {
        return messageMapper.selectNoticeCount(userId, topic);
    }

    /**
     * 查询某个主题未读个数(topic可为null,若为null:查询所有类系统未读通知个数)
     * @param userId
     * @param topic
     * @return
     */
    public int findNoticeUnreadCount(int userId, String topic) {
        return messageMapper.selectNoticeUnreadCount(userId, topic);
    }

    /**
     * 分页查询某个主题的详情
     * @param userId
     * @param topic
     * @return
     */
    public IPage<Message> findNotices(int userId, String topic,IPage<Message> page) {
        return messageMapper.selectNotices(userId, topic, page);
    }


}
