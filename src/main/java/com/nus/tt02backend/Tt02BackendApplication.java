package com.nus.tt02backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class Tt02BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(Tt02BackendApplication.class, args);
	}

}
