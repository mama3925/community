package com.qiuyu.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qiuyu.bean.Comment;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author QiuYuSY
 * @create 2023-01-19 23:49
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}
