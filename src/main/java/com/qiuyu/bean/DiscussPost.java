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
@Document(indexName = "discusspost", shards = 6, replicas = 3)

public class DiscussPost {
//    @Id
    private Integer id;
    @Field(type = FieldType.Integer)
    private String userId;
    @Field(type = FieldType.Text, analyzer = "ik_max_word",searchAnalyzer = "ik_smart")
    private String title;
    @Field(type = FieldType.Text, analyzer = "ik_max_word",searchAnalyzer = "ik_smart")
    private String content;
    @Field(type = FieldType.Integer)

    private Integer type;
    @Field(type = FieldType.Integer)

    private Integer status;
    @Field(type = FieldType.Date)

    private Date createTime;
    @Field(type = FieldType.Integer)

    private Integer commentCount;
    @Field(type = FieldType.Double)

    private Double score;
}
