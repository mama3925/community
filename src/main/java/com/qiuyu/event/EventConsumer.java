package com.qiuyu.event;

import com.alibaba.fastjson.JSONObject;
import com.qiuyu.bean.DiscussPost;
import com.qiuyu.bean.Event;
import com.qiuyu.bean.Message;
import com.qiuyu.service.DiscussPostService;
import com.qiuyu.service.ElasticsearchService;
import com.qiuyu.service.MessageService;
import com.qiuyu.utils.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author QiuYuSY
 * @create 2023-01-24 21:12
 */
@Component
public class EventConsumer implements CommunityConstant {
    public static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private ElasticsearchService elasticsearchService;

    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_LIKE, TOPIC_FOLLOW})
    public void handleCommentMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空!");
            return;
        }
        // 将record.value字符串格式转化为Event对象
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);

        if (event == null) {
            logger.error("消息格式错误!");
            return;
        }


        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        // Message表中ToId设置为被发起事件的用户id
        message.setToId(event.getEntityUserId());
        // ConversationId设置为事件的主题（点赞、评论、关注）
        message.setConversationId(event.getTopic());
        message.setStatus(0);
        message.setCreateTime(new Date());

        // 设置content为可扩展内容，封装在Map集合中,用于显示xxx评论..了你的帖子
        HashMap<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityId", event.getEntityId());
        content.put("entityType", event.getEntityType());

        // 将event.getData里的k-v存到context这个Map中，再封装进message
        // Map.Entry是为了更方便的输出map键值对,Entry可以一次性获得key和value者两个值
        if (!event.getData().isEmpty()) {
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }

        // 将content(map类型)转化成字符串类型封装进message
        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);

    }

    /**
     * 消费发帖事件
     * @param record
     */
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record){
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空!");
            return;
        }
        // 将record.value字符串格式转化为Event对象
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误!");
            return;
        }

        //根据帖子id查询到帖子,然后放到ES中
        DiscussPost discussPost = discussPostService.findDiscussPostById(event.getEntityId());
        elasticsearchService.saveDiscussPost(discussPost);

    }
}
