package com.qiuyu.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author QiuYuSY
 * @create 2023-01-16 23:55
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyPage {
    //当前页面
    private int currentPage = 1;
    //总页数
    private int totalPage ;
    //分页大小
    private int pageSize = 10;

    //路径
    private String path;

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public void setPageSize(int pageSize) {
        if (pageSize >= 1 && pageSize <= 100) {
            this.pageSize = pageSize;
        }
    }



    //获取起始页
    public int getStartPage() {
        int start = currentPage - 2;
        return start < 1 ? 1 : start;
    }

    //获取结束页
    public int getEndPage() {
        int end = currentPage + 2;
        return end > totalPage ? totalPage : end;
    }


}
