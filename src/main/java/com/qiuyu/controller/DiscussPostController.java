package com.qiuyu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiuyu.annotation.LoginRequired;
import com.qiuyu.bean.Comment;
import com.qiuyu.bean.DiscussPost;
import com.qiuyu.bean.User;
import com.qiuyu.service.CommentService;
import com.qiuyu.service.DiscussPostService;
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


    /**
     *  添加帖子
     * @param title 标题
     * @param content 内容
     * @return
     */
    @PostMapping("/add")
    @ResponseBody
//    @LoginRequired
    public String addDiscussPost(String title, String content){
        //获取当前登录的用户
        User user = hostHolder.getUser();
        if (user == null){
            //403权限不够
            return CommunityUtil.getJSONString(403,"你还没有登录哦！");
        }
        if(StringUtils.isBlank(title) || StringUtils.isBlank(content)){
            return CommunityUtil.getJSONString(222,"贴子标题或内容不能为空！");
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

        //返回Json格式字符串给前端JS,报错的情况将来统一处理
        return CommunityUtil.getJSONString(0,"发布成功！");
    }

    /**
     * 查看帖子详细页
     * @param discussPostId
     * @param model
     * @return
     */
    @GetMapping( "/detail/{discussPostId}")
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page<Comment> page){
        //通过前端传来的Id查询帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post",post);

        //用以显示发帖人的头像及用户名
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user",user);

        //得到帖子的评论
        page.setSize(5);
        page.setCurrent(1);
        page = (Page<Comment>) commentService.findCommentsByEntity(ENTITY_TYPE_POST, post.getId(), page);


        //ViewObject给前端展示数据用
        List<Map<String,Object>> commentVoList = new ArrayList<>();
        for (Comment comment : page.getRecords()) {
            Map<String, Object> map = new HashMap<>(15);
            map.put("comment",comment);
            map.put("user",userService.findUserById(comment.getUserId().toString()));
        }



        return "/site/discuss-detail";
    }
}
