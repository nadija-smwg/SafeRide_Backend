package com.saferide.SaferideBackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SaferideBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SaferideBackendApplication.class, args);
	}

}
