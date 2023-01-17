package com.qiuyu.controller;

import com.qiuyu.bean.User;
import com.qiuyu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Map;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    /**
     * 跳转到请求页面
     * @return
     */
    @GetMapping("/register")
    public String getRegisterPage() {
        return "/site/register";
    }

    /**
     * 注册账号,发送邮箱
     */
    @PostMapping("/register")
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);

        if(map == null || map.isEmpty()){
            //注册成功
            model.addAttribute("msg","注册成功,我们已经向您的邮件发送了一封激活邮件,请尽快激活！");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }else{
            //注册失败
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "/site/register";
        }
    }
}
