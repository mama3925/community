package com.qiuyu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qiuyu.bean.User;
import com.qiuyu.dao.UserMapper;
import com.qiuyu.utils.CommunityConstant;
import com.qiuyu.utils.CommunityUtil;
import com.qiuyu.utils.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author QiuYuSY
 * @create 2023-01-16 20:46
 */

@Service
public class UserService implements CommunityConstant{

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MailClient mailClient; //邮件客户端
    @Autowired
    private TemplateEngine templateEngine; //模板引擎

    @Value("${community.path.domain}")
    private String domain; //域名
    @Value("${server.servlet.context-path}")
    private String contextPath; //项目地址

    public User findUserById(String id) {
        return userMapper.selectById(Integer.parseInt(id));
    }

    public Map<String,Object> register(User user){
        Map<String,Object> map = new HashMap<>();

        //空值处理
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg", "账号不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg", "密码不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg", "邮箱不能为空");
            return map;
        }

        //判断账号是否被注册
        Integer integer = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername()));
        if(integer > 0){
            map.put("usernameMsg", "该账号已被注册");
            return map;
        }
        //判断邮箱是否被注册
        integer = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, user.getEmail()));
        if(integer > 0){
            map.put("emailMsg", "该邮箱已被注册");
            return map;
        }


        //给用户加盐
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        //加密
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        //初始化其他数据
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        //注册用户
        userMapper.insert(user);


        //激活邮件
        //创建数据
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        //http://localhost:8080/community/activation/101/code 激活链接
        String url = domain + contextPath + "/activation/"+ user.getId()+"/" + user.getActivationCode();
        context.setVariable("url", url);

        //根据模板,放入数据
        String content = templateEngine.process("/mail/activation", context);
        //发送
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        //map为空则注册成功
        return map;
    }


    /**
     * 激活账号
     * @param userId
     * @param activationCode
     * @return
     */
    public int activate(int userId, String activationCode) {
        //根据userid获取用户信息
        User user = userMapper.selectById(userId);

        if(user.getStatus() == 1){
            //已经激活,则返回重复
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode() .equals(activationCode)) {
            //如果未激活,判断激活码是否相等
            //激活账号
            user.setStatus(1);
            userMapper.updateById(user);
            return ACTIVATION_SUCCESS;
        } else {
            //不相等
            return ACTIVATION_FAILURE;
        }
    }


}
