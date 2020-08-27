package com.yuan.community.dao.elasticsearch;

import com.yuan.community.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

//@Repository                                                         // 处理的实体类 ， 主键
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost, Integer> {

}
