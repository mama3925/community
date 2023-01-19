package com.qiuyu.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author QiuYuSY
 * @create 2023-01-19 23:17
 * 评论
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    private Integer id;
    private Integer userId;
    private Integer entityType; //目标类型
    private Integer entityId; //目标Id
    private Integer targetId; //回复时目标用户ID
    private String content;
    private Integer status;
    private Date createTime;
}
