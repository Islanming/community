package com.example.community.service;

import com.example.community.entity.DiscussPost;
import com.example.community.mapper.elasticsearch.DiscussPostRepository;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * 实现往es服务器里面增删改查
 * @author Lenovo
 */
@Service
public class ElasticsearchService {

    @Autowired
    private DiscussPostRepository discussPostRepository;


    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    /**
     * 往es服务器存入帖子
     * @param discussPost
     */
    public void saveDiscussPost(DiscussPost discussPost){
        discussPostRepository.save(discussPost);
    }

    /**
     * 删除帖子
     * @param id
     */
    public void deleteDiscussPost(int id){
        discussPostRepository.deleteById(id);
    }


    /**
     * 查询帖子（模糊查询），实现分页
     * @param keyword 关键词
     * @param current 当前页数,一般为0，即查询页数范围为（0，limit]
     * @param limit 大小
     * @return
     * @throws IOException
     */
    public SearchPage<DiscussPost> searchDiscussPost(String keyword, int current, int limit) throws IOException {
        // 构建查询条件
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyword,"title","content"))
                .withSorts(SortBuilders.fieldSort("type").order(SortOrder.DESC),
                        SortBuilders.fieldSort("score").order(SortOrder.DESC),
                        SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(current,limit))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();

        // 查询结果
        SearchHits<DiscussPost> search = restTemplate.search(searchQuery,DiscussPost.class);
        // 将查询结果返回并进行分页
        SearchPage<DiscussPost> page = SearchHitSupport.searchPageFor(search, Page.empty().getPageable());

        if(!page.isEmpty()){
            for (org.springframework.data.elasticsearch.core.SearchHit<DiscussPost> discussPostSearch : page) {
                DiscussPost discussPost = discussPostSearch.getContent();
                // 处理高亮显示的结果
                List<String> title = discussPostSearch.getHighlightFields().get("title");
                if (title != null) {
                    discussPost.setTitle(title.get(0));
                }
                List<String> content = discussPostSearch.getHighlightFields().get("content");
                if (title != null) {
                    discussPost.setTitle(content.get(0));
                }
            }
        }

        return page;

    }


}
