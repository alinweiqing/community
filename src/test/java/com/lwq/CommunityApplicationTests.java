package com.lwq;

import com.lwq.dao.DiscussPostMapper;
import com.lwq.dao.LoginTicketMapper;
import com.lwq.entity.DiscussPost;
import com.lwq.entity.LoginTicket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;

@SpringBootTest
class CommunityApplicationTests {

    @Autowired
    LoginTicketMapper loginTicketMapper;

    @Autowired
    private  DiscussPostMapper discussPostMapper;
    @Test
    void contextLoads() {
    }

    @Test
    public  void TestSelectPosts(){
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(149,0,10);
        for(DiscussPost post : list){
            System.out.println(post);
        }
        int rows=discussPostMapper.selectDiscussPostRows(149);
        System.out.println(rows);
    }


    @Test
    public  void TestInsertLoginTicket(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("a");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis()+1000*60*10));
        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public  void TestselectByTicket(){
        LoginTicket a = loginTicketMapper.selectByTicket("a");
        System.out.println(a);
        loginTicketMapper.updateStatus("a",0);
    }

    @Test
    public  void TestinsertDiscussPost(){
        DiscussPost discussPost=new DiscussPost();
        discussPost.setTitle("哈哈");
        discussPost.setContent("今天很开心！");
        discussPost.setType(0);
        discussPost.setStatus(0);
        discussPost.setCreateTime(new Date(System.currentTimeMillis()));
        discussPost.setCommentCount(0);
        discussPost.setScore(0);
        int i = discussPostMapper.insertDiscussPost(discussPost);
        System.out.println(i);
    }


}
