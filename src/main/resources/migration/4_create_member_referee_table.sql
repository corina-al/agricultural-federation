create table if not exists "member_referee"
(
    id                 varchar primary key,
    member_refereed_id varchar references "member" (id),
    member_referee_id  varchar references "member" (id)
);