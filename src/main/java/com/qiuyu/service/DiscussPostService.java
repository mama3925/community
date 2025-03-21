package com.qiuyu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qiuyu.bean.DiscussPost;
import com.qiuyu.dao.DiscussPostMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DiscussPostService {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    /**
     * 查询不是被拉黑的帖子,并且userId不为0,按照type降序排序
     * @param userId
     * @param page
     * @return
     */
    public IPage<DiscussPost> findDiscussPosts(int userId, IPage<DiscussPost> page) {
        LambdaQueryWrapper<DiscussPost> queryWrapper = new LambdaQueryWrapper<DiscussPost>();
        queryWrapper.ne(DiscussPost::getStatus, 2)
                .eq(userId != 0, DiscussPost::getUserId, userId)
                .orderByDesc(DiscussPost::getType, DiscussPost::getCreateTime);
        discussPostMapper.selectPage(page, queryWrapper);
        return page;
    }

    /**
     * 统计帖子数量，userId=0时存了所有帖子
     * @param userId
     * @return
     */
    public int findDiscussPostRows(int userId) {
        LambdaQueryWrapper<DiscussPost> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ne(DiscussPost::getStatus, 2)
                .eq(userId != 0, DiscussPost::getUserId, userId);
        int nums = discussPostMapper.selectCount(queryWrapper);
        return nums;
    }

}
