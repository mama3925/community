package com.qiuyu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qiuyu.bean.DiscussPost;
import com.qiuyu.bean.MyPage;
import com.qiuyu.dao.elasticsearch.DiscussPostRepository;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author QiuYuSY
 * @create 2023-01-26 1:31
 */
@Service
public class ElasticsearchService {
    @Autowired
    private DiscussPostRepository discussRepository;
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    public void saveDiscussPost(DiscussPost post) {
        discussRepository.save(post);
    }

    public void deleteDiscussPost(int id) {
        discussRepository.deleteById(id);
    }

    /**
     * Elasticsearch高亮搜索
     *
     * @param keyword
     * @param page
     * @return
     */
    public IPage<DiscussPost> searchDiscussPost(String keyword, IPage<DiscussPost> page) {
        page.setCurrent(page.getCurrent() < 1 ? 1 : page.getCurrent());
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyword, "title", "content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of((int) (page.getCurrent() - 1), (int) page.getSize()))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();


        SearchHits<DiscussPost> searchHits = elasticsearchRestTemplate.search(searchQuery, DiscussPost.class);
        SearchPage<DiscussPost> searchPage = SearchHitSupport.searchPageFor(searchHits, searchQuery.getPageable());


        //封装到MyPage
        List<DiscussPost> list = new ArrayList<>();
        for (SearchHit<DiscussPost> discussPostSearchHit : searchPage) {
            DiscussPost discussPost = discussPostSearchHit.getContent();
            //discussPostSearchHit.getHighlightFields() //高亮
            if (discussPostSearchHit.getHighlightFields().get("title") != null) {
                discussPost.setTitle(discussPostSearchHit.getHighlightFields().get("title").get(0));
            }
            if (discussPostSearchHit.getHighlightFields().get("content") != null) {
                discussPost.setContent(discussPostSearchHit.getHighlightFields().get("content").get(0));
            }
            //System.out.println(discussPostSearchHit.getContent());
            list.add(discussPost);
        }

        page.setRecords(list);
        page.setTotal(searchPage.getTotalElements());
        page.setPages(searchPage.getTotalPages());

        return page;
    }
}
