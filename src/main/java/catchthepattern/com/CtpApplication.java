package catchthepattern.com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class CtpApplication {

	public static void main(String[] args) {
		// SpringApplication.run(CtpApplication.class, "--debug");
		SpringApplication.run(CtpApplication.class, args);
	}

}
