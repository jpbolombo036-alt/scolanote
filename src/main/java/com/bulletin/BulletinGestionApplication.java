package com.bulletin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BulletinGestionApplication {

	public static void main(String[] args) {
		SpringApplication.run(BulletinGestionApplication.class, args);
	}
}