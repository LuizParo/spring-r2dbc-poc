package me.github.lgparo.r2dbc;

import lombok.extern.slf4j.Slf4j;
import me.github.lgparo.r2dbc.domain.Child;
import me.github.lgparo.r2dbc.domain.Parent;
import me.github.lgparo.r2dbc.service.ParentService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.List;
import java.util.UUID;

@Slf4j
@SpringBootApplication
@EnableTransactionManagement
public class R2dbcDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(R2dbcDemoApplication.class, args);
	}

	@Bean
	public CommandLineRunner demo(ParentService parentService) {
		return args -> {
			final Parent parent1 = Parent
					.builder()
					.id(UUID.randomUUID().toString())
					.name("parent1")
					.age(40)
					.build();

			final Child child1 = Child
					.builder()
					.id(UUID.randomUUID().toString())
					.name("child1")
					.age(10)
					.parent(parent1)
					.build();

			final Child child2 = Child
					.builder()
					.id(UUID.randomUUID().toString())
					.name("child2")
					.age(10)
					.parent(parent1)
					.build();

			parentService
					.save(
							parent1
									.toBuilder()
									.children(List.of(child1, child2))
									.build()
					)
					.block();

			parentService
					.findAll()
					.doOnNext(parent -> log.info("Parent: " + parent))
					.blockLast();
		};
	}
}