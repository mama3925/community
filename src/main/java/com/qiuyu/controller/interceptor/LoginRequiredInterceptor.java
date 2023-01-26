package com.qiuyu.controller.interceptor;


import com.qiuyu.annotation.LoginRequired;
import com.qiuyu.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * @LoginRequired的拦截器实现
 */
@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {
    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断拦截的是否为方法
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            //获取拦截到的方法对象
            Method method = handlerMethod.getMethod();
            //获取注解
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
            //如果这个方法被@LoginRequired注解,并且未登录,跳转并拦截!
            if (loginRequired != null && hostHolder.getUser() == null) {
                response.sendRedirect(request.getContextPath()+"/login");
                return false;
            }
            //
        }

        return true;
    }


}
