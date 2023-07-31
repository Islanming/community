package com.example.community.event;

import com.alibaba.fastjson2.JSONObject;
import com.example.community.entity.DiscussPost;
import com.example.community.entity.Event;
import com.example.community.entity.Message;
import com.example.community.service.DiscussPostService;
import com.example.community.service.ElasticsearchService;
import com.example.community.service.MessagerService;
import com.example.community.util.CommunityConstant;
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
 * 事件消费者
 * @author Lenovo
 */
@Component
public class EventConsumer implements CommunityConstant {

    private static Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessagerService messagerService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    /**
     * 消费评论、点赞、关注事件
     * @param record
     */
    @KafkaListener(topics = {TOPIC_COMMENT,TOPIC_LIKE,TOPIC_FOLLOW})
    public void handMessage(ConsumerRecord record){
        // 空值处理
        if(record == null) {
            logger.error("消息的内容为空！");
            return;
        }
        // 从Json字符串格式转回为类对象,若为空则为格式错误
        Event event = JSONObject.parseObject(record.value().toString(),Event.class);
        if(event == null){
            logger.error("消息格式错误！");
            return;
        }

        // 发送站内通知
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        Map<String,Object> content = new HashMap<>();
        content.put("userId",event.getUserId());
        content.put("entityType",event.getEntityType());
        content.put("entityId",event.getEntityId());

        // 将event中的其他数据都存到content中
        if(!event.getData().isEmpty()){
            for (Map.Entry<String,Object> entry: event.getData().entrySet()) {
                content.put(entry.getKey(),entry.getValue());
            }
        }
        // 用json字符串格式存储，里面存储前端拼接通知语句需要的数据
        message.setContent(JSONObject.toJSONString(content));

        messagerService.addMessage(message);
    }

    /**
     * 消费发帖事件
     * @param record
     */
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record){
        // 空值处理
        if(record == null) {
            logger.error("消息的内容为空！");
            return;
        }
        // 从Json字符串格式转回为类对象,若为空则为格式错误
        Event event = JSONObject.parseObject(record.value().toString(),Event.class);
        if(event == null){
            logger.error("消息格式错误！");
            return;
        }

        // 把该帖子添加到es服务器
        DiscussPost post = discussPostService.findDiscussPostById(event.getEntityId());
        elasticsearchService.saveDiscussPost(post);

    }

    /**
     * 消费删帖事件
     * @param record
     */
    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDeleteMessage(ConsumerRecord record){
        // 空值处理
        if(record == null) {
            logger.error("消息的内容为空！");
            return;
        }
        // 从Json字符串格式转回为类对象,若为空则为格式错误
        Event event = JSONObject.parseObject(record.value().toString(),Event.class);
        if(event == null){
            logger.error("消息格式错误！");
            return;
        }

        // 把该帖子从es服务器删除
        elasticsearchService.deleteDiscussPost(event.getEntityId());

    }


}
