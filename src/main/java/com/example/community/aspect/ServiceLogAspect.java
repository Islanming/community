package com.example.community.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 使用AOP实现业务层的统一记录日志
 * @author Lenovo
 */
//@Component
//@Aspect
public class ServiceLogAspect {
    private static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);

    /**
     * 使用@Pointcut注解定义了一个名为serviceMethods的切点。
     * 该切点匹配com.example.community.service包中的所有方法。
     */
    @Pointcut("execution(* com.example.community.service.*.*(..))")
    public void pointcut(){

    }

    /**
     * 在织入点之前记录日志
     * @param joinPoint 织入点
     */
    @Before("pointcut()")
    public void before(JoinPoint joinPoint){
        // 日志格式：用户[1.2.3.4]（IP），在[xxx]（时间）,访问了[com.example.community.service.xxx()].
        //获取用户IP
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        // attributes 是与请求有关的，之前的service都是通过controller层调用，有请求的数据，现在通过EventConsumer调用，请求会为空，故需要对空值进行处理，防止空指针异常
        if(attributes == null){
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getRemoteHost();
        //获取时间
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        //获取方法
        String target = joinPoint.getSignature().getDeclaringTypeName()+"."+joinPoint.getSignature().getName();

        logger.info(String.format("用户[%s],在[%s],访问了[%s].",ip,now,target));

    }

}
