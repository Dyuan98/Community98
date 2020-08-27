package com.yuan.community.service;

import com.alibaba.fastjson.JSON;
import com.yuan.community.dao.DiscussPostMapper;
import com.yuan.community.entity.DiscussPost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
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
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ElasticSearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired(required = false)
    private DiscussPostMapper discussPostMapper;


    /**
     * 添加一个对象到es中
     * @param post
     * @throws IOException
     */
    public void saveDiscussPost(DiscussPost post) throws IOException {
        // 创建请求
        IndexRequest request = new IndexRequest("discuss_post");
        // 规则 PUT /yuan_index/_doc/1
        request.id(""+post.getId());
        request.timeout(TimeValue.timeValueSeconds(1));
        // 将数据放入请求
        request.source(JSON.toJSONString(post), XContentType.JSON);
        // 客户端发送请求,获取响应结果
        IndexResponse indexResponse = restHighLevelClient.index(request, RequestOptions.DEFAULT);
    }

    /**
     * 根据id删除某条数据
     * @param id
     * @throws IOException
     */
    public void deleteDiscussPost(int id) throws IOException {
        DeleteRequest request = new DeleteRequest("discuss_post", ""+id);
        request.timeout("1s");
        DeleteResponse delete = restHighLevelClient.delete(request, RequestOptions.DEFAULT);
        System.out.println(delete);
    }

    /**
     * 执行搜索
     * @param keyWord
     * @param current
     * @param limit
     * @return
     * @throws IOException
     */
    public List<Map<String, Object>> searchDiscussPost(String keyWord, int current, int limit) throws IOException {
        SearchRequest request = new SearchRequest("discuss_post");
        // 构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(keyWord, "title", "content");
        searchSourceBuilder.sort("type", SortOrder.DESC);
//        searchSourceBuilder.sort("score", SortOrder.DESC);
//        searchSourceBuilder.sort("createTime", SortOrder.DESC);
        // 设置分页大小
        searchSourceBuilder.from(current);
        searchSourceBuilder.size(limit);
        // 设置高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.field("content");
        highlightBuilder.requireFieldMatch(false); // 多个高亮显示
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlighter(highlightBuilder);

        searchSourceBuilder.query(multiMatchQueryBuilder);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        // 组合请求
        request.source(searchSourceBuilder);

        // 接受响应
        SearchResponse searchResponse = restHighLevelClient.search(request, RequestOptions.DEFAULT);
//        System.out.println(JSON.toJSONString(searchResponse.getHits()));

        // 解析结果
        List<Map<String, Object>> list = new ArrayList<>();
        // 遍历获取命中的数据，并将高亮词替换原来的词，
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField title = highlightFields.get("title" );
            HighlightField content = highlightFields.get("content" );
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();  // 原来的数据
            // 解析高亮的字段，将原来的字段替换为高亮的字段；
            if (title != null) {
                Text[] fragments = title.fragments();
                String new_title = "";
                for (Text fragment : fragments) {
                    new_title += fragment;
                }
                sourceAsMap.put("title", new_title);
            }
            if (content != null) {
                Text[] fragments = content.fragments();
                String new_content = "";
                for (Text fragment : fragments) {
                    new_content += fragment;
                }
                sourceAsMap.put("content", new_content);
            }
            list.add(sourceAsMap);
        }
        return  list;
    }

    /**
     * 查出符合条件的行数
     * @param keyWord
     * @return
     * @throws IOException
     */
    public int searchCount(String keyWord) throws IOException {
        CountRequest countRequest = new CountRequest("discuss_post");
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(keyWord);
        countRequest.query(multiMatchQueryBuilder);
        CountResponse response = restHighLevelClient.count(countRequest, RequestOptions.DEFAULT);
        return (int) response.getCount();
    }
}
