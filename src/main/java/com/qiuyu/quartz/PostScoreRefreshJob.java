package com.qiuyu.quartz;

import com.qiuyu.bean.DiscussPost;
import com.qiuyu.service.DiscussPostService;
import com.qiuyu.service.ElasticsearchService;
import com.qiuyu.service.LikeService;
import com.qiuyu.utils.CommunityConstant;
import com.qiuyu.utils.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 计算帖子的分数
 */
@Component
public class PostScoreRefreshJob implements Job, CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private ElasticsearchService elasticsearchService;

    // 网站创建时间
    private static final Date epoch;

    //初始化日期
    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-10-22 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化时间失败!", e);
        }
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String redisKey = RedisKeyUtil.getPostScore();
        // 处理每一个key
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);

        if (operations.size() == 0) {
            logger.info("[任务取消] 没有需要刷新的帖子");
            return;
        }

        logger.info("[任务开始] 正在刷新帖子分数" + operations.size());
        while (operations.size() > 0) {
            // 刷新每一个从set集合里弹出的postId
            this.refresh((Integer) operations.pop());
        }
        logger.info("[任务结束] 帖子分数刷新完毕！");

    }

    // 从redis中取出每一个value:postId
    private void refresh(int postId) {
        DiscussPost post = discussPostService.findDiscussPostById(postId);
        if (post == null) {
            logger.error("该帖子不存在：id = " + postId);
            return;
        }
        if (post.getStatus() == 2) {
            logger.error("帖子已被删除");
            return;
        }

        /**
         * 帖子分数计算公式：[加精（75）+ 评论数*  10 + 点赞数*  2] + 距离天数
         */
        // 是否加精帖子
        boolean wonderful = post.getStatus() == 1;
        // 点赞数量
        long liketCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);
        // 评论数量
        int commentCount = post.getCommentCount();

        // 计算权重
        double weight = (wonderful ? 75 : 0) + commentCount * 10 + liketCount * 2;
        // 分数 = 取对数(帖子权重) + 距离天数
        double score = Math.log10(Math.max(weight, 1)) +
                (post.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);

        // 更新帖子分数
        discussPostService.updateScore(postId, score);

        // 同步搜索数据
        post.setScore(score);
        elasticsearchService.saveDiscussPost(post);
    }
}
