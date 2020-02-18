package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * DemoApplication
 */
@EnableJpaAuditing	// JPA Auditing 활성화
@SpringBootApplication
public class DemoApplication {

	public static final String APPLICATION_LOCATIONS = "spring.config.location=" +
			"classpath:application.yml," +
			"/app/config/springboot-webservice/real-application.yml";

	/**
	 * Main
	 * @param args arguments
	 */
	public static void main(String[] args) {
		new SpringApplicationBuilder(DemoApplication.class)
				.properties(APPLICATION_LOCATIONS)
				.run(args);
	}

}
