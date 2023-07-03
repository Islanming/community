package com.example.community;

import com.example.community.util.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SensitiveFilterTest {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void testSensitiveFilter(){
//        String string = "这里可以赌博，可以吸毒，可以嫖娼，可以代考，但是不接待傻逼，哈哈哈哈！";
        String string = "❤嫖❤嫖❤娼❤";

//        System.out.println(text);

        System.out.println(sensitiveFilter.filter(string));
//        System.out.println(sensitiveFilter.filter(text));
    }


}
