package com.shopflow.orders.notification;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.jayway.jsonpath.JsonPath;
import com.shopflow.orders.AbstractIntegrationTest;
import com.shopflow.orders.ApiClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.kafka.autoconfigure.KafkaConnectionDetails;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class OrderEventsIT extends AbstractIntegrationTest {

	@Autowired
	private KafkaConnectionDetails kafka;

	@Value("${shopflow.kafka.topics.order-events}")
	private String topic;

	@Test
	void publishesOrderLifecycleKeyedByOrderId() throws Exception {
		ApiClient api = new ApiClient(mockMvc, newCustomerId());
		api.addToCart(1005L, 1);
		String orderId = api.checkoutAndGetOrderId("CARD", "tok_ok");

		try (KafkaConsumer<String, String> consumer = consumer()) {
			consumer.subscribe(List.of(topic));
			List<ConsumerRecord<String, String>> received = new ArrayList<>();
			await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
				consumer.poll(Duration.ofMillis(200)).forEach(r -> {
					if (r.value().contains(orderId)) {
						received.add(r);
					}
				});
				assertThat(received).hasSize(2);
			});

			assertThat(received).allSatisfy(r -> assertThat(r.key()).isEqualTo(orderId));
			assertThat(received).extracting(r -> (String) JsonPath.read(r.value(), "$.status"))
				.containsExactly("NEW", "PAID");
		}
	}

	private KafkaConsumer<String, String> consumer() {
		return new KafkaConsumer<>(Map.of(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
				ConsumerConfig.GROUP_ID_CONFIG, "it-" + UUID.randomUUID(), ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
				"earliest"), new StringDeserializer(), new StringDeserializer());
	}

}
