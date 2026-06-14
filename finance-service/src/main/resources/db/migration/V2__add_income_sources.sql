create table if not exists income_sources (
    id bigserial primary key,
    name varchar(80) not null,
    user_id bigint not null,
    group_id bigint references family_groups(id)
);

alter table finance_operations add column if not exists income_source_id bigint;

create index if not exists idx_income_sources_group on income_sources(group_id);
