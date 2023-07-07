package com.example.community.controller.advice;

import com.example.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 异常统一处理
 * @author Lenovo
 * @ControllerAdvice(annotations = Controller.class)表示有Controller注解的类都会被管理
 */
@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    /**
     * 处理Exception异常，即全部异常，
     * 处理多种的话可以在注解中按照下面格式写入：
     * @ExceptionHandler({XXX,XXX,XXX})
     * @param e
     * @param request
     * @param response
     */
    @ExceptionHandler({Exception.class})
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        //记录异常（概括）
        logger.error("服务器发生异常："+e.getMessage());
        //记录具体的异常
        for(StackTraceElement element : e.getStackTrace()){
            logger.error(element.toString());
        }
        //获取请求方式
        String xRequestedWith = request.getHeader("x-requested-with");
        if("XMLHttpRequest".equals(xRequestedWith)){
            //异步请求,返回一个普通字符串，需要前端用$.Json()方法进行转换
            response.setContentType("application/plain;charset=utf-8");
            //输出流
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJSONString(1,"服务器异常！"));
        } else {
            //普通请求，重定向到错误页面
            response.sendRedirect(request.getContextPath()+"/error");
        }


    }



}
