package com.shopflow.orders;

import org.springframework.boot.SpringApplication;

/**
 * Локальный запуск без docker compose: поднимает Postgres, Redis и Kafka в Testcontainers.
 */
public class TestOrderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(OrderServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
