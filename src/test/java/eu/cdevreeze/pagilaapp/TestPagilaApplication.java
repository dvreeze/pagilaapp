package eu.cdevreeze.pagilaapp;

import org.springframework.boot.SpringApplication;

public class TestPagilaApplication {

	public static void main(String[] args) {
		SpringApplication.from(PagilaApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
