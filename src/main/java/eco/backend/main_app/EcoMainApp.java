package eco.backend.main_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class EcoMainApp {
	public static void main(String[] args) {
		SpringApplication.run(EcoMainApp.class, args);
	}
}