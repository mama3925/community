package com.qiuyu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiuyu.bean.DiscussPost;
import com.qiuyu.dao.DiscussPostMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author QiuYuSY
 * @create 2023-01-16 17:32
 */
@Service
public class DiscussPostService {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    /**
     * 查询不是被拉黑的帖子并且userId不为0按照type排序
     *
     * @param userId
     * @param currentPage
     * @param pageSize
     * @return
     */
    public Page<DiscussPost> findDiscussPosts(int userId, int currentPage, int pageSize) {
        Page<DiscussPost> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<DiscussPost> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .ne(DiscussPost::getStatus, 2)
                .eq(userId != 0, DiscussPost::getUserId, userId)
                .orderByDesc(DiscussPost::getType, DiscussPost::getCreateTime);
        discussPostMapper.selectPage(page, queryWrapper);
        return page;
    }

    /**
     * userId=0查所有;userId!=0查个人发帖数
     *
     * @param userId
     * @return
     */
    public int findDiscussPostRows(int userId) {
        LambdaQueryWrapper<DiscussPost> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .ne(DiscussPost::getStatus, 2)
                .eq(userId != 0, DiscussPost::getUserId, userId);
        int nums = discussPostMapper.selectCount(queryWrapper);
        return nums;
    }
}
