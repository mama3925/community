package com.qiuyu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiuyu.annotation.LoginRequired;
import com.qiuyu.bean.Comment;
import com.qiuyu.bean.DiscussPost;
import com.qiuyu.bean.MyPage;
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
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, MyPage<Comment> page){
        //通过前端传来的Id查询帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post",post);

        //用以显示发帖人的头像及用户名
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user",user);

        //得到帖子的评论
        page.setSize(5);
        page.setPath("/discuss/detail/"+discussPostId);
        page = (MyPage<Comment>) commentService.findCommentsByEntity(ENTITY_TYPE_POST, post.getId(), page);
        //评论列表
        List<Comment> commentList = page.getRecords();

        // 评论: 给帖子的评论
        // 回复: 给评论的评论
        // 评论VO(viewObject)列表 (将comment,user信息封装到每一个Map，每一个Map再封装到一个List中)
        List<Map<String,Object>> commentVoList = new ArrayList<>();
        if(commentList != null){
            for (Comment comment : commentList) {
                //一条评论的VO
                Map<String, Object> commentVo = new HashMap<>(10);
                //评论
                commentVo.put("comment",comment);
                //评论作者
                commentVo.put("user",userService.findUserById(comment.getUserId().toString()));

                //回复
                Page<Comment> replyPage = new Page<>();
                replyPage.setCurrent(1);
                replyPage.setSize(Integer.MAX_VALUE);
                replyPage = (Page<Comment>) commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT, comment.getId(), replyPage);
                //回复列表
                List<Comment> replyList = replyPage.getRecords();
                //回复的VO列表
                List<Map<String,Object>> replyVoList = new ArrayList<>();
                if(replyList != null){
                    for (Comment reply : replyList) {
                        //一条回复的VO
                        Map<String, Object> replyVo = new HashMap<>(10);
                        //回复
                        replyVo.put("reply",reply);
                        //作者
                        replyVo.put("user",user);
                        //回复的目标
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId().toString());
                        replyVo.put("target",target);

                        replyVoList.add(replyVo);
                    }
                }

                //回复列表放入评论
                commentVo.put("reply",replyVoList);

                //评论的回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount",replyCount);


                commentVoList.add(commentVo);
            }
        }

//        for (Map<String, Object> commentVo : commentVoList) {
//            Comment comment = (Comment) commentVo.get("comment");
//            User user1 = (User) commentVo.get("user");
//            int replyCount = (int) commentVo.get("replyCount");
//            ArrayList<Map<String,Object>> replyVoList = (ArrayList<Map<String, Object>>) commentVo.get("reply");
//            System.out.println("作者:"+user1.getUsername()+" 回复数:"+replyCount+" 内容:"+comment.getContent());
//
//            for (Map<String, Object> replyVo : replyVoList) {
//                Comment reply = (Comment) replyVo.get("reply");
//                User user2 = (User) replyVo.get("user");
//
//                System.out.println("\t作者:"+user2.getUsername()+" 内容:"+reply.getContent());
//            }
//        }

        model.addAttribute("comments",commentVoList);
        model.addAttribute("page",page);

        return "/site/discuss-detail";
    }
}
