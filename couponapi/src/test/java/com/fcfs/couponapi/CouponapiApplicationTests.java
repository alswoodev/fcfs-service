package com.fcfs.couponapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.config.name=application-core-test")
class CouponapiApplicationTests {

	@Test
	void contextLoads() {
	}

}
