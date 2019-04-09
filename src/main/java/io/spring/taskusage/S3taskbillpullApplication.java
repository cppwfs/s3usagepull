package io.spring.taskusage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.task.configuration.EnableTask;

@SpringBootApplication
public class S3taskbillpullApplication {

	public static void main(String[] args) {
		SpringApplication.run(S3taskbillpullApplication.class, args);
	}

}
