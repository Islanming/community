package com.example.community.event;

import com.alibaba.fastjson2.JSONObject;
import com.example.community.entity.DiscussPost;
import com.example.community.entity.Event;
import com.example.community.entity.Message;
import com.example.community.service.DiscussPostService;
import com.example.community.service.ElasticsearchService;
import com.example.community.service.MessagerService;
import com.example.community.util.CommunityConstant;
import com.example.community.util.CommunityUtil;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import joptsimple.internal.Strings;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

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

    @Value("${wk.image.command}")
    private String wkImageCommand;

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.share.name}")
    private String shareBucketName;

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

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

    /**
     * 消费分享事件
     * @param record
     */
    @KafkaListener(topics = TOPIC_SHARE)
    public void handleShareMessage(ConsumerRecord record){
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

        String htmlUrl = (String) event.getData().get("htmlUrl");
        String fileName = (String) event.getData().get("fileName");
        String suffix = (String) event.getData().get("suffix");

        String cmd = wkImageCommand + " --quality 75 "+ htmlUrl + " " + wkImageStorage +
               "/" +fileName + suffix;
        try {
            Runtime.getRuntime().exec(cmd);
            // 图片生成会比日志打印慢，上面的命令是提交给命令行执行的，执行完成与否下面这个日志都会输出
            logger.info("生成长图成功！" + cmd);
        } catch (IOException e) {
            logger.error("生成长图失败！" + e.getMessage());
        }

        // 启动定时器，监视该图片，一旦生成，则上传到云服务器
        UploadTask uploadTask = new UploadTask(fileName,suffix);
        Future future = taskScheduler.scheduleAtFixedRate(uploadTask, 500);
        uploadTask.setFuture(future);
    }

    class UploadTask implements Runnable{

        // 文件名
        private String fileName;
        // 文件后缀
        private String suffix;
        // 启动任务的返回值，可以用于停止定时器
        private Future future;
        // 开始时间
        private long startTime;
        // 上传次数
        private int uploadTimes;


        public UploadTask(String fileName, String suffix){
            this.fileName = fileName;
            this.suffix = suffix;
            this.startTime = System.currentTimeMillis();
        }

        public void setFuture(Future future){
            this.future = future;
        }

        @Override
        public void run() {
            // 生成图片失败
            if(System.currentTimeMillis() - startTime > 30000){
                logger.error("执行时间过长，终止任务："+fileName);
                future.cancel(true);
                return;
            }
            // 上传失败
            if(uploadTimes >= 3){
                logger.error("上传次数过多，终止任务："+fileName);
                future.cancel(true);
                return;
            }

            String path = wkImageStorage + "/" + fileName + suffix;
            File file = new File(path);
            if(file.exists()){
                logger.info(String.format("开始第%d次上传[%s]。",++uploadTimes,fileName));
                // 设置响应信息
                StringMap polity = new StringMap();
                polity.put("returnBody", CommunityUtil.getJSONString(0));
                // 生成上传凭证
                Auth auth = Auth.create(accessKey,secretKey);
                String uploadToken = auth.uploadToken(shareBucketName,fileName,3600,polity);
                // 指定上传的机房
                UploadManager manager = new UploadManager(new Configuration(Zone.zone2()));
                try {
                    // 开始上传图片
                    Response response = manager.put(
                            path,fileName,uploadToken,null,"image/png",false);
                    // 处理响应结果
                    JSONObject json = JSONObject.parseObject(response.bodyString());
                    if (json == null || json.get("code") == null || !json.get("code").toString().equals("0")){
                        logger.info(String.format("第%d次上传失败[%s]",uploadTimes,fileName));
                    } else {
                        logger.info(String.format("第%d次上传成功[%s]",uploadTimes,fileName));
                        future.cancel(true);
                    }
                }catch (QiniuException e){
                    logger.info(String.format("第%d次上传失败[%s]",uploadTimes,fileName));
                }
            }else {
                logger.info("等待图片生成[" + fileName + "].");
            }
        }
    }

}
