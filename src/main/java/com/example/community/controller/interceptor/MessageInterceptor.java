package com.example.community.controller.interceptor;

import com.example.community.entity.Message;
import com.example.community.entity.User;
import com.example.community.service.MessagerService;
import com.example.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class MessageInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessagerService messagerService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if(user!=null && modelAndView != null){
            int conversationUnreadCount = messagerService.findConversationUnreadCount(user.getId(), null);
            int noticeUnreadCount = messagerService.findNoticeUnreadCount(user.getId(), null);
            // 未读的私信数和未读的系统通知
            modelAndView.addObject("allUnreadCount",conversationUnreadCount+noticeUnreadCount);
        }
    }
}
