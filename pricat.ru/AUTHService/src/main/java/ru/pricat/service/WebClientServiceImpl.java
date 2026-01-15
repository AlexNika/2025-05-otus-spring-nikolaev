package ru.pricat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import ru.pricat.config.properties.InternalApiKeyConfig;
import ru.pricat.exception.ClientServiceCommunicationException;
import ru.pricat.exception.EmailNotUniqueException;
import ru.pricat.model.dto.request.EmailCheckRequestDto;
import ru.pricat.model.dto.response.EmailCheckResponseDto;
import ru.pricat.model.dto.request.ProfileCreateRequestDto;

/**
 * Реализация {@link WebClientService} для взаимодействия с client-service.
 * Использует WebClient для выполнения HTTP-запросов и проверки уникальности email
 * или создания профиля с аутентификацией через внутренний API-ключ.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebClientServiceImpl implements WebClientService {

    /**
     * Конфигурация для получения внутреннего API-ключа и базового URL client-service.
     */
    private final InternalApiKeyConfig internalApiKeyConfig;

    /**
     * Builder WebClient для создания HTTP-клиентов.
     */
    private final WebClient.Builder webClientBuilder;

    /**
     * Проверяет, является ли указанный email уникальным в client-service.
     * Отправляет POST-запрос на внутренний endpoint client-service с использованием
     * внутреннего API-ключа для аутентификации между микросервисами. Преобразует ответ в boolean.
     *
     * @param email Email для проверки.
     * @return реактивный объект, содержащий true, если email уникален, иначе false.
     * @throws ClientServiceCommunicationException если произошла ошибка при взаимодействии с client-service
     */
    public Mono<Boolean> isEmailUnique(String email) {
        String url = internalApiKeyConfig.getClientServiceBaseUrl() + "/internal/check-email";
        log.debug("Checking email uniqueness for '{}' via client-service: {}", email, url);
        EmailCheckRequestDto emailCheckRequestDto = new EmailCheckRequestDto(email);
        return webClientBuilder.build()
                .post()
                .uri(url)
                .header("X-API-Key", internalApiKeyConfig.getInternalApiKey())
                .bodyValue(emailCheckRequestDto)
                .retrieve()
                .bodyToMono(EmailCheckResponseDto.class)
                .map(EmailCheckResponseDto::isUnique)
                .doOnSuccess(isUnique -> log.debug("Email uniqueness check result: {}", isUnique))
                .onErrorMap(error -> new ClientServiceCommunicationException(
                        "Failed to check email uniqueness for '" + email + "' due to client-service error: "
                        + error.getMessage(), error));
    }

    /**
     * Создает профиль пользователя в client-service.
     * Отправляет POST-запрос на внутренний endpoint client-service с использованием
     * внутреннего API-ключа для аутентификации между микросервисами. Обрабатывает HTTP-ошибки 4xx и 5xx,
     * выбрасывая соответствующие исключения.
     *
     * @param username Имя пользователя для профиля.
     * @param email    Email для профиля.
     * @return реактивный объект, сигнализирующий о завершении операции.
     * @throws EmailNotUniqueException              если client-service сообщает, что email уже существует (409 Conflict)
     * @throws ClientServiceCommunicationException  если произошла ошибка 5xx или другая ошибка WebClient
     */
    @Override
    public Mono<Void> createProfile(String username, String email) {
        String url = internalApiKeyConfig.getClientServiceBaseUrl() + "/internal/profile";
        log.debug("Creating profile for user '{}' with email '{}' via client-service: {}", username, email, url);
        ProfileCreateRequestDto requestDto = new ProfileCreateRequestDto(username, email);
        return webClientBuilder.build()
                .post()
                .uri(url)
                .header("X-API-Key", internalApiKeyConfig.getInternalApiKey())
                .bodyValue(requestDto)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    if (response.statusCode().equals(HttpStatus.CONFLICT)) {
                        log.warn("Client-service reported: Email already exists for user '{}'", username);
                        return Mono.error(new EmailNotUniqueException("Email already exists in client-service"));
                    }
                    log.warn("Client-service returned client error {} for profile creation request for user '{}'",
                            response.statusCode(), username);
                    return Mono.error(new RuntimeException("Client-service returned client error: " +
                                                           response.statusCode()));
                })
                .onStatus(HttpStatusCode::is5xxServerError, response -> {
                    log.error("Client-service returned server error {} for profile creation request for user '{}'",
                            response.statusCode(), username);
                    return Mono.error(new ClientServiceCommunicationException("Client-service returned server error: " +
                                                                              response.statusCode()));
                })
                .bodyToMono(Void.class)
                .doOnSuccess(_ -> log.info("Profile created successfully in client-service for user: {}",
                        username))
                .doOnError(error -> log.error("Failed to create profile in client-service for user: {}",
                        username, error))
                .onErrorMap(WebClientResponseException.class, ex ->
                        new ClientServiceCommunicationException("Failed to create profile for user '" + username +
                                                                "' due to client-service error: " + ex.getStatusCode() +
                                                                " " + ex.getResponseBodyAsString(), ex));
    }
}
