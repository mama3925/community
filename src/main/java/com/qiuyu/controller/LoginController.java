package com.qiuyu.controller;

import com.google.code.kaptcha.Producer;
import com.qiuyu.bean.User;
import com.qiuyu.service.LoginService;
import com.qiuyu.service.UserService;
import com.qiuyu.utils.CommunityConstant;
import com.qiuyu.utils.CommunityUtil;
import com.qiuyu.utils.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private LoginService loginService;

    @Autowired
    private Producer kaptchaProducer;
    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    /**
     * 跳转到注册页面
     * @return
     */
    @GetMapping("/register")
    public String getRegisterPage() {
        return "/site/register";
    }

    /**
     * 跳转到登录页面
     * @return
     */
    @GetMapping("/login")
    public String getLoginPage() {
        return "/site/login";
    }

    /**
     * 注册账号,发送邮箱
     */
    @PostMapping("/register")
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);

        if (map == null || map.isEmpty()) {
            // 根据返回结果判断是否注册成功。若成功，跳转中转页面
            model.addAttribute("msg", "注册成功,我们已经向您的邮件发送了一封激活邮件,请尽快激活！");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        } else {
            // 注册失败，重新注册
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwdMsg", map.get("passwdMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }

    /**
     * 激活账户
     *
     * @param model
     * @param userId
     * @param code
     * @return
     */
    @GetMapping("/activation/{userId}/{code}")
    public String activate(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        int result = userService.activate(userId, code);
        if (result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功,你的账号已经可以正常使用了！");
            model.addAttribute("target", "/login");
        } else if (result == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "无效操作,该账号已经激活过了！");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活失败,你提供的激活码不正确！");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }

    /**
     * 验证码生成
     * @param response
     */
    @GetMapping("/kaptcha")
    public void getKaptcha(HttpServletResponse response){
        //生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        //优化前：将验证码存入session.....
        //session.setAttribute("kaptcha",text);

        //优化后：生成验证码的归属传给浏览器Cookie
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60); //s
        cookie.setPath(contextPath);
        response.addCookie(cookie);

        //优化后：将验证码存入Redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey, text, 60 , TimeUnit.SECONDS);


        //将图片输出到浏览器
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
            os.flush();
        } catch (IOException e) {
            logger.error("响应验证码失败:{}", e.getMessage());
        }
    }

    /**
     * 登录功能
     *
     * @param username
     * @param password
     * @param code
     * @param rememberme
     * @param model
     * @param response
     * @param kaptchaOwner
     * @return
     */
    @PostMapping("/login")
    public String login(String username, String password, String code, boolean rememberme, Model model,
                        HttpServletResponse response, @CookieValue("kaptchaOwner") String kaptchaOwner) {
        //优化前：首先检验验证码(从session取验证码)
        //String kaptcha = (String) session.getAttribute("kaptcha");

        String kaptcha = null;
        // 优化后：从redis中获取kaptcha的key
        if(!StringUtils.isBlank(kaptchaOwner)){
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            //获取redis中的验证码答案
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
            System.out.println(kaptcha);
        }


        if(StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)){
            //空值或者不相等
            model.addAttribute("codeMsg","验证码不正确");
            return "site/login";
        }

        /*
         * 1.验证用户名和密码(重点)
         * 2.传入浏览器cookie=ticket
         */
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = loginService.login(username, password, expiredSeconds);

        //登录成功
        if(map.containsKey("ticket")){
            Cookie cookie = new Cookie("ticket",map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        } else {
            // 登录失败
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login"; // 重定向
        }
    }

    /**
     * 退出登录功能
     * @CookieValue()注解:将浏览器中的Cookie值传给参数
     */
    @GetMapping("/logout")
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        return "redirect:/login"; // 重定向
    }
}
