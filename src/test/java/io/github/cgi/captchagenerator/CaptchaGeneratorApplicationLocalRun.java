package io.github.cgi.captchagenerator;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Tag("localRun")
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(initializers = {
		CaptchaGeneratorApplicationLocalRun.Initializer.class})
class CaptchaGeneratorApplicationLocalRun {

	@Container
	public static GenericContainer redis = new GenericContainer(DockerImageName.parse("redis:6.2.5-alpine"))
			.withExposedPorts(6379);


	@Test
	void localRun(){
		boolean interuped = false;
		do{
			try{
				Thread.sleep(10_000L);
			} catch (InterruptedException e){
				interuped = true;
			}
		} while (!interuped);
	}

	/**
	 * Класс добавляет в конфигурацию контекста параметры,
	 * значения которых зависят от контейнера.
	 */
	public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

		@Override
		public void initialize(ConfigurableApplicationContext applicationContext) {
			TestPropertyValues.of(
					"captcha.redis.hostName=" + redis.getHost(),
					"captcha.redis.port=" + redis.getFirstMappedPort()
			).applyTo(applicationContext.getEnvironment());
		}
	}

}
