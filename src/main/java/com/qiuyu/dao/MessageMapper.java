package com.qiuyu.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qiuyu.bean.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author QiuYuSY
 * @create 2023-01-20 17:33
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
    /**
     * 分页查询出当前用户的所有会话,以及会话中最新的一条消息
     * @param userId
     * @param page
     * @return
     */
    IPage<Message> selectConversations(@Param("userId") Integer userId, IPage<Message> page);

    /**
     * 查询当前用户的会话数量
     * @param userId
     * @return
     */
    int selectConversationCount(@Param("userId") int userId);

    /**
     * 查询某个会话所包含的私信列表
     * @param conversationId
     * @param page
     * @return
     */
    IPage<Message> selectLetters(@Param("conversationId") String conversationId, IPage<Message> page);

    /**
     * 查询某个会话所包含的私信数量
     * @param conversationId
     * @return
     */
    int selectLetterCount(@Param("conversationId") String conversationId);

    /**
     * 查询未读的数量
     * 1.带参数conversationId ：私信未读数量
     * 2.不带参数conversationId ：当前登录用户 所有会话未读数量
     */
    int selectLetterUnreadCount(@Param("userId")int userId,@Param("conversationId") String conversationId);


    /**
     * 插入会话
     * @param message
     * @return
     */
    int insertMessage(Message message);

    /**
     * 批量更改每个会话的所有未读消息为已读
     * @param ids
     * @param status
     * @return
     */
    int updateStatus(@Param("ids") List<Integer> ids,@Param("status") int status);
}
