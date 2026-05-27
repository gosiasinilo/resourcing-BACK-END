package io.nology.resources;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ResourcesApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResourcesApplication.class, args);
	}

}
