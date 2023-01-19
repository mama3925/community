package com.qiuyu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiuyu.bean.DiscussPost;
import com.qiuyu.dao.DiscussPostMapper;
import com.qiuyu.utils.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @author QiuYuSY
 * @create 2023-01-16 17:32
 */
@Service
public class DiscussPostService {
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private SensitiveFilter sensitiveFilter;

    /**
     * 查询不是被拉黑的帖子,并且userId不为0按照type排序
     * @param userId
     * @Param page
     * @return
     */
    public IPage<DiscussPost> findDiscussPosts(int userId, IPage<DiscussPost> page) {
        LambdaQueryWrapper<DiscussPost> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .ne(DiscussPost::getStatus, 2)
                .eq(userId != 0, DiscussPost::getUserId, userId)
                .orderByDesc(DiscussPost::getType, DiscussPost::getCreateTime);
        discussPostMapper.selectPage(page, queryWrapper);
        return page;
    }

    /**
     * 查询帖子数量
     * userId=0查所有;userId!=0查个人发帖数
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


    /**
     * 新增一条帖子
     * @param post 帖子
     * @return
     */
    public int addDiscussPost(DiscussPost post){
        if(post == null){
            //不用map直接抛异常
            throw new IllegalArgumentException("参数不能为空！");
        }

        //转义< >等HTML标签为 &lt; &gt 让浏览器认为是普通字符,防止被注入
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));

        //过滤敏感词
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));

        return discussPostMapper.insert(post);
    }

    /**
     * 通过id查找帖子
     * @param id
     * @return
     */
    public DiscussPost findDiscussPostById(int id){
        return discussPostMapper.selectById(id);
    }
}
