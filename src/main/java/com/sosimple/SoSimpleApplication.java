package com.sosimple;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com")
public class SoSimpleApplication {

	public static void main(String[] args) {
		SpringApplication.run(SoSimpleApplication.class, args);
	}

}
