package ru.pricat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@Slf4j
@EnableEurekaServer
@SpringBootApplication
public class EurekaServerApplication {

    static void main(String[] args) {
        log.info("Starting EurekaServer service application");
        SpringApplication.run(EurekaServerApplication.class, args);
        log.info("EurekaServer service application started successfully");
        log.info("EurekaServer URL: http://localhost:8761");
    }

}
