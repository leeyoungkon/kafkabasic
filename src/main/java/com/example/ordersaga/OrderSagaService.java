package com.example.ordersaga;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class OrderSagaService {

    private final OrderRepository orderRepository;
    private final OrderProducer orderProducer;
    private final SagaEventLogRepository sagaEventLogRepository;

    public Order createOrder(CreateOrderRequest request) {
        String orderId = UUID.randomUUID().toString();

        Order order = new Order(
                orderId,
                request.getProductId(),
                request.getQuantity(),
                request.getAmount(),
                OrderStatus.CREATED
        );
        orderRepository.save(order);

        SagaEvent event = new SagaEvent(
                "OrderCreated",
                order.getOrderId(),
                order.getProductId(),
                order.getQuantity(),
                order.getAmount(),
                null
        );
        sagaEventLogRepository.save(SagaEventLog.of("ORDER_SERVICE", event));
        orderProducer.publishOrderCreated(event);
        return order;
    }

    public void markStockReserved(String orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order != null && order.getStatus() != OrderStatus.CANCELLED) {
            order.setStatus(OrderStatus.STOCK_RESERVED);
            orderRepository.save(order);
        }
    }

    public void markCompleted(String orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order != null && order.getStatus() != OrderStatus.CANCELLED) {
            order.setStatus(OrderStatus.COMPLETED);
            orderRepository.save(order);
        }
    }

    public void cancelOrder(String orderId, String reason) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order != null) {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);

            SagaEvent cancelEvent = new SagaEvent(
                    "OrderCancelled",
                    order.getOrderId(),
                    order.getProductId(),
                    order.getQuantity(),
                    order.getAmount(),
                    reason
            );
            sagaEventLogRepository.save(SagaEventLog.of("ORDER_SERVICE", cancelEvent));
            orderProducer.publishOrderCancelled(cancelEvent);
        }
    }

    public void logIncomingEvent(String source, SagaEvent event) {
        sagaEventLogRepository.save(SagaEventLog.of(source, event));
    }

    public Collection<Order> findAll() {
        return orderRepository.findAll();
    }

    public List<SagaEventLog> findAllEvents() {
        return sagaEventLogRepository.findAllByOrderByCreatedAtDesc();
    }
}