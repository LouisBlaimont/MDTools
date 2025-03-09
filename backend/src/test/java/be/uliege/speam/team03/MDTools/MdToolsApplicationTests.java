package be.uliege.speam.team03.MDTools;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import be.uliege.speam.team03.MDTools.config.SecurityConfig;
import be.uliege.speam.team03.MDTools.config.TestSecurityConfig;

@ContextConfiguration(classes = TestSecurityConfig.class)
@SpringBootTest
class MdToolsApplicationTests {

@Test
void contextLoads() { 	}

}
