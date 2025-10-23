create sequence user_seq start with 0 increment by 1 cache 100;

create table if not exists users (
    id bigserial,
    username varchar(255) not null,
    password varchar(255) not null,
    is_active boolean,
    created timestamp default current_timestamp,
    updated timestamp default current_timestamp,
    primary key (id)
);