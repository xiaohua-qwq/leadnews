package org.xiaohuadev.kafka.sample;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.consumer.internals.ConsumerProtocol;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * 消费者
 */
public class ConsumerQuickStart {
    public static void main(String[] args) {
        //1.设置基本信息
        Properties prop = new Properties();
        //连接地址
        prop.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "39.105.170.134:9092");
        //key和value反序列化器
        prop.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        prop.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        //设置消费者组
        prop.put(ConsumerConfig.GROUP_ID_CONFIG, "group1");

        //关闭自动提交偏移量
        prop.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        //2.创建消费者对象
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(prop);

        //3.订阅主题
        consumer.subscribe(Collections.singletonList("topic-first"));

        //同步+异步提交偏移量
        try {
            while (true) {
                ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofMinutes(1000));
                for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                    System.out.println(consumerRecord.key());
                    System.out.println(consumerRecord.value());
                    System.out.println(consumerRecord.offset());
                }
                consumer.commitAsync(); //异步提交偏移量
            }
        } catch (Exception e) {
            System.out.println("记录错误信息: " + e.getMessage());
        } finally {
            consumer.commitSync(); //同步提交偏移量
        }

        //4.拉取消息
        /*while (true) {
            ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofMinutes(1000));
            for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                System.out.println(consumerRecord.key());
                System.out.println(consumerRecord.value());
                System.out.println(consumerRecord.offset());

                try {
                    //同步提交偏移量
                    consumer.commitAsync();
                } catch (CommitFailedException e) {
                    System.out.println("记录提交失败的异常" + e.getMessage());
                }
            }
            //异步提交偏移量
            consumer.commitAsync(new OffsetCommitCallback() {
                @Override
                public void onComplete(Map<TopicPartition, OffsetAndMetadata> map, Exception e) {
                    if (e != null) {
                        System.out.println("记录错误的提交偏移量" + map + " 异常信息为: " + e.getMessage());
                    }
                }
            });
        }*/
    }

}
