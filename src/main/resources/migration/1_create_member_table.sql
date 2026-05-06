do
$$
    begin
        if not exists(select from pg_type where typname = 'gender') then
            create type gender as enum ('MALE', 'FEMALE');
        end if;
    end
$$;

do
$$
    begin
        if not exists(select from pg_type where typname = 'member_occupation') then
            create type member_occupation as enum ('JUNIOR', 'SENIOR', 'SECRETARY', 'TREASURER', 'VICE_PRESIDENT', 'PRESIDENT');
        end if;
    end
$$;

create table if not exists "member"
(
    id                    varchar primary key,
    first_name            varchar,
    last_name             varchar,
    birth_date            date,
    gender                gender,
    phone_number          varchar,
    email                 varchar,
    occupation            member_occupation,
    registration_fee_paid boolean,
    membership_dues_paid  boolean
);