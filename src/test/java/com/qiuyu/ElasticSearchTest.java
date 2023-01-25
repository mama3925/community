package com.qiuyu;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qiuyu.bean.DiscussPost;
import com.qiuyu.bean.MyPage;
import com.qiuyu.dao.DiscussPostMapper;
import com.qiuyu.dao.elasticsearch.DiscussPostRepository;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticSearchTest {
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private DiscussPostRepository discussPostRepository;
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;


    /**
     * 插入数据
     */
    @Test
    public void testInsert(){
        discussPostRepository.save(discussPostMapper.selectById(241));
        discussPostRepository.save(discussPostMapper.selectById(242));
        discussPostRepository.save(discussPostMapper.selectById(243));
    }

    /**
     * 批量插入数据
     */
    @Test
    public void testInsertList(){
        List<DiscussPost> list = discussPostMapper.selectList(new QueryWrapper<DiscussPost>()
                .lambda()
                .ge(DiscussPost::getId, 195));
        discussPostRepository.saveAll(list);
    }

    /**
     * 修改
     */
    @Test
    public void testUpdate(){
        DiscussPost discussPost = discussPostMapper.selectById(231);
        discussPost.setContent("秋雨灌水");
        discussPostRepository.save(discussPost);
    }

    /**
     * 删除
     */
    @Test
    public void testDelete(){
//        discussPostRepository.deleteById(231);
        //删除所有
        discussPostRepository.deleteAll();
    }

    /**
     * 根据id查找
     */
    @Test
    public void findById(){
        DiscussPost discussPost = discussPostRepository.findById(230).get();
        System.out.println(discussPost);
    }

    @Test
    public void testSearch(){
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬","title","content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0,10))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();

        SearchHits<DiscussPost> searchHits = elasticsearchRestTemplate.search(searchQuery, DiscussPost.class);
        SearchPage<DiscussPost> searchPage = SearchHitSupport.searchPageFor(searchHits, searchQuery.getPageable());
//        System.out.println(searchPage.getTotalElements());
//        System.out.println(searchPage.getTotalPages());
//        System.out.println(searchPage.getNumber());
//        System.out.println(searchPage.getSize());
//        for (SearchHit<DiscussPost> discussPostSearchHit : page) {
//            System.out.println(discussPostSearchHit.getHighlightFields()); //高亮内容
//            System.out.println(discussPostSearchHit.getContent()); //原始内容
//        }

        //封装到MyPage
        List<DiscussPost> list = new ArrayList<>();
        IPage<DiscussPost> page = new MyPage<>();

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
        page.setSize(searchPage.getSize());
        page.setTotal(searchPage.getTotalElements());
        page.setPages(searchPage.getTotalPages());
        page.setCurrent(searchPage.getNumber()+1);


        for (DiscussPost record : page.getRecords()) {
            System.out.println(record);
        }


    }
}
