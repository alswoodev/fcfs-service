package com.fcfs.couponapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import com.fcfs.couponcore.CouponCoreConfiguration;

@Import(CouponCoreConfiguration.class)
@SpringBootApplication
public class CouponapiApplication {

	public static void main(String[] args) {
		SpringApplication.run(CouponapiApplication.class, args);
	}

}
