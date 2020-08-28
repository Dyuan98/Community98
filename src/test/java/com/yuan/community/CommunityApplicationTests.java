package com.yuan.community;

import com.yuan.community.dao.DiscussPostMapper;
import com.yuan.community.dao.UserMapper;
import com.yuan.community.entity.DiscussPost;
import com.yuan.community.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class CommunityApplicationTests {

	@Autowired(required = false)
	private UserMapper userMapper;

	@Autowired(required = false)
	private DiscussPostMapper discussPostMapper;

	@Test
	void contextLoads() {
	}

	@Test
	public void testSelectUser(){
		User user = userMapper.selectById(111);
		System.out.println(user);
	}
	@Test
	public void testSelectDiscuss(){
		int i = discussPostMapper.selectDiscussPostRows(101);
		System.out.println(i);
		List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(0, 1, 5,0);
		for (DiscussPost discussPost : discussPosts) {
			System.out.println(discussPost);
		}
	}
}
