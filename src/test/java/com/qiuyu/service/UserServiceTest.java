package com.qiuyu.service;

import com.qiuyu.bean.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author QiuYuSY
 * @create 2023-01-16 20:47
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    public void testSelectUserById() {
        String id = "101";
        User user = userService.findUserById(id);
        System.out.println(user);
    }

}
