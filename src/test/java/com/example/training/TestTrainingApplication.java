package com.example.training;

import org.springframework.boot.SpringApplication;

public class TestTrainingApplication {

	public static void main(String[] args) {
		SpringApplication.from(TrainingApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
