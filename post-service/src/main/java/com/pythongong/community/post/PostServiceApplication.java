package com.pythongong.community.post;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableR2dbcRepositories
@SpringBootApplication(scanBasePackages = { "com.pythongong.community" })
public class PostServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PostServiceApplication.class, args);
	}

}
