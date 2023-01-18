package com.qiuyu.utils;

import com.qiuyu.bean.User;
import org.springframework.stereotype.Component;

/**
 * 持有用户信息,代替session对象
 */
@Component  //放入容器里不用设为静态方法
public class HostHolder {
    //key就是线程对象，值为线程的变量副本
    private ThreadLocal<User> users = new ThreadLocal<>();

    /**
     * 以线程为key存入User
     * @param user
     */
    public void setUser(User user){
        users.set(user);
    }

    /**
     * 从ThreadLocal线程中取出User
     * @return
     */
    public User getUser(){
        return users.get();
    }

    /**
     * 从ThreadLocal线程中删除user
     */
    public void clear(){
        users.remove();
    }
}
