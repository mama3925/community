package com.qiuyu.service;

import com.qiuyu.bean.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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


    @Test
    public void testRegister() {
        User user = new User();
        user.setUsername("qiuyu");
        user.setPassword("123456");
        user.setEmail("2448567284@qq.com");
        ;

        Map<String, Object> map = userService.register(user);

        System.out.println(map);
    }

    @Test
    public void testLoginOut(){
        String ticket = "9973d00c2c104d119c67539e2d5ccb6f";
        userService.logout(ticket);
    }


    @Test
    public void testUpdateHeaderUrl(){
        int userId = 163;
        String headerUrl = "http://images.nowcoder.com/head/111t.png";
        int i = userService.updateHeaderUrl(userId, headerUrl);
        System.out.println(i);
    }

    @Test
    public void testUpdatePassword(){
        Map<String, Object> map = userService.
                updatePassword(163, "123456", "123456789");

        Set<String> set = map.keySet();
        Iterator<String> iterator = set.iterator();
        if(iterator.hasNext()){
            String s = (String) map.get(iterator.next());
            System.out.println(s);
        }
    }


}
