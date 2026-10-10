package com.shopflow.orders;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Базовый класс интеграционных тестов. Контекст и контейнеры переиспользуются между тестами
 * (Spring TestContext cache), поэтому каждый тест работает со своим покупателем — см. {@link #newCustomerId()}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public abstract class AbstractIntegrationTest {

	@Autowired
	protected MockMvc mockMvc;

	protected static long newCustomerId() {
		return ThreadLocalRandom.current().nextLong(1_000_000, Long.MAX_VALUE);
	}

}
