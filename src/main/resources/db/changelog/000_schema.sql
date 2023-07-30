create type user_genders as enum ('MALE','FEMALE');

create table users (
ID     serial primary key,
EMAIL       varchar (128) not null,
FULL_NAME   varchar (256) not null,
AGE         int not null,
GENDER      user_genders not null,
CREATED_AT  timestamp with time zone default now()
);

alter table users add constraint unique_email unique (email);
