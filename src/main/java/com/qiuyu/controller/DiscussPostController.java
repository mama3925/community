package com.qiuyu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiuyu.annotation.LoginRequired;
import com.qiuyu.bean.*;
import com.qiuyu.event.EventProducer;
import com.qiuyu.service.CommentService;
import com.qiuyu.service.DiscussPostService;
import com.qiuyu.service.LikeService;
import com.qiuyu.service.UserService;
import com.qiuyu.utils.CommunityConstant;
import com.qiuyu.utils.CommunityUtil;
import com.qiuyu.utils.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author QiuYuSY
 * @create 2023-01-19 16:51
 * 帖子相关的操作
 */
@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {
    public static final Logger logger = LoggerFactory.getLogger(DiscussPostController.class);
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private EventProducer eventProducer;

    /**
     * 添加帖子
     * @param title   标题
     * @param content 内容
     * @return
     */
    @PostMapping("/add")
    @ResponseBody
//    @LoginRequired
    public String addDiscussPost(String title, String content) {
        //获取当前登录的用户
        User user = hostHolder.getUser();
        if (user == null) {
            //403权限不够
            return CommunityUtil.getJSONString(403, "你还没有登录哦！");
        }
        if (StringUtils.isBlank(title) || StringUtils.isBlank(content)) {
            return CommunityUtil.getJSONString(222, "贴子标题或内容不能为空！");
        }

        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId().toString());
        post.setTitle(title);
        post.setContent(content);
        post.setType(0);
        post.setStatus(0);
        post.setCreateTime(new Date());

        //业务处理，将用户给的title，content进行处理并添加进数据库
        discussPostService.addDiscussPost(post);

        //触发发帖事件,让消费者将帖子存入ElasticSearch
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(post.getId());
        eventProducer.fireEvent(event);


        //返回Json格式字符串给前端JS,报错的情况将来统一处理
        return CommunityUtil.getJSONString(0, "发布成功！");
    }

    /**
     * 查看帖子详细页
     *
     * @param discussPostId
     * @param model
     * @return
     */
    @GetMapping("/detail/{discussPostId}")
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, MyPage<Comment> page) {
        //通过前端传来的Id查询帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);

        //用以显示发帖人的头像及用户名
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);

        //得到帖子的评论
        page.setSize(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page = (MyPage<Comment>) commentService.findCommentsByEntity(ENTITY_TYPE_POST, post.getId(), page);

        //得到帖子点赞数
        long entityLikeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount", entityLikeCount);
        //得到帖子点赞状态,没登陆直接0
        int entityLikeStatus = hostHolder.getUser() == null ?
                0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeStatus", entityLikeStatus);

        //评论列表
        List<Comment> commentList = page.getRecords();

        // 评论: 给帖子的评论
        // 回复: 给评论的评论
        // 评论VO(viewObject)列表 (将comment,user信息封装到每一个Map，每一个Map再封装到一个List中)
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                //一条评论的VO
                Map<String, Object> commentVo = new HashMap<>(10);
                //评论
                commentVo.put("comment", comment);
                //评论作者
                commentVo.put("user", userService.findUserById(comment.getUserId().toString()));
                //得到评论点赞数
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount", likeCount);
                //得到评论点赞状态,没登陆直接0
                int likeStatus = hostHolder.getUser() == null ?
                        0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus", likeStatus);


                //回复
                Page<Comment> replyPage = new Page<>();
                replyPage.setCurrent(1);
                replyPage.setSize(Integer.MAX_VALUE);
                replyPage = (Page<Comment>) commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT, comment.getId(), replyPage);
                //回复列表
                List<Comment> replyList = replyPage.getRecords();
                //回复的VO列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        //一条回复的VO
                        Map<String, Object> replyVo = new HashMap<>(10);
                        //回复
                        replyVo.put("reply", reply);
                        //回复的作者
                        replyVo.put("user", userService.findUserById(reply.getUserId().toString()));
                        //回复给谁
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId().toString());
                        replyVo.put("target", target);

                        //得到回复点赞数
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount", likeCount);
                        //得到回复点赞状态,没登陆直接0
                        likeStatus = hostHolder.getUser() == null ?
                                0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeStatus", likeStatus);

                        replyVoList.add(replyVo);
                    }
                }

                //回复列表放入评论
                commentVo.put("reply", replyVoList);

                //评论的回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);


                commentVoList.add(commentVo);
            }
        }

        model.addAttribute("comments", commentVoList);
        model.addAttribute("page", page);

        return "/site/discuss-detail";
    }


    /**
     * 置顶、取消置顶(与以下类似)
     * @param id
     * @return
     */
    @PostMapping( "/top")
    @ResponseBody
    public String setTop(int id) {
        DiscussPost post = discussPostService.findDiscussPostById(id);
        // 获取置顶状态，1为置顶，0为正常状态,1^1=0 0^1=1 异或
        int type = post.getType() ^ 1;
        discussPostService.updateType(id, type);
        // 返回结果给JS异步请求
        HashMap<String, Object> map = new HashMap<>();
        map.put("type", type);

        // 触发事件，修改Elasticsearch中的帖子type
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0, null, map);
    }

    /**
     * 加精、取消加精
     * @param id
     * @return
     */
    @PostMapping( "/wonderful")
    @ResponseBody
    public String setWonderful(int id) {
        DiscussPost post = discussPostService.findDiscussPostById(id);
        int status = post.getStatus() ^ 1;
        discussPostService.updateStatus(id, status);
        // 返回结果给JS异步请求
        HashMap<String, Object> map = new HashMap<>();
        map.put("status", status);

        // 触发事件，修改Elasticsearch中的帖子status
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0, null, map);
    }

    // 删除
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id) {
        discussPostService.updateStatus(id, 2);

        // 触发删帖事件,将帖子从Elasticsearch中删除
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

}
