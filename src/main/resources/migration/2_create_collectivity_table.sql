create table if not exists "collectivity"
(
    id                varchar primary key,
    name              varchar,
    number            integer,
    location          varchar,
    president_id      varchar references "member" (id),
    vice_president_id varchar references "member" (id),
    treasurer_id      varchar references "member" (id),
    secretary_id      varchar references "member" (id)
);