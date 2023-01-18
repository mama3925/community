package com.qiuyu.controller;

import com.qiuyu.utils.CommunityUtil;
import org.junit.Test;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author QiuYuSY
 * @create 2023-01-17 23:50
 */
@Controller
public class TestController {

    @GetMapping("/cookie/set")
    @ResponseBody
    public String setCookie(HttpServletResponse response) {
        //创建Cookie
        Cookie cookie = new Cookie("code", CommunityUtil.generateUUID());
        //设置Cookie生效范围
        cookie.setPath("/community");
        //设置cookie有效时间(s)
        cookie.setMaxAge(60 * 10);
        //发送Cookie
        response.addCookie(cookie);

        return "setCookie";
    }

    @GetMapping("/cookie/get")
    @ResponseBody
    public String getCookie(@CookieValue("code") String code) {

        return code;
    }


    @GetMapping("/session/set")
    @ResponseBody
    public String setSession(HttpSession session) {
        session.setAttribute("id",1);
        session.setAttribute("name","Test");
        session.setAttribute("pwd","ASDASDDADASD");
        return "setSession";
    }

    @GetMapping("/session/get")
    @ResponseBody
    public String getSession(HttpSession session) {
        System.out.println(session.getAttribute("id"));
        System.out.println(session.getAttribute("name"));
        System.out.println(session.getAttribute("pwd"));
        return "getSession";
    }
}
