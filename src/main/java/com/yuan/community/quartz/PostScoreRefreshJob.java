package com.yuan.community.quartz;

import com.yuan.community.entity.DiscussPost;
import com.yuan.community.service.DiscussPostService;
import com.yuan.community.service.ElasticSearchService;
import com.yuan.community.service.LikeService;
import com.yuan.community.util.CommunityConstant;
import com.yuan.community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PostScoreRefreshJob implements Job, CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticSearchService elasticSearchService;

    // 纪元
    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2019-08-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化纪元失败!", e);
        }
    }

    @Override
    public void execute(JobExecutionContext context){
        String redisKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);

        if (operations.size() == 0) {
            logger.info("[任务取消] 没有需要刷新的帖子!");
            return;
        }

        logger.info("[任务开始] 正在刷新帖子分数: " + operations.size());
        while (operations.size() > 0) {
            try {
                this.refresh((Integer) operations.pop());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        logger.info("[任务结束] 帖子分数刷新完毕!");
    }

    private void refresh(int postId) throws IOException {
        DiscussPost post = discussPostService.findDiscussPostById(postId);

        if (post == null) {
            logger.error("该帖子不存在: id = " + postId);
            return;
        }

        // 是否精华
        boolean wonderful = post.getStatus() == 1;
        // 评论数量
        int commentCount = post.getCommentCount();
        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);

        // 计算权重
        double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
        // 分数 = 帖子权重 + 距离天数
        double score = Math.log10(Math.max(w, 1))
                + (post.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);
        // 更新帖子分数
        discussPostService.updateScore(postId, score);
        // 同步搜索数据
        post.setScore(score);
        elasticSearchService.saveDiscussPost(post);
    }

}