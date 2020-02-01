package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * DemoApplication
 */
@EnableJpaAuditing	// JPA Auditing 활성화
@SpringBootApplication
public class DemoApplication {

	/**
	 * Main
	 * @param args arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
