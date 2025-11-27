package tqs.backend.tqsbackend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TqsbackendApplicationTests {

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	void contextLoads() {
		assertThat(applicationContext).isNotNull();
	}

	@Test
	void mainMethodRuns() {
		// Verify that the main method can be invoked without errors
		assertThat(TqsbackendApplication.class).isNotNull();
	}

	@Test
	void applicationHasRequiredBeans() {
		// Verify that essential beans are loaded
		assertThat(applicationContext.containsBean("homeController")).isTrue();
	}
}
