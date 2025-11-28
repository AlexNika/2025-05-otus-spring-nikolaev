package ru.pricat.service;

import reactor.core.publisher.Mono;
import ru.pricat.model.User;
import ru.pricat.model.dto.request.AdminChangePasswordRequestDto;
import ru.pricat.model.dto.request.ChangePasswordRequestDto;
import ru.pricat.model.dto.response.LoginResponseDto;
import ru.pricat.model.dto.request.RegisterRequestDto;
import ru.pricat.model.dto.UserProfileDto;

/**
 * Интерфейс сервиса аутентификации и управления пользователями.
 * Определяет методы для логина, регистрации, обновления токенов, управления ролями и профилями пользователей.
 */
@SuppressWarnings("unused")
public interface AuthService {

    /**
     * Аутентифицирует пользователя по имени пользователя и паролю.
     * Проверяет, не заблокирован ли пользователь, существует ли он, включена ли его учетная запись,
     * и корректен ли пароль. При успешной аутентификации генерирует новые access и refresh токены.
     *
     * @param username имя пользователя
     * @param password пароль пользователя
     * @return реактивный объект с DTO, содержащим access и refresh токены
     * @throws ru.pricat.exception.InvalidCredentialsException если пользователь не найден, пароль неверен или учетная запись заблокирована
     * @throws ru.pricat.exception.UserDisabledException       если учетная запись пользователя отключена
     */
    Mono<LoginResponseDto> authenticate(String username, String password);

    /**
     * Регистрирует нового пользователя.
     * Проверяет уникальность email и username. Сохраняет пользователя в базе данных,
     * создает профиль в client-service (с откатом при ошибке), и присваивает ему роль 'USER'.
     *
     * @param registerRequestDto DTO с данными для регистрации (имя пользователя, email, пароль)
     * @return реактивный объект с созданным пользователем
     * @throws ru.pricat.exception.EmailNotUniqueException    если email уже используется другим пользователем
     * @throws ru.pricat.exception.UserAlreadyExistsException если имя пользователя уже занято
     */
    Mono<User> register(RegisterRequestDto registerRequestDto);

    /**
     * Обновляет пару access/refresh токенов на основе существующего refresh-токена.
     * Проверяет, действителен ли переданный refresh-токен и не истек ли срок его действия.
     * При успешной проверке генерирует новый refresh-токен и новый access-токен.
     *
     * @param refreshToken текущий refresh-токен пользователя
     * @return реактивный объект с DTO, содержащим новые access и refresh токены
     * @throws ru.pricat.exception.InvalidCredentialsException если токен недействителен или срок его действия истек
     */
    Mono<LoginResponseDto> refreshToken(String refreshToken);

    /**
     * Получает пользователя по его имени пользователя.
     * Не включает роли в возвращаемом объекте.
     *
     * @param username имя пользователя
     * @return реактивный объект с найденным пользователем
     * @throws ru.pricat.exception.UserNotFoundException если пользователь не найден
     */
    Mono<User> getUserByUsername(String username);

    /**
     * Удаляет пользователя по его имени пользователя.
     * Также удаляет все связанные с ним refresh-токены.
     *
     * @param username имя пользователя для удаления
     * @return реактивный объект, сигнализирующий о завершении операции
     * @throws ru.pricat.exception.UserNotFoundException если пользователь не найден
     */
    Mono<Void> deleteUser(String username);

    /**
     * Обновляет статус включения/отключения учетной записи пользователя.
     *
     * @param username имя пользователя
     * @param enabled  новый статус включения
     * @return реактивный объект с обновленным пользователем
     * @throws ru.pricat.exception.UserNotFoundException если пользователь не найден
     */
    Mono<User> updateUserEnabledStatus(String username, boolean enabled);

    /**
     * Получает профиль текущего пользователя (включая роли).
     *
     * @param username имя текущего пользователя
     * @return реактивный объект с DTO профиля пользователя
     * @throws ru.pricat.exception.UserNotFoundException если пользователь не найден
     */
    Mono<UserProfileDto> getCurrentUserProfile(String username);

    /**
     * Получает профиль пользователя по его имени пользователя (включая роли).
     * Может использоваться администратором или для просмотра профиля другого пользователя.
     *
     * @param username имя пользователя, чей профиль нужно получить
     * @return реактивный объект с DTO профиля пользователя
     * @throws ru.pricat.exception.UserNotFoundException если пользователь не найден
     */
    Mono<UserProfileDto> getUserProfileByUsername(String username);

    /**
     * Добавляет роль пользователю.
     * Если пользователь уже имеет эту роль, операция игнорируется.
     *
     * @param username имя пользователя
     * @param roleName название роли для добавления
     * @return реактивный объект, сигнализирующий о завершении операции
     * @throws ru.pricat.exception.UserNotFoundException если пользователь не найден
     * @throws ru.pricat.exception.RoleNotFoundException если роль не найдена
     */
    Mono<Void> addRoleToUser(String username, String roleName);

    /**
     * Удаляет роль у пользователя.
     * Предотвращает удаление последней роли пользователя.
     *
     * @param username имя пользователя
     * @param roleName название роли для удаления
     * @return реактивный объект, сигнализирующий о завершении операции
     * @throws ru.pricat.exception.UserNotFoundException        если пользователь не найден
     * @throws ru.pricat.exception.RoleNotFoundException        если роль не найдена
     * @throws ru.pricat.exception.LastRoleRemovalException     если попытка удалить последнюю роль пользователя
     */
    Mono<Void> removeRoleFromUser(String username, String roleName);

    /**
     * Изменяет пароль текущего пользователя.
     * Проверяет, совпадает ли старый пароль, и не совпадает ли новый пароль со старым.
     *
     * @param username                    имя текущего пользователя
     * @param changePasswordRequestDto DTO с текущим и новым паролем
     * @return реактивный объект, сигнализирующий о завершении операции
     * @throws ru.pricat.exception.UserNotFoundException       если пользователь не найден
     * @throws ru.pricat.exception.InvalidCredentialsException если старый пароль неверен
     */
    Mono<Void> changeCurrentUserPassword(String username, ChangePasswordRequestDto changePasswordRequestDto);

    /**
     * Изменяет пароль пользователя по инициативе администратора.
     * Инвалидирует все refresh-токены пользователя после смены пароля.
     *
     * @param targetUsername                  имя пользователя, пароль которого нужно изменить
     * @param adminChangePasswordRequestDto DTO с новым паролем
     * @return реактивный объект, сигнализирующий о завершении операции
     * @throws ru.pricat.exception.UserNotFoundException если пользователь не найден
     */
    Mono<Void> changeUserPasswordByAdmin(String targetUsername,
                                         AdminChangePasswordRequestDto adminChangePasswordRequestDto);
}
