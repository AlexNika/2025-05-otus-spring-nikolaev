package ru.pricat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.pricat.config.properties.EnvLoader;

@Slf4j
@EnableCaching
@EnableScheduling
@EnableDiscoveryClient
@SpringBootApplication
public class AuthServiceApplication {

	static void main(String[] args) {
        EnvLoader.loadEnvFile();
        log.info("Starting Auth service application");
		SpringApplication.run(AuthServiceApplication.class, args);
        log.info("Auth service application started successfully");
        log.info("Auth service URL: http://localhost:8081/actuator");
    }

}
