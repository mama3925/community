package com.qiuyu.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiuyu.annotation.LoginRequired;
import com.qiuyu.bean.Message;
import com.qiuyu.bean.MyPage;
import com.qiuyu.bean.User;
import com.qiuyu.service.MessageService;
import com.qiuyu.service.UserService;
import com.qiuyu.utils.CommunityConstant;
import com.qiuyu.utils.CommunityUtil;
import com.qiuyu.utils.HostHolder;
import org.apache.catalina.Host;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

/**
 * @author QiuYuSY
 * @create 2023-01-20 21:21
 */
@Controller
public class MessageController implements CommunityConstant {
    @Autowired
    private MessageService messageService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;

    /**
     * 私信列表
     *
     * @param model
     * @param page
     * @return
     */
//    @LoginRequired
    @GetMapping("/letter/list")
    public String getLetterList(Model model, MyPage<Message> page) {
        User user = hostHolder.getUser();

        //分页信息
        page.setSize(5);
        page.setPath("/letter/list");
        //会话列表
        page = (MyPage<Message>) messageService.findConversations(user.getId(), page);
        List<Message> conversationList = page.getRecords();
        //VO
        List<Map<String, Object>> conversationVo = new ArrayList<>();
        if (conversationList != null) {
            for (Message message : conversationList) {
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", message);
                //会话中的消息数
                map.put("letterCount", messageService.findLetterCount(message.getConversationId()));
                //未读消息数
                map.put("unreadCount", messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));
                //显示的目标用户
                Integer targetId = user.getId().equals(message.getFromId()) ? message.getToId() : message.getFromId();
                map.put("target", userService.findUserById(targetId.toString()));

                conversationVo.add(map);
            }
        }

        model.addAttribute("conversations", conversationVo);
        // 当前登录用户总未读条数
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);

        // 查询所有未读系统通知数量
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        model.addAttribute("page", page);
        return "site/letter";
    }

    /**
     * 获取会话中的消息
     * @param conversationId
     * @param model
     * @param page
     * @return
     */
//    @LoginRequired
    @GetMapping("/letter/detail/{conversationId}")
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Model model, MyPage<Message> page) {
        //是否为会话中的用户


        //分页信息
        page.setSize(5);
        page.setPath("/letter/detail/" + conversationId);

        //获取私信信息
        page = (MyPage<Message>) messageService.findLetters(conversationId, page);
        List<Message> letterList = page.getRecords();
        //VO
        List<Map<String, Object>> letterVo = new ArrayList<>();
        if (letterList != null) {
            for (Message message : letterList) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter", message);
                map.put("fromUser", userService.findUserById(message.getFromId().toString()));

                letterVo.add(map);
            }
        }
        //消息设置已读(当打开这个页面是就更改status =1)
        List<Integer> ids = getLetterIds(letterList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        model.addAttribute("letters", letterVo);

        //获取私信目标
        model.addAttribute("target", getLetterTarget(conversationId));

        model.addAttribute("page", page);
        return "site/letter-detail";
    }

    /**
     * 封装获取目标会话用户(将如：101_107拆开)
     * @param conversationId
     * @return
     */
    private User getLetterTarget(String conversationId) {
        String[] s = conversationId.split("_");

        Integer id0 = Integer.parseInt(s[0]);
        Integer id1 = Integer.parseInt(s[1]);

        //不是会话中的用户
        int userId = hostHolder.getUser().getId();
        if(userId != id0 && userId != id1){
            throw new IllegalArgumentException("无权限查看");
        }

        //当前用户是哪个就选另一个
        Integer target = hostHolder.getUser().getId().equals(id0) ? id1 : id0;
        return userService.findUserById(target.toString());
    }


    /**
     * 获得批量私信的未读数id
     * @param letterList
     * @return
     */
    private List<Integer> getLetterIds(List<Message> letterList){
        List<Integer> ids = new ArrayList<>();

        if (letterList != null) {
            for (Message message : letterList) {
                //只有当前登录用户与message列表中目标用户一致并且staus = 0 时才是未读数，加入未读私信集合
                if (hostHolder.getUser().getId().equals(message.getToId()) && message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }


    /**
     * 发送消息
     * @param toName
     * @param content
     * @return
     */
    @PostMapping("/letter/send")
    @ResponseBody
    public String sendLetter(String toName, String content){

        //获得发送目标
        User target = userService.findUserByName(toName);
        if (target == null){
            return CommunityUtil.getJSONString(1,"目标用户不存在!");
        }

        //设置message属性
        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        message.setContent(content);
        message.setStatus(0);
        message.setCreateTime(new Date());
        // conversationId (如101_102: 小_大)
        if (message.getFromId() < message.getToId()) {
            message.setConversationId(message.getFromId() + "_" +message.getToId());
        }else{
            message.setConversationId(message.getToId() + "_" +message.getFromId());
        }
        messageService.addMessage(message);

        return CommunityUtil.getJSONString(0,"发送消息成功!");

    }


    /**
     * 查询系统通知
     * @param model
     * @return
     */
    @GetMapping( "/notice/list")
    public String getNoticeList(Model model) {
        User user = hostHolder.getUser();
        /**查询评论类通知**/
        Message message = messageService.findLatestNotice(user.getId(), TOPIC_COMMENT);

        if (message != null) {
            HashMap<String, Object> messageVO = new HashMap<>();
            messageVO.put("message", message);

            // 转化message表中content为HashMap<k,v>类型
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            // 将content数据中的每一个字段都存入map
            // 用于显示->用户[user] (评论、点赞、关注[entityType])...了你的(帖子、回复、用户[entityId]) 查看详情连接[postId]
            messageVO.put("user", userService.findUserById( data.get("userId").toString()));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));

            // 共几条会话
            int count = messageService.findNoticeCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("count", count);
            // 评论类未读数
            int unreadCount = messageService.findNoticeUnreadCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("unreadCount", unreadCount);

            model.addAttribute("commentNotice", messageVO);
        }

        /**查询点赞类通知**/
        message = messageService.findLatestNotice(user.getId(), TOPIC_LIKE);

        if (message != null) {
            HashMap<String, Object> messageVO = new HashMap<>();
            messageVO.put("message", message);
            // 转化message表中content为HashMap<k,v>类型
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            // 将content数据中的每一个字段都存入map
            // 用于显示->用户[user] (评论、点赞、关注[entityType])...了你的(帖子、回复、用户[entityId]) 查看详情连接[postId]
            messageVO.put("user", userService.findUserById(data.get("userId").toString()));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));

            // 共几条会话
            int count = messageService.findNoticeCount(user.getId(), TOPIC_LIKE);
            messageVO.put("count", count);
            // 点赞类未读数
            int unreadCount = messageService.findNoticeUnreadCount(user.getId(), TOPIC_LIKE);
            messageVO.put("unreadCount", unreadCount);
            model.addAttribute("likeNotice", messageVO);
        }

        /**查询关注类通知**/
        message = messageService.findLatestNotice(user.getId(), TOPIC_FOLLOW);

        if (message != null) {
            HashMap<String, Object> messageVO = new HashMap<>();
            messageVO.put("message", message);
            // 转化message表中content为HashMap<k,v>类型
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            // 将content数据中的每一个字段都存入map
            // 用于显示->用户[user] (评论、点赞、关注)...了你的(帖子、回复、用户[entityType]) 查看详情连接[postId]
            messageVO.put("user", userService.findUserById( data.get("userId").toString()));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));

            // 共几条会话
            int count = messageService.findNoticeCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("count", count);
            // 关注类未读数
            int unreadCount = messageService.findNoticeUnreadCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("unreadCount", unreadCount);
            model.addAttribute("followNotice", messageVO);
        }

        // 查询未读私信数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        // 查询所有未读系统通知数量
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "site/notice";
    }

    /**
     * 查询系统通知详情页（分页）
     * @param topic
     * @param page
     * @param model
     * @return
     */
    @GetMapping( "/notice/detail/{topic}")
    public String getNoticeDetail(@PathVariable("topic")String topic, MyPage page, Model model) {
        User user = hostHolder.getUser();

        page.setSize(5);
        page.setPath("/notice/detail/" + topic);

        page = (MyPage) messageService.findNotices(user.getId(), topic, page);
        List<Message> noticeList = page.getRecords();
        // 聚合拼接User
        List<Map<String, Object>> noticeVoList = new ArrayList<>();
        if (noticeList != null) {
            for (Message notice : noticeList) {
                HashMap<String, Object> map = new HashMap<>();
                // 将查询出来的每一个通知封装Map
                map.put("notice", notice);


                // 把message中的content内容转化Object
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

                // 发起事件的user
                map.put("user", userService.findUserById( data.get("userId").toString()));

                map.put("entityType", data.get("entityType"));
                map.put("entityId", data.get("entityId"));
                map.put("postId", data.get("postId"));
                // 系统通知->id=1的系统用户
                map.put("fromUser", userService.findUserById(notice.getFromId().toString()));

                noticeVoList.add(map);
            }
        }
        model.addAttribute("notices", noticeVoList);
        model.addAttribute("page",page);

        //设置已读(当打开这个页面是就更改status =1)
        List<Integer> ids = getLetterIds(noticeList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        return "site/notice-detail";
    }



}
