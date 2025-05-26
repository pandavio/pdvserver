package com.pandav.pdvserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PdvServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(PdvServerApplication.class, args);
	}

}
