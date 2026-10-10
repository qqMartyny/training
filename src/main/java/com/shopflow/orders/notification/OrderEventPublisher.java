package com.shopflow.orders.notification;

import com.shopflow.orders.order.OrderStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Пересылает доменные события заказа в Kafka. Отправка — только после коммита транзакции,
 * ключ сообщения — id заказа (гарантирует порядок событий одного заказа внутри партиции).
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

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void on(OrderStatusChangedEvent event) {
		OrderEventMessage message = OrderEventMessage.from(event);
		kafkaTemplate.send(topic, message.orderId().toString(), message).whenComplete((result, ex) -> {
			if (ex != null) {
				log.error("Failed to publish event {} for order {}", message.eventId(), message.orderId(), ex);
			}
		});
	}

}
