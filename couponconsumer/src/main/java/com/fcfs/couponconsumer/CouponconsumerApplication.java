package com.fcfs.couponconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import com.fcfs.couponcore.CouponCoreConfiguration;

@Import(CouponCoreConfiguration.class)
@SpringBootApplication
public class CouponconsumerApplication {

	public static void main(String[] args) {
		System.setProperty("spring.config.name", "application-core,application-consumer");
		SpringApplication.run(CouponconsumerApplication.class, args);
	}

}
