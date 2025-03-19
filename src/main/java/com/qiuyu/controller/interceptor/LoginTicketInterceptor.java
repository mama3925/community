package com.qiuyu.controller.interceptor;

import com.qiuyu.bean.LoginTicket;
import com.qiuyu.bean.User;
import com.qiuyu.service.UserService;
import com.qiuyu.utils.CookieUtil;
import com.qiuyu.utils.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * 登录凭证拦截器,用于根据凭证号获取用户,并传给视图
 */
@Component
public class LoginTicketInterceptor implements HandlerInterceptor {
    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //从request中获取cookie 凭证
        String ticket = CookieUtil.getValue(request, "ticket");

        if (!StringUtils.isBlank(ticket)) {
            // 查询凭证
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            // 检查凭证是否有效
            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
                // 根据凭证查询用户
                User user = userService.findUserById(String.valueOf(loginTicket.getUserId()));
                // 把用户存入ThreadLocal
                hostHolder.setUser(user);

//                /**
//                 * 构建用户认证结果,并存入SecurityContext,以便于Security进行授权
//                 */
//                Authentication authentication = new UsernamePasswordAuthenticationToken(
//                        user, user.getPassword(), userService.getAuthorities(user.getId()));
//                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        //在调用模板引擎之前,把user给model
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            modelAndView.addObject("loginUser",user);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //最后把ThreadLocal中的当前user删除
        hostHolder.clear();

        // 释放SecurityContext资源
//        SecurityContextHolder.clearContext();
    }
}
