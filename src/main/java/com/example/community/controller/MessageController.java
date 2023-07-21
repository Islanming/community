package com.example.community.controller;

import com.alibaba.fastjson2.JSONObject;
import com.example.community.entity.Message;
import com.example.community.entity.Page;
import com.example.community.entity.User;
import com.example.community.service.MessagerService;
import com.example.community.service.UserService;
import com.example.community.util.CommunityConstant;
import com.example.community.util.CommunityUtil;
import com.example.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
public class MessageController implements CommunityConstant {

    @Autowired
    private MessagerService messagerService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    /**
     * 私信列表
     * @param model
     * @param page
     * @return
     */
    @RequestMapping(path = "/letter/list",method = RequestMethod.GET)
    public String getLetterList(Model model, Page page){
        User user = hostHolder.getUser();
        //分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messagerService.findConversationCount(user.getId()));

        //会话列表
        List<Message> conversationList = messagerService.findConversations(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String,Object>> conversations = new ArrayList<>();
        if(conversationList != null){
            for (Message message:conversationList) {
                Map<String,Object> map = new HashMap<>();
                map.put("conversation",message);
                map.put("letterCount",messagerService.findLetterCount(message.getConversationId()));
                map.put("unreadCount",messagerService.findConversationUnreadCount(user.getId(), message.getConversationId()));
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target",userService.findUserById(targetId));

                conversations.add(map);
            }
        }
        model.addAttribute("conversations",conversations);

        //查询未读私信数
        int letterUnreadCount = messagerService.findConversationUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);

        // 查询未读通知数量
        int noticeUnreadCount = messagerService.findNoticeUnreadCount(user.getId(),null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);

        return "/site/letter";

    }

    /**
     * 获取私信详情
     * @param conversationId
     * @param page
     * @param model
     * @return
     */
    @RequestMapping(path = "/letter/detail/{conversationId}",method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId,Page page,Model model){
        //设置分页信息
        page.setLimit(5);
        page.setPath("/letter/detail/"+conversationId);
        page.setRows(messagerService.findLetterCount(conversationId));

        //私信列表
        List<Message> letterList = messagerService.findLetters(conversationId, page.getOffset(), page.getLimit());

        List<Map<String,Object>> letters = new ArrayList<>();
        if(letterList != null){
            for (Message letter:letterList) {
                Map<String,Object> map = new HashMap<>();
                map.put("letter",letter);
                map.put("fromUser",userService.findUserById(letter.getFromId()));

                letters.add(map);
            }
        }
        model.addAttribute("letters",letters);

        //设置私信对象
        model.addAttribute("target",getLetterTarget(conversationId));

        //设置已读
        List<Integer> unreadIds = getLetterIds(letterList);
        if(!unreadIds.isEmpty()){
            messagerService.readMessage(unreadIds);
        }

        return "/site/letter-detail";
    }

    /**
     * 获取私信对象
     * @param conversationId
     * @return
     */
    private User getLetterTarget(String conversationId){
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if(hostHolder.getUser().getId() == id0){
            return userService.findUserById(id1);
        }

        return userService.findUserById(id0);

    }

    /**
     * 获取未读消息的id
     * @param letterList
     * @return
     */
    private List<Integer> getLetterIds(List<Message> letterList){
        List<Integer> ids = new ArrayList<>();
        if(letterList!=null){
            for (Message letter:letterList) {
                if(hostHolder.getUser().getId() == letter.getToId() && letter.getStatus() == 0){
                    ids.add(letter.getId());
                }
            }
        }
        return ids;
    }


    /**
     * 发送私信，是异步请求
     * @param toName
     * @param content
     * @return
     */
    @RequestMapping(path = "/letter/send",method = RequestMethod.POST)
    @ResponseBody
    public String sendletter(String toName,String content){

        User target = userService.findUserByName(toName);
        if(target == null){
            return CommunityUtil.getJSONString(1,"目标用户不存在！");
        }

        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        // 根据发送者id和接收id拼接会话id，小的拼在前面
        if(message.getFromId() < message.getToId()){
            message.setConversationId(message.getFromId()+"_"+message.getToId());
        }else {
            message.setConversationId(message.getToId()+"_"+message.getFromId());
        }
        message.setContent(content);
        //默认为未读
        message.setStatus(0);
        message.setCreateTime(new Date());

        messagerService.addMessage(message);
        return CommunityUtil.getJSONString(0);
    }

    /**
     * 显示通知列表
     * @param model
     * @return
     */
    @RequestMapping(path = "/notice/list",method = RequestMethod.GET)
    public String getNoticeList(Model model){
        User user = hostHolder.getUser();
        // 查询评论类的通知
        Message message = messagerService.findLatestNotice(user.getId(),TOPIC_COMMENT);
        Map<String,Object> messageVO = new HashMap<>();
         messageVO.put("message",message);
        if(message != null){
            // 去掉content里面的转义字符
            String content = HtmlUtils.htmlUnescape(message.getContent());
            // 将json字符串转成map，因为存的时候是用map存的，便于取用
            Map<String,Object> data = JSONObject.parseObject(content,HashMap.class);

            messageVO.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("entityId",data.get("entityId"));
            messageVO.put("postId",data.get("postId"));

            //查询评论类通知的数量
            int count = messagerService.findNoticeCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("count",count);

            //查询评论类未读通知的数量
            int unread = messagerService.findNoticeUnreadCount(user.getId(),TOPIC_COMMENT);
            messageVO.put("unread",unread);
        }
        model.addAttribute("commentNotice",messageVO);

        // 查询点赞类的通知
        message = messagerService.findLatestNotice(user.getId(),TOPIC_LIKE);
        messageVO = new HashMap<>();
        messageVO.put("message",message);
        if(message != null){
            // 去掉content里面的转义字符
            String content = HtmlUtils.htmlUnescape(message.getContent());
            // 将json字符串转成map，因为存的时候是用map存的，便于取用
            Map<String,Object> data = JSONObject.parseObject(content,HashMap.class);

            messageVO.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("entityId",data.get("entityId"));
            messageVO.put("postId",data.get("postId"));

            //查询评论类通知的数量
            int count = messagerService.findNoticeCount(user.getId(), TOPIC_LIKE);
            messageVO.put("count",count);

            //查询评论类未读通知的数量
            int unread = messagerService.findNoticeUnreadCount(user.getId(),TOPIC_LIKE);
            messageVO.put("unread",unread);
        }
        model.addAttribute("likeNotice",messageVO);

        // 查询关注类的通知
        message = messagerService.findLatestNotice(user.getId(),TOPIC_FOLLOW);
        messageVO = new HashMap<>();
        messageVO.put("message",message);
        if(message != null){
            // 去掉content里面的转义字符
            String content = HtmlUtils.htmlUnescape(message.getContent());
            // 将json字符串转成map，因为存的时候是用map存的，便于取用
            Map<String,Object> data = JSONObject.parseObject(content,HashMap.class);

            messageVO.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("entityId",data.get("entityId"));

            //查询评论类通知的数量
            int count = messagerService.findNoticeCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("count",count);

            //查询评论类未读通知的数量
            int unread = messagerService.findNoticeUnreadCount(user.getId(),TOPIC_FOLLOW);
            messageVO.put("unread",unread);
        }
        model.addAttribute("followNotice",messageVO);

        // 查询未读私信数量
        int letterUnreadCount = messagerService.findConversationUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);

        // 查询未读通知数量
        int noticeUnreadCount = messagerService.findNoticeUnreadCount(user.getId(),null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);

        return "/site/notice";
    }

    /**
     * 通知详情，支持分页
     * @param topic
     * @param page
     * @param model
     * @return
     */
    @RequestMapping(path = "/notice/detail/{topic}",method = RequestMethod.GET)
    public String getNoticeDetail(@PathVariable("topic") String topic,Page page,Model model){
        User user = hostHolder.getUser();

        page.setLimit(5);
        page.setPath("/notice/detail/"+topic);
        page.setRows(messagerService.findNoticeCount(user.getId(),topic));

        List<Message> noticeList = messagerService.findNotices(user.getId(),topic, page.getOffset(), page.getLimit());
        List<Map<String,Object>> noticeVoList = new ArrayList<>();
        if(noticeList != null){
            for (Message notice : noticeList) {
                Map<String,Object> map = new HashMap<>();
                // 通知
                map.put("notice",notice);

                // 内容
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String,Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user",userService.findUserById((Integer) data.get("userId")));
                map.put("entityType",data.get("entityType"));
                map.put("entityId",data.get("entityId"));
                map.put("postId",data.get("postId"));

                // 通知的作者
                map.put("fromUser",userService.findUserById(notice.getFromId()));

                noticeVoList.add(map);
            }
        }
        model.addAttribute("notices",noticeVoList);

        // 设置已读
        List<Integer> ids = getLetterIds(noticeList);
        if(!ids.isEmpty()){
            messagerService.readMessage(ids);
        }

        return "/site/notice-detail";
    }


}
