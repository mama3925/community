package com.qiuyu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.qiuyu.bean.LoginTicket;
import com.qiuyu.bean.User;
import com.qiuyu.dao.LoginTicketMapper;
import com.qiuyu.dao.UserMapper;
import com.qiuyu.utils.CommunityUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author QiuYuSY
 * @create 2023-01-18 14:13
 */
@Service
public class LoginService {
    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 登录
     * @param username
     * @param password
     * @param expiredSeconds
     * @return
     */
    public Map<String,Object> login(String username, String password, int expiredSeconds){
        HashMap<String, Object> map = new HashMap<>();

        //空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg","用户名不能为空");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg","密码不能为空");
            return map;
        }

        //验证账号是否存在
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        if(user == null){
            map.put("usernameMsg","该账号不存在");
            return map;
        }

        //验证激活状态
        if(user.getStatus() == 0){
            map.put("usernameMsg","该账号未激活");
            return map;
        }

        //验证密码(先加密再对比)
        String pwdMd5 = CommunityUtil.md5(password + user.getSalt());
        if(!pwdMd5.equals(user.getPassword())){
            map.put("passwordMsg","密码错误");
            return map;
        }

        //生成登录凭证(相当于记住我这个功能==session)
        LoginTicket ticket = new LoginTicket();
        ticket.setUserId(user.getId());
        ticket.setTicket(CommunityUtil.generateUUID());
        ticket.setStatus(0); //有效
        //当前时间的毫秒数+过期时间毫秒数
        ticket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        Date date = new Date();

        loginTicketMapper.insert(ticket);

        map.put("ticket",ticket.getTicket());
        //map中能拿到ticket说明登录成功了
        return map;
    }


}
