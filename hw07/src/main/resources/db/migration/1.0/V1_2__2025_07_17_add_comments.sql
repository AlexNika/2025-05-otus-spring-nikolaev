create sequence genre_seq start with 7 increment by 1 cache 100;
create sequence author_seq start with 4 increment by 1 cache 100;
create sequence comment_seq start with 7 increment by 1 cache 100;
create sequence book_seq start with 4 increment by 1 cache 100;

alter table authors add column created timestamp default current_timestamp;
alter table authors add column updated timestamp default current_timestamp;
alter table genres add column created timestamp default current_timestamp;
alter table genres add column updated timestamp default current_timestamp;
alter table books add column created timestamp default current_timestamp;
alter table books add column updated timestamp default current_timestamp;
alter table books_genres add column created timestamp default current_timestamp;
alter table books_genres add column updated timestamp default current_timestamp;

create table if not exists comments (
    id bigserial,
    text varchar(255) not null,
    book_id bigint not null,
    created timestamp default current_timestamp,
    updated timestamp default current_timestamp,
    foreign key (book_id) references books(id) on delete cascade,
    primary key (id)
);