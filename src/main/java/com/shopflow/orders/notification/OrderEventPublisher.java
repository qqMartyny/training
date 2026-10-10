package com.shopflow.orders.notification;

import com.shopflow.orders.order.Order;
import com.shopflow.orders.order.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * SHOP-201: уведомляем сервис нотификаций и сервис доставки об изменении статуса заказа.
 */
@Component
public class OrderEventPublisher {

	private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);

	private final KafkaTemplate<String, OrderEventMessage> kafkaTemplate;

	private final String topic;

	public OrderEventPublisher(KafkaTemplate<String, OrderEventMessage> kafkaTemplate,
			@Value("${shopflow.kafka.topics.order-events}") String topic) {
		this.kafkaTemplate = kafkaTemplate;
		this.topic = topic;
	}

	public void publish(Order order, OrderStatus previousStatus) {
		OrderEventMessage message = OrderEventMessage.from(order, previousStatus);
		kafkaTemplate.send(topic, message);
		log.info("Order event sent: order={}, status={}", message.orderId(), message.status());
	}

}
