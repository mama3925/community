package com.qiuyu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.qiuyu.bean.LoginTicket;
import com.qiuyu.bean.User;
import com.qiuyu.dao.LoginTicketMapper;
import com.qiuyu.dao.UserMapper;
import com.qiuyu.utils.CommunityConstant;
import com.qiuyu.utils.CommunityUtil;
import com.qiuyu.utils.MailClient;
import com.qiuyu.utils.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author QiuYuSY
 * @create 2023-01-16 20:46
 */

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;
//    @Autowired
//    private LoginTicketMapper loginTicketMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MailClient mailClient; //邮件客户端
    @Autowired
    private TemplateEngine templateEngine; //模板引擎

    @Value("${community.path.domain}")
    private String domain; //域名
    @Value("${server.servlet.context-path}")
    private String contextPath; //项目地址

    public User findUserById(String userId) {
//        return userMapper.selectById(Integer.parseInt(id));
        //优先从缓存中取值
        User user = getCache(Integer.parseInt(userId));
        if (user == null) {
            user = initCache(Integer.parseInt(userId));
        }
        return user;
    }

    public User findUserByName(String name) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(User::getUsername,name);
        return userMapper.selectOne(wrapper);
    }

    /**
     * 注册账号
     *
     * @param user
     * @return
     */
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();

        //空值处理
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空");
            return map;
        }

        //判断账号是否被注册
        Integer integer = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername()));
        if (integer > 0) {
            map.put("usernameMsg", "该账号已被注册");
            return map;
        }
        //判断邮箱是否被注册
        integer = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, user.getEmail()));
        if (integer > 0) {
            map.put("emailMsg", "该邮箱已被注册");
            return map;
        }


        //给用户加盐
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
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
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
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
     *
     * @param userId
     * @param activationCode
     * @return
     */
    public int activate(int userId, String activationCode) {
        //根据userid获取用户信息
        User user = userMapper.selectById(userId);

        if (user.getStatus() == 1) {
            //已经激活,则返回重复
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(activationCode)) {
            //如果未激活,判断激活码是否相等
            //激活账号
            user.setStatus(1);
//            userMapper.updateById(user);
            //redis优化后
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        } else {
            //不相等
            return ACTIVATION_FAILURE;
        }
    }


    /**
     * 登出
     * @param ticket 登录凭证
     */
    public void logout(String ticket) {
        //优化前:找到数据库中的ticket,把状态改为1

        //优化后：loginticket对象从redis中取出后状态设为1后放回
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        //放回
        redisTemplate.opsForValue().set(redisKey,loginTicket);
    }

    /**
     * 通过凭证号找到凭证
     *
     * @param ticket
     * @return
     */
    public LoginTicket findLoginTicket(String ticket) {
//        return loginTicketMapper.selectOne(new LambdaQueryWrapper<LoginTicket>()
//                .eq(LoginTicket::getTicket, ticket));

        //redis优化后:从redis中取出
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);

    }

    /**
     * 更新用户头像路径
     *
     * @param userId
     * @param headerUrl
     * @return
     */
    public int updateHeaderUrl(int userId, String headerUrl) {
        User user = new User();
        user.setId(userId);
        user.setHeaderUrl(headerUrl);
        int rows = userMapper.updateById(user);
        clearCache(userId);
        return rows;
    }

    /**
     * 更新密码
     * @param userId
     * @param oldPassword
     * @param newPassword
     * @return map返回信息
     */
    public Map<String, Object> updatePassword(int userId, String oldPassword,
                                              String newPassword) {
        Map<String, Object> map = new HashMap<>();

        //空值判断
        if(StringUtils.isBlank(oldPassword)){
            map.put("oldPasswordMsg","原密码不能为空");
            return map;
        }
        if(StringUtils.isBlank(newPassword)){
            map.put("newPasswordMsg","新密码不能为空");
            return map;
        }

        //根据userId获取对象
        User user = userMapper.selectById(userId);
        //旧密码加盐，加密
        oldPassword = CommunityUtil.md5(oldPassword+user.getSalt());
        //判断密码是否相等
        if(!user.getPassword().equals(oldPassword)){
            //不相等,返回
            map.put("oldPasswordMsg","原密码错误");
            return map;
        }

        //新密码加盐,加密
        newPassword = CommunityUtil.md5(newPassword+user.getSalt());

        user.setPassword(newPassword);
        userMapper.updateById(user);

        //map为空表示修改成功
        return map;
    }

    // 1.优先从缓存中取值
    private User getCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }
    // 2.取不到时,从数据库中取,然后初始化缓存数据(redis存值)
    private User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);

        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }
    // 3.数据变更时清除缓存(删除redis的key)
    private void clearCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

}
