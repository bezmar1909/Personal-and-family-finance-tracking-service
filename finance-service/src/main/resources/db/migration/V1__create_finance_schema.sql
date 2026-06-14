create table family_groups (
    id bigserial primary key,
    name varchar(120) not null,
    owner_user_id bigint not null,
    created_at timestamp with time zone not null
);

create table group_members (
    id bigserial primary key,
    group_id bigint not null references family_groups(id),
    user_id bigint not null,
    role varchar(32) not null,
    constraint uk_group_members_group_user unique (group_id, user_id)
);

create table categories (
    id bigserial primary key,
    name varchar(80) not null,
    type varchar(32) not null,
    user_id bigint not null,
    group_id bigint references family_groups(id)
);

create table income_sources (
    id bigserial primary key,
    name varchar(80) not null,
    user_id bigint not null,
    group_id bigint references family_groups(id)
);

create table finance_operations (
    id bigserial primary key,
    amount numeric(19, 2) not null,
    operation_date date not null,
    type varchar(32) not null,
    description varchar(300) not null,
    user_id bigint not null,
    category_id bigint not null references categories(id),
    income_source_id bigint references income_sources(id),
    group_id bigint references family_groups(id),
    created_at timestamp with time zone not null
);

create index idx_finance_operations_period on finance_operations(operation_date);
create index idx_finance_operations_group_period on finance_operations(group_id, operation_date);
