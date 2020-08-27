package com.yuan.community;

import com.alibaba.fastjson.JSON;
import com.yuan.community.dao.DiscussPostMapper;
import com.yuan.community.dao.elasticsearch.DiscussPostRepository;
import com.yuan.community.entity.DiscussPost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class ElasticTests {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired(required = false)
    private DiscussPostMapper discussPostMapper;


    @Autowired
    private DiscussPostRepository discussRepository;

    @Autowired(required = false)
    private ElasticsearchTemplate elasticTemplate;

    @Test
    public void testInsert() throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        ArrayList<DiscussPost> discussPosts = new ArrayList<>();
        discussPosts.add(discussPostMapper.selectDiscussPostById(241));
        discussPosts.add(discussPostMapper.selectDiscussPostById(242));
        discussPosts.add(discussPostMapper.selectDiscussPostById(243));

        for (int i = 0; i < discussPosts.size(); i++) {
            bulkRequest.add(
                    new IndexRequest("discuss")
                            .id(""+discussPosts.get(i).getId())
                            .source(JSON.toJSONString(discussPosts.get(i)), XContentType.JSON));
        }
        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        System.out.println(bulkResponse.status());
        System.out.println(bulkResponse.hasFailures());

    }

    @Test
    public void testInsertList() throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        ArrayList<List<DiscussPost>> discussPosts = new ArrayList<>();
        discussPosts.add(discussPostMapper.selectDiscussPosts(101,0,100));
        discussPosts.add(discussPostMapper.selectDiscussPosts(102,0,100));
        discussPosts.add(discussPostMapper.selectDiscussPosts(103,0,100));
        discussPosts.add(discussPostMapper.selectDiscussPosts(111,0,100));
        discussPosts.add(discussPostMapper.selectDiscussPosts(112,0,100));
        discussPosts.add(discussPostMapper.selectDiscussPosts(131,0,100));
        discussPosts.add(discussPostMapper.selectDiscussPosts(132,0,100));
        discussPosts.add(discussPostMapper.selectDiscussPosts(133,0,100));

        for (int i = 0; i < discussPosts.size(); i++) {
            for (int j = 0; j < discussPosts.get(i).size(); j++) {
                // 批量更新删除在这里修改对应的请求
                bulkRequest.add(
                        new IndexRequest("discuss_post")
                                .id(""+discussPosts.get(i).get(j).getId())
                                .source(JSON.toJSONString(discussPosts.get(i).get(j)), XContentType.JSON));
            }
        }
        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        System.out.println(bulkResponse.status());
        System.out.println(bulkResponse.hasFailures());

    }

    @Test
    public void testSearch() throws IOException {
        SearchRequest request = new SearchRequest("discuss");
        // 构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

//        searchSourceBuilder.highlighter();  // 高亮

        // 查询条件，可以使用QueryBuilders来实现
        //  QueryBuilders.termQuery 精确搜索
        // QueryBuilders.matchAllQuery()    匹配所有
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("互联","title","content");
        searchSourceBuilder.sort("id", SortOrder.DESC);
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.field("content");
        highlightBuilder.requireFieldMatch(true); // 多个高亮显示
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlighter(highlightBuilder);
        searchSourceBuilder.query(multiMatchQueryBuilder);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        request.source(searchSourceBuilder);

        SearchResponse searchResponse = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        System.out.println(JSON.toJSONString(searchResponse.getHits()));
        System.out.println("--------------------");

        // 解析结果
        List<Map<String,Object>> list = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField title = highlightFields.get("title");
            Map<String,Object> sourceAsMap =  hit.getSourceAsMap();  // 原来的数据
            // 解析高亮的字段，将原来的字段替换为高亮的字段；
            if(title!=null){
                Text[] fragments = title.fragments();
                String new_title = "";
                for (Text fragment : fragments) {
                    new_title += fragment;
                }
                sourceAsMap.put("title",new_title);
            }
            list.add(sourceAsMap);
            System.out.println(hit.getSourceAsMap());
            System.out.println(list);
        }

    }
    @Test
    public void selectCount() throws IOException {
        CountRequest countRequest = new CountRequest("discuss_post");
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("互联网");
        countRequest.query(multiMatchQueryBuilder);
        CountResponse response = restHighLevelClient.count(countRequest, RequestOptions.DEFAULT);
        System.out.println((int) response.getCount());
    }

    @Test
    public void testInsert02() {
        discussRepository.save(discussPostMapper.selectDiscussPostById(241));
        discussRepository.save(discussPostMapper.selectDiscussPostById(242));
        discussRepository.save(discussPostMapper.selectDiscussPostById(243));
    }
}
