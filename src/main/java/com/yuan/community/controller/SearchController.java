package com.yuan.community.controller;


import com.yuan.community.entity.Page;
import com.yuan.community.service.ElasticSearchService;
import com.yuan.community.service.LikeService;
import com.yuan.community.service.UserService;
import com.yuan.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {


    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    // search?keyword=xxx
    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model) throws IOException {
        // 搜索帖子获取从es中查询到的信息
        List<Map<String, Object>> discussPostsList = elasticSearchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());

        System.out.println(discussPostsList);
//   [{score=0.0, createTime=1598496592000, id=283, title=这是个<span style='color:red'>帖</span><span style='color:red'>子</span>, type=0, userId=158, content=帖子帖子。, commentCount=0, status=0}]
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if(discussPostsList!=null){
            for (int i = 0; i < discussPostsList.size(); i++) {
                // 查询帖子作者
                Map<String, Object> map = discussPostsList.get(i);
                map.put("user",userService.findUserById((Integer) map.get("userId")));
                // 查询帖子获赞量
                map.put("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_POST, (Integer)map.get("id")));
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        model.addAttribute("keyword",keyword);

        //分页信息
        page.setLimit(10);
        page.setPath("/search?keyword="+keyword);
        page.setRows(discussPostsList == null ? 0 : elasticSearchService.searchCount(keyword));
        model.addAttribute("page",page);
        return "/site/search";
    }

}
