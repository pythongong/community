package com.pythongong.community.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.pythongong.community.infras.constant.GlobalVariable;

@EnableJpaRepositories
@SpringBootApplication(scanBasePackages = { GlobalVariable.INFRAS_PACKAGE, "com.pythongong.community.user" })
public class UserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}

}
