package com.yuan.community;

import com.alibaba.fastjson.JSON;
import com.yuan.community.entity.DiscussPost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.joda.time.Seconds;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class EsTests {
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    //测试索引的创建 Request PUT yuan_index
    @Test
    public void testCreatIndex() throws IOException {
        // 创建索引请求
        CreateIndexRequest request = new CreateIndexRequest("yuan_index");
        //  客户端执行请求 IndicesClient，请求后获得相应
        CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
        System.out.println(createIndexResponse);

    }

    // 测试获取索引
    @Test
    public void testExistIndex() throws IOException {
        GetIndexRequest request = new GetIndexRequest("yuan_index");
        boolean exists = restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println("是否存在-----"+exists);

    }

    // 测试删除索引
    @Test
    public void testDelIndex() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest("yuan_index");
        // 删除
        AcknowledgedResponse delete = restHighLevelClient.indices().delete(request, RequestOptions.DEFAULT);
        System.out.println(delete.isAcknowledged());

    }

    // 测试添加文档
    @Test
    public void testDocument() throws IOException {
        // 创建对象
        DiscussPost discussPost = new DiscussPost(1,2,"标题","内容",1,1,new Date(),2,2);
        // 创建请求
        IndexRequest request = new IndexRequest("yuan_index");
        // 规则 PUT /yuan_index/_doc/1
        request.id("1");
        request.timeout(TimeValue.timeValueSeconds(1));

        // 将数据放入请求
        request.source(JSON.toJSONString(discussPost), XContentType.JSON);

        // 客户端发送请求,获取响应结果
        IndexResponse indexResponse = restHighLevelClient.index(request, RequestOptions.DEFAULT);
        System.out.println(indexResponse.toString());
//    输出结果：  IndexResponse[index=yuan_index,type=_doc,id=1,version=1,result=created,seqNo=0,primaryTerm=1,shards={"total":2,"successful":1,"failed":0}]
        System.out.println(indexResponse.status()); // 命令返回状态 ：CREATED
    }

    // 获取文档，判断是否存在 GET /index/_doc/1
    @Test
    public void testExistDocument() throws IOException {
        GetRequest request = new GetRequest("yuan_index", "1");
        // 获取返回_source的上下文，这里设为false，不返回
        request.fetchSourceContext(new FetchSourceContext(false));
        request.storedFields("_none_");

        boolean exists = restHighLevelClient.exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);

    }
    // 获取文档的信息
    @Test
    public void testGetDocument() throws IOException {
        GetRequest request = new GetRequest("yuan_index", "1");
        GetResponse response = restHighLevelClient.get(request, RequestOptions.DEFAULT);
        System.out.println(response.getSourceAsString());  //打印文档的内容
//        {"commentCount":2,"content":"内容","createTime":1598429439763,"id":1,"score":2.0,"status":1,"title":"标题","type":1,"userId":2}

    }

    // 更新文档的信息
    @Test
    public void testUpdateDocument() throws IOException {
        UpdateRequest request = new UpdateRequest("yuan_index", "1");
        request.timeout("1s");
        DiscussPost discussPost = new DiscussPost(1,2,"更新标题","更新内容",1,1,new Date(),2,2);
        request.doc(JSON.toJSONString(discussPost),XContentType.JSON);

        UpdateResponse update = restHighLevelClient.update(request, RequestOptions.DEFAULT);
        System.out.println(update.status());

    }

    // 删除文档信息
    @Test
    public void testDelDocument() throws IOException{
        DeleteRequest request = new DeleteRequest("yuan_index", "1");
        request.timeout("1s");
        DeleteResponse delete = restHighLevelClient.delete(request, RequestOptions.DEFAULT);
        System.out.println(delete);
        // DeleteResponse[index=yuan_index,type=_doc,id=1,version=3,result=deleted,shards=ShardInfo{total=2, successful=1, failures=[]}]
    }

    // 批量插入信息
    @Test
    public void testBulkDocument() throws IOException{
        BulkRequest bulkRequest = new BulkRequest();
        ArrayList<DiscussPost> discussPosts = new ArrayList<>();
        discussPosts.add(new DiscussPost(1,2,"更新标题01","更新内容",1,1,new Date(),2,2));
        discussPosts.add(new DiscussPost(1,2,"更新标题02","更新内容",1,1,new Date(),2,2));
        discussPosts.add(new DiscussPost(1,2,"更新标题03","更新内容",1,1,new Date(),2,2));

        for (int i = 0; i < discussPosts.size(); i++) {
            // 批量更新删除在这里修改对应的请求
            bulkRequest.add(
                    new IndexRequest("yuan_index")
                    .id(""+i+1)
                    .source(JSON.toJSONString(discussPosts.get(i)),XContentType.JSON));
        }
        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        System.out.println(bulkResponse.status());
        System.out.println(bulkResponse.hasFailures());
    }

    // 查询
    // SearchRequest 搜索请求
    // SearchSourceBuilder 条件构造
    @Test
    public void testSearch() throws IOException {
        SearchRequest request = new SearchRequest("yuan_index");
        // 构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

//        searchSourceBuilder.highlighter();  // 高亮

        // 查询条件，可以使用QueryBuilders来实现
        //  QueryBuilders.termQuery 精确搜索
        // QueryBuilders.matchAllQuery()    匹配所有
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("content", "更新内容");

        searchSourceBuilder.query(termQueryBuilder);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        request.source(searchSourceBuilder);

        SearchResponse searchResponse = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        System.out.println(JSON.toJSONString(searchResponse.getHits()));
        System.out.println("--------------------");
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            System.out.println(hit.getSourceAsMap());
        }

    }
}
