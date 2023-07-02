package com.example.community.controller.interceptor;

import com.example.community.annotation.LoginRequired;
import com.example.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 检查登录状态的拦截器
 * @author Lenovo
 */
@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //handler是拦截的目标，如果是方法则拦截
        if(handler instanceof HandlerMethod){
            HandlerMethod handlerMethod = (HandlerMethod)handler;
            Method method = handlerMethod.getMethod();
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
            //拦截未登录的状态下访问带有LoginRequired注解的方法的请求
            if(loginRequired!=null && hostHolder.getUser() == null){
                //重定向到登录页面
                response.sendRedirect(request.getContextPath()+"/login");
                return false;
            }
        }


        return true;
    }
}
