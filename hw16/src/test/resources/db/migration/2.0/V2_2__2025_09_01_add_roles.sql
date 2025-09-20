create sequence role_seq start with 0 increment by 1 cache 100;

create table if not exists roles (
    id bigserial,
    role_name varchar(255) not null,
    created timestamp default current_timestamp,
    updated timestamp default current_timestamp,
    primary key (id)
);

create table if not exists users_roles (
    user_id bigint references users(id) on delete cascade,
    role_id bigint references roles(id) on delete cascade,
    created timestamp default current_timestamp,
    updated timestamp default current_timestamp,
    primary key (user_id, role_id)
);