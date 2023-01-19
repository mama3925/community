package com.qiuyu.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;

/**
 * @author QiuYuSY
 * @create 2023-01-19 16:22
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class CommunityUtilTest {

    @Test
    public void testJSON(){
        HashMap<String, Object> map = new HashMap<>();
        map.put("name","qiuyu");
        map.put("age",16);
        System.out.println(CommunityUtil.getJSONString(1));

        System.out.println(CommunityUtil.getJSONString(1,"fail"));

        System.out.println(CommunityUtil.getJSONString(1,"success",map));



    }


}
