package ru.pricat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Основной класс приложения для микросервиса API-Gateway.
 * Этот сервис действует как единая точка входа для всех клиентских запросов,
 * выполняет проверку токена JWT, управление обновлением токена с помощью файлов cookie HttpOnly,
 * и маршрутизирует запросы к соответствующим микросервисам.
 */
@Slf4j
@EnableDiscoveryClient
@SpringBootApplication
public class ApiGatewayApplication {

    /**
     * Основная точка входа для приложения API-Gateway.
     *
     * @param args аргументы командной строки
     */
    static void main(String[] args) {
        log.info("Starting API Gateway application");
        SpringApplication.run(ApiGatewayApplication.class, args);
        log.info("API Gateway application started successfully");
    }
}
