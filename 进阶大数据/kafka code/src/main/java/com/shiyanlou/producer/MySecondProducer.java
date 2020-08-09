package com.shiyanlou.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Properties;
import java.util.concurrent.Future;

public class MySecondProducer {
    public static void main(String[] args) {
        Properties kafkaProps = new Properties();
        kafkaProps.put("bootstrap.servers", "localhost:9092");
        kafkaProps.put("key.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");
        kafkaProps.put("value.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");

        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(kafkaProps);
        ProducerRecord<String, String> record = new ProducerRecord<String, String>("mySecondTopic",
                "messageKey", "hello kafka");

        try {
            Future<RecordMetadata> future = producer.send(record);
            RecordMetadata recordMetadata = future.get();
            long offset = recordMetadata.offset();
            int partition = recordMetadata.partition();
            System.out.println("the message offset: "+offset+" , partition: "+partition+" .");
            producer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
