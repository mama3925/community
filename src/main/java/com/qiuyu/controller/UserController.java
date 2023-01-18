package com.qiuyu.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author QiuYuSY
 * @create 2023-01-18 20:58
 */

@Controller
@RequestMapping("/user")
public class UserController {

    @GetMapping("/setting")
    public String getUserPage(){
        return "/site/setting";
    }
}
