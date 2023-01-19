package com.qiuyu.bean;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 我的分页组件
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyPage<T> extends Page<T> {
    /**
     * 分页跳转的路径
     */
    protected String path;

}
