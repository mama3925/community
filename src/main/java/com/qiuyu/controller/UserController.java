package com.qiuyu.controller;

import com.qiuyu.annotation.LoginRequired;
import com.qiuyu.bean.User;
import com.qiuyu.service.UserService;
import com.qiuyu.utils.CommunityUtil;
import com.qiuyu.utils.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * @author QiuYuSY
 * @create 2023-01-18 20:58
 */

@Controller
@RequestMapping("/user")
public class UserController {
    public static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.domain}")
    private String domain;
    @Value("${community.path.upload-path}")
    private String uploadPath;
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;
    @Autowired
    private HostHolder hostHolder;

    /**
     * 跳转设置页面
     * @return
     */
    @LoginRequired
    @GetMapping("/setting")
    public String getUserPage() {
        return "/site/setting";
    }

    /**
     * 上传头像
     *
     * @param headerImage
     * @param model
     * @return
     */
    @LoginRequired
    @PostMapping("/upload")
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "您还没有选择图片！");
            return "/site/setting";
        }

        /*
         * 获得原始文件名字
         * 目的是：生成随机不重复文件名，防止同名文件覆盖
         * 方法：获取.后面的图片类型 加上 随机数
         */
        String filename = headerImage.getOriginalFilename();
        int index = filename.lastIndexOf(".");
        String suffix = filename.substring( index+1);

        //任何文件都可以上传,根据业务在此加限制.这里为没有后缀不合法
        if (StringUtils.isBlank(suffix) || index < 0) {
            model.addAttribute("error", "文件格式不正确！");
            return "/site/setting";
        }

        //生成随机文件名
        filename = CommunityUtil.generateUUID() +"."+ suffix;

        //确定文件存放路径
        File dest = new File(uploadPath + "/" + filename);
        try {
            //将文件存入指定位置
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败： " + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常！", e);
        }


        //更新当前用户的头像的路径（web访问路径）
        //http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + filename;
        userService.updateHeaderUrl(user.getId(), headerUrl);

        return "redirect:/index";
    }


    /**
     * 得到服务器图片
     * void:返回给浏览器的是特色的图片类型所以用void
     *
     * @param fileName
     * @param response
     */
    @GetMapping("/header/{fileName}")
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // 服务器存放路径(本地路径)
        fileName = uploadPath + "/" + fileName;
        // 文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        // 浏览器响应图片
        response.setContentType("image/" + suffix);
        try (
            //图片是二进制用字节流
            FileInputStream fis = new FileInputStream(fileName);
            OutputStream os = response.getOutputStream();
        ) {
            //设置缓冲区
            byte[] buffer = new byte[1024];
            //设置游标
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败: " + e.getMessage());
        }
    }

    /**
     *  更新密码
     * @param oldPassword
     * @param newPassword
     * @param model
     * @return
     */
    @LoginRequired
    @PostMapping("/updatePassword")
    public String updatePassword(String oldPassword, String newPassword,Model model){
        User user = hostHolder.getUser();

        Map<String, Object> map =
                userService.updatePassword(user.getId(), oldPassword, newPassword);

        if(map == null || map.isEmpty()){
            //成功!
            return "redirect:/index";
        }else{
            //失败
            model.addAttribute("oldPasswordMsg",map.get("oldPasswordMsg"));
            model.addAttribute("newPasswordMsg",map.get("newPasswordMsg"));
            return "/site/setting";
        }
    }

}
