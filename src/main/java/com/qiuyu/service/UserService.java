package com.qiuyu.service;

import com.qiuyu.bean.User;
import com.qiuyu.dao.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author QiuYuSY
 * @create 2023-01-16 20:46
 */

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public User findUserById(String id) {
        return userMapper.selectById(Integer.parseInt(id));
    }

}
