package org.xiaohuadev.kafka.sample;

import org.apache.kafka.clients.producer.*;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * 生产者
 */
public class ProducerQuickStart {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //1.配置kafka连接信息
        Properties prop = new Properties();
        //kafka服务器连接地址
        prop.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "39.105.170.134:9092");
        //key和value的序列化配置
        prop.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        prop.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        //2.创建kafka生产者对象
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(prop);

        //3.发送消息
        ProducerRecord<String, String> record = new ProducerRecord<String, String>
                ("topic-first", "key-001", "hello kafka");

        //同步发送消息
        /*RecordMetadata recordMetadata = producer.send(record).get();
        System.out.println(recordMetadata.offset());*/

        //异步发送消息
        producer.send(record, new Callback() {
            @Override
            public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                if (e != null) {
                    System.out.println("记录异常信息到日志表中");
                }
                System.out.println(recordMetadata.offset());
            }
        });

        //4.关闭消息通道 (必须要关闭 否则消息发送不成功)
        producer.close();
    }
}
