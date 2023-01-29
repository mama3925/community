package com.qiuyu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.qiuyu.bean.DiscussPost;
import com.qiuyu.bean.MyPage;
import com.qiuyu.dao.DiscussPostMapper;
import com.qiuyu.utils.SensitiveFilter;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author QiuYuSY
 * @create 2023-01-16 17:32
 */
@Slf4j
@Service
public class DiscussPostService {

    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Value("${caffeine.post.max-size}")
    private int maxSize;
    @Value("${caffeine.post.expire-seconds}")
    private int expireSeconds;

    // 帖子列表缓存
    private LoadingCache<String, MyPage<DiscussPost>> postPageCache;

    // 项目启动时初始化缓存
    @PostConstruct
    public void init() {
        // 初始化帖子列表缓存
        postPageCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds,TimeUnit.SECONDS)
                .build(new CacheLoader<String, MyPage<DiscussPost>>(){
                    @Override
                    public @Nullable MyPage<DiscussPost> load(@NonNull String key) throws Exception {
                        if (key == null || key.length() == 0) {
                            throw new IllegalArgumentException("参数错误!");
                        }
                        String[] params = key.split(":");
                        if (params == null || params.length != 3) {
                            throw new IllegalArgumentException("参数错误!");
                        }

                        //拆分key
                        int current = Integer.valueOf(params[0]);
                        int size = Integer.valueOf(params[1]);
                        String path = params[2];

                        // 这里可用二级缓存：Redis -> mysql

                        //本地缓存中查不到,从数据库中查询,查到后会自动存入本地缓存
                        log.debug("正在从数据库加载热门帖子总数！");
                        MyPage<DiscussPost> page = new MyPage<>();
                        page.setCurrent(current);
                        page.setSize(size);
                        page.setPath(path);
                        LambdaQueryWrapper<DiscussPost> queryWrapper = new LambdaQueryWrapper<>();
                        queryWrapper
                                .ne(DiscussPost::getStatus, 2)
                                .orderByDesc( DiscussPost::getType, DiscussPost::getScore, DiscussPost::getCreateTime);

                        discussPostMapper.selectPage(page, queryWrapper);

                        return page;
                    }
                });
    }


    /**
     * 查询没被拉黑的帖子,并且userId不为0按照type排序
     *
     * @param userId
     * @param orderMode 0-最新 1-最热
     * @param page
     * @return
     */
    public MyPage<DiscussPost> findDiscussPosts(int userId, int orderMode, MyPage<DiscussPost> page) {
        //全部查询并且查的是热门帖子的话先去缓存查询
        if (userId == 0 && orderMode == 1) {
            //按照当前页和页面最大值作为Key查询
            log.debug("正在从Caffeine缓存中加载热门帖子！");
            return postPageCache.get(page.getCurrent()+":"+ page.getSize()+":"+ page.getPath());
        }

        log.debug("正在从数据库加载热门帖子总数！");
        //从数据库中查
        LambdaQueryWrapper<DiscussPost> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .ne(DiscussPost::getStatus, 2)
                .eq(userId != 0, DiscussPost::getUserId, userId)
                .orderBy(orderMode == 0, false, DiscussPost::getType, DiscussPost::getCreateTime)
                .orderBy(orderMode == 1, false, DiscussPost::getType, DiscussPost::getScore, DiscussPost::getCreateTime);

        discussPostMapper.selectPage(page, queryWrapper);

        return page;
    }

    /**
     * 查询帖子数量
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


    /**
     * 新增一条帖子
     *
     * @param post 帖子
     * @return
     */
    public int addDiscussPost(DiscussPost post) {
        if (post == null) {
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
     *
     * @param id
     * @return
     */
    public DiscussPost findDiscussPostById(int id) {
        return discussPostMapper.selectById(id);
    }

    /**
     * 根据帖子id修改帖子的评论数量
     *
     * @param id
     * @param commentCount
     * @return
     */
    public int updateCommentCount(int id, int commentCount) {
        DiscussPost discussPost = new DiscussPost();
        discussPost.setId(id);
        discussPost.setCommentCount(commentCount);
        return discussPostMapper.updateById(discussPost);
    }

    /**
     * 修改帖子类型
     *
     * @param id
     * @param type
     * @return
     */
    public int updateType(int id, int type) {
        DiscussPost discussPost = new DiscussPost();
        discussPost.setId(id);
        discussPost.setType(type);
        return discussPostMapper.updateById(discussPost);
    }

    /**
     * 修改帖子状态
     *
     * @param id
     * @param status
     * @return
     */
    public int updateStatus(int id, int status) {
        DiscussPost discussPost = new DiscussPost();
        discussPost.setId(id);
        discussPost.setStatus(status);
        return discussPostMapper.updateById(discussPost);
    }

    public int updateScore(int postId, double score) {
        DiscussPost discussPost = new DiscussPost();
        discussPost.setId(postId);
        discussPost.setScore(score);
        return discussPostMapper.updateById(discussPost);
    }
}
