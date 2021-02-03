package com.lwq;

import com.lwq.dao.DiscussPostMapper;
import com.lwq.dao.LoginTicketMapper;
import com.lwq.dao.MessageMapper;
import com.lwq.entity.DiscussPost;
import com.lwq.entity.LoginTicket;
import com.lwq.entity.Message;
import com.lwq.util.CommunityUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

@SpringBootTest
class CommunityApplicationTests {

    @Autowired
    LoginTicketMapper loginTicketMapper;

    @Autowired
    private  DiscussPostMapper discussPostMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Test
    void contextLoads() {
    }
    @Test
    public void testMd5() throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        // java自带工具包MessageDigest
        String resultString = CommunityUtil.md5("123456");
        System.out.println(resultString);
        // e10adc3949ba59abbe56e057f20f883e
        String resultString1 = CommunityUtil.md5("1234");
        System.out.println(resultString1);
        //81dc9bdb52d04dc20036dbd8313ed055

        // spring自带工具包DigestUtils
        System.out.println(DigestUtils.md5DigestAsHex("1234".getBytes()));
        // 81dc9bdb52d04dc20036dbd8313ed055
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


    @Test
    public void testSelectLetters() {
        List<Message> list = messageMapper.selectConversations(111, 0, 20);
        for (Message message : list) {
            System.out.println(message);
        }

        int count = messageMapper.selectConversationCount(111);
        System.out.println(count);

        list = messageMapper.selectLetters("111_112", 0, 10);
        for (Message message : list) {
            System.out.println(message);
        }

        count = messageMapper.selectLetterCount("111_112");
        System.out.println(count);

        count = messageMapper.selectLetterUnreadCount(131, "111_131");
        System.out.println(count);

    }



}
