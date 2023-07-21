package com.example.community.event;

import com.alibaba.fastjson2.JSONObject;
import com.example.community.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 事件生产者
 * @author Lenovo
 */
@Component
public class EventProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    /**
     * 处理事件，本质是发一个消息
     * @param event 事件对象
     */
    public void fireEvent(Event event){
        // 将事件发布到指定的主题上，将封装事件的类对象转换成Json格式字符串，消费者拿到再进行还原就可以直接用
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }
}
