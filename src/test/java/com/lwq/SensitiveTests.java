package com.lwq;

import com.alibaba.fastjson.JSONArray;
import com.lwq.util.SensitiveFilter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;

@SpringBootTest
public class SensitiveTests {
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public  void testSensitive(){
        String text="这里可以赌c博,可以吸毒,可以嫖娼,可以开票,可以草你妈的,哈哈哈!";
        text = sensitiveFilter.filter(text);
        System.out.println(text);

         text="这里可以□赌□博□,可以□□吸□□毒□□,可以嫖□□□娼,可以开票,可以草你妈的,哈哈哈!";
        text="这里可以□赌拖博□,可以□□吸少毒□□,可以嫖□□□娼,可以开票,可以草你妈的,哈哈哈!";
        text = sensitiveFilter.filter(text);
        System.out.println(text);

    }


    @ResponseBody
    public static String jsons() {
        HashMap<String, String> map = new HashMap<>();
        map.put("1", "q");
        map.put("2", "w");
        map.put("3", "x");
//        JSONObject jsonObject=new JSONObject(map);
//        System.out.println(jsonObject);
        return JSONArray.toJSONString(map);

    }

    public static void main(String[] args) {

        System.out.println(  jsons());
    }

}
