package ru.pricat.service;

import ru.pricat.exception.AuthServiceException;
import ru.pricat.model.dto.auth.LoginRequestDto;
import ru.pricat.model.dto.auth.LoginResponseDto;
import ru.pricat.model.dto.auth.RegisterRequestDto;
import ru.pricat.model.dto.auth.UserResponseDto;
import ru.pricat.model.dto.request.ChangePasswordRequestDto;

/**
 * Клиент для взаимодействия с микросервисом аутентификации (auth-service).
 * Предоставляет методы для операций входа, выхода, регистрации и получения данных пользователя.
 */
public interface AuthService {

    /**
     * Выполняет аутентификацию пользователя.
     *
     * @param request данные для входа (username и password)
     * @return ответ с access и refresh токенами
     * @throws AuthServiceException в случае ошибки аутентификации
     */
    LoginResponseDto login(LoginRequestDto request);

    /**
     * Выполняет выход пользователя (инвалидация токена).
     *
     * @param jwtToken JWT токен для инвалидации
     * @throws AuthServiceException в случае ошибки при выходе
     */
    void logout(String jwtToken);

    /**
     * Регистрирует нового пользователя в системе.
     *
     * @param request данные для регистрации
     * @throws AuthServiceException в случае ошибки регистрации
     */
    void register(RegisterRequestDto request);

    /**
     * Получает информацию о текущем аутентифицированном пользователе.
     *
     * @param jwtToken JWT токен пользователя
     * @return информация о пользователе
     * @throws AuthServiceException в случае ошибки получения данных
     */
    UserResponseDto getMe(String jwtToken);

    /**
     * Получает информацию о пользователе по username.
     *
     * @param username имя пользователя
     * @param jwtToken JWT токен для аутентификации
     * @return информация о пользователе
     * @throws AuthServiceException в случае ошибки получения данных
     */
    UserResponseDto getUser(String username, String jwtToken);

    /**
     * Обновляет access токен с помощью refresh токена.
     *
     * @param refreshToken refresh токен
     * @return новый набор токенов (access и refresh)
     * @throws AuthServiceException в случае ошибки обновления токена
     */
    LoginResponseDto refresh(String refreshToken);

    /**
     * Удаляет пользователя по username в auth-service.
     *
     * @param username имя пользователя
     * @param jwtToken JWT токен для аутентификации
     * @throws AuthServiceException в случае ошибки удаления
     */
    void deleteUser(String username, String jwtToken);

    /**
     * Включает пользователя по username в auth-service.
     *
     * @param username имя пользователя
     * @param jwtToken JWT токен администратора
     */
    void enableUser(String username, String jwtToken);

    /**
     * Отключает пользователя по username в auth-service.
     *
     * @param username имя пользователя
     * @param jwtToken JWT токен администратора
     */
    void disableUser(String username, String jwtToken);

    /**
     * Добавляет роль пользователю в auth-service.
     *
     * @param username имя пользователя
     * @param role роль для добавления
     * @param jwtToken JWT токен администратора
     */
    void addRole(String username, String role, String jwtToken);

    /**
     * Убирает роль у пользователя в auth-service.
     *
     * @param username имя пользователя
     * @param role роль для удаления
     * @param jwtToken JWT токен администратора
     */
    void removeRole(String username, String role, String jwtToken);

    /**
     * Меняет пароль текущего пользователя в auth-service.
     *
     * @param changePasswordRequestDto DTO с текущим и новым паролем
     * @param jwtToken JWT токен пользователя
     */
    void changePassword(ChangePasswordRequestDto changePasswordRequestDto, String jwtToken);

    /**
     * Меняет пароль другого пользователя от имени администратора в auth-service.
     *
     * @param username имя пользователя, чей пароль меняется
     * @param newPassword новый пароль
     * @param jwtToken JWT токен администратора
     */
    void changeUserPasswordByAdmin(String username, String newPassword, String jwtToken);
}
