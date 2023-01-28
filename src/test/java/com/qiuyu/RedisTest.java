package com.qiuyu;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;
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


    //统计20万个重复数据的独立总数
    @Test
    public void testHyperLogLog(){
        String redisKey = "test:hll:01";

        for (int i = 1; i < 10_0000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey,i);
        }

        for (int i = 1; i < 10_0000; i++) {
            int t = (int) (Math.random() * 10_0000 + 1);
            redisTemplate.opsForHyperLogLog().add(redisKey,t);
        }

        System.out.println(redisTemplate.opsForHyperLogLog().size(redisKey));//99553
    }

    //将多组数据合并,再统计合并后的重复数据的独立总数
    @Test
    public void testHyperLogLogUnion(){
        String redisKey2 = "test:hll:02";
        for (int i = 1; i < 10000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey2,i);
        }

        String redisKey3 = "test:hll:03";
        for (int i = 1; i < 15000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey3,i);
        }

        String redisKey4 = "test:hll:04";
        for (int i = 1; i < 20000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey4,i);
        }

        String unionKey = "test:hll:union";
        redisTemplate.opsForHyperLogLog().union(unionKey,redisKey2,redisKey3,redisKey4);

        System.out.println(redisTemplate.opsForHyperLogLog().size(unionKey));//19833
    }

    //统计一组数据的布尔值
    @Test
    public void testBitMap(){
        String redisKey = "test:bm:01";

        //记录
        redisTemplate.opsForValue().setBit(redisKey,1,true);
        redisTemplate.opsForValue().setBit(redisKey,3,true);
        redisTemplate.opsForValue().setBit(redisKey,7,true);

        //查询
        for (int i = 1; i <= 7; i++) {
            System.out.println(redisTemplate.opsForValue().getBit(redisKey,i));
            //true false true false false false true
        }

        //统计
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                //统计数组中1的个数
                return connection.bitCount(redisKey.getBytes());
            }
        });

        System.out.println(obj); //3
    }

    //统计3组数据的布尔值, 并对3组数据做OR运算
    @Test
    public void testBitMapOperation(){
        String redisKey2 = "test:bm:02";
        redisTemplate.opsForValue().setBit(redisKey2,0,true);
        redisTemplate.opsForValue().setBit(redisKey2,1,true);
        redisTemplate.opsForValue().setBit(redisKey2,2,true);

        String redisKey3 = "test:bm:03";
        redisTemplate.opsForValue().setBit(redisKey3,2,true);
        redisTemplate.opsForValue().setBit(redisKey3,3,true);
        redisTemplate.opsForValue().setBit(redisKey3,4,true);

        String redisKey4 = "test:bm:04";
        redisTemplate.opsForValue().setBit(redisKey4,4,true);
        redisTemplate.opsForValue().setBit(redisKey4,5,true);
        redisTemplate.opsForValue().setBit(redisKey4,6,true);

        String redisKey = "test:bm:or";
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(),
                        redisKey2.getBytes(), redisKey3.getBytes(), redisKey4.getBytes());
                return connection.bitCount(redisKey.getBytes());
            }
        });

        System.out.println(obj); //7
    }

}
