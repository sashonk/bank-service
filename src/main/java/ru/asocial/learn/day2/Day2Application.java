package ru.asocial.learn.day2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@EnableAutoConfiguration
@SpringBootApplication
public class Day2Application {

	public static void main(String[] args) {
		SpringApplication.run(Day2Application.class, args);
	}

}
