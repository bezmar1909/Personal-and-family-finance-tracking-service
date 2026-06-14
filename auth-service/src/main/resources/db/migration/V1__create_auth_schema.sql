create table users (
    id bigserial primary key,
    email varchar(255) not null unique,
    password_hash varchar(255) not null,
    full_name varchar(120) not null,
    role varchar(32) not null,
    created_at timestamp with time zone not null
);
