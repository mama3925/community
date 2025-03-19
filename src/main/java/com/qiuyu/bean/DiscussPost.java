package com.qiuyu.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.lang.annotation.Documented;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscussPost {

//    @Id
    private Integer id;
    private String userId;
    private String title;
    private String content;

    private Integer type;

    private Integer status;

    private Date createTime;

    private Integer commentCount;

    private Double score;

}
