package com.qiuyu.controller;

import com.qiuyu.bean.User;
import com.qiuyu.service.UserService;
import com.qiuyu.utils.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Map;

@Controller
public class LoginController implements CommunityConstant {

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
            //注册成功,跳转到中转页面
            model.addAttribute("msg","注册成功,我们已经向您的邮件发送了一封激活邮件,请尽快激活！");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }else{
            //注册失败,重新注册
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "/site/register";
        }
    }

    /**
     * 激活邮箱 http://localhost:8080/community/activation/101/code 激活链接
     * @param model
     * @param userId
     * @param code
     * @return
     */
    @GetMapping("/activation/{userId}/{code}")
    public String activate(Model model,
                           @PathVariable("userId") int userId,
                           @PathVariable("code") String code) {
        int result = userService.activate(userId, code);
        if (result == ACTIVATION_SUCCESS){
            model.addAttribute("msg","激活成功,你的账号已经可以正常使用了！");
            model.addAttribute("target","/login");
        }else if (result == ACTIVATION_REPEAT){
            model.addAttribute("msg","无效操作,该账号已经激活过了！");
            model.addAttribute("target","/index");
        }else {
            model.addAttribute("msg","激活失败,你提供的激活码不正确！");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
    }

}
