package com.roy.morago;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class MoragoApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoragoApplication.class, args);
	}

}
