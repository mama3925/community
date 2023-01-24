package com.qiuyu.aspect;


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

@Component
@Aspect
public class ServiceLogAspect {
    public static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);
    @Pointcut("execution(* com.qiuyu.service.*.*(..))")
    public void pointCut(){}

    @Before("pointCut()")
    public void before(JoinPoint joinPoint){
        // 用户ip[1.2.3.4],在[时间],访问了[com.qiuyu.service.xxx()].
        // 通过RequestContextHolder工具类获取request
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        //加入消费者后,消费者也会调用service,消费者没有request,所以这里会空指针
        if(attributes == null){
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        // 通过request.getRemoteHost获取当前用户ip
        String ip = request.getRemoteHost();
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        /**
         * joinPoint.getSignature().getDeclaringTypeName()-->得到类名com.qiuyu.service.*
         * joinPoint.getSignature().getName() -->方法名
         */
        String target = joinPoint.getSignature().getDeclaringTypeName() + "." +joinPoint.getSignature().getName();
        // String.format()加工字符串
        logger.info(String.format("用户[%s],在[%s],访问了[%s]业务.", ip, time, target));
    }

}
