package org.xiaohuadev.kafka.controller;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xiaohuadev.kafka.pojo.User;

@RestController
public class HelloController {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @GetMapping("/hello")
    public String hello() {
        //kafkaTemplate.send("itcast-topic", "hello world");
        User user = new User();
        user.setUsername("xiaowang");
        user.setAge(18);
        kafkaTemplate.send("user-topic", JSON.toJSONString(user));
        return "ok";
    }
}
