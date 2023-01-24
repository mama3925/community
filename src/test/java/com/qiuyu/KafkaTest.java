package com.qiuyu;

import com.qiuyu.bean.Event;
import com.qiuyu.event.EventConsumer;
import com.qiuyu.event.EventProducer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;



@SpringBootTest
@RunWith(SpringRunner.class)
public class KafkaTest {
    @Autowired
    private KafkaProducer kafkaProducer;
    @Test
    public void testKafka() throws InterruptedException {
        kafkaProducer.sendMessage("test1","hello1");
        kafkaProducer.sendMessage("test1","world1");

        Thread.sleep(5000);
    }


    @Autowired
    EventProducer eventProducer;
    @Autowired
    EventConsumer eventConsumer;
    @Test
    public void test() throws InterruptedException {
        eventProducer.fireEvent(new Event().setTopic("test").setEntityId(1));
        Thread.sleep(2000);
    }
}

@Component
class KafkaProducer{
    @Autowired
    private KafkaTemplate kafkaTemplate;

    public void sendMessage(String topic, String content){
        kafkaTemplate.send(topic,content);
    }
}

@Component
class KafkaComsumer{
    @KafkaListener(topics = {"test1"})
    public void handleMessage(ConsumerRecord record){
        System.out.println(record.value());
    }
}
