package com.qiuyu;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

/**
 * @author QiuYuSY
 * @create 2023-01-22 19:46
 */

@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisTest {
    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testRedis(){
        String redisKey = "test:redis";
        redisTemplate.opsForValue().set(redisKey,1);
        System.out.println(redisTemplate.opsForValue().get(redisKey));
    }



    @Test
    public void testHash(){
        String redisKey = "test:redis2";
        redisTemplate.opsForHash().put(redisKey,"id",6);
        redisTemplate.opsForHash().put(redisKey,"username","qiuyu");
        Object id = redisTemplate.opsForHash().get(redisKey, "id");
        Object username = redisTemplate.opsForHash().get(redisKey, "username");
        System.out.println(id);
        System.out.println(username);
    }

    @Test
    public void testList(){
        String redisKey = "test:redis3";
        redisTemplate.opsForList().leftPush(redisKey,101);
        redisTemplate.opsForList().leftPush(redisKey,102);
        redisTemplate.opsForList().leftPush(redisKey,103);

        System.out.println(redisTemplate.opsForList().size(redisKey));
        System.out.println(redisTemplate.opsForList().index(redisKey,0));
        System.out.println(redisTemplate.opsForList().range(redisKey,0,2));

        System.out.println(redisTemplate.opsForList().rightPop(redisKey));
        System.out.println(redisTemplate.opsForList().rightPop(redisKey));
        /*
        3
        103
        [103, 102, 101]
        101
        102
         */
    }

    @Test
    public void testSet(){
        String redisKey = "test:redis4";
        redisTemplate.opsForSet().add(redisKey,"bbb","ccc","aaa");

        System.out.println(redisTemplate.opsForSet().size(redisKey));
        System.out.println(redisTemplate.opsForSet().pop(redisKey));
        System.out.println(redisTemplate.opsForSet().members(redisKey));
        /*
        3
        bbb
        [aaa, ccc]
         */
    }

    @Test
    public void testZSet(){
        String redisKey = "test:redis5";
        redisTemplate.opsForZSet().add(redisKey,"aaa",80);
        redisTemplate.opsForZSet().add(redisKey,"bbb",90);
        redisTemplate.opsForZSet().add(redisKey,"ccc",60);
        redisTemplate.opsForZSet().add(redisKey,"ddd",100);
        redisTemplate.opsForZSet().add(redisKey,"eee",50);

        System.out.println(redisTemplate.opsForZSet().size(redisKey));
        System.out.println(redisTemplate.opsForZSet().score(redisKey,"bbb"));
        System.out.println(redisTemplate.opsForZSet().rank(redisKey,"bbb"));
        System.out.println(redisTemplate.opsForZSet().reverseRank(redisKey,"bbb"));
        System.out.println(redisTemplate.opsForZSet().range(redisKey,0,2));
        System.out.println(redisTemplate.opsForZSet().reverseRange(redisKey,0,2));
        /*
        5
        90.0
        3
        1
        [eee, ccc, aaa]
        [ddd, bbb, aaa]
         */
    }

    @Test
    public void testKeys(){
        redisTemplate.delete("aaa");
        System.out.println(redisTemplate.hasKey("aaa"));
        redisTemplate.expire("test:redis",10, TimeUnit.SECONDS);
    }

    @Test
    public void testBoundOperations(){
        String redisKey = "test:count3";
        BoundValueOperations operations = redisTemplate.boundValueOps(redisKey);
        operations.set(1);
        //报错
//        operations.increment();
//        operations.increment();
//        operations.increment();
        System.out.println(operations.get());
    }

    //编程式事务
    @Test
    public void testTransaction(){
        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String redisKey = "test:tx";
                //启用事务
                operations.multi();

                operations.opsForSet().add(redisKey,"zhangsan");
                operations.opsForSet().add(redisKey,"lisi");
                operations.opsForSet().add(redisKey,"wangwu");

                //redis会把这些操作放在队列中.提交事务时才执行,所以此时还没有数据
                System.out.println(operations.opsForSet().members(redisKey));

                //提交事务
                return operations.exec();
            }
        });

        System.out.println(obj);
        //[]
        //[1, 1, 1, [lisi, zhangsan, wangwu]]
    }

}
