package com.healthrecord.healthrecord;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HealthrecordApplication {

	public static void main(String[] args) {
		SpringApplication.run(HealthrecordApplication.class, args);
	}

}
