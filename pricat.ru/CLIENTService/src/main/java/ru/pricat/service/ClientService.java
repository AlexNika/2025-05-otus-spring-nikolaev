package ru.pricat.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.pricat.model.Client;
import ru.pricat.model.dto.request.ProfileCreateRequestDto;
import ru.pricat.model.dto.request.ProfilePatchRequestDto;
import ru.pricat.model.dto.request.ProfileUpdateRequestDto;
import ru.pricat.model.dto.response.AdminProfileDto;
import ru.pricat.model.dto.response.ProfileResponseDto;

public interface ClientService {

    /**
     * Создает профиль клиента на основе запроса.
     *
     * @param request данные для создания профиля
     * @return созданный клиент
     */
    Client createClientProfile(ProfileCreateRequestDto request);

    /**
     * Проверяет, уникален ли email клиента.
     *
     * @param email email для проверки
     * @return true если email уникален, иначе false
     */
    boolean isClientEmailUnique(String email);

    /**
     * Получает список всех клиентов с пагинацией.
     * Используется администраторами для просмотра всех пользователей.
     *
     * @param pageable параметры пагинации
     * @return страница с клиентами
     */
    Page<AdminProfileDto> getAllClients(Pageable pageable);

    /**
     * Находит клиента по email.
     *
     * @param email email клиента
     * @return клиент
     */
    Client getClientProfileByEmail(String email);

    /**
     * Проверяет существование клиента по имени пользователя.
     *
     * @param username имя пользователя
     * @return true если клиент существует, иначе false
     */
    boolean existsClientProfileByUsername(String username);

    /**
     * Проверяет существование клиента по e-mail пользователя.
     *
     * @param email e-mail пользователя
     * @return true если клиент существует, иначе false
     */
    boolean existsClientProfileByEmail(String email);

    /**
     * Получает профиль текущего аутентифицированного пользователя.
     *
     * @param username имя пользователя
     * @return DTO с данными профиля
     */
    AdminProfileDto getCurrentUserProfile(String username);

    /**
     * Получает профиль пользователя по username.
     * Используется администраторами для просмотра чужих профилей.
     *
     * @param username имя пользователя для поиска
     * @return DTO с данными профиля
     */
    AdminProfileDto getUserProfile(String username);

    /**
     * Полностью обновляет профиль текущего пользователя.
     * Проверяет уникальность email при изменении.
     *
     * @param username имя пользователя
     * @param request DTO с данными для обновления
     * @return обновленный DTO профиля
     */
    ProfileResponseDto updateCurrentUserProfile(String username, ProfileUpdateRequestDto request);

    /**
     * Обновляет профиль пользователя по username.
     * Используется администраторами для изменения чужих профилей.
     *
     * @param username имя пользователя для обновления
     * @param request DTO с данными для обновления
     * @return обновленный DTO профиля
     */
    ProfileResponseDto updateUserProfile(String username, ProfileUpdateRequestDto request);

    /**
     * Частично обновляет профиль текущего пользователя.
     * Обновляет только переданные поля.
     *
     * @param username имя пользователя
     * @param request DTO с частичными данными для обновления
     * @return обновленный DTO профиля
     */
    ProfileResponseDto patchCurrentUserProfile(String username, ProfilePatchRequestDto request);

    /**
     * Удаляет профиль пользователя по username.
     * Используется администраторами для удаления профилей.
     *
     * @param username имя пользователя для удаления
     */
    void deleteProfile(String username);
}
