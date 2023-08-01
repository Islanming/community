package com.example.community.controller.interceptor;

import com.example.community.entity.User;
import com.example.community.service.DataService;
import com.example.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 访问则进行记录，将IP计入UV或者将用户ID计入DAU，则需要在每一次访问处理前进行记录
 * @author Lenovo
 */
@Component
public class DateInterceptor implements HandlerInterceptor {
    @Autowired
    private DataService dataService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 统计UV
        String ip = request.getRemoteHost();
        dataService.recordUV(ip);

        // 统计DAU
        User user = hostHolder.getUser();
        if (user != null){
            dataService.recordDAU(user.getId());
        }

        return true;
    }
}
