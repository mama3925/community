package com.qiuyu.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;
import java.util.Set;

/**
 * @author QiuYuSY
 * @create 2023-01-18 14:41
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class LoginServiceTest {

    @Autowired
    private LoginService loginService;


    @Test
    public void testLogin(){
        String username = "qiuyu666";
        String password = "123456";
        int expiredSeconds = 60 * 60;

        Map<String, Object> map = loginService.login(username, password, expiredSeconds);

        Set<Map.Entry<String, Object>> entries = map.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            System.out.println(entry.getKey()+" : "+entry.getValue());
        }
    }


}
