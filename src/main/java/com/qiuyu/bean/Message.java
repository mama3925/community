package com.qiuyu.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author QiuYuSY
 * @create 2023-01-20 17:28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private Integer id;
    private Integer fromId;
    private Integer toId;
    private String conversationId;
    private String content;
    private Integer status;
    private Date createTime;
}
