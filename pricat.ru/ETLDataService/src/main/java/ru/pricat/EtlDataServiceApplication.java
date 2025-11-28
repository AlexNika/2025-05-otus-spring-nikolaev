package ru.pricat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import ru.pricat.config.EnvLoader;

@Slf4j
@EnableDiscoveryClient
@SpringBootApplication
@ConfigurationPropertiesScan
public class EtlDataServiceApplication {

	static void main(String[] args) {
        EnvLoader.loadEnvFile();
        log.info("Starting ETLData service application");
		SpringApplication.run(EtlDataServiceApplication.class, args);
        log.info("ETLData service application started successfully");
	}

}
