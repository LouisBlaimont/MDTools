package be.uliege.speam.team03.MDTools;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
@AllArgsConstructor
public class MdToolsApplication {

	public static void main(String[] args) {
		SpringApplication.run(MdToolsApplication.class, args);
	}

}
