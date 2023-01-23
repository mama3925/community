package com.qiuyu.controller.advice;

import com.qiuyu.utils.CommunityUtil;
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
 * 程序出现异常,跳转error页面或返回JSON格式错误信息
 */
//annotations只扫描带Controller注解的Bean
@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {
    public static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    //发生异常时会被调用
    @ExceptionHandler
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.error("服务器发生异常:" + e.getMessage());

        // 循环打印异常栈中的每一条错误信息并记录
        for (StackTraceElement element : e.getStackTrace()) {
            logger.error(element.toString());
        }

        // 判断请求返回的是一个页面还是异步的JSON格式字符串
        String xRequestedWith = request.getHeader("x-requested-with");
        // XMLHttpRequest: Json格式字符串
        if ("XMLHttpRequest".equals(xRequestedWith)) {
            // 要求以JSON格式返回
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJSONString(1, "服务器异常!"));
        } else {
            //普通请求直接重定向到错误页面
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }
}
