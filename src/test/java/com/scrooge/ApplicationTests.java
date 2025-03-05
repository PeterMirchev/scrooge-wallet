package com.scrooge;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootTest(classes = Application.class)  // Point to the main application class
@EnableFeignClients
class ApplicationTests {

	@Test
	void contextLoads() {
	}

}
