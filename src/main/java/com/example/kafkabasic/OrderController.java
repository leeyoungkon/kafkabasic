package com.example.kafkabasic;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderProducer orderProducer;

    public OrderController(OrderProducer orderProducer) {
        this.orderProducer = orderProducer;
    }

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody OrderEvent request) {
        String orderId = UUID.randomUUID().toString();

        OrderEvent event = new OrderEvent(
                "OrderCreated",
                orderId,
                request.getProductId(),
                request.getQuantity(),
                request.getAmount()
        );

        orderProducer.sendOrderCreated(event);

        return ResponseEntity.ok("Order sent to Kafka. orderId=" + orderId);
    }
}