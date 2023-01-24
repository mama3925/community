package com.qiuyu.event;

import com.alibaba.fastjson.JSONObject;
import com.qiuyu.bean.Event;
import com.qiuyu.utils.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @author QiuYuSY
 * @create 2023-01-24 21:09
 */
@Component
public class EventProducer {
    @Autowired
    private KafkaTemplate kafkaTemplate;

    public void fireEvent(Event event){
        // 将事件发布到指定的主题,内容为event对象转化的json格式字符串
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }
}
