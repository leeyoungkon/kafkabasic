package com.example.kafkabasic;


import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderConsumer {

 @KafkaListener(topics = "order-events", groupId = "order-demo-group")
    public void consume(OrderEvent event) {
        System.out.println("[CONSUMER] " + event);
    }
}