package com.yuan.community;

import com.yuan.community.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class SensitiveTests {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void testSensitiveFilter() {
        String text = "我是秦始皇，朱元璋快来打钱，曹操有多少钱，李世民借我一点，朱厚璁要上天了！";
        text = sensitiveFilter.filter(text);
        System.out.println(text);

        text = "我是秦☆始☆皇，☆朱元☆璋快来打钱，曹☆操有多少钱，李世☆民借我一点，朱☆厚☆璁要上天了！";
        text = sensitiveFilter.filter(text);
        System.out.println(text);
    }

}
