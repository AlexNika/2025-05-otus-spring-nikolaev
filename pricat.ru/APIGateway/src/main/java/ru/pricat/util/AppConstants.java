package ru.pricat.util;

/**
 * Класс, содержащий константы, используемые в приложении.
 * Включает в себя базовые пути к API-эндпоинтам для различных сервисов.
 */
public class AppConstants {

    /**
     * Базовый путь для API-эндпоинтов, связанных с клиентским сервисом.
     */
    public static final String API_V1_CLIENT_PATH = "/api/v1/client";

    /**
     * Базовый путь для API-эндпоинтов, связанных с сервисом аутентификации.
     */
    public static final String API_V1_AUTH_PATH = "/api/v1/auth";

    /**
     * Приватный конструктор для предотвращения создания экземпляров класса.
     */
    private AppConstants() {
    }
}