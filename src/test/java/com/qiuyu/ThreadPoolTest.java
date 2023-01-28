package com.qiuyu;

import com.mysql.cj.log.LogFactory;
import com.qiuyu.service.TestService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.concurrent.*;

/**
 * @author QiuYuSY
 * @create 2023-01-27 22:49
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class ThreadPoolTest {
    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolTest.class);

    //JDK普通线程池
    ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2,
            5,
            60,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.AbortPolicy()
    );
    //JDK定时线程池
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

    private void sleep(int t) {
        try {
            Thread.sleep(t);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    //Spring普通线程池
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    //Spring定时线程池
    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;
    @Autowired
    private TestService testService;

    /**
     * JDK普通线程池测试
     */
    @Test
    public void testJDK1(){
        for (int i = 0; i < 10; i++) {
            threadPoolExecutor.submit(()->{
                logger.debug("Hello!");
            });
        }

    }

    /**
     * JDK定时线程池测试
     */
    @Test
    public void testJDK2(){
        // 任务 多久后开始(延迟) 间隔 时间单位
        scheduledExecutorService.scheduleAtFixedRate(()->{
            logger.debug("Hello!");
        }, 10, 1,TimeUnit.SECONDS);

        sleep(30000);
    }


    /**
     * Spring普通线程池
     */
    @Test
    public void testSpringExecutors(){
        for (int i = 0; i < 10; i++) {
            threadPoolTaskExecutor.submit(()->{
                logger.debug("hello!");
            });
        }
    }

    /**
     * Spring定时线程池
     */
    @Test
    public void testSpringExecutors2(){
        //开始进行任务的时间
        Date startTime = new Date(System.currentTimeMillis() + 5000);

        threadPoolTaskScheduler.scheduleAtFixedRate(() -> logger.debug("Hello!"), startTime, 1000);

        sleep(30000);
    }



    @Test
    public void testSpringExecutors3(){
        for (int i = 0; i < 10; i++) {
            testService.task();
        }
    }

//    @Test
//    public void testSpringExecutors4(){
//        for (int i = 0; i < 10; i++) {
//            testService.task2();
//        }
//        sleep(10000);
//    }


}
