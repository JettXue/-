package com.shiyanlou.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class MultiThreadProducer extends Thread {

    private final KafkaProducer<String, String> producer;

    private final String topic;

    private final int messageNumToSend = 10;

    public MultiThreadProducer(String topicName) {
        Properties kafkaProps = new Properties();
        kafkaProps.put("bootstrap.servers", "localhost:9092");
        kafkaProps.put("key.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");
        kafkaProps.put("value.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");
        producer = new KafkaProducer<String, String>(kafkaProps);
        topic = topicName;
    }

    @Override
    public void run() {
        int messageNo = 0;
        while (messageNo <= messageNumToSend) {
            String messageContent = "Message_" + messageNo;
            ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic,
                    messageNo + "", messageContent);
            producer.send(record, new DemoProducerCallback());
            messageNo++;
        }
        producer.flush();
    }

    public static void main(String[] args) {
        for (int i = 0; i < 3; i++) {
            new MultiThreadProducer("mySecondTopic").start();
        }
    }
}
