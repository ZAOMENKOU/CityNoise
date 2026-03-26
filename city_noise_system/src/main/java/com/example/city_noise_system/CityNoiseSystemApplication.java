package com.example.city_noise_system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.example.city_noise_system.mapper")
@EnableScheduling
public class CityNoiseSystemApplication {
	public static void main(String[] args) {
		SpringApplication.run(CityNoiseSystemApplication.class, args);
	}
}