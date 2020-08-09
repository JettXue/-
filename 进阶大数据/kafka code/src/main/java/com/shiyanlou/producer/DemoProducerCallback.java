package com.shiyanlou.producer;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;

public class DemoProducerCallback implements Callback {

    public void onCompletion(RecordMetadata recordMetadata, Exception e) {
        if (e != null) {
            e.printStackTrace();
        } else {
            long offset = recordMetadata.offset();
            int partition = recordMetadata.partition();
            String topic = recordMetadata.topic();
            System.out.println("the message topic: "+topic
                    +",offset: "+offset+",partition: "+partition);

        }
    }
}
