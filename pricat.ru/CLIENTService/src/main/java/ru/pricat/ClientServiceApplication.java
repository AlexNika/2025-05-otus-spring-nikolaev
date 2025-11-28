package ru.pricat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import ru.pricat.config.properties.EnvLoader;

@Slf4j
@EnableDiscoveryClient
@SpringBootApplication
@ConfigurationPropertiesScan
public class ClientServiceApplication {

    static void main(String[] args) {
        EnvLoader.loadEnvFile();
        log.info("Starting Client service application");
        SpringApplication.run(ClientServiceApplication.class, args);
        log.info("Client service application started successfully");
    }

}
