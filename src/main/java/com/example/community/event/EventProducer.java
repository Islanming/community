package com.example.community.event;

import com.alibaba.fastjson2.JSONObject;
import com.example.community.controller.UserController;
import com.example.community.entity.Event;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 事件生产者
 * @author Lenovo
 */
@Component
public class EventProducer {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private KafkaTemplate kafkaTemplate;

    /**
     * 处理事件，本质是发一个消息
     * @param event 事件对象
     */
    public void fireEvent(Event event){
        // 将事件发布到指定的主题上，将封装事件的类对象转换成Json格式字符串，消费者拿到再进行还原就可以直接用
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event), new Callback() {
            @Override
            public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                // 偏移量
                long offset = recordMetadata.offset();
                // 分区
                int partition = recordMetadata.partition();
                // 主题
                String topic = recordMetadata.topic();
                // 如果e为空则说明发送成功，若因为网络抖动而发送失败可以在配置文件或者配置类中设置重发次数
                if(e!=null){
                    // 这里可以对生产者发送消息到Brocker丢失的情况进行处理，例如记录日志，方便后序处理
                    logger.error("《kafka》生产者消息发送失败,topic:"+topic+"partition:"+partition+"offset:"+offset);
                }
            }
        });
    }
}
