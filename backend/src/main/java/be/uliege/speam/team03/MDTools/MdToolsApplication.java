package be.uliege.speam.team03.MDTools;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class MdToolsApplication {

	public static void main(String[] args) {
		SpringApplication.run(MdToolsApplication.class, args);
	}

}
