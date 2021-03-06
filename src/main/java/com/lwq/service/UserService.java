package com.lwq.service;

import com.lwq.dao.LoginTicketMapper;
import com.lwq.dao.UserMapper;
import com.lwq.entity.LoginTicket;
import com.lwq.entity.User;
import com.lwq.util.CommunityConstant;
import com.lwq.util.CommunityUtil;
import com.lwq.util.HostHolder;
import com.lwq.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;

import java.util.*;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private  HostHolder hostHolder;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();
        //空值处理
        if (user == null) {
            try {
                throw new IllegalAccessException("参数不能为空!");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空！");
            return map;
        }
        //验证账号
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "该账号已存在！");
            return map;
        }

        //验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "该邮箱已被注册！");
            return map;
        }

        // 注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
   user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        //user.setPassword(user.getPassword());
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);
        userMapper.updateStatus(user.getId(),1);

        //发送激活邮件
//        Context context = new Context();
//        context.setVariable("email", user.getEmail());

        //http://localhost:8080/cpmmunity/activation/101/code
//        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
//        context.setVariable("url", url);
//        String content = templateEngine.process("/mail/activation", context);
//        mailClient.sendMail(user.getEmail(), "激活账号", content);

        return map;
    }

    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPETITION;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILED;
        }

    }

    public Map<String, Object> login(String username, String password, int expredSeconds) {
        Map<String, Object> map = new HashMap<>();

        //空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }

        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }

        //验证账号
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "该账号不存在！");
            return map;
        }

        //验证状态
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活！");
            return map;
        }

        //验证密码
       password = CommunityUtil.md5(password + user.getSalt());
      //  password = password;
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码不正确！");
            return map;
        }

        //生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expredSeconds * 1000));
        loginTicketMapper.insertLoginTicket(loginTicket);

        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket) {
        loginTicketMapper.updateStatus(ticket, 1);

    }

    public LoginTicket findLoginTicket(String ticket) {

        return loginTicketMapper.selectByTicket(ticket);
    }

    public int updateHeader(int userId, String headerUrl) {
        return userMapper.updateHeader(userId, headerUrl);
    }

    public User findUserByName(String username){
        return  userMapper.selectByName(username);
    }



    public Map<String, Object> updatePassword(String oldPassword,String newPassword,String newPasswordConfim) {
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isBlank(oldPassword)) {
            map.put("oldPasswordMsg", "原始密码不能为空！");
            return map;
        }
        if (StringUtils.isBlank(newPassword)) {
            map.put("newPasswordMsg", "新密码不能为空！");
            return map;
        }
        if (StringUtils.isBlank(newPasswordConfim)) {
            map.put("newPasswordConfimMsg", "确认密码不能为空！");
            return map;
        }
        if (oldPassword.equals(newPassword)){
            map.put("newPasswordMsg", "新密码不能与旧密码相同！");
            return map;
        }
        if (!newPassword.equals(newPasswordConfim)){
            map.put("newPasswordConfimMsg", "确认密码与新密码不同！");
            return map;
        }

        //LoginTicket loginTicket = loginTicketMapper.selectByTicket(ticket);
        // User user = userMapper.selectById(loginTicket.getUserId());
        // 拦截器中已经将user存入了ThreadLocal内,可以使用HostHolder来获取user，避免重复请求数据库内容
         User user = hostHolder.getUser();
         oldPassword=CommunityUtil.md5(oldPassword+user.getSalt());
        if(!oldPassword.equals(user.getPassword())){
            map.put("oldPasswordMsg","密码错误！");
        return map;
        }
        userMapper.updatePassword(user.getId(),CommunityUtil.md5(newPassword+user.getSalt()));
        return map;



    }



    //授权
    public Collection<? extends GrantedAuthority> getAuthorities(int userId){
        User user = this.findUserById(userId);

        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()){
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                         return AUTHORITY_USER;

                }
            }
        });
        return list;
    }


}
