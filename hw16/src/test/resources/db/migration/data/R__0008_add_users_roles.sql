-- Связываем пользователей с ролями
-- admin имеет все роли
insert into users_roles(user_id, role_id) values (1, 1); -- admin -> ADMIN
insert into users_roles(user_id, role_id) values (1, 2); -- admin -> AUTHOR
insert into users_roles(user_id, role_id) values (1, 3); -- admin -> LIBRARIAN
insert into users_roles(user_id, role_id) values (1, 4); -- admin -> READER

-- author имеет базовые роли для создания контента
insert into users_roles(user_id, role_id) values (2, 2); -- Roman Zlotnikov -> AUTHOR
insert into users_roles(user_id, role_id) values (3, 2); -- Julian Semenov -> AUTHOR
insert into users_roles(user_id, role_id) values (4, 2); -- Alexandre Dumas -> AUTHOR
insert into users_roles(user_id, role_id) values (5, 2); -- Theodore Dreiser -> AUTHOR
insert into users_roles(user_id, role_id) values (6, 2); -- Leo Tolstoy -> AUTHOR
insert into users_roles(user_id, role_id) values (2, 4); -- Roman Zlotnikov -> READER
insert into users_roles(user_id, role_id) values (3, 4); -- Julian Semenov -> READER
insert into users_roles(user_id, role_id) values (4, 4); -- Alexandre Dumas -> READER
insert into users_roles(user_id, role_id) values (5, 4); -- Theodore Dreiser -> READER
insert into users_roles(user_id, role_id) values (6, 4); -- Leo Tolstoy -> READER

-- librarian управляет библиотекой
insert into users_roles(user_id, role_id) values (7, 3); -- librarian -> LIBRARIAN
insert into users_roles(user_id, role_id) values (8, 3); -- librarian -> LIBRARIAN
insert into users_roles(user_id, role_id) values (7, 4); -- librarian -> READER
insert into users_roles(user_id, role_id) values (8, 4); -- librarian -> READER

-- reader только читает
insert into users_roles(user_id, role_id) values (9, 4); -- reader -> READER
insert into users_roles(user_id, role_id) values (10, 4); -- reader -> READER
insert into users_roles(user_id, role_id) values (11, 4); -- reader -> READER