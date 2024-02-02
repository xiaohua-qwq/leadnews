package org.xiaohuadev.kafka.listener;

import com.alibaba.fastjson.JSON;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.xiaohuadev.kafka.pojo.User;

import java.util.List;

@Component
public class HelloListener {

    @KafkaListener(topics = "user-topic")
    public void onMessage(String message) {
        if (!StringUtils.isEmpty(message)) {
            User user = JSON.parseObject(message, User.class);
            System.out.println(user);
        }
    }
}
