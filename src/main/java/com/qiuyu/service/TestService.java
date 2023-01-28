package com.qiuyu.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * @author QiuYuSY
 * @create 2023-01-27 23:34
 */
@Service
public class TestService {
    public static final Logger logger = LoggerFactory.getLogger(TestService.class);

    @Async
    public void task(){
        logger.debug("hello  " + Thread.currentThread().getName());
    }

//    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
    public void task2(){
        logger.debug("hello2  " + Thread.currentThread().getName());
    }
}
