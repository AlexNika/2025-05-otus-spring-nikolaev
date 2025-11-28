package ru.pricat.service;

import reactor.core.publisher.Mono;

/**
 * Интерфейс сервиса для взаимодействия с client-service через WebClient.
 * Определяет методы для проверки уникальности email и создания профиля пользователя
 * в другом микросервисе с использованием внутреннего API.
 */
public interface WebClientService {

    /**
     * Проверяет, является ли указанный email уникальным в client-service.
     *
     * @param email Email для проверки.
     * @return реактивный объект, содержащий true, если email уникален, иначе false.
     */
    Mono<Boolean> isEmailUnique(String email);

    /**
     * Создает профиль пользователя в client-service.
     *
     * @param username Имя пользователя для профиля.
     * @param email    Email для профиля.
     * @return реактивный объект, сигнализирующий о завершении операции.
     */
    Mono<Void> createProfile(String username, String email);
}
