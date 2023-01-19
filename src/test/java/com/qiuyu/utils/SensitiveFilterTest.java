package com.qiuyu.utils;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class SensitiveFilterTest {
    @Autowired SensitiveFilter sensitiveFilter;

    @Test
    public void testFilter(){
        String oldText = "赌博  fuck f**k  shit ☆f☆uc☆k☆ ";
        String newText = sensitiveFilter.filter(oldText);
        System.out.println(newText);
        Assert.assertEquals("***  *** f**k  *** ☆***☆ ",newText);
    }
}
